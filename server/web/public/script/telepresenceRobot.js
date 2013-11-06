$(function() {
  "use strict";

  var status = $('#status');
  var joystick = $('#joystick');
  var joystickSize = 200;
  var joystickStickSize = 10;
  var socket = $.atmosphere;
  var transport = 'websocket';
  var id = Date.now();

  var request = {
    url: document.location.protocol + "//" + document.location.host + '/',
    contentType: 'application/json',
    logLevel: 'debug',
    transport: transport,
    enableProtocol: false,
    fallbackTransport: 'long-polling'
  };

  request.onOpen = function(response) {
    status.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
    transport = response.transport;

    push({
      type: 'setId',
      id: id
    });
  };

  request.onTransportFailure = function(errorMsg, request) {
    jQuery.atmosphere.info(errorMsg);
    if (window.EventSource) {
      request.fallbackTransport = "sse";
    }
    status.html($('<h3>', { text: 'Default transport is WebSocket, fallback is ' + request.fallbackTransport }));
  };

  request.onMessage = function(response) {
    var data = JSON.parse(response.responseBody);
    console.log(data);
  };

  request.onClose = function(response) {
    console.log('onClose', response);
  };

  request.onError = function(response) {
    console.log('onError', response);
    status.html($('<p>', { text: 'Sorry, but there\'s some problem with your socket or the server is down' }));
  };

  var subSocket = socket.subscribe(request);

  var joystickResetPositionTimer = null;
  var joystickReportTimer = null;
  var joystickLimitRadius = joystickSize / 2 - 10;
  var joystickLastFireEvent = Date.now();
  var paper = new Raphael(joystick.get(0), joystickSize, joystickSize);
  paper.circle(joystickSize / 2, joystickSize / 2, joystickLimitRadius);
  var joystickStick = paper.circle(joystickSize / 2, joystickSize / 2, joystickStickSize);
  joystickStick.attr({fill: "red"});
  joystickStick.node.onmouseover = function() {
    this.style.cursor = 'crosshair';
  };
  joystickStick.drag(onJoystickMove, onJoystickMoveStart, onJoystickMoveEnd)

  $("#tilt").slider({
    orientation: "vertical",
    range: "min",
    min: 0,
    max: 100,
    value: 50,
    slide: function( event, ui ) {
      onTiltChange(ui.value);
    }
  });
  onTiltChange($("#tilt").slider("value"));

  function onTiltChange(val) {
    $("#tilt-amount").html(val);
    push({
      type: 'setTilt',
      tilt: val / 100.0
    });
  }

  function onJoystickMove(dx, dy) {
    var newX = this.startPos.x + dx;
    var newY = this.startPos.y + dy;
    var center = joystickSize / 2;
    var dxCenter = newX - center;
    var dyCenter = newY - center;
    var distanceFromCenter = Math.sqrt(dxCenter * dxCenter + dyCenter * dyCenter);
    if (distanceFromCenter > joystickLimitRadius) {
      var scale = joystickLimitRadius / distanceFromCenter;
      newX = ((newX - center) * scale) + center;
      newY = ((newY - center) * scale) + center;
    }
    this.attr({cx: newX, cy: newY});
    fireJoystickEvent();
  }

  function onJoystickMoveStart() {
    this.startPos = {
      x: this.attr("cx"),
      y: this.attr("cy")
    };
    this.animate({r: joystickStickSize + 5, opacity: .25}, 500, ">");
    joystickReportTimer = setInterval(fireJoystickEvent, 1000);
  }

  function onJoystickMoveEnd() {
    this.animate({r: joystickStickSize, opacity: 1.0}, 500, ">");
    joystickResetPositionTimer = setInterval(onJoystickResetPositionTimer, 10);
    clearInterval(joystickReportTimer);
    joystickReportTimer = null;
  }

  function onJoystickResetPositionTimer() {
    var stickPos = {
      x: joystickStick.attr("cx"),
      y: joystickStick.attr("cy")
    };
    var targetPos = {
      x: joystickSize / 2,
      y: joystickSize / 2
    };

    var dx = targetPos.x - stickPos.x;
    var dy = targetPos.y - stickPos.y;
    var len = Math.sqrt(dx * dx + dy * dy);
    if (len < (joystickSize / 100)) {
      joystickStick.attr({cx: joystickSize / 2, cy: joystickSize / 2});
      clearInterval(joystickResetPositionTimer);
      joystickResetPositionTimer = null;
      fireJoystickEvent();
      setTimeout(fireJoystickEvent, 100);
      return;
    }

    var scale = (joystickSize / 100) / len;
    joystickStick.attr({cx: stickPos.x + (dx * scale), cy: stickPos.y + (dy * scale)});
    fireJoystickEvent();
  }

  function fireJoystickEvent() {
    if (Date.now() - joystickLastFireEvent < 100) {
      return;
    }
    var center = joystickSize / 2;
    var pos = {
      x: joystickStick.attr("cx") - center,
      y: center - joystickStick.attr("cy")
    };
    var power = Math.sqrt(pos.x * pos.x + pos.y * pos.y) / joystickLimitRadius;

    // 0 radians = forward
    // pi radians = back
    // negative angle is anything left of center
    // positive angle is anything right of center
    var angle = Math.atan2(pos.x, pos.y);
    push({
      type: 'setSpeedPolar',
      power: power,
      angle: angle
    });
    joystickLastFireEvent = Date.now();
  }

  function push(json) {
    console.log('pushing', json);
    subSocket.push(JSON.stringify(json));
  }
});
