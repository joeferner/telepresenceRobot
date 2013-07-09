#!/usr/bin/env node
'use strict';

var optimist = require('optimist');

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

require('../lib/server')(args);
