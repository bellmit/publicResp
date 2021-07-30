package com.insta.hms.Registration;

import com.bob.hms.common.Constants;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ReadmitAction extends DispatchAction{

	static Logger log = LoggerFactory.getLogger(ReadmitAction.class);
	
	private final InterfaceEventMappingService interfaceEventMappingService =
		      (InterfaceEventMappingService) ApplicationContextProvider.getApplicationContext()
		          .getBean("interfaceEventMappingService");
	
	public ActionForward getReadmitScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		String patient_id = request.getParameter("patient_id");
		if (patient_id == null) {
			return mapping.findForward("readmit");
		}
		patient_id = patient_id.trim();
		BasicDynaBean selectedVisit = new VisitDetailsDAO().findByKey("patient_id", patient_id);
		if (selectedVisit == null) {
			FlashScope flash = FlashScope.getScope(request);
			flash.error("No Patient with Id:"+patient_id);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("visits"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		String mr_no = (String)selectedVisit.get("mr_no");
		List activePatientList = VisitDetailsDAO.getPatientVisits(mr_no, true);
		int centerId = RequestContext.getCenterId();
		String latestInactiveVisitId = VisitDetailsDAO.getPatientLatestVisitId(mr_no, false, null, centerId);
		request.setAttribute("activePatientList", activePatientList);
		request.setAttribute("latestInactiveVisitId",latestInactiveVisitId);
		request.setAttribute("selectedVisit", selectedVisit.getMap());
		return mapping.findForward("readmit");
	}

	public ActionForward patientReadmit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{

		String patient_id = request.getParameter("visitid");
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("visits"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("patient_id", patient_id);

		String visit_type = VisitDetailsDAO.getVisitType(patient_id);
		HttpSession session = request.getSession();
		String username = (String)session.getAttribute("userid");

		// Check if any active visit exists if preference is not to allow multiple active visits.
		RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
		String mrNo = VisitDetailsDAO.getMrno(patient_id);
		if (mrNo == null || mrNo.equals("")){
			flash.error("Invalid visit id. Mrno for this visit id does not exists.");
			return redirect;
		}
		if(visit_type != null && visit_type.equals("i")) {
			String latestActiveVisitId = VisitDetailsDAO.getPatientLatestVisitId(mrNo, true, "i");
			if (latestActiveVisitId != null && !latestActiveVisitId.equals("")) {
				flash.error("Patient has active visit (Visit ID: "+latestActiveVisitId+"). Cannot make this Visit: "+patient_id+" active.");
				return redirect;
			}
		}
		if (regPrefs.getAllow_multiple_active_visits() != null && regPrefs.getAllow_multiple_active_visits().equals("N") && !visit_type.equals("i")) {
		  List columns = new ArrayList();
		  Map identifiers = new HashMap();
		  columns.add("patient_id");
		  identifiers.put("visit_type", "o");
		  identifiers.put("mr_no", mrNo);
		  identifiers.put("status", "A");
		  List<BasicDynaBean> visits = new GenericDAO("patient_registration").listAll(columns, identifiers, null);
		  for(BasicDynaBean basicDynaBean : visits) {
			  if (basicDynaBean.get("patient_id") != null) {
				  flash.error("The patient has an active OP visit ID : "+basicDynaBean.get("patient_id")+ " please close that visit and try again.");
				  return redirect;
			  }
		  }
		}
		MessageResources bundle = (MessageResources)request.getAttribute(Globals.MESSAGES_KEY);
		boolean success = VisitDetailsDAO.readmitPatient(patient_id, visit_type, username);
		if(success){
			interfaceEventMappingService.readmitEvent(patient_id);
      ADTService adtService = (ADTService) ApplicationContextProvider.getApplicationContext()
          .getBean("adtService");
      Map<String, Object> adtData = new HashMap<>();

      adtData.put("patient_id", patient_id);
      adtData.put(Constants.MR_NO, null);
      adtService.createAndSendADTMessage("ADT_08", adtData);
      flash.info(bundle.getMessage("registration.readmit.details.readmitsuccess"));
			return redirect;
		}else {
			flash.error("Patient readmission unsuccessful...");
			return redirect;
		}
	}
}
