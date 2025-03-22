package jadx.plugins.ai.ui;

import jadx.plugins.ai.module.QAMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownProcessor {

	private static final String SOLID_LINE_HTML = "<div style='border-top: 2px solid black; margin-top: 10px;'></div>"; // 实线
	private static final String DASHED_LINE_HTML = "<div style='border-top: 2px dashed black; margin-top: 10px;'></div>"; // 虚线
	private final JPanel panel;
	private JScrollPane scrollPane;

	public MarkdownProcessor() {
		panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		scrollPane = new JScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getViewport().setBackground(Color.WHITE);
	}

	public static String getHtmlByQaHistory(List<QAMap> QueAnsHistory){
		StringBuilder sb = new StringBuilder();
		Parser parser = Parser.builder().build();
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		for (QAMap qaMap : QueAnsHistory) {
			sb.append(renderer.render(parser.parse(qaMap.getQue()))).append("\n").append(DASHED_LINE_HTML)
					.append(renderer.render(parser.parse(qaMap.getAns()))).append("\n").append(SOLID_LINE_HTML);
		}
		return sb.toString();
	}

	public void setMarkdown(List<QAMap> QueAnsHistory) {
		panel.removeAll();

		String html = getHtmlByQaHistory(QueAnsHistory);

		String css = "<style>" +
				"body { font-size: 14px; } " +
				"code { font-size: 14px; background-color: #f9f2f4; padding: 2px 4px; border-radius: 4px; }" +
				"</style>";


		Pattern pattern = Pattern.compile("<pre><code class=\"language-(.*?)\">(.*?)</code></pre>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);

		int lastEnd = 0;
		int rowIndex = 0;

		while (matcher.find()) {
			String nonCodeHtml = html.substring(lastEnd, matcher.start());
			rowIndex = addHtmlComponent(css + nonCodeHtml, rowIndex);
			String language = matcher.group(1);
			String code = StringEscapeUtils.unescapeHtml4(matcher.group(2));
			rowIndex = addCodeComponent(language, code, rowIndex);
			lastEnd = matcher.end();
		}
		String remainingHtml = html.substring(lastEnd);
		addHtmlComponent(css + remainingHtml, rowIndex);

		panel.revalidate();
		panel.repaint();

		// don't know why not refres.
		SwingUtilities.invokeLater(() -> {
			JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
			verticalBar.setValue(verticalBar.getMaximum());
		});
	}

	private int addHtmlComponent(String htmlContent, int rowIndex) {
		if (!htmlContent.trim().isEmpty()) {
			JEditorPane editorPane = new JEditorPane();
			editorPane.setContentType("text/html");
			editorPane.setText(htmlContent);
			editorPane.setEditable(false);
			editorPane.setOpaque(false);  // Make it blend with background
			editorPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Minimize padding

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = rowIndex++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 5, 5);
			panel.add(editorPane, gbc);
		}
		return rowIndex;
	}

	private int addCodeComponent(String language, String code, int rowIndex) {
		RSyntaxTextArea textArea = new RSyntaxTextArea();
		textArea.setSyntaxEditingStyle("text/" + language);
		textArea.setCodeFoldingEnabled(true);
		textArea.setText(code);
		textArea.setEditable(false);

		int lines = code.split("\n").length;
		textArea.setRows(lines);
		textArea.setColumns(80);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		RTextScrollPane sp = new RTextScrollPane(textArea);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = rowIndex++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(sp, gbc);

		return rowIndex;
	}

	public JScrollPane getPanel() {
		return scrollPane;
	}
}
