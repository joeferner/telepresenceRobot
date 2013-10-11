package com.telepresenceRobot.android.robot;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.telepresenceRobot.android.Constants;

public class RobotBroadcast {
    public static final String BROADCAST_NAME = "robotBroadcast";
    private static final String TYPE_CONNECTED = "connected";
    private static final String TYPE_DISCONNECTED = "disconnected";
    private static final String TYPE_DATA = "data";
    private static final String TYPE_SET_SPEED = "setSpeed";

    public static void sendConnected(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_CONNECTED);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendDisconnected(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_DISCONNECTED);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendData(Context source, byte[] buffer) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_DATA);
        intent.putExtra("buffer", buffer);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendSetSpeed(Context source, Speed speed) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", TYPE_SET_SPEED);
        intent.putExtra("leftSpeed", speed.getLeftSpeed());
        intent.putExtra("rightSpeed", speed.getRightSpeed());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static class Receiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if (type.equals(TYPE_CONNECTED)) {
                onConnected(context, intent);
            } else if (type.equals(TYPE_DISCONNECTED)) {
                onDisconnected(context, intent);
            } else if (type.equals(TYPE_DATA)) {
                byte[] buffer = intent.getByteArrayExtra("buffer");
                onData(context, intent, buffer);
            } else if (type.equals(TYPE_SET_SPEED)) {
                double leftSpeed = intent.getDoubleExtra("leftSpeed", 0.0);
                double rightSpeed = intent.getDoubleExtra("rightSpeed", 0.0);
                onSetSpeed(context, intent, new Speed(leftSpeed, rightSpeed));
            } else {
                Log.e(Constants.LOG, "Invalid status type: " + type);
            }
        }

        protected void onSetSpeed(Context context, Intent intent, Speed speed) {

        }

        protected void onData(Context context, Intent intent, byte[] buffer) {

        }

        protected void onConnected(Context context, Intent intent) {

        }

        protected void onDisconnected(Context context, Intent intent) {

        }
    }
}
