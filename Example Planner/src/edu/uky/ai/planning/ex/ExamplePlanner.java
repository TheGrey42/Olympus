package edu.uky.ai.planning.ex;

import java.util.List;

import edu.uky.ai.SearchBudget;
import edu.uky.ai.planning.ss.ForwardPlanner;
import edu.uky.ai.planning.ss.StateSpaceProblem;
import edu.uky.ai.logic.*;
import edu.uky.ai.planning.*;
import edu.uky.ai.planning.Utilities;

import java.util.Iterator;
import java.util.function.Consumer;
import edu.uky.ai.util.*;
/**
 * A planner that uses simple breadth first search through the space of states.
 * 
 * @author Your Name
 */
public class ExamplePlanner extends ForwardPlanner {

	/**
	 * Constructs a new breadth first search planner. You should change the
	 * string below from "Example" to your ID. You should also change the name
	 * of this class. In Eclipse, you can do that easily by right-clicking on
	 * this file (ExamplePlanner.java) in the Package Explorer and choosing
	 * Refactor > Rename.
	 */
	public ExamplePlanner() {
		super("Example");
	}

	@Override
	protected BreadthFirstSearch makeForwardSearch(StateSpaceProblem problem, SearchBudget budget) {
		Proposition goal  = problem.goal;
		State initialstate = problem.initial;
		ImmutableArray<Operator> operators = problem.domain.operators;
		Operator chosengoal = operators.get(0);
		Literal[] preagenda = Utilities.toLiterals(goal);
		Literal agenda1 = preagenda[0];
		Proposition preffectgoal = chosengoal.precondition;
		System.out.println(agenda1);
		
		return new BreadthFirstSearch(problem, budget);
	}
}
