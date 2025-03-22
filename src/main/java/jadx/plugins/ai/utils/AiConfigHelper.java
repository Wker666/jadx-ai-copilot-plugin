package jadx.plugins.ai.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jadx.plugins.ai.config.OpenAiConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AiConfigHelper {

	public static final String AI_CONFIG_NAME = "aiConfig.json";

	public static OpenAiConfig loadConfigsFromJson(String jsonString) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(jsonString, new TypeReference<>() {});
	}
	public static OpenAiConfig loadConfigsFromFile(String filePath) throws IOException {
		String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
		return loadConfigsFromJson(jsonString);
	}
}
