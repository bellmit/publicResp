package com.insta.hms.emr;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.vaccinationsinfo.VaccinationsInfoDao;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BasicDynaBean;

/**
 * The Class VaccinationProviderImpl.
 */
public class VaccinationProviderImpl implements EMRInterface {

  /** The dao. */
  VaccinationsInfoDao dao = new VaccinationsInfoDao();

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.emr.EMRInterface#listDocumentsByVisit(java.lang.String)
   */
  @Override
  public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.emr.EMRInterface#listVisitDocumentsForMrNo(java.lang.String)
   */
  @Override
  public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
    Map modulesActivatedMap =
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap();
    String modVaccinationStatus = (String) modulesActivatedMap.get("mod_vaccination");
    
    List patientVaccinationList = dao.getAllPatientVaccinationListForPrint(mrNo);
    
    if (dao.getDosageMasterList(mrNo, null, false).isEmpty() ||
      !"Y".equals(modVaccinationStatus) ||
      patientVaccinationList.isEmpty() ) {
      return null;
    }

    List<EMRDoc> vaccineList = new ArrayList<>();
    EMRDoc doc = new EMRDoc();
    doc.setTitle("Patient Vaccinations");
    BasicDynaBean bean = dao.getRecentVaccineAdministration(mrNo);
    if (bean != null) {
      doc.setDate((Date) bean.get("administered_date"));
      doc.setUpdatedBy((String) bean.get("updated_by"));
      doc.setUpdatedDate(DateUtil.formatTimestamp((Date) bean.get("updated_datetime")));
    }
    doc.setPdfSupported(true);
    doc.setType("SYS_VACCINATION");
    doc.setAuthorized(true);
    doc.setProvider(EMRInterface.Provider.ClinicalLabResultsProvider);
    doc.setDisplayUrl("/vaccinationInfo/utils.do?_method=printVaccinationsList&mr_no=" + mrNo);
    vaccineList.add(doc);
    return vaccineList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.emr.EMRInterface#listDocumentsByMrno(java.lang.String)
   */
  @Override
  public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.emr.EMRInterface#getPDFBytes(java.lang.String, int)
   */
  @Override
  public byte[] getPDFBytes(String docid, int printId) throws Exception {
    return null;
  }

}
