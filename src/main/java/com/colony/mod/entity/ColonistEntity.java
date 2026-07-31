package com.colony.mod.entity;

import com.colony.mod.ColonyConfig;
import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.ai.goap.GOAPAction;
import com.colony.mod.entity.ai.goap.GOAPPlanner;
import com.colony.mod.entity.ai.goals.*;
import com.colony.mod.entity.needs.NeedsComponent;
import com.colony.mod.entity.needs.NeedType;
import com.colony.mod.entity.schedule.DailySchedule;
import com.colony.mod.entity.schedule.SchedulePhase;
import com.colony.mod.network.ColonistInspectPacket;
import com.colony.mod.performance.ColonyAIExecutor;
import com.colony.mod.social.RelationshipData;
import com.colony.mod.social.SocialNetwork;
import com.colony.mod.town.JobRole;
import com.colony.mod.town.TownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The main NPC entity of the Colony mod — an autonomous citizen.
 *
 * <p>Each colonist:
 * <ul>
 *   <li>Tracks continuous {@link NeedsComponent} stats (Hunger, Energy, Social, Safety)</li>
 *   <li>Uses a {@link UtilityAI} each evaluation cycle to determine the highest-priority
 *       {@link GOAPGoal}</li>
 *   <li>Feeds that goal to the {@link GOAPPlanner} which produces an ordered action plan —
 *       computed asynchronously on the {@link ColonyAIExecutor} background thread</li>
 *   <li>Executes the cached plan step-by-step on the main thread</li>
 *   <li>Follows a {@link DailySchedule} that biases goal weights by time of day</li>
 * </ul>
 */
public class ColonistEntity extends PathfinderMob {

    // -------------------------------------------------------------------------
    // Synced data (visible to client for rendering/HUD)
    // -------------------------------------------------------------------------

    private static final EntityDataAccessor<String> DATA_JOB_ROLE =
            SynchedEntityData.defineId(ColonistEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_COLONIST_NAME =
            SynchedEntityData.defineId(ColonistEntity.class, EntityDataSerializers.STRING);

    // -------------------------------------------------------------------------
    // AI systems
    // -------------------------------------------------------------------------

    private final NeedsComponent needs = new NeedsComponent();
    private final DailySchedule schedule = new DailySchedule();

    /** All goals this colonist can pursue, evaluated each AI cycle. */
    private final List<GOAPGoal> goals = new ArrayList<>();

    /** All atomic GOAP actions available to this colonist. */
    private final List<GOAPAction> availableActions = new ArrayList<>();

    /** Current active plan consumed on the main thread. */
    private final Deque<GOAPAction> currentPlan = new ArrayDeque<>();

    /** The goal currently being pursued. */
    private GOAPGoal activeGoal;

    // -------------------------------------------------------------------------
    // Async AI planning
    // -------------------------------------------------------------------------

    /**
     * Pending plan computed asynchronously. The main thread swaps this in when not null
     * and then clears it.
     */
    private final AtomicReference<List<GOAPAction>> pendingPlanResult = new AtomicReference<>(null);

    /** Future tracking the in-flight planning task (null when idle). */
    private Future<?> planningFuture = null;

    /**
     * Snapshot of need values at the time planning was last submitted.
     * Used to detect significant changes (> threshold) that warrant a replan.
     */
    private float[] lastPlannedNeedValues = new float[NeedType.values().length];

    /** Whether an initial plan has never been computed. */
    private boolean firstPlan = true;

    // -------------------------------------------------------------------------
    // Colony membership
    // -------------------------------------------------------------------------

    private JobRole jobRole = JobRole.UNEMPLOYED;
    private UUID homePos = null; // UUID placeholder; real impl stores BlockPos

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public ColonistEntity(EntityType<? extends ColonistEntity> type, Level level) {
        super(type, level);
        registerGoals();
    }

    /**
     * Defines the NeoForge attribute defaults for colonists.
     * Called by the entity type builder during registration.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    // -------------------------------------------------------------------------
    // Goal / action registration
    // -------------------------------------------------------------------------

    private void registerGoals() {
        goals.add(new SleepGoal());
        goals.add(new EatGoal());
        goals.add(new SocializeGoal());
        goals.add(new SeekSafetyGoal());
        goals.add(new WorkGoal());
    }

    // -------------------------------------------------------------------------
    // Data parameter definition
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_JOB_ROLE, JobRole.UNEMPLOYED.name());
        builder.define(DATA_COLONIST_NAME, "Colonist");
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            needs.tick();
            tickAI();
        }
    }

    /**
     * Runs one tick of the colonist's AI execution loop.
     *
     * <ol>
     *   <li>Checks if a newly computed async plan is ready and swaps it in.</li>
     *   <li>If needs have changed beyond the replan threshold (or this is the first tick),
     *       submits a new async planning task.</li>
     *   <li>Executes the next step of the current plan.</li>
     * </ol>
     */
    private void tickAI() {
        ActionContext ctx = new ActionContext(this, level());

        // --- Swap in a newly computed async plan ---
        List<GOAPAction> incoming = pendingPlanResult.getAndSet(null);
        if (incoming != null) {
            currentPlan.clear();
            currentPlan.addAll(incoming);
            recordNeedSnapshot();
        }

        // --- Submit async replan if dirty ---
        if (shouldReplan()) {
            submitAsyncPlan(ctx);
        }

        // --- Execute current plan ---
        if (!currentPlan.isEmpty()) {
            GOAPAction action = currentPlan.peek();
            if (action.checkProceduralPrecondition(ctx)) {
                boolean done = action.perform(ctx);
                if (done) currentPlan.poll();
            } else {
                action.reset(ctx);
                currentPlan.clear();
                activeGoal = null;
                firstPlan = true; // trigger replan immediately
            }
        }
    }

    /**
     * Returns {@code true} if the AI should be replanned:
     * <ul>
     *   <li>First-ever plan has not been computed yet.</li>
     *   <li>No in-flight planning task and the current plan is exhausted.</li>
     *   <li>A need value has changed by more than {@link ColonyConfig#getAiReplanNeedThreshold()}
     *       points since the last plan.</li>
     * </ul>
     */
    private boolean shouldReplan() {
        if (planningFuture != null && !planningFuture.isDone()) return false; // already planning
        if (firstPlan) return true;
        if (currentPlan.isEmpty() && activeGoal != null) return true;
        return needsChangedSignificantly();
    }

    private boolean needsChangedSignificantly() {
        int threshold = ColonyConfig.getAiReplanNeedThreshold();
        NeedType[] types = NeedType.values();
        for (int i = 0; i < types.length; i++) {
            if (Math.abs(needs.getValue(types[i]) - lastPlannedNeedValues[i]) > threshold) {
                return true;
            }
        }
        return false;
    }

    private void recordNeedSnapshot() {
        NeedType[] types = NeedType.values();
        for (int i = 0; i < types.length; i++) {
            lastPlannedNeedValues[i] = needs.getValue(types[i]);
        }
    }

    /**
     * Submits an async AI planning task to {@link ColonyAIExecutor}.
     * The result will be picked up on the next main-thread tick via {@link #pendingPlanResult}.
     */
    private void submitAsyncPlan(ActionContext ctx) {
        firstPlan = false;

        // Capture immutable snapshots for use on the background thread
        final List<GOAPGoal> goalsCopy = new ArrayList<>(goals);
        final List<GOAPAction> actionsCopy = new ArrayList<>(availableActions);
        final Map<String, Object> worldState = buildCurrentWorldState();
        final SchedulePhase phase = schedule.getPhase(level().getDayTime());

        planningFuture = ColonyAIExecutor.getInstance().submit(() -> {
            GOAPGoal best = goalsCopy.stream()
                    .max(Comparator.comparingDouble(g -> g.getPriority(ctx)))
                    .orElse(null);

            if (best == null) return null;

            // Store active goal reference (written on AI thread, read on main thread — ok since it's
            // a reference assignment which is atomic on all JVMs)
            activeGoal = best;

            List<GOAPAction> plan = GOAPPlanner.plan(best, actionsCopy, worldState);
            pendingPlanResult.set(plan.isEmpty() ? Collections.emptyList() : plan);
            return null;
        });
    }

    /**
     * Assembles the current world state as a string→Object map for the GOAP planner.
     * Values reflect what the colonist knows right now.
     */
    private Map<String, Object> buildCurrentWorldState() {
        Map<String, Object> state = new HashMap<>();
        state.put("colonist_sleeping", false);
        state.put("colonist_fed", needs.getValue(NeedType.HUNGER) > 60f);
        state.put("colonist_socialised", needs.getValue(NeedType.SOCIAL) > 50f);
        state.put("colonist_safe", needs.getValue(NeedType.SAFETY) > 40f);
        state.put("colonist_worked", false);
        state.put("has_bed", false);       // updated by smart-object scanner
        state.put("food_available", false); // updated by smart-object scanner
        return state;
    }

    // -------------------------------------------------------------------------
    // Player interaction
    // -------------------------------------------------------------------------

    /**
     * When a player right-clicks this colonist, send an inspection packet to the client
     * that triggers the {@link com.colony.mod.client.ColonistInspectHud} overlay.
     */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            buildAndSendInspectPacket(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void buildAndSendInspectPacket(ServerPlayer player) {
        String goalName = activeGoal != null ? activeGoal.getName() : "Idle";

        // Top-3 relationships from the social network
        List<ColonistInspectPacket.RelationshipEntry> relEntries = new ArrayList<>();
        TownManager manager = TownManager.get(player.serverLevel());
        if (manager != null) {
            SocialNetwork sn = manager.getTownData().getSocialNetwork();
            List<RelationshipData> topRels = sn.getTopRelationships(getUUID(), 3);
            for (RelationshipData rel : topRels) {
                UUID otherId = rel.getColonistA().equals(getUUID()) ? rel.getColonistB() : rel.getColonistA();
                // Look up the other colonist by UUID; fall back to short UUID string
                net.minecraft.world.entity.Entity other = player.serverLevel().getEntity(otherId);
                String otherName = other != null ? other.getName().getString() : otherId.toString().substring(0, 8);
                relEntries.add(new ColonistInspectPacket.RelationshipEntry(otherName, rel.getAffinity()));
            }
        }

        ColonistInspectPacket packet = new ColonistInspectPacket(
                goalName,
                needs.getValue(NeedType.HUNGER),
                needs.getValue(NeedType.ENERGY),
                needs.getValue(NeedType.SOCIAL),
                needs.getValue(NeedType.SAFETY),
                relEntries
        );
        PacketDistributor.sendToPlayer(player, packet);
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("needs", needs.save());
        tag.putString("jobRole", jobRole.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("needs")) needs.load(tag.getCompound("needs"));
        if (tag.contains("jobRole")) {
            try {
                jobRole = JobRole.valueOf(tag.getString("jobRole"));
            } catch (IllegalArgumentException e) {
                jobRole = JobRole.UNEMPLOYED;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public NeedsComponent getNeeds() { return needs; }
    public DailySchedule getSchedule() { return schedule; }
    public JobRole getJobRole() { return jobRole; }

    public void setJobRole(JobRole role) {
        this.jobRole = role;
        getEntityData().set(DATA_JOB_ROLE, role.name());
    }

    public GOAPGoal getActiveGoal() { return activeGoal; }
}
