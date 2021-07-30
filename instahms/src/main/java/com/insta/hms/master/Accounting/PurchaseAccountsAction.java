/**
 *
 */
package com.insta.hms.master.Accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.StoresMaster.CategoryMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class PurchaseAccountsAction extends DispatchAction {

	CategoryMasterDAO categoryDao = new CategoryMasterDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		PagedList pagedList = categoryDao.list1(request.getParameterMap(),
				ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);
		return mapping.findForward("list");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String catId = request.getParameter("category_id");
		if (catId != null && !catId.equals(""))
			request.setAttribute("category", categoryDao.findByKey("category_id", Integer.parseInt(catId)));
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		Map params = request.getParameterMap();
		BasicDynaBean bean = categoryDao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		String success = null;
		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (categoryDao.update(con, bean.getMap(), "category_id", bean.get("category_id")) != 0)
					success = "Account Head updated successfully..";
				else
					error = "Failed to update Account Head";
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted details supplied";
		}
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));;

		if (success != null) {
			flash.success(success);
		} else {
			flash.error(error);
		}
		redirect.addParameter("category_id", bean.get("category_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
