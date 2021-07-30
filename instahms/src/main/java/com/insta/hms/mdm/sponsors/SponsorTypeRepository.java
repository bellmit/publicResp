package com.insta.hms.mdm.sponsors;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class SponsorTypeRepository.
 */
@Repository
public class SponsorTypeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new sponsor type repository.
   */
  public SponsorTypeRepository() {
    super("sponsor_type", "sponsor_type_id", "sponsor_type_name");
  }

  /** The Constant GET_ALL_SPONSOR_TYPE_NAMES. */
  private static final String GET_ALL_SPONSOR_TYPE_NAMES =
      "select sponsor_type_name from sponsor_type where sponsor_type_id != ?";

  /**
   * Gets the ALL sponsor type names.
   *
   * @param sponsorId the sponsor id
   * @return the ALL sponsor type names
   */
  public List<BasicDynaBean> getAllSponsorTypeNames(int sponsorId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_SPONSOR_TYPE_NAMES, new Object[] {sponsorId});
  }
}
