package com.telepresenceRobot.android.webSocket;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.telepresenceRobot.android.Constants;
import com.telepresenceRobot.android.StatusBroadcast;
import com.telepresenceRobot.android.robot.RobotBroadcast;
import com.telepresenceRobot.android.robot.Speed;
import org.atmosphere.wasync.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WebSocketService extends IntentService {
    private static final String LOG_TAG = Constants.getLogTag(WebSocketService.class);
    private static final String EXTRA_URL = "url";
    private static final long RECONNECT_INTERVAL = 1000;
    private Client client;
    private int reconnectAttempt;

    private Socket socket;
    private String url;
    private long serverId = new Date().getTime();
    private boolean pendingReconnect;
    private boolean broadcastLoopStarted;
    private boolean destorying;

    public WebSocketService() {
        super("WebSocket");
        client = ClientFactory.getDefault().newClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        destorying = false;
        this.url = intent.getStringExtra(EXTRA_URL);
        StatusBroadcast.sendLog(this, "Connecting to web socket: " + this.url);
        tryReconnect();
        beginBroadcastLoop();
    }

    private void beginBroadcastLoop() {
        if (broadcastLoopStarted) {
            return;
        }
        broadcastLoopStarted = true;
        try {
            while (!destorying) {
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
        destorying = false;
        super.onCreate();
        Log.i(LOG_TAG, WebSocketService.class.getName() + " created");
    }

    @Override
    public void onDestroy() {
        destorying = true;
        disconnect();
        Log.w(LOG_TAG, WebSocketService.class.getName() + " destroyed");
        super.onDestroy();
    }

    private void disconnect() {
        if (socket != null) {
            Log.i(LOG_TAG, "closing socket");
            socket.close();
            socket = null;
        }
    }

    private void connect(long timeoutMS) throws IOException {
        disconnect();
        Log.i(LOG_TAG, "connect " + timeoutMS + "ms");

        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(url)
                .encoder(socketEncode)
                .decoder(socketDecode)
                .transport(Request.TRANSPORT.WEBSOCKET);

        socket = client.create();

        socket
                .on(Event.MESSAGE, onSocketMessage)
                .on(onSocketError)
                .on(Event.OPEN, onSocketOpen)
                .on(Event.CLOSE, onSocketClose)
                .open(request.build(), timeoutMS, TimeUnit.MILLISECONDS);
    }

    private Function<String> onSocketOpen = new Function<String>() {
        @Override
        public void on(String t) {
            Log.i(LOG_TAG, "open");
            reconnectAttempt = 0;
            try {
                JSONObject json = new JSONObject();
                json.put("type", "setId");
                json.put("id", Long.toString(serverId));
                send(json);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);
                StatusBroadcast.sendException(WebSocketService.this, e);
            }
            StatusBroadcast.sendWebSocketConnectionOpened(WebSocketService.this);
        }
    };

    private void send(JSONObject json) throws IOException {
        if (socket == null) {
            Log.e(LOG_TAG, "Socket was null for message: " + json);
            return;
        }
        if (socket.status() != Socket.STATUS.OPEN && socket.status() != Socket.STATUS.INIT) {
            Log.e(LOG_TAG, "Socket status was " + socket.status() + " expected open/init for message: " + json);
            return;
        }

        Log.d(LOG_TAG, "Sending: " + json.toString());
        socket.fire(json);
    }

    private Function<String> onSocketClose = new Function<String>() {
        @Override
        public void on(String o) {
            Log.w(LOG_TAG, "close");
            StatusBroadcast.sendWebSocketConnectionClosed(WebSocketService.this);
            if (socket.status() != Socket.STATUS.OPEN) {
                tryReconnect();
            }
        }
    };

    private Function<JSONObject> onSocketMessage = new Function<JSONObject>() {
        @Override
        public void on(final JSONObject t) {
            Log.i(LOG_TAG, "onMessage " + t.toString());
            try {
                String type = t.getString("type");
                if (type.equals("setSpeedPolar")) {
                    Speed speed = SpeedPolarConversion.toSpeed(t.getDouble("angle"), t.getDouble("power"));
                    RobotBroadcast.sendSetSpeed(WebSocketService.this, speed);
                } else {
                    Log.e(LOG_TAG, "Invalid packet type: " + type);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                StatusBroadcast.sendException(WebSocketService.this, e);
            }
        }
    };

    private Function<Throwable> onSocketError = new Function<Throwable>() {
        @Override
        public void on(Throwable t) {
            Log.e(LOG_TAG, "Error", t);
            StatusBroadcast.sendException(WebSocketService.this, t);
            if (socket.status() != Socket.STATUS.OPEN) {
                tryReconnect();
            }
        }
    };

    private void tryReconnect() {
        if (destorying) {
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
                StatusBroadcast.sendLog(WebSocketService.this, logMessage);
                while (new Date().getTime() < nextReconnectAttemptDate.getTime()) {
                    Thread.sleep(100);
                }
            } finally {
                pendingReconnect = false;

            }

            connect(nextReconnectAttemptDurationMS);
        } catch (Exception e) {
            String logMessage = "Web Socket failed to connect to " + this.url;
            Log.e(LOG_TAG, logMessage, e);
            StatusBroadcast.sendLog(WebSocketService.this, logMessage);
            StatusBroadcast.sendException(this, e);
        }
    }

    private long getNextReconnectAttemptTime() {
        return RECONNECT_INTERVAL * (long) Math.pow(2, reconnectAttempt);
    }

    public static void startService(Context context, String url) {
        stopService(context);
        Log.i(LOG_TAG, "startService (url: " + url + ")");
        Intent serviceIntent = new Intent(context, WebSocketService.class);
        serviceIntent.putExtra(EXTRA_URL, url);
        context.startService(serviceIntent);
    }

    public static void stopService(Context context) {
        Log.i(LOG_TAG, "Stopping service");
        Intent serviceIntent = new Intent(context, WebSocketService.class);
        context.stopService(serviceIntent);
    }

    private Encoder<JSONObject, String> socketEncode = new Encoder<JSONObject, String>() {
        @Override
        public String encode(JSONObject data) {
            return data.toString();
        }
    };

    private Decoder<String, JSONObject> socketDecode = new Decoder<String, JSONObject>() {
        @Override
        public JSONObject decode(Event event, String s) {
            if (event.equals(Event.MESSAGE)) {
                try {
                    Log.i(LOG_TAG, "JSONObject: " + s);
                    return new JSONObject(s);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "could not create JSON", e);
                    StatusBroadcast.sendException(WebSocketService.this, e);
                }
            }
            return null;
        }
    };
}
