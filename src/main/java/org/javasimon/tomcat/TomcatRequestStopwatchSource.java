package org.javasimon.tomcat;

import org.apache.catalina.connector.Request;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.javasimon.source.AbstractStopwatchSource;
import org.javasimon.source.MonitorSource;
import org.javasimon.utils.Replacer;
import org.javasimon.javaee.SimonServletFilterUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Stopwatch produced for Tomcat HTTP Request.
 * Very similar to {@link org.javasimon.javaee.HttpStopwatchSource}
 */
public class TomcatRequestStopwatchSource extends AbstractStopwatchSource<Request> {
    /**
     * Simon name prefix
     */
    private String prefix="org.javasimon.tomcat.http";
    /**
     * Replace Unallowed characters by _
     */
    private Replacer unallowedCharacterReplacer = SimonServletFilterUtils.createUnallowedCharsReplacer("_");
    /**
     * Remove JSESSIONID attribute
     */
    private Replacer jsessionParameterReplacer = new Replacer("[;&]?JSESSIONID=[^;?/&]*", "", Replacer.Modificator.IGNORE_CASE);

    /**
     * Constructor with {@link org.javasimon.Manager}.
     *
     * @param manager Simon manager used for producing Stopwatches
     */
    public TomcatRequestStopwatchSource(Manager manager) {
        super(manager);
    }

    public TomcatRequestStopwatchSource() {
        super(SimonManager.manager());
    }

    /**
     * Compute Simon name from HTTP Request URI
     * @param request HTTP Request
     * @return Simon name
     */
    @Override
    protected String getMonitorName(Request request) {
        String monitorName=request.getRequestURI();
        monitorName=jsessionParameterReplacer.process(monitorName);
        monitorName=unallowedCharacterReplacer.process(monitorName);
        if (prefix!=null && !prefix.isEmpty()) {
            monitorName=prefix+Manager.HIERARCHY_DELIMITER+monitorName;
        }
        return monitorName;
    }

    /**
     * Indicates which uri should be monitored. CSS, JS and Images are not monitored by defaut.
     * Override this method to customize filtering.
     * @param request Request
     * @return Monitored or not?
     */
    @Override
    public boolean isMonitored(Request request) {
        String uri = request.getRequestURI().toLowerCase();
        return !(uri.endsWith(".css") || uri.endsWith(".png") || uri.endsWith(".gif") || uri.endsWith(".jpg") || uri.endsWith(".js"));
    }
    //659.50
    /**
     * Get a stopwatch for given HTTP request.
     * @param request HTTP request
     * @return Stopwatch for the HTTP request
     */
    @Override
    public Stopwatch getMonitor(Request request) {
        final Stopwatch stopwatch = super.getMonitor(request);
        if (stopwatch.getNote() == null) {
            stopwatch.setNote(request.getRequestURI());
        }
        return stopwatch;
    }
}