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

import java.io.File;
import java.io.IOException;
import net.sf.rej.files.FileSet;
import net.sf.rej.gui.SystemFacade;

import org.apache.bcel.classfile.ClassParser;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 * 
 */
public class JCallGraph {

	private FileSet classFiles;
	
	
	
    public void createCall(String[] args) 
    {
        ClassParser cp;
        
        
        try {
            for (String arg : args) {

                File f = new File(arg);
                classFiles = SystemFacade.getInstance().getFileSet(f);
                
                if (!f.exists()) {
                    System.err.println("Project " + arg + " does not exist");
                }
                
                for(String classFile : classFiles.getContentsList())
                {
                	
//                }
//                
//                JarFile jar = new JarFile(f);
//
//                Enumeration<JarEntry> entries = jar.entries();
//                while (entries.hasMoreElements()) {
//                    JarEntry entry = entries.nextElement();
                	File directoryCheck = new File(classFile);
                    if (directoryCheck.isDirectory())
                    	continue;
                        

                    if (!classFile.endsWith(".class"))
                    	continue;

                    
//                    System.out.println(arg+classFile);
                    cp = new ClassParser(arg+"/"+classFile);
                    ClassVisitor visitor = new ClassVisitor(cp.parse());
                    
                    visitor.start();
                    visitor.start();
//                    System.out.println("Set: "+MethodVisitor.graph);
                }
            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
