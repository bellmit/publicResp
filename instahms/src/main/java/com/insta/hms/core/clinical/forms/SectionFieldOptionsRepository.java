package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SectionFieldOptionsRepository.
 *
 * @author krishnat
 */
@Repository
public class SectionFieldOptionsRepository extends GenericRepository {

  /**
   * Instantiates a new section field options repository.
   */
  public SectionFieldOptionsRepository() {
    super("patient_section_options");
  }

  /**
   * Mark not available.
   *
   * @param otherFields the other fields
   * @param sectionDetailId the section detail id
   */
  public void markNotAvailable(List<BasicDynaBean> otherFields, int sectionDetailId) {
    if (otherFields.isEmpty()) {
      String markquery = "update patient_section_options options set available='N'"
          + " from patient_section_fields psf "
          + " where section_detail_id=? and psf.field_detail_id=options.field_detail_id"
          + " and available='Y'";
      DatabaseHelper.update(markquery, new Object[] {sectionDetailId});
      return;
    }
    StringBuilder query =
        new StringBuilder("update patient_section_options options set available='N'"
            + " from patient_section_fields psf "
            + " where section_detail_id=? and  psf.field_detail_id=options.field_detail_id"
            + " and available='Y' and not (options.field_detail_id, option_id) in (");

    boolean first = true;
    Object[] objs = new Object[otherFields.size() * 2 + 1];
    int index = 0;
    objs[index++] = sectionDetailId;

    for (BasicDynaBean bean : otherFields) {
      if (!first) {
        query.append(",");
      }
      query.append("(?,?)");
      first = false;
      objs[index++] = bean.get("field_detail_id");
      objs[index++] = bean.get("option_id");
    }
    query.append(")");
    DatabaseHelper.update(query.toString(), objs);
  }

}
