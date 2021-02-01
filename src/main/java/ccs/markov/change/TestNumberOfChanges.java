package ccs.markov.change;

import ccs.markov.slicer.AlgorithmTag;

public class TestNumberOfChanges {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("START DIFF PROCESS");
		ChangedMethodProbability change = new ChangedMethodProbability(args[0], args[1], AlgorithmTag.FORWARD_SLICE_STATEMENT);
		int cnt=0;
		for(String key : change.getMethodChangeProb().keySet()) {
//			System.out.println("Change: "+change.getMethodChangeProb().get(key)+", Method: "+key);
			if(change.getMethodChangeProb().get(key) > 0) {
//				System.out.println(key.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_"));
//				System.out.println(change.getMethodChangeProb().get(key)+" - "+key.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_"));
				System.out.println("Method: "+key+" Change Percentage: "+change.getMethodChangeProb().get(key));
//				System.out.println(key+"\t"+change.getMethodChangeProb().get(key));
				cnt++;
			}
		}
		System.out.println("Methods changed= "+cnt+"/"+change.getMethodChangeProb().keySet().size());
	}

}
