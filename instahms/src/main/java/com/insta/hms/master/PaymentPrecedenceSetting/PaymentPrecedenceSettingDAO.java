package com.insta.hms.master.PaymentPrecedenceSetting;

import com.insta.hms.common.AbstractCachingDAO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class PaymentPrecedenceSettingDAO extends AbstractCachingDAO {

	public PaymentPrecedenceSettingDAO() {
		super("payment_precedence_settings");
	}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 10, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}
}
