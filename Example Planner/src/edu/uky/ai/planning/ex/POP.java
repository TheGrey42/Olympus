package edu.uky.ai.planning.ex;

import edu.uky.ai.logic.*;
import edu.uky.ai.util.*;
import edu.uky.ai.planning.ps.*;
import edu.uky.ai.planning.ss.ForwardNode;
import edu.uky.ai.planning.io.*;
import edu.uky.ai.logic.io.*;
import edu.uky.ai.planning.*;
import edu.uky.ai.planning.Utilities;
import edu.uky.ai.SearchBudget;
import java.util.*;
import edu.uky.ai.planning.ss.StateSpaceProblem;




public class POP extends PlanSpaceSearch {
	
	private final MinPriorityQueue<PlanSpaceNode> queue = new MinPriorityQueue();
	
	
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
	
	
	public static final String DemotionOrPromotion(DirectedAcyclicGraph<PartialStep> orderings, CausalLink link, PartialStep threat) {
		DirectedAcyclicGraph<PartialStep> testing = orderings.add(link.tail, threat);
		if (testing != null) {
			testing = orderings.add(threat, link.head);
			if (testing != null) {
				return "Both";
			}
		}
		testing = orderings.add(link.tail, threat);
		if (testing == null)
			return "Demote";
		//Tests if the threat can occur before the head of the link
		testing = orderings.add(threat, link.head);
		if (testing == null)
			return "Promote";
		return "Abort";
	}
	
	public static final Boolean IsLinkThreatened(DirectedAcyclicGraph<PartialStep> orderings, Bindings bindings, CausalLink link, PartialStep threat) {
		// Sees if the negation of the literal can be unified with the effect of the probable threat
		Bindings test = threat.operator.effect.negate().unify(link.label, bindings);
		if (test == null)
			return false;
		//Sees if the action does threatens the label of the link that they can occur at the same time
		//Start by seeing if threat can occur after the tail of the link
		DirectedAcyclicGraph<PartialStep> testing = orderings.add(link.tail, threat);
		if (testing == null)
			return false;
		//Tests if the threat can occur before the head of the link
		testing = orderings.add(threat, link.head);
		if (testing == null)
			return false;
		//If it gets this far it can occur in the same space as the link and it will alter the label
		return true;
		
	}
	
	public static final ThreatenedCausalLinkFlaw[] ThreatListToArray(ImmutableList<ThreatenedCausalLinkFlaw> hardlist) {
		List<ThreatenedCausalLinkFlaw> newer = new ArrayList<>();
		for (ThreatenedCausalLinkFlaw flaw : hardlist) {
			newer.add(flaw);
		}
		ThreatenedCausalLinkFlaw[] newest;
		newest = newer.toArray();
		return newest;
		
	}
	
	public static final ForwardPlan PlanGenerator(PlanSpaceNode winner) {
		return null;
		//Takes the DirectedAcyclicGraph orderings and turns into a Totally Ordered Plan from the PartialPlan
	}
	
	
	
	public POP(Problem problem, SearchBudget budget) {
		super(problem, budget);
	}
	
	public Plan Solve() {
		ImmutableList<CausalLink> causallink = new ImmutableList();
		ImmutableList<Flaw> flaws = new ImmutableList();
		ImmutableList<PartialStep> steps = new ImmutableList();
		Bindings bindings = null;
		DirectedAcyclicGraph<PartialStep> orderings = new DirectedAcyclicGraph();
		Variable[] parameters = new Variable[] {new Variable("null", "void")};
		Proposition effects = problem.initial.toProposition();
		Proposition preconditions = effects;
		
		Operator startstep = new Operator("start", parameters, preconditions, effects);
		PartialStep start = new PartialStep(startstep);
		
		Proposition endeffects = problem.goal;
		Proposition endpreconditions = problem.goal;
		
		Operator endstep = new Operator("end", parameters, endpreconditions, endeffects);
		PartialStep end = new PartialStep(endstep);
		
		//Ensures that the Start Step is before the End Step
		orderings = orderings.add(start, end);
		steps = steps.add(start);
		steps = steps.add(end);
		Literal[] initialflaws = Utilities.toLiterals(problem.goal);
		for (Literal flaw : initialflaws) {
			OpenPreconditionFlaw preconditionflaw = new OpenPreconditionFlaw(end, flaw);
			flaws = flaws.add(preconditionflaw);
		}
		
		
		
		PlanSpaceRoot root = new PlanSpaceRoot(problem, budget);
		PlanSpaceNode current = root.expand(steps, bindings, orderings, causallink, flaws);
		
		ImmutableList<Flaw> test = current.flaws;
		int priority = current.steps.size() + current.flaws.size();
		queue.push(current, priority);
		while(!queue.isEmpty()) {
			current = queue.pop();
			//1. Termination 
			//Returns a plan if there are no flaws to fix
			if (current.flaws.size() == 0)
				return PlanGenerator(current);
			//2. Goal Selection
			//Creates a branch for each flaw that could be taken
			for (Flaw flaw : current.flaws) {
				OpenPreconditionFlaw preconditionflaw = (OpenPreconditionFlaw) flaw;
				PlanSpaceNode current1 = current.expand(current.steps, current.bindings, current.orderings, current.causalLinks, current.flaws);
				
				//3. Action Selection
				// Create a Branch for reusing current steps or picking new action from the schemata
				for (int i = 0; i < 2; i++) {
					//If i = 0 we will create the node for searching current actions that can be ordered before the flaw
					for (PartialStep consideration: current1.steps) {
						Literal tosatisfy = preconditionflaw.precondition;
						if (consideration.operator.name != "end") {
							Bindings test1 = consideration.operator.effect.unify(tosatisfy, current1.bindings);
							if (test1 != null) {
								DirectedAcyclicGraph<PartialStep> tester = current1.orderings.add(consideration, preconditionflaw.step);
								if (tester != null) {
									//4. Updating of the Flaws, Orderings, and Links
									Bindings bindings2 = test1;
									ImmutableList<CausalLink> causallink2 =  current1.causalLinks.add(new CausalLink(consideration, tosatisfy, preconditionflaw.step));
									DirectedAcyclicGraph<PartialStep> orderings2 = tester;							
									ImmutableList<Flaw> flaws2 = current1.flaws.remove(preconditionflaw);
									ImmutableList<PartialStep> steps2 = current1.steps;
									//Flag to detect unresolvable threat
									Boolean DontPost = false;
									//Records every threatened link
									ImmutableList<ThreatenedCausalLinkFlaw> threatenedlink = new ImmutableList();
									for (PartialStep step : steps2) {
										for (CausalLink link : causallink2) {
											// If threat detected adds it to list of threats
											if (IsLinkThreatened(orderings2, bindings2, link, step))
												threatenedlink = threatenedlink.add(new ThreatenedCausalLinkFlaw(link, step));
										}
									}
									//Creates list of all links that require a decision
									ImmutableList<ThreatenedCausalLinkFlaw> hardlinks = threatenedlink;
									for (ThreatenedCausalLinkFlaw newest : threatenedlink) {
										String option = DemotionOrPromotion(orderings2, newest.link, newest.threat);
										if (option == "Promote") {
											orderings2 = orderings2.add(newest.threat, newest.link.tail);
											hardlinks = hardlinks.remove(newest);
										}
										else if (option == "Demote") {
											orderings2 = orderings2.add(newest.link.head, newest.threat);
											hardlinks = hardlinks.remove(newest);
										}
										//
										else if (option == "Abort") {
											DontPost = true;
										}
									}
									if (!DontPost) {
										if (hardlinks.size() == 0) {
											PlanSpaceNode current2 = current1.expand(steps2, bindings2, orderings2, causallink2, flaws2);
											int priority1 = current2.flaws.size() + current2.steps.size();
											queue.push(current2, priority);
										}
										else {
											//stuff to handle all the bullshit
										}
									}
								}
							}
						}
					}
					//If i = 1 create a node for each action that could unify with the selected flaw
					for (Operator consider: problem.domain.operators) {
						Literal needsatisfy = preconditionflaw.precondition;
						Bindings test2 = consider.effect.unify(needsatisfy, current1.bindings);
						if (test2 != null) {
							PartialStep considernewstep = new PartialStep(consider);
							DirectedAcyclicGraph<PartialStep> tester = current1.orderings.add(considernewstep, preconditionflaw.step);
							if (tester != null) {
								tester = tester.add(start, considernewstep);
								ImmutableList<PartialStep> newsteps = current1.steps.add(considernewstep);
								ImmutableList<CausalLink> newlinks = current1.causalLinks.add(new CausalLink(considernewstep, needsatisfy, preconditionflaw.step));
								ImmutableList<Flaw> newflaws = current1.flaws.remove(preconditionflaw);
								for (Literal newestflaw : considernewstep.preconditions)
									newflaws = newflaws.add(new OpenPreconditionFlaw(considernewstep, newestflaw));
								
							}
						}
					}
					//Always order a new state after the start state
				}
			}
		}
	}
}