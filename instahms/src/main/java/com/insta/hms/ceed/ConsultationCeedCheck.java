package com.insta.hms.ceed;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.ceed.generated_test.Response;
import com.insta.hms.ceed.generated_test.Response.ClaimEdit;
import com.insta.hms.ceed.generated_test.Response.ClaimEdit.ActivityEdit;
import com.insta.hms.ceed.generated_test.Response.ClaimEdit.Edit;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

/*
 *  This is a extension of AbstractCeedCheck class for consultation screen. 
 */
public class ConsultationCeedCheck extends AbstractCeedCheck {

  /*
   * This method returns a list of all activities
   */
  @Override
  public List<BasicDynaBean> getActivities(String conId, String visitId) throws SQLException {
    String healthAuthority = getHealthAuthority(visitId);

    List<BasicDynaBean> allActivities = new ArrayList<BasicDynaBean>();

    // get list of all operation activities
    List<BasicDynaBean> operationactivities = CeedDAO.getOperationActivities(conId);
    // get list of all service activities
    List<BasicDynaBean> serviceactivities = CeedDAO.getServiceActivities(conId);
    // get list of all test activities
    List<BasicDynaBean> testactivities = CeedDAO.getTestActivities(conId);
    // get list of all medicine activities
    List<BasicDynaBean> medicineactivities = CeedDAO.getMedicineActivities(conId, healthAuthority);

    allActivities.addAll(operationactivities);
    allActivities.addAll(serviceactivities);
    allActivities.addAll(testactivities);
    allActivities.addAll(medicineactivities);

    return allActivities;
  }

  /*
   * This method inserts activities into ceed_integration_details table.
   */
  @Override
  public Map insertActivitiesInCeedIntegrationDetails(Connection con, List allActivities,
      int claimId, Integer consultationId, String patientId, String serviceType)
      throws SQLException, IOException {
    List activitiesToSend = new ArrayList();

    Character serviceTypeCode = null;
    if (serviceType.equals("2")) {
      serviceTypeCode = 'M';
    } else if (serviceType.equals("1")) {
      serviceTypeCode = 'C';
    }

    // insert activities into ceed_integration_details
    Iterator itr = allActivities.iterator();
    List<String> validCodeTypes = (List<String>) validActivityCodeTypesMap.get(serviceType);
    boolean first = true;
    while (itr.hasNext()) {
      if (first) {
        if (!insertRequestDetailsInCeedMain(con, claimId, consultationId, patientId, 'A',
            serviceTypeCode)) {
          logger.error(ceedTransactionFailureMessage);
          return returnTransactionFailureMessageMap(ceedTransactionFailureMessage);
        }
      }

      DynaBean bean = (DynaBean) itr.next();
      String claimEditRank = null;
      if (isValidCodeType((String) bean.get("code_type_classification"), validCodeTypes)) {
        if (bean.get("item_code") != null && !bean.get("item_code").equals("")) {
          claimEditRank = null;
          activitiesToSend.add(bean);
        } else {
          claimEditRank = "E";
        }
      } else {
        claimEditRank = "NA";
      }

      first = false;
      if (!CeedDAO.insertCeedIntegrationDetails(con, claimId,
          (Integer) bean.get("patient_presc_id"), null, (Integer) null,
          (String) bean.get("code_type"), (String) bean.get("item_code"), claimEditRank, null)) {
        logger.error(ceedTransactionFailureMessage);
        return returnTransactionFailureMessageMap(ceedTransactionFailureMessage);
      }
    }
    Map ret = new HashMap();
    ret.put("flag", FLAG.SUCCESS);
    ret.put("list", activitiesToSend);
    return ret;
  }

  // RC: move to health authority preferences dao
  /**
   * Get health authority for a visit.
   * @param visitId Visit Identifier
   * @return bean containing health authority detials
   * @throws SQLException the SQL exception
   */
  public String getHealthAuthority(String visitId) throws SQLException {
    VisitDetailsDAO rdao = new VisitDetailsDAO();
    BasicDynaBean visitBean = rdao.findByKey("patient_id", visitId);
    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter((Integer) visitBean.get("center_id"));

    return healthAuthority;
  }

  /*
   * This method returns encounter type for a visit.
   */
  @Override
  public BasicDynaBean getEncounterDetails(String visitId) throws SQLException {
    BasicDynaBean bean = CeedDAO.getEncounterDetails(visitId);
    // hard code encounter type to 1 if null for ceed check on consultation sceen
    if (bean.get("encounter_type") == null) {
      bean.set("encounter_type", 1);
    }
    return bean;
  }

  /*
   * This method processes ceed response.
   */
  @Override
  public Map processResponse(Connection con, Holder<Response> response, int claimId)
      throws NumberFormatException, SQLException, IOException {
    // update response info
    CeedDAO.updateResponseInfo(con, claimId, null);

    if (response.value.getClaimEdit().size() > 0) {
      ClaimEdit responseclaimedit = response.value.getClaimEdit().get(0);
      List<ActivityEdit> ls = responseclaimedit.getActivityEdit();
      Iterator<ActivityEdit> aeitr = ls.iterator();
      String claimEditRefNum = responseclaimedit.getRefNum();
      List<Edit> editslist = responseclaimedit.getEdit();
      Map editsMap = getAllEdits(editslist);

      while (aeitr.hasNext()) {
        ActivityEdit ae = aeitr.next();
        int activityId = Integer.parseInt(ae.getID());

        int index = 0;
        for (String claimEditId : ae.getEditId()) {
          String claimEditSubType = (String) ((Map) editsMap.get(claimEditId)).get("edit_sub_type");
          String claimEditCode = (String) ((Map) editsMap.get(claimEditId)).get("edit_code");
          String claimEditRank = (String) ((Map) editsMap.get(claimEditId)).get("edit_rank");
          String claimEditResponseComments = (String) ((Map) editsMap.get(claimEditId))
              .get("edit_comment");

          if (index == 0) {
            if (!CeedDAO.updateCeedIntegrationDetails(con, claimId, activityId, claimEditRefNum,
                Integer.parseInt(claimEditId), claimEditSubType, claimEditCode, claimEditRank,
                claimEditResponseComments)) {
              logger.error(ceedTransactionFailureMessage);
              return returnTransactionFailureMessageMap(ceedTransactionFailureMessage);
            }
          } else {
            if (!CeedDAO.insertCeedIntegrationDetails(con, claimId, activityId, claimEditRefNum,
                Integer.parseInt(claimEditId), claimEditSubType, claimEditCode, claimEditRank,
                claimEditResponseComments)) {
              logger.error(ceedTransactionFailureMessage);
              return returnTransactionFailureMessageMap(ceedTransactionFailureMessage);
            }
          }
          index++;
        }
      }
    }
    Map ret = new HashMap();
    ret.put("flag", FLAG.SUCCESS);

    return ret;
  }

  /*
   * This method insert into ceed_integration_main table
   */
  @Override
  public boolean insertRequestDetailsInCeedMain(Connection con, int claimId, Integer consultationId,
      String visitId, char status, char serviceType) throws SQLException, IOException {
    return CeedDAO.insertRequestDetailsInCeedMain(con, claimId, consultationId, visitId, status,
        serviceType);
  }

  /*
   * This method returns a list of observations.
   */
  @Override
  public Map getObservations(String visitId) {
    return null;
  }

}
