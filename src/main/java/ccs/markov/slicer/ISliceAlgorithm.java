package ccs.markov.slicer;

import java.util.HashMap;
import java.util.Set;

public interface ISliceAlgorithm {

	void slice(HashMap<Statement, Set<Statement>> cfg, SliceCriterion criteria, String BSTag);
	HashMap<Statement, Set<Statement>> traverse(Statement node, HashMap<Statement, Set<Statement>> cfg, SliceCriterion criteria, String tag, Statement startingStatement);

}
