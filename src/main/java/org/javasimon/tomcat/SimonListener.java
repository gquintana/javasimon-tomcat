package org.javasimon.tomcat;

import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.javasimon.SimonManager;
import org.javasimon.callback.Callback;
import org.javasimon.jmx.JmxRegisterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomcat lifecycle listener initializes Simon manager:<ul> 
 * <li>Enable/disable Simon manager</li> 
 * <li>Register callbacks</li> 
 * </ul>
 *
 * @author gquintana
 */
public class SimonListener implements LifecycleListener {
	/**
	 * Logger
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(SimonListener.class);
	/**
	 * Flag indicating whether Simon manager should be enabled or disabled. 
	 * null means leave as default
	 */
	private Boolean enabled;
	/**
	 * Comma separated list of class names implementing the {@link Callback}
	 * interface
	 */
	private String callbacks;
	/**
	 * List of callbacks registered by this listener
	 */
	private List<Callback> addedCallbacks;

	private Callback logCallbackException(String className,Exception e) {
		LOGGER.error("Callback "+className+" instantiation failed", e);
		return null;
	}
	private Callback createCallback(String callbackName) {
		try {
			return (Callback) Class.forName(callbackName).newInstance();
		} catch (ClassCastException classCastException) {
			return logCallbackException(callbackName, classCastException);
		} catch (ClassNotFoundException classNotFoundException) {
			return logCallbackException(callbackName, classNotFoundException);
		} catch (InstantiationException instantiationException) {
			return logCallbackException(callbackName, instantiationException);
		} catch (IllegalAccessException illegalAccessException) {
			return logCallbackException(callbackName, illegalAccessException);
		}
	}
	/**
	 * Register callbacks (if any)
	 */
	private void registerCallbacks() {
		if (callbacks != null) {
			String[] callbackNames = callbacks.split(",");
			addedCallbacks = new ArrayList<Callback>(callbackNames.length);
			for (String callbackName : callbackNames) {
				callbackName = callbackName.trim();
				if (!callbackName.isEmpty()) {
					Callback callback;
					if (callbackName.equals(JmxRegisterCallback.class.getName())) {
						callback=new JmxRegisterCallback("org.javasimon");
					} else {
						callback=createCallback(callbackName);
					}
					if (callback!=null) {
						SimonManager.callback().addCallback(callback);
						addedCallbacks.add(callback);
						LOGGER.info("Simon Callback "+callbackName+" registered");						
					}
				}
			}
		}
	}

	/**
	 * Unegister callbacks (if any)
	 */
	private void unregisterCallbacks() {
		if (addedCallbacks != null) {
			for (Callback callback : addedCallbacks) {
				SimonManager.callback().removeCallback(callback);
			}
			addedCallbacks = null;
		}
	}

	/**
	 * Listener main method
	 */
	public void lifecycleEvent(LifecycleEvent event) {
		if (event.getType().equals(Lifecycle.START_EVENT)) {
			if (enabled != null) {
				if (enabled.booleanValue()) {
					SimonManager.enable();
				} else {
					SimonManager.disable();
				}
			}
			registerCallbacks();
		} else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
			unregisterCallbacks();
		}
	}

	public String getCallbacks() {
		return callbacks;
	}

	public void setCallbacks(String callbacks) {
		// Trim to null
		if (callbacks!=null) {
			callbacks=callbacks.trim();
			if ("".equals(callbacks)) {
				callbacks=null;
			}
		}
		this.callbacks = callbacks;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
}
 