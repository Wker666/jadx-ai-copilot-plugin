package jadx.plugins.ai.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jadx.plugins.ai.config.OpenAiConfig;
import jadx.plugins.ai.module.QAMap;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LangchainOpenAiChatModel {
	private OpenAiChatModel ChatModel;
	private OpenAiConfig config;

	SystemMessage systemMessage;

	private static LangchainOpenAiChatModel instance = null;

	public static LangchainOpenAiChatModel getInstance(OpenAiConfig aConfig){
		if(instance == null){
			instance = new LangchainOpenAiChatModel(aConfig);
		}
		return instance;
	}

	public static String ask(List<QAMap> history){
		return getInstance(null).askByHistory(history);
	}

	public static String ask(String question){
		List<QAMap> arrayList = new ArrayList<>();
		QAMap qaMap = new QAMap();
		qaMap.setQue(question);
		arrayList.add(qaMap);
		return getInstance(null).askByHistory(arrayList);
	}

	private LangchainOpenAiChatModel(OpenAiConfig aConfig) {
		this.config = aConfig;
		systemMessage = new SystemMessage(aConfig.getSystemMsg());
		createChatModuleByConfig();
	}

	public void createChatModuleByConfig(){
		try{
			OpenAiChatModel.OpenAiChatModelBuilder openAiChatModelBuilder = OpenAiChatModel.builder()
					.apiKey(config.getApiKey())
					.baseUrl(config.getBaseUrl())
					.temperature(config.getTemperature())
					.maxTokens(config.getMaxTokens())
					.modelName(config.getModelName())
					.timeout(Duration.ofSeconds(config.getTimeout()))
					.maxRetries(config.getMaxRetries());
			if(config.getUseProxy()){
				openAiChatModelBuilder.proxy(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(config.getProxyHost(),config.getProxyPort())));
			}
			this.ChatModel = openAiChatModelBuilder.build();
		}catch (Exception e){
		}
	}

	public String askByHistory(List<QAMap> history) {
		if(history.isEmpty()) return "";
		// not last answer, add system msg
		int msgCtxSize = history.size();
		ChatMessage[] msgs = new ChatMessage[msgCtxSize * 2 + 1 - 1];
		msgs[0] = systemMessage;
		for (int idx = 0;idx < msgCtxSize;idx++) {
			QAMap qaMap = history.get(idx);
			msgs[idx*2+1] = UserMessage.from(qaMap.getQue());
			if(idx < msgCtxSize - 1){
				msgs[idx*2+2] = new AiMessage(qaMap.getAns());
			}
		}
		return ChatModel.generate(msgs).content().text();
	}

	public OpenAiConfig getConfig() {
		return config;
	}

}
