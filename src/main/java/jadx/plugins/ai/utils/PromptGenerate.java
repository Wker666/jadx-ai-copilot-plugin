package jadx.plugins.ai.utils;

import jadx.api.JavaNode;

public class PromptGenerate {

	public static String CodeWrapper(JavaNode node){
		return "\n" + "```java" + "\n" +CodeExtractor.getCode(node) + "\n" + "```" + "\n";

	}
	public static String GenerateQueByNode(JavaNode node,String prompt){
		return prompt + CodeWrapper(node);
	}

	public static String GenerateQueByEdge(JavaNode source,JavaNode target,String prompt){

		return prompt + CodeWrapper(source) + CodeWrapper(target);
	}

}
