package com.insta.hms.cssd;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.ItemKitMaster.ItemKitMasterDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SurgeryKitAvailabilityAction extends BaseAction {

  JSONSerializer js = new JSONSerializer().exclude("class");

  /**
   * List.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    SurgeryKitAvailabilityDao dao = new SurgeryKitAvailabilityDao();
    Map params = getParameterMap(req);
    Map listing = ConversionUtils.getListingParameter(params);
    // no pagination since it will affect availability. Front end filters only if possible.
    listing.put(ConversionUtils.LISTING.PAGESIZE, 0);
    listing.put(ConversionUtils.LISTING.PAGENUM, 0);

    int centerId = (Integer) req.getSession().getAttribute("centerId");
    req.setAttribute("pagedList", dao.getScheduledOperations(params, listing, centerId));

    return am.findForward("list");
  }

  /**
   * Gets the active kit items stock script. Get a list of items in all kits, and their stock
   * availability as a script.
   *
   * @param mapping
   *          the ActionMapping
   * @param form
   *          the ActionForm
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the active kit items stock script
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getActiveKitItemsStockScript(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    int centerId = (Integer) req.getSession().getAttribute("centerId");
    List stock = new SurgeryKitAvailabilityDao().getActiveKitItemsStock(centerId);
    // convert to <store_id> : { <medicine_id> : { medicine_id: ??, qty: ?? } } and send
    sendScript(res, "stock", ConversionUtils.listBeanToMapMapMap(stock, "dept_id", "medicine_id"));

    return null;
  }

  /**
   * Gets the active kit details script.
   *
   * @param mapping
   *          the ActionMapping
   * @param form
   *          the ActionForm
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the active kit details script
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getActiveKitDetailsScript(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    List kitItems = new ItemKitMasterDAO().getActiveKitItems();
    // convert to a map of kit_id => [record1, record2 ..] and send
    sendScript(res, "kitDetails", ConversionUtils.listBeanToMapListMap(kitItems, "kit_id"));

    return null;
  }

  /**
   * Gets the sterile stores script.
   *
   * @param mapping
   *          the ActionMapping
   * @param form
   *          the ActionForm
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the sterile stores script
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getSterileStoresScript(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    int centerId = (Integer) req.getSession().getAttribute("centerId");
    List<BasicDynaBean> stores = StoreMasterDAO.getCenterSterileStores(centerId);

    sendScript(res, "stores", ConversionUtils.listBeanToListMap(stores));
    return null;
  }

  private void sendScript(HttpServletResponse res, String varName, Object object)
      throws IOException {
    res.setHeader("Cache-Control", "no-cache");
    res.setHeader("Expires", "0");
    res.setContentType("text/javascript");

    java.io.Writer writer = res.getWriter();
    writer.write("var ");
    writer.write(varName);
    writer.write(" = ");
    js.deepSerialize(object, writer);
  }
}
