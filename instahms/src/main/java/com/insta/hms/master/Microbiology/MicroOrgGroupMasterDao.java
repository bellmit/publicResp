package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;

import java.sql.SQLException;
import java.util.List;


public class MicroOrgGroupMasterDao{


	public static final String GET_ALL_ORGGRP_NAMES = "SELECT org_group_id, org_group_name FROM micro_org_group_master";


	public static List getOrgGrpNamesIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_ORGGRP_NAMES));
	 }

}