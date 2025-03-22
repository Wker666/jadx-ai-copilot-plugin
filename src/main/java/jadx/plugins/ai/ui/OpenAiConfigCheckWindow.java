package jadx.plugins.ai.ui;

import jadx.core.utils.StringUtils;
import jadx.plugins.ai.ai.LangchainOpenAiChatModel;
import jadx.plugins.ai.config.OpenAiConfig;
import jadx.plugins.ai.config.PromptConfigManager;
import jadx.plugins.ai.config.PromptType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class OpenAiConfigCheckWindow extends JFrame {

	public OpenAiConfigCheckWindow() {
		OpenAiConfig config = LangchainOpenAiChatModel.getInstance(null).getConfig();
		setTitle("Config Check");
		setSize(600, 400);
		setLayout(new BorderLayout());

		JScrollPane scrollPane = getjScrollPane(config);
		add(scrollPane, BorderLayout.CENTER);

        try {
            JScrollPane treeScrollPane = new JScrollPane(createTree(
					PromptConfigManager.getConfigFromJson(FlowchartUI.PROMPT_FILE).getConfigMap()));
			add(treeScrollPane, BorderLayout.WEST);
        } catch (IOException e) {
			JOptionPane.showMessageDialog(OpenAiConfigCheckWindow.this, "not found prompt json." + e, "error:" , JOptionPane.ERROR_MESSAGE);
        }

		JPanel buttonPanel = getButtonPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	@NotNull
	private JPanel getButtonPanel() {
		JButton testButton = new JButton("test");
		testButton.addActionListener(e -> {
			int result = JOptionPane.showConfirmDialog(OpenAiConfigCheckWindow.this,
					"A hello message will be sent to the target Ai server. Do you accept it?", "ask", JOptionPane.YES_NO_OPTION);
            if(result == JOptionPane.NO_OPTION) return;
			String s = LangchainOpenAiChatModel.ask("hello");
            if(StringUtils.isEmpty(s)){
				JOptionPane.showMessageDialog(OpenAiConfigCheckWindow.this, "ai rep null.", "error:" , JOptionPane.ERROR_MESSAGE);
            }else{
				JOptionPane.showMessageDialog(OpenAiConfigCheckWindow.this, "ai rep success: "+s, "succ:" , JOptionPane.INFORMATION_MESSAGE);
			}
        });
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(testButton);
		return buttonPanel;
	}

	@NotNull
	private static JScrollPane getjScrollPane(OpenAiConfig config) {
		String[] columnNames = {"name", "value"};
		Object[][] data = {
				{"baseUrl", config.getBaseUrl()},
				{"model", config.getModelName()},
				{"apk-key", config.getApiKey() != null ? "********************************" : ""},
				{"system message", config.getSystemMsg()},
				{"max tokens", config.getMaxTokens()},
				{"temperature", config.getTemperature()},
				{"max retries", config.getMaxRetries()}
		};

		JTable table = new JTable(data, columnNames);
		table.setEnabled(false);
		return new JScrollPane(table);
	}

	private static JTree createTree(Map<String, Map<String, Map<PromptType, String>>> dataMap) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Prompt");

		for (Map.Entry<String, Map<String, Map<PromptType, String>>> outerEntry : dataMap.entrySet()) {
			String outerKey = outerEntry.getKey();
			DefaultMutableTreeNode outerNode = new DefaultMutableTreeNode("Category: " + outerKey);
			root.add(outerNode);

			Map<String, Map<PromptType, String>> innerMap = outerEntry.getValue();
			for (Map.Entry<String, Map<PromptType, String>> innerEntry : innerMap.entrySet()) {
				String innerKey = innerEntry.getKey();
				DefaultMutableTreeNode innerNode = new DefaultMutableTreeNode("Info: " + innerKey);
				outerNode.add(innerNode);

				Map<PromptType, String> innermostMap = innerEntry.getValue();
				for (Map.Entry<PromptType, String> innermostEntry : innermostMap.entrySet()) {
					PromptType promptType = innermostEntry.getKey();
					String value = innermostEntry.getValue();
					DefaultMutableTreeNode innermostNode = new DefaultMutableTreeNode("Prompt Type: " + promptType + ", Value: " + value);
					innerNode.add(innermostNode);
				}
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		return new JTree(treeModel);
	}
}
