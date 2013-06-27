package com.telepresenceRobot.android;

public class RobotLink {
  private RobotLinkEventHandler eventHandler;

  public void setSpeed(MovementDirection movementDirection, double percentage) {

  }

  public void connect() {
    eventHandler.onConnectionStatusChanged(ConnectionStatus.CONNECTED);
  }

  public void setEventHandler(RobotLinkEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  public RobotLinkEventHandler getEventHandler() {
    return eventHandler;
  }
}
