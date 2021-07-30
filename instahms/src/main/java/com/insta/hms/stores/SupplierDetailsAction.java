package com.insta.hms.stores;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.RelevantSorting;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

public class SupplierDetailsAction extends BaseAction {

  private static final JSONSerializer js = new JSONSerializer().exclude("class");

  @IgnoreConfidentialFilters
  public ActionForward getSuppliersByQuery(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, NumberFormatException, SQLException {
    String strCenterId = request.getParameter("centerId");
    String query = request.getParameter("query");
    String strLimit = request.getParameter("limit");
    if (StringUtils.isEmpty(strCenterId) || !NumberUtils.isNumber(strCenterId)
        || (!StringUtils.isEmpty(strLimit) && !NumberUtils.isNumber(strLimit))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }

    String responseContent;
    Integer centerId = Integer.parseInt(strCenterId);
    Integer limit = null;
    if (NumberUtils.isNumber(strLimit)) {
      limit = Integer.parseInt(strLimit);
    }

    Map<String, Object> responseMap = new HashMap<>();
    List<BasicDynaBean> suppliersList = PurchaseOrderDAO.filterSuppliersByQuery(centerId,
        query, limit);
    responseMap.put("result", ConversionUtils.listBeanToListMap(RelevantSorting
        .rankBasedSorting(suppliersList, query, "supplier_name_with_city")));
    responseContent = js.deepSerialize(responseMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(responseContent);
    response.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getSupplierById(ActionMapping mapping, ActionForm form,
      final HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException {
    String supplierCode = request.getParameter("supplierCode");

    if (StringUtils.isEmpty(supplierCode)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }

    String responseContent = js.deepSerialize(PurchaseOrderDAO.getSupplierById(supplierCode).getMap());

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(responseContent);
    response.flushBuffer();
    return null;
  }
}
