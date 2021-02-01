package ccs.markov;

import java.util.ArrayList;

public class MethodNode {
	
	private String methodName;
	private Double methodChange;
	private ArrayList<String> toMethodName;
	private ArrayList<Double> toMethodProbability;
	
	public MethodNode(String methodName, Double methodChange) {
		this.methodName = methodName;
		this.setMethodChange(methodChange);
		toMethodName = new ArrayList<>();
		toMethodProbability = new ArrayList<>();
	}
	
	public void addToMethod(String toMethodName, Double probability) {
		this.toMethodName.add(toMethodName);
		this.toMethodProbability.add(probability);
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public ArrayList<String> getToMethodName() {
		return toMethodName;
	}

	public void setToMethodName(ArrayList<String> toMethodName) {
		this.toMethodName = toMethodName;
	}

	public ArrayList<Double> getToMethodProbability() {
		return toMethodProbability;
	}

	public void setToMethodProbability(ArrayList<Double> toMethodProbability) {
		this.toMethodProbability = toMethodProbability;
	}

	public Double getMethodChange() {
		return methodChange;
	}

	public void setMethodChange(Double methodChange) {
		this.methodChange = methodChange;
	}

}
