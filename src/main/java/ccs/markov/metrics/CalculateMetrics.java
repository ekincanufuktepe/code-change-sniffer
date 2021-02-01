package ccs.markov.metrics;

import ccs.markov.SelectModel;

public class CalculateMetrics {

	public static void main(String[] args) {
		// write the commit number range to calculate Precision/Recall/F-Measure
		for(int i=1; i<35; i++) {
			double threshold = 0.1;
			MetricCalculator init = new MetricCalculator(i, threshold); //new MetricCalculator(i);
			init.setActualImpactSetDirectory("data/commons-codec/AIS.txt");
			init.setEsitmatedImpactSetDirectory("data/commons-codec/1.14.");
			init.setModel(SelectModel.EFFECT_GRAPH);
			init.initilize();
			
//			PrecisionCalculator precision = new PrecisionCalculator(i, threshold);
//			precision.alternateCalculate(init.getActualImpactSet(), init.getEstimatedImpactSet(), init.getEstimatedImpactSetProb());
			
			RecallCalculator recall = new RecallCalculator(i, threshold);
			recall.alternateCalculate(init.getActualImpactSet(), init.getEstimatedImpactSet(), init.getEstimatedImpactSetProb());
			
//			FMeasureCalculator fMeasure = new FMeasureCalculator(i, threshold);
//			fMeasure.alternateCalculate(init.getActualImpactSet(), init.getEstimatedImpactSet(), init.getEstimatedImpactSetProb());
		}
	}
 
}
