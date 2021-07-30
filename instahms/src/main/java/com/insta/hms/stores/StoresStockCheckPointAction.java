package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StoresStockCheckPointAction extends BaseAction {
  
  private static final GenericDAO storeCheckPointMainDAO = new GenericDAO("store_checkpoint_main");


  /*
   * Stock check point scrren
   */
  @IgnoreConfidentialFilters
  public ActionForward viewCheckpoints(ActionMapping mapping, ActionForm fm,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    Map map = getParameterMap(request);
    PagedList list = StoresStockCheckPointDAO.searchchkpoints(map,
        ConversionUtils.getListingParameter(map));
    request.setAttribute("pagedList", list);
    return mapping.findForward("chkpointlist");
  }

  @IgnoreConfidentialFilters
  public ActionForward getChkpointDetailsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    int chkId = request.getParameter("_chkId") != null
        ? Integer.parseInt(request.getParameter("_chkId"))
        : 0;
    if (chkId > 0) {
      BasicDynaBean bean = storeCheckPointMainDAO.findByKey("checkpoint_id",
          chkId);
      request.setAttribute("chkdto", bean);
    }
    ArrayList<String> chkpoints = StoresStockCheckPointDAO.getChkpointNamesInMaster();
    request.setAttribute("chkpoints", chkpoints);
    return mapping.findForward("chkpointdetails");
  }

  @IgnoreConfidentialFilters
  public ActionForward saveChkpointDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    HttpSession session = request.getSession(false);
    Object username = session.getAttribute("userid");
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect("stockcheckpoint.do");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("_method", "viewCheckpoints");
    redirect.addParameter("sortOrder", "checkpoint_name");
    redirect.addParameter("sortReverse", "false");

    Connection con = null;
    boolean status = true;
    String error = "Transaction Failure";
    String success = null;
    String info = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = request.getParameterMap();
      List errors = new ArrayList();
      StoresStockCheckPointDAO dao = new StoresStockCheckPointDAO(con);
      int chkId = 0;
      BasicDynaBean bean = storeCheckPointMainDAO.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);
      if (request.getParameter("operation").equalsIgnoreCase("update")) {
        int key = Integer.parseInt(request.getParameter("checkpoint_id"));
        Map<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("checkpoint_id", key);
        if (status && storeCheckPointMainDAO.update(con, bean.getMap(), keys) > 0) {
          status = true;
          success = "Checkpoint updated Successfully";
          flash.put("success", success);
          con.commit();
        } else {
          status = false;
          con.rollback();
        }
      } else {
        String countQuery = "select count(*) from store_stock_details ";
        int seqCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(countQuery));
        if (seqCount == 0) {
          info = " stock is not available in store you can't create Checkpoint at this moment .....";
          flash.put("info", info);
        } else {
          bean.set("user_name", username.toString());
          chkId = dao.getNextCategoryId();
          bean.set("checkpoint_id", chkId);
          bean.set("checkpoint_date", DataBaseUtil.getDateandTime());
          if (status)
            status = storeCheckPointMainDAO.insert(con, bean);
          if (status)
            status = dao.insertChkpointDetail(chkId);
          if (status) {
            con.commit();
            success = "Checkpoint inserted Successfully";
            flash.put("success", success);
          } else {
            con.rollback();
            flash.put("error", error);
          }
        }
      }
    } catch (Exception e) {
      if (null != con) {
        con.rollback();
      }
      flash.put("error", error);
    } finally {
      DataBaseUtil.closeConnections(con, null);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }

  @IgnoreConfidentialFilters
  public ActionForward deleteChkpointDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    Connection con = null;
    String success = null;
    String error = null;
    boolean status = true;
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect("stockcheckpoint.do");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("_method", "viewCheckpoints");
    redirect.addParameter("sortOrder", "checkpoint_name");
    redirect.addParameter("sortReverse", "false");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      int chkId = Integer.parseInt(request.getParameter("_chkId"));
      BasicDynaBean bean = storeCheckPointMainDAO.findByKey("checkpoint_id", chkId);
      GenericDAO daosub = new GenericDAO("STORE_CHECKPOINT_DETAILS");
      if (status)
        status = daosub.delete(con, "CHECKPOINT_ID", bean.get("checkpoint_id"));
      if (status)
        status = storeCheckPointMainDAO.delete(con, "CHECKPOINT_ID", bean.get("checkpoint_id"));
      if (status) {
        con.commit();
        success = "" + bean.get("checkpoint_name") + " deleted Successfully...";
        flash.put("success", success);
      } else {
        status = false;
        con.rollback();
        error = "Transaction Failure";
        flash.put("error", error);
      }
    } catch (Exception e) {
      if (null != con) {
        con.rollback();
      }
      status = false;
      flash.put("error", error);
    } finally {
      DataBaseUtil.closeConnections(con, null);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }

}
