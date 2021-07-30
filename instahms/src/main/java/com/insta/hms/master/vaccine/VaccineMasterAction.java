package com.insta.hms.master.vaccine;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.mdm.vaccinecategory.VaccineCategoryService;
import com.insta.hms.mdm.vaccinecategory.VaccineMasterCategoryMappingRepository;
import com.insta.hms.mdm.vaccinemaster.VaccineMasterService;

import flexjson.JSONSerializer;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VaccineMasterAction extends DispatchAction{

	VaccineMasterDao vaccineMasterDao = new VaccineMasterDao();
	
    VaccineMasterService vaccineMasterService = ApplicationContextProvider.getBean(
        VaccineMasterService.class);
    VaccineCategoryService vaccineCategoryService =
        ApplicationContextProvider.getBean(VaccineCategoryService.class);
    VaccineMasterCategoryMappingRepository vaccineMasterCategoryMappingRepository =
        ApplicationContextProvider.getBean(VaccineMasterCategoryMappingRepository.class);

    private  final GenericDAO vaccineDoseMasterDAO = new GenericDAO("vaccine_dose_master");

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {

      Map map = request.getParameterMap();
      PagedList pagedList = vaccineMasterDao.getVaccineDetails(map,
          ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response)throws SQLException{

		JSONSerializer json = new JSONSerializer().exclude("class");
		ArrayList vaccineList = VaccineMasterDao.getVaccineList();
        request.setAttribute("vaccineCategories",
            ConversionUtils.listBeanToListMap(vaccineCategoryService.getVaccineCategoryList()));
        request.setAttribute("vaccineList", json.serialize(vaccineList));
		return m.findForward("addshow");
	}

    public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest request,
        HttpServletResponse response) throws Exception {

      Map<String, String[]> params = request.getParameterMap();
      List errors = new ArrayList();
      BasicDynaBean bean = vaccineMasterDao.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);
      String error = null;
      if (errors.isEmpty()) {
        if (vaccineMasterDao.exist("vaccine_name", bean.get("vaccine_name"))) {
          error = "Vaccine name already exists.....";
        } else {
          if (!vaccineMasterService.insertVaccine(bean, params)) {
            error = "Fail to add vaccine master....";
          }
        }
      } else {
        error = "Incorrectly formatted values supplied..";
      }
      ActionRedirect redirect = null;
      FlashScope flash = FlashScope.getScope(request);
      if (error != null) {
        redirect = new ActionRedirect(m.findForward("addRedirect"));
        flash.error(error);

      } else {
        redirect = new ActionRedirect(m.findForward("showRedirect"));
        redirect.addParameter("vaccine_id", bean.get("vaccine_id"));
      }
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

      return redirect;
    }

  public ActionForward showVaccineDetails(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = vaccineMasterDao.findByKey("vaccine_id", Integer.parseInt(req.getParameter("vaccine_id")));
		req.setAttribute("bean", bean);
        req.setAttribute("vaccineCategories",
            ConversionUtils.listBeanToListMap(vaccineCategoryService.getVaccineCategoryList()));
        List<String> mappedCategories = new ArrayList<>();
        List<BasicDynaBean> mappedCategoriesBeanList = vaccineMasterCategoryMappingRepository
            .getVaccineCategory(Integer.parseInt(req.getParameter("vaccine_id")));
        for (BasicDynaBean mappedCategory : mappedCategoriesBeanList) {
          mappedCategories.add(String.valueOf(mappedCategory.get("vaccine_category_id")));
        }
        req.setAttribute("mappedCategories", mappedCategories.toArray(new String[0]));
		JSONSerializer js = new JSONSerializer().exclude("class");
		ArrayList vaccineList = VaccineMasterDao.getVaccineList();
		req.setAttribute("vaccineList", js.serialize(vaccineList));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
            Map<String, String[]> params = req.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = vaccineMasterDao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

            String key = req.getParameter("vaccine_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("vaccine_id", Integer.parseInt(key));
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				boolean success = vaccineMasterService.updateVaccine(bean, params);
				if (vaccineDoseMasterDAO.findByKey("vaccine_id", bean.get("vaccine_id")) != null && bean.get("status").toString().equalsIgnoreCase("I")) {
					success &= vaccineMasterDao.changeDosageStatus(con, bean.get("vaccine_id").toString());
				}
				if (success) {
					con.commit();
					flash.success("Vaccine details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update vaccine details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("vaccine_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward showDossageDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException {

			JSONSerializer serializer = new JSONSerializer().exclude("class");
			String status[] = request.getParameterValues("status");
			String vaccineId = request.getParameter("vaccine_id");
			request.setAttribute("dosageList", vaccineMasterDao.getDosageList(Integer.parseInt(vaccineId), status));
			request.setAttribute("inactivatedDoseNos", serializer.deepSerialize
					(ConversionUtils.copyListDynaBeansToMap(vaccineMasterDao.getInactiveVaccines(Integer.parseInt(vaccineId)))));
			request.setAttribute("vaccineBean", vaccineMasterDao.findByKey("vaccine_id", Integer.parseInt(vaccineId)));

		return mapping.findForward("showDosageMaster");
	}

	public ActionForward saveDosageValues(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException {

			Connection con = null;

			String[] dose_nums = request.getParameterValues("dose_num");
			String[] recommended_ages = request.getParameterValues("recommended_age");
			String[] age_units = request.getParameterValues("age_units");
			String[] notification_lead_time_days = request.getParameterValues("notification_lead_time_days");
			String[] dosageStatus = request.getParameterValues("status");
			String[] is_new = request.getParameterValues("is_new");
			String[] is_edited = request.getParameterValues("is_edited");
			String[] is_deleted = request.getParameterValues("is_deleted");
			String[] vaccine_dose_ids = request.getParameterValues("vaccine_dose_id");
			String single_dose = request.getParameter("single_dose");

			Integer vaccine_id = Integer.parseInt(request.getParameter("vaccine_id"));

			FlashScope flash = FlashScope.getScope(request);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showDosageRedirect"));
			redirect.addParameter("vaccine_id", vaccine_id.toString());
			redirect.addParameter("status", "A");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());


			boolean status = true;

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				for (int i=0; i<dose_nums.length-1; i++) {

					BasicDynaBean dosageBean = vaccineDoseMasterDAO.getBean();
					if (dose_nums[i] != null && !dose_nums[i].equals("") && single_dose.equalsIgnoreCase("N"))
						dosageBean.set("dose_num", Integer.parseInt(dose_nums[i]));
					if (notification_lead_time_days[i] != null && !notification_lead_time_days[i].equals(""))
						dosageBean.set("notification_lead_time_days", Integer.parseInt(notification_lead_time_days[i]));
					if (recommended_ages[i] != null && !recommended_ages[i].equals(""))
						dosageBean.set("recommended_age", Integer.parseInt(recommended_ages[i]));
					dosageBean.set("status", dosageStatus[i]);
					dosageBean.set("age_units", age_units[i]);
					dosageBean.set("vaccine_id", vaccine_id);


					if (is_new[i].equalsIgnoreCase("Y") && !is_deleted[i].equalsIgnoreCase("Y")) {
						Integer vaccine_dosage_id = vaccineDoseMasterDAO.getNextSequence();
						dosageBean.set("vaccine_dose_id", vaccine_dosage_id);
						status &= vaccineDoseMasterDAO.insert(con, dosageBean);

					} else if (is_edited[i].equalsIgnoreCase("Y") && !is_deleted[i].equalsIgnoreCase("Y")) {
						Map keys = new HashMap<String, Integer>();
						keys.put("vaccine_dose_id", Integer.parseInt(vaccine_dose_ids[i]));
						status &= vaccineDoseMasterDAO.update(con, dosageBean.getMap(), keys) > 0;

					} else if (is_deleted[i].equalsIgnoreCase("Y") && !is_new[i].equalsIgnoreCase("Y")) {

						status &= vaccineDoseMasterDAO.delete(con, "vaccine_dose_id", Integer.parseInt(vaccine_dose_ids[i]));
					}
				}
			} finally {
				DataBaseUtil.commitClose(con, status);
			}
			if (status == false) {
				flash.error("Failed to save dosage values.");
			} else {
				flash.success("Vaccine Dosage details saved successfully.");
			}

		return redirect;
	}

}
