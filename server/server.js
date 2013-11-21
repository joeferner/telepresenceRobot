"use strict";

var optimist = require('optimist');
var webSocketServer = require('websocket').server;
var http = require('http');
var url = require('url');
var st = require('node-static');
var path = require('path');
var net = require('net');
var Lazy = require('lazy');
var fileServer = new st.Server(path.join(__dirname, 'web/public'));

var args = optimist
  .alias('h', 'help')
  .alias('h', '?')
  .options('webport', {
    default: 8888,
    describe: 'Web Port.'
  })
  .options('robotport', {
    default: 8889,
    describe: 'Robot Port.'
  })
  .argv;

if (args.help) {
  optimist.showHelp();
  return process.exit(-1);
}

var webSocketClients = {};
var robotClients = {};

var webServer = http.createServer(function(request, response) {
  //request.addListener('end', function() {
  fileServer.serve(request, response);
  //});
});
webServer.listen(args.webport, function() {
  console.log('Server is listening: http://localhost:' + args.webport);
});

var wsServer = new webSocketServer({
  httpServer: webServer
});

wsServer.on('request', function(request) {
  console.log('Connection from origin ' + request.origin + '.');

  var connection = request.accept(null, request.origin);
  var id = null;

  console.log('Connection accepted.');

  connection.on('message', function(message) {
    if (message.type === 'utf8') {
      var data = JSON.parse(message.utf8Data);
      var newId = onMessage(data, id);
      if (newId) {
        id = newId;
        webSocketClients[id] = connection;
      }
    }
  });

  connection.on('close', function(connection) {
    console.log('Peer ' + id + ' disconnected.');
    if (id && webSocketClients[id]) {
      delete webSocketClients[id];
    }
  });
});

function broadcast(data, excludeId) {
  var clientId, client;

  for (clientId in webSocketClients) {
    if (clientId == excludeId) {
      continue;
    }
    client = webSocketClients[clientId];
    console.log('broadcasting webSocketClients[' + clientId + ']:', data);
    client.sendUTF(JSON.stringify(data) + "\n");
  }

  for (clientId in robotClients) {
    if (clientId == excludeId) {
      continue;
    }
    client = robotClients[clientId];
    console.log('broadcasting robotClients[' + clientId + ']:', data);
    client.write(JSON.stringify(data) + "\n");
  }
}

var robotServer = net.createServer(function(c) {
  var id = null;
  console.log('robot connected');

  c.on('end', function() {
    console.log('robot disconnected');
    if (id && robotClients[id]) {
      delete robotClients[id];
    }
  });

  Lazy(c)
    .lines
    .map(String)
    .map(function(line) {
      try {
        var json = JSON.parse(line);
        var newId = onMessage(json, id);
        if (newId) {
          id = newId;
          robotClients[id] = c;
        }
      } catch (e) {
        console.error("invalid robot message: " + line);
      }
    });
});
robotServer.listen(args.robotport, function() {
  console.log('robot server listening on port ' + args.robotport);
});

function onMessage(data, excludeId) {
  if (data.type == 'broadcast') {
    // ignore
  } else if (data.type == 'setId') {
    console.log('Client with id registered:', data.id);
    return data.id;
  } else {
    broadcast(data, excludeId);
  }
  return null;
}