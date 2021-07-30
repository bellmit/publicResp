package com.insta.hms.master.SystemMessageMaster;

import com.insta.hms.common.AbstractCachingDAO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class SystemMessagesDAO extends AbstractCachingDAO {

	public SystemMessagesDAO() {
		super("system_messages");
	}

	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

	/*
	 * Forced clearing of the cache available for callers
	 * (Esp. useful when something else changed not related to this table, but you
	 * still want to refresh the cache, or when updating tables from backends, as in scripts.
	 */
	public void clearCache() {
		getCache().removeAll();
	}
}

