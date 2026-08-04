# Colony Mod

A self-governing autonomous colony simulation mod for Minecraft (Fabric 1.21.1).

## Concept

In standard colony-sim mods the player acts as an "invisible god," placing supply camps and assigning jobs. **Colony** flips that dynamic: the town operates on emergent AI where **villagers manage their own needs, choose their own work, and expand the settlement when conditions demand it**.

---

## Features

- **Utility AI + GOAP planning** for colonist decision-making
- **Smart objects** like beds, ovens, guard posts, and social spaces
- **Autonomous town expansion** based on housing, food, and defence
- **Daily schedules** that drive work, rest, and social behaviour
- **Social network simulation** with affinity between colonists

---

## Architecture

### 1. AI Engine — Utility AI + GOAP

Each colonist continuously tracks four internal stats (needs):

| Need    | Decays when…               | Satisfied by…                     |
|---------|----------------------------|-----------------------------------|
| Hunger  | Always (slow)              | Eating cooked food                |
| Energy  | During activity            | Sleeping in a bed                 |
| Social  | During solitary activity   | Chatting, music, socialising      |
| Safety  | Near mobs / when homeless  | Guard post, shelter, safety       |

**Utility AI** scores every possible action dynamically each cycle using a logistic curve:

```
utilityScore = logistic(deficitFraction) × weight × 100
```

The highest-scoring goal is then fed to the **GOAP Planner** which backward-chains the cheapest sequence of atomic actions to reach the goal state.

Example: `Eat Meal ← Cook ← Gather Ingredients ← Go to Storage`

### 2. Smart Objects

Blocks and entities advertise their utility to nearby colonists instead of having each interaction hard-coded in the NPC brain.

| Smart Object   | Need satisfied | Satisfaction |
|----------------|---------------|-------------|
| Bed            | Energy        | +80         |
| Campfire       | Hunger        | +50         |
| Oven           | Hunger        | +60         |
| Jukebox        | Social        | +30         |
| Social Seat    | Social        | +25         |
| Notice Board   | Social        | +40         |
| Guard Post     | Safety        | +50         |
| Home           | Safety        | +20 (passive)|

### 3. Autonomous Town Expansion

The **Colony State Monitor** runs every 10 seconds and checks colony health:

```
[ Colony State Monitor ]
         │
         ├── Population >= Housing Capacity? ──> Build Small House
         ├── Food Storage < 30?              ──> Assign Farmer / Build Farm
         └── Defence Level == 0?             ──> Build Guard Post
```

Colonist builders claim `BuilderTask`s, pathfind to the town storage, gather materials, and place blocks step-by-step from pre-made NBT templates.

### 4. Daily Schedule

Colonists follow a time-based routine:

| Phase          | In-game time  | Activity                         |
|----------------|---------------|----------------------------------|
| Wake Up        | 06:00–06:50   | Leave bed, eat breakfast         |
| Morning Work   | 06:50–12:00   | Primary job                      |
| Lunch          | 12:00–13:00   | Eat, brief rest                  |
| Afternoon Work | 13:00–17:00   | Secondary tasks                  |
| Free Time      | 17:00–19:00   | Town square, socialising         |
| Sleep          | 19:00–06:00   | Sleep in assigned bed            |

### 5. Social Network

Pairwise affinity values (−100 to +100) between every colonist pair.  
- Positive interactions (chatting, proximity) increase affinity.  
- When affinity ≥ 70, the Town Planner builds a shared home.  
- When affinity ≤ −50, colonists actively avoid each other.

---

## Technical Stack

| Component       | Choice                         |
|-----------------|-------------------------------|
| Mod Loader      | Fabric 1.21.1                 |
| Java Version    | Java 21                       |
| Build System    | Gradle 8 + Fabric Loom        |
| Pathfinding     | Extension of `PathNavigation` |
| Structure API   | `StructureTemplate` (NBT)     |
| Animations      | GeckoLib / AzureLib (planned) |

---

## Project Structure

```
src/main/java/com/colony/mod/
├── ColonyMod.java                      # Mod entry point (ModInitializer)
├── ColonyClientMod.java                # Client entry point (ClientModInitializer)
├── ColonyConfig.java                   # JSON config loader
├── registry/                           # Fabric Registry registrations
│   ├── ColonyEntityTypes.java
│   ├── ColonyBlocks.java
│   └── ColonyItems.java
├── entity/
│   ├── ColonistEntity.java             # Main NPC entity
│   ├── needs/
│   │   ├── NeedType.java               # Enum of need categories
│   │   ├── Need.java                   # Single need stat (0–100)
│   │   └── NeedsComponent.java         # All needs for one colonist
│   ├── ai/
│   │   ├── UtilityAI.java              # Dynamic action scoring engine
│   │   ├── UtilityAction.java          # Action contract
│   │   ├── ActionContext.java          # Execution context
│   │   └── goap/
│   │       ├── GOAPAction.java         # Atomic plan step
│   │       ├── GOAPGoal.java           # Desired world state
│   │       └── GOAPPlanner.java        # Backward-chaining A* planner
│   ├── goals/                          # Concrete GOAP goals
│   │   ├── SleepGoal.java
│   │   ├── EatGoal.java
│   │   ├── SocializeGoal.java
│   │   ├── SeekSafetyGoal.java
│   │   └── WorkGoal.java
│   └── schedule/
│       ├── SchedulePhase.java          # Enum of daily phases
│       └── DailySchedule.java          # Day-time → phase mapping
├── smartobject/
│   ├── SmartObjectType.java            # Enum of smart-object types
│   ├── SmartObject.java                # Live instance at a block pos
│   └── SmartObjectRegistry.java        # Per-level registry
├── town/
│   ├── JobRole.java                    # Colonist job enum
│   ├── TownData.java                   # Persistent colony state
│   ├── TownManager.java                # Server-side tick driver
│   ├── ColonyStateMonitor.java         # Macro-level expansion logic
│   └── builder/
│       ├── StructureBlueprintType.java # Blueprint enum (NBT templates)
│       └── BuilderTask.java            # Single construction task
└── social/
    ├── RelationshipData.java           # Pairwise affinity record
    └── SocialNetwork.java              # Colony-wide relationship graph
```

---

## Development Roadmap

- [x] **Phase 1** — Sim Brain: Needs system, Utility AI, GOAP planner, daily schedule
- [x] **Phase 2** — Smart Objects: block utility advertisement, colonist search & pathfind
- [x] **Phase 3** — Autonomous Construction: builder tasks, NBT structure templates
- [x] **Phase 4** — Town Logic: Colony State Monitor, demographic-driven expansion
- [x] **Phase 5** — Player Systems: employment & wages, housing market, laws, taxes & crime enforcement
- [x] **Phase 6** — Performance: async AI executor, abstract tickless simulation for unloaded colonies
- [x] **Phase 7** — UI: colonist inspector HUD overlay, Town Ledger block & screen
- [x] **Phase 8** — Cross-mod API: `ColonySmartObjectAPI`, `SmartObjectDefinition`, JSON config

---

## Building

```bash
./gradlew build
```

Requires Java 21 and an internet connection to download Fabric dependencies.

## Installation

Drop the built JAR (from `build/libs/`) into the `mods/` folder of a Fabric 1.21.1 profile alongside **Fabric API**.
