package com.colony.mod.entity.ai.goap;

import java.util.*;

/**
 * GOAP (Goal-Oriented Action Planning) planner.
 *
 * <p>Given a goal's desired world state, a set of available {@link GOAPAction}s, and the
 * colonist's current world state, this planner performs a backward-chaining A* search to find
 * the cheapest sequence of actions that satisfies the goal.
 *
 * <p>Algorithm overview:
 * <ol>
 *   <li>Start from the desired goal state.</li>
 *   <li>Find actions whose effects satisfy at least one unsatisfied goal condition.</li>
 *   <li>Apply the action's preconditions as new unsatisfied conditions.</li>
 *   <li>Repeat until all conditions are satisfied by the current world state (leaf node).</li>
 *   <li>Return the action sequence with the lowest total cost.</li>
 * </ol>
 *
 * <p>This is a simplified but fully functional GOAP planner suitable for real-time NPC use.
 */
public final class GOAPPlanner {

    private GOAPPlanner() {}

    /**
     * Attempts to build a plan (ordered list of actions) that transitions the colonist from
     * {@code currentState} to the desired state defined by {@code goal}.
     *
     * @param goal         the goal to satisfy
     * @param availActions all actions the colonist can potentially perform
     * @param currentState the current world state as key/value pairs
     * @return an ordered list of actions (first = next to execute), or an empty list if no plan
     *         was found
     */
    public static List<GOAPAction> plan(
            GOAPGoal goal,
            List<GOAPAction> availActions,
            Map<String, Object> currentState) {

        // Start node: desired state is unsatisfied
        Node startNode = new Node(null, null, 0f, new HashMap<>(goal.getDesiredState()));

        // Priority queue ordered by cumulative cost (A* without heuristic = Dijkstra)
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        open.add(startNode);

        Node cheapestLeaf = null;

        while (!open.isEmpty()) {
            Node current = open.poll();

            // If all unsatisfied conditions are met by the current world state → plan found
            if (allSatisfied(current.unsatisfied, currentState)) {
                if (cheapestLeaf == null || current.cost < cheapestLeaf.cost) {
                    cheapestLeaf = current;
                }
                continue; // continue searching for cheaper alternatives
            }

            // Try every action whose effects satisfy at least one unsatisfied condition
            for (GOAPAction action : availActions) {
                if (!contributes(action, current.unsatisfied)) continue;

                // Build new unsatisfied set: remove what this action satisfies, add its preconditions
                Map<String, Object> newUnsatisfied = applyAction(action, current.unsatisfied);
                Node next = new Node(current, action, current.cost + action.getCost(), newUnsatisfied);
                open.add(next);
            }
        }

        if (cheapestLeaf == null) return Collections.emptyList();
        return buildPlan(cheapestLeaf);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns true if all entries in {@code required} are matched in {@code worldState}. */
    private static boolean allSatisfied(Map<String, Object> required, Map<String, Object> worldState) {
        for (Map.Entry<String, Object> entry : required.entrySet()) {
            Object actual = worldState.get(entry.getKey());
            if (!entry.getValue().equals(actual)) return false;
        }
        return true;
    }

    /** Returns true if the action's effects satisfy at least one entry in {@code unsatisfied}. */
    private static boolean contributes(GOAPAction action, Map<String, Object> unsatisfied) {
        for (Map.Entry<String, Object> effect : action.getEffects().entrySet()) {
            Object required = unsatisfied.get(effect.getKey());
            if (required != null && required.equals(effect.getValue())) return true;
        }
        return false;
    }

    /**
     * Returns a new unsatisfied map after applying an action:
     * effects that are in unsatisfied are removed; preconditions are added if not already present.
     */
    private static Map<String, Object> applyAction(
            GOAPAction action, Map<String, Object> unsatisfied) {

        Map<String, Object> result = new HashMap<>(unsatisfied);

        // Remove satisfied conditions
        for (Map.Entry<String, Object> effect : action.getEffects().entrySet()) {
            if (effect.getValue().equals(result.get(effect.getKey()))) {
                result.remove(effect.getKey());
            }
        }

        // Add new preconditions that are not already in the set
        for (Map.Entry<String, Object> pre : action.getPreconditions().entrySet()) {
            result.putIfAbsent(pre.getKey(), pre.getValue());
        }

        return result;
    }

    /** Walks the node chain from leaf to root, reversing to produce execution order. */
    private static List<GOAPAction> buildPlan(Node leaf) {
        List<GOAPAction> plan = new ArrayList<>();
        Node node = leaf;
        while (node.action != null) {
            plan.add(node.action);
            node = node.parent;
        }
        Collections.reverse(plan);
        return plan;
    }

    // -------------------------------------------------------------------------
    // Internal node type
    // -------------------------------------------------------------------------

    private static final class Node {
        final Node parent;
        final GOAPAction action;
        final float cost;
        final Map<String, Object> unsatisfied;

        Node(Node parent, GOAPAction action, float cost, Map<String, Object> unsatisfied) {
            this.parent = parent;
            this.action = action;
            this.cost = cost;
            this.unsatisfied = unsatisfied;
        }
    }
}
