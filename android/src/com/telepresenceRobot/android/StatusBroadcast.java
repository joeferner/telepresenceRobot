package com.telepresenceRobot.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class StatusBroadcast {
    public static final String BROADCAST_NAME = "exceptionBroadcast";
    private static final String TYPE_EXCEPTION = "exception";
    private static final String TYPE_WEB_SOCKET_OPENED = "webSocketOpened";
    private static final String TYPE_WEB_SOCKET_CLOSED = "webSocketClosed";
    private static final String TYPE_WEB_SOCKET_CONNECT = "webSocketConnect";
    private static final String TYPE_WEB_SOCKET_DISCONNECT = "webSocketDisconnect";
    private static final String TYPE_ROBOT_CONNECT = "robotConnect";
    private static final String TYPE_ROBOT_DISCONNECT = "robotDisconnect";
    private static final String TYPE_FOREGROUND_SERVICE_STARTED = "foregroundServiceStarted";

    public static void sendException(Context source, Throwable e) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_EXCEPTION);
        intent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnectionOpened(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_WEB_SOCKET_OPENED);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnectionClosed(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_WEB_SOCKET_CLOSED);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketDisconnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_WEB_SOCKET_DISCONNECT);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnect(Context source, String address) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_WEB_SOCKET_CONNECT);
        intent.putExtra("url", address);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendRobotDisconnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_ROBOT_DISCONNECT);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendRobotConnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_ROBOT_CONNECT);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendForegroundServiceStarted(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_FOREGROUND_SERVICE_STARTED);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static class Receiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if (type.equals(TYPE_EXCEPTION)) {
                Throwable e = (Throwable) intent.getSerializableExtra("exception");
                onException(context, intent, e);
            } else if (type.equals(TYPE_WEB_SOCKET_OPENED)) {
                onWebSocketOpened(context, intent);
            } else if (type.equals(TYPE_WEB_SOCKET_CLOSED)) {
                onWebSocketClosed(context, intent);
            } else if (type.equals(TYPE_WEB_SOCKET_CONNECT)) {
                String address = intent.getStringExtra("url");
                onWebSocketConnect(context, intent, address);
            } else if (type.equals(TYPE_WEB_SOCKET_DISCONNECT)) {
                onWebSocketDisconnect(context, intent);
            } else if (type.equals(TYPE_ROBOT_CONNECT)) {
                onRobotConnect(context, intent);
            } else if (type.equals(TYPE_ROBOT_DISCONNECT)) {
                onRobotDisconnect(context, intent);
            } else if (type.equals(TYPE_FOREGROUND_SERVICE_STARTED)) {
                onForegroundServiceStarted(context, intent);
            } else {
                Log.e(Constants.LOG, "Invalid status type: " + type);
            }
        }

        protected void onForegroundServiceStarted(Context context, Intent intent) {

        }

        protected void onRobotDisconnect(Context context, Intent intent) {

        }

        protected void onRobotConnect(Context context, Intent intent) {

        }

        protected void onWebSocketDisconnect(Context context, Intent intent) {

        }

        protected void onWebSocketConnect(Context context, Intent intent, String address) {

        }

        protected void onWebSocketClosed(Context context, Intent intent) {

        }

        protected void onWebSocketOpened(Context context, Intent intent) {

        }

        protected void onException(Context context, Intent intent, Throwable e) {

        }
    }
}
