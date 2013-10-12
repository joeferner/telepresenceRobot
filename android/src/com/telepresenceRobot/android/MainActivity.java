package com.telepresenceRobot.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.Speed;

public class MainActivity extends Activity {
    private StringBuilder logBuffer = new StringBuilder();
    private TextView log;
    private Button forward;
    private Button back;
    private Button left;
    private Button right;
    private ConnectState webSocketConnectState = ConnectState.DISCONNECTED;
    private ConnectState robotConnectState = ConnectState.DISCONNECTED;
    private MenuItem connectRobotMenuItem;
    private MenuItem connectWebSocketMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.main);

        log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        forward = (Button) findViewById(R.id.forward);
        back = (Button) findViewById(R.id.back);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);

        forward.setOnTouchListener(new MovementOnTouchListener(MovementDirection.FORWARD));
        back.setOnTouchListener(new MovementOnTouchListener(MovementDirection.BACK));
        left.setOnTouchListener(new MovementOnTouchListener(MovementDirection.LEFT));
        right.setOnTouchListener(new MovementOnTouchListener(MovementDirection.RIGHT));

        LocalBroadcastManager.getInstance(this).registerReceiver(robotBroadcastReceiver, new IntentFilter(RobotBroadcast.BROADCAST_NAME));
        LocalBroadcastManager.getInstance(this).registerReceiver(statusBroadcastReceiver, new IntentFilter(StatusBroadcast.BROADCAST_NAME));
        ForegroundService.startService(this);
    }

    private void robotConnect() {
        robotConnectState = ConnectState.CONNECTING;
        updateMenuItems();
        log("Connecting to robot");
        StatusBroadcast.sendRobotConnect(this);
    }

    private void webSocketConnect() {
        webSocketConnectState = ConnectState.CONNECTING;
        updateMenuItems();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String address = sharedPref.getString(SettingsActivity.WEB_SOCKET_URL, null);
        log("Connecting to " + address);
        StatusBroadcast.sendWebSocketConnect(this, address);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        connectRobotMenuItem = menu.findItem(R.id.robot_connect);
        connectWebSocketMenuItem = menu.findItem(R.id.web_service_connect);
        updateMenuItems();
        return true;
    }

    private void updateMenuItems() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.LOG, "webSocketConnectState: " + webSocketConnectState);
                if (connectWebSocketMenuItem != null) {
                    switch (webSocketConnectState) {
                        case CONNECTED:
                            connectWebSocketMenuItem.setEnabled(true);
                            connectWebSocketMenuItem.setTitle(getString(R.string.disconnect_web_socket));
                            break;
                        case CONNECTING:
                            connectWebSocketMenuItem.setEnabled(false);
                            connectWebSocketMenuItem.setTitle(getString(R.string.connecting_web_socket));
                            break;
                        case DISCONNECTED:
                            connectWebSocketMenuItem.setEnabled(true);
                            connectWebSocketMenuItem.setTitle(getString(R.string.connect_web_socket));
                            break;
                        case DISCONNECTING:
                            connectWebSocketMenuItem.setEnabled(false);
                            connectWebSocketMenuItem.setTitle(getString(R.string.disconnecting_web_socket));
                            break;
                    }
                }
                Log.i(Constants.LOG, "connectRobotMenuItem: " + robotConnectState);
                if (connectRobotMenuItem != null) {
                    switch (robotConnectState) {
                        case CONNECTED:
                            connectRobotMenuItem.setEnabled(true);
                            connectRobotMenuItem.setTitle(getString(R.string.disconnect_robot));
                            break;
                        case CONNECTING:
                            connectRobotMenuItem.setEnabled(false);
                            connectRobotMenuItem.setTitle(getString(R.string.connecting_robot));
                            break;
                        case DISCONNECTED:
                            connectRobotMenuItem.setEnabled(true);
                            connectRobotMenuItem.setTitle(getString(R.string.connect_robot));
                            break;
                        case DISCONNECTING:
                            connectRobotMenuItem.setEnabled(false);
                            connectRobotMenuItem.setTitle(getString(R.string.disconnecting_robot));
                            break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), 0);
                return true;
            case R.id.exit:
                System.exit(0);
                return true;
            case R.id.robot_connect:
                connectRobotMenuItem.setEnabled(false);
                if (robotConnectState == ConnectState.CONNECTED || robotConnectState == ConnectState.CONNECTING) {
                    robotConnectState = ConnectState.DISCONNECTING;
                    updateMenuItems();
                    StatusBroadcast.sendRobotDisconnect(MainActivity.this);
                } else {
                    robotConnect();
                }
                return true;
            case R.id.web_service_connect:
                connectWebSocketMenuItem.setEnabled(false);
                if (webSocketConnectState == ConnectState.CONNECTED || webSocketConnectState == ConnectState.CONNECTING) {
                    webSocketConnectState = ConnectState.DISCONNECTING;
                    updateMenuItems();
                    StatusBroadcast.sendWebSocketDisconnect(MainActivity.this);
                } else {
                    webSocketConnect();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private StatusBroadcast.Receiver statusBroadcastReceiver = new StatusBroadcast.Receiver() {
        @Override
        protected void onWebSocketOpened(Context context, Intent intent) {
            super.onWebSocketOpened(context, intent);
            webSocketConnectState = ConnectState.CONNECTED;
            updateMenuItems();
            log("Web socket opened");
        }

        @Override
        protected void onWebSocketClosed(Context context, Intent intent) {
            super.onWebSocketClosed(context, intent);
            webSocketConnectState = ConnectState.DISCONNECTED;
            updateMenuItems();
            log("Web socket disconnected");
        }

        @Override
        protected void onException(Context context, Intent intent, Throwable e) {
            super.onException(context, intent, e);
            log(e.getMessage());
        }

        @Override
        protected void onForegroundServiceStarted(Context context, Intent intent) {
            super.onForegroundServiceStarted(context, intent);
            robotConnect();
            webSocketConnect();
        }
    };

    private RobotBroadcast.Receiver robotBroadcastReceiver = new RobotBroadcast.Receiver() {
        @Override
        protected void onSetSpeed(Context context, Intent intent, Speed speed) {
            super.onSetSpeed(context, intent, speed);
            log("Setting speed " + speed.getLeftSpeed() + ", " + speed.getRightSpeed());
        }

        @Override
        protected void onData(Context context, Intent intent, byte[] buffer) {
            super.onData(context, intent, buffer);
            log("Read: " + new String(buffer));
        }

        @Override
        protected void onConnected(Context context, Intent intent) {
            super.onConnected(context, intent);
            log("robot connected");
            robotConnectState = ConnectState.CONNECTED;
            updateMenuItems();
        }

        @Override
        protected void onDisconnected(Context context, Intent intent) {
            super.onDisconnected(context, intent);
            log("robot disconnected");
            robotConnectState = ConnectState.DISCONNECTED;
            updateMenuItems();
        }
    };

    private void log(String line) {
        Log.i(Constants.LOG, "log: " + line);
        logBuffer.append(line);
        logBuffer.append("\n");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (log == null || log.getLayout() == null) {
                    return;
                }
                log.setText(logBuffer.toString());
                int scrollAmount = log.getLayout().getLineTop(log.getLineCount()) - log.getHeight();
                log.scrollTo(0, scrollAmount);
            }
        });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(robotBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusBroadcastReceiver);
        super.onDestroy();
    }

    private class MovementOnTouchListener implements View.OnTouchListener {
        private final MovementDirection movementDirection;

        public MovementOnTouchListener(MovementDirection movementDirection) {
            this.movementDirection = movementDirection;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            double speedLeft = 0.0;
            double speedRight = 0.0;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (movementDirection) {
                        case FORWARD:
                            speedLeft = speedRight = 1.0;
                            break;
                        case BACK:
                            speedLeft = speedRight = -1.0;
                            break;
                        case LEFT:
                            speedLeft = -1.0;
                            speedRight = 1.0;
                            break;
                        case RIGHT:
                            speedLeft = 1.0;
                            speedRight = -1.0;
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    speedLeft = speedRight = 0.0;
                    break;
                default:
                    return true;
            }
            log("Setting speed " + speedLeft + ", " + speedRight);
            RobotBroadcast.sendSetSpeed(MainActivity.this, new Speed(speedLeft, speedRight));
            return true;
        }
    }

    private enum ConnectState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        DISCONNECTING
    }
}
