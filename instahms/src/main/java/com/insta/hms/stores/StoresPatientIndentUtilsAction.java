package com.insta.hms.stores;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresPatientIndentUtilsAction extends DispatchAction {

  @IgnoreConfidentialFilters
  public ActionForward getItemDetails(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    String item_name = req.getParameter("itemname");
    String store_id = req.getParameter("store_id");
    BasicDynaBean storeDetails = StoreDAO.findByStore(Integer.parseInt(store_id));
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    Map map = null;
    List<BasicDynaBean> l = StockEntryDAO.getItemDetails(item_name, Integer.parseInt(store_id),
        healthAuthority);
    if (l != null && l.size() > 0) {
      map = l.get(0).getMap();
    }
    JSONSerializer js = new JSONSerializer();
    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.setContentType("application/json");
    resp.getWriter().write(js.serialize(map));
    resp.flushBuffer();
    return null;
  }

}
