package com.insta.hms.master.RegistrationPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import flexjson.JSONSerializer;

public class RegistrationPreferencesAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(RegistrationPreferencesAction.class);

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

	  DepartmentService departmentService = ApplicationContextProvider.getApplicationContext().getBean(DepartmentService.class);
	  PatientCategoryService patientCategoryService = ApplicationContextProvider.getApplicationContext().getBean(PatientCategoryService.class);

		RegistrationPreferencesDAO dao = new RegistrationPreferencesDAO("registration_preferences");
		BasicDynaBean bean = dao.getRecord();
		request.setAttribute("bean", bean);
		request.setAttribute("beanMap", bean.getMap());
		dao.clearCache();
		JSONSerializer js = new JSONSerializer().exclude("class");
		List cityList = CityMasterDAO.getPatientCityList(false);
		request.setAttribute("cityJson", js.serialize(cityList));
		request.setAttribute("categoryWiseRateplans", js.serialize(ConversionUtils.listBeanToListMap(
				PatientCategoryDAO.getAllCategoriesIncSuperCenter(0))));
		request.setAttribute("department", js.serialize(ConversionUtils.listBeanToListMap(departmentService.lookup(true))));
		List<Integer> centerIds = new ArrayList<Integer>();
		centerIds.add(RequestContext.getCenterId());
		request.setAttribute("patient_category", patientCategoryService.listByCenter(centerIds, false));

		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		Connection con = DataBaseUtil.getConnection();
		Map<String,Object> params = getParameterMap(request);
		List errors = new ArrayList();
		if (params.get("emergency_patient_department_id") != null 
		    && ((Object[]) params.get("emergency_patient_department_id"))[0].equals("")) {
		  params.put("emergency_patient_department_id", new Object[] {null});
		}
		RegistrationPreferencesDAO dao = new RegistrationPreferencesDAO("registration_preferences");
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean,errors);

		Object key = ((Object[]) params.get("pr_no"))[0];
		Map<String,String>keys = new HashMap<String, String>();
		keys.put("pr_no", key.toString());

		FlashScope flash = FlashScope.getScope(request);
		try {
			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if ((success > 0))
					flash.success("Registration Preferences updated successfully");
				else
					flash.error("Failed to update charges");
			} else {
				flash.error("Incorrectly  formated values supplied ");
			}
		} finally {
			DataBaseUtil.closeConnections(con,null);
		}

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
