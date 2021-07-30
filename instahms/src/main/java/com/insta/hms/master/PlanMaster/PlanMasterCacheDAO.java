package com.insta.hms.master.PlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractCachingDAO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.sql.SQLException;
import java.util.List;

public class PlanMasterCacheDAO extends AbstractCachingDAO{
	
	public PlanMasterCacheDAO() {
		super("insurance_plan_main");
	}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}
	
	private static final String GET_PLANS_LIST = "SELECT * from insurance_plan_main where  " +
	    " category_id in (select category_id from insurance_category_center_master where  " +
	    "       center_id in (?,0) AND status = 'A') " +
	    "       AND status ='A'";
	
	public List getPlansList(int centerId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PLANS_LIST,centerId);
	}
}
