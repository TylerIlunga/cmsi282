package pathfinder.informed;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;

/**
 * Maze Pathfinding algorithm that implements the informed search strategy A*
 * star graph search
 */
public class Pathfinder {
  /**
   * Given a MazeProblem, which specifies the actions and transitions available in
   * the search, returns a solution to the problem as a sequence of actions that
   * leads from the initial to a goal state.
   *
   * @param problem A MazeProblem that specifies the maze, actions, transitions.
   * @return An ArrayList of Strings representing actions that lead from the
   *         initial to the goal state, of the format: ["R", "R", "L", ...]
   */
  public static ArrayList<String> solve(MazeProblem problem) {
    ArrayList<String> stepsFromHome = solve(problem, problem.INITIAL_STATE, problem.objectives(false));
    if (stepsFromHome == null) {
      return null;
    }
    ArrayList<String> stepsFromKey = solve(problem, problem.KEY_STATE, problem.objectives(true));
    if (stepsFromKey == null) {
      return null;
    }
    stepsFromHome.addAll(stepsFromKey);
    return stepsFromHome;
  }

  public static ArrayList<String> solve(MazeProblem problem, MazeState initState, Set<MazeState> objectives) {
    Queue<SearchTreeNode> frontier = new PriorityQueue<SearchTreeNode>(11, (s1, s2) -> s1.evaluation - s2.evaluation);
    Set<MazeState> closedSet = new HashSet<MazeState>();
    frontier.add(new SearchTreeNode(initState, null, null, 0, computeHeuristic(initState, objectives)));

    while (!frontier.isEmpty()) {
      SearchTreeNode current = frontier.poll();
      if (objectives.contains(current.state)) {
        return getActionSequence(current);
      }
      closedSet.add(current.state);

      Map<String, MazeState> transitions = problem.getTransitions(current.state);
      for (Map.Entry<String, MazeState> transition : transitions.entrySet()) {
        String nextAction = transition.getKey();
        MazeState nextState = transition.getValue();
        if (!closedSet.contains(nextState)) {
          frontier.add(new SearchTreeNode(nextState, nextAction, current,
              current.totalCost + problem.getCost(nextState), computeHeuristic(nextState, objectives)));
        }
      }
    }
    return null;
  }

  /**
   * Returns the final list of actions that the agent took.
   *
   * @param node    A SearchTreeNode (state, action, parent) contained in final
   *                sequence.
   * @param actions A list of strings that contain of the following actions: ("U",
   *                "D", "L", "R")
   * @return Boolean of whether or not the given state is a Goal.
   */
  private static ArrayList<String> getActionSequence(SearchTreeNode node) {
    ArrayList<String> sequence = new ArrayList<String>();
    for (SearchTreeNode c = node; c.parent != null; c = c.parent) {
      sequence.add(c.action);
    }
    return sequence;
  }

  /**
   * Returns the sum of the absolute values of the differences of the given state
   * coordinates.
   *
   * @param s1 A MazeState (col, row)
   * @param s2 A MazeState (col, row)
   * @return Integer representing the sum.
   */
  private static int manhattanDistance(MazeState s1, MazeState s2) {
    return Math.abs(s1.col - s2.col) + Math.abs(s1.row - s2.row);
  }

  /**
   * Returns the heuristic cost to closet goal, from a given state, within a list
   * of objectives.
   *
   * @param current    A MazeState (col, row)
   * @param objectives A set of MazeStates {(col, row), ...}
   * @return int that represents the heuristic cost to the closet goal.
   */
  private static int computeHeuristic(MazeState current, Set<MazeState> objectives) {
    int leastCost = Integer.MAX_VALUE;
    for (MazeState obj : objectives) {
      leastCost = Math.min(leastCost, manhattanDistance(current, obj));
    }
    return leastCost;
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
  int totalCost;
  int heuristic;
  int evaluation;

  /**
   * Constructs a new SearchTreeNode to be used in the Search Tree.
   *
   * @param state  The MazeState (col, row) that this node represents.
   * @param action The action that *led to* this state / node.
   * @param parent Reference to parent SearchTreeNode in the Search Tree.
   */
  SearchTreeNode(MazeState state, String action, SearchTreeNode parent, int totalCost, int heuristic) {
    this.state = state;
    this.action = action;
    this.parent = parent;
    this.totalCost = totalCost;
    this.heuristic = heuristic;
    this.evaluation = totalCost + heuristic;
  }

  @Override
  public String toString() {
    return String.format("Node:\nState: %s\nAction: %s\nParent: %s\ng(n): %d\nh(n): %d, f(n): %d", this.state,
        this.action, this.parent, this.totalCost, this.heuristic, this.evaluation);
  }
}
