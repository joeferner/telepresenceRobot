package com.telepresenceRobot.android;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.RobotService;

public class ForegroundService extends IntentService {
    private static final String LOG_TAG = Constants.getLogTag(ForegroundService.class);
    private static final int FOREGROUND_ID = 1111;
    private boolean webSocketConnected;
    private boolean robotConnected;

    public ForegroundService() {
        super("telepresenceForeground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "starting foreground service");

        updateNotification();

        LocalBroadcastManager.getInstance(this).registerReceiver(statusBroadcastReceiver, new IntentFilter(StatusBroadcast.BROADCAST_NAME));
        LocalBroadcastManager.getInstance(this).registerReceiver(robotBroadcastReceiver, new IntentFilter(RobotBroadcast.BROADCAST_NAME));

        StatusBroadcast.sendForegroundServiceStarted(this);

        return START_STICKY;
    }

    private String getNotificationContentText() {
        return getString(R.string.foreground_content_text, webSocketConnected ? "Connected" : "Disconnected", robotConnected ? "Connected" : "Disconnected");
    }

    private void updateNotification() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        Notification notification = new Notification.Builder(this)
                .setTicker(getString(R.string.foreground_ticker))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentTitle(getString(R.string.foreground_content_title))
                .setContentText(getNotificationContentText())
                .setOngoing(true)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .getNotification();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        stopForeground(true);
        RobotService.stopService(this);
        TelepresenceServerClientService.stopService(this);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    public static void startService(Context context) {
        stopService(context);
        Log.i(LOG_TAG, "startService");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.startService(serviceIntent);
    }

    public static void stopService(Context context) {
        Log.i(LOG_TAG, "Stopping service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
    }

    private BroadcastReceiver statusBroadcastReceiver = new StatusBroadcast.Receiver() {
        @Override
        protected void onRobotDisconnect(Context context, Intent intent) {
            super.onRobotDisconnect(context, intent);
            RobotService.stopService(ForegroundService.this);
        }

        @Override
        protected void onRobotConnect(Context context, Intent intent) {
            super.onRobotConnect(context, intent);
            RobotService.startService(ForegroundService.this);
        }

        @Override
        protected void onWebSocketDisconnect(Context context, Intent intent) {
            super.onWebSocketDisconnect(context, intent);
            TelepresenceServerClientService.stopService(ForegroundService.this);
        }

        @Override
        protected void onWebSocketConnect(Context context, Intent intent, String hostname, int port) {
            super.onWebSocketConnect(context, intent, hostname, port);
            TelepresenceServerClientService.startService(ForegroundService.this, hostname, port);
        }

        @Override
        protected void onWebSocketClosed(Context context, Intent intent) {
            super.onWebSocketClosed(context, intent);
            webSocketConnected = false;
            updateNotification();
        }

        @Override
        protected void onWebSocketOpened(Context context, Intent intent) {
            super.onWebSocketOpened(context, intent);
            webSocketConnected = true;
            updateNotification();
        }
    };

    private BroadcastReceiver robotBroadcastReceiver = new RobotBroadcast.Receiver() {
        @Override
        protected void onConnected(Context context, Intent intent) {
            super.onConnected(context, intent);
            robotConnected = true;
            updateNotification();
        }

        @Override
        protected void onConnectFailed(Context context, Intent intent, Throwable e) {
            super.onConnectFailed(context, intent, e);
            robotConnected = false;
            updateNotification();
        }

        @Override
        protected void onDisconnected(Context context, Intent intent) {
            super.onDisconnected(context, intent);
            robotConnected = false;
            updateNotification();
        }
    };
}
