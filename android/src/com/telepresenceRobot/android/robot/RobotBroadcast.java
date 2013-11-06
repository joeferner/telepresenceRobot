package com.telepresenceRobot.android.robot;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.telepresenceRobot.android.Constants;
import com.telepresenceRobot.android.webSocket.WebSocketService;

public class RobotBroadcast {
    private static final String LOG_TAG = Constants.getLogTag(RobotService.class);
    public static final String BROADCAST_NAME = "robotBroadcast";

    public static void sendConnected(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.CONNECTED.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendDisconnected(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.DISCONNECTED.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendData(Context source, byte[] buffer) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.DATA.toString());
        intent.putExtra("buffer", buffer);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendSetSpeed(Context source, Speed speed) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.SET_SPEED.toString());
        intent.putExtra("leftSpeed", speed.getLeftSpeed());
        intent.putExtra("rightSpeed", speed.getRightSpeed());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendSetTile(WebSocketService source, double tilt) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.SET_TILT.toString());
        intent.putExtra("tile", tilt);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendResume(Context source) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.RESUME.toString());
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendConnectFailed(Context source, Throwable e) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.CONNECT_FAILED.toString());
        intent.putExtra("error", e);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static void sendBatteryVoltage(Context source, int batteryVoltage) {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra("type", MessageType.BATTERY_VOLTAGE.toString());
        intent.putExtra("batteryVoltage", batteryVoltage);
        LocalBroadcastManager.getInstance(source).sendBroadcast(intent);
    }

    public static class Receiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MessageType type = MessageType.valueOf(intent.getStringExtra("type"));
            switch (type) {
                case CONNECTED:
                    onConnected(context, intent);
                    break;
                case DISCONNECTED:
                    onDisconnected(context, intent);
                    break;
                case RESUME:
                    onResume(context, intent);
                    break;
                case DATA:
                    byte[] buffer = intent.getByteArrayExtra("buffer");
                    onData(context, intent, buffer);
                    break;
                case BATTERY_VOLTAGE:
                    int batteryVoltage = intent.getIntExtra("batteryVoltage", 0);
                    onBatteryVoltage(context, intent, batteryVoltage);
                    break;
                case SET_SPEED:
                    double leftSpeed = intent.getDoubleExtra("leftSpeed", 0.0);
                    double rightSpeed = intent.getDoubleExtra("rightSpeed", 0.0);
                    onSetSpeed(context, intent, new Speed(leftSpeed, rightSpeed));
                    break;
                case SET_TILT:
                    double tilt = intent.getDoubleExtra("tilt", 0.5);
                    onSetTilt(context, intent, tilt);
                    break;
                case CONNECT_FAILED:
                    Throwable e = (Throwable) intent.getSerializableExtra("error");
                    onConnectFailed(context, intent, e);
                    break;
                default:
                    Log.e(LOG_TAG, "Invalid status type: " + type);
                    break;
            }
        }

        protected void onSetTilt(Context context, Intent intent, double tilt) {

        }

        protected void onBatteryVoltage(Context context, Intent intent, int batteryVoltage) {

        }

        protected void onConnectFailed(Context context, Intent intent, Throwable e) {

        }

        protected void onSetSpeed(Context context, Intent intent, Speed speed) {

        }

        protected void onData(Context context, Intent intent, byte[] buffer) {

        }

        protected void onConnected(Context context, Intent intent) {

        }

        protected void onDisconnected(Context context, Intent intent) {

        }

        protected void onResume(Context context, Intent intent) {

        }
    }

    private enum MessageType {
        CONNECTED,
        DISCONNECTED,
        DATA,
        RESUME,
        CONNECT_FAILED,
        BATTERY_VOLTAGE,
        SET_SPEED,
        SET_TILT
    }
}
