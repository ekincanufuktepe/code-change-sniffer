package ccs.markov.slicer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SliceCriterion {
	
	private CFG controlFlowGraph;
	private Statement statement;
	private Set<Variables> setOfVars;
	private Set<Variables> setOfVarsForCallees;
	
	private HashMap<String, Set<Variables>> calleeVars;
	private HashMap<String, Set<String>> initialSlicedStatements;
	
	public HashMap<String, Set<Variables>> getCalleeVars() {
		return calleeVars;
	}

	public void setCalleeVars(HashMap<String, Set<Variables>> calleeVars) {
		this.calleeVars = calleeVars;
	}

	public HashMap<String, Set<String>> getInitialSlicedStatements() {
		return initialSlicedStatements;
	}

	public void setInitialSlicedStatements(HashMap<String, Set<String>> initialSlicedStatements) {
		this.initialSlicedStatements = initialSlicedStatements;
	}

	private HashMap<String, Set<String>> callgraph;
	private String cfgName;
	
	public SliceCriterion(SliceCriterion newCriteria) {
		this.controlFlowGraph = newCriteria.controlFlowGraph;
		this.statement = newCriteria.statement;
		this.setOfVars = newCriteria.setOfVars;
	}
	
	/**
	 * This constructor is for the method parameter
	 * slicing criterion 
	 * */
	public SliceCriterion(HashMap<Statement, Set<Statement>> cfg, String CFGName) {
		//method parameter
		this.cfgName = CFGName;
		initVariables(cfg);
		initStatement(cfg);
	}
	
	public String getCfgName() {
		return cfgName;
	}

	public void setCfgName(String cfgName) {
		this.cfgName = cfgName;
	}

	/**
	 * This constructor is for the method callee
	 * slicing criterion. Uses callgraph to extract the callees 
	 * */
	public SliceCriterion(HashMap<Statement, Set<Statement>> cfg, HashMap<String, Set<String>> callgraph, String CFGName) {	
		this.calleeVars = new HashMap<>();
		this.initialSlicedStatements = new HashMap<>();
		this.callgraph = callgraph;
		this.cfgName = CFGName;

		//method callees
		initVariablesForCallee(cfg);
//		System.out.println("CALLEES:");
//		for(Variables var : setOfVarsForCallees)
//		{
//			System.out.println("\t"+var.getVariableName());
//		}

		
		initStatement(cfg);
	}
	
	/**
	 * This method initializes slicing criterion based on the method's
	 * defined parameter.
	 * */
	private void initVariables(HashMap<Statement, Set<Statement>> cfg)
	{
		setOfVars = new HashSet<>();
		for(Statement node : cfg.keySet())
		{
			if(node.getNodeName().contains(" := @parameter"))
			{
//				System.out.println("EQUAL2");
				// Set parameters as a variables
				int firstIndex = node.getNodeName().indexOf("\"")+1;
				int lastIndex = node.getNodeName().indexOf(":=")-1;
				String variable = node.getNodeName().substring(firstIndex, lastIndex);
				Variables var = new Variables();
				var.setVariableName(variable);
				var.setVariableType(VariableTypes.PARAMETER);
				for(Statement node2 : cfg.keySet())
				{
					var.getPassedStatement().add(node2.getId());
				}
				node.setSliced(true);
				this.setOfVars.add(var);
			}
		}
	}
	
	/**
	 * This method initializes slicing criterion based on the method's
	 * callee methods. The callee methods are used as return values.
	 * */
	private void initVariablesForCallee(HashMap<Statement, Set<Statement>> cfg)
	{
		this.setOfVarsForCallees = new HashSet<>();
		for(String caller : this.callgraph.keySet())
		{
			String[] cfgNameTokens = this.cfgName.split(" ");
			
			int beginIndex = cfgNameTokens[2].indexOf("(")+1;
			int endIndex = cfgNameTokens[2].indexOf(")");
			
			String[] cfgMethodParams = cfgNameTokens[2].substring(beginIndex, endIndex).split(",");
			
//			int redundantPartIndex = cfgNameTokens[0].indexOf(".")+1;
//			String newCfgName = cfgNameTokens[0].substring(redundantPartIndex)+"."+cfgNameTokens[2];
			String newCfgName = cfgNameTokens[0]+"."+cfgNameTokens[2];
							
			String[] tokensOfCaller = caller.split(":");
			
			tokensOfCaller[3]= "("+tokensOfCaller[3].substring(1, tokensOfCaller[3].length()-1)+")";
			String newCaller = tokensOfCaller[0]+"."+tokensOfCaller[1]+tokensOfCaller[3];
			newCaller = newCaller.replace(" ", "");
			
//			System.out.println(newCfgName);
//			System.out.println(newCaller);
			if(newCfgName.equals(newCaller))
			{
//				System.out.println("EQUAL");
				for(String callee : this.callgraph.get(caller))
				{
					String[] tmpTokensOfCallee = callee.split(":"); 
					
					tmpTokensOfCallee[3]= "("+tmpTokensOfCallee[3].substring(1, tmpTokensOfCallee[3].length()-1)+")";
					String tmpNewCallee = tmpTokensOfCallee[0]+"."+tmpTokensOfCallee[1]+tmpTokensOfCallee[3];
					tmpNewCallee = tmpNewCallee.replace(" ", "");

					int cropStopIndex = tmpNewCallee.indexOf("(");
					String cropParameters = tmpNewCallee.substring(0, cropStopIndex);
					
					for(Statement node : cfg.keySet())
					{
						// This statement is necessary to find the callee if they are called via parameter object
						for(int i=0; i<cfgMethodParams.length; i++)
						{
							if(node.getNodeName().contains(" := @parameter"+i+"\"") && callee.startsWith(cfgMethodParams[i]) && !node.isSliced())
							{
								/**
								 * TODO: Parameter IDs must be found and cross referenced
								 * with the type (Object class) of their parameter.
								 * Then the detected object class will be replaced with its
								 * referenced variable. 
								 * */
								// Set parameters as a variables
								int firstIndex = node.getNodeName().indexOf("\"")+1;
								int lastIndex = node.getNodeName().indexOf(":=")-1;
								String variable = node.getNodeName().substring(firstIndex, lastIndex);
								Variables var = new Variables();
								var.setVariableName(variable);
								var.setVariableType(VariableTypes.CALLEE_THROUGH_PARAMETER);
								
								var.getParameterClassType().put(variable, cfgMethodParams[i]);
								
//								System.out.println("CRITERION VAR: "+variable+" -:- "+cfgMethodParams[i]);
//								System.out.println(node.getNodeName());
								
								for(Statement node2 : cfg.keySet()){
									var.getPassedStatement().add(node2.getId());
								}
								
								node.setSliced(true);
								this.setOfVarsForCallees.add(var);
							}
						}
						
						Set<Variables> tmpSetOfVarsForCallees = new HashSet<>();
						tmpSetOfVarsForCallees.addAll(this.setOfVarsForCallees);
						
						for(Variables variables: tmpSetOfVarsForCallees)
						{
							for(String calleeControl : this.callgraph.get(caller))
							{
								if(variables.getParameterClassType().get(variables.getVariableName()) != null)
								{
										String[] tokensOfCallee = calleeControl.split(":");
										tokensOfCallee[3]= "("+tokensOfCallee[3].substring(1, tokensOfCallee[3].length()-1)+")";
										String tmpCalleeControl = tokensOfCallee[0]+"."+tokensOfCallee[1]+tokensOfCallee[3];
										tmpCalleeControl = tmpCalleeControl.replace(" ", "");


										if(node.getNodeName().contains(variables.getVariableName()+"."+tokensOfCallee[1]) && 
												!node.isSliced() &&
												node.getNodeName().contains(" = "))
										{
//											System.out.println(node.getNodeName());
											int firstIndex = node.getNodeName().indexOf("\"")+1;
											int lastIndex = node.getNodeName().indexOf("=", firstIndex)-1;
											String variable = node.getNodeName().substring(firstIndex, lastIndex);
											
											Variables var = new Variables();
											var.setVariableName(variable);
											var.setVariableType(VariableTypes.CALLEE);
											
											if(this.calleeVars.containsKey(calleeControl))
											{
												if(var.getVariableName().contains(" ") && var.getVariableName().contains("label"))
												{
													String purifiedVar[] = var.getVariableName().split(" ");
													var.setVariableName(purifiedVar[purifiedVar.length-1]);
												}
												
												this.calleeVars.get(calleeControl).add(var);
												
												this.initialSlicedStatements.get(calleeControl).add(node.getNodeName());
											}
											else
											{
												if(var.getVariableName().contains(" ") && var.getVariableName().contains("label"))
												{
													String purifiedVar[] = var.getVariableName().split(" ");
													var.setVariableName(purifiedVar[purifiedVar.length-1]);
												}
												
												this.calleeVars.put(calleeControl, new HashSet<Variables>());
												this.calleeVars.get(calleeControl).add(var);
												
												this.initialSlicedStatements.put(calleeControl, new HashSet<String>());
												this.initialSlicedStatements.get(calleeControl).add(node.getNodeName());
											}
											
											for(Statement node2 : cfg.keySet())
											{
												var.getPassedStatement().add(node2.getId());
											}
											
											node.setSliced(true);
											this.setOfVarsForCallees.add(var);
										}
										
										if(node.getNodeName().contains(variables.getVariableName()+"."+tokensOfCallee[1]) && 
												!node.isSliced() &&
												!node.getNodeName().contains(" = "))
										{
											if(node.getNodeName().contains(":"))
											{
												int firstIndex = node.getNodeName().indexOf("\"")+1;
												int lastIndex = node.getNodeName().indexOf(":", firstIndex);
												String variable = node.getNodeName().substring(firstIndex, lastIndex);
//												System.out.println(variable);
												Variables var = new Variables();
												var.setVariableName(variable);
												var.setVariableType(VariableTypes.CALLEE);
												
												if(this.calleeVars.containsKey(calleeControl))
												{
													this.calleeVars.get(calleeControl).add(var);
													
													this.initialSlicedStatements.get(calleeControl).add(node.getNodeName());
												}
													
												else
												{
													this.calleeVars.put(calleeControl, new HashSet<Variables>());
													this.calleeVars.get(calleeControl).add(var);
													
													this.initialSlicedStatements.put(calleeControl, new HashSet<String>());
													this.initialSlicedStatements.get(calleeControl).add(node.getNodeName());
												}
												
												for(Statement node2 : cfg.keySet())
												{
													var.getPassedStatement().add(node2.getId());
												}
												this.setOfVarsForCallees.add(var);
											}
//											System.out.println(node.getNodeName());
											node.setSliced(true);
										}
								}
								
							}
						}
						
						tmpSetOfVarsForCallees.clear();
						tmpSetOfVarsForCallees = null;
						
						int classInfoStopIndex = callee.indexOf(":");
						String classInfo = callee.substring(0, classInfoStopIndex);

						if(node.getNodeName().contains(" "+classInfo+"\"") && !node.isSliced())
						{
							int firstIndex = node.getNodeName().indexOf("\"")+1;
							int lastIndex = node.getNodeName().indexOf("=", firstIndex)-1;
							String variable = node.getNodeName().substring(firstIndex, lastIndex);
							
							Variables var = new Variables();
							var.setVariableName(variable);
							var.setVariableType(VariableTypes.CALLEE_THROUGH_OBJECT_CREATION);
							
							if(this.calleeVars.containsKey(callee))
							{
								if(var.getVariableName().contains(" "))
								{
									String purifiedVar[] = var.getVariableName().split(" ");
									var.setVariableName(purifiedVar[purifiedVar.length-1]);
								}
								
								this.calleeVars.get(callee).add(var);
								
								this.initialSlicedStatements.get(callee).add(node.getNodeName());
							}
								
							else
							{
								if(var.getVariableName().contains(" "))
								{
									String purifiedVar[] = var.getVariableName().split(" ");
									var.setVariableName(purifiedVar[purifiedVar.length-1]);
								}
								
								this.calleeVars.put(callee, new HashSet<Variables>());
								this.calleeVars.get(callee).add(var);
								
								this.initialSlicedStatements.put(callee, new HashSet<String>());
								this.initialSlicedStatements.get(callee).add(node.getNodeName());
							}
							
							for(Statement node2 : cfg.keySet())
							{
								var.getPassedStatement().add(node2.getId());
							}
							
							node.setSliced(true);
							this.setOfVarsForCallees.add(var);
							/**
							 * TODO: Parameters with object class type are controlled with the
							 * defined is statement above. However, an object class type variable
							 * that has been defined inside the method's scope is not check, and we
							 * do not have any idea about it. Therefore, an example should be demonstrated
							 * and examined!!!
							 * 
							 * Example already demonstrated. Thereby, object class types are that are not
							 * defined/given as a parameter but defined inside the scope of a method are 
							 * shown in the cfg an referenced with variables similar to parameters. They are 
							 * show as an instance of an object. The could be used with "new" or with out "new".
							 * The key is to find them by their object class info and cross reference them.
							 * */
							
						}
													
						// This if statement detects the callee among the all statements in the CFG
						if(node.getNodeName().contains(cropParameters))
						{
							if(node.getNodeName().contains(" = ") && node.getNodeName().contains(":"))
							{
								int firstIndex = node.getNodeName().indexOf(" ")+1;
								int lastIndex = node.getNodeName().indexOf("=", firstIndex)-1;
								String variable;
								
								if(firstIndex > lastIndex)
								{
									firstIndex = node.getNodeName().indexOf("\"")+1;
									lastIndex = node.getNodeName().indexOf("=", firstIndex)-1;
									variable = node.getNodeName().substring(firstIndex, lastIndex);
								}
								else
								{
									variable = node.getNodeName().substring(firstIndex, lastIndex);
								}
								
								
								Variables var = new Variables();
								var.setVariableName(variable);
								var.setVariableType(VariableTypes.CALLEE_THROUGH_SCOPE_DEF);
								
								if(this.calleeVars.containsKey(callee))
								{
									
									if(var.getVariableName().contains(" "))
									{
										String purifiedVar = var.getVariableName().replace(" ", "");
										var.setVariableName(purifiedVar);
									}
									
									this.calleeVars.get(callee).add(var);
									
									this.initialSlicedStatements.get(callee).add(node.getNodeName());
								}
								else
								{
									if(var.getVariableName().contains(" "))
									{
										String purifiedVar = var.getVariableName().replaceAll(" ", "");
										var.setVariableName(purifiedVar);
									}
									this.calleeVars.put(callee, new HashSet<Variables>());
									this.calleeVars.get(callee).add(var);
									
									this.initialSlicedStatements.put(callee, new HashSet<String>());
									this.initialSlicedStatements.get(callee).add(node.getNodeName());
								}
								
								for(Statement node2 : cfg.keySet())
								{
									var.getPassedStatement().add(node2.getId());
								}
								
								
								this.setOfVarsForCallees.add(var);
							}
							else if(node.getNodeName().contains(" = "))
							{
								int firstIndex = node.getNodeName().indexOf("\"")+1;
								int lastIndex = node.getNodeName().indexOf("=", firstIndex)-1;
//								System.out.println(firstIndex+" "+lastIndex);
								String variable = node.getNodeName().substring(firstIndex, lastIndex);
								
								Variables var = new Variables();
								var.setVariableName(variable);
								var.setVariableType(VariableTypes.CALLEE_THROUGH_SCOPE_DEF);
								
								if(this.calleeVars.containsKey(callee))
								{
									
									if(var.getVariableName().contains(" "))
									{
										String purifiedVar = var.getVariableName().replace(" ", "");
										var.setVariableName(purifiedVar);
									}
									
									this.calleeVars.get(callee).add(var);
									
									this.initialSlicedStatements.get(callee).add(node.getNodeName());
								}
								else
								{
									if(var.getVariableName().contains(" "))
									{
										String purifiedVar = var.getVariableName().replaceAll(" ", "");
										var.setVariableName(purifiedVar);
									}
									
									this.calleeVars.put(callee, new HashSet<Variables>());
									this.calleeVars.get(callee).add(var);
									
									this.initialSlicedStatements.put(callee, new HashSet<String>());
									this.initialSlicedStatements.get(callee).add(node.getNodeName());
								}
								
								for(Statement node2 : cfg.keySet())
								{
									var.getPassedStatement().add(node2.getId());
								}
								
								
								this.setOfVarsForCallees.add(var);
							}
							node.setSliced(true);
						}
					}
				}
			}
			
		}
		
		setOfVarsForCallees = new HashSet<>();
		// just for printing purposes
//		int cnt=0;
//		for(String callee : calleeVars.keySet())
//		{
//			System.out.println("Callee: "+callee);
//			for(Variables varvar : calleeVars.get(callee))
//			{
//				System.out.println("\tVariable: "+varvar.getVariableName());
//				cnt++;
//			}
//		}
//		System.out.println(cnt);
	}
	
	
	private void initStatement(HashMap<Statement, Set<Statement>> cfg)
	{
		String maxNodeID = "";
		for(Statement node : cfg.keySet())
		{
			if(cfg.get(node).isEmpty())
			{
				if(this.statement != null)
				{
					String tmpOld = maxNodeID.replace("\"", "");
					String tmpNew = node.getNodeID().replace("\"", "");
					
					Integer oldMax = Integer.parseInt(tmpOld);
					Integer newMax = Integer.parseInt(tmpNew);
					
					if(oldMax < newMax)
					{
						maxNodeID = node.getNodeID();
						this.statement = new Statement(maxNodeID, node.getNodeName());
					}
				}
				else
				{
					maxNodeID = node.getNodeID();
					this.statement = new Statement(maxNodeID, node.getNodeName());
				}
				
			}
			
			
			
		}
	}
	
	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public Set<Variables> getSetOfVars() {
		return setOfVars;
	}

	public void setSetOfVars(Set<Variables> setOfVars) {
		this.setOfVars = setOfVars;
	}

	public Set<Variables> getSetOfVarsForCallees() {
		return setOfVarsForCallees;
	}

	public void setSetOfVarsForCallees(Set<Variables> setOfVarsForCallees) {
		this.setOfVarsForCallees = setOfVarsForCallees;
	}

	

}
