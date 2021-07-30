package com.insta.hms.emr;

import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatientDocTypeFilter implements Filter {

  @Override
  public List applyFilter(List<EMRDoc> allDocs, String criteria) {
    Map patientSharableDocTypes = new HashMap();
    List<EMRDoc> filteredDocs = new ArrayList<>();
    try {
      patientSharableDocTypes = DocumentTypeMasterDAO.getPatientShareableDocTypes();
    } catch (SQLException throwables) {
    }
    Set<String> sharableDocTypes = patientSharableDocTypes.keySet();
    for(EMRDoc doc : allDocs) {
      if(sharableDocTypes.contains(doc.getType())) {
        filteredDocs.add(doc);
      }
    }
    return filteredDocs;
  }
}
