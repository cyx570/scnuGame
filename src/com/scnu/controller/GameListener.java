package com.scnu.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.scnu.element.ElementObj;
import com.scnu.manager.ElementManager;
import com.scnu.manager.GameElement;

public class GameListener implements KeyListener {
	private ElementManager em = ElementManager.getManager();
	private Set<Integer> set = new HashSet<Integer>();
	private GameThread gameThread;

	public GameListener(GameThread gameThread) {
		this.gameThread = gameThread;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == 27) {
			if (gameThread != null && !gameThread.isVictory()) {
				gameThread.togglePause();
			}
			return;
		}

		if (set.contains(key)) {
			return;
		} else {
			set.add(key);
		}

		List<ElementObj> playerList = em.getElementByKey(GameElement.PLAYER);
		for (ElementObj obj : playerList) {
			obj.keyClick(true, e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == 27)
			return;

		if (set.contains(key)) {
			set.remove(key);
		} else {
			return;
		}

		List<ElementObj> playerList = em.getElementByKey(GameElement.PLAYER);
		for (ElementObj obj : playerList) {
			obj.keyClick(false, e.getKeyCode());
		}
	}

}