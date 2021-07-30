package com.insta.hms.master.Accounting.vouchertemplates;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VoucherTemplateAction extends DispatchAction{

	private VoucherTemplateDAO dao=new VoucherTemplateDAO();
	public ActionForward list(ActionMapping map,ActionForm from,
			HttpServletRequest request,HttpServletResponse response)throws Exception,ServletException{

		request.setAttribute("acc_vc_templates", dao.listAll());
		return map.findForward("list");

	}

	public ActionForward show(ActionMapping map,ActionForm from,HttpServletRequest request,HttpServletResponse response)
			throws ServletException, IOException, IllegalArgumentException, SQLException{

		String reqVcType = request.getParameter("voucher_type");

		if (reqVcType == null || reqVcType.equals(""))
			throw new IllegalArgumentException("Voucher Template Type is null");

		VoucherTemplate enumVcTemplate=null;
		for (VoucherTemplate voucherTemplate: VoucherTemplate.values()) {
			if (voucherTemplate.getFtlType().equals(reqVcType))
				enumVcTemplate = voucherTemplate;
		}

		if (enumVcTemplate == null)
			throw new IllegalArgumentException("Accounting Voucher Type does not exists : "+reqVcType);

		Boolean customized = new Boolean(request.getParameter("customized"));
		String templateContent = null;
		String templateReason=null;

		if (customized) {
			BasicDynaBean templateBean = dao.findByKey("voucher_type", enumVcTemplate.getFtlType());
			templateContent = (String) templateBean.get("template_content");
			templateReason = (String)templateBean.get("reason");
		} else {
			String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates/accounting");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + enumVcTemplate.getFtlFileName()+ ".ftl");
			templateContent = new String(DataBaseUtil.readInputStream(stream));
		}
		request.setAttribute("template_content", templateContent);
		request.setAttribute("title", request.getParameter("title"));
		request.setAttribute("reason", templateReason);
		return map.findForward("show");
	}

	public ActionForward update(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws ServletException,IOException,SQLException{
			String voucher_type=request.getParameter("voucher_type");
			if(voucher_type==null || voucher_type.equals("")){
					throw new IllegalArgumentException("Accounting Voucher Template type is null");
			}
		Map params=request.getParameterMap();
		BasicDynaBean bean=dao.getBean();
		List errors=new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean,errors);
		bean.set("username",request.getSession(false).getAttribute("userid"));
		bean.set("mode_time", DateUtil.getCurrentTimestamp());
		ActionRedirect redirect=null;
		Boolean resetToDefault=new Boolean(request.getParameter("resetToDefault"));
		if(resetToDefault)
			bean.set("template_content", "");
		String error=null;
		String msg=null;

		if(errors.isEmpty()){
			Connection con=null;
			try{
				con=DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if(dao.update(con, bean.getMap(), "voucher_type",voucher_type)==1)
						msg="Voucher Template saved successfully";
				else
						error="Fail to save the Voucher Template";
			}finally{
				if(msg!=null){
					con.commit();
				}else{
					con.rollback();

				}
				DataBaseUtil.closeConnections(con, null);
			}
		}else{
			error="Incorrectly formated details supplied..";
		}

		FlashScope flash=FlashScope.getScope(request);
		flash.put("success", msg);
		flash.put("error", error);
		if(msg!=null){
			if(resetToDefault)
				flash.put("success", "Template reset to default successfully..");

			redirect=new ActionRedirect(mapping.findForwardConfig("showRedirect"));

		}else{
			redirect=new ActionRedirect(mapping.findForwardConfig("showRedirect"));
		}

		redirect.addParameter("title", request.getParameter("title"));
		redirect.addParameter("voucher_type", voucher_type);
		redirect.addParameter("customized",request.getParameter("customized"));
		request.setAttribute("reason", request.getParameter("reason"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
