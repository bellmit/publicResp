package com.insta.hms.integration.salucro;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.Map;


/**
 * The Class SalucroPaymentRepository.
 */
@Repository
public class SalucroPaymentRepository extends GenericRepository {

  /** The Constant QUERY_CONTEXT_PARAMETERS. */
  private static final String QUERY_CONTEXT_PARAMETERS = "parameters";

  /** The Constant QUERY_CONTEXT_PARAMETERS. */
  private static final String QUERY_CONTEXT_QUERY = "query";

  /** The named jdbc template. */
  private static NamedParameterJdbcTemplate namedJdbcTemplate;

  /** The Constant COUNTER_FIELDS. */
  private static final String COUNTER_FIELDS = "SELECT * " ;

  /** The Constant COUNT_FIELD. */
  private static final String COUNT_FIELD = "SELECT count(*) ";

  /** The Constant TABLES. */
  private static final String TABLES = " from "
      + " (select pt.*, r.receipt_id, r.mr_no, CASE WHEN r.receipt_type = 'R' THEN 'RECEIPT' "
      + " WHEN r.receipt_type = 'F' THEN 'REFUND' END as receipt_type "
      + " from payment_transactions pt LEFT JOIN receipts r "
      + " on ( pt.payment_transaction_id = r.payment_transaction_id)) as foo";



  /**
   * Instantiates a new salucro payment repository.
   */
  public SalucroPaymentRepository() {
    super("payment_transactions");
  }


  /** The Constant GET_TRANSACTION_DETAILS. */
  private static final String GET_TRANSACTION_DETAILS = "Select * from "
      + " payment_transactions where bill_no=?";

  /**
   * Gets the transaction id.
   *
   * @param billNo the bill no
   * @return the transaction id
   */
  public BasicDynaBean getTransactionId(String billNo) {
    String query = GET_TRANSACTION_DETAILS;
    return DatabaseHelper.queryToDynaBean(query, new Object[] { billNo });
  }

  /**
   * Insert a row in database return primary key value.
   *
   * @param bean the bean
   * @return the number of rows affected
   */
  public Integer insertPaymentTransaction(BasicDynaBean bean) {
    Map<String, Object> beanMap = bean.getMap();

    Map<String, Object> queryContext = getNamedParametersInsertQueryContext(beanMap);
    MapSqlParameterSource parameters = (MapSqlParameterSource) queryContext
        .get(QUERY_CONTEXT_PARAMETERS);
    String query = (String) queryContext.get(QUERY_CONTEXT_QUERY);
    if (null != query) {   
      String[] coulumnNames = new String[] { "payment_transaction_id" };
      return DatabaseHelper.insertAndReturnRowID(query, parameters, coulumnNames);
    }
    return 0;
  }

  /**
   * Gets the transaction deatils map.
   *
   * @param params the params
   * @param listingParams the listing params
   * @param configMap the configuration
   * @return the transaction map
   * @throws ParseException the parse exception
   */
  public PagedList fetchTransaction(
      Map<String, String[]> params, Map<LISTING, Object> listingParams,
      Map<String, Object> configMap ) throws ParseException {
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
    String sortField = (String) listingParams.get(LISTING.SORTCOL);
    boolean sortRev = (Boolean) listingParams.get(LISTING.SORTASC);

    SearchQueryAssembler qb = new SearchQueryAssembler(COUNTER_FIELDS, COUNT_FIELD, TABLES, null,
        sortField, sortRev, pageSize, pageNum);
    if (params != null) {
      if (params.containsKey("fromDate")
              && !params.get("fromDate")[0].trim().isEmpty()) {
        String fromDateStr = params.get("fromDate")[0];
        java.sql.Date fromDate = DateUtil.parseDate(fromDateStr);
        qb.addFilter(SearchQueryAssembler.DATE, "DATE(created_at)",">=",
            fromDate);
      }
      if (params.containsKey("toDate")
              && !params.get("toDate")[0].trim().isEmpty()) {
        String toDateStr = params.get("toDate")[0];
        java.sql.Date toDate = DateUtil.parseDate(toDateStr);
        qb.addFilter(SearchQueryAssembler.DATE, "DATE(created_at)","<=",
            toDate);
      }
      String roleId = (String) configMap.get(SalucroConstants.ROLE);
      if (!(roleId.equalsIgnoreCase(SalucroConstants.ADMINISTRATOR_ROLE_ID)
              || roleId.equalsIgnoreCase(SalucroConstants.INSTAADMIN_ROLE_ID))) {
        if (configMap.containsKey("username")
                && !configMap.get("username").toString().isEmpty()) {
          String username = (String) configMap.get("username");
          qb.addFilter(SearchQueryAssembler.STRING, "initiated_by","=",
              username);
        }
      }
      if (params.containsKey("transactionId")
              && !params.get("transactionId")[0].trim().isEmpty()) {
        String transactionId = params.get("transactionId")[0];
        qb.addFilter(SearchQueryAssembler.STRING, "transaction_id","=",
               transactionId);
      }
      qb.addFilter(QueryAssembler.STRING, "transaction_type","=",
          SalucroConstants.SALUCROPAYMENT);

    }
    qb.build();
    PagedList counterDetailList = null;
    counterDetailList = qb.getMappedPagedList();
    return counterDetailList;
  }
}
