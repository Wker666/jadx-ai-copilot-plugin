package jadx.plugins.ai.ui;

import jadx.api.plugins.JadxPluginContext;
import jadx.plugins.ai.config.PromptConfigManager;
import jadx.plugins.ai.module.GraphStructure;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlowchartUI {

	private JTabbedPane tabbedPane = new JTabbedPane();
	public static String PROMPT_FILE = "prompt.json";

	public void show(JadxPluginContext context, GraphStructure node) {
		JFrame frame = new JFrame("Ai Analyze");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1200, 600);
		frame.setLayout(new BorderLayout());
		frame.setAlwaysOnTop(true);

		MarkdownProcessor processor = new MarkdownProcessor();
		tabbedPane.addTab("Ai Answer", new JScrollPane(processor.getPanel()));

		PromptConfigManager configFromJson;
		try {
			configFromJson = PromptConfigManager.getConfigFromJson(PROMPT_FILE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,  "pls confirm that the location of the prompt.json file is the same as in the preferences\n" + e, "prompt.json not found." ,JOptionPane.ERROR_MESSAGE);
			return ;
		}
		GraphPanel graphPanel = new GraphPanel(context, node, configFromJson,processor);
		JSplitPane splitPane = createSplitPane(graphPanel.getGraphComponent(), tabbedPane);

		frame.add(splitPane, BorderLayout.CENTER);
		frame.add(createBottomPanel(), BorderLayout.SOUTH);

		centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	private JSplitPane createSplitPane(Component leftComponent, Component rightComponent) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
		splitPane.setResizeWeight(0.7);
		splitPane.setDividerSize(8);
		return splitPane;
	}

	private JPanel createBottomPanel() {
		JPanel bottomPanel = new JPanel();
		return bottomPanel;
	}

	private void centerFrameOnScreen(JFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - frame.getWidth()) / 2;
		int y = (screenSize.height - frame.getHeight()) / 2;
		frame.setLocation(x, y);
	}
}
