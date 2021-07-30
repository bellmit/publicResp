package com.insta.hms.master.MiscellaneousSettings;


import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MiscellaneousSettingsAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(MiscellaneousSettingsAction.class);

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception{

		MiscellaneousSettingsDAO dao = new MiscellaneousSettingsDAO();
		BasicDynaBean bean = dao.getRecord();
		request.setAttribute("bean",bean);
		return mapping.findForward("addshow");
	}


	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse respnse)
		throws IOException, ServletException,Exception{
		FlashScope flash = FlashScope.getScope(request);
		Map params = request.getParameterMap();
		List error = new ArrayList();
		Connection con = null;

		String success = " Transaction Failure ...";
		String err = "";

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			MiscellaneousSettingsDAO dao = new MiscellaneousSettingsDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, error);
			if (error.isEmpty()){
				int count = dao.update(con, bean.getMap(),null);
				if (count >0){
					con.commit();
					success = "Miscellaneous Settings updated successfully";
					flash.put("success", success);
				}else{
					con.rollback();
					err = "Failed to update Pharmacy Settings ";
					flash.put("error", err);
				}
			}else {
				err = "incorrectly formated values supplied";
				flash.put("error", err);

			}

			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}

}

