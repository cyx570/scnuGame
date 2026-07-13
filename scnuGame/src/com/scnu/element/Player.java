package com.scnu.element;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.ImageIcon;

import com.scnu.manager.ElementManager;
import com.scnu.manager.GameElement;
import com.scnu.manager.GameLoad;

public class Player extends ElementObj {

	private boolean left, up, right, down;
	private String fx = "up";
	private boolean pkType = false;

	// 基础属性
	private int attack = 1;
	private int maxHp = 5;
	private int hp = 5;
	private int speed = 1; // 上限 3
	private long shootInterval = 20; // 下限 5

	// 累计5次升级
	private int speedCharge = 0;
	private int attackSpeedCharge = 0;
	private static final int CHARGE_MAX = 5;

	public Player() {
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		this.setX(Integer.parseInt(split[0]));
		this.setY(Integer.parseInt(split[1]));
		ImageIcon icon = GameLoad.imgMap.get(split[2]);
		this.setW(icon.getIconWidth());
		this.setH(icon.getIconHeight());
		this.setIcon(icon);
		return this;
	}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
	}

	@Override
	public void keyClick(boolean bl, int key) {
		if (bl) {
			switch (key) {
			case KeyEvent.VK_A:
				up = false;
				down = false;
				right = false;
				left = true;
				fx = "left";
				break;
			case KeyEvent.VK_W:
				up = true;
				down = false;
				right = false;
				left = false;
				fx = "up";
				break;
			case KeyEvent.VK_D:
				up = false;
				down = false;
				right = true;
				left = false;
				fx = "right";
				break;
			case KeyEvent.VK_S:
				up = false;
				down = true;
				right = false;
				left = false;
				fx = "down";
				break;
			case KeyEvent.VK_SPACE:
				pkType = true;
				break;
			}
		} else {
			switch (key) {
			case KeyEvent.VK_A:
				left = false;
				break;
			case KeyEvent.VK_W:
				up = false;
				break;
			case KeyEvent.VK_D:
				right = false;
				break;
			case KeyEvent.VK_S:
				down = false;
				break;
			case KeyEvent.VK_SPACE:
				pkType = false;
				break;
			}
		}
	}

	@Override
	protected void move() {
		List<ElementObj> maps = ElementManager.getManager().getElementByKey(GameElement.MAPS);
		int nx = this.getX(), ny = this.getY();
		int w = this.getW(), h = this.getH();

		if (left && nx > 0)
			nx -= speed;
		if (up && ny > 0)
			ny -= speed;
		if (right && nx < 800 - 2 * w)
			nx += speed;
		if (down && ny < 600 - 2 * h)
			ny += speed;

		if (nx != getX()) {
			Rectangle r = new Rectangle(nx, getY(), w, h);
			boolean ok = true;
			for (ElementObj m : maps)
				if (m instanceof MapObj && !((MapObj) m).isPassable() && r.intersects(m.getRectangle())) {
					ok = false;
					break;
				}
			if (ok)
				for (ElementObj e : ElementManager.getManager().getElementByKey(GameElement.ENEMY))
					if (e.isLive() && r.intersects(e.getRectangle())) {
						ok = false;
						break;
					}
			if (ok)
				for (ElementObj b : ElementManager.getManager().getElementByKey(GameElement.BOSS))
					if (b.isLive() && r.intersects(b.getRectangle())) {
						ok = false;
						break;
					}
			if (ok)
				setX(nx);
		}
		if (ny != getY()) {
			Rectangle r = new Rectangle(getX(), ny, w, h);
			boolean ok = true;
			for (ElementObj m : maps)
				if (m instanceof MapObj && !((MapObj) m).isPassable() && r.intersects(m.getRectangle())) {
					ok = false;
					break;
				}
			if (ok)
				for (ElementObj e : ElementManager.getManager().getElementByKey(GameElement.ENEMY))
					if (e.isLive() && r.intersects(e.getRectangle())) {
						ok = false;
						break;
					}
			if (ok)
				for (ElementObj b : ElementManager.getManager().getElementByKey(GameElement.BOSS))
					if (b.isLive() && r.intersects(b.getRectangle())) {
						ok = false;
						break;
					}
			if (ok)
				setY(ny);
		}
	}

	@Override
	protected void updateImage(long gameTime) {
		setIcon(GameLoad.imgMap.get(fx));
	}

	private long fileTime = 0;

	@Override
	protected void add(long gameTime) {
		if (!pkType)
			return;
		if (gameTime - fileTime <= shootInterval)
			return;
		fileTime = gameTime;
		ElementManager.getManager().addElement(new PlayFile().createElement(this.toString()), GameElement.PLAYFILE);
	}

	@Override
	public void pkByOther(ElementObj other) {
		if (other instanceof EnemyFile) {
			hp -= ((EnemyFile) other).getAttack();
			if (hp <= 0) {
				hp = 0;
				die();
			}
		}
	}

	@Override
	public void die() {
		setLive(false);
	}

	@Override
	public String toString() {
		return "x:" + getX() + ",y:" + getY() + ",f:" + fx + ",a:" + attack + ",s:" + speed;
	}

	// 属性提高

	public void addSpeedCharge() {
		speedCharge++;
		if (speedCharge >= CHARGE_MAX) {
			speedCharge = 0;
			if (speed < 3)
				speed++;
		}
	}

	public void addAttackSpeedCharge() {
		attackSpeedCharge++;
		if (attackSpeedCharge >= CHARGE_MAX) {
			attackSpeedCharge = 0;
			long iv = shootInterval - 3;
			shootInterval = Math.max(iv, 5);
		}
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int a) {
		this.attack = a;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(int m) {
		this.maxHp = m;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int h) {
		this.hp = h;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int s) {
		this.speed = Math.min(s, 3);
	}

	public long getShootInterval() {
		return shootInterval;
	}

	public void setShootInterval(long v) {
		this.shootInterval = Math.max(v, 5);
	}

	public int getSpeedCharge() {
		return speedCharge;
	}

	public void setSpeedCharge(int v) {
		this.speedCharge = v;
	}

	public int getAttackSpeedCharge() {
		return attackSpeedCharge;
	}

	public void setAttackSpeedCharge(int v) {
		this.attackSpeedCharge = v;
	}
}
