package com.insta.hms.master.PharmacyRetailDoctor;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;
import java.util.List;

public class PharmacyRetailDoctorDAO extends GenericDAO{

	public String status = "Active";
	public PharmacyRetailDoctorDAO() {
		super("store_retail_doctor");
	}

	private static final String PHARMACYRETAILDOCTORS_NAMESAND_iDS="select doctor_name,doctor_id from store_retail_doctor";

	   public static List getPharmacyRetailDoctorsNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(PHARMACYRETAILDOCTORS_NAMESAND_iDS));
	}

}