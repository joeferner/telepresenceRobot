package com.telepresenceRobot.android;

/**
 * Created by andrew on 8/10/13.
 */
public class SpeedTryTwo implements Speed {
    private double leftSpeed;
    private double rightSpeed;

    // adjust to change how much wiggle room there is n the joystick when going straight
    // forward,back, or spinning
    private static final double DEAD_ZONE = Math.PI/32;

    // adjust lower to slow down the in place spin
    private static final double SPIN_SPEED = 1.0;

    // do not adjust, convenience constant to make math look better
    private static final double HALF_PI = Math.PI/2.0;

    private static final double CURVE_SCALE = 1.0/(HALF_PI-DEAD_ZONE-DEAD_ZONE);

    public SpeedTryTwo(double angleRad, double power) {
        if(angleRad < -Math.PI) {
            angleRad = -Math.PI;
        }
        if (angleRad > Math.PI) {
            angleRad = Math.PI;
        }

        if(angleRad < DEAD_ZONE && angleRad > -DEAD_ZONE) { // straight forward
            leftSpeed = 1.0;
            rightSpeed = 1.0;
        } else if( Math.abs(angleRad) > Math.PI-DEAD_ZONE) { // straight back
            leftSpeed = -1.0;
            rightSpeed = -1.0;
        } else if( angleRad < -(HALF_PI-DEAD_ZONE) && angleRad > -(HALF_PI+DEAD_ZONE)) { // spin left
            leftSpeed = -SPIN_SPEED;
            rightSpeed = SPIN_SPEED;
        } else if( angleRad > HALF_PI-DEAD_ZONE && angleRad < HALF_PI+DEAD_ZONE) { // spin right
            leftSpeed = SPIN_SPEED;
            rightSpeed = -SPIN_SPEED;
        } else if( angleRad < 0.0) { // curve left
            if( angleRad > -HALF_PI) { // forward
                leftSpeed = (angleRad + HALF_PI-DEAD_ZONE) * CURVE_SCALE;
                rightSpeed = 1.0;
            } else { // back
                leftSpeed = 0.0 - (1.0 - (angleRad + Math.PI-DEAD_ZONE) * CURVE_SCALE);
                rightSpeed = -1.0;
            }
        } else if( angleRad > 0.0) { // curve right
            if( angleRad < HALF_PI) { // forward
                leftSpeed = 1.0;
                rightSpeed = 1.0 - ((angleRad - DEAD_ZONE) * CURVE_SCALE);
            } else { // back
                leftSpeed = -1.0;
                rightSpeed = 0.0 - ((angleRad - HALF_PI-DEAD_ZONE) * CURVE_SCALE);
            }
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
            Speed s = new SpeedTryTwo(r, 1.0);
            System.out.printf("%f | %f | %f\n", r, s.getLeftSpeed(), s.getRightSpeed());
        }
    }
}
