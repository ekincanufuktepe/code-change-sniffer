package ccs.markov.slicer;

public class RegexRulesForForwardSlicingRules {

	private static String[] rulesDirectLeft = {
			".*\\s",
			".*\\s",
			".*\\s\\$",
			"\\$",
			".*\\[",
			".*\\(",
			".*\\[\\$",
			".*\\(\\$",
			".*\\s",
	};
	
	private static String[] rulesDirectRight = {
			"\\s.*",
			"\\..*",
			"\\s.*",
			"\\s.*",
			"\\].*",
			"\\).*",
			"\\].*",
			"\\).*",
			"\".*",
	}; 
	
	public static String[] rules(String variable)
	{
		String [] ruleSet = new String[rulesDirectLeft.length];
		
		for(int i=0; i<ruleSet.length; i++)
		{
			//if(variable.contains("$"))
			if(variable.contains("$r"))
				ruleSet[i] = rulesDirectLeft[i]+"\\"+variable+rulesDirectRight[i];
			else
				ruleSet[i] = rulesDirectLeft[i]+variable+rulesDirectRight[i];
		}
		
		return ruleSet;
	}
	
}
