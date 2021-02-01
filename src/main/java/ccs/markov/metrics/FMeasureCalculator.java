package ccs.markov.metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FMeasureCalculator extends MetricCalculator implements MetricCalculatorStrategy {

	public FMeasureCalculator(int count, double threshold) {
		super(count, threshold);
	}

	@Override
	@Deprecated
	public void calculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		// TODO Auto-generated method stub
		double countMatch = 0;
		double countRetSize = 0;

		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				countRetSize++;
				if(relevant.contains(retrived.get(i)))
					countMatch++;
			}
		}
		
		double precision = countMatch/countRetSize;
		double recall = countMatch/relevant.size();
		
		double fMeasure = 2*((precision*recall)/(precision+recall));
		
		System.out.println("Match: "+countMatch);
		System.out.println("Total Retrieved: "+countRetSize);
		System.out.println("Total Relavant: "+relevant.size());
		System.out.println("F-Measure: "+fMeasure);
	}

	@Override
	public void alternateCalculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		// TODO Auto-generated method stub
		
		Set<String> tmpRelevant = new HashSet<>();
		for(String relevantMethod : relevant) {
			tmpRelevant.add(relevantMethod);
		}
		
		for(String relevantMethod : relevant) {
			if(!retrived.contains(relevantMethod)) {
				tmpRelevant.remove(relevantMethod);
			}
				
		}
		
		double countMatch = 0;
		double countRetSize = 0;

		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				countRetSize++;
				if(tmpRelevant.contains(retrived.get(i)))
					countMatch++;
			}
		}
		
		
		double precision = countMatch/countRetSize;
		double recall = countMatch/tmpRelevant.size();
		
		double fMeasure = 2*((precision*recall)/(precision+recall));
		
//		System.out.println("Match: "+countMatch);
//		System.out.println("Total Retrieved: "+countRetSize);
//		System.out.println("Total Relavant: "+tmpRelevant.size());
//		System.out.println("Alternate #1 F-Measure: "+fMeasure);
		System.out.println(fMeasure);
		
	}

	
}
