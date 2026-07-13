package com.scnu.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scnu.element.ElementObj;

/**
 * 元素管理器，单例模式，存储所有游戏元素
 */
public class ElementManager {

	private Map<GameElement, List<ElementObj>> gameElements;

	public Map<GameElement, List<ElementObj>> getGameElements() {
		return gameElements;
	}

	public void addElement(ElementObj obj, GameElement ge) {
		List<ElementObj> list = gameElements.get(ge);
		synchronized (list) {
			list.add(obj);
		}
	}

	public List<ElementObj> getElementByKey(GameElement ge) {
		return gameElements.get(ge);
	}

	private static ElementManager EM = null;

	public static synchronized ElementManager getManager() {
		if (EM == null) {
			EM = new ElementManager();
		}
		return EM;
	}

	private ElementManager() {
		init();
	}

	public void init() {
		gameElements = new HashMap<GameElement, List<ElementObj>>();
		for (GameElement ge : GameElement.values()) {
			gameElements.put(ge, new ArrayList<ElementObj>());
		}
	}

	/**
	 * 重置所有元素（关卡重新开始时调用）
	 */
	public void reset() {
		gameElements.clear();
		init();
	}

	/**
	 * 重置单例实例
	 */
	public static synchronized void resetManager() {
		if (EM != null) {
			EM.reset();
		}
	}
}
