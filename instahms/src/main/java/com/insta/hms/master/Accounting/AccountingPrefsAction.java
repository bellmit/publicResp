/**
 *
 */
package com.insta.hms.master.Accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;

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
public class AccountingPrefsAction extends DispatchAction{

	AccountingPrefsDAO accprefdao = new AccountingPrefsDAO();

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		request.setAttribute("accounting_prefs", accprefdao.getRecord());
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = accprefdao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		String success = null;
		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (accprefdao.update(con, bean.getMap(), null) > 0)
					success = "Prefereces updated successfully..";
				else error = "Failed to update Preferences..";
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}

		} else {
			error = "Incorrectly formatted details supplied..";
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("prefRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}
