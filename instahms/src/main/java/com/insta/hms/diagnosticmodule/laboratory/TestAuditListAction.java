package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class TestAuditListAction extends DispatchAction {

  @IgnoreConfidentialFilters
	public ActionForward getTestListScreen(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,Exception{

		String category = mapping.getProperty("category");
		JSONSerializer json = new JSONSerializer();
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		request.setAttribute("inHouses", json.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		List outHouses = OutHouseMasterDAO.getAllOutSources();
		request.setAttribute("outHouses", json.serialize(outHouses));
		request.setAttribute("DiagArraylist", DiagnosticDepartmentMasterDAO.getDepts(category));
		request.setAttribute("module", category);
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		request.setAttribute("category", category);
		return mapping.findForward("showTestList");
	}

  @IgnoreConfidentialFilters
	public ActionForward getTestList(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,Exception{

		HttpSession session = request.getSession(false);

		LaboratoryForm dForm = (LaboratoryForm)form;
		LaboratoryDAO dao = new LaboratoryDAO();

		String mrno = null;
		String department = null;
		String userDept = null;
		String testName = null;
		String userId =(String)session.getAttribute("userid");


		mrno = request.getParameter("mrno");
		if(mrno !=null && mrno.equals("") ){mrno = null;}

		userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
  	    department = request.getParameter("department");

  		if(department != null && department.equals("")){department = null;}
		if(department == null){if(userDept!=null &&  !userDept.equals("")) department = userDept;}

		if(dForm.getDiagname() != null && !dForm.getDiagname().equals("")){testName = dForm.getDiagname();}

		int pageNum = 1;
		String page = request.getParameter("pageNum");
		if(page !=null && !page.equals("") ){pageNum = Integer.parseInt(page);}

		ArrayList patient = null;
		if (!dForm.getPatientAll()) {
			patient = new ArrayList();
			if (dForm.getPatientIp()) patient.add(Bill.BILL_VISIT_TYPE_IP);
			if (dForm.getPatientOp()) patient.add(Bill.BILL_VISIT_TYPE_OP);
			if (dForm.getPatientRetail()) patient.add(Bill.BILL_VISIT_TYPE_RETAIL);
		}

		ArrayList testStatus = null;
		if (!dForm.getTestStatusAll()) {
			testStatus = new ArrayList();
			if (dForm.getTestPrescribed()) testStatus.add("N");
			if (dForm.getTestPartialConducted()) testStatus.add("P");
			if (dForm.getTestConducted()) testStatus.add("C");
			if (dForm.getTestCancelled()) testStatus.add("X");
			if (dForm.isTestPrescribedNoResults()) testStatus.add("NRN");
			if (dForm.isTestConductedNoResults()) testStatus.add("CRN");
		}

		java.sql.Date fromDate = DataBaseUtil.parseDate(dForm.getFdate());
		java.sql.Date toDate = DataBaseUtil.parseDate(dForm.getTdate());

		 String formSort = request.getParameter("sortOrder");
		 if(formSort != null){
			if(formSort.equals("mrno")){formSort ="tp.mr_no";}
			if(formSort.equals("testname")){formSort = "D.test_name";}
		 }

		 String category = request.getParameter("category");
		 if (category != null){request.setAttribute("category", category);}

		 BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		 request.setAttribute("DiagArraylist", DiagnosticDepartmentMasterDAO.getDepts(category));

		 String showOnlyouthouseTestsStr = request.getParameter("showOnlyouthouseTests");
		 boolean showOnlyHouseTests = false;
		 if(showOnlyouthouseTestsStr!=null){showOnlyHouseTests = true;}


		 String showOnlyInhouseTestsStr = request.getParameter("showOnlyInhouseTests");
		 boolean showOnlyInhouseTests = false;
		 if(showOnlyInhouseTestsStr !=null){showOnlyInhouseTests = true ;}

		 PagedList pagedList = dao.getAllTestDetails(mrno, department, testName, pageNum,
				 patient, fromDate, toDate,formSort, dForm.getSortReverse(), category,
				 showOnlyHouseTests,showOnlyInhouseTests,testStatus);

		 JSONSerializer json = new JSONSerializer();
		 List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		 request.setAttribute("inHouses", json.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));

		 request.setAttribute("userDept", department);
		 request.setAttribute("pagedList",pagedList);
		 request.setAttribute("testName", testName);
		 request.setAttribute("diagGenericPref", diagGenericPref);
		 request.setAttribute("module", category);
		 request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		 return mapping.findForward("showTestList");
}

	public ActionForward viewTestAuditLog(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, SQLException{

		int pageNum = 1;
		if (request.getParameter("pageNum") !=null && ! request.getParameter("pageNum").equals("")) {
			pageNum = Integer.parseInt(request.getParameter("pageNum"));
		}

		PagedList list = LaboratoryDAO.getTestAuditLog(pageNum,request.getParameter("prescribedId"));
		request.setAttribute("prescribedId", request.getParameter("prescribedId"));
		request.setAttribute("testName", request.getParameter("testName"));
		request.setAttribute("pagedList", list);

		return mapping.findForward("ViewLog");
	}

}
