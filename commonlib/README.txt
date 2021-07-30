commonlib directory
-------------------
The contents of this directory needs to be copied into tomcat's
common/lib directory, eg, /usr/local/tomcat/common/lib.

This should contain any libraries that are referred in the
context xml, because the context is intialized before our
own WEB-INF/lib is initialized.

commonlib/endorsed directory
---------------------------
The contents of this directory needs to be copied into tomcat's
common/endorsed directory, eg, /usr/local/tomcat/endorsed.
