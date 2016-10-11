package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase{
    @SuppressWarnings("rawtypes")
    public static Test suite() {
            Class[] testClasses = {
            		 MyHttpServletRequestTest.class,
            		 MyHttpServletResponseTest.class,
            		 MyHttpSessionTest.class,
            		 ServletContainerTest.class
            };
            return new TestSuite(testClasses);
    }
}
