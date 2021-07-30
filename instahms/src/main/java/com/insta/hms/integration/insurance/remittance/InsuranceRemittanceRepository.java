package com.insta.hms.integration.insurance.remittance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class InsuranceRemittanceRepository.
 */
@Repository
public class InsuranceRemittanceRepository extends GenericRepository {

  /**
   * Instantiates a new insurance remittance repository.
   */
  public InsuranceRemittanceRepository() {
    super("insurance_remittance");
  }

  /** The Constant SEARCH_FIELDS. */
  private static final String SEARCH_FIELDS =
      " SELECT ic.insurance_co_id, ic.insurance_co_name, ir.file_id, "
          + " ir.tpa_id, tp.tpa_name, ir.remittance_id, received_date::date,"
          + " file_name, ir.detail_level,ir.processing_status ";

  /** The Constant SEARCH_COUNT. */
  private static final String SEARCH_COUNT = " SELECT count(*) ";

  /** The Constant SEARCH_TABLE. */
  private static final String SEARCH_TABLE = " FROM insurance_remittance ir "
      + " LEFT JOIN insurance_company_master ic USING (insurance_co_id) "
      + " LEFT JOIN tpa_master tp ON (tp.tpa_id = ir.tpa_id) ";

  /**
   * Gets the remittance details.
   *
   * @param parameters the parameters
   * @param listingParameters the listing parameters
   * @return the remittance details
   */
  public PagedList getRemittanceDetails(Map<String, String[]> parameters,
      Map<LISTING, Object> listingParameters) {

    SearchQueryAssembler qb = null;
    qb = new SearchQueryAssembler(SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLE,
        listingParameters);
    qb.addFilterFromParamMap(parameters);
    qb.addSecondarySort("received_date", true);
    qb.addSecondarySort("remittance_id");
    qb.build();
    return qb.getMappedPagedList();
  }

  /** The Constant SEARCH_FIELD_DISTINCT_FILEID. */
  private static final String SEARCH_FIELD_DISTINCT_FILEID =
      " SELECT DISTINCT ON (ir.file_id) ic.insurance_co_id, ic.insurance_co_name, ir.file_id, "
          + " ir.tpa_id, tp.tpa_name, ir.remittance_id, received_date::date, file_name,"
          + " ir.detail_level,ir.processing_status ";

  /**
   * Gets the remittance details for online upload. similar to getRemittanceDetails() but
   * ignores files that have been processed using manual upload method.(they do not contain
   * file id)
   *
   * @param parameters the parameters
   * @param listingParameters the listing parameters
   * @return the remittance details for online upload
   */
  public PagedList getRemittanceDetailsForOnlineUpload(Map<String, String[]> parameters,
      Map<LISTING, Object> listingParameters) {

    SearchQueryAssembler qb = null;
    qb = new SearchQueryAssembler(SEARCH_FIELD_DISTINCT_FILEID, SEARCH_COUNT, SEARCH_TABLE,
        listingParameters);
    qb.addFilterFromParamMap(parameters);
    qb.appendToQuery(" ir.file_id IS NOT NULL ");
    qb.addSecondarySort("file_id", true);
    qb.addSecondarySort("remittance_id");
    qb.build();
    return qb.getMappedPagedList();
  }

  /**
   * Gets the processing statuses.
   *
   * @param statusList the status list
   * @return the processing statuses
   */
  public List<BasicDynaBean> getProcessingStatuses(List<String> statusList) {

    String[] placeHolderArr = new String[statusList.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);

    String getRemittanceStatusesQuery =
        "SELECT processing_status, file_id FROM insurance_remittance WHERE file_id IN ("
            + placeHolders + ")";
    return DatabaseHelper.queryToDynaList(getRemittanceStatusesQuery, statusList.toArray());

  }

  /** The Constant GET_REMITTANCE_FILENAME. */
  private static final String GET_REMITTANCE_FILENAME =
      " SELECT file_name from insurance_remittance where remittance_id = ? ";

  /**
   * Gets the file name.
   *
   * @param remittanceId the remittance id
   * @return the file name
   */
  public BasicDynaBean getFileName(int remittanceId) {
    return DatabaseHelper.queryToDynaBean(GET_REMITTANCE_FILENAME,
        new Object[] {remittanceId});
  }

  /** The etpa cre. */
  private static String ETPA_CRE =
      "SELECT tpa_name,tm.tpa_id FROM ha_tpa_code tc JOIN tpa_master tm ON (tm.tpa_id=tc.tpa_id)"
          + " WHERE tc.tpa_code = ? AND tc.health_authority = ?";

  /**
   * Gets the TPA info.
   *
   * @param tpaCode the tpa code
   * @param healthAuthority the health authority
   * @return the TPA info
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getTPAInfo(String tpaCode, String healthAuthority)
      throws SQLException {
    return DataBaseUtil.queryToDynaBean(ETPA_CRE, new Object[] {tpaCode, healthAuthority});
  }
}
