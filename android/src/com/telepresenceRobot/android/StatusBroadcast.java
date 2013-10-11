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
            } else {
                Log.e(Constants.LOG, "Invalid status type: " + type);
            }
        }

        protected void onWebSocketClosed(Context context, Intent intent) {

        }

        protected void onWebSocketOpened(Context context, Intent intent) {

        }

        protected void onException(Context context, Intent intent, Throwable e) {

        }
    }
}
