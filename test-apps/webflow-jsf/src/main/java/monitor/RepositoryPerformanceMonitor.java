package monitor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import accounts.AccountRepository;

import restaurants.RestaurantRepository;
import rewards.internal.RewardRepository;


/**
 * An aspect that monitors the performance of all three repositories used in the application.
 * @see AccountRepository
 * @see RestaurantRepository
 * @see RewardRepository
 */
@Aspect
public class RepositoryPerformanceMonitor extends AbstractPerformanceMonitor {

	public RepositoryPerformanceMonitor(MonitorFactory monitorFactory) {
		super(monitorFactory);
	}

	@Override
	@Pointcut("execution(public * *.internal.*Repository+.*(..))")
	public void monitoredMethods() {}

}
