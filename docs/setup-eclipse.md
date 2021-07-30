[Back to Development Guide](devguide_toc.md)

## Eclipse IDE Setup

1. Create new files called instahms.launch and instaapps.launch in insta/.externalToolBuilders.
1. Copy the contents of instahms.launch.prototype to instahms.launch and instaaaps.launch.prototype to instaapps.launch.
2. In eclipse: Click on Project → Properties → Builders → Select instahms checkbox → Edit → Click on JRE Tab → Select separate JRE → Select JRE 1.7
→ Click on Environment Tab → Add JAVA_HOME and TOMCAT_HOME variables with their values.

Refer to the screenshots on this page: [Setting up repository in eclipse](https://practo.atlassian.net/wiki/display/HIMS/Setting+up+merged+repository+of+hms+and+apps+in+eclipse)
