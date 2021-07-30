/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CounterMaster.CounterMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi.p
 *
 */
public class ClaimReceiptsAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(ClaimReceiptsAction.class);

	static ClaimReceiptsDAO cdao = new ClaimReceiptsDAO();

	@IgnoreConfidentialFilters
	public ActionForward getScreen(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		setAttributes(req);
		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward getClaimReceipts(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		setAttributes(req);
		Map map = getParameterMap(req);
		PagedList list = ClaimReceiptsDAO.searchClaimReceipts(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public void setAttributes(HttpServletRequest req) throws SQLException {
		JSONSerializer js = new JSONSerializer().exclude("class");

		req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
				new InsuCompMasterDAO().listAll(null, "status", "A", "insurance_co_name"))));

		req.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_category_master").listAll(null, "status", "A", "category_name"))));

		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("tpa_master").listAll(null, "status", "A", "tpa_name"))));

		req.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		setAttributes(req);
		HttpSession session = req.getSession();

		// If User has no counter access then user cannot open claim receipt screen.
		boolean hasBillingCounter = false, hasPharmacyCounter = false;
		hasBillingCounter = (session.getAttribute("billingcounterId") != null && !((String)session.getAttribute("billingcounterId")).equals(""));
		hasPharmacyCounter = (session.getAttribute("pharmacyCounterId") != null && !((String)session.getAttribute("pharmacyCounterId")).equals(""));

		if (!hasBillingCounter && !hasPharmacyCounter) {
			ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
			FlashScope flash = FlashScope.getScope(req);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.error("Cannot create receipt. User has no counter access.");
			return redirect;
		}
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		setAttributes(req);
		BasicDynaBean bean = cdao.findByKey("receipt_no", req.getParameter("receipt_no"));
		req.setAttribute("bean", bean);

		CounterMasterDAO cmdao = new CounterMasterDAO();
		BasicDynaBean cntbean = cmdao.findByKey("counter_id", bean.get("counter"));
		String counterName = "";
		if (cntbean != null)
			counterName = (String)((String)cntbean.get("counter_no"));
		req.setAttribute("counterName", counterName);

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		HttpSession session = req.getSession();
		// If User has no counter access then user cannot create claim receipt.
		boolean hasBillingCounter = false, hasPharmacyCounter = false;
		hasBillingCounter = (session.getAttribute("billingcounterId") != null && !((String)session.getAttribute("billingcounterId")).equals(""));
		hasPharmacyCounter = (session.getAttribute("pharmacyCounterId") != null && !((String)session.getAttribute("pharmacyCounterId")).equals(""));

		if (!hasBillingCounter && !hasPharmacyCounter) {
			ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
			FlashScope flash = FlashScope.getScope(req);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.error("Cannot create receipt. User has no counter access.");
			return redirect;
		}

		String userid = (String)session.getAttribute("userid");
		Map<String, String[]> params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = cdao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if(req.getParameter("paymentDate") != null && !req.getParameter("paymentDate").equals("")){
			Timestamp paymentDateTime = new Timestamp(
						new DateUtil().getTimeStampFormatter().parse(req.getParameter("paymentDate")+" "+req.getParameter("paymentTime")).getTime());
			bean.set("display_date", paymentDateTime);
		}

		bean.set("mod_time", DateUtil.getCurrentTimestamp());
		bean.set("username", userid);

		if (errors.isEmpty()) {
			BasicDynaBean exists = cdao.findByKey("payment_reference", bean.get("payment_reference"));
			if (exists != null) {
				flash.info("Receipt: "+exists.get("receipt_no")+" exists for payment reference: "+bean.get("payment_reference"));
			} else {
				String receipt_no = cdao.getNextClaimReceiptNo();
				bean.set("receipt_no", receipt_no);
				boolean success = cdao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.info("Claim receipt created.."+receipt_no);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("receipt_no", bean.get("receipt_no"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to created claim receipt..");
				}
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		HttpSession session=req.getSession();
		String userid = (String)session.getAttribute("userid");
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map<String, String[]> params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = cdao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		if(req.getParameter("paymentDate") != null && !req.getParameter("paymentDate").equals("")){
			Timestamp paymentDateTime = new Timestamp(
						new DateUtil().getTimeStampFormatter().parse(req.getParameter("paymentDate")+" "+req.getParameter("paymentTime")).getTime());
			bean.set("display_date", paymentDateTime);
		}

		bean.set("mod_time", DateUtil.getCurrentTimestamp());
		bean.set("username", userid);

		Object key = req.getParameter("receipt_no");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("receipt_no", key.toString());
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			/*BasicDynaBean exists = cdao.findByKey("payment_reference", bean.get("payment_reference"));
			if (exists != null) {
				flash.info("Receipt: "+exists.get("receipt_no")+" exists for payment reference: "+bean.get("payment_reference"));
			}
			else {*/
				int success = cdao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
				} else {
					con.rollback();
					flash.error("Failed to update receipt details..");
				}
			//}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}

		redirect.addParameter("receipt_no", bean.get("receipt_no"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
