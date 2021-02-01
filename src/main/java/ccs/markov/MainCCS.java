package ccs.markov;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ccs.markov.change.ChangedMethodProbability;
import ccs.markov.slicer.AlgorithmTag;
import ccs.markov.slicer.CFG;
import ccs.markov.slicer.CFGGenerator;
import ccs.markov.slicer.DOTtoPNG;
import ccs.markov.slicer.ForwardSlicer;
import gr.gousiosg.javacg.stat.JCallGraph;
import gr.gousiosg.javacg.stat.MethodVisitor;

public class MainCCS {

	public static long endConstruct;
	public static long totalConstruct;

	public static long startCalc;
	public static long endCalc;
	public static long totalCalc;

	public static void main(String[] args) {
		// Set the commit number to be analyzed
		MarkovChains.commitNum=16;
		
		// Select and initialize one of the graph model you want to use
		MarkovChains.model = SelectModel.EFFECT_GRAPH;
//		MarkovChains.model = SelectModel.CALL_GRAPH;
		
		String predictionOutputFile = "data/jjwt/jjwt-0.6.";
//		String predictionOutputFile = "data/jsoup/1.10.3.";
//		String predictionOutputFile = "data/commons-csv/1.7.";
//		String predictionOutputFile = "data/junit4/4.11.";
//		String predictionOutputFile = "data/commons-codec/1.14.";
		
		long start = System.currentTimeMillis();

		// Create CFGs
		System.out.println("Creating CFG of project: "+args[0]);
		CFGGenerator cfgGen = new CFGGenerator();
		cfgGen.createCFG(args[0]);
		System.out.println("CFG creation complete!");

		System.out.println("Creating .dot files of CFGs...");
		DOTtoPNG dot = new DOTtoPNG();
		dot.findDotFiles();
		System.out.println(".dot file generation complete");

		long startCall = System.currentTimeMillis();
		// Create Call Graph
		JCallGraph cg = new JCallGraph();
		String[] newVersion = {args[0]};
		cg.createCall(newVersion);


		// Add methods as nodes to the HashMap that doesn't have any child
		HashMap<String,Set<String>> tmpDetailedGraph = new HashMap<>();
		tmpDetailedGraph.putAll(MethodVisitor.detailedGraph);

		for(String parent : MethodVisitor.detailedGraph.keySet())
		{
			for(String child : MethodVisitor.detailedGraph.get(parent))
			{
				if(!tmpDetailedGraph.containsKey(child))
					tmpDetailedGraph.put(child, new HashSet<>());
			}
		}
		long endCall = System.currentTimeMillis();
		long totalCallgraph = endCall - startCall;

		MethodVisitor.detailedGraph.clear();
		MethodVisitor.detailedGraph.putAll(tmpDetailedGraph);
		tmpDetailedGraph.clear();
		tmpDetailedGraph = null;

		// Initiate Forward Slicing Algorithm
		System.out.println("\nStarting Forward Slicing...");
		long startForwardSlice = System.currentTimeMillis();
		// Create CFG Object file
		CFG cfg = new CFG(MethodVisitor.detailedGraph);
		// Parse the DOT versions of DOT files
		cfg.dotParser(dot.getMyDOTFiles());
		long endForwardSlice = System.currentTimeMillis();
		long totalForwardSlice = endForwardSlice - startForwardSlice;
		System.out.println("Forward Slicing Complete!\n");
		fillMissingData();

		long startChange = System.currentTimeMillis();
		// Calculate Method Changes
		ChangedMethodProbability change = new ChangedMethodProbability(args[0], args[1], AlgorithmTag.FORWARD_SLICE_STATEMENT);
		long endChange = System.currentTimeMillis();

		long totalChange = endChange - startChange;

		// Create own call graph that also contains method change rates
		long startConstruct = System.currentTimeMillis();
		MarkovGraphConstruction mgc = new MarkovGraphConstruction();
		
		// Set the project directory 
		MarkovChains mc = new MarkovChains(change,predictionOutputFile);

		long end = System.currentTimeMillis();

		long total = end - start;
		totalCalc = endCalc - startCalc;
		totalConstruct = endConstruct - startConstruct;

		// Print Runtime info
		System.out.println("Callgraph Time: "+totalCallgraph/1000.0+" seconds");
		System.out.println("Change Calculation Time: "+totalChange/1000.0+" seconds");
		System.out.println("Forward Slicing Time: "+totalForwardSlice/1000.0+" seconds");
		System.out.println("Markov Construction Time: "+totalConstruct/1000.0+" seconds");
		System.out.println("Markov Calculation Time: "+totalCalc/1000.0+" seconds");
		System.out.println("Total Time: "+total/1000.0+" seconds");
	}
	
	
	/**
	 * Fills the missing data for external methods, which does not have code implementation, such as built-in methods
	 * Example: System.out.println(), toString(), etc.
	 */
	private static void fillMissingData() {
		for(String caller : MethodVisitor.detailedGraph.keySet())
		{
			if(!ForwardSlicer.callerForwardSliceInfo.containsKey(caller))
			{
				ForwardSlicer.callerForwardSliceInfo.put(caller, 0.0);
				ForwardSlicer.getCallerSlicedStatments().put(caller, new HashSet<>());
			}
		}
		
		for(String caller : MethodVisitor.detailedGraph.keySet())
		{
			if(!ForwardSlicer.calleeForwardSliceInfo.containsKey(caller))
			{
				ForwardSlicer.getCalleeSlicedStatments().put(caller, new HashMap<>());
			}

			if(ForwardSlicer.calleeForwardSliceInfo.containsKey(caller))
			{
				for(String callee : MethodVisitor.detailedGraph.get(caller))
				{
					if(!ForwardSlicer.calleeForwardSliceInfo.get(caller).containsKey(callee))
					{
						ForwardSlicer.calleeForwardSliceInfo.get(caller).put(callee, 0.0);
					}

					if(!ForwardSlicer.getCalleeSlicedStatments().get(caller).containsKey(callee))
					{
						ForwardSlicer.getCalleeSlicedStatments().get(caller).put(callee, new HashSet<>());
					}
				}
			}
		}
	}

}
