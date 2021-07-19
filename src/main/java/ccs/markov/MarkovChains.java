package ccs.markov;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ccs.markov.change.ChangedMethodProbability;
import ccs.markov.slicer.ForwardSlicer;
import gr.gousiosg.javacg.stat.MethodVisitor;

public class MarkovChains {

	public static int commitNum;
	public static SelectModel model = SelectModel.CALL_GRAPH;
	private Double squareMatrix[][];
	private int adjacencyMatrix[][];
	private ChangedMethodProbability change;
	private Double methodChangeVector[][];
	private static String reportName;
	private String projectReportDirectory;

	private ArrayList<Double> methodChangeProbabilityVector = new ArrayList<>();
	private ArrayList<String> methodList = new ArrayList<>();


	public MarkovChains(ChangedMethodProbability change, String projectReportDirectory) {
		if(model == SelectModel.EFFECT_GRAPH) {
			reportName = "/EIS_Markov_FS_EG.txt";
		}
		else if (model == SelectModel.CALL_GRAPH) {
			reportName = "/EIS_Markov_FS_CG.txt";
		}
		else {
			System.err.print("Model not supported");
			System.exit(0);
		}
		this.squareMatrix = new Double[MethodVisitor.detailedGraph.size()][MethodVisitor.detailedGraph.size()];
		this.adjacencyMatrix =  new int[MethodVisitor.detailedGraph.size()][MethodVisitor.detailedGraph.size()];
		this.methodChangeVector =  new Double[1][MethodVisitor.detailedGraph.size()];
		this.projectReportDirectory = projectReportDirectory;
		createSquareMatrix();
		this.change = change;
		createMethodChangeVector(change);
		weightRowValues();
		//		printSquareMatrix();
		MainCCS.endConstruct = System.currentTimeMillis();
		MainCCS.startCalc = System.currentTimeMillis();
		multiplication(this.methodChangeVector, this.squareMatrix);
		MainCCS.endCalc = System.currentTimeMillis();
		//		printChangeVector();
	}

	public MarkovChains() {

	}

	/**
	 * This method create the probability vector (initial probability) values,
	 * that will be multiplied with the transition matrix. This method also
	 * changes the the initial probability values by weighting them, so that
	 * the summation of all the values in the vector should be equal to 1.
	 * */
	private void createMethodChangeVector(ChangedMethodProbability change) {
		methodChangeProbabilityVector = new ArrayList<>(methodList.size());
		double sum = 0.0;
		for(int i = 0; i<methodList.size(); i++) {
			if(change.getMethodChangeProb().containsKey(methodList.get(i))) {
				if(change.getMethodChangeProb().get(methodList.get(i)) == null) {
					methodChangeVector[0][i] = 0.0;
					this.methodChangeProbabilityVector.add(0.0);
					sum = 0.0 + sum;					
				}
				else {
					methodChangeVector[0][i] = change.getMethodChangeProb().get(methodList.get(i));
					this.methodChangeProbabilityVector.add(change.getMethodChangeProb().get(methodList.get(i)));
					sum = change.getMethodChangeProb().get(methodList.get(i)) + sum;
				}
			}

			else {
				methodChangeVector[0][i] = 0.0;
				this.methodChangeProbabilityVector.add(0.0);
			}
		}

		double newSum = 0.0;
		for(int i=0; i<methodChangeVector[0].length; i++) {
			if(sum == 0) {
				System.err.println("SUM IS ZERO");
				System.exit(0);
			}
			else {
				newSum = newSum + (methodChangeVector[0][i]/sum);
				this.methodChangeProbabilityVector.set(i, (methodChangeVector[0][i]/sum));
				methodChangeVector[0][i] = (methodChangeVector[0][i]/sum);
			}
		}
		/***
		 * Uncomment this line to use Full dependency graph
		 */
		//		for(int i=0; i<methodChangeVector[0].length; i++) {
		//			this.squareMatrix[i][i] = methodChangeVector[0][i];
		//		}
	}

	/**
	 * This method creates a square matrix,
	 * in other words the transition matrix for Markov Chains calculation.
	 * 
	 * It assigns the probabilistic values that are calculated by Forward Slicing algorithm
	 * */
	private void createSquareMatrix() {
		for(int i=0; i<MethodVisitor.detailedGraph.keySet().size(); i++) {
			for(int j=0; j<MethodVisitor.detailedGraph.keySet().size(); j++) {
				this.squareMatrix[i][j] = 0.0;
				adjacencyMatrix[i][j] = 0;
			}

		}

		for(String method : MethodVisitor.detailedGraph.keySet()) {
			methodList.add(method);
		}

		for(String method : MethodVisitor.detailedGraph.keySet()) {
			for(String callee : MethodVisitor.detailedGraph.get(method)) {
				int i = methodList.indexOf(method);
				int j = methodList.indexOf(callee);
				adjacencyMatrix[i][j] = 1;
				//				adjacencyMatrix[i][i] = 1;
			}
		}

		for(int i=0; i<methodList.size(); i++) {
			for(int j=0; j<methodList.size(); j++) {
				if(adjacencyMatrix[i][j] == 1) {
					this.squareMatrix[i][j] = ForwardSlicer.callerForwardSliceInfo.get(methodList.get(j));
					if(model == SelectModel.EFFECT_GRAPH) {
						if((ForwardSlicer.calleeForwardSliceInfo.containsKey(methodList.get(i)) && 
								ForwardSlicer.calleeForwardSliceInfo.get(methodList.get(i)).containsKey(methodList.get(j)))) {
							/**
							 * Comment the line below if your are going to use only call graph
							 * Otherwise, uncomment the line below if your want use a graph with full dependency
							 * */
							double returnVal = ForwardSlicer.calleeForwardSliceInfo.get(methodList.get(i)).get(methodList.get(j));
							if(this.squareMatrix[j][i] < returnVal)
								this.squareMatrix[j][i] = returnVal;
						}
					}
					//					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void printSquareMatrix() {
		for(String method : MethodVisitor.detailedGraph.keySet()) {
			System.out.println("FROM: "+method);
			for(String callee : MethodVisitor.detailedGraph.get(method)) {
				System.out.println("\tTO: "+callee);
			}
		}
		int index=0;
		for(String method : MethodVisitor.detailedGraph.keySet()) {
			System.out.println(index+" - "+method);
			index++;
		}

		System.out.println("==============SQUARE MATRIX================");
		for(int i=0; i<this.squareMatrix.length; i++) {
			double sum = 0;
			for(int j=0; j<squareMatrix[i].length; j++) {
				sum = this.squareMatrix[i][j] + sum;
			}
		}
	}

	private void multiplication(Double[][] vector, Double[][] transitionMatrix) {
		int vectorRows = vector.length;
		int vectorColumns = vector[0].length;
		int transMatRows = transitionMatrix.length;
		int transMatColumns = transitionMatrix[0].length;

		if (vectorColumns != transMatRows) {
			throw new IllegalArgumentException("vector:Rows: " + vectorColumns + " did not match squareMatrix:Columns " + transMatRows + ".");
		}

		Double[][] impactResults = new Double[vectorRows][transMatColumns];
		for (int i = 0; i < vectorRows; i++) {
			for (int j = 0; j < transMatColumns; j++) {
				impactResults[i][j] = 0.0;
			}
		}

		for (int i = 0; i < vectorRows; i++) { // the number of rows of vector should always be 1
			for (int j = 0; j < transMatColumns; j++) { // the column number if the transition matrix should be equal to the number of methods
				for (int k = 0; k < vectorColumns; k++) { // the column number should be equal to the number of methods
					impactResults[i][j] = impactResults[i][j] + (vector[i][k] * transitionMatrix[k][j]);
				}
			}
		}

		//		System.out.println("==================BEFORE SORT====================");
		//		for(int i=0; i<methodList.size(); i++) {
		//			System.out.println(methodList.get(i)+" - "+impactResults[0][i]);
		//		}

		impactResults = sort(impactResults);

		//		System.out.println("==================AFTER SORT====================");
		//		for(int i=0; i<methodList.size(); i++) {
		//			System.out.println(methodList.get(i)+" - "+impactResults[0][i]);
		//		}

		//		printImpactVector(impactResults);

		//		printImpactVector(normalize(impactResults));

		reportEstimations(normalize(impactResults));

	}

	private Double[][] sort(Double[][] impactResults) {

		for(int i=0; i<impactResults[0].length; i++) {
			for(int j=0; j<impactResults[0].length; j++) {
				if(impactResults[0][i] > impactResults[0][j]) {
					Double tmpValue = impactResults[0][i];
					String tmpMethodName = methodList.get(i);

					impactResults[0][i] = impactResults[0][j];
					methodList.set(i, methodList.get(j));

					impactResults[0][j] = tmpValue;
					methodList.set(j, tmpMethodName);
				}
			}
		}

		return impactResults;
	}

	/**
	 * Weights all the values by row in the transition matrix.
	 * The summation of the values of a row should be equal to 1.
	 * */

	private void weightRowValues() {

		for(int i=0; i<squareMatrix.length; i++) {
			double sum = 0.0;

			for(int j=0; j<squareMatrix[i].length; j++) {
				sum = squareMatrix[i][j] + sum;
			}
			if(sum == 0)
				squareMatrix[i][i] = 1.0;
			for(int j=0; j<squareMatrix[i].length; j++) {
				if(sum>0) {
					squareMatrix[i][j] = squareMatrix[i][j]/sum;
				}
			}
		}

		for(int i=0; i<squareMatrix.length; i++) {
			double sum = 0.0;
			for(int j=0; j<squareMatrix[i].length; j++) {
				sum = squareMatrix[i][j] + sum;
			}

			if(sum != 1) {
				if(sum < 1) {
					sum = 1.0 - sum;
					exitLoop : for(int j=0; j<squareMatrix[i].length; j++) {
						if(squareMatrix[i][j] !=0 && (squareMatrix[i][j] + sum) < 1.0) {
							squareMatrix[i][j] = squareMatrix[i][j] + sum;
							break exitLoop;
						}
					}
				}
				else {
					sum = sum - 1.0;

					exitLoop : for(int j=0; j<squareMatrix[i].length; j++) {
						if(squareMatrix[i][j] > sum) {
							squareMatrix[i][j] = squareMatrix[i][j] - sum;
							break exitLoop;
						}
					}
				}
			}
		}
	}

	/**
	 * Print the initial probability change vector.
	 * */
	@SuppressWarnings("unused")
	private void printChangeVector() {
		double sum = 0.0;
		for(int i=0; i<this.methodChangeVector[0].length; i++) {
			sum = sum + this.methodChangeVector[0][i];
		}
		System.out.println("(Change probs.) Initial Vector: "+sum);
		System.out.println(this.methodChangeProbabilityVector);
	}

	/**
	 * Print the impact vector after multiplication and print the
	 * summation of the vectors values.
	 * */
	public void printImpactVector(Double[][] impactResults) {
		double sum = 0.0;
		System.out.print("\n [ ");
		for(int i=0; i<impactResults[0].length; i++) {
			sum = sum + impactResults[0][i];
			System.out.print(impactResults[0][i]+" ");
		}
		System.out.print("]\n");
	}

	public Double[][] normalize(Double[][] impactResults) {

		Double min = impactResults[0][impactResults[0].length-1];
		Double max = impactResults[0][0];
		for(int i=0; i<impactResults[0].length; i++) {
			impactResults[0][i] = (impactResults[0][i] - min) / (max - min);
		}

		return impactResults;
	}

	private void reportEstimations(Double[][] normalizedImpactResults) {

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(projectReportDirectory+commitNum+reportName));
			for(int i=0; i<methodList.size(); i++) {
				
				if(change.getMethodChangeProb().containsKey(methodList.get(i))) {
					if(change.getMethodChangeProb().get(methodList.get(i)) > 0.0 && normalizedImpactResults[0][i] == 0) {
						bw.write(methodList.get(i)+" = "+change.getMethodChangeProb().get(methodList.get(i))+", Parents; -2\n");
					}
					else if(change.getMethodChangeProb().get(methodList.get(i)) > 0.0 && normalizedImpactResults[0][i] != 0) {
						bw.write(methodList.get(i)+" = "+normalizedImpactResults[0][i]+", Parents; -2\n");
					}
				}
				else {
					bw.write(methodList.get(i)+" = "+normalizedImpactResults[0][i]+", Parents; -2\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getProjectReportDirectory() {
		return projectReportDirectory;
	}

	public void setProjectReportDirectory(String projectReportDirectory) {
		this.projectReportDirectory = projectReportDirectory;
	}
}
