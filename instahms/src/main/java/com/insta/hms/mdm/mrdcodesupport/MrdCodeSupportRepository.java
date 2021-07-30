package com.insta.hms.mdm.mrdcodesupport;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/** @author Anand Patel.
 * */
@Repository
public class MrdCodeSupportRepository extends MasterRepository<Integer> {

  public MrdCodeSupportRepository() {
    super("mrd_supported_codes", "code_id");
  }

  public List<BasicDynaBean> getDiagnosisCodeType() {
    return DatabaseHelper.queryToDynaList(
        "SELECT code_type FROM mrd_supported_codes WHERE code_category='Diagnosis' ");
  }

  public List<BasicDynaBean> getDrugCodeType() {
    return DatabaseHelper.queryToDynaList(
        "SELECT code_type FROM mrd_supported_codes WHERE code_category='Drug' order by code_type ");
  }

  public List<BasicDynaBean> getConsultationsCodeType() {
    return DatabaseHelper.queryToDynaList(
        "SELECT code_type FROM mrd_supported_codes WHERE code_category='Consultations' ");
  }

  public List<BasicDynaBean> getObservationCodeType() {
    return DatabaseHelper.queryToDynaList(
        "SELECT code_type FROM mrd_supported_codes WHERE code_category='Observations' ");
  }

  public List<BasicDynaBean> getTreatmentCodeType() {
    return DatabaseHelper.queryToDynaList(
        "SELECT code_type FROM mrd_supported_codes WHERE code_category='Treatment' ");
  }
}
