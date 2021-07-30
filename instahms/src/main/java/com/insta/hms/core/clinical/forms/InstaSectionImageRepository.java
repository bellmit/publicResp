package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * The Class InstaSectionImageRepository.
 *
 * @author krishnat
 */
@Repository
public class InstaSectionImageRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new insta section image repository.
   */
  public InstaSectionImageRepository() {
    super("patient_section_images", "image_id");
  }


  /**
   * Gets the images.
   *
   * @param imageIds the image ids
   * @return the images
   */
  public List<BasicDynaBean> getImages(List<Integer> imageIds) {
    if (imageIds == null || imageIds.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    StringBuilder query =
        new StringBuilder("SELECT image_id FROM patient_section_images WHERE image_id in (");
    boolean first = true;
    for (Integer imageId : imageIds) {
      if (!first) {
        query.append(" , ");
      }
      query.append("?");
      first = false;
    }
    query.append(")");
    return DatabaseHelper.queryToDynaList(query.toString(), imageIds.toArray());
  }
}
