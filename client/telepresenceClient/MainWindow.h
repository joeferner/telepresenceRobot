#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QGraphicsScene>
#include <QTcpSocket>
#include <QTime>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
  Q_OBJECT

public:
  explicit MainWindow(QWidget *parent = 0);
  ~MainWindow();

protected:
  bool eventFilter(QObject *obj, QEvent *event);
  void updateJoystick();

private:
  Ui::MainWindow *ui;
  QGraphicsScene *scene;
  QTcpSocket *socket;
  double mouseSpeed;
  double mouseAngle;
  int joystickTravel;
  int joystickCenter;
  QTime lastSendTime;
  bool mouseDown;

private slots:
  void updatePos();
  void readSocketData();

  void on_tiltSlider_sliderMoved(int position);
  void on_connect_clicked();
};

#endif // MAINWINDOW_H
