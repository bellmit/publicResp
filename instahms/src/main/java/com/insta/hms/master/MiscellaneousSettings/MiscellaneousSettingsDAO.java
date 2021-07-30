package com.insta.hms.master.MiscellaneousSettings;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class MiscellaneousSettingsDAO extends GenericDAO{
	Connection con = null;

	public MiscellaneousSettingsDAO(){
		super("store_miscellaneous_settings");
	}

	public static String GET_PHARMACY_ITEMS = "SELECT * FROM store_miscellaneous_settings "	;

	public  static BasicDynaBean getPharmacyLicenseNo() throws SQLException{
		List l = DataBaseUtil.queryToDynaList(GET_PHARMACY_ITEMS);
		if( l.size() >0 ){
			return (BasicDynaBean) l.get(0);
		}

		return  null;
	}

}
