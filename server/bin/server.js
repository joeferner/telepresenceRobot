#!/usr/bin/env node
'use strict';

var express = require('express');
var http = require('http');
var optimist = require('optimist');
var socketio = require('socket.io');
var redis = require('redis');
var path = require('path');
var partials = require('express-partials');

var args = optimist
  .alias('h', 'help')
  .alias('h', '?')
  .options('port', {
    alias: 'p',
    default: 80,
    describe: 'Port.'
  })
  .argv;

process.on('uncaughtException', function(err) {
  if (err.stack) {
    err = err.stack;
  }
  console.error('Caught exception:', err);
});

start(args);

function start(options) {
  options = options || {};
  options.port = options.port || 80;
  options.redisPort = options.redisPort || 6379;
  options.redisHost = options.redisHost || 'localhost';

  var app = express();
  app.redisClient = redis.createClient(options.redisPort, options.redisHost);
  app.redisClient.on('error', function(err) {
    console.error('redis error', err);
  });
  app.redisClient.del('telepresenceRobot:servers');

  app.socketioHandlers = {};
  app.set('views', path.join(__dirname, '../web/views'));
  app.set('view engine', 'ejs');
  app.use(partials());
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.query());
  app.use(app.router);
  app.use(express.static(path.join(__dirname, '../web/public')));
  app.socketioMessage = function(cmd, handler) {
    app.socketioHandlers[cmd] = handler;
  };
  app.fireSocketioMessage = function(cmd, socket, msg) {
    var handler = app.socketioHandlers[cmd];
    if (handler) {
      return handler(socket, msg, function(err) {
        if (err) {
          socket.emit('message', {
            command: 'error',
            options: {
              message: err.message
            }
          });
        }
      });
    }
    console.error("Could not find handler for command:", cmd);
    socket.emit('message', {
      command: 'error',
      options: {
        message: 'invalid command: ' + cmd
      }
    });
  };
  app.withRedisClient = function(req, res, next) {
    req.redisClient = app.redisClient;
    return next();
  };
  require('../lib/routes')(app);

  var server = http.createServer(app);
  app.sockets = {};
  app.io = socketio.listen(server);
  app.io.sockets.on('connection', function(socket) {
    app.sockets[socket.id] = socket;
    console.log('socket.io connection:', socket.id);
    socket.on('message', function(msg) {
      app.fireSocketioMessage(msg.command, socket, msg);
    });
    socket.on('disconnect', function() {
      delete app.sockets[socket.id];
      console.log('socket.io disconnect');
    })
  });

  server.listen(options.port);
}
