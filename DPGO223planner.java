package edu.uky.ai.planning.ex;

import java.util.LinkedList;
import java.util.Queue;

import edu.uky.ai.SearchBudget;
import edu.uky.ai.logic.HashState;
import edu.uky.ai.logic.MutableState;
import edu.uky.ai.planning.Plan;
import edu.uky.ai.planning.Problem;
import edu.uky.ai.planning.Step;
import edu.uky.ai.planning.pg.*;
import edu.uky.ai.util.*;
import edu.uky.ai.logic.*;


public class DPGO223planner extends PlanGraphPlanner {
	public DPGO223planner() {
		super("dpgo223");
	}

	@Override
	protected SubgraphSearch makeSearch(PlanGraph graph, SearchBudget budget) {
		// TODO Auto-generated method stub
		return new PG(graph, budget, new Subgraph());
	}
	
	
}