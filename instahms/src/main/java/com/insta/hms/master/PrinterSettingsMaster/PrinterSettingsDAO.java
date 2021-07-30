package com.insta.hms.master.PrinterSettingsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PrinterSettingsDAO extends GenericDAO {

	Connection con = null;

	public PrinterSettingsDAO() {
		super("printer_definition");
	}

	 private static final String PRINT_DEFINITION_FIELDS ="SELECT printer_id,printer_definition_name, "+
		 "print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, "+
		 "left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status," +
		 " repeat_patient_info, text_mode_extra_lines ";

	 private static final String PRINT_DEFINITION_COUNT = "SELECT count(*) ";

	 private static final String PRINT_DEFINITION_TABLES = " FROM printer_definition ";


	 public PagedList getPrintDefinition(Map<LISTING, Object> pagingParams) throws SQLException{
		 int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);
		 Connection con = DataBaseUtil.getReadOnlyConnection();
		 SearchQueryBuilder qb =
			 new SearchQueryBuilder(con, PRINT_DEFINITION_FIELDS, PRINT_DEFINITION_COUNT,
					 PRINT_DEFINITION_TABLES, null,null,"printer_id", false, 25,pageNum);
		 qb.addFilter(qb.STRING, "status", "=", "A");
		 qb.build();
		 PreparedStatement psData = qb.getDataStatement();
		 PreparedStatement psCount = qb.getCountStatement();

		 List printList = DataBaseUtil.queryToDynaList(psData);
		 int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		 psData.close();
		 psCount.close();
		 con.close();

		 return new PagedList(printList,count,25,pageNum);
	 }


}


