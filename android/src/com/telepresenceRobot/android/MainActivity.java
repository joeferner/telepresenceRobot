package com.telepresenceRobot.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
  private StringBuilder logBuffer = new StringBuilder();
  private TextView log;
  private Button forward;
  private Button back;
  private Button left;
  private Button right;
  private Button connect;
  private RobotLink robotLink;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    log = (TextView) findViewById(R.id.log);
    connect = (Button) findViewById(R.id.connect);
    forward = (Button) findViewById(R.id.forward);
    back = (Button) findViewById(R.id.back);
    left = (Button) findViewById(R.id.left);
    right = (Button) findViewById(R.id.right);

    robotLink = new RobotLink(this);
    log.setMovementMethod(new ScrollingMovementMethod());

    robotLink.setEventHandler(new RobotLinkEventHandler() {
      @Override
      public void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        log("Robot Link connection status changed: " + connectionStatus);
      }

      @Override
      public void onData(byte[] buffer, int start, int length) {
        log("Read: " + new String(buffer, start, length));
      }
    });

    forward.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.FORWARD));
    back.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.BACK));
    left.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.LEFT));
    right.setOnTouchListener(new MovementOnTouchListener(this, MovementDirection.RIGHT));
    connect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        log("Connecting...");
        robotLink.connect();
      }
    });
  }

  private void log(String line) {
    logBuffer.append(line);
    logBuffer.append("\n");
    this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        log.setText(logBuffer.toString());
        int scrollAmount = log.getLayout().getLineTop(log.getLineCount()) - log.getHeight();
        log.scrollTo(0, scrollAmount);
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    robotLink.resume();
  }

  @Override
  protected void onDestroy() {
    robotLink.destroy();
    super.onDestroy();
  }

  private class MovementOnTouchListener implements View.OnTouchListener {
    private final MainActivity mainActivity;
    private final MovementDirection movementDirection;

    public MovementOnTouchListener(MainActivity mainActivity, MovementDirection movementDirection) {
      this.mainActivity = mainActivity;
      this.movementDirection = movementDirection;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      double speedLeft = 0.0;
      double speedRight = 0.0;
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          switch (movementDirection) {
            case FORWARD:
              speedLeft = speedRight = 1.0;
              break;
            case BACK:
              speedLeft = speedRight = -1.0;
              break;
            case LEFT:
              speedLeft = -1.0;
              speedRight = 1.0;
              break;
            case RIGHT:
              speedLeft = 1.0;
              speedRight = -1.0;
              break;
          }
          break;
        case MotionEvent.ACTION_UP:
          speedLeft = speedRight = 0.0;
          break;
        default:
          return true;
      }
      log("Setting speed " + speedLeft + ", " + speedRight);
      robotLink.setSpeed(speedLeft, speedRight);
      return true;
    }
  }

}
