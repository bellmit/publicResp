package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.paymentterms.PaymentTermsRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

@Deprecated
public class PaymentTermsMasterDAO {

  Connection con = null;
  public PaymentTermsMasterDAO() {
  }

  public PaymentTermsMasterDAO(Connection con) {
    this.con = con;
  }

  private static final String GETTEMPLATES = "SELECT TEMPLATE_CODE,TEMPLATE_NAME FROM PH_PAYMENT_TERMS  ORDER BY TEMPLATE_NAME";

  @MigratedTo(value = PaymentTermsRepository.class)
  public static ArrayList getTemplateNamesInMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GETTEMPLATES);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String MISC_EXT_QUERY_FIELDS = " SELECT *";

  private static final String MISC_EXT_QUERY_COUNT = " SELECT count(TEMPLATE_CODE) ";

  private static final String MISC_EXT_QUERY_TABLES = " FROM PH_PAYMENT_TERMS ";

  @MigratedTo(value = PaymentTermsRepository.class)
  public static PagedList searchPhterms(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, MISC_EXT_QUERY_FIELDS, MISC_EXT_QUERY_COUNT,
        MISC_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("template_code");
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }
  private static final String GETTEMPLATESDETAILS = "select template_name,template_code from PH_PAYMENT_TERMS";
  @MigratedTo(value = PaymentTermsRepository.class)
  public static java.util.List getTemplateNamesMaster() throws SQLException {
    return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GETTEMPLATESDETAILS));
  }

  public static final String GET_TEMPLATE_TEXT = " SELECT terms_conditions FROM ph_payment_terms WHERE template_code=?";

  @MigratedTo(value = PaymentTermsRepository.class)
  public static final String getTemplateText(String templateCode) throws SQLException {
    BasicDynaBean b = DataBaseUtil.queryToDynaBean(GET_TEMPLATE_TEXT, templateCode);
    if (b != null) {
      return (String) b.get("terms_conditions");
    } else {
      return null;
    }
  }

}
