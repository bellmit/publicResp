package com.insta.hms.adminmaster.packagemaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.organization.RateMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.master.packages.CenterDAO;
import com.insta.hms.master.packages.SponsorDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


// TODO: Auto-generated Javadoc
/**
 * The Class PackageMasterAction.
 */
public class PackageMasterAction extends DispatchAction {

  /** The bo. */
  private static PackageBO bo = new PackageBO();

  /** The doc DAO. */
  private static PackageDocDAO docDAO = new PackageDocDAO();

  /** The pkg chrg DAO. */
  private static PackageChargeDAO pkgChrgDAO = new PackageChargeDAO();

  /** The pkg org DAO. */
  private static GenericDAO pkgOrgDAO = new GenericDAO("pack_org_details");

  /** The pack master dao. */
  private static GenericDAO packMasterDao = new GenericDAO("packages");

  /** The pack sponsor DAO. */
  private static SponsorDAO packSponsorDAO = new SponsorDAO();

  /** The pack center DAO. */
  private static CenterDAO packCenterDAO = new CenterDAO();

  /** The center DAO. */
  private static CenterMasterDAO centerDAO = new CenterMasterDAO();

  /** The operation master dao. */
  private static OperationMasterDAO operationMasterDao = new OperationMasterDAO();

  /** The bed master dao. */
  private static BedMasterDAO bedMasterDao = new BedMasterDAO();

  /** The package details dao. */
  private static GenericDAO packageDetailsDao = new GenericDAO("package_componentdetail");

  /** The org dao. */
  private static OrgMasterDao orgDao = new OrgMasterDao();

  /** The pkg item chrg DAO. */
  private static GenericDAO pkgItemChrgDAO = new GenericDAO("package_item_charges");

  /** The Constant VALID_TO_DATE. */
  private static final String VALID_TO_DATE = "valid_to_date";

  /** The Constant VALID_FROM_DATE. */
  private static final String VALID_FROM_DATE = "valid_from_date";

  /** Rateplan Details DAO. */
  private static final GenericDAO organizationDetails = new GenericDAO("organization_details");

  /** Priority Rate Sheet Parameters View. */
  private static final GenericDAO priorityRateSheetParamatersView = new GenericDAO(
      "priority_rate_sheet_parameters_view");

  /** Tax Sub Group Repository. */
  private static TaxSubGroupRepository taxSubGroupRepository = new TaxSubGroupRepository();

  /**
   * Gets the package list screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the package list screen
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getPackageListScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    PagedList pagedList = PackageDAO.getPackages(request.getParameterMap(),
        ConversionUtils.getListingParameter(request.getParameterMap()),
        request.getParameter("org_id"));
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("bedTypes", bedMasterDao.getUnionOfAllBedTypes());
    request.setAttribute("method", "savePackageDetails");
    request.setAttribute("packages", js.serialize(packMasterDao.getColumnList("package_name")));
    request.setAttribute("centers", centerDAO.getAllCentersExceptSuper());
    request.setAttribute("sponsors", new TpaMasterDAO().listAll(null, "status", "A", "tpa_name"));
    Map<String, Object> cronJobKeys = new HashMap<String, Object>();
    cronJobKeys.put("entity", "PACKAGE");
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    cronJobKeys.put("status", status);
    MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
        ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
    List<BasicDynaBean> masterCronJobDetails =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
    request.setAttribute("masterCronJobDeatils", 
        ConversionUtils.listBeanToListMap(masterCronJobDetails));
    return mapping.findForward("packagelist");
  }

  /**
   * Adds the.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException     the SQL exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException, Exception {
    setPackageAttributes(request);
    request.setAttribute("rateplanwiseoperations",
        operationMasterDao.getOperationDeptCharges("GENERAL", "ORG0001"));
    request.setAttribute("operationIncompatibility", true);
    request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(
        new GenericDAO("item_group_type").findAllByKey("item_group_type_id", "TAX")));
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils
        .listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status", "A"))));
    // request.setAttribute("itemSubGroupListJson",
    // js.serialize(ConversionUtils.listBeanToListMap(new
    // GenericDAO("item_sub_groups").findAllByKey("status","A"))));
    List<BasicDynaBean> itemSubGroupList = taxSubGroupRepository
        .getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itemSubGroupbean = itemSubGroupListIterator.next();
      if (itemSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itemSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itemSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itemSubGroupbean);
      }
    }
    request.setAttribute("itemSubGroupListJson",
        js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
    return mapping.findForward("addshow");
  }

  /**
   * Validate date range.
   *
   * @param packageBean the package bean
   * @return true, if successful
   */
  private boolean validateDateRange(BasicDynaBean packageBean) {
    Date validToDate = (Date) packageBean.get(VALID_TO_DATE);
    Date validFromDate = (Date) packageBean.get(VALID_FROM_DATE);

    if (validToDate != null && validFromDate != null
        && validToDate.compareTo(validFromDate) == -1) {
      return false;
    }
    return true;
  }

  /**
   * Show.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException     the SQL exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException, Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    setPackageAttributes(request);

    boolean notExistOperation = true;
    Connection con = null;
    String packageId = request.getParameter("packId");

    try {
      con = DataBaseUtil.getConnection();
      PackageDAO packageDAO = new PackageDAO(con);
      BasicDynaBean packageBean = packageDAO.getPackageMasterDetails(Integer.parseInt(packageId));

      if (packageBean.get("type").equals("Package")) {
        // get the package charge if package type is of Package.
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("org_id", (String) request.getParameter("org_id"));
        keys.put("package_id", Integer.parseInt(packageId));
        keys.put("bed_type", "GENERAL");
        BasicDynaBean chgBean = pkgChrgDAO.findByKey(keys);
        BigDecimal pkgCharge = (BigDecimal) chgBean.get("charge");
        request.setAttribute("pkgCharge", pkgCharge);
      }

      request.setAttribute("packageComponentDetails",
          packageDAO.getPackDetails(Integer.parseInt(packageId), request.getParameter("org_id")));
      List<BasicDynaBean> docList = docDAO.findAllByKey("pack_id", Integer.parseInt(packageId));
      String[] docTypes = new String[docList.size()];
      int inc = 0;
      for (BasicDynaBean bean : docList) {
        docTypes[inc++] = (String) bean.get("doc_type_id");
      }
      request.setAttribute("doc_types", docTypes);
      request.setAttribute("doc_type_bean_list", docList);
      request.setAttribute("packageDetails", packageBean);
      request.setAttribute("packageDetailsJSON", js.serialize(packageBean.getMap()));

      List operations = operationMasterDao.getOperationDeptCharges("GENERAL",
          request.getParameter("org_id"));

      BasicDynaBean packageOperationDetails = operationMasterDao.getOperationChargeBean(
          (String) packageBean.get("operation_id"), "GENERAL", request.getParameter("org_id"));

      request.setAttribute("operationIncompatibility",
          packageOperationDetails != null ? (Boolean) packageOperationDetails.get("applicable")
              : true);

      Hashtable packOpeDetails = packageDAO.getPackageOperationdetails(Integer.parseInt(packageId));
      for (int i = 0; i < operations.size(); i++) {
        Hashtable operation = (Hashtable) operations.get(i);

        if (packOpeDetails != null
            && ((String) packOpeDetails.get("OP_ID")).equals((String) operation.get("OP_ID"))) {
          notExistOperation = false;
        }
      }
      if (packOpeDetails != null && notExistOperation) {
        packOpeDetails.put("APPLICABLE", "f");
        operations.add(packOpeDetails);
      }

      List<BasicDynaBean> activeInsurance = PackageDAO
          .getActiveInsuranceCategories(Integer.parseInt(packageId));
      StringBuilder activeInsuranceCategories = new StringBuilder();
      for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
        activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
        activeInsuranceCategories.append(",");
      }
      request.setAttribute("insurance_categories", activeInsuranceCategories.toString());
      request.setAttribute("rateplanwiseoperations", operations);
      request.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(
          packageDAO.getPackageItemSubGroupDetails(Integer.parseInt(packageId))));
      request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(
          new GenericDAO("item_group_type").findAllByKey("item_group_type_id", "TAX")));
      request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils
          .listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status", "A"))));
      // request.setAttribute("itemSubGroupListJson",
      // js.serialize(ConversionUtils.listBeanToListMap(new
      // GenericDAO("item_sub_groups").findAllByKey("status","A"))));
      List<BasicDynaBean> itemSubGroupList = taxSubGroupRepository
          .getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
      Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
      List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String currentDateStr = sdf.format(new java.util.Date());
      while (itemSubGroupListIterator.hasNext()) {
        BasicDynaBean itemSubGroupbean = itemSubGroupListIterator.next();
        if (itemSubGroupbean.get("validity_end") != null) {
          Date endDate = (Date) itemSubGroupbean.get("validity_end");

          try {
            if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
              validateItemSubGrouList.add(itemSubGroupbean);
            }
          } catch (ParseException ex) {
            continue;
          }
        } else {
          validateItemSubGrouList.add(itemSubGroupbean);
        }
      }
      request.setAttribute("itemSubGroupListJson",
          js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
    } finally {
      if (con != null) {
        con.close();
      }
    }

    return mapping.findForward("addshow");
  }

  /**
   * Creates the.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   * @throws Exception        the exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException, Exception {

    HttpSession session = request.getSession(false);
    Map modulesActivatedMap = ((Preferences) session.getAttribute("preferences"))
        .getModulesActivatedMap();
    String modAdvPackages = (String) modulesActivatedMap.get("mod_adv_packages");
    modAdvPackages = modAdvPackages == null ? "" : modAdvPackages;

    BasicDynaBean packageDetails = packMasterDao.getBean();
    Map<String, String[]> requestMap = request.getParameterMap();

    List<BasicDynaBean> packageComponents = new ArrayList<>();
    List errors = new ArrayList();

    ConversionUtils.copyToDynaBean(requestMap, packageDetails, errors);

    String[] activityId = requestMap.get("activity_id");
    for (int i = 0; i < activityId.length - 1; i++) {
      BasicDynaBean packageComponentBean = packageDetailsDao.getBean();
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, packageComponentBean,
          errors);
      packageComponents.add(packageComponentBean);
    }
    PackageForm packForm = (PackageForm) form;
    Connection con = null;
    String seqNo = null;
    boolean success = true;

    String hideApprovalStatus = request.getParameter("hidden_approval_status");
    String approvalStatus = (String) packageDetails.get("approval_status");

    String userName = (String) session.getAttribute("userid");
    if (!modAdvPackages.equals("Y") || (approvalStatus != null && !approvalStatus.equals("")
        && !approvalStatus.equals(hideApprovalStatus))) {
      packageDetails.set("approval_process_by", userName);
    }

    seqNo = DataBaseUtil.getValue("packages_seq", "N", "");
    packageDetails.set("package_id", Integer.parseInt(seqNo));
    Boolean clonePackage = new Boolean(request.getParameter("clone_package"));
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      /*
       * Saving package definition part
       */

      String packageApprovalRights = (String) ((Map) session.getAttribute("actionRightsMap"))
          .get("package_approval");
      int roleId = (Integer) session.getAttribute("roleId");
      // if the mod_adv_packages is not enabled, then irrespective of the package
      // type, package approval process will not be there, all packages should go with
      // 'approved' status.
      // for template packages also approval process will not be there.
      if (!modAdvPackages.equals("Y") || packageDetails.get("type").equals("T")) {
        packageDetails.set("approval_status", "A");
      } else {
        if ((roleId != 1 && roleId != 2 && packageApprovalRights.equals("N"))
            || packageDetails.get("approval_status") == null) {
          packageDetails.set("approval_status", "P");
        }
      }
      success = packMasterDao.insert(con, packageDetails);

      if (success) {
        int packId = (Integer) packageDetails.get("package_id");
        success = saveOrUpdateItemSubGroup(packId, con, request);
      }

      if (success) {
        success = saveOrUpdateInsuranceCategory(Integer.parseInt(seqNo), con, request);
      }

      if (success) {
        List<BasicDynaBean> packageComponentsToInsert = new ArrayList<>();
        for (int i = 0; i < packageComponents.size(); i++) {
          BasicDynaBean packageComponent = packageComponents.get(i);
          packageComponent.set("package_id", new BigDecimal(seqNo));
          if (((String) packageComponent.get("pack_ob_id")).equals("")) {
            continue;
          }
          // new component added
          if (((String) packageComponent.get("pack_ob_id")).startsWith("_")) {
            packageComponent.set("pack_ob_id",
                DataBaseUtil.getValue("package_contents_seq", "N", ""));
            packageComponentsToInsert.add(packageComponent);
          } else if ((requestMap.get("edited"))[i].equals("Y")) {
            // gets executed while cloning the package if the user modifies or cancels the
            // component.
            if ((requestMap.get("cancelled"))[i].equals("Y")) {
              success = packageDetailsDao.delete(con, "pack_ob_id",
                  packageComponent.get("pack_ob_id"));
            } else {
              Map<String, String> keys = new HashMap<String, String>();
              keys.put("pack_ob_id", (String) packageComponent.get("pack_ob_id"));
              success = packageDetailsDao.update(con, packageComponent.getMap(), keys) > 0;
            }
          }
        }
        packageDetailsDao.insertAll(con, packageComponentsToInsert);

        ArrayList<PackageDetails> packagesList = new ArrayList<>();
        ArrayList<PackageDetails> rateplanList = new ArrayList<>();
        ArrayList<Hashtable<String, String>> al = orgDao.getAllOrgs();

        PackageDetails packDetails = null;

        Map pkgCharges = new HashMap();
        Map pkgOrgMap = new HashMap();
        Integer packageId = null;
        if (clonePackage) {
          packageId = Integer.parseInt(request.getParameter("package_id"));

          List<String> pkgChrgColumns = new ArrayList<String>();
          pkgChrgColumns.add("charge");
          pkgChrgColumns.add("discount");
          pkgChrgColumns.add("org_id");
          pkgChrgColumns.add("bed_type");

          List existingCharges = pkgChrgDAO.listAll(con, pkgChrgColumns, "package_id", packageId,
              null);
          pkgCharges = ConversionUtils.listBeanToMapMapBean(existingCharges, "org_id", "bed_type");

          List<String> pkgOrgColumns = new ArrayList<String>();
          pkgOrgColumns.add("org_id");
          pkgOrgColumns.add("applicable");

          List pkgOrgList = pkgOrgDAO.listAll(con, pkgOrgColumns, "package_id", packageId, null);
          pkgOrgMap = ConversionUtils.listBeanToMapBean(pkgOrgList, "org_id");
          Iterator<Hashtable<String, String>> organizationsIterator = al.iterator();
          Set<String> pkgSet = pkgOrgMap.keySet();
          while (organizationsIterator.hasNext()) {
            Hashtable<String, String> ht = organizationsIterator.next();
            String orgId = ht.get("ORG_ID");
            if (!pkgSet.contains(orgId)) {
              organizationsIterator.remove();
            }
          }
        }
        Iterator<Hashtable<String, String>> orgIt = al.iterator();
        String applyChargeToAllRateSheets = request.getParameter("applyChargeToAll");
        PackageDAO packageDAO = new PackageDAO(con);
        Boolean packOrgSuccess = packageDAO.insertPackageOrgDetails(con,
            Integer.parseInt(seqNo), packageId, clonePackage);
        //Old while flow
        /*while (orgIt.hasNext()) {
          Hashtable<String, String> ht = orgIt.next();
          String orgId = ht.get("ORG_ID");
          ArrayList<Hashtable<String, String>> bedTypes = bedMasterDao.getUnionOfAllBedTypes();
          Iterator<Hashtable<String, String>> bedIt = bedTypes.iterator();
          while (bedIt.hasNext()) {
            String bedType = bedIt.next().get("BED_TYPE");

            packDetails = new PackageDetails();
            packDetails.setBedType(bedType);
            packDetails.setOrgId(orgId);
            packDetails.setPackageId(Integer.parseInt(seqNo));
            packDetails.setPackageName(packForm.getPackName());

            if (clonePackage && ((String) packageDetails.get("type")).equalsIgnoreCase("P")) {
              BasicDynaBean chargeBean = (pkgCharges.get(orgId) != null)
                  ? (BasicDynaBean) ((Map) pkgCharges.get(orgId)).get(bedType)
                  : null;
              if (applyChargeToAllRateSheets == null && chargeBean != null) {
                // copy the existing charges, discounts for cloned package.
                packDetails.setPackageCharge((BigDecimal) chargeBean.get("charge"));
                packDetails.setDiscount((BigDecimal) chargeBean.get("discount"));
              } else {
                // apply the same charge entered by user, to all the rate plans
                // Also, if the bedType is not present in the package, when added,
                // populate the charge entered for that bedType
                packDetails.setPackageCharge(packForm.getTotAmt());
                packDetails.setDiscount(BigDecimal.ZERO);
              }
            } else {
              packDetails.setPackageCharge(packForm.getTotAmt());
              packDetails.setDiscount(BigDecimal.ZERO);
            }

            packagesList.add(packDetails);
          }
          // copy the existing rate plan applicability for cloned package.
          PackageDetails packageCode = new PackageDetails();
          if (clonePackage) {
            packageCode.setRateplanApplicable(
                (Boolean) ((BasicDynaBean) pkgOrgMap.get(orgId)).get("applicable"));
          } else {
            packageCode.setRateplanApplicable(true);
          }
          packageCode.setOrgId(orgId);
          packageCode.setPackageId(Integer.parseInt(seqNo));
          rateplanList.add(packageCode);
        }*/
        if (packOrgSuccess && ((String) packageDetails.get("type")).equalsIgnoreCase("P")) {
          success &= packageDAO.insertPackageCharges(con,
              applyChargeToAllRateSheets,clonePackage,
              packageId, Integer.parseInt(seqNo), packForm.getTotAmt());
          if (success && !clonePackage) {
            packageDAO.packageChargeScheduleJob(Integer.parseInt(seqNo),
                packForm.getTotAmt());
          }
        }

        String isMultiVisitPackage = request.getParameter("multi_visit_package");
        if (null != isMultiVisitPackage && isMultiVisitPackage.equals("true")) {
          success &= insertPackageItemCharges(con, seqNo);
        }

        List<BasicDynaBean> ratePlans = orgDao.getRatePlanList();
        success &= pkgChrgDAO.updateApplicableflagForDerivedRatePlans(con, ratePlans, "packages",
            "package_id", seqNo, "pack_org_details", null);

        if (clonePackage) {
          packageId = Integer.parseInt(request.getParameter("package_id"));
          success &= packSponsorDAO.insert(con, packageId,
              (Integer) packageDetails.get("package_id"));
          success &= packCenterDAO.insert(con, packageId,
              (Integer) packageDetails.get("package_id"));
        } else {
          BasicDynaBean bean = packSponsorDAO.getBean();
          bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
          bean.set("pack_id", packageDetails.get("package_id"));
          bean.set("tpa_id", "-1");
          bean.set("status", "A");
          success &= packSponsorDAO.insert(con, bean);

          BasicDynaBean cbean = packCenterDAO.getBean();
          cbean.set("package_center_id", packCenterDAO.getNextSequence());
          cbean.set("pack_id", packageDetails.get("package_id"));
          cbean.set("center_id", -1);
          cbean.set("status", "A");
          success &= packCenterDAO.insert(con, cbean);
        }

      }

      String[] docTypes = request.getParameterValues("doc_type_id");
      int packageId = (Integer) packageDetails.get("package_id");
      if (success && docDAO.findByKey(con, "pack_id", packageId) != null) {
        success = docDAO.delete(con, "pack_id", packageId);
      }
      if (docTypes != null) {
        ArrayList docList = new ArrayList();
        for (int d = 0; d < docTypes.length; d++) {
          BasicDynaBean docBean = docDAO.getBean();
          docBean.set("pack_doc_id", docDAO.getNextSequence());
          docBean.set("pack_id", packageId);
          docBean.set("doc_type_id", docTypes[d]);
          docList.add(docBean);
        }
        if (success && !docList.isEmpty()) {
          success = docDAO.insertAll(con, docList);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    FlashScope scope = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    if (success) {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("packId", Integer.parseInt(seqNo));
      redirect.addParameter("multi_visit_package", request.getParameter("multi_visit_package"));
    } else {
      scope.error("Failed to save package details....");
    }
    redirect.addParameter("org_id", request.getParameter("org_id"));
    redirect.addParameter("multi_visit_package", request.getParameter("multi_visit_package"));
    redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
    return redirect;
  }

  /**
   * Insert package item charges.
   *
   * @param con       the con
   * @param packageId the package id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public boolean insertPackageItemCharges(Connection con, String packageId)
      throws SQLException, Exception {
    boolean success = false;
    PackageDAO packageDAO = new PackageDAO(con);
    List<BasicDynaBean> packageItemList = packageDetailsDao.findAllByKey(con, "package_id",
        Integer.parseInt(packageId));
    for (int i = 0; i < packageItemList.size(); i++) {
      BasicDynaBean bean = packageItemList.get(i);
      String packObId = (String) bean.get("pack_ob_id");
      BigDecimal charge = ((BigDecimal) bean.get("activity_charge"))
          .multiply(new BigDecimal(bean.get("activity_qty").toString()));
      pkgChrgDAO.insertPackageItemCharges(con, Integer.parseInt(packageId), packObId, charge);
      List<BasicDynaBean> ratePlanList = packageDAO.getAllRatePlans();
      for (BasicDynaBean rateBean : ratePlanList) {
        String baseRateSheetId = (String) rateBean.get("base_rate_sheet_id");
        int varianceBy = (Integer) rateBean.get("rate_variation_percent");
        int nearstRoundOfValue = (Integer) rateBean.get("round_off_amount");
        pkgChrgDAO.updatePackageItemChargesForRateplans(con, Integer.parseInt(packageId), packObId,
            baseRateSheetId, (String) rateBean.get("org_id"), new Double(varianceBy),
            new Double(nearstRoundOfValue));
      }

    }
    success = true;
    return success;
  }

  /**
   * Update.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws Exception    the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, Exception {
    BasicDynaBean packageDetails = packMasterDao.getBean();
    Map<String, String[]> requestMap = request.getParameterMap();

    List<BasicDynaBean> packageComponents = new ArrayList<BasicDynaBean>();
    List errors = new ArrayList();
    FlashScope scope = FlashScope.getScope(request);

    ConversionUtils.copyToDynaBean(requestMap, packageDetails, errors);

    String[] activityId = requestMap.get("activity_id");
    for (int i = 0; i < activityId.length - 1; i++) {
      BasicDynaBean packageComponentBean = packageDetailsDao.getBean();
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, packageComponentBean,
          errors);
      packageComponents.add(packageComponentBean);
    }
    PackageForm packForm = (PackageForm) form;
    Connection con = null;
    String seqNo = request.getParameter("package_id");
    boolean success = true;
    String isMultiVisitPackage = (String) request.getParameter("multi_visit_package");

    String approvalStatus = (String) packageDetails.get("approval_status");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userid");
    String hideApprovalStatus = request.getParameter("hidden_approval_status");
    if (approvalStatus != null && !approvalStatus.equals("")
        && !approvalStatus.equals(hideApprovalStatus)) {
      packageDetails.set("approval_process_by", userName);
    }

    packageDetails.set("package_id", Integer.parseInt(seqNo));

    Map packageDetailsMap = packageDetails.getMap();
    if (!validateDateRange(packageDetails)) {
      packageDetailsMap.remove(VALID_FROM_DATE);
      packageDetailsMap.remove(VALID_TO_DATE);
      scope.error("Not updating date. Valid To Date must be greater than Valid From Date.");
    }

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map keys = new HashMap();
      keys.put("package_id", Integer.parseInt(seqNo));
      success = packMasterDao.update(con, packageDetailsMap, keys) > 0;

      if (success) {
        success = saveOrUpdateItemSubGroup(Integer.parseInt(seqNo), con, request);
      }

      if (success) {
        success = saveOrUpdateInsuranceCategory(Integer.parseInt(seqNo), con, request);
      }

      for (int i = 0; i < packageComponents.size(); i++) {
        BasicDynaBean packageComponent = packageComponents.get(i);
        packageComponent.set("package_id", new BigDecimal(seqNo));
        if (((String) packageComponent.get("pack_ob_id")).equals("")) {
          continue;
        }
        if (((String) packageComponent.get("pack_ob_id")).startsWith("_")) { // new component added
          String packObId = DataBaseUtil.getValue("package_contents_seq", "N", "");
          packageComponent.set("pack_ob_id", packObId);
          success = packageDetailsDao.insert(con, packageComponent);
          if (null != isMultiVisitPackage && isMultiVisitPackage.equals("true")) {
            pkgChrgDAO.insertPackageItemCharges(con, Integer.parseInt(seqNo), packObId,
                new BigDecimal(0));
          }
        } else if ((requestMap.get("edited"))[i].equals("Y")) {
          if ((requestMap.get("cancelled"))[i].equals("Y")) {
            success = packageDetailsDao.delete(con, "pack_ob_id",
                packageComponent.get("pack_ob_id"));
            if (null != isMultiVisitPackage && isMultiVisitPackage.equals("true")) {

              List<BasicDynaBean> pkgCharges = pkgChrgDAO.findAllByKey(con, "package_id",
                  Integer.parseInt(seqNo));

              for (int j = 0; j < pkgCharges.size(); j++) {
                BasicDynaBean bean = pkgCharges.get(j);
                String orgId = (String) bean.get("org_id");
                String bedType = (String) bean.get("bed_type");

                Map<String, Object> ikeys = new HashMap<String, Object>();
                ikeys.put("package_id", Integer.parseInt(seqNo));
                ikeys.put("pack_ob_id", (String) packageComponent.get("pack_ob_id"));
                ikeys.put("org_id", orgId);
                ikeys.put("bed_type", bedType);
                BasicDynaBean pkgItemCharges = pkgItemChrgDAO.findByKey(con, ikeys);
                // Need null check to avoid null pointer exception Refer Bug# 48474
                if (pkgItemCharges != null) {
                  Map<String, Object> pkeys = new HashMap<String, Object>();
                  pkeys.put("package_id", Integer.parseInt(seqNo));
                  pkeys.put("org_id", orgId);
                  pkeys.put("bed_type", bedType);
                  BigDecimal itemChg = (BigDecimal) (pkgItemCharges.get("charge"));
                  BigDecimal pkgChg = (BigDecimal) (bean.get("charge"));
                  bean.set("charge", pkgChg.subtract(itemChg));
                  pkgChrgDAO.update(con, bean.getMap(), pkeys);
                }
              }

              success = pkgItemChrgDAO.delete(con, "pack_ob_id",
                  packageComponent.get("pack_ob_id"));
            }
          } else {
            Map<String, String> compkeys = new HashMap<String, String>();
            compkeys.put("pack_ob_id", (String) packageComponent.get("pack_ob_id"));
            success = packageDetailsDao.update(con, packageComponent.getMap(), compkeys) > 0;
          }
        }
      }
      String[] docTypes = request.getParameterValues("doc_type_id");
      int packageId = (Integer) packageDetails.get("package_id");
      if (success && docDAO.findByKey(con, "pack_id", packageId) != null) {
        success = docDAO.delete(con, "pack_id", packageId);
      }
      if (docTypes != null) {
        ArrayList docList = new ArrayList();
        for (int d = 0; d < docTypes.length; d++) {
          BasicDynaBean docBean = docDAO.getBean();
          docBean.set("pack_doc_id", docDAO.getNextSequence());
          docBean.set("pack_id", packageId);
          docBean.set("doc_type_id", docTypes[d]);
          docList.add(docBean);
        }
        if (success && !docList.isEmpty()) {
          success = docDAO.insertAll(con, docList);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("packId", Integer.parseInt(seqNo));
    redirect.addParameter("multi_visit_package", request.getParameter("multi_visit_package"));
    if (!success) {
      scope.error("Failed to save package details....");
    }
    redirect.addParameter("org_id", request.getParameter("org_id"));
    redirect.addParameter("multi_visit_package", request.getParameter("multi_visit_package"));
    redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
    return redirect;
  }

  /**
   * Gets the packandbeds.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the packandbeds
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public ActionForward getPackandbeds(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String orgId = request.getParameter("orgId");
    response.setContentType("text/xml");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(
        js.serialize(ConversionUtils.copyListDynaBeansToMap(bo.getPackageNamesAndBeds(orgId))));
    response.flushBuffer();
    return null;
  }

  /**
   * Sets the package attributes.
   *
   * @param request the new package attributes
   * @throws Exception the exception
   */
  public void setPackageAttributes(HttpServletRequest request) throws Exception {
    JSONSerializer js = new JSONSerializer().exclude("class");
    List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
    request.setAttribute("doctorsList",
        js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));
    request.setAttribute("packages",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(PackageDAO.getAllPackageDetails())));
    BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
    request.setAttribute("masterTimeStamp", mst.get("master_count"));
    List chargeHeadList = new BillBO().getChargeHeadConstNames();
    request.setAttribute("chargeHeadsJSON", js.serialize(chargeHeadList));
    request.setAttribute("orgDetails",
        organizationDetails.findByKey("org_id", request.getParameter("org_id")));

    request.setAttribute("serviceGroups",
        new GenericDAO("service_groups").listAll(null, "status", "A", "service_group_name"));
    request.setAttribute("serviceGroupsJSON",
        new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("service_groups").listAll(null, "status", "A", null))));
    request.setAttribute("servicesSubGroupsJSON",
        new JSONSerializer()
            .serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("service_sub_groups")
                .listAll(null, "status", "A", "service_sub_group_name"))));
    request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    request.setAttribute("anaeTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        new GenericDAO("anesthesia_type_master").listAll(null, "status", "A", null))));

    request.setAttribute("allDoctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new ConsultationTypesDAO().listAll())));
  }

  /** The Constant PACKAGE_CHARGES. */
  private static final String PACKAGE_CHARGES = "SELECT charge FROM  package_charges WHERE "
      + "package_id=? AND bed_type=? AND org_id=?  ";

  /** The Constant DISCOUNT. */
  private static final String DISCOUNT = "SELECT discount FROM  package_charges WHERE "
      + "package_id=? AND bed_type=? AND org_id=?  ";

  /** The Constant PACAKGE_ITEM_SERVICE_CHARGES. */
  private static final String PACAKGE_ITEM_SERVICE_CHARGES = "SELECT unit_charge"
      + " FROM  service_master_charges WHERE "
      + "service_id=? AND bed_type=? AND org_id=?  ";

  /** The Constant PACAKGE_ITEM_DIAG_CHARGES. */
  private static final String PACAKGE_ITEM_DIAG_CHARGES = "SELECT charge FROM diagnostic_charges dc"
      + " JOIN diagnostics d USING(test_id) "
      + " JOIN diagnostics_departments dd ON(dd.ddept_id=d.ddept_id) "
      + "WHERE test_id = ? AND bed_type=? AND org_name=?  AND category = ?";

  /** The Constant PACAKGE_ITEM_CONSULTATION_CHARGES. */
  private static final String PACAKGE_ITEM_CONSULTATION_CHARGES = "SELECT charge"
      + " FROM  consultation_charges WHERE "
      + "consultation_type_id = ? AND bed_type=? AND org_id=?  ";

  /** The Constant PACAKGE_ITEM_OTHER_CHARGES. */
  private static final String PACAKGE_ITEM_OTHER_CHARGES = "SELECT charge"
      + " FROM  common_charges_master WHERE "
      + " charge_name = ?";

  /** The Constant PACAKGE_ITEM_CHARGES. */
  private static final String PACAKGE_ITEM_CHARGES = "SELECT * FROM  package_item_charges WHERE "
      + "package_id = ? AND org_id = ? order by pack_ob_id,bed_type";

  /**
   * Gets the edits the package charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the edits the package charges
   * @throws Exception the exception
   */
  public ActionForward getEditPackageCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String packageId = request.getParameter("packId");
    String orgId = request.getParameter("org_id");
    if (orgId.isEmpty()) {
      orgId = "ORG0001";
    }
    Connection con = null;
    PreparedStatement ps = null;
    PreparedStatement dps = null;
    PreparedStatement picps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PACKAGE_CHARGES);
      dps = con.prepareStatement(DISCOUNT);
      PackageDAO packageDAO = new PackageDAO(con);
      request.setAttribute("packageDetails",
          packageDAO.getPackageMasterDetails(Integer.parseInt(packageId)));
      request.setAttribute("packageOrgDetails",
          packageDAO.getPackageOrgDetails(Integer.parseInt(packageId), orgId));
      Map filterMap = new HashMap();
      filterMap.put("package_id", Integer.parseInt(packageId));
      List<BasicDynaBean> packageItemDetails = packageDetailsDao.listAll(null, filterMap,
          "pack_ob_id");

      BasicDynaBean orgDetails = orgDao.getOrgdetailsDynaBean(orgId);
      List orgList = orgDao.getAllOrgIdNames();

      LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
      LinkedHashMap<String, ArrayList<Integer>> packageItemChargeMap = new LinkedHashMap<>();
      LinkedHashMap<String, BigDecimal> packageChargeMap = new LinkedHashMap<>();
      LinkedHashMap<String, String> packageBedTypesMap = new LinkedHashMap<>();
      ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();

      ArrayList chargebeds = new ArrayList<String>();
      ArrayList packages = new ArrayList<String>();
      ArrayList discounts = new ArrayList<String>();

      Iterator<String> beds = bedTypes.iterator();
      while (beds.hasNext()) {
        String bed = beds.next();
        ps.setInt(1, Integer.parseInt(packageId));
        ps.setString(2, bed);
        ps.setString(3, orgId);

        dps.setInt(1, Integer.parseInt(packageId));
        dps.setString(2, bed);
        dps.setString(3, orgId);

        chargebeds.add(bed);
        String packCharge = DataBaseUtil.getStringValueFromDb(ps);
        packageChargeMap.put(bed, (packCharge == null || packCharge.isEmpty()) ? BigDecimal.ZERO
            : new BigDecimal(packCharge));
        packages.add(packCharge);
        discounts.add(DataBaseUtil.getStringValueFromDb(dps));
      }
      map.put("CHARGES", chargebeds);
      map.put("PACKAGECHARGE", packages);
      map.put("DISCOUNT", discounts);
      BasicDynaBean packBean = packMasterDao.findByKey("package_id", Integer.parseInt(packageId));
      boolean isMultiVisitPackage = (Boolean) packBean.get("multi_visit_package");
      if (isMultiVisitPackage) {
        packageItemChargeMap = getPackageItemCharges(request, packageId, bedTypes,
            packageItemDetails, picps, con, orgId, packageChargeMap);
        for (int i = 0; i < bedTypes.size(); i++) {
          packageBedTypesMap.put(new Integer(i).toString(), bedTypes.get(i));
        }
      }
      request.setAttribute("bedTypesLength", bedTypes.size());
      request.setAttribute("bedTypesLengthJson",
          new JSONSerializer().exclude("class").serialize(bedTypes.size()));
      request.setAttribute("packageItemsLength",
          new JSONSerializer().exclude("class").serialize(packageItemDetails.size()));
      request.setAttribute("packageCharges", map);
      request.setAttribute("packageItemCostMap", packageItemChargeMap);
      request.setAttribute("isMultiVisitPack", isMultiVisitPackage);
      request.setAttribute("isMultiVisitPackageJson",
          new JSONSerializer().serialize(isMultiVisitPackage));
      request.setAttribute("packageBedTypesMap", packageBedTypesMap);
      // return mapping.findForward("geteditpackagecharges");
    } finally {
      if (con != null) {
        con.close();
      }
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    List<BasicDynaBean> derivedRatePlanDetails = pkgChrgDAO.getDerivedRatePlanDetails(orgId,
        packageId);
    if (derivedRatePlanDetails.size() < 0) {
      request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
    } else {
      request.setAttribute("derivedRatePlanDetails",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));
    }
    Map<String, Object> searchMap = new HashMap<String, Object>();
    searchMap.put("entity", "PACKAGE");
    searchMap.put("entity_id", packageId);
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    searchMap.put("status", status);
    List<String> bedTypes = bedMasterDao.getUnionOfBedTypes();
    MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
        ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
    List<BasicDynaBean> masterJobData =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(searchMap);
    request.setAttribute("masterJobCount", masterJobData.size());

    request.setAttribute("bedTypes", bedTypes);

    return mapping.findForward("geteditpackagecharges");

  }

  /**
   * Edits the package charges.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward editPackageCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, String[]> requestMap = request.getParameterMap();
    String[] bedTypes = requestMap.get("bed_type");
    String[] packcharges = requestMap.get("charge");
    String[] packDiscout = requestMap.get("discount");
    String[] derivedRateplanIds = request.getParameterValues("ratePlanId");
    String[] ratePlanApplicable = request.getParameterValues("applicable");

    Double[] charges = new Double[packcharges.length];
    Double[] discount = new Double[packDiscout.length];
    for (int i = 0; i < packcharges.length; i++) {
      charges[i] = new Double(packcharges[i]);
      discount[i] = new Double(packDiscout[i]);
    }

    ArrayList<BasicDynaBean> packageList = new ArrayList<BasicDynaBean>();
    Connection con = null;
    boolean status = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean packageOrgDetBean = pkgOrgDAO.getBean();
      ConversionUtils.copyToDynaBean(request.getParameterMap(), packageOrgDetBean);// copy to bean
      packageOrgDetBean.set("applicable", true);
      String packageId = request.getParameter("package_id");
      String orgId = (String) packageOrgDetBean.get("org_id");
      Map filterMap = new HashMap();
      filterMap.put("package_id", (Integer) packageOrgDetBean.get("package_id"));
      List<BasicDynaBean> packageItemDetails = packageDetailsDao.listAll(null, filterMap,
          "pack_ob_id");

      Map keys = new HashMap<String, Object>();
      keys.put("package_id", packageOrgDetBean.get("package_id"));
      keys.put("org_id", packageOrgDetBean.get("org_id"));

      status = pkgOrgDAO.update(con, packageOrgDetBean.getMap(), keys) > 0;

      BasicDynaBean packageChargeBean = null;
      for (int i = 0; i < bedTypes.length; i++) {

        packageChargeBean = pkgChrgDAO.getBean();
        ConversionUtils.copyToDynaBean(request.getParameterMap(), packageChargeBean);
        ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, packageChargeBean);
        keys.put("bed_type", bedTypes[i]);

        // PackageDetails package_org_details = new PackageDetails();
        // package_org_details.setPackageId(
        // Integer.parseInt(((String[])requestMap.get("packId"))[0]));
        // package_org_details.setBedType(bedTypes[i]);
        // package_org_details.setOrgId(org_id[0]);
        // package_org_details.setPackageCharge(new BigDecimal(charge[i]));
        // package_org_details.setRateplanApplicable(rate_plan);
        // package_org_details.setDiscount(new BigDecimal(discount[i]));
        packageList.add(packageChargeBean);

        status &= pkgChrgDAO.update(con, packageChargeBean.getMap(), keys) > 0;
      }

      if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {
        status = pkgChrgDAO.updateOrgForDerivedRatePlans(con, derivedRateplanIds,
            ratePlanApplicable, packageId);
        status = pkgChrgDAO.updateChargesForDerivedRatePlans(con, orgId, derivedRateplanIds,
            bedTypes, charges, packageId, discount, ratePlanApplicable);
      }

      RateMasterDao rdao = new RateMasterDao();
      List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
      if (null != allDerivedRatePlanIds) {
        status = pkgChrgDAO.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds,
            "packages", "package_id", packageId, "pack_org_details", orgId);
      }
      BasicDynaBean packBean = packMasterDao.findByKey("package_id",
          (Integer) packageOrgDetBean.get("package_id"));
      boolean isMultiVisitPackage = (Boolean) packBean.get("multi_visit_package");
      if (isMultiVisitPackage) {
        String[] packageObId = request.getParameterValues("pack_ob_id");
        String[] bedTypesArr = request.getParameterValues("package_bed_type");
        String[] itemCharges = request.getParameterValues("pack_item_charge");
        Map identifiers = new HashMap();
        identifiers.put("package_id", (Integer) packageOrgDetBean.get("package_id"));
        for (int j = 0; j < bedTypesArr.length; j++) {
          if (bedTypesArr[j] != null && !bedTypesArr[j].equals("")) {
            String packObId = packageObId[j];
            identifiers.put("pack_ob_id", packObId);
            identifiers.put("org_id", (String) packageOrgDetBean.get("org_id"));
            identifiers.put("bed_type", bedTypesArr[j]);
            BasicDynaBean packItemChargeBean = null;

            packItemChargeBean = pkgItemChrgDAO.getBean();
            Map keysMap = new HashMap();
            keysMap.put("package_id", (Integer) packageOrgDetBean.get("package_id"));
            keysMap.put("pack_ob_id", packObId);
            keysMap.put("org_id", (String) packageOrgDetBean.get("org_id"));
            keysMap.put("bed_type", bedTypesArr[j]);
            packItemChargeBean.set("charge",
                itemCharges[j].equals("") ? BigDecimal.ZERO : new BigDecimal(itemCharges[j]));
            status = pkgItemChrgDAO.update(con, packItemChargeBean.getMap(), keysMap) >= 0;
          }
        }

        Double[] itemChgs = new Double[itemCharges.length];
        for (int j = 0; j < itemCharges.length; j++) {
          itemChgs[j] = new Double(itemCharges[j]);
        }

        if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {
          status = pkgChrgDAO.updatePkgItemChargesForDerivedRatePlans(con, orgId,
              derivedRateplanIds, bedTypesArr, itemChgs, packageId, ratePlanApplicable,
              packageObId);
        }
      }

      FlashScope scope = FlashScope.getScope(request);
      if (!status) {
        scope.error("Failed to updated package charges....");
      }

      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showPackageCharges"));
      redirect.addParameter("_newEdit", "new");
      redirect.addParameter("method", "savePackageDetails");
      redirect.addParameter("packId", request.getParameter("packId"));
      redirect.addParameter("org_id", request.getParameter("org_id"));
      redirect.addParameter("multi_visit_package", request.getParameter("multi_visit_package"));
      redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
      return redirect;

    } finally {
      DataBaseUtil.commitClose(con, status);
    }
  }

  /**
   * writes operation charge surg_asstance_charge+surgeon_charge+anesthetist_charge after respective
   * discounts.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the package operation charge
   * @throws Exception the exception
   */
  public ActionForward getPackageOperationCharge(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String operationId = request.getParameter("id");
    String orgId = request.getParameter("orgId");
    BasicDynaBean operationBean = operationMasterDao.getOperationChargeBean(operationId, "GENERAL",
        orgId);

    float surgAsstChgAfterDis = (((BigDecimal) operationBean.get("surg_asstance_charge"))
        .subtract((BigDecimal) operationBean.get("surg_asst_discount"))).floatValue();
    float surgeonChgAfterDis = ((BigDecimal) operationBean.get("surgeon_charge"))
        .subtract((BigDecimal) operationBean.get("surg_discount")).floatValue();
    float aneChgAfterDis = ((BigDecimal) operationBean.get("anesthetist_charge"))
        .subtract((BigDecimal) operationBean.get("surg_discount")).floatValue();
    float operationCharge = surgAsstChgAfterDis + surgeonChgAfterDis + aneChgAfterDis;

    response.setContentType("text/plain");
    response.setHeader("Cache_Control", "no_cache");
    response.getWriter().write(new JSONSerializer().exclude("class").serialize(operationCharge));
    return null;

  }

  /**
   * Gets the package item charges.
   *
   * @param request            the request
   * @param packageId          the package id
   * @param bedTypes           the bed types
   * @param packageItemDetails the package item details
   * @param picps              the picps
   * @param con                the con
   * @param orgId              the org id
   * @param packageChargeMap   the package charge map
   * @return the package item charges
   * @throws Exception the exception
   */
  public LinkedHashMap<String, ArrayList<Integer>> getPackageItemCharges(HttpServletRequest request,
      String packageId, ArrayList<String> bedTypes, List<BasicDynaBean> packageItemDetails,
      PreparedStatement picps, Connection con, String orgId,
      LinkedHashMap<String, BigDecimal> packageChargeMap) throws Exception {
    LinkedHashMap map = new LinkedHashMap();
    LinkedHashMap<String, ArrayList<Integer>> packageItemChargeMap = new LinkedHashMap<>();
    LinkedHashMap<String, Integer> packageItemQtyMap = new LinkedHashMap<>();
    Map<String, Map<String, BigDecimal>> packageBedTypeItemChargeMap = new LinkedHashMap<>();
    LinkedHashMap<String, Object> descMap = new LinkedHashMap<>();
    Map<String, Object> identifiers = new HashMap<>();
    identifiers.put("package_id", Integer.parseInt(packageId));
    identifiers.put("org_id", orgId);

    picps = con.prepareStatement(PACAKGE_ITEM_CHARGES);
    picps.setInt(1, Integer.parseInt(packageId));
    picps.setString(2, orgId);
    List<BasicDynaBean> packageItemCharges = DataBaseUtil.queryToDynaList(picps);
    for (int i = 0; i < packageItemDetails.size(); i++) {
      BasicDynaBean packItemBean = packageItemDetails.get(i);
      String itemId = (String) packItemBean.get("activity_id");
      Integer consultationTypeId = (Integer) packItemBean.get("consultation_type_id");
      String itemType = (String) packItemBean.get("activity_type");
      itemId = itemType.equals("Doctor") ? packItemBean.get("consultation_type_id").toString()
          : itemId;
      String consultationType = getConsultationType(itemType, consultationTypeId);
      String packObId = (String) packItemBean.get("pack_ob_id");
      String itemDescription = (String) packItemBean.get("activity_description")
          + ((null != consultationType) ? (" (" + consultationType + ") ") : "");

      ArrayList<Integer> packItemCharges = new ArrayList<Integer>();

      for (int j = 0; j < packageItemCharges.size(); j++) {
        String packageObId = (String) packageItemCharges.get(j).get("pack_ob_id");
        if (packObId.equals(packageObId)) {
          Integer itemCharge = ((BigDecimal) packageItemCharges.get(j).get("charge")).intValue();
          packItemCharges.add(itemCharge);
        }
      }
      descMap.put(packObId, itemDescription);
      packageItemChargeMap.put(packObId, packItemCharges);
      Integer itemQty = (Integer) packItemBean.get("activity_qty");
      packageItemQtyMap.put(packObId, itemQty);

      request.setAttribute("pkgItemCharges",
          ConversionUtils.listBeanToMapMapBean(packageItemCharges, "bed_type", "pack_ob_id"));
      map.put("itemdesc", descMap);
      map.put("itemcharges", packageItemChargeMap);
      map.put("itemQty", packageItemQtyMap);
      packageBedTypeItemChargeMap = getPackageBedTypeBaseItemCharge(orgId, packageItemDetails,
          bedTypes, con);
      map.put("packageItemBaseCharge", packageBedTypeItemChargeMap);
    }
    return map;
  }

  /**
   * Gets the consultation type.
   *
   * @param itemType           the item type
   * @param consultationTypeId the consultation type id
   * @return the consultation type
   * @throws SQLException the SQL exception
   */
  private String getConsultationType(String itemType, Integer consultationTypeId)
      throws SQLException {
    if (null == itemType || null == consultationTypeId) {
      return null;
    }
    if (itemType.equalsIgnoreCase("Doctor")) {
      GenericDAO cdao = new GenericDAO("consultation_types");
      BasicDynaBean cbean = cdao.findByKey("consultation_type_id", consultationTypeId);
      return (String) cbean.get("consultation_type");
    }
    return null;
  }

  /**
   * Gets the package bed type base item charge.
   *
   * @param orgId              the org id
   * @param packageItemDetails the package item details
   * @param bedTypes           the bed types
   * @param con                the con
   * @return the package bed type base item charge
   * @throws Exception the exception
   */
  public Map<String, Map<String, BigDecimal>> getPackageBedTypeBaseItemCharge(String orgId,
      List<BasicDynaBean> packageItemDetails, ArrayList<String> bedTypes, Connection con)
      throws Exception {
    Map<String, Map<String, BigDecimal>> packageBedTypeItemChargeMap = new LinkedHashMap<>();
    Iterator<String> beds = bedTypes.iterator();
    PreparedStatement picps = null;
    for (int i = 0; i < packageItemDetails.size(); i++) {
      beds = bedTypes.iterator();
      BasicDynaBean packItemBean = packageItemDetails.get(i);
      String itemId = (String) packItemBean.get("activity_id");
      String itemType = (String) packItemBean.get("activity_type");
      itemId = itemType.equals("Doctor") ? packItemBean.get("consultation_type_id").toString()
          : itemId;

      String packObId = (String) packItemBean.get("pack_ob_id");
      ArrayList<Integer> packageItemCharges = new ArrayList<>();
      Map<String, BigDecimal> itemChargesMap = new LinkedHashMap<>();
      while (beds.hasNext()) {
        String bed = beds.next();
        picps = getItemCharge(itemType, picps, con, itemId, bed, orgId);
        BigDecimal itemCharge = DataBaseUtil.getBigDecimalValueFromDb(picps);
        itemChargesMap.put(bed, itemCharge);
      }
      packageBedTypeItemChargeMap.put(packObId, itemChargesMap);
    }

    return packageBedTypeItemChargeMap;
  }

  /**
   * Gets the item charge.
   *
   * @param itemType the item type
   * @param picps    the picps
   * @param con      the con
   * @param itemId   the item id
   * @param bed      the bed
   * @param orgId    the org id
   * @return the item charge
   * @throws Exception the exception
   */
  public PreparedStatement getItemCharge(String itemType, PreparedStatement picps, Connection con,
      String itemId, String bed, String orgId) throws Exception {
    if (itemType.equals("Service")) {
      picps = con.prepareStatement(PACAKGE_ITEM_SERVICE_CHARGES);
    } else if (itemType.equals("Doctor")) {
      picps = con.prepareStatement(PACAKGE_ITEM_CONSULTATION_CHARGES);
    } else if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
      picps = con.prepareStatement(PACAKGE_ITEM_DIAG_CHARGES);
    } else if (itemType.equals("Other Charge")) {
      picps = con.prepareStatement(PACAKGE_ITEM_OTHER_CHARGES);
    }

    if (itemType.equals("Other Charge")) {
      picps.setString(1, itemId);
    } else if (itemType.equals("Doctor")) {
      picps.setInt(1, Integer.parseInt(itemId));
    } else {
      picps.setString(1, itemId);
    }
    if (!itemType.equals("Other Charge")) {
      picps.setString(2, bed);
      picps.setString(3, orgId);
    }
    if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
      picps.setString(4, itemType.equals("Laboratory") ? "DEP_LAB" : "DEP_RAD");
    }

    return picps;
  }

  /**
   * Update packge centers and sponsors.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward updatePackgeCentersAndSponsors(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    String allPackages = request.getParameter("allPackages");
    String allCenters = request.getParameter("all_centers_checkbox");
    String allSponsors = request.getParameter("all_sponsors_checkbox");
    String error = null;
    Connection con = null;
    List<String> packages = null;
    List<String> columns = new ArrayList<String>();
    columns.add("pack_id");
    if (!allPackages.equals("yes")) {
      packages = ConversionUtils.getParamAsList(request.getParameterMap(), "selectPackage");
    } else {
      packages = PackageDAO.getPackageIds();
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("groupUpdateRedirect"));

    List<String> centers = ConversionUtils.getParamAsList(request.getParameterMap(),
        "selectCenter");
    List<String> sponsors = ConversionUtils.getParamAsList(request.getParameterMap(),
        "selectSponsor");
    BasicDynaBean bean = null;
    try {
      txn: {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        for (String packId : packages) {
          bean = packCenterDAO.getBean();
          if (allCenters != null) {
            if (!packCenterDAO.delete(con, Integer.parseInt(packId))) {
              error = "Failed to delete package applicability of few centers..";
              break txn;
            }

            if (packCenterDAO.findByKey(con, "pack_id", Integer.parseInt(packId)) == null) {
              bean.set("package_center_id", packCenterDAO.getNextSequence());
              bean.set("pack_id", Integer.parseInt(packId));
              bean.set("center_id", -1);
              bean.set("status", "A");
              if (!packCenterDAO.insert(con, bean)) {
                error = "Failed to update package applicability for all centers..";
                break txn;
              }
            }
          } else {
            if (centers != null) {
              if (!packCenterDAO.delete(con, "pack_id", Integer.parseInt(packId))) {
                error = "Failed to delete package applicability of few centers..";
                break txn;
              }
              for (String center : centers) {
                bean.set("package_center_id", packCenterDAO.getNextSequence());
                bean.set("pack_id", Integer.parseInt(packId));
                bean.set("center_id", Integer.parseInt(center));
                bean.set("status", "A");
                if (!packCenterDAO.insert(con, bean)) {
                  error = "Failed to update package applicability for all centers..";
                  break txn;
                }
              }
            }
          }
          bean = packSponsorDAO.getBean();
          if (allSponsors != null) {

            if (!packSponsorDAO.delete(con, Integer.parseInt(packId))) {
              error = "Failed to delete package applicability of few sponsors..";
              break txn;
            }

            if (packSponsorDAO.findByKey(con, "pack_id", Integer.parseInt(packId)) == null) {
              bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
              bean.set("pack_id", Integer.parseInt(packId));
              bean.set("tpa_id", "-1");
              bean.set("status", "A");
              if (!packSponsorDAO.insert(con, bean)) {
                error = "Failed to update package applicability for all sponsors..";
                break txn;
              }
            }
          } else {
            if (sponsors != null) {
              if (!packSponsorDAO.delete(con, "pack_id", Integer.parseInt(packId))) {
                error = "Failed to delete package applicability of few sponsors..";
                break txn;
              }
              for (String sponsor : sponsors) {
                bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
                bean.set("pack_id", Integer.parseInt(packId));
                bean.set("tpa_id", sponsor);
                bean.set("package_active", "A");
                if (!packSponsorDAO.insert(con, bean)) {
                  error = "Failed to update package applicability for all sponsors..";
                  break txn;
                }
              }
            }
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, error == null);
    }
    redirect.addParameter("status", "A");
    redirect.addParameter("approval_status", "A");
    redirect.addParameter("sortReverse", false);
    return redirect;
  }

  /**
   * Save or update item sub group.
   *
   * @param packId  the pack id
   * @param con     the con
   * @param request the request
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private boolean saveOrUpdateItemSubGroup(int packId, Connection con, HttpServletRequest request)
      throws SQLException, IOException {
    Map params = request.getParameterMap();
    List errors = new ArrayList();

    boolean flag = true;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");
    String[] addedrow = request.getParameterValues("addedrow");

    if (errors.isEmpty()) {
      if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
        GenericDAO itemsubgroupdao = new GenericDAO("package_item_sub_groups");
        List records = itemsubgroupdao.findAllByKey("package_id", packId);
        if (records.size() > 0) {
          flag = itemsubgroupdao.delete(con, "package_id", packId);
        }
        List<BasicDynaBean> itemSubgroupBeans = new ArrayList<>();
        for (int i = 0; i < itemSubgroupId.length; i++) {
          if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
            if (delete[i].equalsIgnoreCase("false")) {
              BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
              ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
              itemsubgroupbean.set("package_id", packId);
              itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
              itemSubgroupBeans.add(itemsubgroupbean);
            }
          }
        }
        flag = itemsubgroupdao.insertAll(con, itemSubgroupBeans);
      }
    }
    return flag;

  }

  /**
   * Save or update insurance category.
   *
   * @param packageId the package id
   * @param con       the con
   * @param request   the request
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private boolean saveOrUpdateInsuranceCategory(int packageId, Connection con,
      HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
    if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
      GenericDAO insuranceCategoryDao = new GenericDAO("packages_insurance_category_mapping");
      List<BasicDynaBean> records = insuranceCategoryDao.findAllByKey("package_id", packageId);
      if (records != null && records.size() > 0) {
        flag = insuranceCategoryDao.delete(con, "package_id", packageId);
      }
      List<BasicDynaBean> insuranceCategoryBeans = new ArrayList<>();
      for (String insuranceCategory : insuranceCategories) {
        BasicDynaBean insuranceCategoryBean = insuranceCategoryDao.getBean();
        insuranceCategoryBean.set("package_id", packageId);
        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
        insuranceCategoryBeans.add(insuranceCategoryBean);
      }
      flag = insuranceCategoryDao.insertAll(con, insuranceCategoryBeans);
    }
    return flag;
  }

}
