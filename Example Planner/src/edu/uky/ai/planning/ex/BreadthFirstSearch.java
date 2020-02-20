package edu.uky.ai.planning.ex;

import java.util.LinkedList;
import java.util.Queue;

import edu.uky.ai.SearchBudget;
import edu.uky.ai.logic.HashState;
import edu.uky.ai.logic.MutableState;
import edu.uky.ai.planning.Plan;
import edu.uky.ai.planning.Step;
import edu.uky.ai.planning.ss.ForwardNode;
import edu.uky.ai.planning.ss.ForwardSearch;
import edu.uky.ai.planning.ss.StateSpaceProblem;

/**
 * A planner that uses simple breadth first search through the space of states.
 * 
 * @author Stephen G. Ware
 */
public class BreadthFirstSearch extends ForwardSearch {

	/** The queue which will hold the frontier (states not yet visited) */
	private final Queue<ForwardNode> queue = new LinkedList<>();
	
	/**
	 * Constructs a new state space search object.
	 * 
	 * @param problem the problem to solve
	 * @param budget the search budget, which constrains how many states may be
	 * visited and how much time the search can take
	 */
	public BreadthFirstSearch(StateSpaceProblem problem, SearchBudget budget) {
		super(problem, budget);
	}

	@Override
	public Plan solve() {
		// Start with only the root node (initial state) in the queue.
		queue.add(root);
		// Search until the queue is empty (no more states to consider).
		while(!queue.isEmpty()) {
			// Pop a state off the frontier.
			ForwardNode current = queue.poll();
			// Check if it is a solution.
			if(problem.isSolution(current.plan))
				{
				return current.plan;
				}
			// Consider every possible step...
			for(Step step : problem.steps) {
				// If it's precondition is met in the current state...
				if(step.precondition.isTrue(current.state)) {
					// Create the state that results from taking this step.
					MutableState next = new HashState(current.state);
					step.effect.makeTrue(next);
					// Add the state results from that step to the frontier.
					queue.offer(current.expand(step, next));
				}
			}
		}
		// If the queue is empty and we never found a solution, the problem
		// cannot be solved. Return null.
		return null;
	}
}
