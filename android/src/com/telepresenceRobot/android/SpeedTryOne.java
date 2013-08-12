package com.telepresenceRobot.android;

/**
 * @author Joe Ferner
 */
public class SpeedTryOne implements Speed {
    private double leftSpeed;
    private double rightSpeed;

    public SpeedTryOne(double angleRad, double power) {
        while (angleRad < Math.PI) {
            angleRad += 2 * Math.PI;
        }
        while (angleRad > Math.PI) {
            angleRad -= 2 * Math.PI;
        }
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
        leftSpeed = leftSpeed * power;
        rightSpeed = rightSpeed * power;
    }
    public double getLeftSpeed() {
        return this.leftSpeed;
    }
    public double getRightSpeed() {
        return this.rightSpeed;
    }

    public static final void main(String[] args) {
        for(double r = -Math.PI; r < Math.PI; r += Math.PI/64) {
            Speed s = new SpeedTryOne(r, 1.0);
            System.out.printf("%f | %f | %f\n", r, s.getLeftSpeed(), s.getRightSpeed());
        }
    }
}