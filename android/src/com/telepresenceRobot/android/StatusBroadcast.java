package com.telepresenceRobot.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class StatusBroadcast {
    private static final String LOG_TAG = Constants.getLogTag(StatusBroadcast.class);
    public static final String BROADCAST_NAME = "exceptionBroadcast";

    public static void sendException(Context source, Throwable e) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.EXCEPTION.toString());
        intent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnectionOpened(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.WEB_SOCKET_OPENED.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnectionClosed(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.WEB_SOCKET_CLOSED.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketDisconnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.WEB_SOCKET_DISCONNECT.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendWebSocketConnect(Context source, String address) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.WEB_SOCKET_CONNECT.toString());
        intent.putExtra("url", address);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendRobotDisconnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.ROBOT_DISCONNECT.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendRobotConnect(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.ROBOT_CONNECT.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendForegroundServiceStarted(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.FOREGROUND_SERVICE_STARTED.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendLog(Context source, String message) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.LOG.toString());
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static class Receiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MessageType type = MessageType.valueOf(intent.getStringExtra("type"));
            switch (type) {
                case EXCEPTION:
                    Throwable e = (Throwable) intent.getSerializableExtra("exception");
                    onException(context, intent, e);
                    break;
                case WEB_SOCKET_OPENED:
                    onWebSocketOpened(context, intent);
                    break;
                case WEB_SOCKET_CLOSED:
                    onWebSocketClosed(context, intent);
                    break;
                case WEB_SOCKET_CONNECT:
                    String address = intent.getStringExtra("url");
                    onWebSocketConnect(context, intent, address);
                    break;
                case WEB_SOCKET_DISCONNECT:
                    onWebSocketDisconnect(context, intent);
                    break;
                case ROBOT_CONNECT:
                    onRobotConnect(context, intent);
                    break;
                case ROBOT_DISCONNECT:
                    onRobotDisconnect(context, intent);
                    break;
                case FOREGROUND_SERVICE_STARTED:
                    onForegroundServiceStarted(context, intent);
                    break;
                case LOG:
                    String message = intent.getStringExtra("message");
                    onLog(context, intent, message);
                    break;
                default:
                    Log.e(LOG_TAG, "Invalid status type: " + type);
                    break;
            }
        }

        protected void onLog(Context context, Intent intent, String message) {

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

    private enum MessageType {
        EXCEPTION,
        WEB_SOCKET_OPENED,
        WEB_SOCKET_CLOSED,
        WEB_SOCKET_CONNECT,
        WEB_SOCKET_DISCONNECT,
        ROBOT_CONNECT,
        ROBOT_DISCONNECT,
        LOG,
        FOREGROUND_SERVICE_STARTED
    }
}
