package ccs.markov.slicer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class DOTtoPNG {

		private ArrayList<File> myDOTFiles;
		protected static boolean flag = false;

	public ArrayList<File> getMyDOTFiles() {
			return myDOTFiles;
		}

		public void setMyDOTFiles(ArrayList<File> myDOTFiles) {
			this.myDOTFiles = myDOTFiles;
		}

	public DOTtoPNG() {
		myDOTFiles = new ArrayList<>();
	}

	public void findDotFiles()
	{
		String currentDir = System.getProperty("user.dir");
		String sootOut = currentDir + "\\sootOutput";
		File[] files = new File(sootOut).listFiles(); 
		
		for (int i = 0; i<files.length; i++) 
		{
			if (!files[i].isDirectory()) {
				if(files[i].getName().endsWith(".dot")) {
					if(flag) {
						String oldName = files[i].getName();
						int index = oldName.indexOf(".")+1;
						String newName = sootOut + "\\" +oldName.substring(index);
//						System.out.println(newName);
						File rename = new File(newName);
						files[i].renameTo(rename);
//						System.out.println(files[i].getName());
					}
				}
			} 
		}
		
		File[] renamedFiles = new File(sootOut).listFiles();
		
		for (int i = 0; i<renamedFiles.length; i++) {
			if (!renamedFiles[i].isDirectory()) {
				if(renamedFiles[i].getName().endsWith(".dot")) {
					myDOTFiles.add(renamedFiles[i]);
				}
			} 
		}
//		System.out.println(myDOTFiles);
//		System.exit(0);		
	}

	/**
	 * If you want to use this method, it is specifically coded for Windows OS,
	 * and it require Graphviz. 
	 * 
	 * You need to change the "cmd.exe" code and adapt it to the OS you want to use.
	 */
	public void generateDOTtoPNGFiles()
	{
		for(int i=0; i<myDOTFiles.size(); i++)
		{
			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c", "dot \""+myDOTFiles.get(i).getPath()+"\" -Tpng -O");
			builder.redirectErrorStream(true);
			Process p;
			try {
				p = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while (true) 
				{
					line = r.readLine();
					if (line == null) { 
						break; 
					}
				} 
			}catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String... args) {

		DOTtoPNG dot = new DOTtoPNG();
		
		dot.findDotFiles();
		dot.generateDOTtoPNGFiles();
	}

}
