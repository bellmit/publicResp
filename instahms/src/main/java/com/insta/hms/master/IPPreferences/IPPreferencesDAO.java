package com.insta.hms.master.IPPreferences;

import com.insta.hms.common.AbstractCachingDAO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;

public class IPPreferencesDAO extends AbstractCachingDAO {

	public IPPreferencesDAO() {
		super("ip_preferences");
	}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 10, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

	public BasicDynaBean getPreferences() throws SQLException {
		return getRecord();
	}

}

