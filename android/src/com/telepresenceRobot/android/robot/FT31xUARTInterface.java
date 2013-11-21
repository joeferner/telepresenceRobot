package com.telepresenceRobot.android.robot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.telepresenceRobot.android.Constants;
import com.telepresenceRobot.android.StatusBroadcast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class FT31xUARTInterface extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.telepresenceRobot.android.robot.USB_PERMISSION";
    private static final String LOG_TAG = Constants.getLogTag(FT31xUARTInterface.class);
    private final Context context;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private ParcelFileDescriptor fileDescriptor = null;
    private FileInputStream inputStream = null;
    private FileOutputStream outputStream = null;
    private boolean permissionRequestPending = false;

    private final RingBuffer ringBuffer = new RingBuffer();
    private boolean readEnable = false;

    private static final String ManufacturerString = "mManufacturer=FTDI";
    private static final String ModelString1 = "mModel=FTDIUARTDemo";
    private static final String ModelString2 = "mModel=Android Accessory FT312D";
    private static final String VersionString = "mVersion=1.0";
    private ReadThread readThread;

    public FT31xUARTInterface(Context context) {
        super();
        this.context = context;

        /***********************USB handling******************************************/
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(usbReceiver, filter);

        inputStream = null;
        outputStream = null;
    }

    public void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) throws IOException {
        byte[] data = new byte[8];

        if (outputStream == null) {
            throw new IOException("Could not set config device not open");
        }

        // prepare the baud rate buffer
        data[0] = (byte) baud;
        data[1] = (byte) (baud >> 8);
        data[2] = (byte) (baud >> 16);
        data[3] = (byte) (baud >> 24);

        // data bits
        data[4] = dataBits;

        // stop bits
        data[5] = stopBits;

        // parity
        data[6] = parity;

        // flow control
        data[7] = flowControl;

        //send the UART configuration packet
        outputStream.write(data, 0, 8);
    }

    public void send(byte[] buffer) throws IOException {
        send(buffer, 0, buffer.length);
    }

    private void send(byte[] buffer, int start, int length) throws IOException {
        for (int i = start; i < start + length; i += 32) {
            int count = Math.min(32, length - (i - start));
            outputStream.write(buffer, i, count);
            outputStream.flush();
            safeSleep(1);
        }
    }

    public int read(byte[] buffer, int start, int length, int timeout) throws IOException, InterruptedException {
        return ringBuffer.blockingRead(buffer, start, length, timeout);
    }

    public void resumeAccessory() throws Exception {
        Log.d(LOG_TAG, "resumeAccessory");
        if (inputStream != null && outputStream != null) {
            Log.d(LOG_TAG, "already open");
            return;
        }

        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null) {
            Log.i(LOG_TAG, "Accessory Attached");
        } else {
            Log.e(LOG_TAG, "resumeAccessory (accessories == null)");
            throw new Exception("No accessories found");
        }

        UsbAccessory accessory = accessories[0];
        if (accessory != null) {
            Log.d(LOG_TAG, "accessory: " + accessory);

            if (!accessory.toString().contains(ManufacturerString)) {
                String msg = "Manufacturer is not matched! Expected " + ManufacturerString + " found " + accessory;
                Log.e(LOG_TAG, msg);
                StatusBroadcast.sendLog(this, msg);
                // TODO change this to use read data
            }

            if (!accessory.toString().contains(ModelString1) && !accessory.toString().contains(ModelString2)) {
                String msg = "Model is not matched! Expected " + ModelString1 + " and " + ModelString2 + " found " + accessory;
                Log.e(LOG_TAG, msg);
                StatusBroadcast.sendLog(this, msg);
                // TODO change this to use read data
            }

            if (!accessory.toString().contains(VersionString)) {
                String msg = "Version is not matched! Expected " + VersionString + " found " + accessory;
                Log.e(LOG_TAG, msg);
                StatusBroadcast.sendLog(this, msg);
                // TODO change this to use read data
            }

            String msg = "Manufacturer, Model & Version are matched! " + accessory;
            Log.i(LOG_TAG, msg);
            StatusBroadcast.sendLog(this, msg);

            if (usbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!permissionRequestPending) {
                        Log.i(LOG_TAG, "Request USB Permission");
                        usbManager.requestPermission(accessory, permissionIntent);
                        permissionRequestPending = true;
                    }
                }
            }
        }
    }

    public void destroyAccessory() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "destroyAccessory");
        readEnable = false;
        try {
            byte[] data = new byte[1];
            data[0] = 0;
            outputStream.write(data, 0, 1);

            readThread.join(1000);
        } finally {
            safeSleep(10);
            closeAccessory();

            context.unregisterReceiver(usbReceiver);
        }
    }

    private void safeSleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not sleep", e);
        }
    }

    public void openAccessory(UsbAccessory accessory) throws IOException {
        Log.d(LOG_TAG, "openAccessory");
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor == null) {
            throw new IOException("Could not open accessory");
        }
        Log.d(LOG_TAG, "fileDescriptor: " + fileDescriptor);

        FileDescriptor fd = fileDescriptor.getFileDescriptor();

        inputStream = new FileInputStream(fd);
        outputStream = new FileOutputStream(fd);

        if (!readEnable) {
            Log.d(LOG_TAG, "starting read thread");
            readEnable = true;
            readThread = new ReadThread();
            readThread.start();
        }
    }

    private void closeAccessory() {
        Log.i(LOG_TAG, "closeAccessory");
        try {
            if (fileDescriptor != null) {
                fileDescriptor.close();
                fileDescriptor = null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "could not close fileDescriptor", e);
        }

        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "could not close inputStream", e);
        }

        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "could not close outputStream", e);
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(LOG_TAG, "usbReceiver " + action);
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            Log.i(LOG_TAG, "Allow USB Permission");
                            try {
                                openAccessory(accessory);
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "could not open accessory", e);
                                StatusBroadcast.sendLog(FT31xUARTInterface.this, "could not open accessory: " + e.getMessage());
                            }
                        } else {
                            Log.e(LOG_TAG, "permission denied for accessory " + accessory);
                        }
                        permissionRequestPending = false;
                    }
                    break;
                case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    try {
                        destroyAccessory();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Could not destroy accessory", e);
                    }
                    closeAccessory();
                    break;
                default:
                    Log.d(LOG_TAG, "received unknown action " + action);
                    break;
            }
        }
    };

    public boolean isConnected() {
        return outputStream != null;
    }

    private class ReadThread extends Thread {
        ReadThread() {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            byte[] usbData = new byte[1024];

            Log.i(LOG_TAG, "Read thread started");
            while (readEnable && inputStream != null) {
                try {
                    int readCount = inputStream.read(usbData, 0, usbData.length);
                    if (readCount > 0) {
                        while (!ringBuffer.blockingWrite(usbData, 0, readCount, 100)) {
                            if (!readEnable || inputStream == null) {
                                return;
                            }
                        }
                    } else {
                        safeSleep(10);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Could not read", e);
                }
            }
            Log.i(LOG_TAG, "Read thread stopped");
        }
    }

    private static class RingBuffer {
        private static final int MAX_NUM_BYTES = 65536;
        private final byte[] buffer;
        private int length;
        private int nextGet;
        private int nextPut;

        public RingBuffer() {
            buffer = new byte[MAX_NUM_BYTES];
        }

        public synchronized boolean blockingWrite(byte[] data, int start, int length, int timeout) throws IOException, InterruptedException {
            if (availableSpace() < length) {
                wait(timeout);
                if (availableSpace() < length) {
                    return false;
                }
            }
            write(data, start, length);
            return true;
        }

        public synchronized void write(byte[] data, int start, int length) throws IOException {
            for (int i = start; i < start + length; i++) {
                write(data[i]);
            }
        }

        private synchronized void write(byte b) throws IOException {
            if (isFull()) {
                throw new IOException("Buffer overflow");
            }

            length++;
            buffer[nextPut] = b;
            nextPut++;
            if (nextPut >= buffer.length) {
                nextPut = 0;
            }
            notifyAll();
        }

        public synchronized int blockingRead(byte[] buffer, int start, int length, int timeout) throws IOException, InterruptedException {
            if (length() <= 0) {
                wait(timeout);
            }
            return read(buffer, start, length);
        }

        public synchronized int read(byte[] buffer, int start, int length) throws IOException {
            int readCount = 0;
            for (int i = start; i < start + length && !isEmpty(); i++) {
                buffer[i] = read();
                readCount++;
            }
            return readCount;
        }

        public synchronized byte read() throws IOException {
            if (isEmpty()) {
                throw new IOException("Buffer underflow");
            }

            length--;
            byte b = buffer[nextGet];
            nextGet++;
            if (nextGet >= buffer.length) {
                nextGet = 0;
            }
            notifyAll();
            return b;
        }

        public boolean isEmpty() {
            return length() == 0;
        }

        public boolean isFull() {
            return length() == size();
        }

        public int availableSpace() {
            return size() - length();
        }

        public int size() {
            return buffer.length;
        }

        public int length() {
            return length;
        }
    }
}
