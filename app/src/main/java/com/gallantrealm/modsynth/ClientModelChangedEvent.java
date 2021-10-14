package com.gallantrealm.modsynth;

/**
 * This event is fired on any change to the client model.
 */
public class ClientModelChangedEvent {

	public static int EVENT_TYPE_WWMODEL_UPDATED = 1;
	public static int EVENT_TYPE_NAVIGATOR_FIELD_UPDATED = 2;
	public static int EVENT_TYPE_MESSAGE_FIELD_UPDATED = 3;
	public static int EVENT_TYPE_MESSAGE_RECEIVED = 4;
	public static int EVENT_TYPE_CONNECTED = 5;
	public static int EVENT_TYPE_DISCONNECTED = 6;
	public static int EVENT_TYPE_RENDERING_UPDATED = 7;
	public static int EVENT_TYPE_FIELD_OF_VIEW_CHANGED = 8;
	public static int EVENT_TYPE_STYLE_CHANGED = 9;
	public static int EVENT_TYPE_REFRESH_RATE_CHANGED = 10;
	public static int EVENT_TYPE_ANTIALIAS_CHANGED = 11;
	public static int EVENT_TYPE_RENDERING_THRESHOLD_CHANGED = 12;
	public static int EVENT_TYPE_SELECTED_GAME_CHANGED = 13;
	public static int EVENT_TYPE_SELECTED_AVATAR_CHANGED = 14;
	public static int EVENT_TYPE_MESSAGE_FLASHED = 15;
	public static int EVENT_TYPE_AVATAR_ACTIONS_CHANGED = 16;
	public static int EVENT_TYPE_AVATAR_ACTIONS_RESTORED = 17;
	public static int EVENT_TYPE_WORLD_ACTIONS_CHANGED = 18;
	public static int EVENT_TYPE_WORLD_ACTIONS_RESTORED = 19;
	public static int EVENT_TYPE_USE_CONTROLLER_CHANGED = 20;
	public static int EVENT_TYPE_SELECTED_CATEGORY_CHANGED = 21;
	public static int EVENT_TYPE_CALIBRATE_SENSORS = 22;
	public static int EVENT_TYPE_FULLVERSION_CHANGED = 23;
	public static int EVENT_TYPE_OBJECT_SELECTED = 24;
	public static int EVENT_TYPE_POINT_SELECTED = 25;

	private final int eventType;

	public ClientModelChangedEvent(int eventType) {
		this.eventType = eventType;
	}

	public int getEventType() {
		return eventType;
	}

}
