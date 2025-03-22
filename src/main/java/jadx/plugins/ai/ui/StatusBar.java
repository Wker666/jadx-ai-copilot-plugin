package jadx.plugins.ai.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class StatusBar extends JPanel {

	private JLabel statusLabel;
	private Timer animationTimer;
	private TimerTask animationTask;
	private String waitingText = "Processing";
	private String[] dots = {"", ".", "..", "..."};
	private int dotIndex = 0;

	public StatusBar() {
		setLayout(new BorderLayout());
		statusLabel = new JLabel("Ready");
		add(statusLabel, BorderLayout.WEST);
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setPreferredSize(new Dimension(0, 20));
		setStatusBackgroundColor(Color.LIGHT_GRAY);
	}

	public void setStatusText(String text) {
		statusLabel.setText(text);
	}

	public void setStatusBackgroundColor(Color color) {
		setBackground(color);
		statusLabel.setBackground(color);
	}

	public void startWaiting() {
		stopWaiting();

		setStatusBackgroundColor(Color.YELLOW);

		animationTimer = new Timer();
		animationTask = new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(() -> {
					statusLabel.setText(waitingText + dots[dotIndex]);
					dotIndex = (dotIndex + 1) % dots.length;
				});
			}
		};

		animationTimer.schedule(animationTask, 0, 500);
	}

	public void stopWaiting() {
		if (animationTimer != null) {
			animationTimer.cancel();
			animationTimer = null;
		}
		setStatusText("Completed");
		setStatusBackgroundColor(Color.GREEN);
	}
}

