package ccs.markov.metrics;

import java.util.ArrayList;
import java.util.Set;

public interface MetricCalculatorStrategy {
	
	@Deprecated
	public void calculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb);
	
	public void alternateCalculate(Set<String> relevant, ArrayList<String> retrived, ArrayList<Double> retrivedProb);

}
