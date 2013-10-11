package com.telepresenceRobot.android.robot;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.telepresenceRobot.android.Constants;
import com.telepresenceRobot.android.StatusBroadcast;
import com.telepresenceRobot.android.util.ByteUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class RobotService extends IntentService {
    private static final int BAUD = 9600;
    private static final byte DATA_BITS = 8;
    private static final byte STOP_BITS = 1;
    private static final byte PARITY = 0;
    private static final byte FLOW_CONTROL = 0;
    private FT31xUARTInterface uartInterface;
    private final Queue<String> commandQueue = new LinkedList<String>();
    private boolean connecting;
    private boolean destorying;

    public RobotService() {
        super("robotService");
    }

    @Override
    public void onCreate() {
        destorying = false;
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(robotBroadcastReceiver, new IntentFilter(RobotBroadcast.BROADCAST_NAME));
    }

    @Override
    public void onDestroy() {
        destorying = true;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(robotBroadcastReceiver);
        this.uartInterface.destroyAccessory(true);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        destorying = false;
        try {
            uartInterface = new FT31xUARTInterface(this, null);

            connect();

            Log.i(Constants.LOG, "Starting robot loop");
            while (!destorying) {
                try {
                    loop();
                } catch (Exception ex) {
                    StatusBroadcast.sendException(this, ex);
                }
            }
        } catch (Exception ex) {
            StatusBroadcast.sendException(this, ex);
        } finally {
            Log.i(Constants.LOG, "Stopped robot loop");
        }
    }

    private void loop() throws Exception {
        synchronized (commandQueue) {
            if (commandQueue.size() > 0) {
                String cmd = commandQueue.remove() + "\n";
                Log.d("RobotLink", "Sending:" + cmd);
                uartInterface.sendData(cmd.length(), cmd.getBytes());
            }
        }

        byte[] readBuffer = new byte[4096];
        int[] actualNumberOfBytesReadArray = new int[1];
        byte readStatus = uartInterface.readData(readBuffer.length, readBuffer, actualNumberOfBytesReadArray);
        if (readStatus == 0x00) {
            int actualNumberOfBytesRead = actualNumberOfBytesReadArray[0];
            if (actualNumberOfBytesRead > 0) {
                try {
                    byte[] data = Arrays.copyOfRange(readBuffer, 0, actualNumberOfBytesRead);
                    RobotBroadcast.sendData(this, data);
                } catch (Exception ex) {
                    Log.e("RobotLink", "Calling eventHandler.onData", ex);
                }
                Log.d("RobotLink", "bytes read " + actualNumberOfBytesRead + " " + new String(readBuffer, 0, actualNumberOfBytesRead));
            }
        } else if (readStatus == 0x01) {

        } else {
            Log.w("RobotLink", "readData status was not 0x00 but was 0x" + ByteUtil.byteToHex(readStatus));
        }

        if (connecting) {
            RobotBroadcast.sendConnected(this);
            connecting = false;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e("RobotLink", "Failed to sleep", e);
        }
    }

    private RobotBroadcast.Receiver robotBroadcastReceiver = new RobotBroadcast.Receiver() {
        @Override
        protected void onSetSpeed(Context context, Intent intent, Speed speed) {
            super.onSetSpeed(context, intent, speed);
            setSpeed(speed.getLeftSpeed(), speed.getRightSpeed());
        }
    };

    public void resume() {
        this.uartInterface.resumeAccessory();
    }

    public void setSpeed(double speedLeft, double speedRight) {
        speedLeft = clamp(speedLeft, -1.0, 1.0);
        byte speedLeftByte = (byte) (speedLeft * (255 / 2));

        speedRight = clamp(speedRight, -1.0, 1.0);
        byte speedRightByte = (byte) (speedRight * (255 / 2));

        enqueueSetCommand(RobotRegister.SPEED, ByteUtil.byteToHex(speedLeftByte) + ByteUtil.byteToHex(speedRightByte));
    }

    private double clamp(double val, double min, double max) {
        return Math.max(Math.min(val, max), min);
    }

    private void enqueueSetCommand(RobotRegister robotRegister, String val) {
        enqueueCommand("set " + robotRegister.toString().toLowerCase() + "=" + val);
    }

    private void enqueueCommand(String cmd) {
        synchronized (commandQueue) {
            commandQueue.add(cmd);
        }
    }

    public void connect() {
        connecting = true;
        uartInterface.setConfig(BAUD, DATA_BITS, STOP_BITS, PARITY, FLOW_CONTROL);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e("RobotLink", "Could not sleep", e);
        }
        enqueueCommand("connect");
    }

    public static void startService(Context context) {
        stopService(context);
        Log.i(Constants.LOG, "Starting service");
        Intent serviceIntent = new Intent(context, RobotService.class);
        context.startService(serviceIntent);
    }

    public static void stopService(Context context) {
        Log.i(Constants.LOG, "Stopping service");
        Intent serviceIntent = new Intent(context, RobotService.class);
        context.stopService(serviceIntent);
    }

    private enum RobotRegister {
        SPEED
    }
}
