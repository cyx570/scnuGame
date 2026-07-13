package com.scnu.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.scnu.element.ElementObj;
import com.scnu.element.MapObj;

/**
 * @说明-加载器(工具类 用于读取配置文件)
 * 		大多提供static方法
 */
public class GameLoad {
	private static ElementManager em = ElementManager.getManager();
	public static Map<String, ImageIcon> imgMap = new HashMap<String, ImageIcon>();

	public static Properties pro = new Properties();

	/**
	 * 获取资源输入流：优先 classpath，其次文件系统
	 */
	private static InputStream getResourceStream(String classpathPath) {
		ClassLoader cl = GameLoad.class.getClassLoader();
		InputStream is = (cl != null) ? cl.getResourceAsStream(classpathPath) : null;
		if (is == null) {
			// 尝试文件系统路径（从项目根目录）
			try {
				is = new FileInputStream(classpathPath);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return is;
	}

	/**
	 * @说明 传入地图ID由加载方法依据文件规则自动产生地图文件名称
	 * @param mapId
	 */
	public static void MapLoad(int mapId) {
		String mapName = "com/scnu/text/" + mapId + ".map";
		InputStream maps = getResourceStream(mapName);
		if (maps == null) {
			System.out.println("配置文件读取异常: " + mapName);
			return;
		}

		try {
			pro.clear();
			pro.load(maps);
			Enumeration<?> names = pro.propertyNames();
			while (names.hasMoreElements()) {
				String key = names.nextElement().toString();
				String[] arrs = pro.getProperty(key).split(";");
				for (int i = 0; i < arrs.length; i++) {
					ElementObj element = new MapObj().createElement(key + "," + arrs[i]);
					em.addElement(element, GameElement.MAPS);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @说明-加载图片方法
	 */
	public static void loadImg() {
		String texturl = "com/scnu/text/GameData.pro";
		InputStream texts = getResourceStream(texturl);
		if (texts == null) {
			System.out.println("图片配置文件读取异常: " + texturl);
			return;
		}

		try {
			pro.clear();
			pro.load(texts);
			Set<Object> set = pro.keySet();
			for (Object object : set) {
				String url = pro.getProperty(object.toString());
				imgMap.put(object.toString(), new ImageIcon(url));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载敌人
	 */
	public static void loadEnemy() {
		int[][] spawns = { { 100, 30 }, { 400, 30 }, { 700, 30 } };
		for (int[] spawn : spawns) {
			ElementObj obj = getObj("enemy");
			ElementObj enemy = obj.createElement(spawn[0] + "," + spawn[1]);
			em.addElement(enemy, GameElement.ENEMY);
		}
	}

	/**
	 * 加载玩家
	 */
	public static void loadPlayer() {
		loadObj();
		String playString = "500,500,up";
		ElementObj obj = getObj("player");
		ElementObj play = obj.createElement(playString);
		em.addElement(play, GameElement.PLAYER);
	}

	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			Object newInstance = class1.newInstance();
			if (newInstance instanceof ElementObj) {
				return (ElementObj) newInstance;
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map<String, Class<?>> objMap = new HashMap<String, Class<?>>();

	public static void loadObj() {
		String texturl = "com/scnu/text/obj.pro";
		InputStream texts = getResourceStream(texturl);
		if (texts == null) {
			System.out.println("对象配置文件读取异常: " + texturl);
			return;
		}

		try {
			pro.clear();
			pro.load(texts);
			Set<Object> set = pro.keySet();
			for (Object object : set) {
				String classUrl = pro.getProperty(object.toString());
				Class<?> forName = Class.forName(classUrl);
				objMap.put(object.toString(), forName);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
