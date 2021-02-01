package ccs.markov.dotview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import gr.gousiosg.javacg.stat.MethodVisitor;

public class DOTCreation {

	private Set<String> aisNodes = new HashSet<>();
	private Set<String> eisNodes = new HashSet<>();

	private Set<String> reachableMethods = new HashSet<>();

	public void readAISNodes() {
		try {
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/junit4/AIS.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/jsoup/AIS.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/jjwt/AIS.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/commons-csv/AIS.txt"));
			BufferedReader br = new BufferedReader(new FileReader("CIAReports/commons-codec/AIS.txt"));

			String line;

			while((line = br.readLine()) != null) {
				int startPos = line.indexOf(" ")+1;
				int endPos = line.indexOf(" Change Percentage: ");
				//				String modifiedMethodStr = line.substring(startPos, endPos).replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_")+"_";
				String modifiedMethodStr = line.substring(startPos, endPos);
				aisNodes.add(modifiedMethodStr);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readEISNodes() {
		try {
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/junit4/4.11.27/EIS_CG.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/jsoup/1.10.3.54/EIS_CG.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/jjwt_0.6.0-0.6.16/EIS_BN_CG.txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("CIAReports/commons-csv/1.7.18/EIS_Markov_FS_CG.txt"));
			BufferedReader br = new BufferedReader(new FileReader("CIAReports/commons-csv/1.7.30/EIS_Markov_FS_CG.txt"));


			String line;
			double threshold = 0.0;
			while((line = br.readLine()) != null) {
				int startPos = 0;
				int endPos = line.indexOf("] = ");
				int startProbPos = endPos+4;;
				int endProbPos = -1;
				if(line.contains(", Parents;")) {
					endProbPos = line.indexOf(", Parents;");
				}
				else {
					endProbPos = line.length();	
				}

				String probStr = line.substring(startProbPos, endProbPos);
				Double prob = Double.parseDouble(probStr);
				if(prob>threshold)
				{
					String modifiedMethodStr = line.substring(startPos, endPos).trim();
					eisNodes.add(modifiedMethodStr);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*private void findReachableMethods(String method) {
		if(MethodVisitor.detailedGraph.containsKey(method)) {
			reachableMethods.add(method);
			for(String child : MethodVisitor.detailedGraph.get(method)) {
				reachableMethods.add(child);
				findReachableMethods(child);
			}
		}
	}*/

	public void writeDOTFile() {
		HashMap<String, Integer> methods = new HashMap<>();
		int count = 1;
		for(String parent : MethodVisitor.detailedGraph.keySet()) {
			String modifiedParent = parent;
			if(!methods.containsKey(modifiedParent)) {
				methods.put(modifiedParent, count);
				count++;
			}
			for(String child : MethodVisitor.detailedGraph.get(parent)) {
				String modifiedChild = child;
				if(!methods.containsKey(modifiedChild)) {
					methods.put(modifiedChild, count);
					count++;
				}
			}
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("CIAReports/callgraph_commons-codec.dot"));

			bw.write("digraph {\n");

			bw.write("\tgraph [layout = fdp]\n");
			bw.write("\tnode[shape=circle, fontname=\"Courier-Bold\", fontsize=10, width=0.4, height=0.4, fixedsize=true]\n");
			bw.write("\tedge[arrowsize=0.6, arrowhead=vee]\n");

			System.out.println(aisNodes.toString());
			System.out.println(reachableMethods.size());
			for(String greenNodes: reachableMethods) {
				if(methods.containsKey(greenNodes)) {
					if(aisNodes.contains(greenNodes)) {
						bw.write("\t"+methods.get(greenNodes)+" [style=filled, fillcolor=\"green\"]\n");
					}
					else
						bw.write("\t"+methods.get(greenNodes)+" [style=filled, fillcolor=yellow]\n");
				}
			}

			for(String redNodes: aisNodes) {
				if(methods.containsKey(redNodes) && !reachableMethods.contains(redNodes))
					bw.write("\t"+methods.get(redNodes)+" [style=filled, fillcolor=red]\n");
			}

			for(String parent : MethodVisitor.detailedGraph.keySet()) {
				//				String modifiedParent = parent.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_")+"_";
				String modifiedParent = parent;
				for(String child : MethodVisitor.detailedGraph.get(parent)) {
					//					String modifiedChild = child.replace(" ","").replace(".", "_").replace("[", "_").replace("]", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace(",", "_").replace("$", "_")+"_";
					String modifiedChild = child;
					//					bw.write("\t"+"\""+parent+"\""+" -> "+"\""+child+"\""+";\n");
					bw.write("\t"+methods.get(modifiedParent)+" -> "+methods.get(modifiedChild)+";\n");

				}
			}
			bw.write("}");
			bw.flush();
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
