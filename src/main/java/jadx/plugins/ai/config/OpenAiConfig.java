package jadx.plugins.ai.config;

public class OpenAiConfig {
	private String baseUrl = "https://api.openai.com/v1";
	private String modelName = "gpt-4o";
	private String apiKey;
	private String systemMsg = "You are a professional Android and Java engineer, helping to analyze the code content that will be sent to you:";
	private int maxTokens = 3000;
	private double temperature = 0;
	private int maxRetries = 3;

	private boolean useProxy = false;
	private String proxyHost = "127.0.0.1";
	private int proxyPort = 1080;

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

	public boolean getUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Override
	public String toString() {
		return "OpenAiConfig{" +
				"baseUrl='" + baseUrl + '\'' +
				", modelName='" + modelName + '\'' +
				", apiKey='" + apiKey + '\'' +
				", systemMsg='" + systemMsg + '\'' +
				", maxTokens=" + maxTokens +
				", temperature=" + temperature +
				", maxRetries=" + maxRetries +
				", useProxy=" + useProxy +
				", proxyHost='" + proxyHost + '\'' +
				", proxyPort=" + proxyPort +
				'}';
	}
}
