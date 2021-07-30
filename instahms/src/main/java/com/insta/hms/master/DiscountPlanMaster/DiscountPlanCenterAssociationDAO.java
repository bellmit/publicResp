package com.insta.hms.master.DiscountPlanMaster;

import com.insta.hms.master.CenterAssociationDAO;

public class DiscountPlanCenterAssociationDAO extends CenterAssociationDAO {

	public DiscountPlanCenterAssociationDAO(){
		super("discount_plan_center_master", "discount_plan_center_id",	"discount_plan_main","discount_plan_id","discount_plan_center_master_discount_plan_center_id_seq");
	}

}
