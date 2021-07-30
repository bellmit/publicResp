/**
 * 
 */
package com.insta.hms.master.DiscountPlan;

import com.insta.hms.master.MasterAction;
import com.insta.hms.master.MasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anupama
 *
 */
public class DiscountPlanAction extends MasterAction {

	private DiscountPlanDAO dao = new DiscountPlanDAO();

	@Override
	public MasterDAO getMasterDao() {
		return dao;
	}

	@Override
	public Map<String, List<BasicDynaBean>> getLookupLists() throws SQLException {
		Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>(); 
		List<BasicDynaBean> discountPlans = dao.listAll();
		map.put("discount_plan_name", discountPlans);
		return map;
	}

}
