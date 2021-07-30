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

public class UserDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory.getLogger(UserDataProvider.class);

  private static final String THIS_NAME = "Users";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = " from ( SELECT u.emp_username as key,"
      + " u.emp_username as recipient_name, "
      + "'USER' as receipient_type__, u.emp_username as receipient_id__, "
      + "CASE WHEN u.email_id = '' THEN NULL ELSE u.email_id END as recipient_email, "
      + "CASE WHEN u.mobile_no = '' THEN NULL ELSE u.mobile_no END as recipient_mobile, "
      + "r.role_name as user_role, u.emp_status as emp_status, u.center_id::text as center_id "
      + "from u_user u " + "LEFT JOIN u_role r ON u.role_id = r.role_id ) as foo";

  public UserDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
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
