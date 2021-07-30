package com.insta.hms.messaging.providers;

import com.bob.hms.common.RequestContext;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DoctorDataProvider.
 */
public class DoctorDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(DoctorDataProvider.class);

  /** The Constant THIS_NAME. */
  private static final String THIS_NAME = "Doctors";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";
  
  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";
  
  /** The Constant fromTables_specific_center. */
  private static final String fromTables_specific_center = " from (SELECT distinct"
      + " d.doctor_id as key,  " + "'DOCTOR' as receipient_type__, d.doctor_id as receipient_id__, "
      + "d.doctor_name as recipient_name, "
      + "(CASE WHEN d.doctor_mail_id = '' THEN NULL ELSE d.doctor_mail_id END) as recipient_email, "
      + "(CASE WHEN d.doctor_mobile = '' THEN NULL ELSE d.doctor_mobile END) as recipient_mobile, "
      + "d.doctor_address as doctor_address, "
      + "d.status as doctor_status, dd.dept_name as doctor_dept, dd.dept_id as dept_id__, "
      + "d.doctor_type as doctor_type, dcm.center_id::text as center_id  "
      + "from doctors d LEFT JOIN department dd ON (d.dept_id = dd.dept_id) "
      + "LEFT JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)) " + "as foo";

  /** The Constant fromTables_default_center. */
  private static final String fromTables_default_center = " from (SELECT"
      + " distinct d.doctor_id as key,  "
      + "'DOCTOR' as receipient_type__, d.doctor_id as receipient_id__, "
      + "d.doctor_name as recipient_name, "
      + "(CASE WHEN d.doctor_mail_id = '' THEN NULL ELSE d.doctor_mail_id END) as recipient_email, "
      + "(CASE WHEN d.doctor_mobile = '' THEN NULL ELSE d.doctor_mobile END) as recipient_mobile, "
      + "d.doctor_address as doctor_address, "
      + "d.status as doctor_status, dd.dept_name as doctor_dept, dd.dept_id as dept_id__, "
      + "d.doctor_type as doctor_type  "
      + "from doctors d LEFT JOIN department dd ON (d.dept_id = dd.dept_id)) " + "as foo";

  /**
   * Instantiates a new doctor data provider.
   */
  public DoctorDataProvider() {
    super(THIS_NAME);
    Integer centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      setQueryParams(selectFields, selectCount, fromTables_specific_center, null);
    } else {
      setQueryParams(selectFields, selectCount, fromTables_default_center, null);
    }
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();
    Integer centerId = RequestContext.getCenterId();
    if (null != eventData) { // this is sent when the search filter is applied.
      addCriteriaFilter(eventData);
    }
    if (centerId != 0
        && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
      Map map = new HashMap();
      map.put("center_id", new String[] { centerId.toString() });
      addCriteriaFilter(map);
    }
    return super.getMessageDataList(ctx);
  }

}
