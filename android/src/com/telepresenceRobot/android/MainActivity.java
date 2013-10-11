package com.telepresenceRobot.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.RobotService;
import com.telepresenceRobot.android.robot.Speed;
import com.telepresenceRobot.android.webSocket.WebSocketService;

public class MainActivity extends Activity {
    private StringBuilder logBuffer = new StringBuilder();
    private TextView log;
    private Button forward;
    private Button back;
    private Button left;
    private Button right;
    private Button connectWebSocket;
    private Button connectRobot;
    private EditText address;
    private boolean webSocketConnected;
    private boolean robotConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.main);

        log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        connectWebSocket = (Button) findViewById(R.id.connectWebSocket);
        connectRobot = (Button) findViewById(R.id.connectRobot);
        forward = (Button) findViewById(R.id.forward);
        back = (Button) findViewById(R.id.back);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        address = (EditText) findViewById(R.id.address);

        forward.setOnTouchListener(new MovementOnTouchListener(MovementDirection.FORWARD));
        back.setOnTouchListener(new MovementOnTouchListener(MovementDirection.BACK));
        left.setOnTouchListener(new MovementOnTouchListener(MovementDirection.LEFT));
        right.setOnTouchListener(new MovementOnTouchListener(MovementDirection.RIGHT));
        connectWebSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    disconnectWebSocket();
                } else {
                    connectWebSocket();
                }
            }
        });
        connectRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (robotConnected) {
                    disconnectRobot();
                } else {
                    connectRobot();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(robotBroadcastReceiver, new IntentFilter(RobotBroadcast.BROADCAST_NAME));
        LocalBroadcastManager.getInstance(this).registerReceiver(statusBroadcastReceiver, new IntentFilter(StatusBroadcast.BROADCAST_NAME));

        connectWebSocket();
        connectRobot();
    }

    private void connectRobot() {
        log("Connecting robot...");
        RobotService.startService(this);
    }

    private void disconnectRobot() {
        log("Disconnecting robot...");
        RobotService.stopService(this);
    }

    private void connectWebSocket() {
        log("Connecting web socket...");
        WebSocketService.startService(this, address.getText().toString());
    }

    private void disconnectWebSocket() {
        log("Disconnecting...");
        WebSocketService.stopService(this);
    }

    private StatusBroadcast.Receiver statusBroadcastReceiver = new StatusBroadcast.Receiver() {
        @Override
        protected void onWebSocketOpened(Context context, Intent intent) {
            super.onWebSocketOpened(context, intent);
            webSocketConnected = true;
            connectWebSocket.setText(getString(R.string.disconnectWebSocket));
            log("Web socket opened");
        }

        @Override
        protected void onWebSocketClosed(Context context, Intent intent) {
            super.onWebSocketClosed(context, intent);
            webSocketConnected = false;
            connectWebSocket.setText(getString(R.string.connectWebSocket));
            log("Web socket disconnected");
        }

        @Override
        protected void onException(Context context, Intent intent, Throwable e) {
            super.onException(context, intent, e);
            log(e.getMessage());
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
            robotConnected = true;
            connectRobot.setText(getString(R.string.disconnectRobot));
        }

        @Override
        protected void onDisconnected(Context context, Intent intent) {
            super.onDisconnected(context, intent);
            log("robot disconnected");
            robotConnected = false;
            connectRobot.setText(getString(R.string.connectRobot));
        }
    };

    private void log(String line) {
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

}
