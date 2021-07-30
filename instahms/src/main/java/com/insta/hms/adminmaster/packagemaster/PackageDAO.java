package com.insta.hms.adminmaster.packagemaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.jobs.CronJobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageDAO.
 */
public class PackageDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PackageDAO.class);

  /** The con. */
  private Connection con = null;

  /** The pdao. */
  private static GenericDAO pdao = new GenericDAO("patient_packages");

  /**
   * Instantiates a new package DAO.
   *
   * @param con the con
   */
  public PackageDAO(Connection con) {
    this.con = con;
  }

  /**
   * Gets the next charge id.
   *
   * @return the next charge id
   * @throws SQLException the SQL exception
   */
  public String getNextChargeId() throws SQLException {
    return AutoIncrementId.getSequenceId("pack_chid_sequence", "CHARGEID");
  }

  /** The create package. */
  private static String CREATE_PACKAGE = "INSERT INTO packages "
      + "(package_id, package_name, visit_applicability, status,"
      + "description,type,allow_discount,operation_id) "
      + " VALUES (?,?,?,?,?,?,?,?)";

  /**
   * Insert package.
   *
   * @param pack the pack
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertPackage(PackageDetails pack) throws SQLException {

    PreparedStatement ps = null;
    int intValue = 1;
    int count = 0;
    String packType = "*";
    try {
      ps = con.prepareStatement(CREATE_PACKAGE);
      ps.setInt(intValue++, pack.getPackageId());
      ps.setString(intValue++, pack.getPackageName());
      if (!"d".equals(pack.getPackageType())) {
        packType = pack.getPackageType();
      }
      ps.setString(intValue++, packType);
      ps.setString(intValue++, pack.getPackageActive());
      ps.setString(intValue++, pack.getDescrip());
      String type = "P";
      if ("T".equals(pack.getTemplate())) {
        type = "O";
      }
      ps.setString(intValue++, type);
      ps.setBoolean(intValue++, pack.isAllowDiscount());
      ps.setString(intValue++, pack.getOperations());
      count = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return (count == 1);
  }

  /** The create package comp. */
  private static String CREATE_PACKAGE_COMP = "INSERT INTO package_contents "
      + "(package_id,pack_ob_id,activity_id,charge_group,charge_head,"
      + "activity_description,activity_remarks,activity_units,activity_qty) "
      + " VALUES (?,?,?,?,?,?,?,?,?)";

  /**
   * Sets the insert pack params.
   *
   * @param pack the pack
   * @param ps   the ps
   * @throws SQLException the SQL exception
   */
  private void setInsertPackParams(PackageDetails pack, PreparedStatement ps) throws SQLException {
    ps.setInt(1, pack.getPackageId());
    ps.setInt(2, Integer.parseInt(DataBaseUtil.getValue("pack_chid_sequence", "N", "")));
    ps.setString(3, pack.getActivityId());
    ps.setString(4, pack.getChargeGroupId());
    ps.setString(5, pack.getChargeHeadId());
    ps.setString(6, pack.getDescription());
    ps.setString(7, pack.getRemarks());
    ps.setString(8, pack.getUnits());
    ps.setInt(9, pack.getChargeQty());
  }

  /**
   * Insert pack element.
   *
   * @param pack the pack
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertPackElement(PackageDetails pack) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(CREATE_PACKAGE_COMP);
      setInsertPackParams(pack, ps);
      count = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return (count == 1);
  }

  /**
   * Insert pack elements.
   *
   * @param list the list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertPackElements(List list) throws SQLException {
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(CREATE_PACKAGE_COMP);
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        PackageDetails pack = (PackageDetails) iterator.next();
        setInsertPackParams(pack, ps);
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    } catch (Exception ex) {
      success = false;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /** The update package comp. */
  private static String UPDATE_PACKAGE_COMP = "UPDATE package_contents set "
      + " activity_remarks=?  , activity_qty = ? WHERE package_content_id=? ";

  /**
   * Update pack params.
   *
   * @param pack the pack
   * @param ps   the ps
   * @throws SQLException the SQL exception
   */
  private void updatePackParams(PackageDetails pack, PreparedStatement ps) throws SQLException {
    ps.setString(1, pack.getRemarks());
    ps.setInt(2, pack.getChargeQty());
    ps.setString(3, pack.getPackObId());
  }

  /**
   * Update pack element.
   *
   * @param pack the pack
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updatePackElement(PackageDetails pack) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(UPDATE_PACKAGE_COMP);
      updatePackParams(pack, ps);
      count = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return (count == 1);
  }

  /**
   * Update pack elements.
   *
   * @param list the list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updatePackElements(List list) throws SQLException {
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(UPDATE_PACKAGE_COMP);
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        PackageDetails pack = (PackageDetails) iterator.next();
        updatePackParams(pack, ps);
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    } catch (Exception ex) {
      success = false;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /** The get pack details. */
  private static String GET_PACK_DETAILS =
      " SELECT pc.charge_head,pc.activity_description,"
      + "pc.activity_remarks,pc.activity_qty_uom as activity_units,chargehead_name,"
      + " pc.activity_id,pc.pack_ob_id,pc.package_id,pc.activity_qty,"
      + "pc.activity_type,pc.activity_charge "
      + " FROM package_contents pc" + " JOIN packages pm USING(package_id)"
      + " JOIN chargehead_constants ON(chargehead_id = charge_head) WHERE package_id=?";

  /**
   * Gets the pack details.
   *
   * @param packId  the pack id
   * @param bedType the bed type
   * @param orgId   the org id
   * @return the pack details
   * @throws SQLException the SQL exception
   */
  public ArrayList getPackDetails(int packId, String bedType, String orgId) throws SQLException {
    PreparedStatement ps = null;
    ArrayList packList = new ArrayList();
    ResultSet rs = null;
    PackageDetails pack = null;
    try {
      String queryString = GET_PACK_DETAILS
          + " and bed_type=? and org_id=? and chargehead_id = charge_head";
      ps = con.prepareStatement(queryString);
      ps.setInt(1, packId);
      ps.setString(2, bedType);
      ps.setString(3, orgId);
      rs = ps.executeQuery();

      while (rs.next()) {
        pack = new PackageDetails();
        pack.setChargeHeadId(rs.getString("charge_head"));
        pack.setDescription(rs.getString("activity_description"));
        pack.setRemarks(rs.getString("activity_remarks"));
        pack.setUnits(rs.getString("activity_units"));
        pack.setRate(Float.parseFloat(rs.getString("activity_rate")));
        pack.setChargeHeadName(rs.getString("chargehead_name"));
        pack.setActivityId(rs.getString("activity_id"));
        packList.add(pack);
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
    return packList;
  }

  /** The Constant PACKAGE_DETAILS_WITH_RATEPLAN_APPLICABLE. */
  private static final String PACKAGE_DETAILS_WITH_RATEPLAN_APPLICABLE = 
      " SELECT pc.charge_head,pc.activity_description,pc.activity_remarks,"
      + " (case when pc.activity_qty_uom = 'D' then 'Days' "
      + " when pc.activity_qty_uom = 'H' then 'Hrs'  else '' end) "
      + " as display_units,pc.activity_qty_uom as activity_units ,chargehead_name,"
      + " pc.activity_id,pc.package_content_id as pack_ob_id,pc.package_id,pc.activity_qty,"
      + " pc.activity_type,pcc.charge as activity_charge,"
      + " (CASE WHEN foo.applicable IS null then 't' "
      + " ELSE foo.applicable END ) AS applicable,chargegroup_id, "
      + " ct.consultation_type as consultation_type_name, "
      + " pc.consultation_type_id, pc.display_order "
      + " FROM package_contents pc"
      + " JOIN package_content_charges pcc ON pcc.package_content_id = pc.package_content_id "
      + " AND pcc.org_id = ? AND pcc.bed_type='GENERAL'"
      + " JOIN packages pm USING(package_id)"
      + " LEFT JOIN consultation_types ct ON (ct.consultation_type_id=pc.consultation_type_id) "
      + " JOIN chargehead_constants  cc ON(cc.chargehead_id = pc.charge_head) "
      + " JOIN chargegroup_constants cg using (chargegroup_id) "
      + " LEFT JOIN (select applicable,id,org_id from ("
      + " SELECT td.applicable,test_id as id,org_id from  test_org_details td " + " UNION  "
      + " SELECT sd.applicable,service_id  as id,org_id from  service_org_details sd ) as allfoo"
      + " )AS foo on(foo.id = pc.activity_id and foo.org_id = ?)" + " WHERE package_id=?";

  /**
   * Gets the pack details.
   *
   * @param packId the pack id
   * @param orgId  the org id
   * @return the pack details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPackDetails(int packId, String orgId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(PACKAGE_DETAILS_WITH_RATEPLAN_APPLICABLE + " "
        + "and cc.chargehead_id = pc.charge_head ORDER BY pc.display_order");
    ps.setString(1, orgId);
    ps.setString(2, orgId);
    ps.setInt(3, packId);
    return DataBaseUtil.queryToDynaList(ps);
  }

  /** The Constant ALL_PACKAGE_MASTER_DETAILS. */
  private static final String ALL_PACKAGE_MASTER_DETAILS =
      " SELECT p.package_id,package_name,p.status as package_active,"
      + " (CASE when visit_applicability='*' and type='D' then 'd' else visit_applicability"
      + " end) as package_type,description, valid_from as valid_from_date, "
      + "  valid_till as valid_to_date, (CASE when type='P' then 'Package' else 'Template' end)"
      + "  as type, (CASE when allow_discount= 't' then 'true' else 'false' end) "
      + " AS allow_discount,operation_id,(case when operation_id is null then 'false' "
      + " else operation_id end) as hasope,"
      + " service_sub_group_id ,service_group_id, package_code, "
      + " insurance_category_id, package_category_id, "
      + " prior_auth_required,allow_rate_increase,allow_rate_decrease, "
      + " billing_group_id, multi_visit_package, "
      + " approval_status, approval_remarks, approval_processed_by as approval_process_by,"
      + " handover_to, chanelling "
      + " FROM packages p INNER JOIN (select distinct pc.package_id, pc.operation_id "
      + " from package_contents pc) as dpc on dpc.package_id = p.package_id "
      + " JOIN service_sub_groups using(service_sub_group_id) ";

  /** The Constant PACKAGE_MASTER_DETAILS. */
  private static final String PACKAGE_MASTER_DETAILS = ALL_PACKAGE_MASTER_DETAILS
      + " where p.package_id = ?";

  /**
   * Gets the package master details.
   *
   * @param packageId the package id
   * @return the package master details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageMasterDetails(int packageId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_MASTER_DETAILS);
      ps.setInt(1, packageId);
      return (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the package details.
   *
   * @param packageId the package id
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPackageDetails(int packageId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      ps = con.prepareStatement(PACKAGE_MASTER_DETAILS);
      ps.setInt(1, packageId);
      return (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the package details.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPackageDetails(int packageId, String orgId, String bedType)
       throws SQLException {
    return DataBaseUtil.queryToDynaBean(PACKAGE_DETAILS,
        new Object[] { packageId, orgId, bedType });
  }

  /**
   * Brings all packages details.
   *
   * @return the all package details
   * @throws SQLException the SQL exception
   */
  public static List getAllPackageDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(ALL_PACKAGE_MASTER_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The delete package comp. */
  private static String DELETE_PACKAGE_COMP =
      "delete from package_contents where pack_ob_id=?";

  /**
   * Delete pack element.
   *
   * @param pack the pack
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deletePackElement(PackageDetails pack) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(DELETE_PACKAGE_COMP);
      ps.setString(1, pack.getChargeHeadId());
      count = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return (count == 1);
  }

  /**
   * Delete pack elements.
   *
   * @param list the list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deletePackElements(List list) throws SQLException {
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(DELETE_PACKAGE_COMP);
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        PackageDetails pack = (PackageDetails) iterator.next();
        if (!pack.getPackObId().startsWith("_")) {
          ps.setString(1, pack.getPackObId());
          ps.addBatch();
        }
      }
      int[] results = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    } catch (Exception ex) {
      success = false;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /** The Constant PACKAGE_FILEDS. */
  private static final String PACKAGE_FILEDS = "SELECT * ";

  /** The Constant PACKAGES_COUNT. */
  private static final String PACKAGES_COUNT = "SELECT count(*) ";

  /** The Constant PACKAGES_TABLES. */
  private static final String PACKAGES_TABLES =
      " FROM(SELECT pm.package_id, pm.package_name,pm.multi_visit_package,"
      + " (CASE when pm.visit_applicability='*' and pm.type='D' then 'd'"
      + "  else pm.visit_applicability end) as package_type, "
      + " pm.status as package_active,'P'::text as type,pob.org_id,"
      + " pob.applicable,pm.service_sub_group_id,"
      + " od.org_name,'packages'::text as chargeCategory, pob.is_override,"
      + " pob.applicable as appl, pm.approval_status "
      + " FROM packages pm  JOIN pack_org_details pob using(package_id)"
      + " JOIN organization_details od on (od.org_id = pob.org_id))as foo";

  /** The Constant WHERE. */
  private static final String WHERE = 
      " WHERE package_id in (select pack_id from package_center_master pcm "
      + " where package_id=pack_id AND (#center#)) "
      + " AND package_id in (select pack_id from package_sponsor_master psm "
      + " where package_id=pack_id AND (#sponsor#)) ";

  /** The Constant PACKAGE_CHARGES. */
  private static final String PACKAGE_CHARGES = "SELECT charge FROM  package_charges WHERE "
      + "package_id=? AND bed_type=? AND org_id=?  ";

  /**
   * Gets the packages.
   *
   * @param filters      the filters
   * @param pagingParams the paging params
   * @param orgId        the org id
   * @return the packages
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getPackages(Map filters, Map pagingParams, String orgId)
      throws SQLException, ParseException {

    Connection con = null;
    int count = 0;

    PreparedStatement ps = null;
    ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
    int pageNumber = (Integer) pagingParams.get(ConversionUtils.LISTING.PAGENUM);
    int pageSize = (Integer) pagingParams.get(ConversionUtils.LISTING.PAGESIZE);

    try {
      String[] tpaId = (String[]) filters.get("_sponsor");
      String[] applicableForTpa = (String[]) filters.get("_applicable_for_sponsor");
      String sponsor = "";
      StringBuffer sponsorBuf = new StringBuffer();
      boolean allSponsorsSelected = false;
      if (applicableForTpa != null) {
        boolean first = true;
        for (int j = 0; j < applicableForTpa.length; j++) {
          if (applicableForTpa[j].equals("")) {
            allSponsorsSelected = true;
            break;
          }
          if (!first) {
            sponsorBuf.append(" or ");
          }
          if (applicableForTpa[j].equals("0")) {
            sponsorBuf.append("psm.tpa_id = '0'");
          } else if (applicableForTpa[j].equals("-1")) {
            sponsorBuf.append("psm.tpa_id = '-1'");
          } else if (applicableForTpa[j].equals("specific")) {
            sponsorBuf.append("psm.tpa_id = '" + tpaId[0] + "'");
          }
          first = false;
        }
        sponsor = sponsorBuf.toString();
      } else {
        allSponsorsSelected = true;
      }
      String[] centerId = (String[]) filters.get("_center_id");
      String[] applicableForCenter = (String[]) filters.get("_applicable_for_center");
      String center = "";
      boolean allCentersSelected = false;
      if (applicableForCenter != null) {
        boolean first = true;
        for (int j = 0; j < applicableForCenter.length; j++) {
          if (applicableForCenter[j].equals("")) {
            allCentersSelected = true;
            break;
          }
          if (!first) {
            center += " or ";
          }
          if (applicableForCenter[j].equals("-1")) {
            center += "pcm.center_id = -1";
          } else if (applicableForCenter[j].equals("specific")) {
            center += "pcm.center_id = " + centerId[0];
          }

          first = false;
        }
      } else {
        int loggedInCenterId = RequestContext.getCenterId();
        if (loggedInCenterId != 0) {
          center += " pcm.center_id = -1 or pcm.center_id = " + loggedInCenterId;
        } else {
          allCentersSelected = true;
        }
      }
      String whereCond = WHERE.replace("#sponsor#", allSponsorsSelected ? "true" : sponsor);
      whereCond = whereCond.replace("#center#", allCentersSelected ? "true" : center);

      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, PACKAGE_FILEDS, PACKAGES_COUNT,
          PACKAGES_TABLES, whereCond, pagingParams);

      if (orgId == null) {
        orgId = "ORG0001";
        qb.addFilter(SearchQueryBuilder.STRING, "org_id", "=", orgId);
      }

      qb.addFilterFromParamMap(filters);
      qb.addSecondarySort("package_id");
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      PreparedStatement psItem = null;
      ArrayList<List> packageMasterList = new ArrayList<List>();
      count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));
      packageMasterList.addAll(DataBaseUtil.queryToArrayList(psData));
      psData.close();
      psCount.close();
      ps = con.prepareStatement(PACKAGE_CHARGES);

      ArrayList<List> packagesList = new ArrayList<List>();
      ArrayList<String> headers = new ArrayList<String>();
      headers.add("Package Name");
      headers.addAll(bedTypes);
      packagesList.add(headers);
      Iterator it = packageMasterList.iterator();
      while (it.hasNext()) {
        Hashtable ht = (Hashtable) it.next();
        String packageName = (String) ht.get("PACKAGE_NAME");
        String packageId = (String) ht.get("PACKAGE_ID");
        String status = (String) ht.get("status");
        String applicable = (String) ht.get("APPL");
        String isMultiVisitPackage = (String) ht.get("MULTI_VISIT_PACKAGE");

        ArrayList<String> packages = new ArrayList<String>();
        packages.add(status);
        packages.add(packageId);
        packages.add(packageName);
        packages.add((String) ht.get("TYPE"));
        packages.add(applicable);
        packages.add(isMultiVisitPackage);

        Iterator<String> beds = bedTypes.iterator();
        while (beds.hasNext()) {
          String bed = beds.next();
          ps.setInt(1, Integer.parseInt((String) ht.get("PACKAGE_ID")));
          ps.setString(2, bed);
          ps.setString(3, orgId);

          packages.add(DataBaseUtil.getStringValueFromDb(ps));
        }
        packagesList.add(packages);

      }
      PagedList list = new PagedList(packagesList, count, pageSize, pageNumber);
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Gets the package names.
   *
   * @return the package names
   * @throws SQLException the SQL exception
   */
  public ArrayList getPackageNames() throws SQLException {
    PreparedStatement ps = null;
    ArrayList packageNames = null;
    String packQuery = "select package_name from packages ";
    ps = con.prepareStatement(packQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
    packageNames = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return packageNames;
  }

  /**
   * Gets the details.
   *
   * @param packid the packid
   * @return the details
   * @throws SQLException the SQL exception
   */
  public PackageDetails getDetails(int packid) throws SQLException {
    PreparedStatement ps = null;
    PackageDetails packDetail = null;
    ResultSet rs = null;
    try {
      String detailsQuery =
          "select pm.package_name,d.dept_name from packages pm,department d "
          + " (CASE when pm.visit_applicability='*' and pm.type='D' then 'd'"
          + "  else pm.visit_applicability end) as package_type"
          + " where pm.dept_id=d.dept_id and package_id=? and status='A'";
      ps = con.prepareStatement(detailsQuery);
      ps.setInt(1, packid);
      rs = ps.executeQuery();
      if (rs.next()) {
        packDetail = new PackageDetails();
        packDetail.setPackageName(rs.getString(1));
        packDetail.setPackageType(rs.getString(2));
        packDetail.setDeptName(rs.getString(3));
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (rs != null) {
        rs.close();
      }
    }

    return packDetail;
  }

  /** The update package. */
  private static String UPDATE_PACKAGE =
      "update packages set status=?,package_name=?,allow_discount=?,"
      + "visit_applicability=?,description=?,operation_id=? where package_id=?";

  /**
   * Update package details.
   *
   * @param packageDetails the package details
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updatePackageDetails(PackageDetails packageDetails) throws SQLException {
    PreparedStatement ps = null;
    String visitApplicability = "*";
    try {
      ps = con.prepareStatement(UPDATE_PACKAGE, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      ps.setString(1, packageDetails.getPackageActive());
      ps.setString(2, packageDetails.getPackageName());
      ps.setBoolean(3, packageDetails.isAllowDiscount());
      if (!"d".equals(packageDetails.getPackageType())) {
        visitApplicability = packageDetails.getPackageType();
      }
      ps.setString(4, visitApplicability);
      ps.setString(5, packageDetails.getDescription());
      ps.setString(6, packageDetails.getOperations());
      ps.setInt(7, packageDetails.getPackageId());

      return ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The get packandbeds. */
  private static String GET_PACKANDBEDS =
      "select pm.package_id,pm.package_name,po.org_id,(case when "
      + "  pm.visit_applicability='*' and pm.type='D' then 'd' else pm.visit_applicability"
      + "  end) as package_type, po.package_id,pm.operation_id from packages pm,"
      + " pack_org_details po where  po.package_id = pm.package_id and po.org_id=?"
      + " and status='A' and po.applicable='t'";

  /**
   * Gets the package names and beds.
   *
   * @param orgid the orgid
   * @return the package names and beds
   * @throws SQLException the SQL exception
   */
  public List getPackageNamesAndBeds(String orgid) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PACKANDBEDS);
      ps.setString(1, orgid);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The update description. */
  private static String UPDATE_DESCRIPTION =
      "update packages set description=? where package_id=?";

  /**
   * Update pack description.
   *
   * @param description the description
   * @param packid      the packid
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updatePackDescription(String description, String packid) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(UPDATE_DESCRIPTION, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      ps.setString(1, description);
      ps.setInt(2, Integer.parseInt(packid));
      count = ps.executeUpdate();
      return (count == 1);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant GET_PACKAGE_DEPT_CHARGES. */
  /*
   * Retrieves a list of packages, their departments and corresponding charges.
   * Charges can vary depending on bed type and organization, so we need these as
   * input to this method.
   *
   * A GENERAL bed package is available for prescription for any bed, whereas, a
   * ORG0001 orgid package is available only for ORG0001 patients. In other words,
   * the fallback to GENERAL is asymmetrical: it applies only for bed-type and not
   * for rate plan.
   *
   * Note: We use bed_type=? OR bed_type=GENERAL to include GENERAL as well as
   * specific packages, and then we use DISTINCT to get only one row per
   * package_id. To ensure that we pick the non-GENERAL package in preference, we
   * force an order by bed_type = 'GENERAL':
   */
  private static final String GET_PACKAGE_DEPT_CHARGES =
      " SELECT  distinct PC.PACKAGE_ID, 'P' AS TYPE, PM.ALLOW_DISCOUNT,"
      + " PM.PACKAGE_NAME, (CASE WHEN PM.VISIT_APPLICABILITY='*' AND PM.TYPE='D'"
      + " THEN 'd' ELSE PM.VISIT_APPLICABILITY) AS PACKAGE_TYPE, DESCRIPTION,"
      + " PC.CHARGE, PC.DISCOUNT FROM PACKAGE_CHARGES PC  "
      + "JOIN PACKAGES PM ON (PM.PACKAGE_ID = PC.PACKAGE_ID)  "
      + "WHERE PM.status = 'A' AND PC.ORG_ID=? AND (PC.BED_TYPE=?)" + " UNION ALL"
      + " SELECT  distinct PO.PACKAGE_ID, 'P' AS TYPE, PM.ALLOW_DISCOUNT,"
      + "PM.PACKAGE_NAME, (CASE WHEN PM.VISIT_APPLICABILITY='*' AND PM.TYPE='D' THEN 'd' "
      + "ELSE PM.VISIT_APPLICABILITY) AS PACKAGE_TYPE, DESCRIPTION,0,0 "
      + " FROM pack_org_details PO JOIN PACKAGES PM ON (PM.PACKAGE_ID = PO.PACKAGE_ID)  "
      + " WHERE PM.status = 'A' AND PO.ORG_ID=? and PM.type='O'";

  /**
   * Gets the package dept charges.
   *
   * @param bedType the bed type
   * @param orgid   the orgid
   * @return the package dept charges
   * @throws SQLException the SQL exception
   */
  public static List getPackageDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PACKAGE_DEPT_CHARGES);
      ps.setString(1, orgid);
      ps.setString(2, bedType);
      ps.setString(3, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }

    return list;
  }

  /** The Constant PACK_TEMP_STATUS. */
  public static final String PACK_TEMP_STATUS = "select type from packages  where package_id=? ";
  
  /** The Constant PAT_PACK_SUMMARY. */
  public static final String PAT_PACK_SUMMARY =
      " SELECT pm.package_id, pm.package_name, (case when "
      + "  pm.visit_applicability='*' and pm.type='D' then 'd' else pm.visit_applicability end)"
      + "  as package_type, pc.org_id, pc.bed_type, pm.description, pc.charge "
      + "  FROM package_charges pc JOIN packages pm ON (pc.package_id = pm.package_id) "
      + "  WHERE pm.package_id = ?  AND pc.org_id=? and pc.bed_type=?  ";

  /** The Constant PAT_PACK_DETAILS. */
  private static final String PAT_PACK_DETAILS = "SELECT "
      + " pcd.pack_ob_id, pcd.activity_id,pcd.charge_group,pcd.charge_head,"
      + " CASE WHEN pc.item_code IS NULL THEN pcd.activity_description "
      + " ELSE pc.item_code||'/'||pcd.activity_description END AS activity_description, "
      + " pcd.activity_remarks, pcd.activity_units, pc.package_id, pc.org_id  "
      + " from package_contents pcd " + " join pack_org_details  pc using (package_id) "
      + " LEFT OUTER JOIN "
      + " (SELECT item_code,test_id AS activity_id FROM test_org_details WHERE org_id=? "
      + " UNION SELECT item_code,service_id AS activity_id FROM service_org_details where org_id=? "
      + " UNION SELECT item_code,operation_id AS activity_id FROM operation_org_details "
      + " WHERE org_id= ? ) AS item " + " ON item.activity_id = pcd.activity_id"
      + " where pc.package_id=? and pc.org_id=?" + " UNION "
      + " SELECT '' as pack_ob_id, operation_id as activity_id, 'OPE' as charge_group,"
      + " 'SACOPE' as charge_head, "
      + " CASE WHEN NULLIF(ood.item_code, '') IS NULL THEN operation_name "
      + " ELSE ood.item_code||'/'||operation_name END AS activity_description, "
      + " '' AS activity_remarks, '' AS activity_units, pc.package_id, pc.org_id "
      + " FROM packages pm JOIN operation_org_details  ood USING (operation_id) "
      + " JOIN operation_master op on (pm.operation_id = op.op_id) "
      + " JOIN pack_org_details  pc USING (package_id) WHERE pc.package_id = ? "
      + " AND pc.org_id= ? AND ood.org_id= ?";

  /**
   * Gets the package dept charges.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package dept charges
   * @throws SQLException the SQL exception
   */
  public static List getPackageDeptCharges(int packageId, String orgId, String bedType)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    List list = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PACK_TEMP_STATUS);
      ps.setInt(1, packageId);
      rs = ps.executeQuery();

      if (rs != null) {
        if (!rs.next()) {
          bedType = "GENERAL";
        }
      } else {
        bedType = "GENERAL";
      }
      ;

      ps = con.prepareStatement(PACK_TEMP_STATUS);
      ps.setInt(1, packageId);
      rs = ps.executeQuery();
      if (rs.next()) {
        if (rs.getString("type").equalsIgnoreCase("P")) {
          ps = con.prepareStatement(PAT_PACK_SUMMARY);
          ps.setInt(1, packageId);
          ps.setString(2, orgId);
          ps.setString(3, bedType);
          list = DataBaseUtil.queryToArrayList(ps);
        } else if (rs.getString("type").equalsIgnoreCase("T")) {
          ps = con.prepareStatement(PAT_PACK_DETAILS);
          ps.setString(1, orgId);
          ps.setString(2, orgId);
          ps.setString(3, orgId);
          ps.setInt(4, packageId);
          ps.setString(5, orgId);
          ps.setString(5, orgId);
          ps.setInt(6, packageId);
          ps.setString(7, orgId);
          ps.setString(8, orgId);
          list = DataBaseUtil.queryToArrayList(ps);
        }
      }

    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
    return list;
  }

  /** The Constant INSERT_PACKAGE_CHARGE. */
  private static final String INSERT_PACKAGE_CHARGE = "INSERT INTO package_charges(package_id,"
      + "org_id,charge,bed_type,discount)"
      + "(SELECT ?, od.org_id,?, 'GENERAL',0.0 FROM organization_details od)";

  /** The Constant CLONE_INSERT_PACKAGE_CHARGE. */
  private static final String CLONE_INSERT_PACKAGE_CHARGE = "INSERT INTO package_charges("
      + "package_id,org_id,charge,bed_type,discount)"
      + "(SELECT ?, abov.org_id,abov.charge, abov.bed_type,abov.discount "
      + " FROM package_charges abov WHERE abov.package_id = ?)";

  /** The Constant CLONE_CHARGE_INSERT_PACKAGE_CHARGE. */
  private static final String CLONE_CHARGE_INSERT_PACKAGE_CHARGE = "INSERT INTO package_charges("
      + "package_id, org_id,charge,bed_type,discount)"
      + "(SELECT ?, abov.org_id , ? , abov.bed_type,abov.discount "
      + " FROM package_charges abov WHERE abov.package_id = ?)";

  /** The Constant PACKAGE_EXISTS. */
  private static final String PACKAGE_EXISTS = "SELECT COUNT(*) FROM  package_charges WHERE "
      + "package_id=? AND bed_type=? AND org_id=?  ";

  /** The Constant UPDATE_PACKAGE_CHARGE. */
  private static final String UPDATE_PACKAGE_CHARGE = 
      "UPDATE package_charges SET charge=?, discount=? WHERE "
      + "package_id=? AND bed_type=? AND org_id=?  ";

  /**
   * Insert package charges.
   *
   * @param con                        the con
   * @param applyChargeToAllRateSheets apply charges to all rate sheets
   * @param isClonePackage             clone package?
   * @param oldPackageId               old package id
   * @param packageId                  package id
   * @param packFormCharge             pack form charge
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertPackageCharges(Connection con, String applyChargeToAllRateSheets,
      Boolean isClonePackage, Integer oldPackageId, Integer packageId, BigDecimal packFormCharge)
      throws SQLException {
    boolean status = false;
    PreparedStatement insertPs = null;
    if (isClonePackage) {
      if (applyChargeToAllRateSheets == null) {
        insertPs = con.prepareStatement(CLONE_INSERT_PACKAGE_CHARGE);
        insertPs.setInt(1, packageId);
        insertPs.setInt(2, oldPackageId);
      } else {
        insertPs = con.prepareStatement(CLONE_CHARGE_INSERT_PACKAGE_CHARGE);
        insertPs.setInt(1, packageId);
        insertPs.setBigDecimal(2, packFormCharge);
        insertPs.setInt(3, oldPackageId);
      }
    } else {
      insertPs = con.prepareStatement(INSERT_PACKAGE_CHARGE);
      insertPs.setInt(1, packageId);
      insertPs.setBigDecimal(2, packFormCharge);
    }
    status = insertPs.executeUpdate() >= 0;
    insertPs.close();

    return status;
  }

  /** The Constant INSERT_PACKAGE_ORG_DETAILS. */
  private static final String INSERT_PACKAGE_ORG_DETAILS = 
      "INSERT INTO pack_org_details(package_id,org_id,applicable)"
      + "(SELECT ?, ob.org_id, true FROM organization_details ob)  ";

  /** The Constant CLONE_INSERT_PACKAGE_ORG_DETAILS. */
  private static final String CLONE_INSERT_PACKAGE_ORG_DETAILS = 
      "INSERT INTO pack_org_details(package_id,"
      + "org_id,applicable)(SELECT ?, abv.org_id, abv.applicable FROM"
      + " pack_org_details abv WHERE abv.package_id = ?)  ";

  /**
   * Insert Package rate plan details.
   * 
   * @param con            database connection
   * @param packageId      package id
   * @param oldPackageId   old package id
   * @param isClonePackage clone package ?
   * @return flag indicating success or failure fo operation
   * @throws SQLException SQL Exception
   */
  public boolean insertPackageOrgDetails(Connection con, Integer packageId, Integer oldPackageId,
      Boolean isClonePackage) throws SQLException {
    boolean status = false;

    PreparedStatement insertPs = con.prepareStatement(INSERT_PACKAGE_ORG_DETAILS);
    if (isClonePackage) {
      insertPs = con.prepareStatement(CLONE_INSERT_PACKAGE_ORG_DETAILS);
      insertPs.setInt(1, packageId);
      insertPs.setInt(2, oldPackageId);
    } else {
      insertPs = con.prepareStatement(INSERT_PACKAGE_ORG_DETAILS);
      insertPs.setInt(1, packageId);
    }
    status = insertPs.executeUpdate() >= 0;

    insertPs.close();

    return status;
  }

  /** The Constant PACKAGE_CHARGES_ALL_ORG. */
  private static final String PACKAGE_CHARGES_ALL_ORG = "SELECT * FROM package_charges "
      + "JOIN packages using (package_id) WHERE package_id = ?";

  /**
   * Gets the package charges.
   *
   * @param packageId the package id
   * @return the package charges
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPackageCharges(int packageId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_CHARGES_ALL_ORG);
      ps.setInt(1, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant PACKAGE_ORG_DETAILS. */
  private static final String PACKAGE_ORG_DETAILS = 
      "select * from pack_org_details where package_id=? and org_id=? ";

  /**
   * Gets the package org details.
   *
   * @param packId the pack id
   * @param orgId  the org id
   * @return the package org details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageOrgDetails(int packId, String orgId) throws SQLException {
    PreparedStatement ps = null;
    List dblist = null;
    BasicDynaBean packOrgdetals = null;
    try {
      ps = con.prepareStatement(PACKAGE_ORG_DETAILS);
      ps.setInt(1, packId);
      ps.setString(2, orgId);
      dblist = DataBaseUtil.queryToDynaList(ps);
      if (dblist.size() > 0) {
        packOrgdetals = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
      } else {
        ps = con.prepareStatement(PACKAGE_ORG_DETAILS);
        ps.setInt(1, packId);
        ps.setString(2, "ORG0001");
        dblist = DataBaseUtil.queryToDynaList(ps);
        if (dblist.size() > 0) {
          packOrgdetals = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
        }
      }
      return packOrgdetals;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant PACKAGE_CHARGE. */
  private static final String PACKAGE_CHARGE = PACKAGE_CHARGES_ALL_ORG
      + " AND org_id = ? AND bed_type = ?";

  /**
   * Gets the package charge.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package charge
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageCharge(int packageId, String orgId, String bedType)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_CHARGE);
      ps.setInt(1, packageId);
      ps.setString(2, orgId);
      ps.setString(3, bedType);
      List<BasicDynaBean> lis = DataBaseUtil.queryToDynaList(ps);
      if ((lis != null) && (lis.size() > 0)) {
        return lis.get(0);
      }
      return null;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant PACKAGE_DETAILS. */
  private static final String PACKAGE_DETAILS = 
      " SELECT pm.*, pc.org_id, pc.bed_type, pc.charge, pc.discount,"
      + "  pod.item_code,pm.service_sub_group_id, pod.applicable,pod.code_type,billing_group_id, "
      + " pm.insurance_category_id,pm.submission_batch_type"
      + " FROM packages pm " + "  JOIN package_charges pc ON (pc.package_id = pm.package_id) "
      + "  JOIN pack_org_details pod ON (pod.package_id = pm.package_id "
      + " AND pod.org_id = pc.org_id) "
      + " WHERE pm.package_id=? AND pc.org_id=? AND pc.bed_type=?";

  /** The Constant PACKAGE_COMPONENT_DETAILS. */
  private static final String PACKAGE_COMPONENT_DETAILS =
      "SELECT pc.*,coalesce(test.test_name, s.service_name, "
      + "  om.operation_name, sid.medicine_name, oi.item_name)  as activity_description, "
      + "  pm.package_name,"
      + " coalesce(tod.item_code, sod.item_code, ood.item_code, "
      + "  cod.item_code, '') "
      + "  as ct_code, coalesce(tod.code_type, sod.code_type, ood.code_type, cod.code_type, '') "
      + "  as code_type,coalesce(test.allow_zero_claim_amount, s.allow_zero_claim_amount, "
      + "  om.allow_zero_claim_amount, ct.allow_zero_claim_amount, '') "
      + "  as allow_zero_claim_amount, "
      + "  coalesce(test.ddept_id,s.serv_dept_id::text) AS act_department_id"
      + "  FROM package_contents pc"
      + "  JOIN packages pm ON pm.package_id = pc.package_id "
      + "  JOIN package_content_charges pcc ON pcc.package_content_id = pc.package_content_id "
      + "  LEFT JOIN operation_master om ON om.op_id = pc.activity_id  "
      + "  LEFT JOIN operation_org_details ood ON(ood.operation_id=om.op_id "
      + "  AND pcc.org_id=ood.org_id) "
      + "  LEFT JOIN store_item_details sid ON ("
      + "  sid.medicine_id::character varying = pc.activity_id "
      + "  AND pc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
      + "  =pc.activity_id AND oi.entity=pc.activity_type)"
      + "  LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id) "
      + "  LEFT JOIN test_org_details tod ON (test.test_id=tod.test_id and pcc.org_id = tod.org_id)"
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + "  LEFT JOIN service_org_details sod ON (sod.service_id = s.service_id AND "
      + "  pcc.org_id=sod.org_id) "
      + "  LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id)"
      + "  LEFT JOIN consultation_org_details cod"
      + "  ON (cod.consultation_type_id=ct.consultation_type_id AND pcc.org_id=cod.org_id)"
      + "  WHERE pm.package_id = ? AND pcc.org_id = ? and pcc.bed_type = ?  "
      + "  ORDER BY pc.package_content_id,pc.display_order";

  /**
   * Gets the packge component details.
   *
   * @param packageId the package id
   * @return the packge component details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPackgeComponentDetails(int packageId, 
       String orgId, String bedType) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
       PreparedStatement ps = con.prepareStatement(PACKAGE_COMPONENT_DETAILS)) {
      ps.setInt(1, packageId);
      ps.setString(2, orgId);
      ps.setString(3, bedType);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant PACKAGE_CONTENT_DETAIL. */
  private static final String PACKAGE_CONTENT_DETAIL = 
      "SELECT * FROM package_contents WHERE package_content_id = ? and package_id=?";

  /**
   * Gets the package content detail.
   *
   * @param packageId        the package id
   * @param packageContentId the package content id
   * @return the package content detail
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageContentDetail(int packageId, Integer packageContentId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_CONTENT_DETAIL);
      ps.setInt(1, packageContentId);
      ps.setInt(2, packageId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant package_charge_components. */
  private static final String package_charge_components = 
      "select charge,discount,pack_ob_id,package_id,activity_id,"
      + " charge_group,charge_head,chargehead_name,"
      + " activity_description,activity_remarks,activity_units,description,"
      + " operation_id from package_contents"
      + " join packages using(package_id)" + " right join package_charges using(package_id)"
      + " left join chargehead_constants on(chargehead_id=charge_head) "
      + " where package_id=? and org_id=? and bed_type=?";

  /** The Constant template_components. */
  private static final String template_components = "select pack_ob_id,"
      + " package_id,activity_id,charge_group,charge_head,chargehead_name,"
      + " activity_description,activity_remarks,activity_units,description,"
      + " operation_id from package_contents"
      + " right join packages using(package_id)"
      + " right join pack_org_details using(package_id)"
      + " left join chargehead_constants on(chargehead_id=charge_head) "
      + " where package_id=? and org_id=?";

  /**
   * Gets the package component with charge.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package component with charge
   * @throws SQLException the SQL exception
   */
  public static List getPackageComponentWithCharge(int packageId, String orgId, String bedType)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List components = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(package_charge_components);
      ps.setInt(1, packageId);
      ps.setString(2, orgId);
      ps.setString(3, bedType);
      components = DataBaseUtil.queryToArrayList(ps);
      if (components.size() == 0) {
        ps = con.prepareStatement(template_components);
        ps.setInt(1, packageId);
        ps.setString(2, orgId);
        components = DataBaseUtil.queryToArrayList(ps);

      }
      return components;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PACKAGE_COMPONENTS. */
  private static final String GET_PACKAGE_COMPONENTS = 
      " SELECT type, activity_id, pm.package_id, operation_id, "
      + " coalesce(test.test_name, s.service_name, operation_name, "
      + " sid.medicine_name, oi.item_name) as activity_description, "
      + "  operation_name, charge_head, chargehead_name, activity_qty, activity_qty_uom as"
      + " activity_units, pm.description, pm.service_sub_group_id, activity_type AS item_type,"
      + " consultation_type_id, charge_head as activity_charge,pm.insurance_category_id,"
      + " pm.package_category_id, package_content_id as pack_ob_id, pm.multi_visit_package,"
      + " pm.package_name,  (case when coalesce(test.conducting_doc_mandatory, "
      + " s.conducting_doc_mandatory, 'N') = 'O' then true "
      + " else false end) as conducting_doc_mandatory, "
      + " coalesce(test.test_name, s.service_name, sid.medicine_name, oi.item_name, "
      + " operation_name, '') as item_name, "
      + " pc.activity_remarks as remarks, "
      + " coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
      + " coalesce(test.additional_info_reqts, '') as additional_info_reqts, pm.billing_group_id,  "
      + " pc.package_content_id, pm.submission_batch_type"
      + " FROM packages pm "
      + "   LEFT JOIN package_contents pc ON pm.package_id = pc.package_id "
      + "   LEFT JOIN chargehead_constants cc ON cc.chargehead_id = charge_head "
      + "   LEFT JOIN operation_master ON op_id = operation_id "
      + " LEFT JOIN store_item_details sid ON ("
      + " sid.medicine_id::character varying = pc.activity_id "
      + "  AND pc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
      + "  =pc.activity_id AND oi.entity=pc.activity_type)"
      + " LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id) "
      + " LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + " WHERE pm.package_id = ? ORDER BY pc.display_order";

  /**
   * Gets the package components.
   *
   * @param packageId the package id
   * @return the package components
   * @throws SQLException the SQL exception
   */
  public static List getPackageComponents(int packageId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PACKAGE_COMPONENTS);
      ps.setInt(1, packageId);
      List opId = DataBaseUtil.queryToDynaList(ps);
      return opId;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MVP_FOR_CHANNELING_RESOURCE. */
  private static final String GET_MVP_FOR_CHANNELING_RESOURCE =
      " SELECT pc.package_id from package_contents pc "
      + " JOIN packages pm using(package_id) "
      + " WHERE pc.activity_id like ? and pm.chanelling = 'Y' and status = 'A'";

  /**
   * Gets the MVP for channeling res.
   *
   * @param activityId the activity id
   * @return the MVP for channeling res
   * @throws SQLException the SQL exception
   */
  public static List getMvpForChannelingRes(String activityId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_MVP_FOR_CHANNELING_RESOURCE);
      ps.setString(1, activityId);
      List packId = DataBaseUtil.queryToDynaList(ps);
      return packId;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * The Constant GET_CHANNELLING_MULTIVISIT_PACKAGE_UNORDERED_COMPONENT_DETAILS.
   */
  private static final String GET_CHANNELLING_MULTIVISIT_PACKAGE_UNORDERED_COMPONENT_DETAILS = 
      "select pd.pack_ob_id, pd.package_id, pd.activity_id, pd.charge_head, cc.chargehead_name,"
      + " pd.activity_description, pd.activity_qty, pd.activity_type, "
      + " pd.activity_charge, pd.consultation_type_id, pd.activity_remarks, d.doctor_name, "
      + " COALESCE(test.mandate_additional_info, 'N') as mandate_additional_info, "
      + " COALESCE(test.additional_info_reqts, '') as additional_info_reqts "
      + " from package_contents pd "
      + " JOIN chargehead_constants cc on (pd.charge_head = cc.chargehead_id) "
      + " LEFT JOIN diagnostics test ON (pd.activity_id=test.test_id) "
      + " JOIN doctors d on (pd.activity_id = d.doctor_id) "
      + " where activity_id not in (select item_id FROM patient_multivisit_orders_view pmov "
      + " LEFT JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
      + " WHERE ppr.package_id = ? AND pmov.mr_no = ? "
      + " AND pmov.status != 'X' and pat_package_id = ? "
      + " GROUP BY pmov.mr_no,item_id,ppr.package_id,ppr.pat_package_id) and package_id = ? ";

  /**
   * Gets the channelling multi visit package unordered component details.
   *
   * @param packageId    the package id
   * @param mrNo         the mr no
   * @param patPackageId the pat package id
   * @return the channelling multi visit package unordered component details
   * @throws SQLException the SQL exception
   */
  public static List getChannellingMultiVisitPackageUnorderedComponentDetails(int packageId,
      String mrNo, int patPackageId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_CHANNELLING_MULTIVISIT_PACKAGE_UNORDERED_COMPONENT_DETAILS);
      ps.setInt(1, packageId);
      ps.setString(2, mrNo);
      ps.setInt(3, patPackageId);
      ps.setInt(4, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS. */
  private static final String GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS = 
      "SELECT * FROM "
      + " (SELECT pmov.mr_no,item_id,sum(quantity) as consumed_qty,ppr.package_id,"
      + " ppr.pat_package_id, to_char(MAX(pmov.ordered_on), "
      + " 'YYYY-MM-DD HH24:MI:SS') as last_ordered "
      + "   FROM patient_multivisit_orders_view pmov "
      + "   LEFT JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
      + " WHERE ppr.package_id = ? AND pmov.mr_no = ? AND pmov.status != 'X'"
      + " GROUP BY pmov.mr_no,item_id,ppr.package_id,ppr.pat_package_id) as foo"
      + " JOIN patient_packages pp USING(pat_package_id) WHERE pp.status NOT IN('X','C')";

  /**
   * Gets the multi visit package component quantity details.
   *
   * @param packageId the package id
   * @param mrNo      the mr no
   * @return the multi visit package component quantity details
   * @throws SQLException the SQL exception
   */
  public static List getMultiVisitPackageComponentQuantityDetails(int packageId, String mrNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS);
      ps.setInt(1, packageId);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_FRESH_MULTIVISIT_PACKAGE_COMPONENT_QUANTITY_DETAILS. */
  private static final String GET_FRESH_MULTIVISIT_PACKAGE_COMPONENT_QUANTITY_DETAILS = 
      " SELECT activity_id as item_id,0 as consumed_qty "
      + " FROM package_contents pc WHERE package_id = ?";

  /**
   * Gets the fresh multi visit package component quantity details.
   *
   * @param packageId the package id
   * @return the fresh multi visit package component quantity details
   * @throws SQLException the SQL exception
   */
  public static List getFreshMultiVisitPackageComponentQuantityDetails(int packageId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_FRESH_MULTIVISIT_PACKAGE_COMPONENT_QUANTITY_DETAILS);
      ps.setInt(1, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PACKAGE_ORDERED_QUANTITY_DETAILS. */
  private static final String PACKAGE_ORDERED_QUANTITY_DETAILS = 
      " SELECT pp.mr_no,pp.package_id,item_id,sum(quantity) as consumed_qty "
      + "   FROM patient_multivisit_orders_view pmov "
      + "   LEFT JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
      + "   LEFT JOIN patient_packages pp USING(pat_package_id)"
      + " WHERE pp.package_id = ? AND pp.mr_no = ? AND pmov.status != 'X' AND pp.status = 'P' "
      + " GROUP BY pp.mr_no,pp.package_id,item_id";

  /**
   * Gets the ordered package items.
   *
   * @param packageId the package id
   * @param mrNo      the mr no
   * @return the ordered package items
   * @throws SQLException the SQL exception
   */
  private List getOrderedPackageItems(int packageId, String mrNo) throws SQLException {
    PreparedStatement ps = null;
    List result = null;
    try {
      ps = con.prepareStatement(GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS);
      ps.setInt(1, packageId);
      ps.setString(2, mrNo);
      result = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return result;
  }

  /**
   * Update multivisit package status.
   *
   * @param packageId the package id
   * @param mrNo      the mr no
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateMultivisitPackageStatus(int packageId, String mrNo)
      throws SQLException, IOException {

    List<BasicDynaBean> orderedItems = getOrderedPackageItems(packageId, mrNo);
    List<BasicDynaBean> packageItems = getPackageComponents(packageId);
    Map orderMap = DataBaseUtil.mapDynaRowSet(orderedItems, "item_id");
    boolean itemsPending = false;
    boolean updated = true;
    logger.debug("Processing package item list :" + packageItems.size());
    for (BasicDynaBean packageItem : packageItems) {
      Integer consultationTypeId = (Integer) packageItem.get("consultation_type_id");
      String itemId = (String) packageItem.get("activity_id");
      String itemType = (String) packageItem.get("item_type");
      itemId = ("Doctor".equals(itemType)) ? consultationTypeId + "" : itemId;
      if (null == itemId) {
        continue; // we should not hit this, really.
      }

      BasicDynaBean item = (BasicDynaBean) orderMap.get(itemId);
      if (item == null) { // item is not ordered, so the package is pending
        logger.debug("Item not found in order map :" + itemId);
        itemsPending = true;
        break;
      }
      Integer itemQty = (Integer) packageItem.get("activity_qty");
      Integer consumedQty = (null != item.get("consumed_qty"))
          ? ((BigDecimal) item.get("consumed_qty")).intValue()
          : 0;
      if (consumedQty < itemQty) {
        logger
            .debug("Consumed qty < package qty :" + consumedQty + "<" + itemQty + "for " + itemId);
        itemsPending = true;
        break;
      }
    }
    if (!itemsPending) {
      logger.debug("No items pending after processing the item list");
      updated = false;
      Map<String, Object> keyMap = new HashMap<String, Object>();
      keyMap.put("package_id", packageId);
      keyMap.put("mr_no", mrNo);
      keyMap.put("status", "P");

      BasicDynaBean bean = pdao.findByKey(this.con, keyMap); 
      // There should be only one active pacakge for an mr_no and package_id combination
      if (null == bean) {
        logger.info("No active patient package to update status");
      } else {
        String currentStatus = (String) bean.get("status");
        if (!"X".equalsIgnoreCase(currentStatus)) {
          logger.debug(
              "setting the status to completed :" + packageId + ":" + mrNo + ":" + currentStatus);
          bean.set("status", "C");
        }
        int updateCount = pdao.updateWithName(con, bean.getMap(), "pat_package_id");
        updated = (updateCount >= 0);
      }
    }
    return updated;
  }

  /**
   * The Constant
   * GET_CHANNELLING_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS.
   */
  private static final String GET_CHANNELLING_MVP_COMPONENT_ORDERED_QUANTITY_DETAILS = 
      "SELECT * FROM "
      + " (SELECT pmov.mr_no,item_id,sum(quantity) as consumed_qty,ppr.package_id,"
      + "  ppr.pat_package_id  FROM patient_multivisit_orders_view pmov "
      + "   LEFT JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
      + " WHERE ppr.package_id = ? AND pmov.mr_no = ? AND pmov.status != 'X'"
      + " GROUP BY pmov.mr_no,item_id,ppr.package_id,ppr.pat_package_id) as foo"
      + " JOIN patient_packages pp USING(pat_package_id) WHERE pp.status "
      + " NOT IN('X','C') AND pp.pat_package_id = ?";

  /**
   * Gets the ordered package items for channelling.
   *
   * @param packageId    the package id
   * @param mrNo         the mr no
   * @param patPackageId the pat package id
   * @return the ordered package items for channelling
   * @throws SQLException the SQL exception
   */
  private List getOrderedPackageItemsForChannelling(int packageId, String mrNo, int patPackageId)
      throws SQLException {
    PreparedStatement ps = null;
    List result = null;
    try {
      ps = con
          .prepareStatement(GET_CHANNELLING_MVP_COMPONENT_ORDERED_QUANTITY_DETAILS);
      ps.setInt(1, packageId);
      ps.setString(2, mrNo);
      ps.setInt(3, patPackageId);
      result = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return result;
  }

  /**
   * Update channelling multivisit package status.
   *
   * @param packageId    the package id
   * @param mrNo         the mr no
   * @param patPackageId the pat package id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateChannellingMultivisitPackageStatus(int packageId, String mrNo,
      int patPackageId) throws SQLException, IOException {
    List<BasicDynaBean> orderedItems = getOrderedPackageItemsForChannelling(packageId, mrNo,
        patPackageId);
    List<BasicDynaBean> packageItems = getPackageComponents(packageId);
    Map orderMap = DataBaseUtil.mapDynaRowSet(orderedItems, "item_id");
    boolean itemsPending = false;
    boolean updated = true;
    logger.debug("Processing package item list :" + packageItems.size());
    for (BasicDynaBean packageItem : packageItems) {
      Integer consultationTypeId = (Integer) packageItem.get("consultation_type_id");
      String itemId = null;
      if (((String) packageItem.get("item_type")).equals("Doctor")) {
        itemId = consultationTypeId + "";
      } else {
        itemId = (String) packageItem.get("activity_id");
      }

      // itemId = ("Doctor".equals(itemId)) ? consultationTypeId+"" : itemId;
      if (null == itemId) {
        continue; // we should not hit this, really.
      }

      BasicDynaBean item = (BasicDynaBean) orderMap.get(itemId);
      if (item == null) { 
        // item is not ordered, so the package is pending
        logger.debug("Item not found in order map :" + itemId);
        itemsPending = true;
        break;
      }

      Integer consumedQty = (null != item.get("consumed_qty"))
          ? ((BigDecimal) item.get("consumed_qty")).intValue()
          : 0;
      Integer itemQty = (Integer) packageItem.get("activity_qty");
      if (consumedQty < itemQty) {
        logger
            .debug("Consumed qty < package qty :" + consumedQty + "<" + itemQty + "for " + itemId);
        itemsPending = true;
        break;
      }
    }
    if (!itemsPending) {
      logger.debug("No items pending after processing the item list");
      updated = false;
      Map<String, Object> keyMap = new HashMap<String, Object>();
      keyMap.put("package_id", packageId);
      keyMap.put("mr_no", mrNo);
      keyMap.put("status", "P");
      keyMap.put("pat_package_id", patPackageId);
      BasicDynaBean bean = pdao.findByKey(this.con, keyMap); 
      // There should be only one active pacakge for an mr_no and package_id combination
      if (null == bean) {
        logger.info("No active patient package to update status");
      } else {
        String currentStatus = (String) bean.get("status");
        if (!"X".equalsIgnoreCase(currentStatus)) {
          logger.debug(
              "setting the status to completed :" + packageId + ":" + mrNo + ":" + currentStatus);
          bean.set("status", "C");
        }
        int updateCount = pdao.updateWithName(con, bean.getMap(), "pat_package_id");
        updated = (updateCount >= 0);
      }
    }
    return updated;
  }

  /** The Constant PACKAGE_OPE_DETAILS. */
  private static final String PACKAGE_OPE_DETAILS = " SELECT op_id,operation_name"
      + " FROM packages p INNER JOIN (select distinct pc.package_id, pc.operation_id "
      + " from package_contents pc) as dpc on dpc.package_id = p.package_id "
      + " JOIN operation_master ON(op_id = dpc.operation_id)"
      + " WHERE p.package_id = ?";

  /**
   * Gets the package operationdetails.
   *
   * @param packageId the package id
   * @return the package operationdetails
   * @throws SQLException the SQL exception
   */
  public Hashtable getPackageOperationdetails(int packageId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_OPE_DETAILS);
      ps.setInt(1, packageId);
      List opeDetails = DataBaseUtil.queryToArrayList(ps);
      return opeDetails.size() > 0 ? (Hashtable) DataBaseUtil.queryToArrayList(ps).get(0) : null;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant ALL_PACKAGES. */
  private static final String ALL_PACKAGES =
      "select p.package_id,p.package_name from packages p where type = 'P'";

  /**
   * Gets the all packages.
   *
   * @return the all packages
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllPackages() throws SQLException {
    ArrayList lis = new ArrayList();

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(ALL_PACKAGES);
      lis = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }

    return lis;
  }

  /** The Constant INSERT_PACKAGE_CHARGE_PLUS. */
  private static final String INSERT_PACKAGE_CHARGE_PLUS = 
      "INSERT INTO package_charges(package_id,org_id,charge,bed_type)"
      + "(SELECT package_id,?,ROUND(charge + ?), bed_type FROM package_charges WHERE org_id=?)";

  /** The Constant INSERT_PACKAGE_CHARGE_MINUS. */
  private static final String INSERT_PACKAGE_CHARGE_MINUS = 
      "INSERT INTO package_charges(package_id,org_id,charge,bed_type)"
      + "(SELECT package_id,?, GREATEST(ROUND(charge - ?), 0), bed_type"
      + " FROM package_charges WHERE org_id=?)";

  /** The Constant INSERT_PACKAGE_CHARGE_BY. */
  private static final String INSERT_PACKAGE_CHARGE_BY = 
      "INSERT INTO package_charges(package_id,org_id,charge,bed_type)"
      + "(SELECT package_id,?,doroundvarying(charge,?,?), bed_type "
      + " FROM package_charges WHERE org_id=?)";

  /** The Constant INSERT_PACKAGE_WITH_DISCOUNT_BY. */
  private static final String INSERT_PACKAGE_WITH_DISCOUNT_BY = 
      "INSERT INTO package_charges(package_id,org_id,charge,discount,bed_type)"
      + "(SELECT package_id,?,doroundvarying(charge,?,?), doroundvarying(discount,?,?), bed_type "
      + " FROM package_charges WHERE org_id=?)";

  /**
   * Adds the org for packages.
   *
   * @param con                the con
   * @param newOrgId           the new org id
   * @param varianceType       the variance type
   * @param varianceValue      the variance value
   * @param varianceBy         the variance by
   * @param useValue           the use value
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addOrgForPackages(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws Exception {
    return addOrgForPackages(con, newOrgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearstRoundOfValue, false);
  }

  /**
   * Adds the org for packages.
   *
   * @param con                the con
   * @param newOrgId           the new org id
   * @param varianceType       the variance type
   * @param varianceValue      the variance value
   * @param varianceBy         the variance by
   * @param useValue           the use value
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param updateDiscounts    the update discounts
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addOrgForPackages(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {
    boolean status = false;
    PreparedStatement ps = null;
    try {
      if (useValue) {
        if (varianceType.equals("Incr")) {
          ps = con.prepareStatement(INSERT_PACKAGE_CHARGE_PLUS);
          ps.setString(1, newOrgId);
          ps.setDouble(2, varianceValue);
          ps.setString(3, baseOrgId);

          int val = ps.executeUpdate();
          logger.debug(Integer.toString(val));
          if (val >= 0) {
            status = true;
          }
        } else {
          ps = con.prepareStatement(INSERT_PACKAGE_CHARGE_MINUS);
          ps.setString(1, newOrgId);
          ps.setDouble(2, varianceValue);
          ps.setString(3, baseOrgId);

          int val = ps.executeUpdate();
          logger.debug(Integer.toString(val));
          if (val >= 0) {
            status = true;
          }
        }
      } else {
        if (!varianceType.equals("Incr")) {
          varianceBy = new Double(-varianceBy);
        }
        /*
         * ps = con.prepareStatement(INSERT_PACKAGE_CHARGE_BY); ps.setString(1,
         * newOrgId); ps.setBigDecimal(2, new BigDecimal(varianceBy));
         * ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue)); ps.setString(4,
         * baseOrgId);
         * 
         * int i = ps.executeUpdate();
         */
        int val = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue,
            updateDiscounts);
        logger.debug(Integer.toString(val));
        if (val >= 0) {
          status = true;
        }
      }
    } finally {
      if (null != ps) {
        ps.close();
      }
    }
    return status;
  }

  /** The Constant INSERT_PACKAGE_CODES_FOR_ORG. */
  private static final String INSERT_PACKAGE_CODES_FOR_ORG = "INSERT INTO pack_org_details "
      + " SELECT package_id, ?, applicable, item_code,code_type,?,'N'"
      + " FROM pack_org_details WHERE org_id=?;";

  /*
   * private static final String INSERT_PACKAGE_CODES_FOR_ORG_TEMPLATE =
   * "INSERT INTO pack_org_details SELECT package_id, ?, true, NULL" +
   * " FROM packages WHERE  type='T';";
   */

  /**
   * Adds the org codes for packages.
   *
   * @param con                the con
   * @param newOrgId           the new org id
   * @param varianceType       the variance type
   * @param varianceValue      the variance value
   * @param varianceBy         the variance by
   * @param useValue           the use value
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addOrgCodesForPackages(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws Exception {
    boolean status = false;
    PreparedStatement ps = null;
    BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
    String rateSheetId = ("N".equals((String) obean.get("is_rate_sheet")) ? baseOrgId : null);
    try {
      ps = con.prepareStatement(INSERT_PACKAGE_CODES_FOR_ORG);
      ps.setString(1, newOrgId);
      ps.setString(2, rateSheetId);
      ps.setString(3, baseOrgId);

      int val = ps.executeUpdate();
      logger.debug(Integer.toString(val));
      if (val >= 0) {
        status = true;
      }
      /*
       * if(status){ ps = con.prepareStatement(INSERT_PACKAGE_CODES_FOR_ORG_TEMPLATE);
       * ps.setString(1,newOrgId);
       * 
       * i = ps.executeUpdate(); logger.debug(i); if(i>=0)status = true; }
       */
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Insert charges by percent.
   *
   * @param con                the con
   * @param newOrgId           the new org id
   * @param baseOrgId          the base org id
   * @param varianceBy         the variance by
   * @param nearstRoundOfValue the nearst round of value
   * @param updateDiscounts    the update discounts
   * @return the int
   * @throws Exception the exception
   */
  private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
      Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

    int ndx = 1;
    int numCharges = 1;

    PreparedStatement pstmt = null;
    try {
      pstmt = con.prepareStatement(
          updateDiscounts ? INSERT_PACKAGE_WITH_DISCOUNT_BY : INSERT_PACKAGE_CHARGE_BY);
      pstmt.setString(ndx++, newOrgId);

      for (int i = 0; i < numCharges; i++) {
        pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
        pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
      }

      if (updateDiscounts) { // go one more round setting the parameters
        for (int i = 0; i < numCharges; i++) {
          pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
          pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
        }
      }

      pstmt.setString(ndx++, baseOrgId);

      return pstmt.executeUpdate();

    } finally {
      if (null != pstmt) {
        pstmt.close();
      }
    }
  }

  /** The Constant UPDATE_PKG_CHARGES_PLUS. */
  private static final String UPDATE_PKG_CHARGES_PLUS = "UPDATE package_charges totab SET "
      + " charge = round(fromtab.charge + ?)" + " FROM package_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id AND totab.bed_type = fromtab.bed_type "
      + " AND totab.is_override='N'";

  /** The Constant UPDATE_PKG_CHARGES_MINUS. */
  private static final String UPDATE_PKG_CHARGES_MINUS = "UPDATE package_charges totab SET "
      + " charge = GREATEST(round(fromtab.charge - ?), 0)" + " FROM package_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id AND totab.bed_type = fromtab.bed_type "
      + " AND totab.is_override='N'";

  /** The Constant UPDATE_PKG_CHARGES_BY. */
  private static final String UPDATE_PKG_CHARGES_BY = "UPDATE package_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?)" + " FROM package_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id AND totab.bed_type = fromtab.bed_type "
      + " AND totab.is_override='N'";

  /**
   * Update org for packages.
   *
   * @param con                the con
   * @param orgId              the org id
   * @param varianceType       the variance type
   * @param varianceValue      the variance value
   * @param varianceBy         the variance by
   * @param useValue           the use value
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static boolean updateOrgForPackages(Connection con, String orgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws SQLException, IOException {

    boolean status = false;
    PreparedStatement pstmt = null;
    try {
      if (useValue) {

        if (varianceType.equals("Incr")) {
          pstmt = con.prepareStatement(UPDATE_PKG_CHARGES_PLUS);
        } else {
          pstmt = con.prepareStatement(UPDATE_PKG_CHARGES_MINUS);
        }

        pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
        pstmt.setString(2, orgId);
        pstmt.setString(3, baseOrgId);

        int val = pstmt.executeUpdate();
        if (val >= 0) {
          status = true;
        }

      } else {

        pstmt = con.prepareStatement(UPDATE_PKG_CHARGES_BY);
        if (!varianceType.equals("Incr")) {
          varianceBy = new Double(-varianceBy);
        }

        pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
        pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
        pstmt.setString(3, orgId);
        pstmt.setString(4, baseOrgId);

        int val = pstmt.executeUpdate();
        if (val >= 0) {
          status = true;
        }

      }
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
    return status;
  }

  /**
   * Gets the diag pack components.
   *
   * @param packageId the package id
   * @return the diag pack components
   * @throws SQLException the SQL exception
   */
  public static List getDiagPackComponents(int packageId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("SELECT test_name as item_name, test_id as item_id, 'I' as item_type, "
              + " consultation_type_id " + " FROM diagnostics d "
              + " JOIN package_contents comp ON (comp.activity_id=d.test_id) "
              + " JOIN packages pm USING(package_id) "
              + " WHERE pm.package_id=? and pm.type='D'");
      ps.setInt(1, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pack components.
   *
   * @param packageId the package id
   * @return the pack components
   * @throws SQLException the SQL exception
   */
  public static List getPackComponents(int packageId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement(" SELECT coalesce(test_name, service_name, '_others') as item_name, "
              + " coalesce(test_id, service_id, '_others_id') as item_id, "
              + " case when test_name is not null then 'I' when service_name is not null then 'S' "
              + "  when activity_id='Doctor' then 'C' "
              + " else 'POC' end as item_type, consultation_type_id "
              + " FROM package_contents comp " + " JOIN packages pm USING (package_id) "
              + " LEFT JOIN diagnostics d ON (comp.activity_id=d.test_id)"
              + " LEFT JOIN services s ON (comp.activity_id=s.service_id) "
              + " WHERE pm.package_id=? and pm.visit_applicability='i'");
      ps.setInt(1, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PACKAGE_ORG_DETAILS. */
  private static final String GET_PACKAGE_ORG_DETAILS = 
      " select pm.*, porg.org_id, porg.applicable, porg.item_code, porg.code_type,od.org_name "
      + " FROM packages  pm " 
      + " JOIN pack_org_details porg on porg.package_id = pm.package_id "
      + " JOIN organization_details od on (od.org_id = porg.org_id) "
      + " WHERE pm.package_id=? and porg.org_id=? ";

  /**
   * Package org details.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean packageOrgDetails(int packageId, String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PACKAGE_ORG_DETAILS);
      ps.setInt(1, packageId);
      ps.setString(2, orgId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_CHARGES_FOR_ORG. */
  private static final String GET_ALL_CHARGES_FOR_ORG = 
      " select package_id,bed_type,charge, discount from package_charges where org_id=? ";

  /**
   * Gets the all charges for org.
   *
   * @param orgId     the org id
   * @param packageId the package id
   * @return the all charges for org
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllChargesForOrg(String orgId, String packageId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG + "and package_id=? ");
      ps.setString(1, orgId);
      ps.setInt(2, Integer.parseInt(packageId));
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE. */
  // multivisitpackage
  private static final String GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE = 
      " SELECT charge FROM package_item_charges where org_id = ? AND bed_type = ? "
      + " AND package_id = ? AND pack_ob_id = ?";

  /**
   * Gets the multi visit package item charge.
   *
   * @param packageId the package id
   * @param itemBean  the item bean
   * @param itemId    the item id
   * @param bedType   the bed type
   * @param orgId     the org id
   * @param itemType  the item type
   * @param qty       the qty
   * @return the multi visit package item charge
   * @throws Exception the exception
   */
  public static BigDecimal getMultiVisitPackageItemCharge(String packageId, BasicDynaBean itemBean,
      String itemId, String bedType, String orgId, String itemType, BigDecimal qty)
      throws Exception {

    Connection con = null;
    PreparedStatement ps = null;
    String activityId = "";
    BasicDynaBean resultBean = null;
    BigDecimal charge = BigDecimal.ZERO;
    Integer packageIdInt = Integer.parseInt(packageId);
    Integer itemTotalQtyInt;
    String packObId;
    try {
      itemTotalQtyInt = DataBaseUtil.getIntValueFromDb(
          "SELECT activity_qty FROM package_contents " + " WHERE package_id = '"
              + Integer.parseInt(packageId) + "' AND activity_id= '" + itemId + "' ");

      packObId = DataBaseUtil.getStringValueFromDb(
          "SELECT pack_ob_id FROM package_contents " + " WHERE package_id = '"
              + Integer.parseInt(packageId) + "' AND activity_id= '" + itemId + "' ");

      if (itemType.equals("doctor")) {
        activityId = (String) itemBean.get("doctor_name");
        packObId = DataBaseUtil.getStringValueFromDb(
            "SELECT pack_ob_id FROM package_contents " + " WHERE package_id = '"
                + Integer.parseInt(packageId) + "' AND activity_id= '" + activityId + "' "
                + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");
        itemTotalQtyInt = DataBaseUtil.getIntValueFromDb(
            "SELECT activity_qty FROM package_contents " + " WHERE package_id = '"
                + Integer.parseInt(packageId) + "' AND activity_id= '" + activityId + "' "
                + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");

        if (packObId == null) {

          activityId = "Doctor";
          packObId = DataBaseUtil.getStringValueFromDb(
              "SELECT pack_ob_id FROM package_contents " + " WHERE package_id = '"
                  + Integer.parseInt(packageId) + "' AND activity_id= '" + activityId + "' "
                  + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");
          itemTotalQtyInt = DataBaseUtil.getIntValueFromDb(
              "SELECT activity_qty FROM package_contents " + " WHERE package_id = '"
                  + Integer.parseInt(packageId) + "' AND activity_id= '" + activityId + "' "
                  + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");
        }
      }

      BigDecimal itemTotalQtyNumeric = BigDecimal.ZERO;
      itemTotalQtyNumeric = itemTotalQtyInt == null ? BigDecimal.ZERO
          : new BigDecimal(itemTotalQtyInt);
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE);
      ps.setString(1, orgId);
      ps.setString(2, bedType);
      ps.setInt(3, Integer.parseInt(packageId));
      ps.setString(4, packObId);
      resultBean = DataBaseUtil.queryToDynaBean(ps);

      if (resultBean != null) {
        charge = (BigDecimal) resultBean.get("charge");
      }

      if (resultBean == null) {
        ps = null;
        ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE);
        ps.setString(1, "ORG0001");
        ps.setString(2, "GENERAL");
        ps.setInt(3, Integer.parseInt(packageId));
        ps.setString(4, packObId);
        charge = DataBaseUtil.getBigDecimalValueFromDb(ps);
      }
      charge = charge.compareTo(BigDecimal.ZERO) != 0
          ? charge.divide(itemTotalQtyNumeric, RoundingMode.CEILING).multiply(qty)
          : BigDecimal.ZERO;

      return charge;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the doc specific multi visit package doc item charge.
   *
   * @param packageId the package id
   * @param itemId    the item id
   * @param bedType   the bed type
   * @param orgId     the org id
   * @param docId     the doc id
   * @param qty       the qty
   * @return the doc specific multi visit package doc item charge
   * @throws Exception the exception
   */
  public static BigDecimal getDocSpecificMultiVisitPackageDocItemCharge(String packageId,
      String itemId, String bedType, String orgId, String docId, BigDecimal qty) throws Exception {

    Connection con = null;
    PreparedStatement ps = null;
    String activityId = "";
    BasicDynaBean resultBean = null;
    BigDecimal charge = BigDecimal.ZERO;
    try {
      Integer itemTotalQtyInt = null;
      String packObId = null;

      activityId = docId;
      packObId = DataBaseUtil.getStringValueFromDb("SELECT pack_ob_id FROM package_contents "
          + " WHERE package_id = '" + Integer.parseInt(packageId) + "' AND activity_id= '"
          + activityId + "' " + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");
      itemTotalQtyInt = DataBaseUtil.getIntValueFromDb(
          "SELECT activity_qty FROM package_contents " + " WHERE package_id = '"
              + Integer.parseInt(packageId) + "' AND activity_id= '" + activityId + "' "
              + " AND consultation_type_id = '" + Integer.parseInt(itemId) + "' ");

      BigDecimal itemTotalQtyNumeric = BigDecimal.ZERO;
      itemTotalQtyNumeric = itemTotalQtyInt == null ? BigDecimal.ZERO
          : new BigDecimal(itemTotalQtyInt);
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE);
      ps.setString(1, orgId);
      ps.setString(2, bedType);
      ps.setInt(3, Integer.parseInt(packageId));
      ps.setString(4, packObId);
      resultBean = DataBaseUtil.queryToDynaBean(ps);

      if (resultBean != null) {
        charge = (BigDecimal) resultBean.get("charge");
      }

      if (resultBean == null) {
        ps = null;
        ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE);
        ps.setString(1, "ORG0001");
        ps.setString(2, "GENERAL");
        ps.setInt(3, Integer.parseInt(packageId));
        ps.setString(4, packObId);
        charge = DataBaseUtil.getBigDecimalValueFromDb(ps);
      }
      charge = charge.compareTo(BigDecimal.ZERO) != 0
          ? charge.divide(itemTotalQtyNumeric, RoundingMode.CEILING).multiply(qty)
          : BigDecimal.ZERO;

      return charge;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MULTI_VISIT_PACKAGE_STATUS. */
  private static final String GET_MULTI_VISIT_PACKAGE_STATUS = 
      "SELECT status FROM patient_packages where package_id = ? AND pat_package_id = ?";

  /**
   * Gets the multi visit package status.
   *
   * @param patPackId the pat pack id
   * @param packageId the package id
   * @return the multi visit package status
   * @throws Exception the exception
   */
  public static String getMultiVisitPackageStatus(int patPackId, int packageId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_STATUS);
      ps.setInt(1, packageId);
      ps.setInt(2, patPackId);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The get GET_MULTI_VISIT_OBPACKAGE_ID. */
  private static final String GET_MULTI_VISIT_OBPACKAGE_ID =
      "SELECT package_content_id as pack_ob_id "
      + " FROM package_contents WHERE package_id = ? AND #";

  /**
   * Gets the multi visit package ob id.
   *
   * @param itemId    the item id
   * @param packageId the package id
   * @param itemType  the item type
   * @return the multi visit package ob id
   * @throws Exception the exception
   */
  public String getMultiVisitPackageObId(String itemId, String packageId, String itemType)
      throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    String query = "";
    try {
      con = DataBaseUtil.getConnection();
      if (itemType.equals("doctor")) {
        query = GET_MULTI_VISIT_OBPACKAGE_ID.replace("#",
            " consultation_type_id = ? AND activity_id = 'Doctor'");
      } else {
        query = GET_MULTI_VISIT_OBPACKAGE_ID.replace("#", " activity_id = ?");
      }
      ps = con.prepareStatement(query);
      ps.setInt(1, Integer.parseInt(packageId));
      if (itemType.equals("doctor")) {
        ps.setInt(2, Integer.parseInt(itemId));
      } else {
        ps.setString(2, itemId);
      }
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MULTI_VISIT_PACKAGE_IDS. */
  private static final String GET_MULTI_VISIT_PACKAGE_IDS = 
      "SELECT package_id FROM patient_packages where mr_no = ? "
      + " AND status IN('P') group by package_id";

  /**
   * Gets the multi visit package id S.
   *
   * @param mrNo the mr no
   * @return the multi visit package id S
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getMultiVisitPackageIdS(String mrNo) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_MULTI_VISIT_PACKAGE_IDS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PACKAGE_IDS. */
  private static final String GET_PACKAGE_IDS =
      "SELECT package_id FROM packages WHERE status = 'A' order by package_id";

  /**
   * Gets the package ids.
   *
   * @return the package ids
   * @throws Exception the exception
   */
  public static List<String> getPackageIds() throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<String> resultList = new ArrayList<String>();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PACKAGE_IDS);
      rs = ps.executeQuery();
      while (rs.next()) {
        resultList.add(rs.getString(1));
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
      if (rs != null) {
        rs.close();
      }
    }
    return resultList;
  }

  /** The Constant STATIC_PACKAGE_FIELDS. */
  private static final String STATIC_PACKAGE_FIELDS = "SELECT * ";

  /** The Constant STATIC_PACKAGE_COUNT. */
  private static final String STATIC_PACKAGE_COUNT = " SELECT count(package_id) ";

  /** The Constant STATIC_PACKAGE_TABLES. */
  private static final String STATIC_PACKAGE_TABLES = 
      " FROM (SELECT pc.operation_id,om.operation_name,"
      + " CASE WHEN pc.panel_id is not null THEN "
      + " concat(test.test_name, ': ', pap.package_name) "
      + " ELSE coalesce(test.test_name, s.service_name, "
      + " om.operation_name, sid.medicine_name, oi.item_name) END as activity_description, "
      + " pc.activity_qty,pc.activity_type,pm.package_id, pc.display_order, "
      + " pc.consultation_type_id "
      + " FROM packages pm "
      + " LEFT JOIN package_contents pc on  (pm.package_id = pc.package_id) "
      + " LEFT JOIN operation_master om ON (om.op_id = pc.activity_id "
      + " AND pc.activity_type ='Operation') "
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id::character varying = pc.activity_id"
      + " AND pc.activity_type ='Inventory') "
      + " LEFT JOIN orderable_item oi ON (oi.entity_id = pc.activity_id "
      + " AND oi.entity=pc.activity_type) "
      + " LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id) "
      + " LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + " LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + " LEFT JOIN packages pap ON (pap.package_id=pc.panel_id) "
      + " UNION ALL "
      + " SELECT pc.operation_id,om.operation_name,om.operation_name AS activity_description, "
      + " '1' AS activity_qty, 'Operation' AS activity_type, pm.package_id, 0 as display_order, "
      + " null as consultation_type_id   FROM packages pm "
      + " LEFT JOIN package_contents pc on  (pm.package_id = pc.package_id) "
      + " LEFT JOIN operation_master om ON (om.op_id = pc.operation_id) "
      + " WHERE (pc.operation_id is not null AND pc.operation_id != '') ) AS foo ";

  /**
   * Gets the static packge component details.
   *
   * @param packageId     the package id
   * @param listingParams the listing params
   * @return the static packge component details
   * @throws SQLException the SQL exception
   */
  public static PagedList getStaticPackgeComponentDetails(int packageId, Map listingParams)
      throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    try {
      String sortOrder = "package_id";
      Integer pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
      Integer pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
      String pkgid = Integer.toString(packageId);

      if (pkgid == null || pkgid.equals("")) {
        return new PagedList(new ArrayList(), 0, pageSize, pageNum);
      }

      qb = new SearchQueryBuilder(con, STATIC_PACKAGE_FIELDS, STATIC_PACKAGE_COUNT,
          STATIC_PACKAGE_TABLES, null, sortOrder, true, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.INTEGER, "package_id", "=", packageId);
      qb.addSecondarySort("display_order");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }
  }

  /** The Constant GET_Package_ITEM_SUBGROUP_DETAILS. */
  private static final String GET_Package_ITEM_SUBGROUP_DETAILS = 
      "select pisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,"
      + " item_group_name,igt.item_group_type_id,igt.item_group_type_name "
      + " from package_item_sub_groups pisg "
      + " left join item_sub_groups isg on (isg.item_subgroup_id = pisg.item_subgroup_id) "
      + " left join packages pm on (pm.package_id = pisg.package_id) "
      + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
      + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
      + " where pisg.package_id = ? ";

  /**
   * Gets the package item sub group details.
   *
   * @param packId the pack id
   * @return the package item sub group details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPackageItemSubGroupDetails(int packId) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_Package_ITEM_SUBGROUP_DETAILS);
      ps.setInt(1, packId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS = 
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM package_item_sub_groups pisg "
      + " JOIN item_sub_groups isg ON(pisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE pisg.package_id = ? ";

  /**
   * Gets the package item sub group tax details.
   *
   * @param itemId the item id
   * @return the package item sub group tax details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPackageItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setInt(1, Integer.parseInt(itemId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the active insurance categories.
   *
   * @param packageId the package id
   * @return the active insurance categories
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getActiveInsuranceCategories(Integer packageId)
      throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setInt(1, packageId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SELECT_INSURANCE_CATEGORY_IDS. */
  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM packages_insurance_category_mapping WHERE package_id =?";

  private static final String GET_ALL_RATEPLANS = "SELECT rpp.* from organization_details od"
      + " LEFT JOIN rate_plan_parameters rpp ON (rpp.org_id = od.org_id and od.is_rate_sheet = 'N')"
      + " JOIN (SELECT org_id, min(priority) as priority"
      + "   from rate_plan_parameters GROUP BY org_id) as x "
      + "ON (rpp.org_id = x.org_id and rpp.priority = x.priority)";

  /**
   * Get all Rateplans.
   * 
   * @return List of rateplans
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getAllRatePlans() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALL_RATEPLANS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Schedule Job for processing package charges.
   * 
   * @param packageId  package id
   * @param packCharge package charge
   */
  public void packageChargeScheduleJob(Integer packageId, BigDecimal packCharge) {
    LinkedHashMap<String, Object> queryParams = new LinkedHashMap<String, Object>();
    // Maintain the queryparams as per package charges index
    queryParams.put("package_id", packageId);
    queryParams.put("org_id", "abov.org_id");
    queryParams.put("bed_type", "abov.bed_type");
    queryParams.put("charge", packCharge);
    queryParams.put("discount", BigDecimal.ZERO);
    CronJobService cronJobService = ApplicationContextProvider.getBean(CronJobService.class);
    cronJobService.masterChargeScheduleJob(queryParams, "package_charges", "PACKAGE");
  }

  /** The Constant PACKAGE_COMPONENT_DETAILS. */
  private static final String PACKAGE_CONTENT_DETAILS =
        " SELECT pcc.package_content_id,pcc.org_id,pcc.bed_type,"
      + " pcc.charge,pcc.discount,pcc.is_override,p.package_id,"
      + " p.package_name,p.package_code,"
      + " p.service_sub_group_id,p.allow_discount,p.allow_rate_increase,"
      + " p.allow_rate_decrease,p.billing_group_id,p.insurance_category_id,"
      + " p.description,pod.applicable,"
      + " coalesce(tod.item_code, sod.item_code, ood.item_code, "
      + " cod.item_code, '') AS item_code, "
      + " coalesce(tod.code_type, sod.code_type, ood.code_type, cod.code_type, '') As code_type,"
      + " p.submission_batch_type"
      + " FROM package_content_charges pcc "
      + " JOIN package_contents pc ON pc.package_content_id =  pcc.package_content_id "
      + " JOIN packages p ON p.package_id=pc.package_id "
      + " JOIN pack_org_details pod ON pod.package_id=p.package_id AND pcc.org_id=pod.org_id "
      + " LEFT JOIN operation_org_details ood ON(ood.operation_id=pc.activity_id "
      + " AND pcc.org_id=ood.org_id) AND pc.activity_type='Operation'"
      + " LEFT JOIN test_org_details tod ON(tod.test_id=pc.activity_id "
      + " AND pcc.org_id=tod.org_id) AND pc.activity_type in('Radiology','Laboratory')"
      + " LEFT JOIN consultation_org_details cod "
      + " ON(cod.consultation_type_id=pc.consultation_type_id AND pcc.org_id=cod.org_id)"
      + " LEFT JOIN service_org_details sod ON (sod.service_id = pc.activity_id AND "
      + " pcc.org_id=sod.org_id) AND pc.activity_type='Service'"
      + " WHERE pcc.package_content_id=? AND pcc.org_id =? AND pcc.bed_type = ? ";

  /**
   * Gets the package content details.
   *
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPackageContentDetails(int packContentId, String orgId,
      String bedType) throws SQLException {
    return DataBaseUtil.queryToDynaBean(PACKAGE_CONTENT_DETAILS,
        new Object[] { packContentId, orgId, bedType });
  }


  /** The Constant GET_RATE_PLAN_APPLICABLE. */
  private static final String GET_RATE_PLAN_APPLICABLE =
      " SELECT * from pack_org_details "
      + " WHERE package_id=? AND org_id=? ";
  /**
   * Gets the rate plan applicability.
   *
   * @param packId
   *          the pack id
   * @return the rate plan bean
   * @throws SQLException
   *           the SQL exception
   */

  public static BasicDynaBean getRatePlanBean(int packId, String orgId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_RATE_PLAN_APPLICABLE,
       new Object[] { packId, orgId });
  }


  /** The Constant PACKAGE_CONTENT_CHARGES_FOR_INVENTORY. */
  private static final String PACKAGE_CONTENT_CHARGES_FOR_INVENTORY =
        " SELECT SUM(charge) as charge,SUM(discount) as discount "
      + " FROM package_content_charges pcc "
      + " JOIN package_contents pc ON pc.package_content_id =  pcc.package_content_id "
      + " JOIN packages p ON p.package_id=pc.package_id "
      + " WHERE pc.activity_type='Inventory' "
      + " AND p.package_id=? AND pcc.org_id =? AND pcc.bed_type = ? ";

  /**
   * Gets the package content Charge for inventory.
   *
   * @param orgId     the org id
   * @param bedType   the bed type
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPackContChargesForInventory(int packageId, String orgId,
      String bedType) throws SQLException {
    return DataBaseUtil.queryToDynaBean(PACKAGE_CONTENT_CHARGES_FOR_INVENTORY,
        new Object[] { packageId, orgId, bedType });
  }

}
