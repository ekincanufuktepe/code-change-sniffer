package ccs.markov.dotview;

import gr.gousiosg.javacg.stat.JCallGraph;
import gr.gousiosg.javacg.stat.MethodVisitor;

public class RunDOTRep {

	public static void main(String[] args) {
		JCallGraph cg = new JCallGraph();
		String[] newVersion = {args[0]};
		
		cg.createCall(newVersion);
		
		System.out.println(MethodVisitor.graph.size());
		System.out.println(MethodVisitor.detailedGraph.size());
		
		DOTCreation dot = new DOTCreation();
		dot.readAISNodes();
		dot.readEISNodes();
		dot.writeDOTFile();
	}

}
