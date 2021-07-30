/**
 *
 */
package com.insta.hms.master.Accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

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
public class PartyAccountNamesAction extends DispatchAction{

	GenericDAO partyacoountdao = new GenericDAO("hosp_party_account_names");

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("partyaccountnames", partyacoountdao.getRecord().getMap());
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = partyacoountdao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		String success = null;
		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
				if (partyacoountdao.update(con, bean.getMap(), null) > 0)
					success = "Party Account Names Updated Successfully..";
				else error = "Failed to Update the Party Account Names..";
			} finally {
				if (success != null) {
					con.commit();
				} else {
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		FlashScope flash = FlashScope.getScope(request);

		ActionRedirect redirect = null;
		if (success != null) {
			flash.put("success", success);
			redirect = new ActionRedirect(mapping.findForward("prefRedirect"));
		} else {
			flash.put("error", error);
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
