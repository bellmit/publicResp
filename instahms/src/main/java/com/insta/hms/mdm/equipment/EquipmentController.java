package com.insta.hms.mdm.equipment;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.BulkDataController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
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

@Controller
@RequestMapping(URLRoute.EQUIPMENT_MASTER)
public class EquipmentController extends BulkDataController {

  @Autowired private MessageUtil messageUtil;
  @Autowired private EquipmentService equipmentService;

  static final String FILE_NAME = "equipments";

  public EquipmentController(EquipmentService equipmentservice) {
    super(equipmentservice, MasterResponseRouter.EQUIPMENT_ROUTER, FILE_NAME);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {

    return ((EquipmentService) getService()).getListPageData(params);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {

    return ((EquipmentService) getService()).getAddPageData(params);
  }

  /**
   * create method of equipment.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @param redirect RedirectAttributes
   * @return ModelAndView
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect) {
    String orgId = request.getParameter("org_id");
    StringBuilder msg = new StringBuilder();
    Map<String, String[]> parameters = request.getParameterMap();
    String equipmentId = null;
    equipmentId = equipmentService.insertEquipmentDetails(request, parameters, msg);
    if (equipmentId == null || equipmentId.equals("")) {
      redirect.addFlashAttribute("error", messageUtil.getMessage("flash.update.failed", null));
      redirect.addFlashAttribute("org_id", orgId);
      return new ModelAndView(URLRoute.ADD_EDIT_EQUIPMENT_REDIRECT_TO_ADD);
    }
    if (msg != null && !msg.equals("")) {
      redirect.addFlashAttribute("info", msg.toString());
    }
    redirect.addAttribute("equip_id", equipmentId);
    redirect.addAttribute("org_id", orgId);
    response.setStatus(HttpStatus.CREATED.value());

    return new ModelAndView(URLRoute.EQUIPMENT_TO_SHOW);
  }

  /**
   * show method of equipment.
   *
   * @param req HttpServletRequest
   * @param response HttpServletResponse
   * @return ModelAndView
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String equipmentId = req.getParameter("equip_id");
    String orgId = req.getParameter("org_id");
    Map mergeMap = null;
    mergeMap = equipmentService.getEditPageDate(equipmentId, orgId);
    mav.addAllObjects(mergeMap);

    mav.setViewName(router.route("show"));
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /**
   * update method of equipment.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @param redirect RedirectAttributes
   * @return ModelAndView
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes redirect) {

    boolean success = true;
    String equipId = req.getParameter("equip_id");
    StringBuilder msg = new StringBuilder();
    Map<String, String[]> parameters = req.getParameterMap();
    success = equipmentService.updateEquipmentDetails(req, parameters, equipId, msg);

    if (msg != null && !msg.equals("")) {
      redirect.addFlashAttribute("info", msg.toString());
    }
    String orgId = req.getParameter("org_id");
    redirect.addAttribute("equip_id", equipId);
    redirect.addAttribute("org_id", orgId);
    resp.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.EQUIPMENT_TO_SHOW);
  }

  /**
   * editcharge method of equipment.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return ModelAndView
   */
  @RequestMapping(value = "/editcharge", method = RequestMethod.GET)
  public ModelAndView editcharge(HttpServletRequest req, HttpServletResponse resp)
      throws SQLException {

    ModelAndView mav = new ModelAndView();
    String orgId = req.getParameter("org_id");
    String equipId = req.getParameter("equip_id");

    Map mergeMap = equipmentService.getListEditChargeData(equipId, orgId);
    mav.addAllObjects(mergeMap);
    mav.setViewName(URLRoute.EQUIPMENT_EDIT_CHARGE_PATH);
    resp.setStatus(HttpStatus.CREATED.value());
    return mav;
  }

  /**
   * updatecharge method of equipment.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @param redirect RedirectAttributes
   * @return ModelAndView
   */
  @RequestMapping(value = "/updatecharge", method = RequestMethod.POST)
  public ModelAndView updatecharge(
      HttpServletRequest req, HttpServletResponse resp, RedirectAttributes redirect)
      throws Exception {

    boolean success = true;
    String orgId = req.getParameter("org_id");
    String equipId = req.getParameter("equip_id");
    Map<String, String[]> parameters = req.getParameterMap();
    StringBuilder msg = new StringBuilder();
    success = equipmentService.updatecharges(parameters, orgId, equipId, msg);
    if (success) {
      msg.append("Equipment Charges updated successfully");
      redirect.addFlashAttribute("info", msg.toString());
    } else if (msg != null && !msg.equals("")) {
      redirect.addFlashAttribute("error", msg.toString());
    }
    redirect.addAttribute("equip_id", equipId);
    redirect.addAttribute("org_id", orgId);
    return new ModelAndView(URLRoute.EDIT_EQUIPMENT_CHARGE_REDIRECT_TO_SHOW);
  }
}
