package ccs.markov.metrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ccs.markov.SelectModel;

public class MetricCalculator {
	private Set<String> actualImpactSet;
	private ArrayList<String> estimatedImpactSet;
	private ArrayList<Double> estimatedImpactSetProb;
	private String actualImpactSetDirectory;
	private String esitmatedImpactSetDirectory;
	private SelectModel model = SelectModel.EFFECT_GRAPH;
	private String estimatedImpactSetFileName = "EIS_Markov_FS_EG.txt";
	private int commitID = 0;

	public double threshold = 0.2;
	private String EISReport;
	
	public MetricCalculator(int count, double threshold) {
		this.threshold = threshold;
		this.commitID = count;
		actualImpactSet = new HashSet<>();
		estimatedImpactSet = new ArrayList<>();
		estimatedImpactSetProb = new ArrayList<>();
	}
	
	public void initilize() {
		EISReport = esitmatedImpactSetDirectory+commitID+"/"+estimatedImpactSetFileName;
		createAIS();
		createEIS();
	}
	
	public Set<String> getActualImpactSet() {
		return actualImpactSet;
	}
	
	public void setActualImpactSet(Set<String> actualImpactSet) {
		this.actualImpactSet = actualImpactSet;
	}
	
	public ArrayList<String> getEstimatedImpactSet() {
		return estimatedImpactSet;
	}
	
	public void setEstimatedImpactSet(ArrayList<String> estimatedImpactSet) {
		this.estimatedImpactSet = estimatedImpactSet;
	}
	
	public void createAIS() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(actualImpactSetDirectory));
			
			String line = null;
			while((line = br.readLine()) != null) {
				int methodDefStart = line.indexOf(" ")+1;
				int methodDefEnd = line.lastIndexOf("]")+1;
				String methodDef = line.substring(methodDefStart, methodDefEnd);
				actualImpactSet.add(methodDef.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_")+"_");
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createEIS() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(EISReport));
			
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.contains("]")) {
					int methodDefEnd = line.lastIndexOf("]")+1;
					String methodDef = line.substring(0, methodDefEnd);
					estimatedImpactSet.add(methodDef.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_")+"_");
					
					int probStart = line.indexOf("= ")+2;
					int probEnd = line.indexOf(", Parents; ");
					Double prob = Double.parseDouble(line.substring(probStart, probEnd));
					estimatedImpactSetProb.add(prob);
				}
				else {
					int methodDefEnd = line.lastIndexOf("_")+1;
					String methodDef = line.substring(0, methodDefEnd);
					estimatedImpactSet.add(methodDef);
					
					int probStart = line.indexOf("= ")+2;
					Double prob = Double.parseDouble(line.substring(probStart));
					estimatedImpactSetProb.add(prob);
				}
			
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Double> getEstimatedImpactSetProb() {
		return estimatedImpactSetProb;
	}

	public void setEstimatedImpactSetProb(ArrayList<Double> estimatedImpactSetProb) {
		this.estimatedImpactSetProb = estimatedImpactSetProb;
	}

	public SelectModel getModel() {
		return model;
	}

	public void setModel(SelectModel model) {
		this.model = model;
		if(this.model == SelectModel.CALL_GRAPH) {
			estimatedImpactSetFileName = "EIS_Markov_FS_CG.txt";
		}
		else if(this.model == SelectModel.EFFECT_GRAPH) {
			estimatedImpactSetFileName = "EIS_Markov_FS_EG.txt";
		}
		else {
			System.err.print("Not such model defined");
			System.exit(0);
		}
	}

	public int getCommitID() {
		return commitID;
	}

	public void setCommitID(int commitID) {
		this.commitID = commitID;
	}
	
	public String getActualImpactSetDirectory() {
		return actualImpactSetDirectory;
	}

	public void setActualImpactSetDirectory(String actualImpactSetDirectory) {
		this.actualImpactSetDirectory = actualImpactSetDirectory;
	}

	public String getEstimatedImpactSetFileName() {
		return estimatedImpactSetFileName;
	}

	public void setEstimatedImpactSetFileName(String estimatedImpactSetFileName) {
		this.estimatedImpactSetFileName = estimatedImpactSetFileName;
	}
	
	public String getEsitmatedImpactSetDirectory() {
		return esitmatedImpactSetDirectory;
	}

	public void setEsitmatedImpactSetDirectory(String esitmatedImpactSetDirectory) {
		this.esitmatedImpactSetDirectory = esitmatedImpactSetDirectory;
	}

}
