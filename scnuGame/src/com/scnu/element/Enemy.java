package com.scnu.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import com.scnu.manager.ElementManager;
import com.scnu.manager.GameElement;

public class Enemy extends ElementObj {

	public static int totalKilled = 0;

	protected String fx = "up";
	private int moveNum = 1;
	private Random random = new Random();
	private int dirChangeTimer = 0;

	private int attack = 1;
	protected int hp = 2;
	protected int maxHp = 2;

	private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();
	static {
		ICON_CACHE.put("up", new ImageIcon("image/tank/bot/bot_up.png"));
		ICON_CACHE.put("down", new ImageIcon("image/tank/bot/bot_down.png"));
		ICON_CACHE.put("left", new ImageIcon("image/tank/bot/bot_left.png"));
		ICON_CACHE.put("right", new ImageIcon("image/tank/bot/bot_right.png"));
	}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
		// 头顶红色血量
		g.setColor(Color.RED);
		g.setFont(new Font("微软雅黑", Font.BOLD, 12));
		g.drawString(hp + "/" + maxHp, getX() + 5, getY() - 4);
	}

	@Override
	public ElementObj createElement(String str) {
		// 格式: "x,y" 或 "x,y,level"
		String[] s = str.split(",");
		setX(Integer.parseInt(s[0]));
		setY(Integer.parseInt(s[1]));

		// 根据关卡缩放属性
		int level = (s.length > 2) ? Integer.parseInt(s[2]) : 1;
		maxHp = 2 * level; 
		hp = maxHp;
		attack = 1 + level / 4; 

		ImageIcon icon = ICON_CACHE.get("up");
		setW(icon.getIconWidth());
		setH(icon.getIconHeight());
		setIcon(icon);
		this.fx = "up";
		this.dirChangeTimer = random.nextInt(100) + 50;
		return this;
	}

	@Override
	protected void updateImage(long gameTime) {
		setIcon(ICON_CACHE.get(fx));
	}

	@Override
	protected void move() {
		List<ElementObj> maps = ElementManager.getManager().getElementByKey(GameElement.MAPS);

		dirChangeTimer--;
		if (dirChangeTimer <= 0) {
			changeDirection();
			dirChangeTimer = random.nextInt(100) + 50;
		}

		int nx = getX(), ny = getY();
		switch (fx) {
		case "up":
			ny -= moveNum;
			break;
		case "down":
			ny += moveNum;
			break;
		case "left":
			nx -= moveNum;
			break;
		case "right":
			nx += moveNum;
			break;
		}

		if (nx < 0 || ny < 0 || nx > 800 - getW() || ny > 600 - getH()) {
			changeDirection();
			dirChangeTimer = random.nextInt(30) + 10;
			return;
		}

		Rectangle r = new Rectangle(nx, ny, getW(), getH());
		for (ElementObj m : maps) {
			if (m instanceof MapObj && !((MapObj) m).isPassable() && r.intersects(m.getRectangle())) {
				changeDirection();
				dirChangeTimer = random.nextInt(30) + 10;
				return;
			}
		}
		// 防止穿越玩家
		for (ElementObj p : ElementManager.getManager().getElementByKey(GameElement.PLAYER)) {
			if (p.isLive() && r.intersects(p.getRectangle())) {
				changeDirection();
				dirChangeTimer = random.nextInt(30) + 10;
				return;
			}
		}
		setX(nx);
		setY(ny);
	}

	private long fileTime = 0;
	private long fireInterval = 80;

	@Override
	protected void add(long gameTime) {
		if (gameTime - fileTime <= fireInterval)
			return;
		fileTime = gameTime;
		fireInterval = 60 + random.nextInt(60);
		ElementManager.getManager().addElement(new EnemyFile().createElement(toString()), GameElement.ENEMYFILE);
	}

	@Override
	public String toString() {
		return "x:" + getX() + ",y:" + getY() + ",f:" + fx + ",a:" + attack;
	}

	private void changeDirection() {
		String[] dirs = { "up", "down", "left", "right" };
		String n;
		do {
			n = dirs[random.nextInt(4)];
		} while (n.equals(fx));
		fx = n;
	}

	@Override
	public void pkByOther(ElementObj other) {
		if (other instanceof PlayFile) {
			hp -= ((PlayFile) other).getAttack();
			if (hp <= 0) {
				hp = 0;
				die();
			}
		}
	}

	@Override
	public void die() {
		if (isLive()) {
			totalKilled++;
			if (random.nextDouble() < 0.30) {
				ElementManager.getManager().addElement(new Item().createElement(getX() + "," + getY()),
						GameElement.ITEM);
			}
		}
		setLive(false);
	}

	public int getAttack() {
		return attack;
	}

	public int getHp() {
		return hp;
	}

	public int getMaxHp() {
		return maxHp;
	}
}
