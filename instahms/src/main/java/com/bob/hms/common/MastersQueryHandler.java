/**
 *
 */

package com.bob.hms.common;

/**
 * The Class MastersQueryHandler.
 *
 * @author krishna
 */
public class MastersQueryHandler {

  public static final String getCityNames = "SELECT  CITY_NAME FROM CITY";
  public static final String getCityDetails = "SELECT C.CITY_ID, C.CITY_NAME,"
      + " S.STATE_ID FROM CITY C, STATE_MASTER S "
      + "WHERE C.STATE_ID = S.STATE_ID ORDER BY C.CITY_NAME";
  public static final String getCityIdAndNames = "SELECT CITY_ID,CITY_NAME"
      + " FROM CITY ORDER BY CITY_NAME";

  public static final String getGenDeptMaster = "SELECT DEPT_ID, DEPT_NAME, COUNTER_ID"
      + " FROM stores ORDER BY DEPT_NAME";
  public static final String getDeptIdsAndNames = "SELECT DEPT_ID,DEPT_NAME"
      + " FROM stores ORDER BY DEPT_NAME";
  public static final String getDeptName = "SELECT DEPT_NAME FROM stores WHERE DEPT_ID=?";

}
