package com.scnu.show;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.scnu.controller.GameListener;
import com.scnu.controller.GameThread;
import com.scnu.manager.ElementManager;

/**
 * 游戏主窗体：CardLayout 切换面板
 */
public class GameJFrame extends JFrame {

	public static final int GameX = 800;
	public static final int GameY = 600;

	private CardLayout cardLayout;
	private JPanel container;

	private GameThread gameThread;
	private Thread renderThread;
	private GameMainJPanel gamePanel;

	private int currentSaveSlot = 0;
	private int gameSeq = 0; // 防止过期回调

	public GameJFrame() {
		init();
	}

	private void init() {
		this.setSize(GameX, GameY);
		this.setTitle("坦克大战");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		cardLayout = new CardLayout();
		container = new JPanel(cardLayout);
		this.add(container);
	}

	public void start() {
		switchToMainMenu();
		this.setVisible(true);
	}

	// 面板切换

	public void switchToMainMenu() {
		stopGame();
		MainMenuJPanel p = new MainMenuJPanel(this);
		addPanel("menu", p);
		cardLayout.show(container, "menu");
	}

	public void switchToLevelSelect() {
		stopGame();
		LevelSelectJPanel p = new LevelSelectJPanel(this, currentSaveSlot);
		addPanel("levelSelect", p);
		cardLayout.show(container, "levelSelect");
	}

	public void switchToSaveSelect() {
		stopGame();
		SaveSelectJPanel p = new SaveSelectJPanel(this);
		addPanel("saveSelect", p);
		cardLayout.show(container, "saveSelect");
	}

	public void switchToGame(int level, int saveSlot) {
		stopGame();
		ElementManager.resetManager();

		final int seq = ++gameSeq;

		GameThread newThread = new GameThread(level, saveSlot, (cleared, lv, slot) -> {
			SwingUtilities.invokeLater(() -> {
				if (seq != gameSeq) return;
				stopGame();
				switchToLevelSelect();
			});
		});

		gamePanel = new GameMainJPanel(this, newThread);
		GameListener listener = new GameListener(newThread);
		gameThread = newThread;

		for (java.awt.event.KeyListener kl : this.getKeyListeners()) {
			this.removeKeyListener(kl);
		}
		this.addKeyListener(listener);

		addPanel("game", gamePanel);
		cardLayout.show(container, "game");
		this.requestFocus();

		gameThread.start();
		renderThread = new Thread(gamePanel);
		renderThread.start();
	}

	// 内部

	private void stopGame() {
		if (gameThread != null) {
			gameThread.stopGame();
			gameThread = null;
		}
		if (renderThread != null) {
			if (gamePanel != null) gamePanel.stopRendering();
			renderThread.interrupt();
			renderThread = null;
		}
		gamePanel = null;
	}

	private void addPanel(String name, JPanel panel) {
		for (Component c : container.getComponents()) {
			if (name.equals(c.getName())) {
				container.remove(c);
				break;
			}
		}
		panel.setName(name);
		container.add(panel, name);
	}

	public int getCurrentSaveSlot() { return currentSaveSlot; }
	public void setCurrentSaveSlot(int slot) { this.currentSaveSlot = slot; }
}