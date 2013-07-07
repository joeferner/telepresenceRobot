'use strict';

module.exports = function(app) {
  app.get('/server/:id', app.withRedisClient, getServer);
  app.socketioMessage('server/create', createServer);
  app.socketioMessage('server/connect', connectToServer);
  app.socketioMessage('server/send', send);
  app.get('/', app.withRedisClient, getServerList);
  app.get('/server', app.withRedisClient, getServerList);

  function getServerList(req, res) {
    return req.redisClient.hgetall('telepresenceRobot:servers', function(err, servers) {
      if (err) {
        console.error('could not get server list', err);
        return next(err);
      }

      servers = Object.keys(servers).map(function(serverId) {
        return JSON.parse(servers[serverId]);
      });

      return res.render('server/list.ejs', {
        title: 'Server List',
        layout: 'layout.ejs',
        servers: servers
      });
    });
  }

  function getServer(req, res) {
    var serverId = req.params.id;
    return req.redisClient.hget('telepresenceRobot:servers', serverId, function(err, server) {
      if (err) {
        console.error('could not get server', err);
        return next(err);
      }
      server = JSON.parse(server);
      return res.render('server/details.ejs', {
        title: 'Server: ' + server.name,
        layout: 'layout.ejs',
        server: server
      });
    });
  }

  function createServer(socket, msg, next) {
    var data = {
      name: msg.options.name,
      id: msg.options.name,
      serverSocketId: socket.id,
      clientSocketIds: []
    };
    app.redisClient.hset('telepresenceRobot:servers', data.id, JSON.stringify(data), function(err) {
      if (err) {
        console.error('could not add server', err);
        return next(err);
      }
      socket.emit('message', {
        command: 'server/created',
        options: {
          id: data.id,
          name: data.name
        }
      });
    });
  }

  function connectToServer(socket, msg, next) {
    var serverId = msg.options.id;
    return app.redisClient.hget('telepresenceRobot:servers', serverId, function(err, server) {
      if (err) {
        console.error('could not get server', err);
        return next(err);
      }
      server = JSON.parse(server);
      server.clientSocketIds.push(socket.id);
      app.redisClient.hset('telepresenceRobot:servers', serverId, JSON.stringify(server), function(err) {
        if (err) {
          console.error('could not save server', err);
          return next(err);
        }
        socket.emit('message', {
          command: 'server/connected',
          options: {
            id: server.id
          }
        });
      });
    });
  }

  function send(socket, msg, next) {
    var serverId = msg.options.id;
    return app.redisClient.hget('telepresenceRobot:servers', serverId, function(err, server) {
      if (err) {
        console.error('could not get server', err);
        return next(err);
      }
      server = JSON.parse(server);

      // from server
      if (server.serverSocketId == socket.id) {

      }

      // to server
      else {
        var s = app.sockets[server.serverSocketId];
        if (!s) {
          err = new Error('Could not find server socket');
          return next(err);
        }
        s.emit('message', msg);
      }
    });
  }
};
