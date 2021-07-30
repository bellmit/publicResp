package com.insta.hms.master.PlanMaster;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.insurance.InsuranceItemCategoryMasterDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PlanMasterAction extends DispatchAction{

    static Logger logger = LoggerFactory.getLogger(PlanMasterAction.class);
    private static final GenericDAO itemInsuranceCategoriesDAO =new GenericDAO("item_insurance_categories");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");

		PlanMasterDAO planDao = new PlanMasterDAO();
		PagedList pagedList = planDao.getRecords(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()));

		req.setAttribute("policyJSONList", js.serialize(PlanMasterDAO.getPlanNamesAndIds()));
		req.setAttribute("insuranceCompaniesLists", js.serialize(InsuCompMasterDAO.getInsuranceCompaniesNamesAndIds()));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCategoryMasterDAO().getInsCatCenter(RequestContext.getCenterId()))));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("defaultDiscountPlanList",ConversionUtils.copyListDynaBeansToMap(PlanMasterDAO.getDefaultDiscountPlanList()));

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");

		PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();

		req.setAttribute("chargeBean", ConversionUtils.listBeanToListMap(planDetailsDao.getAllPlanCharges()));
		req.setAttribute("insuranceCompaniesLists", js.serialize(InsuCompMasterDAO.getInsuranceCompaniesNamesAndIds()));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCategoryMasterDAO().getInsuranceCategoryActiveList())));

		Map<Object,Object> insuPayableMap = new HashMap<Object,Object>();
		ArrayList<BasicDynaBean> itemCatList = (ArrayList)itemInsuranceCategoriesDAO.listAll();
		for(BasicDynaBean b : itemCatList){
			insuPayableMap.put( b.get("insurance_category_id"), b.get("insurance_payable"));
		}
		req.setAttribute("insuPayableMap",insuPayableMap);
		req.setAttribute("itemCatMap",itemCatList);
		req.setAttribute("itemCatMapJSON",js.serialize(ConversionUtils.listBeanToListMap(itemCatList)));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCategoryMasterDAO().getInsuranceCategoryActiveList())));
		req.setAttribute("insuranceCompaniesTpaLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaList())));
		req.setAttribute("tpaMasterLists", js.serialize(getTpasNamesAndIds()));
		req.setAttribute("defaultDiscountPlanList",ConversionUtils.copyListDynaBeansToMap(PlanMasterDAO.getDefaultDiscountPlanList()));

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		List errors = new ArrayList();

		InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
		req.setAttribute("insuranceCompaniesLists", js.serialize(InsuCompMasterDAO.getInsuranceCompaniesNamesAndIds()));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCategoryMasterDAO().getInsuranceCategoryActiveList())));
		//initialize the DAO's
		PlanMasterDAO planDao = new PlanMasterDAO();
		PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();

		//get respective beans
		BasicDynaBean planBean = planDao.getBean();

		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("plan_id", req.getParameter("plan_id")==null||req.getParameter("plan_id").equals("") ? null:Integer.parseInt((String)req.getParameter("plan_id")));

		ConversionUtils.copyToDynaBean(req.getParameterMap(), planBean, errors,true);

		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");
		planBean.set("username", userName);
		planBean.set("mod_time", DateUtil.getCurrentTimestamp());
		
		String type = req.getParameter("episode_visit_opvalue");
		String from_date = req.getParameter("from_date");
		planBean.set("insurance_validity_start_date", DateUtil.parseDate(from_date));
		planBean.set("insurance_validity_end_date", DateUtil.parseDate(req.getParameter("to_date")));
		String op_planlimit = (String)req.getParameter("op_planlimit");
		if(op_planlimit !=null && !op_planlimit.isEmpty())
		planBean.set("op_plan_limit",BigDecimal.valueOf(Double.parseDouble(op_planlimit)));
		String episode_visit_op = (String)req.getParameter("episode_visit_op");
		if(episode_visit_op !=null && !episode_visit_op.isEmpty()){
		   if(type.equalsIgnoreCase("episode")){
			   planBean.set("op_episode_limit", BigDecimal.valueOf(Double.parseDouble(episode_visit_op)));
			   planBean.set("op_visit_limit",BigDecimal.ZERO);
		    }else{
			   planBean.set("op_visit_limit", BigDecimal.valueOf(Double.parseDouble(req.getParameter("episode_visit_op"))));
			   planBean.set("op_episode_limit",BigDecimal.ZERO);
		    }
		}
		String op_visit_deductible = (String)req.getParameter("op_visit_deductible");
		if(op_visit_deductible !=null && !op_visit_deductible.isEmpty())
		planBean.set("op_visit_deductible", BigDecimal.valueOf(Double.parseDouble(op_visit_deductible)));
		String op_visit_copay = (String)req.getParameter("op_visit_copay");
		if(op_visit_copay !=null && !op_visit_copay.isEmpty())
		planBean.set("op_copay_percent", BigDecimal.valueOf(Double.parseDouble(op_visit_copay)));
		String op_visit_copay_limit = (String)req.getParameter("op_visit_copay_limit");
		if(op_visit_copay_limit !=null && !op_visit_copay_limit.isEmpty())
		planBean.set("op_visit_copay_limit", BigDecimal.valueOf(Double.parseDouble(op_visit_copay_limit)));
		
		String ip_planlimit = (String)req.getParameter("ip_planlimit");
		String ip_per_day_limit = (String)req.getParameter("ip_per_day_limit");
		if(ip_planlimit !=null && !ip_planlimit.isEmpty())
		planBean.set("ip_plan_limit",BigDecimal.valueOf(Double.parseDouble(ip_planlimit)));
		if(ip_per_day_limit !=null && !ip_per_day_limit.isEmpty())
		planBean.set("ip_per_day_limit", BigDecimal.valueOf(Double.parseDouble(ip_per_day_limit)));
		String episode_visit_ip = (String)req.getParameter("episode_visit_ip");
		if(episode_visit_ip !=null && !episode_visit_ip.isEmpty()){
			 planBean.set("ip_visit_limit", BigDecimal.valueOf(Double.parseDouble(req.getParameter("episode_visit_ip"))));
		}
		String ip_visit_deductible = (String)req.getParameter("ip_visit_deductible");
		if(ip_visit_deductible !=null && !ip_visit_deductible.isEmpty())
		planBean.set("ip_visit_deductible", BigDecimal.valueOf(Double.parseDouble(ip_visit_deductible)));
		String ip_visit_copay = (String)req.getParameter("ip_visit_copay");
		if(ip_visit_copay !=null && !ip_visit_copay.isEmpty())
		planBean.set("ip_copay_percent", BigDecimal.valueOf(Double.parseDouble(ip_visit_copay)));
		String ip_visit_copay_limit = (String)req.getParameter("ip_visit_copay_limit");
		if(ip_visit_copay_limit !=null && !ip_visit_copay_limit.isEmpty())
		planBean.set("ip_visit_copay_limit", BigDecimal.valueOf(Double.parseDouble(ip_visit_copay_limit)));
		
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		boolean success = false;
		Connection con = null;

		// insert new plan
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			planBean.set("plan_id", planDao.getNextSequence());
			success = planDao.insert(con, planBean);
			if (success) {
				success = planDetailsDao.insertChargesForPlan((Integer)planBean.get("plan_id"),userName);
				String updtdChargeList = req.getParameter("updtdChargeHeadList");
				if (updtdChargeList != null && !updtdChargeList.equals("") && updtdChargeList.split(",").length>0) {
					String[] chargeHeadIdArray = updtdChargeList.split(",");
					ArrayList<String> chargeIdList = new ArrayList<String>();
					if (chargeHeadIdArray != null) {
						for(String chId: chargeHeadIdArray) {
							chargeIdList.add(chId);
						}
						for(String ch: chargeIdList) {
							String[] chargeArr = ch.split("@");
							String chg = chargeArr[0];
							String patientTyp = chargeArr[1];
							keys.put("patient_type", patientTyp);
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("patient_amount", req.getParameter(chg+"_amtHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_amtHidden_"+patientTyp))));
							map.put("patient_amount_per_category", req.getParameter(chg+"_catAmtHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_catAmtHidden_"+patientTyp))));
							map.put("patient_percent", req.getParameter(chg+"_perHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_perHidden_"+patientTyp))));
							map.put("patient_amount_cap",
									req.getParameter(chg+"_capHidden_"+patientTyp)==null ||
									req.getParameter(chg+"_capHidden_"+patientTyp).equals("")?
									null:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_capHidden_"+patientTyp))));
							map.put("per_treatment_limit", req.getParameter(chg+"_treatHidden_"+patientTyp)==null ||
									req.getParameter(chg+"_treatHidden_"+patientTyp).equals("")?
									null:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_treatHidden_"+patientTyp))));
							map.put("category_payable", req.getParameter(chg+"_category_payable_"+patientTyp)==null ||
									req.getParameter(chg+"_category_payable_"+patientTyp).equals("")?"N":req.getParameter(chg+"_category_payable_"+patientTyp));
							map.put("category_prior_auth_required", req.getParameter(chg+"_category_prior_auth_required_"+patientTyp)==null ||
									req.getParameter(chg+"_category_prior_auth_required_"+patientTyp).equals("")?null:req.getParameter(chg+"_category_prior_auth_required_"+patientTyp));
							map.put("username", userName);
							Map<String,Object>  keyMap = new HashMap<String,Object>();
							keyMap.put("plan_id", (Integer)planBean.get("plan_id"));
							keyMap.put("insurance_category_id",Integer.parseInt(chg.toString()));
							keyMap.put("patient_type",patientTyp);
							int i = planDetailsDao.update(con,"insurance_plan_details", map, keyMap);
							success = success && (i > 0);
						}
					}
				}
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("plan_id", planBean.get("plan_id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				flash.success("Plan details inserted successfully..");
			} else {
				flash.error("Failed to add Plan Details..");
			}
		} finally {
			DataBaseUtil.commitClose(con,success);
		}
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");

		PlanMasterDAO planDao = new PlanMasterDAO();
		PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
		BasicDynaBean planBean = null;
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		if(req.getParameter("plan_id")!= null && !req.getParameter("plan_id").equals(""))
			planBean = planDao.findPlan("plan_id", Integer.parseInt(req.getParameter("plan_id")));
		else if(req.getParameter("planName")!= null && !req.getParameter("planName").equals("")){
			planBean = planDao.findPlan("plan_name", req.getParameter("planName"), "ilike");
		}
		if(planBean == null) {
			flash.error("No such Plan found...");
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		List<BasicDynaBean> planDetailsbean = planDetailsDao.getAllPlanCharges((Integer)(planBean.get("plan_id")));
		//footer or charge fields
		req.setAttribute("from_date", DateUtil.formatDate((Date)planBean.get("insurance_validity_start_date")));
		req.setAttribute("to_date", DateUtil.formatDate((Date)planBean.get("insurance_validity_end_date")));
		req.setAttribute("discount_plan_id", planBean.get("discount_plan_id"));
		req.setAttribute("bean", planBean);
		req.setAttribute("chargeBean", ConversionUtils.listBeanToListMap(planDetailsbean));
		req.setAttribute("planLists", js.serialize(PlanMasterDAO.getPlanNamesAndIds()));
		ArrayList<BasicDynaBean> itemCatList = (ArrayList)itemInsuranceCategoriesDAO.listAll();
		Map<Object,Object> insuPayableMap = new HashMap<Object,Object>();
		for(BasicDynaBean b : itemCatList){
			insuPayableMap.put( b.get("insurance_category_id"), b.get("insurance_payable"));
		}
		req.setAttribute("insuPayableMap",insuPayableMap);
		req.setAttribute("itemCatMap",itemCatList);
		req.setAttribute("itemCatMapJSON",js.serialize(ConversionUtils.listBeanToListMap(itemCatList)));
		req.setAttribute("tpaMasterLists", js.serialize(getTpasNamesAndIds()));
		req.setAttribute("insuranceCompaniesTpaLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCompanyTPAMasterDAO().getCompanyTpaList())));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(new InsuranceCategoryMasterDAO().getInsuranceCategoryActiveList())));
		
		req.setAttribute("defaultDiscountPlanList",ConversionUtils.copyListDynaBeansToMap(planDao.getDefaultDiscountPlanList()));
		return m.findForward("addshow");
	}
	public static String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}


	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException, Exception {
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		List errors = new ArrayList();
		String type = req.getParameter("episode_visit_opvalue");
		String from_date = req.getParameter("from_date");
		PlanMasterDAO planDao = new PlanMasterDAO();
		PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
		BasicDynaBean planBean = planDao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), planBean, errors, true);
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");
		planBean.set("username", userName);
		planBean.set("mod_time", DateUtil.getCurrentTimestamp());
		planBean.set("insurance_validity_start_date", DateUtil.parseDate(from_date));
		planBean.set("insurance_validity_end_date", DateUtil.parseDate(req.getParameter("to_date")));
		String op_planlimit = (String)req.getParameter("op_planlimit");
		if(op_planlimit !=null && !op_planlimit.isEmpty())
		planBean.set("op_plan_limit",BigDecimal.valueOf(Double.parseDouble(op_planlimit)));
		String episode_visit_op = (String)req.getParameter("episode_visit_op");
		if(episode_visit_op !=null && !episode_visit_op.isEmpty()){
		   if(type.equalsIgnoreCase("episode")){
			   planBean.set("op_episode_limit", BigDecimal.valueOf(Double.parseDouble(episode_visit_op)));
			   planBean.set("op_visit_limit",BigDecimal.ZERO);
		    }else{
			   planBean.set("op_visit_limit", BigDecimal.valueOf(Double.parseDouble(req.getParameter("episode_visit_op"))));
			   planBean.set("op_episode_limit",BigDecimal.ZERO);
		    }
		}
		String op_visit_deductible = (String)req.getParameter("op_visit_deductible");
		if(op_visit_deductible !=null && !op_visit_deductible.isEmpty())
		planBean.set("op_visit_deductible", BigDecimal.valueOf(Double.parseDouble(op_visit_deductible)));
		String op_visit_copay = (String)req.getParameter("op_visit_copay");
		if(op_visit_copay !=null && !op_visit_copay.isEmpty())
		planBean.set("op_copay_percent", BigDecimal.valueOf(Double.parseDouble(op_visit_copay)));
		String op_visit_copay_limit = (String)req.getParameter("op_visit_copay_limit");
		if(op_visit_copay_limit !=null && !op_visit_copay_limit.isEmpty())
		planBean.set("op_visit_copay_limit", BigDecimal.valueOf(Double.parseDouble(op_visit_copay_limit)));
		
		String ip_planlimit = (String)req.getParameter("ip_planlimit");
		String ip_per_day_limit = (String)req.getParameter("ip_per_day_limit");
		if(ip_planlimit !=null && !ip_planlimit.isEmpty())
		planBean.set("ip_plan_limit",BigDecimal.valueOf(Double.parseDouble(ip_planlimit)));
		if(ip_per_day_limit !=null && !ip_per_day_limit.isEmpty())
		planBean.set("ip_per_day_limit", BigDecimal.valueOf(Double.parseDouble(ip_per_day_limit)));
		String episode_visit_ip = (String)req.getParameter("episode_visit_ip");
		if(episode_visit_ip !=null && !episode_visit_ip.isEmpty()){
			 planBean.set("ip_visit_limit", BigDecimal.valueOf(Double.parseDouble(req.getParameter("episode_visit_ip"))));
		}
		String ip_visit_deductible = (String)req.getParameter("ip_visit_deductible");
		if(ip_visit_deductible !=null && !ip_visit_deductible.isEmpty())
		planBean.set("ip_visit_deductible", BigDecimal.valueOf(Double.parseDouble(ip_visit_deductible)));
		String ip_visit_copay = (String)req.getParameter("ip_visit_copay");
		if(ip_visit_copay !=null && !ip_visit_copay.isEmpty())
		planBean.set("ip_copay_percent", BigDecimal.valueOf(Double.parseDouble(ip_visit_copay)));
		String ip_visit_copay_limit = (String)req.getParameter("ip_visit_copay_limit");
		if(ip_visit_copay_limit !=null && !ip_visit_copay_limit.isEmpty())
		planBean.set("ip_visit_copay_limit", BigDecimal.valueOf(Double.parseDouble(ip_visit_copay_limit)));
		
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("plan_id", Integer.parseInt(req.getParameter("plan_id")));
		String updtdChargeHeadList = req.getParameter("updtdChargeHeadList");

		Connection con = null;

		try {
			con = DataBaseUtil.getConnection();
			if(updtdChargeHeadList != null && !updtdChargeHeadList.equals("") && updtdChargeHeadList.split(",").length>0){
				String[] chargeHeadIdArray = updtdChargeHeadList.split(",");
				ArrayList<String> chargeHeadIdList = new ArrayList<String>();
				if(chargeHeadIdArray != null){
					for(String chId: chargeHeadIdArray) {
						chargeHeadIdList.add(chId);
					}
					for(String ch: chargeHeadIdList) {
						Map<String,Object> map = new HashMap<String,Object>();
						String[] chargeArr = ch.split("@");
						String chg = chargeArr[0];
						String patientTyp = chargeArr[1];
						map.put("patient_amount", req.getParameter(chg+"_amtHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_amtHidden_"+patientTyp))));
						map.put("patient_amount_per_category", req.getParameter(chg+"_catAmtHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_catAmtHidden_"+patientTyp))));
						map.put("patient_percent", req.getParameter(chg+"_perHidden_"+patientTyp)==null?BigDecimal.ZERO:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_perHidden_"+patientTyp))));
						map.put("patient_amount_cap",
								req.getParameter(chg+"_capHidden_"+patientTyp)==null ||
								req.getParameter(chg+"_capHidden_"+patientTyp).equals("")?
								null:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_capHidden_"+patientTyp))));
						map.put("per_treatment_limit", req.getParameter(chg+"_treatHidden_"+patientTyp)==null ||
								req.getParameter(chg+"_treatHidden_"+patientTyp).equals("")?
								null:BigDecimal.valueOf(Double.parseDouble(req.getParameter(chg+"_treatHidden_"+patientTyp))));
						map.put("username", userName);
						map.put("category_payable", req.getParameter(chg+"_category_payable_"+patientTyp)==null ||
								req.getParameter(chg+"_category_payable_"+patientTyp).equals("")?'Y':req.getParameter(chg+"_category_payable_"+patientTyp));
						map.put("category_prior_auth_required", req.getParameter(chg+"_category_prior_auth_required_"+patientTyp)==null ||
								req.getParameter(chg+"_category_prior_auth_required_"+patientTyp).equals("")?null:req.getParameter(chg+"_category_prior_auth_required_"+patientTyp));
						keys.put("patient_type", patientTyp);
						Map keyMap = new HashMap();
						keyMap.putAll(keys);
						keyMap.put("insurance_category_id",Integer.parseInt(chg));
						keyMap.put("patient_type",patientTyp);
						planDetailsDao.update(con, map, keyMap);
					}
				}
			}

			keys.clear();
			keys.put("plan_id", Integer.parseInt(req.getParameter("plan_id")));
			int success = planDao.update(con, planBean.getMap(), keys);
			if(success>0)
				flash.info("Plan details updated successfully...");
			else
				flash.error("Failed to update plan details...");

		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("plan_id",req.getParameter("plan_id"));
		
		return redirect;
	}

	/*
	 * CSV helper functions
	 */
	public static String validateCsvHeaderFormat(String[] header) {
		String error = null;
		do{
			if (header.length < 1) {
				error = "Uploaded file does not appear to be a CSV file (no headers found)";
				break ;
			}

			if (!header[0].matches("\\p{Print}*")) {
				error = "Uploaded file does not appear to be a CSV file (non-printable characters found)";
				break;
			}

			if (header[0].length() > 20) {
				error = "Uploaded file does not appear to be a CSV file (header is too long)";
				break;
			}

			if (!header[0].equals("plan_id")) {
				error = "CSV format error: first column must be plan_id but found :" + header[0];
				break;
			}

			if (header.length < 20) {
				error = "CSV format error: need at least 20 columns, but found " + header.length;
				break;
			}

		}while(false);
		if(error == null) {
			List hdrList =  getHeaderNamesForCSV();
			for(int i=1; i<hdrList.size();i++) {
				if(!header[i].equals(hdrList.get(i))){
					error = "CSV format error: column "+(i+1)+" must be "+hdrList.get(i)+" but found " + header[i];
					break;
				}
			}
		}
		return error;
	}


	public static  String[] planHeaderColumns = {"plan_id","plan_name","category_name","overall_treatment_limit", "insurance_company",
			"plan_notes", "plan_exclusions", "default_rate_plan", "ip_applicable", "op_applicable",
			"status","is_copay_pc_on_post_discnt_amt", "base_rate", "gap_amount", "marginal_percent", "add_on_payment_factor",
			"perdiem_copay_per", "perdiem_copay_amount",
			"op_visit_copay_limit", "ip_visit_copay_limit",
			"require_pbm_authorization"};

	public static List getHeaderNamesForCSV() {
		List<String> insuranceCategoryHeadersList = new PlanMasterDAO().getInsuranceCategoryHeaderNames();
		Object[] insuranceCategoryHeaders = insuranceCategoryHeadersList.toArray();
		String[] combinedHeaderArray= new String[insuranceCategoryHeaders.length+planHeaderColumns.length];
		// static header names for insurance plan main...
		for(int i=0; i<planHeaderColumns.length; i++) {
			combinedHeaderArray[i]= planHeaderColumns[i];
		}
		int index = planHeaderColumns.length;
		/*
		 * For insurance plan detail- insurance category column values are fetched as a row,
		 * with ip or op suffixed to distinguish between op and ip patient type values. The various
		 * amounts (fixed, per item, percent etc.) are also suffixed to differentiate amongst the various
		 * patient-type, insurance_category wise, plan amount values.
		 */
		for(int j=0;j<insuranceCategoryHeaders.length; j++) {
			combinedHeaderArray[index++]= (String)insuranceCategoryHeaders[j];
		}
		List combinedHeaderArrayList = Arrays.asList(combinedHeaderArray);
		return combinedHeaderArrayList ;
	}


	/*
	 * This function is used to import the plan details from the csv file. Commits are done incrementally
	 * every 25 lines to prevent time-out. Numeric format errors in file are ignored, unless its for overall treatment limit.
	 */
	public ActionForward importValuesFromCSV(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {
		PlanUploadForm pform = (PlanUploadForm) f;
		CSVReader csvReader = new CSVReader(new InputStreamReader(pform.getCsvFile().getInputStream()));
		FlashScope flash = FlashScope.getScope(req);
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String error = null;
		String[] header = csvReader.readNext();
		error = validateCsvHeaderFormat(header);
		if(error != null) {
			flash.put("error", error);
			return redirect;
		}
		//processing of CSV lines, starts here...
		String[] line = null;
		Connection con = null;
		String [] combinedHeaderArray = (String[])getHeaderNamesForCSV().toArray();
		Boolean success = false;
		String info = "Plan Details updated successfully";

		MultiValueMap multiInsuCatMap = InsuranceCategoryMasterDAO.getInsuranceCategoriesNamesAndIdsMultiMap ();
		Map insuMap = InsuCompMasterDAO.getInsuranceCompaniesNamesAndIdsMap();
		Map ratePlanMap = OrgMasterDao.getOrgNamesAndIdsMap();
		Map insuItemCatMap = InsuranceItemCategoryMasterDAO.getInsuranceCategoryNamesAndIdsMap();
		Map insurNameAndCatIdsMap = InsuranceCategoryMasterDAO.getInsuranceNamesAndCatIdsMap();
		String prevInsuCatName = "";
		String prevPatType = "";
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");


		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			PlanDetailsDAO pdDAO = new PlanDetailsDAO();
			PlanMasterDAO pDAO = new PlanMasterDAO();
			Map<String, String> planMainMap = new HashMap<String, String>();
			int lineCount = 1;
			Map amtValMap = new HashMap();
			while ((line = csvReader.readNext()) != null) {
				lineCount++;
				planMainMap.put("plan_id", line[0]);
				planMainMap.put("plan_name", line[1]);
				planMainMap.put("category_name", line[2]);
				planMainMap.put("overall_treatment_limit", line[3]);
				planMainMap.put("insurance_company", line[4]);
				planMainMap.put("plan_notes", line[5]);
				planMainMap.put("plan_exclusions", line[6]);
				planMainMap.put("default_rate_plan", line[7]);
				planMainMap.put("ip_applicable", line[8]);
				planMainMap.put("op_applicable", line[9]);
				planMainMap.put("status", line[10]);
				planMainMap.put("is_copay_pc_on_post_discnt_amt",line[11]);
				planMainMap.put("base_rate",line[12]);
				planMainMap.put("gap_amount",line[13]);
				planMainMap.put("marginal_percent",line[14]);
				planMainMap.put("add_on_payment_factor",line[15]);
				planMainMap.put("perdiem_copay_per",line[16]);
				planMainMap.put("perdiem_copay_amount",line[17]);
				planMainMap.put("op_visit_copay_limit",line[18]);
				planMainMap.put("ip_visit_copay_limit",line[19]);
				planMainMap.put("require_pbm_authorization",line[20]);

				if((planMainMap.get("plan_name") == null || planMainMap.get("plan_name").equals(""))
					|| planMainMap.get("category_name")== null || planMainMap.get("category_name").equals("")
					|| !multiInsuCatMap.containsKey(planMainMap.get("category_name"))
					|| planMainMap.get("insurance_company")== null || planMainMap.get("insurance_company").equals("")
					|| !insuMap.containsKey(planMainMap.get("insurance_company")) ) {
					flash.put("error", "Failed to update, some required values are not present");
					return redirect;
				}

				BasicDynaBean planMainBean = pDAO.getBean();
				planMainBean.set("plan_name", planMainMap.get("plan_name"));
				try{
					planMainBean.set("overall_treatment_limit",
						planMainMap.get("overall_treatment_limit") == null
								|| planMainMap.get("overall_treatment_limit").equals("") ? null
								: new BigDecimal(planMainMap.get("overall_treatment_limit")));
				}catch(NumberFormatException e){
					planMainBean.set("overall_treatment_limit",null);
				}
				planMainBean.set("insurance_co_id", insuMap.get(planMainMap.get("insurance_company")));
				planMainBean.set("plan_notes", planMainMap.get("plan_notes"));
				planMainBean.set("plan_exclusions", planMainMap.get("plan_exclusions"));
				planMainBean.set("default_rate_plan", !ratePlanMap.containsKey(planMainMap.get("default_rate_plan"))? null :ratePlanMap.get(planMainMap.get("default_rate_plan")));
				planMainBean.set("ip_applicable", planMainMap.get("ip_applicable") == null
						|| planMainMap.get("ip_applicable").equals("")? "N":
							planMainMap.get("ip_applicable").equals("Y")?"Y":"N");

				List<Integer> catIdList = (ArrayList)multiInsuCatMap.get(planMainMap.get("category_name"));
				for(Integer id: catIdList){
					String insuranceId = (String) insuMap.get(planMainMap.get("insurance_company"));
					if(insurNameAndCatIdsMap.get(id).equals(insuranceId))
						planMainBean.set("category_id", id);
				}

				planMainBean.set("op_applicable", planMainMap.get("op_applicable") == null
						|| planMainMap.get("op_applicable").equals("")? "N":
							planMainMap.get("op_applicable").equals("Y")?"Y":"N");

				planMainBean.set("is_copay_pc_on_post_discnt_amt", planMainMap.get("is_copay_pc_on_post_discnt_amt") == null
						|| planMainMap.get("is_copay_pc_on_post_discnt_amt").equals("")? "N":
							planMainMap.get("is_copay_pc_on_post_discnt_amt").equals("Y")?"Y":"N");

				planMainBean.set("status", planMainMap.get("status") == null
						|| planMainMap.get("status").equals("")? "A":
							planMainMap.get("status").equals("I")?"I":"A");

				planMainBean.set("base_rate", (planMainMap.get("base_rate") == null
							|| planMainMap.get("base_rate").trim().equals("") )? BigDecimal.ZERO
							: new BigDecimal(planMainMap.get("base_rate")));

				planMainBean.set("gap_amount", (planMainMap.get("gap_amount") == null
								|| planMainMap.get("gap_amount").trim().equals("") )? BigDecimal.ZERO
								: new BigDecimal(planMainMap.get("gap_amount")));

				planMainBean.set("marginal_percent", (planMainMap.get("marginal_percent") == null
								|| planMainMap.get("marginal_percent").trim().equals("") )? BigDecimal.ZERO
								: new BigDecimal(planMainMap.get("marginal_percent")));
								
				planMainBean.set("add_on_payment_factor", (planMainMap.get("add_on_payment_factor") == null
								|| planMainMap.get("add_on_payment_factor").trim().equals("") )? new BigDecimal(75)
								: new BigDecimal(planMainMap.get("add_on_payment_factor")));

				planMainBean.set("perdiem_copay_per", (planMainMap.get("perdiem_copay_per") == null
						|| planMainMap.get("perdiem_copay_per").trim().equals("") )? BigDecimal.ZERO
						: new BigDecimal(planMainMap.get("perdiem_copay_per")));

				planMainBean.set("perdiem_copay_amount", (planMainMap.get("perdiem_copay_amount") == null
						|| planMainMap.get("perdiem_copay_amount").trim().equals("") )? BigDecimal.ZERO
						: new BigDecimal(planMainMap.get("perdiem_copay_amount")));

				planMainBean.set("op_visit_copay_limit", (planMainMap.get("op_visit_copay_limit") == null
						|| planMainMap.get("op_visit_copay_limit").trim().equals("") )? BigDecimal.ZERO
						: new BigDecimal(planMainMap.get("op_visit_copay_limit")));

				planMainBean.set("ip_visit_copay_limit", (planMainMap.get("ip_visit_copay_limit") == null
						|| planMainMap.get("ip_visit_copay_limit").trim().equals("") )? BigDecimal.ZERO
						: new BigDecimal(planMainMap.get("ip_visit_copay_limit")));

				planMainBean.set("require_pbm_authorization", planMainMap.get("require_pbm_authorization") == null
						|| planMainMap.get("require_pbm_authorization").equals("")? "N":
							planMainMap.get("require_pbm_authorization").equals("Y")?"Y":"N");

				if((planMainMap.get("plan_id") == null || planMainMap.get("plan_id").equals(""))
						&& (pDAO.findByKey("plan_name", planMainMap.get("plan_name")) == null)) {
					// do insert of plan details
					int newPlanId = pDAO.getNextSequence();
					planMainBean.set("plan_id", newPlanId);
					planMainBean.set("username", userName+":CSV");
					planMainBean.set("mod_time", DateUtil.getCurrentTimestamp());
					Boolean mainSuccess = false;
					mainSuccess = pDAO.insert(con, planMainBean);
					// if insert into insurance plan main was successful, then insert into plan_details...
					if(mainSuccess) {
						 mainSuccess = pdDAO.insertChargesForPlan(newPlanId,userName);
						 for (int i=21; i<line.length; i++) {
							 if ((line[i].isEmpty() || line[i]== null || line[i].equals(""))
									 && !(combinedHeaderArray[i].split("-")[2].equals("patient_amount_cap")
											 || combinedHeaderArray[i].split("-")[2].equals("per_treatment_limit"))) {
								 continue;
							 }
							 if(i>21) {
								 prevInsuCatName = combinedHeaderArray[i-1].split("-")[0];
								 prevPatType = combinedHeaderArray[i-1].split("-")[1];
							 } else {
								 prevInsuCatName = combinedHeaderArray[i].split("-")[0];
								 prevPatType = combinedHeaderArray[i].split("-")[1];
							 }
							 String insuItemCatName = combinedHeaderArray[i].split("-")[0];
							 Integer itemItemCatId = (Integer)insuItemCatMap.get(insuItemCatName);
							 String patType = combinedHeaderArray[i].split("-")[1];
							 String amtType = combinedHeaderArray[i].split("-")[2];
							 Boolean nullableAmtType = amtType.equals("patient_amount_cap") || amtType.equals("per_treatment_limit");
							 if(amtType.equals("patient_percent") && line[i] != null && !line[i].isEmpty() && !line[i].equals("null")
							 	&& (new BigDecimal(line[i]).compareTo(new BigDecimal(100))) > 0){
								 line[i]="100";
							 }
							 try {
								 if (prevInsuCatName.equals(insuItemCatName) && !prevPatType.equals(patType)) {
									 updatePlanDetailValues(con, amtValMap, newPlanId, (Integer)insuItemCatMap.get(prevInsuCatName), prevPatType);
									 amtValMap = new HashMap();
								 }
								if (prevInsuCatName.equals(insuItemCatName)) {
									if (amtType.equals("category_payable")) {
										amtValMap.put(amtType, line[i] == null || line[i].equals("") ? 'Y' : line[i]);
									} else {
										BigDecimal value = nullableAmtType
												? line[i].isEmpty() || line[i] == null || line[i].equals("") || line[i].equals("null") ? null
														: new BigDecimal(line[i])
												: line[i].equals("null") ? null : new BigDecimal(line[i]);
										amtValMap.put(amtType, value);
									}

								}
								amtValMap.put("username", userName);
							} catch (NumberFormatException e) {
								// ignore incorrectly formatted numbers...
							}
							if(!prevInsuCatName.equals(insuItemCatName)) {
								 updatePlanDetailValues(con, amtValMap, newPlanId, (Integer)insuItemCatMap.get(prevInsuCatName), prevPatType);
								 amtValMap = new HashMap();
								 BigDecimal value = nullableAmtType ? line[i].isEmpty() || line[i] == null
											|| line[i].equals("") || line[i].equals("null") ? null : new BigDecimal(line[i])
											: line[i].equals("null") ? null : new BigDecimal(line[i]);
									amtValMap.put(amtType, value);
							}
							if(i == line.length-1){
								updatePlanDetailValues(con, amtValMap, newPlanId, itemItemCatId, patType);
								amtValMap = new HashMap();
							}
						 }
					}//else update existing plan details...
				} else if((planMainMap.get("plan_id") != null && !planMainMap.get("plan_id").equals(""))) {
					Boolean mainSuccess = false;
					planMainBean.set("plan_id",Integer.parseInt(planMainMap.get("plan_id")));
					planMainBean.set("username", userName + ":CSV");
					planMainBean.set("mod_time", DateUtil.getCurrentTimestamp());
					mainSuccess = pDAO.update(con, planMainBean.getMap(), "plan_id", Integer.parseInt(planMainMap.get("plan_id"))) > 0;
					if(mainSuccess) {
						 for (int i=21; i<line.length; i++) {
							 if ( (line[i].isEmpty() || line[i]== null || line[i].equals("") || line[i].equals("null"))
									 && !(combinedHeaderArray[i].split("-")[2].equals("patient_amount_cap")
											 || combinedHeaderArray[i].split("-")[2].equals("per_treatment_limit"))) {
								 continue;
							 }
							 if(i>21) {
								 prevInsuCatName = combinedHeaderArray[i-1].split("-")[0];
								 prevPatType = combinedHeaderArray[i-1].split("-")[1];
							 } else {
								 prevInsuCatName = combinedHeaderArray[i].split("-")[0];
								 prevPatType = combinedHeaderArray[i].split("-")[1];
							 }
							 String insuItemCatName = combinedHeaderArray[i].split("-")[0];
							 Integer itemItemCatId = (Integer)insuItemCatMap.get(insuItemCatName);
							 String patType = combinedHeaderArray[i].split("-")[1];
							 String amtType = combinedHeaderArray[i].split("-")[2];
							 Boolean nullableAmtType = amtType.equals("patient_amount_cap") || amtType.equals("per_treatment_limit");
							 if(amtType.equals("patient_percent") && line[i] != null && !line[i].isEmpty() && !line[i].equals("null")
							 	&& (new BigDecimal(line[i]).compareTo(new BigDecimal(100))) > 0) {
								 line[i]="100";
							 }
							 try {
								 if (prevInsuCatName.equals(insuItemCatName) && !prevPatType.equals(patType)) {
									 updatePlanDetailValues(con, amtValMap, Integer.parseInt(planMainMap.get("plan_id")), (Integer)insuItemCatMap.get(prevInsuCatName), prevPatType);
									 amtValMap = new HashMap();
								 }
								if (prevInsuCatName.equals(insuItemCatName)) {
									if(amtType.equals("category_payable")){
										amtValMap.put(amtType, line[i]==null || line[i].equals("")?'Y':line[i]);
									}else{
										BigDecimal value = nullableAmtType ? line[i].isEmpty() || line[i] == null
												|| line[i].equals("") ? null : new BigDecimal(line[i])
												: new BigDecimal(line[i]);
										amtValMap.put(amtType, value);
									}
								}
								amtValMap.put("username", userName);
							} catch (NumberFormatException e) {
								// ignore incorrectly formatted numbers...
							}
							if(!prevInsuCatName.equals(insuItemCatName)){
								 updatePlanDetailValues(con, amtValMap, Integer.parseInt(planMainMap.get("plan_id")), (Integer)insuItemCatMap.get(prevInsuCatName), prevPatType);
								 amtValMap = new HashMap();
								 BigDecimal value = nullableAmtType ? line[i].isEmpty() || line[i] == null
											|| line[i].equals("") || line[i].equals("null") ? null : new BigDecimal(line[i])
											: line[i].equals("null") ? null : new BigDecimal(line[i]);
									amtValMap.put(amtType, value);
							}
							if(i == line.length-1){
								updatePlanDetailValues(con, amtValMap, Integer.parseInt(planMainMap.get("plan_id")), itemItemCatId, patType);
								amtValMap = new HashMap();
							}
						 }
					}
				}else {
					info = info + "<br/> Plan- "+planMainMap.get("plan_name")+" was not inserted, as a plan with the same name exists.";
				}
				//incremental commit for every twenty five lines...
				if(lineCount%25 == 0) {
					con.commit();
					DataBaseUtil.closeConnections(con, null);
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
				}

			}
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		flash.put("info", info);
		return redirect;
	}


	public void updatePlanDetailValue(Connection con, Integer newPlanId, Integer itemCatId, String patType, String amtType, BigDecimal value,String userName)
		throws IOException, SQLException{
		PlanDetailsDAO pdDAO = new PlanDetailsDAO();
		String amtCol = amtType.equals("per_item_pat_amt")? "patient_amount":
						amtType.equals("per_categ_pat_amt")? "patient_amount_per_category":
						amtType.equals("patient_percent")? "patient_percent":
							amtType.equals("patient_amount_cap")? "patient_amount_cap":
							"per_treatment_limit";
		String patientType = patType.equals("ip")? "i":"o";
		BigDecimal amtValue  = value;
		Map columnData = new HashMap();
		Map keys = new HashMap();

		columnData.put(amtCol, amtValue);
		keys.put("patient_type",patientType);
		keys.put("plan_id", newPlanId);
		keys.put("insurance_category_id", itemCatId);
		pdDAO.update(con, columnData, keys);
	}


	public void updatePlanDetailValues(Connection con, Map m, Integer newPlanId, Integer itemCatId, String patType)
		throws IOException, SQLException{
		PlanDetailsDAO pdDAO = new PlanDetailsDAO();
		Map columnData = new HashMap();
		if(m.get("per_item_pat_amt") != null)
			columnData.put("patient_amount", m.get("per_item_pat_amt"));

		if(m.get("per_categ_pat_amt") != null)
			columnData.put("patient_amount_per_category", m.get("per_categ_pat_amt"));

		if(m.get("patient_percent") != null)
			columnData.put("patient_percent", m.get("patient_percent"));

		//columnData.put("patient_percent", m.get("patient_percent"));
		columnData.put("patient_amount_cap", m.get("patient_amount_cap"));
		columnData.put("per_treatment_limit", m.get("per_treatment_limit"));
		columnData.put("category_payable", m.get("category_payable"));
		columnData.put("username",  m.get("username")+ ":CSV");

		Map keys = new HashMap();
		String patientType = patType.equals("ip")? "i":"o";
		keys.put("patient_type",patientType);
		keys.put("plan_id", newPlanId);
		keys.put("insurance_category_id", itemCatId);
		pdDAO.update(con, columnData, keys);
	}

	public ActionForward exportChargesCSV(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {
		String[] combinedHeaderArray= (String[])getHeaderNamesForCSV().toArray();
		int MaxPlanHeadsLength = (itemInsuranceCategoriesDAO.listAll().size()*12)+21;
		PlanMasterDAO planDao = new PlanMasterDAO();
		int MaxNumberOfPlans =  planDao.listAll().size();
		String[][] valueArray = new String[MaxNumberOfPlans+1][MaxPlanHeadsLength];
		res.setHeader("Content-type","application/csv");
		res.setHeader("Content-disposition","attachment; filename=PlansUpload.csv");
		res.setHeader("Readonly","true");
		CSVWriter writer = new CSVWriter(res.getWriter(),CSVWriter.DEFAULT_SEPARATOR);
		List<BasicDynaBean> planList =  planDao.getInsurancePlanForCsv();
		List<BasicDynaBean> insuCatList = itemInsuranceCategoriesDAO.listAll("insurance_category_name");
		planList.size();
		writer.writeNext(combinedHeaderArray);
		writer.flush();
		int outerIndex = 0;
		for ( BasicDynaBean planData : planList) {
			int innerIndex = 0;
			valueArray[outerIndex][innerIndex++] = String.valueOf(planData.get("plan_id"));
					valueArray[outerIndex][innerIndex++] = (String)planData.get("plan_name");
					valueArray[outerIndex][innerIndex++] = (String)planData.get("category_name");
					valueArray[outerIndex][innerIndex++] =  planData.get("overall_treatment_limit")== null
							? "" : String.valueOf(planData.get("overall_treatment_limit"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("insurance_company"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("plan_notes"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("plan_exclusions"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("default_rate_plan"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("ip_applicable"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("op_applicable"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("status"));
					valueArray[outerIndex][innerIndex++] = (String)(planData.get("is_copay_pc_on_post_discnt_amt"));
					valueArray[outerIndex][innerIndex++] = planData.get("base_rate")== null? "0" : String.valueOf(planData.get("base_rate"));
					valueArray[outerIndex][innerIndex++] = planData.get("gap_amount")== null? "0" : String.valueOf(planData.get("gap_amount"));
					valueArray[outerIndex][innerIndex++] = planData.get("marginal_percent")== null? "0" : String.valueOf(planData.get("marginal_percent"));
					valueArray[outerIndex][innerIndex++] = planData.get("add_on_payment_factor")== null? "75" : String.valueOf(planData.get("add_on_payment_factor"));
					valueArray[outerIndex][innerIndex++] = planData.get("perdiem_copay_per")== null? "0" : String.valueOf(planData.get("perdiem_copay_per"));
					valueArray[outerIndex][innerIndex++] = planData.get("perdiem_copay_amount")== null? "0" : String.valueOf(planData.get("perdiem_copay_amount"));
					valueArray[outerIndex][innerIndex++] = planData.get("op_visit_copay_limit")== null? "0" : String.valueOf(planData.get("op_visit_copay_limit"));
					valueArray[outerIndex][innerIndex++] = planData.get("ip_visit_copay_limit")== null? "0" : String.valueOf(planData.get("ip_visit_copay_limit"));

					valueArray[outerIndex][innerIndex++] = (String)(planData.get("require_pbm_authorization"));
			        List<BasicDynaBean>  planListBean =  planDao.getInsurancePlanDetailsForCsv((Integer) planData.get("plan_id"));
			        Map<Integer,List<BasicDynaBean>> patientTypeMapList = ConversionUtils.listBeanToMapListBean(planListBean,"patient_type");
					for (BasicDynaBean insuCat : insuCatList) {
						BasicDynaBean ipBean = getMatchBean(patientTypeMapList.get("i"),(Integer) insuCat.get("insurance_category_id"));
						BasicDynaBean opBean = getMatchBean(patientTypeMapList.get("o"),(Integer) insuCat.get("insurance_category_id"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null ? "" : ipBean.get("patient_amount"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null ? "" : ipBean.get("patient_amount_per_category"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null ? "" : ipBean.get("patient_percent"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null || ipBean.get("patient_amount_cap") == null ? "" : ipBean.get("patient_amount_cap"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null || ipBean.get("per_treatment_limit") == null ? "" : ipBean.get("per_treatment_limit"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(ipBean == null || ipBean.get("category_payable") == null ? "" : ipBean.get("category_payable"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null ? "" : opBean.get("patient_amount"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null ? "" : opBean.get("patient_amount_per_category"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null ? "" : opBean.get("patient_percent"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null || opBean.get("patient_amount_cap") == null ? "" : opBean.get("patient_amount_cap"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null || opBean.get("per_treatment_limit") == null ? "" : opBean.get("per_treatment_limit"));
						valueArray[outerIndex][innerIndex++] = String.valueOf(opBean == null || opBean.get("category_payable") == null ? "" : opBean.get("category_payable"));
					}

				  writer.writeNext(valueArray[outerIndex]);
				  writer.flush();
				  outerIndex++;
		}
		writer.flush();
		writer.close();
		return null;
	}

	private BasicDynaBean getMatchBean(List<BasicDynaBean> listBean, Integer matchId){
		BasicDynaBean resultBean = null;
		for (BasicDynaBean bean: listBean){
			if(bean.get("insurance_category_id").equals(matchId)){
				  resultBean  = bean;
          break;
			}
		}
		return resultBean;
	}
	
	 private static final String TPAS_NAMESAND_iDS="select tpa_id,tpa_name,sponsor_type from tpa_master where status='A'  order by tpa_name";

	 public  List getTpasNamesAndIds() throws SQLException{
			   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(TPAS_NAMESAND_iDS));
		}
}
