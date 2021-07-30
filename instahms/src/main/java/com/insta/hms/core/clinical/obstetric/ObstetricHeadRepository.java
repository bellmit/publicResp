package com.insta.hms.core.clinical.obstetric;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class ObstetricHeadRepository.
 *
 * @author anupvishwas
 */

@Repository
public class ObstetricHeadRepository extends GenericRepository {

  /**
   * Instantiates a new obstetric head repository.
   */
  public ObstetricHeadRepository() {
    super("obstetric_headrecords");
  }

  /** The Constant GET_ALL_ACTIVE_OBSTETRIC_HEAD. */
  private static final String GET_ALL_ACTIVE_OBSTETRIC_HEAD = " SELECT ohr.obstetric_record_id,"
      + " ohr.field_g, ohr.field_p, ohr.field_l, ohr.field_a, ohr.username"
      + " FROM obstetric_headrecords ohr"
      + " JOIN patient_section_details psd ON (ohr.section_detail_id = psd.section_detail_id)"
      + " WHERE psd.mr_no = ? AND psd.section_id = -13 AND psd.section_status = 'A'";

  /**
   * Gets the all active obs head.
   *
   * @param mrNo
   *          the mr no
   * @return the all active obs head
   */
  public BasicDynaBean getAllActiveObsHead(String mrNo) {
    return DatabaseHelper.queryToDynaBean(GET_ALL_ACTIVE_OBSTETRIC_HEAD, mrNo);
  }

  /** The Constant GET_OBSTETRIC_HEAD. */
  private static final String GET_OBSTETRIC_HEAD = "SELECT ohr.obstetric_record_id,"
      + " ohr.field_g, ohr.field_p, ohr.field_l, ohr.field_a, ohr.username"
      + " FROM patient_section_details psd"
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id)"
      + " LEFT JOIN obstetric_headrecords ohr ON (ohr.section_detail_id = psd.section_detail_id)"
      + " WHERE psd.patient_id = ? AND #filter#=?"
      + " AND psd.item_type= ? AND psf.form_type= ? AND section_id=-13";

  /**
   * Gets the all obs head.
   *
   * @param parameter
   *          the parameter
   * @return the all obs head
   */
  public BasicDynaBean getAllObsHead(FormParameter parameter) {
    return DatabaseHelper.queryToDynaBean(
        GET_OBSTETRIC_HEAD.replace("#filter#", parameter.getFormFieldName()),
        parameter.getPatientId(), parameter.getId(), parameter.getItemType(),
        parameter.getFormType());
  }

  /** The Constant GET_ALL_OBSTETRIC_HEAD. */
  private static final String GET_ALL_OBSTETRIC_HEAD = " SELECT ord.*, psd.mr_no, "
      + " psd.finalized, psd.finalized_user, usr.temp_username "
      + " FROM obstetric_headrecords ord "
      + " JOIN patient_section_details psd ON (ord.section_detail_id = psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
      + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -13"
      + " AND  psf.form_id=? ";

  /**
   * Gets the all obstetric head details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param itemType
   *          the item type
   * @param formId
   *          the form id
   * @return the all obstetric head details
   */
  public List<BasicDynaBean> getAllObstetricHeadDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {

    return DatabaseHelper.queryToDynaList(GET_ALL_OBSTETRIC_HEAD, mrNo, patientId, itemId,
        genericFormId, itemType, formId);
  }
}
