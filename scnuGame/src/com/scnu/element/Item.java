package com.scnu.element;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

/**
 * 道具类：敌人被击杀后掉落，玩家拾取可永久提升属性 01=移速, 02=攻速, 03=攻击力, 04=生命值 移速/攻速：累计拾取5次才提升1级
 */
public class Item extends ElementObj {

	public enum ItemType {
		SPEED, ATTACK_SPEED, ATTACK, MAX_HP
	}

	private ItemType type;
	private static final Random RANDOM = new Random();

	private static final Map<ItemType, String> IMAGE_PATH = new HashMap<>();
	static {
		IMAGE_PATH.put(ItemType.SPEED, "image/tool/01.png");
		IMAGE_PATH.put(ItemType.ATTACK_SPEED, "image/tool/02.png");
		IMAGE_PATH.put(ItemType.ATTACK, "image/tool/03.png");
		IMAGE_PATH.put(ItemType.MAX_HP, "image/tool/04.png");
	}

	private static final Map<ItemType, String> NAME_MAP = new HashMap<>();
	static {
		NAME_MAP.put(ItemType.SPEED, "移速");
		NAME_MAP.put(ItemType.ATTACK_SPEED, "攻速");
		NAME_MAP.put(ItemType.ATTACK, "攻击");
		NAME_MAP.put(ItemType.MAX_HP, "生命");
	}

	@Override
	public ElementObj createElement(String str) {
		String[] s = str.split(",");
		setX(Integer.parseInt(s[0]));
		setY(Integer.parseInt(s[1]));
		type = ItemType.values()[RANDOM.nextInt(ItemType.values().length)];
		ImageIcon icon = new ImageIcon(IMAGE_PATH.get(type));
		setW(icon.getIconWidth());
		setH(icon.getIconHeight());
		setIcon(icon);
		return this;
	}

	@Override
	public void showElement(Graphics g) {
		g.drawImage(getIcon().getImage(), 
				getX(), getY(), 
				getW(), getH(), null);
	}

	@Override
	public void pkByOther(ElementObj other) {
		if (other instanceof Player) {
			applyEffect((Player) other);
			die();
		}
	}

	private void applyEffect(Player p) {
		switch (type) {
		case SPEED:
			p.addSpeedCharge();
			break;
		case ATTACK_SPEED:
			p.addAttackSpeedCharge();
			break;
		case ATTACK:
			p.setAttack(p.getAttack() + 1);
			break;
		case MAX_HP:
			p.setMaxHp(p.getMaxHp() + 1);
			p.setHp(p.getHp() + 1);
			break;
		}
	}

	@Override
	public void die() {
		setLive(false);
	}

	public ItemType getItemType() {
		return type;
	}

	public String getItemName() {
		return NAME_MAP.get(type);
	}
}
