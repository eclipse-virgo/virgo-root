package monitor;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * An aspect for recording performance statistics on monitored methods.
 */
@Aspect
abstract public class AbstractPerformanceMonitor {

	private static final Logger logger = Logger.getLogger(AbstractPerformanceMonitor.class);

	private MonitorFactory monitorFactory;

	public AbstractPerformanceMonitor(MonitorFactory monitorFactory) {
		this.monitorFactory = monitorFactory;
	}

	@Pointcut
	abstract public void monitoredMethods();

	/**
	 * Times method invocations and outputs performance results to a Log4J logger.
	 * @param method The join point representing the intercepted repository method
	 * @return The object returned by the target method
	 * @throws Throwable if thrown by the target method
	 */
	@Around("monitoredMethods()")
	public Object monitor(ProceedingJoinPoint method) throws Throwable {
		String name = createJoinPointTraceName(method);
		Monitor monitor = monitorFactory.start(name);
		try {
			return method.proceed();
		} finally {
			monitor.stop();
			logger.info(monitor);
		}
	}

	private String createJoinPointTraceName(JoinPoint joinPoint) {
		Signature signature = joinPoint.getSignature();
		StringBuilder sb = new StringBuilder();
		sb.append(signature.getDeclaringType().getSimpleName());
		sb.append('.').append(signature.getName());
		return sb.toString();
	}
}