package com.insta.hms.mdm.imagemarkers;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/**
 * The Class ImageMarkersRepository.
 */
@Repository
public class ImageMarkersRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new image markers repository.
   */
  public ImageMarkersRepository() {
    super("image_markers", "image_id", "label");
  }

  /** The Constant IMAGE_MARKERS_SEARCH. */
  private static final String IMAGE_MARKERS_SEARCH =
      "FROM (SELECT image_id, label, filename, status FROM image_markers) AS FOO";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(IMAGE_MARKERS_SEARCH);
  }
}
