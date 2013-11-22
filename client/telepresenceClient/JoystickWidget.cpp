#include "JoystickWidget.h"
#include "MainWindow.h"
#include <QStatusBar>
#include <QDebug>

JoystickWidget::JoystickWidget(QWidget *parent) :
  QGraphicsView(parent)
{
}

void JoystickWidget::mousePressEvent(QGraphicsSceneMouseEvent* evt) {
  qDebug() << "mousePressEvent";
  ((MainWindow*)parent())->statusBar()->showMessage("mousePressEvent");
}

void JoystickWidget::mouseMoveEvent(QGraphicsSceneMouseEvent* evt) {
  qDebug() << "mouseMoveEvent";
}

void JoystickWidget::mouseReleaseEvent(QGraphicsSceneMouseEvent* evt) {
  qDebug() << "mouseReleaseEvent";
}
