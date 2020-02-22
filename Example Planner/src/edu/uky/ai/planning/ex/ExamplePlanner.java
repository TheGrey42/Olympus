package edu.uky.ai.planning.ex;

import java.util.Random;

import edu.uky.ai.SearchBudget;
import edu.uky.ai.logic.Bindings;
import edu.uky.ai.logic.Literal;
import edu.uky.ai.logic.Proposition;
import edu.uky.ai.logic.Variable;
import edu.uky.ai.planning.Operator;
import edu.uky.ai.planning.Utilities;
import edu.uky.ai.planning.ps.CausalLink;
import edu.uky.ai.planning.ps.Flaw;
import edu.uky.ai.planning.ps.OpenPreconditionFlaw;
import edu.uky.ai.planning.ps.PartialStep;
import edu.uky.ai.planning.ps.PlanSpaceNode;
import edu.uky.ai.planning.ps.PlanSpaceRoot;
import edu.uky.ai.planning.ss.ForwardPlanner;
import edu.uky.ai.planning.ss.StateSpaceProblem;
import edu.uky.ai.util.DirectedAcyclicGraph;
import edu.uky.ai.util.ImmutableList;

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
	
	public static final<E> String Stringify(ImmutableList<E> list){
		String string = "[";
		boolean first = true;
		for (E element : list){
			if (first)
				first = false;
			else
				string += ", ";
			string += element;
		}
		return string + "]";
	}
	
	public ExamplePlanner() {
		super("Example");
	}

	@Override
	protected BreadthFirstSearch makeForwardSearch(StateSpaceProblem problem, SearchBudget budget) {
		return new BreadthFirstSearch(problem, budget);
	}
}
