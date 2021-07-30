package com.insta.hms.stores;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for items in the master. Note: this returns items in the MASTER, regardless of stock. For
 * items in stock in each store, please use MedicineCacheAction.
 */
public class ItemMasterCacheAction extends Action {

  /**
   *  Execute method of the action.
   */
  @IgnoreConfidentialFilters
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {

    String searchQuery = request.getParameter("query");
    if (!StringUtils.isNotBlank(searchQuery)) { 
      if (null != request.getHeader("If-Modified-Since")) {
        /*
        * This means that the browser is just requesting the same URL that it has received a response
        * to earlier, only to check if modified. Since we encode the timestamp in the request, if we
        * get a request, it CANNOT have been modified. So, just respond with "304 not modified"
        * without checking.
        */
        response.setStatus(304);
        return null;
      }
      // Last-Modified is required, cache-control is good to have to enable caching
      response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
    }

    response.setContentType("text/javascript");

    String addVarName = request.getParameter("addVarName");
    if (addVarName == null) {
      addVarName = "Y";
    }

    String retailableStr = request.getParameter("retailable");
    Boolean retailable = null; // default: no filter
    if (retailableStr != null && !retailableStr.equals("")) {
      retailable = retailableStr.equalsIgnoreCase("y");
    }

    String billableStr = request.getParameter("billable");
    Boolean billable = null; // default: no filter
    if (billableStr != null && !billableStr.equals("")) {
      billable = billableStr.equalsIgnoreCase("y");
    }

    String issueTypeStr = request.getParameter("issueType");
    String[] issueTypes = null; // default: no filter
    if (issueTypeStr != null && !issueTypeStr.equals("")) {
      issueTypes = issueTypeStr.split("");
    }

		Boolean isBarCodeSearch = false;
		if (StringUtils.isNotBlank(searchQuery)) {
		  isBarCodeSearch = Boolean.parseBoolean(request.getParameter("isBarCodeSearch"));
		}
    List<Map> medicineList = PharmacymasterDAO.getItemNamesMap(retailable, billable, issueTypes, 
        searchQuery, isBarCodeSearch);

    JSONSerializer js = new JSONSerializer().exclude("class");
    if (StringUtils.isNotBlank(searchQuery)) {
		  HashMap<String, List<?>> retVal = new HashMap<>();
		  retVal.put("result",medicineList);
		  js.deepSerialize(retVal, response.getWriter());
		  response.flushBuffer();

		  return null;
    }
    
    if (addVarName.equals("Y")) {
      response.getWriter().write("var jItemNames = ");
    }
    js.deepSerialize(medicineList, response.getWriter());
    if (addVarName.equals("Y")) {
      response.getWriter().write(";");
    }

    return null;
  }
}
