package jadx.plugins.ai.ui;

import jadx.api.JavaNode;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DynamicSuggestionWindow extends JFrame {
	private JTextField suggestionTextField;
	private JList<String> suggestionListUI;
	private DefaultListModel<String> listModel;
	private Map<String, JavaNode> suggestionList;
	private Consumer<JavaNode> onCloseCallback;
	private static final int MAX_SUGGESTIONS = 10;
	private int currentSuggestionIndex = 0;
	private List<String> currentFilteredSuggestions;

	public DynamicSuggestionWindow(String title, Map<String, JavaNode> suggestions, Consumer<JavaNode> onCloseCallback) {
		this.suggestionList = suggestions;
		this.onCloseCallback = onCloseCallback;

		setTitle(title);
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);

		suggestionTextField = new JTextField();
		listModel = new DefaultListModel<>();
		suggestionListUI = new JList<>(listModel);
		suggestionListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		suggestionListUI.setVisibleRowCount(5);
		JScrollPane listScrollPane = new JScrollPane(suggestionListUI);

		suggestionTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSuggestions();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSuggestions();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateSuggestions();
			}

			private void updateSuggestions() {
				String input = suggestionTextField.getText();
				listModel.clear();
				currentSuggestionIndex = 0;

				if (!input.isEmpty()) {
					currentFilteredSuggestions = suggestionList.keySet().stream()
							.filter(suggestion -> suggestion.toLowerCase().startsWith(input.toLowerCase()))
							.collect(Collectors.toList());

					addSuggestionsToList();
				}
			}
		});

		suggestionListUI.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && suggestionListUI.getSelectedIndex() != -1) {
				String selectedValue = suggestionListUI.getSelectedValue();
				if (!"More results...".equals(selectedValue)) {
					suggestionTextField.setText(selectedValue);
				} else {
					SwingUtilities.invokeLater(this::addSuggestionsToList);
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		JButton confirmButton = new JButton("Confirm");
		JButton cancelButton = new JButton("Cancel");

		confirmButton.addActionListener(e -> {
			if (onCloseCallback != null) {
				onCloseCallback.accept(suggestionList.get(suggestionTextField.getText()));
			}
			dispose();
		});

		cancelButton.addActionListener(e -> dispose());

		buttonPanel.add(confirmButton);
		buttonPanel.add(cancelButton);

		add(suggestionTextField, BorderLayout.NORTH);
		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
			}
		});
	}

	private void addSuggestionsToList() {
		if (!listModel.isEmpty() && "More results...".equals(listModel.lastElement())) {
			listModel.removeElementAt(listModel.size() - 1);
		}

		int count = 0;
		for (int i = currentSuggestionIndex; i < currentFilteredSuggestions.size() && count < MAX_SUGGESTIONS; i++, count++) {
			listModel.addElement(currentFilteredSuggestions.get(i));
		}

		currentSuggestionIndex += count;

		if (currentSuggestionIndex < currentFilteredSuggestions.size()) {
			listModel.addElement("More results...");
		}
	}

	public void showWindow() {
		SwingUtilities.invokeLater(() -> {
			setVisible(true);
			suggestionTextField.requestFocusInWindow();
		});
	}
}
