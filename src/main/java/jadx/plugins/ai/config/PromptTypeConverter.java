package jadx.plugins.ai.config;

public class PromptTypeConverter {

	public static PromptType getType(String typeString) {
		return PromptType.valueOf(typeString);
	}
}
