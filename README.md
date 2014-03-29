terminal-recall
===============

An unofficial engine remake for Terminal Velocity and Fury3 written in Java. It aims to accept POD files for the original games (or third-party creations) and play them with updated graphics and sound, similar to the way Spring RTS enhances the Total Annihilation game. This will also be an experimenting ground for fairly-radical rendering and processing methods.

See files also: CREDITS, COPYING, LICENSE*

Terminal Recall (TRCL) is part of the Java Terminal Reality File Parsers project (jTRFP.org).

TRCL is currently in a playable development phase. Because it is not yet close to being released (it changes too much), you must build the program from source. Fortunately we put some effort into making this possible without you having to have a computer science degree.

##Build Instructions
You will need:
* Java JDK 6 or later (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* Git (http://git-scm.com/downloads)
* Maven (https://maven.apache.org/download.cgi)
* Video card (and drivers) capable of OpenGL 3.3 (post-2009 roughly speaking).
* Windows or Linux. (maybe Mac too if they get caught up)

In a folder/directory of your choice run:
```
git clone https://github.com/jtrfp/terminal-recall.git
```
... this will create a directory called terminal-recall and download the project to that directory.
```
    cd terminal-recall
    mvn clean install
```

... you will see lots of fireworks as it builds. (it doesn't really 'install' anything)
You should see BUILD SUCCESS. If you see BUILD FAILURE come to the Terminal Recall gitHub issues section (https://github.com/jtrfp/terminal-recall/issues?page=1), file a new issue and post the output and we'll hopefully figure it out.
```
    cd target
    java -jar RunMe.jar [path_to_POD_file0] [path_to_POD_file1] [...] [level_name.LVL]
```

For example:
```
java -jar RunMe.jar "/home/chuck/pods/STARTUP.POD" "/home/chuck/pods/FURY3.POD" "BORG3.LVL"
```
... to run the final level in Fury3.

If its been a few weeks since you first downloaded and you want to update to the newest version you can cd into your terminal-recall directory and run:

```
    git pull
    mvn clean install
```

... this will only download whatever has changed since your last download and then rebuild it all.

If the dash in the terminal-recall name is much of a pain in Windows it can be renamed without issue after downloading.

If you wish to move the RunMe.jar file to another location, be sure to also move the 'lib' directory. All other files are unneeded for running.


##Disclaimer

This project and its contributors are not affiliated with nor represent Microsoft, Apogee Software Ltd. (3DRealms) or Terminal Reality Inc. Terminal Recall is not supported by any of the aforementioned. Terminal Velocity, and their original content are property of Terminal Reality Inc. Always support the original content creators when possible.

##Trademarks

Microsoft is a registered trademark of the Microsoft Corporation. Fury3 was a registered trademark of the Microsoft Corporation, canceled in 2003. Terminal Reality and Terminal Velocity are registered trademarks of Terminal Reality Inc. 3DRealms is a registered trademark of Apogee Software Ltd.
