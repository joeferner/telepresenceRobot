#ifndef JOYSTICKWIDGET_H
#define JOYSTICKWIDGET_H

#include <QGraphicsView>

class JoystickWidget : public QGraphicsView
{
  Q_OBJECT
public:
  explicit JoystickWidget(QWidget *parent = 0);

protected:
  virtual void mousePressEvent(QGraphicsSceneMouseEvent* evt) Q_DECL_OVERRIDE;
  virtual void mouseMoveEvent(QGraphicsSceneMouseEvent* evt) Q_DECL_OVERRIDE;
  virtual void mouseReleaseEvent(QGraphicsSceneMouseEvent* evt) Q_DECL_OVERRIDE;

signals:

public slots:

};

#endif // JOYSTICKWIDGET_H
