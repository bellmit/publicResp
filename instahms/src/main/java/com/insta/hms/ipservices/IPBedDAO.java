package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class IPBedDAO.
 */
public class IPBedDAO {

  /**
   * Gets the date and time.
   *
   * @param con the con
   * @param mrno the mrno
   * @param patientId the patient id
   * @return the date and time
   * @throws SQLException the SQL exception
   */
  public Timestamp getDateAndTime(Connection con, String mrno, String patientId)
      throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("SELECT ADMIT_TIME FROM ADMISSION WHERE MR_NO=? AND PATIENT_ID=?")) {
      ps.setString(1, mrno);
      ps.setString(2, patientId);
      try (ResultSet rs = ps.executeQuery()) {
        Timestamp date = null;
        while (rs.next()) {
          date = rs.getTimestamp(1);
        }

        return date;
      }
    }
  }

  /**
   * Gets the ip bed details.
   *
   * @param con the con
   * @return the ip bed details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getIpBedDetails(Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(IP_BED_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
  
  /**
   * Gets the ip bed details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param status the status
   * @param state the state
   * @return the ip bed details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getIpBedDetails(Connection con, 
      String patientId, String status, String state)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_BED_DETAILS_STATE);
      ps.setString(1, patientId);
      ps.setString(2, status);
      ps.setString(3, state);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant IP_BED_DETAILS. */
  private static final String IP_BED_DETAILS = "  SELECT *,b.bill_no  "
      + " FROM ip_bed_details ipb  "
      + " LEFT JOIN bill_activity_charge bac ON (ipb.admit_id::text = bac.activity_id  "
      + " or ipb.ref_admit_id::text = bac.activity_id ) "
      + " LEFT JOIN bill_charge bc  ON (bac.charge_id = bc.charge_id "
      + " AND bac.activity_code = 'BED' AND bc.act_unit = 'Days')"
      + " JOIN bill b on(bc.bill_no = b.bill_no AND b.status = 'A')  "
      + " WHERE ipb.bed_state = 'O'";

  /**
   * Method returns beds retained by the patient in the current ip visit.
   *
   * @return List
   */
  private static final String GET_RETAINED_BEDS = " SELECT * FROM ip_bed_details ip  "
      + " JOIN bed_names bn using(bed_id) " + " JOIN ward_names wn using(ward_no)"
      + " WHERE patient_id=? AND ip.status='R'";

  /**
   * Gets the retained beds.
   *
   * @param patientId the patient id
   * @return the retained beds
   * @throws SQLException the SQL exception
   */
  public static List getRetainedBeds(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_RETAINED_BEDS, patientId);
  }

  /** The Constant BLOCKED_BEDS. */
  private static final String BLOCKED_BEDS = " select wn.ward_name,wn.ward_no,"
      + " b.bed_type,b.bed_name,b.bed_id,b.bed_ref_id "
      + " from bed_names  b "
      + " join ward_names wn on b.ward_no=wn.ward_no "
      + " join (select b.bed_id,b.bed_name,b.ward_no,b.bed_ref_id  from bed_names b"
      + " join ip_bed_details ip on (ip.patient_id = ? and bed_state ='O')"
      + " where    b.bed_id = ip.bed_id) as foo "
      + " on (b.bed_id = foo.bed_ref_id OR foo.bed_id = b.bed_ref_id )"
      + " WHERE b.status='A' ";

  /**
   * Gets the blocked beds.
   *
   * @param patientId the patient id
   * @return the blocked beds
   * @throws SQLException the SQL exception
   */
  public static List getBlockedBeds(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(BLOCKED_BEDS, patientId);

  }

  /**
   * Returns true is the patient retaines the bed or else false.
   *
   * @param mrno the mrno
   * @param patientid the patientid
   * @param bedId the bed id
   * @return true, if is retained
   * @throws SQLException the SQL exception
   */
  public static boolean isRetained(String mrno, String patientid, int bedId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT MRNO "
              + " FROM IP_BED_DETAILS WHERE bed_id=? "
              + " AND PATIENT_ID=? AND STATUS='R' AND MRNO=?");
      ps.setInt(1, bedId);
      ps.setString(2, patientid);
      ps.setString(3, mrno);
      String retainedpatient = DataBaseUtil.getStringValueFromDb(ps);
      return (retainedpatient != null);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant GET_BED_DETAILS. */
  public static final String GET_BED_DETAILS = "SELECT "
      + " A.mr_no,A.patient_id,A.admit_time,a.daycare_status,"
      + " A.estimated_days,A.isbaby,A.parent_id,A.BED_ID,"
      + " bn.bed_type as bed_type,w.ward_name,bn.bed_name,"
      + " ip.admit_id,A.daysorhrs,A.last_updated,"
      + " TO_CHAR(ip.start_date,'DD-MM-YYYY hh24:mm') as start_date,"
      + " bn.bed_ref_id,ip.bed_state,ip.status,w.ward_no "
      + " FROM ADMISSION A,bed_names bn,ward_names w,ip_bed_details ip  "
      + " WHERE a.PATIENT_ID=? and bn.bed_id=A.bed_id "
      + " and bn.ward_no = w.ward_no  and ip.patient_id= a.patient_id";

  /**
   * Gets the current beddetails.
   *
   * @param bed the bed
   * @param patientid the patientid
   * @return the current beddetails
   * @throws SQLException the SQL exception
   */
  public static BedDTO getCurrentBeddetails(BedAdmissionDTO bed, String patientid)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet set = null;
    BedDTO beddetails = new BedDTO();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_DETAILS + " ip.status  in ('C','A')");
      ps.setString(1, patientid);
      set = ps.executeQuery();
      while (set.next()) {
        beddetails.setMrno(set.getString(1));
        beddetails.setPatientid(set.getString(2));
        beddetails.setAdmitdate(set.getTimestamp(3).toString());
        beddetails.setDaycare(set.getString(4));
        beddetails.setEstimateddays(set.getFloat(5));
        beddetails.setBaby(!set.getString(6).isEmpty());
        beddetails.setParentid(set.getString(7));
        beddetails.setBed_id(set.getInt(8));
        beddetails.setBedtype(set.getString(9));
        beddetails.setWardname(set.getString("WARD_NAME"));
        beddetails.setBedname(set.getString("BED_NAME"));
        beddetails.setAdmitid(set.getInt("ADMIT_ID"));
        beddetails.setLastUpdated(set.getTimestamp(15));
        beddetails.setBed_ref_id(set.getInt("bed_ref_id"));
        beddetails.setBed_state(set.getString("bed_state"));
        beddetails.setStatus(set.getString("status"));
        beddetails.setWardNo(set.getString("ward_no"));
      }
      if (beddetails.getMrno() == null) {
        beddetails.setMrno(bed.getMrNo());
        beddetails.setPatientid(bed.getPatientid());
        beddetails.setBedtype(bed.getAdmitbedtype());
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, set);
    }
    return beddetails;

  }

  /**
   * Gets the current beddetails.
   *
   * @param patientid the patientid
   * @return the current beddetails
   * @throws SQLException the SQL exception
   */
  public static BedDTO getCurrentBeddetails(String patientid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet set = null;
    BedDTO beddetails = new BedDTO();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_DETAILS + " and ip.status in('C','A')");
      ps.setString(1, patientid);
      set = ps.executeQuery();
      while (set.next()) {
        beddetails.setMrno(set.getString(1));
        beddetails.setPatientid(set.getString(2));
        beddetails.setAdmitdate(set.getString("ADMIT_TIME"));
        beddetails.setDaycare(set.getString(4));
        beddetails.setEstimateddays(set.getFloat(5));
        beddetails.setBaby(!set.getString(6).isEmpty());
        beddetails.setParentid(set.getString(7));
        beddetails.setBed_id(set.getInt(8));
        beddetails.setBedtype(set.getString(9));
        beddetails.setWardname(set.getString("WARD_NAME"));
        beddetails.setBedname(set.getString("BED_NAME"));
        beddetails.setAdmitid(set.getInt("ADMIT_ID"));
        beddetails.setUnits(set.getString("DAYSORHRS"));
        beddetails.setLastUpdated(set.getTimestamp(14));
        beddetails.setBed_ref_id(set.getInt("bed_ref_id"));
        beddetails.setBed_state(set.getString("bed_state"));
        beddetails.setStatus(set.getString("status"));
        beddetails.setStartdate(set.getString("start_date"));
        beddetails.setWardNo(set.getString("ward_no"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, set);
    }
    return beddetails;

  }

  /** The Constant GET_IP_BED_DETAILS. */
  public static final String GET_IP_BED_DETAILS = "SELECT "
      + " p.mrno,p.patient_id,p.start_date,p.status,p.end_date,"
      + " p.admit_id,a.estimated_days,p.bed_id,bn.bed_type as bed_type,"
      + " w.ward_name,bn.bed_name,p.bed_state,"
      + " a.daycare_status,bn.bed_ref_id,p.is_bystander,a.bystander_bed_id  "
      + " FROM IP_BED_DETAILS p,bed_names bn,ward_names w,admission a "
      + " WHERE ADMIT_ID=? and bn.bed_id=p.bed_id and bn.ward_no = w.ward_no "
      + " and a.patient_id=p.patient_id";

  /**
   * Gets the beddetails.
   *
   * @param admitid the admitid
   * @return the beddetails
   * @throws SQLException the SQL exception
   */
  public static BedDTO getBeddetails(int admitid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet set = null;
    BedDTO beddetails = new BedDTO();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_IP_BED_DETAILS);
      ps.setInt(1, admitid);
      set = ps.executeQuery();
      while (set.next()) {
        beddetails.setMrno(set.getString("MRNO"));
        beddetails.setPatientid(set.getString("PATIENT_ID"));
        beddetails.setBedname(set.getString("BED_NAME"));
        beddetails.setStartdate(set.getString("START_DATE"));
        beddetails.setStatus(set.getString("STATUS"));
        beddetails.setWardname(set.getString("WARD_NAME"));
        beddetails.setEnddate(set.getString("END_DATE"));
        beddetails.setAdmitid(set.getInt("ADMIT_ID"));
        beddetails.setBedtype(set.getString("BED_TYPE"));
        beddetails.setBed_id(set.getInt("BED_ID"));
        beddetails.setDaycare(set.getString("DAYCARE_STATUS"));
        beddetails.setEstimateddays(set.getFloat("ESTIMATED_DAYS"));
        beddetails.setBed_state(set.getString("BED_STATE"));
        beddetails.setBed_ref_id(set.getInt("bed_ref_id"));
        beddetails.setIs_bystander(set.getBoolean("is_bystander"));
        beddetails.setBystander_bed_id(set.getInt("bystander_bed_id"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, set);
    }
    return beddetails;
  }

  /** The Constant UPDATE_ADMISSION. */
  public static final String UPDATE_ADMISSION = "UPDATE "
      + " ADMISSION SET estimated_days=?,daysorhrs=? ,last_updated=?,"
      + " bed_id=?,daycare_status=?,finalized_time=? ";

  /**
   * Update admission.
   *
   * @param con the con
   * @param bed the bed
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateAdmission(Connection con, BedDTO bed) throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    try {
      ps = con.prepareStatement(UPDATE_ADMISSION + " WHERE patient_id=?");
      ps.setFloat(1, bed.getEstimateddays());
      ps.setString(2, bed.getUnits());
      ps.setTimestamp(3, bed.getLastUpdated());
      ps.setInt(4, bed.getBed_id());
      ps.setString(5, bed.getDaycare());
      ps.setTimestamp(6, bed.getFinalizedTime());
      ps.setString(7, bed.getPatientid());
      if (ps.executeUpdate() > 0) {
        status = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /** The Constant UPDATE_IP_BED_DETAILS. */
  public static final String UPDATE_IP_BED_DETAILS = "UPDATE IP_BED_DETAILS SET END_DATE=?";

  /**
   * Update ip bed details.
   *
   * @param con the con
   * @param beds the beds
   * @param enddate the enddate
   * @param units the units
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateIpBedDetails(Connection con, ArrayList beds, Timestamp enddate,
      String units, String userName) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_IP_BED_DETAILS
        + ",status=?,BED_STATE=?,UPDATED_DATE=LOCALTIMESTAMP(0),USERNAME=? WHERE ADMIT_ID=?");
    boolean status = true;
    try {
      for (int i = 0; i < beds.size(); i++) {
        BedDTO bed = (BedDTO) beds.get(i);
        ps.setTimestamp(1, enddate);
        ps.setString(2, bed.getStatus());
        ps.setString(3, bed.getBed_state());
        ps.setString(4, userName);
        ps.setInt(5, bed.getAdmitid());
        ps.addBatch();
      }
      int[] result = ps.executeBatch();
      for (int i = 0; i < result.length; i++) {
        if (result[i] == 0) {
          status = false;
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Gets the beds.
   *
   * @param patientid the patientid
   * @return the beds
   * @throws SQLException the SQL exception
   */
  public List<BedDTO> getBeds(String patientid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList<BedDTO> list = new ArrayList<BedDTO>();
    BedDTO beddetails = new BedDTO();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT "
              + " p.mrno,p.patient_id,p.start_date,p.status,p.end_date,p.admit_id,"
              + " p.bed_id,bn.bed_type as bed_type,w.ward_name,bn.bed_name,"
              + " p.bed_state,bn.bed_ref_id, p.is_bystander "
              + " FROM IP_BED_DETAILS p,bed_names bn,ward_names w"
              + " WHERE PATIENT_ID=? and bn.bed_id=p.bed_id and bn.ward_no = w.ward_no");
      ps.setString(1, patientid);
      try (ResultSet set = ps.executeQuery()) {
        while (set.next()) {
          beddetails = new BedDTO();
          beddetails.setMrno(set.getString("MRNO"));
          beddetails.setPatientid(set.getString("PATIENT_ID"));
          beddetails.setBedname(set.getString("BED_NAME"));
          beddetails.setStartdate(set.getString("START_DATE"));
          beddetails.setStatus(set.getString("STATUS"));
          beddetails.setWardname(set.getString("WARD_NAME"));
          beddetails.setEnddate(set.getString("END_DATE"));
          beddetails.setAdmitid(set.getInt("ADMIT_ID"));
          beddetails.setBedtype(set.getString("BED_TYPE"));
          beddetails.setBed_id(set.getInt("BED_ID"));
          beddetails.setBed_state(set.getString("BED_STATE"));
          beddetails.setBed_ref_id(set.getInt("bed_ref_id"));
          beddetails.setIs_bystander(set.getBoolean("is_bystander"));
          list.add(beddetails);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant GET_IP_BED_LIST. */
  public static final String GET_IP_BED_LIST = "SELECT "
      + " p.mrno,p.patient_id,p.start_date,p.status,p.end_date,p.admit_id,"
      + " p.bed_id,bn.bed_type as bed_type,w.ward_name,bn.bed_name "
      + " FROM ip_bed_details p,bed_names bn,ward_names w"
      + " WHERE patient_id=? and bn.bed_id=p.bed_id and bn.ward_no = w.ward_no ";

  /**
   * Alternate function to the getBeds.
   *
   * @param visitId the visit id
   * @return the list of accupied beds
   * @throws SQLException the SQL exception
   */

  public static List<BasicDynaBean> getListOfAccupiedBeds(String visitId) throws SQLException {
    List<BasicDynaBean> al = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_IP_BED_LIST);
      ps.setString(1, visitId);
      al = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return al;
  }

  /** The Constant GET_ACTIVE_BED_DETAILS. */
  private static final String GET_ACTIVE_BED_DETAILS = " SELECT "
      + " ipb.*, bn.*, adm.daycare_status, adm.isbaby "
      + " FROM ip_bed_details ipb "
      + " JOIN bed_names bn ON (bn.bed_id = ipb.bed_id) "
      + " JOIN admission adm ON (adm.patient_id = ?) "
      + " WHERE ipb.patient_id=? AND (NOT is_bystander) AND ipb.status IN ('A','C')";

  /**
   * Gets the active bed details.
   *
   * @param patientId the patient id
   * @return the active bed details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getActiveBedDetails(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_ACTIVE_BED_DETAILS,
        new Object[] { patientId, patientId });
  }

  /**
   * Gets the active bed details.
   *
   * @param con the con
   * @param patientId the patient id
   * @return the active bed details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getActiveBedDetails(Connection con, String patientId)
      throws SQLException {
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      ps = con.prepareStatement(GET_ACTIVE_BED_DETAILS);
      ps.setString(1, patientId);
      ps.setString(2, patientId);
      bean = DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return bean;
  }

  /** The Constant GET_ADMISSION_DETAILS. */
  private static final String GET_ADMISSION_DETAILS = "SELECT ad.* "
      + " FROM admission ad WHERE ad.patient_id=? ";

  /**
   * Gets the admission details.
   *
   * @param visitId the visit id
   * @return the admission details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAdmissionDetails(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ADMISSION_DETAILS);
      ps.setString(1, visitId);

      List admList = DataBaseUtil.queryToDynaList(ps);
      if (admList != null && admList.size() > 0) {
        return (BasicDynaBean) admList.get(0);
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the admission details.
   *
   * @param cobn the cobn
   * @param visitId the visit id
   * @return the admission details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAdmissionDetails(Connection cobn, String visitId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ADMISSION_DETAILS);
      ps.setString(1, visitId);

      List admList = DataBaseUtil.queryToDynaList(ps);
      if (admList != null && admList.size() > 0) {
        return (BasicDynaBean) admList.get(0);
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CANCLE_BED. */
  public static final String CANCLE_BED = UPDATE_IP_BED_DETAILS
      + " ,status=? , BED_STATE='F',UPDATED_DATE=LOCALTIMESTAMP(0),USERNAME=? WHERE ADMIT_ID =?";

  /**
   * Cancle bed.
   *
   * @param con the con
   * @param beddetails the beddetails
   * @param userId the user id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean cancleBed(Connection con, BedDTO beddetails, String userId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CANCLE_BED);
      ps.setTimestamp(1, beddetails.getFinaliseddate());
      ps.setString(2, "X");
      ps.setString(3, userId);
      ps.setInt(4, beddetails.getAdmitid());
      return (ps.executeUpdate() > 0);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant ADMISSION_BED_DETAILS. */
  public static final String ADMISSION_BED_DETAILS = " SELECT a.mr_no,a.PATIENT_ID,"
      + " a.ADMIT_TIME,a.DAYCARE_STATUS,a.ESTIMATED_DAYS,a.ISBABY,"
      + " a.PARENT_ID,a.BED_ID ,a.DAYSORHRS,a.LAST_UPDATED "
      + " FROM ADMISSION a WHERE PATIENT_ID=?";

  /**
   * Gets the admission bed.
   *
   * @param patientId the patient id
   * @return the admission bed
   * @throws Exception the exception
   */
  public BedDTO getAdmissionBed(String patientId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    BedDTO beddetails = new BedDTO();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT *,bed_name,bed_type,ward_name FROM ADMISSION "
          + " JOIN bed_names using(bed_id) "
          + " join ward_names using(ward_no)  where patient_id=? ");
      ps.setString(1, patientId);
      rs = ps.executeQuery();

      while (rs.next()) {
        beddetails.setMrno(rs.getString("mr_no"));
        beddetails.setPatientid(rs.getString("patient_id"));
        beddetails.setAdmitdate(rs.getString("admit_date"));
        beddetails.setDaycare(rs.getString("daycare_status"));
        beddetails.setEstimateddays(rs.getFloat("estimated_days"));
        beddetails.setBaby(!rs.getString("isbaby").isEmpty());
        beddetails.setParentid(rs.getString("parent_id"));
        beddetails.setBed_id(rs.getInt("bed_id"));
        beddetails.setBedtype(rs.getString("bed_type"));
        beddetails.setWardname(rs.getString("ward_name"));
        beddetails.setBedname(rs.getString("bed_name"));
        beddetails.setLastUpdated(rs.getTimestamp("last_updated"));
      }
      return beddetails;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /** The Constant UPDATE_BED_OCCUPANCY. */
  private static final String UPDATE_BED_OCCUPANCY = "UPDATE "
      + " bed_names "
      + " SET occupancy = ? "
      + " WHERE bed_id = ?";

  /**
   * Update child beds.
   *
   * @param con the con
   * @param patientId the patient id
   * @param parentBedId the parent bed id
   * @param occupancy the occupancy
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateChildBeds(Connection con, String patientId, 
      int parentBedId, String occupancy)
      throws Exception {
    List<BasicDynaBean> childBeds = BedMasterDAO.getChildBeds(con, parentBedId);
    BasicDynaBean childBed = null;
    PreparedStatement ps = null;
    BasicDynaBean parentBed = getBedStatus(parentBedId);
    if (childBeds.size() <= 0
        || (parentBed.get("patient_id") != null 
        && !parentBed.get("patient_id").equals(patientId))) {
      // possible in case of a bed is not a suit bed || another patient occupies suit
      return true;
    }
    try {
      ps = con.prepareStatement(UPDATE_BED_OCCUPANCY);
      for (int i = 0; i < childBeds.size(); i++) {
        childBed = childBeds.get(i);

        ps.setString(1, occupancy);
        ps.setInt(2, (Integer) childBed.get("bed_id"));
        ps.addBatch();
      }
      int[] result = ps.executeBatch();
      return DataBaseUtil.checkBatchUpdates(result);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Update parent bed.
   *
   * @param con the con
   * @param parentBedId the parent bed id
   * @param occupancy the occupancy
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateParentBed(Connection con, int parentBedId, String occupancy)
      throws SQLException {
    PreparedStatement ps = null;
    if (occupancy.equals("N") && !isReleaseParentBed(con, parentBedId)) {
      // possible when any one child bed is blocked
      return true;
    }
    try {
      ps = con.prepareStatement(UPDATE_BED_OCCUPANCY);
      ps.setString(1, occupancy);
      ps.setInt(2, parentBedId);
      return ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Checks if is release parent bed.
   *
   * @param con the con
   * @param parentBedId the parent bed id
   * @return true, if is release parent bed
   * @throws SQLException the SQL exception
   */
  public boolean isReleaseParentBed(Connection con, int parentBedId) throws SQLException {
    BasicDynaBean childBed = null;
    boolean releaseParentBed = true;
    List<BasicDynaBean> childBeds = BedMasterDAO.getChildBeds(con, parentBedId);
    for (int i = 0; i < childBeds.size(); i++) {
      childBed = childBeds.get(i);
      if (((String) childBed.get("occupancy")).equals("Y")) {
        // can not release parent bed unless all child beds get released
        releaseParentBed = false;
        break;
      }
    }
    return releaseParentBed;
  }

  /**
   * Checks if is any child occupied.
   *
   * @param bedId the bed id
   * @param currentBedId the current bed id
   * @return true, if is any child occupied
   * @throws SQLException the SQL exception
   */
  public boolean isAnyChildOccupied(int bedId, int currentBedId) throws SQLException {
    Connection con = null;
    BasicDynaBean childBed = null;
    BasicDynaBean childBedStatus = null;
    boolean occupied = false;
    try {
      con = DataBaseUtil.getConnection();
      List<BasicDynaBean> childBeds = BedMasterDAO.getChildBeds(con, bedId);
      if (childBeds.size() <= 0) {
        return false;
      } else {
        for (int i = 0; i < childBeds.size(); i++) {
          childBed = childBeds.get(i);
          childBedStatus = getBedStatus((Integer) childBed.get("bed_id"));
          String bedStatus = (String) childBedStatus.get("status");
          int tempBedId = (Integer) childBedStatus.get("bed_id");
          if (currentBedId != tempBedId) {
            if (bedStatus != null
                && (bedStatus.equals("A") || bedStatus.equals("C") || bedStatus.equals("R"))) {
              occupied = true;
              break;
            }
          }
        }
      }
      return occupied;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the bed status.
   *
   * @param bedId the bed id
   * @return the bed status
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getBedStatus(int bedId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT * FROM bed_status_report WHERE bed_id = ? ");
      ps.setInt(1, bedId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Checks wether the visit has bystander bed or not. Which is use fule while shift back from icu
   * to normal bed .
   *
   * @param visitId the visit id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean hasBystander(String visitId) throws SQLException {
    List<BedDTO> bedsFortheVisit = getBeds(visitId);
    boolean hasbystanderBed = false;
    for (BedDTO bed : bedsFortheVisit) {
      if (bed.getBed_state().equals("O") && bed.isIs_bystander()) {
        hasbystanderBed = true;
        break;
      }
    }
    return hasbystanderBed;
  }

  /**
   * Checks wether passed bed is alive bystander or not.
   *
   * @param visitId          String
   * @param bedId          int
   * @return isBystanderBed boolean
   * @throws SQLException the SQL exception
   */
  public boolean isBystanderBed(String visitId, int bedId) throws SQLException {
    List<BedDTO> prevBedDetails = new IPBedDAO().getBeds(visitId);
    boolean isBystanderBed = false;
    for (BedDTO bedDetails : prevBedDetails) {
      if (bedDetails.getBed_id() == bedId && bedDetails.getBed_state().equals("O")
          && bedDetails.isIs_bystander()) {
        isBystanderBed = true;
      }
    }
    return isBystanderBed;
  }

  /** The Constant GET_BED_DETAILS_STATE. */
  private static final String GET_BED_DETAILS_STATE = " SELECT * "
      + " FROM ip_bed_details "
      + " WHERE patient_id=? AND status=? AND bed_state=? ";

  /** The Constant VISIT_BEDS. */
  private static final String VISIT_BEDS = " SELECT * FROM ip_bed_details"
      + " JOIN bed_names using(bed_id)" + " JOIN bed_types on(bed_type_name = bed_type)"
      + " WHERE patient_id = ? ";

  /**
   * Gets the visit beds.
   *
   * @param con the con
   * @param patientId the patient id
   * @param bedState the bed state
   * @return the visit beds
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getVisitBeds(Connection con, String patientId, String bedState)
      throws SQLException {
    int idx = 1;
    try (PreparedStatement ps = con.prepareStatement(VISIT_BEDS
        + " AND ip_bed_details.status != 'X' " + (bedState == null ? "" : "AND bed_state = ?")
        + " ORDER BY admit_id ")) {
      ps.setString(idx++, patientId);
      if (bedState != null) {
        ps.setString(idx++, bedState);
      }
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /**
   * Gets the vist main beds.
   *
   * @param patientId the patient id
   * @return the vist main beds
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getVistMainBeds(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(VISIT_BEDS
          + " AND ip_bed_details.status != 'X' AND ref_admit_id is null ");
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the vist main beds.
   *
   * @param con the con
   * @param patientId the patient id
   * @return the vist main beds
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getVistMainBeds(Connection con, String patientId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(VISIT_BEDS
          + " AND ip_bed_details.status != 'X'  AND ref_admit_id is null" + " ORDER BY admit_id  ");
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the thread beds.
   *
   * @param con the con
   * @param patientId the patient id
   * @param admitId the admit id
   * @return the thread beds
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getThreadBeds(Connection con, String patientId, int admitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(VISIT_BEDS + " AND ip_bed_details.status != 'X' "
          + "AND ( admit_id = ? or ref_admit_id = ? ) ORDER BY admit_id ");
      ps.setString(1, patientId);
      ps.setInt(2, admitId);
      ps.setInt(3, admitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Brings list of bed lies in the specified period.
   *
   * @param con the con
   * @param from the from
   * @param to the to
   * @param admitId the admit id
   * @return the beds occupied btw
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getBedsOccupiedBtw(Connection con, Timestamp from, Timestamp to,
      int admitId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("  SELECT * FROM ip_bed_details "
              + "  JOIN bed_names using(bed_id) "
              + "  JOIN bed_types on(bed_type_name = bed_type)"
              + "  WHERE (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ? "
              + "  OR"
              + "  ? BETWEEN start_date AND end_date OR  ? BETWEEN start_date AND end_date ) "
              + "  AND (ref_admit_id = ? OR admit_id =? ) "
              + "  AND ip_bed_details.status != 'X' ORDER BY admit_id ");
      ps.setTimestamp(1, from);
      ps.setTimestamp(2, to);
      ps.setTimestamp(3, from);
      ps.setTimestamp(4, to);
      ps.setTimestamp(5, from);
      ps.setTimestamp(6, to);
      ps.setInt(7, admitId);
      ps.setInt(8, admitId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Same bed type.
   *
   * @param con the con
   * @param prvAdmitId the prv admit id
   * @param curBedId the cur bed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public boolean sameBedType(Connection con, int prvAdmitId, int curBedId) throws SQLException,
      ParseException {
    BasicDynaBean fromBed = new GenericDAO("ip_bed_details").findByKey(con, "admit_id", prvAdmitId);
    BasicDynaBean prvBedDetails = new BedMasterDAO().getBedDetailsBean(con,
        (Integer) fromBed.get("bed_id"));
    BasicDynaBean curBedDetails = new BedMasterDAO().getBedDetailsBean(con, curBedId);
    if (prvBedDetails.get("is_icu").equals(curBedDetails.get("is_icu"))) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Can release.
   *
   * @param con the con
   * @param bedId the bed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean canRelease(Connection con, int bedId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("SELECT * "
              + " FROM ip_bed_details "
              + " where bed_id = ? AND (bed_state = 'O' OR status in ('A','C'))");
      ps.setInt(1, bedId);
      return DataBaseUtil.queryToArrayList(ps).size() == 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Retuns list of reference beds.
   *
   * @param con the con
   * @param admitId the admit id
   * @return the ref beds
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getRefBeds(Connection con, int admitId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("SELECT * "
              + " FROM ip_bed_details "
              + " where ref_admit_id = ? AND status != 'X' ORDER BY admit_id");
      ps.setInt(1, admitId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

}
