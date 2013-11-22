#include "MainWindow.h"
#include "ui_MainWindow.h"
#include <QMouseEvent>
#include <QTimer>
#include <math.h>

#define min(a,b) ( ((a) > (b)) ? (b) : (a) )

MainWindow::MainWindow(QWidget *parent) :
  QMainWindow(parent),
  ui(new Ui::MainWindow)
{
  ui->setupUi(this);

  qApp->installEventFilter(this);

  lastSendTime = QTime::currentTime();

  socket = new QTcpSocket(this);
  connect(socket, SIGNAL(readyRead()), SLOT(readSocketData()));

  scene = new QGraphicsScene();
  scene->setBackgroundBrush(QBrush(Qt::white));
  scene->clear();
  ui->joystick->setScene(scene);
  ui->joystick->show();

  joystickCenter = ui->joystick->width() / 2;
  joystickTravel = joystickCenter - 10;

  mouseSpeed = 0.0;
  mouseAngle = 0.0;
  updateJoystick();

  QTimer *timer = new QTimer(this);
  connect(timer, SIGNAL(timeout()), this, SLOT(updatePos()));
  timer->start(50);
}

MainWindow::~MainWindow()
{
  delete ui;
}

bool MainWindow::eventFilter(QObject *obj, QEvent *event)
{
  if (event->type() == QEvent::MouseMove) {
    QMouseEvent *mouseEvent = static_cast<QMouseEvent*>(event);
    if(mouseEvent->buttons() == Qt::LeftButton && mouseDown) {
      QPoint pos = ui->joystick->mapFromParent(mouseEvent->pos());
      pos.setX(pos.x() - joystickCenter);
      pos.setY(joystickCenter - pos.y());

      mouseSpeed = sqrt(pos.x() * pos.x() + pos.y() * pos.y()) / joystickTravel;
      if(mouseSpeed > 1.0) {
        mouseSpeed = 1.0;
      }
      mouseAngle = atan2(pos.x(), pos.y());

      statusBar()->showMessage(QString("Mouse move (%1,%2)").arg(mouseSpeed).arg(mouseAngle));
      updateJoystick();
    }
  } else if (event->type() == QEvent::MouseButtonPress) {
    QMouseEvent *mouseEvent = static_cast<QMouseEvent*>(event);
    if(mouseEvent->buttons() == Qt::LeftButton && ui->joystick->underMouse()) {
      mouseDown = true;
    }
  } else if (event->type() == QEvent::MouseButtonRelease) {
    mouseDown = false;
  }
  return false;
}

void MainWindow::updateJoystick() {
  QBrush mouseBrush = QBrush(Qt::red);
  QPen mousePen = QPen(QBrush(Qt::black), 1);
  QBrush backgroundBrush = QBrush(Qt::white);
  QPen outsidePen = QPen(QBrush(Qt::black), 1);

  scene->clear();

  int margin = joystickCenter - joystickTravel;
  scene->addEllipse(margin, margin, ui->joystick->width() - margin * 2, ui->joystick->height() - margin * 2, outsidePen, backgroundBrush);

  double s = mouseSpeed * joystickTravel;
  int mouseX = (s * sin(mouseAngle)) + (ui->joystick->width() / 2);
  int mouseY = (ui->joystick->height() / 2) - (s * cos(mouseAngle));
  scene->addEllipse(mouseX - 5, mouseY - 5, 10, 10, mousePen, mouseBrush);

  ui->joystick->show();

  if(lastSendTime.msecsTo(QTime::currentTime()) > 100) {
    if(socket->waitForConnected()) {
      socket->write(QString("{ \"type\": \"setSpeedPolar\", \"power\": %1, \"angle\": %2 }\n").arg(mouseSpeed).arg(mouseAngle).toLocal8Bit());
      lastSendTime = QTime::currentTime();
    }
  }
}

void MainWindow::updatePos() {
  if(QApplication::mouseButtons() == Qt::LeftButton) {
    updateJoystick();
  } else {
    if(mouseSpeed > 0) {
      mouseSpeed -= min(mouseSpeed, 0.1);
    } else {
      mouseSpeed += min(abs(mouseSpeed), 0.1);
    }
    updateJoystick();
  }
}

void MainWindow::on_tiltSlider_sliderMoved(int position)
{
  double pos = ((double)position) / 100.0;
  if(socket->waitForConnected()) {
    socket->write(QString("{ \"type\": \"setTilt\", \"tile\": %1 }\n").arg(pos).toLocal8Bit());
  }
}

void MainWindow::readSocketData() {

}

void MainWindow::on_connect_clicked()
{
  socket->connectToHost(ui->hostname->text(), 8889);
  if(socket->waitForConnected()) {
    socket->write("{\"type\":\"setId\", \"id\":88888}\n");
  }
}
