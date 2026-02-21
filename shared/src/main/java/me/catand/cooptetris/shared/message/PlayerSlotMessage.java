package me.catand.cooptetris.shared.message;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 玩家槽位消息 - 用于房间中玩家槽位的管理
 * 颜色由玩家选择，跟随玩家移动
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerSlotMessage extends NetworkMessage {
	public enum SlotAction {
		UPDATE_SLOTS,       // 服务器广播所有槽位状态
		REQUEST_SLOT,       // 客户端请求移动到指定槽位
		REQUEST_LOCK,       // 房主请求锁定/解锁槽位
		REQUEST_KICK,       // 房主请求踢出玩家
		REQUEST_COLOR,      // 客户端请求更改颜色
		COLOR_CHANGED,      // 服务器通知颜色变更
		SLOT_ASSIGNED,      // 服务器通知槽位分配结果
		LOCK_CHANGED,       // 服务器通知锁定状态变更
		REQUEST_SPECTATOR,  // 客户端请求成为观战者/退出观战
		REQUEST_SPECTATOR_LOCK, // 房主请求锁定/解锁观战功能
		SPECTATOR_CHANGED   // 服务器通知观战状态变更
	}

	private SlotAction action;
	private List<SlotInfo> slots;
	private int slotIndex;          // 操作的槽位索引
	private int colorIndex;         // 颜色索引（0-3）
	private boolean locked;         // 锁定状态
	private String targetPlayerId;  // 目标玩家ID（踢出用）
	private boolean success;
	private String message;

	// 当前客户端的槽位索引（服务器告诉客户端自己在哪个位置）
	private int mySlotIndex;
	private int myColorIndex;       // 当前客户端的颜色索引
	private boolean isHost;

	// 观战者相关
	private boolean spectatorLocked;    // 观战是否被锁定（禁用）
	private int spectatorCount;         // 当前观战人数
	private boolean isSpectator;        // 当前客户端是否是观战者

	public PlayerSlotMessage() {
		super("playerSlot");
	}

	public PlayerSlotMessage(SlotAction action) {
		super("playerSlot");
		this.action = action;
	}

	/**
	 * 槽位信息
	 * 颜色由玩家选择，存储在玩家身上
	 */
	@Data
	public static class SlotInfo {
		private int slotIndex;      // 槽位索引 (0-3)
		private String playerName;  // 玩家名称（空表示无人）
		private String playerId;    // 玩家ID
		private int colorIndex;     // 玩家选择的颜色索引（0-3，-1表示未选择）
		private boolean locked;     // 是否锁定
		private boolean enabled;    // 是否启用

		public SlotInfo() {}

		public SlotInfo(int slotIndex, String playerName, String playerId, int colorIndex, boolean locked, boolean enabled) {
			this.slotIndex = slotIndex;
			this.playerName = playerName;
			this.playerId = playerId;
			this.colorIndex = colorIndex;
			this.locked = locked;
			this.enabled = enabled;
		}
	}
}
