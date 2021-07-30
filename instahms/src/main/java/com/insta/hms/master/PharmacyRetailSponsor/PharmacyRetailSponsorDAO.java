package com.insta.hms.master.PharmacyRetailSponsor;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;
import java.util.List;

public class PharmacyRetailSponsorDAO extends GenericDAO{

	public PharmacyRetailSponsorDAO() {
		super("store_retail_sponsors");
	}


	private static final String PHARMACYRETAILSPONSORS_NAMESAND_iDS="select sponsor_name,sponsor_id from store_retail_sponsors";

	   public static List getPharmacyRetailSponsorsNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(PHARMACYRETAILSPONSORS_NAMESAND_iDS));
	}

}