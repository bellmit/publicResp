package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;

import java.sql.SQLException;
import java.util.List;


public class MicroNoGrowthTemplateMasterDAO {


	public static final String GET_ALL_TEMPLATENAMES = "SELECT nogrowth_template_id, nogrowth_template_name FROM micro_nogrowth_template_master";

	public static List getTemplateNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_TEMPLATENAMES));
	 }

}