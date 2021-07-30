package com.insta.hms.visitdetailssearch;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;

import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class VisitDetailsSearchAction.
 *
 * @author krishna.t
 */
public class VisitDetailsSearchAction extends DispatchAction {

  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  /** The area dao. */
  AreaMasterDAO areaDao = new AreaMasterDAO();

  /** The city dao. */
  CityMasterDAO cityDao = new CityMasterDAO();

  /** The state dao. */
  StateMasterDAO stateDao = new StateMasterDAO();
  
  private static final GenericDAO customList1Master = new GenericDAO("custom_list1_master");
  private static final GenericDAO customList2Master = new GenericDAO("custom_list2_master");
  private static final GenericDAO customList3Master = new GenericDAO("custom_list3_master");
  private static final GenericDAO customList4Master = new GenericDAO("custom_list4_master");
  private static final GenericDAO customList5Master = new GenericDAO("custom_list5_master");
  private static final GenericDAO customList6Master = new GenericDAO("custom_list6_master");
  private static final GenericDAO customList7Master = new GenericDAO("custom_list7_master");
  private static final GenericDAO customList8Master = new GenericDAO("custom_list8_master");
  private static final GenericDAO customList9Master = new GenericDAO("custom_list9_master");

  /**
   * Sets the attributes.
   *
   * @param request
   *          the new attributes
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private void setAttributes(HttpServletRequest request)
      throws ServletException, IOException, SQLException, ParseException {

    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    request.setAttribute("countryList", js.serialize(CountryMasterDAO.getCountryList(true)));
    request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(true)));
    request.setAttribute("stateList", js.serialize(stateDao.getStateList(true)));
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    Map customRegFieldsMap = PatientDetailsDAO.getCustomRegFieldsMap();
    request.setAttribute("customRegFieldsMap", customRegFieldsMap);
    request.setAttribute("ward_names", js.serialize(
        ConversionUtils.listBeanToListMap(BedMasterDAO.getAllWardNames(centerId, multiCentered))));
    request.setAttribute("bed_names", js.serialize(
        ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedNames(centerId, multiCentered))));
    request.setAttribute("bed_types",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedTypesList())));
    request.setAttribute("orgNameJSONList",
        js.serialize(ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations())));
    // request.setAttribute("tpasponsorList",
    // js.serialize(ConversionUtils.listBeanToListMap(new TpaMasterDAO().listAll(null,
    // "tpa_name"))));
    request.setAttribute("categoryList", js.serialize(ConversionUtils
        .listBeanToListMap(new PatientCategoryDAO().listAll(null, "category_name"))));
    request.setAttribute("customList1", js.serialize(
        ConversionUtils.listBeanToListMap(customList1Master.listAll(null, "custom_value"))));
    request.setAttribute("customList2", js.serialize(
        ConversionUtils.listBeanToListMap(customList2Master.listAll(null, "custom_value"))));
    request.setAttribute("customList3", js.serialize(
        ConversionUtils.listBeanToListMap(customList3Master.listAll(null, "custom_value"))));
    request.setAttribute("customList4", js.serialize(
        ConversionUtils.listBeanToListMap(customList4Master.listAll(null, "custom_value"))));
    request.setAttribute("customList5", js.serialize(
        ConversionUtils.listBeanToListMap(customList5Master.listAll(null, "custom_value"))));
    request.setAttribute("customList6", js.serialize(
        ConversionUtils.listBeanToListMap(customList6Master.listAll(null, "custom_value"))));
    request.setAttribute("customList7", js.serialize(
        ConversionUtils.listBeanToListMap(customList7Master.listAll(null, "custom_value"))));
    request.setAttribute("customList8", js.serialize(
        ConversionUtils.listBeanToListMap(customList8Master.listAll(null, "custom_value"))));
    request.setAttribute("customList9", js.serialize(
        ConversionUtils.listBeanToListMap(customList9Master.listAll(null, "custom_value"))));
  }

  /**
   * Show.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
          throws ServletException, IOException, SQLException, ParseException {

    setAttributes(request);
    return mapping.findForward("list");
  }

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
          throws ServletException, IOException, SQLException, ParseException {
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    Map<LISTING, Object> listingParams = ConversionUtils
        .getListingParameter(request.getParameterMap());
    PagedList list = null;
    try {
      list = VisitDetailsSearchDAO.searchVisitsDetails(request.getParameterMap(),
        listingParams, centerId);
    } catch (HMSException exc) {
      setAttributes(request);
      FlashScope flash = FlashScope.getScope(request);
      ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("list"));
      flash.put("error", "Invalid Column!");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    

    setAttributes(request);
    request.setAttribute("pagedList", list);
    return mapping.findForward("list");
  }
}
