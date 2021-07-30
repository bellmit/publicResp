package com.insta.instaapi.customer.visitemr;

import java.util.Arrays;
import java.util.List;

public enum DocTypesProvider {
  SYS_CONSULT(Arrays.asList(new String[] {"CaseSheetsProvider",
      "EandMcalculatorResultsProvider"})),
  SYS_OP(Arrays.asList(new String[] {"CaseFormsProvider"})),
  SYS_LR(Arrays.asList(new String[] {"DIAGProvider"})),
  SYS_RR(Arrays.asList(new String[] {"DIAGProvider"})),
  SYS_ST(Arrays.asList(new String[] {"ServiceProvider"})),
  SYS_DS(Arrays.asList(new String[] {"DischargeSummaryProvider"})),
  SYS_TRIAGE(Arrays.asList(new String[] {"TriageSummaryProvider"})),
  SYS_ASSESSMENT(Arrays.asList(new String[] {"AssessmentProvider"})),
  SYS_IVP(Arrays.asList(new String[] {"IntakeOutputParamProvider"})),
  SYS_VP(Arrays.asList(new String[] {"VitalParamProvider"})),
  SYS_RX(Arrays.asList(new String[] {"MealsPrescriptionsProvider"})),
  SYS_DIE(Arrays.asList(new String[] {"DietChartProvider"})),
  SYS_RG(Arrays.asList(new String[] {"RegistrationFormProvider"})),
  SYS_OT(Arrays.asList(new String[] {"OperationProvider", "OtRecordsProvider"})),
  SYS_DIALYSIS(Arrays.asList(new String[] {"DialysisSessionsProvider"})),
  SYS_CLINICAL(Arrays.asList(new String[] {"ScoreCardProvider"})),
  SYS_CLINICAL_LAB(Arrays.asList(new String[] {"ClinicalLabResultsProvider"})),
  SYS_PROGRESS_NOTES(Arrays.asList(new String[] {"ProgressNotesProvider"})),
  SYS_IP(Arrays.asList(new String[] {"DoctorOrderProvider", "VisitSummaryRecordsProvider",
      "MedicationChartProvider"})),
  SYS_GROWTH_CHART(Arrays.asList(new String[] {"GrowthChartsProvider"})),
  SYS_VACCINATION(Arrays.asList(new String[] {"VaccinationProviderServiceProvider"})),
  SYS_MRDCODE(Arrays.asList(new String[] {"MRDCodesProvider"})),
  GENERIC_DOCS_FORMS(Arrays.asList(new String[] {"GenericDocumentsProvider",
      "GenericInstaFormProvider"})),
  SYS_OPHTHALMOLOGY(Arrays.asList(new String[] {"OphthalmologyProvider"})),
  OTHERS(Arrays.asList(new String[] {"PlanCardProvider", "CorporateCardProvider",
      "NationalCardProvider", "MLCFormProvider" }))
  ;

  private final List<String> providers;

  DocTypesProvider(List<String> providers) {
    this.providers = providers;
  }

  public List<String> getProviders() {
    return providers;
  }
}