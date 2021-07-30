package com.insta.hms.mdm.savedsearches;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SearchParameters.
 *
 * @author krishnat
 */
public class SearchParameters {

  /** The fields. */
  protected Map<String, Parameter> fields = new HashMap<String, Parameter>();

  /**
   * Gets the parameters.
   *
   * @return the parameters
   */
  public Map<String, Parameter> getParameters() {
    return fields;
  }

  /**
   * Gets the param types.
   *
   * @return the param types
   */
  public Map<String, String[]> getParamTypes() {
    Map<String, String[]> typeMap = new HashMap<String, String[]>();
    for (Map.Entry<String, Parameter> entry : fields.entrySet()) {
      Parameter param = entry.getValue();
      if (param.getSqlType() != null && !"".equals(param.getSqlType())) {
        typeMap.put(entry.getKey(), new String[] {param.getSqlType()});
      }
    }
    return typeMap;
  }

  /**
   * Gets the operators.
   *
   * @return the operators
   */
  public Map<String, String[]> getOperators() {
    Map<String, String[]> opMap = new HashMap<String, String[]>();
    for (Map.Entry<String, Parameter> entry : fields.entrySet()) {
      Parameter param = entry.getValue();
      if (param.getOp() != null && !"".equals(param.getOp())) {
        opMap.put(entry.getKey(), new String[] {param.getOp()});
      }
    }
    return opMap;
  }

  /**
   * Gets the casts.
   *
   * @return the casts
   */
  public Map<String, String[]> getCasts() {
    Map<String, String[]> castMap = new HashMap<String, String[]>();
    for (Map.Entry<String, Parameter> entry : fields.entrySet()) {
      Parameter param = entry.getValue();
      if (param.getCast() != null && !"".equals(param.getCast())) {
        castMap.put(entry.getKey(), new String[] {param.getCast()});
      }
    }
    return castMap;
  }
}
