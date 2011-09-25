#!/bin/sh
java -cp "tools/buildlib/*:lib/*" -Xmx256m org.apache.tools.ant.launch.Launcher $@
