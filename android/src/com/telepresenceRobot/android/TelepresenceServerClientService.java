package com.telepresenceRobot.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.telepresenceRobot.android.robot.PolarToSpeedConverter;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.Speed;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

public class TelepresenceServerClientService extends IntentService {
    private static final String LOG_TAG = Constants.getLogTag(TelepresenceServerClientService.class);
    public static final String EXTRA_HOSTNAME = "hostname";
    public static final String EXTRA_PORT = "port";
    private static final long RECONNECT_INTERVAL = 1000;
    public static final int DEFAULT_PORT = 8889;
    private int reconnectAttempt;

    private String hostname;
    private int port;
    private long serverId = new Date().getTime();
    private boolean pendingReconnect;
    private boolean broadcastLoopStarted;
    private boolean destroying;
    private Socket socket;
    private final Object socketLock = new Object();
    private BufferedReader socketReader;

    public TelepresenceServerClientService() {
        super("TelepresenceServerClient");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        destroying = false;
        this.hostname = intent.getStringExtra(EXTRA_HOSTNAME);
        this.port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        StatusBroadcast.sendLog(this, "Connecting to telepresence server: " + this.hostname + ":" + this.port);
        tryReconnect();
        beginBroadcastLoop();
    }

    private void beginBroadcastLoop() {
        if (broadcastLoopStarted) {
            return;
        }
        broadcastLoopStarted = true;
        try {
            while (!destroying) {
                JSONObject json = new JSONObject();
                json.put("type", "broadcast");
                try {
                    send(json);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to send broadcast", e);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed in broadcast loop", e);
        } finally {
            broadcastLoopStarted = false;
        }
    }

    @Override
    public void onCreate() {
        destroying = false;
        super.onCreate();
        Log.i(LOG_TAG, TelepresenceServerClientService.class.getName() + " created");
    }

    @Override
    public void onDestroy() {
        Log.w(LOG_TAG, TelepresenceServerClientService.class.getName() + " destroyed");
        destroying = true;
        disconnect();
        super.onDestroy();
    }

    private void disconnect() {
        synchronized (socketLock) {
            if (socket != null) {
                Log.i(LOG_TAG, "closing socket");
                try {
                    socket.close();
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Could not close socket", ex);
                }
                socketReader = null;
                socket = null;
            }
        }
    }

    private void connect(long timeoutMS) throws IOException {
        synchronized (socketLock) {
            disconnect();
            Log.i(LOG_TAG, "connect " + timeoutMS + "ms");
            socket = new Socket(hostname, port);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Log.i(LOG_TAG, "open");
            reconnectAttempt = 0;
            try {
                JSONObject json = new JSONObject();
                json.put("type", "setId");
                json.put("id", Long.toString(serverId));
                send(json);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);
                StatusBroadcast.sendException(TelepresenceServerClientService.this, e);
            }
            StatusBroadcast.sendWebSocketConnectionOpened(TelepresenceServerClientService.this);

            beginReadLoop();
        }
    }

    private void send(JSONObject json) throws IOException {
        synchronized (socketLock) {
            if (socket == null) {
                Log.e(LOG_TAG, "Socket was null for message: " + json);
                return;
            }

            if (socket.isClosed()) {
                onSocketClosed();
                return;
            }

            Log.d(LOG_TAG, "Sending: " + json.toString());
            socket.getOutputStream().write((json.toString() + "\n").getBytes());
        }
    }

    private void onSocketClosed() {
        synchronized (socketLock) {
            Log.w(LOG_TAG, "close");
            socketReader = null;
            socket = null;
            StatusBroadcast.sendWebSocketConnectionClosed(TelepresenceServerClientService.this);
        }
        tryReconnect();
    }

    private void beginReadLoop() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "read loop started");
                try {
                    while (!destroying) {
                        synchronized (socketLock) {
                            if (socket == null || socketReader == null) {
                                Log.i(LOG_TAG, "socket or socketReader was null, exiting loop");
                                break;
                            }
                            if (socket.isClosed()) {
                                Log.i(LOG_TAG, "socket is closed");
                                onSocketClosed();
                                break;
                            }
                        }

                        try {
                            String line = socketReader.readLine();
                            if (line == null) {
                                Log.i(LOG_TAG, "line was empty");
                                onSocketClosed();
                                break;
                            }
                            Log.i(LOG_TAG, "read loop line: " + line);
                            JSONObject json = new JSONObject(line);
                            onSocketMessage(json);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Failed to read message", e);
                            Thread.sleep(500);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed in read loop", e);
                } finally {
                    Log.i(LOG_TAG, "read loop ended");
                }
            }
        });
        t.start();
    }

    private void onSocketMessage(JSONObject t) {
        Log.i(LOG_TAG, "onMessage " + t.toString());
        try {
            String type = t.getString("type");
            if (type.equals("setSpeedPolar")) {
                Speed speed = PolarToSpeedConverter.toSpeed(t.getDouble("angle"), t.getDouble("power"));
                RobotBroadcast.sendSetSpeed(TelepresenceServerClientService.this, speed);
            } else if (type.equals("setTilt")) {
                RobotBroadcast.sendSetTile(TelepresenceServerClientService.this, t.getDouble("tilt"));
            } else {
                Log.e(LOG_TAG, "Invalid packet type: " + type);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error", e);
            StatusBroadcast.sendException(TelepresenceServerClientService.this, e);
        }
    }

    private void tryReconnect() {
        if (destroying) {
            return;
        }

        Log.i(LOG_TAG, "tryReconnect");
        try {
            if (pendingReconnect) {
                return;
            }

            long nextReconnectAttemptDurationMS;
            try {
                pendingReconnect = true;
                reconnectAttempt++;
                nextReconnectAttemptDurationMS = getNextReconnectAttemptTime();
                Date nextReconnectAttemptDate = new Date(new Date().getTime() + nextReconnectAttemptDurationMS);
                String logMessage = "Web Socket waiting for reconnect (" + (nextReconnectAttemptDurationMS / 1000) + "s)";
                Log.i(LOG_TAG, logMessage);
                StatusBroadcast.sendLog(TelepresenceServerClientService.this, logMessage);
                while (new Date().getTime() < nextReconnectAttemptDate.getTime()) {
                    Thread.sleep(100);
                }
            } finally {
                pendingReconnect = false;

            }

            connect(nextReconnectAttemptDurationMS);
        } catch (Exception e) {
            String logMessage = "Socket failed to connect to " + this.hostname + ":" + this.port;
            Log.e(LOG_TAG, logMessage, e);
            StatusBroadcast.sendLog(TelepresenceServerClientService.this, logMessage);
            StatusBroadcast.sendException(this, e);
            disconnect();
            onSocketClosed();
        }
    }

    private long getNextReconnectAttemptTime() {
        return RECONNECT_INTERVAL * (long) Math.pow(2, reconnectAttempt);
    }

    public static void startService(Context context, String hostname, int port) {
        stopService(context);
        Log.i(LOG_TAG, "startService (" + hostname + ":" + port + ")");
        Intent serviceIntent = new Intent(context, TelepresenceServerClientService.class);
        serviceIntent.putExtra(EXTRA_HOSTNAME, hostname);
        serviceIntent.putExtra(EXTRA_PORT, port);
        context.startService(serviceIntent);
    }

    public static void stopService(Context context) {
        Log.i(LOG_TAG, "Stopping service");
        Intent serviceIntent = new Intent(context, TelepresenceServerClientService.class);
        context.stopService(serviceIntent);
    }
}
