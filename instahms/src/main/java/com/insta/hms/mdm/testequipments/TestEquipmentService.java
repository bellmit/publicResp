package com.insta.hms.mdm.testequipments;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.diagtestresults.DiagTestResultService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestEquipmentService.
 */
@Service
public class TestEquipmentService extends MasterDetailsService {

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The test equipment repository. */
  @LazyAutowired
  private TestEquipmentRepository testEquipmentRepository;

  /** The diag test result service. */
  @LazyAutowired
  private DiagTestResultService diagTestResultService;

  /** The test equipment results repository. */
  @LazyAutowired
  private TestEquipmentResultsRepository testEquipmentResultsRepository;

  /** The diag department service. */
  @LazyAutowired
  private DiagDepartmentService diagDepartmentService;

  
  /**
   * Instantiates a new test equipment service.
   *
   * @param ter the ter
   * @param tev the tev
   * @param ted the ted
   */
  public TestEquipmentService(TestEquipmentRepository ter, TestEquipmentValidator tev,
      TestEquipmentResultsRepository ted) {
    super(ter, tev, ted);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    return ((TestEquipmentRepository) getRepository()).getSearchEquipmentList(params,
        listingParams);
  }

  /**
   * Gets the list page lookup.
   *
   * @param params
   *          the params
   * @return the list page lookup
   */
  public Map<String, List<BasicDynaBean>> getListPageLookup(Map params) {
    Map<String, List<BasicDynaBean>> listReference = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> genPrefs = new ArrayList<BasicDynaBean>();
    genPrefs.add(genericPreferencesService.getPreferences());
    listReference.put("genPrefs", genPrefs);
    listReference.put("centers", centerService.getAllCentersExceptSuper());
    List<BasicDynaBean> diagDepartmentList = diagDepartmentService.getActiveDiagDepartments();
    listReference.put("diagdepts", diagDepartmentList);
    return listReference;
  }

  /**
   * Gets the adds the page data.
   *
   * @param params
   *          the params
   * @return the adds the page data
   */
  public Map<String, List<BasicDynaBean>> getAddPageData(Map params) {

    Map<String, List<BasicDynaBean>> listReference = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> genPrefs = new ArrayList<BasicDynaBean>();
    genPrefs.add(genericPreferencesService.getPreferences());
    listReference.put("genPrefs", genPrefs);
    listReference.put("equipmentNames", testEquipmentRepository.lookup(false));
    listReference.put("result", diagTestResultService.getResultsForEquipment());
    listReference.put("centers", centerService.getAllCentersExceptSuper());
    List<BasicDynaBean> diagDepartmentList = diagDepartmentService.getActiveDiagDepartments();
    listReference.put("diagdepts", diagDepartmentList);
    return listReference;
  }

  /**
   * Gets the next test id.
   *
   * @return the next test id
   */
  public Integer getNextTestId() {

    return (Integer) testEquipmentRepository.getNextId();
  }

  /**
   * Insert test equipment details.
   *
   * @param parameters
   *          the parameters
   * @param msg
   *          the msg
   * @return the int
   * @throws SQLException
   *           the SQL exception
   */
  public int insertTestEquipmentDetails(Map<String, String[]> parameters, StringBuilder msg)
      throws SQLException {

    boolean success = true;
    List errors = new ArrayList();
    int equipmentId = getNextTestId();
    BasicDynaBean testeEuipmentBean = testEquipmentRepository.getBean();
    ConversionUtils.copyToDynaBean(parameters, testeEuipmentBean, errors);
    List<BasicDynaBean> newResultsList = getNewResultBeanList(parameters);
    if (parameters.get("schedule") == null || parameters.get("schedule").equals("")) {
      testeEuipmentBean.set("schedule", false);
    }
    if (errors.isEmpty()) {
      boolean exists = testEquipmentRepository.exist("equipment_name",
          (String) testeEuipmentBean.get("equipment_name"));
      if (exists) {
        msg = msg.append("Equipment name already exists.....");
        return 0;
      } else {
        testeEuipmentBean.set("eq_id", equipmentId);
        success = testEquipmentRepository.insertTestEquipment(testeEuipmentBean);

        for (BasicDynaBean resultBean : newResultsList) {
          resultBean.set("equipment_id", equipmentId);
          testEquipmentResultsRepository.insert(resultBean);// insert into equipment_test_result
        }
        if (success) {
          msg = msg.append("Test Equipment Master details inserted successfully...");
        } else {
          msg = msg.append("Fail to add Test Equipment master....");
          return 0;
        }
      }
    } else {
      msg = msg.append("Incorrectly formatted values supplied..");
      return 0;
    }
    return equipmentId;
  }

  /**
   * Gets the new result bean list.
   *
   * @param params
   *          the params
   * @return the new result bean list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getNewResultBeanList(Map params) throws SQLException {

    String[] equipmentId = (String[]) params.get("equipment_id");
    String[] newResult = (String[]) params.get("new");
    String[] deleted = (String[]) params.get("deleted");
    List errorList = new ArrayList();
    List<BasicDynaBean> resultBeanList = new ArrayList<BasicDynaBean>();
    BasicDynaBean resultBean = null;

    if (equipmentId != null) {
      for (int i = 0; i < equipmentId.length; i++) {
        if (newResult[i].equals("Y") && deleted[i].equals("")) {
          resultBean = testEquipmentResultsRepository.getBean();
          ConversionUtils.copyIndexToDynaBean(params, i, resultBean, errorList);
          if (errorList.size() > 0) {
            break;
          }
          resultBeanList.add(resultBean);
        }
      }
    }

    return resultBeanList;
  }

  /**
   * Gets the list edit page data.
   *
   * @param eqId
   *          the eq id
   * @return the list edit page data
   * @throws SQLException
   *           the SQL exception
   */
  public Map getListEditPageData(String eqId) throws SQLException {

    Map detailsMap = new HashMap();
    BasicDynaBean bean = testEquipmentRepository.getTestEquipmentBean(eqId);
    detailsMap.put("bean", bean);
    List<BasicDynaBean> testEquipmentResults = testEquipmentResultsRepository
        .getEquipementResults(eqId);
    detailsMap.put("equipemtResults", testEquipmentResults);
    detailsMap.put("genPrefs", genericPreferencesService.getPreferences());
    detailsMap.put("equipmentNames", testEquipmentRepository.lookup(true));
    detailsMap.put("result",
        ConversionUtils.listBeanToListMap(diagTestResultService.getResultsForEquipment()));
    detailsMap.put("centers", centerService.getAllCentersExceptSuper());
    List<BasicDynaBean> diagDepartmentList = diagDepartmentService.getActiveDiagDepartments();
    detailsMap.put("diagdepts", ConversionUtils.listBeanToListMap(diagDepartmentList));
    return detailsMap;
  }

  /**
   * Update test equipment details.
   *
   * @param parameters
   *          the parameters
   * @param eqId
   *          the eq id
   * @param msg
   *          the msg
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateTestEquipmentDetails(Map<String, String[]> parameters, String eqId,
      StringBuilder msg) throws SQLException {
    boolean success = true;
    List errors = new ArrayList();
    BasicDynaBean testeEuipmentBean = testEquipmentRepository.getBean();
    ConversionUtils.copyToDynaBean(parameters, testeEuipmentBean, errors);
    List<BasicDynaBean> newResultsList = getNewResultBeanList(parameters);
    List<BasicDynaBean> deletedResults = getDeleteResults(parameters);
    if (parameters.get("schedule") == null || parameters.get("schedule").equals("")) {
      testeEuipmentBean.set("schedule", false);
    }
    Map<String, Integer> keys = new HashMap<String, Integer>();
    keys.put("eq_id", Integer.parseInt(eqId));
    if (errors.isEmpty()) {
      success = testEquipmentRepository.updateTestEquipment(testeEuipmentBean, keys);
      for (BasicDynaBean resultBean : newResultsList) {
        if (testEquipmentResultsRepository.findByKey((Integer) resultBean.get("equipment_id"),
            (Integer) resultBean.get("resultlabel_id"))) {
          msg = msg.append("Duplicate Result Label Not Allowed.");
          return false;
        }
        testEquipmentResultsRepository.insert(resultBean);
      }
      for (BasicDynaBean deleteBean : deletedResults) {
        testEquipmentResultsRepository.delete("resultlabel_id", deleteBean.get("resultlabel_id"));
      }
      if (!success) {
        msg = msg.append("Failed to update Test Equipment master details..");
      }
    } else {
      msg = msg.append("Incorrectly formatted values supplied..");
    }

    return success;
  }

  /**
   * Gets the delete results.
   *
   * @param params
   *          the params
   * @return the delete results
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getDeleteResults(Map params) throws SQLException {
    String[] equipmentId = (String[]) params.get("equipment_id");
    String[] newResult = (String[]) params.get("new");
    String[] deleted = (String[]) params.get("deleted");
    List<BasicDynaBean> deleteResultBeanList = new ArrayList<BasicDynaBean>();
    BasicDynaBean deleteResultBean = null;
    List errorList = new ArrayList();

    if (equipmentId != null) {
      for (int i = 0; i < equipmentId.length; i++) {
        if (newResult[i].equals("") && deleted[i].equals("Y")) {
          deleteResultBean = testEquipmentResultsRepository.getBean();
          ConversionUtils.copyIndexToDynaBean(params, i, deleteResultBean, errorList);
          if (errorList.size() > 0) {
            break;
          }
          deleteResultBeanList.add(deleteResultBean);
        }
      }
    }

    return deleteResultBeanList;
  }

  /**
   * Gets the test equipment overbook limit.
   *
   * @param eqId
   *          the eq id
   * @return the test equipment overbook limit
   */
  public Integer getTestEquipmentOverbookLimit(String eqId) {
    return testEquipmentRepository.getTestEquipmentOverbookLimit(eqId);
  }

  /**
   * Gets the test equipment name.
   *
   * @param eqId
   *          the eq id
   * @return the test equipment name
   */
  public String getTestEquipmentName(String eqId) {
    return testEquipmentRepository.getTestEquipmentName(eqId);
  }
}
