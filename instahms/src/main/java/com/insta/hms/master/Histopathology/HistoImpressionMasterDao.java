package com.insta.hms.master.Histopathology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class HistoImpressionMasterDao {


	public static final String GET_ALL_HISTONAMES = "SELECT impression_id, short_impression FROM histo_impression_master";

	public static List getHistoNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_HISTONAMES));
	 }

	public static final String GET_ALL_HISTO_IMPRESSIONS = "SELECT short_impression FROM histo_impression_master where status='A'";

	public static ArrayList getAllActiveHistoImpressions() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_HISTO_IMPRESSIONS);
			return  DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	 }

}