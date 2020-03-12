package edu.uky.ai.planning.ex;

import edu.uky.ai.logic.*;
import edu.uky.ai.util.*;
import edu.uky.ai.planning.pg.*;
import edu.uky.ai.planning.ss.StateSpaceProblem;
import edu.uky.ai.planning.io.*;
import edu.uky.ai.logic.io.*;
import edu.uky.ai.planning.*;
import edu.uky.ai.planning.Utilities;
import edu.uky.ai.SearchBudget;
import java.util.*;
import java.util.Stack;

public class PG extends SubgraphSearch {

	Stack<SubgraphSpaceNode> stack = new Stack<SubgraphSpaceNode>();
	
	//Recursive Satisfy goals 
	SubGraphSpaceNode GoalSatisfier(Iterable<LiteralNode> goals, SubgraphSpaceNode current, int level) {
		if (level == 0) {
			return current;
		}
		Iterator<LiteralNode> iterate = goals.iterator();
		while(iterate.hasNext()) {
			
		}
		
	}
	//Helper for recursive satisfaction
	
	public SubgraphSpaceNode findSubgraph() {
		stack.add(root);
		Iterable<LiteralNode> goal = root.graph.goals;
		while (!stack.empty()) {
			SubgraphSpaceNode currentNode = stack.pop();
			int testLevel = currentNode.subgraph.first.level;
			if (testLevel == 0) {
				return currentNode;
			}
		}
		return null;
	}
}
