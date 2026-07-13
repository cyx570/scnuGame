package com.scnu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.scnu.controller.GameThread;
import com.scnu.element.ElementObj;
import com.scnu.element.Player;
import com.scnu.manager.ElementManager;
import com.scnu.manager.GameElement;

public class GameMainJPanel extends JPanel implements Runnable {

	private ElementManager em;
	private volatile boolean running = true;
	private GameThread gameThread;
	private GameJFrame frame;

	// 暂停/胜利界面按钮
	private static final int BTN_W = 220;
	private static final int BTN_H = 50;
	private static final int BTN_X = (800 - BTN_W) / 2;
	private static final int BTN1_Y = 280;
	private static final int BTN2_Y = 350;

	private String[] pauseLabels = { "返回主菜单", "重新开始" };
	private String[] victoryLabels = { "返回主菜单", "下一关" };
	private int hoverPauseBtn = -1;
	private int hoverVictoryBtn = -1;

	public GameMainJPanel(GameJFrame frame, GameThread gameThread) {
		this.frame = frame;
		this.gameThread = gameThread;
		em = ElementManager.getManager();
		setupMouseListeners();
	}

	private void setupMouseListeners() {
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!gameThread.isPaused() && !gameThread.isVictory())
					return;
				int oldHover = hoverPauseBtn;
				hoverPauseBtn = -1;
				hoverVictoryBtn = -1;
				if (gameThread.isPaused()) {
					hoverPauseBtn = hitButton(e.getX(), e.getY(), pauseLabels.length);
				} else if (gameThread.isVictory()) {
					int max = gameThread.getLevel() >= 10 ? 1 : victoryLabels.length;
					hoverVictoryBtn = hitButton(e.getX(), e.getY(), max);
				}
				if (oldHover != (gameThread.isPaused() ? hoverPauseBtn : hoverVictoryBtn))
					repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (gameThread.isPaused()) {
					int idx = hitButton(e.getX(), e.getY(), pauseLabels.length);
					if (idx >= 0) {
						onPauseClick(idx);
					}
				} else if (gameThread.isVictory()) {
					int max = gameThread.getLevel() >= 10 ? 1 : victoryLabels.length;
					int idx = hitButton(e.getX(), e.getY(), max);
					if (idx >= 0) {
						onVictoryClick(idx);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (hoverPauseBtn != -1 || hoverVictoryBtn != -1) {
					hoverPauseBtn = -1;
					hoverVictoryBtn = -1;
					repaint();
				}
			}
		});
	}

	private int hitButton(int mx, int my, int btnCount) {
		for (int i = 0; i < btnCount; i++) {
			Rectangle r = new Rectangle(BTN_X, BTN1_Y + i * (BTN_H + 20), BTN_W, BTN_H);
			if (r.contains(mx, my))
				return i;
		}
		return -1;
	}

	private void onPauseClick(int idx) {
		switch (idx) {
		case 0:
			frame.switchToMainMenu();
			break;
		case 1:
			frame.switchToGame(gameThread.getLevel(), gameThread.getSaveSlot());
			break;
		}
	}

	private void onVictoryClick(int idx) {
		switch (idx) {
		case 0:
			frame.switchToMainMenu();
			break;
		case 1:
			if (gameThread.getLevel() < 10) {
				frame.switchToGame(gameThread.getLevel() + 1, gameThread.getSaveSlot());
			}
			break;
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			if (list == null || list.isEmpty())
				continue;
			List<ElementObj> snapshot;
			synchronized (list) {
				snapshot = new java.util.ArrayList<>(list);
			}
			for (ElementObj obj : snapshot)
				obj.showElement(g);
		}

		// 显示玩家属性
		List<ElementObj> pl = em.getElementByKey(GameElement.PLAYER);
		if (!pl.isEmpty() && pl.get(0) instanceof Player) {
			Player p = (Player) pl.get(0);
			g.setColor(Color.BLACK);
			g.setFont(new Font("微软雅黑", Font.BOLD, 14));
			g.drawString("生命: " + p.getHp() + "/" + p.getMaxHp(), 10, 20);
			g.drawString("攻击: " + p.getAttack(), 10, 38);
			String spd = p.getSpeed() >= 3 ? "MAX" : p.getSpeedCharge() + "/5";
			g.drawString("速度: " + p.getSpeed() + " (" + spd + ")", 10, 56);
			String aspd = p.getShootInterval() <= 5 ? "MAX" : p.getAttackSpeedCharge() + "/5";
			g.drawString("攻速: " + p.getShootInterval() + " (" + aspd + ")", 10, 74);
			g.drawString("击杀: " + gameThread.getTotalKills() + "/" + gameThread.getMaxKills(), 10, 92);
		}

		// ---- 暂停界面 ----
		if (gameThread.isPaused()) {
			drawOverlay(g, "游 戏 暂 停", pauseLabels, hoverPauseBtn);
		}

		// ---- 胜利界面 ----
		if (gameThread.isVictory()) {
			String title = "胜 利 !";
			String[] labels;
			int hover;
			if (gameThread.getLevel() >= 10) {
				labels = new String[] { "返回主菜单" };
				hover = hoverVictoryBtn >= 0 ? 0 : -1;
			} else {
				labels = victoryLabels;
				hover = hoverVictoryBtn;
			}
			drawOverlay(g, title, labels, hover);
		}
	}

	private void drawOverlay(Graphics g, String title, String[] labels, int hoverIdx) {
		// 半透明背景
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRect(0, 0, getWidth(), getHeight());

		// 标题
		g.setFont(new Font("微软雅黑", Font.BOLD, 48));
		g.setColor(Color.WHITE);
		FontMetrics fm = g.getFontMetrics();
		int tx = (getWidth() - fm.stringWidth(title)) / 2;
		g.drawString(title, tx, 200);

		// 按钮
		g.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		for (int i = 0; i < labels.length; i++) {
			int y = BTN1_Y + i * (BTN_H + 20);
			if (i == hoverIdx) {
				g.setColor(new Color(0xFF, 0xD7, 0x00));
				g.fillRoundRect(BTN_X, y, BTN_W, BTN_H, 12, 12);
				g.setColor(Color.BLACK);
			} else {
				g.setColor(new Color(0x55, 0x55, 0x55, 220));
				g.fillRoundRect(BTN_X, y, BTN_W, BTN_H, 12, 12);
				g.setColor(Color.WHITE);
			}
			FontMetrics fm2 = g.getFontMetrics();
			int sw = fm2.stringWidth(labels[i]);
			g.drawString(labels[i], BTN_X + (BTN_W - sw) / 2, y + BTN_H - 14);
		}
	}

	@Override
	public void run() {
		while (running) {
			repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void stopRendering() {
		this.running = false;
	}
}