package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.antenatal.AntenatalService;
import com.insta.hms.core.clinical.complaints.ComplaintsService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.consultation.triagesummary.TriageSummaryService;
import com.insta.hms.core.clinical.consultationnotes.ConsultationFieldValuesService;
import com.insta.hms.core.clinical.diagnosisdetails.DiagnosisDetailsService;
import com.insta.hms.core.clinical.dischargemedication.DischargeMedicationService;
import com.insta.hms.core.clinical.documentsforms.DocumentsFormsService;
import com.insta.hms.core.clinical.healthmaintenance.HealthMaintenanceService;
import com.insta.hms.core.clinical.immunization.ImmunizationService;
import com.insta.hms.core.clinical.mar.MarService;
import com.insta.hms.core.clinical.notes.NotesService;
import com.insta.hms.core.clinical.obstetric.ObstetricHistoryService;
import com.insta.hms.core.clinical.pac.PreAnaesthestheticService;
import com.insta.hms.core.clinical.patientproblems.PatientProblemListService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating Section objects.
 *
 * @author krishnat
 */
@Component
public class SectionFactory {

  /** The pre anaesthesthetic service. */
  @Autowired
  private PreAnaesthestheticService preAnaesthestheticService;

  /** The allergies service. */
  @Autowired
  private AllergiesService allergiesService;

  /** The diagnosis details service. */
  @Autowired
  private DiagnosisDetailsService diagnosisDetailsService;

  /** The antenatal service. */
  @LazyAutowired
  private AntenatalService antenatalService;

  /** The obstetric history service. */
  @Autowired
  private ObstetricHistoryService obstetricHistoryService;

  /** The complaints service. */
  @Autowired
  private ComplaintsService complaintsService;

  /** The reading service. */
  @Autowired
  private VitalReadingService readingService;

  /** The health maint service. */
  @Autowired
  private HealthMaintenanceService healthMaintService;

  /** The pres service. */
  @Autowired
  private PrescriptionsService presService;

  /** The cons notes service. */
  @Autowired
  private ConsultationFieldValuesService consNotesService;

  /** The triage summary service. */
  @Autowired
  private TriageSummaryService triageSummaryService;

  /** The immunization service. */
  @Autowired
  private ImmunizationService immunizationService;

  /** The notes service. */
  @Autowired
  private NotesService notesService;

  /** The mar service. */
  @Autowired
  private MarService marService;
  
  @Autowired
  private DocumentsFormsService documentsFormsService;

  /** The bean factory. */
  @Autowired
  private BeanFactory beanFactory;
  
  @Autowired
  DischargeMedicationService dischargeMedicationService;

  @LazyAutowired
  private PatientProblemListService patientProblemListService;

  /** The services. */
  private Map<String, SectionService> services = new HashMap<>();

  /**
   * Gets the system services.
   *
   * @return the system services
   */
  public Map<String, SectionService> getSystemServices() {

    services.put("-1", complaintsService);
    services.put("-2", allergiesService);
    services.put("-3", triageSummaryService);
    services.put("-4", readingService);
    services.put("-5", consNotesService);
    services.put("-6", diagnosisDetailsService);
    services.put("-7", presService);
    services.put("-13", obstetricHistoryService);
    services.put("-14", antenatalService);
    services.put("-16", preAnaesthestheticService);
    services.put("-15", healthMaintService);
    services.put("-14", antenatalService);
    services.put("-17", immunizationService);
    services.put("-18", notesService);
    services.put("-19", marService);
    services.put("-20", documentsFormsService);
    services.put("-21", patientProblemListService);
    services.put("-22", dischargeMedicationService);
    
    return services;
  }

  /**
   * Gets the dynamic section service.
   *
   * @param sectionId the section id
   * @return the dynamic section service
   */
  public SectionService getDynamicSectionService(Integer sectionId) {
    return (SectionService) beanFactory.getBean(DynamicSectionService.class, sectionId);
  }

  /**
   * Gets the section service.
   *
   * @param sectionId the section id
   * @return the section service
   */
  public SectionService getSectionService(Integer sectionId) {
    if (sectionId > 0) {
      return getDynamicSectionService(sectionId);
    } else {
      return getSystemServices().get(sectionId.toString());
    }
  }

}
