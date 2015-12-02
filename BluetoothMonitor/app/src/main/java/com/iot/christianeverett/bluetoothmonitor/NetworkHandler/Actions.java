package com.iot.christianeverett.bluetoothmonitor.NetworkHandler;

/**
 * Created by Christian Everett on 10/23/2015.
 * Corresponding action supported by server
 */
public interface Actions
{
    //Server side actions
    public final static int GET_STATUS = 1;
    public final static int GET_LOG = 2;
    public final static int DISCOVER = 3;
    public final static int START_SCAN = 4;
    public final static int STOP_SCAN = 5;
    public final static int IS_SCAN_ENABLE = 6;

    //Client side only action
    public final static int UPDATE_UI = 7;

    public final static String DEVICE_NAME = "device_name";
    public final static String MAC_ADDRESS = "MAC";

    public final static String ACTION = "action";
    public final static String DATE = "date";
    public final static String TIME = "time";
}
