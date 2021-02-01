package ccs.markov.slicer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import soot.tools.CFGViewer;

public class CFGGenerator {
	
	private String command = "--graph=ClassicCompleteUnitGraph -cp . -ignore-resolution-errors -src-prec only-class -allow-phantom-refs -pp -process-dir ";
//	private String command = "--graph=ClassicCompleteUnitGraph -cp . -ignore-resolution-errors -allow-phantom-refs -pp -process-dir ";
//	private String command = "--graph=CompleteUnitGraph -cp . -pp -process-dir ";
	
	/**
	 * Generate CFG files with Soot under the /sootOutput folder.
	 * @param path
	 * @return void
	 */
	public void createCFG(String path)
	{
		cleanPrevCFGs();
		path = path.replace("\\\\", "\\");
//		path = path + "\\bin";
		/**
		 * Comment the line above and uncomment the line below
		 * if your are analyzing a Maven Project. If you are 
		 * analyzing an ordinary Java project then do the vice versa
		 * */
		String path1 = path + "\\target";
		if(path1.contains("\\target")) {
			DOTtoPNG.flag = true;
		}
		String newCommand = command + path1 + " -w";
		System.out.println("Path: "+path1); 
		System.out.println("Command: "+newCommand);
		String args[] = newCommand.split(" ");
		CFGViewer.main(args);
		
//		String path2 = path + "\\target\\test-classes";
//		String newCommand2 = command + path2 + " -w";
//		System.out.println("Path: "+path2); 
//		System.out.println("Command: "+newCommand2);
//		String args2[] = newCommand2.split(" ");
//		CFGViewer.main(args2);
	}
	
	
	/**
	 *	Clean-up CFG files from previous execution. Deletes everything
	 *	that is under the /sootOutput folder
	 */
	private void cleanPrevCFGs()
	{
		String currentDir = System.getProperty("user.dir");
		String sootOut = currentDir + "\\sootOutput";
//		System.out.println(sootOut);
//		System.out.println(currentDir);
		File sootDirectory = new File(sootOut);
		try {
			FileUtils.cleanDirectory(sootDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
