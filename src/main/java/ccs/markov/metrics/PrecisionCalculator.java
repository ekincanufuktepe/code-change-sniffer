package ccs.markov.metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PrecisionCalculator extends MetricCalculator implements MetricCalculatorStrategy {

	public PrecisionCalculator(int count, double threshold) {
		super(count, threshold);
	}

	@Override
	@Deprecated
	public void calculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		double countMatch = 0;
		double countRetSize = 0;
		
		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				countRetSize++;
				if(relevant.contains(retrived.get(i)))
					countMatch++;
			}
		}
 
//		System.out.println("Match: "+countMatch);
//		System.out.println("TotalRet: "+countRetSize);
		System.out.println("Precision: "+(countMatch/countRetSize));
	}

	@Override
	public void alternateCalculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb) {
		// TODO Auto-generated method stub
		double countMatch = 0;
		double countRetSize = 0;
		
		Set<String> tmpRelevant = new HashSet<>();
		for(String relevantMethod : relevant) {
			tmpRelevant.add(relevantMethod);
		}
		
		for(String relevantMethod : relevant) {
			if(!retrived.contains(relevantMethod))
			{
//				System.out.println("REMOVED");
				tmpRelevant.remove(relevantMethod);
			}
				
		}
		
		for(int i=0; i<retrived.size(); i++) {
			if(threshold < retrivedProb.get(i)) {
				countRetSize++;
				if(tmpRelevant.contains(retrived.get(i)))
					countMatch++;
			}
		}
 
//		System.out.println("Match ALT: "+countMatch);
//		System.out.println("TotalRet ALT: "+countRetSize);
//		System.out.println("Alternate #1 Precision: "+(countMatch/countRetSize));
		System.out.println((countMatch/countRetSize));
	}


}
