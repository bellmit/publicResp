package com.insta.hms.integration.insurance;

import com.insta.hms.integration.insurance.dha.DhaInsurancePlugin;
import com.insta.hms.integration.insurance.shafafiya.HaadInsurancePlugin;

/**
 * The Class InsurancePluginManager.
 */
public class InsurancePluginManager {

  /**
   * Gets the plugin.
   *
   * @param caseDetails the case details
   * @return the plugin
   */
  public InsurancePlugin getPlugin(InsuranceCaseDetails caseDetails) {
    // iterate through all plugins and their registration information
    /*
     * InsurancePlugin [] plugins = getAllInstalledPlugIns(); for (InsurancePlugin plugin :
     * plugins) { //future development //PluginMatcher matcher = plugin.getMatcher(); // see if
     * the insurance case details match //boolean matched = matcher.match(caseDetails); //if
     * (matched) return plugin; return plugin; }
     */
    if ("DHA".equals(caseDetails.getHealthAuthority())) {
      return new DhaInsurancePlugin();
    } else if ("HAAD".equals(caseDetails.getHealthAuthority())) {
      return new HaadInsurancePlugin();
    }
    return null;
  }

  /**
   * Gets the all installed plug ins.
   *
   * @return the all installed plug ins
   */
  private InsurancePlugin[] getAllInstalledPlugIns() {

    return new InsurancePlugin[] {new HaadInsurancePlugin(), new DhaInsurancePlugin()};
  }

}
