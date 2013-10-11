package com.telepresenceRobot.android.robot;

public class Speed {
    private final double leftSpeed;
    private final double rightSpeed;

    public Speed(double leftSpeed, double rightSpeed) {
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
    }

    public double getLeftSpeed() {
        return leftSpeed;
    }

    public double getRightSpeed() {
        return rightSpeed;
    }
}
