package com.insta.hms.common;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.master.MasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class AbstractMasterAction.
 */
public abstract class AbstractMasterAction extends DispatchAction {

  /** The Constant ADD_SUCCESS_MSG_SUFFIX. */
  public static final String ADD_SUCCESS_MSG_SUFFIX = "added successfully";

  /** The Constant EDIT_SUCCESS_MSG_SUFFIX. */
  public static final String EDIT_SUCCESS_MSG_SUFFIX = "updated successfully";

  /** The Constant ERROR_UNKNOWN_MSG. */
  public static final String ERROR_UNKNOWN_MSG = "An unknown error occured";

  /** The Constant INVALID_PARAM_MSG. */
  private static final String INVALID_PARAM_MSG = "Incorrectly formatted values provided";

  /** The Constant MISSING_PATIENT_RECORD_PREFIX. */
  private static final String MISSING_PATIENT_RECORD_PREFIX = " does not exist";

  /**
   * Find.
   *
   * @param mapping the mapping
   * @param req     the req
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward find(ActionMapping mapping, HttpServletRequest req) throws Exception {

    String mrNo = req.getParameter("mr_no");
    String patientId = req.getParameter("patient_id");

    if ((patientId != null && !patientId.equals(""))) {
      boolean visitExists = new VisitDetailsDAO().exist("patient_id", patientId, false);
      if (visitExists) {
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
        redirect.addParameter("_method", "add");
        redirect.addParameter("patient_id", patientId);
        return redirect;
      }
    }

    if ((mrNo != null) && !mrNo.equals("")) {
      boolean patientExists = new PatientDetailsDAO().exist("mr_no", mrNo, false);
      if (patientExists) {
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
        redirect.addParameter("_method", "add");
        redirect.addParameter("mr_no", mrNo);
        return redirect;
      }
    }

    FlashScope flash = FlashScope.getScope(req);
    flash
        .put("error",
            ((null != patientId) ? "Visit ID " + patientId
                : (null != mrNo) ? "MR No " + mrNo : "Search Item ")
                + MISSING_PATIENT_RECORD_PREFIX);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    redirect.addParameter("_method", "find");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Method invoked when search action is invoked on an entity.
   *
   * @param mapping the mapping
   * @param req     the req
   * @return the action forward
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  protected ActionForward list(ActionMapping mapping, HttpServletRequest req)
      throws SQLException, ParseException {

    Map<String, String[]> paramMap = req.getParameterMap();
    MasterDAO theDao = getMasterDao();
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(paramMap);
    PagedList pagedList = theDao.search(paramMap, listingParams);
    req.setAttribute("pagedList", pagedList);
    getAutoLookupLists(req);
    return mapping.findForward("list");
  }

  /**
   * Method called when add / edit action is invoked on an entity. Implements the most common use
   * case of not involving Action Forms
   *
   * @param mapping the mapping
   * @param req     the req
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  protected ActionForward addShow(ActionMapping mapping, HttpServletRequest req)
      throws SQLException {

    MasterDAO theDao = getMasterDao();
    MasterDAO detailsDao = getDetailsDao();

    BasicDynaBean theBean = theDao.getBean();
    Map<String, String[]> map = req.getParameterMap();
    List errorFields = new ArrayList();
    ConversionUtils.copyToDynaBean(map, theBean, errorFields);
    String idColumnName = null;
    Object id = null;

    if (null != theDao) {
      idColumnName = theDao.getIdColumnName();
      if (null != idColumnName && !idColumnName.isEmpty()) {
        id = (theBean.get(idColumnName));
        if (null != id) {
          req.setAttribute("bean", theDao.findByKey(idColumnName, id));
          req.setAttribute(idColumnName, id);
        }
      }
    }
    if (null != detailsDao && null != idColumnName && null != id) {
      List<BasicDynaBean> detailsList = detailsDao.findAllByKey(idColumnName, id);
      req.setAttribute("detailsList", detailsList);
      JSONSerializer js = new JSONSerializer().exclude("class");
      String detailsJSON = js.deepSerialize(ConversionUtils.listBeanToListMap(detailsList));
      req.setAttribute("detailsListJSON", detailsJSON);
    }
    return mapping.findForward("addshow");
  }

  /**
   * Determines if the object is to be inserted or updated depending on the action method that is
   * called.
   *
   * @param mapping the mapping
   * @param req     the req
   * @return the action redirect
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected ActionRedirect doSave(ActionMapping mapping, HttpServletRequest req)
      throws SQLException, IOException {
    boolean createNew = "create".equalsIgnoreCase(getAction(mapping, req));
    return doSave(mapping, req, createNew);
  }

  /**
   * Calls the respective DAO class to insert or update an entity in the database. Validates the
   * data passed in parameters before passing it to the DAO. Returns the ActionRedirect after adding
   * requisite parameters depending on the result of the operation
   *
   * @param mapping   the mapping
   * @param req       the req
   * @param createNew - boolean indicating if a new entity should be inserted or updated
   * @return the action redirect
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected ActionRedirect doSave(ActionMapping mapping, HttpServletRequest req, boolean createNew)
      throws SQLException, IOException {
    Map<String, String[]> params = req.getParameterMap();
    List<String> errors = new ArrayList<String>();

    BasicDynaBean baseBean = mapToBean(params, errors);
    if (null == errors || errors.isEmpty()) {
      MasterDAO theDao = getMasterDao();
      if (null != theDao) {
        baseBean = createNew ? theDao.insert(baseBean, errors) : theDao.update(baseBean, errors);
      }
    }

    if (null == errors || errors.isEmpty()) {
      doSaveDetails(baseBean, params, errors, true);
    }

    req.setAttribute("bean", baseBean);
    return getRedirect(mapping, req, baseBean, errors);
  }

  /**
   * Do save details.
   *
   * @param parentBean the parent bean
   * @param params     the params
   * @param errors     the errors
   * @param softDelete the soft delete
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  protected void doSaveDetails(BasicDynaBean parentBean, Map params, List<String> errors,
      boolean softDelete) throws SQLException, IOException {
    MasterDAO detailDao = getDetailsDao();
    MasterDAO parentDao = getMasterDao();
    Object parentId = null;
    String parentKey = null;
    if (null != parentDao) {
      parentKey = parentDao.getIdColumnName();
      parentId = (null != parentKey) ? parentBean.get(parentKey) : null;
    }

    if (null != detailDao) {
      List<BasicDynaBean> newDetailBeans = mapNewBeans(params, errors);
      if (null == errors || errors.isEmpty()) {
        for (int i = 0; i < newDetailBeans.size(); i++) {
          BasicDynaBean bean = newDetailBeans.get(i);
          if (null != parentKey && null != parentId) {
            bean.set(parentKey, parentId);
          }
          detailDao.insert(bean, errors);
        }
      }
      List<BasicDynaBean> editedDetailBeans = mapEditedBeans(params, errors);
      if (null == errors || errors.isEmpty()) {
        for (int i = 0; i < editedDetailBeans.size(); i++) {
          detailDao.update(editedDetailBeans.get(i), errors);
        }
      }
      List<BasicDynaBean> deletedDetailBeans = mapDeletedBeans(params, errors);
      if (null == errors || errors.isEmpty()) {
        for (int i = 0; i < deletedDetailBeans.size(); i++) {
          if (softDelete) {
            detailDao.inactivate(deletedDetailBeans.get(i), errors);
          } else {
            detailDao.delete(deletedDetailBeans.get(i), errors);
          }
        }
      }
    }
  }

  /**
   * Validates the incoming data for against the data types of the corresponding database fields.
   *
   * @param params the params
   * @param errors the errors
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean validateData(Map params, List<String> errors) throws SQLException {
    return mapToBean(params, errors);
  }

  /**
   * Prepares and returns the ActionRedirect object with all parameters set based on the operation
   * and its result.
   *
   * @param mapping the mapping
   * @param req     the req
   * @param result  the result
   * @param errors  the errors
   * @return the redirect
   */
  public ActionRedirect getRedirect(ActionMapping mapping, HttpServletRequest req,
      BasicDynaBean result, List<String> errors) {

    boolean success = (null != result);
    List<String> errorList = null;
    Map redirectData = null;
    if (success) {
      redirectData = result.getMap();
    } else {
      redirectData = req.getParameterMap();
      errorList = errors;
    }

    return getRedirect(success, mapping, req, redirectData, errorList);

  }

  /**
   * Prepares and returns the ActionRedirect object with all parameters set based on the operation
   * and its result.
   *
   * @param success   the success
   * @param mapping   the mapping
   * @param req       the req
   * @param resultMap the result map
   * @param errors    the errors
   * @return the redirect
   */
  private ActionRedirect getRedirect(boolean success, ActionMapping mapping, HttpServletRequest req,
      Map resultMap, List<String> errors) {
    String message = getRedirectMessage(success, mapping, req, errors);
    ActionRedirect redirect = getForward(success, mapping, req);
    setRedirectParams(redirect, success, req, message, resultMap);
    return redirect;
  }

  /**
   * Returns the message string to be displayed to the user, when an insert or update operation is
   * complete.
   *
   * @param success the success
   * @param mapping the mapping
   * @param req     the req
   * @param errors  the errors
   * @return the redirect message
   */
  private String getRedirectMessage(boolean success, ActionMapping mapping, HttpServletRequest req,
      List<String> errors) {
    String action = getAction(mapping, req);
    MasterDAO dao = getMasterDao();
    String entityName = StringUtil.prettyName(dao.getTable());
    String message = (success)
        ? (entityName + " "
            + (("create".equalsIgnoreCase(action)) ? ADD_SUCCESS_MSG_SUFFIX
                : EDIT_SUCCESS_MSG_SUFFIX))
        : ((null == errors || errors.isEmpty()) ? ERROR_UNKNOWN_MSG : errors.get(0));
    return message;
  }

  /**
   * Returns the forward name corresponding to an action depending on whether the database operation
   * was successful or not.
   *
   * @param success the success
   * @param mapping the mapping
   * @param req     the req
   * @return the forward
   */
  private ActionRedirect getForward(boolean success, ActionMapping mapping,
      HttpServletRequest req) {
    String action = getAction(mapping, req);
    String forward = (success) ? "showRedirect"
        : ("create".equalsIgnoreCase(action)) ? "addRedirect" : "showRedirect";
    return new ActionRedirect(mapping.findForward(forward));
  }

  /**
   * Sets the common parameters to be set in the redirect object when an object is inserted and
   * updated. This includes the flash object with the message and the id of the entity that was
   * inserted or updated
   *
   * @param redirect the redirect
   * @param success  the success
   * @param req      the req
   * @param message  the message
   * @param result   the result
   */
  private void setRedirectParams(ActionRedirect redirect, boolean success, HttpServletRequest req,
      String message, Map result) {

    MasterDAO theDao = getMasterDao();
    Object id = result.get(theDao.getIdColumnName());
    if (null != id) {
      redirect.addParameter(theDao.getIdColumnName(), id);
    }
    FlashScope flash = FlashScope.getScope(req);
    if (success) {
      flash.info(message);
    } else {
      flash.error(message);
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
  }

  /**
   * Returns the struts action method name invoked by the URL.
   *
   * @param mapping the mapping
   * @param req     the req
   * @return the action
   */
  private String getAction(ActionMapping mapping, HttpServletRequest req) {

    String parameter = mapping.getParameter();
    Map<String, String[]> params = req.getParameterMap();
    Object[] actionObj = null;
    String action = null;

    if (null != parameter && null != params) {
      actionObj = (Object[]) params.get(parameter);
    }
    if (null != actionObj && actionObj.length > 0) {
      action = (String) actionObj[0];
    }
    return action;

  }

  /**
   * Creates a bean whose properties are set from the matching request parameters.
   *
   * @param parameterMap the parameter map
   * @param errors       the errors
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */

  protected final BasicDynaBean mapToBean(Map parameterMap, List<String> errors)
      throws SQLException {

    List<String> errorFields = new ArrayList<String>();
    MasterDAO theDao = getMasterDao();
    BasicDynaBean baseBean = theDao.getBean();
    ConversionUtils.copyToDynaBean(parameterMap, baseBean, errorFields);
    BasicDynaBean result = null;
    if (errorFields.isEmpty()) {
      result = baseBean;
    } else {
      // TODO : format the error fields and include it in the message
      errors.add(INVALID_PARAM_MSG);
    }
    return result;

  }

  /**
   * Map deleted beans.
   *
   * @param params the params
   * @param errors the errors
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> mapDeletedBeans(Map params, List<String> errors) throws SQLException {
    List<BasicDynaBean> returnList = new ArrayList<BasicDynaBean>();
    MasterDAO detailsDao = getDetailsDao();
    String detailsKey = detailsDao.getIdColumnName();
    String[] detailsId = (String[]) params.get(detailsKey);
    String delDetailsKey = "deleted";
    String[] delDetailsRecord = (String[]) params.get(delDetailsKey);
    if (null != detailsId) {
      for (int i = 0; i <= detailsId.length - 1; i++) {
        if (detailsId[i] == null || detailsId[i].equals("") || delDetailsRecord == null
            || delDetailsRecord[i] == null || !delDetailsRecord[i].equals("true")) {
          continue;
        }

        BasicDynaBean detailsBean = detailsDao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, detailsBean, errors);
        if (errors.isEmpty()) {
          returnList.add(detailsBean);
        } else {
          // TODO : format the error fields and include it in the message
          errors.add(INVALID_PARAM_MSG);
        }
      }
    }
    return returnList;

  }

  /**
   * Map edited beans.
   *
   * @param params the params
   * @param errors the errors
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> mapEditedBeans(Map params, List<String> errors) throws SQLException {
    List<BasicDynaBean> returnList = new ArrayList<BasicDynaBean>();
    MasterDAO detailsDao = getDetailsDao();
    String detailsKey = detailsDao.getIdColumnName();
    String[] detailsId = (String[]) params.get(detailsKey);
    String delDetailsKey = "deleted";
    String[] delDetailsRecord = (String[]) params.get(delDetailsKey);
    if (null != detailsId) {
      for (int i = 0; i <= detailsId.length - 1; i++) {
        if (detailsId[i] == null || detailsId[i].equals("") || (delDetailsRecord != null
            && delDetailsRecord[i] != null && delDetailsRecord[i].equals("true"))) {
          continue;
        }

        BasicDynaBean detailsBean = detailsDao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, detailsBean, errors);
        if (errors.isEmpty()) {
          returnList.add(detailsBean);
        } else {
          // TODO : format the error fields and include it in the message
          errors.add(INVALID_PARAM_MSG);
        }
      }
    }
    return returnList;
  }

  /**
   * Map new beans.
   *
   * @param params the params
   * @param errors the errors
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> mapNewBeans(Map params, List<String> errors) throws SQLException {
    List<BasicDynaBean> returnList = new ArrayList<BasicDynaBean>();
    // loop through the id column size : e.g sponsor_approval_detail_id.length
    // for each index, use copyIndexToDynaBean to get the bean - bean is detailBean
    // getDetailDao().getBean()
    // check if the id column is empty. If so, add it to the list.
    MasterDAO detailsDao = getDetailsDao();
    String detailsKey = detailsDao.getIdColumnName();
    String[] detailsId = (String[]) params.get(detailsKey);
    String delDetailsKey = "deleted";
    String[] delDetailsRecord = (String[]) params.get(delDetailsKey);
    if (null != detailsId) {
      for (int i = 0; i <= detailsId.length - 1; i++) {
        if (detailsId[i] == null || !detailsId[i].equals("")
            || delDetailsRecord[i].equals("true")) {
          continue;
        }

        BasicDynaBean detailsBean = detailsDao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, detailsBean, errors);
        if (errors.isEmpty()) {
          returnList.add(detailsBean);
        } else {
          // TODO : format the error fields and include it in the message
          errors.add(INVALID_PARAM_MSG);
        }
      }
    }
    return returnList;
  }

  /**
   * Must be overridden in the subclass. Should return a DAO class which handles all the CRUD
   * operations corresponding to the action
   * 
   * @return sub-type of MasterDAO
   */

  public abstract MasterDAO getMasterDao();

  /**
   * Gets the details dao.
   *
   * @return the details dao
   */
  public MasterDAO getDetailsDao() {
    return null;
  }

  /**
   * Gets the auto lookup lists.
   *
   * @param req the req
   * @return the auto lookup lists
   * @throws SQLException the SQL exception
   */
  protected void getAutoLookupLists(HttpServletRequest req) throws SQLException {
    Map<String, List<BasicDynaBean>> lookupListMap = new HashMap<String, List<BasicDynaBean>>();
    Map<String, List<BasicDynaBean>> lookupMaps = getLookupLists();
    if (null != lookupMaps) {
      for (String lookupKey : lookupMaps.keySet()) {
        List mapList = ConversionUtils.listBeanToListMap(lookupMaps.get(lookupKey));
        lookupListMap.put(lookupKey, mapList);
      }
      JSONSerializer js = new JSONSerializer().exclude("class");
      String serialized = js.deepSerialize(lookupListMap);
      req.setAttribute("lookupListMap", serialized);
    }
  }

  /**
   * Returns a map of record lists used in as data for auto-complete that are part of either a) the
   * search panel of the dash-board b) lookup / foreign key fields used in add / edit page.
   * Each entry in the map corresponds to one field with the a) key being the name of the table
   * field for which auto-complete is required b) value being the list of records that need to show
   * up in the auto-complete.
   * The list is expected to contain dyna-beans corresponding to the entity record Conversion of
   * this list to any other form is not necessary. Such conversions are handled by the base class.
   *
   * @return the lookup lists
   * @throws SQLException the SQL exception
   */
  public Map<String, List<BasicDynaBean>> getLookupLists() throws SQLException {
    return Collections.emptyMap();
    /*
     * Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>(); MasterDAO
     * dao = getMasterDao(); String uniqueColumn = null; if (null != dao) { uniqueColumn =
     * dao.getUniqueColumnName(); List<BasicDynaBean> l = dao.listAll(uniqueColumn);
     * map.put(uniqueColumn, l); } return map;
     */
  }
}
