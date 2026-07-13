package com.scnu.element;

import java.awt.Graphics;
import java.sql.Driver;
import java.time.Year;

import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
	private String type;
	private int hp;

	@Override
	public void showElement(Graphics g) {
		// TODO 自动生成的方法存根
		g.drawImage(this.getIcon().getImage(), getX(), getY(), getW(), getH(), null);
//		System.out.println("打印墙壁");
	}

	@Override
	public void die() {
		// TODO 自动生成的方法存根
		this.setLive(false);
	}

	@Override // 传入 墙类型,x,y
	public ElementObj createElement(String str) {
		// TODO 自动生成的方法存根
		String[] arr = str.split(",");
//		for (String g : arr) {
//			System.out.println(g);
//		}
		ImageIcon icon = null;
		switch (arr[0]) {
		case "GRASS":
			icon = new ImageIcon("image/wall/grass.png");
			this.type = "grass";
			this.hp = -1;
			break;
		case "BRICK":
			icon = new ImageIcon("image/wall/brick.png");
			this.type = "brick";
			this.hp = 1;
			break;
		case "RIVER":
			icon = new ImageIcon("image/wall/river.png");
			this.type = "river";
			this.hp = -1;
			break;
		case "IRON":
			icon = new ImageIcon("image/wall/iron.png");
			this.type = "iron";
			this.hp = 4;
			break;
		}
		int x = Integer.parseInt(arr[1]);
		int y = Integer.parseInt(arr[2]);
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();

		this.setX(x);
		this.setY(y);
		this.setW(w);
		this.setH(h);
		this.setIcon(icon);

		return this;
	}

	@Override
	public void pkByOther(ElementObj other) {
		if (other instanceof PlayFile) {
			if (this.type == "river" || this.type == "grass") {
				return;
			} else {
				this.hp--;
				if (this.hp == 0) {
					this.die();
				}
			}
		}
	}

	/**
	 * 判断该地图元素是否可通行 只有草地(grass)可通行，砖墙/铁墙/河流不可通行
	 */
	public boolean isPassable() {
		return "grass".equals(this.type);
	}

	public String getType() {
		return type;
	}

}
