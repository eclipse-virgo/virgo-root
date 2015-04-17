package org.eclipse.virgo.util.jmx;

//FIXME Bug 463462 - Move back to test source folder when we know how to weave test classes
public class ExceptionCleanerMXBean implements JmxExceptionCleanerExtensionTestInterface {

    private StackTraceElement[] steArray = null;

    public void unCaughtMethod() throws Exception {
        RuntimeException rte = new RuntimeException("unCaughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;
    }

    public void caughtMethod() throws Exception {
        RuntimeException rte = new RuntimeException("caughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;
    }
    
    public StackTraceElement[] getStackTrace() {
        return this.steArray;
    }

    public void anotherCaughtMethod() {
        RuntimeException rte = new RuntimeException("anotherCaughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;            
    }

}
