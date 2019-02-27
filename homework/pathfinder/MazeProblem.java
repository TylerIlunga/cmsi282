package pathfinder.informed;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the Maze Grid pathfinding problem including the actions, transitions,
 * goal test, and solution test. Can be fed as an input to a Search algorithm to
 * find and then test a solution.
 */
public class MazeProblem {

    // Fields
    // -----------------------------------------------------------------------------
    private String[] maze;
    private int rows, cols;
    public final MazeState INITIAL_STATE, KEY_STATE;
    public final Set<MazeState> GOAL_STATES;
    public final Set<MazeState> KEY_STATES;
    private static final Map<String, MazeState> TRANS_MAP = createTransitions();

    /**
     * @return Creates the transition map that maps String actions to
     * MazeState offsets, of the format:
     * { "U": (0, -1), "D": (0, +1), "L": (-1, 0), "R": (+1, 0) }
     */
    private static final Map<String, MazeState> createTransitions () {
        Map<String, MazeState> result = new HashMap<>();
        result.put("U", new MazeState(0, -1));
        result.put("D", new MazeState(0,  1));
        result.put("L", new MazeState(-1, 0));
        result.put("R", new MazeState( 1, 0));
        return result;
    }


    // Constructor
    // -----------------------------------------------------------------------------

    /**
     * Constructs a new MazeProblem from the given maze; responsible for finding
     * the initial and goal states in the maze, and storing in the MazeProblem state.
     *
     * @param maze An array of Strings in which characters represent the legal maze
     * entities, including:<br>
     * 'X': A wall, 'G': A goal, 'I': The initial state, '.': an open spot
     * For example, a valid maze might look like:
     * <pre>
     * String[] maze = {
     *     "XXXXXXX",
     *     "X.....X",
     *     "XIX.X.X",
     *     "XX.X..X",
     *     "XG....X",
     *     "XXXXXXX"
     * };
     * </pre>
     */
    MazeProblem (String[] maze) {
        this.maze = maze;
        this.rows = maze.length;
        this.cols = (rows == 0) ? 0 : maze[0].length();
        MazeState foundInitial = null, foundKey = null;
        Set<MazeState> foundGoals = new HashSet<MazeState>();
        Set<MazeState> foundKeys = new HashSet<MazeState>();

        // Find the initial and goal state in the given maze, and then
        // store in fields once found
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                switch (maze[row].charAt(col)) {
                case 'I':
                    foundInitial = new MazeState(col, row); break;
                case 'G':
                    foundGoals.add(new MazeState(col, row)); break;
                case 'K':
                    foundKey = new MazeState(col, row);
                    foundKeys.add(foundKey); break;
                case '.':
                case 'X':
                case 'M':
                  break;
                default:
                    throw new IllegalArgumentException("Maze formatted invalidly");
                }
            }
        }

        INITIAL_STATE = foundInitial;
        GOAL_STATES = foundGoals;
        KEY_STATE = foundKey;
        KEY_STATES = foundKeys;
    }


    // Methods
    // -----------------------------------------------------------------------------

    /**
     * Returns a list of objectives for the agent.
     * @param foundKey Boolean representing whether the agent found the key
     * or not.
     * @return List of MazeStates [(col, row), ...]
     */
    public Set<MazeState> objectives (boolean foundKey) {
        if (!foundKey) { return KEY_STATES; }
        return GOAL_STATES;
    }

    /**
     * Returns whether or not the given state is a Goal state.
     *
     * @param state A MazeState (col, row) to test
     * @return Boolean of whether or not the given state is a Goal.
     */
    public boolean isGoal (MazeState state) {
        return GOAL_STATES.contains(state);
    }

    /**
     * Returns a map of the states that can be reached from the given input
     * state using any of the available actions.
     *
     * @param state A MazeState (col, row) representing the current state
     * from which actions can be taken
     * @return Map A map of actions to the states that they lead to, of the
     * format, for current MazeState (c, r):<br>
     * { "U": (c, r-1), "D": (c, r+1), "L": (c-1, r), "R": (c+1, r) }
     */
    public Map<String, MazeState> getTransitions (MazeState state) {
        // Store transitions as a Map between actions ("U", "D", ...) and
        // the MazeStates that they result in from state
        Map<String, MazeState> result = new HashMap<>();

        // For each of the possible directions (stored in TRANS_MAP), test
        // to see if it is a valid transition
        for (Map.Entry<String, MazeState> action : TRANS_MAP.entrySet()) {
            MazeState actionMod = action.getValue(),
                      newState  = new MazeState(state.col, state.row);
            newState.add(actionMod);

            // If the given state *is* a valid transition (i.e., within
            // map bounds and no wall at the position)...
            if (newState.row >= 0 && newState.row < rows &&
                newState.col >= 0 && newState.col < cols &&
                maze[newState.row].charAt(newState.col) != 'X') {
                // ...then add it to the result!
                result.put(action.getKey(), newState);
            }
        }
        return result;
    }

    /**
    * Given a state, determine the cost for moving from the agent's current
    * state to the given state.
    *
    * @param state A state that represents an option that the agent can take
    * from it's current state.
    *
    * @return An integer that represents the cost from moving from the current
    * state to the given state.
    */
    public int getCost (MazeState state) {
        switch (maze[state.row].charAt(state.col)) {
            case 'M': return 3;
            default: return 1;
        }
    }

    /**
     * Given a possibleSoln, tests to ensure that it is indeed a solution to this MazeProblem,
     * as well as returning the cost.
     *
     * @param possibleSoln A possible solution to test, which is a list of actions of the format:
     * ["U", "D", "D", "L", ...]
     * @return A 2-element array of ints of the format [isSoln, cost] where:
     * isSoln will be 0 if it is not a solution, and 1 if it is
     * cost will be an integer denoting the cost of the given solution to test optimality
     */
    public int[] testSolution (ArrayList<String> possibleSoln) {
      // Update the "moving state" that begins at the start and is modified by the transitions
        MazeState movingState = new MazeState(INITIAL_STATE.col, INITIAL_STATE.row);
        int cost = 0;
        boolean hasKey = false;
        int[] result = {0, -1};

        // For each action, modify the movingState, and then check that we have landed in
        // a legal position in this maze
        for (String action : possibleSoln) {
            MazeState actionMod = TRANS_MAP.get(action);
            movingState.add(actionMod);
            switch (maze[movingState.row].charAt(movingState.col)) {
            case 'X':
                return result;
            case 'K':
                hasKey = true; break;
            }
            cost += getCost(movingState);
        }
        result[0] = isGoal(movingState) && hasKey ? 1 : 0;
        result[1] = cost;
        return result;
    }

}
