#!/bin/sh
java -cp "lib/*" -Xmx512m -splash:lib/splash.jpg  org.asterope.gui.GuiMain $@
