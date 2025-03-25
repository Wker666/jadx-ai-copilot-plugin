package jadx.plugins.ai;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;
import jadx.plugins.ai.ai.LangchainOpenAiChatModel;
import jadx.plugins.ai.ui.FlowchartUI;

public class JadxAiViewOptions extends BasePluginOptionsBuilder {

	@Override
	public void registerOptions() {
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Prompt Json File Path")
				.description("Set Prompt Json File Path")
				.defaultValue(FlowchartUI.PROMPT_FILE)
				.setter(s -> FlowchartUI.PROMPT_FILE = s);

		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Gpt modelName")
				.description("Set Gpt modelName")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getModelName())
				.setter(s -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setModelName(s);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Gpt baseUrl")
				.description("Set Gpt baseUrl")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getBaseUrl())
				.setter(s -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setBaseUrl(s);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Gpt Api Key")
				.description("Set Api Key")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getApiKey())
				.setter(s -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setApiKey(s);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Gpt System Messages")
				.description("Set Gpt System Messages")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getSystemMsg())
				.setter(s -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setSystemMsg(s);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		intOption(JadxAiPlugin.PLUGIN_ID + ".Set maxTokens")
				.description("Set maxTokens")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getMaxTokens())
				.setter(i -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setMaxTokens(i);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		intOption(JadxAiPlugin.PLUGIN_ID + ".Set temperature * 10")
				.description("Set temperature * 10")
				.defaultValue((int) (LangchainOpenAiChatModel.getInstance(null).getConfig().getTemperature() * 10))
				.setter(i -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setTemperature((double) i / 10);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		intOption(JadxAiPlugin.PLUGIN_ID + ".Set maxRetries")
				.description("Set maxRetries")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getMaxRetries())
				.setter(i -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setMaxRetries(i);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});


		boolOption(JadxAiPlugin.PLUGIN_ID + ".Use Socks5 Proxy")
				.description("Use Socks5 Proxy")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getUseProxy())
				.setter(i -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setUseProxy(i);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});


		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Socks5 Proxy Host")
				.description("Set Proxy Host")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getProxyHost())
				.setter(s -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setProxyHost(s);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		intOption(JadxAiPlugin.PLUGIN_ID + ".Set Socks5 Proxy Port")
				.description("Set Socks5 Proxy Port")
				.defaultValue(LangchainOpenAiChatModel.getInstance(null).getConfig().getProxyPort())
				.setter(i -> {LangchainOpenAiChatModel.getInstance(null).getConfig().setProxyPort(i);LangchainOpenAiChatModel.getInstance(null).createChatModuleByConfig();});

		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Rename method prompt")
				.description("Set Rename method prompt")
				.defaultValue(JadxAiViewAction.RENAME_METHOD)
				.setter(s -> JadxAiViewAction.RENAME_METHOD = s);
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Rename class prompt")
				.description("Set Rename class prompt")
				.defaultValue(JadxAiViewAction.RENAME_CLASS)
				.setter(s -> JadxAiViewAction.RENAME_CLASS = s);
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Rename Class All method prompt")
				.description("Set Rename Class All method prompt")
				.defaultValue(JadxAiViewAction.RENAME_ALL_METHOD)
				.setter(s -> JadxAiViewAction.RENAME_ALL_METHOD = s);
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Comment method prompt")
				.description("Set Comment method prompt")
				.defaultValue(JadxAiViewAction.COMMENT_METHOD)
				.setter(s -> JadxAiViewAction.COMMENT_METHOD = s);
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Comment class prompt")
				.description("Set Comment class prompt")
				.defaultValue(JadxAiViewAction.COMMENT_CLASS)
				.setter(s -> JadxAiViewAction.COMMENT_CLASS = s);
		strOption(JadxAiPlugin.PLUGIN_ID + ".Set Decompile prompt")
				.description("Set Decompile prompt")
				.defaultValue(JadxAiViewAction.DECOMPILE_METHOD)
				.setter(s -> JadxAiViewAction.DECOMPILE_METHOD = s);
	}

}
