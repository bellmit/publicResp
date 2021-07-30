package com.insta.hms.master.SupplierRateContract;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.csvutils.TableDataHandler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class SupplierRateDataHandler {

	static Logger logger = LoggerFactory.getLogger(
			TableDataHandler.class);

	private String tableName;
	private String sequenceName;
	private String autoIncrName;
	private boolean idValAsString;
	private String[] keys;
	private String[] fields;
	private List<String> allFields = new ArrayList<String>();
	private String[][] masters;
	private String[] filters;
	private HashMap<String, Master> masterFieldMap = new HashMap<String, Master>();
	private HashMap<String, Master> masterNameFieldMap = new HashMap<String, Master>();
	private HashMap<String, Object> masterFilterCheck = new HashMap<String, Object>();
	private HashMap<String, Lookup> masterLookupMap = new HashMap<String, Lookup>();

	private HashMap<String, String> aliasToName = new HashMap<String, String>();
	private HashMap<String, String> nameToAlias = new HashMap<String, String>();
	private HashMap<String, Class> typeMap = new HashMap<String, Class>();

	public static class Master {
		public String field, refTable, idField, nameField;
		public Master(String field, String refTable, String idField, String nameField) {
			this.field = field; this.refTable = refTable; this.idField = idField; this.nameField = nameField;
		}
	}
	
	public static class Lookup {
		public String field, idField, errorMsg;
		public Map map;
		public Lookup(String field, String idField, Map map, String errorMsg) {
			this.field = field; this.idField = idField; this.map = map; this.errorMsg = errorMsg;
		}
	}

	public SupplierRateDataHandler(String tableName, String[] keys, String[] fields, String[][] masters,
			String[] filters) {
		this.tableName = tableName;
		this.keys = keys;
		this.fields = fields;
		this.masters = masters;
		this.filters = filters;
		//this.allowInserts = allowInserts;

		if (fields != null) {
			allFields.addAll(Arrays.asList(keys));
			allFields.addAll(Arrays.asList(fields));
		}
		for (String[] m : masters) {
			Master ms = new Master(m[0],m[1],m[2],m[3]);
			masterFieldMap.put(ms.field, ms);
			masterNameFieldMap.put(ms.nameField, ms);
		}
	}

	public void setAlias(String name, String alias) {
		aliasToName.put(alias, name);
		nameToAlias.put(name, alias);
	}

	public void enforceType(String name, Class typeClass) {
		typeMap.put(name, typeClass);
	}
	
	public void setMasterDataForCorrValue(Object[][] data){
		for (Object[] d : data) {
			Lookup l = new Lookup((String)d[0], (String)d[1], (Map)d[2], (String)d[3]);
			masterLookupMap.put(l.field, l);
		}
	}

	public String getSequenceName() { return sequenceName; }
	public void setSequenceName(String v) { sequenceName = v; }

	public String getAutoIncrName() { return autoIncrName; }
	public void setAutoIncrName(String v) { autoIncrName = v; }

	public boolean isIdValAsString() {
		return idValAsString;
	}

	public void setIdValAsString(boolean idValAsString) {
		this.idValAsString = idValAsString;
	}

	public String importTable(InputStreamReader isReader, StringBuilder infoMsg, String supplierRateContractName)
		throws SQLException, IOException {

		CSVReader csvReader = new CSVReader(isReader);

		String[] header = csvReader.readNext();

		if (header.length < 1)
			return "Uploaded file does not appear to be a CSV file (no headers found)";

		if (!header[0].matches("\\p{Print}*"))
			return "Uploaded file does not appear to be a CSV file (non-printable characters found)";

		if (header.length == 1)
			return "Uploaded file appears to be using non-comma separators (maybe semi-colon or tab)";

		GenericDAO dao = new GenericDAO(tableName);
		BasicDynaBean bean = dao.getBean();

		StringBuilder warnings = new StringBuilder();
		int lineNum = 1;
		int lineWarnings = 0;
		int headerWarnings = 0;
		int numInserted = 0;
		int numUpdated = 0;
		boolean ignoreColumn[] = new boolean[header.length];

		for (int i=0; i<header.length; i++) {
			String fieldName = getRealFieldName(header[i].trim());
			if (!allFields.contains(fieldName)) {
				addWarning(warnings, 0, "Unknown property in header (ignoring data in column): " + header[i]);
				ignoreColumn[i] = true;
				// continue ... we will ignore unknown errors, but show a warning.
				headerWarnings++;
			} else {
				ignoreColumn[i] = false;
			}

			header[i] = fieldName;
		}

		Map<String, Map<String, String>> masterData = getMasterData();

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(true);		// we are committing after every record.
		String[] line = null;

		while ((line = csvReader.readNext()) != null) {
			lineNum++;
			logger.debug("Processing line: " + lineNum);
			bean = dao.getBean(con);
			boolean hasWarnings = false;
			int numNonEmptyColumns = 0;

			/*
			 * Convert the line into one bean
			 */
			for (int i=0; i<header.length && i<line.length; i++) {
				if (ignoreColumn[i])
					continue;

				String fieldName = header[i];
				logger.debug("Processing field: " + fieldName);
				String value = line[i].trim();
				String oldValue = value;

				if ((value != null) && !value.equals("")) {
					Map<String, String> masterValuesMap = masterData.get(fieldName);
					if (masterValuesMap != null) {
						String valueId = masterValuesMap.get(value.toLowerCase());
						if (valueId == null) {
							addWarning(warnings, lineNum, "No master value found for " + value +
									" ("+fieldName+")");
							hasWarnings = true;
							continue;		// next column
						}
						String filterCheckName = (String)masterFilterCheck.get(fieldName);
						if(filterCheckName != null && !filterCheckName.equals(value)) {
							addWarning(warnings, lineNum, "supported value is " + value + " for " +
									" ("+fieldName+")");
							hasWarnings = true;
							continue;
						}
						value = valueId;
					}

					DynaProperty property = null;
					try {
						property = bean.getDynaClass().getDynaProperty(fieldName);
						Class enforcedType = typeMap.get(fieldName);
						if (null != enforcedType) {
							// Convert to make sure that the type is convertible.
							// because of default setting in AppInit ConvertUtils.convert returns null
							// if failed to convert.
							if (ConvertUtils.convert(value, enforcedType) == null) {
								addWarning(warnings, lineNum, "Conversion error: " + value +
										" could not converted to " + (enforcedType == BigDecimal.class ? " Number " : enforcedType.getSimpleName()) +
										" for " + fieldName);
								hasWarnings = true;
								continue;
							}
						}
						logger.debug("Property type is " + property.getType() + ", value: " + value);
						
						if(fieldName.equals("discount")){
							boolean val = NumberUtils.isNumber(oldValue);
							if(val){
								if(Double.parseDouble(oldValue) > 100){
									addWarning(warnings, lineNum, "Invalid Discount Value Found - "+ oldValue + ": Valid value is upto - 100%");
									hasWarnings = true;
									continue;
								}
							}else{
								addWarning(warnings, lineNum, "Invalid Discount Value Found - "+ oldValue + ": Valid value is - Numeric value");
								hasWarnings = true;
								continue;
							}
							
						}
						if(fieldName.equals("mrp")){
							boolean val = NumberUtils.isNumber(oldValue);
							if(!val){
								addWarning(warnings, lineNum, "Invalid MRP Value Found - "+ oldValue + ": Valid value is - Numeric value ");
								hasWarnings = true;
								continue;
							}
							
						}
						
						if(fieldName.equals("supplier_rate_contract_id") && !oldValue.equals(supplierRateContractName) ) {
							addWarning(warnings, lineNum, "Invalid Contract Name Found - "+ oldValue + ": Valid - " + supplierRateContractName);
							hasWarnings = true;
							continue;
							//bean.set(fieldName, ConvertUtils.convert(value, property.getType()));
						} else {
							bean.set(fieldName, ConvertUtils.convert(value, property.getType()));
						}
						
						if(fieldName.equals("margin_type") && !ArrayUtils.contains(new String[]{"P","A"},  oldValue) ){
						  addWarning(warnings, lineNum, "Invalid margin type. Should be A or P.");
						  hasWarnings = true;
						}
						
					} catch (ConversionException e) {
						addWarning(warnings, lineNum, "Conversion error: " + value +
								" could not be converted to " + property.getType() +
								" for " + fieldName);
						hasWarnings = true;
						logger.error("Conversion error: ", e);
					}
					numNonEmptyColumns++;
				} else {
					// "" converted to null
					bean.set(fieldName, null);
				}
			}
			
			if (masterLookupMap != null && !masterLookupMap.isEmpty()) {
				for (DynaProperty property : bean.getDynaClass().getDynaProperties()) {
					if (masterLookupMap.get(property.getName()) != null) {
						Lookup l = masterLookupMap.get(property.getName());
						Map map = l.map;
						List list = (List)map.get(bean.get(l.idField));
						if (list == null || !list.contains(bean.get(l.field))) {
							hasWarnings = true;
							addWarning(warnings, lineNum, l.errorMsg);
						}							
					}
				}
			}

			if(bean.get("supplier_rate") == null && bean.get("margin") == null){
			  addWarning(warnings, lineNum, "Margin or Rate is required.");
			  hasWarnings = true;
			}
			
			if(bean.get("margin") != null && StringUtils.isEmpty((String) bean.get("margin_type"))){
			  addWarning(warnings, lineNum, "Margin type cannot be empty.");
			  hasWarnings = true;
			}

      if (bean.get("margin") != null && bean.get("margin_type") != null
          && bean.get("margin_type").equals("P") && ((BigDecimal) bean.get("margin")).compareTo(new BigDecimal(100)) > 0) {
        addWarning(warnings, lineNum, "Margin cannot exceed 100%.");
        hasWarnings = true;
      }
			
			if (hasWarnings) {
				lineWarnings++;
				continue;		// next line.
			}

			if (numNonEmptyColumns == 0)
				continue;		// empty line, nothing to do here.

			// Create the key value map for updates
			HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
			boolean allKeysGiven = true;
			for (String key : keys) {
				keyValueMap.put(key, bean.get(key));
				if (bean.get(key) == null)
					allKeysGiven = false;
			}

			try {
				if (keys.length == 1) {
					// single key: update if key given, else insert.
					if (allKeysGiven) {
						int rows = dao.update(con, bean.getMap(), keyValueMap);
						if (rows != 0) {
							numUpdated++;
						} else {
							dao.insert(con, bean);
							numInserted++;
						}

						/* if (rows == 0) {
							addWarning(warnings, lineNum, "Object with given key not found: " + keys[0] + "="
									+ bean.get(keys[0]));
							lineWarnings++;
						} else {
							numUpdated++;
						} */
					} else {
						if (sequenceName != null && autoIncrName != null) {
							bean.set(keys[0],
									AutoIncrementId.getSequenceId(sequenceName, autoIncrName));
						} else if (sequenceName != null) {
							// idValAsString is set to true, when we have an id(primary key) coloumn of the table is character varying type in the table design
							bean.set(keys[0], idValAsString ? ((Integer)DataBaseUtil.getNextSequence(sequenceName)).toString() : DataBaseUtil.getNextSequence(sequenceName));
						} else if (autoIncrName != null) {
							bean.set(keys[0],
									AutoIncrementId.getNewIncrUniqueId(keys[0], tableName, autoIncrName));
						}
						dao.insert(con, bean);
						numInserted++;
					}

				} else {
					if (allKeysGiven) {
						// try an update. If it fails, do an insert.
						int rows = dao.update(con, bean.getMap(), keyValueMap);
						if (rows != 0) {
							numUpdated++;
						} else {
							dao.insert(con, bean);
							numInserted++;
						}
					} else {
						// we don't allow inserts for multiple keys by autogeneration
						addWarning(warnings, lineNum, "Key fields not given");
						lineWarnings++;
					}
				}
			} catch (SQLException sqle) {
				if (DataBaseUtil.isDuplicateViolation(sqle)) {
					addWarning(warnings, lineNum, "Duplicate record found");
				} else {
					addWarning(warnings, lineNum, "Unknown error: " + sqle.getMessage());
					logger.error("Error uploading csv line", sqle);
				}
				lineWarnings++;
			}

		}	// end while each line

		con.close();

		infoMsg.append("Processed lines: ").append(lineNum-1).append("<br>");
		if (numInserted > 0) {
			infoMsg.append("New rows inserted: ").append(numInserted).append("<br>");
		}
		if(numUpdated > 0) {
			infoMsg.append("Existing records updated: ").append(numUpdated).append("<br>");
		}
		if (lineWarnings > 0 || headerWarnings > 0) {
			if (lineWarnings > 0)
				infoMsg.append("Lines with errors: ").append(lineWarnings);
			if (headerWarnings > 0)
				infoMsg.append("Headers with errors: ").append(headerWarnings);

			infoMsg.append("<br>") ;
			infoMsg.append("<hr>");
			infoMsg.append(warnings);
		}
		logger.info("{}", infoMsg);
		return null;
	}

	public void exportTable(HttpServletResponse res) throws SQLException, java.io.IOException {
		exportTable(res, null);
	}

	public void exportTable(HttpServletResponse res, String fileName) throws SQLException, java.io.IOException {
		// form the query based on the master data
		StringBuilder query = new StringBuilder();
		query.append("SELECT");
		if (fields == null) {
			query.append(" *");
		} else {
			boolean first = true;
			for (String f : allFields) {
				query.append(first ? " " : ", ");
				Master ms = masterFieldMap.get(f);
				if (ms != null) {
					query.append(ms.refTable).append(".").append(ms.nameField);
				} else {
					query.append(tableName).append(".").append(f);
				}
				String alias = nameToAlias.get(f);
				if (alias != null) {
					query.append(" AS ").append(alias);
				}
				first = false;
			}
		}
		query.append("\nFROM ").append(tableName);
		for (Master ms : masterFieldMap.values()) {
			query.append("\n LEFT JOIN ").append(ms.refTable).append(" ON ")
				.append(tableName).append(".").append(ms.field).append(" = ")
				.append(ms.refTable).append(".").append(ms.idField);
		}

		if (filters != null) {
			boolean first = true;
			for (String filter : filters) {
				query.append(first ? "\nWHERE " : " AND " );
				query.append(filter);
				first = false;
			}
		}

		// order by the primary keys in the given order.
		query.append("\nORDER BY");
		boolean first = true;
		for (String k : keys) {
			query.append(first ? " " : ", ");
			query.append(tableName).append(".").append(k);
			first = false;
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// run the query and stuff it into the writer.
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false); // required for setFetchSize to work
			ps = con.prepareStatement(query.toString());
			ps.setFetchSize(1000); // fetch only 1000 rows at a time
			rs = ps.executeQuery();

			res.setHeader("Content-type", "application/csv");
			res.setHeader("Content-disposition", "attachment; filename="+ (null == fileName ? tableName : fileName) +".csv");

			CSVWriter writer = new CSVWriter(res.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
			writer.writeAll(rs, true);
			writer.flush();

		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

	}

	private Map getMasterData() throws SQLException {
		Map masterData = new HashMap();
		for (Master ms : masterFieldMap.values()) {
			List<BasicDynaBean> beans =
				new GenericDAO(ms.refTable).listAll(Arrays.asList(new String[]{ms.idField, ms.nameField}));
			Map beanMap = new HashMap();
			for (BasicDynaBean b : beans) {
				// name : id, eg: Bangalore : CT0001
				String name = (String) b.get(ms.nameField);
				if (name != null)		// avoid bad data in masters
					beanMap.put(name.toLowerCase(), b.get(ms.idField).toString());
			}
			// field : beanMap, eg: patient_city : { Bangalore: CT0001, ...}
			masterData.put(ms.field, beanMap);
		}
		return masterData;
	}

	private void addWarning(StringBuilder warnings, int line, String msg) {
		if (line > 0) {
			warnings.append("Line ").append(line).append(": ");
		} else {
			warnings.append("Error in header: ");
		}
		warnings.append(msg).append("<br>");
		logger.warn("Line " + line + ": " + msg);
	}

	/*
	 * Fetch the real field name (as in the main table) given the spreadsheet name. This could be:
	 * (a) aliased: the spreadsheet name is an alias for the real field in the DB
	 * (b) master: the spreadsheet name is the field name of the "name" field in the master
	 * When the two are combined (eg, org_id -> (master) org_name -> (alias) rate_plan,
	 *  we only use the alias (org_id -> rate_plan)
	 */
	private String getRealFieldName(String fieldName) {
		String realName = aliasToName.get(fieldName);
		if (realName != null)
			return realName;

		Master ms = masterNameFieldMap.get(fieldName);
		if (ms != null)
			return ms.field;

		return fieldName;
	}

	public HashMap<String, Object> getMasterFilterCheck() {
		return masterFilterCheck;
	}

	public void setMasterFilterCheck(HashMap<String, Object> masterFilterCheck) {
		this.masterFilterCheck = masterFilterCheck;
	}

}
