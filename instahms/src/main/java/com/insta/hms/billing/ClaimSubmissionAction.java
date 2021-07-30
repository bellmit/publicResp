/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.clinical.diagnosisdetails.HospitalClaimDiagnosisRepository;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.emr.GenericDocumentsProviderBOImpl;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi.p
 *
 */
public class ClaimSubmissionAction extends BaseAction {

	private static final GenericDAO tpaMasterDao = new GenericDAO("tpa_master");
	private static final Logger logger = LoggerFactory.getLogger(ClaimSubmissionAction.class);
	private static final ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
	private static final ClaimDAO claimDao = new ClaimDAO();
	private static final AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();
	private static final HospitalClaimDiagnosisRepository hospitalClaimDiagRepo = ApplicationContextProvider.getBean(HospitalClaimDiagnosisRepository.class);
	private static final InsuranceCompanyTPAMasterDAO insuranceCoTpaMasterDao = new InsuranceCompanyTPAMasterDAO();
	private static final JSONSerializer js = new JSONSerializer().exclude("class");
	private static final InsuCompMasterDAO insuranceCompanyMasterDao = new InsuCompMasterDAO();
	private static final GenericDAO insurancePlanMainDao = new GenericDAO("insurance_plan_main");
	private static final GenericDAO accountGroupAndCenterViewDao =
			new GenericDAO("accountgrp_and_center_view");
	private static final AccountingGroupMasterDAO accountingGroupMasterDao =
			new AccountingGroupMasterDAO();
	private static final CenterMasterDAO centerMasterDao = new CenterMasterDAO();
	private static final GenericDAO claimSubmissionsDao = new GenericDAO("claim_submissions");

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		Map map= getParameterMap(req);
		if (req.getParameter("submission_batch_id")!=null && !req.getParameter("submission_batch_id").equals("")) {
			String[] submissionBatchId = {ClaimSubmissionDAO.fillSubmissionIDSearch(req.getParameter("submission_batch_id"))};
			map.put("submission_batch_id", submissionBatchId);
		}

		map.remove("center_or_account_group_id");
		map.remove("center_or_account_group");

		String centerOrAccountGroup   = req.getParameter("center_or_account_group_id");
		String accGrpOrCenterIdStr = (centerOrAccountGroup == null || centerOrAccountGroup.equals("")) ? "" : (centerOrAccountGroup);
		int accGrpId = 0;
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		//comment1: if center_or_account_group filter is selectd then it will filters
		if (!accGrpOrCenterIdStr.equals("")) {
			if (accGrpOrCenterIdStr.startsWith("A")) {
				accGrpId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
			}else if (accGrpOrCenterIdStr.startsWith("C")) {
				centerId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
			}

			if (accGrpId != 0) {
				map.put("account_group", new String[]{accGrpId+""});
				map.put("account_group@type", new String[]{"integer"});
			}else {
				map.put("center_id", new String[]{centerId+""});
				map.put("center_id@type", new String[]{"integer"});
			}
		}

		int userCenterId = (Integer) req.getSession(false).getAttribute("centerId");
		List<String> accGrpFilter = new ArrayList<>();
		List<String> centerFilter = new ArrayList<>();

		if(userCenterId != 0){
			centerFilter.add(String.valueOf(userCenterId));
		}

		PagedList list = ClaimSubmissionDAO.searchClaimSubmissions(map, ConversionUtils.getListingParameter(map),accGrpFilter,centerFilter);
		req.setAttribute("pagedList", list);
		List<String> columns = new ArrayList<>();
		columns.add("insurance_co_id");
		columns.add("insurance_co_name");

		req.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
        InsuranceCategoryMasterDAO.getInsCatCenter(centerId))));
		
		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				tpaMasterDao.listAll(columns, "status", "A", "tpa_name"))));
		
		columns.clear();
		columns.add("plan_id");
		columns.add("plan_name");
		columns.add("category_id");


		req.setAttribute("acc_prefs", acPrefsDAO.getRecord());
		req.setAttribute("center_or_account_group", centerOrAccountGroup);
		req.setAttribute("inscatname", InsuranceCategoryMasterDAO.getInsCatCenter(centerId));

		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
  public ActionForward getInsuranceCompanyTpaList(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    
    String insuranceCoId = req.getParameter("insurance_co_id");
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if(insuranceCoId != null && !insuranceCoId.equals("")){
      List<BasicDynaBean> insCompTpaList = insuranceCoTpaMasterDao.getCompanyTpaList(insuranceCoId);
      res.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(insCompTpaList)));
      res.flushBuffer();
    }
    
    return null;
  }

    @IgnoreConfidentialFilters
	public ActionForward add(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException {
    
	  List<String> columns = new ArrayList<>();
	  columns.add("insurance_co_id");
	  columns.add("insurance_co_name");
		req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
				insuranceCompanyMasterDao.listAll(columns, "status", "A", "insurance_co_name"))));

		req.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
					InsuranceCategoryMasterDAO.getInsCatCenter(RequestContext.getCenterId()))));
		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				tpaMasterDao.listAll(columns, "status", "A", "tpa_name"))));

		columns.clear();
		columns.add("plan_id");
		columns.add("category_id");
		columns.add("plan_name");
		req.setAttribute("planList",js.serialize(ConversionUtils.listBeanToListMap(
				insurancePlanMainDao.listAll(columns, "status", "A", "plan_name"))));

		req.setAttribute("inscatname", InsuranceCategoryMasterDAO.getInsCatCenter(RequestContext.getCenterId()));
		req.setAttribute("acc_prefs", acPrefsDAO.getRecord());
		List<BasicDynaBean> accGrpAndCenterList = accountGroupAndCenterViewDao.listAll();

		Map accGrpAndCenterType = null;
		// this can be optimized too
		List<BasicDynaBean> accGrpAndCenterDropdn = new ArrayList<BasicDynaBean>();
		int user_centerId = (Integer) req.getSession(false).getAttribute("centerId");
		if(user_centerId != 0){
			for(int i=0;i<accGrpAndCenterList.size();i++){
				accGrpAndCenterType = new HashMap(((BasicDynaBean)accGrpAndCenterList.get(i)).getMap());
				String type=(String)accGrpAndCenterType.get("type");
				if(type.equals("C")){
					int ac_id = Integer.parseInt(accGrpAndCenterType.get("ac_id")+"");
						if(ac_id == user_centerId){
							accGrpAndCenterDropdn.add( (BasicDynaBean)accGrpAndCenterList.get(i) ) ;
						}
				}
				else{
					if(null != accGrpAndCenterType.get("store_center_id")) {
						int storeCenterId = (Integer)accGrpAndCenterType.get("store_center_id");
						if(user_centerId == storeCenterId)
							accGrpAndCenterDropdn.add( (BasicDynaBean)accGrpAndCenterList.get(i) ) ;
					} else {
						accGrpAndCenterDropdn.add((BasicDynaBean)accGrpAndCenterList.get(i) );
					}

				}

			}
		}

		if(user_centerId == 0) accGrpAndCenterDropdn = accGrpAndCenterList;

		req.setAttribute("accountGrpAndCenterList", accGrpAndCenterDropdn);

		return mapping.findForward("add");
	}

	@IgnoreConfidentialFilters
	public ActionForward createSubmission(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, SQLException, ParseException, Exception {

		HttpSession session = req.getSession();
		Connection con = null;
		String submissionBatchID = null;
		String patient_type = null;
		String msg = "Claim submission batch creation unsuccessful...";
		boolean success = false;
		String insurance_co_id = req.getParameter("insurance_co_id");
		String tpa_id          = req.getParameter("tpa_id");
		String plan_id         = req.getParameter("plan_id");
		String[] category_id   = req.getParameterValues("category_id");
		String[] visit_type    = req.getParameterValues("visit_type");
		String[] bill_status   = req.getParameterValues("bill_status");
		String is_resubmission = req.getParameter("is_resubmission");
		String center_or_account_group   = req.getParameter("center_or_account_group");
		String[] codification_status   = req.getParameterValues("codification_status");

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect("raiseClaimSubmission.do");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("_method", "add");
		redirect.addParameter("sorOrder", "created_date");
		redirect.addParameter("sortReverse", "true");
		redirect.addParameter("status", "O");

		redirect.addParameter("insurance_co_id", insurance_co_id);
		redirect.addParameter("tpa_id", tpa_id);
		redirect.addParameter("plan_id", plan_id);
		redirect.addParameter("center_or_account_group", center_or_account_group);

		redirect.addParameter("is_resubmission", is_resubmission != null && !is_resubmission.equals("") ? "Y" :"");
		String[] last_bill_finalized_time =  req.getParameterValues("_last_bill_finalized_time");
		String[] last_bill_finalized_date =  req.getParameterValues("_last_bill_finalized_date");
		String[] last_bill_finalized_date_arr = new String[2];

		if (last_bill_finalized_date != null){
			for (int i=0; i<last_bill_finalized_date.length; i++){
				
				boolean last_bill_finalized_date0_empty = false;
				if(!last_bill_finalized_date[0].isEmpty() && last_bill_finalized_time[0].isEmpty()) {
					last_bill_finalized_time[0] = "00:00";
					last_bill_finalized_date0_empty = true;
				}
				boolean last_bill_finalized_date1_empty = false;
				if(!last_bill_finalized_date[1].isEmpty() && last_bill_finalized_time[1].isEmpty()) {
					last_bill_finalized_time[1] = "23:59";
					last_bill_finalized_date1_empty = true;
				}
					
				last_bill_finalized_date_arr[i] = last_bill_finalized_date[i]+" "+last_bill_finalized_time[i];
				last_bill_finalized_date_arr[i] = last_bill_finalized_date_arr[i].trim();
				if(last_bill_finalized_date0_empty)
					last_bill_finalized_time[0] = "";
				if(last_bill_finalized_date1_empty)
					last_bill_finalized_time[1] = "";
				redirect.addParameter("last_bill_finalized_date", last_bill_finalized_date[i]);
				redirect.addParameter("last_bill_finalized_time", last_bill_finalized_time[i]);

			}
		}
		
		String[] first_bill_open_date =  req.getParameterValues("_first_bill_open_date");
		String[] first_bill_open_time =  req.getParameterValues("_first_bill_open_time");
		String[] first_bill_open_date_arr = new String[2];

		if (first_bill_open_date != null){
			for (int i=0; i<first_bill_open_date.length; i++){
				
				boolean first_bill_open_date0_empty = false;
				if(!first_bill_open_date[0].isEmpty() && first_bill_open_time[0].isEmpty()) {
					first_bill_open_time[0] = "00:00";
					first_bill_open_date0_empty = true;
				}
				boolean first_bill_open_date1_empty = false;
				if(!first_bill_open_date[1].isEmpty() && first_bill_open_time[1].isEmpty()) {
					first_bill_open_time[1] = "23:59";
					first_bill_open_date1_empty = true;
				}
				
				first_bill_open_date_arr[i] = first_bill_open_date[i]+" "+first_bill_open_time[i];
				first_bill_open_date_arr[i] = first_bill_open_date_arr[i].trim();
				if(first_bill_open_date0_empty)
					first_bill_open_time[0] = "";
				if(first_bill_open_date1_empty)
					first_bill_open_time[1] = "";
				redirect.addParameter("first_bill_open_date", first_bill_open_date[i]);
				redirect.addParameter("first_bill_open_time", first_bill_open_time[i]);
			}
		}
		
		String[] reg_date =  req.getParameterValues("_reg_date");
		String[] reg_time =  req.getParameterValues("_reg_time");
		String[] reg_date_arr = new String[2];
		if (reg_date != null){
			for (int i=0; i<reg_date.length; i++){
				boolean reg_time0_empty = false;
				if(!reg_date[0].isEmpty() && reg_time[0].isEmpty()) {
					reg_time[0] = "00:00";
					reg_time0_empty = true;
				}
				boolean reg_time1_empty = false;
				if(!reg_date[1].isEmpty() && reg_time[1].isEmpty()) {
					reg_time[1] = "23:59";
					reg_time1_empty = true;
				}
				reg_date_arr[i] = reg_date[i]+" "+reg_time[i];
				reg_date_arr[i] = reg_date_arr[i].trim();
				if(reg_time0_empty)
					reg_time[0] = "";
				if(reg_time1_empty)
					reg_time[1] = "";
				redirect.addParameter("reg_date", reg_date[i]);
				redirect.addParameter("reg_time", reg_time[i]);
			}
		}
		
		String[] discharge_time =  req.getParameterValues("_discharge_time");
		String[] discharge_date = req.getParameterValues("_discharge_date");
		String[] discharge_date_arr = new String[2];
		if (discharge_date != null){
			for (int i=0; i<discharge_date.length; i++){
				boolean discharge_time0_empty = false;
				if(!discharge_date[0].isEmpty() && discharge_time[0].isEmpty()) {
					discharge_time[0] = "00:00";
					discharge_time0_empty = true;
				}
				boolean discharge_time1_empty = false;
				if(!discharge_date[1].isEmpty() && discharge_time[1].isEmpty()) {
					discharge_time[1] = "23:59";
					discharge_time1_empty = true;
				}
				discharge_date_arr[i] = discharge_date[i]+" "+discharge_time[i];
				discharge_date_arr[i] = discharge_date_arr[i].trim();
				if(discharge_time0_empty)
					discharge_time[0] = "";
				if(discharge_time1_empty)
					discharge_time[1] = "";
				redirect.addParameter("discharge_date", discharge_date[i]);
				redirect.addParameter("discharge_time", discharge_time[i]);
			}
		}

		if (visit_type != null){
			for(int i=0;i<visit_type.length;i++){
				redirect.addParameter("visit_type", visit_type[i]);
			}
		}

		if (bill_status != null){
			for(int i=0;i<bill_status.length;i++){
				redirect.addParameter("bill_status", bill_status[i]);
			}
		}
		if (codification_status != null){
			for(int i=0;i<codification_status.length;i++){
				redirect.addParameter("codification_status", codification_status[i]);
			}
		}

		StringBuilder categoryIds = new StringBuilder();
		int j =0;
		if (category_id != null) {
			for(int i=0;i<category_id.length;i++){
				redirect.addParameter("category_id", category_id[i]);
				if (category_id[i] != null && !category_id[i].equals("")) {
					if (j > 0)
						categoryIds.append(",");
					categoryIds.append(category_id[i]);
					j++;
				}
			}
		}

		try {
	submit:
			{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String userId = session.getAttribute("userid").toString();				
				is_resubmission = (is_resubmission != null && !is_resubmission.equals("")) ? "Y" : null;
				String facility_id = null;
				String healthAuthority = null;
				String tpa_code = tpa_id ;
				Integer planId = (plan_id == null || plan_id.equals("")) ? 0 : Integer.parseInt(plan_id.toString());

				String accGrpOrCenterIdStr = (center_or_account_group == null || center_or_account_group.equals("")) ? "" : (center_or_account_group.toString());
				int accGrpId = 0;
				int centerId = 0;
				if (!accGrpOrCenterIdStr.equals("")) {
					if (accGrpOrCenterIdStr.startsWith("A")) {
						accGrpId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
						BasicDynaBean accbean = (BasicDynaBean) accountingGroupMasterDao
								.findByKey("account_group_id", accGrpId);
						// setting centerId with current center id - Bug no 42930
						centerId = (Integer) session.getAttribute("centerId");
						facility_id = (String)accbean.get("account_group_service_reg_no");
						BasicDynaBean centerbean = (BasicDynaBean) centerMasterDao
								.findByKey("center_id", centerId);
						healthAuthority = (String)centerbean.get("health_authority");
						
					}else if (accGrpOrCenterIdStr.startsWith("C")) {
						centerId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
						BasicDynaBean centerbean = (BasicDynaBean)centerMasterDao.findByKey("center_id", centerId);
						facility_id = (String)centerbean.get("hospital_center_service_reg_no");
						healthAuthority = (String)centerbean.get("health_authority");
					}
				}
				if(null !=healthAuthority && !healthAuthority.equals("")){
					String tpaCodeQuery = "SELECT tpa_code FROM ha_tpa_code WHERE tpa_id=? AND health_authority =?";
					String tpaCode = DataBaseUtil.getStringValueFromDb(tpaCodeQuery, new Object[]{tpa_id, healthAuthority});
					if(null != tpaCode && !tpaCode.equals(""))
						tpa_code = tpaCode ;
				}

				String lastBillFinalizedDateTo = req.getParameterValues("_last_bill_finalized_date")[1];
				String regDateTo = req.getParameterValues("_reg_date")[1];
				String firstBillOpenDateTo = req.getParameterValues("_first_bill_open_date")[1];
				String dischargeDateTo = req.getParameterValues("_discharge_date")[1];
				String regTimeTo = req.getParameterValues("_reg_time")[1];

				String date = lastBillFinalizedDateTo.equals("")
								? (regDateTo.equals("") ? firstBillOpenDateTo : regDateTo)
								: lastBillFinalizedDateTo;
				if(date.equals("") && null != dischargeDateTo && !dischargeDateTo.equals(""))
					date = dischargeDateTo;

				Boolean ignoreExternalPbm = getParameterMap(req).get("is_external_pbm") != null ? true : false;
				Map filter = getParameterMap(req);
				filter.remove("is_resubmission");
				filter.remove("center_or_account_group");
				filter.remove("is_external_pbm");

				if (accGrpId != 0) {
					filter.put("account_group", new String[]{accGrpId+""});
					filter.put("account_group@type", new String[]{"integer"});

					filter.put("center_id", new String[]{centerId+""});
					filter.put("center_id@type", new String[]{"integer"});
				}else {
					filter.put("center_id", new String[]{centerId+""});
					filter.put("center_id@type", new String[]{"integer"});

					filter.put("account_group", new String[]{1+""});
					filter.put("account_group@type", new String[]{"integer"});
				}

				if (visit_type != null) {
					if (visit_type.length == 1) {
						if (visit_type[0].equals("")) {
							filter.remove("visit_type");
							filter.put("visit_type", new String[]{"i","o"});
							patient_type = "*";
						}else {
							patient_type = visit_type[0];
						}
					}else {
						filter.remove("visit_type");
						filter.put("visit_type", new String[]{"i","o"});
						patient_type = "*";
					}
				}

				if (bill_status != null) {
					if (bill_status.length == 1) {
						if (bill_status[0].equals("")) {
							filter.remove("bill_status");
							filter.put("bill_status", new String[]{"A","F"});
						}
					}
				}
				if (codification_status != null && codification_status.length == 1 
						&& codification_status[0].equals("")) {
							filter.remove("codification_status");
							filter.put("codification_status", new String[]{"P","C","R","V"});
				}
				if (last_bill_finalized_date_arr != null) {
							filter.remove("last_bill_finalized_date");
							filter.remove("last_bill_finalized_time");
							filter.put("last_bill_finalized_date", last_bill_finalized_date_arr);
				}
				if (first_bill_open_date_arr != null) {
					filter.remove("first_bill_open_date");
					filter.remove("first_bill_open_time");
					filter.put("first_bill_open_date", first_bill_open_date_arr);
				}
				if (reg_date_arr != null) {
					filter.remove("reg_date");
					filter.remove("reg_time");
					filter.put("reg_date_time", reg_date_arr);
				}
				if (discharge_date_arr != null) {
					filter.remove("discharge_date");
					filter.remove("discharge_time");
					filter.put("discharge_date_time", discharge_date_arr);
				}
				
				// Format Example : 54665756_TPAID0104_2017-12-19_IS000027									
				String file_name = facility_id +"_"+tpa_code;

				if (is_resubmission != null && is_resubmission.equals("Y")) 
					file_name = "Resubmission_" + file_name;

				PagedList claimsList = new ClaimDAO().searchClaimsForSubmission(filter, is_resubmission);

				Set set = new HashSet();
				List claims = new ArrayList();
				List dtoList = claimsList.getDtoList();
				int claimsTotal = 0;

				if (dtoList != null && dtoList.size() > 0) {

					for (int i=0; i < dtoList.size(); i++) {
						String claim_id = (String)((DynaBeanMapDecorator)dtoList.get(i)).get("claim_id");
						//if the claim has activities then include the claim id in submission batch
						boolean claimHasActivities = claimDao
								.hasClaimAcitivity(claim_id, ignoreExternalPbm);
						if (claimHasActivities) {
							set.add(claim_id);
						}
					}
					claims.addAll(set);

					if (claims.size() > 0 ) {
						claimsTotal = claims.size();

					    submissionBatchID = submitdao.getGeneratedSubmissionBatchId();

					    if (is_resubmission != null && is_resubmission.equals("Y")) {
					    	
					    	success = claimDao
									.updateClaimResubmission(con, claims, submissionBatchID);
					    	//reassign new resubmission batchId to claim
					    	claimDao.updateClaimSubmissionIdWithCorrection(con, claims, submissionBatchID);

					    } else {
					    	success = claimDao.updateClaimSubmission(con, claims, submissionBatchID);
					    }
					    
						if (!success)
							break submit;

            BasicDynaBean submissionbean = submitdao.getBean();

						submissionbean.set("submission_batch_id", submissionBatchID);
						submissionbean.set("username", userId);
						submissionbean.set("created_date", DataBaseUtil.getDateandTime());
						submissionbean.set("insurance_co_id", insurance_co_id);
						submissionbean.set("tpa_id", tpa_id);
						submissionbean.set("file_name", file_name + "_" + DataBaseUtil.getCurrentDate()+"_"+submissionBatchID);
						submissionbean.set("insurance_category_id", !categoryIds.toString().equals("") ? categoryIds.toString() : "0");
						submissionbean.set("plan_id", planId);
						submissionbean.set("account_group", accGrpId);
						submissionbean.set("patient_type", patient_type);
						submissionbean.set("is_resubmission", is_resubmission != null && is_resubmission.equals("Y") ? "Y" : "N");
						submissionbean.set("status", "O");
						submissionbean.set("center_id", centerId);
						submissionbean.set("is_external_pbm_batch", ignoreExternalPbm);

						success = submitdao.insert(con, submissionbean);
						
						//Insert into claim_submissions table and set status of claim in insurance_claim to B (Batched)
						for (int i=0; i<claims.size(); i++) {
							BasicDynaBean claimSubmissionBean = claimSubmissionsDao.getBean();
							claimSubmissionBean.set("submission_batch_id", submissionBatchID);
							claimSubmissionBean.set("claim_id", claims.get(i));
							claimSubmissionBean.set("status", "O");
							claimSubmissionsDao.insert(con, claimSubmissionBean);
							Map columndata = new HashMap();
							columndata.put("status", "B");
							Map keys = new HashMap();
							keys.put("claim_id", claims.get(i));
							keys.put("last_submission_batch_id", submissionBatchID);
							claimDao.update(con, columndata, keys);
						}
				
						if (!success)
							break submit;

						if (success && submissionBatchID != null) {
							msg = "Created Submission Batch Id: " + submissionBatchID + " which include " + claimsTotal + " claims.";
							flash.put("info", msg);
						} else {
							flash.put("error", msg);
						}
					}
				} else {
					flash.put("info", "No claims found for the given submission criteria.");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		} finally {
			DataBaseUtil.commitClose(con, success);
			
	  /* At the time of batch creation. We copy codes into hospital_claim_diagnosis
       * to mark that claim xml diagnosis codes are to contain coder modified codes.
       * Coder modified values  will be present in hospital_claim_diagnosis table.
       */
      insertRecordsIntoHospDiagnosis(submissionBatchID);
		}
		return redirect;
	}

   /**
   * Insert records into hospital_claim_diagnosis that
   * haven't been copied from mrd_diagnosis before.
   * This method ensures that when claim is generated 
   * the codes that it is generated with is saved from
   * doctor modification at a later stage.
	 *
	 * @param submissionBatchId the submission batch id
	 * @throws SQLException the SQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void insertRecordsIntoHospDiagnosis(String submissionBatchId) {
		try {
			// get the visit ids that have not been copied to hospital_claim_diagnosis from
			// mrd_diagnosis
			List<BasicDynaBean> visitIdsBeanList = hospitalClaimDiagRepo.getClaimVisitsNotCopied(submissionBatchId);
			// copy records matching visitIdsList from mrd_diagnosis to
			// hospital_claim_diagnosis
			List<String> visitIdsList = new ArrayList<>();
			for (BasicDynaBean bean : visitIdsBeanList) {
				visitIdsList.add((String) bean.get("visit_id"));
			}
			List<BasicDynaBean> diagnosisList = MRDDiagnosisDAO.getListWhereVisit(visitIdsList);
			if (diagnosisList != null && !diagnosisList.isEmpty()) {
				hospitalClaimDiagRepo.batchInsert(diagnosisList);
			}
			return;
		} catch (SQLException exception) {
			logger.error("Unable to copy records from mrd_diagnosis to hospital_claim_diagnosis" + exception);
		}
	}
	
  @IgnoreConfidentialFilters
  public ActionForward delete(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		Connection con = null;
		boolean success = false;
		String submissionBatchID = req.getParameter("submission_batch_id");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = true;

			if (submissionBatchID != null && !submissionBatchID.equals("")) {
				BasicDynaBean sbean = submitdao.findByKey("submission_batch_id", submissionBatchID);
				if (sbean != null) {
					//delete from insurance_submission_batch
					success = submitdao.delete(con, "submission_batch_id", submissionBatchID);
					if(sbean.get("is_resubmission") != null && sbean.get("is_resubmission").equals("Y")){
						//update status to M, reset submission batch to latest submission batch id
						claimDao.deleteReSubmissionFromInsurance(con, submissionBatchID);
					} else {
						//update status to O, clear submission batch
						claimDao.deleteSubmissionFromInsurance(con, submissionBatchID);
					}
					//delete from claim_submissions
					claimDao.deleteSubmission(con, submissionBatchID);
				}					
			}
		}catch (Exception e) {
			flash.error("Submission deletion unsuccessful...");
			throw e;
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		redirect.addParameter("sortOrder", "created_date");
		redirect.addParameter("sortReverse", "true");
		redirect.addParameter("status", "O");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward claimSent(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect("claimSubmissionsList.do");

		Connection con = null;
		boolean success = false;
		String submissionBatchID = req.getParameter("submission_batch_id");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			String status = "S"; // Claim sent

			if (submissionBatchID != null && !submissionBatchID.equals("")) {
				
				//update status in insurance_submission_batch
				Map key = new HashMap();
				Map fields = new HashMap();
				key.put("submission_batch_id", submissionBatchID);
				fields.put("status", status);
				fields.put("submission_date", new Timestamp(new java.util.Date().getTime()));
				submitdao.update(con, fields, key);
				//update claim_submissions status as Sent
				claimDao.updateClaimBatchStatus(con, submissionBatchID, status);
				success = true;			
			}
		}catch (Exception e) {
			flash.error("Submission sent status updation unsuccessful.");
			throw e;
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("submission_batch_id", submissionBatchID);
		redirect.addParameter("_method", "getAddorEditBatchRefScreen");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getAddorEditBatchRefScreen(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {
		String submissionBatchID = req.getParameter("submission_batch_id");
		if (submissionBatchID == null || submissionBatchID.trim().equals("")) {
			req.setAttribute("error", "Invalid/No submission batch with id: " + submissionBatchID);
			return mapping.findForward("getAddorEditBatchRefScreen");
		}
		BasicDynaBean batch = submitdao.findSubmissionBatch(submissionBatchID);
		if (batch == null)
			req.setAttribute("error", "Invalid/No submission batch with id: " + submissionBatchID);

		req.setAttribute("batch", batch);
		return mapping.findForward("getAddorEditBatchRefScreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward addoreditBatchRef(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect("claimSubmissionsList.do");

		Connection con = null;
		String submissionBatchID = req.getParameter("submission_batch_id");
		String reference_number = req.getParameter("reference_number");
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			BasicDynaBean sbean = submitdao.findByKey("submission_batch_id", submissionBatchID);
			sbean.set("reference_number", reference_number);
			submitdao.updateWithName(con, sbean.getMap(), "submission_batch_id");
		}catch (Exception e) {
			flash.error("Batch Reference no. updation unsuccessful...");
			throw e;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("submission_batch_id", submissionBatchID);
		redirect.addParameter("_method", "getAddorEditBatchRefScreen");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward reject(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		Connection con = null;
		boolean success = false;
		String submissionBatchID = req.getParameter("submission_batch_id");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			
			if (submissionBatchID != null && !submissionBatchID.equals("")) {
				BasicDynaBean sbean = submitdao.findByKey("submission_batch_id", submissionBatchID);
				if (sbean != null) {
					if(sbean.get("is_resubmission") != null && sbean.get("is_resubmission").equals("Y")){
						//update status to M, reset submission batch to latest submission batch id
						claimDao.deleteReSubmissionFromInsurance(con, submissionBatchID);
					} else {
						//update status to O, clear submission batch
						claimDao.deleteSubmissionFromInsurance(con, submissionBatchID);
					}
					//delete from claim_submissions
					claimDao.deleteSubmission(con, submissionBatchID);
					// Update rejected submission with "X", rejected status but not delete the batch id.
	        sbean.set("status", "X");
	        success = submitdao.updateWithName(con, sbean.getMap(), "submission_batch_id") == 1;
				}
      }
		}catch (Exception e) {
			flash.error("Submission batch rejection unsuccessful...");
			throw e;
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		redirect.addParameter("submission_batch_id", submissionBatchID);
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward downloadDocuments(ActionMapping mapping, ActionForm f,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String zipFileName="Submission_"+req.getParameter("submission_batch_id")+"_Documents"+".zip";
    res.setHeader("Content-Disposition", "attachment; filename="+zipFileName);
    res.setContentType("application/zip");

    OutputStream os=res.getOutputStream();
    ZipOutputStream zout = new ZipOutputStream(os);

    HtmlConverter hc = new HtmlConverter();
    Connection con = null;
    String submissionBatchID = req.getParameter("submission_batch_id");
    String userId = (String) req.getSession().getAttribute("userid");

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getPrefsBean();

    List<String> claimCols = new ArrayList<String>();
    claimCols.add("patient_id");

    try {
      con = DataBaseUtil.getConnection();
      List<BasicDynaBean> claimsOfSubmission = claimDao
			  .listAll(claimCols, "last_submission_batch_id", req.getParameter("submission_batch_id"));

      for(BasicDynaBean claimBean : claimsOfSubmission){

        File tempFile = new File("temp_claim_docs");
        try(FileOutputStream billsFile = new FileOutputStream(tempFile,true);){
          zout.putNextEntry(new ZipEntry(" Basicdy_" + (String)claimBean.get("patient_id") + ".pdf"));
          List<BasicDynaBean> listOfBills = BillDAO.getVisitBills((String)claimBean.get("patient_id"), BillDAO.bill_type.BOTH, true, null);
          String billsContent = "";
          BasicDynaBean printPref = null;
          int printerId = 0;


          for (BasicDynaBean billBean:listOfBills) {

            String billType = (String) billBean.get("bill_type");
            String billNo = (String) billBean.get("bill_no");

            printerId = billType.equals("P") ?
                (Integer) genericPrefs.get("default_printer_for_bill_now") : (Integer) genericPrefs.get("default_printer_for_bill_later");

            printPref =
              PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
            PrintPageOptions opts = new PrintPageOptions(printPref);

            String templateName = "BUILTIN_HTML"; // anything after CUSTOM- is name of template


            StringWriter writer = new StringWriter();
            String[] returnVals = BillPrintHelper.processBillTemplate(writer, billNo, templateName, userId);

            billsContent = billsContent.concat(writer.toString());


          }

          PdfCopyFields copy = new PdfCopyFields(billsFile);
          byte[] pdfBytes = new byte[1];
          PdfReader reader = null;

              reader = new PdfReader(new ByteArrayInputStream(hc.getPdfBytes(billsContent, "Bill",printPref,false,false,true, true,false)));;
            copy.addDocument(reader);//bill to the pdf


            //list of test reports part of package
              List<BasicDynaBean> packageTestReports = DiagnosticsDAO.getPackageTestReportList( (String)claimBean.get("patient_id") );

              String reportString = "";
              int centerID = -1;
              for ( BasicDynaBean report : packageTestReports ) {

                  reportString = reportString.concat( DiagReportGenerator.getReport(
                          (Integer)report.get("report_id"), "patient", req.getContextPath(),false, false ) );
                  if(centerID == -1) {
                    centerID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), "col");
                  }
              }
              printPref =
                PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, 0, centerID);
              if ( !reportString.isEmpty() ) {
                reader = new PdfReader(new ByteArrayInputStream(hc.getPdfBytes(reportString, "Test Report",printPref,false,false,true, true,false, centerID)));;
              copy.addDocument(reader);//test reports to the pdf
              }

            List<BasicDynaBean> listOfDocs = submitdao.getPackageDocs((String)claimBean.get("patient_id"));
            for(BasicDynaBean doc : listOfDocs){
              if ( doc.get("doc_id") == null ) continue;
              pdfBytes =  new GenericDocumentsProviderBOImpl().getPDFBytes(doc.get("doc_id").toString(), printerId);

              if( pdfBytes == null ) continue;
              reader = new PdfReader(new ByteArrayInputStream(pdfBytes));;
                copy.addDocument(reader);//package related docs to the pdf

            }

              copy.close();
              FileInputStream fileIn = new FileInputStream(tempFile);//get temp file and write to zip entry
              zout.write(DataBaseUtil.readInputStream(fileIn));
              if(!tempFile.delete()){//delete temp file
                logger.info("could not delete temporary file" + tempFile.getName());
              }
          }
      }

      zout.closeEntry();
      zout.close();
      os.flush();
      os.close();

    }finally{
      DataBaseUtil.closeConnections(con, null);
    }

    redirect.addParameter("submission_batch_id", submissionBatchID);
    return null;
  }

	@IgnoreConfidentialFilters
	public ActionForward downloadEClaimXmlFile(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {
		
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		
		String submissionBatchID = req.getParameter("submission_batch_id");
		BasicDynaBean insSubmsnBean = submitdao.findExistsByKey("submission_batch_id", submissionBatchID);
		String fileName = (String)insSubmsnBean.get("file_name")+".xml";
		
		File file = new File("/var/log/insta/insta-ia-sync/"+fileName);
		if(file.exists()) {
			res.setContentType("text/xml");
			res.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
			OutputStream eclaimXmlOutStream = res.getOutputStream();
			FileInputStream eclaimXmlInstream = new FileInputStream(file);
			eclaimXmlOutStream.write(DataBaseUtil.readInputStream(eclaimXmlInstream));
			eclaimXmlOutStream.flush();
			eclaimXmlOutStream.close();
			return null;
		}
		
		flash.info("Requested Resource No longer Available For Submission Batch : "+submissionBatchID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("sortOrder", "created_date");
		redirect.addParameter("sortReverse", "true");
		redirect.addParameter("status", "O");
		
		return redirect;
	}
}
