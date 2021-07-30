package com.insta.hms.master.DiagCenterResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.diagnosticsmasters.addtest.TestBO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mohammed.r
 *
 */

public class DiagResultsCenterApplicability extends DispatchAction {

	DiagResultsCenterApplicabilityDAO diagResultsCenterDao = new DiagResultsCenterApplicabilityDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String test_id = request.getParameter("test_id");
		request.setAttribute("dynaResultLblList",
				ResultRangesDAO.resultsforTest(request.getParameter("test_id")));
		request.setAttribute("applicable_centers", diagResultsCenterDao
				.getCenters(request.getParameter("test_id")));
		request.setAttribute("applicable_centers_json", js
				.deepSerialize(ConversionUtils.listBeanToMapListMap(
						diagResultsCenterDao.getCentersJson(request
								.getParameter("test_id")), "resultlabel")));
		request.setAttribute("testDeatils", AddTestDAOImpl.getTestDetails(
				request.getParameter("test_id"), "GENERAL",
				request.getParameter("orgId")));
		// request.setAttribute("dynaResultLblList",
		// ResultRangesDAO.listAllresultlblsForATest(request.getParameter("test_id")));

		List centers = CenterMasterDAO.getCentersList();
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils
				.copyListDynaBeansToMap(new CityMasterDAO()
						.listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils
				.copyListDynaBeansToMap(centers)));
		request.setAttribute("results_json", js.deepSerialize(ConversionUtils
				.copyListDynaBeansToMap(diagResultsCenterDao
						.getResultsListForJson(test_id))));
		request.setAttribute("expression_JSON", js
				.deepSerialize(ConversionUtils
						.copyListDynaBeansToMap(diagResultsCenterDao
								.getExpressionForTest(test_id))));

		return mapping.findForward("diag_result_applicability");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String resultLabelId = request.getParameter("resultlabel_id");
		String test_id = request.getParameter("test_id");
		String orgId = request.getParameter("orgId");
		int resultId;
		StringBuilder msg = new StringBuilder();
		
		try {
          resultId= Integer.parseInt(resultLabelId);
        } catch (NumberFormatException e) {
          throw new NumberFormatException("resultlabel_id value is invalid: " + resultLabelId);
        }
		Object[] queryParams = new Object[] {test_id, resultId};
		String isHavingExp = DataBaseUtil.getStringValueFromDb(
		    "SELECT expr_4_calc_result from test_results_master WHERE test_id=? and resultlabel_id=?", queryParams);
		    
		String resultlabel = DataBaseUtil.getStringValueFromDb(
		    "SELECT case when trm.method_id is not null then resultlabel|| '.' ||method_name "
						+ " else resultlabel end as resultlabel FROM test_results_master trm "
						+ " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
						+ " WHERE test_id=? and resultlabel_id=?", queryParams);
		
		
		String error = null;
        Connection con = null;
		if(!"".equals(isHavingExp)) {
		  isHavingExp = resultlabel+ "=" +isHavingExp;
  		}
		
		TestBO test = new TestBO();
		
		try {
			txn: {
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				String app_for_centers = request
						.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = diagResultsCenterDao.getBean();
						
						if (!diagResultsCenterDao.delete(con, resultId)) {
							error = "Failed to delete diag results association of few centers..";
							break txn;
						}

						if (diagResultsCenterDao.findByKey(con,
								"resultlabel_id", resultId) == null) {
							bean.set("result_center_id",
									diagResultsCenterDao.getNextSequence());
							bean.set("resultlabel_id", resultId);
							bean.set("center_id", 0);
							bean.set("status", "A");
							if (!diagResultsCenterDao.insert(con, bean)) {
								error = "Failed to update diag results association for all centers..";
								break txn;
							}
						}
					} else {
						
						if (!diagResultsCenterDao.delete(con, 0, resultId)) {
							error = "Failed to delete diag results association for all centers..";
							break txn;
						}						
						BasicDynaBean bean = diagResultsCenterDao.getBean();
						String[] totalcenterIds = request.getParameterValues("center_id");
						String[] result_center_id = request
								.getParameterValues("result_center_id");
						String[] diag_center_delete = request
								.getParameterValues("cntr_delete");
						String[] diag_center_edited = request
								.getParameterValues("cntr_edited");
						String[] center_statuses = request
								.getParameterValues("center_status");

						boolean success =true;						
						for (int i = 0; i < totalcenterIds.length-1; i++) {
							if(result_center_id[i].equals("_")) {
							bean.set("resultlabel_id", resultId);
							bean.set("center_id",
									Integer.parseInt(totalcenterIds[i]));
							bean.set("status", center_statuses[i]);
							bean.set("result_center_id",
									diagResultsCenterDao.getNextSequence());
								if (!diagResultsCenterDao.insert(con, bean)) {
									error = "Failed to insert the  diag results association for selected centers..";
									break txn;
								}
						} else if (new Boolean(diag_center_delete[i])) {
							if (!(success && diagResultsCenterDao.delete(con,"result_center_id",Integer.parseInt(result_center_id[i])))) {
									error = "Failed to delete the diag results association for selected center..";
									break txn;
							}
						} else if (new Boolean(diag_center_edited[i])) {
							bean.set("status", center_statuses[i]);
							if (diagResultsCenterDao.update(con,
									bean.getMap(), "result_center_id",
									Integer.parseInt(result_center_id[i])) != 1) {
									error = "Failed to update the diag results association for selected center..";
									break txn;
								}
							}
						}
					}
				}
				if (!test.resultsCenterApplicabilityCheck(con, test_id, msg)) {
					error = "Failed to change the diag results association for selected center..";
				}					
			}
		} finally {
			DataBaseUtil.commitClose(con, error == null);
		}
	
		ActionRedirect redirect = new ActionRedirect(
				mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		flash.put("error", error);
		redirect.addParameter("resultlabel_id", resultId);
		redirect.addParameter("test_id", test_id);
		redirect.addParameter("orgId", orgId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward getCentersRequest(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ParseException,
			IOException {
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class").exclude(
				"dynaClass");
		Connection con = null;

		String test_id = request.getParameter("test_id");
		String resultLabel = request.getParameter("resultLabel");
		String methodName = request.getParameter("methodname");
		String methodology = "";
		if (!"undefined".equals(methodName)) {
			methodology = methodName;
		}

		List listofMapsCenters = new ArrayList();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			List listofCenters = diagResultsCenterDao.getCentersRequest(con,
					resultLabel, methodology, test_id);
			listofMapsCenters = ConversionUtils
					.copyListDynaBeansToMap(listofCenters);
			response.getWriter().write(js.deepSerialize(listofMapsCenters));
			response.flushBuffer();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return null;
	}

}
