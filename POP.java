package edu.uky.ai.planning.ex;

import java.util.LinkedList;
import java.util.Queue;

import edu.uky.ai.logic.*;
import edu.uky.ai.util.*;
import edu.uky.ai.planning.ps.*;
import edu.uky.ai.planning.ss.ForwardNode;
import edu.uky.ai.planning.ss.ForwardPlanner;
import edu.uky.ai.planning.ss.ForwardSearch;
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
	
	//Determines the choices available to the Threat Resolution
	public static final String DemotionOrPromotion(DirectedAcyclicGraph<PartialStep> orderings, CausalLink link, PartialStep threat) {
		DirectedAcyclicGraph<PartialStep> testing = orderings.add(threat , link.tail);
		if (testing != null) {
			testing = orderings.add(link.head, threat);
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
	
	//Determines if a link is threatened if its label can be threatened by the PartialStep threat
	public static final Boolean IsLinkThreatened(DirectedAcyclicGraph<PartialStep> orderings, Bindings bindings, CausalLink link, PartialStep threat) {
		// Sees if the negation of the literal can be unified with the effect of the probable threat
		Bindings test1 = null;
		Bindings storage = null;
		int sizeofeffects = threat.effects.size();
		for (int m = 0; m < sizeofeffects; m++) {
			storage = link.label.negate().unify(threat.effects.get(m), bindings);
			if (storage != null) {
				test1 = storage;
			}
		}
		if (test1 == null)
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
	
	public static final Plan PlanGenerator(PlanSpaceNode winner) {
		Iterator iterator = winner.orderings.iterator();
		ForwardPlan solution = new ForwardPlan();
		while (iterator.hasNext()) {
			PartialStep currentstep = (PartialStep) iterator.next();
			if (currentstep.operator != null) {
				Step step = currentstep.makeStep(winner.bindings);
				solution = solution.addStep(step);
			}
		}
		return solution;
	}
	
	
	public POP(Problem problem, SearchBudget budget) {
		super(problem, budget);
	}
	@Override
	public Plan solve() {
		int priority = root.steps.size() + root.flaws.size();
		queue.push(root, priority);
		while(!queue.isEmpty()) {
			PlanSpaceNode current = queue.pop();
			//1. Termination 
			//Returns a plan if there are no flaws to fix
			if (current.flaws.size() == 0) {
				return PlanGenerator(current);
			}
			//2. Goal Selection
			//Creates a branch for each flaw that could be taken
			for (Flaw flaw : current.flaws) {
				OpenPreconditionFlaw preconditionflaw = (OpenPreconditionFlaw) flaw;
				PlanSpaceNode current1 = current.expand(current.steps, current.bindings, current.orderings, current.causalLinks, current.flaws);
				//3. Action Selection
				// Create a Branch for each existing action that satisfies the chosen flaw
				for (PartialStep consideration: current1.steps) {
					Literal tosatisfy = preconditionflaw.precondition;
					String check1 = consideration.toString();
					//Checks each existing step to see if any unify with the current flaw being analyzed
					if (check1 != "end") {
						Bindings test1 = null;
						Bindings storage = null;
						int sizeofeffects = consideration.effects.size();
						for (int m = 0; m < sizeofeffects; m++) {
							storage = tosatisfy.unify(consideration.effects.get(m), current1.bindings);
							if (storage != null) {
								test1 = storage;
							}
						}
						// If it can successfully be bound then it is a potential step so 
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
										if (step.operator != null) {
											Boolean check = IsLinkThreatened(orderings2, bindings2, link, step);
											if (IsLinkThreatened(orderings2, bindings2, link, step))
												threatenedlink = threatenedlink.add(new ThreatenedCausalLinkFlaw(link, step));
										}
									}
								}
								//4. Creates list of all links that require a decision
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
										//Block for if there is a decision and creates a node for each choice
										for (ThreatenedCausalLinkFlaw flaw1: hardlinks) {
											DirectedAcyclicGraph<PartialStep> orderings3 = orderings2.add(flaw1.threat, flaw1.link.tail);
											PlanSpaceNode current3 = current1.expand(steps2, bindings2, orderings3, causallink2, flaws2);
											int priority3 = current3.flaws.size() + current3.steps.size();
											queue.push(current3, priority3);
											DirectedAcyclicGraph<PartialStep> orderings4 = orderings2.add(flaw1.link.head, flaw1.threat);
											PlanSpaceNode current4 = current1.expand(steps2, bindings2, orderings4, causallink2, flaws2);
											int priority4 = current4.flaws.size() + current4.steps.size();
											queue.push(current4, priority4);
										}
									}
								}
							}
						}
					}
				}
				//Create a node for each new action that could unify with the selected flaw
				for (Operator consider: problem.domain.operators) {
					Literal needsatisfy = preconditionflaw.precondition;
					PartialStep considernewstep = new PartialStep(consider);
					Bindings newbindings = null;
					Bindings storage = null;
					int sizeofeffects = considernewstep.effects.size();
					for (int m = 0; m < sizeofeffects; m++) {
						storage = needsatisfy.unify(considernewstep.effects.get(m), current1.bindings);
						if (storage != null) {
							newbindings = storage;
						}
					}
					if (newbindings != null) {
						DirectedAcyclicGraph<PartialStep> neworderings = current1.orderings.add(considernewstep, preconditionflaw.step);
						if (neworderings != null) {
							ImmutableList<PartialStep> newsteps = current1.steps.add(considernewstep);
							ImmutableList<CausalLink> newlinks = current1.causalLinks.add(new CausalLink(considernewstep, needsatisfy, preconditionflaw.step));
							ImmutableList<Flaw> newflaws = current1.flaws.remove(preconditionflaw);
							// Adds the OpenPreconditions of the new action to the flaws
							for (Literal newestflaw : considernewstep.preconditions)
								newflaws = newflaws.add(new OpenPreconditionFlaw(considernewstep, newestflaw));
							//Flag to detect unresolvable threat
							Boolean DontPost = false;
							//Records every threatened link
							ImmutableList<ThreatenedCausalLinkFlaw> threatenedlink = new ImmutableList();
							for (PartialStep step : newsteps) {
								for (CausalLink link : newlinks) {
									// If threat detected adds it to list of threats
									if (step.operator != null) {
										if (IsLinkThreatened(neworderings, newbindings, link, step))
											threatenedlink = threatenedlink.add(new ThreatenedCausalLinkFlaw(link, step));
									}
								}
							}
							//Creates list of all links that require a decision
							ImmutableList<ThreatenedCausalLinkFlaw> hardlinks = threatenedlink;
							for (ThreatenedCausalLinkFlaw newest : threatenedlink) {
								String option = DemotionOrPromotion(neworderings, newest.link, newest.threat);
								if (option == "Promote") {
									neworderings = neworderings.add(newest.threat, newest.link.tail);
									hardlinks = hardlinks.remove(newest);
								}
								else if (option == "Demote") {
									neworderings = neworderings.add(newest.link.head, newest.threat);
									hardlinks = hardlinks.remove(newest);
								}
								//
								else if (option == "Abort") {
									DontPost = true;
								}
							}
							if (!DontPost) {
								if (hardlinks.size() == 0) {
									PlanSpaceNode current2 = current1.expand(newsteps, newbindings, neworderings, newlinks, newflaws);
									int priority1 = current2.flaws.size() + current2.steps.size();
									queue.push(current2, priority);
								}
								else {
									//Block for dealing with all the threats that require a decision to resolve
									for (ThreatenedCausalLinkFlaw flaw1: hardlinks) {
										DirectedAcyclicGraph<PartialStep> orderings3 = neworderings.add(flaw1.threat, flaw1.link.tail);
										PlanSpaceNode current3 = current1.expand(newsteps, newbindings, orderings3, newlinks, newflaws);
										int priority3 = current3.flaws.size() + current3.steps.size();
										queue.push(current3, priority3);
										DirectedAcyclicGraph<PartialStep> orderings4 = neworderings.add(flaw1.link.head, flaw1.threat);
										PlanSpaceNode current4 = current1.expand(newsteps, newbindings, orderings4, newlinks, newflaws);
										int priority4 = current4.flaws.size() + current4.steps.size();
										queue.push(current4, priority4);
									
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
}