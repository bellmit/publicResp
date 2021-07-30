package com.insta.hms.common;

import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * The Class HibernateHelper. This is a bad class containing bad methods, avoid using these
 * functions as far as possible when using hibernate.
 */
public class HibernateHelper {

  /**
   * Generate next id for patterns in hosp_id_patterns.
   *
   * @param session
   *          the session
   * @param patternId
   *          the pattern id
   * @return the string
   */
  public static String generateNextId(Session session, String patternId) {
    Query query = session.createSQLQuery("select generate_id(?)");
    query.setString(0, patternId);
    return (String) query.uniqueResult();
  }

  /**
   * Generate next sequence id for patterns in unique_number.
   *
   * @return the string
   */
  public static String generateNextSequenceId(Session session, String sequenceName,
      String typeNumber) {
    BigInteger nextNumber;
    Query query = session.createSQLQuery(
        "SELECT nextval(?), prefix, pattern FROM unique_number WHERE type_number = ?");
    query.setString(0, sequenceName);
    query.setString(1, typeNumber);
    Object[] res = (Object[]) query.uniqueResult();
    nextNumber = (BigInteger) res[0];
    String prefix = (String) res[1];
    String pattern = (String) res[2];
    DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
    decimalFormat.applyPattern(prefix + pattern);
    return decimalFormat.format(nextNumber);
  }

}
