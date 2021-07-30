## Setting up IntelliJ Community Edition on Mac OS

### Steps:

1. Download from [here](https://www.jetbrains.com/idea/download/#section=mac)
2. Add Project using Maven and configure compilter as per project (Java1.7 or Java 1.8 for Insta 12.4.2 and above)

#### Configuring Tomcat on IntelliJ CE
IntelliJ CE is the free version and doesn't support Tomcat Plugin from its marketplace.

To debug Java code, you need to start Tomcat in debug mode.
Command:

```sh
$TOMCAT_HOME/bin/catalina.sh jpda start
```

Default address for JPDA mode is `localhost:8000`

If multiple tomcats on local environment

Edit `$TOMCAT_HOME/bin/setenv.sh`
And add property

```sh
JPDA_ADDRESS=localhost:xxxx
```

Add/Edit the same configuration in Intellij.
Need to add Remote Configuration (Name it as tomcat)
Port is defaulted to 8000.

Once tomcat is successfully started, start debug mode of IntelliJ.
