package com.telepresenceRobot.android;

public interface RobotLinkEventHandler {
  void onConnectionStatusChanged(ConnectionStatus connected);

  void onData(byte[] buffer, int start, int length);
}
