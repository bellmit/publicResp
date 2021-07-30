package com.insta.hms.master.ReferalDoctor;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.ReferalDoctorApplicability.CenterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReferalDoctorAction extends AbstractDataHandlerAction {

private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {
		ReferalDoctorDAO dao = new ReferalDoctorDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.getReferalDoctorDetails(requestParams,
						ConversionUtils.getListingParameter(requestParams));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {
	  String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
	  if(StringUtil.isNullOrEmpty(countryCode)){
	    countryCode = centerMasterDAO.getCountryCode(0);
	  }
		req.setAttribute("defaultCountryCode", countryCode);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		ReferalDoctorDAO dao = new ReferalDoctorDAO();
		BasicDynaBean bean = dao.getBean();
		bean.set("created_timestamp",DateUtil.getCurrentTimestamp());
		ConversionUtils.copyToDynaBean(params, bean, errors);

		String referalMobilenoMobile = String.valueOf(bean.get("referal_mobileno"));
		String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
		List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				referalMobilenoMobile, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			bean.set("referal_mobileno", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			if (referalMobilenoMobile != null && !referalMobilenoMobile.equals("") && !referalMobilenoMobile.startsWith("+")) {
				bean.set("doctor_mobile", "+" + defaultCode + referalMobilenoMobile);
			}
		}

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("addRedirect"));

		if (errors.isEmpty()) {
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("referal_name", bean.get("referal_name"));
			keys.put("referal_mobileno", bean.get("referal_mobileno"));
			BasicDynaBean exists = dao.findByKey(keys);

			if (exists == null) {
				bean.set("referal_no", dao.getNextReferalId(con));
				bean.set("referal_doctor_area_id", getValue("area_id",params));
				bean.set("referal_doctor_city_id", getValue("city_id",params));
				boolean success = dao.insert(con, bean);

				CenterDAO referalCenterDAO = new CenterDAO();
				if (success) {
					BasicDynaBean cbean = referalCenterDAO.getBean();
					cbean.set("referal_center_id", referalCenterDAO.getNextSequence());
					cbean.set("referal_no", bean.get("referal_no"));
					cbean.set("center_id", 0);
					cbean.set("status", "A");
					success &= referalCenterDAO.insert(con, cbean);
				}

				if (success) {
					con.commit();
					flash.success("Referal Doctor inserted successfully..");
					redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("referal_no",bean.get("referal_no"));
					con.close();
					return redirect;
				}
			} else {
				flash.error("Referral name and mobile no already exists..");
				con.close();
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

		} else {
			flash.error("Incorrectly formatted values supplied..");
			con.close();
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		ReferalDoctorDAO dao = new ReferalDoctorDAO();
		BasicDynaBean bean = dao.getReferalDoctorDetails((String)req.getParameter("referal_no"));
		req.setAttribute("bean", bean);
		
		String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    if(StringUtil.isNullOrEmpty(countryCode)){
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    
		req.setAttribute("defaultCountryCode", countryCode);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
		
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		ReferalDoctorDAO dao = new ReferalDoctorDAO();
		BasicDynaBean bean = dao.getBean();
		bean.set("updated_timestamp",DateUtil.getCurrentTimestamp());
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Object key = req.getParameter("referal_no");

		String referalMobilenoMobile = String.valueOf(bean.get("referal_mobileno"));
		String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
		List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				referalMobilenoMobile, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			bean.set("referal_mobileno", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			if (referalMobilenoMobile != null && !referalMobilenoMobile.equals("") && !referalMobilenoMobile.startsWith("+")) {
				bean.set("doctor_mobile", "+" + defaultCode + referalMobilenoMobile);
			}
		}

		Map<String, String> keys = new HashMap<String, String>();
		keys.put("referal_no", key.toString());
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			Map<String,Object> keymap = new HashMap<String, Object>();
			keymap.put("referal_name", bean.get("referal_name"));
			keymap.put("referal_mobileno", bean.get("referal_mobileno"));
			BasicDynaBean exists = dao.findByKey(keymap);
			if(exists != null && !key.equals(exists.get("referal_no")) ) {
				flash.error("Referral name and mobile no already exists..");
			} else {
			    bean.set("referal_doctor_area_id", getValue("area_id",params));
			    bean.set("referal_doctor_city_id", getValue("city_id",params));
  				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Referal details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Referal details..");
				}
			}
		} else {
			flash.error("Incorectly formated values..");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("referal_no", req.getParameter("referal_no"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward checkUniqueRefLicenseNo(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException {

	    String refLicenseNo = req.getParameter("refLicenseNo");
	    String responseText = "false";

	    boolean exists = ReferalDoctorDAO.getDoctorLicenseNo(refLicenseNo);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

	private static TableDataHandler masterHandler = null;

	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"referral",		// table name
					new String[]{"referal_no"},	// keys
					new String[]{"referal_name", "referal_mobileno", "status",
						"payment_category", "payment_eligible", "referal_doctor_address",
						"referal_doctor_phone", "referal_doctor_email", "clinician_id"
					},
					new String[][]{
						// our field        ref table        ref table id field  ref table name field
						{"payment_category", "category_type_master", "cat_id", "cat_name"}
					},
					null
			);
		}

		masterHandler.setSequenceName("referal_id_sequence");
		masterHandler.setIdValAsString(true);
		masterHandler.setAlias("clinician_id", "license_number");
		return masterHandler;
	}

	
  private static String getValue(String key, Map params) {
    if (params == null) {
      return null;
    }
    Object[] obj = (Object[]) params.get(key);
    if (obj != null && obj[0] != null) {
      return obj[0].toString();
    }
    return null;
  }
}