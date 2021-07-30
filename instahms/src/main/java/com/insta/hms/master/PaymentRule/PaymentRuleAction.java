package com.insta.hms.master.PaymentRule;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PaymentRuleAction extends DispatchAction {

	public enum ActionType {
		UPDATE, CREATE;
	}

	static Logger log = LoggerFactory.getLogger(PaymentRuleAction.class);

  private static final String GET_CATEGORY_DETAILS = 
      "select cat_id,cat_name from category_type_master";

  public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		PaymentRuleDAO dao = new PaymentRuleDAO();
		PagedList pagedList = dao.getPaymentRuleDetails(req.getParameterMap(),
				ConversionUtils.getListingParameter(req.getParameterMap()));

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("laboratoryTestDetails", js.serialize(PaymentRuleDAO.paymentRuleLabTests()));
		req.setAttribute("radiologyTestDetails", js.serialize(PaymentRuleDAO.paymentRuleRadTests()));
		req.setAttribute("serviceDetails", js.serialize(PaymentRuleDAO.paymentRuleServices()));
		req.setAttribute("organizationDetails", OrgMasterDao.getAllOrgIdNames());
		req.setAttribute("categoryDetails", DataBaseUtil.queryToDynaList(GET_CATEGORY_DETAILS));
		req.setAttribute("packageDetails", js.serialize(PackageDAO.getAllPackages()));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward addShow(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		String paymentIdValue = req.getParameter("payment_id");
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("organizationDetails", OrgMasterDao.getAllOrgIdNames());
		req.setAttribute("categoryDetails", DataBaseUtil.queryToDynaList(GET_CATEGORY_DETAILS));
		req.setAttribute("laboratoryTestDetails", js.serialize(AddTestDAOImpl.getAllLaboratoryTests()));
		req.setAttribute("radiologyTestDetails", js.serialize(AddTestDAOImpl.getAllRadiolodyTests()));
		req.setAttribute("packageDetails", js.serialize(PackageDAO.getAllPackages()));
		MasterServicesDao dao = new MasterServicesDao();
		req.setAttribute("serviceDetails", js.serialize(dao.getAllServiceNames()));
		req.setAttribute("chargeHeads", js.serialize(new BillBO().getChargeHeadConstNames()));
		req.setAttribute("chargeGroups", ChargeHeadsDAO.getPayableChargeGroups());

		if (paymentIdValue != null && !paymentIdValue.equals("")) {
			int paymentId = Integer.parseInt(paymentIdValue);
			PaymentRuleDAO paymentDAO = new PaymentRuleDAO();
			BasicDynaBean bean = paymentDAO.getPaymentRecord(paymentId);
			req.setAttribute("bean", bean);
		}
		HashMap<String, Object> exprTokens = new LinkedHashMap<String, Object>();
		PaymentEngine.putPaymentExprParams(exprTokens, null, null, null, null, null, null);
		req.setAttribute("exprTokens", exprTokens.keySet());

		return m.findForward("addShow");
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, SQLException {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		HttpSession session = req.getSession();
		String userName = (String) session.getAttribute("userid");

		String centerId = req.getParameter("center_id");
		centerId = (centerId == null || centerId.isEmpty()) ? "*" : centerId;

		
		PaymentRuleDAO dao = new PaymentRuleDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope fScope = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());

		List<String> errorStrings = new PaymentRuleActionValidator(ActionType.CREATE, req).getErrorList();
		if(CollectionUtils.isNotEmpty(errorStrings)){
			fScope.error(StringUtils.join(errorStrings,"<br/>"));
			return redirect;
		}

		bean.set("username", userName);
		bean.set("center_id", centerId);

		try {
			if (errors.isEmpty()) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				int dupPriority = dao.duplicatePaymentRule(bean);

				if (dupPriority == 0) {
					bean.set("payment_id", DataBaseUtil.getNextSequence("payment_rules_seq"));
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						fScope.success("Payment Rule inserted successfully..");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
						redirect.addParameter("payment_id", bean.get("payment_id"));
						con.close();
						return redirect;
					} else {
						fScope.error("Failed to insert Payment Rule Details..");
						redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
						con.close();
						return redirect;
					}
				} else {
					fScope.error("A payment rule with the same condition exists at priority: " + dupPriority);
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					con.close();
					return redirect;
				}
			} else {
				fScope.error("Incorectly formated values supplied..");
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			JSONSerializer js = new JSONSerializer().exclude("class");
			fScope.error("Precedance Value already exists..");
			redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		return redirect;
	}

	public ActionForward edit(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		String paymentIdValue = req.getParameter("payment_id");
		int paymentId = Integer.parseInt(paymentIdValue);

		PaymentRuleDAO dao = new PaymentRuleDAO();
		BasicDynaBean bean = dao.getPaymentRecord(paymentId);
		req.setAttribute("bean", bean);

		HashMap<String, Object> exprTokens = new LinkedHashMap<String, Object>();
		PaymentEngine.putPaymentExprParams(exprTokens, null, null, null, null, null, null);
		req.setAttribute("exprTokens", exprTokens.keySet());

		return m.findForward("show");
	}

	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		HttpSession session = req.getSession();
		String userName = (String) session.getAttribute("userid");
		Connection con = null;
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("payment_id", req.getParameter("payment_id"));
		List<String> errorStrings = new PaymentRuleActionValidator(ActionType.UPDATE, req).getErrorList();
		if (CollectionUtils.isNotEmpty(errorStrings)) {
			flash.error(StringUtils.join(errorStrings, "<br/>"));
			return redirect;
		}

		Map param = req.getParameterMap();
		List errors = new ArrayList();

		PaymentRuleDAO dao = new PaymentRuleDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(param, bean, errors);
		bean.set("username", userName);

		String[] payees = new String[3];
		payees[0] = req.getParameter("firstPayee");
		payees[1] = req.getParameter("secondPayee");
		payees[2] = req.getParameter("thirdPayee");

		for (int j = 0; j < payees.length; j++) {
			if (payees[j].equals("dr_payment")) {
				if (!req.getParameter("dr_payment_value").isEmpty()) {
					bean.set(payees[j] + "_value", new BigDecimal(req.getParameter("dr_payment_value")));
				} else {
					bean.set(payees[j] + "_option", "1");
					bean.set(payees[j] + "_value", new BigDecimal(0));
				}
			}
			if (payees[j].equals("ref_payment")) {
				if (!req.getParameter("ref_payment_value").isEmpty()) {
					bean.set(payees[j] + "_value", new BigDecimal(req.getParameter("ref_payment_value")));
				} else {
					bean.set(payees[j] + "_option", "1");
					bean.set(payees[j] + "_value", new BigDecimal(0));
				}
			}
			if (payees[j].equals("presc_payment")) {
				if (!req.getParameter("presc_payment_value").isEmpty()) {
					bean.set(payees[j] + "_value", new BigDecimal(req.getParameter("presc_payment_value")));
				} else {
					bean.set(payees[j] + "_option", "1");
					bean.set(payees[j] + "_value", new BigDecimal(0));
				}
			}
		}

		// bean.set("precedance", new Integer(req.getParameter("precedance")));

		Object key = req.getParameter("payment_id");
		Map keys = new HashMap();
		keys.put("payment_id", Integer.parseInt(key.toString()));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Payment Rule details updated successfully...");

				} else {
					flash.error("Failed to update payment rule details...");
				}
			} else {

			}
		} catch (Exception e) {
			flash.error("Duplicate Precedence value...");
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		return redirect;
	}

	public ActionForward reorderPrecedenceValues(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException, Exception {

		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		PaymentRuleDAO dao = new PaymentRuleDAO();
		boolean status = dao.updatePrecedenceValues();
		if (status) {
			flash.success("Priorities renumbered successfully...");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} else {
			flash.error("failed to renumber priorities...");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

	}

	public ActionForward deletePaymentRule(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException, Exception {

		Connection con = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			PaymentRuleDAO dao = new PaymentRuleDAO();
			String priority = req.getParameter("priorityValue");

			ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
			FlashScope flash = FlashScope.getScope(req);

			boolean status = dao.delete(con, "precedance", Integer.parseInt(priority));
			if (status) {
				con.commit();
				flash.success("Payment Rule deleted successfully...");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			} else {
				con.rollback();
				flash.error("Failed to delete Payment Rule...");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward getPrecedance(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		String chargeId = request.getParameter("chargeId");
		String precedence = "";
		boolean allStarRule = new Boolean(request.getParameter("all"));
		Integer intVal = PaymentRuleDAO.getPrecedance(allStarRule, chargeId);
		if (intVal != null)
			precedence = intVal.toString();

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(precedence);
		return null;
	}

}
