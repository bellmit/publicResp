package com.insta.hms.master.PaymentPrecedenceSetting;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PaymentPrecedenceSettingAction extends DispatchAction {

	public ActionForward show(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws IOException, Exception, ServletException {

		PaymentPrecedenceSettingDAO dao = new PaymentPrecedenceSettingDAO();
		BasicDynaBean bean = dao.getRecord();
		req.setAttribute("bean", bean);

		req.setAttribute("method", "update");
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m,ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, Exception, ServletException {

		Map param = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;

		PaymentPrecedenceSettingDAO dao = new PaymentPrecedenceSettingDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(param, bean, errors);

		FlashScope fScope = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(errors.isEmpty()){
				int success = dao.update(con, bean.getMap(), null);
				if ((success > 0)){
					con.commit();
					fScope.success("Payment PrecedenceSetting updated successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					return redirect;
				}else {
					con.rollback();
					fScope.error("Failed to update Payment PrecedenceSetting..");
				}
			}else{
				fScope.error("Incorrectly formatted values supplied..");
			}

			return m.findForward("addshow");

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
