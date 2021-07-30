/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.common.SampleCollection;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class OuthouseAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(OuthouseAction.class);

	public ActionForward getOuthouseScreen(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,
			Exception {

		String visitId = request.getParameter("visitid");
		String category = request.getParameter("category");
		request.setAttribute("patientvisitdetails", VisitDetailsDAO.getPatientVisitDetailsBean(visitId));
		request.setAttribute("custmer",
    			OhSampleRegistrationDAO.getIncomingCustomer(visitId));

		List l = LaboratoryDAO.getOutsourceTestList(visitId,category);
		request.setAttribute("outHousetests", l);
		request.setAttribute("category", category);

		return mapping.findForward("getOutHouseScreen");
	}


	public ActionForward SaveOuthouseDetails(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException,
			Exception {

		ArrayList<SampleCollection> scList = new ArrayList<SampleCollection>();
		ArrayList<OutHouseSampleDetails> ohlist = null;
		SampleCollection sc = null;
		boolean status=false;

		LaboratoryForm rf = (LaboratoryForm) form;

		String prescribedid[] = rf.getHprescribedId();
		String mrNo = rf.getMrno();
		String visitId = rf.getVisitid();
		String testid[] = rf.getHtestId();
		String outhouseId[] = rf.getHoutHouseId();
		String category = request.getParameter("category");
		String userName = (String) request.getSession(false).getAttribute("userid");
		java.util.Date date = new java.util.Date();
		//get the center id from visit not from the session.
		int centerId = VisitDetailsDAO.getCenterId(visitId);

		 FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("outhouseRedirect"));

       if(prescribedid!=null){
		for (int i = 0; i < prescribedid.length; i++) {

            if (outhouseId[i].equals("empty") || outhouseId[i].isEmpty())
				continue;
			sc = new SampleCollection();
			sc.setMrNo(mrNo);
			sc.setVisitId(visitId);
			sc.setTestId(testid[i]);
			sc.setSampleNo(visitId);
			sc.setPrescribedId(Integer.parseInt(prescribedid[i]));
			sc.setSampleStatus("C");
			sc.setSampleDate(new java.sql.Timestamp(date.getTime()));
			sc.setUserName(userName);
			sc.setSampleTypeId(0);
			scList.add(sc);
		}

		ohlist = new ArrayList<OutHouseSampleDetails>();

		List<BasicDynaBean> presList = new ArrayList<BasicDynaBean>();
		for (int i = 0; i < prescribedid.length; i++) {
			BasicDynaBean prescBean = new GenericDAO("tests_prescribed").getBean();
			 if (outhouseId[i].equals("empty") || outhouseId[i].isEmpty() )
				continue;
			OutHouseSampleDetails ohDetails = new OutHouseSampleDetails();
			ohDetails.setVisitId(visitId);
			ohDetails.setPrescribedId(Integer.parseInt(prescribedid[i]));
			ohDetails.setSampleNo(visitId);
			ohDetails.setTestId(testid[i]);
			int outsourceDestId = (Integer)new GenericDAO("diag_outsource_master").
				findByKey("outsource_dest", outhouseId[i]).get("outsource_dest_id");
			ohDetails.setoutSourceId(String.valueOf(outsourceDestId));
			ohlist.add(ohDetails);
			prescBean.set("prescribed_id", Integer.parseInt(prescribedid[i]));
			prescBean.set("pat_id", visitId);
			prescBean.set("outsource_dest_id", outsourceDestId);
			presList.add(prescBean);
		}

		if ((scList.size()==0 && ohlist.size()==0)) {
			flash.error("There are no outhouse for that corresponding test.<br>"+
					"Failed to insert Outhouse details ..");
			redirect.addParameter("visitid", visitId);
			redirect.addParameter("category", category);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
			}

			status = LaboratoryBO.saveOuthouses(scList, ohlist, category, centerId);
			for(BasicDynaBean bean : presList){
				LaboratoryBO.updateOutsourceInTestPres((Integer)bean.get("outsource_dest_id"),(Integer)bean.get("prescribed_id"),visitId);
			}
			status = true;
		}
		if (status) {
			flash.success("Outhouse details inserted successfully..");
		} else {
			flash.error("Failed to insert Outhouse details..");
		}
		redirect.addParameter("visitid", visitId);
		redirect.addParameter("category", category);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}


}
