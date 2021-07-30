package com.insta.hms.integration.insurance;

/**
 * The Interface PluginMatcher.
 */
public interface PluginMatcher {

  /**
   * Match.
   *
   * @param caseDetails the case details
   * @return true, if successful
   */
  public boolean match(InsuranceCaseDetails caseDetails);

}
