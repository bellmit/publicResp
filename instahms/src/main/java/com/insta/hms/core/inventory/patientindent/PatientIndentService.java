package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientIndentService {
	
	static Logger logger = LoggerFactory.getLogger(PatientIndentService.class);
	
	/** The Patient Indent repository. */
	@LazyAutowired PatientIndentRepository  patientIndentRepository;
	
	public List<BasicDynaBean> getEquivalentMedicinesList(String medicineName, String genericName,
			String storeId, Boolean allStores, String saleType) throws Exception {
		
		List<BasicDynaBean> medicineNames = null;
		medicineNames = patientIndentRepository.getEquivalentMedicinesList(medicineName, genericName, storeId, allStores, saleType);
		return medicineNames;
	}

	/**
   * Gets the visit id.
   *
   * @param indentNo String
   * @return the visit id
   */
  public String getVisitId(String indentNo) {
    return patientIndentRepository.getVisitId(indentNo);
  }

  public Boolean isPatientIdentIdValid(String indentNo) {
    return patientIndentRepository.exist("patient_indent_no", indentNo);
  }

  public List<BasicDynaBean> getMedicineBatchDetailsForPatient(String patientId, List<Integer> medicineIds) {
    return patientIndentRepository.getMedicineBatchDetailsForPatient(patientId, medicineIds);
  }

}
