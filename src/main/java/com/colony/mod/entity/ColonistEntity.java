package com.colony.mod.entity;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.UtilityAI;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.ai.goap.GOAPAction;
import com.colony.mod.entity.ai.goap.GOAPPlanner;
import com.colony.mod.entity.ai.goals.*;
import com.colony.mod.entity.needs.NeedsComponent;
import com.colony.mod.entity.needs.NeedType;
import com.colony.mod.entity.schedule.DailySchedule;
import com.colony.mod.entity.schedule.SchedulePhase;
import com.colony.mod.town.JobRole;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * The main NPC entity of the Colony mod — an autonomous citizen.
 *
 * <p>Each colonist:
 * <ul>
 *   <li>Tracks continuous {@link NeedsComponent} stats (Hunger, Energy, Social, Safety)</li>
 *   <li>Uses a {@link UtilityAI} each evaluation cycle to determine the highest-priority
 *       {@link GOAPGoal}</li>
 *   <li>Feeds that goal to the {@link GOAPPlanner} which produces an ordered action plan</li>
 *   <li>Executes the plan step-by-step, with each action completing over one or more ticks</li>
 *   <li>Follows a {@link DailySchedule} that biases goal weights by time of day</li>
 * </ul>
 *
 * <p>The colonist does NOT need player input to function. It self-manages survival, social
 * interaction, job performance, and (via the Town Planner) contributes to colony expansion.
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

    /** Current active plan produced by the GOAP planner. */
    private final Deque<GOAPAction> currentPlan = new ArrayDeque<>();

    /** The goal currently being pursued. */
    private GOAPGoal activeGoal;

    /** How often (in ticks) to re-evaluate goals. */
    private static final int AI_EVAL_INTERVAL = 40; // every 2 seconds
    private int aiEvalCountdown = 0;

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
     * Runs one tick of the colonist's Utility AI + GOAP execution loop.
     *
     * <ol>
     *   <li>Every {@link #AI_EVAL_INTERVAL} ticks, re-score all goals and (re-)plan if the
     *       highest-priority goal changed.</li>
     *   <li>Each tick, attempt to execute the next step in the current plan.</li>
     * </ol>
     */
    private void tickAI() {
        ActionContext ctx = new ActionContext(this, level());
        SchedulePhase phase = schedule.getPhase(level().getDayTime());

        aiEvalCountdown--;
        if (aiEvalCountdown <= 0) {
            aiEvalCountdown = AI_EVAL_INTERVAL;
            replanIfNeeded(ctx, phase);
        }

        // Execute current plan
        if (!currentPlan.isEmpty()) {
            GOAPAction action = currentPlan.peek();
            if (action.checkProceduralPrecondition(ctx)) {
                boolean done = action.perform(ctx);
                if (done) {
                    currentPlan.poll();
                }
            } else {
                // Precondition failed mid-execution — replan next cycle
                action.reset(ctx);
                currentPlan.clear();
                activeGoal = null;
            }
        }
    }

    /**
     * Selects the highest-priority goal and, if it differs from the active goal,
     * re-runs the GOAP planner to produce a new action plan.
     */
    private void replanIfNeeded(ActionContext ctx, SchedulePhase phase) {
        GOAPGoal best = goals.stream()
                .max(Comparator.comparingDouble(g -> g.getPriority(ctx)))
                .orElse(null);

        if (best == null) return;

        if (best != activeGoal) {
            // Abort current plan
            if (!currentPlan.isEmpty()) {
                currentPlan.peek().reset(ctx);
                currentPlan.clear();
            }
            activeGoal = best;

            // Build new plan
            List<GOAPAction> plan = GOAPPlanner.plan(
                    activeGoal, availableActions, buildCurrentWorldState());
            currentPlan.addAll(plan);
        }
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
