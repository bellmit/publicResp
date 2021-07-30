package com.insta.hms.mdm.grnprinttemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is for GRN template operations.
 *
 * @author irshadmohammed
 */
@Controller
@RequestMapping(URLRoute.GRN_PRINT_TEMPLATE_PATH)
public class GrnPrintTemplatesController extends MasterController {

  /** The grn service. */
  GrnPrintTemplatesService grnService = null;

  /** The grn router. */
  MasterResponseRouter grnRouter = null;

  /**
   * Instantiates a new grn print templates controller.
   *
   * @param service the service
   */
  public GrnPrintTemplatesController(GrnPrintTemplatesService service) {
    super(service, MasterResponseRouter.GRN_PRINT_TEMPLATES_ROUTER);
    grnService = service;
    grnRouter = MasterResponseRouter.GRN_PRINT_TEMPLATES_ROUTER;
  }

  /**
   * This method is used for referenceLists.
   *
   * @param params Map
   * @return the reference lists
   */
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> refSupplierCategoryMap =
        new HashMap<String, List<BasicDynaBean>>();
    return refSupplierCategoryMap;
  }

  /**
   * This method is used to add grn template.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/addnewtemplate", method = RequestMethod.GET)
  public ModelAndView addtemplate(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    ModelAndView mav = new ModelAndView();
    Map params = req.getParameterMap();
    String realPath = req.getServletContext().getRealPath("");

    FileInputStream fis = null;
    fis = new FileInputStream(new File(realPath + "/WEB-INF/templates/GrnPrint.ftl"));
    String templateContent = new String(DataBaseUtil.readInputStream(fis));
    mav.addObject("template_content", templateContent);
    Map<String, List<Map>> referenceData = getReferenceData(params);
    if (null != referenceData & referenceData.size() > 0) {
      mav.addAllObjects(referenceData);
    }
    mav.setViewName(grnRouter.route("add"));
    return mav;
  }

  /**
   * This method is used to creates the template.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/createtemplate", method = RequestMethod.POST)
  public ModelAndView createTemplate(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes attribs)
      throws SQLException, IOException {

    Map<String, String[]> params = req.getParameterMap();
    BasicDynaBean bean = grnService.toBean(params);
    grnService.insert(bean);
    ModelAndView modelView = new ModelAndView();
    modelView.addObject("bean", bean.getMap());
    modelView.setViewName(URLRoute.GRN_PRINT_TEMPLATE_REDIRECT_TO_SHOW);
    return modelView;
  }

  /**
   * This method us used to update template.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/updatetemplate", method = RequestMethod.POST)
  public ModelAndView updateTemplate(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes attribs)
      throws SQLException, IOException {

    Map<String, String[]> params = req.getParameterMap();
    BasicDynaBean bean = grnService.toBean(params);
    grnService.update(bean);
    ModelAndView modelView = new ModelAndView();
    modelView.addObject("bean", bean.getMap());
    modelView.setViewName(URLRoute.GRN_PRINT_TEMPLATE_REDIRECT_TO_SHOW);
    return modelView;
  }

  /**
   * This method is used to reset template to default.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  public ModelAndView resetToDefault(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes attribs)
      throws SQLException, IOException {

    Map<String, Object> params = getParameterMap(req);
    String templateName = (String) params.get("template_name");
    String templateId = (String) params.get("template_id");
    String realPath = req.getServletContext().getRealPath("");
    FileInputStream fis = null;
    String errormsg = "";
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("template_id", templateId);
    BasicDynaBean bean = grnService.findByPk(keys);

    try {
      fis = new FileInputStream(new File(realPath + "/WEB-INF/templates/GrnPrint.ftl"));
      String templateContent = new String(DataBaseUtil.readInputStream(fis));
      bean.set("user_name", req.getSession(false).getAttribute("userid"));
      bean.set("template_name", templateName);
      bean.set("grn_template_content", templateContent);
      grnService.update(bean);
    } catch (Exception exp) {
      errormsg = "Failed to Reset the GRN template to Default..";
    }

    ModelAndView modelView = new ModelAndView();
    if (bean != null) {
      modelView.addObject("bean", bean.getMap());
    }
    attribs.addFlashAttribute("error", errormsg);
    modelView.setViewName(
        !errormsg.isEmpty()
            ? URLRoute.GRN_PRINT_TEMPLATE_REDIRECT_TO_LIST
            : URLRoute.GRN_PRINT_TEMPLATE_REDIRECT_TO_SHOW);
    return modelView;
  }

  /**
   * This method is used to delete template.
   *
   * @param req the req
   * @param resp the resp
   * @param attribs the attribs
   * @return the model and view
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/deletetemplates", method = RequestMethod.GET)
  public ModelAndView delete(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes attribs)
      throws SQLException, IOException {
    String[] billPrints = req.getParameterValues("deleteGRNPrint");
    if (billPrints != null) {
      for (String templateId : billPrints) {
        boolean status = grnService.deleteGrnTemplate(Integer.parseInt(templateId));
        if (!status) {
          break;
        }
      } //end for
    } //end outer if

    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.GRN_PRINT_TEMPLATE_REDIRECT_TO_LIST);
    return modelView;
  }
}
