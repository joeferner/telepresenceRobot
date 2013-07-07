'use strict';

module.exports = function(app) {
  app.get('/', app.withRedisClient, getServerList);
  app.socketioMessage('server/create', createServer);

  function getServerList(req, res) {
    return req.redisClient.smembers('telepresenceRobot:servers', function(err, servers) {
      if (err) {
        console.error('could not get server list', err);
        return next(err);
      }
      return res.render('server/list.ejs', {
        title: 'Server List',
        layout: 'layout.ejs',
        servers: servers
      });
    });
  }

  function createServer(socket, msg, next) {
    app.redisClient.sadd('telepresenceRobot:servers', msg.options.name, function(err, count) {
      if (err) {
        console.error('could not add server', err);
        return next(err);
      }
      if (count == 0) {
        err = new Error('Name already taken.');
        console.error('could not add server.', err);
        return next(err);
      }
      socket.emit('message', {
        command: 'server/created',
        options: {
          name: msg.options.name
        }
      });
    });
  }
};
