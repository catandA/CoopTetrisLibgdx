package me.catand.cooptetris.shared.server;

import lombok.Data;
import me.catand.cooptetris.shared.tetris.CoopGameLogic;

/**
 * 玩家槽位 - 用于房间中的位置管理
 * 每个槽位包含玩家信息、位置和锁定状态
 * 颜色由玩家选择，跟随玩家移动
 */
@Data
public class PlayerSlot {
	// 槽位索引 (0-3)
	private int slotIndex;

	// 当前在此槽位的玩家（可能为空）
	private ClientConnection player;

	// 槽位是否被锁定（关闭）
	private boolean locked;

	// 槽位是否启用（用于计算房间显示人数）
	private boolean enabled;

	public PlayerSlot(int slotIndex) {
		this.slotIndex = slotIndex;
		this.player = null;
		this.locked = false;
		this.enabled = true;
	}

	/**
	 * 获取槽位中玩家的颜色索引
	 * 如果槽位为空，返回槽位索引作为默认颜色
	 */
	public int getColorIndex() {
		if (player != null && player.getColorIndex() >= 0) {
			return player.getColorIndex(); // 返回玩家选择的颜色
		}
		// 空槽位显示为未选择状态（-1）
		return -1;
	}

	/**
	 * 检查槽位是否为空（没有玩家）
	 */
	public boolean isEmpty() {
		return player == null;
	}

	/**
	 * 获取玩家名称（如果槽位为空返回空字符串）
	 */
	public String getPlayerName() {
		return player != null ? player.getPlayerName() : "";
	}

	/**
	 * 获取玩家ID（如果槽位为空返回空字符串）
	 */
	public String getPlayerId() {
		return player != null ? player.getClientId() : "";
	}

	/**
	 * 检查指定玩家是否在此槽位
	 */
	public boolean hasPlayer(String clientId) {
		return player != null && player.getClientId().equals(clientId);
	}

	/**
	 * 清除槽位中的玩家
	 */
	public void clearPlayer() {
		this.player = null;
	}

	/**
	 * 设置槽位中的玩家
	 */
	public void setPlayer(ClientConnection player) {
		this.player = player;
	}

	/**
	 * 切换锁定状态
	 */
	public void toggleLock() {
		this.locked = !this.locked;
		if (this.locked && this.player != null) {
			// 如果锁定时有玩家，需要踢出玩家
			this.player = null;
		}
	}

	/**
	 * 获取槽位的显示人数贡献（锁定或占用都算作1）
	 */
	public int getDisplayCount() {
		return (locked || player != null) ? 1 : 0;
	}
}
