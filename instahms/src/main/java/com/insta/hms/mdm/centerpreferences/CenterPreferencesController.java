package com.insta.hms.mdm.centerpreferences;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeService;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class CenterPreferencesController.
 */
@Controller
@RequestMapping(URLRoute.CENTER_PREFERENCES_PATH)
public class CenterPreferencesController extends MasterController {

  /** The service. */
  @LazyAutowired
  private CenterPreferencesService service;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;
  
  /** The patient category service. */
  @LazyAutowired
  private PatientCategoryService patientCategoryService;
  
  /** The practitionermapping service. */
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerMappingService;
  
  /** The practitioner service. */
  @LazyAutowired
  private PractitionerTypeService practitionerService;
  
  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;
  
  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * Instantiates a new center preferences controller.
   *
   * @param service the service
   */
  public CenterPreferencesController(CenterPreferencesService service) {
    super(service, MasterResponseRouter.CENTER_PREFERENCES_ROUTER);
    // TODO Auto-generated constructor stub
  }

  /* @see com.insta.hms.mdm.MasterController#list(
   * javax.servlet.http.HttpServletRequest, 
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.CENTER_PREFERENCES_REDIRECT_TO_SHOW);
    return modelView;
  }

  /* @see com.insta.hms.mdm.MasterController#show(
   * javax.servlet.http.HttpServletRequest, 
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    Map<String, Integer> params = new HashMap<String, Integer>();
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    params.put("center_id", centerId);
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = service.findByPk(params);
    if (null == bean) {
      params.put("center_id", 0);
      bean = service.findByPk(params);
    }
    modelView.addObject("bean", bean.getMap());
    JSONSerializer js = new JSONSerializer().exclude("class");
    modelView.addObject("categoryWiseRateplans", js.serialize(ConversionUtils
        .listBeanToListMap(patientCategoryService.getAllCategoriesIncSuperCenter(centerId))));
    List<BasicDynaBean> practitionerMappingList = practitionerMappingService
        .getPractitionerMappings(centerId);
    modelView.addObject("practitioner_mappings",
        js.serialize(ConversionUtils.listBeanToListMap(practitionerMappingList)));
    modelView.addObject("practitioner_mappings_length", practitionerMappingList.size());
    modelView.addObject("practitioner_list", js
        .serialize(ConversionUtils.listBeanToListMap(practitionerService.getPractitionerTypes())));
    modelView.addObject("consultation_types", js.serialize(
        ConversionUtils.listBeanToListMap(consultationTypesService.getConsultationTypes())));

    addReferenceData(getReferenceData(params), modelView);
    addReferenceData(getReferenceBean(params), modelView);
    modelView.setViewName(router.route("show"));

    return modelView;
  }

  /**
   * Update.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = req.getParameterMap();
    BasicDynaBean centerPreferencesBean = mapToBean(params);
    BasicDynaBean centerPrefBeanExist = service.findByPk(params);
    Integer ret = 0;
    if (centerPrefBeanExist == null) {
      ret = service.insert(centerPreferencesBean);
    } else {
      ret = service.update(centerPreferencesBean);
    }

    Map<String, Object> mappingInsertRes = service.insertPractitionerMapping(params);
    if (mappingInsertRes.containsKey("error")) {
      attribs.addFlashAttribute("error", mappingInsertRes.get("error").toString());
      modelView.setViewName(router.route("update"));
      return modelView;
    }

    if (ret != 0) {
      attribs.mergeAttributes(centerPreferencesBean.getMap());
      String updatedMessage = messageUtil.getMessage("flash.updated.successfully", null);
      attribs.addFlashAttribute("info", updatedMessage);
    }
    modelView.setViewName(router.route("update"));
    return modelView;
  }
}
