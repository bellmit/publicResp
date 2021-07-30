package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SelfPaySubmissionRepository.
 */
@Repository
public class PersonRegisterSubmissionDetailsRepository extends MasterRepository<String> {

  /**
   * Instantiates a new self pay submission repository.
   */
  public PersonRegisterSubmissionDetailsRepository() {
    super("person_register_submission_batch_details", "personregister_batch_details_id", null, 
        new String[] { "personregister_batch_details_id" });
  }

 
}
