package com.insta.hms.master.TpaMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.TpaMasterCenterAssociation.CenterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class TpaMasterAction extends DispatchAction{
  
  private static final GenericDAO insuranceClaimTemplateDAO =
      new GenericDAO("insurance_claim_template");
  
  private static final GenericDAO healthAuthorityMasterDAO =
      new GenericDAO("health_authority_master");
  private static final GenericDAO haTpaCodeDAO = new GenericDAO("ha_tpa_code");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer json = new JSONSerializer().exclude("class");
		TpaMasterDAO dao = new TpaMasterDAO();
		Map map = req.getParameterMap();
		PagedList pagedList = dao.getTpaMasterList(map, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("tpaMastersLists", json.serialize(dao.getActiveTpasNamesAndIds()));
		req.setAttribute("sponsor_type_id",req.getParameter("sponsor_type_id"));
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
			JSONSerializer json = new JSONSerializer().exclude("class");
			CityMasterDAO dao = new CityMasterDAO();
			ArrayList city =  (ArrayList)dao.getAvalCitynames();
			req.setAttribute("cityList", json.serialize(city));

			List<String> columns = new ArrayList<String>();
			columns.add("claim_template_id");
			columns.add("template_name");

			List claimForms = insuranceClaimTemplateDAO.listAll(columns, "status", "A");
			req.setAttribute("claimForms", claimForms);
			req.setAttribute("healthAuthorities", healthAuthorityMasterDAO.listAll());

			return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		TpaMasterDAO dao = new TpaMasterDAO();
		String tpaId = null;
		BasicDynaBean bean = dao.getBean();
		bean.set("created_timestamp",DateUtil.getCurrentTimestamp());
		bean.set("tpa_member_id_validation_type",req.getParameter("member_id_validation_status"));
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if(!req.getParameter("validityEnd_date").equals("")){
			Date startdate = new Date(new DateUtil().getDateFormatter().parse(req.getParameter("validityEnd_date")).getTime());
			bean.set("validity_end_date", startdate);
		}

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("tpa_name", bean.get("tpa_name"));
			if (exists == null) {
				tpaId = dao.getNextTPAId();
				bean.set("tpa_id", tpaId);
				boolean success = dao.insert(con, bean);

				CenterDAO tpaCenterDao = new CenterDAO();
				if (success) {
					BasicDynaBean cbean = tpaCenterDao.getBean();
					cbean.set("tpa_center_id", tpaCenterDao.getNextSequence());
					cbean.set("tpa_id", bean.get("tpa_id"));
					cbean.set("center_id", -1);
					cbean.set("status", "A");
					success &= tpaCenterDao.insert(con, cbean);
				}

				if(success) {
					success  = saveOrUpdateHealthAuthorityCodes((String)bean.get("tpa_id"), con, req);
				}

				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("TPA master details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("tpa_id", bean.get("tpa_id"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add  TPA..");
				}
			} else {
				flash.error("TPA name already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer json = new JSONSerializer().exclude("class");
		TpaMasterDAO dao = new TpaMasterDAO();
		CityMasterDAO cityDao = new CityMasterDAO();
		BasicDynaBean bean = dao.findByKey("tpa_id", req.getParameter("tpa_id"));
		req.setAttribute("bean", bean);
		ArrayList city =  (ArrayList)cityDao.getAvalCitynames();
		req.setAttribute("cityList", json.serialize(city));
		req.setAttribute("tpaMastersLists", json.serialize(dao.getTpasNamesAndIds()));


		List<String> columns = new ArrayList<String>();
		columns.add("claim_template_id");
		columns.add("template_name");

		List claimForms = insuranceClaimTemplateDAO.listAll(columns, "status", "A");
		req.setAttribute("claimForms", claimForms);
		req.setAttribute("healthAuthorities", healthAuthorityMasterDAO.listAll());
		req.setAttribute("healthAuthorityCodes", haTpaCodeDAO.listAll(null, "tpa_id", req.getParameter("tpa_id"),"tpa_id"));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		TpaMasterDAO dao = new TpaMasterDAO();
		BasicDynaBean bean = dao.getBean();
		bean.set("updated_timestamp",DateUtil.getCurrentTimestamp());
		bean.set("tpa_member_id_validation_type",req.getParameter("member_id_validation_status"));

		ConversionUtils.copyToDynaBean(params, bean, errors);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("tpa_id", bean.get("tpa_id"));
		FlashScope flash = FlashScope.getScope(req);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if(!req.getParameter("validityEnd_date").equals("")){
			Date startdate = new Date(new DateUtil().getDateFormatter().parse(req.getParameter("validityEnd_date")).getTime());
			bean.set("validity_end_date", startdate);
		}else{
			bean.set("validity_end_date", null);
		}

		Object key = req.getParameter("tpa_id");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("tpa_id", key.toString());

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("tpa_name", bean.get("tpa_name"));
			if (exists != null && !key.equals(exists.get("tpa_id"))) {
				flash.error("TPA name already exists..");
			}
			else {

				if (bean.get("status") != null && ((String)bean.get("status")).equals("I")) {
					List<BasicDynaBean> patCatDependants = getPatientCategoryDependants((String)key);
					if (patCatDependants != null && patCatDependants.size() > 0) {
						flash.error("Cannot mark TPA as inactive. <br/>" +
								" One (or) more Patient Category TPAs (OP Allowed sponsors/IP Allowed sponsors) are linked with this TPA.");
						return redirect;
					}

					List<BasicDynaBean> compTPADependants = getCompanyTPADependants((String)key);
					if (compTPADependants != null && compTPADependants.size() > 0) {
						flash.error("Cannot mark TPA as as inactive. <br/>" +
								" One (or) more Insurance Company TPAs are linked with this TPA.");
						return redirect;
					}
				}

				int success = dao.update(con, bean.getMap(), keys);

				if(success > 0) {
					success = !saveOrUpdateHealthAuthorityCodes((String)bean.get("tpa_id"), con, req) ? 0 : 1;
				}

				if (success > 0) {
					con.commit();
					flash.success("TPA master details updated successfully..");


				} else {
					con.rollback();
					flash.error("Failed to update TPA master details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public static final String GET_PATIENT_CATEGORY_DEPENDANTS =
		" SELECT ip_allowed_sponsors, op_allowed_sponsors, status FROM patient_category_master"
		+ " WHERE status = 'A'"
		+ " AND ("
		+ "   (ip_allowed_sponsors IS NOT NULL AND ip_allowed_sponsors != '*' AND ? = any(string_to_array(replace(ip_allowed_sponsors, ' ', ''), ',')))"
		+ "   OR "
		+ "   (op_allowed_sponsors IS NOT NULL AND op_allowed_sponsors != '*' AND ? = any(string_to_array(replace(op_allowed_sponsors, ' ', ''), ',')))"
		+ " )";

	private List<BasicDynaBean> getPatientCategoryDependants(String tpaId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PATIENT_CATEGORY_DEPENDANTS, new Object[]{tpaId, tpaId});
	}

	public static final String GET_COMPANY_TPA_DEPENDANTS =
		" SELECT * FROM insurance_company_tpa_master WHERE tpa_id = ? ";

	private List<BasicDynaBean> getCompanyTPADependants(String tpaId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_COMPANY_TPA_DEPENDANTS, new Object[]{tpaId});
	}

	private boolean saveOrUpdateHealthAuthorityCodes(String tpa_id, Connection con,HttpServletRequest request)
	throws SQLException, IOException{

		boolean flag = true;
		String [] healthAuth = request.getParameterValues("h_health_authority");
		String [] tpaCodes = request.getParameterValues("h_tpa_code");
		String [] h_ha_tpa_code_id = request.getParameterValues("h_ha_tpa_code_id");
		String [] hacodeoldrnew = request.getParameterValues("hacodeoldrnew");
		String [] delete = request.getParameterValues("h_ha_deleted");
		String[] eligibilityAuthorization = request.getParameterValues("h_eligibility_authorization");
		String[] enableEligibilityAuthInXml = request.getParameterValues("h_eligibility_authorization_in_xml");


		if(healthAuth != null){
			for(int i=0; i<healthAuth.length; i++){
				BasicDynaBean bean  = haTpaCodeDAO.getBean();
				bean.set("tpa_id", tpa_id);
				bean.set("health_authority", healthAuth[i]);
				bean.set("enable_eligibility_authorization", Boolean.parseBoolean(eligibilityAuthorization[i]));
				bean.set("enable_eligibility_auth_in_xml", enableEligibilityAuthInXml[i]);
				bean.set("tpa_code", tpaCodes[i]);
				if (hacodeoldrnew[i].equalsIgnoreCase("new") && delete[i].equalsIgnoreCase("false")) {
					bean.set("ha_tpa_code_id", haTpaCodeDAO.getNextSequence());
					flag = haTpaCodeDAO.insert(con, bean);
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("false")) {
					if(h_ha_tpa_code_id != null && h_ha_tpa_code_id[i] != null && !h_ha_tpa_code_id[i].isEmpty()) {
						Map<String, Integer> keys = new HashMap<String, Integer>();
						bean.set("ha_tpa_code_id", Integer.parseInt(h_ha_tpa_code_id[i]));
						keys.put("ha_tpa_code_id", Integer.parseInt(h_ha_tpa_code_id[i]));

						if(flag)
							flag = haTpaCodeDAO.update(con, bean.getMap(), keys) > 0;
					}
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("true")) {
					if(h_ha_tpa_code_id != null && h_ha_tpa_code_id[i] != null && !h_ha_tpa_code_id[i].isEmpty()) {
						if (flag) flag = haTpaCodeDAO.delete(con, "ha_tpa_code_id", Integer.parseInt(h_ha_tpa_code_id[i]));
					}
				}

			}

		}
		return flag;
	}

}