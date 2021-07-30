package com.insta.hms.master.DiscountPlan;

import com.insta.hms.master.MasterDAO;

public class DiscountPlanDAO  extends MasterDAO {

	protected DiscountPlanDAO() {
		super("discount_plan_main", "discount_plan_id", "discount_plan_name");
	}

}
