package com.insta.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class JSONAction.
 */
public class JSONAction extends DispatchAction {

  /**
   * Masterdata.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward masterdata(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException {

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String dbtable = req.getParameter("master");
    String[] columns = req.getParameterValues("columns");
    String key = req.getParameter("key");
    String value = req.getParameter("value");

    GenericDAO dao = new GenericDAO(dbtable);
    List<BasicDynaBean> beans = dao.listAll(Arrays.asList(columns), key, value);
    Map result = new HashMap();
    result.put("result", ConversionUtils.copyListDynaBeansToMap(beans));

    JSONSerializer js = new JSONSerializer().exclude("class");
    res.getWriter().write(js.deepSerialize(result));
    res.flushBuffer();

    return null;
  }
}
