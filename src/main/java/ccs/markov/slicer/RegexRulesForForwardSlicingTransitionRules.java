package ccs.markov.slicer;

public class RegexRulesForForwardSlicingTransitionRules {
	
//	String regexRule1 = ".*\\s"+variable.getVariableName()+"\\s.*";
//	String regexRule2 = ".*\\s"+variable.getVariableName()+"\\..*";
//	// Variables with $ usage
//	// EXAMPLES
//	// 3) ... xyz $VAR abc ...
//	// 4) $VAR abc ...
//	String regexRule3 = ".*\\s\\$"+variable.getVariableName()+"\\s.*";
//	String regexRule4 = "\\$"+variable.getVariableName()+"\\s.*";
//	// Variables within parenthesis
//	// EXAMPLES
//	// 5) ... xyz[VAR]...
//	// 6) ... xyz(VAR)...
//	// 7) ... xyz[$VAR]...
//	// 8) ... xyz($VAR)...
//	String regexRule5 = ".*\\["+variable.getVariableName()+"\\].*";
//	String regexRule6 = ".*\\("+variable.getVariableName()+"\\).*";
//	String regexRule7 = ".*\\[\\$"+variable.getVariableName()+"\\].*";
//	String regexRule8 = ".*\\(\\$"+variable.getVariableName()+"\\).*";
//	// 9) ... xyz VAR"
//	// 10) "VAR xyz ...
//	String regexRule9 = ".*\\s"+variable.getVariableName()+"\".*";
//	String regexRule10 = ".*\""+variable.getVariableName()+"\\s.*";
	
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
	
	// [label="r2 = r1.getDeliveryDay()",];
	
	private static String[]transitionLeft = {
			// TOTAL 40 rules
			// 16 standard rules
			".*\\s.*\\s\\=\\s.*\\s", // 5) *** VAR1 = ***
			".*\\s.*\\s\\=\\s.*\\s", // 6) *** VAR1 = ***
			".*\\s.*\\s\\=\\s.*\\s", // 7) *** VAR1 = ***
			".*\\s.*\\s\\=\\s.*\\s", // 8) *** VAR1 = ***
			
			".*\".*\\s\\=\\s", // 1) ***"VAR1 = 
			".*\".*\\s\\=\\s", // 2) ***"VAR1 = 
			".*\".*\\s\\=\\s", // 3) ***"VAR1 = 
			".*\".*\\s\\=\\s", // 4) ***"VAR1 = 
			
			".*\\s\\$.*\\s\\=\\s.*\\s", // 13) *** $VAR1 = ***
			".*\\s\\$.*\\s\\=\\s.*\\s", // 14) *** $VAR1 = ***
			".*\\s\\$.*\\s\\=\\s.*\\s", // 15) *** $VAR1 = ***
			".*\\s\\$.*\\s\\=\\s.*\\s", // 16) *** $VAR1 = ***
			
			".*\"\\$.*\\s\\=\\s", // 9) ***"$VAR1 =
			".*\"\\$.*\\s\\=\\s", // 10) ***"$VAR1 =
			".*\"\\$.*\\s\\=\\s", // 11) ***"$VAR1 =
			".*\"\\$.*\\s\\=\\s", // 12) ***"$VAR1 =
			
			// 16 Parenthesis without blanks
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\[", // 17) *** $VAR1 = ***[
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\[", // 18) *** $VAR1 = ***[	
			
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\(", // 19) *** $VAR1 = ***(
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\(", // 20) *** $VAR1 = ***(
			
			".*\"\\$.*\\s\\=\\s.*\\[", // 21) ***"$VAR1 = ***[
			".*\"\\$.*\\s\\=\\s.*\\[", // 22) ***"$VAR1 = ***[
			
			".*\"\\$.*\\s\\=\\s.*\\(", // 23) ***"$VAR1 = ***(
			".*\"\\$.*\\s\\=\\s.*\\(", // 24) ***"$VAR1 = ***(
			
			".*\\s.*\\s\\=\\s.*\\[", // 25) *** VAR1 = ***[
			".*\\s.*\\s\\=\\s.*\\[", // 26) *** VAR1 = ***[
			
			".*\\s.*\\s\\=\\s.*\\(", // 27) *** VAR1 = ***(
			".*\\s.*\\s\\=\\s.*\\(", // 28) *** VAR1 = ***(
			
			".*\".*\\s\\=\\s.*\\[", // 29) ***"VAR1 = ***[
			".*\".*\\s\\=\\s.*\\[", // 30) ***"VAR1 = ***[
			
			".*\".*\\s\\=\\s.*\\(", // 31) ***"VAR1 = ***(
			".*\".*\\s\\=\\s.*\\(", // 32) ***"VAR1 = ***(
			
			// 8 paranthesis rules with empty spaces for ( paranthesis
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\s\\(", // 33) *** $VAR1 = *** (
			".*\\s\\$.*\\s\\=\\s.*\\s.*\\s\\(", // 34) *** $VAR1 = *** (
			
			".*\"\\$.*\\s\\=\\s.*\\s\\(", // 35) ***"$VAR1 = *** (
			".*\"\\$.*\\s\\=\\s.*\\s\\(", // 36) ***"$VAR1 = *** (
			
			".*\\s.*\\s\\=\\s.*\\s\\(", // 37) *** VAR1 = *** (
			".*\\s.*\\s\\=\\s.*\\s\\(", // 38) *** VAR1 = *** (
			
			".*\".*\\s\\=\\s.*\\s\\(", // 39) ***"VAR1 = *** (
			".*\".*\\s\\=\\s.*\\s\\(" // 40) ***"VAR1 = *** (
			
			//".*\"\\$.*\\s\\=\\s" // ***"$VAR1 = 
	};
	
	private static String[] transitionRight = {
			// TOTAL 40 rules
			// 16 standard rules
			"\\..*", // 5) VAR2.***
			"\\..*\".*", // 7) VAR2.***"***
			"\".*", // 6) VAR2"*** 
			".*", // 8) VAR2 *** 
			
			"\\..*", // 1) VAR2.***
			"\\..*\".*", // 3) VAR2.***"***
			"\".*", // 2) VAR2"*** 
			".*", // 4) VAR2 ***
			
			"\\..*", // 13) VAR2.***
			"\\..*\".*", // 15) VAR2.***"***
			"\".*", // 14) VAR2"*** 
			".*", // 16) VAR2 ***
			
			"\\..*", // 9) VAR2.***
			"\\..*\".*", // 11) VAR2.***"***
			"\".*", // 10) VAR2"*** 
			".*", // 12) VAR2 ***
			
			//16 Parenthesis rules for no blanks
			"\\..*\\].*", // 17) VAR2.***]***
			"\\].*", // 18) VAR2]***
			
			"\\..*\\).*", // 19) VAR2.***)***
			"\\).*", // 20) VAR2)***
			
			"\\..*\\].*", // 21) VAR2.***]***
			"\\].*", // 22) VAR2]***
			
			"\\..*\\).*", // 23) VAR2.***)***
			"\\).*", // 24) VAR2)***

			"\\..*\\].*", // 25) VAR2.***]***
			"\\].*", // 26) VAR2]***
			
			"\\..*\\).*", // 27) VAR2.***)***
			"\\).*", // 28) VAR2)***
			
			"\\..*\\].*", // 29) VAR2.***]***
			"\\].*", // 30) VAR2]***
			
			"\\..*\\).*", // 31) VAR2.***)***
			"\\).*", // 32) VAR2)***
			
			// 8 rules for empty spaces with ) paranthesis
			"\\..*\\).*", // 33) VAR2.***)***
			"\\).*", // 34) VAR2)***
			
			"\\..*\\).*", // 35) VAR2.***)***
			"\\).*", // 36) VAR2)***
			
			"\\..*\\).*", // 37) VAR2.***)***
			"\\).*", // 38) VAR2)***
			
			"\\..*\\).*", // 39) VAR2.***)***
			"\\).*" // 40) VAR2)***

			//"\".*" // VAR2"***
	};
	
	public static String[] rules(String variable)
	{
		String [] ruleSet = new String[rulesDirectLeft.length];
		
		for(int i=0; i<ruleSet.length; i++)
		{
			if(variable.contains("$"))
				ruleSet[i] = rulesDirectLeft[i]+"\\"+variable+rulesDirectRight[i];
			else
				ruleSet[i] = rulesDirectLeft[i]+variable+rulesDirectRight[i];
		}
		
		return ruleSet;
	}
	
	public static String[] transitionRules(String variable)
	{
		String [] transitionRuleSet = new String[transitionLeft.length];
		
		for(int i=0; i<transitionRuleSet.length; i++)
		{
			if(variable.contains("$"))
				transitionRuleSet[i] = transitionLeft[i]+"\\"+variable+transitionRight[i];
			else
				transitionRuleSet[i] = transitionLeft[i]+variable+transitionRight[i];
		}
		
		return transitionRuleSet;
	}

}
