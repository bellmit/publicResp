/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.confidentialitycheck.ConfidentialityQueryHelper;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.outpatientlist.Patient.OtherDetails;
import com.insta.hms.core.scheduler.ResourceRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat
 *
 */
@Repository
public class PatientSearchRepository extends GenericRepository{
	
	static Logger logger = LoggerFactory.getLogger(PatientSearchRepository.class);

	@LazyAutowired
	private GenericPreferencesService genPrefService;
	
  @LazyAutowired
  private ResourceRepository resourceRepository;  

	@LazyAutowired
	private SessionService sessionService;

	private final List<String> searchOnValues = Arrays.asList("mr_no", "name", "government_identifier", "phone_number");
	private final List<String> matchTypeValues = Arrays.asList("ends_with", "starts_with", "contains", "exact_match");
	private Map<String, String> defaultMatchType = new HashMap<String, String>();
	private Map<String, Object> fieldMap = new HashMap<String, Object>();
	private Map<String, String> sortMap = new HashMap<String, String>();
	private static String TOKEN_WRAPPER_HEAD = "select r.entity_id from (" ;
	private static String TOKEN_WRAPPER_TAIL = ") r group by r.entity_id having count(r.entity_id)=?";
	private static String TOKEN_SEARCH = "SELECT distinct entity_id FROM patient_search_tokens "
			+ " where token like ? and not reversed and entity='#'";
	
	public PatientSearchRepository() {
		super("op_patient_list");
		defaultMatchType.put("mr_no", "ends_with");
		defaultMatchType.put("name", "contains");
		defaultMatchType.put("phone_number", "starts_with");
		defaultMatchType.put("government_identifier", "starts_with");
		sortMap.put("mr_no", "pd.mr_no");
		sortMap.put("name", "pd.patient_name, pd.middle_name, pd.last_name");
		sortMap.put("phone_number", "pd.patient_phone");
		sortMap.put("government_identifier", "pd.government_identifier");
		fieldMap.put("mr_no", Arrays.asList("pd.mr_no", "COALESCE(pd.oldmrno, '')"));
		fieldMap.put("name", Arrays.asList("LOWER(pd.patient_name)", "LOWER(pd.middle_name)", "LOWER(pd.last_name)"));
		fieldMap.put("phone_number", Arrays.asList("pd.patient_phone", "replace(pd.patient_phone, CASE WHEN pd.patient_phone_country_code IS NULL THEN '' ELSE pd.patient_phone_country_code END,'')"));
		fieldMap.put("government_identifier", "LOWER(pd.government_identifier)");
	}
	
	
	private List<String> getArgsList(String[] values) {
		List<String> response = new ArrayList<String>();
		List<String> temp = values != null ? Arrays.asList(values) : new ArrayList<String>();
		for (String k : temp) {
			if (k != null && !k.trim().isEmpty() && !k.trim().equalsIgnoreCase("*")) {
				response.add(k.trim());
			}
		}
		return response;
	}
		
	public List<String> getArgsList(List<String> values) {
		List<String> response = new ArrayList<String>();
		List<String> temp = values != null ? values : new ArrayList<String>();
		for (String k : temp) {
			if (k != null && !k.trim().isEmpty() && !k.trim().equalsIgnoreCase("*")) {
				response.add(k.trim());
			}
		}
		return response;
	}
		
	private boolean selectAllInList(String[] values) {
		return values != null && values.length == 1 && 
				(values[0].equalsIgnoreCase("*") || values[0].trim().isEmpty());
	}
	
	private boolean selectAllInList(List<String> values) {
		return values != null && values.size() == 1 && 
				(values.get(0).equalsIgnoreCase("*") || values.get(0).trim().isEmpty());
	}

	private boolean parseBool(String values) {
		return values != null && Arrays.asList("true", "y", "yes", "1").contains(values.toLowerCase());
	}
	
	public String getQuery(String table, List<String> fields, List<String> joins, 
	    List<String> pureJoins, List<String> filters, List<String> groupBy, List<String> sortOn) {
		List<String> queryParts = new ArrayList<String>(Arrays.asList("SELECT"));
		queryParts.add(StringUtils.collectionToDelimitedString(fields, ", "));
		queryParts.add("FROM " + table);
        if (pureJoins != null && !pureJoins.isEmpty()) {
          queryParts.add("JOIN " + StringUtils.collectionToDelimitedString(pureJoins, " JOIN "));
        }
		if (joins != null && !joins.isEmpty()) {
			queryParts.add("LEFT JOIN " + StringUtils.collectionToDelimitedString(joins, " LEFT JOIN "));
		}
		if (filters != null && !filters.isEmpty()) {
			queryParts.add("WHERE " + StringUtils.collectionToDelimitedString(filters, " AND "));
		}
		if (groupBy != null && !groupBy.isEmpty()) {
			queryParts.add("GROUP BY " + StringUtils.collectionToDelimitedString(groupBy, ", "));
		}		
		if (sortOn != null && !sortOn.isEmpty()) {
			queryParts.add("ORDER BY " + StringUtils.collectionToDelimitedString(sortOn, ", "));
		}
		return StringUtils.collectionToDelimitedString(queryParts, " ");
	}

	private List<Date> getDateRange(String[] params) {
		int weekMils = 7 * 24 * 60 * 60 * 1000;
		List<Date> dateList = new ArrayList<Date>();
		Date today = DateUtil.getCurrentDate();
		if (params == null) {
			return dateList;
		}
		for (String param : params) {
			if (param.toLowerCase().trim().equalsIgnoreCase("today")) {
				dateList.add(today);
			} else if (param.toLowerCase().trim().equalsIgnoreCase("last_week")) {
				dateList.add(new Date(today.getTime() - weekMils));
			} else if (param.toLowerCase().trim().equalsIgnoreCase("next_week")) {
				dateList.add(new Date(today.getTime() + weekMils));break;
			} else {
		    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		        try {
		        	dateList.add(dateFormatter.parse(param));
		        }catch (ParseException ex) {
		            
		        }
			}
		}
		return dateList;
	}
	
	@SuppressWarnings("rawtypes")
  public Map<String, Object> getPatientsForOpFlow(Map<String, String[]> params, boolean advanced) {
		String querySql = "";
		Date today = DateUtil.getCurrentDate();
		List<Date> visitRange = this.getDateRange(params.get("visit_date"));
		List<Date> appointmentRange = this.getDateRange(params.get("appointment_date"));
		String scanVisits = "";
		
		int appointmentId = params.get("appointment_id") != null ? Integer.parseInt(params.get("appointment_id")[0]) : -1;

		Integer pageSize = params.get("page_size") != null && NumberUtils.isDigits((params.get("page_size")[0])) ? Integer.parseInt(params.get("page_size")[0]) : 0;
		if (pageSize == 0) {
			pageSize = params.get("pageSize") != null && NumberUtils.isDigits((params.get("pageSize")[0])) ? Integer.parseInt(params.get("pageSize")[0]) : 15;
		}
		boolean consultationOnly = params.get("consultations_only") != null ? parseBool(params.get("consultations_only")[0]) : false;
		String visitMode = params.get("visit_mode") !=null ? params.get("visit_mode")[0] : "" ;
		boolean userIsDoctor = params.get("user_is_doctor") != null ? parseBool(params.get("user_is_doctor")[0]) : false;
		String visitType = params.get("visit_type") != null ? params.get("visit_type")[0] : "o";
		int pageNum = params.get("page_num") != null && NumberUtils.isDigits((params.get("page_num")[0])) ? Integer.parseInt(params.get("page_num")[0]) : 0;
		if (pageNum == 0) {
			pageNum = params.get("pageNum") != null && NumberUtils.isDigits((params.get("pageNum")[0])) ? Integer.parseInt(params.get("pageNum")[0]) : 1;
		}
		pageNum = pageNum < 0 ? 15 : pageNum;
		pageSize = pageSize < 0 ? 15 : pageSize;
		pageSize = pageSize > 100 ? 100 : pageSize;
		String[] paramFindString = (String[]) params.get("find_string");
		String findString = paramFindString == null || paramFindString[0] == null ? "" : paramFindString[0].trim();
		String[] paramDeptTypes = (String[]) params.get("_department_type");
		String deptType = (paramDeptTypes == null || paramDeptTypes[0] == null) ? "" : paramDeptTypes[0];
		if (!Arrays.asList("test", "service", "consultation").contains(deptType.toLowerCase())) {
			deptType = "";
		}
		List<String> doctorIds = getArgsList(params.get("doctor"));
		boolean allDoctorIds = selectAllInList(params.get("doctor"));
		boolean allDeptIds = selectAllInList(params.get("department"));
		List<String> deptIds = getArgsList(params.get("department"));
		List<String> visitStatuses = getArgsList(params.get("visit_status"));
		List<String> billStatuses = getArgsList(params.get("bill_status"));
		boolean allBillStatuses = selectAllInList(params.get("bill_status"));
		List<String> priInsurance = getArgsList(params.get("primary_insurance"));
		boolean allPriInsurance = selectAllInList(params.get("primary_insurance"));
		List<String> secInsurance = getArgsList(params.get("secondary_insurance"));
		boolean allSecInsurance = selectAllInList(params.get("secondary_insurance"));
		boolean isSystemSearch = params.get("is_system_search") != null ? parseBool(params.get("is_system_search")[0]) : false;
		boolean withCount = params.get("with_count") != null ? parseBool(params.get("with_count")[0]) : false;
		Boolean scanAppointmentsWithMr = false;
		Boolean scanAppointmentsWithoutMr = false;
		Boolean sortOnMr = false;
		String mrAggregator = "";
		String sortOrder = "";
		int roleId = RequestContext.getRoleId();
		Map urlRightsMap = (Map) RequestContext.getSession().getAttribute("urlRightsMap");
		Map actionRightsMap = (Map) RequestContext.getSession().getAttribute("actionRightsMap");
		int centerId = RequestContext.getCenterId();
		BasicDynaBean genPrefBean = genPrefService.getPreferences();	
        String enableForceSelection = (String) genPrefBean.get("enable_force_selection_for_mrno_search");
        enableForceSelection = enableForceSelection == null ? "N" : enableForceSelection;

        // All center access available to InstaAdmin, admin roles.
		// For other roles it is allowed if user has screen access to Registration or Orders 
		// and action right "Show Other Center Patients on Reg Screen".
		// Action right is discarded if system pref "Registration -> Selection For Mrno Search" is No
        // Refer Bugzilla BUG 45807 for center filter for discard condition
		boolean allCenterAccess = roleId == 1 || roleId == 2;
		allCenterAccess = allCenterAccess || (
				(urlRightsMap.get("new_op_registration").equals("A") || urlRightsMap.get("order").equals("A")) 
				&& (enableForceSelection.equals("N") || 
					(enableForceSelection.equals("Y") && actionRightsMap.get("show_other_center_patients").equals("A")))
				);

		//Handle scheduler flow when request only based on appointment_id
		if (appointmentId != -1) {
			scanAppointmentsWithMr = true;
			scanAppointmentsWithoutMr = true;
			mrAggregator = "MIN";			
			sortOrder = "";
			scanVisits = "none";
			sortOnMr = true;
		}
		
		//All patients scan with a visit
		if (appointmentId == -1 && visitRange.isEmpty() && appointmentRange.isEmpty()) {
			sortOnMr = true;
			//If logged in user is doctor MRs to only the ones the doctor has/had consultation visit with.
			if (userIsDoctor) {
				scanVisits = "consultation";
				scanAppointmentsWithMr = !findString.isEmpty(); 
				scanAppointmentsWithoutMr = !findString.isEmpty();
				mrAggregator = "MAX";
			} else if (!findString.isEmpty()) {
				//If performing a search then search considering all center access rights.
 				scanVisits = (allCenterAccess || centerId == 0) ? "none" : "all";
 				scanAppointmentsWithMr = !(allCenterAccess || centerId == 0);
 				scanAppointmentsWithoutMr = true;
				mrAggregator = "MAX";
			} else {
				//other wise just list the patients with atleast one visit to current center
				scanVisits = "all";
				mrAggregator = "MAX";
				scanAppointmentsWithMr = false;
				scanAppointmentsWithoutMr = false;
			}
			sortOrder = "DESC";
		} 
		//All appointments scan
		if (appointmentId == -1 && !appointmentRange.isEmpty()) {
			scanAppointmentsWithMr = true;
			scanAppointmentsWithoutMr = true;
			mrAggregator = appointmentRange.get(0).before(DateUtil.getCurrentTimestamp()) ? "MAX" : "MIN";
			sortOrder = appointmentRange.get(0).before(DateUtil.getCurrentTimestamp()) ? "ASC" : "";
		}
		//If a visit date range is provided, appointmentId param is absent and consultation_only is true or doctorIds are provided
		if (!visitRange.isEmpty() && appointmentId == -1 && (consultationOnly || !doctorIds.isEmpty() || allDoctorIds)) {
			scanVisits = "consultation";
		}
		//If a visit date range is provided and consultation_only is not set or false scan all otherwise skip scan 
		if (scanVisits.isEmpty() && !visitRange.isEmpty()){
			scanVisits = "all";
		} else if (scanVisits.isEmpty() && visitRange.isEmpty()){
			scanVisits = "none";
		}
		if (!scanVisits.equals("none") && !visitRange.isEmpty()) {
			//To select most recent/upcoming visit
			//Select Most recent visit for MR if date range starting in past, also sort present to past
			//Select First Upcoming visit for MR if date range starting in today or in future, also sort present to future
			mrAggregator = visitRange.get(0).before(today) ? "MAX" : "MIN";			
			sortOrder = visitRange.get(0).before(today) ? "DESC" : !sortOrder.equals("")
					? sortOrder : "";
		}
		Map<String, String[]> params2 = new HashMap<String, String[]>();
		params2.putAll(params);

		List<String> fieldsMr = new ArrayList<String>(Arrays.asList("1 as registered", "COALESCE(CASE WHEN pd.original_mr_no = '' THEN NULL ELSE pd.original_mr_no END, pd.mr_no) AS mr_no", "0 as appointment_id", "null as contact_id"));
		List<String> joinsMr = new ArrayList<String>();
		List<String> filtersMr = new ArrayList<String>();
		List<String> groupMr = new ArrayList<String>();
		List<String> sortMr = new ArrayList<String>();
		List<String> fieldsNonMr = new ArrayList<String>(Arrays.asList("cd.contact_id", "sanm.appointment_time"));
		List<String> joinsNonMr = new ArrayList<String>();
        List<String> pureJoinsNonMr = new ArrayList<String>();		
		List<String> filtersNonMr = new ArrayList<String>(Arrays.asList("(sanm.mr_no is null OR sanm.mr_no = '')"));
		List<String> groupNonMr = new ArrayList<String>();
		List<String> sortNonMr = new ArrayList<String>();
		List<Object> argsMr = new ArrayList<Object>();
		List<String> filtersPdRegMr = new ArrayList<>();
		List<Object> argsPdRegMr = new ArrayList<>();
		List<String> filtersPdApptMr = new ArrayList<>();
		List<Object> argsPdApptMr = new ArrayList<>();
		List<Object> argsNonMr = new ArrayList<Object>();
		if (isSystemSearch) {
			filtersNonMr.add("LOWER(sanm.appointment_status) in ('booked', 'confirmed')");
		} 
		StringBuilder timeField = new StringBuilder().append(mrAggregator.isEmpty() ? "" : (mrAggregator + "("));
		if (scanAppointmentsWithMr && scanVisits != "none") {
			timeField.append("COALESCE(pr.reg_date + pr.reg_time, sa.appointment_time)");
		} else if (!scanAppointmentsWithMr && scanVisits != "none") {
			timeField.append("pr.reg_date + pr.reg_time");
		} else if (scanAppointmentsWithMr && scanVisits == "none") {
			timeField.append("sa.appointment_time");
		} else {
			timeField.append("pd.mod_time");
		}
		timeField.append(mrAggregator.isEmpty() ? "" : ")").append(" AS activity_time");
		String timeFieldStr = timeField.toString();
		fieldsMr.add(timeFieldStr);
		if (scanAppointmentsWithMr) {
			filtersPdApptMr.add("sa.mr_no IS NOT NULL");
			String saJoin = "scheduler_appointments sa ON ";
			List<String> saJoinConditions = new ArrayList<String>();
			saJoinConditions.add("sa.mr_no = pd.mr_no");
			if (isSystemSearch) {
				saJoinConditions.add("LOWER(sa.appointment_status) in ('booked', 'confirmed')");
				filtersPdApptMr.add("LOWER(sa.appointment_status) in ('booked', 'confirmed')");
			}
			if (!appointmentRange.isEmpty() && appointmentRange.get(0).equals(appointmentRange.get(1))) {
				filtersPdApptMr.add("date(sa.appointment_time) = ?");
				saJoinConditions.add("date(sa.appointment_time) = ?");
				argsMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
				argsPdApptMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
			}  else if (!appointmentRange.isEmpty()) {
				saJoinConditions.add("date(sa.appointment_time) BETWEEN ? AND ?");
				filtersPdApptMr.add("date(sa.appointment_time) BETWEEN ? AND ?");
				argsMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
				argsMr.add(new java.sql.Date(appointmentRange.get(1).getTime()));
				argsPdApptMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
				argsPdApptMr.add(new java.sql.Date(appointmentRange.get(1).getTime()));
			}
			joinsMr.add(saJoin + StringUtils.collectionToDelimitedString(saJoinConditions, " AND "));
			joinsMr.add("scheduler_master schm on schm.res_sch_id = sa.res_sch_id AND schm.res_sch_category in ('DOC', 'SNP','DIA')");
		}
		if (!scanVisits.equals("none") && scanAppointmentsWithMr) {
			filtersMr.add("(pr.patient_id IS NOT NULL OR (sa.appointment_id IS NOT NULL AND schm.res_sch_category IS NOT NULL))");
		} else if (scanVisits.equals("none") && scanAppointmentsWithMr) {
			filtersMr.add("schm.res_sch_category IS NOT NULL");
			if (appointmentId != -1){
				filtersPdApptMr.add("sa.appointment_id = ?");
				filtersMr.add("sa.appointment_id = ?");
				argsMr.add(appointmentId);
				argsPdApptMr.add(appointmentId);
			}
		}
		if (scanAppointmentsWithoutMr) {
            if (!findString.isEmpty()) {
              joinsNonMr.add("scheduler_appointments sanm on (sanm.contact_id = cd.contact_id AND LOWER(sanm.appointment_status) in ('booked', 'confirmed'))");
            } else {
              pureJoinsNonMr.add("scheduler_appointments sanm on (sanm.contact_id = cd.contact_id AND LOWER(sanm.appointment_status) in ('booked', 'confirmed'))");
            }
            joinsNonMr.add("scheduler_master schmnm on schmnm.res_sch_id = sanm.res_sch_id AND schmnm.res_sch_category in ('DOC', 'SNP','DIA')");
            if (findString.isEmpty()) {
			  filtersNonMr.add("schmnm.res_sch_category IS NOT NULL");
            }
			if (appointmentId != -1){
				filtersNonMr.add("sanm.appointment_id = ?");
				argsNonMr.add(appointmentId);
			}
		}
		if (!scanVisits.equals("none")) {
			String prJoin = "patient_registration pr ON ";
			List<String> prJoinConditions = new ArrayList<String>();
			prJoinConditions.add("pr.mr_no = pd.mr_no");
			if (visitType.equalsIgnoreCase("o")) {
				filtersPdRegMr.add("pr.visit_type = 'o'");
				prJoinConditions.add("pr.visit_type = 'o'");
			} else if (visitType.equalsIgnoreCase("i")) {
				filtersPdRegMr.add("pr.visit_type = 'i'");
				prJoinConditions.add("pr.visit_type = 'i'");
			}
			if (!visitRange.isEmpty() && visitRange.get(0).equals(visitRange.get(1))) {
				prJoinConditions.add("pr.reg_date = ?");
				filtersPdRegMr.add("pr.reg_date = ?");
				argsMr.add(new java.sql.Date(visitRange.get(0).getTime()));
				argsPdRegMr.add(new java.sql.Date(visitRange.get(0).getTime()));
			} else if (!visitRange.isEmpty()) {
				filtersPdRegMr.add("(pr.reg_date BETWEEN ? AND ?)");
				prJoinConditions.add("(pr.reg_date BETWEEN ? AND ?)");
				argsPdRegMr.add(new java.sql.Date(visitRange.get(0).getTime()));
				argsPdRegMr.add(new java.sql.Date(visitRange.get(1).getTime()));
				argsMr.add(new java.sql.Date(visitRange.get(0).getTime()));
				argsMr.add(new java.sql.Date(visitRange.get(1).getTime()));
			}
			joinsMr.add(prJoin + "(" + StringUtils.collectionToDelimitedString(prJoinConditions, " AND ") + ")");
		} 
		if (scanVisits.equals("consultation")) {
		  if (!visitMode.isEmpty()) {
			joinsMr.add("doctor_consultation dc ON (pr.patient_id=dc.patient_id AND dc.status not in ('U', 'X') AND (dc.cancel_status IS NULL OR dc.cancel_status != 'C')  AND dc.visit_mode= ?)");
			argsMr.add(visitMode);
		  }
			else {
			  joinsMr.add("doctor_consultation dc ON (pr.patient_id=dc.patient_id AND dc.status not in ('U', 'X') AND (dc.cancel_status IS NULL OR dc.cancel_status != 'C'))");
			}
			joinsMr.add("bill_activity_charge bac ON (bac.activity_code = 'DOC' and bac.activity_id = dc.consultation_id::character varying)");
			joinsMr.add("bill_charge bc ON (bc.charge_id = bac.charge_id AND bc.status = 'A')");
			joinsMr.add("bill dcb ON (dcb.bill_no = bc.bill_no AND (dcb.bill_type='C' OR (dcb.bill_type='P' AND dcb.payment_status = 'P')))");
		}
		if (!mrAggregator.isEmpty()) {
			groupMr.add("COALESCE(CASE WHEN pd.original_mr_no = '' THEN NULL ELSE pd.original_mr_no END, pd.mr_no)");
		}
		List<String> dateFiltersOrMr = new ArrayList<String>();
	
		if (!visitRange.isEmpty() && visitRange.get(0).equals(visitRange.get(1))) {
			dateFiltersOrMr.add("pr.reg_date = ?");
			argsMr.add(new java.sql.Date(visitRange.get(0).getTime()));
		} else if (!visitRange.isEmpty()) {
			dateFiltersOrMr.add("(pr.reg_date BETWEEN ? AND ?)");
			argsMr.add(new java.sql.Date(visitRange.get(0).getTime()));
			argsMr.add(new java.sql.Date(visitRange.get(1).getTime()));
		}
		if (!appointmentRange.isEmpty() && appointmentRange.get(0).equals(appointmentRange.get(1))) {
			dateFiltersOrMr.add("date(sa.appointment_time) = ?");
			argsMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
			filtersNonMr.add("date(sanm.appointment_time) = ?");
			argsNonMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
		} else if (!appointmentRange.isEmpty()) {
			dateFiltersOrMr.add("(date(sa.appointment_time) BETWEEN ? AND ?)");
			argsMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
			argsMr.add(new java.sql.Date(appointmentRange.get(1).getTime()));
			filtersNonMr.add("date(sanm.appointment_time) BETWEEN ? AND ?");
			argsNonMr.add(new java.sql.Date(appointmentRange.get(0).getTime()));
			argsNonMr.add(new java.sql.Date(appointmentRange.get(1).getTime()));
		}
		if (!dateFiltersOrMr.isEmpty()){
			filtersMr.add("(" + StringUtils.collectionToDelimitedString(dateFiltersOrMr, " OR ") + ")");	
		}
		//If center is not default restrict search scope to center level
		if ((!allCenterAccess && centerId !=0) || (centerId != 0 && findString.isEmpty())) {
			filtersNonMr.add("sanm.center_id = ?");
			argsNonMr.add(centerId);
			if (scanAppointmentsWithMr && scanVisits != "none") {
				filtersMr.add("(sa.center_id = ? OR pr.center_id = ?)");
				argsMr.add(centerId);
				argsMr.add(centerId);
				filtersPdRegMr.add("pr.center_id = ?");
				argsPdRegMr.add(centerId);			
				filtersPdApptMr.add("sa.center_id = ?");
				argsPdApptMr.add(centerId);			
			} else if (!scanAppointmentsWithMr && scanVisits != "none") {
				filtersMr.add("pr.center_id = ?");
				argsMr.add(centerId);
				filtersPdRegMr.add("pr.center_id = ?");
				argsPdRegMr.add(centerId);			
			} else if (scanAppointmentsWithMr && scanVisits == "none") {
				filtersMr.add("sa.center_id = ?");
				argsMr.add(centerId);
				filtersPdApptMr.add("sa.center_id = ?");
				argsPdApptMr.add(centerId);			
			}
		}
		if (!findString.isEmpty()) {
			String searchString = findString.trim().replaceAll("[\\s\\t]+"," ");
			String[] stringParts = searchString.split(" ");
      /**
       * Matches for mr number/token entered without space or any alphanumeric string without space.
			 * eg.. 1111-2222-3333-4444, MR\12345, MR12034, 9845098450  etc
       * MR013015 also matches when mr number's last digits entered (reverse mr) -> 13015
       */
      if (stringParts.length == 1 && findString.matches("^[A-Za-z\\-\\.\\\\\\/0-9]*[0-9]+$")) {
				String valInLowerCase = new StringBuilder(searchString).toString().toLowerCase();
				String lowerReversal = new StringBuilder(valInLowerCase).reverse().toString() + "%";
				valInLowerCase = valInLowerCase + "%";
				filtersMr.add("pd.mr_no IN (select distinct(entity_id) from patient_search_tokens where "
						+ " ((token like ? and reversed) or (token like ? and not reversed))"
						+ (scanAppointmentsWithoutMr ? "" : " and entity = 'patient_details'") +")");
				argsMr.addAll(Arrays.asList(lowerReversal, valInLowerCase));
				//need to scan on both mr and non mr tables when it is phone number. This clause also removes redundant data when reverse mr number search is performed.
				filtersNonMr.add("cd.contact_id::text IN (select distinct(entity_id) from patient_search_tokens where "
						+ " token like ? and not reversed and entity = 'contact_details')");
				argsNonMr.addAll(Arrays.asList(valInLowerCase));
				//For Alphanumeric strings/tokens which are not pure digits no need to scan non-mr number tables
				if (findString.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$")) {
					scanAppointmentsWithoutMr = false;
				}
			} else {
				for (int i=0; i<stringParts.length;i++) {
					argsMr.add(stringParts[i].toLowerCase()+"%");
					argsNonMr.add(stringParts[i].toLowerCase()+"%");
				}
				String mrPatients = getForwardTokenSearchFilter(stringParts.length, "patient_details");
				String nonMrPatients = getForwardTokenSearchFilter(stringParts.length, "contact_details");
				filtersMr.add("pd.mr_no IN (" +mrPatients+" )");
				argsMr.addAll(Arrays.asList(stringParts.length));
				filtersNonMr.add("cd.contact_id::text in (" +nonMrPatients+" )");
				argsNonMr.addAll(Arrays.asList(stringParts.length));
      }
		}
		//Doctor Id Filters
		if (!doctorIds.isEmpty() || allDoctorIds) {
			String docIdPlaceholders = fillWithQuestionMarksForInQuery(doctorIds);
			String filter = "";
			//Filter for visits with consultation orders
			if (!scanVisits.equals("none")) {
				if (allDoctorIds) {
					filter = "dc.doctor_name IS NOT NULL";
				} else {
					filter = "dc.doctor_name in (" + docIdPlaceholders + ")";
					argsMr.addAll(doctorIds);
				}
			}
			Boolean isTodayOPPatientFilter = !visitRange.isEmpty() && visitRange.get(0).equals(visitRange.get(1)) 
					&& visitRange.get(0).equals(today) && !appointmentRange.isEmpty() && appointmentRange.get(0).equals(appointmentRange.get(1)) 
					&& appointmentRange.get(0).equals(today) && isSystemSearch;
			
		   List<BasicDynaBean> resourceItemList = null;
	      List<String> resourceIds = new ArrayList<>();
	      String resIdPlaceholders = "";
	      if (appointmentId != -1) {
	        resourceItemList = resourceRepository.getAppointmentItems(appointmentId);
	        for (BasicDynaBean resourceItem : resourceItemList) {
	          String resourceId = (String)resourceItem.get("resource_id");
	          resourceIds.add(resourceId);
	        }
	        resIdPlaceholders = fillWithQuestionMarksForInQuery(resourceIds);
	      }
			
			// Filter for appointments with doctor as a resource.
			// In case of user is doctor and text search is being performed, or today's op patient filter is selected,
			// consider test and service appointments too where doctor is secondary resource 
			
			
			if (scanAppointmentsWithMr) {
				joinsMr.add("scheduler_appointment_items sai ON sai.appointment_id = sa.appointment_id AND sai.resource_type in (" + ((userIsDoctor && (!findString.isEmpty() || isTodayOPPatientFilter)) ? "'LABTECH', 'DOC', " : "") + "'OPDOC','EQID')");
				if (allDoctorIds) {
					filter += (filter.isEmpty() ? "" : " OR ") + "sai.resource_id IS NOT NULL";
				} else {
          if (resourceIds.isEmpty()) {
            filter += (filter.isEmpty() ? "" : " OR ") + "sai.resource_id in (" + docIdPlaceholders
                + ")";
            argsMr.addAll(doctorIds);
          } else {
            filter += (filter.isEmpty() ? "" : " OR ") + "sai.resource_id in (" + resIdPlaceholders
                + ")";
            argsMr.addAll(resourceIds);
          }
				}
			}
			if (scanAppointmentsWithoutMr) {
				joinsNonMr.add("scheduler_appointment_items sanmi ON sanmi.appointment_id = sanm.appointment_id AND sanmi.resource_type in (" + ((userIsDoctor && (!findString.isEmpty() || isTodayOPPatientFilter)) ? "'LABTECH', 'DOC', " : "") + "'OPDOC','EQID')");
				if (allDoctorIds) {
					filtersNonMr.add("sanmi.resource_id IS NOT NULL");
				} else {
          if (resourceIds.isEmpty()) {
            filtersNonMr.add("sanmi.resource_id in (" + docIdPlaceholders + ")");
            argsNonMr.addAll(doctorIds);
          } else {
            filtersNonMr.add("sanmi.resource_id in (" + resIdPlaceholders + ")");
            argsNonMr.addAll(resourceIds);
          }
				}
			}
			if (!filter.isEmpty()) {
				filtersMr.add("(" + filter + ")");
			}
			//For logged in doctor user and today op or todays consultation filter is applied   
			if (userIsDoctor && scanVisits.equals("consultation") 
					&& !visitRange.isEmpty() && visitRange.get(0).equals(visitRange.get(1)) 
					&& visitRange.get(0).equals(today)
					&& isSystemSearch) {
				if (scanAppointmentsWithMr) {
					filtersMr.add("(dcb.bill_no IS NOT NULL OR sa.appointment_id IS NOT NULL)");
				} else {
					filtersMr.add("dcb.bill_no IS NOT NULL");
				}
								
			}
		} else if (scanVisits.equals("consultation")) {
			filtersMr.add("dc.doctor_name IS NOT NULL");
			filtersMr.add("dcb.bill_no IS NOT NULL");
		}
		if (scanVisits.equals("all") && findString.isEmpty() && !scanAppointmentsWithMr) {
			filtersMr.add("pr.patient_id IS NOT NULL");
		}
		//Dept Id Filters for dept Type = consultation
		if (deptType.equals("consultation") && (allDeptIds || !deptIds.isEmpty())) {
			if (!scanVisits.equals("none") ) {
				joinsMr.add("bill deptb ON deptb.visit_id = pr.patient_id and deptb.status != 'X'");
				joinsMr.add("bill_charge deptbc ON deptbc.bill_no = deptb.bill_no and deptbc.status = 'A'");
				joinsMr.add("bill_activity_charge deptbac ON deptbc.charge_id = deptbac.charge_id and deptbac.activity_code = 'DOC'");
			}
			if (scanAppointmentsWithMr && !scanVisits.equals("none")) {
				joinsMr.add("doctors doc ON (sa.prim_res_id = doc.doctor_id OR doc.doctor_id = deptbac.act_description_id)");				
			} else if (scanAppointmentsWithMr && scanVisits.equals("none")) {
				joinsMr.add("doctors doc ON (sa.prim_res_id = doc.doctor_id)");
			} else if (!scanAppointmentsWithMr && !scanVisits.equals("none")) {
				joinsMr.add("doctors doc ON (doc.doctor_id = deptbac.act_description_id)");
			} 
			if (scanAppointmentsWithoutMr) {
				joinsNonMr.add("doctors docnm ON sanm.prim_res_id = docnm.doctor_id");				
			}
			if (!deptIds.isEmpty()) {
				String deptIdPlaceholders = fillWithQuestionMarksForInQuery(deptIds);
				if (scanVisits.equals("none") && scanAppointmentsWithMr) {
					filtersMr.add("doc.dept_id in (" + deptIdPlaceholders + ")");
					argsMr.addAll(deptIds);
				} else if (!scanVisits.equals("none")) {
					filtersMr.add("(doc.dept_id in (" + deptIdPlaceholders + ") OR pr.dept_name IN (" + deptIdPlaceholders + "))");
					argsMr.addAll(deptIds);
					argsMr.addAll(deptIds);
				}
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("docnm.dept_id in (" + deptIdPlaceholders + ")");
					argsNonMr.addAll(deptIds);
				}
			} else if (deptIds.isEmpty()) {
				if (scanVisits.equals("none") && scanAppointmentsWithMr) {
					filtersMr.add("doc.dept_id IS NOT NULL");
				} else if (!scanVisits.equals("none")) {
					filtersMr.add("(doc.dept_id IS NOT NULL OR pr.dept_name IS NOT NULL)");
				}
				
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("docnm.dept_id IS NOT NULL");
				}				
			}
		}
		//Dept Id Filters for dept type == service
		if (deptType.equals("service") && (allDeptIds || !deptIds.isEmpty())) {
			if (!scanVisits.equals("none") ) {
				joinsMr.add("bill deptb ON deptb.visit_id = pr.patient_id and deptb.status != 'X'");
				joinsMr.add("bill_charge deptbc ON deptbc.bill_no = deptb.bill_no and deptbc.status = 'A'");
				joinsMr.add("bill_activity_charge deptbac ON deptbc.charge_id = deptbac.charge_id and deptbac.activity_code = 'SER'");
			}
			if (!scanVisits.equals("none") && scanAppointmentsWithMr) {
				joinsMr.add("services ser ON (deptbac.act_description_id = ser.service_id OR sa.res_sch_name = ser.service_id)");				
			} else if (scanAppointmentsWithMr && scanVisits.equals("none")) {
				joinsMr.add("services ser ON sa.res_sch_name = ser.service_id");
			} else if (!scanAppointmentsWithMr && !scanVisits.equals("none")) {
				joinsMr.add("services ser ON deptbac.act_description_id = ser.service_id");
			}
			if (scanAppointmentsWithoutMr) {
				joinsNonMr.add("services sernm ON sanm.res_sch_name = sernm.service_id");
			}
			if (!deptIds.isEmpty()) {
				String deptIdPlaceholders = fillWithQuestionMarksForInQuery(deptIds);
				if (!scanVisits.equals("none") || scanAppointmentsWithMr) {
					filtersMr.add("ser.serv_dept_id::character varying in (" + deptIdPlaceholders + ")");
					argsMr.addAll(deptIds);
				}
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("sernm.serv_dept_id::character varying in (" + deptIdPlaceholders + ")");
					argsNonMr.addAll(deptIds);
				}
			} else if (deptIds.isEmpty()) {
				if (!scanVisits.equals("none") || scanAppointmentsWithMr) {
					filtersMr.add("ser.serv_dept_id IS NOT NULL");
				}
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("sernm.serv_dept_id IS NOT NULL");
				}				
			}
		}
		//Dept Id Filters for dept type == test
		if (deptType.equals("test") && (allDeptIds || !deptIds.isEmpty())) {
			if (!scanVisits.equals("none") ) {
				joinsMr.add("bill deptb ON deptb.visit_id = pr.patient_id and deptb.status != 'X'");
				joinsMr.add("bill_charge deptbc ON deptbc.bill_no = deptb.bill_no and deptbc.status = 'A'");
				joinsMr.add("bill_activity_charge deptbac ON deptbc.charge_id = deptbac.charge_id and deptbac.activity_code = 'DIA'");
			}
			if (!scanVisits.equals("none") && scanAppointmentsWithMr) {
				joinsMr.add("diagnostics dia ON (deptbac.act_description_id = dia.test_id OR sa.res_sch_name = dia.test_id)");				
			} else if (scanAppointmentsWithMr && scanVisits.equals("none")) {
				joinsMr.add("diagnostics dia ON sa.res_sch_name = dia.test_id");
			} else if (!scanAppointmentsWithMr && !scanVisits.equals("none")) {
				joinsMr.add("diagnostics dia ON deptbac.act_description_id = dia.test_id");
			}
			if (scanAppointmentsWithoutMr) {
				joinsNonMr.add("diagnostics dianm ON sanm.res_sch_name = dianm.test_id");
			}
			if (!deptIds.isEmpty()) {
				String deptIdPlaceholders = fillWithQuestionMarksForInQuery(deptIds);
				if (!scanVisits.equals("none") || scanAppointmentsWithMr) {
					filtersMr.add("dia.ddept_id in (" + deptIdPlaceholders + ")");
					argsMr.addAll(deptIds);
				}
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("dianm.ddept_id in (" + deptIdPlaceholders + ")");
					argsNonMr.addAll(deptIds);
				}
			} else if (deptIds.isEmpty()) {
				if (!scanVisits.equals("none") || scanAppointmentsWithMr) {
					filtersMr.add("dia.ddept_id IS NOT NULL");
				}
				if (scanAppointmentsWithoutMr) {
					filtersNonMr.add("dianm.ddept_id IS NOT NULL");
				}				
			}
		}
		//Visit Status Filters
		if (!scanVisits.equals("none") && !visitStatuses.isEmpty()) {
			String visitStatusPlaceholders = fillWithQuestionMarksForInQuery(visitStatuses);
			filtersMr.add("COALESCE(pr.status, 'N'::character varying) in (" + visitStatusPlaceholders + ")");
			argsMr.addAll(visitStatuses);
		}
		
		//Primary Insurance Filters OR Secondary Insurance Filters
		if (!scanVisits.equals("none") && (!priInsurance.isEmpty() || allPriInsurance || !secInsurance.isEmpty() || allSecInsurance)) {
			joinsMr.add("patient_insurance_plans p_ins ON p_ins.patient_id = pr.patient_id AND p_ins.priority = 1");				
			joinsMr.add("patient_insurance_plans s_ins ON s_ins.patient_id = pr.patient_id AND s_ins.priority = 2");				
			String filter = "(";
			if (allPriInsurance) {
				filter += "(p_ins.insurance_co IS NOT NULL AND p_ins.insurance_co != '')";
			} else if (!priInsurance.isEmpty()) {
				String priInsurancePlaceholders = fillWithQuestionMarksForInQuery(priInsurance);
				filter +="p_ins.insurance_co in (" + priInsurancePlaceholders + ")";
				argsMr.addAll(priInsurance);
			}
			if ((allPriInsurance || !priInsurance.isEmpty()) && (allSecInsurance || !secInsurance.isEmpty())) {
				filter += " OR ";
			}
			if (allSecInsurance) {
				filter += "(s_ins.insurance_co IS NOT NULL AND s_ins.insurance_co != '')";
			} else if (!secInsurance.isEmpty()){
				String secInsurancePlaceholders = fillWithQuestionMarksForInQuery(secInsurance);
				filter += "s_ins.insurance_co in (" + secInsurancePlaceholders + ")";
				argsMr.addAll(secInsurance);
			}
			filter += ")";
			filtersMr.add(filter);
		}		
		
		//Bill Filters
		if (!scanVisits.equals("none") && (!billStatuses.isEmpty() || allBillStatuses)) {
			String billAlias = "";
			if (!deptType.isEmpty() && (allDeptIds || !deptIds.isEmpty())) {
				billAlias = "deptb";
			} else if (!doctorIds.isEmpty() || allDoctorIds) {
				billAlias = "dcb";
			} else {
				billAlias = "b";
				joinsMr.add("bill b ON pr.patient_id = b.visit_id AND b.status <> 'X' AND b.restriction_type <> 'P'");
			}
								
			if (allBillStatuses) {
				filtersMr.add(billAlias + ".status IS NOT NULL");
			} else {
				String billStatusesPlaceholders = fillWithQuestionMarksForInQuery(billStatuses);
				filtersMr.add(billAlias + ".status in (" + billStatusesPlaceholders + ")");
				argsMr.addAll(billStatuses);
			}
		}

//		if (!findString.isEmpty() && scanAppointmentsWithoutMr) {
//			filtersNonMr.add("LOWER(sanm.appointment_status) in ('booked', 'confirmed')"); 			
//		}
	  Map<String, Object> sessionAttribs = sessionService
	      .getSessionAttributes(new String[] { "userId", "user_accessible_patient_groups" });
	  @SuppressWarnings("unchecked")
	  List<Integer> userGroupsList = (ArrayList<Integer>) sessionAttribs.get("user_accessible_patient_groups");
		String[] userGroupPlaceholdersArr = new String[userGroupsList.size()];
		Arrays.fill(userGroupPlaceholdersArr, "?");
		String userGroupPlaceholders = StringUtils.arrayToCommaDelimitedString(userGroupPlaceholdersArr);
    filtersMr.add("(pd.patient_group in(" + userGroupPlaceholders + ") "
        + "OR pd.mr_no in (" + ConfidentialityQueryHelper.QUERY_MRNO_USER_ACCESS + "))");
		argsMr.addAll(userGroupsList);
		String tableMr = "patient_details pd";
		List<String> filtersTableMr = new ArrayList<>();
		List<Object> queryArgs = new ArrayList<Object>();
		if (!scanVisits.equals("none") && !visitRange.isEmpty()) {
			filtersTableMr.add("mr_no IN (" + this.getQuery("patient_registration pr", Arrays.asList("pr.mr_no"), null, null, filtersPdRegMr, null, null) + ")");
			queryArgs.addAll(argsPdRegMr);
		}
		if (scanAppointmentsWithMr && !appointmentRange.isEmpty()) {
			filtersTableMr.add("mr_no IN (" + this.getQuery("scheduler_appointments sa", Arrays.asList("sa.mr_no"), null, null, filtersPdApptMr, null, null) + ")");
			queryArgs.addAll(argsPdApptMr);
		}
		if ((scanAppointmentsWithMr && !appointmentRange.isEmpty()) || (!scanVisits.equals("none") && !visitRange.isEmpty())) {
			
			tableMr = "(" + this.getQuery("patient_details", Arrays.asList("*"), null, null, Arrays.asList(StringUtils.collectionToDelimitedString(filtersTableMr, " OR ")), null, null) +") pd";
		}
		String queryMr = this.getQuery(tableMr, fieldsMr, joinsMr, null, filtersMr, groupMr, sortMr);
		queryArgs.addAll(argsMr);
		if (scanAppointmentsWithoutMr) {
            String queryNonMr = this.getQuery("contact_details cd", fieldsNonMr, joinsNonMr, pureJoinsNonMr, filtersNonMr, groupNonMr, sortNonMr);
            String finalQueryNonMr = " SELECT 0 as registered, '' as mr_no, 0 as appointment_id, contact_id, appointment_time as activity_time "
                + " FROM ("+ queryNonMr +") as foo GROUP BY contact_id, registered, mr_no, appointment_id, activity_time ";
            querySql = "SELECT " + (withCount ? "count(*) over (partition by 1) as cn, ": "") + "registered, mr_no, appointment_id, contact_id, activity_time FROM (" + queryMr
                + " UNION " + finalQueryNonMr + ") union_table";
           queryArgs.addAll(argsNonMr);
		} else {
			if (withCount) {
				fieldsMr.add("count(*) over (partition by 1) as cn");
			}
			
			queryMr = this.getQuery(tableMr, fieldsMr, joinsMr, null, filtersMr, groupMr, sortMr);
			querySql = queryMr;
		}
		if (sortOnMr) {
			querySql += " ORDER BY mr_no " + sortOrder;
			if (scanAppointmentsWithoutMr) {
				querySql += ", appointment_id " + sortOrder;
			}
		} else {
			querySql += " ORDER BY activity_time " + sortOrder + ",appointment_id " + sortOrder + ",mr_no " + sortOrder;			
		}
		if (pageSize != 0) {
			querySql += " LIMIT ?";
			queryArgs.add(pageSize);
		}
		
		if (pageNum != 0) {
			querySql += " OFFSET ?";
			queryArgs.add((pageNum - 1) * pageSize);
		}
		logger.debug(querySql);
		List<BasicDynaBean> patientList = DatabaseHelper.queryToDynaList(querySql, queryArgs.toArray());
		List<Object> mrNos = new ArrayList<Object>();
		List<Object> contactIds = new ArrayList<Object>();
		Integer totalCount = 0;
		if (patientList.size() > 0 && withCount) { 
			totalCount = (Integer.parseInt(patientList.get(0).get("cn").toString()));
		}
    for (BasicDynaBean patient : patientList) {
      if (Integer.parseInt(patient.get("registered").toString()) == 1) {
        if (!mrNos.contains(patient.get("mr_no"))) {
          mrNos.add(patient.get("mr_no"));
        }
      } else if (!contactIds.contains(patient.get("contact_id"))) {
				contactIds.add(patient.get("contact_id"));
			}
    }
		List<Patient> results = new ArrayList<Patient>();
		Map<String, Patient> cardData = new HashMap<String, Patient>();
		AdvancedFilterOptions options = new AdvancedFilterOptions();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total_records", totalCount);
		result.put("page_size", pageSize);
		result.put("page_num", pageNum);
		if (advanced) {
			options.setAppointmentRange(appointmentRange);
			options.setVisitRange(visitRange);
			options.setCenterId(centerId);
			options.setBillStatuses(billStatuses);
			options.setDeptIds(deptIds);
			options.setDeptType(deptType);
			options.setPriInsurance(priInsurance);
			options.setSecInsurance(secInsurance);
			options.setVisitStatuses(visitStatuses);
			options.setDoctorIds(doctorIds);
			options.setVisitType(visitType);
			options.setAllBillStatuses(allBillStatuses);
			options.setAllDoctorIds(allDoctorIds);
			options.setAllPriInsurance(allPriInsurance);
			options.setAllSecInsurance(allSecInsurance);
			cardData.putAll(getAdvancedPatientListCards(mrNos, contactIds, options));
		}
		if (!mrNos.isEmpty() && !advanced) {
			cardData.putAll(getPatientListCardByMr(mrNos, visitType, (userIsDoctor && doctorIds.size() > 0) ? doctorIds.get(0) : null, consultationOnly, centerId)); 
		}
		if (!contactIds.isEmpty() && !advanced) {
			cardData.putAll(getPatientListCardByContactIds(contactIds, (userIsDoctor && doctorIds.size() > 0) ? doctorIds.get(0) : null, centerId, appointmentId, findString)); 
		}
		
    for (BasicDynaBean patient : patientList) {
      if (Integer.parseInt(patient.get("registered").toString()) == 1) {
        if (!results.contains(cardData.get(patient.get("mr_no").toString()))) {
          results.add(cardData.get(patient.get("mr_no").toString()));
        }
      } else if (!results.contains(cardData.get(patient.get("contact_id").toString()))) {
        results.add(cardData.get(patient.get("contact_id").toString()));
      }
    }
		result.put("patients", results);
		return result;
	}

	/**
	 * created query filter for each search  tokens.
	 * @param count
	 * @param entity
	 * @return
	 */
	private String getForwardTokenSearchFilter(int count, String entity) {
		String[] tokenSearchArr = new String[count];
		Arrays.fill(tokenSearchArr, TOKEN_SEARCH.replace("#", entity));
		return TOKEN_WRAPPER_HEAD + StringUtils.arrayToDelimitedString(tokenSearchArr, " UNION ALL ") + TOKEN_WRAPPER_TAIL;
	}
    
    private Map<String, Patient> getPatientListCardByMr(List<Object> mrNos, String visitType, String doctorId, boolean consultationOnly, int centerId) {
        String placeHolders = fillWithQuestionMarksForInQuery(mrNos);
        String visitFilter = "";
        if (visitType.equalsIgnoreCase("o")) {
            visitFilter = "r.visit_type = 'o'";
        } else if (visitType.equalsIgnoreCase("i")) {
            visitFilter = "r.visit_type = 'i'";
        }
        
        String patientGroupFilter = " (" + ConfidentialityQueryHelper.QUERY_CONFIDENTIALITY_GROUP_ACCESS_USER + ")";
        
        String mrnoUserFilter = "(" + ConfidentialityQueryHelper.QUERY_MRNO_USER_ACCESS + ")";
        
        String querySql = "SELECT * FROM ("
        		+ "	SELECT "
        		+ "		mr_no, "
        		+ "		appointment_id,"
                + "     null as contact_id,"        		
        		+ "		full_name,"
        		+ "		patient_name,"
        		+ "		middle_name,"
        		+ "		last_name,"
        		+ "		patient_gender,"
        		+ "		dob,"
        		+ "		death_date,"
        		+ "		death_time,"
        		+ "		patient_phone,"
        		+ "		patient_phone_country_code,"
        		+ "		government_identifier,"
        		+ "		oldmrno,salutation_name,email_id, send_sms, send_email, lang_code, "
        		+ "		visit_date_time,"
        		+ "		visit_type,"
        		+ "		appointment_date_time,"
        		+ "		sum(past_visit) over (PARTITION BY mr_no) AS past_visit_count,"
        		+ "		sum(current_day_visit) over (PARTITION BY mr_no) AS current_day_visit_count,"
        		+ "		sum(future_appointment) over (PARTITION BY mr_no) future_appointment_count,"
        		+ "		sum(current_day_appointment) over (PARTITION BY mr_no) AS current_day_appointment_count,"
        		+ "		sum(current_day_consultations) over (PARTITION BY mr_no) AS current_day_consultation_count,"
        		+ "		sum(current_day_mlc_visit) over (PARTITION BY mr_no) AS current_day_mlc_visit_count,"
        		+ "		sum(current_day_er_visit) over (PARTITION BY mr_no) AS current_day_er_visit_count,"
        		+ "		row_number() over (PARTITION BY mr_no ORDER BY no_visit, no_appointment, past_visit, future_appointment, current_day_appointment, current_day_visit,"
        		+ "			CASE WHEN past_visit = 1 THEN extract(epoch from (now() - activity_date_time)) ELSE extract(epoch from activity_date_time) END) AS mr_activity_priority_absolute,"
        		+ "		activity_date_time,"
        		+ "		dept_name,"
        		+ "		doctor_name,"
        		+ "   test_name,"
        		+ "   service_name,"
        		+ "     appointment_resource_category as res_sch_category,"
        		+ "		appointment_resource_category,"
        		+ "		CASE"
        		+ "			WHEN patient_age >= interval '5 year' THEN to_char(patient_age, 'FMYYYY\"Y\"')"
        		+ "			WHEN patient_age >= interval '1 year' THEN to_char(patient_age, 'FMYYYY\"Y\"+FMMM\"M\"')"
        		+ "			WHEN patient_age >= interval '1 month' THEN to_char(patient_age, 'FMMM\"M\"')"
        		+ "			ELSE to_char(patient_age, 'FMDD\"D\"')"
        		+ "			END AS age_text,"
				+ "		raw_age_text,"
        		+ "     duplicate_mr_nos,"
        		+ "     MAX(vip_status) over (PARTITION BY mr_no) AS vip_status, abbreviation"
        		+ "	FROM ("
        		+ "		SELECT pd.mr_no,"
        		+ "			null as appointment_id,"
        		+ "			CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS full_name,"
        		+ "			pd.patient_name,"
        		+ "			pd.middle_name,"
        		+ "			pd.last_name,"
        		+ "			pd.patient_gender,"
        		+ "			pd.dateofbirth AS dob,"
        		+ "			pd.death_date AS death_date,"
        		+ "			pd.death_time AS death_time,"
        		+ "         AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) AS patient_age,"
				+ "			to_char(age(coalesce(dateofbirth, expected_dob)), 'FMYYY\"Y\"FMMM\"M\"') "
				+ "			as raw_age_text, "
        		+ "			pd.patient_phone,"
        		+ "			pd.patient_phone_country_code,"
        		+ "			pd.government_identifier,"
        		+ "			pd.oldmrno,"
        		+ "     pd.salutation as salutation_name,"
        		+ "      pd.email_id,"
        		+ "     CASE WHEN (cpref.receive_communication in ('S','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_sms, "
        		+ "       CASE WHEN (cpref.receive_communication in ('E','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_email, " 
        		+ "       cpref.lang_code, "
        		+ "			pr.reg_date + pr.reg_time AS visit_date_time,"
        		+ "         CASE WHEN pr.op_type = 'O' THEN 'osp' ELSE pr.visit_type END AS visit_type, "
        		+ "			NULL AS appointment_date_time,"
        		+ "			NULL AS appointment_resource_category,"
        		+ "			pr.reg_date + pr.reg_time AS activity_date_time,"
        		+ "			CASE WHEN pr.reg_date IS NOT NULL AND pr.reg_date < current_date THEN 1 ELSE 0 END AS past_visit,"
        		+ "			CASE WHEN pr.reg_date IS NOT NULL AND pr.reg_date = current_date THEN 1 ELSE 0 END AS current_day_visit,"
        		+ "			CASE WHEN pr.reg_date IS NOT NULL AND pr.reg_date = current_date AND pr.mlc_status = 'Y' THEN 1 ELSE 0 END AS current_day_mlc_visit,"
        		+ "			CASE WHEN pr.reg_date IS NOT NULL AND pr.reg_date = current_date AND pr.is_er_visit THEN 1 ELSE 0 END AS current_day_er_visit,"
        		+ "			CASE WHEN pr.reg_date IS NOT NULL AND pr.reg_date = current_date THEN pr.consultation_order_count ELSE 0 END AS current_day_consultations,"
        		+ "			0 AS future_appointment,"
        		+ "			0 AS current_day_appointment,"
        		+ "			CASE WHEN pr.reg_date IS NULL THEN 1 ELSE 0 END AS no_visit,"
        		+ "			0 AS no_appointment,"
        		+ "			dept.dept_name,"
        		+ "			doc.doctor_name,"
            + "     null as service_name,"
            + "     null as test_name,"
        		+ "         COALESCE(mr_dup.duplicate_mr_nos, '') AS duplicate_mr_nos,"
        		+ "         pd.vip_status, cgm.abbreviation"
        		+ "		FROM patient_details pd "
        		+ "		LEFT JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group AND cgm.confidentiality_grp_id != 0) "
        		+ "		LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation "
        		+ "   LEFT JOIN contact_preferences cpref on (pd.mr_no = cpref.mr_no)"
        		+ "     LEFT JOIN ("
        		+ "         SELECT original_mr_no as mr_no,"
        		+ "                string_agg(mr_no, ',') as duplicate_mr_nos"
        		+ "         FROM patient_details pd"
        		+ "         WHERE original_mr_no IN (" + placeHolders + ") AND (pd.patient_group in "+ patientGroupFilter
        		+ "         OR pd.mr_no in " + mrnoUserFilter + ")"
        		+ "         GROUP BY original_mr_no"
        		+ "     ) AS mr_dup ON mr_dup.mr_no = pd.mr_no"
        		+ "		LEFT JOIN ( "
        		+ "			SELECT mr_no, patient_id, reg_date, reg_time, op_type, visit_type, consultation_order_count, doctor_name, dept_name, mlc_status, is_er_visit FROM ("
        		+ "				SELECT r.patient_id,"
        		+ "					COALESCE(CASE WHEN pdr.original_mr_no = '' THEN NULL ELSE pdr.original_mr_no END, r.mr_no) as mr_no,"
        		+ "					r.reg_date,"
        		+ "					r.reg_time,"
        		+ "                 r.dept_name,"
        		+ "                 r.mlc_status, r.is_er_visit, r.op_type, r.visit_type,"
        		+ "					sum(CASE WHEN dc.doctor_name " + (doctorId != null ? " = ?" : " IS NOT NULL")
        		+ " 					THEN 1 ELSE 0 END) over (PARTITION BY r.patient_id) AS consultation_order_count,"
        		+ "					dc.doctor_name,"
        		+ "					row_number() over (PARTITION BY r.patient_id) AS row_id "
        		+ "					FROM (SELECT * FROM patient_registration "
        		+ "                             WHERE mr_no in (SELECT mr_no FROM patient_details pd"
        		+ "                                   WHERE mr_no IN (" + placeHolders + ") OR original_mr_no IN (" + placeHolders + "))) r "
        		+ "                 JOIN patient_details pdr ON (r.mr_no = pdr.mr_no AND (pdr.mr_no IN (" + placeHolders + ") OR pdr.original_mr_no IN (" + placeHolders + ")) "
        		+ "                 AND (pdr.patient_group in " + patientGroupFilter + " OR pdr.mr_no in " + mrnoUserFilter + "))"
        		+ "					LEFT JOIN doctor_consultation dc ON (r.patient_id=dc.patient_id AND dc.status not in ('U', 'X') AND (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
                + (doctorId != null ? " AND dc.doctor_name = ?)" : ")")
                + (consultationOnly ? " LEFT JOIN bill_activity_charge bac ON (bac.activity_code = 'DOC' and bac.activity_id = dc.consultation_id::character varying)" : "")
                + (consultationOnly ? " JOIN bill_charge bc ON (bc.charge_id = bac.charge_id AND bc.status = 'A')" : "")
                + (consultationOnly ? " JOIN bill dcb ON (dcb.bill_no = bc.bill_no AND (dcb.bill_type='C' OR (dcb.bill_type='P' AND dcb.payment_status = 'P')))" : "")
        		+ "                 WHERE "
                + visitFilter
                + (centerId !=0 ? " AND r.center_id = ?" : "")
                + "					AND (r.mr_no in (" + placeHolders + ") OR pdr.original_mr_no in (" + placeHolders + "))"  
                + (consultationOnly ? " AND dcb.bill_no IS NOT NULL" : "") 
        		+ "				) pr_temp WHERE " + (doctorId != null ? "doctor_name IS NOT NULL AND " : "") + "row_id = 1"
        		+ "			) pr ON pr.mr_no = pd.mr_no "
        		+ "		LEFT JOIN doctors doc ON doc.doctor_id = pr.doctor_name"
        		+ "		LEFT JOIN department dept ON (COALESCE(doc.dept_id,pr.dept_name) = dept.dept_id)"
        		+ "		WHERE pd.mr_no in (" + placeHolders + ")"
        		+ "		UNION"
        		+ "		SELECT pd.mr_no,"
        		+ "			sa.appointment_id,"
        		+ "			CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS full_name,"
        		+ "			pd.patient_name,"
        		+ "			pd.middle_name,"
        		+ "			pd.last_name,"
        		+ "			pd.patient_gender,"
        		+ "			pd.dateofbirth AS dob,"
        		+ "			pd.death_date AS death_date,"
        		+ "			pd.death_time AS death_time,"
        		+ "         AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) AS patient_age,"
				+ "			to_char(age(coalesce(dateofbirth, expected_dob)), 'FMYYY\"Y\"FMMM\"M\"') "
				+ "			as raw_age_text, "
        		+ "			COALESCE(sa.patient_contact, pd.patient_phone) AS patient_phone,"
        		+ "			COALESCE(sa.patient_contact_country_code, pd.patient_phone_country_code) AS patient_phone_country_code,"
        		+ "			pd.government_identifier,"
        		+ "			pd.oldmrno,"
            + "     pd.salutation as salutation_name,"
        		+ "     pd.email_id,"
        		+ "     CASE WHEN (cpref.receive_communication in ('S','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_sms, "
            + "       CASE WHEN (cpref.receive_communication in ('E','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_email, " 
            + "       cpref.lang_code, "
        		+ "			NULL AS visit_date_time, NULL AS visit_type, "
        		+ "			sa.appointment_time AS appointment_date_time,"
        		+ "			sa.res_sch_category AS appointment_resource_category,"
        		+ "			sa.appointment_time AS activity_date_time,"
        		+ "			0 AS past_visit,"
        		+ "			0 AS current_day_visit,"
        		+ "			0 AS current_day_mlc_visit,"
        		+ "			0 AS current_day_er_visit,"
        		+ "			0 AS current_day_consultations,"
        		+ "			CASE WHEN sa.appointment_time IS NOT NULL AND date(sa.appointment_time) > current_date THEN 1 ELSE 0 END AS future_appointment,"
        		+ "			CASE WHEN sa.appointment_time IS NOT NULL AND date(sa.appointment_time) = current_date THEN 1 ELSE 0 END AS current_day_appointment,"
        		+ "			0 AS no_visit,"
        		+ "			CASE WHEN sa.appointment_time IS NULL THEN 1 ELSE 0 END AS no_appointment,"
        		+ "			dept.dept_name,"
        		+ "			doc.doctor_name,"
        		+ "     ser.service_name,"
        		+ "     dia.test_name,"
        		+ "         COALESCE(samr_dup.duplicate_mr_nos, '') AS duplicate_mr_nos,"
        		+ "         pd.vip_status, cgm.abbreviation"
        		+ "		FROM patient_details pd "
        		+ "   LEFT JOIN contact_preferences cpref on (pd.mr_no = cpref.mr_no)"
        		+ "		LEFT JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group AND cgm.confidentiality_grp_id != 0)"
        		+ "     LEFT JOIN ("
        		+ "         SELECT original_mr_no as mr_no,"
        		+ "                string_agg(mr_no, ',') as duplicate_mr_nos"
        		+ "         FROM patient_details pd"
        		+ "         WHERE original_mr_no IN (" + placeHolders + ") AND (pd.patient_group in "+ patientGroupFilter
        		+ "         OR pd.mr_no in " + mrnoUserFilter + ")"
        		+ "         GROUP BY original_mr_no"
        		+ "     ) AS samr_dup ON samr_dup.mr_no = pd.mr_no"
        		+ "		LEFT JOIN ("
        		+ "			select a.appointment_id, "
        		+ "				COALESCE(CASE WHEN pda.original_mr_no = '' THEN NULL ELSE pda.original_mr_no END, a.mr_no) AS mr_no, "
        		+ "				a.appointment_time, "
        		+ (doctorId != null ? "sai.resource_id," : "a.prim_res_id as resource_id,")
        		+ "				schm.res_sch_category, "
        		+ "				a.patient_contact, "
        		+ "				a.patient_contact_country_code"
        		+ "			FROM scheduler_appointments a "
                + (doctorId != null ? " LEFT JOIN scheduler_appointment_items sai ON (sai.appointment_id = a.appointment_id AND sai.resource_type in ('LABTECH', 'OPDOC', 'DOC') AND sai.resource_id = ?)" : "")
        		+ "			LEFT JOIN scheduler_master schm on schm.res_sch_id = a.res_sch_id AND schm.res_sch_category in ('DOC', 'SNP','DIA')"
        		+ "			AND LOWER(a.appointment_status) in ('booked', 'confirmed')"
        		+ "         JOIN patient_details pda ON (a.mr_no = pda.mr_no AND (pda.mr_no in (" + placeHolders + ") OR pda.original_mr_no in (" + placeHolders + ")) "
        		+ "  AND (pda.patient_group in " + patientGroupFilter + " OR pda.mr_no in " + mrnoUserFilter + "))"
        		+ "			WHERE schm.res_sch_category IS NOT NULL AND (a.mr_no in (" + placeHolders + ") OR pda.original_mr_no in (" + placeHolders + "))"
                + (centerId !=0 ? " AND a.center_id = ?" : "")
                + (doctorId !=null ? " AND sai.resource_id IS NOT NULL" : "")
                + "         AND (a.appointment_time) >= current_date"
                + "			) sa ON sa.mr_no = pd.mr_no"
        		+ "		LEFT JOIN doctors doc ON sa.resource_id = doc.doctor_id"
            + "   LEFT JOIN services ser ON sa.resource_id = ser.service_id"
        		+ "   LEFT JOIN diagnostics dia ON sa.resource_id = dia.test_id"
        		+ "		LEFT JOIN department dept ON doc.dept_id = dept.dept_id"
        		+ "		LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation"
        		+ "		WHERE pd.mr_no in (" + placeHolders + ")"
        		+ "		) all_visits_appt"
        		+ "	) windowed_list WHERE mr_activity_priority_absolute = 1";
		logger.debug(querySql);
        List<Object> args = new ArrayList<Object>();
        args.addAll(mrNos);
        if (doctorId != null) {
            args.add(doctorId);
        }
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        if (doctorId != null) {
            args.add(doctorId);
        }
        if (centerId != 0) {
        	args.add(centerId);
        }
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        if (doctorId != null) {
            args.add(doctorId);
        }
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        args.addAll(mrNos);
        if (centerId != 0) {
        	args.add(centerId);
        }
        args.addAll(mrNos);
        List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, args.toArray());
        Map<String, Patient> mapResults = new HashMap<String, Patient>();
        if (results == null) {
            return mapResults;
        }
        for (BasicDynaBean result : results) {
            mapResults.put(result.get("mr_no").toString(), this.beanToPatient(result, false));
        }
        return mapResults;
    }

    private Map<String, Patient> getPatientListCardByContactIds(List<Object> contactIds, String doctorId, int centerId, Integer appointmentId, String findString) {
        String placeHolders = fillWithQuestionMarksForInQuery(contactIds);
        String querySql = " SELECT full_name, patient_name, patient_phone, patient_phone_country_code,"
            + " sum(future_appointment) as future_appointment_count, contact_id, salutation_name,email_id, "
            + " sum(current_day_appointment) as current_day_appointment_count, send_sms, send_email, "
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(appointment_time) ELSE null::timestamp END as appointment_date_time, "
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(appointment_id) ELSE NULL END as appointment_id,"
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(doctor_name) ELSE NULL END as doctor_name,"
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(test_name) ELSE NULL END as test_name,"
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(service_name) ELSE NULL END as service_name,"
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(res_sch_category) ELSE NULL END as res_sch_category,"
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(dept_name) ELSE NULL END as dept_name,"            
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(appointment_resource_category) ELSE NULL END as appointment_resource_category,"            
            + " CASE WHEN sum(current_day_appointment) < 2 THEN max(dept_name) ELSE NULL END as dept_name,"            
            + " 0 as current_day_mlc_visit_count, 0 as current_day_er_visit_count, 0 as current_day_consultation_count,"
            + " 1 AS mr_activity_priority_absolute, vip_status,"
            + " 0 AS past_visit_count, 0 AS current_day_visit_count,"
            + " NULL AS mr_no, middle_name, last_name,"
            + " patient_gender, patient_dob AS dob, NULL as death_date, "
            + " NULL as death_time, NULL as abbreviation, NULL AS government_identifier,"
            + " NULL AS oldmrno, NULL AS visit_date_time, NULL as visit_type,"
            + " patient_age as age, CONCAT_WS('', patient_age, patient_age_units) AS age_text, "
			+ " CONCAT_WS('', patient_age, patient_age_units) AS raw_age_text,'' AS duplicate_mr_nos"
            + " FROM ("
            + "  SELECT cd.contact_id, cd.vip_status,sa.appointment_id, "
            + "  CONCAT_WS(' ', sm.salutation, cd.patient_name, cd.middle_name, cd.last_name) as full_name,"
            + "  cd.patient_name, cd.middle_name, cd.last_name,"
            + "  cd.patient_contact AS patient_phone, cd.patient_gender,"
            + "  cd.patient_dob, cd.patient_age, cd.patient_age_units,cd.salutation_name,cd.patient_email_id as email_id, "
            + "  cd.patient_contact_country_code AS patient_phone_country_code,cd.send_sms AS send_sms,cd.send_email AS send_email,"
            + "  CASE WHEN sa.appointment_time IS NOT NULL AND date(sa.appointment_time) > current_date "
            + "  THEN 1 ELSE 0 END AS future_appointment, sa.appointment_time, "
            + "  CASE WHEN sa.appointment_time IS NOT NULL AND date(sa.appointment_time) = current_date "
            + "  THEN 1 ELSE 0 END AS current_day_appointment,"
            + "  dept.dept_name, doc.doctor_name, schm.res_sch_category AS appointment_resource_category,"
            + "  dg.test_name, s.service_name, schm.res_sch_category"
            + "  FROM contact_details cd"
            + "  LEFT JOIN scheduler_appointments sa ON (sa.contact_id = cd.contact_id" + (centerId !=0 ? " AND sa.center_id = ?" : "")+" "
            + "  AND LOWER(sa.appointment_status) in ('booked', 'confirmed'))"
            + "  LEFT JOIN salutation_master sm ON (sm.salutation_id = cd.salutation_name)"
            + "  LEFT JOIN diagnostics dg ON (dg.test_id = sa.res_sch_name) "
            + "  LEFT JOIN services s ON (s.service_id = sa.res_sch_name) "
            +    (doctorId != null ? " LEFT JOIN scheduler_appointment_items sai "
                + " ON (sai.appointment_id = sa.appointment_id AND sai.resource_type in ('LABTECH', 'OPDOC', 'DOC')"
                + " AND sai.resource_id = ?)" : "")
                + " LEFT JOIN doctors doc ON ("
                + (doctorId != null ? "sai.resource_id" : "sa.prim_res_id")
                + " = doc.doctor_id)"
                + " LEFT JOIN department dept ON doc.dept_id = dept.dept_id"
                + " LEFT JOIN scheduler_master schm on schm.res_sch_id = sa.res_sch_id AND schm.res_sch_category IN ('DOC', 'SNP', 'DIA')"
                + " WHERE (CASE WHEN sa.appointment_id IS NOT NULL THEN schm.res_sch_category IS NOT NULL ELSE TRUE END)"
                + " AND (CASE WHEN sa.appointment_id IS NOT NULL THEN LOWER(sa.appointment_status) in ('booked', 'confirmed') ELSE TRUE END)"
                + " AND (sa.mr_no is null OR sa.mr_no = '') and cd.contact_id in (" + placeHolders + ") # )"
                + " as foo"
                + " GROUP BY full_name, patient_name, patient_phone, patient_phone_country_code, contact_id, "
                + " current_day_mlc_visit_count, current_day_er_visit_count, mr_activity_priority_absolute,"
                + " current_day_consultation_count, past_visit_count, send_sms, send_email, "
                + " current_day_visit_count, mr_no, middle_name, last_name, salutation_name,email_id, patient_gender, dob, death_date,"
                + " death_time, abbreviation, government_identifier, oldmrno, visit_date_time,"
                + " visit_type, vip_status, age, age_text, duplicate_mr_nos";

        logger.debug(querySql);
        List<Object> args = new ArrayList<Object>();
        if (centerId != 0) {
            args.add(centerId);
          }
        if (doctorId != null) {
            args.add(doctorId);
        }
        args.addAll(contactIds);
        
        if (appointmentId != null && appointmentId != -1) {
          querySql = querySql.replace("#", " and sa.appointment_id = ?");
          args.add(appointmentId);
        } else if ("".equals(findString)){
          querySql = querySql.replace("#", "AND date(sa.appointment_time) = current_date ");
        } else {
          querySql = querySql.replace("#","");
        }
        List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, args.toArray());
        Map<String, Patient> mapResults = new HashMap<String, Patient>();
        if (results == null) {
            return mapResults;
        }
        for (BasicDynaBean result : results) {
            mapResults.put(result.get("contact_id").toString(), this.beanToPatient(result, false));
        }
        return mapResults;
    }

    private Map<String, Patient> getAdvancedPatientListCards(List<Object> mrNos, List<Object> contactIds, AdvancedFilterOptions options) {
        String mrPlaceHolders = fillWithQuestionMarksForInQuery(mrNos);
        
        Map<String, Patient> mapResults = new HashMap<String, Patient>();

        String patientGroupFilter = " (" + ConfidentialityQueryHelper.QUERY_CONFIDENTIALITY_GROUP_ACCESS_USER + ")";
        
        String mrnoUserFilter = "(" + ConfidentialityQueryHelper.QUERY_MRNO_USER_ACCESS + ")";

        if (!mrNos.isEmpty()) {
    		String querySql = "SELECT"
    				+ "		pd.mr_no,"
    		    	+ "		CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS full_name,"
    		    	+ "     sm.salutation as salutation_name," 
    				+ "		pd.patient_name,"
    				+ "		pd.middle_name,"
    				+ "		pd.last_name,"
    				+ "		pd.patient_gender,"
    				+ "		pd.dateofbirth AS dob,"
    				+ "		pd.death_date AS death_date,"
    				+ "		pd.death_time AS death_time,"
    				+ "		pd.patient_phone,"
    				+ "		pd.patient_phone_country_code,"
    				+ "		pd.government_identifier,"
    				+ "     cgm.abbreviation, "
    				+ "     pd.email_id,"
    				+ "     null as contact_id,"
    				+ "     null as res_sch_category,"
    				+ "		COALESCE(mr_dup.duplicate_mr_nos, '') as duplicate_mr_nos,"
    				+ "		CASE WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '5 year' "
    				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMYYYY\"Y\"') "
    				+ "			WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '1 year' "
    				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMYYYY\"Y\"+FMMM\"M\"') "
    				+ "			WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '1 month' "
    				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMMM\"M\"') "
    				+ "			ELSE to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMDD\"D\"') END AS age_text, "
					+ "     to_char(age(coalesce(pd.dateofbirth, pd.expected_dob)), 'FMYYY\"Y\"FMMM\"M\"') "
					+ "			as raw_age_text "
    				+ "		FROM patient_details pd"
    				+ "     LEFT JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group AND cgm.confidentiality_grp_id != 0) "
    				+ "     LEFT JOIN salutation_master sm on sm.salutation_id = pd.salutation"
    				+ "     LEFT JOIN ("
    				+ "         SELECT original_mr_no as mr_no,"
    				+ "                string_agg(mr_no, ',') as duplicate_mr_nos"
    				+ "         FROM patient_details"
    				+ "         WHERE original_mr_no IN (" + mrPlaceHolders + ")"
    				+ "         GROUP BY original_mr_no"
    				+ "     ) AS mr_dup ON mr_dup.mr_no = pd.mr_no"
    				+ "  	WHERE pd.mr_no IN (" + mrPlaceHolders + ") AND (pd.patient_group in " +  patientGroupFilter
    				+ "   OR pd.mr_no in " + mrnoUserFilter +")";
        	List<Object> mrArgs = new ArrayList<Object>();
        	mrArgs.addAll(mrNos);
        	mrArgs.addAll(mrNos);
        	logger.debug(querySql);
    		List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, mrArgs.toArray());
    		if (results != null) {
                for (BasicDynaBean result : results) {
                    mapResults.put(result.get("mr_no").toString(), this.beanToPatient(result, true));
                }    			
    		}
        }
        if (!options.getAppointmentRange().isEmpty()) {
            String apptPlaceholders = fillWithQuestionMarksForInQuery(contactIds);
        	List<Object> apptArgs = new ArrayList<Object>();
        	String querySql = "SELECT"
        			+ " CONCAT_WS(' ', smas.salutation, cd.patient_name, cd.middle_name, cd.last_name) AS full_name,"
        			+ " cd.patient_contact as patient_phone, cd.contact_id, "
        			+ " CONCAT_WS('',cd.patient_age, cd.patient_age_units) as age_text, cd.patient_gender,"
        			+ " a.patient_name,"
        			+ " COALESCE(CASE WHEN pd.original_mr_no = '' THEN NULL ELSE pd.original_mr_no END, pd.mr_no) AS mr_no,"
        			+ " a.appointment_id,"
        			+ " a.visit_id,"
        			+ " a.appointment_status,"
        			+ " DATE(a.appointment_time) AS appointment_date,"
        			+ " a.appointment_time::time AS appointment_time,"
        			+ " sm.res_sch_category AS category,";
        	if (options.getDeptType().equals("service")) {
        		querySql = querySql
        				+ " dept.serv_dept_id AS department_id,"
        				+ " dept.department as department,"
        				+ " a.res_sch_name,"
        				+ " ser.service_name as appointment_resource";        		
        	} else if (options.getDeptType().equals("test")) {
        		querySql = querySql
        				+ " dept.ddept_id AS department_id,"
        				+ " dept.ddept_name as department,"
        				+ " a.res_sch_name,"
        				+ " dia.test_name as appointment_resource";        		
            } else {
        		querySql = querySql
        				+ " dept.dept_id AS department_id,"
        				+ " dept.dept_name as department,"
        				+ " a.prim_res_id,"
        				+ " doc.doctor_name as appointment_resource";
        	}
        	querySql = querySql
        	        + " FROM contact_details cd"
        			+ " LEFT JOIN scheduler_appointments a on a.contact_id = cd.contact_id"
        			+ " LEFT JOIN patient_details pd on pd.mr_no = a.mr_no"
                    + " LEFT JOIN scheduler_master sm on sm.res_sch_id = a.res_sch_id"
                    + " LEFT JOIN salutation_master smas on smas.salutation_id = cd.salutation_name";
        	if (options.getDeptType().equals("service")) {
        		querySql = querySql
        				+ " LEFT JOIN services ser ON a.res_sch_name = ser.service_id"
        				+ " LEFT JOIN services_departments dept ON ser.serv_dept_id = dept.serv_dept_id";
        	} else if (options.getDeptType().equals("test")) {
        		querySql = querySql
        				+ " LEFT JOIN diagnostics dia ON a.res_sch_name = dia.test_id"
        				+ " LEFT JOIN diagnostics_departments dept ON dia.ddept_id = dept.ddept_id";
        	} else {
        		querySql = querySql
        				+ " LEFT JOIN doctors doc ON a.prim_res_id = doc.doctor_id"
        				+ " LEFT JOIN department dept ON doc.dept_id = dept.dept_id";
        	}
        	querySql = querySql + " WHERE"
        			+ " (DATE(a.appointment_time) BETWEEN ? AND ?) AND "
        			+ " ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )";
			apptArgs.add(new java.sql.Date(options.getAppointmentRange().get(0).getTime()));
			apptArgs.add(new java.sql.Date(options.getAppointmentRange().get(1).getTime()));
        	if (!contactIds.isEmpty() && !mrNos.isEmpty()) {
        		querySql = querySql + " AND (a.mr_no in (" + mrPlaceHolders + ")"
        				+ " OR pd.original_mr_no IN (" + mrPlaceHolders + ")"
        				+ " OR a.contact_id IN (" + apptPlaceholders + "))";
        		apptArgs.addAll(mrNos);
        		apptArgs.addAll(mrNos);
        		apptArgs.addAll(contactIds);
        	} else if (!mrNos.isEmpty()) {
        		querySql = querySql + " AND (a.mr_no in (" + mrPlaceHolders + ")"
        				+ " OR pd.original_mr_no IN (" + mrPlaceHolders + "))";
        		apptArgs.addAll(mrNos);
        		apptArgs.addAll(mrNos);
        	} else if (!contactIds.isEmpty()) {
        		querySql = querySql + " AND (a.contact_id IN (" + apptPlaceholders + "))";
        		apptArgs.addAll(contactIds);
        	}
        	if (!options.getDeptType().isEmpty()) {
        		String deptFilterValue = " IS NOT NULL";
        		if (!options.getDeptIds().isEmpty()) {
    				deptFilterValue = "::character varying IN (" + fillWithQuestionMarksForInQuery(options.getDeptIds()) + ")";
    				apptArgs.addAll(options.getDeptIds());
        		}
            	if (options.getDeptType().equals("consultation")) {
        			querySql = querySql + " AND dept.dept_id" + deptFilterValue;
            	} else if (options.getDeptType().equals("service")) {
            		querySql = querySql + " AND dept.serv_dept_id" + deptFilterValue;
            	} else if (options.getDeptType().equals("test")) {
            		querySql = querySql + " AND dept.ddept_id" + deptFilterValue;
            	}
        	} else {
        		String docFilterValue = " IS NOT NULL";
        		if (!options.getDoctorIds().isEmpty()) {
    				docFilterValue = "::character varying IN (" + fillWithQuestionMarksForInQuery(options.getDoctorIds()) + ")";
    				apptArgs.addAll(options.getDoctorIds());
        		}
        		querySql = querySql + " AND doc.doctor_id" + docFilterValue;
        	}
        	if (options.getCenterId() != 0) {
        		querySql = querySql + " AND a.center_id = ?";
        		apptArgs.add(options.getCenterId());
        	}
        	logger.debug(querySql);
    		List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, apptArgs.toArray());
    		if (results == null) {
    			return mapResults;
    		}
            for (BasicDynaBean result : results) {
            	String mrNo = (String) result.get("mr_no");
            	Patient patient = null;
            	if (mrNo != null && mapResults.containsKey(mrNo)) {
            		patient = mapResults.get(mrNo);
            	} else {
            		patient = new Patient();
            		patient.setFull_name((String) result.get("full_name"));
            		patient.setPatient_phone((String) result.get("patient_phone"));
                	patient.setPatient_name((String) result.get("patient_name"));
            	}
            	String apptId = result.get("appointment_id").toString();
            	String contactId = result.get("contact_id").toString();
            	Appointment appt = new Appointment();
            	appt.setAppointment_id(Integer.parseInt(apptId));
            	appt.setAppointment_date(DateUtil.formatDate((java.sql.Date) result.get("appointment_date")));
            	appt.setAppointment_resource((String) result.get("appointment_resource"));
            	appt.setAppointment_time(result.get("appointment_time").toString());
            	appt.setCategory((String) result.get("category"));
            	appt.setDepartment((String) result.get("department"));
            	appt.setDepartment_id(result.get("department_id").toString());
            	appt.setMr_no(mrNo);
            	appt.setPatient_name((String) result.get("patient_name"));
            	appt.setPatient_phone((String) result.get("patient_phone"));
            	appt.setVisit_id((String) result.get("visit_id"));
            	appt.setStatus((String) result.get("appointment_status"));
            	appt.setAge_text((String) result.get("age_text"));
            	appt.setPatient_gender((String) result.get("patient_gender"));
            	patient.addAppointment(appt);
            	mapResults.put(mrNo != null ? mrNo : contactId, patient);
            }    			
        }
        if (!options.getVisitRange().isEmpty() && !mrNos.isEmpty()) {
        	List<Object> visitArgs = new ArrayList<Object>();
            String visitFilter = "";
            if (options.getVisitType().equalsIgnoreCase("o")) {
                visitFilter = "r.visit_type = 'o'";
            } else if (options.getVisitType().equalsIgnoreCase("i")) {
                visitFilter = "r.visit_type = 'i'";
            }
        	String querySql = "SELECT"
        			+ " COALESCE(CASE WHEN pd.original_mr_no = '' THEN NULL ELSE pd.original_mr_no END, pd.mr_no) AS mr_no,"
        			+ " r.patient_id AS visit_id,"
        			+ " r.reg_date AS visit_date,"
        			+ " r.reg_time AS visit_time,"
        			+ " rdept.dept_name AS visit_dept_name,"
        			+ " r.dept_name AS visit_dept_id,"
        			+ " r.status AS visit_status,"
        			+ " sec_ico.insurance_co_name as secondary_insurance,"
        			+ " pri_ico.insurance_co_name as primary_insurance,"
        			+ " bac.activity_code as order_type,"
        			+ " bc.charge_id as order_id,"
        			+ " null as contact_id,"
        			+ " CASE WHEN bac.activity_code = 'DOC' THEN cdept.dept_id"
        			+ "      WHEN bac.activity_code = 'SER' THEN sdept.serv_dept_id::character varying"
        			+ "      WHEN bac.activity_code = 'DIA' THEN ddept.ddept_id END AS order_department_id,"
        			+ " CASE WHEN bac.activity_code = 'DOC' THEN cdept.dept_name"
        			+ "      WHEN bac.activity_code = 'SER' THEN sdept.department"
        			+ "      WHEN bac.activity_code = 'DIA' THEN ddept.ddept_name END AS order_department,"
        			+ " CASE WHEN bac.activity_code = 'DOC' THEN doc.doctor_name"
        			+ "      WHEN bac.activity_code = 'SER' THEN ser.service_name"
        			+ "      WHEN bac.activity_code = 'DIA' THEN dia.test_name END AS order_item_name,"
        			+ " b.bill_no,"
        			+ " b.status AS bill_status,"
        			+ " b.payment_status AS bill_payment_status"
        			+ " FROM patient_registration r"
        			+ " LEFT JOIN patient_details pd on pd.mr_no = r.mr_no"
		        	+ " LEFT JOIN patient_insurance_plans ins ON ins.patient_id = r.patient_id"
					+ " LEFT JOIN insurance_company_master pri_ico on pri_ico.insurance_co_id = ins.insurance_co and ins.priority = 1"
		        	+ " LEFT JOIN insurance_company_master sec_ico on sec_ico.insurance_co_id = ins.insurance_co and ins.priority = 2"
					+ " LEFT JOIN bill b ON b.visit_id = r.patient_id and b.status != 'X' AND b.restriction_type != 'P'"
    				+ " LEFT JOIN bill_charge bc ON bc.bill_no = b.bill_no and bc.status = 'A'"
    				+ " LEFT JOIN bill_activity_charge bac ON bc.charge_id = bac.charge_id"
        			+ " LEFT JOIN diagnostics dia ON bac.act_description_id = dia.test_id AND bac.activity_code = 'DIA'"
        			+ " LEFT JOIN services ser ON bac.act_description_id = ser.service_id AND bac.activity_code = 'SER'"
        			+ " LEFT JOIN doctors doc ON bac.act_description_id = doc.doctor_id AND bac.activity_code = 'DOC'"
        			+ " LEFT JOIN department cdept ON doc.dept_id = cdept.dept_id"
        			+ " LEFT JOIN department rdept ON r.dept_name = rdept.dept_id"
        			+ " LEFT JOIN services_departments sdept ON ser.serv_dept_id = sdept.serv_dept_id"
        			+ " LEFT JOIN diagnostics_departments ddept ON dia.ddept_id = ddept.ddept_id"
        			+ " WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )"
        			+ " AND"
        			+ "   (r.mr_no in (" + mrPlaceHolders + ") OR pd.original_mr_no IN (" + mrPlaceHolders + "))"
        			+ "   AND " + visitFilter
					+ "   AND (r.reg_date BETWEEN ? AND ?)"
					+ (options.getDeptType().isEmpty() ? " AND (bac.activity_code in ('DIA', 'SER', 'DOC') OR bac.activity_code IS NULL)" : "");
					
			visitArgs.addAll(mrNos);
			visitArgs.addAll(mrNos);
			visitArgs.add(new java.sql.Date(options.getVisitRange().get(0).getTime()));
			visitArgs.add(new java.sql.Date(options.getVisitRange().get(1).getTime()));
			if (options.getCenterId() != 0) {
        		querySql = querySql + " AND r.center_id = ?";
        		visitArgs.add(options.getCenterId());
        	}
			if (options.isAllBillStatuses()) {
        		querySql = querySql + " AND b.status IS NOT NULL";
			} else if (!options.getBillStatuses().isEmpty()) {
				querySql = querySql + " AND b.status IN (" + fillWithQuestionMarksForInQuery(options.getBillStatuses()) + ")";
				visitArgs.addAll(options.getBillStatuses());				
			} else if (!options.getVisitStatuses().isEmpty()) {
				querySql = querySql + " AND r.status IN (" + fillWithQuestionMarksForInQuery(options.getVisitStatuses()) + ")";
				visitArgs.addAll(options.getVisitStatuses());				
			}
			if (options.isAllDoctorIds()) {
        		querySql = querySql + " AND doc.doctor_id IS NOT NULL AND bac.activity_code = 'DOC'";
        	} else if (!options.getDoctorIds().isEmpty()){
				querySql = querySql + " AND doc.doctor_id::character varying IN (" + fillWithQuestionMarksForInQuery(options.getDoctorIds()) + ")";
				visitArgs.addAll(options.getDoctorIds());
        	} else if (options.isAllPriInsurance() || options.isAllSecInsurance() 
        			|| !options.getPriInsurance().isEmpty() || !options.getSecInsurance().isEmpty()) {
        		querySql = querySql + " AND (";
        		if (options.isAllPriInsurance()) {
        			querySql = querySql + "pri_ico.insurance_co_id IS NOT NULL";
        		} else if (!options.getPriInsurance().isEmpty()) {
        			querySql = querySql + "pri_ico.insurance_co_id IN (" + fillWithQuestionMarksForInQuery(options.getPriInsurance()) + ")";
        			visitArgs.addAll(options.getPriInsurance());
        		}
        		if ((options.isAllPriInsurance() || !options.getPriInsurance().isEmpty()) && (options.isAllSecInsurance() || !options.getSecInsurance().isEmpty())) {
        			querySql = querySql + " OR ";
        		}
        		if (options.isAllSecInsurance()) {
        			querySql = querySql + "sec_ico.insurance_co_id IS NOT NULL";
        		} else if (!options.getSecInsurance().isEmpty()) {
        			querySql = querySql + "sec_ico.insurance_co_id IN (" + fillWithQuestionMarksForInQuery(options.getSecInsurance()) + ")";
        			visitArgs.addAll(options.getSecInsurance());
        		}
        		querySql = querySql + ")";
        	} else if (!options.getDeptType().isEmpty()) {
        		String deptFilterValue = " IS NOT NULL";
        		if (!options.getDeptIds().isEmpty()) {
    				deptFilterValue = "::character varying IN (" + fillWithQuestionMarksForInQuery(options.getDeptIds()) + ")";
    				visitArgs.addAll(options.getDeptIds());
    				if (options.getDeptType().equals("consultation")) {
        				visitArgs.addAll(options.getDeptIds());    					
    				}
        		}
            	if (options.getDeptType().equals("consultation")) {
        			querySql = querySql + " AND (r.dept_name" + deptFilterValue + " OR (bac.activity_code = 'DOC' AND cdept.dept_id" + deptFilterValue + "))";
            	} else if (options.getDeptType().equals("service")) {
            		querySql = querySql + " AND bac.activity_code = 'SER' AND sdept.serv_dept_id" + deptFilterValue;
            	} else if (options.getDeptType().equals("test")) {
            		querySql = querySql + " AND bac.activity_code = 'DIA' AND ddept.ddept_id" + deptFilterValue;
            	}
            }
    		List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, visitArgs.toArray());
    		if (results == null) {
    			return mapResults;
    		}
    		Map<String, Visit> visits = new HashMap<String, Visit>();
    		Map<String, Order> orders = new HashMap<String, Order>();
    		Map<String, Bill> bills = new HashMap<String, Bill>();
            for (BasicDynaBean result : results) {
            	String mrNo = (String) result.get("mr_no");
            	String visitId = (String) result.get("visit_id");
            	String orderId = (String) result.get("order_id");
            	String billNo = (String) result.get("bill_no");
        		String orderType = (String) result.get("order_type");
        		if (visitId != null && !visits.containsKey(visitId)) {
            		Visit visit = new Visit();
            		visit.setVisit_id(visitId);
            		visit.setMr_no(mrNo);
            		visit.setDept_name((String) result.get("visit_dept_id"));
            		visit.setDepartment((String) result.get("visit_dept_name"));
            		visit.setVisit_date(DateUtil.formatDate((java.sql.Date) result.get("visit_date")));
            		visit.setVisit_time(result.get("visit_time").toString());
            		visit.setPrimary_insurance((String) result.get("primary_insurance"));
            		visit.setSecondary_insurance((String) result.get("secondary_insurance"));
            		visit.setVisit_status((String) result.get("visit_status"));
            		visits.put(visitId, visit);
                	//Include Visit Department
            		Order order = new Order();
            		order.setMr_no(mrNo);
            		order.setVisit_id(visitId);
            		order.setDepartment((String) result.get("visit_dept_name"));
            		order.setDepartment_id((String) result.get("visit_dept_id"));
            		orders.put("DUMMYORDER_" + visitId, order);  
            	} else if (visitId != null && visits.containsKey(visitId)) {
            		Visit visit = visits.get(visitId);
            		String priInsurance = (String) result.get("primary_insurance");
            		String secInsurance = (String) result.get("secondary_insurance");
            		if (visit.getPrimary_insurance() == null && priInsurance != null) {
            			visit.setPrimary_insurance(priInsurance);
            		}
            		if (visit.getSecondary_insurance() == null && secInsurance != null) {
            			visit.setSecondary_insurance(secInsurance);
            		}
            		visits.put(visitId, visit);
            	}
            	if (orderId != null && !orders.containsKey(orderId) && 
            			!(options.getDeptType().equals("consultation") && (orderType == null || !orderType.equalsIgnoreCase("DOC")))) {
            		Order order = new Order();
            		order.setMr_no(mrNo);
            		order.setVisit_id(visitId);
            		order.setItem_name((String) result.get("order_item_name"));
            		order.setOrder_type(orderType);
            		order.setDepartment((String) result.get("order_department"));
            		order.setDepartment_id((String) result.get("order_department_id"));
            		orders.put(orderId, order);
            	}
            	if (billNo != null && !bills.containsKey(billNo)) {
            		Bill bill = new Bill();
            		bill.setMr_no(mrNo);
            		bill.setBill_no(billNo);
            		bill.setBill_status((String) result.get("bill_status"));
            		bill.setPayment_status((String) result.get("bill_payment_status"));
            		bill.setVisit_id(visitId);
            		bills.put(billNo, bill);
            	}
            }
            for (Visit visit : visits.values()) {
            	if (!mapResults.containsKey(visit.getMr_no())) {
            		continue;
            	}
            	Patient patient = mapResults.get(visit.getMr_no());
            	patient.addVisit(visit);
            }
            for (Order order : orders.values()) {
            	if (!mapResults.containsKey(order.getMr_no())) {
            		continue;
            	}
            	Patient patient = mapResults.get(order.getMr_no());
            	patient.addOrder(order);
            }
            for (Bill bill : bills.values()) {
            	if (!mapResults.containsKey(bill.getMr_no())) {
            		continue;
            	}
            	Patient patient = mapResults.get(bill.getMr_no());
            	patient.addBill(bill);
            }
        }
        return mapResults;
    }
    
    private Patient beanToPatient(BasicDynaBean bean, boolean skipOtherDetails) {
		Patient patient = new Patient();
		patient.setMr_no((String) bean.get("mr_no"));
		patient.setFull_name((String) bean.get("full_name"));
		patient.setPatient_name((String) bean.get("patient_name"));
		patient.setMiddle_name((String) bean.get("middle_name"));
		patient.setLast_name((String) bean.get("last_name"));
		patient.setPatient_gender((String) bean.get("patient_gender"));
		patient.setAbbreviation((String) bean.get("abbreviation"));
		patient.setSalutationName(bean.get("salutation_name") != null ? (String)bean.get("salutation_name") : null );
		patient.setEmailId(bean.get("email_id") != null ? (String)bean.get("email_id") : null );
	  	patient.setContactId(bean.get("contact_id") != null ? (Integer) bean.get("contact_id"): null);
		if (bean.get("dob") != null) {
			patient.setDate_of_birth(DateUtil.formatDate((java.sql.Date) bean.get("dob")));
		}
		if (bean.get("death_date") != null) {
			if (bean.get("death_time") != null) {
				patient.setDate_of_death(
					DateUtil.formatDate((java.sql.Date) bean.get("death_date"))
					+ " " + DateUtil.formatSQlTime((java.sql.Time) bean.get("death_time"))
				);
			} else {
				patient.setDate_of_death(DateUtil.formatDate((java.sql.Date) bean.get("dob")) + " 00:00:00");
			}
		}
		patient.setPatient_phone((String) bean.get("patient_phone"));
		patient.setPatient_phone_country_code((String) bean.get("patient_phone_country_code"));
		patient.setGovernment_identifier((String) bean.get("government_identifier"));
		//Old Mr No ?
		if (!bean.get("duplicate_mr_nos").toString().isEmpty()) {
			patient.setDuplicate_mr_nos(Arrays.asList(bean.get("duplicate_mr_nos").toString().split(",")));
		}
		String ageText = (String) bean.get("age_text");
		if (ageText != null) {
			patient.setAge_text(ageText.replace("+0M", ""));
		}
		patient.setRawAgeText((String)bean.get("raw_age_text"));

		if (skipOtherDetails) {
			return patient;
		}
		OtherDetails od = patient.new OtherDetails();
		if (bean.get("contact_id") != null) {
			od.setAppointment_id((Integer) bean.get("contact_id"));
		}
		Map keyMap = bean.getMap();
		od.setSendEmail(keyMap.containsKey("send_email") && bean.get("send_email") != null ? (String)bean.get("send_email") : "Y");
		od.setSendSms(keyMap.containsKey("send_sms") && bean.get("send_sms") != null ? (String)bean.get("send_sms") : "Y");
		od.setLangCode(keyMap.containsKey("lang_code") && bean.get("lang_code") != null ? (String)bean.get("lang_code") : null);
		//Past visit count?
		od.setTodays_visit_count(Long.parseLong(bean.get("current_day_visit_count").toString()));
		if (od.getTodays_visit_count() > 0 && bean.get("visit_date_time") != null) {
			od.setTodays_visit_time(DateUtil.formatTimestamp((java.sql.Timestamp) bean.get("visit_date_time")));
			od.setTodays_visit_type((String) bean.get("visit_type"));
			od.setDoctor_name((String) bean.get("doctor_name"));
		} else if (bean.get("visit_date_time") != null) {
			od.setLast_visited_date(DateUtil.formatTimestamp((java.sql.Timestamp) bean.get("visit_date_time")));
		}
		od.setFuture_appointments_count(Long.parseLong(bean.get("future_appointment_count").toString()));
		od.setTodays_appointments_count(Long.parseLong(bean.get("current_day_appointment_count").toString()));
		if (od.getTodays_appointments_count() > 0 && bean.get("appointment_date_time") != null) {
			od.setTodays_appointment_time(DateUtil.formatTimestamp((java.sql.Timestamp) bean.get("appointment_date_time")));
	         if (bean.get("res_sch_category") != null) {
	              if (bean.get("res_sch_category").equals("DOC")) {
	                od.setResource_name((String) bean.get("doctor_name"));
	              } else if (bean.get("res_sch_category").equals("DIA")) {
	                od.setResource_name((String) bean.get("test_name"));
	              } else if (bean.get("res_sch_category").equals("SNP")) {
	                od.setResource_name((String) bean.get("service_name"));
	              }
	            }
	         if (bean.get("appointment_id") != null) {
	           od.setApptId((Integer) bean.get("appointment_id"));
	         }
  
		}
		od.setTodays_doctor_order_count(Long.parseLong(bean.get("current_day_consultation_count").toString()));
		od.setDept_name((String) bean.get("dept_name"));
		od.setCategory((String) bean.get("appointment_resource_category"));
		String vipStatus = (String) bean.get("vip_status");
		od.setVip(vipStatus != null && vipStatus.equalsIgnoreCase("Y"));
		od.setMlc(Long.parseLong(bean.get("current_day_mlc_visit_count").toString()) > 0);
		od.setEr(Long.parseLong(bean.get("current_day_er_visit_count").toString()) > 0);
		patient.setOther_details(od);
		
		return patient;
    }


	public Map<String, Object> searchRegisteredPatients(MultiValueMap<String, String> params) {
		boolean withCount = params.getFirst("with_count") != null ? parseBool(params.getFirst("with_count")) : false;
    boolean allowConfidentialPatients = params.getFirst("allow_confidential_patients") != null
        ? parseBool(params.getFirst("allow_confidential_patients")) : false;
		String findString = (params.getFirst("q") != null && !params.getFirst("q").isEmpty()) ? params.getFirst("q").trim() : "";
    Map<String, Object> mapResults = new HashMap<String, Object>();
		if (findString.equals("")){
      mapResults.put("page_size", 15);
      mapResults.put("page_num", 1);
      mapResults.put("total_records", 0);
      mapResults.put("patients", new ArrayList<Object>());
      return mapResults;
		}
		String searchOn = (params.getFirst("search_on") != null && searchOnValues.contains(params.getFirst("search_on").toLowerCase())) ? 
				params.getFirst("search_on").toLowerCase() : null;
		String matchType = (params.getFirst("match_type") != null && matchTypeValues.contains(params.getFirst("match_type").toLowerCase())) ? 
				params.getFirst("match_type").toLowerCase() : null;
		Integer pageSize = params.getFirst("page_size") != null ? Integer.parseInt(params.getFirst("page_size")) : 15;
		int pageNum = params.getFirst("page_num") != null ? Integer.parseInt(params.getFirst("page_num")) : 1;
		pageNum = pageNum < 0 ? 15 : pageNum;
		pageSize = pageSize < 0 ? 15 : pageSize;
		pageSize = pageSize > 100 ? 100 : pageSize;
		int roleId = RequestContext.getRoleId();
		Map urlRightsMap = (Map) RequestContext.getSession().getAttribute("urlRightsMap");
		Map actionRightsMap = (Map) RequestContext.getSession().getAttribute("actionRightsMap");
		int centerId = RequestContext.getCenterId();
		BasicDynaBean genPrefBean = genPrefService.getPreferences();	
        String enableForceSelection = (String) genPrefBean.get("enable_force_selection_for_mrno_search");
        enableForceSelection = enableForceSelection == null ? "N" : enableForceSelection;
        boolean withSchedulerDetails = params.getFirst("with_scheduler_details") != null ? parseBool(params.getFirst("with_scheduler_details")) : false;
        // All center access available to InstaAdmin, admin roles.
		// For other roles it is allowed if user has screen access to Registration or Orders 
		// and action right "Show Other Center Patients on Reg Screen".
		// Action right is discarded if system pref "Registration -> Selection For Mrno Search" is No
        // Refer Bugzilla BUG 45807 for center filter for discard condition
		boolean allCenterAccess = roleId == 1 || roleId == 2;
		if (params.get("with_scheduler_details") != null && params.get("with_scheduler_details").get(0).equalsIgnoreCase("true")) {
			allCenterAccess = allCenterAccess || (enableForceSelection.equals("Y")
					&& actionRightsMap.get("show_other_center_patients").equals("A")
					|| enableForceSelection.equals("N"));
    } else {
      allCenterAccess = allCenterAccess || ((urlRightsMap.get("new_op_registration").equals("A")
					|| urlRightsMap.get("order").equals("A"))
					&& (enableForceSelection.equals("N")
					|| (enableForceSelection.equals("Y")
					&& actionRightsMap.get("show_other_center_patients").equals("A"))));
     }
		List<String> filterParams = this.getArgsList(params.get("filters"));
		List<String> fields = new ArrayList<String>(Arrays.asList(
				"pd.mr_no",
				"pd.patient_name",
				"pd.middle_name",
				"pd.last_name",
				"pd.patient_gender",
				"pd.dateofbirth as dob",
				"pd.patient_phone",
				"pd.patient_phone_country_code",
				"pd.government_identifier",
				"pd.oldmrno",
				"pd.vip_status",
				"cgm.abbreviation",
				"sm.salutation_id ",
				"pd.email_id as patient_email_id",		
				"CASE WHEN (cpref.receive_communication in ('S','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_sms",
			    "CASE WHEN (cpref.receive_communication in ('E','B')  OR cpref.receive_communication is null) then 'Y' else 'N' end as send_email",
				"cpref.lang_code ",
				"CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS full_name",
				"CASE WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '5 year' "
				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMYYYY\"Y\"') "
				+ "			WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '1 year' "
				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMYYYY\"Y\"+FMMM\"M\"') "
				+ "			WHEN AGE(COALESCE(pd.dateofbirth, pd.expected_dob)) >= interval '1 month' "
				+ "			THEN to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMMM\"M\"') "
				+ "			ELSE to_char(AGE(COALESCE(pd.dateofbirth, pd.expected_dob)), 'FMDD\"D\"') END AS age_text"));
		if(allowConfidentialPatients) {
		  fields = new ArrayList<String>(Arrays.asList("pd.mr_no","pd.patient_name","pd.middle_name","pd.last_name"));
		}
		if (withCount) {
			fields.add("count(pd.mr_no) OVER (PARTITION BY 1) AS cn");
		}
		if (allowConfidentialPatients) {
		  fields.add(" cgm.abbreviation as patient_group ");
		}
		List<String> joins = new ArrayList<String>();
		List<String> filters = new ArrayList<String>();
		List<String> groupBy = new ArrayList<String>();
		List<String> sortOn = new ArrayList<String>();
		List<String> searchOnList = new ArrayList<String>();
		List<Object> args = new ArrayList<Object>();
		if (searchOn == null) {
			searchOnList.add("mr_no");
			searchOnList.add("government_identifier");
			if (findString.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
				searchOnList.add("phone_number");
			}
			if (findString.matches("\\D+")) {
				searchOnList.add("name");
			}
			//sortOn.add("pd.mr_no");
		} else {
			searchOnList.add(searchOn);
			/*if (fieldMap.containsKey(searchOn)) {
				sortOn.add(sortMap.get(searchOn));
			} else {
				sortOn.add("pd.mr_no");
			}*/
			
		}
		if (matchType != null && matchType.equalsIgnoreCase("exact_match") && !findString.isEmpty()) {
			List<String> partFilter = new ArrayList<String>();
			for (String filterField: searchOnList) {
				if (!fieldMap.containsKey(filterField)) {
					continue;
				}
				Object obj = fieldMap.get(filterField);
				if (obj instanceof List) {
					List<String> list = (List<String>) obj;
					for (String item : list) {
						boolean hasLower = item.toUpperCase().startsWith("LOWER"); 
						String value = hasLower ? findString.toLowerCase() : findString;
						partFilter.add(item + " = ?");
						args.add(value);
					}
				} else if (obj instanceof String) {
					String field = (String) fieldMap.get(filterField);
					boolean hasLower = field.toUpperCase().startsWith("LOWER"); 
					String value = hasLower ? findString.toLowerCase() : findString;
					partFilter.add(field + " = ?");
					args.add(value);
				}
			}
			filters.add("(" + StringUtils.collectionToDelimitedString(partFilter, " OR ") + ")");
		} else if (!findString.isEmpty()) {
			String[] stringParts = findString.split(" ");
			for (String stringPart : stringParts) {
				if (stringPart.isEmpty()) {
					continue;
				}
				List<String> partFilter = new ArrayList<String>();
				for (String filterField: searchOnList) {
					if (!fieldMap.containsKey(filterField)) {
						continue;
					}
					String fieldMatchType = matchType;
					if (fieldMatchType == null) {
						fieldMatchType = defaultMatchType.get(filterField);
					}
					Object obj = fieldMap.get(filterField);
					if (obj instanceof List) {
						List<String> list = (List<String>) obj;
						for (String item : list) {
							boolean hasLower = item.toUpperCase().startsWith("LOWER"); 
							String value = hasLower ? stringPart.toLowerCase() : stringPart;
							if (fieldMatchType.equalsIgnoreCase("ends_with")) {
	              if (filterField.equalsIgnoreCase("mr_no")) {
	                String revValue = new StringBuilder().append(value).reverse().toString().toUpperCase();
	                partFilter.add("reverse(" + item + ") LIKE ?");
	                args.add(revValue + "%");
	              } else {
	                partFilter.add(item + " LIKE ?");
	                args.add("%" + value);
	              }
							} else if (fieldMatchType.equalsIgnoreCase("starts_with")) {
	              partFilter.add(item + " LIKE ?");
								args.add(value + "%");
							} else if (fieldMatchType.equalsIgnoreCase("contains")) {
	              partFilter.add(item + " LIKE ?");
								args.add("%" + value + "%");
							}
						}
					} else if (obj instanceof String) {
						String field = (String) fieldMap.get(filterField);
						boolean hasLower = field.toUpperCase().startsWith("LOWER"); 
						String value = hasLower ? stringPart.toLowerCase() : stringPart;
						if (fieldMatchType.equalsIgnoreCase("ends_with")) {
							if (filterField.equalsIgnoreCase("mr_no")) {
								String revValue = new StringBuilder().append(value).reverse().toString();
								partFilter.add("reverse(" + field + ") LIKE ?");
								args.add(revValue + "%");
							} else {
								partFilter.add(field + " LIKE ?");
								args.add("%" + value);
							}
						} else if (fieldMatchType.equalsIgnoreCase("starts_with")) {
							partFilter.add(field + " LIKE ?");
							args.add(value + "%");
						} else if (fieldMatchType.equalsIgnoreCase("contains")) {
							partFilter.add(field + " LIKE ?");
							args.add("%" + value + "%");
						}
					}
				}
				filters.add("(" + StringUtils.collectionToDelimitedString(partFilter, " OR ") + ")");
			}
		}
		
		if (filterParams.contains("skip_duplicate_mrno")) {
			filters.add("(pd.original_mr_no IS NULL or pd.original_mr_no = '')");
		}
		
		//If center is not default restrict search scope to center level
		if (!allCenterAccess && centerId !=0) {
			joins.add("patient_registration pr on pr.mr_no = pd.mr_no");
			filters.add("pr.center_id = ?");
			args.add(centerId);
			groupBy.add("pd.mr_no, sm.salutation, cgm.abbreviation, sm.salutation_id ,cpref.receive_communication , cpref.lang_code ");
		}
		joins.add("confidentiality_grp_master cgm on (cgm.confidentiality_grp_id = pd.patient_group AND cgm.confidentiality_grp_id != 0)");
		joins.add("salutation_master sm on sm.salutation_id = pd.salutation");
		joins.add("contact_preferences cpref on pd.mr_no = cpref.mr_no");
    if (!allowConfidentialPatients) {
      filters.add("( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no)) ");
    }
    else {
      filters.add(" pd.patient_group not in (SELECT ufa.confidentiality_grp_id from user_confidentiality_association ufa "
          + " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id) "
          + " where emp_username = current_setting('application.username') "
          + " AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT confidentiality_grp_id from"
          + " confidentiality_grp_master where break_the_glass != 'Y' UNION SELECT 0) ");
      filters.add(" pd.mr_no not in (SELECT mr_no from user_mrno_association where emp_username = current_setting('application.username')) ");
    }
		String querySql = this.getQuery("patient_details pd", fields, joins, null, filters, groupBy, sortOn);
		if (pageSize != 0) {
			querySql += " LIMIT ?";
			args.add(pageSize);
		}
		
		if (pageNum != 0) {
			querySql += " OFFSET ?";
			args.add((pageNum - 1) * pageSize);
		}
        List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, args.toArray());
        mapResults.put("page_size", pageSize);
        mapResults.put("page_num", pageNum);
        if (results == null || results.isEmpty()) {
            mapResults.put("total_records", 0);
        	mapResults.put("patients", new ArrayList<Object>());
        	
            return mapResults;
        }
        mapResults.put("total_records", withCount ? results.get(0).get("cn") : 0);
        Map<String, Map<String,Object>> patientMap = new HashMap<String,Map<String,Object>>();
        for (BasicDynaBean result : results) {
        	Map<String,Object> resultMap = new HashMap<String, Object>(result.getMap());
        	resultMap.remove("cn");
        	patientMap.put((String) resultMap.get("mr_no"), resultMap);
        }
        
        if(withSchedulerDetails){
          List<String> patientMaplist = new ArrayList<String>(patientMap.keySet());
	        String placeHolders = fillWithQuestionMarksForInQuery(patientMaplist);
	        
			String noshowCountQuery = "select distinct count(CASE WHEN appointment_status='Noshow'  THEN 1 else null end) OVER (partition by mr_no) AS noshow_count,"
					+ " count(appointment_status) OVER (partition by mr_no) AS total_appointment_count, mr_no "
					+ "FROM scheduler_appointments where appointment_time::date > (CURRENT_DATE -INTERVAL '1 year')::date and mr_no in ("+placeHolders+") ";
	
			
			List<BasicDynaBean> apptResults = DatabaseHelper.queryToDynaList(noshowCountQuery,patientMap.keySet().toArray());
	        
	        for (BasicDynaBean apptCount: apptResults) {
	        	Map<String, Object> patient = patientMap.get((String)apptCount.get("mr_no"));
	        	patient.put("noshow_count", apptCount.get("noshow_count"));
	        	patient.put("total_appointment_count", apptCount.get("total_appointment_count"));
	        }
        }
        mapResults.put("patients", patientMap.values());
        
        return mapResults;
	}
	
	private static final String GET_PATIENT_NAME_LIST = "SELECT pd.mr_no, pd.patient_name,pd.middle_name,pd.last_name, "
	          + " pd.patient_phone, cgm.abbreviation as patient_group "
						+ " from patient_details pd "
						+ " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group) WHERE ";
	
	public List<BasicDynaBean> getPatientList(String[] strTokens,
				String phoneNo, boolean phoneMatch, int acceptableDiff) {
	  boolean first ;
    if(strTokens.length > 4){
      return Collections.<BasicDynaBean>emptyList();
    }
    String nationalNumber = PhoneNumberUtil.getNationalNumber(phoneNo);
    String  phnoWtSplChar= phoneNo.replaceAll("\\+","");
    String phnoWtZero = "0"+nationalNumber;
    StringBuilder queryStr = new StringBuilder(GET_PATIENT_NAME_LIST);
    first = true;
    if(phoneMatch)  
      queryStr.append("(patient_phone='"+phoneNo+"' OR patient_phone='"+phnoWtZero+"' OR patient_phone='"+nationalNumber+"' OR patient_phone='"+phnoWtSplChar+"') AND "); 

    queryStr.append("(original_mr_no is null OR original_mr_no ='') AND");
    if(strTokens.length == 1){
      queryStr.append(" ((soundex(pd.patient_name) = soundex('"+strTokens[0]+"') and levenshtein(lower(pd.patient_name), lower('"+strTokens[0]+"')) <="+acceptableDiff+") "
          + "OR (soundex(pd.last_name) = soundex('"+strTokens[0]+"') and levenshtein(lower(pd.last_name), lower('"+strTokens[0]+"')) <="+acceptableDiff+"))");

    }else{
      for(int i=0; i<strTokens.length; i++){
        if(first){
          queryStr.append(" ((soundex(pd.patient_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.patient_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+") "
                + "OR (soundex(pd.middle_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.middle_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+") "
                + "OR (soundex(pd.last_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.last_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+"))");
            first = false;
          }else{
          queryStr.append(" AND ((soundex(pd.patient_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.patient_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+") "
                + "OR (soundex(pd.middle_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.middle_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+") "
                + "OR (soundex(pd.last_name) = soundex('"+strTokens[i]+"') and levenshtein(lower(pd.last_name), lower('"+strTokens[i]+"')) <="+acceptableDiff+"))");        
        }   
      }
    }
    queryStr.append(" AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no)) ");

    return DatabaseHelper.queryToDynaList(queryStr.toString());
  }
	
//	public List<String> searchUsedTpaMemberIds(String memberId, String sponsorId, String excludeMrNo) {
//		
//		String querySql = "";
//		querySql = "SELECT distinct(ppd.mr_no) as mrno FROM "
//				+ "patient_policy_details ppd "
//				+ " JOIN patient_insurance_plans pip ON pip.plan_id = ppd.plan_id AND ppd.visit_id = pip.patient_id "
//				+ " JOIN patient_details pd ON(pd.mr_no = ppd.mr_no) "
//				+ "WHERE ppd.member_id = ? AND pip.sponsor_id = ? ";
//				//+ "AND pd.original_mr_no = ''  ";
//		List<Object> mrArgs = new ArrayList<Object>();
//    	mrArgs.add(memberId);
//    	mrArgs.add(sponsorId);
//		if (excludeMrNo != null){
//			querySql += " AND ppd.mr_no != ?";
//	    	mrArgs.add(excludeMrNo);
//		}
//        
//    	List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, mrArgs.toArray());
//    	
//        List<String> resultList = new ArrayList<String>();
//        for (BasicDynaBean result : results) {
//        	resultList.add((String) result.get("mrno"));
//        }
//        
//        return resultList;
//	}
	
	private static final String GET_DUPLICATE_MR_NOS_CASE_PARENT = "select ppd.member_id,adm.mr_no as child_mr_no,pr.mr_no as parent_mr_no, pr.patient_id as parent_visit_id,ppd.mr_no as ppdmrno " +
			"from patient_registration pr " +
			"join admission adm on (adm.parent_id = pr.patient_id) " +
			"join patient_policy_details ppd on (ppd.mr_no = adm.mr_no) " +
			"where pr.mr_no= ? AND ppd.member_id = ?";
			
	private static final String GET_DUPLICATE_MR_NOS_CASE_CHILD = "select ppd.member_id,adm.mr_no as child_mr_no,pr.mr_no as parent_mr_no, pr.patient_id as parent_visit_id,ppd.mr_no as ppdmrno " +
			"from patient_policy_details ppd " +
			"join patient_registration pr on(pr.mr_no = ppd.mr_no AND pr.patient_id = ppd.visit_id) " +
			"join patient_details pd on (pd.mr_no=ppd.mr_no) " +
			"join admission adm on (adm.parent_id = pr.patient_id) " +
			"where adm.mr_no= ? AND ppd.member_id = ? AND pd.patient_group IN " +
			"(SELECT confidentiality_grp_id from user_confidentiality_association where" +
			" emp_username = current_setting('application.username') UNION SELECT 0);";
	
	public List<Map<String, Object>> searchUsedTpaMemberIdsMap(String memberId, String sponsorId, String excludeMrNo){
		List<Map<String, Object>> resultList = new ArrayList<>();
    	if (excludeMrNo != null){
    		String isBabyMrNo = isBabyMrNo(excludeMrNo);
    		
	    	List<BasicDynaBean> member_id_of_parents = DatabaseHelper.queryToDynaList(
	    			!isBabyMrNo.equals("Y") ? GET_DUPLICATE_MR_NOS_CASE_PARENT : GET_DUPLICATE_MR_NOS_CASE_CHILD ,
	    			new Object[]{excludeMrNo, memberId});
    		// case when current mr_no's member_id is same of mr_no's mothers's member_id
    		for (BasicDynaBean result : member_id_of_parents) {
    			Map<String, Object> resultMap =  new HashMap<>();
    			String child_mr_no = result.get("child_mr_no") != null ? (String) result.get("child_mr_no") : "";
    			String parent_mr_no = result.get("parent_mr_no") != null ? (String) result.get("parent_mr_no") : "";
	        	resultMap.put("mrno",  child_mr_no.equals(excludeMrNo) ? parent_mr_no : child_mr_no);
	        	resultMap.put("is_parent_mr_no", child_mr_no.equals(excludeMrNo));
	        	resultList.add(resultMap);	
    		}
    		if(resultList.size() > 0)
	        	return resultList;
	    }
    	
    	    	
    	String querySql = "SELECT distinct(ppd.mr_no) as mrno, pd.original_mr_no FROM "
				+ "patient_policy_details ppd "
				+ " JOIN patient_insurance_plans pip ON pip.plan_id = ppd.plan_id AND ppd.visit_id = pip.patient_id "
				+ " JOIN patient_details pd ON(pd.mr_no = ppd.mr_no) "
				+ "WHERE ppd.member_id = ? AND pip.sponsor_id = ? AND "
				+ " ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )";
				//+ "AND pd.original_mr_no = ''  ";
		List<Object> mrArgs = new ArrayList<Object>();
    	mrArgs.add(memberId);
    	mrArgs.add(sponsorId);
    	    	
		if (excludeMrNo != null){
			querySql += " AND ppd.mr_no != ?";
	    	mrArgs.add(excludeMrNo);
	    if(!excludeMrNo.equals("0"))	{
				querySql += " AND pd.original_mr_no != ?";
				mrArgs.add(excludeMrNo);
			}
		}

		String query = "SELECT original_mr_no from patient_details where mr_no = '" + excludeMrNo +"'";
        
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(querySql, mrArgs.toArray());

    BasicDynaBean basicDynaBean = DatabaseHelper.queryToDynaBean(query);
            
    for (BasicDynaBean result : results) {
      Map<String, Object> resultMap =  new HashMap<>();
			if (basicDynaBean == null) {
        resultMap.put("mrno", (String) result.get("mrno"));
				resultMap.put("is_parent_child", false);
				resultList.add(resultMap);
			} else if((basicDynaBean.get("original_mr_no") != null
						&& !basicDynaBean.get("original_mr_no").equals(result.get("mrno"))
						&& !"".equals(result.get("mrno")))
					|| (basicDynaBean.get("original_mr_no") !=null 
						&& !basicDynaBean.get("original_mr_no").equals(result.get("original_mr_no"))
						&& !"".equals(result.get("original_mr_no")))){
				resultMap.put("mrno", (String) result.get("mrno"));
				resultMap.put("is_parent_child", false);
				resultList.add(resultMap);
			}
    }
    return resultList;
	}

	private static final String GET_MR_NO_BABY_DET = "SELECT isbaby "
			+ " FROM admission WHERE mr_no=? and isBaby='Y' limit 1";
	private String isBabyMrNo(String excludeMrNo) {
		String isBabyMrNo = DatabaseHelper.getString(GET_MR_NO_BABY_DET, excludeMrNo);
		return isBabyMrNo;
	}

  private String fillWithQuestionMarksForInQuery(List<?> values) {
    String[] valuesArr = new String[values.size()];
    Arrays.fill(valuesArr, "?");
    return StringUtils.arrayToCommaDelimitedString(valuesArr);
  }
  
  private static final String FILTER_BY_MR_NO = "SELECT pd.mr_no, pd.patient_name,"
      + " pd.middle_name, pd.last_name, cgm.abbreviation as patient_group "
      + " from patient_details pd "
      + " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group)"
      + " where " ;

  public List<BasicDynaBean> filterOnMrNo(String filterText) {
    StringBuilder query = new StringBuilder(FILTER_BY_MR_NO);
    query.append(
        "((reverse(pd.mr_no)) LIKE ? " + " OR reverse(COALESCE(pd.original_mr_no,'')) LIKE ? "
            + " OR reverse(COALESCE(pd.oldmrno,'')) LIKE ?)");
    String reverseFilterText = new StringBuilder(filterText).reverse().toString();
    String upperFilterText = reverseFilterText.toUpperCase();
    List<Object> queryArgs = new ArrayList<Object>();
    queryArgs
        .addAll(Arrays.asList(upperFilterText + "%", upperFilterText + "%", upperFilterText + "%"));
    query.append(" AND pd.patient_group not in (SELECT ufa.confidentiality_grp_id from user_confidentiality_association ufa "
                + " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id) "
                + " where emp_username = current_setting('application.username') "
                + " AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT confidentiality_grp_id from"
                + " confidentiality_grp_master where break_the_glass != 'Y' UNION SELECT 0) AND "
        + " pd.mr_no not in (SELECT mr_no from user_mrno_association where emp_username = current_setting('application.username')) ");
    query.append(" LIMIT ? ");
    queryArgs.add(20);
    return DatabaseHelper.queryToDynaList(query.toString(), queryArgs.toArray());
  }
  
}
