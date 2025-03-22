package jadx.plugins.ai.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jadx.plugins.ai.utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PromptConfigManager {
	private Map<String, Map<String, Map<PromptType, String>>> configMap;

	public PromptConfigManager() {
		configMap = new HashMap<>();
	}


	public static void createDefaultPrompt(String filePath) throws IOException {
		if (new File(filePath).exists()) return;
		Helper.writeResourceToTargetFile("DefaultPrompt.json",filePath);
	}
	public static PromptConfigManager getConfigFromJson(String filePath) throws IOException {
		PromptConfigManager promptConfigManager = new PromptConfigManager();
		promptConfigManager.loadConfigFromJson(filePath);
		return promptConfigManager;
	}

	public void loadConfigFromJson(String filePath) throws IOException {
		createDefaultPrompt(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Map<String, Map<String, String>>> tempMap =
				objectMapper.readValue(new File(filePath), new TypeReference<HashMap<String, Map<String, Map<String, String>>>>() {});

		configMap = new HashMap<>();
		for (Map.Entry<String, Map<String, Map<String, String>>> categoryEntry : tempMap.entrySet()) {
			String category = categoryEntry.getKey();
			Map<String, Map<PromptType, String>> newCategoryMap = new HashMap<>();
			for (Map.Entry<String, Map<String, String>> keyEntry : categoryEntry.getValue().entrySet()) {
				String key = keyEntry.getKey();
				Map<PromptType, String> newTypeMap = new HashMap<>();
				for (Map.Entry<String, String> typeEntry : keyEntry.getValue().entrySet()) {
					PromptType typeKey = PromptTypeConverter.getType(typeEntry.getKey());
					if (typeKey != null) {
						newTypeMap.put(typeKey, typeEntry.getValue());
					}
				}
				newCategoryMap.put(key, newTypeMap);
			}
			configMap.put(category, newCategoryMap);
		}
	}

	public void addConfig(String category, String key, PromptType type, String value) {
		configMap.computeIfAbsent(category, k -> new HashMap<>())
				.computeIfAbsent(key, k -> new HashMap<>())
				.put(type, value);
	}

	public String getConfigValue(String category, String key, PromptType type) {
		Map<String, Map<PromptType, String>> categoryMap = configMap.get(category);
		if (categoryMap != null) {
			Map<PromptType, String> typeMap = categoryMap.get(key);
			if (typeMap != null) {
				return typeMap.get(type);
			}
		}
		return null;
	}

	public Map<String, Map<String, Map<PromptType, String>>> getConfigMap() {
		return configMap;
	}
}
