package ccs.markov.slicer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ForwardSlicer extends ProgramSlicer implements ISliceAlgorithm{
	private Set<Variables> analyzedVars;
	private Set<Variables> calleeAnalyzedVars = new HashSet<>();

	private String currentCallee;

	private HashMap<String, HashMap<Statement, Set<Statement>>> calleeCFGs = new HashMap<>();
	private HashMap<String, HashMap<Statement, Set<Statement>>> newCalleeCFGs = new HashMap<>();


	public static HashMap<String, Double> callerForwardSliceInfo = new HashMap<>();
	public static HashMap<String, HashMap<String, Double>> calleeForwardSliceInfo = new HashMap<>();

	public static HashMap<String, Double> callerForwardSliceInfoTotal = new HashMap<>();
	public static HashMap<String, HashMap<String, Double>> calleeForwardSliceInfoTotal = new HashMap<>();

	private static HashMap<String, Set<String>> callerSlicedStatments = new HashMap<>();
	public static HashMap<String, Set<String>> getCallerSlicedStatments() {
		return callerSlicedStatments;
	}

	public static void setCallerSlicedStatments(HashMap<String, Set<String>> callerSlicedStatments) {
		ForwardSlicer.callerSlicedStatments = callerSlicedStatments;
	}

	public static HashMap<String, HashMap<String, Set<String>>> getCalleeSlicedStatments() {
		return calleeSlicedStatments;
	}

	public static void setCalleeSlicedStatments(HashMap<String, HashMap<String, Set<String>>> calleeSlicedStatments) {
		ForwardSlicer.calleeSlicedStatments = calleeSlicedStatments;
	}

	private static HashMap<String, HashMap<String, Set<String>>> calleeSlicedStatments = new HashMap<>();

	@Override
	public void slice(HashMap<Statement, Set<Statement>> cfg, SliceCriterion criteria, String bsTag) {
		int startNode = -1;
		Statement startingStatement = null;

		// TODO: according to backward program slicing it starts from the node where
		// the criterion has bee defined, which is also an exclusive node.
		// Therefore the code below that has been used for finding the start node
		// is not necessary can be removed later.


		/**
		 * Different than Backward Slicing, Forward Slicing runs top-down. Therefore, we start
		 * from the top of the CFG.
		 * */
		// Process direct relevant statements
		for(Statement statement : cfg.keySet()){
			if(startNode == -1) {
				startNode = Integer.parseInt(statement.getNodeID().replace("\"", ""));
				startingStatement = statement;
			}
			else if(startNode > Integer.parseInt(statement.getNodeID().replace("\"", ""))) {
				startNode = Integer.parseInt(statement.getNodeID().replace("\"", ""));
				startingStatement = statement;
			}
		}

		if(bsTag.equals("SLICE_BY_PARAMETER")){
//			System.out.println(bsTag);
			HashMap<Statement, Set<Statement>> newCFG = traverse(criteria.getStatement(), cfg, criteria, bsTag, startingStatement);

			// Output for viewing the sliced statements
			double sliceCount = 0;
//			System.out.println("-------ANALYZED VARS - BASED ON PARAMETER-------");
//			for(Variables vars : analyzedVars)
//			{
//				System.out.print(vars.getVariableName()+", ");
//			}
//			System.out.println();


			String[] tmpName = criteria.getCfgName().split(" ");

			int beginIndex = tmpName[2].indexOf("(")+1;
			int endIndex = tmpName[2].indexOf(")");

			String cfgMethodName = tmpName[2].substring(0, beginIndex-1);

			String[] cfgMethodParams = tmpName[2].substring(beginIndex, endIndex).split(",");
			String params = "";

			for(int i=0; i<cfgMethodParams.length; i++) {
				if(cfgMethodParams.length==1) {
					params = params + cfgMethodParams[i];
				}
				else if(i < cfgMethodParams.length-1) {
					params = params + cfgMethodParams[i] + ", ";
				}
				else {
					params = params + cfgMethodParams[i];
				}
			}
			String cfgName = tmpName[0]+":"+cfgMethodName+":"+cfgMethodParams.length+":["+params+"]";

			if(params.equals(""))
				cfgName = tmpName[0]+":"+cfgMethodName+":"+"0"+":["+params+"]";

			callerSlicedStatments.put(cfgName, new HashSet<>());
			calleeSlicedStatments.put(cfgName, new HashMap<>());

			for(Statement statement : newCFG.keySet())
			{
				if(statement.isSliced())
				{
//					System.out.println(statement.getNodeID()+": SLICED: "+statement.getNodeName());
					sliceCount++;
					callerSlicedStatments.get(cfgName).add(statement.getNodeName());
				}
			}

			// print the statement-based sliced ratio.
//			System.out.println("Slice Ratio Based on Statement: "+sliceCount+"/"+newCFG.keySet().size());

//			System.out.println(sliceCount/newCFG.keySet().size());

			callerForwardSliceInfo.put(cfgName, sliceCount/newCFG.keySet().size());
			calleeForwardSliceInfo.put(cfgName, new HashMap<>());

			callerForwardSliceInfoTotal.put(cfgName, (double) newCFG.keySet().size());
			calleeForwardSliceInfoTotal.put(cfgName, new HashMap<>());
		}
		else if(bsTag.equals("SLICE_BY_CALLEE")) {
//			System.out.println("=============================================================================");
			for(String callee : criteria.getCalleeVars().keySet()) {
				for(Statement statement : cfg.keySet()) {
					if(startNode == -1) {
						startNode = Integer.parseInt(statement.getNodeID().replace("\"", ""));
					}
					else if(startNode > Integer.parseInt(statement.getNodeID().replace("\"", ""))) {
						startNode = Integer.parseInt(statement.getNodeID().replace("\"", ""));
					}
					statement.setSliced(false);
					statement.setFlag(false);
				}
				this.calleeCFGs.put(callee, new HashMap<>());
				this.calleeCFGs.get(callee).putAll(cfg);
			}

			for(String callee : this.calleeCFGs.keySet()) {

				this.currentCallee = callee;
			
				for(Statement statement : this.calleeCFGs.get(callee).keySet()) {
					statement.setSliced(false);
					statement.setFlag(false);
				}

				for(String sliced : criteria.getInitialSlicedStatements().get(callee)) {
					for(Statement statement : cfg.keySet()) {
						if(statement.getNodeName().equals(sliced))
							statement.setSliced(true);
					}
				}

				HashMap<Statement, Set<Statement>> newCFG = new HashMap<>();
				//				System.out.println("I AM SENDING THIS-->"+bsTag);
				newCFG.putAll(traverse(criteria.getStatement(), this.calleeCFGs.get(callee), criteria, bsTag, criteria.getStatement()));
				this.newCalleeCFGs.put(callee,newCFG);

//				System.out.println("-------ANALYZED VARS - BASED ON CALLEE:"+callee+" -------");
				//				System.out.println(calleeAnalyzedVars.size());
//				for(Variables vars : calleeAnalyzedVars)
//				{
//					System.out.print(vars.getVariableName()+", ");
//				}
//				System.out.println();

				String[] tmpName = criteria.getCfgName().split(" ");

				int beginIndex = tmpName[2].indexOf("(")+1;
				int endIndex = tmpName[2].indexOf(")");

				String cfgMethodName = tmpName[2].substring(0, beginIndex-1);

				String[] cfgMethodParams = tmpName[2].substring(beginIndex, endIndex).split(",");
				String params = "";

				for(int i=0; i<cfgMethodParams.length; i++)
				{
					if(cfgMethodParams.length==1)
					{
						params = params + cfgMethodParams[i];
					}
					else if(i < cfgMethodParams.length-1)
					{
						params = params + cfgMethodParams[i] + ", ";
					}
					else
					{
						params = params + cfgMethodParams[i];
					}
				}
				String cfgName = tmpName[0]+":"+cfgMethodName+":"+cfgMethodParams.length+":["+params+"]";

				if(params.equals(""))
					cfgName = tmpName[0]+":"+cfgMethodName+":"+"0"+":["+params+"]";

				calleeSlicedStatments.get(cfgName).put(callee, new HashSet<>());

				double sliceCount = 0;
				for(Statement statement : this.newCalleeCFGs.get(callee).keySet()) {
					if(statement.isSliced()) {
						// print sliced statements
						calleeSlicedStatments.get(cfgName).get(callee).add(statement.getNodeName());
						sliceCount++;
					}
				}

				calleeForwardSliceInfo.get(cfgName).put(callee, sliceCount/this.newCalleeCFGs.get(callee).keySet().size());
				calleeForwardSliceInfoTotal.get(cfgName).put(callee,(double) this.newCalleeCFGs.get(callee).keySet().size());
				if(!callerForwardSliceInfoTotal.containsKey(callee))
					callerForwardSliceInfoTotal.put(callee,(double) this.newCalleeCFGs.get(callee).keySet().size());
			}
		}
	}

	@Override
	public HashMap<Statement, Set<Statement>> traverse(Statement statement, HashMap<Statement, Set<Statement>> cfg, SliceCriterion criteria, String tag, Statement startingStatement) {

		Set<Variables> extendSetOfVars = new HashSet<>();
		for(Statement findStatement : cfg.keySet())
		{
			if(findStatement.getId() == startingStatement.getId())
			{
				if(findStatement.isFlag() == false)
				{
					if(tag.equals("SLICE_BY_PARAMETER")) {
						for(Variables variable : criteria.getSetOfVars()) {
							String ruleSet[] = RegexRulesForForwardSlicingRules.rules(variable.getVariableName());
							String transitionRuleSet[] = RegexRulesForForwardSlicingTransitionRules.transitionRules(variable.getVariableName());
							for(int i=0; i<ruleSet.length; i++) {
								// Capture direct parameter usage
								if(findStatement.getNodeName().replaceAll("\\\\\"", "'").matches(ruleSet[i])) {
									findStatement.setSliced(true);
									for(int j=0; j<transitionRuleSet.length; j++) {
										if(findStatement.getNodeName().replaceAll("\\\\\"", "'").matches(transitionRuleSet[j])) {
											if((i>=4 && i<=7) || 
													(i>=0 && i<=1) ||
													(i>=12 && i<=15) ||
													(i>=20 && i<=24) ||
													(i>=28 && i<=31) ||
													(i>=34 && i<=35) ||
													(i>=38 && i<=39))
											{
												int firstIndex = findStatement.getNodeName().indexOf("\"")+1;
												int lastIndex = findStatement.getNodeName().indexOf(" = ");													

												Variables transitiveVariable = new Variables();
												transitiveVariable.setVariableName(findStatement.getNodeName().substring(firstIndex, lastIndex));
												transitiveVariable.setVariableType(VariableTypes.TRASITIVE_VARIABLE);

												if(transitiveVariable.getVariableName().contains(" ")) {
													String purifiedVar[] = transitiveVariable.getVariableName().split(" ");
													transitiveVariable.setVariableName(purifiedVar[purifiedVar.length-1]);
												}

												boolean insideSet = false;
												for(Variables var : criteria.getSetOfVars()) {
													if(var.getVariableName().equals(transitiveVariable.getVariableName())) {
														insideSet = true;
													}
												}
												if(!insideSet) {
													extendSetOfVars.add(transitiveVariable);
												}
											}
											else {
												int firstIndex = findStatement.getNodeName().indexOf("\"")+1;
												int lastIndex = findStatement.getNodeName().indexOf(" = ");
												Variables transitiveVariable = new Variables();
												transitiveVariable.setVariableName(findStatement.getNodeName().substring(firstIndex, lastIndex));
												transitiveVariable.setVariableType(VariableTypes.TRASITIVE_VARIABLE);

												if(transitiveVariable.getVariableName().contains(" ")) {
													String purifiedVar[] = transitiveVariable.getVariableName().split(" ");
													transitiveVariable.setVariableName(purifiedVar[purifiedVar.length-1]);
												}

												boolean insideSet = false;
												for(Variables var : criteria.getSetOfVars()) {
													if(var.getVariableName().equals(transitiveVariable.getVariableName())) {
														insideSet = true;
													}
												}
												if(!insideSet) {
													extendSetOfVars.add(transitiveVariable);
												}
											}
										}
									}
								}

							}
						}
						findStatement.setFlag(true);
						for(Statement child : cfg.get(findStatement)) {
							if(!extendSetOfVars.isEmpty()) {
								Set<Variables> copy = new HashSet<>();
								Set<Variables> copy2 = new HashSet<>();
								copy = extendSetOfVars;

								for(Variables var : copy) {
									boolean hasIt = false;
									copy2 = criteria.getSetOfVars();
									for(Variables var2 : copy2) {
										if(var.getVariableName().equals(var2.getVariableName())) {
											hasIt = true;
										}

									}
									if(!hasIt) {
										criteria.getSetOfVars().add(var);
									}
								}
								HashMap<Statement, Set<Statement>> copyCFG = cfg;
								for(Statement resetStatementFlags : copyCFG.keySet()) {
									resetStatementFlags.setFlag(false);
								}								
							}
							traverse(startingStatement, cfg, criteria, tag, child);
						}
					}
					else if(tag.equals("SLICE_BY_CALLEE")) {
						for(Variables variable : criteria.getCalleeVars().get(this.currentCallee)) {
							String varName = variable.getVariableName();
							if(variable.getVariableName().contains("(") && !variable.getVariableName().contains(")"))
								varName = variable.getVariableName().replace("(", "\\(");
							else if(variable.getVariableName().contains(")") && !variable.getVariableName().contains("("))
								varName = variable.getVariableName().replace(")", "\\)");


							String ruleSet[] = RegexRulesForForwardSlicingTransitionRules.rules(varName.replaceAll("\\\\\"", "'"));
							String transitionRuleSet[] = RegexRulesForForwardSlicingTransitionRules.transitionRules(varName.replaceAll("\\\\\"", "'"));

							for(int i=0; i<ruleSet.length; i++) {
								if(findStatement.getNodeName().matches(ruleSet[i])) {
									findStatement.setSliced(true);
									// if there is a transition to another variable from a direct used parameter
									// find the variable and trace it where it has been used
									for(int j=0; j<transitionRuleSet.length; j++) {
										if(findStatement.getNodeName().replaceAll("\\\\\"", "'").matches(transitionRuleSet[j])) {
											if((i>=4 && i<=7) || 
													(i>=0 && i<=1) ||
													(i>=12 && i<=15) ||
													(i>=20 && i<=24) ||
													(i>=28 && i<=31) ||
													(i>=34 && i<=35) ||
													(i>=38 && i<=39))
											{
												int firstIndex = findStatement.getNodeName().indexOf("\"")+1;
												int lastIndex = findStatement.getNodeName().indexOf(" = ");													

												Variables transitiveVariable = new Variables();
												transitiveVariable.setVariableName(findStatement.getNodeName().substring(firstIndex, lastIndex));
												transitiveVariable.setVariableType(VariableTypes.TRASITIVE_VARIABLE);

												if(transitiveVariable.getVariableName().contains(" ")) {
													String purifiedVar[] = transitiveVariable.getVariableName().split(" ");
													transitiveVariable.setVariableName(purifiedVar[purifiedVar.length-1]);
												}
												boolean insideSet = false;
												for(Variables var : criteria.getCalleeVars().get(this.currentCallee)) {
													if(var.getVariableName().equals(transitiveVariable.getVariableName())) {
														insideSet = true;
													}
												}
												if(!insideSet) {
													extendSetOfVars.add(transitiveVariable);
												}
											}
											else {
												int firstIndex = findStatement.getNodeName().indexOf("\"")+1;
												int lastIndex = findStatement.getNodeName().indexOf(" = ");
												Variables transitiveVariable = new Variables();
												transitiveVariable.setVariableName(findStatement.getNodeName().substring(firstIndex, lastIndex));
												transitiveVariable.setVariableType(VariableTypes.TRASITIVE_VARIABLE);

												if(transitiveVariable.getVariableName().contains(" ")) {
													String purifiedVar[] = transitiveVariable.getVariableName().split(" ");
													transitiveVariable.setVariableName(purifiedVar[purifiedVar.length-1]);
												}

												boolean insideSet = false;
												for(Variables var : criteria.getCalleeVars().get(this.currentCallee)) {
													if(var.getVariableName().equals(transitiveVariable.getVariableName())) {
														insideSet = true;
													}
												}
												if(!insideSet) {
													extendSetOfVars.add(transitiveVariable);
												}
											}
										}
									}
								}
							}
						}
						for(Statement child : cfg.get(findStatement)) {
							if(!extendSetOfVars.isEmpty()) {
								Set<Variables> copy = new HashSet<>();
								Set<Variables> copy2 = new HashSet<>();
								copy = extendSetOfVars;

								for(Variables var : copy) {
									boolean hasIt = false;
									copy2 =  criteria.getCalleeVars().get(this.currentCallee);
									for(Variables var2 : copy2) {
										if(var.getVariableName().equals(var2.getVariableName())) {
											hasIt = true;
										}
									}
									if(!hasIt)
										criteria.getCalleeVars().get(this.currentCallee).add(var);
								}
								HashMap<Statement, Set<Statement>> copyCFG = cfg;
								for(Statement resetStatementFlags : copyCFG.keySet()) {
									resetStatementFlags.setFlag(false);
								}
								traverse(startingStatement, cfg, criteria, tag, child);
							}
						}
					}
				}					
			}
		}
		if(tag.equals("SLICE_BY_PARAMETER"))
			analyzedVars = criteria.getSetOfVars();
		else
			calleeAnalyzedVars = criteria.getCalleeVars().get(currentCallee);
		return cfg;
	}

}
