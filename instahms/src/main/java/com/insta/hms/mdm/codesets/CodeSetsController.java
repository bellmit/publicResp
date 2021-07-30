package com.insta.hms.mdm.codesets;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("codeSetsController")
@RequestMapping(URLRoute.CODE_SETS_PATH)
public class CodeSetsController extends MasterController {

  @LazyAutowired
  CodeSetsService service;

  public CodeSetsController(CodeSetsService service) {
    super(service, MasterResponseRouter.CODE_SETS_ROUTER);
  }

  /**
   * List.
   */
  @SuppressWarnings("unchecked")
  @GetMapping(value = {"/list", ""})
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    Map<String, Object> flattenMap = ConversionUtils.flatten(req.getParameterMap());
    modelView.addObject("codeSystemCategoryId", flattenMap.get("code_system_category_id"));
    modelView.addObject("codeSystemsId", flattenMap.get("code_systems_id"));
    if (!StringUtils.isEmpty(flattenMap.get("code_system_category_id"))) {
      modelView.addObject("codeSystemCategoryLabel", service.getCodeSystemCategoryLabel(
          Integer.parseInt((String) flattenMap.get("code_system_category_id"))));
    }
    if (!StringUtils.isEmpty(flattenMap.get("code_systems_id"))) {
      modelView.addObject("codeSystemsLabel",
          service.getCodeSystemLabel(Integer.parseInt((String) flattenMap.get("code_systems_id"))));
    }
    Map paramMap = req.getParameterMap();
    modelView.addObject("pagedList", service.search(paramMap));
    addReferenceData(getFilterLookups(paramMap), modelView);
    modelView.setViewName(router.route("list"));
    return modelView;
  }

  /**
   * Save.
   * 
   * @param req the req
   * @param resp the resp
   * @param redirectAttrs the attributes to redirect
   * @return string
   */
  @PostMapping(value = {"/save"})
  public String save(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttrs) {
    Map paramMap = req.getParameterMap();
    service.saveCodeSets(paramMap);
    redirectAttrs.addAttribute("code_system_category_id",
        ((String[]) paramMap.get("code_system_category_id"))[0]);
    redirectAttrs.addAttribute("code_systems_id", ((String[]) paramMap.get("code_system_id"))[0]);
    return "redirect:list";
  }

  /**
   * Get default code sets.
   * 
   * @param req the HttpServletRequest
   * @param resp the HttpServletResponse
   * @return modelview
   */
  @GetMapping(value = {"/defaultcodesets"})
  public ModelAndView defaultCodeSets(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    Map<String, Object> flattenMap = ConversionUtils.flatten(req.getParameterMap());
    modelView.addObject("codeSystemCategoryId", flattenMap.get("code_system_category_id"));
    modelView.addObject("codeSystemsId", flattenMap.get("code_systems_id"));
    if (!StringUtils.isEmpty(flattenMap.get("code_system_category_id"))) {
      modelView.addObject("codeSystemCategoryLabel", service.getCodeSystemCategoryLabel(
          Integer.parseInt((String) flattenMap.get("code_system_category_id"))));
    }
    if (!StringUtils.isEmpty(flattenMap.get("code_systems_id"))) {
      modelView.addObject("codeSystemsLabel",
          service.getCodeSystemLabel(Integer.parseInt((String) flattenMap.get("code_systems_id"))));
    }
    Map paramMap = req.getParameterMap();
    modelView.addObject("records", service.searchDefaultCodeSets(paramMap));
    addReferenceData(getFilterLookups(paramMap), modelView);
    modelView.setViewName(URLRoute.DEFAULT_CODE_SETS_FILE);
    return modelView;
  }

  /**
   * Saves default code sets.
   * 
   * @param req the HttpServletRequest
   * @param resp the HttpServletResponse
   * @param redirectAttrs the RedirectAttributes
   * @return string to redirect
   */
  @PostMapping(value = {"/saveDefaultCodeSets"})
  public String saveDefaultCodeSets(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirectAttrs) {
    Map paramMap = req.getParameterMap();
    service.saveDefaultCodeSets(paramMap);
    redirectAttrs.addAttribute("code_system_category_id",
        ((String[]) paramMap.get("code_system_category_id"))[0]);
    redirectAttrs.addAttribute("code_systems_id", ((String[]) paramMap.get("code_system_id"))[0]);
    return "redirect:defaultcodesets";
  }
}
