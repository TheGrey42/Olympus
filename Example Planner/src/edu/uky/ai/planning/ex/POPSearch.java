package edu.uky.ai.planning.ex;

import edu.uky.ai.logic.*;
import edu.uky.ai.util.*;
import edu.uky.ai.planning.ps.*;
import edu.uky.ai.planning.io.*;
import edu.uky.ai.logic.io.*;
import edu.uky.ai.planning.*;
import edu.uky.ai.planning.Utilities;
import edu.uky.ai.SearchBudget;
import java.util.*;


final class myPlan
{
	public ImmutableList<Operator> actions;
	public DirectedAcyclicGraph<Operator> orderings;
	public ImmutableList<CausalLink> causallinks;
	
	public myPlan(ImmutableList<Operator> actions, DirectedAcyclicGraph<Operator> orderings, ImmutableList<CausalLink> causallinks) {
		this.actions = actions;
		this.orderings = orderings;
		this.causallinks = causallinks;
	}
}

public class POPsearch extends PlanSpaceSearch {
	private final ImmutableList<Operator> actions;
	private final DirectedAcyclicGraph<Operator> orderings;
	private final ImmutableList<CausalLink> causallinks;
	
	
	
	
	public POPsearch(Problem problem, SearchBudget budget) {
		super(problem, budget);
	}
	
	@Override
	public Plan Solve() {
		Proposition goalstate = problem.goal;
		//Creates the agenda for the POP Algorithm
		Literal[] agenda = Utilities.toLiterals(goalstate);
		//Creates the Action Schemata of all available actions
		ImmutableArray<Operator> operators = problem.domain.operators;
		// 1. Termination
		// If Agenda is empty return <AOL>
		if (agenda.length == 0)
			return Plan(this.actions, this.orderings, this.causallinks);
		State initialstate = problem.initial;
		if (goalstate.isTrue(initialstate))
			System.out.println("Sweet God Yes!!!");
		// 2. Goal Selection 
		// Select a goal out of the agenda
		
	}
}