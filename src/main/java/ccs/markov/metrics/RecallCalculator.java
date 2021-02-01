package ccs.markov.metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RecallCalculator extends MetricCalculator implements MetricCalculatorStrategy {

	public RecallCalculator(int count, double threshold) {
		super(count, threshold);
	}

	@Override
	@Deprecated
	public void calculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		double countMatch = 0;
		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				if(relevant.contains(retrived.get(i)))
					countMatch++;
			}
		}

		System.out.println("Recall: "+(countMatch/relevant.size()));
	}

	@Override
	public void alternateCalculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		double countMatch = 0;
		Set<String> tmpRelevant = new HashSet<>();
		for(String relevantMethod : relevant) {
			tmpRelevant.add(relevantMethod);
		}
		
		for(String relevantMethod : relevant) {
			if(!retrived.contains(relevantMethod)) {
				tmpRelevant.remove(relevantMethod);
			}
				
		}

		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				if(tmpRelevant.contains(retrived.get(i)))
					countMatch++;
			}
		}
		System.out.println((countMatch/tmpRelevant.size()));
	}

}
