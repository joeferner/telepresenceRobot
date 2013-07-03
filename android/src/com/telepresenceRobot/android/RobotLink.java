package com.telepresenceRobot.android;

import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class RobotLink {
  private static final int BAUD = 9600;
  private static final byte DATA_BITS = 8;
  private static final byte STOP_BITS = 1;
  private static final byte PARITY = 0;
  private static final byte FLOW_CONTROL = 0;
  private final Thread communicationsThread;
  private FT31xUARTInterface uartInterface;
  private RobotLinkEventHandler eventHandler;
  private final Queue<String> commandQueue = new LinkedList<>();

  public RobotLink(Context context) {
    uartInterface = new FT31xUARTInterface(context, null);
    communicationsThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
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
                eventHandler.onData(readBuffer, 0, actualNumberOfBytesRead);
              } catch (Exception ex) {
                Log.e("RobotLink", "Calling eventHandler.onData", ex);
              }
              Log.d("RobotLink", "bytes read " + actualNumberOfBytesRead + " " + new String(readBuffer, 0, actualNumberOfBytesRead));
            }
          } else if (readStatus == 0x01) {

          } else {
            Log.w("RobotLink", "readData status was not 0x00 but was 0x" + byteToHex(readStatus));
          }

          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Log.e("RobotLink", "Failed to sleep", e);
          }
        }
      }
    });
    communicationsThread.start();
  }

  public void resume() {
    this.uartInterface.resumeAccessory();
  }

  public void setSpeed(double speedLeft, double speedRight) {
    speedLeft = clamp(speedLeft, -1.0, 1.0);
    byte speedLeftByte = (byte) (speedLeft * (255 / 2));

    speedRight = clamp(speedRight, -1.0, 1.0);
    byte speedRightByte = (byte) (speedRight * (255 / 2));

    enqueueSetCommand(RobotRegister.SPEED, byteToHex(speedLeftByte) + byteToHex(speedRightByte));
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
    uartInterface.setConfig(BAUD, DATA_BITS, STOP_BITS, PARITY, FLOW_CONTROL);
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Log.e("RobotLink", "Could not sleep", e);
    }
    enqueueCommand("connect");
    eventHandler.onConnectionStatusChanged(ConnectionStatus.CONNECTED);
  }

  public void setEventHandler(RobotLinkEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  public RobotLinkEventHandler getEventHandler() {
    return eventHandler;
  }

  public void destroy() {
    this.uartInterface.destroyAccessory(true);
    communicationsThread.stop();
  }

  final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static String byteToHex(byte b) {
    char[] hexChars = new char[2];
    hexChars[0] = hexArray[(b >> 4) & 0x0f];
    hexChars[1] = hexArray[(b >> 0) & 0x0f];
    return new String(hexChars);
  }

  private enum RobotRegister {
    SPEED
  }
}
