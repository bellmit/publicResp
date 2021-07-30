/**
 *
 */

package com.insta.hms.patientsdetailssearch;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;

import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PatientsDetailsSearchAction.
 *
 * @author krishna.t
 */
public class PatientsDetailsSearchAction extends DispatchAction {

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
  private void setAttributes(HttpServletRequest request) throws ServletException, IOException,
      SQLException, ParseException {

    request.setAttribute("countryList", js.serialize(CountryMasterDAO.getCountryList(true)));
    request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(true)));
    request.setAttribute("stateList", js.serialize(stateDao.getStateList(true)));
    Map customRegFieldsMap = PatientDetailsDAO.getCustomRegFieldsMap();
    request.setAttribute("customRegFieldsMap", customRegFieldsMap);
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("categoryList",
        js.serialize(ConversionUtils.listBeanToListMap(new PatientCategoryDAO().listAll())));
    request.setAttribute("customList1", js.serialize(ConversionUtils
        .listBeanToListMap(customList1Master.listAll())));
    request.setAttribute("customList2", js.serialize(ConversionUtils
        .listBeanToListMap(customList2Master.listAll())));
    request.setAttribute("customList3", js.serialize(ConversionUtils
        .listBeanToListMap(customList3Master.listAll())));
    request.setAttribute("customList4", js.serialize(ConversionUtils
        .listBeanToListMap(customList4Master.listAll(null, "custom_value"))));
    request.setAttribute("customList5", js.serialize(ConversionUtils
        .listBeanToListMap(customList5Master.listAll(null, "custom_value"))));
    request.setAttribute("customList6", js.serialize(ConversionUtils
        .listBeanToListMap(customList6Master.listAll(null, "custom_value"))));
    request.setAttribute("customList7", js.serialize(ConversionUtils
        .listBeanToListMap(customList7Master.listAll(null, "custom_value"))));
    request.setAttribute("customList8", js.serialize(ConversionUtils
        .listBeanToListMap(customList8Master.listAll(null, "custom_value"))));
    request.setAttribute("customList9", js.serialize(ConversionUtils
        .listBeanToListMap(customList9Master.listAll(null, "custom_value"))));
    request.setAttribute("centers", new CenterMasterDAO().getAllCentersExceptSuper());
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
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      ParseException {

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
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      ParseException {

    setAttributes(request);

    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(request
        .getParameterMap());
    PagedList pagedList = PatientsDetailsSearchDAO.searchPatientsDetails(request.getParameterMap(),
        listingParams);

    request.setAttribute("pagedList", pagedList);
    return mapping.findForward("list");
  }
}
