# ExplosionReversal
A lightweight plugin for Minecraft servers which makes blocks destroyed by explosions regenerate.

Download: [Spigot Page](https://www.spigotmc.org/resources/explosionregen.60308/)

This plugin is for [Paper](https;//papermc.io) 1.15.2.
To install it, download the latest release and put it in your plugins folder, then restart your server.
Use the config.yml file to edit the settings.

# Commands
The only command is "/regen" to regenerate all pending blocks.
It requires the `explosionreversal.regen` permission.

# Contributing
Fork the project.
Run these commands (use git bash if you're on Windows) (Requires Maven to be properly installed)
```Shell
$ cd ~/Documents
$ git clone https://github.com/PaperMC/Paper
$ cd Paper
$ ./paper jar
$ cd ../
$ git clone https://github.com/<YOURNAME>/ExplosionReversal
$ cd ExplosionReversal
$ ./gradlew build
```
Import the Gradle project. Using IntelliJ IDEA, create new projects from sources, navigate to the build.gradle, use the setup wizard.

<br/><br/><br/>
![Image](https://www.yourkit.com/images/yklogo.png)

ExplosionReversal uses Yourkit's profiler to enhance its performance.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of [YourKit .NET Profiler](https://www.yourkit.com/java/profiler/),
innovative and intelligent tools for profiling Java and .NET applications.
