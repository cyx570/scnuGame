package com.scnu.controller;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scnu.element.ElementObj;
import com.scnu.element.Enemy;
import com.scnu.element.MapObj;
import com.scnu.element.Player;
import com.scnu.manager.ElementManager;
import com.scnu.manager.GameElement;
import com.scnu.manager.GameLoad;
import com.scnu.manager.SaveManager;
import com.scnu.manager.SaveManager.SaveData;

/**
 * 游戏主线程 — 单次运行，结束后通过回调通知 GameJFrame
 */
public class GameThread extends Thread {

	public interface GameEndCallback {
		void onGameEnd(boolean cleared, int level, int saveSlot);
	}

	private ElementManager em;
	private int level;
	private int saveSlot;
	private GameEndCallback callback;
	private volatile boolean running = true;
	private volatile boolean paused = false;
	private volatile boolean victory = false;

	public boolean isPaused() {
		return paused;
	}

	public boolean isVictory() {
		return victory;
	}

	public void togglePause() {
		paused = !paused;
	}

	// 敌人复活系统
	private static final int[][] SPAWNS = { { 100, 5 }, { 400, 5 }, { 700, 5 } };
	private static final int MAX_KILLS = 20; // 击杀 20 通关
	private static final int RESPAWN_DELAY = 200; // 帧数（≈2秒）

	private int totalKills = 0;
	private long respawnTimer = 0;
	private int lastSpawnIndex = SPAWNS.length - 1; // 用于轮流出生

	public GameThread(int level, int saveSlot, GameEndCallback callback) {
		this.level = level;
		this.saveSlot = saveSlot;
		this.callback = callback;
		em = ElementManager.getManager();
	}

	@Override
	public void run() {
		Enemy.totalKilled = 0;
		gameLoad();
		gameRun();
		// 线程自然结束
	}

	public void stopGame() {
		this.running = false;
	}

	// 加载

	private void gameLoad() {
		// 图层加载顺序：图片→地图→玩家→敌人
		GameLoad.loadImg();
		GameLoad.MapLoad(level);
		GameLoad.loadPlayer();
		spawnAllEnemies();

		if (saveSlot > 0) {
			SaveData data = SaveManager.load(saveSlot);
			if (data != null) {
				List<ElementObj> players = em.getElementByKey(GameElement.PLAYER);
				if (!players.isEmpty() && players.get(0) instanceof Player) {
					SaveManager.applyToPlayer(data, (Player) players.get(0));
				}
			}
		}
	}

	/** 在三个固定复活点生成敌人，自动避开墙壁 */
	private void spawnAllEnemies() {
		for (int[] sp : SPAWNS) {
			int[] pos = findValidSpawn(sp[0], sp[1]);
			spawnOneEnemy(pos[0], pos[1]);
		}
		// 第5关和第10关刷新 Boss
		if (level == 5 || level == 10) {
			spawnBoss();
		}
	}

	private void spawnOneEnemy(int x, int y) {
		ElementObj obj = GameLoad.getObj("enemy");
		ElementObj enemy = obj.createElement(x + "," + y + "," + level);
		em.addElement(enemy, GameElement.ENEMY);
	}

	private void spawnBoss() {
		int[] pos = findValidSpawn(SPAWNS[1][0], SPAWNS[1][1]);
		ElementObj obj = GameLoad.getObj("boss");
		ElementObj boss = obj.createElement(pos[0] + "," + pos[1] + "," + level);
		em.addElement(boss, GameElement.BOSS);
	}

	/** 寻找有效生成位置（避开墙壁） */
	private int[] findValidSpawn(int preferX, int preferY) {
		List<ElementObj> maps = em.getElementByKey(GameElement.MAPS);
		// 尝试水平扫描
		for (int dx = 0; dx < 800; dx += 50) {
			for (int sign : new int[] { 1, -1 }) {
				int testX = preferX + dx * sign;
				if (testX < 0 || testX + 50 > 800)
					continue;
				Rectangle r = new Rectangle(testX, preferY, 50, 50);
				boolean blocked = false;
				for (ElementObj map : maps) {
					if (map instanceof MapObj && !((MapObj) map).isPassable()) {
						if (r.intersects(map.getRectangle())) {
							blocked = true;
							break;
						}
					}
				}
				if (!blocked)
					return new int[] { testX, preferY };
			}
		}
		return new int[] { preferX, preferY }; // 实在找不到就硬生成
	}

	// 运行

	private void gameRun() {
		long gameTime = 0L;
		respawnTimer = RESPAWN_DELAY;

		while (running) {
			if (paused || victory) {
				try {
					sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				continue;
			}

			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			List<ElementObj> enemyList = em.getElementByKey(GameElement.ENEMY);
			List<ElementObj> file = em.getElementByKey(GameElement.PLAYFILE);
			List<ElementObj> enemyFile = em.getElementByKey(GameElement.ENEMYFILE);
			List<ElementObj> maps = em.getElementByKey(GameElement.MAPS);
			List<ElementObj> player = em.getElementByKey(GameElement.PLAYER);
			List<ElementObj> items = em.getElementByKey(GameElement.ITEM);
			List<ElementObj> boss = em.getElementByKey(GameElement.BOSS);

			// 自动化移动
			moveUpdate(all, gameTime);

			// 碰撞检测
			ElementPK(enemyList, file);
			ElementPK(boss, file);
			ElementPK(file, maps);
			ElementPK(enemyFile, player);
			ElementPK(enemyFile, maps);
			ElementPK(items, player);

			// 读取累计击杀
			totalKills = Enemy.totalKilled;

			// 敌人复活
			if (totalKills < MAX_KILLS && enemyList.size() < SPAWNS.length) {
				respawnTimer--;
				if (respawnTimer <= 0) {
					respawnTimer = RESPAWN_DELAY;
					// 轮流从三个出生点重生
					boolean[] occupied = new boolean[SPAWNS.length];
					for (ElementObj e : enemyList) {
						for (int i = 0; i < SPAWNS.length; i++) {
							if (Math.abs(e.getX() - SPAWNS[i][0]) < 40 && Math.abs(e.getY() - SPAWNS[i][1]) < 40) {
								occupied[i] = true;
								break;
							}
						}
					}
					for (int attempt = 0; attempt < SPAWNS.length; attempt++) {
						lastSpawnIndex = (lastSpawnIndex + 1) % SPAWNS.length;
						if (!occupied[lastSpawnIndex]) {
							int[] pos = findValidSpawn(SPAWNS[lastSpawnIndex][0], SPAWNS[lastSpawnIndex][1]);
							spawnOneEnemy(pos[0], pos[1]);
							break;
						}
					}
				}
			}

			// 胜利条件
			if (totalKills >= MAX_KILLS && !victory) {
				onLevelClear();
				victory = true;
				continue;
			}

			// 玩家死亡
			if (player.isEmpty()) {
				notifyEnd(false);
				return;
			}

			gameTime++;
			try {
				sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void notifyEnd(boolean cleared) {
		if (callback != null) {
			callback.onGameEnd(cleared, level, saveSlot);
		}
	}

	// 通关

	private void onLevelClear() {
		if (saveSlot <= 0)
			return;
		SaveData data = SaveManager.load(saveSlot);
		List<Integer> cleared = (data != null) ? data.clearedLevels : new ArrayList<>();
		if (!cleared.contains(level)) {
			cleared.add(level);
		}
		List<ElementObj> players = em.getElementByKey(GameElement.PLAYER);
		if (!players.isEmpty() && players.get(0) instanceof Player) {
			SaveManager.save(saveSlot, (Player) players.get(0), cleared);
		}
	}

	// 碰撞

	private void ElementPK(List<ElementObj> listA, List<ElementObj> listB) {
		if (listA == null || listB == null)
			return;
		for (int i = 0; i < listA.size(); i++) {
			for (int j = 0; j < listB.size(); j++) {
				if (listA.get(i).pk(listB.get(j))) {
					listA.get(i).pkByOther(listB.get(j));
					listB.get(j).pkByOther(listA.get(i));
					break;
				}
			}
		}
	}

	public void moveUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			if (list == null)
				continue;
			synchronized (list) {
				for (int i = 0; i < list.size(); i++) {
					ElementObj obj = list.get(i);
					if (!obj.isLive()) {
						obj.die();
						list.remove(i--);
						continue;
					}
					obj.model(gameTime);
				}
			}
		}
	}

	public int getLevel() {
		return level;
	}

	public int getSaveSlot() {
		return saveSlot;
	}

	public int getTotalKills() {
		return totalKills;
	}

	public int getMaxKills() {
		return MAX_KILLS;
	}
}