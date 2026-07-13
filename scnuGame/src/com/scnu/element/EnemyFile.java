package com.scnu.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * 敌人子弹类（黄色）
 */
public class EnemyFile extends ElementObj {
	private int attack = 1;
	private int moveNum = 3;
	private String fx;

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		for (String s : split) {
			String[] kv = s.split(":");
			switch (kv[0]) {
			case "x":
				this.setX(Integer.parseInt(kv[1]));
				break;
			case "y":
				this.setY(Integer.parseInt(kv[1]));
				break;
			case "f":
				this.fx = kv[1];
				break;
			case "a":
				this.attack = Integer.parseInt(kv[1]);
				break;
			}
		}
		changeLocate();
		this.setW(10);
		this.setH(10);
		return this;
	}

	@Override
	public void showElement(Graphics g) {
		g.setColor(Color.yellow);
		g.fillOval(this.getX(), this.getY(), this.getW(), this.getH());
	}

	@Override
	protected void move() {
		if (this.getX() < 0 || this.getY() < 0 || this.getX() > 900 || this.getY() > 600) {
			this.setLive(false);
			return;
		}
		switch (this.fx) {
		case "up":
			this.setY(this.getY() - moveNum);
			break;
		case "down":
			this.setY(this.getY() + moveNum);
			break;
		case "right":
			this.setX(this.getX() + moveNum);
			break;
		case "left":
			this.setX(this.getX() - moveNum);
			break;
		}
	}

	@Override
	public void pkByOther(ElementObj other) {
		if (other instanceof MapObj) {
			String type = ((MapObj) other).getType();
			if ("river".equals(type) || "grass".equals(type)) {
				return;
			}
		}
		this.die();
	}

	@Override
	public void die() {
		this.setLive(false);
	}

	public int getAttack() {
		return attack;
	}

	private void changeLocate() {
		switch (this.fx) {
		case "up":
			this.setX(this.getX() + 13);
			break;
		case "down":
			this.setX(this.getX() + 13);
			this.setY(this.getY() + 40);
			break;
		case "right":
			this.setX(this.getX() + 40);
			this.setY(this.getY() + 13);
			break;
		case "left":
			this.setY(this.getY() + 13);
			break;
		}
	}
}
