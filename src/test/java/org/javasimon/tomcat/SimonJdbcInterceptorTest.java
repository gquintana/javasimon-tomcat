package org.javasimon.tomcat;

import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.javasimon.jdbc4.SimonCallableStatement;
import org.javasimon.jdbc4.SimonPreparedStatement;
import org.javasimon.jdbc4.SimonStatement;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.sql.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
/**
 * Unit test for {@link SimonJdbcInterceptor}
 */
public class SimonJdbcInterceptorTest {
    private Method getConnectionMethod(String methodName) {
        for(Method method:Connection.class.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
    @Test
    public void testStatementWrapper() throws Throwable {
        SimonJdbcInterceptor simonJdbcInterceptor=new SimonJdbcInterceptor();
        JdbcInterceptor nextJdbcInterceptor=mock(JdbcInterceptor.class);
        simonJdbcInterceptor.setNext(nextJdbcInterceptor);
        Connection connectionMock=mock(Connection.class);
        // Basic statement
        when(nextJdbcInterceptor.invoke(anyObject(),any(Method.class),any(new Object[0].getClass()))).thenReturn(mock(Statement.class));
        Object result=simonJdbcInterceptor.invoke(connectionMock,getConnectionMethod("createStatement"),new Object[0]);
        assertNotNull(result);
        assertTrue(result instanceof SimonStatement);
        // Prepared statement
        when(nextJdbcInterceptor.invoke(anyObject(),any(Method.class),any(new Object[0].getClass()))).thenReturn(mock(PreparedStatement.class));
        result=simonJdbcInterceptor.invoke(connectionMock,getConnectionMethod("prepareStatement"),new Object[]{"sql"});
        assertNotNull(result);
        assertTrue(result instanceof SimonPreparedStatement);
        // Callable statement
        when(nextJdbcInterceptor.invoke(anyObject(),any(Method.class),any(new Object[0].getClass()))).thenReturn(mock(CallableStatement.class));
        result=simonJdbcInterceptor.invoke(connectionMock,getConnectionMethod("prepareCall"),new Object[]{"sql"});
        assertNotNull(result);
        assertTrue(result instanceof SimonCallableStatement);
    }
}
