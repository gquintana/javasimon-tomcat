package org.javasimon.tomcat;

import java.io.IOException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.source.MonitorSource;
import org.javasimon.source.StopwatchTemplate;

/**
 * Tomcat HTTP Valve.
 * Monitor HTTP request durations.
 *
 * @author gquintana
 */
public class SimonValve extends ValveBase {
    /**
     * The descriptive information about this implementation.
     */
    private static final String INFO =
            "org.javasimon.tomcat.SimonValve/3.2";
    /**
     * Stopwatch usage template
     */
    private StopwatchTemplate<Request> stopwatchTemplate = new StopwatchTemplate<Request>(new TomcatRequestStopwatchSource());

    /**
     * Change default stopwatch source {@see TomcatRequestStopwatchSource}.
     *
     * @param stopwatchSource Stopwatch source
     */
    public void setStopwatchSource(MonitorSource<Request, Stopwatch> stopwatchSource) {
        stopwatchTemplate = new StopwatchTemplate<Request>(stopwatchSource);
    }

    /**
     * Valve main method
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, javax.servlet.ServletException {
        Split split = stopwatchTemplate.start(request);
        try {
            getNext().invoke(request, response);
        } finally {
            stopwatchTemplate.stop(split);
        }
    }

    @Override
    public String getInfo() {
        return INFO;
    }
}
