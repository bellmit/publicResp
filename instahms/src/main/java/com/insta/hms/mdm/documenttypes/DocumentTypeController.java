package com.insta.hms.mdm.documenttypes;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.documenttypecategory.DocumentCategoryMappingRepository;
import com.insta.hms.mdm.documenttypecategory.DocumentCategoryRepository;
import com.insta.hms.mdm.documenttypecategory.DocumentCategoryService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Document type master controller.
 */
@Controller
@RequestMapping(URLRoute.DOCUMENT_TYPE_PATH)
public class DocumentTypeController extends MasterController {

  @LazyAutowired
  private DocumentTypeService documentTypeService;

  @LazyAutowired
  private DocumentCategoryRepository documentCategoryRepository;

  @LazyAutowired
  private DocumentCategoryService documentCategoryService;

  @LazyAutowired
  private DocumentCategoryMappingRepository documentCategoryMappingRepository;

  @LazyAutowired
  private MessageUtil messageUtil;

  public DocumentTypeController(DocumentTypeService service) {
    super(service, MasterResponseRouter.DOCUMENT_TYPE_ROUTER);
  }

  /**
   * add document.
   * @param req the req object
   * @param resp the resp object
   * @return returns ModelAndView
   */
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    Map criteriaMap = new HashMap();
    criteriaMap.put("specialized", "N");
    List<BasicDynaBean> beans  = documentCategoryRepository.findByCriteria(criteriaMap);
    mav.addObject("docTypeCategories", beans);
    List<String> temp = new ArrayList<>();
    mav.addObject("mappedCategories", temp.toArray(new String[0]));
    Map params = req.getParameterMap();
    addReferenceData(getReferenceData(params), mav);
    addReferenceData(getReferenceBean(params), mav);
    mav.setViewName(router.route("add"));
    return mav;
  }

  /**
   * Show documents.
   * @param req the req object
   * @param resp the resp object
   * @return returns ModelAndView
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    Map params = req.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = documentTypeService.findByPk(params, true);
    modelView.addObject("bean", bean.getMap());
    Map criteriaMap = new HashMap();
    criteriaMap.put("specialized", "N");
    List<BasicDynaBean> beans  = documentCategoryRepository.findByCriteria(criteriaMap);
    modelView.addObject("docTypeCategories", beans);
    List<String> mappedCatResults = new ArrayList<>();
    List<BasicDynaBean> mappedCategories = documentCategoryMappingRepository
        .getDocTypesByCatMapping(((String[]) params.get("doc_type_id"))[0]);
    for (BasicDynaBean b : mappedCategories) {
      mappedCatResults.add(String.valueOf(b.get("doc_type_category_id")));
    }
    modelView.addObject("mappedCategories", mappedCatResults.toArray(new String[0]));
    addReferenceData(getReferenceData(params), modelView);
    addReferenceData(getReferenceBean(params), modelView);
    modelView.setViewName(router.route("show"));

    return modelView;
  }

  /**
   * create documents.
   * @param req the req object
   * @param resp the resp object
   * @param attribs the attribs
   * @return returns ModelAndView
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);
    BasicDynaBean bean = mapToBean(params, fileMap);
    int ret = documentTypeService.insertDocumentType(bean, params);
    if (ret != 0) {
      attribs.mergeAttributes(getAttributesForRedirection(bean.getMap(), fileMap));
      String createdMessage = messageUtil.getMessage("flash.created.successfully", null);
      attribs.addFlashAttribute("info", createdMessage);
    }
    modelView.setViewName(router.route("create"));
    return modelView;
  }

  /**
   * Update documents.
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return returns ModelAndView
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);
    BasicDynaBean bean = mapToBean(params, fileMap);
    int ret = documentTypeService.updateDocumentType(bean, params);
    if (ret != 0) {
      attribs.mergeAttributes(getAttributesForRedirection(bean.getMap(), fileMap));
      String updatedMessage = messageUtil.getMessage("flash.updated.successfully", null);
      attribs.addFlashAttribute("info", updatedMessage);
    }
    modelView.setViewName(router.route("update"));
    return modelView;
  }

}
