package jadx.plugins.ai;

import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.gui.JadxGuiContext;
import jadx.plugins.ai.ai.LangchainOpenAiChatModel;
import jadx.plugins.ai.config.OpenAiConfig;
import jadx.plugins.ai.ui.OpenAiConfigCheckWindow;
import jadx.plugins.ai.utils.AiConfigHelper;


public class JadxAiPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "Ai-Copilot";

	@Override
	public JadxPluginInfo getPluginInfo() {
		return new JadxPluginInfo(PLUGIN_ID, "Ai-Copilot", "An AI copilot improves reverse analysis capabilities"
				,"https://github.com/Wker666/jadx-ai-copilot-plugin","Ai copilot");
	}

	@Override
	public void init(JadxPluginContext context) {
		OpenAiConfig openAiConfig;
		try {
			openAiConfig = AiConfigHelper.loadConfigsFromFile(AiConfigHelper.AI_CONFIG_NAME);
		} catch (Exception e) {
			openAiConfig = new OpenAiConfig();
		}
		LangchainOpenAiChatModel.getInstance(openAiConfig);
		JadxAiViewOptions options = new JadxAiViewOptions();
		context.registerOptions(options);
		JadxGuiContext guiContext = context.getGuiContext();
		if (guiContext != null) {
				guiContext.addMenuAction("Jadx AI Copilot", () -> {
					new OpenAiConfigCheckWindow();
				});
		}
		JadxAiViewAction.addToPopupMenu(context);
	}
}
