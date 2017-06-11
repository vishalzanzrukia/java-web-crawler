package com.vishalzanzrukia.crawler;

import org.springframework.context.SmartLifecycle;

/**
 * The base class with default implementation of {@link SmartLifecycle}
 * 
 * @author VishalZanzrukia
 * @see {@link SmartLifecycle}
 */
public abstract class AbstractLifecycleAdapter implements SmartLifecycle {

	@Override
	public void stop() {

	}

	@Override
	public void stop(Runnable callback) {
	}

	@Override
	public boolean isRunning() {
		/** this will make sure that start method called only once */
		return false;
	}

	@Override
	public boolean isAutoStartup() {
		/** this will make sure that start will be called automatically */
		return true;
	}
}
