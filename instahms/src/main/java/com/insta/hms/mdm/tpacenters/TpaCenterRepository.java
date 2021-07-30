package com.insta.hms.mdm.tpacenters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The Class TpaCenterRepository.
 */
@Repository
public class TpaCenterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new tpa center repository.
   */
  public TpaCenterRepository() {
    super("tpa_center_master", "tpa_center_id");
  }

  /** The Constant GET_XML_TPA_LIST. */
  private static final String GET_XML_TPA_LIST = 
      " SELECT * from tpa_center_master where status = ? and claim_format = ?";

  /**
   * Gets the tpa center list.
   *
   * @param xlmTpaKeyMap the xlm tpa key map
   * @return the tpa center list
   */
  public List<BasicDynaBean> getTpaCenterList(Map xlmTpaKeyMap) {
    String status = (String) xlmTpaKeyMap.get("status");
    String claimformat = (String) xlmTpaKeyMap.get("claim_format");

    return DatabaseHelper.queryToDynaList(GET_XML_TPA_LIST, new Object[] { status, claimformat });

  }

}
