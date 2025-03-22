package jadx.plugins.ai.config;

public class OpenAiConfig {
	private String baseUrl = "https://api.openai.com/v1";
	private String modelName = "gpt-4o";
	private String apiKey;
	private String systemMsg = "You are a professional Android and Java engineer, helping to analyze the code content that will be sent to you:";
	private int maxTokens = 3000;
	private double temperature = 0;
	private int maxRetries = 3;

	public OpenAiConfig() {
	}


	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public int getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public String getSystemMsg() {
		return systemMsg;
	}

	public void setSystemMsg(String systemMsg) {
		this.systemMsg = systemMsg;
	}

	@Override
	public String toString() {
		return "OpenAiConfig{" +
				", baseUrl='" + baseUrl + '\'' +
				", apiKey='" + apiKey + '\'' +
				", maxTokens=" + maxTokens +
				", temperature=" + temperature +
				", modelName='" + modelName + '\'' +
				", maxRetries=" + maxRetries +
				", systemMsg='" + systemMsg + '\'' +
				'}';
	}
}
