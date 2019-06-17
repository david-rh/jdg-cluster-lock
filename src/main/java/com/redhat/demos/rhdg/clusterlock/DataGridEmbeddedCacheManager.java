package com.redhat.demos.rhdg.clusterlock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.stereotype.Component;

@Component
public class DataGridEmbeddedCacheManager {

	private ClusteredLockManager clMgr;

	public DataGridEmbeddedCacheManager() {
		

		GlobalConfiguration globalConfig = new GlobalConfigurationBuilder().transport()
				.defaultTransport().addProperty("configurationFile", "config/jgroups.xml")
				.build();
		
		Configuration cacheConfiguration = new ConfigurationBuilder()
				.clustering().cacheMode(CacheMode.REPL_SYNC).build();
		
		EmbeddedCacheManager cacheManager = new DefaultCacheManager(globalConfig, cacheConfiguration);
		cacheManager.defineConfiguration("default", cacheConfiguration);
		
		clMgr = EmbeddedClusteredLockManagerFactory.from(cacheManager);
		
	}
	
	

	public synchronized boolean acquireLock(String filename) throws InterruptedException, ExecutionException {
		if(!clMgr.isDefined(filename))
		{
			clMgr.defineLock(filename);
		}
		ClusteredLock cl = clMgr.get(filename);
		return cl.tryLock(5, TimeUnit.SECONDS).get();
	}
	
	public synchronized void releaseLock(String filename) {
		if(clMgr.isDefined(filename))
		{
			clMgr.get(filename).unlock();
		}
	}
	

}
