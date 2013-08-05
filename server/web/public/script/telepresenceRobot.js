$(function() {
  "use strict";

  var status = $('#status');
  var statusLed = $('#statusLed');
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

    subSocket.push(JSON.stringify({
      type: 'setId',
      id: id
    }));
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
    if(data.type == 'statusLed') {
      var newState = data.newState;
      if(newState) {
        statusLed.attr('checked', 'checked');
      } else {
        statusLed.removeAttr('checked');
      }
    } else {
      console.log(data);
    }
  };

  request.onClose = function(response) {
    console.log('onClose', response);
  };

  request.onError = function(response) {
    console.log('onError', response);
    status.html($('<p>', { text: 'Sorry, but there\'s some problem with your socket or the server is down' }));
  };

  var subSocket = socket.subscribe(request);

  statusLed.click(function() {
    var statusLedState = statusLed.is(':checked');
    subSocket.push(JSON.stringify({
      type: 'statusLed',
      newState: statusLedState
    }));
  })
});
