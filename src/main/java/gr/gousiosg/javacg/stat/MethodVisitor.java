/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 * 
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

	static public HashMap<String,Set<String>> graph = new HashMap<>();
	static public HashMap<String,Set<String>> detailedGraph = new HashMap<>();

	JavaClass visitedClass;
	private MethodGen mg;
	private ConstantPoolGen cp;
	private String format;
	private String detailedFormat;

	public MethodVisitor(MethodGen m, JavaClass jc) {
		visitedClass = jc;
		mg = m;
		cp = mg.getConstantPool();
		int argNum = mg.getArgumentNames().length;
		ArrayList<String> argTypes = new ArrayList<>();
		ArrayList<String> detailedArgTypes = new ArrayList<>();
		for(int i=0; i<argNum; i++)
		{
			if(mg.getArgumentType(i).toString().contains("."))
			{
				argTypes.add(mg.getArgumentType(i).toString().substring(mg.getArgumentType(i).toString().lastIndexOf(".")+1));
				detailedArgTypes.add(mg.getArgumentType(i).toString());
			}
			else
			{
				argTypes.add(mg.getArgumentType(i).toString());
				detailedArgTypes.add(mg.getArgumentType(i).toString());
			}
				
		}
		format = "M:" + visitedClass.getClassName() + ":" + mg.getName() + ":" + argNum + ":" + argTypes.toString()
				+ " --> " + "(%s)%s:%s:%s:%s";
		detailedFormat = "M:" + visitedClass.getClassName() + ":" + mg.getName() + ":" + argNum + ":" + detailedArgTypes.toString()
		+ " --> " + "(%s)%s:%s:%s:%s";
	}

	public void start() {
		if (mg.isAbstract() || mg.isNative())
			return;
		for (InstructionHandle ih = mg.getInstructionList().getStart(); 
				ih != null; ih = ih.getNext()) {
			Instruction i = ih.getInstruction();
			if (!visitInstruction(i))
				i.accept(this);
		}
	}

	private boolean visitInstruction(Instruction i) {
		short opcode = i.getOpcode();

		return ((InstructionConstants.INSTRUCTIONS[opcode] != null)
				&& !(i instanceof ConstantPushInstruction) 
				&& !(i instanceof ReturnInstruction));
	}

	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
		int empty = format.indexOf(" -->");
		String classMethodName = format.substring(2, empty);
		
		int empty2 = detailedFormat.indexOf(" -->");
		String classMethodName2 = detailedFormat.substring(2, empty2);
		int argNum = i.getArgumentTypes(cp).length;
		ArrayList<String> argTypes = new ArrayList<>();
		ArrayList<String> detailedArgTypes = new ArrayList<>();
		for(int j=0; j<argNum; j++)
		{
			if(i.getArgumentTypes(cp)[j].toString().contains("."))
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString().substring(i.getArgumentTypes(cp)[j].toString().lastIndexOf(".")+1));
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
			else
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString());
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
				
		}
		
		if(graph.containsKey(classMethodName))
		{
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
			
		}
		else
		{
			graph.put(classMethodName, new HashSet<String>());
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
		}
		
		if(detailedGraph.containsKey(classMethodName2))
		{
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		else
		{
			detailedGraph.put(classMethodName2, new HashSet<String>());
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		System.out.println(String.format(format,"M",i.getReferenceType(cp),i.getMethodName(cp),argNum,argTypes.toString()));
		System.out.println(String.format(detailedFormat,"M",i.getReferenceType(cp),i.getMethodName(cp),argNum,detailedArgTypes.toString()));
	}

	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
		int empty = format.indexOf(" -->");
		String classMethodName = format.substring(2, empty);

		int empty2 = detailedFormat.indexOf(" -->");
		String classMethodName2 = detailedFormat.substring(2, empty2);

		int argNum = i.getArgumentTypes(cp).length;
		ArrayList<String> argTypes = new ArrayList<>();
		ArrayList<String> detailedArgTypes = new ArrayList<>();
		for(int j=0; j<argNum; j++)
		{
			if(i.getArgumentTypes(cp)[j].toString().contains("."))
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString().substring(i.getArgumentTypes(cp)[j].toString().lastIndexOf(".")+1));
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
			else
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString());
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
				
		}
		
		if(graph.containsKey(classMethodName))
		{
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
			
		}
		else
		{
			graph.put(classMethodName, new HashSet<String>());
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
		}
		
		if(detailedGraph.containsKey(classMethodName2))
		{
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		else
		{
			detailedGraph.put(classMethodName2, new HashSet<String>());
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}

		System.out.println(String.format(format,"I",i.getReferenceType(cp),i.getMethodName(cp),argNum,argTypes.toString()));
		System.out.println(String.format(detailedFormat,"I",i.getReferenceType(cp),i.getMethodName(cp),argNum,detailedArgTypes.toString()));
	}

	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL i) {
		int empty = format.indexOf(" -->");
		String classMethodName = format.substring(2, empty);

		int empty2 = detailedFormat.indexOf(" -->");
		String classMethodName2 = detailedFormat.substring(2, empty2);

		int argNum = i.getArgumentTypes(cp).length;
		ArrayList<String> argTypes = new ArrayList<>();
		ArrayList<String> detailedArgTypes = new ArrayList<>();
		for(int j=0; j<argNum; j++)
		{
			if(i.getArgumentTypes(cp)[j].toString().contains("."))
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString().substring(i.getArgumentTypes(cp)[j].toString().lastIndexOf(".")+1));
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
			else
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString());
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
				
		}
		
		if(graph.containsKey(classMethodName))
		{
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
			
		}
		else
		{
			graph.put(classMethodName, new HashSet<String>());
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
		}
		
		if(detailedGraph.containsKey(classMethodName2))
		{
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		else
		{
			detailedGraph.put(classMethodName2, new HashSet<String>());
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}

		
		System.out.println(String.format(format,"O",i.getReferenceType(cp),i.getMethodName(cp),argNum,argTypes.toString()));
		System.out.println(String.format(detailedFormat,"O",i.getReferenceType(cp),i.getMethodName(cp),argNum,detailedArgTypes.toString()));
	}

	@Override
	public void visitINVOKESTATIC(INVOKESTATIC i) {
		int argNum = i.getArgumentTypes(cp).length;
		ArrayList<String> argTypes = new ArrayList<>();
		ArrayList<String> detailedArgTypes = new ArrayList<>();
		for(int j=0; j<argNum; j++)
		{
			if(i.getArgumentTypes(cp)[j].toString().contains("."))
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString().substring(i.getArgumentTypes(cp)[j].toString().lastIndexOf(".")+1));
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
			else
			{
				argTypes.add(i.getArgumentTypes(cp)[j].toString());
				detailedArgTypes.add(i.getArgumentTypes(cp)[j].toString());
			}
				
		}
		
		int empty = format.indexOf(" -->");
		String classMethodName = format.substring(2, empty);
		
		int empty2 = detailedFormat.indexOf(" -->");
		String classMethodName2 = detailedFormat.substring(2, empty2);
		
		if(graph.containsKey(classMethodName))
		{
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
		}
		else
		{
			graph.put(classMethodName, new HashSet<String>());
			graph.get(classMethodName).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+argTypes.toString());
		}
		
		if(detailedGraph.containsKey(classMethodName2))
		{
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		else
		{
			detailedGraph.put(classMethodName2, new HashSet<String>());
			detailedGraph.get(classMethodName2).add(i.getReferenceType(cp)+":"+i.getMethodName(cp).toString()+":"+argNum+":"+detailedArgTypes.toString());
		}
		
		System.out.println(String.format(format,"S",i.getReferenceType(cp),i.getMethodName(cp),argNum,argTypes.toString()));
		System.out.println(String.format(detailedFormat,"S",i.getReferenceType(cp),i.getMethodName(cp),argNum,detailedArgTypes.toString()));
	}
}
