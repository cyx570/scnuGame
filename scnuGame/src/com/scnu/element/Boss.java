package com.scnu.element;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Boss敌人 — 使用 player2 图片，在第5关和第10关刷新
 */
public class Boss extends Enemy {

	private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();
	static {
		ICON_CACHE.put("up",    new ImageIcon("image/tank/play2/player2_up.png"));
		ICON_CACHE.put("down",  new ImageIcon("image/tank/play2/player2_down.png"));
		ICON_CACHE.put("left",  new ImageIcon("image/tank/play2/player2_left.png"));
		ICON_CACHE.put("right", new ImageIcon("image/tank/play2/player2_right.png"));
	}

	@Override
	public ElementObj createElement(String str) {
//		super.createElement(str);
		setIcon(ICON_CACHE.get("up"));
		setW(ICON_CACHE.get("up").getIconWidth());
		setH(ICON_CACHE.get("up").getIconHeight());
		// Boss 血量 = 3 倍普通敌人
		String[] s = str.split(",");
		int level = (s.length > 2) ? Integer.parseInt(s[2]) : 1;
		maxHp = 6 * level;
		hp = maxHp;
		return this;
	}

	@Override
	protected void updateImage(long gameTime) {
		setIcon(ICON_CACHE.get(fx));
	}
}