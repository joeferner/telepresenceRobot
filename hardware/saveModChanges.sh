#!/bin/bash

~/dev/node-kicad-tools/bin/kicad-split.js -i telepresenceRobotNew.mod -o ~/dev/kicad-library/mods/
./updateModFromLibrary.sh
