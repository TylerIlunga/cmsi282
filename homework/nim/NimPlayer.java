package nim;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Artificial Intelligence responsible for playing the game of Nim!
 * Implements the alpha-beta-pruning mini-max search algorithm
 */
public class NimPlayer {

    private final int MAX_REMOVAL;

    NimPlayer (int MAX_REMOVAL) {
        this.MAX_REMOVAL = MAX_REMOVAL;
    }

    /**
     *
     * @param   remaining   Integer representing the amount of stones left in the pile
     * @return  An int action representing the number of stones to remove in the range
     *          of [1, MAX_REMOVAL]
     */
    public int choose (int remaining) {
        int choice = this.MAX_REMOVAL;
        GameTreeNode root = new GameTreeNode(remaining, 0, true);
        int rootScore = alphaBetaMinimax(
            root,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            true,
            new HashMap<GameTreeNode, Integer>()
        );
        for (GameTreeNode child : root.children) {
            if (child.score == rootScore) {
                choice = Math.min(choice, child.action);
            }
        }
        return choice;
    }

    /**
     * Constructs the minimax game tree by the tenets of alpha-beta pruning with
     * memoization for repeated states.
     * @param   node    The root of the current game sub-tree
     * @param   alpha   Smallest minimax score possible
     * @param   beta    Largest minimax score possible
     * @param   isMax   Boolean representing whether the given node is a max (true) or min (false) node
     * @param   visited Map of GameTreeNodes to their minimax scores to avoid repeating large subtrees
     * @return  Minimax score of the given node + [Side effect] constructs the game tree originating
     *          from the given node
     */
    private int alphaBetaMinimax (GameTreeNode node, int alpha, int beta, boolean isMax, Map<GameTreeNode, Integer> visited) {
        // If we reached a terminal node, return it's utility in the view of the agent
        if (node.remaining == 0) { return node.scoreNode(isMax ? 0 : 1); }
        if (isMax) {
            int vertex = Integer.MIN_VALUE;
            // Generate and loop through the current node's children
            for (int action = this.MAX_REMOVAL; action > 0; action--) {
                // If the action returns a value of remaining stones to be < 0, continue
                if (node.remaining - action < 0) { continue; }
                GameTreeNode child = new GameTreeNode(
                    node.remaining - action,
                    action,
                    isMax ? false : true
                );
                // If we already found a child's minimax value, compare the vertex with
                // pre-computed value, otherwise compare with it's generated
                // minimax value
                if (visited.containsKey(child)) {
                    vertex = Math.max(vertex, visited.get(child));
                } else {
                    vertex = Math.max(vertex, alphaBetaMinimax(child, alpha, beta, false, visited));
                }
                // Set alpha value for the max node
                alpha = Math.max(alpha, vertex);
                if (beta <= alpha) { break; }
                // Adds generated node to the current node's children(list)
                node.addChild(child);
            }
            // After determining the minimax value of the current max node, set
            // the max node's minimax value and store it's value in our memo map.
            node.scoreNode(vertex);
            visited.put(node, vertex);
            // return the current node's minimax value.
            return vertex;
        } else {
            int vertex = Integer.MAX_VALUE;
            // Generate and loop through the current node's children
            for (int action = this.MAX_REMOVAL; action > 0; action--) {
                // If the action returns a value of remaining stones to be < 0, continue
                if (node.remaining - action < 0) { continue; }
                GameTreeNode child = new GameTreeNode(
                    node.remaining - action,
                    action,
                    isMax ? false : true
                );
                // If we already found a child's minimax value, compare the vertex with
                // pre-computed value, otherwise compare with it's generated
                // minimax value
                if (visited.containsKey(child)) {
                    vertex = Math.min(vertex, visited.get(child));
                } else {
                    vertex = Math.min(vertex, alphaBetaMinimax(child, alpha, beta, true, visited));
                }
                // Set beta value for the min node
                beta = Math.max(beta, vertex);
                if (beta <= alpha) { break; }
                // Adds generated node to the current node's children(list)
                node.addChild(child);
          }
          // After determining the minimax value of the current min node, set
          // the min node's minimax value and store it's value in our memo map.
          node.scoreNode(vertex);
          visited.put(node, vertex);
          // return the current node's minimax value.
          return vertex;
        }
    }

}

/**
 * GameTreeNode to manage the Nim game tree.
 */
class GameTreeNode {

    int remaining, action, score;
    boolean isMax;
    ArrayList<GameTreeNode> children;

    /**
     * Constructs a new GameTreeNode with the given number of stones
     * remaining in the pile, and the action that led to it. We also
     * initialize an empty ArrayList of children that can be added-to
     * during search, and a placeholder score of -1 to be updated during
     * search.
     *
     * @param   remaining   The Nim game state represented by this node: the #
     *          of stones remaining in the pile
     * @param   action  The action (# of stones removed) that led to this node
     * @param   isMax   Boolean as to whether or not this is a maxnode
     */
    GameTreeNode (int remaining, int action, boolean isMax) {
        this.remaining = remaining;
        this.action = action;
        this.isMax = isMax;
        children = new ArrayList<>();
        score = -1;
    }

    /**
    * Appends a child node to the current node's children list.
    * @param child GameTreeNode representing the child that will be added to
    * children list
    */
    public void addChild (GameTreeNode child) {
      children.add(child);
    }

    /**
    * Scores the current node and returns it's new value.
    * @param  score Integer representing the new value for the node.
    * @return integer value representing the new value for the node.
    *
    */
    public int scoreNode (int score) {
      this.score = score;
      return this.score;
    }

    @Override
    public boolean equals (Object other) {
        return other instanceof GameTreeNode
            ? remaining == ((GameTreeNode) other).remaining &&
              isMax == ((GameTreeNode) other).isMax &&
              action == ((GameTreeNode) other).action
            : false;
    }

    @Override
    public int hashCode () {
        return remaining + ((isMax) ? 1 : 0);
    }

    @Override
    public String toString () {
      return String.format(
          "Remaining: %d\nAction: %d\nScore: %d\nisMax: %b",
          this.remaining,
          this.action,
          this.score,
          this.isMax
      );
    }

}
