package com.bob.hms.adminmasters.wardandbed;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class WardAndBedMasterBo.
 */
public class WardAndBedMasterBo {

  /** The log. */
  public static final Logger log = LoggerFactory.getLogger(WardAndBedMasterBo.class);

  /**
   * Edits the ward details.
   *
   * @param wn the wn
   * @param bnList the bn list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean editWardDetails(WardNames wn, List<BedNames> bnList)
      throws SQLException {
    boolean status = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);

    do {

      status = WardAndBedMasterDao.updateWardDetails(con, wn);
      if (!status) {
        break;
      }

      Iterator<BedNames> it = bnList.iterator();
      while (it.hasNext()) {
        BedNames bn = it.next();
        bn.setBedId(WardAndBedMasterDao.getNextBedId());
        status = WardAndBedMasterDao.insertBedNames(con, bn);
        if (!status) {
          break;
        }

      }

    } while (false);

    DataBaseUtil.commitClose(con, status);

    return status;
  }

  /**
   * Insert new ward details.
   *
   * @param wn the wn
   * @param bnList the bn list
   * @param bedType the bed type
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String insertNewWardDetails(WardNames wn, List<BedNames> bnList,
      String bedType) throws SQLException {
    boolean status = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String newWardId = null;

    do {

      newWardId = WardAndBedMasterDao.getNextWardId();
      wn.setWardNo(newWardId);

      status = WardAndBedMasterDao.insertWard(con, wn, bedType);
      if (!status) {
        newWardId = null;
        break;
      }

      Iterator<BedNames> it = bnList.iterator();
      while (it.hasNext()) {
        BedNames bn = it.next();
        bn.setBedId(WardAndBedMasterDao.getNextBedId());
        bn.setWardNo(newWardId);
        status = WardAndBedMasterDao.insertBedNames(con, bn);
        if (!status) {
          newWardId = null;
          break;
        }

      }

    } while (false);

    DataBaseUtil.commitClose(con, status);

    return newWardId;
  }

  /**
   * Update bed names details.
   *
   * @param al the al
   * @param error the error
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateBedNamesDetails(List<BedNames> al, StringBuilder error)
      throws SQLException {
    boolean status = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    BedMasterDAO bedNamesDao = new BedMasterDAO();

    do {
      Iterator<BedNames> it = al.iterator();
      while (it.hasNext()) {
        BedNames bn = it.next();

        boolean duplicateBed = bedNamesDao.isDuplicateBed(con, bn.getBedName().trim(),
            bn.getWardNo(), bn.getBedId());

        error.append(
            duplicateBed ? "Duplicate Bed Name " + bn.getBedName() + " is not allowed" : "");
        if (duplicateBed) {
          status = false;
          break;
        }
        status = WardAndBedMasterDao.updateBedNamesDetails(con, bn);
        if (!status) {
          break;
        }
      }

      List<BasicDynaBean> childBeans = null;
      BasicDynaBean bedBean = null;
      it = al.iterator();
      while (it.hasNext()) {
        BedNames bn = it.next();
        bedBean = BedMasterDAO.getBedDetailsBean(bn.getBedId());
        childBeans = BedMasterDAO.getChildBeds(con, bn.getBedId());
        if (!childBeans.isEmpty() && bn.getStatus().equals("I")
            && !bedBean.get("status").equals(bn.getStatus())) {
          status &= WardAndBedMasterDao.updateChilds(con, bn.getBedId());
        }
        if (!status) {
          break;
        }
      }

    } while (false);

    DataBaseUtil.commitClose(con, status);

    return status;
  }

}
