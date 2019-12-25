package org.eclipse.virgo.util.jmx;

public class ExceptionCleanerNoMXBean implements JmxExceptionCleanerExtensionTestInterface {

    private StackTraceElement[] steArray = null;

    @Override
    public void unCaughtMethod() {
        RuntimeException rte = new RuntimeException("unCaughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;
    }

    @Override
    public void caughtMethod() {
        RuntimeException rte = new RuntimeException("caughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;
    }
    
    public StackTraceElement[] getStackTrace() {
        return this.steArray;
    }

    @Override
    public void anotherCaughtMethod() {
        RuntimeException rte = new RuntimeException("anotherCaughtMethod", new Exception("test exception"));
        this.steArray = rte.getStackTrace();
        throw rte;            
    }

}
