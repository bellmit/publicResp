package com.insta.hms.mdm.mrdcodesupport;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MrdCodeSupportService extends MasterService {

  public MrdCodeSupportService(
      MrdCodeSupportRepository mrdCodeSupportRepository,
      MrdCodeSupportValidator mrdCodeSupportValidator) {
    super(mrdCodeSupportRepository, mrdCodeSupportValidator);
  }

  public List<BasicDynaBean> getDefaultDiagnosisCodeType() {

    return ((MrdCodeSupportRepository) getRepository()).getDiagnosisCodeType();
  }

  public List<BasicDynaBean> getDrugCodeType() {

    return ((MrdCodeSupportRepository) getRepository()).getDrugCodeType();
  }

  public List<BasicDynaBean> getConsultationsCodeType() {

    return ((MrdCodeSupportRepository) getRepository()).getConsultationsCodeType();
  }

  public List<BasicDynaBean> getObservationCodeType() {
    return ((MrdCodeSupportRepository) getRepository()).getObservationCodeType();
  }

  public List<BasicDynaBean> getTreatmentCodeType() {
    return ((MrdCodeSupportRepository) getRepository()).getTreatmentCodeType();
  }
}
