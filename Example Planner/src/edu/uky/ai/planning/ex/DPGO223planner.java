package edu.uky.ai.planning.ex;

import java.util.LinkedList;
import java.util.Queue;

import edu.uky.ai.SearchBudget;
import edu.uky.ai.logic.HashState;
import edu.uky.ai.logic.MutableState;
import edu.uky.ai.planning.Plan;
import edu.uky.ai.planning.Problem;
import edu.uky.ai.planning.Step;
import edu.uky.ai.planning.ps.*;
import edu.uky.ai.util.*;
import edu.uky.ai.logic.*;


public class DPGO223planner extends PlanSpacePlanner {
	public DPGO223planner() {
		super("dpgo223");
	}
		
	protected DPGO223planner makeSearch(Problem problem, SearchBudget budget) {
		return new POPsearch(problem, budget);
	}
	
}