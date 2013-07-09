'use strict';

var express = require('express');
var http = require('http');
var socketio = require('socket.io');
var path = require('path');
var partials = require('express-partials');

module.exports = function(options) {
  options = options || {};
  options.port = options.port || 80;

  var app = express();

  app.socketioHandlers = {};
  app.set('views', path.join(__dirname, '../web/views'));
  app.set('view engine', 'ejs');
  app.use(partials());
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.query());
  app.use(app.router);
  app.use(express.static(path.join(__dirname, '../web/public')));

  app.get('/', getHomepage);

  app.sendCommandToServer = sendCommandToServer.bind(null, app);

  var server = http.createServer(app);
  app.sockets = {};
  app.io = socketio.listen(server);
  app.io.sockets.on('connection', function(socket) {
    app.sockets[socket.id] = socket;
    console.log('socket.io connection:', socket.id);
    socket.on('message', onSocketioMessage.bind(null, app, socket));
    socket.on('disconnect', function() {
      delete app.sockets[socket.id];
      console.log('socket.io disconnect');
    })
  });

  server.listen(options.port);
};

function getHomepage(req, res, next) {
  return res.render('home/home.ejs', {
    title: 'Home',
    layout: 'layout.ejs'
  });
}

function sendCommandToServer(app, cmd) {
  var serverSocket = app.io.sockets.sockets[app.serverSocketioId];
  if (!serverSocket) {
    throw new Error('No server connected');
  }
  console.log('sending command to server', cmd);
  serverSocket.emit('message', {
    command: 'command',
    options: {
      command: cmd
    }
  });
}

function onSocketioMessage(app, src, msg) {
  switch (msg.command) {
  case 'createServer':
    return onCreateServer(app, src, msg);
  case 'send':
    return onSend(app, src, msg);
  default:
    console.log('unhandled command', src.id, msg);
    break;
  }
}

function onCreateServer(app, src, msg) {
  app.serverSocketioId = src.id;
  console.log('create server: ' + app.serverSocketioId);
}

function onSend(app, src, msg) {
  var cmd = msg.options.command;
  console.log('send', src.id, cmd);
  app.sendCommandToServer(cmd);
}