package com.bob.hms.adminmasters.wardandbed;

import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class WardAndBedMasterAction.
 */
public class WardAndBedMasterAction extends DispatchAction {

  /**
   * Gets the wardand bed master.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the wardand bed master
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws ParseException the parse exception
   * @throws SQLException the sql exception
   */
  public ActionForward getWardandBedMaster(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException {

    request.setAttribute("wardandbeddetails", WardAndBedMasterDao.getWardAndBedTypeList(
        request.getParameterMap(),
        ConversionUtils.getListingParameter(request.getParameterMap())));
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    return mapping.findForward("getWardBedMasterScreen");
  }

  /**
   * Gets the ward details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the ward details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward getWardDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String wardId = request.getParameter("wardId");
    request.setAttribute("method", "editWardDetails");

    List<BasicDynaBean> al = WardAndBedMasterDao.getWarDetails(wardId);
    request.setAttribute("wardDetils", al);
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);

    if (!al.isEmpty()) {
      String wardName = (String) al.get(0).get("ward_name");
      String status = (String) al.get(0).get("status");
      String description = (String) al.get(0).get("description");
      int store = al.get(0).get("store_id") != null
          ? (Integer) al.get(0).get("store_id") : 0;

      String allowedGender = (String) ((BasicDynaBean) al.get(0)).get("allowed_gender");
      request.setAttribute("wardName", wardName);
      request.setAttribute("status", status);
      request.setAttribute("description", description);
      request.setAttribute("wardId", wardId);
      request.setAttribute("store", store);
      request.setAttribute("center_id", (Integer) al.get(0).get("center_id"));
      request.setAttribute("allowedGender", allowedGender);
    }

    return mapping.findForward("addOrEditWard");
  }

  /**
   * Gets the new ward screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the new ward screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward getNewWardScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    request.setAttribute("method", "insertNewWardDetails");
    request.setAttribute("bedTypes", BedMasterDAO.getAllBedTypesList());
    request.setAttribute("wardId", request.getParameter("wardId"));
    request.setAttribute("ward", request.getParameter("wardName"));

    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    request.setAttribute("multiCenters",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    return mapping.findForward("addOrEditWard");
  }

  /**
   * Insert new ward details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward insertNewWardDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    WardAndBedMasterForm wf = (WardAndBedMasterForm) form;
    int[] addBeds = wf.getNoOfBedToAdd();
    String selectedBedType = null;
    String linenStoreStr = request.getParameter("phStore");
    if (linenStoreStr != null && linenStoreStr.equals("")) {
      linenStoreStr = null;
    }

    WardNames wn = null;
    BedNames bn = null;


    String wardStatus = wf.getWardStatus();
    wn = new WardNames();
    wn.setWardNo(null);// new ward
    wn.setStatus(wardStatus);
    wn.setWardName(wf.getWardName());
    wn.setDescription(wf.getDescription());
    wn.setCenter_id(wf.getCenter_id());
    wn.setAllowedGender(wf.getAllowedGender());

    if (linenStoreStr != null) {
      wn.setLinenStore(Integer.parseInt(linenStoreStr));
    } else {
      wn.setLinenStore(-9);
    }

    String[] bedType = wf.getBedType();
    ArrayList<BedNames> bnList = new ArrayList<>();
    for (int i = 0; i < bedType.length; i++) {
      for (int j = 0; j < addBeds[i]; j++) {
        bn = new BedNames();
        bn.setWardNo(null);
        bn.setBedType(bedType[i]);
        selectedBedType = bn.getBedType();
        bn.setOccupancy("N");
        bnList.add(bn);
      }
    }

    ActionRedirect redirect = null;
    String wardId = WardAndBedMasterBo.insertNewWardDetails(wn, bnList, selectedBedType);
    FlashScope flash = FlashScope.getScope(request);
    if (wardId != null && !wardId.equals("")) {
      flash.success("Ward Details are Inserted Successfully..");
      redirect = new ActionRedirect(mapping.findForward("showWardRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("wardId", wardId);
      return redirect;
    } else {
      flash.error("Ward name already exists ..");
    }
    redirect = new ActionRedirect(mapping.findForward("wardListRedirect"));
    redirect.addParameter("method", "getNewWardScreen");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Edits the ward details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward editWardDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    WardAndBedMasterForm wf = (WardAndBedMasterForm) form;

    String linenStoreStr = request.getParameter("phStore");
    if (linenStoreStr != null && linenStoreStr.equals("")) {
      linenStoreStr = null;
    }

    ArrayList<BedNames> bnList = new ArrayList<>();

    WardNames wn = null;
    BedNames bn = null;

    wn = new WardNames();

    String wardNo = wf.getWardId();
    int[] addBeds = wf.getNoOfBedToAdd();
    String wardStatus = wf.getWardStatus();
    String description = wf.getDescription();

    wn.setWardNo(wardNo);
    wn.setStatus(wardStatus);
    wn.setDescription(description);
    wn.setAllowedGender(wf.getAllowedGender());

    if (linenStoreStr != null) {
      wn.setLinenStore(Integer.parseInt(linenStoreStr));
    } else {
      wn.setLinenStore(-9);
    }

    String[] bedType = wf.getBedType();
    for (int i = 0; i < bedType.length; i++) {
      for (int j = 0; j < addBeds[i]; j++) {
        bn = new BedNames();
        bn.setWardNo(wardNo);
        bn.setBedType(bedType[i]);
        bn.setOccupancy("N");
        bnList.add(bn);
      }
    }

    boolean status = WardAndBedMasterBo.editWardDetails(wn, bnList);
    FlashScope flash = FlashScope.getScope(request);

    if (status) {
      flash.success("Ward Details are Updated Successfully..");
    } else {
      flash.error("Faild to Update Ward Details ..");
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showWardRedirect"));
    redirect.addParameter("wardId", wardNo);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Gets the bed names.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the bed names
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward getBedNames(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String wardId = request.getParameter("wardId");
    String bedType = request.getParameter("bedType");

    List<BasicDynaBean> al = WardAndBedMasterDao.getWarDetails(wardId);
    request.setAttribute("wardDetils", al);

    if (!al.isEmpty()) {
      String wardName = (String) al.get(0).get("ward_name");
      String status = (String) al.get(0).get("status");
      String description = (String) al.get(0).get("description");

      request.setAttribute("wardName", wardName);
      request.setAttribute("status", status);
      request.setAttribute("description", description);
      request.setAttribute("wardId", wardId);
      request.setAttribute("bedType", bedType);
    }

    PagedList pl = WardAndBedMasterDao.getBedNames(wardId, bedType, 1);
    request.setAttribute("bedTypeStatus", WardAndBedMasterDao.getBedTypeStatus(bedType));
    request.setAttribute("pagedList", pl);

    return mapping.findForward("getBedNames");
  }

  /**
   * Update bed names.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the sql exception
   */
  public ActionForward updateBedNames(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    WardAndBedMasterForm wf = (WardAndBedMasterForm) form;

    String[] bedStatus = wf.getBedStatus();
    String[] bedName = wf.getBedName();
    String[] bedId = wf.getBedId();
    String bedType = wf.getBedTypeToUpdate();
    String wardNo = wf.getWardId();

    BedNames bn = null;
    ArrayList<BedNames> al = new ArrayList<>();

    for (int i = 0; i < bedName.length; i++) {
      bn = new BedNames();
      bn.setBedName(bedName[i]);
      bn.setBedId(Integer.parseInt(bedId[i]));
      bn.setBedType(bedType);
      bn.setStatus(bedStatus[i]);
      bn.setWardNo(wardNo);

      al.add(bn);
    }

    StringBuilder error = new StringBuilder();
    boolean status = WardAndBedMasterBo.updateBedNamesDetails(al, error);
    FlashScope flash = FlashScope.getScope(request);

    if (status) {
      flash.success("BedNames are Updated Successfully..");
    } else {
      flash.error("Failed to Update Bed Details .." + error.toString());
    }

    ActionRedirect redirect = new ActionRedirect(
        mapping.findForwardConfig("updateFailureRedirect"));
    redirect.addParameter("wardId", wardNo);
    redirect.addParameter("bedType", bedType);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Checks if is occupied.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the sql exception
   * @throws IOException the io exception
   */
  public ActionForward isOccupied(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    String wardId = request.getParameter("wardId");
    String bedId = request.getParameter("bedId");
    String isOccupied = null;

    if (wardId != null && !wardId.equals("")) {
      List<BasicDynaBean> list = WardAndBedMasterDao.getAllBedIds(wardId);
      Iterator it = list.iterator();
      while (it.hasNext()) {
        BasicDynaBean bean = (BasicDynaBean) it.next();
        isOccupied = WardAndBedMasterDao
            .getOccupancy(Integer.parseInt(bean.get("bed_id").toString()));
        if (isOccupied.equals("occupied")) {
          break;
        }
      }

    } else {
      isOccupied = WardAndBedMasterDao.getOccupancy(Integer.parseInt(bedId));
    }
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(isOccupied);

    return null;
  }

}
