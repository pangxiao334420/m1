package com.goluk.a6.common.event;

/* public for event bus operation code */
public class EventConfig {

	/* EventBus opcode for MainActivity and CarRecorderActivity */
	public static final int CAR_RECORDER_BIND_CREATEAP = 500;
	public static final int CAR_RECORDER_RESULT = 600;
	// 删除wifi本地配置文件
	public static final int BIND_LIST_DELETE_CONFIG = 700;

	public final static int LIVE_MAP_QUERY = 99;
	public final static int WIFI_STATE = 3;
	public final static int LOCATION_FINISH = 1000;

	/* Event opcode for refresh user info view */
	public final static int REFRESH_USER_INFO = 100;

	/* Event opcode for IPC update ununited */
	public final static int UPDATE_IPC_UNUNITED = 18;

	/* Event opcode for IPC update file not exist */
	public static final int UPDATE_FILE_NOT_EXISTS = 10;
	/* Event opcode for IPC update prepare file */
	public static final int UPDATE_PREPARE_FILE = 11;
	/* Event opcode for IPC update transfer t1 file */
	public static final int UPDATE_TRANSFER_FILE_T1 = 12;
	/* Event opcode for IPC update transfer t1 file ok */
	public static final int UPDATE_TRANSFER_FILE_OK_T1 = 13;

	/** Wifi connect_anim failed */
	public final static int WIFI_STATE_FAILED = 0;
	/** Wifi connecting */
	public final static int WIFI_STATE_CONNING = 1;
	/** Wifi connect_anim successful */
	public final static int WIFI_STATE_SUCCESS = 2;

	/** Update address */
	public final static int CAR_RECORDER_UPDATE_ADDR = 118;

	public final static int PHOTO_ALBUM_UPDATE_DATE = -2;

	public final static int PHOTO_ALBUM_UPDATE_LOGIN_STATE = -1;

	/** IPC断开连接 */
	public static final int IPC_DISCONNECT = 0;
	/** IPC连接成功 */
	public static final int IPC_CONNECT = 1;

	public static final int IPC_ADAS_CONFIG_FROM_GUIDE = 1;

	public static final int IPC_ADAS_CONFIG_FROM_MODIFY = 0;
	public static final int BINDING = 0;
	/** 绑定成功的消息 */
	public static final int BIND_COMPLETE = 0;
	/** Update message */
	public final static int MESSAGE_UPDATE = 10001;
	/** Update request */
	public final static int MESSAGE_REQUEST = 10002;
	/** 告诉activity remove handler消息 **/
	public final static int PHOTO_ALBUM_REMOVE_HANLDER = 1003;
	/** 告诉activity delayed handler消息 **/
	public final static int PHOTO_ALBUM_DELAYED_HANLDER = 1004;

	/** User login result */
	public final static int USER_LOGIN_RET = 1005;

	/** 关注 推送消息 **/
	public final static int FOLLOW_PUSH = 1006;

	public final static int PRAISE_STATUS_CHANGE = 1007;
	/** 删除视频 */
	public static final int VIDEO_DELETE = 10008;
	/** 注册成功 */
	public static final int EVENT_REGISTER_CODE = 1000;

}
