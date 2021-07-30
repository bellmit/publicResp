package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.Dialysis.DialysisMachineMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class DialysisMachineShiftAction.
 */
public class DialysisMachineShiftAction extends DispatchAction {

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {

    String mrNo = req.getParameter("mr_no");
    String orderId = req.getParameter("order_id");
    DialysisSessionsDao dao = new DialysisSessionsDao();

    BasicDynaBean bean = dao.findByKey("order_id", Integer.parseInt(orderId));
    int visitCenter = dao.getVisitCenter(Integer.parseInt(orderId));
    req.setAttribute("machines", DialysisMachineMasterDAO.getMachines(visitCenter));
    req.setAttribute("original_machine_id", bean.get("machine_id"));
    req.setAttribute("order_id", orderId);
    req.setAttribute("mr_no", mrNo);
    req.setAttribute("dialysis_presc_id", req.getParameter("dialysis_presc_id"));

    return mapping.findForward("addshow");
  }

  /**
   * Shift.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward shift(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {

    String orderId = req.getParameter("order_id");
    String originalMachineId = req.getParameter("original_machine_id");
    String machineId = req.getParameter("machine_id");
    ActionRedirect redirect = new ActionRedirect("DialysisCurrentSessions.do?_method=list");
    FlashScope flash = FlashScope.getScope(req);
    String discardSessionData = req.getParameter("discard_session_details");
    GenericDAO daoParams = new GenericDAO("dialysis_session_parameters");
    DialysisSessionsDao dao = new DialysisSessionsDao();

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean machinebean = DialysisMachinesStatusDAO
          .checkForAlreadyAssigned(Integer.parseInt(machineId));

      if (machinebean != null && !machinebean.get("assigned_order_id").toString().equals("")
          && !machinebean.get("assigned_order_id").toString().equals(orderId)) {
        flash.put("error", "Selected Machine is already Assigned");
        redirect = new ActionRedirect("DialysisMachineShift.do?_method=show");
        redirect.addParameter("mr_no", req.getParameter("mr_no"));
        redirect.addParameter("order_id", orderId);
        redirect.addParameter("dialysis_presc_id", req.getParameter("dialysis_presc_id"));
        return redirect;
      }

      if (discardSessionData.equals("Y")) {
        daoParams.delete(con, "order_id", Integer.parseInt(orderId));
      }

      DialysisMachinesStatusDAO.unassignMachine(con, Integer.parseInt(originalMachineId));
      DialysisMachinesStatusDAO.assignMachine(con, Integer.parseInt(machineId),
          Integer.parseInt(orderId));
      dao.updateMachine(con, Integer.parseInt(machineId), Integer.parseInt(orderId));

      allSuccess = true;
      flash.put("success", "Machine Shifted successfully");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }
}
