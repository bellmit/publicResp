package com.insta.hms.usermanager;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class UserDashBoardAction.
 */
public class UserDashBoardAction extends DispatchAction {
  private static final GenericDAO sampleCollectionCentersDAO =
      new GenericDAO("sample_collection_centers");

  /**
   * List.
   *
   * @param actionMapping
   *          the actionMapping
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping actionMapping, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, ParseException, Exception {

    String roleName = null;
    String userName = null;
    RoleForm roleForm = (RoleForm) af;

    if ((roleForm.getRoleName() != null) && !roleForm.getRoleName().equals("")) {
      roleName = roleForm.getRoleName();
    }
    if ((roleForm.getUserName() != null) && !roleForm.getUserName().equals("")) {
      userName = roleForm.getUserName();
    }

    int pageNum = 1;
    if (roleForm.getPageNum() != null && !roleForm.getPageNum().equals("")) {
      pageNum = Integer.parseInt(req.getParameter("pageNum"));
    }

    String formSort = req.getParameter("sortOrder");
    if (formSort != null) {
      if (formSort.equals("rolename")) {
        formSort = "role_name";
      }
      if (formSort.equals("username")) {
        formSort = "emp_username";
      }

    }

    List statusList = new ArrayList();
    if (roleForm.isActive()) {
      statusList.add("A");
    }
    if (roleForm.isInActive()) {
      statusList.add("I");
    }

    List userTypeList = new ArrayList();
    if (roleForm.isHospital()) {
      userTypeList.add("N");
    }
    if (roleForm.isPatientPortal()) {
      userTypeList.add("P");
    }
    if (roleForm.isDoctorPortal()) {
      userTypeList.add("D");
    }

    HttpSession session = req.getSession();
    int usercenterId = (Integer) session.getAttribute("centerId");
    String centerIdStr = req.getParameter("userCenter");
    if (usercenterId != 0) {
      centerIdStr = session.getAttribute("centerId").toString();
    }

    int userCollectionCenter = (Integer) session.getAttribute("sampleCollectionCenterId");
    String collectionCenter = req.getParameter("CollectionCenter");
    if (userCollectionCenter != -1) {
      collectionCenter = session.getAttribute("sampleCollectionCenterId").toString();
    }

    List<BasicDynaBean> collectionCenters =
        sampleCollectionCentersDAO.findAllByKey("center_id", usercenterId);
    req.setAttribute("collectionCenters", collectionCenters);
    BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
    req.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

    String hospRoleId = req.getParameter("hospital_roles_master");
    PagedList pagedList = UserDashBoardDAO.getUserDashBoard(roleName, pageNum, userName, formSort,
        roleForm.isSortReverse(), statusList, userTypeList, centerIdStr, collectionCenter,
        hospRoleId);
    req.setAttribute("pagedList", pagedList);

    JSONSerializer js = new JSONSerializer().exclude("class");

    List allRoleNames = UserDashBoardDAO.getAllRoleNames();
    req.setAttribute("allRoleNames", js.serialize(allRoleNames));

    List allUserNames = UserDashBoardDAO.getAllUserNames();
    req.setAttribute("allUserNames", js.serialize(allUserNames));
    req.setAttribute("genPrefs", GenericPreferencesDAO.getAllPrefs());
    req.setAttribute("userCenterId", usercenterId);
    req.setAttribute("userCollectionCenter",
        (Integer) (session.getAttribute("sampleCollectionCenterId")));

    return actionMapping.findForward("list");

  }

}
