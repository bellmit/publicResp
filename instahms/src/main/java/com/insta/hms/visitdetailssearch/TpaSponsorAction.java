package com.insta.hms.visitdetailssearch;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import flexjson.JSONSerializer;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class TpaSponsorAction.
 */
public class TpaSponsorAction extends BaseAction {

  /**
   * Gets the details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the details
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getdetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws SQLException, IOException, ParseException {

    Date currentTime = new Date();
    if (null != request.getHeader("If-Modified-Since")) {
      String lastModifiedTimeStr = request.getHeader("If-Modified-Since");
      DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
      Date lastModifiedTime = (Date) formatter.parse(lastModifiedTimeStr);
      lastModifiedTime.setSeconds(lastModifiedTime.getSeconds() + 3600);
      if (lastModifiedTime.getTime() > currentTime.getTime()) {
        response.setStatus(304);
        return null;
      } else {
        return getDetails(response, currentTime);
      }
    }
    return getDetails(response, currentTime);
  }

  /**
   * Gets the details.
   *
   * @param response
   *          the response
   * @param currentTime
   *          the current time
   * @return the details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  private ActionForward getDetails(HttpServletResponse response, Date currentTime)
      throws IOException, SQLException {
    response.setContentType("text/javascript");

    response.setHeader("Last-modified", currentTime.toString());
    response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=3600");

    response.getWriter().write("var  tpasponsorList= ");
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(
        ConversionUtils.listBeanToListMap(new TpaMasterDAO().listAll(null, "tpa_name")),
        response.getWriter());
    response.getWriter().write(";");
    return null;
  }

}
