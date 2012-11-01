JavaSimon Tomcat Support
========================

Description
-----------
Integrates [JavaSimon](http://javasimon.googlecode.com) with [Tomcat](http://tomcat.apache.org) and allows to monitor an application response
time without modifying it.

Installation
------------
Download and copy the following jars in Tomcat's `lib` folder

- javasimon-core
- javasimon-tomcat
- javasimon-javaee for HTTP Valve
- javasimon-jdbc4  for JDBC Interceptor

Configure everything in Tomcat's `conf/server.xml` configuration file

### Lifecycle listener
This Tomcat lifecycle listener is in charge of initilizing Simon, it can

- register system callbacks (like the JMX one) and initialize Simon
- disable Simon monitoring


	<Listener className="org.javasimon.tomcat.SimonListener"
		callbacks="org.javasimon.jmx.JmxRegisterCallback,org.javasimon.utils.SLF4JLoggingCallback"
		enabled="true" />

### Valve
This Tomcat valve aims at monitoring HTTP Requests response times. It's very similar to JavaSimon's servlet filter
(for that reason javasimon-javaee.jar is required).

	<Valve className="org.javasimon.tomcat.SimonValve"
		prefix="valve"/>

### JDBC Interceptor
This Tomcat JDBC Interceptor aims at monitoring SQL Requests response times. It's very similar to JavaSimon's datasource
wrapper ((for that reason javasimon-jdbc4.jar is required) and Tomcat's SlowQueryReportJmx interceptor. This feature requires at least Tomcat 7.

	<Resource name="jdbc/MyDataSource" auth="Container" type="javax.sql.DataSource"
		jdbcInterceptors="ConnectionState;StatementFinalizer;SlowQueryReportJmx(threshold=1000);org.javasimon.tomcat.SimonJdbcInterceptor(prefix=jdbc)"
		jmxEnabled="true"
		removeAbandoned="true" removeAbandonedTimeout="60" logAbandoned="true"
		...
