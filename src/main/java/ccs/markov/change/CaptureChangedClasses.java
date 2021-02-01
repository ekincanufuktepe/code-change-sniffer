package ccs.markov.change;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ccs.markov.slicer.AlgorithmTag;
import net.sf.rej.Imports;
import net.sf.rej.files.FileSet;
import net.sf.rej.gui.EditorFacade;
//import tr.iyte.edu.testcaseprioritize.collectInfo.ChangeInformationProbability;
import net.sf.rej.gui.SystemFacade;
import net.sf.rej.gui.editor.row.BlankRow;
import net.sf.rej.gui.editor.row.ClassCommentRow;
import net.sf.rej.gui.editor.row.ClassDefRow;
import net.sf.rej.gui.editor.row.CodeRow;
import net.sf.rej.gui.editor.row.DeprecatedAnnotationDefRow;
import net.sf.rej.gui.editor.row.EditorRow;
import net.sf.rej.gui.editor.row.FieldDefRow;
import net.sf.rej.gui.editor.row.ImportDefRow;
import net.sf.rej.gui.editor.row.LabelRow;
import net.sf.rej.gui.editor.row.LocalVariableDefRow;
import net.sf.rej.gui.editor.row.MethodAnnotationDefRow;
import net.sf.rej.gui.editor.row.MethodDefRow;
import net.sf.rej.gui.editor.row.PackageDefRow;
import net.sf.rej.java.ClassFile;
import net.sf.rej.java.Code;
import net.sf.rej.java.Descriptor;
import net.sf.rej.java.Disassembler;
import net.sf.rej.java.Field;
import net.sf.rej.java.Interface;
import net.sf.rej.java.LocalVariable;
import net.sf.rej.java.Method;
import net.sf.rej.java.attribute.Attributes;
import net.sf.rej.java.attribute.CodeAttribute;
import net.sf.rej.java.attribute.LineNumberTableAttribute;
import net.sf.rej.java.attribute.LocalVariableTableAttribute;
import net.sf.rej.java.attribute.RuntimeInvisibleAnnotationsAttribute;
import net.sf.rej.java.attribute.RuntimeVisibleAnnotationsAttribute;
import net.sf.rej.java.attribute.SourceFileAttribute;
import net.sf.rej.java.attribute.annotations.Annotation;
import net.sf.rej.java.instruction.DecompilationContext;
import net.sf.rej.java.instruction.Instruction;
import net.sf.rej.java.instruction.Label;

public class CaptureChangedClasses {
	
	public static HashMap<String, Double> changedMethods = new HashMap<>();

	private FileSet filesetA;
	private FileSet filesetB;

	private static List<EditorRow> rowsAll;
	private static Collection<EditorRow> rowsA;
	private static Collection<EditorRow> rowsB;

	public HashMap<String, Double> compareClasses(String newFile, String oldFile, AlgorithmTag tag) {
		File fileNew = new File(newFile);
		File fileOld = new File(oldFile);


		try {
			filesetA = SystemFacade.getInstance().getFileSet(fileNew);
			filesetB = SystemFacade.getInstance().getFileSet(fileOld);

			for(String fn : filesetA.getContentsList()) {
				if(filesetB.getContentsList().contains(fn) && fn.endsWith(".class")) {
					// print directory output
//					System.out.println(filesetA.getClasspath(fn)+"\\"+fn.replace("/", "\\"));
					try {
						byte[] dataA = this.filesetA.getData(fn.replace("/", "\\"));
						byte[] dataB = this.filesetB.getData(fn.replace("/", "\\"));

						ClassFile cfA = Disassembler.readClass(dataA);
						ClassFile cfB = Disassembler.readClass(dataB);
						setClassFiles(cfA, cfB, tag, true);
					} catch (Exception ex) {
						SystemFacade.getInstance().handleException(ex);
					}
				}
				else if(fn.endsWith(".class")) {
					try {
						byte[] dataA = this.filesetA.getData(fn.replace("/", "\\"));
						byte[] dataB = this.filesetA.getData(fn.replace("/", "\\"));

						ClassFile cfA = Disassembler.readClass(dataA);
						ClassFile cfB = Disassembler.readClass(dataB);
						setClassFiles(cfA, cfB, tag, false);
					} catch (Exception ex) {
						SystemFacade.getInstance().handleException(ex);
					}
				}

			}

		}
		catch(Exception ex){

		}

		return changedMethods; 
	}

	public static void setClassFiles(ClassFile cfA, ClassFile cfB, AlgorithmTag tag, boolean existingClass) {
		//uncomment code block below to view the compared codes.
//		if (cfA.getFullClassName().equals(cfB.getFullClassName())) {
//			System.out.println("Bytecode Compare: " + cfA.getFullClassName());
//		} else {
//			System.out.println("Bytecode Compare: " + cfA.getFullClassName() + " / " + cfB.getFullClassName());
//		}

		load(cfA, cfB, tag, existingClass);
	}

	public static void load(ClassFile cfA, ClassFile cfB, AlgorithmTag tag, boolean existingClass) {

		rowsAll = new ArrayList<EditorRow>();
		rowsA = new HashSet<EditorRow>();
		rowsB = new HashSet<EditorRow>();

		// Package
		if (cfA.getPackageName().equals(cfB.getPackageName())) {
			rowsAll.add(new PackageDefRow(cfA));			
		} else {
			PackageDefRow pdrA = new PackageDefRow(cfA);
			PackageDefRow pdrB = new PackageDefRow(cfB);
			rowsA.add(pdrA);
			rowsB.add(pdrB);
			rowsAll.add(pdrA);
			rowsAll.add(pdrB);
		}

		rowsAll.add(new BlankRow());

		// Imports
//		try {
		Imports importsA = EditorFacade.getInstance().getImports(cfA);
		Imports importsB = EditorFacade.getInstance().getImports(cfB);
//		}
//		catch (Exception e) {
			// TODO: handle exception
//		}
		//		this.renderer.setImports(importsA, importsB);

		Set<String> tsA = importsA.getImports();
		Set<String> tsB = importsB.getImports();
		Set<String> allOrdered = new TreeSet<String>();
		allOrdered.addAll(tsA);
		allOrdered.addAll(tsB);
		for (String imp : allOrdered) {
			ImportDefRow idr = new ImportDefRow(imp);
			rowsAll.add(idr);
			if (!tsA.contains(imp)) {
				rowsB.add(idr);
			}
			if (!tsB.contains(imp)) {
				rowsA.add(idr);
			}
		}

		if (allOrdered.size() > 0) {
			rowsAll.add(new BlankRow());
			/* empty space between imports and class def */
		}

		// Add some useful information as comments

		// Source file name
		SourceFileAttribute sfA = cfA.getAttributes().getSourceFileAttribute();
		SourceFileAttribute sfB = cfB.getAttributes().getSourceFileAttribute();
		if (sfA == null && sfB == null) {
			// Add nothing, neither has a source file attribute
		} else if (sfA == null || sfB == null) {
			// Only one has a source file attribute
			if (sfA != null) {
				ClassCommentRow sfComment = new ClassCommentRow("SourceFile = " + sfA.getSourceFile());
				rowsA.add(sfComment);
				rowsAll.add(sfComment);
			}

			if (sfB != null) {
				ClassCommentRow sfComment = new ClassCommentRow("SourceFile = " + sfB.getSourceFile());
				rowsB.add(sfComment);
				rowsAll.add(sfComment);				
			}

		} else {
			// Both have source file attributes
			if (sfA.getSourceFile().equals(sfB.getSourceFile())) {
				ClassCommentRow sfComment = new ClassCommentRow("SourceFile = " + sfA.getSourceFile());
				rowsAll.add(sfComment);
			} else {
				ClassCommentRow sfCommentA = new ClassCommentRow("SourceFile = " + sfA.getSourceFile());
				rowsA.add(sfCommentA);
				rowsAll.add(sfCommentA);

				ClassCommentRow sfCommentB = new ClassCommentRow("SourceFile = " + sfB.getSourceFile());
				rowsB.add(sfCommentB);
				rowsAll.add(sfCommentB);
			}
		}

		// Class version
		if (cfA.getMajorVersion() == cfB.getMajorVersion()
				&& cfA.getMinorVersion() == cfB.getMinorVersion()) {
			ClassCommentRow versionComment = new ClassCommentRow("Class Version: " + cfA.getMajorVersion() + "." + cfA.getMinorVersion());
			rowsAll.add(versionComment);
		} else {
			ClassCommentRow versionCommentA = new ClassCommentRow("Class Version: " + cfA.getMajorVersion() + "." + cfA.getMinorVersion());
			rowsA.add(versionCommentA);
			ClassCommentRow versionCommentB = new ClassCommentRow("Class Version: " + cfB.getMajorVersion() + "." + cfB.getMinorVersion());
			rowsB.add(versionCommentB);
			rowsAll.add(versionCommentA);
			rowsAll.add(versionCommentB);
		}

		List<Interface> interfacesA = cfA.getInterfaces();
		List<Interface> interfacesB = cfB.getInterfaces();
		boolean interfacesAreEqual = interfacesA.equals(interfacesB);

		// Class
		if (cfA.getShortClassName().equals(cfB.getShortClassName())
				&& cfA.getAccessFlags() == cfB.getAccessFlags()
				&& cfA.getSuperClassName().equals(cfB.getSuperClassName())
				&& interfacesAreEqual) {
			rowsAll.add(new ClassDefRow(cfA, true));
		} else {
			ClassDefRow cdrA = new ClassDefRow(cfA, true);
			ClassDefRow cdrB = new ClassDefRow(cfB, true);
			rowsA.add(cdrA);
			rowsB.add(cdrB);
			rowsAll.add(cdrA);
			rowsAll.add(cdrB);
		}

		rowsAll.add(new BlankRow());

		// Fields
		// TODO: Constant value compare
		Map<String, Field> fieldsA = new HashMap<String, Field>();
		for (Field field : cfA.getFields()) {
			fieldsA.put(field.getSignatureLine(), field);
		}
		Map<String, Field> fieldsB = new HashMap<String, Field>();
		for (Field field : cfB.getFields()) {
			fieldsB.put(field.getSignatureLine(), field);
		}

		Set<String> allFields = new TreeSet<String>();
		allFields.addAll(fieldsA.keySet());
		allFields.addAll(fieldsB.keySet());
		for (String fieldSignature : allFields) {
			// Field annotations
			FieldDefRow fdr = null;
			Field field = fieldsA.get(fieldSignature);
			if (field == null) {
				field = fieldsB.get(fieldSignature);
				fdr = new FieldDefRow(cfB, field);
				rowsB.add(fdr);
			} else {
				fdr = new FieldDefRow(cfA, field);
				if (!fieldsB.keySet().contains(fieldSignature)) {
					rowsA.add(fdr);
				}
			}

			rowsAll.add(fdr);
		}

		if (allFields.size() > 0) {
			rowsAll.add(new BlankRow());
		}

		// Methods
		Map<String, Method> methodsA = new HashMap<String, Method>();
		for (Method method : cfA.getMethods()) {
			Descriptor desc = method.getDescriptor();
			methodsA.put(desc.getReturn() + method.getName() + " " + desc.getParams(), method);
		}
		Map<String, Method> methodsB = new HashMap<String, Method>();
		for (Method method : cfB.getMethods()) {
			Descriptor desc = method.getDescriptor();
			methodsB.put(desc.getReturn() + method.getName() + " " + desc.getParams(), method);
		}

		Set<String> allMethods = new TreeSet<String>();
		allMethods.addAll(methodsA.keySet());
		allMethods.addAll(methodsB.keySet());

		double [] calRed = new double[allMethods.size()+1];
		double [] calYellow = new double[allMethods.size()+1];
		double redish = 0;
		double yellowish = 0;
		int incrementor = 0;
		double stdDevRed = 0;
		double stdDevYel = 0;
		for (String methodTypeNameParams : allMethods) {
			Method methodA = methodsA.get(methodTypeNameParams);
			Method methodB = methodsB.get(methodTypeNameParams);

			if (methodA == null) {
				// method only exists in B
				List<EditorRow> methodRows = getMethodRows(cfB, methodB);
				rowsB.addAll(methodRows);
				rowsAll.addAll(methodRows);
//				System.out.println("No A, B method name: "+methodB.getName());
				int aa = methodRows.size();
				double rr = rowsA.size();
				double yy = rowsB.size();

				if(yy == 1 && rr ==1) {
					yy = 0;
				}
				calRed[incrementor] = rr;
				calYellow[incrementor] = yy;
				if(incrementor != 0) {
					yellowish = calYellow[incrementor] - calYellow[incrementor - 1];
					redish = calRed[incrementor] - calRed[incrementor - 1];

					/**
					 * FOR CIA ANALYSIS
					 * START OF EXIST IN OLD VERSION BUT DOESN'T EXIST IN CURRENT VERSION
					 * */
					// sanity check
//					System.out.println("Change Prob. B: "+(yellowish+redish)/(aa));
					String signature = methodB.getSignatureLine();
					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);

					String[] paramTypes = null;
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(tag.equals(AlgorithmTag.LINES_OF_CODE))
						changedMethods.put(cfB.getPackageName()+"."+cfB.getShortClassName()+":"+methodB.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
					else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
						changedMethods.put(cfB.getPackageName()+"."+cfB.getShortClassName()+":"+methodB.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);

					/**
					 * END OF EXIST IN OLD VERSION BUT DOESN'T EXIST IN CURRENT VERSION
					 * */
				}
				else {
					yellowish = calYellow[incrementor];
					redish = calRed[incrementor];
					// sanity check
//					if((yellowish+redish)/(aa) < 0) {
//						System.out.println("Change Prob B: "+(yellowish+redish)/(aa));
//						System.out.println();
//					}

					/**
					 * FOR CIA ANALYSIS
					 * START OF EXIST IN OLD VERSION BUT DOESN'T EXIST IN CURRENT VERSION
					 * */
					// sanity check
//					System.out.println("Change Prob B: "+(yellowish+redish)/(aa));
					String signature = methodB.getSignatureLine();
					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);

					String[] paramTypes = null;
					// Parameter types
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

					// sanity check
//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(tag.equals(AlgorithmTag.LINES_OF_CODE))
						changedMethods.put(cfB.getPackageName()+"."+cfB.getShortClassName()+":"+methodB.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
					else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
						changedMethods.put(cfB.getPackageName()+"."+cfB.getShortClassName()+":"+methodB.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);

					/**
					 * END OF EXIST IN OLD VERSION BUT DOESN'T EXIST IN CURRENT VERSION
					 * */
				}

			} 
			else if (methodB == null) {
				// method only exists in A
				List<EditorRow> methodRows = getMethodRows(cfA, methodA);
				rowsA.addAll(methodRows);
				rowsAll.addAll(methodRows);
				// sanity check
//				System.out.println("No B, A method name: "+methodA.getName());
				int aaa = methodRows.size();
				double rr = 0;
				rr = rowsA.size();
				
				double yy = 0;
				yy = rowsB.size();

				if(yy == 1 && rr ==1) {
					yy = 0;
				}
				calRed[incrementor] = rr;
				calYellow[incrementor] = yy;
				
				if(incrementor != 0) {
					yellowish = calYellow[incrementor] - calYellow[incrementor - 1];
					redish = calRed[incrementor] - calRed[incrementor - 1];
//					System.out.println("Change Prob. A: "+(yellowish+redish)/(aaa) + " --> 1.0");
					String signature = methodA.getSignatureLine();
//					System.out.println(signature);
					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);
//					System.out.println(signature);

					String[] paramTypes = null;
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(tag.equals(AlgorithmTag.LINES_OF_CODE))
						changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
					else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
						changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);

//					System.out.println();
				}
				else {
					yellowish = calYellow[incrementor];
					redish = calRed[incrementor];
					// sanity check
//					System.out.println("Change Prob. A: "+(yellowish+redish)/(aaa) + " --> 1.0");

					String signature = methodA.getSignatureLine();

					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);

					String[] paramTypes = null;
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(tag.equals(AlgorithmTag.LINES_OF_CODE))
						changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
					else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
						changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);

					System.out.println();
				}

			} 
			else {
				List<EditorRow> methodRowsA = getMethodRows(cfA, methodA);
				List<EditorRow> methodRowsB = getMethodRows(cfB, methodB);

				int diff = methodRowsA.size() - methodRowsB.size();

				if(diff < 0) {
					diff = diff * -1;
				}

				int aaa = methodRowsA.size();//+diff;
				int bbb = methodRowsB.size();
				// method exists in both
				// sanity check
//				System.out.println("A & B New method name: "+methodA.getName());

				addMethodRows(cfA, cfB, methodA, methodB);
				double rr = 0;
				double yy = 0;

				double begRed = 0;
				double begYel = 0;

				for (EditorRow er : rowsA) {
					int i = rowsAll.indexOf(er);
					if(incrementor == 0) {
						if((rowsAll.size() - aaa) < i) {
							rr++;
						}
					}
					else {
						rr++;
					}

					if(incrementor == 0) {
						begRed++;
					}
				}

				for (EditorRow er : rowsB) {
					int i = rowsAll.indexOf(er);
					if(incrementor == 0) {
						if((rowsAll.size() - bbb) < i) {
							yy++;
						}
					}
					else {
						yy++;
					}

					if(incrementor == 0) {
						begYel++;
					}
				}

				calRed[incrementor] = rr - stdDevRed;
				calYellow[incrementor] = yy - stdDevYel;
				yellowish = yy - yellowish;

				if(incrementor != 0) {
					if(calYellow[incrementor] > calYellow[incrementor - 1] && calYellow[incrementor] > 0) {
						yellowish = calYellow[incrementor] - calYellow[incrementor - 1];
					}
					else {
						calYellow[incrementor] = calYellow[incrementor - 1];
						yellowish = 0.0;
					}


					if(calRed[incrementor] > calRed[incrementor - 1] && calRed[incrementor] > 0) {
						redish = calRed[incrementor] - calRed[incrementor - 1];
					}
					else {
						calRed[incrementor] = calRed[incrementor - 1];
						redish = 0.0;
					}

//					System.out.println(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName());
//					System.out.println("Y1: "+calYellow[incrementor]+", Y0: "+calYellow[incrementor - 1]);
//					System.out.println("Y: "+yellowish+"Y_Alt: "+yy+", R: "+redish+", Total: "+(aaa)+ " "+incrementor);
					String signature = methodA.getSignatureLine();
					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);

					String[] paramTypes = null;
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

//					System.out.println(methodA.getSignatureLine()+"Change Prob. AB: "+(yellowish+redish)/(aaa+yellowish));
					double value = (yellowish+redish)/(aaa+yellowish);

//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(existingClass) {
						if(tag.equals(AlgorithmTag.LINES_OF_CODE))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), value);
						else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", value);
					}
					else {
						if(tag.equals(AlgorithmTag.LINES_OF_CODE))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
						else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);
					}
				}
				else {
					stdDevRed = begRed - rr;
					calRed[incrementor] = stdDevRed;
					incrementor++;
					calRed[incrementor] = rr;
					stdDevYel = begYel - yy;
					calYellow[incrementor] = stdDevYel;
					yellowish = calYellow[incrementor] - calYellow[incrementor - 1];
					redish = calRed[incrementor];
					redish = rr;
					yellowish = yy;
					calRed[incrementor] = redish;
					calYellow[incrementor] = yellowish;

					double value = (yellowish+redish)/(aaa+yellowish);

					String signature = methodA.getSignatureLine();
					int leftP = 0;
					int rightP = 0;
					leftP = signature.indexOf("(")+1;
					rightP = signature.indexOf(")");
					signature = signature.substring(leftP, rightP);
//					System.out.println(signature);

					String[] paramTypes = null;
					ArrayList<String> pTypes = null;
					if(!signature.equals("")) {
						if(signature.contains(",")) {
							paramTypes = signature.trim().split(", ");
							for(int k=0; k<paramTypes.length; k++) {
								if(paramTypes[k].contains(".")) {
									paramTypes[k] = paramTypes[k].substring(paramTypes[k].lastIndexOf(".")+1);
								}
							}
						}
						else {
							paramTypes = new String[1];
							paramTypes[0] = signature;
							if(paramTypes[0].contains(".")) {
								paramTypes[0] = paramTypes[0].substring(paramTypes[0].lastIndexOf(".")+1);
							}
						}
						pTypes = new ArrayList<>(Arrays.asList(paramTypes));
					}

					if(pTypes == null) {
						pTypes = new ArrayList<>();
					}

//					System.out.println("SIGNATURE ->"+"["+signature+"]");
					if(existingClass) {
						if(tag.equals(AlgorithmTag.LINES_OF_CODE))	
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), value);
						else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", value);
					}
					else {
						if(tag.equals(AlgorithmTag.LINES_OF_CODE))	
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+pTypes.toString(), 0.99);
						else if(tag.equals(AlgorithmTag.FORWARD_SLICE_STATEMENT))
							changedMethods.put(cfA.getPackageName()+"."+cfA.getShortClassName()+":"+methodA.getName()+":"+pTypes.size()+":"+"["+signature+"]", 0.99);
					}
				}

			}
			incrementor++;

			rowsAll.add(new BlankRow());
		}

		rowsAll.add(new ClassDefRow(cfA, false));

		// Scrollbar clue image
		double red = rowsA.size();
		double yel = rowsB.size();
		
		if(yel == 1 && red ==1) {
			yel = 0;
		}

//		System.out.println(cfA.getAccessString()+" - "+ cfA.getPackageName()+ " - "+cfA.getShortClassName());
//		System.out.println("Y: "+yel+", R: "+red+", Total: "+(rowsAll.size()));
//		System.out.println("Total Change: "+(yel+red)/(rowsAll.size()));
	}

	public static List<EditorRow> getMethodRows(ClassFile cf, Method method) {
		List<EditorRow> list = new ArrayList<EditorRow>();

		// Method annotations
		boolean deprecatedAnnotationAdded = false;
		RuntimeInvisibleAnnotationsAttribute methodAnnInvisible = method
				.getAttributes().getRuntimeInvisibleAnnotationsAttribute();
		RuntimeVisibleAnnotationsAttribute methodAnnVisible = method
				.getAttributes().getRuntimeVisibleAnnotationsAttribute();
		List<Annotation> methodAnnotations = new ArrayList<Annotation>();
		if (methodAnnInvisible != null) {
			methodAnnotations.addAll(methodAnnInvisible.getAnnotations());
		}
		if (methodAnnVisible != null) {
			methodAnnotations.addAll(methodAnnVisible.getAnnotations());
		}
		for (Annotation annotation : methodAnnotations) {
			MethodAnnotationDefRow madr = new MethodAnnotationDefRow(
					annotation);
			list.add(madr);
			if ("java.lang.Deprecated".equals(annotation.getName())) {
				deprecatedAnnotationAdded = true;
				// store this information so that
				// the Deprecated attribute isn't used to
				// create another deprecation EditorRow
			}
		}

		if (!deprecatedAnnotationAdded && method.isDeprecated()) {
			DeprecatedAnnotationDefRow ddr = new DeprecatedAnnotationDefRow();
			list.add(ddr);
		}

		MethodDefRow mdr = new MethodDefRow(cf, method, true, method.getAttributes().getCode() != null);
		list.add(mdr);

		Attributes attr = method.getAttributes();
		CodeAttribute codeAttr = attr.getCode();

		LineNumberTableAttribute lnAttr = null;
		LocalVariableTableAttribute lvs = null;
		if (codeAttr != null) {
			if (codeAttr.getAttributes() != null) {
				lnAttr = codeAttr.getAttributes().getLineNumberTable();
				lvs = codeAttr.getAttributes().getLocalVariableTable();
			}
			Code code = codeAttr.getCode();
			DecompilationContext dc = code.createDecompilationContext();
			List<?> instructions = code.getInstructions();
			dc.setPosition(0);
			for (int j = 0; j < instructions.size(); j++) {
				Instruction instruction = (Instruction) instructions.get(j);

				if (instruction instanceof Label) {
					LabelRow lr = new LabelRow((Label) instruction, mdr);
					lr.setParentCode(code);
					list.add(lr);
					mdr.addCodeRow(lr);
				} else {
					int lineNumber = -1;

					if (lnAttr != null) {
						lineNumber = lnAttr.getLineNumber(dc.getPosition());
					}
					if (lvs != null) {
						List<?> locals = lvs
								.getLocalVariable(dc.getPosition());
						for (int k = 0; k < locals.size(); k++) {
							LocalVariable lv = (LocalVariable) locals
									.get(k);
							LocalVariableDefRow lvdr = new LocalVariableDefRow(
									lv, mdr);
							list.add(lvdr);
							mdr.addLocalVariable(lvdr);
						}
					}

					CodeRow cd = new CodeRow(cf, mdr, instruction);
					cd.setPosition(dc.getPosition());
					cd.setDecompilationContext(dc);
					cd.setParentCode(code);

					if (lineNumber != -1) {
						cd.setLineNumber(lineNumber);
					}

					list.add(cd);
					mdr.addCodeRow(cd);

					dc.incrementPosition(instruction);
				}
			}
			list.add(new MethodDefRow(cf, method, false, true));
		}
		return list; 
	}

	private static void addMethodRows(ClassFile cfA, ClassFile cfB, Method methodA, Method methodB) {
		// TODO: Annotations and non-annotation deprecation

		// We matched these methods based on descriptor (=type & params) and name
		// but the thrown exceptions and access type may still be different
		// and if it is, we want that highlighted
		MethodDefRow mdrA = null;
		MethodDefRow mdrB = null;
		if (methodA.getAccessFlags() == methodB.getAccessFlags()
				&& methodA.getExceptions().equals(methodB.getExceptions())) {
			// everything is equal, just add one line
			MethodDefRow mdr = new MethodDefRow(cfA, methodA, true, methodA.getAttributes().getCode() != null);
			mdrA = mdr;
			mdrB = mdr;
			rowsAll.add(mdr);
		} else {
			mdrA = new MethodDefRow(cfA, methodA, true, methodA.getAttributes().getCode() != null);
			rowsA.add(mdrA);
			mdrB = new MethodDefRow(cfB, methodB, true, methodB.getAttributes().getCode() != null);
			rowsB.add(mdrB);
			rowsAll.add(mdrA);
			rowsAll.add(mdrB);
		}

		Attributes attrA = methodA.getAttributes();
		Attributes attrB = methodB.getAttributes();
		CodeAttribute codeAttrA = attrA.getCode();
		CodeAttribute codeAttrB = attrB.getCode();

		LineNumberTableAttribute lnAttrA = null;
		LineNumberTableAttribute lnAttrB = null;
		LocalVariableTableAttribute lvsA = null;
		LocalVariableTableAttribute lvsB = null;
		if (codeAttrA != null && codeAttrB != null) {
			if (codeAttrA.getAttributes() != null) {
				lnAttrA = codeAttrA.getAttributes().getLineNumberTable();
				lvsA = codeAttrA.getAttributes().getLocalVariableTable();
			}
			if (codeAttrB.getAttributes() != null) {
				lnAttrB = codeAttrB.getAttributes().getLineNumberTable();
				lvsB = codeAttrB.getAttributes().getLocalVariableTable();
			}

			Code codeA = codeAttrA.getCode();
			Code codeB = codeAttrB.getCode();

			DecompilationContext dcA = codeA.createDecompilationContext();
			DecompilationContext dcB = codeB.createDecompilationContext();

			List<Instruction> instructionsA = codeA.getInstructions();
			List<Instruction> instructionsB = codeB.getInstructions();
			dcA.setPosition(0);
			dcB.setPosition(0);

			List<EditorRow> methodRowsA = getMethodRows(instructionsA, codeA, mdrA, lnAttrA, lvsA, dcA, cfA);
			List<EditorRow> methodRowsB = getMethodRows(instructionsB, codeB, mdrB, lnAttrB, lvsB, dcB, cfB);

			// find out the equal instructions at the beginning of the block
			int startEqCount = 0;
			while (true) {
				if (startEqCount == methodRowsA.size()) break;
				if (startEqCount == methodRowsB.size()) break;

				EditorRow erA = methodRowsA.get(startEqCount);
				EditorRow erB = methodRowsB.get(startEqCount);
				boolean equal = rowsAreEqual(erA, erB);

				if (!equal) break;
				startEqCount++;
			}
			for (int i=0; i < startEqCount; i++) {
				rowsAll.add(methodRowsA.get(0));
				methodRowsA.remove(0);
				methodRowsB.remove(0);
			}

			// find out the equal instructions at the end of each code block
			int endEqCount = 0;
			while (true) {
				if (endEqCount == methodRowsA.size()) break;
				if (endEqCount == methodRowsB.size()) break;

				EditorRow erA = methodRowsA.get((methodRowsA.size()-1)-endEqCount);
				EditorRow erB = methodRowsB.get((methodRowsB.size()-1)-endEqCount);
				boolean equal = rowsAreEqual(erA, erB);

				if (!equal) break;
				endEqCount++;
			}
			List<EditorRow> equalRowsAtTheEnd = new ArrayList<EditorRow>();
			for (int i=0; i < endEqCount; i++) {
				equalRowsAtTheEnd.add(methodRowsA.get(methodRowsA.size()-1));
				methodRowsA.remove(methodRowsA.size()-1);
				methodRowsB.remove(methodRowsB.size()-1);
			}

			int m = methodRowsA.size();
			int n = methodRowsB.size();
			int[][] C = new int[m+1][n+1];
			lcs(C, methodRowsA, methodRowsB);

			List<EditorRow> common = new ArrayList<EditorRow>();
			bt(C, methodRowsA, methodRowsB, m, n, common);

			for (EditorRow commonRow : common) {
				// rows from set A before the next common row
				while (!rowsAreEqual(methodRowsA.get(0), commonRow)) {
					rowsAll.add(methodRowsA.get(0));
					rowsA.add(methodRowsA.get(0));
					methodRowsA.remove(0);
				}

				// rows from set B before the next common row
				while (!rowsAreEqual(methodRowsB.get(0), commonRow)) {
					rowsAll.add(methodRowsB.get(0));
					rowsB.add(methodRowsB.get(0));
					methodRowsB.remove(0);
				}

				rowsAll.add(commonRow);
				methodRowsA.remove(0);
				methodRowsB.remove(0);
			}

			rowsA.addAll(methodRowsA);
			rowsAll.addAll(methodRowsA);

			rowsB.addAll(methodRowsB);
			rowsAll.addAll(methodRowsB);

			rowsAll.addAll(equalRowsAtTheEnd);
			rowsAll.add(new MethodDefRow(cfA, methodA, false, true));
		}

	}

	private static List<EditorRow> getMethodRows(List<Instruction> instructions, Code code, MethodDefRow mdr, LineNumberTableAttribute lnAttr, LocalVariableTableAttribute lvs, DecompilationContext dc, ClassFile cf) {
		List<EditorRow> list = new ArrayList<EditorRow>();

		for (Instruction instruction : instructions) {

			if (instruction instanceof Label) {
				LabelRow lr = new LabelRow((Label) instruction, mdr);
				lr.setParentCode(code);
				list.add(lr);
				mdr.addCodeRow(lr);
			} else {
				int lineNumber = -1;

				if (lnAttr != null) {
					lineNumber = lnAttr.getLineNumber(dc.getPosition());
				}
				if (lvs != null) {
					List<?> locals = lvs
							.getLocalVariable(dc.getPosition());
					for (int k = 0; k < locals.size(); k++) {
						LocalVariable lv = (LocalVariable) locals
								.get(k);
						LocalVariableDefRow lvdr = new LocalVariableDefRow(
								lv, mdr);
						list.add(lvdr);
						mdr.addLocalVariable(lvdr);
					}
				}

				CodeRow cd = new CodeRow(cf, mdr, instruction);
				cd.setPosition(dc.getPosition());
				cd.setDecompilationContext(dc);
				cd.setParentCode(code);

				if (lineNumber != -1) {
					cd.setLineNumber(lineNumber);
				}

				list.add(cd);
				mdr.addCodeRow(cd);

				dc.incrementPosition(instruction);
			}

		}

		return list;
	}

	public static boolean rowsAreEqual(EditorRow erA, EditorRow erB) {
		if (!erA.getClass().equals(erB.getClass())) return false;

		if (erA instanceof LocalVariableDefRow) {
			LocalVariableDefRow lvdrA = (LocalVariableDefRow) erA;
			LocalVariableDefRow lvdrB = (LocalVariableDefRow) erB;
			return lvdrA.getLocalVariable().getName().equals(lvdrB.getLocalVariable().getName());
		} else if (erA instanceof LabelRow) {
			LabelRow lrA = (LabelRow) erA;
			LabelRow lrB = (LabelRow) erB;
			return lrA.getLabel().getId().equals(lrB.getLabel().getId());
		} else if (erA instanceof CodeRow) {
			CodeRow crA = (CodeRow) erA;
			CodeRow crB = (CodeRow) erB;
			Instruction instA = crA.getInstruction();
			Instruction instB = crB.getInstruction();
			boolean opCodesEqual = (instA.getOpcode() == instB.getOpcode());
			if (!opCodesEqual) return false;

			return instA.getParameters().getString(crA.getDecompilationContext()).equals(instB.getParameters().getString(crB.getDecompilationContext()));
		} else {
			throw new AssertionError("Invalid object type: " + erA.getClass());
		}
	}

	public static void lcs(int[][] C, List<EditorRow> X, List<EditorRow> Y) {
		for (int i = 1; i < X.size()+1; i++) {
			for(int j = 1; j < Y.size()+1; j++) {
				if (rowsAreEqual(X.get(i-1), Y.get(j-1))) {
					C[i][j] = C[i-1][j-1] + 1;
				} else {
					C[i][j] = Math.max(C[i][j-1], C[i-1][j]);
				}
			}
		}
	}

	public static void bt(int[][] C, List<EditorRow> X, List<EditorRow> Y, int i, int j, List<EditorRow> list) {
		if (i == 0 || j == 0) {
			return;
		} else if (rowsAreEqual(X.get(i-1), Y.get(j-1))) {
			bt(C, X, Y, i-1, j-1, list);
			list.add(X.get(i-1));
		} else {
			if (C[i][j-1] > C[i-1][j]) {
				bt(C, X, Y, i, j-1, list);
			} else {
				bt(C, X, Y, i-1, j, list);
			}
		}
	}

}
