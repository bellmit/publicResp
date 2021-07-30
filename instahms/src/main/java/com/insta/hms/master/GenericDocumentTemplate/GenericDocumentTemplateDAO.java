/**
 *
 */
package com.insta.hms.master.GenericDocumentTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import com.insta.hms.common.StringUtil;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class GenericDocumentTemplateDAO extends GenericDAO {

  static Logger log = LoggerFactory.getLogger(GenericDocumentTemplateDAO.class);

  /*
   * Constants used for sort order
   */
  public static final int FIELD_NONE = 0;
  public static final int FIELD_TEMPLATE_NAME = 1;
  public static final int FIELD_DOCUMENT_TYPE = 2;
  public static final int FIELD_FORMAT = 3;

  private static final String[] QUERY_FIELD_NAMES = { "", "template_name", "doc_type_name",
      "format" };

  private String table = null;

  public GenericDocumentTemplateDAO(String table) {
    super(table);
    this.table = DataBaseUtil.quoteIdent(table);
  }

  public static final String ALL_TEMPLATE_FIELDS = "select doc_type_id, doc_type_name, dept_name, foo.template_id, format, "
      + "template_name, status ";

  public static final String ALL_TEMPLATE_TABLES = " FROM (select dt.doc_type_id , dt.doc_type_name, "
      + " hvf.template_id,'doc_hvf_templates' as format, hvf.template_name, hvf.status, hvf.dept_name, hvf.specialized "
      + " FROM doc_type dt JOIN doc_hvf_templates as hvf ON dt.doc_type_id=hvf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rich.template_id,'doc_rich_templates' as format, rich.template_name,"
      + " rich.status, rich.dept_name, rich.specialized  FROM doc_type dt JOIN doc_rich_templates as rich ON dt.doc_type_id=rich.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, pdf.template_id,'doc_pdf_form_templates' as format, pdf.template_name, "
      + " pdf.status, pdf.dept_name, pdf.specialized FROM doc_type dt JOIN  doc_pdf_form_templates as pdf ON dt.doc_type_id=pdf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rtf.template_id ,'doc_rtf_templates' as format, rtf.template_name, "
      + " rtf.status,rtf.dept_name, rtf.specialized FROM doc_type dt JOIN doc_rtf_templates as rtf on dt.doc_type_id=rtf.doc_type) as foo";

  public static final String ALL_CENTER_TEMPLATE_FIELDS = "select distinct template_name, doc_type_id, doc_type_name, dept_name, foo.template_id, format, "
      + "status, templ_cen_status ";

  public static final String ALL_CENTER_TEMPLATE_TABLES = " FROM (select dt.doc_type_id , dt.doc_type_name, "
      + " hvf.template_id,'doc_hvf_templates' as format, 'H' as type, hvf.template_name, hvf.status, dtcm.status as templ_cen_status, hvf.dept_name, hvf.specialized, dtcm.center_id "
      + " FROM doc_type dt JOIN doc_hvf_templates as hvf ON dt.doc_type_id=hvf.doc_type "
      + " JOIN doc_template_center_master dtcm on(hvf.template_id = dtcm.template_id AND dtcm.doc_template_type = 'H')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rich.template_id,'doc_rich_templates' as format, 'R' as type, rich.template_name,"
      + " rich.status, dtcm.status as templ_cen_status, rich.dept_name, rich.specialized, dtcm.center_id FROM doc_type dt JOIN doc_rich_templates as rich ON dt.doc_type_id=rich.doc_type "
      + " JOIN doc_template_center_master dtcm on(rich.template_id = dtcm.template_id AND dtcm.doc_template_type = 'R')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, pdf.template_id,'doc_pdf_form_templates' as format, 'P' as type, pdf.template_name, "
      + " pdf.status, dtcm.status as templ_cen_status, pdf.dept_name, pdf.specialized, dtcm.center_id FROM doc_type dt JOIN  doc_pdf_form_templates as pdf ON dt.doc_type_id=pdf.doc_type "
      + " JOIN doc_template_center_master dtcm on(pdf.template_id = dtcm.template_id AND dtcm.doc_template_type = 'P')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rtf.template_id ,'doc_rtf_templates' as format, 'T' as type, rtf.template_name, "
      + " rtf.status, dtcm.status as templ_cen_status, rtf.dept_name, rtf.specialized, dtcm.center_id FROM doc_type dt JOIN doc_rtf_templates as rtf on dt.doc_type_id=rtf.doc_type"
      + " JOIN doc_template_center_master dtcm on(rtf.template_id = dtcm.template_id AND dtcm.doc_template_type = 'T')"
      + ") as foo ";

  private static final String ALL_TEMPLATE_COUNT = " SELECT count(doc_type_id) ";

  public static PagedList getGenericDocTemplates(Map filterParams, Boolean specialization,
      Map<LISTING, Object> listingParams) throws SQLException, ParseException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = null;
    try {
      if (specialization) {
        qb = new SearchQueryBuilder(con, ALL_TEMPLATE_FIELDS, ALL_TEMPLATE_COUNT,
            ALL_TEMPLATE_TABLES, listingParams);
        qb.addFilterFromParamMap(filterParams);
        qb.addFilter(qb.BOOLEAN, "specialized", "=", specialization);
        qb.addSecondarySort("template_name");
      } else {
        int centerID = RequestContext.getCenterId();
        Integer templateId = null;
        if (filterParams.get("template_id") != null) {
          templateId = Integer.valueOf(((String[]) filterParams.get("template_id"))[0]);
          filterParams.remove("template_id");
        }
        qb = new SearchQueryBuilder(con, ALL_CENTER_TEMPLATE_FIELDS, ALL_TEMPLATE_COUNT,
            ALL_CENTER_TEMPLATE_TABLES, listingParams);
        qb.addFilterFromParamMap(filterParams);
        qb.addFilter(qb.BOOLEAN, "specialized", "=", specialization);
        qb.addSecondarySort("template_name");
        qb.addFilter(qb.INTEGER, "template_id", "=", templateId);
        if (centerID != 0) {
          List values = new ArrayList();
          values.add(0);
          values.add(centerID);
          qb.addFilter(qb.INTEGER, "center_id", "IN", values);
        }
        qb.addFilter(qb.STRING, "templ_cen_status", "=", "A");
      }
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      if (qb != null)
        qb.close();
      DataBaseUtil.closeConnections(con, null);
    }

  }

  private static String GET_TEMPLATES_OF_A_CENTER = ALL_CENTER_TEMPLATE_FIELDS
      + ALL_CENTER_TEMPLATE_TABLES
      + " where specialized=? AND status = ? AND templ_cen_status = ? ##CENTERFILTER## ";

  public static List<BasicDynaBean> getAllTemplatesBycenterFilter(Map filterParams,
      Boolean specialization) throws SQLException {
    List<BasicDynaBean> templateBeans;
    String query = GET_TEMPLATES_OF_A_CENTER;
    int centerID = RequestContext.getCenterId();
    int defaultCenterId = 0;
    if (centerID ==0) {
      query = query.replace("##CENTERFILTER##", "");
    } else {
      query = query.replace("##CENTERFILTER##", "AND center_id IN (?, ?)");
    }
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      ps.setBoolean(1, specialization);
      ps.setString(2, "A");
      ps.setString(3,"A");
      if (centerID != 0) {
        ps.setInt(4, defaultCenterId);
        ps.setInt(5, centerID);

      }
      templateBeans = DataBaseUtil.queryToDynaList(ps);
    }
    return templateBeans;
  }

  // public void
  private boolean empty(String str) {
    return StringUtil.isNullOrEmpty(str);
  }

  public boolean exist(Boolean specialized, String specializedDocType, String dept,
      Object templateId, String templateName) throws SQLException {
    StringBuilder query = new StringBuilder("SELECT template_name FROM " + table + " WHERE ");
    query.append(" specialized=? ").append(" AND upper(template_name)").append("=upper(?)");
    if (templateId != null)
      query.append(" AND template_id!=?");
    if (specialized && specializedDocType != null && !specializedDocType.equals(""))
      query.append(" AND doc_type=?");

    query.append(";");

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(query.toString());
      int i = 1;
      ps.setBoolean(i++, specialized);
      ps.setObject(i++, templateName);
      if (templateId != null)
        ps.setObject(i++, templateId);
      if (specialized && specializedDocType != null && !specializedDocType.equals(""))
        ps.setObject(i++, specializedDocType);

      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return false;

  }

  public static final String GET_TEMPLATES = ALL_TEMPLATE_FIELDS + ALL_TEMPLATE_TABLES
      + " WHERE  doc_type_id = ?  AND specialized=? AND status = ? ";

  public static List<BasicDynaBean> getTemplates(Boolean specialized, String specializedDocType,
      String status) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_TEMPLATES);
      ps.setString(1, specializedDocType);
      ps.setBoolean(2, specialized);
      ps.setString(3, status);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_OPERATION_TEMPLATES = " SELECT doc_type as doc_type_id, doc_type_name, dept_name, dtv.template_id, dtv.doc_format as format, template_name, dtv.status "
      + " FROM doc_all_templates_view dtv " + "	JOIN doc_type dt ON (dtv.doc_type=dt.doc_type_id) "
      + " 	JOIN operation_doc_templates_master odtm ON (odtm.template_id=dtv.template_id and odtm.format=dtv.doc_format) "
      + " WHERE odtm.op_id=?";

  /*
   * returns the templates associated with the operations.
   */
  public static List<BasicDynaBean> getOperationTemplates(String operationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_OPERATION_TEMPLATES);
      ps.setString(1, operationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
