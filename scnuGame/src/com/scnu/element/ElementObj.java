package com.scnu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.ImageIcon;

/**
 * @说明 所有元素的基类
 */
public abstract class ElementObj {
	private int x;
	private int y;
	private int w;
	private int h;
	private ImageIcon icon;

	private boolean live = true;// 生存状态true存活 false死亡
	// 当重新定义一个用于判断状态的变量时需要思考:1.初始值2.值的改变3.值的判定

	public ElementObj() {
	}

	/**
	 * 带参数的构造方法 可以由子类传输到父类
	 * 
	 * @param x    左上角
	 * @param y    右上角
	 * @param w    宽度
	 * @param h    高度
	 * @param icon 图片
	 */
	public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.icon = icon;
	}

	/**
	 * @说明:显示元素
	 * @param g 画笔 用于进行绘画
	 */
	public abstract void showElement(Graphics g);

	/**
	 * @说明:使用父类定义接受键盘事件的方法 只有需要实现键盘监听的子类重写这个方法
	 * @说明:方式2-使用接口的方式;需要在监听类中实现类型转换
	 * @param bool 点击的类型 按下true 松开false
	 * @param key  代表触发键盘的code值
	 * @拓展-可以分为两个方法对应按下和松开
	 */
	public void keyClick(boolean bl, int key) {
	}

	public ElementObj createElement(String str) {
		return null;
	}

	public void pkByOther(ElementObj other) {
	}

	/**
	 * 本方法返回元素的碰撞矩形对象
	 * 
	 * @return
	 */
	public Rectangle getRectangle() {
		return new Rectangle(x, y, w, h);
	}

	/**
	 * @说明:碰撞方法 返回true碰撞，false则不碰撞
	 */
	public boolean pk(ElementObj obj) {
		return this.getRectangle().intersects(obj.getRectangle());
	}

	/**
	 * @设计模式-模板模式:在模板模式中定义对象执行方法的先后顺序，由子类选择性重写方法 1.移动 换装 发射子弹
	 */
	public final void model(long gameTime) {
		// 先换装
		updateImage(gameTime);
		// 再移动
		move();
		// 再发射子弹
		add(gameTime);
	}

	protected void updateImage(long gameTime) {

	}

	/**
	 * @说明 移动方法:需要移动的子类实现该方法
	 */
	protected void move() {

	}

	protected void add(long gameTime) {

	}

	public void die() { // 死亡也是一个对象

	}

	/**
	 * VO类中必须有所有的get和set
	 * 
	 */
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

}
