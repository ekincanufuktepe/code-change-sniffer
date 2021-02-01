package ccs.markov.change;

import java.util.HashMap;

import ccs.markov.slicer.AlgorithmTag;

public class ChangedMethodProbability {
	
	private HashMap<String, Double> methodChangeProb;// = new HashMap<>();
	
	public ChangedMethodProbability(String newFile, String oldFile, AlgorithmTag tag) {
		CaptureChangedClasses capture = new CaptureChangedClasses();
//		System.out.println("________________________________________");
		setMethodChangeProb(capture.compareClasses(newFile, oldFile, tag));
//		System.out.println("________________________________________");
	}

	public HashMap<String, Double> getMethodChangeProb() { 
//		System.out.println(methodChangeProb);
		return methodChangeProb;
	}

	public void setMethodChangeProb(HashMap<String, Double> methodChangeProb) {
		this.methodChangeProb = methodChangeProb;
	}

}
