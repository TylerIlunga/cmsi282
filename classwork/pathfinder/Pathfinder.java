package pathfinder.uninformed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Maze Pathfinding algorithm that implements a basic, uninformed, breadth-first tree search.
 */
public class Pathfinder {

    /**
     * Given a MazeProblem, which specifies the actions and transitions available in the
     * search, returns a solution to the problem as a sequence of actions that leads from
     * the initial to a goal state.
     *
     * @param problem A MazeProblem that specifies the maze, actions, transitions.
     * @return An ArrayList of Strings representing actions that lead from the initial to
     * the goal state, of the format: ["R", "R", "L", ...]
     */
    public static ArrayList<String> solve (MazeProblem problem) {
        Queue<SearchTreeNode> frontier = new LinkedList<SearchTreeNode>();
        frontier.add(new SearchTreeNode(problem.INITIAL_STATE, null, null));

        while (!frontier.isEmpty()) {
          SearchTreeNode current = frontier.poll();
          if (problem.isGoal(current.state)) {
              return getActionSequence(current, new ArrayList<String>());
          }

          Map<String, MazeState> transitions = problem.getTransitions(current.state);
          for(Map.Entry<String, MazeState> transition : transitions.entrySet()) {
              String action = transition.getKey();
              MazeState nextState = transition.getValue();
              frontier.add(new SearchTreeNode(nextState, action, current));
          }
        }
        return null;
    }

    /**
     * Returns the final list of actions that the agent took.
     *
     * @param node A SearchTreeNode (state, action, parent) contained in final
     * sequence.
     * @param actions A list of strings that contain of the following actions:
     * ("U", "D", "L", "R")
     * @return Boolean of whether or not the given state is a Goal.
     */
    private static ArrayList<String> getActionSequence(SearchTreeNode node, ArrayList<String> actions) {
      if (node.parent == null) { return actions; }
      actions.add(0, node.action);
      return getActionSequence(node.parent, actions);
    }
}

/**
 * SearchTreeNode that is used in the Search algorithm to construct the Search
 * tree.
 */
class SearchTreeNode {

    MazeState state;
    String action;
    SearchTreeNode parent;

    /**
     * Constructs a new SearchTreeNode to be used in the Search Tree.
     *
     * @param state The MazeState (col, row) that this node represents.
     * @param action The action that *led to* this state / node.
     * @param parent Reference to parent SearchTreeNode in the Search Tree.
     */
    SearchTreeNode (MazeState state, String action, SearchTreeNode parent) {
        this.state = state;
        this.action = action;
        this.parent = parent;
    }
}
