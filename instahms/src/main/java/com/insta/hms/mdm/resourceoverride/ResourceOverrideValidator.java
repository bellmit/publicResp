package com.insta.hms.mdm.resourceoverride;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.AccessDeniedException;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

import java.util.Map;

/** 
 * The Class ResourceOverrideValidator. 
 */
@Component
public class ResourceOverrideValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"res_sch_type", "res_sch_name"};

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"res_sch_id", "res_sch_type", "res_sch_name"};

  /** Instantiates a new resource override validator. */
  public ResourceOverrideValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
  
  /**
   * Check allow add bulk override.
   */
  public void checkAllowAddBulkOverride() {
    String rightToOverride = (String) ((Map) RequestContext.getSession()
        .getAttribute("urlRightsMap"))
        .get("res_availability");
    if (!"A".equals(rightToOverride)) {
      throw new AccessDeniedException("exception.access.denied");
    }
  }
}


