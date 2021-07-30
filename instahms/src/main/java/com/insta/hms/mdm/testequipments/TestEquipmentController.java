/*
 * 
 */

package com.insta.hms.mdm.testequipments;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class TestEquipmentController.
 */
@Controller
@RequestMapping(URLRoute.TEST_EQUIPMENT_PATH)
public class TestEquipmentController extends MasterDetailsController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(TestEquipmentController.class);

  /** The test equipment service. */
  @Autowired
  private TestEquipmentService testEquipmentService;

  /** The message util. */
  @Autowired
  private MessageUtil messageUtil;

  /**
   * Instantiates a new test equipment controller.
   *
   * @param service
   *          the service
   */
  public TestEquipmentController(TestEquipmentService service) {
    super(service, MasterResponseRouter.TEST_EQUIPMENT_ROUTER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((TestEquipmentService) getService()).getListPageLookup(params);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((TestEquipmentService) getService()).getAddPageData(params);
  }

  @Override
  /* (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterDetailsController#create(
   * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
   * org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {

    StringBuilder msg = new StringBuilder();
    Map<String, String[]> parameters = request.getParameterMap();
    int equipmentId = 0;
    try {
      equipmentId = testEquipmentService.insertTestEquipmentDetails(parameters, msg);
    } catch (SQLException se) {
      logger.debug("Failed to create test equipment" + se.getMessage());
    }
    if (equipmentId == 0) {
      redirect.addFlashAttribute("error", messageUtil.getMessage("flash.insert.failed", null));
      return new ModelAndView(URLRoute.TEST_EQUIPMENT_REDIRECT_TO_ADD);
    } else if (msg != null && !msg.toString().equals("")) {
      redirect.addFlashAttribute("info", msg.toString());
    }
    redirect.addAttribute("eq_id", equipmentId);
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.TEST_EQUIPMENT_REDIRECT_TO_SHOW);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterDetailsController#show(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {
    ModelAndView mav = new ModelAndView();
    Map mergeMap = null;
    try {
      mergeMap = testEquipmentService.getListEditPageData(req.getParameter("eq_id"));
    } catch (SQLException se) {
      logger.debug("Failed to show the test equipment details" + se.getMessage());
    }
    mav.addAllObjects(mergeMap);
    mav.setViewName(router.route("show"));
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterDetailsController#update(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse,
   * org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirect) {

    String eqid = req.getParameter("eq_id");
    boolean success = true;
    StringBuilder msg = new StringBuilder();
    Map<String, String[]> parameters = req.getParameterMap();
    try {
      success = testEquipmentService.updateTestEquipmentDetails(parameters, eqid, msg);
    } catch (Exception exception) {
      logger.debug("Failed to update the test equipment details" + exception.getMessage());
    }
    if (!success) {
      redirect.addFlashAttribute("error", messageUtil.getMessage("flash.update.failed", null));
    } else if (msg != null && !msg.toString().equals("")) {
      redirect.addFlashAttribute("info", msg.toString());
    }
    redirect.addAttribute("eq_id", eqid);
    resp.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.TEST_EQUIPMENT_REDIRECT_TO_SHOW);

  }

}
