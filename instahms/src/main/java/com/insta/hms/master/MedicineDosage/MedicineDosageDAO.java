/**
 *
 */
package com.insta.hms.master.MedicineDosage;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna.t
 *
 */
public class MedicineDosageDAO extends GenericDAO{

	private static final String table = "medicine_dosage_master";
	public MedicineDosageDAO() {
		super(table);
	}

	private static final String MEDICINEDOSAGES_NAMESAND_iDS="select dosage_name from medicine_dosage_master";

	   public static List getMedicineDosagesNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(MEDICINEDOSAGES_NAMESAND_iDS));
	}


}
