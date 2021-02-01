package ccs.markov.slicer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Variables {
	
	private String variableName;
	private VariableTypes variableType;
	private Set<Integer> passedStatement = new HashSet<>();
	
	private HashMap<String, String> parameterClassType = new HashMap<>();

	public VariableTypes getVariableType() {
		return variableType;
	}

	public void setVariableType(VariableTypes variableType) {
		this.variableType = variableType;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Set<Integer> getPassedStatement() {
		return passedStatement;
	}

	public void setPassedStatement(Set<Integer> passedStatement) {
		this.passedStatement = passedStatement;
	}

	public HashMap<String, String> getParameterClassType() {
		return parameterClassType;
	}

	public void setParameterClassType(HashMap<String, String> parameterClassType) {
		this.parameterClassType = parameterClassType;
	}

}
