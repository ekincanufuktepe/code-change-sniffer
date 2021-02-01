package ccs.markov;

import java.util.ArrayList;

import ccs.markov.change.ChangedMethodProbability;
import gr.gousiosg.javacg.stat.MethodVisitor;

public class MarkovGraphConstruction {

	public ArrayList<MethodNode> assignMethodChangeInfo(ChangedMethodProbability change) {
		ArrayList<MethodNode> methods = new ArrayList<>();

		for(String fromMethod : MethodVisitor.detailedGraph.keySet()) {

			MethodNode fromMethodNode = null;
			
			boolean flagForFromMethod = true;
			
			for(int i=0; i<methods.size(); i++) {
				if(methods.get(i).getMethodName().equals(fromMethod))
					flagForFromMethod = false;
			}
			
			if(flagForFromMethod) {
				if(change.getMethodChangeProb().containsKey(fromMethod)) {
					fromMethodNode = new MethodNode(fromMethod, change.getMethodChangeProb().get(fromMethod));
					methods.add(fromMethodNode);
				}
				else if (!change.getMethodChangeProb().containsKey(fromMethod)) {
					fromMethodNode = new MethodNode(fromMethod, 0.0);
					methods.add(fromMethodNode);
				}
				for(String toMethod : MethodVisitor.detailedGraph.get(fromMethod)) {
					MethodNode toMethodNode = null;
					boolean flagForToMethod = true;
					int toIndex = -1;
					for(int j=0; j<methods.size(); j++) {
						if(methods.get(j).getMethodName().equals(toMethod)) {
							flagForToMethod = false;
							toIndex = j;
						}
							
					}
					
					int fromIndex = -1;
					for(int j=0; j<methods.size(); j++) {
						if(methods.get(j).getMethodName().equals(fromMethod))
							fromIndex = j;
					}
					
					if(flagForToMethod && change.getMethodChangeProb().containsKey(toMethod)) {
						toMethodNode = new MethodNode(toMethod, change.getMethodChangeProb().get(toMethod));
						methods.add(toMethodNode);
						methods.get(methods.indexOf(toMethodNode)).addToMethod(fromMethod, -1.0);

						methods.get(fromIndex).addToMethod(toMethod, -1.0);
					}
					else if(flagForToMethod && !change.getMethodChangeProb().containsKey(toMethod)) {
						toMethodNode = new MethodNode(toMethod, 0.0);
						methods.add(toMethodNode);
						methods.get(methods.indexOf(toMethodNode)).addToMethod(fromMethod, -1.0);
						methods.get(fromIndex).addToMethod(toMethod, -1.0);
					}
					else if(!flagForToMethod && change.getMethodChangeProb().containsKey(toMethod)) {
						methods.get(toIndex).addToMethod(fromMethod, -1.0);
					}
					else if(!flagForToMethod && !change.getMethodChangeProb().containsKey(toMethod)) {
						methods.get(toIndex).addToMethod(fromMethod, -1.0);
					}
				}
			}
		}
		return methods;
	}
}
