package org.javasimon.tomcat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PoolProperties.InterceptorProperty;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.javasimon.jdbc4.SimonCallableStatement;
import org.javasimon.jdbc4.SimonPreparedStatement;
import org.javasimon.jdbc4.SimonStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.StringContent;

/**
 * Tomcat JDBC connection pool interceptor.
 * Monitors JDBC statement duration. Requires Tomcat 7.
 *
 * @author gquintana
 */
public class SimonJdbcInterceptor extends JdbcInterceptor {
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimonJdbcInterceptor.class);
    /**
     * Simon name prefix
     */
    private String prefix = "org.javasimon.tomcat.sql";
    /**
     * Statement replacers list
     */
    private final AbstractStatementWrapper[] statementWrappers;

    /**
     * Constructor
     */
    public SimonJdbcInterceptor() {
        try {
            statementWrappers = new AbstractStatementWrapper[]{
                    new StatementWrapper(),
                    new PreparedStatementWrapper(),
                    new CallableStatementWrapper()
            };
            LOGGER.info("Simon JDBC interceptor initialized");
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new IllegalStateException("Simon JDBC interceptor failed", noSuchMethodException);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void setProperties(Map<String, InterceptorProperty> properties) {
        super.setProperties(properties);
        InterceptorProperty prefixProperty = properties.get("prefix");
        if (prefixProperty != null) {
            prefix = prefixProperty.getValue();
        }
    }


    @Override
    public void reset(ConnectionPool parent, PooledConnection con) {
        // Do nothing
    }

    /**
     * Interceptor main method
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (proxy instanceof Connection) {
            Connection connection = (Connection) proxy;
            Object result = super.invoke(connection, method, args);
            result = wrapStatement(connection, method, args, result);
            return result;
        } else {
            return super.invoke(proxy, method, args);
        }
    }

    /**
     * Wrap returned statement
     */
    private Object wrapStatement(Connection connection, Method method, Object[] args, Object result) {
        Object newResult = null;
        boolean wrapped = false;
        for (AbstractStatementWrapper statementWrapper : statementWrappers) {
            if (statementWrapper.matches(method, args, result)) {
                newResult = statementWrapper.wrap(connection, args, result, prefix);
                wrapped = true;
                break;
            }
        }
        if (!wrapped) {
            newResult = result;
        }
        return newResult;
    }
    /**
     * Statement replacer interface and base class
     */
    private static abstract class AbstractStatementWrapper<B extends Statement, A extends SimonStatement> {
        private final Class<B> statementClass;
        protected final Constructor<A> simonStatementConstructor;

        private AbstractStatementWrapper(Class<B> statementClass, Constructor<A> simonStatementConstructor) {
            this.statementClass = statementClass;
            this.simonStatementConstructor = simonStatementConstructor;
        }

        public boolean matches(Method method, Object[] args, Object result) {
            return statementClass.isInstance(result);
        }

        protected A newInstance(Object... arguments) {
            return SimonJdbcInterceptor.newInstance(simonStatementConstructor, arguments);
        }

        public abstract A wrap(Connection connection, Object[] args, Object result, String prefix);
    }

    /**
     * Statement wrapper for basic statements
     */
    private static class StatementWrapper extends AbstractStatementWrapper<Statement, SimonStatement> {
        private StatementWrapper() throws NoSuchMethodException {
            super(Statement.class, getConstructor(SimonStatement.class,
                    new Class[]{
                            Connection.class,
                            Statement.class,
                            String.class
                    }));
        }

        @Override
        public boolean matches(Method method, Object[] args, Object result) {
            return method.getName().equals("createStatement") && super.matches(method, args, result);
        }

        public SimonStatement wrap(Connection connection, Object[] args, Object result, String prefix) {
            return newInstance(connection, result, prefix);
        }
    }

    /**
     * Statement wrapper for prepared statements
     */
    private static class PreparedStatementWrapper extends AbstractStatementWrapper<PreparedStatement, SimonPreparedStatement> {
        private PreparedStatementWrapper() throws NoSuchMethodException {
            super(PreparedStatement.class, getConstructor(SimonPreparedStatement.class,
                    new Class[]{
                            Connection.class,
                            PreparedStatement.class,
                            String.class,
                            String.class
                    }));
        }

        @Override
        public boolean matches(Method method, Object[] args, Object result) {
            return method.getName().equals("prepareStatement") && super.matches(method, args, result);
        }

        @Override
        public SimonPreparedStatement wrap(Connection connection, Object[] args, Object result, String prefix) {
            return newInstance(connection, result, args[0], prefix);
        }
    }

    /**
     * Statement wrapper for callable statements
     */
    private static class CallableStatementWrapper extends AbstractStatementWrapper<CallableStatement, SimonCallableStatement> {
        private CallableStatementWrapper() throws NoSuchMethodException {
            super(CallableStatement.class, getConstructor(SimonCallableStatement.class,
                    new Class[]{
                            Connection.class,
                            CallableStatement.class,
                            String.class,
                            String.class
                    }));
        }

        @Override
        public boolean matches(Method method, Object[] args, Object result) {
            return method.getName().equals("prepareCall") && super.matches(method, args, result);
        }

        public SimonCallableStatement wrap(Connection connection, Object[] args, Object result, String prefix) {
            return newInstance(connection, result, args[0], prefix);
        }
    }
    /**
     * Find constructor for given type and argument types
     */
    private static <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] argumentTypes) throws NoSuchMethodException {
        Constructor<T> constructor = type.getDeclaredConstructor(argumentTypes);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor;
    }

    /**
     * Create a new instance of T using specified constructor arguments
     *
     * @return New instance or null in case of failure
     */
    private static <T> T newInstance(Constructor constructor, Object[] parameterValues) {
        try {
            @SuppressWarnings("unchecked")
            T instance = (T) constructor.newInstance(parameterValues);
            return instance;
        } catch (InstantiationException instantiationException) {
            LOGGER.error("Simon statement instantiation failed", instantiationException);
            return null;
        } catch (IllegalAccessException illegalAccessException) {
            LOGGER.error("Simon statement instantiation failed", illegalAccessException);
            return null;
        } catch (InvocationTargetException invocationTargetException) {
            LOGGER.error("Simon statement instantiation failed", invocationTargetException);
            return null;
        }
    }
}
