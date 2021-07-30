package com.insta.hms.adminmasters.bedmaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmaster.packagemaster.PackageChargeDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.TestChargesDAO;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.ConsultationCharges.ConsultationChargesDAO;
import com.insta.hms.master.DoctorMaster.DoctorChargeDAO;
import com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.OperationMaster.OperationChargeDAO;
import com.insta.hms.master.RegistrationCharges.RegistrationChargesDAO;
import com.insta.hms.master.ServiceMaster.ServiceChargeDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class BedMasterBO.
 */
public class BedMasterBO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(BedMasterBO.class);

  /**
   * Adds the or update bed charge.
   *
   * @param al the al
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addOrUpdateBedCharge(ArrayList<BedDetails> al) throws Exception {
    boolean status = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);

    BedMasterDAO dao = new BedMasterDAO();

    String bedType = null;
    String bedStatus = null;
    String baseBed = null;
    BedDetails bd = null;
    Iterator<BedDetails> it = al.iterator();
    while (it.hasNext()) {
      bd = it.next();
      bedType = bd.getBedType();
      bedStatus = bd.getBedStatus();
      break;
    }

    do {
      status = dao.addOrUpdateBedCharge(con, al);

      if (!status) {
        break;
      }

      status = dao.updateBedStatus(con, bedType, bedStatus);

      if (!status) {
        break;
      }

      status &= dao.updateBedType(con, bd);

    } while (false);

    DataBaseUtil.commitClose(con, status);

    return status;
  }

  /**
   * Adds the new normal bed.
   *
   * @param al                 the al
   * @param baseBedForCharges  the base bed for charges
   * @param varianceType       the variance type
   * @param varianceBy         the variance by
   * @param varianceValue      the variance value
   * @param useValue           the use value
   * @param nearstRoundOfValue the nearst round of value
   * @param userName           the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addNewNormalBed(ArrayList<BedDetails> al, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName) throws Exception {
    boolean status = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BedMasterDAO dao = new BedMasterDAO();
      BedDetails bd = null;
      do {

        String newBedType = null;
        Iterator<BedDetails> it = al.iterator();
        while (it.hasNext()) {
          bd = it.next();
          bd.setIsIcu("N");
          newBedType = bd.getBedType();
          break;
        }

        boolean duplicate = BedMasterDAO.chekDuplicateBedType(newBedType);
        if (duplicate) {
          status = false;
          break;
        }
        // disbale audit log trigger on masters
        status = dao.addNewNormalBed(con, al);
        if (!status) {
          break;
        }

        status &= dao.insertBedType(con, bd);
      } while (false);
      return status;
    } finally {
      DataBaseUtil.commitClose(con, status);
    }
  }

  /**
   * Adds the new normal bed.
   *
   * @param con                connection
   * @param al                 the al
   * @param baseBedForCharges  the base bed for charges
   * @param varianceType       the variance type
   * @param varianceBy         the variance by
   * @param varianceValue      the variance value
   * @param useValue           the use value
   * @param nearstRoundOfValue the nearst round of value
   * @param userName           the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addNewBedCharges(Connection con, ArrayList<BedDetails> al,
      String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName, String isIcuCategory) throws Exception {
    boolean status = true;
    try {
      BedMasterDAO dao = new BedMasterDAO();
      BedDetails bd = null;
      do {

        String newBedType = null;
        Iterator<BedDetails> it = al.iterator();
        while (it.hasNext()) {
          bd = it.next();
          bd.setIsIcu("N");
          newBedType = bd.getBedType();
          break;
        }

        status = dao.addBedForRegistrationCharges(con, newBedType, baseBedForCharges, varianceType,
            varianceBy, varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedforDoctors(con, newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }
        if (isIcuCategory.equals("N")) {
          status = dao.addNormalBedForIcuCharges(con, newBedType, baseBedForCharges, varianceType,
              varianceBy, varianceValue, useValue);
          if (!status) {
            break;
          }
        }

        status = dao.addBedforOperations(newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

        status = dao.addBedForTheatres(con, newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedForEquipments(con, newBedType, baseBedForCharges, varianceType,
            varianceBy, varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedForAnestesiaTypes(con, newBedType, baseBedForCharges, varianceType,
            varianceBy, varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedForServices(newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

        status = dao.addBedForDynaPackages(con, newBedType, baseBedForCharges, varianceType,
            varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

        status = dao.addBedForDynaPackageCategoryLimits(con, newBedType, baseBedForCharges,
            varianceType, varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

        status = dao.addBedForTest(newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

        status = dao.addBedForDietry(con, newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedForPackages(con, newBedType, baseBedForCharges, varianceType, varianceBy,
            varianceValue, useValue, nearstRoundOfValue);
        if (!status) {
          break;
        }

        status = dao.addBedForConsultationType(con, newBedType, baseBedForCharges, varianceType,
            varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
        if (!status) {
          break;
        }

      } while (false);

      GenericDAO.alterTrigger("ENABLE", "operation_charges", "z_operation_charges_audit_trigger");
      GenericDAO.alterTrigger("ENABLE", "service_master_charges",
          "z_services_charges_audit_trigger");
      GenericDAO.alterTrigger("ENABLE", "diagnostic_charges",
          "z_diagnostictest_charges_audit_trigger");

    } catch (Exception exp) {
      logger.error("error while saving the bed charges", exp);
    }

    return status;
  }

  /**
   * Update bed for billing masters.
   *
   * @parm con connection
   * @param ratePlanId the rate plan id
   * @param rateSheetId the rate sheet id
   * @param variance the variance
   * @param roundOff the round off
   * @param bedType the bed type
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updateBedForBillingMasters(Connection con, String ratePlanId, String rateSheetId,
      Double variance,
      Double roundOff, String bedType) throws SQLException, Exception {
    boolean success = false;
    try {
      success = new TestChargesDAO().updateBedForRatePlan(con, ratePlanId, variance, rateSheetId,
          roundOff, bedType);
      if (success) {
        success = new ServiceChargeDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new OperationChargeDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new AnaesthesiaTypeChargesDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new ConsultationChargesDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new PackageChargeDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new DoctorChargeDAO().updateBedForRatePlan(con, ratePlanId, variance, rateSheetId,
            roundOff, bedType);
      }
      if (success) {
        success = new TheatreMasterDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new EquipmentChargeDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new DynaPackageChargesDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new DynaPackageCategoryLimitsDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new RegistrationChargesDAO().updateBedForRatePlan(con, ratePlanId, variance,
            rateSheetId, roundOff, bedType);
      }
      if (success) {
        success = new BedMasterDAO().updateBedForRatePlan(con, ratePlanId, variance, rateSheetId,
            roundOff, bedType);
      }

    } catch (SQLException sqlexp) {
      logger.error("error while updating bed billing master", sqlexp);
    } catch (Exception exp) {
      logger.error("error while updating bed billing master", exp);
    }
    return success;
  }

  /**
   * Adds the new ICU bed.
   *
   * @param con connection
   * @param al the al
   * @param baseBedForCharges the base bed for charges
   * @param varianceType the variance type
   * @param varianceBy the variance by
   * @param varianceValue the variance value
   * @param useValue the use value
   * @param nearstRoundOfValue the nearst round of value
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addNewIcuBedCharges(Connection con, ArrayList<BedDetails> al,
      String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName) throws Exception {

    boolean status = true;
    BedMasterDAO dao = new BedMasterDAO();
    BedDetails bd = null;
    do {
      String newBedType = null;
      Iterator<BedDetails> it = al.iterator();
      while (it.hasNext()) {
        bd = it.next();
        bd.setIsIcu("Y");
        newBedType = bd.getBedType();
        break;
      }

      status = dao.addBedForRegistrationCharges(con, newBedType, baseBedForCharges, varianceType,
          varianceBy, varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedforDoctors(con, newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedforOperations(newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue, userName);

      if (!status) {
        break;
      }

      status = dao.addBedForTheatres(con, newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedForEquipments(con, newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedForAnestesiaTypes(con, newBedType, baseBedForCharges, varianceType,
          varianceBy, varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedForServices(newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue, userName);
      if (!status) {
        break;
      }

      status = dao.addBedForDynaPackages(con, newBedType, baseBedForCharges, varianceType,
          varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
      if (!status) {
        break;
      }

      status = dao.addBedForDynaPackageCategoryLimits(con, newBedType, baseBedForCharges,
          varianceType, varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
      if (!status) {
        break;
      }

      status = dao.addBedForTest(newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue, userName);
      if (!status) {
        break;
      }

      status = dao.addBedForDietry(con, newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedForPackages(con, newBedType, baseBedForCharges, varianceType, varianceBy,
          varianceValue, useValue, nearstRoundOfValue);
      if (!status) {
        break;
      }

      status = dao.addBedForConsultationType(con, newBedType, baseBedForCharges, varianceType,
          varianceBy, varianceValue, useValue, nearstRoundOfValue, userName);
      if (!status) {
        break;
      }

    } while (false);

    GenericDAO.alterTrigger("ENABLE", "operation_charges", "z_operation_charges_audit_trigger");
    GenericDAO.alterTrigger("ENABLE", "service_master_charges", "z_services_charges_audit_trigger");
    GenericDAO.alterTrigger("ENABLE", "diagnostic_charges",
        "z_diagnostictest_charges_audit_trigger");

    return status;
  }

  /**
   * Adds the new ICU bed.
   *
   * @param al the al
   * @param baseBedForCharges the base bed for charges
   * @param varianceType the variance type
   * @param varianceBy the variance by
   * @param varianceValue the variance value
   * @param useValue the use value
   * @param nearstRoundOfValue the nearst round of value
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addNewIcuBed(ArrayList<BedDetails> al, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName) throws Exception {

    boolean status = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    BedMasterDAO dao = new BedMasterDAO();
    BedDetails bd = null;
    do {
      String newBedType = null;
      Iterator<BedDetails> it = al.iterator();
      while (it.hasNext()) {
        bd = it.next();
        bd.setIsIcu("Y");
        newBedType = bd.getBedType();
        break;
      }

      boolean duplicate = dao.chekDuplicateBedType(newBedType);
      if (duplicate) {
        status = false;
        break;
      }

      status = dao.adddNewIcuBed(con, al);
      if (!status) {
        break;
      }

      status &= dao.insertBedType(con, bd);

    } while (false);

    if (status) {
      con.commit();
    }

    DataBaseUtil.commitClose(con, status);

    return status;
  }

  /**
   * Group update charges.
   *
   * @param orgId the org id
   * @param bedTypes the bed types
   * @param groupUpdate the group update
   * @param amount the amount
   * @param isPercentage the is percentage
   * @param roundOff the round off
   * @param updateTable the update table
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean groupUpdateCharges(String orgId, List<String> bedTypes, String groupUpdate,
      BigDecimal amount, boolean isPercentage, BigDecimal roundOff, String updateTable)
      throws SQLException {

    boolean status = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    BedMasterDAO dao = new BedMasterDAO();

    List<String> normalBeds = null;
    List<String> icuBeds = null;
    try {
      if (bedTypes != null) {
        normalBeds = new ArrayList<String>();
        icuBeds = new ArrayList<String>();
        for (String bed : bedTypes) {
          if (!BedMasterDAO.isIcuBedType(bed)) {
            normalBeds.add(bed);
          } else {
            icuBeds.add(bed);
          }
        }
      }
      if (normalBeds != null && normalBeds.size() > 0) {
        dao.groupUpdateChargesNormalBeds(con, orgId, normalBeds, groupUpdate, amount, isPercentage,
            roundOff, updateTable);
      }
      if (icuBeds != null && icuBeds.size() > 0) {
        dao.groupUpdateChargesIcuBeds(con, orgId, icuBeds, groupUpdate, amount, isPercentage,
            roundOff, updateTable);
      }

    } finally {
      DataBaseUtil.commitClose(con, status);
    }
    return status;
  }
}
