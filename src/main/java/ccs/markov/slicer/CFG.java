package ccs.markov.slicer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CFG {

	private String CFGName;
	// A single CFG
	private HashMap<Statement, Set<Statement>> cfg;

	private HashMap<String, Set<String>> callgraph;

	public HashMap<Statement, Set<Statement>> getCfg() {
		return cfg;
	}

	public void setCfg(HashMap<Statement, Set<Statement>> cfg) {
		this.cfg = cfg;
	}

	public Set<CFG> getCfgInfoSet() {
		return cfgInfoSet;
	}

	public void setCfgInfoSet(Set<CFG> cfgInfoSet) {
		this.cfgInfoSet = cfgInfoSet;
	}

	// Set CFG info of branch, statement and after slice information
	private Set<CFG> cfgInfoSet = new HashSet<>();

	private int numOfStatements = 0;
	private int numOfBranches = 0;
	private int afterSliceStatements = 0;
	private int afterSliceBranches = 0;

	public CFG() {
		cfg = new HashMap<>();
		new HashSet<>();
	}

	public CFG(HashMap<String, Set<String>> callgraph)
	{
		this.callgraph = callgraph;
	}

	public CFG(int statements, int branches, int afterSliceStatements, int afterSliceBranches) {
		this.numOfStatements = statements;
		this.numOfBranches = branches;
		this.afterSliceStatements = afterSliceStatements;
		this.afterSliceBranches = afterSliceBranches;
	}


	public int getNumOfStatements() {
		return numOfStatements;
	}

	public String getCFGName() {
		return CFGName;
	}

	public void setCFGName(String cFGName) {
		CFGName = cFGName;
	}

	public void setNumOfStatements(int numOfStatements) {
		this.numOfStatements = numOfStatements;
	}

	public int getNumOfBranches() {
		return numOfBranches;
	}

	public void setNumOfBranches(int numOfBranches) {
		this.numOfBranches = numOfBranches;
	}

	public int getAfterSliceStatements() {
		return afterSliceStatements;
	}

	public void setAfterSliceStatements(int afterSliceStatements) {
		this.afterSliceStatements = afterSliceStatements;
	}

	public int getAfterSliceBranches() {
		return afterSliceBranches;
	}

	public void setAfterSliceBranches(int afterSliceBranches) {
		this.afterSliceBranches = afterSliceBranches;
	}

	// Process every DOT file one by one
	public void dotParser(ArrayList<File> dotFiles)
	{
		for(int i=0; i<dotFiles.size(); i++)
		{
			cfg = new HashMap<>();
			CFG cfgInfo;
			BufferedReader br = null;
			int branchCount = 0;
			try {
				String sCurrentLine;
				String CFGName = "";

				br = new BufferedReader(new FileReader(dotFiles.get(i)));
				CFGName = dotFiles.get(i).getName().substring(0, dotFiles.get(i).getName().length()-4);
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.trim();

					// Regex to capture nodes and their definitions
					String nodeRegex = "\"[0-9]+\" \\[.*$";	
					// Create nodes of CFG
					if(sCurrentLine.matches(nodeRegex)) {
						//System.out.println(sCurrentLine);
						int IDEndIndex = sCurrentLine.indexOf(" ");
						String nodeID = sCurrentLine.substring(0, IDEndIndex);
						//						System.out.println("ID: "+nodeID);
						int nodeNameStartIndex = sCurrentLine.indexOf("[");
						String nodeName = sCurrentLine.substring(nodeNameStartIndex);
						//						System.out.println("Name: "+nodeName);
						Statement node = new Statement(nodeID, nodeName);
						cfg.put(node, new HashSet<Statement>());

					}

					// Regex to capture edges in .DOT CFG
					String edgeRegex = "\"[0-9]+\"\\-\\>\\\"[0-9]+\\\".*$";
					// Find and add the edges from .DOT
					if(sCurrentLine.matches(edgeRegex)) {
						int fromNodeIndex = sCurrentLine.indexOf("-");
						String fromNode = sCurrentLine.substring(0, fromNodeIndex);
						int toNodeIndex = sCurrentLine.indexOf("\"", fromNodeIndex);
						String toNode = sCurrentLine.substring(toNodeIndex,sCurrentLine.length()-1);

						for(Statement fromNodes : cfg.keySet()) {
							if(fromNodes.getNodeID().equals(fromNode)) {
								for(Statement toNodes : cfg.keySet()) {
									if(toNodes.getNodeID().equals(toNode)) {
										cfg.get(fromNodes).add(toNodes);
										toNodes.getParent().add(fromNodes);
										branchCount++;
									}
								}
							}
						}

					}
				}
				br.close();
				// Initialize CFG info 
				cfgInfo = new CFG(cfg.keySet().size(), branchCount, 0, 0);
				ForwardSlicer fs = new ForwardSlicer();

				// Automatically generate the slicing criterion
				// Variables are the parameters of the methods
				// The statement is the last (sink) node of the CFG 
				
				//Copy the CFG to CFG2 for different slice criterion
				HashMap<Statement, Set<Statement>> cfg2 = new HashMap<>();
				for(Statement state : cfg.keySet())
				{
					Statement tmpState = new Statement(state.getNodeID(), state.getNodeName());
					cfg2.put(tmpState, new HashSet<>());
					for(Statement state2 : cfg.get(state))
					{
						Statement tmpState2 = new Statement(state2.getNodeID(), state2.getNodeName());
						cfg2.get(tmpState).add(tmpState2);
					}
				}
	
				SliceCriterion criteria = new SliceCriterion(cfg, CFGName);
				fs.slice(cfg, criteria, "SLICE_BY_PARAMETER");
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				
				SliceCriterion criteriaCallee = new SliceCriterion(cfg2, callgraph, CFGName);
				
				fs.slice(cfg, criteriaCallee, "SLICE_BY_CALLEE");

				// Collect CFG info in a set
				cfgInfoSet.add(cfgInfo);
			} catch (IOException e) {

				e.printStackTrace();

			}

		}
	}

}
