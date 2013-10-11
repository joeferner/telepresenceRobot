package com.telepresenceRobot.android.robot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.telepresenceRobot.android.Constants;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * ***************************FT311 GPIO interface class*****************************************
 */
public class FT31xUARTInterface extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.UARTTest.USB_PERMISSION";
    private static final String LOG_TAG = Constants.LOG + "-FT31x";
    private final Context context;
    public UsbManager usbManager;
    public UsbAccessory usbAccessory;
    public PendingIntent permissionIntent;
    public ParcelFileDescriptor fileDescriptor = null;
    public FileInputStream inputStream = null;
    public FileOutputStream outputStream = null;
    public boolean permissionRequestPending = false;
    public ReadThread readThread;

    private byte[] usbData;
    private byte[] writeUsbData;
    private byte[] readBuffer; /*circular buffer*/
    private int totalBytes;
    private int writeIndex;
    private int readIndex;
    private byte status;
    final int maxNumBytes = 65536;

    public boolean readEnable = false;
    public boolean accessoryAttached = false;

    public static String ManufacturerString = "mManufacturer=FTDI";
    public static String ModelString1 = "mModel=FTDIUARTDemo";
    public static String ModelString2 = "mModel=Android Accessory FT312D";
    public static String VersionString = "mVersion=1.0";

    public SharedPreferences sharePrefSettings;

    public FT31xUARTInterface(Context context, SharedPreferences sharePrefSettings) {
        super();
        this.context = context;
        this.sharePrefSettings = sharePrefSettings;

        // shall we start a thread here or what
        usbData = new byte[1024];
        writeUsbData = new byte[256];

        // 128 (make it 256, but looks like bytes should be enough)
        readBuffer = new byte[maxNumBytes];


        readIndex = 0;
        writeIndex = 0;

        /***********************USB handling******************************************/
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(usbReceiver, filter);

        inputStream = null;
        outputStream = null;
    }

    public void setConfig(int baud, byte dataBits, byte stopBits,
                          byte parity, byte flowControl) {

        // prepare the baud rate buffer
        writeUsbData[0] = (byte) baud;
        writeUsbData[1] = (byte) (baud >> 8);
        writeUsbData[2] = (byte) (baud >> 16);
        writeUsbData[3] = (byte) (baud >> 24);

        // data bits
        writeUsbData[4] = dataBits;

        // stop bits
        writeUsbData[5] = stopBits;

        // parity
        writeUsbData[6] = parity;

        // flow control
        writeUsbData[7] = flowControl;

        //send the UART configuration packet
        sendPacketToUsb(8);
    }

    public byte sendData(int numBytes, byte[] buffer) {
        status = 0x00; // success by default

        // if num bytes are more than maximum limit
        if (numBytes < 1) {
            // return the status with the error in the command
            return status;
        }

        // check for maximum limit
        if (numBytes > 256) {
            numBytes = 256;
        }

        // prepare the packet to be sent
        System.arraycopy(buffer, 0, writeUsbData, 0, numBytes);

        if (numBytes != 64) {
            sendPacketToUsb(numBytes);
        } else {
            byte temp = writeUsbData[63];
            sendPacketToUsb(63);
            writeUsbData[0] = temp;
            sendPacketToUsb(1);
        }

        return status;
    }

    public byte readData(int numBytes, byte[] buffer, int[] actualNumBytes) {
        status = 0x00; // success by default

        // should be at least one byte to read
        if ((numBytes < 1) || (totalBytes == 0)) {
            actualNumBytes[0] = 0;
            status = 0x01;
            return status;
        }

        // check for max limit
        if (numBytes > totalBytes)
            numBytes = totalBytes;

        // update the number of bytes available
        totalBytes -= numBytes;

        actualNumBytes[0] = numBytes;

        // copy to the user buffer
        for (int count = 0; count < numBytes; count++) {
            buffer[count] = readBuffer[readIndex];
            readIndex++;
            // shouldn't read more than what is there in the buffer,
            // so no need to check the overflow
            readIndex %= maxNumBytes;
        }
        return status;
    }

    private void sendPacketToUsb(int numBytes) {
        try {
            if (outputStream != null) {
                outputStream.write(writeUsbData, 0, numBytes);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not write packet", e);
        }
    }

    public int resumeAccessory() {
        if (inputStream != null && outputStream != null) {
            return 1;
        }

        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null) {
            Log.i(LOG_TAG, "Accessory Attached");
        } else {
            // return 2 for accessory detached case
            //Log.e(">>@@","resumeAccessory RETURN 2 (accessories == null)");
            accessoryAttached = false;
            return 2;
        }

        UsbAccessory accessory = accessories[0];
        if (accessory != null) {
            if (!accessory.toString().contains(ManufacturerString)) {
                Log.e(LOG_TAG, "Manufacturer is not matched! Expected " + ManufacturerString + " found " + accessory);
                return 1;
            }

            if (!accessory.toString().contains(ModelString1) && !accessory.toString().contains(ModelString2)) {
                Log.e(LOG_TAG, "Model is not matched! Expected " + ModelString1 + " and " + ModelString2 + " found " + accessory);
                return 1;
            }

            if (!accessory.toString().contains(VersionString)) {
                Log.e(LOG_TAG, "Version is not matched! Expected " + VersionString + " found " + accessory);
                return 1;
            }

            Log.i(LOG_TAG, "Manufacturer, Model & Version are matched!");
            accessoryAttached = true;

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

        return 0;
    }

    public void destroyAccessory(boolean configured) {
        Log.i(LOG_TAG, "destroyAccessory " + configured);
        if (configured) {
            readEnable = false;  // set false condition for handler_thread to exit waiting data loop
            writeUsbData[0] = 0;  // send dummy data for inStream.read going
            sendPacketToUsb(1);
        } else {
            setConfig(9600, (byte) 1, (byte) 8, (byte) 0, (byte) 0);  // send default setting data for config
            safeSleep(10);

            readEnable = false;  // set false condition for handler_thread to exit waiting data loop
            writeUsbData[0] = 0;  // send dummy data for inStream.read going
            sendPacketToUsb(1);
            if (accessoryAttached) {
                saveDefaultPreference();
            }
        }

        safeSleep(10);
        closeAccessory();

        context.unregisterReceiver(usbReceiver);
    }

    private void safeSleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not sleep", e);
        }
    }

    public void openAccessory(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            usbAccessory = accessory;

            FileDescriptor fd = fileDescriptor.getFileDescriptor();

            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);

            if (!readEnable) {
                readEnable = true;
                readThread = new ReadThread(inputStream);
                readThread.start();
            }
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

    protected void saveDetachPreference() {
        if (sharePrefSettings != null) {
            sharePrefSettings.edit()
                    .putString("configed", "FALSE")
                    .commit();
        }
    }

    protected void saveDefaultPreference() {
        if (sharePrefSettings != null) {
            sharePrefSettings.edit().putString("configed", "TRUE").commit();
            sharePrefSettings.edit().putInt("baudRate", 9600).commit();
            sharePrefSettings.edit().putInt("stopBit", 1).commit();
            sharePrefSettings.edit().putInt("dataBit", 8).commit();
            sharePrefSettings.edit().putInt("parity", 0).commit();
            sharePrefSettings.edit().putInt("flowControl", 0).commit();
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            Log.i(LOG_TAG, "Allow USB Permission");
                            openAccessory(accessory);
                        } else {
                            Log.e(LOG_TAG, "permission denied for accessory " + accessory);
                        }
                        permissionRequestPending = false;
                    }
                    break;
                case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    saveDetachPreference();
                    destroyAccessory(true);
                    //closeAccessory();
                    break;
                default:
                    Log.d(LOG_TAG, "received unknown action " + action);
                    break;
            }
        }
    };

    private class ReadThread extends Thread {
        FileInputStream inStream;

        ReadThread(FileInputStream stream) {
            inStream = stream;
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            while (readEnable) {
                while (totalBytes > (maxNumBytes - 1024)) {
                    safeSleep(50);
                }

                try {
                    if (inStream != null) {
                        int readCount = inStream.read(usbData, 0, 1024);
                        if (readCount > 0) {
                            for (int count = 0; count < readCount; count++) {
                                readBuffer[writeIndex] = usbData[count];
                                writeIndex++;
                                writeIndex %= maxNumBytes;
                            }

                            if (writeIndex >= readIndex)
                                totalBytes = writeIndex - readIndex;
                            else
                                totalBytes = (maxNumBytes - readIndex) + writeIndex;

//					    		Log.e(">>@@","totalBytes:"+totalBytes);
                        }
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not read", e);
                }
            }
        }
    }
}
