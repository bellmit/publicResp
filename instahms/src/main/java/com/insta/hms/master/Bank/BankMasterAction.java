package com.insta.hms.master.Bank;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BankMasterAction extends DispatchAction {
	BankMasterDAO dao = new BankMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f,HttpServletRequest request,
			HttpServletResponse response) throws ServletException , IOException , SQLException, ParseException {

		Map requestParams = request.getParameterMap();
		Map listingParams = ConversionUtils.getListingParameter(requestParams);

		PagedList pagedList = dao.getBankDetailPages(requestParams, listingParams);
		request.setAttribute("pagedList", pagedList);
		return m.findForward("list");

	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws ServletException , IOException , SQLException , ParseException {
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws ServletException , SQLException , IOException , ParseException {

			Map params = request.getParameterMap();
			List errors = new ArrayList();
			Connection con = null;
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			FlashScope flash = FlashScope.getScope(request);
			ActionRedirect redirect=null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				if(errors.isEmpty()) {
					BasicDynaBean  exists = dao.findByKey("bank_name", bean.get("bank_name"));
					if(exists == null) {
						bean.set("bank_id", dao.getNextSequence());
						boolean success = dao.insert(con, bean);
						if(success) {
							con.commit();
						    redirect = new ActionRedirect(m.findForward("showRedirect"));
						    flash.success("Bank master details inserted successfully..");
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							redirect.addParameter("bank_id", bean.get("bank_id"));
							return redirect;
						} else {
							con.rollback();
							flash.error("Failed to add  Bank Name..");
						}
					} else {
						flash.error("Bank name already exists..");
					}
				} else {
					flash.error("Incorrectly formatted values supplied");
				}
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;

			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
	}

	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws ServletException , SQLException , IOException , ParseException {

			Connection con = null;
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			Integer key = Integer.parseInt(request.getParameter("bank_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("bank_id", key);
			FlashScope flash = FlashScope.getScope(request);
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (errors.isEmpty()) {
					DynaBean exists = dao.findByKey("bank_name", bean.get("bank_name"));
					if (exists != null && !key.equals(exists.get("bank_id"))) {
						flash.error("Bank name already exists..");
					}
					else {
						int success = dao.update(con, bean.getMap(), keys);

						if (success > 0) {
							con.commit();
							flash.success("Bank master details updated successfully..");
						} else {
							con.rollback();
							flash.error("Failed to update Bank master details..");
						}
					}
				}
				else {
					flash.error("Incorrectly formatted values supplied");
				}
				ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("bank_id", bean.get("bank_id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;

			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		}


	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws ServletException , IOException , SQLException , ParseException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean bean = dao.findByKey("bank_id", Integer.parseInt(request.getParameter("bank_id")));
		request.setAttribute("bean", bean);
		request.setAttribute("BankDetails", js.serialize(dao.getBankNamesAndIds()));

		return m.findForward("addshow");
	}
}