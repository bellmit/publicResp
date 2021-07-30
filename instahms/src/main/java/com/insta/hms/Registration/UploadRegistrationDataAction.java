package com.insta.hms.Registration;

import au.com.bytecode.opencsv.CSVReader;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.DepartmentUnitMaster.DepartmentUnitMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.SalutationMaster.SalutationMasterDAO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadRegistrationDataAction extends DispatchAction {

    static Logger logger = LoggerFactory.getLogger(
			UploadRegistrationDataAction.class);

	private static Map<String, String> aliasMap;

  private static int deptNameColIndex = -1;

	private static final String[][] masters = {
		// fieldname              table                     idfield          namefield
		{ "patient_city",        "city",                    "city_id",       "city_name" },
		{ "patient_state",       "state_master",            "state_id",      "state_name" },
		{ "salutation",          "salutation_master",       "salutation_id", "salutation" },
		{ "org_id",              "organization_details",    "org_id",        "org_name" },
		{ "patient_category_id", "patient_category_master", "category_id",   "category_name" },
		{ "dept_name",           "department",              "dept_id",       "dept_name" },
		{ "doctor",              "doctors",                 "doctor_id",     "doctor_name" },
		{ "unit_id",           "dept_unit_master",        "unit_id",       "unit_name" },
		{ "custom_list1_value",  "custom_list1_master",     "custom_value",  "custom_value" },
		{ "custom_list2_value",  "custom_list2_master",     "custom_value",  "custom_value" },
		{ "custom_list3_value",  "custom_list3_master",     "custom_value",  "custom_value" },
		{ "custom_list4_value",  "custom_list4_master",     "custom_value",  "custom_value" },
		{ "custom_list5_value",  "custom_list5_master",     "custom_value",  "custom_value" },
		{ "custom_list6_value",  "custom_list6_master",     "custom_value",  "custom_value" },
		{ "custom_list7_value",  "custom_list7_master",     "custom_value",  "custom_value" },
		{ "custom_list8_value",  "custom_list8_master",     "custom_value",  "custom_value" },
		{ "custom_list9_value",  "custom_list9_master",     "custom_value",  "custom_value" },
		{ "visit_custom_list1", "custom_visit_list1_master","custom_value",  "custom_value" },
		{ "visit_custom_list2", "custom_visit_list2_master","custom_value",  "custom_value" },
	};

	static {
		aliasMap = new HashMap();
		// name : field_name in db
		aliasMap.put("title", "salutation");
		aliasMap.put("city", "patient_city");
		aliasMap.put("state", "patient_state");
		aliasMap.put("area", "patient_area");
		aliasMap.put("unit", "unit_id");
		aliasMap.put("phone", "patient_phone");
		aliasMap.put("mobile_number", "patient_phone2");

		aliasMap.put("rate_plan", "org_id");
		aliasMap.put("department", "dept_name");
		aliasMap.put("registration_datetime", "reg_date");
	}

	private StringBuilder errors;
	private int numErrors;

	/*
	 * The following are used to store the last value, and increments the value from
	 * there on to generate new ids for backdated registrations. see getPatternId.
	 */
	// patternId: {prefix: lastVal}
	private Map<String, Map<String, Integer>> patternPrefixValues;
	// patternId: {date: prefix}
	private Map<String, Map<java.sql.Date, String>> patternDatePrefixes;

	private Map getMasterData() throws SQLException {
		Map masterData = new HashMap();
		for (String[] m : masters) {
			String fieldName = m[0];
			String tableName = m[1];
			String idField = m[2];
			String nameField = m[3];
			List<BasicDynaBean> beans = 
				new GenericDAO(tableName).listAll(Arrays.asList(new String[]{idField, nameField}),
						"status", "A");
			Map beanMap = new HashMap();
			for (BasicDynaBean b : beans) {
				// name : id, eg: Bangalore : CT0001
				String name = (String) b.get(nameField);
				beanMap.put(name.toLowerCase(), b.get(idField).toString());
			}
			// field : beanMap, eg: patient_city : { Bangalore: CT0001, ...}
			masterData.put(fieldName, beanMap);
		}
		return masterData;
	}

	private Map<String,Map<String,List<Map<String,Object>>>> getMasterMappings() throws SQLException {
		List<BasicDynaBean> doctorDeptMappings = DatabaseHelper
				.queryToDynaList(GET_DOCTOR_DEPT_MAPPING);
		List<BasicDynaBean> unitDepartmentMappings = DatabaseHelper
				.queryToDynaList(GET_UNIT_DEPARTMENT_MAPPING);
		Map<String,Map<String,List<Map<String,Object>>>> masterMappings = new HashMap();
    Map<String,List<Map<String,Object>>> docMappings = new HashMap<>();
		for(BasicDynaBean bean : doctorDeptMappings) {
			List<Map<String,Object>> list = null;
		  if(docMappings.containsKey(((String)bean.get("doctor_name")).toLowerCase())) {
		    list = docMappings.get(((String) bean.get("doctor_name")).toLowerCase());
		    list.add(bean.getMap());
      } else {
		    list = new ArrayList<>();
		    list.add(bean.getMap());
        docMappings.put(((String)bean.get("doctor_name")).toLowerCase(), list);
      }
    }
    Map<String,List<Map<String,Object>>> unitMappings = new HashMap<>();
    for(BasicDynaBean bean : unitDepartmentMappings) {
			List<Map<String,Object>> list = null;
			if(unitMappings.containsKey(((String)bean.get("unit_name")).toLowerCase())) {
        list = unitMappings.get(((String) bean.get("unit_name")).toLowerCase());
        list.add(bean.getMap());
      } else {
        list = new ArrayList<>();
        list.add(bean.getMap());
        unitMappings.put(((String)bean.get("unit_name")).toLowerCase(), list);
      }

    }
		masterMappings.put("doctor",docMappings);
		masterMappings.put("unit_id", unitMappings);
		return masterMappings;
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		return m.findForward("add");
	}

	public ActionForward importRegistrationCSVData(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		UploadRegistrationDataForm rdf = (UploadRegistrationDataForm) f;
		CSVReader csvReader = new CSVReader(new InputStreamReader(rdf.getCsvFile().getInputStream()));

		FlashScope flash = FlashScope.getScope(req);
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String[] header = csvReader.readNext();

		String error = null;

validate:
		{
			if (header.length < 1) {
				error = "Uploaded file does not appear to be a CSV file (no headers found)";
				break validate;
			}

			if (!header[0].matches("\\p{Print}*")) {
				error = "Uploaded file does not appear to be a CSV file (non-printable characters found)";
				break validate;
			}
		}

		if (error != null) {
			flash.put("error", error);
			return redirect;
		}

		// trim all the headers: common mistake by people
		for (int i=0; i<header.length; i++) {
			header[i] = header[i].trim();
      if (header[i].equals("department")) {
        deptNameColIndex = i;
      }
		}

		PatientDetailsDAO pdao = new PatientDetailsDAO();
		VisitDetailsDAO  rdao = new VisitDetailsDAO();
		BasicDynaBean patientBean 	= pdao.getBean();
		BasicDynaBean visitBean 	= rdao.getBean();

		this.errors = new StringBuilder();
		this.numErrors = 0;
		this.patternDatePrefixes = new HashMap();
		this.patternPrefixValues = new HashMap();

		for (int i=0; i<header.length; i++) {
			String fieldName = header[i];
			String aliasedName = aliasMap.get(fieldName);
			if (aliasedName != null)
				fieldName = aliasedName;
			if ( (patientBean.getDynaClass().getDynaProperty(fieldName) == null)
				&& (visitBean.getDynaClass().getDynaProperty(fieldName) == null) ) {
				addError(0, "Unknown property in header (ignoring data in column): " + header[i]);
				// continue ... we will ignore unknown errors, but show a warning.
			}
		}

		/*
		 * Get all required master data for reference during processing
		 */
		Map<String, Map<String, String>> masterData = getMasterData();
    Map<String,Map<String,List<Map<String,Object>>>>  masterMappings  = getMasterMappings();

		List<BasicDynaBean> categoryList = new PatientCategoryDAO().listAll();
		Map<Integer, BasicDynaBean> categoryMaster = 
			ConversionUtils.listBeanToMapBean(categoryList, "category_id");

		// initial variables
		int lineNum = 1;
		int numNewPatients = 0;
		int numNewVisits = 0;
		String firstMrNo = null;
		String lastMrNo = null;
		String firstVisitId = null;
		String lastVisitId = null;
		Connection con = null;

		String[] line = null;
		while ((line = csvReader.readNext()) != null) {
			lineNum++;
			logger.debug("Processing line: " + lineNum);
			patientBean = pdao.getBean();
			visitBean = rdao.getBean();
			boolean hasErrors = false;
			int numNonEmptyColumns = 0;

			/*
			 * Convert the line into one patient and one visit bean
			 */
			for (int i=0; i<header.length && i<line.length; i++) {
				String fieldName = header[i];
				String aliasedName = aliasMap.get(fieldName);
				if (aliasedName != null)
					fieldName = aliasedName;

				String value = line[i].trim();
				if ((value != null) && !value.equals("")) {
					Map<String, String> masterMap = masterData.get(fieldName);
					String valueId = null;
					if (masterMap != null) {
					  if(fieldName.equals("doctor")) {
              Map<String,List<Map<String,Object>>> mappings = masterMappings.get(fieldName);
              List<Map<String,Object>> doctorswithSameName = mappings.containsKey(value.toLowerCase())
							    ? mappings.get(value.toLowerCase()) : new ArrayList<Map<String, Object>>();
							  for(Map m1 : doctorswithSameName) {
								  if(((String)m1.get("doctor_name")).toLowerCase().equals(value.toLowerCase())
									  && ((String)m1.get("dept_name")).toLowerCase()
                    .equals(line[deptNameColIndex].toLowerCase())) {
								      valueId = (String) m1.get("doctor_id");
										}
									}
							}
						if(fieldName.equals("unit_id")) {
							Map<String,List<Map<String,Object>>>  mappings = masterMappings.get(fieldName);
							List<Map<String,Object>> unitsWithSameName = mappings.containsKey(value.toLowerCase())
									? mappings.get(value.toLowerCase()) : new ArrayList<Map<String, Object>>();
                for(Map m1 : unitsWithSameName) {
								  if(((String)m1.get("unit_name")).toLowerCase().equals(value.toLowerCase())
                    && ((String)m1.get("dept_name")).toLowerCase()
                    .equals(line[deptNameColIndex].toLowerCase())) {
										  valueId = String.valueOf(m1.get("unit_id"));
								  }
                }
							}
						if (valueId == null && !fieldName.equals("unit_id") && !fieldName.equals("doctor")) {
						  valueId = masterMap.get(value.toLowerCase());
						}
						if (valueId == null) {
							addError(lineNum, "No master value found for " + value + " ("+fieldName+")");
							hasErrors = true;
							continue;		// next column
						}
						value = valueId;
					}

					DynaProperty property = null;
					try {
						property = patientBean.getDynaClass().getDynaProperty(fieldName);
						if (property != null) {
							patientBean.set(fieldName, ConvertUtils.convert(value, property.getType()));
						} 
						property = visitBean.getDynaClass().getDynaProperty(fieldName);
						if (property != null) {
							visitBean.set(fieldName, ConvertUtils.convert(value, property.getType()));
						}

					} catch (ConversionException e) {
						addError(lineNum, "Conversion error: " + value + 
								" could not be converted to " + property.getType() + 
								" for " + fieldName);
						hasErrors = true;
						logger.error("Conversion error: ", e);
					}
					numNonEmptyColumns++;
				}
			}
			if(visitBean.get("dept_name") == null || "".equals((String)visitBean.get("dept_name"))) {
			  addError(lineNum, "Department is required");
			  hasErrors = true;
			}
			if(!validateDeptDoctorMapping((String)visitBean.get("doctor"),
			    (String)visitBean.get("dept_name"))) {
			  addError(lineNum, "Doctor and Department mapping is not correct");
			  hasErrors = true;
			}
			if (!validateDeptUnitMapping((String) visitBean.get("dept_name"),
			    (Integer) visitBean.get("unit_id"))) {
			  addError(lineNum, "Unit and Department mapping is not correct");
			  hasErrors = true;
			  }
			if (!validateSaluatuinGenderMapping((String) patientBean.get("salutation"),
			    (String) patientBean.get("patient_gender"))) {
			  addError(lineNum, "Salutation and Gender mapping is not correct");
			  hasErrors = true;
			  }
			if (visitBean.get("reg_date") != null
			    && ((java.sql.Date) visitBean.get("reg_date")).after(DateUtil.getCurrentDate())) {
			  addError(lineNum, "Registration date can not be in future");
			  hasErrors = true;
			  }
						  

			if (hasErrors) {
				this.numErrors++;
				continue;		// next line.
			}

			if (numNonEmptyColumns == 0)
				continue;

			/*
			 * set some defaults
			 */
			if (patientBean.get("patient_category_id") == null)
				patientBean.set("patient_category_id", 1);

			if (visitBean.get("visit_type") == null)
				visitBean.set("visit_type", "o");
			if (visitBean.get("bed_type") == null)
				visitBean.set("bed_type", "GENERAL");
			if (visitBean.get("status") == null)
				visitBean.set("status", "I");
			if (visitBean.get("reg_date") == null)
				visitBean.set("reg_date", DateUtil.getCurrentDate());
			if (visitBean.get("reg_time") == null)
				visitBean.set("reg_time", DateUtil.getCurrentTime());

			patientBean.set("first_visit_reg_date", visitBean.get("reg_date"));

			BasicDynaBean existingPatient = null;
			String mrNo = (String) patientBean.get("mr_no");

			if (visitBean.get("revisit") == null) {
				if (mrNo == null)
					visitBean.set("revisit", "N");
				else
					visitBean.set("revisit", "Y");
			}

			if (mrNo == null) {
				/*
				 * Based on patient category, we may need a different sequence number
				 */
				String patternId = "MRNO";		// normal MR No. pattern id
				int catId = (Integer) patientBean.get("patient_category_id");
				if (catId != 0) {
					// check if we need a special pattern id for this category
					BasicDynaBean category = categoryMaster.get(catId);
					if (category.get("seperate_num_seq").equals("Y")) {
						patternId = (String) category.get("code");
					}
				}

				mrNo = getPatternId(patternId, (java.sql.Date) patientBean.get("first_visit_reg_date"), 
						"patient_details", "mr_no");

				if (firstMrNo == null) {
					firstMrNo = mrNo;
				}
				lastMrNo = mrNo;
				patientBean.set("mr_no", mrNo);
				visitBean.set("mr_no", mrNo);

			} else {
				// cannot use findByKey since patient_details has a bytea column.
				List<BasicDynaBean> records = pdao.listAll(
						Arrays.asList(new String[]{"mr_no", "visit_id"}), "mr_no", mrNo);
				if (records!=null && records.size() > 0)
					existingPatient = records.get(0);
			}

			if (visitBean.get("status").equals("A")) {
				// inserting an "active" patient. Need to check if patient is already active 
				if ((existingPatient != null) && existingPatient.get("visit_id") != null) {
					addError(lineNum, "Active visit exists for MRNO");
					this.numErrors++;
					continue;
				}
			}

			int centerId = RequestContext.getCenterId();
			visitBean.set("center_id", centerId);

			String patientId = getVisitPatternId((String) visitBean.get("visit_type"), centerId);
			visitBean.set("patient_id", patientId);
			if (firstVisitId == null)
				firstVisitId = patientId;
			lastVisitId = patientId;

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				/*
				 * Create the patient_details record, if new patient
				 */
				if (existingPatient == null) {
					pdao.insert(con, patientBean);
					numNewPatients++;
				}

				/*
				 * Create the patient_registration record
				 */
				rdao.insert(con, visitBean);
				numNewVisits++;

			} catch (SQLException sq) {
				addError(lineNum, "Transaction Failure during patient creation");
				logger.error("Transaction failure: ", sq);
				con.rollback();
				this.numErrors++;
				continue;
			}
			finally{
				con.commit();
				con.close();
			}
		}

    getPatternId("MRNO", (java.sql.Date) DateUtil.getCurrentDate(), "patient_details", "mr_no");

		StringBuilder infoMsg = new StringBuilder();
		infoMsg.append("Processed lines: ").append(lineNum-1).append("<br>");
		infoMsg.append("New patients added: ").append(numNewPatients);
		if (numNewPatients > 0) {
			infoMsg.append(", MR Nos: ").append(firstMrNo).append(" - ").append(lastMrNo);
		}
		infoMsg.append("<br>");
		infoMsg.append("New visits added: ").append(numNewVisits);
		if (numNewVisits > 0) {
			infoMsg.append(", Visit IDs: ").append(firstVisitId).append(" - ").append(lastVisitId);
		}
		infoMsg.append("<br>");

		if (this.numErrors > 0) {
			infoMsg.append("Lines with errors: ").append(this.numErrors).append("<br>") ;
			infoMsg.append("<hr>");
		}
		infoMsg.append(this.errors);
		logger.info("{}", infoMsg);

		flash.put("info",infoMsg.toString());
		return redirect;
	}

  private boolean validateDeptDoctorMapping(String doctorId, String deptId) throws SQLException {
    if (doctorId == null || deptId == null) {
      return true;
    }
    DoctorMasterDAO doctorDao = new DoctorMasterDAO();
    Map<String, Object> keys = new HashMap<>();
    keys.put("doctor_id", doctorId);
    keys.put("dept_id", deptId);
    return doctorDao.findByKey(keys) != null;
  }
  
  private boolean validateDeptUnitMapping(String deptId, Integer unitId) throws SQLException {
    if (unitId == null || deptId == null) {
      return true;
    }
    DepartmentUnitMasterDAO unitDao = new DepartmentUnitMasterDAO();
    Map<String, Object> keys = new HashMap<>();
    keys.put("unit_id", unitId);
    keys.put("dept_id", deptId);
    return unitDao.findByKey(keys) != null;
  }
  
  private boolean validateSaluatuinGenderMapping(String salId, String gender) throws SQLException {
    if (salId == null || gender == null) {
      return true;
    }
    SalutationMasterDAO salDao = new SalutationMasterDAO();
    Map<String, Object> keys = new HashMap<>();
    BasicDynaBean salBean = salDao.findByKey("salutation_id", salId);
    return salBean.get("gender") == null || "".equals((String) salBean.get("gender"))
        || gender.equals((String) salBean.get("gender"));
  }

	private void addError(int line, String msg) {
		if (line > 0) {
			this.errors.append("Line ").append(line).append(": ");
		} else {
			this.errors.append("Error in header: ");
		}
		this.errors.append(msg).append("<br>");
		logger.error("Line " + line + ": " + msg);
	}

	/*
	 * Get the pattern based ID, taking care of back-dating. If the date is today
	 * or future, we return the usual pattern generation. If the pattern does not contain any
	 * date prefix also we return the usual pattern generation. This ensures that IDs generated
	 * by UI after this do not clash with the IDs we generated here.
	 *
	 * If the date is in the past, and the pattern includes a date, then we generate the ID
	 * based on max values in the DB instead of using the sequence, for the prefix that will
	 * be generated for that date.
	 *
	 * Note that two different dates can result in the same prefix if the pattern is weekly/monthly
	 * etc. Therefore, we need to go from patternId to prefix based on date, and then the last value
	 * based on the prefix.
	 *
	 * This takes care of daily sequence change, weekly, monthly etc. Sequence changes within
	 * a day are not handled, it is not expected that hosp_id_patterns will handle within
	 * a day sequence change (eg, 6-hourly etc.).
	 */
	private static final String GET_PREFIX = 
		" SELECT std_prefix || coalesce(to_char(?::date, date_prefix_pattern),'') " +
		" FROM hosp_id_patterns WHERE pattern_id=? AND date_prefix_pattern != ''";

	private static final String GET_DATE_NUM_PATTERN_ID = " SELECT generate_id(?,?,?)";

	private String getPatternId(String patternId, java.sql.Date curdt, 
			String table, String field) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		String prefix = null;
		Integer lastValue = null;
		boolean requireUdatSequence = true;

		Map<java.sql.Date, String> datePrefixes = this.patternDatePrefixes.get(patternId);
		if (datePrefixes == null) {
			datePrefixes = new HashMap();
			this.patternDatePrefixes.put(patternId, datePrefixes);
		}

		prefix = datePrefixes.get(curdt);

		if (prefix == null) {
			// fetch the prefix associated with this date and save it
			PreparedStatement ps = con.prepareStatement(GET_PREFIX);
			ps.setDate(1, curdt);
			ps.setString(2, patternId);

			prefix = DataBaseUtil.getStringValueFromDb(ps);
			ps.close();

			if (prefix == null)
				prefix = "";
			datePrefixes.put(curdt, prefix);
		}

		if (prefix.equals("")) {
			// not a date pattern, so do normal pattern generation
			// (in reality, having a date pattern is an attribute of the pattern itself, not of the date.
			//  but we keep it per date to simplify the code, and we don't expect too many dates to
			//  come in one batch of upload)
			logger.debug("Not a date pattern, generating regular ID: " + patternId + ": " + curdt);
			con.close();
			requireUdatSequence =false;
			return DataBaseUtil.getNextPatternId(patternId);
		}

		logger.debug("Generating date based ID for: " + patternId + " Date: " + curdt.getTime() + " cur: "+
				DateUtil.getCurrentDate().getTime());

		Map<String, Integer> prefixValues = this.patternPrefixValues.get(patternId);
		if (prefixValues == null) {
			prefixValues = new HashMap();
			this.patternPrefixValues.put(patternId, prefixValues);
		}

		lastValue = prefixValues.get(prefix);

		if (lastValue == null) {
			// fetch the last value for this prefix and save it
			// since all the inputs are passed in (ie, not user inputs), it is ok to concatenate
			PreparedStatement ps = con.prepareStatement("SELECT max(CAST(COALESCE(NULLIF(REGEXP_REPLACE("+field+", '[^0-9]+', '', 'g'), ''), '0') AS BIGINT)) FROM " 
					+ DataBaseUtil.quoteIdent(table) + " WHERE " + DataBaseUtil.quoteIdent(field) + " LIKE ?");
			ps.setString(1, prefix + "%");
			String maxId = DataBaseUtil.getStringValueFromDb(ps);
			//split string from prefix and append to maxid to make field value
			String prefixId = prefix.replaceAll("[^a-zA-Z]+", "");
			if(maxId != null && !maxId.equals(""))
				maxId = prefixId.concat(maxId);
			ps.close();

			if (maxId == null) {
				// no records: need to start from beginning
				lastValue = new Integer(0);
			} else {
				String numPortion = maxId.substring(prefix.length());
				lastValue = Integer.parseInt(numPortion);
			}
			prefixValues.put(prefix, lastValue);
			requireUdatSequence = true;
		}

		/*
		 * Get the ID by calling generate_id, but use the 3-parameter version that takes
		 * the number and date. It also does not use/affect the sequence.
		 */
		Integer nextVal = lastValue + 1;

		PreparedStatement ps = con.prepareStatement(GET_DATE_NUM_PATTERN_ID);
		ps.setString(1, patternId);
		ps.setDate(2, curdt);
		ps.setInt(3, nextVal);

		String generatedId = DataBaseUtil.getStringValueFromDb(ps);
		ps.close();

		prefixValues.put(prefix, nextVal);		// replace the saved value with the new one
		if(requireUdatSequence)
			updateSeq(con,patternId,nextVal);
		DataBaseUtil.closeConnections(con, null);
		return generatedId;
	}

	private void updateSeq(Connection con,String patternId, Integer nextVal) throws SQLException {
		BasicDynaBean hospIdPatterns;
		PreparedStatement ps = null;
		try {
			hospIdPatterns = new GenericDAO("hosp_id_patterns").findByKey("pattern_id", patternId);
			if(hospIdPatterns !=  null){
				String seqName = (String) hospIdPatterns.get("sequence_name");
				if(seqName != null && !seqName.equals("")){
					ps = con.prepareStatement("ALTER SEQUENCE "+seqName+" RESTART WITH "+nextVal);
					ps.executeUpdate();
				}	
			}	
		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	private String getVisitPatternId(String visit_type, int centerId) throws SQLException {
		
		String patternId = getvisitSequencePatternId(visit_type, centerId);
		return DataBaseUtil.getNextPatternId(patternId);
	}

	private static final String VISITID_SEQUENCE_PATTERN = " SELECT pattern_id FROM hosp_op_ip_seq_prefs "
			+ " WHERE priority = ( " + "  SELECT min(priority) FROM hosp_op_ip_seq_prefs "
			+ "  WHERE (visit_type = ?) AND " + " (center_id = ? OR center_id = 0) " + " ) ";
	
	private String getvisitSequencePatternId(String visit_type, int centerId) throws SQLException {
		Connection con = null;
		String patternId;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(VISITID_SEQUENCE_PATTERN);
			ps.setString(1, visit_type);
			ps.setInt(2, centerId);
			patternId = DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return patternId;
	}

	private static  final String GET_DOCTOR_DEPT_MAPPING = "select d.doctor_id, d.doctor_name, "
			+ " dp.dept_name from doctors d join department dp on(d.dept_id=dp.dept_id) where "
			+ " d.status='A' and dp.status='A'";

	private static final String GET_UNIT_DEPARTMENT_MAPPING= "select du.unit_id, "
			+ " du.unit_name, dp.dept_name from dept_unit_master du join department dp "
			+ " on(du.dept_id=dp.dept_id) where du.status ='A' and dp.status = 'A'";
}