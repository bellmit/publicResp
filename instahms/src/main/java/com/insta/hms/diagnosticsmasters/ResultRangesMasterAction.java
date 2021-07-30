package com.insta.hms.diagnosticsmasters;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResultRangesMasterAction extends DispatchAction {
	 JSONSerializer js = new JSONSerializer().exclude("class");
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
		request.setAttribute("resultRanges", AddTestDAOImpl.getExistingLables(request.getParameter("test_id")));
		request.setAttribute("testDeatils", AddTestDAOImpl.getTestDetails(
				request.getParameter("test_id"), "GENERAL",request.getParameter("orgId")));
		request.setAttribute("resultLabels", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				ResultRangesDAO.listAllresultlblsForATest(request.getParameter("test_id")))));
		request.setAttribute("dynaResultLblList", ResultRangesDAO.listAllresultlblsForATest(request.getParameter("test_id")));
		request.setAttribute("loggedInCenter",RequestContext.getCenterId());
		return mapping.findForward("resultrangesscreen");
	}
	public ActionForward save(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
		Map requestMap = request.getParameterMap();
		BasicDynaBean rangesBean  = null;
		List errors = new ArrayList();
		GenericDAO rangesDAO =new GenericDAO("test_result_ranges");
		boolean success = true;
		List<BasicDynaBean> newResultrangesList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> existingResultRangesList = new ArrayList<BasicDynaBean>();
		String[] resultlabel_ids = (String[])requestMap.get("resultlabel_id");

		if(resultlabel_ids != null){
			for(int i = 0;i<resultlabel_ids.length;i++){
				rangesBean = rangesDAO.getBean();
				ConversionUtils.copyIndexToDynaBean(requestMap, i, rangesBean, errors,false );
				if(errors.isEmpty()){
					if(rangesDAO.findByKey("result_range_id", rangesBean.get("result_range_id")) == null){
						rangesBean.set("result_range_id", rangesDAO.getNextSequence());
						newResultrangesList.add(rangesBean);
					}
					else
						existingResultRangesList.add(rangesBean );
				}
			}
		}
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(!newResultrangesList.isEmpty())
				success = rangesDAO.insertAll(con, newResultrangesList);
			if(!existingResultRangesList.isEmpty()){
				for(BasicDynaBean existingRange : existingResultRangesList){
					success &= rangesDAO.update(con, existingRange.getMap(), "result_range_id", existingRange.get("result_range_id")) > 0;
				}
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("resultrangeslist"));
		redirect.addParameter("test_id", request.getParameterValues("test_id")[0]);
		redirect.addParameter("orgId", request.getParameterValues("orgId")[0]);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}
