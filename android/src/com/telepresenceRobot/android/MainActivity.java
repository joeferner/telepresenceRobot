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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.Speed;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends Activity {
    private static final String LOG_TAG = Constants.getLogTag(MainActivity.class);
    private final Queue<String> logBuffer = new LinkedList<String>();
    private TextView log;
    private Button forward;
    private Button back;
    private Button left;
    private Button right;
    private ConnectState webSocketConnectState = ConnectState.DISCONNECTED;
    private ConnectState robotConnectState = ConnectState.DISCONNECTED;
    private MenuItem connectRobotMenuItem;
    private MenuItem connectWebSocketMenuItem;
    private TextView batteryVoltage;
    private CheckBox enableLogging;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.main);

        log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        forward = (Button) findViewById(R.id.forward);
        back = (Button) findViewById(R.id.back);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        enableLogging = (CheckBox) findViewById(R.id.enable_logging);
        enableLogging.setChecked(sharedPref.getBoolean(SettingsActivity.ENABLE_LOGGING, true));
        enableLogging.setOnCheckedChangeListener(new EnableLoggingCheckedChangedListener());
        batteryVoltage = (TextView) findViewById(R.id.battery_level);
        batteryVoltage.setText("???");

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
        StatusBroadcast.sendRobotConnect(this);
    }

    private void webSocketConnect() {
        webSocketConnectState = ConnectState.CONNECTING;
        updateMenuItems();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String address = sharedPref.getString(SettingsActivity.SERVER_HOSTNAME, null);
        String portStr = sharedPref.getString(SettingsActivity.SERVER_PORT, "" + TelepresenceServerClientService.DEFAULT_PORT);
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception ex) {
            port = TelepresenceServerClientService.DEFAULT_PORT;
        }
        StatusBroadcast.sendWebSocketConnect(this, address, port);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RobotBroadcast.sendResume(this);
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
                Log.i(LOG_TAG, "webSocketConnectState: " + webSocketConnectState);
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
                Log.i(LOG_TAG, "connectRobotMenuItem: " + robotConnectState);
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
                            batteryVoltage.setText("???");
                            break;
                        case DISCONNECTING:
                            connectRobotMenuItem.setEnabled(false);
                            connectRobotMenuItem.setTitle(getString(R.string.disconnecting_robot));
                            batteryVoltage.setText("???");
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
                ForegroundService.stopService(this);
                finish();
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
            log("Error: " + e.getMessage());
        }

        @Override
        protected void onForegroundServiceStarted(Context context, Intent intent) {
            super.onForegroundServiceStarted(context, intent);
            RobotBroadcast.sendResume(MainActivity.this);
            robotConnect();
            webSocketConnect();
        }

        @Override
        protected void onLog(Context context, Intent intent, String message) {
            super.onLog(context, intent, message);
            log(message);
        }
    };

    private RobotBroadcast.Receiver robotBroadcastReceiver = new RobotBroadcast.Receiver() {
        @Override
        protected void onSetSpeed(Context context, Intent intent, Speed speed) {
            super.onSetSpeed(context, intent, speed);
            log("Setting speed " + speed.getLeftSpeed() + ", " + speed.getRightSpeed());
        }

        @Override
        protected void onSetTilt(Context context, Intent intent, double tilt) {
            super.onSetTilt(context, intent, tilt);
            log("Setting tilt " + tilt);
        }

        @Override
        protected void onData(Context context, Intent intent, byte[] buffer) {
            super.onData(context, intent, buffer);
            log("From Robot: " + new String(buffer).trim());
        }

        @Override
        protected void onBatteryVoltage(Context context, Intent intent, int batteryVoltage) {
            super.onBatteryVoltage(context, intent, batteryVoltage);
            double percent = ((double) batteryVoltage) / 0xffff;
            String str = ((int) Math.floor(percent * 100.0)) + "%";
            MainActivity.this.batteryVoltage.setText(str);
        }

        @Override
        protected void onConnected(Context context, Intent intent) {
            super.onConnected(context, intent);
            log("robot connected");
            robotConnectState = ConnectState.CONNECTED;
            updateMenuItems();
        }

        @Override
        protected void onConnectFailed(Context context, Intent intent, Throwable e) {
            super.onConnectFailed(context, intent, e);
            log("robot failed to connect: " + e.getMessage());
            robotConnectState = ConnectState.DISCONNECTED;
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
        Log.i(LOG_TAG, "log: " + line);

        synchronized (logBuffer) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            if (sharedPref.getBoolean(SettingsActivity.ENABLE_LOGGING, true)) {
                logBuffer.add(line);
                while (logBuffer.size() > 200) {
                    logBuffer.remove();
                }
            } else {
                logBuffer.clear();
            }
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (log == null || log.getLayout() == null) {
                    return;
                }
                synchronized (logBuffer) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : logBuffer) {
                        sb.append(s);
                        sb.append('\n');
                    }
                    log.setText(sb.toString());
                }
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

    private class EnableLoggingCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPref.edit().putBoolean(SettingsActivity.ENABLE_LOGGING, isChecked).commit();
        }
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
