package ccs.markov.slicer;

import java.util.Set;

public class ProgramSlicer {
	
	private Set<String> relVars;
	private Set<String> relStatements;
	private Set<String> relBranching;
	private Set<String> INFL_b; // the path from program P to b that contains a set of statements
	
	public Set<String> getRelVars() {
		return relVars;
	}
	
	public void setRelVars(Set<String> relVars) {
		this.relVars = relVars;
	}
	
	public Set<String> getRelStatements() {
		return relStatements;
	}
	
	public void setRelStatements(Set<String> relStatements) {
		this.relStatements = relStatements;
	}
	
	public Set<String> getRelBranching() {
		return relBranching;
	}
	
	public void setRelBranching(Set<String> relBranching) {
		this.relBranching = relBranching;
	}
	
	public Set<String> getINFL_b() {
		return INFL_b;
	}
	
	public void setINFL_b(Set<String> iNFL_b) {
		INFL_b = iNFL_b;
	}
	

}
