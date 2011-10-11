Warning
-----------------------

This project is no longer maintained since October 2011.
I decided to leave 'one big Swing desktop application' idea and make Android application first.
This project was splited into smaller pieces which can be found in my repository.
It is possible that this application will be actually created in a few years, but it is not going to be this repository.

Overview
-----------------------

Asterope is an astronomical application focused on advanced amateur astronomers.
When finished it will provide skycharting, ephemeris, observation planning,
telescope control and image manipulation.

This app is written in Scala and Java. Asterope is implemented as set of libraries and client Swing GUI. 
There are also plans for WebUI and Android app. 

Asterope is not 'just another planetarium'. We take astronomy very seriously and some code from Asterope
is actually used by 'big guys'. 


Build
-----------------------

You need an Linux, Windows or MacOSX machine with **JRE 7** (JRE6 wont work) to build Asterope from sources.
Source distribution already contains everything you need for build 
(including compiler, Ant build system, IDE project files and data). There is no reason to install JDK or Ant.

To invoke Ant use batch file 'ant.bat' on Windows or './ant.sh' on Unix like systems.
Asterope uses embedded Ant, so you should not use one installed on your system.

First step is to compile star and  deep-sky database. This may take 20 to 60 minutes, but needs to be performed only once.
Asterope sources contains all data, you just need to run Ant target:

```
    ant.bat compiledb          (on Windows)
    ./ant.sh compiledb         (on Linux or MacOSX)
```

Second step is to run unit tests. It may download some data from internet, but data are cached locally. 
To run tests:

```
    ant.bat test         (on Windows)
    ./ant.sh test        (on Linux or MacOSX)
```

Test reports are saved in:
```
    build/testReport/index.html
```

Asterope is distributed in two zip archives: sources and compiled. You may build distribution archives with this command:
```
    ./ant.sh dist
```
You may also run Astreope GUI directly from Ant. Use 'main' target:
```
    ./ant.sh main
```

IDE
-----------------------
Git repository contains basic Intellij Idea project files. To import Asterope as your project:

 - Open Asterope folder as new project
 - Set SDK in Project Settings / Project / Project SDK
 - Set resource pattern under File / Settings / Compiler / Resource patterns / enter *.*
 - Project is configured to use Scala Fast Compiler daemon, so start it from Ant:
```
    ./ant.sh fsc
```
 -  Hit Recompile and try to run tests

Screenshots
-----------------------
![M45](https://github.com/jankotek/asterope-scala-legacy/raw/master/tools/artwork/m45-screenshot.jpeg)

