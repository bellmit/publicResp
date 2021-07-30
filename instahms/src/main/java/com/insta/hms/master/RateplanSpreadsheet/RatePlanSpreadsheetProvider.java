package com.insta.hms.master.RateplanSpreadsheet;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RatePlanSpreadsheetProvider {

	static Logger logger = LoggerFactory.getLogger(
			RatePlanSpreadsheetProvider.class);

	private String chargeColumnId;
	private String itemTableId;
	private String bedColumn;
	private String orgColumnName;
	private String itemTable;
	private int serviceGroupId;
	private String discountColName;
	private String orgTable;
	private String chargeTable;
	private String statusColumnName;

	private boolean showDiscount;
	private boolean onlyHospitalCharge;

	private List<String> hospitalCharges;
	private List<String> doctorCharges;
	private List<String> anesthesiaCharges;
	private List<String> orgColumns;
	private List<String> userDefinedColumns;
	private List<String> subHeaders;

	private String[] chargeTypeColumns;
	private String[] chargeColumns;
	private String[] itemColumns;

	private Map<String, String> aliasUnmsToDBnmsMap;

	public void setChargeTable(String chargeTable) {
		this.chargeTable = chargeTable;
	}
	public void setOrgTable(String orgTable) {
		this.orgTable = orgTable;
	}
	public void setBedColumn(String bedColumn) {
		this.bedColumn = bedColumn;
	}
	public void setChargeColumnId(String chargeColumnId) {
		this.chargeColumnId = chargeColumnId;
	}
	public void setItemTable(String itemTable) {
		this.itemTable = itemTable;
	}
	public void setItemTableId(String itemTableId) {
		this.itemTableId = itemTableId;
	}
	public void setOrgColumnName(String orgColumnName) {
		this.orgColumnName = orgColumnName;
	}
	public void setServiceGroupId(int serviceGroupId) {
		this.serviceGroupId = serviceGroupId;
	}
	public void setDiscountColName(String discountColName) {
		this.discountColName = discountColName;
	}
	public void setStatusColumnName(String statusColumnName) {
		this.statusColumnName = statusColumnName;
	}


	public void setShowDiscount(boolean showDiscount) {
		this.showDiscount = showDiscount;
	}
	public void setOnlyHospitalCharge(boolean onlyHospitalCharge) {
		this.onlyHospitalCharge = onlyHospitalCharge;
	}


	public void setSubHeaders(List<String> subHeaders) {
		this.subHeaders = subHeaders;
	}
	public void setAnesthesiaCharges(List<String> anesthesiaCharges) {
		this.anesthesiaCharges = anesthesiaCharges;
	}
	public void setDoctorCharges(List<String> doctorCharges) {
		this.doctorCharges = doctorCharges;
	}
	public void setHospitalCharges(List<String> hospitalCharges) {
		this.hospitalCharges = hospitalCharges;
	}
	public void setOrgColumns(List<String> orgColumns) {
		this.orgColumns = orgColumns;
	}
	public void setUserDefinedColumns(List<String> userDefinedColumns) {
		this.userDefinedColumns = userDefinedColumns;
	}
	public void setChargeColumns(String[] chargeColumns) {
		this.chargeColumns = chargeColumns;
	}
	public void setChargeTypeColumns(String[] chargeTypeColumns) {
		this.chargeTypeColumns = chargeTypeColumns;
	}
	public void setItemColumns(String[] itemColumns) {
		this.itemColumns = itemColumns;
	}

	public void setAliasUnmsToDBnmsMap(Map<String, String> aliasUnmsToDBnmsMap) {
		this.aliasUnmsToDBnmsMap = aliasUnmsToDBnmsMap;
	}


	public List<BasicDynaBean> generateQueryFOrSpreadsheet(List<String> itemColumns, String orgId, int serviceGrpId)throws SQLException {

		StringBuilder query = new StringBuilder();
		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		int bedCount = 0;
		String hospital = "Hospital";
		String doctor = "Doctor";
		String anes = "Anaes";

		query.append("SELECT ");

		for (String columnName : itemColumns) {
			query.append("itemTable." +columnName + ",");
		}

		query.append("serviceSubGroup.service_sub_group_name, ");

		for (String userdefinedCol : userDefinedColumns) {
			query.append("'"+userdefinedCol+"'::character varying AS type,");
		}

		for (String bed : bedTypes) {
			bedCount++;

			for (String hospCharge : hospitalCharges) {
				query.append("(SELECT " +0+"+"+hospCharge);
				query.append(" FROM "+chargeTable);
				query.append(" WHERE "+chargeColumnId +" = itemTable."+itemTableId);
				query.append(" AND ");
				query.append(bedColumn +" = ? AND "+orgColumnName+" = ? ) AS "+DataBaseUtil.quoteIdent(bed+""+hospital, true));

				if (onlyHospitalCharge) {
					if (showDiscount)
						query.append(",");
					else {
						if (bedCount < bedTypes .size())
							query.append(",");
					}

				} else
					query.append(",");
			}
			if (showDiscount) {
				query.append("(SELECT "+0+"+"+discountColName);
				query.append(" FROM "+chargeTable);
				query.append(" WHERE "+chargeColumnId +" =itemTable."+itemTableId);
				query.append(" AND ");
				query.append(bedColumn +" = ? AND "+orgColumnName+" =? ) AS "+DataBaseUtil.quoteIdent(bed+"hospitalDiscount", true));
				if (onlyHospitalCharge) {
					if (bedCount < bedTypes .size())
						query.append(",");
				} else {
					query.append(",");
				}
			}

			if (!onlyHospitalCharge) {
				for (String doctorCharge : doctorCharges) {
					query.append("(SELECT " +0+"::numeric");
					query.append(" FROM "+chargeTable);
					query.append(" WHERE "+chargeColumnId +" = itemTable."+itemTableId);
					query.append(" AND ");
					query.append(bedColumn +" = ? AND "+orgColumnName+" = ? ) AS "+DataBaseUtil.quoteIdent(bed+""+doctor, true)+",");
				}
				if (showDiscount) {
					query.append("(SELECT " +0+"::numeric");
					query.append(" ) AS "+DataBaseUtil.quoteIdent(bed+""+"doctorDiscount", true)+",");
				}


				for (String anesthesiaCharge : anesthesiaCharges) {
					query.append("(SELECT " +0+"::numeric");
					query.append(" FROM "+chargeTable);
					query.append(" WHERE "+chargeColumnId +" = itemTable."+itemTableId);
					query.append(" AND ");
					query.append(bedColumn +" = ? AND "+orgColumnName+" = ? ) AS "+DataBaseUtil.quoteIdent(bed+""+anes, true)+",");
				}
				if (showDiscount) {
					query.append("(SELECT " +0+"::numeric");
					query.append(" ) AS "+DataBaseUtil.quoteIdent(bed+""+"anesthesiaDiscount", true)+",");
				}

				for (int t=0; t<1; t++) {
					query.append("(SELECT "+0+"::numeric");
					query.append(" FROM "+chargeTable);
					query.append(" WHERE "+chargeColumnId +" = itemTable."+itemTableId);
					query.append(" AND ");
					query.append(bedColumn +" = ? AND "+orgColumnName+" = ? ) AS "+DataBaseUtil.quoteIdent("total", true));
				}
				if (bedCount < bedTypes .size())
					query.append(",");
			}



		}

		query.append(" FROM "+itemTable+" itemTable");
		query.append(" JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id)");
		query.append(" JOIN service_groups serviceGroup USING(service_group_id)");
		query.append(" WHERE service_group_id = ? AND itemTable."+statusColumnName + "= 'A'");
		query.append("ORDER BY itemTable."+itemTableId);

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(query.toString());
			int i = 1;
			for (String bed : bedTypes) {

				for (String hospCharge : hospitalCharges) {
					pstmt.setString(i++, bed);
					pstmt.setString(i++, orgId);

				}
				if (showDiscount) {
					pstmt.setString(i++, bed);
					pstmt.setString(i++, orgId);
				}
				if (!onlyHospitalCharge) {
					for (String doctorCharge : doctorCharges) {
						pstmt.setString(i++, bed);
						pstmt.setString(i++, orgId);

					}
					for (String anesthesiaCharge : anesthesiaCharges) {
						pstmt.setString(i++, bed);
						pstmt.setString(i++, orgId);

					}
					for (int t=0; t<1; t++) {
						pstmt.setString(i++, bed);
						pstmt.setString(i++, orgId);
					}
				}
			}
			pstmt.setInt(i++, serviceGrpId);
			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);

		}

	}

	public void setHeaders(HSSFSheet sheet, List<String> headers)throws SQLException {
		int cellNo = 0;
		int from = 0;
		int to = 0;
		int chargesColumn = 0;
		int length = 0;
		int subHeaderLength = subHeaders.size();
		HSSFCell cell = null;
		HSSFRow row = sheet.createRow(0);
		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		HSSFWorkbook workBook = sheet.getWorkbook();
		HSSFCellStyle style = workBook.createCellStyle();
		HSSFCellStyle borderStyle = workBook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		borderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

		if (showDiscount) {
			if (onlyHospitalCharge)
				length = (subHeaderLength + subHeaderLength);
			else
				length = (subHeaderLength + subHeaderLength)-1;
		} else {
			length = subHeaders.size();
		}

		for (String header : headers) {
			cell = row.createCell(cellNo);
			cell.setCellValue(header.toUpperCase());
			cellNo++;
		}
		row.getCell(cellNo-1).setCellStyle(borderStyle);

		from = cellNo;
		chargesColumn = cellNo;

		for (String bed : bedTypes) {
			to = from + length-1;
			sheet.addMergedRegion(new CellRangeAddress(0, 0, from, to));
			cell = row.createCell(from);
			cell.setCellValue(bed.toUpperCase());
			cell.setCellStyle(style);

			from = to+1;
		}
		if (onlyHospitalCharge && !showDiscount)
			cell.setCellStyle(borderStyle);
		else
			row.createCell(from-1).setCellStyle(borderStyle);

		from = chargesColumn;

		HSSFCellStyle subHeaderStyle = workBook.createCellStyle();
		subHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFCellStyle subHeaderStyleWithRightBorder = workBook.createCellStyle();
		subHeaderStyleWithRightBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		subHeaderStyleWithRightBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);

		if (!showDiscount) {


			HSSFRow row1 = sheet.createRow(1);
			row1.createCell(chargesColumn-1).setCellStyle(borderStyle);
			for (String bed : bedTypes) {

				for (String subHeader : subHeaders) {
					cell = row1.createCell(chargesColumn);
					cell.setCellValue(subHeader);
					cell.setCellStyle(subHeaderStyle);

					chargesColumn++;
				}
				cell.setCellStyle(subHeaderStyleWithRightBorder);
			}

		} else {

			HSSFRow row1 = sheet.createRow(1);
			row1.createCell(chargesColumn-1).setCellStyle(borderStyle);
			int subHeadersSize = 0;
			if (onlyHospitalCharge)
				subHeadersSize = subHeaders.size() + 1;
			else
				subHeadersSize = subHeaders.size();

			for (String bed : bedTypes) {
				for (int i=1; i<subHeadersSize; i++) {
					to = from + 1;
					sheet.addMergedRegion(new CellRangeAddress(1, 1, from, to));
					cell = row1.createCell(from);
					cell.setCellValue(subHeaders.get(i-1));
					cell.setCellStyle(subHeaderStyle);
					//chargesColumn++;
					from = to+1;
				}
				if (!onlyHospitalCharge) {
					cell = row1.createCell(from);
					cell.setCellValue(subHeaders.get(subHeaders.size()-1));
					from = from+1;
				}
				if (onlyHospitalCharge && showDiscount)
					cell.setCellStyle(style);
				else
					cell.setCellStyle(subHeaderStyleWithRightBorder);
			}
			List<String> subMostHeaders = Arrays.asList("Charge", "Discount");
			HSSFRow row3 = sheet.createRow(2);
			row3.createCell(chargesColumn-1).setCellStyle(borderStyle);
			for (String bed : bedTypes) {
				for (int i=1; i<subHeadersSize; i++) {
					for (String charge : subMostHeaders) {
						cell = row3.createCell(chargesColumn);
						cell.setCellValue(charge);
						chargesColumn++;
					}
				}
				if (!onlyHospitalCharge)
					cell = row3.createCell(chargesColumn++);
				cell.setCellStyle(subHeaderStyleWithRightBorder);
			}
		}

	}

	public void insertListIntoSpreadSheet(HSSFWorkbook workBook, String tableName, List<BasicDynaBean> dynaList)throws SQLException {


		HSSFSheet subGrpSheet = null;
		int subHeaderLength = subHeaders.size();
		int length4Borders = 0;
		if (showDiscount) {
			if (onlyHospitalCharge)
				length4Borders = (subHeaderLength + subHeaderLength);
			else
				length4Borders = (subHeaderLength + subHeaderLength)-1;
		} else {
			length4Borders = subHeaders.size();
		}

		for (BasicDynaBean bean : dynaList) {
			String subGrpName = (String)bean.get("service_sub_group_name");
			String sheetName = subGrpName.replaceAll("[\\W]", "_");

			if (workBook.getSheet(sheetName) == null) {
				subGrpSheet = workBook.createSheet(sheetName);
				setHeaders(subGrpSheet, Arrays.asList("code","item","type"));

			} else {
				subGrpSheet = workBook.getSheet(sheetName);
			}
			int headersSize = subGrpSheet.getLastRowNum()+1;
			HSSFRow row = subGrpSheet.createRow(headersSize);
			insertRowinSpreadsheet(bean, row, length4Borders);
		}

	}


	public void insertRowinSpreadsheet(BasicDynaBean bean, HSSFRow row, int length4Borders) {

		int cellNo = 0;
		int startingBorder = 3;

		HSSFCellStyle borderStyle = row.getSheet().getWorkbook().createCellStyle();
		borderStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();


		for (DynaProperty property : dynaProperties) {

			HSSFCell cell = null;
			int diffRange = 0;
			int rowIndex = 0;
			int columnIndex = 0;
			StringBuilder formula = null;
			if (showDiscount)
				diffRange = 6;
			else
				diffRange = 3;

			if (property.getName().toString().equalsIgnoreCase("total")) {

				columnIndex = row.getCell(cellNo-1).getColumnIndex()+1;
				rowIndex = row.getCell(cellNo-1).getRowIndex()+1;
				formula = getFormula(columnIndex, rowIndex, diffRange);
				cell = row.createCell(cellNo);
				cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				cell.setCellFormula(formula.toString());

			} else if (property.getName().toString().equalsIgnoreCase("service_sub_group_name")) {
				continue;

			} else {
				if (bean.get(property.getName()) == null) {
					cell = row.createCell(cellNo);
					if (cellNo >= 3)
						cell.setCellValue(new Double(0));
					else
						cell.setCellValue(new HSSFRichTextString());

				} else if (property.getType().equals(java.lang.String.class)) {
					cell = row.createCell(cellNo);
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(bean.get(property.getName()).toString());

				} else if (property.getType().equals(java.lang.Integer.class)) {
					cell = row.createCell(cellNo);
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(((Integer)bean.get(property.getName())).doubleValue());

				} else if (property.getType().equals(java.math.BigDecimal.class)) {
					cell = row.createCell(cellNo);
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(((BigDecimal)bean.get(property.getName())).doubleValue());

				} else if (property.getType().equals(java.lang.Boolean.class)) {
					cell = row.createCell(cellNo);
					cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
					cell.setCellValue((Boolean)bean.get(property.getName()));

				} else if (property.getType().equals(java.sql.Date.class)) {
					cell = row.createCell(cellNo);
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue((java.sql.Date)bean.get(property.getName()));

				}
			}
			cellNo++;
			if (cellNo == startingBorder) {
				cell.setCellStyle(borderStyle);
				startingBorder = startingBorder + length4Borders;
			}

		}


	}

	private StringBuilder getFormula(int columnIndex, int rowIndex, int diffRange) {

		int baseIndex = 65;
		int reminder = 0;
		int coefficient = 0;
		int startingPoint = 0;
		int endingPoint = 0 ;
		List<String> cellReferenceList = new ArrayList<String>();

		startingPoint = columnIndex - diffRange;
		endingPoint = columnIndex;
		char reference = '0';
		char reference1 = '1';
		StringBuilder builder = new StringBuilder();

		for (int i=startingPoint; i<endingPoint; i++) {

			if (!(i>25)) {
				reference = (char)(baseIndex+i);
				cellReferenceList.add(new String(reference+""));
			} else {
				reminder = i%26;
				coefficient = i/26;
				reference = (char)(baseIndex+(coefficient-1));
				reference1 = (char)(baseIndex+reminder);
				cellReferenceList.add(new String(reference+""+reference1));
			}

		}
		builder.append("SUM(");
		if (!showDiscount) {
			for (String ref : cellReferenceList) {
				builder.append("+");
				builder.append(ref+""+rowIndex);
			}

		} else {
			for (int index=0; index<cellReferenceList.size(); index++) {
				builder.append("+");
				builder.append("(");
				builder.append(cellReferenceList.get(index)+""+rowIndex);
				builder.append("-");
				builder.append(cellReferenceList.get(++index)+""+rowIndex);
				builder.append(")");

			}
		}
		builder.append(")");
		return builder;
	}
	public static Map<String, Map<String, String>> tableMap = null;
	static {

		Map<String, String> serviceColumnMap = new HashMap<String, String>();
		serviceColumnMap.put("itemIdName", "service_id");
		serviceColumnMap.put("chargeIdName", "service_id");
		serviceColumnMap.put("code", "service_code");
		serviceColumnMap.put("item", "service_name");
		serviceColumnMap.put("hospital", "unit_charge");
		serviceColumnMap.put("doctor", null);
		serviceColumnMap.put("anaes", null);
		serviceColumnMap.put("total", null);
		serviceColumnMap.put("hospitalcharge", "unit_charge");
		serviceColumnMap.put("hospitaldiscount", "discount");
		serviceColumnMap.put("itemTable", "services");
		serviceColumnMap.put("chargesTable", "service_master_charges");
		serviceColumnMap.put("orgColumnName", "org_id");
		serviceColumnMap.put("userNameColName", "username");

		Map<String, String> diagnosticColumnMap = new HashMap<String, String>();
		diagnosticColumnMap.put("itemIdName", "test_id");
		diagnosticColumnMap.put("chargeIdName", "test_id");
		diagnosticColumnMap.put("code", "diag_code");
		diagnosticColumnMap.put("item", "test_name");
		diagnosticColumnMap.put("hospital", "charge");
		diagnosticColumnMap.put("doctor", null);
		diagnosticColumnMap.put("anaes", null);
		diagnosticColumnMap.put("total", null);
		diagnosticColumnMap.put("hospitalcharge", "charge");
		diagnosticColumnMap.put("hospitaldiscount", "discount");
		diagnosticColumnMap.put("itemTable", "diagnostics");
		diagnosticColumnMap.put("chargesTable", "diagnostic_charges");
		diagnosticColumnMap.put("orgColumnName", "org_name");
		diagnosticColumnMap.put("userNameColName", "username");

		Map<String, String> operationColumnMap = new HashMap<String, String>();
		operationColumnMap.put("itemIdName", "op_id");
		operationColumnMap.put("chargeIdName", "op_id");
		operationColumnMap.put("code", "operation_code");
		operationColumnMap.put("item", "operation_name");
		operationColumnMap.put("hospital", "surg_asstance_charge");
		operationColumnMap.put("doctor", "surgeon_charge");
		operationColumnMap.put("anaes", "anesthetist_charge");
		operationColumnMap.put("total", null);
		operationColumnMap.put("hospitalcharge", "surg_asstance_charge");
		operationColumnMap.put("hospitaldiscount", "surg_asst_discount");
		operationColumnMap.put("doctorcharge", "surgeon_charge");
		operationColumnMap.put("doctordiscount", "surg_discount");
		operationColumnMap.put("anaescharge", "anesthetist_charge");
		operationColumnMap.put("anaesdiscount", "anest_discount");
		operationColumnMap.put("itemTable", "operation_master");
		operationColumnMap.put("chargesTable", "operation_charges");
		operationColumnMap.put("orgColumnName", "org_id");
		operationColumnMap.put("userNameColName", "username");

		Map<String, String> equipmentColumnMap = new HashMap<String, String>();
		equipmentColumnMap.put("itemIdName", "eq_id");
		equipmentColumnMap.put("chargeIdName", "equip_id");
		equipmentColumnMap.put("code", "equipment_code");
		equipmentColumnMap.put("item", "equipment_name");
		equipmentColumnMap.put("hospital", "daily_charge");
		equipmentColumnMap.put("doctor", null);
		equipmentColumnMap.put("anaes", null);
		equipmentColumnMap.put("total", null);
		equipmentColumnMap.put("hospitalcharge", "daily_charge");
		equipmentColumnMap.put("hospitaldiscount", "daily_charge_discount");
		equipmentColumnMap.put("itemTable", "equipment_master");
		equipmentColumnMap.put("chargesTable", "equipement_charges");
		equipmentColumnMap.put("orgColumnName", "org_id");
		equipmentColumnMap.put("userNameColName", null);

		Map<String, String> packageColumnMap = new HashMap<String, String>();
		packageColumnMap.put("itemIdName", "package_id");
		packageColumnMap.put("chargeIdName", "package_id");
		packageColumnMap.put("code", "package_code");
		packageColumnMap.put("item", "package_name");
		packageColumnMap.put("hospital", "charge");
		packageColumnMap.put("doctor", null);
		packageColumnMap.put("anaes", null);
		packageColumnMap.put("total", null);
		packageColumnMap.put("hospitalcharge", "charge");
		packageColumnMap.put("hospitaldiscount", "discount");
		packageColumnMap.put("itemTable", "packages");
		packageColumnMap.put("chargesTable", "package_charges");
		packageColumnMap.put("orgColumnName", "org_id");
		packageColumnMap.put("userNameColName", null);

		Map<String, String> commonColumnMap = new HashMap<String, String>();
		commonColumnMap.put("itemIdName", "charge_name");
		commonColumnMap.put("chargeIdName", "charge_name");
		commonColumnMap.put("code", "othercharge_code");
		commonColumnMap.put("item", "charge_name");
		commonColumnMap.put("hospital", "charge");
		commonColumnMap.put("doctor", null);
		commonColumnMap.put("anaes", null);
		commonColumnMap.put("total", null);
		commonColumnMap.put("hospitalcharge", "charge");
		commonColumnMap.put("hospitaldiscount", null);
		commonColumnMap.put("itemTable", "common_charges_master");
		commonColumnMap.put("chargesTable", "common_charges_master");
		commonColumnMap.put("orgColumnName", "org_id");
		commonColumnMap.put("userNameColName", null);

		Map<String, String> consultationColumnMap = new HashMap<String, String>();
		consultationColumnMap.put("itemIdName", "consultation_type_id");
		consultationColumnMap.put("chargeIdName", "consultation_type_id");
		consultationColumnMap.put("code", "consultation_code");
		consultationColumnMap.put("item", "consultation_type");
		consultationColumnMap.put("hospital", "charge");
		consultationColumnMap.put("doctor", null);
		consultationColumnMap.put("anaes", null);
		consultationColumnMap.put("total", null);
		consultationColumnMap.put("hospitalcharge", "charge");
		consultationColumnMap.put("hospitaldiscount", "discount");
		consultationColumnMap.put("itemTable", "consultation_types");
		consultationColumnMap.put("chargesTable", "consultation_charges");
		consultationColumnMap.put("orgColumnName", "org_id");
		consultationColumnMap.put("userNameColName", null);


		tableMap = new HashMap<String, Map<String,String>>();
		tableMap.put("Service",serviceColumnMap);
		tableMap.put("Diagnostics", diagnosticColumnMap);
		tableMap.put("Package", packageColumnMap);
		tableMap.put("Consultation", consultationColumnMap);
		tableMap.put("Operation", operationColumnMap);
		tableMap.put("Equipment", equipmentColumnMap);
		tableMap.put("Common", commonColumnMap);
	}

	public StringBuilder errors;

	public void importCharges(HSSFWorkbook workBook, String orgId, StringBuilder errors, String userName)
						throws SQLException, IOException {

		List<String> bedTypes = getUnionOfBedTypes();
		Map<String, String> dbBedNamesMap = getMapofOriginalBedtypes();

		this.errors = errors;
		int chargesColNo = 0;
		int noOfCharges = 1;
		int bedsCount = 0;
		int noOfSheets = workBook.getNumberOfSheets();

		for (int s=0; s<noOfSheets; s++) {
			HSSFSheet sheet = workBook.getSheetAt(s);
			String sheetName = sheet.getSheetName();
			CellRangeAddress range = sheet.getMergedRegion(0);
			if (range != null) {
				noOfCharges = range.getNumberOfCells();
			}
			chargesColNo = 3;
			Iterator rowIterator = sheet.rowIterator();
			HSSFRow row1 = (HSSFRow)rowIterator.next();

			row1.getLastCellNum();
			int colLen = chargesColNo + (bedTypes.size() * noOfCharges);
			String[] headers = new String[colLen];

			for (int i=0; i<headers.length; i++) {

				HSSFCell cell = row1.getCell(i);
				if (cell == null)
					headers[i] = null; /*putting null values, if found*/
				else {

					String header = cell.getStringCellValue().toLowerCase();
					headers[i] = header;

					int headerColNo =cell.getColumnIndex();
					if (Arrays.asList(new String[] {"code", "type", "sub group name", "item"}).contains(header)) {

					} else if (bedTypes.contains(header)) {
						bedsCount++;

					} else {
						if (headerColNo < chargesColNo || (headerColNo >= chargesColNo  && cell != null && !header.equals(""))) {
							addError(0, "Unknown property found in header "+header +" in sheet "+sheetName);
							headers[i] = null; /*putting null values, if found unknown properties*/
						}
					}

				}

			}

			HSSFRow row2 = sheet.getRow(1);
			int noOfCellsToRead = headers.length;

			String subHeader = null;
			String[] subHeaders = new String[noOfCellsToRead];

			if (row2 != null) {
				rowIterator.next();
				for (int i=chargesColNo; i<noOfCellsToRead; i++) {
					HSSFCell cell = row2.getCell(i);
					if (cell != null && !cell.equals("")) {
						subHeader = cell.toString().toLowerCase();
						if (Arrays.asList(new String[] {"hospital", "doctor", "anaes", "total"}).contains(subHeader)) {
							subHeaders[i] = subHeader;

						} else if(subHeader != null && !subHeader.equals("")) {
							subHeaders[i] = null;
							addError(2, "Unknown charge type found in subHeader "+subHeader+" in sheet "+sheetName);

						}
					} else {
						subHeaders[i] = null;
					}
				}
			}

			String[] subMostHeaders = new String[noOfCellsToRead];
			boolean isOnlyHospChargeAndDiscount = false;
			if (sheet.getRow(2) != null) {
				HSSFRow row3rd = sheet.getRow(2);
				if (row3rd.getCell(chargesColNo) != null && row3rd.getCell(chargesColNo+1) != null) {
					isOnlyHospChargeAndDiscount = row3rd.getCell(chargesColNo).toString().equalsIgnoreCase("charge");
					isOnlyHospChargeAndDiscount = row3rd.getCell(chargesColNo+1).toString().equalsIgnoreCase("discount");
				}

			}

			if (noOfCharges > 4 || isOnlyHospChargeAndDiscount) {
				HSSFRow row3 = sheet.getRow(2);
				rowIterator.next();
				for (int i=chargesColNo; i<noOfCellsToRead; i++) {
					HSSFCell cell = row3.getCell(i);
					if (cell != null && !cell.equals("")) {
						subHeader = cell.toString().toLowerCase();
						if (Arrays.asList(new String[] {"charge", "discount"}).contains(subHeader)) {
							subMostHeaders[i] = subHeader;

						} else {
							subMostHeaders[i] = null;
							if (subHeader != null && !subHeader.equals(""))
								addError(3, "Unknown charge type found in subHeader "+subHeader+" in sheet "+sheetName);

						}
					} else {
						subMostHeaders[i] = null;
					}
				}
			}

			/*Mapping ....*/
			Map<Integer, String[]> columnMap = new HashMap<Integer, String[]>();
			String[] bedAndCharge = null;
			int subLen = chargesColNo;

			for (int i=0; i<headers.length; i++) {

				if (headers[i] == null || headers[i].equals("")) {
					if (i >= chargesColNo) {
						columnMap.put(i, new String[] {null});
						subLen++;
					} else {
						columnMap.put(i, new String[] {null});
					}

				} else if (bedTypes.contains(headers[i])) {
					String bedType = dbBedNamesMap.get(headers[i]);
					int subI = i;
					for (int charges=0; charges<noOfCharges; charges++) {

						bedAndCharge = new String[3];
						bedAndCharge[0] = bedType;
						bedAndCharge[1] = subHeaders[subLen];
						columnMap.put(subI, bedAndCharge);
						subLen++;
						subI++; /*incrementing header bcoz of spliting the cells remaining cells gives nulls*/
					}
					i = i+(noOfCharges-1);
				} else {

					columnMap.put(i, new String[] {headers[i]});
				}
			}
			List<String> chargeTypeList = Arrays.asList(new String[] {"hospital", "doctor", "anaes"});
			subLen = chargesColNo;
			int chargeColLen = chargeColumns.length;
			if (noOfCharges > 4 || isOnlyHospChargeAndDiscount) {
				String[] columnArray;
				for (int j=chargesColNo; j<headers.length; j++) {
					int sub = j;

					if (actualBedNames.contains(columnMap.get(j)[0]) && chargeTypeList.contains(columnMap.get(j)[1])) {

						for (int charges=0; charges<chargeColLen; charges++) {
							columnArray = columnMap.get(sub);
							columnArray[1] = columnMap.get(j)[1];
							columnArray[2] = subMostHeaders[subLen];
							sub++;
							subLen++;
						}
						j = j+(chargeColLen-1);

					} else {
						if (columnMap.get(j)[1] != null && columnMap.get(j)[1].equals("total")) {
							columnMap.get(j)[2] = "total";
							subLen++;

						} else {
							for (int charges=0; charges<chargeColLen; charges++) {
							columnArray = columnMap.get(sub);
							columnArray[2] = null;
							sub++;
							subLen++;
						}
						j = j+(chargeColLen-1);
						}
					}
				}
			}
		nextLine:while (rowIterator.hasNext()) {
					HSSFRow row = (HSSFRow)rowIterator.next();
					int lineNumber = row.getRowNum()+1;
					logger.debug("Processing line: "+lineNumber);
					if (row != null) {

						Object code = null;
						Object item = null;
						Object type = null;
						BigDecimal chargeVal =null;
						Map<String, String> typeMap = null;

						Map<String, Map> eachBedMap = new HashMap<String, Map>();
						int index = 0;
						Iterator colMapItr = columnMap.entrySet().iterator();

				nextCell:while (colMapItr.hasNext())  {
							Map.Entry entry = (Map.Entry)colMapItr.next();
							Object cellVal = null;

							String colName = columnMap.get(index)[0];
							if (colName == null) {
								index++;
								continue nextCell;
							}
							HSSFCell cell = row.getCell(index);

							if (index < chargesColNo && cell != null) {
								switch(cell.getCellType()) {

								case HSSFCell.CELL_TYPE_BLANK: {
									cellVal = null;
									break;
								}
								case HSSFCell.CELL_TYPE_STRING: {
									cellVal = cell.getStringCellValue();
									break;
								}
								case HSSFCell.CELL_TYPE_NUMERIC: {
									cellVal = cell.getNumericCellValue();
									break;
								}

								}
								if (colName.equalsIgnoreCase("CODE"))
									code = cellVal;
								else if (colName.equalsIgnoreCase("ITEM"))
									item = cellVal;
								else
									type = cellVal;


							} else if (actualBedNames.contains(colName)) {

								if ((item == null || item.equals(""))) {
									addError(lineNumber, "Mandatory field is missing below " +
											"the headers of ITEM in sheet "+sheetName);
								} else if (type == null || type.equals("")) {
									addError(lineNumber, "Mandatory field is missing below " +
											"the headers of TYPE in sheet "+sheetName);
								}

								String chargeName = null;
								typeMap = tableMap.get(type);
								if (typeMap == null) {
									addError(lineNumber, "There is no "+type+" of Type exist "+" in sheet "+sheetName);
									continue nextLine;
								}
								if (noOfCharges > 4 || isOnlyHospChargeAndDiscount)
									chargeName = typeMap.get(columnMap.get(index)[1]+""+columnMap.get(index)[2]);
								else
									chargeName = typeMap.get(columnMap.get(index)[1]);

								if (chargeName != null) {
									if (cell != null && !cell.equals("")) {
										try {
											chargeVal = new BigDecimal(cell.getNumericCellValue());
										} catch (Exception e) {
											addError(lineNumber, "Conversion error: Cell value"  +
													" could not be converted to java.math.BigDecimal below " +
													"headers of "+columnMap.get(index)[1].toUpperCase() +
													" in sheet "+sheetName);
										}
									}
									else
										chargeVal = new BigDecimal(0);

									Map<String, BigDecimal> bedMap = eachBedMap.get(colName);
									if (bedMap != null) {
										bedMap.put(chargeName, chargeVal);
									} else {
										bedMap = new HashMap<String, BigDecimal>();
										bedMap.put(chargeName, chargeVal);
										eachBedMap.put(colName, bedMap);
									}
								}
							}

							index++;
						}

						String itemTable = null;
						String chargesTable = null;
						String keycolumn = null;
						BasicDynaBean bean = null;
						Object keyId = null;
						Map<String, Object> columndata = null;
						Map<String, Object> itemKeys = null;
						Map<String, Object> chargeKeys = null;
						Connection con = null;
						boolean success = false;

						itemTable = typeMap.get("itemTable");
						chargesTable = typeMap.get("chargesTable");
						keycolumn = typeMap.get("item");
						GenericDAO itemDAO = new GenericDAO(itemTable);
						GenericDAO chargesDAO = new GenericDAO(chargesTable);
						bean = itemDAO.findByKey(keycolumn, item);
						if (bean == null) {
							addError(lineNumber, "Master value not found for the item"+" in sheet "+sheetName);
							continue nextLine;
						}
						itemKeys = new HashMap<String, Object>();
						chargeKeys = new HashMap<String, Object>();
						keyId = bean.get(typeMap.get("itemIdName"));
						logger.debug(type +" item id: "+keyId);
						itemKeys.put(typeMap.get("itemIdName"), keyId);
						try {
							con = DataBaseUtil.getReadOnlyConnection();
							con.setAutoCommit(false);

							if (code != null && !code.equals("")) {
								columndata = new HashMap<String, Object>();
								columndata.put(typeMap.get("code"), code);

								success = itemDAO.update(con, columndata, itemKeys) > 0;
							} else {
								success = true;
							}

							chargeKeys.put(typeMap.get("chargeIdName"), keyId);
							chargeKeys.put(typeMap.get("orgColumnName"), orgId);
							Iterator mapiterator = eachBedMap.entrySet().iterator();
							while (mapiterator.hasNext()) {
								Map.Entry<String, Map<String, BigDecimal>> entry = (Map.Entry<String, Map<String, BigDecimal>>)mapiterator.next();
								String bed = entry.getKey();
								chargeKeys.put("bed_type", bed);

								if (chargesTable.equals("common_charges_master")) {
									if (bed.equals("GENERAL")) {
										Map<String, String> commonMasterKeys = new HashMap<String, String>();
										Map<String, BigDecimal> commonMasterData = new HashMap<String, BigDecimal>();
										commonMasterKeys.put(typeMap.get("chargeIdName"), (String)bean.get(typeMap.get("chargeIdName")));
										BigDecimal commonMasterValue = entry.getValue().get("charge");
										if (commonMasterValue != null) {
											commonMasterData.put("charge", commonMasterValue);
											success = chargesDAO.update(con, commonMasterData, commonMasterKeys) > 0;
										}
									}
								} else if (chargesTable.equals("equipement_charges") && bean.get("charge_basis").equals("H")) {
											Map<String, BigDecimal> equipmentMap = new HashMap<String, BigDecimal>();
											equipmentMap.put("incr_charge", entry.getValue().get("daily_charge"));
											if (noOfCharges > 4)
												equipmentMap.put("incr_charge_discount", entry.getValue().get("daily_charge_discount"));

												success &= chargesDAO.update(con, equipmentMap, chargeKeys) > 0;
								} else {
									if (typeMap.get("userNameColName") != null)
										((Map)entry.getValue()).put(typeMap.get("userNameColName"), userName+":XLS");
									success &= chargesDAO.update(con, entry.getValue(), chargeKeys) > 0;
								}

							}
						} finally {
							if (!success)
								logger.error("Updation failed for "+type+" id: "+keyId);
							DataBaseUtil.commitClose(con, success);
						}
					}

				}
		}
	}

	private void addError(int line, String msg) {

		if (line > 0) {
			errors.append("Line ").append(line).append(": ");
		} else {
			errors.append("Error in header: ");
		}
		errors.append(msg).append("<br>");
	}

	private static final String BED_TYPES = "SELECT lower(bed_type_name) as bed_type from bed_types WHERE " +
	"billing_bed_type='Y' AND status = 'A'";

	private static final String ALL_BED_TYPES = "SELECT lower(bed_type_name) as bed_type, bed_type_name as actual_name from bed_types WHERE " +
		"billing_bed_type='Y' AND status = 'A'";

	HashMap<String, String> originalBedNamesMap = null;

	public static ArrayList<String> getUnionOfBedTypes()throws SQLException{
		ArrayList<String> al = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(BED_TYPES);
		al = DataBaseUtil.queryToOnlyArrayList(ps);
		ArrayList<String> duplicate = new ArrayList<String>();

		Iterator<String> it = al.iterator();
		while(it.hasNext()){
			String bed = it.next();
			duplicate.add(bed);
		}
		ps.close();
		con.close();
		return duplicate;
	}

	ArrayList<String> actualBedNames = null;
	public Map<String, String> getMapofOriginalBedtypes()throws SQLException{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		HashMap<String, String> map = new HashMap<String, String>();
		actualBedNames = new ArrayList<String>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ALL_BED_TYPES);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				map.put(resultSet.getString("bed_type"), resultSet.getString("actual_name"));
				actualBedNames.add(resultSet.getString("actual_name"));
			}
		} finally {
			DataBaseUtil.closeConnections(con, pstmt, resultSet);
		}
		return map;
	}

}