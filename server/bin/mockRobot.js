#!/usr/bin/env node
'use strict';

var http = require('http');
var optimist = require('optimist');
var socketio = require('socket.io-client');

var args = optimist
  .alias('h', 'help')
  .alias('h', '?')
  .options('name', {
    alias: 'n',
    describe: 'Name of robot.'
  })
  .options('url', {
    alias: 'u',
    default: 'http://localhost',
    describe: 'URL of server.'
  })
  .argv;

if (!args.name) {
  console.error("name is required");
  return process.exit(-1);
}

start(args);

function start(options) {
  options = options || {};
  options.url = options.url || 'http://localhost';

  var socket = socketio.connect(options.url);
  socket.on('connect', function() {
    console.log("socket connected");
    socket.emit('message', {
      command: "server/create",
      options: {
        name: options.name
      }
    });
  });
  socket.on('disconnect', function() {
    console.log("socket disconnect");
  });
  socket.on('message', function(message) {
    switch (message.command) {
    case 'server/created':
      console.log('Server created. Name: ' + message.options.name);
      break;
    case 'server/send':
      console.log('recv: ' + message.options.command);
      break;
    case 'error':
      console.error('Error:', message.options.message);
      break;
    default:
      console.log(message);
      break;
    }
  });
}
