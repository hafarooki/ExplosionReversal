# ExplosionRegen
A lightweight plugin for Minecraft servers which makes blocks destroyed by explosions regenerate.

This plugin is for Paper https://destroystokyo.com/ci/job/Paper-1.13/) 1.13.
To install it, download the latest release and put it in your plugins folder, then restart your server.
Use the config.yml file to edit the settings.

# Commands
The only command is "/regen" to regenerate all pending blocks, requires the explosionregen.regen permission.

# Contributing
Fork the project.
Run these commands (use git bash if you're on Windows) (Requires Maven to be properly installed)
```Bash
$ cd ~/Documents
$ git clone https://github.com/PaperMC/Paper
$ cd Paper
$ ./paper jar
$ cd ../
$ git clone https://github.com/<YOURNAME>/ExplosionRegen
$ cd ExplosionRegen
$ ./gradlew build
```
Import the Gradle project. Using IntelliJ IDEA, create new projects from sources, navigate to the build.gradle, use the setup wizard.

![Image](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of [YourKit .NET Profiler](https://www.yourkit.com/java/profiler/),
innovative and intelligent tools for profiling Java and .NET applications.
