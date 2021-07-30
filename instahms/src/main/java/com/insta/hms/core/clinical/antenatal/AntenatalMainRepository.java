package com.insta.hms.core.clinical.antenatal;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class AntenatalMainRepository.
 */
@Repository
public class AntenatalMainRepository extends GenericRepository {

  /**
   * Instantiates a new antenatal main repository.
   */
  public AntenatalMainRepository() {
    super("antenatal_main");
  }

  public static final String ANTENATAL_MAIN_ID = "antenatal_main_id";
  
  public static final String SECTION_DETAIL_ID = "section_detail_id";
  
  public static final String CLOSE_PREGNANCY = "close_pregnancy";
  
  public static final String CLOSE_PREGNANCY_USER = "close_pregnancy_user";
  
  public static final String CLOSE_PREGNANCY_DATE_TIME = "close_pregnancy_date_time";
  
  /** The delete antenatal main1. */
  private static final String DELETE_ANTENATAL_MAIN = "DELETE from antenatal_main"
      + " where section_detail_id = ?";

  /** The delete antenatal main2. */
  private static final String DELETE_ANTENATAL_MAIN_BY_ID = "DELETE from antenatal_main"
      + " where antenatal_main_id::character varying not in (?) AND section_detail_id = ?";

  /**
   * Delete antenatal main by id.
   *
   * @param antenatalMainIds
   *          the antenatal main ids
   * @param sectionDetailId
   *          the section detail id
   * @return the int
   */
  public int deleteAntenatalMainById(String antenatalMainIds, Object sectionDetailId) {
    if (antenatalMainIds.isEmpty()) {
      return DatabaseHelper.delete(DELETE_ANTENATAL_MAIN, sectionDetailId);
    } else {
      return DatabaseHelper.delete(DELETE_ANTENATAL_MAIN_BY_ID, antenatalMainIds, sectionDetailId);
    }
  }
}
