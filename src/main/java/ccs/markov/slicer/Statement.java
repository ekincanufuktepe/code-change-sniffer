package ccs.markov.slicer;

import java.util.HashSet;
import java.util.Set;

public class Statement {

	private String nodeID;
	private String nodeName;
	private int id;
	private boolean flag = false;
	private boolean sliced = false;
	public boolean isSliced() {
		return sliced;
	}

	public void setSliced(boolean sliced) {
		this.sliced = sliced;
	}

	private Set<Statement> parent = new HashSet<>();
	private Set<Statement> children = new HashSet<>();
	
	public Set<Statement> getParent() {
		return parent;
	}

	public void setParent(Set<Statement> parent) {
		this.parent = parent;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public int getId() {
		return id;
	}

	public void setId() {
		this.id = Integer.parseInt(this.nodeID.replace("\"", ""));
	}

	public Statement(String nodeID, String nodeName) {
		// TODO Auto-generated constructor stub
		this.nodeID = nodeID;
		this.nodeName = nodeName;
		setId();
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Set<Statement> getChildren() {
		return children;
	}

	public void setChild(Set<Statement> child) {
		this.children = child;
	}
	
	
}
