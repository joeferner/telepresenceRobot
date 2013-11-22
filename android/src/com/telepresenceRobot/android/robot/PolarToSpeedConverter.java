package com.telepresenceRobot.android.robot;

public class PolarToSpeedConverter {
    private static final double HALF_PI = Math.PI / 2.0;

    private static final double QUARTER_PI = Math.PI / 4.0;

    public static Speed toSpeed(double angleRad, double power) {
        double leftSpeed, rightSpeed;

        if (angleRad < -Math.PI) {
            angleRad = -Math.PI;
        }
        if (angleRad > Math.PI) {
            angleRad = Math.PI;
        }

        if (angleRad < 0) { // left
            if (angleRad > -HALF_PI) { // top-left quadrant
                leftSpeed = Math.sin((angleRad + QUARTER_PI) * 2.0);
                rightSpeed = 1.0;
            } else { // bottom-left quadrant
                leftSpeed = -1;
                rightSpeed = Math.sin((angleRad - QUARTER_PI) * 2.0);
            }
        } else { // right
            if (angleRad < HALF_PI) { // top-right quadrant
                leftSpeed = 1.0;
                rightSpeed = Math.sin((angleRad + QUARTER_PI) * 2.0);
            } else { // bottom-right quadrant
                leftSpeed = Math.sin((angleRad - QUARTER_PI) * 2.0);
                rightSpeed = -1.0;
            }
        }

        leftSpeed = leftSpeed * Math.max(0, power) * 0.75;
        rightSpeed = rightSpeed * Math.max(0, power) * 0.75;

        return new Speed(leftSpeed, rightSpeed);
    }
}
