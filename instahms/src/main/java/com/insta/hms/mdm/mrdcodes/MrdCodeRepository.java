package com.insta.hms.mdm.mrdcodes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Deprecated // Use IcdCodesRepository
@Repository
public class MrdCodeRepository extends MasterRepository<Integer> {

  public MrdCodeRepository() {
    super("mrd_codes_master", "mrd_code_id");
  }

  public static final String FIND_CODER_DIAGNOSIS =
      " SELECT CASE WHEN diag_type = 'P' THEN 'Principal' WHEN diag_type = 'A' THEN 'Admitting' "
          + " WHEN diag_type = 'V' THEN 'ReasonForVisit' "
          + " ELSE 'Secondary' END AS diag_type, hcd.diag_type as diagnosis_type, " 
          + " hcd.code_type , icd_code, hcd.description as code_desc, "
          + " hcd.present_on_admission, hcd.year_of_onset " 
          + " FROM hospital_claim_diagnosis hcd " 
          + " WHERE visit_id = ?";

  public static final String FIND_DIAGNOSIS =
      " SELECT CASE WHEN diag_type = 'P' THEN 'Principal' WHEN diag_type = 'A' THEN 'Admitting' "
      + " WHEN diag_type = 'V' THEN 'ReasonForVisit' "
      + " ELSE 'Secondary' END AS diag_type, md.diag_type as diagnosis_type, " 
      + " md.code_type , icd_code, md.description as code_desc, md.sent_for_approval, "
      + " md.present_on_admission, md.year_of_onset " 
      + " FROM mrd_diagnosis md " 
      + " WHERE visit_id = ?";
  
  public List<BasicDynaBean> findAllDiagnosis(String visitId) {
    return DatabaseHelper.queryToDynaList(FIND_DIAGNOSIS, visitId);
  }
  
  public List<BasicDynaBean> findAllCoderDiagnosis(String visitId) {
    return DatabaseHelper.queryToDynaList(FIND_CODER_DIAGNOSIS, visitId);
  }
}