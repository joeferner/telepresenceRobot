package com.telepresenceRobot.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.atmosphere.wasync.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends Activity {
    private StringBuilder logBuffer = new StringBuilder();
    private TextView log;
    private Button forward;
    private Button back;
    private Button left;
    private Button right;
    private Button connect;
    private RobotLink robotLink;
    private long serverId = new Date().getTime();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.main);

        log = (TextView) findViewById(R.id.log);
        connect = (Button) findViewById(R.id.connect);
        forward = (Button) findViewById(R.id.forward);
        back = (Button) findViewById(R.id.back);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);

        robotLink = new RobotLink(this);
        log.setMovementMethod(new ScrollingMovementMethod());

        robotLink.setEventHandler(new RobotLinkEventHandler() {
            @Override
            public void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
                log("Robot Link connection status changed: " + connectionStatus);
            }

            @Override
            public void onData(byte[] buffer, int start, int length) {
                log("Read: " + new String(buffer, start, length));
            }
        });

        forward.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.FORWARD));
        back.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.BACK));
        left.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.LEFT));
        right.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.RIGHT));
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("Connecting...");
                robotLink.connect();
            }
        });

        try {
            connectToServer();
        } catch (IOException e) {
            Log.e("telepresenceRobot", "Could not connect to server", e);
        }
    }

    private void connectToServer() throws IOException {
        Client client = ClientFactory.getDefault().newClient();

        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://192.168.0.160:9999")
                .encoder(new Encoder<JSONObject, String>() {
                    @Override
                    public String encode(JSONObject data) {
                        return data.toString();
                    }
                })
                .decoder(new Decoder<String, JSONObject>() {
                    @Override
                    public JSONObject decode(Event event, String s) {
                        if (event.equals(Event.MESSAGE)) {
                            try {
                                Log.i("telepresenceRobot", "JSONObject: " + s);
                                return new JSONObject(s);
                            } catch (JSONException e) {
                                Log.e("telepresenceRobot", "could not create JSON", e);
                            }
                        }
                        return null;
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET);

        final org.atmosphere.wasync.Socket socket = client.create();
        socket
                .on("message", new Function<JSONObject>() {
                    @Override
                    public void on(final JSONObject t) {
                        Log.i("telepresenceRobot", "onMessage " + t.toString());
                        try {
                            String type = t.getString("type");
                            if (type.equals("statusLed")) {
                                boolean newState = t.getBoolean("newState");
                                Log.i("telepresenceRobot", "setting status led " + newState);
                                robotLink.setStatusLed(newState);
                            } else if (type.equals("setSpeedPolar")) {
                                double angleRad = t.getDouble("angle");
                                while (angleRad < Math.PI) {
                                    angleRad += 2 * Math.PI;
                                }
                                while (angleRad > Math.PI) {
                                    angleRad -= 2 * Math.PI;
                                }
                                double power = t.getDouble("power");
                                double leftSpeed;
                                double rightSpeed;
                                if (angleRad >= 0 && angleRad < (Math.PI / 2.0)) {
                                    leftSpeed = 1.0;
                                    rightSpeed = (1.0 - (angleRad / (Math.PI / 2.0) * 2.0));
                                } else if (angleRad > (Math.PI / 2)) {
                                    rightSpeed = -1.0;
                                    leftSpeed = (1.0 - ((angleRad - (Math.PI / 2.0)) / (Math.PI / 2.0) * 2.0));
                                } else if (angleRad < 0 && angleRad > (-Math.PI / 2.0)) {
                                    rightSpeed = 1.0;
                                    leftSpeed = (1.0 - (Math.abs(angleRad) / (Math.PI / 2.0) * 2.0));
                                } else { //if (angleRad < 0)
                                    leftSpeed = -1.0;
                                    rightSpeed = (1.0 - ((Math.abs(angleRad) - (Math.PI / 2.0)) / (Math.PI / 2.0) * 2.0));
                                }
                                robotLink.setSpeed(leftSpeed * power, rightSpeed * power);
                            } else {
                                Log.e("telepresenceRobot", "Invalid packet type: " + type);
                            }
                        } catch (JSONException e) {
                            Log.e("telepresenceRobot", "Error", e);
                        }
                    }
                })
                .on(new Function<Throwable>() {
                    @Override
                    public void on(Throwable t) {
                        Log.e("telepresenceRobot", "Error", t);
                    }
                })
                .on(Event.OPEN, new Function<String>() {
                    @Override
                    public void on(String t) {
                        Log.i("telepresenceRobot", "open");
                        try {
                            JSONObject json = new JSONObject();
                            json.put("type", "setId");
                            json.put("id", Long.toString(serverId));
                            socket.fire(json);
                        } catch (IOException e) {
                            Log.e("telepresenceRobot", "Error", e);
                        } catch (JSONException e) {
                            Log.e("telepresenceRobot", "Error", e);
                        }
                    }
                })
                .open(request.build());
    }

    private void log(String line) {
        logBuffer.append(line);
        logBuffer.append("\n");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.setText(logBuffer.toString());
                int scrollAmount = log.getLayout().getLineTop(log.getLineCount()) - log.getHeight();
                log.scrollTo(0, scrollAmount);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        robotLink.resume();
    }

    @Override
    protected void onDestroy() {
        robotLink.destroy();
        super.onDestroy();
    }

    private class MovementOnTouchListener implements View.OnTouchListener {
        private final MainActivity mainActivity;
        private final MovementDirection movementDirection;

        public MovementOnTouchListener(MainActivity mainActivity, MovementDirection movementDirection) {
            this.mainActivity = mainActivity;
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
            robotLink.setSpeed(speedLeft, speedRight);
            return true;
        }
    }

}
