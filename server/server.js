"use strict";

var optimist = require('optimist');
var webSocketServer = require('websocket').server;
var http = require('http');
var url = require('url');
var st = require('node-static');
var path = require('path');
var fileServer = new st.Server(path.join(__dirname, 'web/public'));

var args = optimist
  .alias('h', 'help')
  .alias('h', '?')
  .options('port', {
    alias: 'p',
    default: 80,
    describe: 'Port.'
  })
  .argv;

if (args.help) {
  optimist.showHelp();
  return process.exit(-1);
}

var clients = {};

var server = http.createServer(function(request, response) {
  request.addListener('end', function() {
    fileServer.serve(request, response);
  });
});
server.listen(args.port, function() {
  console.log('Server is listening: http://localhost:' + args.port);
});

var wsServer = new webSocketServer({
  httpServer: server
});

wsServer.on('request', function(request) {
  console.log('Connection from origin ' + request.origin + '.');

  var connection = request.accept(null, request.origin);
  var id = null;

  console.log('Connection accepted.');

  connection.on('message', function(message) {
    if (message.type === 'utf8') {
      var data = JSON.parse(message.utf8Data);
      if (data.type == 'setId') {
        id = data.id;
        clients[data.id] = connection;
        console.log('Client with id registered:', data.id);
      } else {
        console.log('broadcasting:', data);
        broadcast(data, id);
      }
    }
  });

  connection.on('close', function(connection) {
    console.log('Peer ' + id + ' disconnected.');
    delete clients[id];
  });
});

function broadcast(data, excludeId) {
  for(var clientId in clients) {
    if(clientId == excludeId) {
      continue;
    }
    var client = clients[clientId];
    client.sendUTF(JSON.stringify(data));
  }
}
