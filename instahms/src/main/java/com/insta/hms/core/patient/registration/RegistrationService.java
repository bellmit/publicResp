package com.insta.hms.core.patient.registration;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillPrintHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.RandomGeneration;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.minio.MinioPatientDocumentsService;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.core.billing.BillOrReceipt;
import com.insta.hms.core.billing.BillRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ReceiptService;
import com.insta.hms.core.clinical.consultation.SecondaryComplaintService;
import com.insta.hms.core.clinical.discharge.DischargeService;
import com.insta.hms.core.clinical.forms.SectionFormService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.order.master.OrderValidator;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.PackageOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService.PreAuthItemType;
import com.insta.hms.core.clinical.prescriptions.PatientConsultationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientOperationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientServicePrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientTestPrescriptionsService;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.medicalrecords.MRDCaseFileIndentService;
import com.insta.hms.core.medicalrecords.MRDCaseFileIssueLogService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.patient.PatientDetailsMapper;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.PatientRegistrationMapper;
import com.insta.hms.core.patient.communication.PatientCommunicationService;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.AppointmentValidator;
import com.insta.hms.core.scheduler.ResourceService;
import com.insta.hms.documents.PatientDocumentRepository;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.documents.PlanCardDocumentsService;
import com.insta.hms.documents.PlanDocsDetailsService;
import com.insta.hms.documents.RegistrationDocumentStore;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.integration.insurance.VisitClassificationType;
import com.insta.hms.integration.insurance.eligbilityauthorization.EligibilityAuthorizationService;
import com.insta.hms.integration.priorauth.PriorAuthorizationService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesCache;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.mdm.areas.AreaService;
import com.insta.hms.mdm.bloodgroup.BloodGroupService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.cities.CityRepository;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupRepository;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.mdm.confidentialitygrpmaster.UserConfidentialityAssociationRepository;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.countries.CountryService;
import com.insta.hms.mdm.deathreasons.DeathReasonRepository;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.departmentunits.DepartmentUnitService;
import com.insta.hms.mdm.districts.DistrictService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.emailtemplates.EmailTemplateService;
import com.insta.hms.mdm.encountertype.EncounterTypeService;
import com.insta.hms.mdm.gendermaster.GenderService;
import com.insta.hms.mdm.govtidentifiers.GovtIdentifierService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplantypes.InsurancePlanTypeService;
import com.insta.hms.mdm.maritalstatusmaster.MaritalStatusRepository;
import com.insta.hms.mdm.maritalstatusmaster.MaritalStatusService;
import com.insta.hms.mdm.opvisittyperuleapplicability.OpVisitTypeRuleApplicabilityService;
import com.insta.hms.mdm.opvisittyperules.OpVisitTypeRulesService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.otheridentificationdocument.OtherIdentificationDocumentTypesService;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.paymentmode.PaymentModeService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.insta.hms.mdm.race.RaceService;
import com.insta.hms.mdm.receiptrefundprinttemplates.ReceiptRefundPrintTemplateService;
import com.insta.hms.mdm.referraldoctors.ReferralDoctorService;
import com.insta.hms.mdm.religionmaster.ReligionRepository;
import com.insta.hms.mdm.religionmaster.ReligionService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.salutations.SalutationRepository;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.mdm.samplecollectioncenters.SampleCollectionCenterService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.states.StateRepository;
import com.insta.hms.mdm.tpas.TpaService;
import com.insta.hms.mdm.transferhospitals.TransferHospitalsService;
import com.insta.hms.redis.RedisMessagePublisher;
import com.insta.hms.security.usermanager.RoleService;
import com.insta.hms.security.usermanager.UserService;
import com.insta.hms.usermanager.PasswordEncoder;
import com.lowagie.text.DocumentException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class RegistrationService.
 *
 * @author amolbagde
 * 
 *         The Class RegistrationService.
 */

@Service
public class RegistrationService {
  // rename this class as OPRegistrationService
  
  private static final String OP_TYPE_FOLLOWUP = "F";

  private static final String OP_TYPE_REVISIT = "R";

  private static final String OP_TYPE_MAIN = "M";

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(RegistrationService.class);
  
  /* The external link generic repository */
  private GenericRepository externalLinksRepo = new GenericRepository("external_links");

  /** The Constant REGISTER_AND_PAY. */
  public static final String REGISTER_AND_PAY = "Y";

  /** The center pref service. */
  @LazyAutowired
  private CenterPreferencesService centerPrefService;

  /** The reg pref service. */
  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The clinical preference service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPrefService;

  /** The patient cat service. */
  @LazyAutowired
  private PatientCategoryService patientCatService;
  
  /** The patientdocrepo. */
  @LazyAutowired
  private PatientDocumentRepository patientdocrepo;

  /** The area service. */
  @LazyAutowired
  private AreaService areaService;

  /** The country service. */
  @LazyAutowired
  private CountryService countryService;

  /** The govt identifier service. */
  @LazyAutowired
  private GovtIdentifierService govtIdentifierService;

  /** The reg custom fields service. */
  @LazyAutowired
  private RegistrationCustomFieldsService regCustomFieldsService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The patient category service. */
  @LazyAutowired
  private PatientCategoryService patientCategoryService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The incoming sample reg service. */
  @LazyAutowired
  private IncomingSampleRegistrationService incomingSampleRegService;

  /** The mrd case file attr service. */
  @LazyAutowired
  private MRDCaseFileIndentService mrdCaseFileAttrService;

  /** The email template service. */
  @LazyAutowired
  private EmailTemplateService emailTemplateService;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;

  /** The patient details mapper. */
  @LazyAutowired
  private PatientDetailsMapper patientDetailsMapper;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The patient insurance plan details service. */
  @LazyAutowired
  private PatientInsurancePlanDetailsService patientInsurancePlanDetailsService;

  /** The patient insurance policy details service. */
  @LazyAutowired
  private PatientInsurancePolicyDetailsService patientInsurancePolicyDetailsService;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The prepopulate visit info repository. */
  @LazyAutowired
  private PrepopulateVisitInfoRepository prepopulateVisitInfoRepository;
  
  /** The validate deathReason info repository. */
  @LazyAutowired
  private DeathReasonRepository deathReasonRepository;
  
  /** The validate religion info repository. */
  @LazyAutowired
  private ReligionRepository religionRepository;
  
  /** The validate maritalStatus info repository. */
  @LazyAutowired
  private MaritalStatusRepository maritalStatusRepository;
  
  /** The validate confidentialityGroup info repository. */
  @LazyAutowired
  private ConfidentialityGroupRepository confidentialityGroupRepository;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The transfer hospital service. */
  @LazyAutowired
  private TransferHospitalsService transferHospitalService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The resource service. */
  @LazyAutowired
  private ResourceService resourceService;

  /** The resource availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resourceAvailabilityService;

  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;

  /** The discharge service. */
  @LazyAutowired
  private DischargeService dischargeService;

  /** The message util. */
  @LazyAutowired
  private MessageUtilSms messageUtil;

  /** The patient registration mapper. */
  @LazyAutowired
  private PatientRegistrationMapper patientRegistrationMapper;

  /** The registration validator. */
  @LazyAutowired
  private RegistrationValidator registrationValidator;

  /** The health auth pref service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;

  /** The practitioner map service. */
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerMapService;

  /** The sample col center service. */
  @LazyAutowired
  private SampleCollectionCenterService sampleColCenterService;

  /** The department unit service. */
  @LazyAutowired
  private DepartmentUnitService departmentUnitService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The registration ACL. */
  @LazyAutowired
  private RegistrationACL registrationACL;

  /** The mlc doc service. */
  @LazyAutowired
  private MLCDocumentsService mlcDocService;

  /** The role service. */
  @LazyAutowired
  private RoleService roleService;

  /** The prior auth service. */
  @LazyAutowired
  private PriorAuthorizationService priorAuthService;

  /** The mlc doc store. */
  @LazyAutowired
  private MLCDocumentsStore mlcDocStore;

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The eligibility auth service. */
  @LazyAutowired
  private EligibilityAuthorizationService eligibilityAuthService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The insurance plan type service. */
  @LazyAutowired
  private InsurancePlanTypeService insurancePlanTypeService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The insurance plan details service. */
  @LazyAutowired
  private InsurancePlanDetailsService insurancePlanDetailsService;

  /** The test presc service. */
  @LazyAutowired
  private PatientTestPrescriptionsService testPrescService;

  /** The service presc service. */
  @LazyAutowired
  private PatientServicePrescriptionsService servicePrescService;

  /** The consultation presc service. */
  @LazyAutowired
  private PatientConsultationPrescriptionsService consultationPrescService;

  /** The operation presc service. */
  @LazyAutowired
  private PatientOperationPrescriptionsService operationPrescService;

  /** The sponsor service. */
  @LazyAutowired
  private SponsorService sponsorService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The registration preferences service. */
  @LazyAutowired
  private RegistrationPreferencesService registrationPreferencesService;

  /** The referral doctor service. */
  @LazyAutowired
  private ReferralDoctorService referralDoctorService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The encounter type service. */
  @LazyAutowired
  private EncounterTypeService encounterTypeService;

  /** The secondary comp service. */
  @LazyAutowired
  private SecondaryComplaintService secondaryCompService;

  /** The mrd case file issue log service. */
  @LazyAutowired
  private MRDCaseFileIssueLogService mrdCaseFileIssueLogService;

  /** The mrd diagnosis service. */
  @LazyAutowired
  private MRDDiagnosisService mrdDiagnosisService;

  /** The reg doc store. */
  @LazyAutowired
  private RegistrationDocumentStore regDocStore;

  /** The receipt service. */
  @LazyAutowired
  private ReceiptService receiptService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /** The service group service. */
  @LazyAutowired
  private ServiceGroupService serviceGroupService;

  /** The service sub group service. */
  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The charge heads service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The package order item service. */
  @LazyAutowired
  private PackageOrderItemService packageOrderItemService;

  /** The packages service. */
  @LazyAutowired
  private PackagesService packagesService;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;

  /** The visit insurance details repository. */
  @LazyAutowired
  private VisitInsuranceDetailsRepository visitInsuDetailsRepo;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService orgService;

  /** The printer definition service. */
  @LazyAutowired
  private PrinterDefinitionService printerDefinitionService;

  /** The receipt refund print template service. */
  @LazyAutowired
  private ReceiptRefundPrintTemplateService receiptRefundPrintTemplateService;

  /** The policy doc img service. */
  @LazyAutowired
  private PlanDocsDetailsService policyDocImgService;

  /** The patient insurance plans repository. */
  @LazyAutowired
  private PatientInsurancePlansRepository patientInsurancePlansRepository;

  /** The patient document service. */
  @LazyAutowired
  private PatientDocumentService patientDocumentService;
  
  @LazyAutowired
  private PlanCardDocumentsService planCradDocumentsService;

  /** The Other Identification Document Types Service. */
  @LazyAutowired
  private OtherIdentificationDocumentTypesService otherIdentificationDocumentTypesService;

  /** The minio patient document service. */
  @LazyAutowired
  MinioPatientDocumentsService minioPatientDocumentsService;

  /** The patient details repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository;

  /** The order validator. */
  @LazyAutowired
  private OrderValidator orderValidator;

  /** The payment mode service. */
  @LazyAutowired
  private PaymentModeService paymentModeService;

  /** The insta integration service. */
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The appointment validator. */
  @LazyAutowired
  private AppointmentValidator appointmentValidator;

  /** The confidentiality group service. */
  @LazyAutowired
  private ConfidentialityGroupService confidentialityGroupService;
  
  /** The user service. */
  @LazyAutowired
  private UserConfidentialityAssociationRepository userConfidentialityAssociationRepository;

  /** The redis message publisher. */
  @LazyAutowired
  private RedisMessagePublisher redisMessagePublisher;

  /** The patient communication service. */
  @LazyAutowired
  private SalutationRepository salutationRepository;

  @LazyAutowired
  private CityRepository cityRepository;

  @LazyAutowired
  private StateRepository stateRepository;

  @LazyAutowired
  private PatientCommunicationService patientCommunicationService;

  @LazyAutowired
  private OpVisitTypeRuleApplicabilityService opVisitTypeRuleApplicabilityService;
  
  @LazyAutowired
  private OpVisitTypeRulesService opVisitTypeRuleService;
  
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;
  
  @LazyAutowired
  private InterfaceEventMappingService interfaceEventService;

  @LazyAutowired
  private SectionFormService sectionFormService;
  
  /** The Constant LOYALTY_CARD_OFFERS. */
  private static final String LOYALTY_CARD_OFFERS = "loyalty_offers";

  /** The Constant ONEAPOLLO_OFFERS. */
  private static final String ONEAPOLLO_OFFERS = "oneapollo_offers";

  /** The district service. */
  @LazyAutowired
  private DistrictService districtService;

  @LazyAutowired
  private PatientFormDetailsRepository patientFormDetailsRepository;

  /** The Constant LOYALTY_CARD. */
  private static final String LOYALTY_CARD = "loyalty_card";

  /** The Constant ONEAPOLLO_CARD. */
  private static final String ONEAPOLLO_CARD = "one_apollo_loyalty_card";

  /** The Constant CENTER_ID. */
  private static final String CENTER_ID = "centerId";

  /** The Constant SCHEMA. */
  private static final String SCHEMA = "schema";

  /** The Constant EVENT ID. */
  private static final String EVENT_ID = "eventId";

  /** The Constant EVENT DATA. */
  private static final String EVENT_DATA = "eventData";

  /** The Constant USER NAME. */
  private static final String USER_NAME = "userName";

  /** The Constant STATUS. */
  private static final String STATUS = "status";

  /** The Constant STORE_CODE. */
  private static final String STORE_CODE = "store_code";

  /** The Constant CENTER_ID1. */
  private static final String CENTER_ID1 = "center_id";

  /** The Constant COUNTRY_CODE. */
  private static final String COUNTRY_CODE = "country_code";

  /** The Constant DEFAULT_ORG. */
  private static final String DEFAULT_ORG = "ORG0001";

  /** The Constant DOC_NAME. */
  private static final String DOC_NAME = "doc_name";
  
  /** The Constant USERNAME. */
  private static final String USERNAME = "username";

  /** The Constant MOBILE_ACCESS. */
  private static final String MOBILE_ACCESS = "mobile_access";

  /** The Constant BILL_TYPE. */
  private static final String BILL_TYPE = "bill_type";

  /** The Constant CONS_DATE. */
  private static final String CONS_DATE = "cons_date";

  /** The Constant CONS_TIME. */
  private static final String CONS_TIME = "cons_time";

  /** The Constant INSURANCE. */
  private static final String INSURANCE = "insurance";

  /** The Constant DOC_ID. */
  private static final String DOC_ID = "doc_id";

  /** The Constant PATIENT_POLICY_ID. */
  private static final String PATIENT_POLICY_ID = "patient_policy_id";

  /** The Constant PRIORITY. */
  private static final String PRIORITY = "priority";

  /** The Constant USE_DRG. */
  private static final String USE_DRG = "use_drg";

  /** The Constant USE_PERDIEM. */
  private static final String USE_PERDIEM = "use_perdiem";

  /** The Constant PATIENT_TYPE. */
  private static final String PATIENT_TYPE = "patient_type";

  /** The Constant INSURANCE_CO_ID. */
  private static final String INSURANCE_CO_ID = "insurance_co_id";

  /** The Constant AREA_ID. */
  private static final String AREA_ID = "area_id";

  /** The Constant NATIONALITY. */
  private static final String NATIONALITY = "nationality";

  /** The Constant USER_ID. */
  private static final String USER_ID = "userId";

  /** The Constant VALUES. */
  private static final String VALUES = "values";

  /** The Constant MAX_CENTERS_DEFAULT. */
  private static final String MAX_CENTERS_DEFAULT = "max_centers_inc_default";

  /** The Constant SAMPLE_COL_CEN_ID. */
  private static final String SAMPLE_COL_CEN_ID = "sampleCollectionCenterId";

  /** The Constant GENERAL_MSG_SEND. */
  private static final String GENERAL_MSG_SEND = "general_message_send";

  /** The Constant VISIT. */
  private static final String VISIT = "visit";

  /** The Constant IS_ER_VISIT. */
  private static final String IS_ER_VISIT = "is_er_visit";

  /** The Constant RELATION. */
  private static final String RELATION = "relation";

  /** The Constant PATIENT_C_OCC. */
  private static final String PATIENT_C_OCC = "patient_care_oftext_country_code";

  /** The Constant OP_TYPE. */
  private static final String OP_TYPE = "op_type";

  /** The Constant APPOINTMENT_ID. */
  private static final String APPOINTMENT_ID = "appointment_id";

  /** The Constant MODE_LOYALTY. */
  private static final Integer MODE_LOYALTY = -3;

  /** The Constant MODE_ONE_APOLLO. */
  private static final Integer MODE_ONE_APOLLO = -5;
  
  /** The accounting job scheduler. */
  @LazyAutowired
  private AccountingJobScheduler accountingJobScheduler;

  /** The adt service. */
  @LazyAutowired
  private ADTService adtService;
  
  @LazyAutowired
  private RaceService raceService;
  
  @LazyAutowired
  private BloodGroupService bloodGroupService;
  
  @LazyAutowired
  private ReligionService religionService;
  
  @LazyAutowired
  private GenderService genderService;
  
  @LazyAutowired
  private MaritalStatusService maritalStatusService;
  
  /** Redis Template. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;
  
  /**
   * Gets the details.
   *
   * @param paramsMap the params map
   * @return the details
   */
  public Map<String, Object> getDetails(Map<String, String[]> paramsMap, String userLangCode) {
    Map<String, Object> opRegData = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);

    opRegData.put("basic_info", getBasicInfo(paramsMap));

    opRegData.putAll(getCustomFields());

    opRegData.put("visit_info", getVisitInfo());

    opRegData.put("registration_preferences", regPrefService.getRegistrationPreferences().getMap());
    Map centerPrefs = new HashMap<String, Object>(
        centerPrefService.getCenterPreferences(centerId).getMap());
    centerPrefs.remove("accumed_ftp_username");
    centerPrefs.remove("accumed_ftp_password");
    opRegData.put("center_preferences",centerPrefs );
    Map genericPreferences = new HashMap<String, Object>(
        genPrefService.getAllPreferences().getMap());
    genericPreferences.remove("password");
    opRegData.put("generic_preferences", genericPreferences);
    opRegData.put("health_authority_preferences",
        healthAuthPrefService.listBycenterId(centerId).getMap());
    opRegData.put("practitioner_consultation_mapping",
        ConversionUtils.listBeanToListMap(practitionerMapService.listByCenterId(centerId)));
    opRegData.put("orders", getOrdersInfo());
    opRegData.put("bill_print_template_list", billService.getBillPrintTemplate());
    opRegData.put("receipt_print_template_list",
        receiptRefundPrintTemplateService.getTemplateList());
    opRegData.put("printer_definition",
        ConversionUtils.listBeanToListMap(printerDefinitionService.lookup(false)));
    opRegData.put("preferred_languages", patientDetailsService.getPreferredLanguages(userLangCode));
    opRegData.put("cashLimitDetails", this.getPatientCashLimitDetails(null));
    String schema = (String) sessionAttributes.get("sesHospitalId");
    Object obj = redisTemplate.opsForValue().get("schema:" + schema + ";discardablesearchpatterns");
    List<String> discardablePatterns = new ArrayList<>(); 
    if (obj != null && !((String) obj).isEmpty()) {
      discardablePatterns = JsonUtility.toStringList((String) obj);
    }
    opRegData.put("discardble_search_patterns", discardablePatterns);      

    String url = null;
    int storeCode = 0;
    // Loyalty Card Offers
    BasicDynaBean loyaltyPaymentModeBean = paymentModeService.getPaymentMode("mode_id",
        MODE_LOYALTY);
    if (loyaltyPaymentModeBean != null && loyaltyPaymentModeBean.get(STATUS) != null) {
      String status = (String) loyaltyPaymentModeBean.get(STATUS);
      if (status.equalsIgnoreCase("A")) {
        BasicDynaBean integrationBean = instaIntegrationService
            .getActiveRecord(LOYALTY_CARD_OFFERS);
        if (integrationBean != null && integrationBean.get("url") != null
            && integrationBean.get(STATUS).equals("A")) {
          storeCode = getStoreCode(LOYALTY_CARD);
          url = (String) integrationBean.get("url");
        }
      }
    }

    // Check for OneApollo payment mode is enabled, the show
    // oneApollo_offers URL.
    BasicDynaBean oneApolloPaymentModeBean = paymentModeService.getPaymentMode("mode_id",
        MODE_ONE_APOLLO);
    if (oneApolloPaymentModeBean != null && oneApolloPaymentModeBean.get(STATUS) != null) {
      String status = (String) oneApolloPaymentModeBean.get(STATUS);
      if (status.equalsIgnoreCase("A")) {
        BasicDynaBean integrationBean = instaIntegrationService.getActiveRecord(ONEAPOLLO_OFFERS);
        if (integrationBean != null && integrationBean.get("url") != null
            && integrationBean.get(STATUS).equals("A")) {
          storeCode = getStoreCode(ONEAPOLLO_CARD);
          url = (String) integrationBean.get("url");
        }
      }
    }
    
    Map<String, Object> map = new HashMap<>();
    map.put("center_id", centerId);
    map.put("screen_id", "registration_screen");
    BasicDynaBean exlbean = externalLinksRepo.findByKey(map);
    if (null != exlbean) {
      if (null != exlbean.get("label")) {
        opRegData.put("retired_pharmacy_util_link_label", (String) exlbean.get("label"));
      }
    }

    opRegData.put("loyalty_offers_url", url);
    opRegData.put(STORE_CODE, storeCode);
    return opRegData;
  }

  /**
   * Gets the basic info.
   *
   * @param paramsMap the params map
   * @return the basic info
   */
  @SuppressWarnings("rawtypes")
  public Map<String, Object> getBasicInfo(Map<String, String[]> paramsMap) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);

    Map<String, Object> basicInfo = new HashMap<>();

    List<Integer> centerIds = new ArrayList<>();
    centerIds.add(centerId);
    basicInfo.put("category_rate_plans",
        ConversionUtils.listBeanToListMap(patientCatService.listByCenter(centerIds, true)));

    BasicDynaBean centerBean = centerService.getCenterDefaults(centerId);
    if (centerId != 0 && centerBean.get(COUNTRY_CODE) == null) {
      BasicDynaBean defaultCenterBean = centerService.getCenterDefaults(0);
      centerBean.set("country_id", defaultCenterBean.get("country_id"));
      centerBean.set(COUNTRY_CODE, defaultCenterBean.get(COUNTRY_CODE));
    }

    HashMap<String, Object> keyMap = new HashMap<String, Object>();
    String areaId = (null != paramsMap && paramsMap.containsKey(AREA_ID))
        ? paramsMap.get(AREA_ID)[0]
        : null;
    keyMap.put(AREA_ID, areaId);
    BasicDynaBean areaBean = areaService.findByPk(keyMap);

    String areaName = "";
    if (areaBean != null) {
      areaName = (String) areaBean.get("area_name");
    }
    List passportIssueCountryList = null;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    if (regPref.get("passport_issue_country") != null
        && !((String) regPref.get("passport_issue_country")).isEmpty()
        && !((String) regPref.get("passport_issue_country_show")).isEmpty()) {
      Map<String, Object> nationalityFilter = new HashMap<>();
      nationalityFilter.put(NATIONALITY, "f");
      passportIssueCountryList = countryService.lookup(true, nationalityFilter);
    }

    List nationalityList = null;
    if (regPref.get(NATIONALITY) != null && !((String) regPref.get(NATIONALITY)).isEmpty()
        && !((String) regPref.get("nationality_show")).isEmpty()) {
      nationalityList = countryService.lookup(true);
    }

    basicInfo.put("passport_issue_countries",
        ConversionUtils.listBeanToListMap(passportIssueCountryList));
    basicInfo.put("default_area_name", areaName);
    basicInfo.put("center_defaults", centerBean.getMap());

    BasicDynaBean districtBean = districtService.getDistrictDetails(
        (String) centerBean.get("city_id"), (String) centerBean.get("state_id"));
    basicInfo.put("center_defaults_district", districtBean != null ? districtBean.getMap() : null);
    basicInfo.put("visit_classifications", VisitClassificationType.enumMapToList());
    String userId = (String) sessionAttributes.get(USER_ID);
    basicInfo.put("patient_confidentiality_categories", ConversionUtils
        .listBeanToListMap(confidentialityGroupService.getUserConfidentialityGroups(userId)));
    basicInfo.put("govt_identifier_types",
        ConversionUtils.listBeanToListMap(govtIdentifierService.lookup(true)));
    basicInfo.put("other_identifier_types",
        ConversionUtils.listBeanToListMap(otherIdentificationDocumentTypesService.lookup(true)));
    basicInfo.put("salutations", ConversionUtils.listBeanToListMap(salutationService.lookup(true)));
    basicInfo.put("prior_auth_mode", ConversionUtils.listBeanToListMap(priorAuthService.listAll()));
    basicInfo.put("nationalities", ConversionUtils.listBeanToListMap(nationalityList));
    basicInfo.put("race", ConversionUtils.listBeanToListMap(raceService.lookup(true)));
    basicInfo.put("blood_group", ConversionUtils.listBeanToListMap(bloodGroupService.lookup(true)));
    basicInfo.put("marital_status", 
        ConversionUtils.listBeanToListMap(maritalStatusService.lookup(true)));
    basicInfo.put("religion", ConversionUtils.listBeanToListMap(religionService.lookup(true)));
    basicInfo.put("gender", ConversionUtils.listBeanToListMap(genderService.lookup(true)));

    return basicInfo;
  }

  /**
   * Gets the custom fields.
   *
   * @return the custom fields
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getCustomFields() {
    List<BasicDynaBean> regCustomFieldsList = regCustomFieldsService
        .getActiveRegCustomFieldsAndValues();
    Map<String, Object> customFieldsMap = new HashMap<>();
    ArrayList<Map<String, Object>> patientCustomFields = new ArrayList<Map<String, Object>>();
    ArrayList<Map<String, Object>> visitCustomFields = new ArrayList<Map<String, Object>>();

    for (BasicDynaBean customFieldBean : regCustomFieldsList) {
      Map<String, Object> customFieldMap = new HashMap<>(customFieldBean.getMap());
      if ((String) customFieldBean.get(VALUES) != null) {
        customFieldMap.put(VALUES,
            ((String) customFieldBean.get(VALUES)).split(Pattern.quote("^^")));
      } else {
        customFieldMap.put(VALUES, Collections.emptyList());
      }
      customFieldMap.put("show_group", customFieldBean.get("show_group"));
      if (customFieldBean.get("applicable_to").equals("P")) {
        patientCustomFields.add(customFieldMap);
      } else {
        visitCustomFields.add(customFieldMap);
      }

    }
    customFieldsMap.put("patient_custom_fields", patientCustomFields);
    customFieldsMap.put("visit_custom_fields", visitCustomFields);

    return customFieldsMap;
  }

  /**
   * Gets the visit info.
   *
   * @return the visit info
   */
  public Map<String, Object> getVisitInfo() {
    Map<String, Object> visitInfo = new HashMap<>();
    // For collection centers.
    Integer maxCentersIncDefault = (Integer) genPrefService.getAllPreferences()
        .get(MAX_CENTERS_DEFAULT);
    Integer sampleCollectionCenterId = (Integer) sessionService
        .getSessionAttributes(new String[] { SAMPLE_COL_CEN_ID }).get(SAMPLE_COL_CEN_ID);
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    List<BasicDynaBean> collectionCenters = Collections.emptyList();
    List<Integer> centerIds = new ArrayList<>();
    if (maxCentersIncDefault > 1) {
      if (sampleCollectionCenterId == -1) {
        centerIds.add(centerId);
        collectionCenters = sampleColCenterService.listByCenter(centerIds, true);
      }
    } else {
      collectionCenters = sampleColCenterService.listByCenter(centerIds, true);
    }
    visitInfo.put("collection_centers", ConversionUtils.listBeanToListMap(collectionCenters));

    visitInfo.put("departments",
        ConversionUtils.listBeanToListMap(departmentService.getNonClinicalDepartments()));
    List<BasicDynaBean> deptUnits = Collections.emptyList();
    if (regPrefService.getRegistrationPreferences().get("hosp_uses_units") != null
        && regPrefService.getRegistrationPreferences().get("hosp_uses_units").equals("Y")) {
      deptUnits = departmentUnitService.listAllActive();
    }
    visitInfo.put("department_units", ConversionUtils.listBeanToListMap(deptUnits));

    visitInfo.put("transfer_sources",
        ConversionUtils.listBeanToListMap(transferHospitalService.listAllActive()));

    visitInfo.put("mlc_templates",
        ConversionUtils.listBeanToListMap(mlcDocService.getTemplates(true, "4", "A")));

    // Default Rate Plan.
    visitInfo.put("consultation_types",
        ConversionUtils.copyListDynaBeansToMap(getConsultationTypesForRateplan(DEFAULT_ORG)));

    visitInfo.put("encounterTypes", ConversionUtils.listBeanToListMap(encounterTypeService
        .getOpIpApplicableEncounterTypes(true, false)));

    BasicDynaBean defaultEncounterTypeBean = encounterTypeService
        .getVisitDefaultEncounter("o", false);
    Integer encounterType = 0;
    if (defaultEncounterTypeBean != null) {
      encounterType = (Integer) defaultEncounterTypeBean.get("encounter_type_id");
    }
    visitInfo.put("defaultEncounterType", encounterType);

    return visitInfo;
  }

  /**
   * Gets the consultation types for rateplan.
   *
   * @param ordId the ord id
   * @return the consultation types for rateplan
   */
  public List<BasicDynaBean> getConsultationTypesForRateplan(String ordId) {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();

    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);
    Map<String, Object> params = new HashMap<>();
    params.put(CENTER_ID1, centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);
    String healthAuthority = (String) centerBean.get("health_authority");
    healthAuthority = healthAuthority == null ? "" : healthAuthority;

    return consultationTypesService.getConsultationTypes("o", ordId, healthAuthority);
  }

  /**
   * Gets the appointment details.
   *
   * @param appointmentId the appointment id
   * @return the appointment details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getAppointmentDetails(String appointmentId) {
    Map<String, Object> appointmentData = new HashMap<>();
    if (appointmentId != null && !appointmentId.equals("")) {
      /*
       * for nonmrno patient while marknig arrive we need all appointments for that
       * ordered order set
       */
      BasicDynaBean appointmentBean = appointmentService.findByKey(Integer.parseInt(appointmentId));
      Integer appointmentPackGroupId = (Integer) appointmentBean.get("appointment_pack_group_id");
      List<BasicDynaBean> results = resourceService
          .getAppointmentDetails(Integer.parseInt(appointmentId), appointmentPackGroupId);
      if (results != null && !results.isEmpty()) {
        List<Object> resources = new ArrayList<>();
        Map<String, Object> appointment = new HashMap<>();
        for (BasicDynaBean result : results) {
          boolean isPrimaryResource = (Boolean) result.get("primary_resource");
          if (isPrimaryResource) {
            appointment.putAll(result.getMap());
          }
          resources.add(result.getMap());
        }
        if (!appointment.isEmpty()) {
          appointment.put("resources", resources);
          appointmentData.put("appointment", appointment);
        }
        if (appointmentPackGroupId != null) {
          appointmentData.put("todays_appointments", ConversionUtils.listBeanToListMap(results));
        }
      }
    }
    if (appointmentData.isEmpty()) {
      throw new EntityNotFoundException(
          new String[] { "Patient", "Appointment Id", appointmentId });
    }
    return appointmentData;
  }

  /**
   * Patient Pre Registration.
   *
   * @param params the params
   * @return the map
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws Exception the exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> preRegistration(Map<String, Object> params)
      throws NoSuchAlgorithmException, IOException, DocumentException, SQLException, ParseException,
      Exception {
    GenericPreferencesCache.CACHEDPREFERENCESDTO.remove(RequestContext.getSchema());
    GenericPreferencesCache.CACHEDPREFERENCESBEAN.remove(RequestContext.getSchema());
    RegistrationPreferencesDAO dao = new RegistrationPreferencesDAO("registration_preferences");
    dao.clearCache();
    genericPreferencesService.invalidateGenericPrefCache();
    BasicDynaBean patientBean = patientDetailsService.getBean();
    String userName = RequestContext.getUserName();
    ((HashMap<String, Object>) params.get("patient")).put("user_name",userName);
    copyToPatientDemographyBean(params, patientBean);

    Map<String, Object> map = new HashMap<String, Object>();
    String errorMsg = validateNewRegistration(params, patientBean);
    if (errorMsg != null && errorMsg != "") {
      map.put("error_message", errorMsg);
      return map;
    }
    insertPatientDemography(params, patientBean);
    map.put("patient", patientBean.get("mr_no"));
    String successMsg = "Patient Registration success";
    map.put("return_message", successMsg);

    return map;
  }


  /**
   * This method is used to send HIE patient Consent data.
   *
   * @param parameterMap the map
   * @throws ParseException the exception
   */
  public void sendPatientConsent(Map<String,String[]>  parameterMap) throws ParseException {
    Map<String,Object> map = new HashMap<>();
    Integer patientConsent = parameterMap.get("patient_consent")[0] != null
        ? Integer.parseInt(parameterMap.get("patient_consent")[0].trim()) : null;
    String consentTimeStamp = parameterMap.get("consent_timestamp")[0] != null
        ? parameterMap.get("consent_timestamp")[0].trim() : null;
    String mrNo = parameterMap.get("mr_no")[0] != null
        ? parameterMap.get("mr_no")[0].trim() : null;
    Integer centerId = parameterMap.get("center_id")[0] != null
        ? Integer.parseInt(parameterMap.get("center_id")[0].trim()) : null;
    interfaceEventService.patientConsentEvent(patientConsent,consentTimeStamp,mrNo,centerId);
  }

  /**
   * Validates patient basic information.
   *
   * @param params
   *          
   * @param patientBean
   * 
   * @return Errors, if unSuccessful
   */
  private String validateNewRegistration(Map<String, Object> params, BasicDynaBean patientBean)
      throws ParseException {
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    String errorMsg = null;
    patientParams.put("old_reg_auto_generate", "Y");
    errorMsg = checkPreference(patientParams, patientBean);
    if (errorMsg != null) {
      return errorMsg;
    }
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    if (!registrationValidator.validatePatientBasicInfo(patientBean, "O", validationErrors,
        false)) {
      ValidationException ex = new ValidationException(validationErrors);
      errorMsg = ex.getErrors().toString();
      return errorMsg;
    }
    validationErrors = new ValidationErrorMap();
    if (!registrationValidator.validatePatientAddnlFields(patientBean, "O", validationErrors,
        false)) {
      ValidationException ex = new ValidationException(validationErrors);
      errorMsg = ex.getErrors().toString();
      return errorMsg;
    }
    Integer centerId = Integer.parseInt((String) patientParams.get("center_id"));
    if (!registrationValidator.validatePatientCategoryAndPassport(patientBean, validationErrors,
        centerId)) {
      ValidationException ex = new ValidationException(validationErrors);
      errorMsg = ex.getErrors().toString();
      return errorMsg;
    }
    if (!registrationValidator.validateCustomFields(patientBean, "patient", validationErrors,
        false)) {
      ValidationException ex = new ValidationException(validationErrors);
      errorMsg = ex.getErrors().toString();
      return errorMsg;
    }
    errorMsg = checkMasterValidations(patientParams, patientBean);
    if (errorMsg != null) {
      return errorMsg;
    }
    if ((patientParams.get(Constants.MR_NO) == null
        || "".equals(patientParams.get(Constants.MR_NO)))
        && (patientParams.get("force") == null || !((Boolean) patientParams.get("force")))) {
      Map duplicateObj = checkPatientDetailsExists(patientParams);
      if (duplicateObj != null) {
        errorMsg = "duplicate patient exists";
        return errorMsg;
      }
    }
    return errorMsg;
  }
  
  /**
   * Validates fields mandatory and based on preferences.
   *
   * @param patientParams the patient params
   * @param patientBean the patient bean
   * @return Errors, if unSuccessful
   */
  private String checkPreference(Map<String, Object> patientParams, BasicDynaBean patientBean) {
    String errorMsg = null;
    if (patientParams.get("center_id") != null
        && !patientParams.get("center_id").toString().trim().isEmpty()) {
      Integer centerId = Integer.parseInt((String) patientParams.get("center_id"));
      Map<String, Object> params = new HashMap<>();
      params.put(CENTER_ID1, centerId);
      BasicDynaBean centerBean = centerService.findByPk(params);
      if (centerBean == null || !centerBean.get("status").equals("A")) {
        errorMsg = "{CENTER_ID is InValid}";
        return errorMsg;
      }
    } else {
      patientParams.put("center_id", "0");
    }

    BasicDynaBean regPrefBean = registrationPreferencesService.getRegistrationPreferences();

    String patientPhonePef = (String) regPrefBean.get("patientphone_field_validate");
    String patientPhoneValue = (String) patientParams.get("patient_phone");
    if (patientPhonePef != null && (patientPhonePef.toString().equals("A"))
        && (patientPhoneValue == null || patientPhoneValue.toString().trim().isEmpty())) {
      errorMsg = "patient_phone is a mandatory field ";
      return errorMsg;
    }

    Object patCatIdObject = (Object) getValue("patient_category_id", patientParams);
    patCatIdObject.toString().trim().isEmpty();
    if (patCatIdObject == null || patCatIdObject.toString().trim().isEmpty()) {
      errorMsg = "patient_category_id is a mandatory field ";
      return errorMsg;
    }
    Map<String, Object> filter = new HashMap<String, Object>();
    Integer categoryId = (Integer) patientBean.get("patient_category_id");
    filter.put("category_id", categoryId);

    BasicDynaBean patientCategoryBean = patientCategoryService.findByPk(filter);
    if (patientCategoryBean == null || !patientCategoryBean.get("status").equals("A")) {
      errorMsg = "InValid patient_category_id";
      return errorMsg;
    }
    filter.clear();
    String salutation = (String) patientParams.get("salutation");
    filter.put("salutation_id", salutation);
    if (patientBean.get("salutation") == null
        || patientBean.get("salutation").toString().trim().isEmpty()) {
      errorMsg = "{salutation=[Title is Mandatory]}";
      return errorMsg;
    }
    BasicDynaBean salutationBean = salutationService.findByPk(filter);
    if (salutationBean == null || !salutationBean.get("status").equals("A")) {
      errorMsg = "{salutation=[Title is InValid]}";
      return errorMsg;
    }
    String age = (String) patientParams.get("age");
    String dateOfBirth = (String) patientParams.get("dateofbirth");
    String allowAgeEntry = (String) regPrefBean.get("allow_age_entry");
    if ((dateOfBirth == null || dateOfBirth.trim().isEmpty())
        && (allowAgeEntry != null && allowAgeEntry.toString().equals("N"))) {
      errorMsg = "{dateofbirth is Mandtory}";
      return errorMsg;
    }
    if ((age == null || age.trim().isEmpty())
        && (dateOfBirth == null || dateOfBirth.trim().isEmpty())) {
      errorMsg = "Either DOB or Age must be provided ";
      return errorMsg;
    }
    String preferredLanguage = (String) patientParams.get("preferred_language");
    if (preferredLanguage == null || preferredLanguage.toString().trim().isEmpty()) {
      String lang = (String) genericPreferencesService.getAllPreferences()
          .get("contact_pref_lang_code");
      patientParams.put("preferred_language", "en");
      if (lang != null && !lang.toString().trim().isEmpty()) {
        patientParams.put("preferred_language", lang);
      }
    }
    Date now = new Date();
    Date dob;
    Map ageMap;
    if (patientBean.get("dateofbirth") != null) {
      dob = new Date(((java.sql.Date) patientBean.get("dateofbirth")).getTime());
      ageMap = DateUtil.getAgeBetweenDates(dob, now);
      if (((BigDecimal) ageMap.get("age")).compareTo(BigDecimal.valueOf(0)) < 0) {
        errorMsg = "DOB cannot be future date";
        return errorMsg;
      }
    }
    String mobileAccess = (String) patientParams.get("mobile_access");
    if (mobileAccess != null && mobileAccess.toString().equals("true")
        && (patientParams.get("email_id") == null
            || patientParams.get("email_id").toString().trim().isEmpty())) {
      errorMsg = "Email id is mandatory if Mobile Access is true";
      return errorMsg;
    }

    Integer identifierId = (Integer) patientBean.get("identifier_id");
    if (identifierId != null && !"".equals(identifierId.toString())) {
      filter.clear();
      filter.put("identifier_id", identifierId);
      BasicDynaBean govtIdentfierBean = govtIdentifierService.findByPk(filter);
      if (govtIdentfierBean == null) {
        errorMsg = "{identifier_id=[identifier_id is InValid]}";
        return errorMsg;
      }
    } else {
      String governmentIdentifier = (String) patientParams.get("government_identifier");
      if (governmentIdentifier != null && !governmentIdentifier.toString().trim().isEmpty()) {
        errorMsg = "{[government_identifier cannot be null without identifier_id ]}";
        return errorMsg;
      }
    }
    return errorMsg;
  }

  /**
   * Validates fields mandatory and based on preferences.
   *
   * @param patientParams the patient params
   * @param patientBean the patient bean
   * @return Errors, if unSuccessful
   */
  private String checkMasterValidations(Map<String, Object> patientParams,
      BasicDynaBean patientBean) {
    String errorMsg = null;

    if (patientParams.get("nationality_id") != null
        && !patientParams.get("nationality_id").toString().trim().isEmpty()) {
      BasicDynaBean countryBean = countryService.getCountry("country_id",
          patientParams.get("nationality_id").toString());
      if (countryBean == null || !countryBean.get("status").equals("A")) {
        errorMsg = "Invalid nationality_id entered for patient";
        return errorMsg;
      }
    }

    if (patientParams.get("patient_city") != null
        && !patientParams.get("patient_city").toString().trim().isEmpty()) {
      String city = patientParams.get("patient_city").toString();
      BasicDynaBean cityBean = cityRepository.findByKey("city_id", city);
      if (cityBean == null || !cityBean.get("status").equals("A")) {
        errorMsg = "invalid patient_city value";
      }
    }

    String countryId = null;
    if (patientParams.get("patient_state") != null
        && !patientParams.get("patient_state").toString().trim().isEmpty()) {
      String state = patientParams.get("patient_state").toString();
      BasicDynaBean st = stateRepository.findByKey("state_id", state);
      if (st == null || !st.get("status").equals("A")) {
        errorMsg = "invalid patient_state value";
        return errorMsg;
      } else {
        countryId = st.get("country_id").toString();
      }
    }
    if (patientParams.get("country") != null
        && !patientParams.get("country").toString().trim().isEmpty()) {
      BasicDynaBean countryBean = countryService.getCountry("country_id",
          patientParams.get("country").toString());
      if (countryBean == null || !countryBean.get("status").equals("A")) {
        errorMsg = "Invalid nationality_id entered for patient";
        return errorMsg;
      }
    }
    if (patientParams.get("country") == null
        || patientParams.get("country").toString().trim().isEmpty() && countryId != null) {
      patientParams.put("country", countryId);
      patientBean.set("country", countryId);
    }

    String patientCity = (String) patientParams.get("patient_city");
    String patientState = (String) patientParams.get("patient_state");
    if (patientCity != null && !patientParams.get("patient_city").toString().trim().isEmpty()
        && patientParams.get("patient_state") != null
        && !patientParams.get("patient_state").toString().trim().isEmpty()) {
      String city = patientParams.get("patient_city").toString();
      BasicDynaBean cityBean = cityRepository.findByKey("city_id", city);
      if (cityBean != null) {
        String stateId = cityBean.get("state_id").toString();
        if (!stateId.equals(patientState.toString())) {
          errorMsg = "City do not belong to Entered state";
          return errorMsg;
        }
      }
    }

    if (patientCity != null && !patientParams.get("patient_city").toString().trim().isEmpty()
        && (patientParams.get("patient_state") == null
            || patientParams.get("patient_state").toString().trim().isEmpty())) {
      String city = patientParams.get("patient_city").toString();
      BasicDynaBean cityBean = cityRepository.findByKey("city_id", city);
      if (cityBean != null && cityBean.get("state_id") != null
          && cityBean.get("status").equals("A")) {
        String stateId = cityBean.get("state_id").toString();
        patientBean.set("patient_state", stateId);
        if (patientParams.get("country") == null
            || patientParams.get("country").toString().trim().isEmpty()) {
          BasicDynaBean st = stateRepository.findByKey("state_id", stateId);
          if (st != null && st.get("status").equals("A")) {
            countryId = st.get("country_id").toString();
            patientBean.set("country", countryId);
          }
        }

      }
    }

    String oldMrNo = (String) patientParams.get("oldmrno");
    if (oldMrNo != null && !oldMrNo.toString().trim().isEmpty()) {
      BasicDynaBean oldMrNoBean = patientDetailsRepository.getMrNoWithOldMrNoBean(oldMrNo);
      if (oldMrNoBean != null) {
        errorMsg = "Duplicate patient with oldmrno Exists";
        return errorMsg;
      }
    }

    HashMap<String, Object> keyMap = new HashMap<String, Object>();
    String areaId = (String) patientParams.get("patient_area");
    keyMap.put(AREA_ID, areaId);
    if (patientParams.get("patient_area") != null
        && !patientParams.get("patient_area").toString().trim().isEmpty()) {
      String city = patientParams.get("patient_area").toString();
      BasicDynaBean areaBean = areaService.findByPk(keyMap);
      if (areaBean == null || !areaBean.get("status").equals("A")) {
        errorMsg = "invalid patient_area value";
      }
    }
    keyMap.clear();
    String customAttribute = "custom_list$_value";
    String customMaster = "custom_list$_master";
    for (int i = 1; i < 10; i++) {
      String customAttributeValue = customAttribute.replace("$", String.valueOf(i));
      String customMasterValue = customMaster.replace("$", String.valueOf(i));
      String customValue = (String) patientParams.get(customAttributeValue);
      if (customValue != null && !customValue.toString().trim().isEmpty()) {
        keyMap.put("custom_value", customValue);
        keyMap.put("status", "A");
        try {
          BasicDynaBean customBean = new GenericDAO(customMasterValue).findByKey(keyMap);
          if (customBean == null) {
            errorMsg = "InValid value for " + customAttributeValue;
            return errorMsg;
          }
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
    }
    keyMap.clear();

    String deathReason = (String) patientParams.get("death_reason_id");
    if (deathReason != null && !deathReason.toString().trim().isEmpty()) {
      keyMap.put("reason_id", deathReason);
      BasicDynaBean reasonBean = deathReasonRepository.findByPk(keyMap);
      if (reasonBean == null || !reasonBean.get("status").equals("A")) {
        errorMsg = "InValid value death_reason_id";
        return errorMsg;
      }
    }
    keyMap.clear();
    String bloodGroupId = (String) patientParams.get("blood_group_id");
    if (bloodGroupId != null && !bloodGroupId.toString().trim().isEmpty()) {
      keyMap.put("blood_group_id", Integer.parseInt(bloodGroupId));
      BasicDynaBean bloodGroupBean = null;
      try {
        bloodGroupBean = new GenericDAO("blood_group_master").findByKey(keyMap);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
      if (bloodGroupBean == null || !bloodGroupBean.get("status").equals("A")) {
        errorMsg = "InValid value blood_group_id";
        return errorMsg;
      }
    }
    keyMap.clear();
    String raceId = (String) patientParams.get("race_id");
    if (raceId != null && !raceId.toString().trim().isEmpty()) {
      keyMap.put("race_id", Integer.parseInt(raceId));
      BasicDynaBean raceBean = null;
      try {
        raceBean = new GenericDAO("race_master").findByKey(keyMap);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
      if (raceBean == null || !raceBean.get("status").equals("A")) {
        errorMsg = "InValid value race_id";
        return errorMsg;
      }
    }
    keyMap.clear();
    String maritalStatusId = (String) patientParams.get("marital_status_id");
    if (maritalStatusId != null && !maritalStatusId.toString().trim().isEmpty()) {
      keyMap.put("marital_status_id", maritalStatusId);
      BasicDynaBean maritalStatusBean = maritalStatusRepository.findByPk(keyMap);
      if (maritalStatusBean == null || !maritalStatusBean.get("status").equals("A")) {
        errorMsg = "InValid value marital_status_id";
        return errorMsg;
      }
    }
    keyMap.clear();
    String religionId = (String) patientParams.get("religion_id");
    if (religionId != null && !religionId.toString().trim().isEmpty()) {
      keyMap.put("religion_id", religionId);
      BasicDynaBean religionBean = religionRepository.findByPk(keyMap);
      if (religionBean == null || !religionBean.get("status").equals("A")) {
        errorMsg = "InValid value religion_id";
        return errorMsg;
      }
    }
    keyMap.clear();
    Integer patientGroupId = null;
    Object patientGroup = (Object) getValue("patient_group", patientParams);
    if (patientGroup != null && !patientGroup.equals("")) {
      patientGroupId = (Integer) patientGroup;
    }
    if (patientGroupId != null) {
      keyMap.put("confidentiality_grp_id", patientGroupId);
      BasicDynaBean confidentialityBean = confidentialityGroupRepository.findByPk(keyMap);
      if (confidentialityBean == null || !confidentialityBean.get("status").equals("A")) {
        errorMsg = "InValid value patient_group";
        return errorMsg;
      }
      Map<String, Object> filterMap = new HashMap<>();
      String userName = (String) patientParams.get("user_name");
      filterMap.put("emp_username", userName);
      filterMap.put("confidentiality_grp_id", patientGroupId);
      filterMap.put("status", "A");
      BasicDynaBean userConfidentialityAssocBean = null;
      userConfidentialityAssocBean = userConfidentialityAssociationRepository.findByKey(filterMap);
      if ((userConfidentialityAssocBean == null
          || !userConfidentialityAssocBean.get("status").equals("A"))
          && !patientGroupId.equals(0)) {
        errorMsg = "User is not Associated to confidentiality group";
        return errorMsg;
      }
    } else {
      patientBean.set("patient_group", 0);
    }
    return errorMsg;
  }
  
  /**
   * Creates the new visit.
   *
   * @param params the params
   * @return the map
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws SQLException             the SQL exception
   * @throws ParseException           the parse exception
   * @throws Exception                the exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> createNewVisit(Map<String, Object> params)
      throws NoSuchAlgorithmException, IOException, DocumentException, SQLException, ParseException,
      Exception {
    BasicDynaBean patientBean = patientDetailsService.getBean();
    BasicDynaBean visitBean = getBean();

    copyToBeansAndValidateNewVisit(params, patientBean, visitBean);

    insertPatientDemography(params, patientBean);

    insertPatientVisitInfo(params, patientBean, visitBean);

    Map<String, Object> map = saveInsuranceAndCreateBills(params, patientBean, visitBean);

    if (messageUtil.allowMessageNotification(GENERAL_MSG_SEND)) {
      Map<String, Object> patientMap = (Map<String, Object>) params.get("patient");
      String mrNo = (String) getValue(Constants.MR_NO, patientMap);
      String visitId = (String) visitBean.get(Constants.PATIENT_ID);
      sendOPAdmissionSMS(visitId, mrNo);
      if ("true".equals(patientMap.get(MOBILE_ACCESS))) {
        sendMobileAccessCommunications(mrNo, visitId);
      }
    }

    map.put(VISIT, visitBean);
    map.put("patient", patientBean);
    
    boolean newPatient = StringUtils.isEmpty((String) getValue(Constants.MR_NO,
        (Map<String, Object>) params.get("patient")));
    interfaceEventService.visitRegistrationEvent((String) visitBean.get(Constants.PATIENT_ID),
        newPatient);
    return map;
  }

  /**
   * Push appointments to web sockets.
   *
   * @param params the params
   */
  public void pushAppointmentsToWebSockets(Map<String, Object> params) {
    List appointmentIdList = (List) params.get("apptIds_to_arrive");
    List<Integer> appointmentIdForResponse = new ArrayList();
    List<String> appointmentCatForResponse = new ArrayList();
    for (Object appointmentIdObj : appointmentIdList) {
      Integer appointmentId = null;
      if (appointmentIdObj != null && !"".equals(appointmentIdObj.toString())) {
        appointmentId = Integer.parseInt(appointmentIdObj.toString());
      }
      if (appointmentId != null && appointmentId > 0) {
        BasicDynaBean appbean = resourceService.findByKey(appointmentId);
        BasicDynaBean resAvailablebean = resourceAvailabilityService
            .getResourceBean((Integer) appbean.get("res_sch_id"));
        String category = resAvailablebean.get("res_sch_category") != null
            ? (String) resAvailablebean.get("res_sch_category")
            : "DOC";
        appointmentIdForResponse.add(appointmentId);
        appointmentCatForResponse.add(category);
      }
    }
    if (!appointmentIdList.isEmpty()) {
      String apptCategories = org.jsoup.helper.StringUtil.join(appointmentCatForResponse, ",");
      String apptIds = org.jsoup.helper.StringUtil.join(appointmentIdForResponse, ",");
      redisMessagePublisher.publishMsgForSchema(
          RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL, apptIds + ";" + apptCategories);
    }
  }

  /**
   * Send mobile access communications.
   * 
   * @param mrNo    the mr no
   * @param visitId the visit id
   */
  private void sendMobileAccessCommunications(String mrNo, String visitId) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put(Constants.PATIENT_ID, visitId);
    dataMap.put(Constants.MR_NO, mrNo);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put(SCHEMA, RequestContext.getSchema());
    jobData.put(EVENT_ID, "enable_mobile_access");
    jobData.put(EVENT_DATA, dataMap);
    jobData.put(USER_NAME, RequestContext.getUserName());
    jobService.scheduleImmediate(
        buildJob("MobileAccessJob_" + visitId, RegistrationSmsJob.class, jobData));
  }

  /**
   * Copy to beans and validate new visit.
   *
   * @param params      the params
   * @param patientBean the patient bean
   * @param visitBean   the visit bean
   * @throws ParseException the parse exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void copyToBeansAndValidateNewVisit(Map<String, Object> params, BasicDynaBean patientBean,
      BasicDynaBean visitBean) throws ParseException {

    copyToPatientDemographyBean(params, patientBean);

    copyToVisitInfoBean(params, patientBean, visitBean);
    ValidationErrorMap validationErrors;
    Map<String, Object> nestedException = new HashMap<String, Object>();
    Integer centerId = !StringUtils.isEmpty(visitBean.get("center_id"))
        ? (int) visitBean.get("center_id") : 0;

    /* Validate Patient Demography. */
    validationErrors = new ValidationErrorMap();
    if (!registrationValidator.validatePatientDemographyNewVisit(patientBean, validationErrors,
        (Boolean) visitBean.get(IS_ER_VISIT),centerId)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("patient", ex.getErrors());
    }
    visitBean.set(RELATION,  patientBean.get(RELATION));
    visitBean.set("patient_care_oftext", patientBean.get("patient_care_oftext"));
    visitBean.set("patient_careof_address", patientBean.get("patient_careof_address"));
    visitBean.set(PATIENT_C_OCC, patientBean.get(PATIENT_C_OCC));

    validationErrors = new ValidationErrorMap();
    boolean paramsCheck = true;
    Map<String, Object> visitParams = (Map<String, Object>) params.get(VISIT);
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    if (visitBean.get("mlc_status") != null && "Y".equals(visitBean.get("mlc_status"))
        && "".equals(getValue("mlc_template", visitParams))) {
      validationErrors.addError("mlc_template", "js.registration.patient.mlc.template.required");
      paramsCheck = false;
    }
    if (!"".equals(getValue(Constants.MR_NO, patientParams)) && visitBean.get(OP_TYPE) != null
        && "D".equals(visitBean.get(OP_TYPE))
        && !"".equals(getValue("doctorcharge", visitParams))) {
      validationErrors.addError("doctorcharge", "js.registration.patient.consultation.is.not."
          + "required.for.follow.up.without.consultation");
      paramsCheck = false;
    }
    /* Scheduler Appointment */
    if (visitParams != null && !"".equals(visitParams.get(APPOINTMENT_ID))
        && visitParams.get(APPOINTMENT_ID) != null) {
      try {
        Integer appointmentId;
        appointmentId = Integer.parseInt(visitParams.get(APPOINTMENT_ID).toString());
        BasicDynaBean apptBean = resourceService.findByKey(appointmentId);
        if (apptBean == null) {
          validationErrors.addError(APPOINTMENT_ID,
              "exception.registration.patient.appointment.invalid");
          paramsCheck = false;
        } else {
          if (apptBean.get("appointment_status").equals("Arrived")) {
            validationErrors.addError(APPOINTMENT_ID,
                "registration.patient.action.message.error.patinet.already.arrived");
            paramsCheck = false;
          }
          String apptMrNo = (String) apptBean.get(Constants.MR_NO);
          if (apptMrNo != null && !apptMrNo.equals(getValue(Constants.MR_NO, patientParams))) {
            validationErrors.addError(APPOINTMENT_ID,
                "exception.registration.patient.appointment.invalid.for.mrno",
                Arrays.asList(appointmentId.toString()));
            paramsCheck = false;
          }
        }

      } catch (NumberFormatException exc) {
        validationErrors.addError(APPOINTMENT_ID,
            "exception.registration.patient.appointment.invalid");
        paramsCheck = false;
      }
    }
    if (visitParams != null && visitParams.get(Constants.ORG_ID) == null
        || visitParams.get(Constants.ORG_ID).equals("")) {
      validationErrors.addError(Constants.ORG_ID, "exception.rate.plan.required");
      paramsCheck = false;
    }

    if (visitParams != null && visitParams.get(BILL_TYPE) == null
        || visitParams.get(BILL_TYPE).equals("")) {
      validationErrors.addError(BILL_TYPE, "exception.bill.type.required");
      paramsCheck = false;
    }
    if (getValue(CONS_DATE, visitParams, true) != null) {
      String consDate = getValue(CONS_DATE, visitParams).toString();
      String consTime = getValue(CONS_TIME, visitParams) != null
          ? getValue(CONS_TIME, visitParams).toString()
          : "";
      String systemDate = getValue("system_date", visitParams).toString();
      String systemTime = getValue("system_time", visitParams) != null
          ? getValue(CONS_TIME, visitParams).toString()
          : "";
      java.util.Date consTimeStamp = DateUtil.parseTimestamp(consDate, consTime);
      java.util.Date systemTimeStamp = DateUtil.parseTimestamp(systemDate, systemTime);
      if (consTimeStamp.before(systemTimeStamp)) {
        validationErrors.addError(CONS_DATE,
            "js.registration.patient.consultation.date.time.current.date.time.check");
        paramsCheck = false;
      }
    }

    List<ValidationErrorMap> listValidationErrors = new ArrayList<ValidationErrorMap>();
    List<Map> insuranceErrors = new ArrayList<Map>();
    Boolean insurancePayloadHasErrors = null != params.get(INSURANCE) && !registrationValidator
        .validateInsurance(params, listValidationErrors, validationErrors, visitParams);
    if (insurancePayloadHasErrors) {
      for (ValidationErrorMap vaErrors : listValidationErrors) {
        ValidationException ex = new ValidationException(vaErrors);
        insuranceErrors.add(ex.getErrors());
      }
      nestedException.put(INSURANCE, insuranceErrors);
    }

    if (!registrationValidator.validateVisitInfoNewVisit(visitBean, patientBean,
        (List<Map>) params.get(INSURANCE), validationErrors)
        || !paramsCheck || insurancePayloadHasErrors) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put(VISIT, ex.getErrors());
    }

    listValidationErrors = new ArrayList<ValidationErrorMap>();
    List<Map> orderErrors = new ArrayList<Map>();
    Map<String, List<Object>> orderParams = (Map<String, List<Object>>) params.get("ordered_items");
    if (!orderValidator.validateOrderingDate(visitBean, orderParams, listValidationErrors)) {
      for (ValidationErrorMap vaErrors : listValidationErrors) {
        ValidationException ex = new ValidationException(vaErrors);
        orderErrors.add(ex.getErrors());
      }
      nestedException.put("ordered_items", orderErrors);
    }
    
    if (orderValidator.validateConductingDoctor(orderParams, listValidationErrors)) {
      for (ValidationErrorMap vaErrors : listValidationErrors) {
        ValidationException ex = new ValidationException(vaErrors);
        orderErrors.add(ex.getErrors());
      }
      nestedException.put("ordered_items", orderErrors);
    }
    listValidationErrors = new ArrayList<ValidationErrorMap>();
    List<Map> preAuthErrors = new ArrayList<Map>();
    if (CollectionUtils.isNotEmpty((List) params.get(INSURANCE))
        && !orderValidator.validatePreAuthItems(orderParams, listValidationErrors)) {
      for (ValidationErrorMap vaErrors : listValidationErrors) {
        ValidationException ex = new ValidationException(vaErrors);
        preAuthErrors.add(ex.getErrors());
      }
      nestedException.put("preauth_orders", preAuthErrors);
    }

    registrationValidator.validatePrescribingDoctor(params, visitParams, nestedException,
        validationErrors);

    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    if ((patientParams.get(Constants.MR_NO) == null
        || "".equals(patientParams.get(Constants.MR_NO)))
        && (patientParams.get("force") == null || !((Boolean) patientParams.get("force")))) {
      Map duplicateObj = checkPatientDetailsExists(patientParams);
      if (duplicateObj != null) {
        throw new DuplicateEntityException(HttpStatus.CONFLICT, new String[] { "Patient", "" },
            duplicateObj);
      }
    }

  }

  /**
   * Send OP admission SMS.
   *
   * @param patientId the patient id
   * @param mrNo      the mr no
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  private void sendOPAdmissionSMS(String patientId, String mrNo)
      throws SQLException, ParseException, IOException {
    Map<String, String> admissionData = getOPAdmissionData(patientId);

    String patientMobileNo = admissionData.get("patient_phone");
    String patientPartyMobileNo = admissionData.get("next_of_kin_contact");

    sendDoctorSMS(admissionData);
    sendPatientAddmissionSMS(mrNo, patientId, patientMobileNo, patientPartyMobileNo, admissionData,
        "op");
  }

  /**
   * Send doctor SMS.
   *
   * @param admissionData the admission data
   */
  private void sendDoctorSMS(Map<String, String> admissionData) {
    String consultingDoctorId = admissionData.get("consulting_doctor_id__");
    String referralDoctorId = admissionData.get("referal_doctor_id__");
    String visitId = admissionData.get("visit_id");

    String userName = (String) sessionService.getSessionAttributes().get(USER_ID);

    admissionData.put("receipient_type__", "DOCTOR");
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put(EVENT_ID, "op_patient_admitted");
    jobData.put(USER_NAME, userName);
    jobData.put(SCHEMA, RequestContext.getSchema());

    String consultingDoctorPhone = admissionData.get("doctor_mobile");
    if (!StringUtil.isNullOrEmpty(consultingDoctorPhone)) {
      admissionData.put("recipient_mobile", consultingDoctorPhone);
      admissionData.put("receipient_id__", consultingDoctorId);
      jobData.put(EVENT_DATA, admissionData);
      jobService.scheduleImmediate(
          buildJob("OPAdmissionMessageToDoctor_" + consultingDoctorId + "_" + visitId,
              OPAdmissionDoctorMessagingJob.class, jobData));
    } else {
      log.info("Consulting doctor mobile # not available. "
          + "Skipping OP admission message to doctor.");
    }

    String referralDoctorPhone = admissionData.get("referal_doctor_mobile");
    if (!StringUtil.isNullOrEmpty(referralDoctorPhone)) {
      admissionData.put("recipient_mobile", referralDoctorPhone);
      admissionData.put("receipient_id__", referralDoctorId);
      jobData.put(EVENT_DATA, admissionData);
      jobService.scheduleImmediate(
          buildJob("OPAdmissionMessageToDoctor_" + referralDoctorId + "_" + visitId,
              OPAdmissionDoctorMessagingJob.class, jobData));
    } else {
      log.info(
          "Referral doctor mobile # not available. " + "Skipping OP admission message to doctor.");
    }
  }

  /**
   * Send patient addmission SMS.
   *
   * @param mrNo                 the mr no
   * @param patientId            the patient id
   * @param patientMobileNo      the patient mobile no
   * @param patientPartyMobileNo the patient party mobile no
   * @param admissionData        the admission data
   * @param patientType          the patient type
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  private void sendPatientAddmissionSMS(String mrNo, String patientId, String patientMobileNo,
      String patientPartyMobileNo, Map<String, String> admissionData, String patientType)
      throws SQLException, ParseException, IOException {

    String userName = (String) sessionService.getSessionAttributes().get(USER_ID);

    admissionData.put("lang_code",
        PatientDetailsDAO.getContactPreference((String) admissionData.get(Constants.MR_NO)));
    admissionData.put("receipient_type__", "PATIENT");
    admissionData.put("receipient_id__", admissionData.get(Constants.MR_NO));

    if (null != mrNo && !mrNo.isEmpty()) {
      // Send Revise SMS to existing patient
      admissionData.put("recipient_mobile", patientMobileNo);
      String eventId = "patient_on_" + patientType + "_patient_revisit";
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put(SCHEMA, RequestContext.getSchema());
      jobData.put(EVENT_ID, eventId);
      jobData.put(EVENT_DATA, admissionData);
      jobData.put(USER_NAME, userName);
      jobService.scheduleImmediate(
          buildJob("RegistrationSmsJob_" + patientId, RegistrationSmsJob.class, jobData));
      // mgr.processEvent(eventId, admissionData);

      // Send Revise SMS to Patient Party
      if (null != patientPartyMobileNo && !patientPartyMobileNo.trim().equals("")) {
        admissionData.put("recipient_mobile", patientPartyMobileNo.trim());
        String msgEventId = "family_on_" + patientType + "_patient_revisit";
        Map<String, Object> jobDataMap = new HashMap<String, Object>();
        jobDataMap.put(SCHEMA, RequestContext.getSchema());
        jobDataMap.put(EVENT_ID, msgEventId);
        jobDataMap.put(EVENT_DATA, admissionData);
        jobDataMap.put(USER_NAME, userName);
        jobService.scheduleImmediate(
            buildJob("RegistrationSmsJobKin_" + patientId, RegistrationSmsJob.class, jobDataMap));
        // mgr.processEvent("family_on_"+ patientType +"_patient_revisit",
        // admissionData);
      } else {
        log.info("Patient Party mobile # not available, skipping SMS on admission");
      }
    } else { // new Patient Registration
      // Send SMS to Patient
      admissionData.put("recipient_mobile", patientMobileNo);
      String eventId = "patient_on_" + patientType + "_patient_admission";
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put(SCHEMA, RequestContext.getSchema());
      jobData.put(EVENT_ID, eventId);
      jobData.put(EVENT_DATA, admissionData);
      jobData.put(USER_NAME, userName);
      jobService.scheduleImmediate(
          buildJob("RegistrationSmsJob_" + patientId, RegistrationSmsJob.class, jobData));

      // Send SMS to Patient Party
      if (null != patientPartyMobileNo && !patientPartyMobileNo.trim().equals("")) {
        admissionData.put("recipient_mobile", patientPartyMobileNo.trim());
        String msgEventId = "family_on_" + patientType + "_patient_admission";
        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put(SCHEMA, RequestContext.getSchema());
        jobDataMap.put(EVENT_ID, msgEventId);
        jobDataMap.put(EVENT_DATA, admissionData);
        jobDataMap.put(USER_NAME, userName);
        jobService.scheduleImmediate(
            buildJob("RegistrationSmsJobKin_" + patientId, RegistrationSmsJob.class, jobDataMap));
        // mgr.processEvent("family_on_"+ patientType +"_patient_admission",
        // admissionData);
      } else {
        log.info("Patient Party mobile # not available, skipping SMS on admission");
      }
    }
  }

  /**
   * Gets the OP admission data.
   *
   * @param patientId the patient id
   * @return the OP admission data
   * @throws SQLException the SQL exception
   */
  private Map<String, String> getOPAdmissionData(String patientId) throws SQLException {
    BasicDynaBean bean = patientRegistrationRepository.getOPAdmissionData(patientId);
    Map<String, String> admissionData = new HashMap<String, String>();

    String referalDoctorId = (String) bean.get("reference_docto_id");
    String referalDocMobile = "";
    if (null != referalDoctorId && !referalDoctorId.trim().equals("")) {
      referalDocMobile = (String) referralDoctorService.getReferralDocMobile(referalDoctorId)
          .get("referrer_phone");
    }
    admissionData.put(Constants.MR_NO, (String) bean.get(Constants.MR_NO));
    admissionData.put("patient_name", (String) bean.get("full_name"));
    admissionData.put("admission_date", DateUtil.formatDate((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_date_yyyy_mm_dd",
        new DateUtil().getSqlDateFormatter().format((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_time", (String) bean.get("reg_time").toString());
    admissionData.put("admission_time_12hr",
        DateUtil.formatTimeMeridiem((java.sql.Time) bean.get("reg_time")));
    admissionData.put("center_name", (String) bean.get("center_name"));
    admissionData.put("center_code", (String) bean.get("center_code"));
    admissionData.put("complaint", (String) bean.get("complaint"));
    admissionData.put("admitted_by", (String) bean.get("admitted_by"));
    admissionData.put("department", (String) bean.get("dept_name"));
    admissionData.put("department_id", (String) bean.get("dept_id"));
    admissionData.put("patient_phone", (String) bean.get("patient_phone"));
    admissionData.put("next_of_kin_contact", (String) bean.get("patient_care_oftext"));
    admissionData.put("next_of_kin_name", (String) bean.get(RELATION));

    admissionData.put("doctor_name", (String) bean.get("doctor_name"));
    admissionData.put("referal_doctor", (String) bean.get("refdoctorname"));
    admissionData.put("doctor_mobile", (String) bean.get("doctor_mobile"));
    admissionData.put("referal_doctor_mobile", referalDocMobile);
    admissionData.put("consulting_doctor_id__", (String) bean.get("doctor"));
    admissionData.put("referal_doctor_id__", (String) bean.get("reference_docto_id"));
    admissionData.put("salutation_name", (String) bean.get("salutation"));
    admissionData.put("doctor_specialization", (String) bean.get("specialization"));
    admissionData.put("visit_id", (String) bean.get("visit_id"));
    admissionData.put("recipient_email", (String) bean.get("email_id"));
    return admissionData;
  }

  /**
   * Copy to visit info bean.
   *
   * @param params           the params
   * @param patDetailsBean   the pat details bean
   * @param visitDetailsBean the visit details bean
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("unchecked")
  private void copyToVisitInfoBean(Map<String, Object> params, BasicDynaBean patDetailsBean,
      BasicDynaBean visitDetailsBean) throws ParseException {

    Map<String, Object> visitParams = (Map<String, Object>) params.get(VISIT);
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(visitParams, visitDetailsBean, errors, true);

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }
    processVisitParamstoBean(visitDetailsBean, patDetailsBean, visitParams);

    String visitType = "o";
    visitDetailsBean.set("visit_type", visitType);

    String billingBedTypeForOP = (String) genPrefService.getAllPreferences()
        .get("billing_bed_type_for_op");
    billingBedTypeForOP = (billingBedTypeForOP != null && !billingBedTypeForOP.equals(""))
        ? billingBedTypeForOP
        : "GENERAL";
    visitDetailsBean.set("bed_type", billingBedTypeForOP);

  }

  /**
   * Copy to patient demography bean.
   *
   * @param params      the params
   * @param patientBean the patient bean
   */
  @SuppressWarnings("unchecked")
  public void copyToPatientDemographyBean(Map<String, Object> params, BasicDynaBean patientBean) {
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    String mrNo = (String) getValue(Constants.MR_NO, patientParams);
    int patientCategoryId = 1;
    int existingAge = 0;
    String existingAgeIn = null;
    BasicDynaBean existingPatientBean = null;

    if (mrNo != null && !"".equals(mrNo)) {
      existingPatientBean = patientDetailsService.getPatientDetailsDisplayBean(mrNo);
      if (existingPatientBean == null) {
        throw new EntityNotFoundException(new String[] { "patient", "MR NO", mrNo });
      }
      String parentMrNo = (String) existingPatientBean.get("original_mr_no");
      if (existingPatientBean != null && parentMrNo != null && !parentMrNo.trim().isEmpty()) {
        ValidationErrorMap errors = new ValidationErrorMap();
        errors.addError(Constants.MR_NO, "js.registration.patient.mrno.marked.duplicate");
        Map<String, Object> nestedException = new HashMap<String, Object>();
        ValidationException ex = new ValidationException(errors);
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      existingAge = (existingPatientBean.get("age") != null)
          ? (Integer) existingPatientBean.get("age")
          : 0;
      existingAgeIn = (existingPatientBean.get("agein") != null)
          ? (String) existingPatientBean.get("agein")
          : null;
      patientCategoryId = (Integer) (existingPatientBean.get("patient_category_id"));
    } else {
      patientBean.set(Constants.MR_NO, "");
    }

    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(patientParams, patientBean, errors, true);

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }

    // setting the null columns to empty.
    processPatientParamsToBean(patientBean, patientParams);

    String ageIn = (String) getValue("agein", patientParams);
    String ageString = getValue("age", patientParams).toString();
    String dateOfBirth = (String) getValue("dateofbirth", patientParams);

    setAge(patientBean, existingAge, existingAgeIn, dateOfBirth, ageString, ageIn);

    Object patCatIdObject = (Object) getValue("patient_category_id", patientParams);
    if (patCatIdObject != null && !patCatIdObject.equals("")) {
      patientCategoryId = (Integer) patCatIdObject;
    }
    patientBean.set("patient_category_id", patientCategoryId);

    if (patientParams.get("user_name") == null) {
      /* ACL check. throws accessdeniedexception is acl fails. */
      registrationACL.authenticatePatientDemographyNewVisit(patientBean, existingPatientBean);
    }
  }

  /**
   * Insert patient visit info.
   *
   * @param params           the params
   * @param patDetailsBean   the pat details bean
   * @param visitDetailsBean the visit details bean
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws ParseException    the parse exception
   */
  @SuppressWarnings("unchecked")
  private void insertPatientVisitInfo(Map<String, Object> params, BasicDynaBean patDetailsBean,
      BasicDynaBean visitDetailsBean) throws IOException, DocumentException, ParseException {
    Map<String, Object> visitParams = (Map<String, Object>) params.get(VISIT);
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    Map<String, Object> charges = (Map<String, Object>) params.get("charges");
    String userName = (String) sessionService.getSessionAttributes().get(USER_ID);
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    String schedulerGenerateOrder = (String) genericPreferencesService.getAllPreferences()
        .get("scheduler_generate_order");
    schedulerGenerateOrder = schedulerGenerateOrder == null ? "N" : schedulerGenerateOrder;

    boolean newReg = true;
    if (patientParams != null && patientParams.get(Constants.MR_NO) != null
        && !"".equals(patientParams.get(Constants.MR_NO))) {
      newReg = false;
    }

    String mrNo = (String) patDetailsBean.get(Constants.MR_NO);
    String deptName = (String) visitParams.get("dept_name");
    // last visit date in same center and same dept
    java.util.Date lastVisitDateTime = patientRegistrationRepository.getLastVistDate(centerId, mrNo,
        deptName);
    visitDetailsBean.set("established_type", "N");
    if (lastVisitDateTime != null) {
      int[] age = DateUtil.getAgeComponents(lastVisitDateTime);
      int years = age[0];
      if (years < 3) {
        visitDetailsBean.set("established_type", "E");
      }
    }
    BasicDynaBean patientBean = patientDetailsRepository.findByKey(Constants.MR_NO, mrNo);

    visitDetailsBean.set(Constants.MR_NO, mrNo);

    if (patientRegistrationRepository.exist(Constants.MR_NO, mrNo, false)) {
      visitDetailsBean.set("revisit", "Y");
    } else {
      visitDetailsBean.set("revisit", "N");
      if (patientBean == null
          || (patientBean != null && (patientBean.get("first_visit_reg_date") == null
              || patientBean.get("first_visit_reg_date").equals("")))) {
        patDetailsBean.set("first_visit_reg_date", DateUtil.getCurrentDate());
      }
    }

    /* Generate Visit Id. */
    String patientId = null;
    patientId = patientRegistrationRepository.getNextVisitId("o", centerId);
    visitDetailsBean.set(Constants.PATIENT_ID, patientId);

    /* If mlc selected then generate mlcnumber and set patient Bean. */
    if ("Y".equals(visitDetailsBean.get("mlc_status"))) {
      String mlcNumber = DatabaseHelper.getNextPatternId("MLCNO");
      visitDetailsBean.set("mlc_no", mlcNumber);
      patDetailsBean.set("first_mlc_visitid", patientId);
    }

    /* Set main visit id. */
    String mainVisitId = (String) getValue("main_visit_id", visitParams);
    String opType = (String) getValue(OP_TYPE, visitParams);
    mainVisitId = (mainVisitId == null || mainVisitId.equals("")) ? patientId : mainVisitId;

    if (OP_TYPE_FOLLOWUP.equals(opType) || "D".equals(opType)) {
      visitDetailsBean.set("main_visit_id", mainVisitId);
    } else {
      visitDetailsBean.set("main_visit_id", patientId);
    }
    /* Set Encounter Type and complaint .If there's a previous episode visit. */
    setComplaintAndEncounterType(visitDetailsBean);

    List<BasicDynaBean> episodePreviousVisitDetails = patientRegistrationRepository
        .listPreviousVisitsForMainVisit(mainVisitId);
    String latestEpisodeVisitId = null;

    if (episodePreviousVisitDetails != null && episodePreviousVisitDetails.size() > 0) {
      for (BasicDynaBean eb : episodePreviousVisitDetails) {
        latestEpisodeVisitId = (String) eb.get(Constants.PATIENT_ID);
        break;
      }
    }

    // Inserting The Visit.
    patientRegistrationRepository.insert(visitDetailsBean);
    Map<String, Object> adtData = new HashMap<String, Object>();
    adtData.put(Constants.PATIENT_ID, (String) visitDetailsBean.get(Constants.PATIENT_ID));
    adtData.put(Constants.MR_NO, (String) patDetailsBean.get(Constants.MR_NO));
    adtService.createAndSendADTMessage("ADT_04", adtData);

    /* Update the visit_id in patient demography if status of visit = 'A' */
    if ("A".equals(visitDetailsBean.get(STATUS))) {
      patDetailsBean.set("visit_id", patientId);
    } else {
      patDetailsBean.set("visit_id", null);
      patDetailsBean.set("patient_visit_id", patientId);
    }

    // Update the patient Details Table
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put(Constants.MR_NO, mrNo);
    patientDetailsService.update(patDetailsBean, keys);

    if (!newReg) {
      /* Close All previous Op visits excluding created visit. */
      String close = (String) getValue("close_visits", visitParams);
      if (close.equals("Y")) {
        Map<String, Object> mapDetails = new HashMap<String, Object>();
        mapDetails.put(Constants.MR_NO, mrNo);
        mapDetails.put("user_name", visitDetailsBean.get("user_name"));
        List<BasicDynaBean> previousActiveVisits = patientRegistrationRepository
            .getPatientVisits(mrNo, "o", centerId, true, false);
        Map<String, Object> hl7EventDataMap;
        Map<String, Object> visitkeys = new HashMap<String, Object>();
        for (BasicDynaBean bean : previousActiveVisits) {
          /* Condition to not close newly added visit. */
          if (patientId.equals(bean.get("visit_id"))) {
            continue;
          }
          BasicDynaBean updateBean = getBean();
          mapDetails.put("visitId", bean.get("visit_id"));
          mapDetails.put("discharge_date", DateUtil.getCurrentDate());
          closeVisit(mapDetails, updateBean);
          visitkeys.put(Constants.PATIENT_ID, bean.get("visit_id"));
          update(updateBean, visitkeys);
          interfaceEventService.visitCloseEvent((String) bean.get("visit_id"), null);
        }
      }

      /*
       * Copy diagnosis codes from previous visit(same episode) to episode follow-up
       * after update
       */
      mrdDiagnosisService.copyDiagCodes(visitDetailsBean, latestEpisodeVisitId, userName);

      /* Copy complaints from previous visit(same episode) */
      secondaryCompService.copyComplaints(visitDetailsBean, latestEpisodeVisitId, userName);

    }

    /* Insert or update Case file Department based on Raise indent */
    String caseFileNo = (String) patDetailsBean.get("casefile_no");
    String raiseIndent = getParamDefault(patientParams, "raise_case_file_indent", null);
    if (caseFileNo != null && !caseFileNo.equals("")) {
      regCaseFile(patDetailsBean, (String) visitDetailsBean.get("dept_name"), userName,
          raiseIndent);
    }
    /* Update the appointment */

    int consultationTypeId = 0;
    String category = null;
    String presDocId = (String) getValue("presDocId", visitParams);
    List appointmentIdList = (List) params.get("apptIds_to_arrive");
    for (Object appointmentIdObj : appointmentIdList) {
      Integer appointmentId = null;
      if (appointmentIdObj != null && !"".equals(appointmentIdObj.toString())) {
        appointmentId = Integer.parseInt(appointmentIdObj.toString());
      }
      if (appointmentId != null && appointmentId > 0) {
        List<BasicDynaBean> results = resourceService.getAppointmentDetails(appointmentId, null);
        BasicDynaBean apptBean = null;
        if (results != null && !results.isEmpty()) {
          for (BasicDynaBean result : results) {
            boolean isPrimaryResource = (Boolean) result.get("primary_resource");
            if (isPrimaryResource) {
              apptBean = result;
            }
          }
        }
        if (apptBean != null) {

          if (apptBean.get("consultation_type_id") != null) {
            if (charges.get("consultation_type_id") != null
                && !charges.get("consultation_type_id").equals("")) {
              consultationTypeId = Integer.parseInt((String) charges.get("consultation_type_id"));
            } else {
              consultationTypeId = (Integer) apptBean.get("consultation_type_id");
            }
            resourceService.updateScheduler(appointmentId, mrNo, patientId, patDetailsBean,
                (String) visitDetailsBean.get("complaint"), userName, consultationTypeId, null,
                presDocId, "Reg");
          }

          boolean conduction = false;
          String apptStatus = null;
          category = (String) apptBean.get("category");
          if (category != null && !category.equals("") && category.equals("OPE")) {
            // TODO: handle Outpatient Surgery Appointments
          }
          if (category != null && !category.equals("") && !category.equals("DOC")) {
            conduction = resourceService.getConductionForTestOrServiceOrOperation(category,
                (String) apptBean.get("res_sch_name"));
          }
          if (schedulerGenerateOrder.equals("Y") && (null == category || category.equals(""))) {
            apptStatus = "Completed";
          }
          if (!conduction && null != category && !category.equals("") && !category.equals("DOC")) {
            resourceService.updateTestOrServiceOrOperationStatus(appointmentId, category);
            apptStatus = "Completed";
          }
          if ((conduction && null != category && !category.equals("") && !category.equals("DOC"))
              || (null != category && category.equals("DOC"))) {
            boolean apptCompleted = resourceService.isAppointmentCompleted(appointmentId, category);
            apptStatus = apptCompleted ? "Completed" : "Arrived";
          }
          if (apptStatus != null) {
            resourceService.updateStatus(appointmentId, apptStatus, userName);
          }
        }
      }
    }

    regDocStore.autoGenerateRegDocuments(patientId, mrNo, "opreg", userName);

    /* MLC template. */
    if ("Y".equals(visitDetailsBean.get("mlc_status"))) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(Constants.MR_NO, mrNo);
      map.put(Constants.PATIENT_ID, patientId);
      String templateIdFormat = (String) getValue("mlc_template", visitParams);
      String[] tempIdAndFormat = templateIdFormat.split(",");
      String format = tempIdAndFormat[1];
      map.put(DOC_NAME, format);
      map.put("username", userName);
      map.put("mlc_template_id", templateIdFormat);
      mlcDocStore.create(map);
    }

  }

  /**
   * Reg case file.
   *
   * @param patientDetailsBean the patient details bean
   * @param deptId             the dept id
   * @param userName           the user name
   * @param raiseIndent        the raise indent
   * @return true, if successful
   */
  private boolean regCaseFile(BasicDynaBean patientDetailsBean, String deptId, String userName,
      String raiseIndent) {

    boolean success = true;
    String mrNo = patientDetailsBean.get(Constants.MR_NO).toString();
    boolean mrnoExists = mrdCaseFileAttrService.exist(Constants.MR_NO, mrNo);

    if (!mrnoExists) {
      BasicDynaBean mrdfile = mrdCaseFileAttrService.getBean();
      mrdfile.set(Constants.MR_NO, mrNo);
      mrdfile.set("file_status", "A");
      mrdfile.set("case_status", "A");
      mrdfile.set("created_date", DateUtil.getCurrentDate());

      BasicDynaBean regPrefBean = regPrefService.getRegistrationPreferences();
      int issueseq = 0;
      if ("Y".equals(regPrefBean.get("issue_to_mrd_on_registration"))) {

        issueseq = DatabaseHelper.getNextSequence("mrd_casefile_issue_id");

        mrdfile.set("requesting_dept", deptId);
        mrdfile.set("request_date", DateUtil.getCurrentTimestamp());
        mrdfile.set("indented", "N");
        mrdfile.set("requested_by", userName);
        mrdfile.set("remarks", "Issued while registration");
        mrdfile.set("issued_to_dept", deptId);
        mrdfile.set("issued_on", DateUtil.getCurrentTimestamp());
        mrdfile.set("issued_id", issueseq);
        mrdfile.set("file_status", "U");
      }

      success = mrdCaseFileAttrService.insert(mrdfile) > 0;

      if (regPrefBean.get("issue_to_mrd_on_registration").toString().equals("Y")) {

        BasicDynaBean issuefile = mrdCaseFileIssueLogService.getBean();
        issuefile.set("issue_id", issueseq);
        issuefile.set(Constants.MR_NO, mrNo);
        issuefile.set("issued_on", DateUtil.getCurrentTimestamp());
        issuefile.set("issued_to_dept", deptId);
        issuefile.set("issue_user", userName);

        success = mrdCaseFileIssueLogService.insert(issuefile) > 0;
      }
    } else if (raiseIndent != null && !raiseIndent.equals("") && "Y".equals(raiseIndent)) {
      BasicDynaBean mrdfile = mrdCaseFileAttrService.getBean();
      mrdfile.set("requesting_dept", deptId);
      mrdfile.set("request_date", DateUtil.getCurrentTimestamp());
      mrdfile.set("indented", "Y");
      mrdfile.set("requested_by", userName);
      mrdfile.set("remarks", "Raised Indent while registration");

      Map<String, Object> key = new HashMap<String, Object>();
      key.put(Constants.MR_NO, mrNo);

      success = mrdCaseFileAttrService.update(mrdfile, key) > 0;
    }
    return success;
  }

  /**
   * Sets the complaint and encounter type.
   *
   * @param visitDetailsBean the new complaint and encounter type
   */
  @SuppressWarnings("rawtypes")
  private void setComplaintAndEncounterType(BasicDynaBean visitDetailsBean) {
    /* Default Encounter Start type and Encounter End type For Op */
    Map regPrefs = regPrefService.getRegistrationPreferences().getMap();
    String encounterStartType = (String) regPrefs.get("default_op_encounter_start_type");
    String encounterEndType = (String) regPrefs.get("default_op_encounter_end_type");
    visitDetailsBean.set("encounter_start_type",
        (encounterStartType != null && !encounterStartType.equals(""))
            ? new Integer(encounterStartType)
            : null);
    visitDetailsBean.set("encounter_end_type",
        (encounterEndType != null && !encounterEndType.equals("")) ? new Integer(encounterEndType)
            : null);

    /*
     * Copy chief complaint & encounter types from previous visit(same episode) to
     * episode follow-up
     */
    copyComplaintAndEncounterTypes(visitDetailsBean);
  }

  /**
   * Copy complaint and encounter types.
   *
   * @param visitDetailsBean the visit details bean
   */
  private void copyComplaintAndEncounterTypes(BasicDynaBean visitDetailsBean) {
    String opType = (String) visitDetailsBean.get(OP_TYPE);

    if (opType != null && (opType.equals(OP_TYPE_FOLLOWUP) || opType.equals("D"))) {

      String mainVisitId = (String) visitDetailsBean.get("main_visit_id");
      List<BasicDynaBean> episodePreviousVisitDetails = patientRegistrationRepository
          .listPreviousVisitsForMainVisit(mainVisitId);
      String latestEpisodeVisitId = null;

      if (episodePreviousVisitDetails != null && !episodePreviousVisitDetails.isEmpty()) {
        latestEpisodeVisitId = (String) episodePreviousVisitDetails.get(0)
            .get(Constants.PATIENT_ID);
      }

      BasicDynaBean previousVisitBean = patientRegistrationRepository
          .findByKey(Constants.PATIENT_ID, latestEpisodeVisitId);
      if (previousVisitBean != null) {
        visitDetailsBean.set("encounter_start_type", previousVisitBean.get("encounter_start_type"));
        visitDetailsBean.set("encounter_end_type", previousVisitBean.get("encounter_end_type"));
        visitDetailsBean.set("complaint", previousVisitBean.get("complaint"));
      }
    }

  }

  /**
   * Process visit paramsto bean.
   *
   * @param visitBean   the visit bean
   * @param patientBean the patient bean
   * @param params      the params
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("unchecked")
  private void processVisitParamstoBean(BasicDynaBean visitBean, BasicDynaBean patientBean,
      Map<String, Object> params) throws ParseException {
    if (visitBean == null || params == null) {
      return;
    }

    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");

    boolean notAllowBackDate = "N".equals(actionRightsMap.get("allow_backdate"));
    
    if ("".equals(getValue("reg_date", params)) || notAllowBackDate) {
      visitBean.set("reg_date", DateUtil.parseDate((String) getValue("system_date", params)));
    } else {
      visitBean.set("reg_date", DateUtil.parseDate((String) getValue("reg_date", params)));
    }
    
    DateUtil dateUtil = new DateUtil();
    if ("".equals(getValue("reg_time", params)) || notAllowBackDate) {
      visitBean.set("reg_time", dateUtil.parseTheTime((String) getValue("system_time", params)));
    } else {
      visitBean.set("reg_time", dateUtil.parseTheTime((String) getValue("reg_time", params)));
    }

    visitBean.set(STATUS, "A");

    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes(new String[] { USER_ID, CENTER_ID, SAMPLE_COL_CEN_ID });
    visitBean.set("user_name", sessionAttributes.get(USER_ID));
    visitBean.set("created_by", sessionAttributes.get(USER_ID));
    visitBean.set(CENTER_ID1, StringUtils.isEmpty(visitBean.get("center_id"))
        ? sessionAttributes.get(CENTER_ID) : visitBean.get("center_id"));
    visitBean.set("collection_center_id", StringUtils.isEmpty(visitBean.get("collection_center_id"))
        ? sessionAttributes.get(SAMPLE_COL_CEN_ID) : visitBean.get("collection_center_id"));
    String orgId = getParamDefault(params, Constants.ORG_ID, DEFAULT_ORG);
    visitBean.set(Constants.ORG_ID, orgId);
    visitBean.set("admitted_dept", visitBean.get("dept_name"));

    if (visitBean.get("ward_id") == null) {
      visitBean.set("ward_id", "");
    }
    visitBean.set("patient_category_id", patientBean.get("patient_category_id"));

  }

  /**
   * Insert patient demography.
   *
   * @param params         the params
   * @param patDetailsBean the pat details bean
   */
  @SuppressWarnings({ "unchecked" })
  @Transactional(rollbackFor = Exception.class)
  public void insertPatientDemography(Map<String, Object> params, BasicDynaBean patDetailsBean) {
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    String mrNo = (String) getValue(Constants.MR_NO, patientParams);
    String userMrNo = (String) getValue("user_provided_mr_no", patientParams);
    if ((Boolean) patDetailsBean.get(MOBILE_ACCESS)
        && (patDetailsBean.get("mobile_password") == null
            || "".equals(patDetailsBean.get("mobile_password")))) {
      patDetailsBean.set("mobile_password", RandomGeneration.randomGeneratedPassword(6));
    }

    /* Bug 20326, 20398 */
    String caseFileNo = (String) getValue("casefile_no", patientParams);
    String oldRegAutoGenerate = (String) getValue("old_reg_auto_generate", patientParams);
    setCaseFileNo(patDetailsBean, caseFileNo, oldRegAutoGenerate);
    if (mrNo == null || mrNo.isEmpty()) {
      if (userMrNo == null || userMrNo.isEmpty()) {
        mrNo = generateMrNo(patDetailsBean);
        patDetailsBean.set(Constants.MR_NO, mrNo);
      } else {
        patDetailsBean.set(Constants.MR_NO, userMrNo);
        patDetailsBean.set("resource_captured_from", "uhid");
      }
      patientDetailsService.insert(patDetailsBean);
    } else {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put(Constants.MR_NO, mrNo);
      patientDetailsService.update(patDetailsBean, keys);
    }

    if (patDetailsBean.get("patient_area") != null
        && !patDetailsBean.get("patient_area").toString().equals("")) {
      checkAndInsertArea(patDetailsBean.get("patient_city").toString(),
          patDetailsBean.get("patient_area").toString());
    }
    List<String> activatedModules = securityService.getActivatedModules();
    if (activatedModules != null && activatedModules.contains("mod_messaging")) {
      String sendSMS = patientParams.get("send_sms") != null
          ? (String) patientParams.get("send_sms")
          : "N";
      String sendEmail = patientParams.get("send_email") != null
          ? (String) patientParams.get("send_email")
          : "N";
      patientCommunicationService.convetAndSavePatientCommPreference(mrNo, sendSMS, sendEmail,
          (String) patientParams.get("preferred_language"));
    }
  }

  /**
   * Process patient params to bean.
   *
   * @param bean   the bean
   * @param params the params
   */
  private void processPatientParamsToBean(BasicDynaBean bean, Map<String, Object> params) {
    if (bean == null || params == null) {
      return;
    }
    String userName = "";
    if (RequestContext.getHttpRequest() != null) {
      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      userName = (String) sessionAttributes.get(USER_ID);
    } else {
      userName = (String) ((params.get("user_name") != null) ? params.get("user_name") : "");
    }

    // when reg_name contains four parts we r appending the 3rd part to 2nd
    // part and saving in DB
    String middleName = null;
    String middleName1 = (String) getValue("middle_name", params);
    String middleName2 = (String) getValue("middle_name2", params);
    if (middleName2 != null && !middleName2.equals("")) {
      middleName = middleName1 + " " + middleName2;
    } else {
      middleName = middleName1;
    }
    // patientDetailsBean is set explicitly just to insert empty string when
    // it is not entered.
    // above copytoDynaBean nullifies all the empty strings. we don't want
    // to nullify the patient name fields.
    bean.set("patient_name", getValue("patient_name", params));
    bean.set("middle_name", middleName);
    bean.set("last_name", getValue("last_name", params));
    bean.set("user_name", userName);
    bean.set("original_mr_no", getValue("original_mr_no", params));
    bean.set("patient_state", getValue("patient_state", params));
    bean.set("patient_city", getValue("patient_city", params));
    bean.set("patient_address", getValue("patient_address", params));
    String areaId = ((String) getValue("patient_area", params)).trim();
    bean.set("patient_area", areaId);
    Map keyMap = new HashMap();
    keyMap.put(AREA_ID, areaId);
    BasicDynaBean areaBean = areaService.findByPk(keyMap);
    if (areaBean != null) {
      bean.set("patient_area", String.valueOf(areaBean.get("area_name")));
    }
    bean.set(RELATION, getValue(RELATION, params));
    bean.set("med_allergies", getValue("med_allergies", params));
    bean.set("food_allergies", getValue("food_allergies", params));
    bean.set("other_allergies", getValue("other_allergies", params));
    bean.set(PATIENT_C_OCC, getValue(PATIENT_C_OCC, params));
    if ("true".equals(getValue("portal_access", params))) {
      bean.set("portal_access", true);
    } else {
      bean.set("portal_access", false);
    }
    if ("true".equals(getValue(MOBILE_ACCESS, params))) {
      bean.set(MOBILE_ACCESS, true);
    } else {
      bean.set(MOBILE_ACCESS, false);
    }
  }

  /**
   * Generate mr no.
   *
   * @param bean the bean
   * @return the string
   */
  private String generateMrNo(BasicDynaBean bean) {
    String mrNo = null;
    // TODO: Chetan uses databaseHelper to get next pattern ID.
    bean.set("first_visit_reg_date", DateUtil.getCurrentDate());
    if (!"1".equals(bean.get("patient_category_id").toString())) {
      Map<String, Object> categoryMap = new HashMap<String, Object>();
      categoryMap.put("category_id", (Integer) bean.get("patient_category_id"));
      BasicDynaBean categoryBean = patientCategoryService.findByPk(categoryMap);

      String categoryCode = null;
      if (categoryBean.get("code") != null) {
        categoryCode = categoryBean.get("code").toString();
      }
      String isSeperateSeqRequire = categoryBean.get("seperate_num_seq").toString();

      if (categoryCode != null && "Y".equals(isSeperateSeqRequire)) {
        mrNo = DatabaseHelper.getNextPatternId(categoryCode);
      } else {
        mrNo = DatabaseHelper.getNextPatternId("MRNO");
      }
    } else {
      mrNo = DatabaseHelper.getNextPatternId("MRNO");
    }

    return mrNo;
  }

  /**
   * Gets the patient details for new visit.
   *
   * @param mrNo the mr no
   * @return the patient details for new visit
   */
  public Map<String, Object> getPatientDetailsForNewVisit(String mrNo) {
    if (mrNo == null || mrNo.equals("")) {
      return null;
    }
    Map<String, Object> patientData = new HashMap<String, Object>();
    patientData.put("patient", getPatientDemography(mrNo));
    Integer currentCenterId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    // Get previous prescriptions, doctor and dues.
    String defaultVisitDetailsAcrossCenter = (String) regPrefService.getRegistrationPreferences()
        .get("default_visit_details_across_center");
    if (defaultVisitDetailsAcrossCenter.equals("Y")) {
      currentCenterId = null;
    }
    BasicDynaBean latestVisitBean = getPatientLatestVisit(mrNo, null, "o", currentCenterId);
    if (latestVisitBean == null) {
      latestVisitBean = getPatientLatestVisit(mrNo, null, "i", currentCenterId);
    }

    BasicDynaBean patientDetails = null;
    String familyId = (String) ((Map) patientData.get("patient")).get("family_id");
    Object recentGenRegChargePostedDate = "";
    List<Map> planDetails = new ArrayList<>();
    Map<String, Object> previousPrescriptionsMap = new HashMap<>();
    if (latestVisitBean != null) {
      String latestVisitId = (String) latestVisitBean.get("patient_id");
      patientDetails = patientRegistrationRepository
          .getPatientVisitDetailsBean(latestVisitId);      
      previousPrescriptionsMap.putAll(getPreviousPrescriptions(
          (String) patientDetails.get("mr_no")));
      
      Map<String, Map<String, Object>> listVisitPreAuthPrescriptions = new HashMap<>();
      listVisitPreAuthPrescriptions = getVisitWisePriorauthItems(latestVisitId);
      previousPrescriptionsMap.putAll(listVisitPreAuthPrescriptions);      
      
      familyId = (String) patientDetails.get("family_id");
      planDetails = getPreviousVisitPatientPlanDetails(mrNo, latestVisitId);
      BasicDynaBean regDateBean = patientDetailsRepository
          .getPreviousRegDateChargeAccepted(new Object[] { mrNo });
      recentGenRegChargePostedDate = regDateBean != null ? regDateBean.get("visit_reg_date") : "";
    }

    List<String> activatedModules = securityService.getActivatedModules();
    String modAdvancedPackagesActive = "N";

    if (activatedModules != null && activatedModules.contains("mod_adv_packages")) {
      modAdvancedPackagesActive = "Y";
    }

    if (activatedModules.contains("mod_sync_external_patient")) {
      BasicDynaBean prepopulateInfo = prepopulateVisitInfoRepository.getPrepopulateVisitInfo(mrNo);
      Map prepopulateInfoJSON = null;
      if (prepopulateInfo != null && prepopulateInfo.get("visit_values") != null) {
        String prepopulateInfoString = (String) prepopulateInfo.get("visit_values");
        ObjectMapper om = new ObjectMapper();
        if (prepopulateInfoString != null) {
          try {
            prepopulateInfoJSON = om.readValue(prepopulateInfoString, Map.class);
          } catch (JsonParseException exc) {
            exc.printStackTrace();
          } catch (JsonMappingException exc) {
            exc.printStackTrace();
          } catch (IOException exc) {
            exc.printStackTrace();
          }
        }
      }
      patientData.put("prepopulate_visit_info",
          prepopulateInfoJSON == null ? new HashMap<String, Object>() : prepopulateInfoJSON);
    }

    List<BasicDynaBean> multiVisitPackageIds = new ArrayList<BasicDynaBean>();
    Map<Object, Object> multiVisitPackageComponentDetails = new HashMap<Object, Object>();
    Map<Object, Object> multiVisitPackageConsumedDetails = new HashMap<Object, Object>();

    if (modAdvancedPackagesActive.equals("Y")) {
      multiVisitPackageIds = packageOrderItemService
          .getMultiVisitPackageBeans(mrNo);
      for (int i = 0; i < multiVisitPackageIds.size(); i++) {
        BasicDynaBean packages = multiVisitPackageIds.get(i);
        Integer packageId = (Integer) packages.get("package_id");
        multiVisitPackageComponentDetails.put(packageId,
            ConversionUtils.copyListDynaBeansToMap(
                packagesService.getPackageComponents(packageId)));
        multiVisitPackageConsumedDetails.put(packageId,
            ConversionUtils.copyListDynaBeansToMap(multiVisitPackageService
                .getOrderedPackageItems(mrNo)));
      }
    }

    patientData.put("multi_visit_package_ids",
        ConversionUtils.copyListDynaBeansToMap(multiVisitPackageIds));
    patientData.put("multi_visit_package_component_details", multiVisitPackageComponentDetails);
    patientData.put("multi_visit_package_consumed_details", multiVisitPackageConsumedDetails);
    int centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    patientData.put("recent_gen_reg_charge_posted_date", recentGenRegChargePostedDate);
    patientData.put("previous_visit_dues", ConversionUtils.listBeanToListMap(
        patientRegistrationRepository.getAllPrevVisitBillWiseDues(mrNo, centerId)));

    BasicDynaBean familyBillTotal = null;
    if (familyId != null && !familyId.equals("")) {
      familyBillTotal = patientRegistrationRepository.getPatientFamilyBillsTotal(familyId);
    }
    patientData.put("patient_family_bills_total",
        familyBillTotal != null ? familyBillTotal.get("total_amount") : null);
    patientData.put("previous_prescriptions", previousPrescriptionsMap);
    patientData.put("previous_visit", patientDetails == null ? null : patientDetails.getMap());

    List<BasicDynaBean> apptList = new ArrayList<BasicDynaBean>();

    List<BasicDynaBean> patientAppointmentDetails = resourceService.getTodaysOPAppointments(mrNo,
        centerId);
    if (patientAppointmentDetails != null && patientAppointmentDetails.size() > 0) {
      apptList.addAll(patientAppointmentDetails);
    }
    patientData.put("todays_appointments", ConversionUtils.listBeanToListMap(apptList));
    patientData.put(INSURANCE, planDetails);

    return patientData;
  }

  /**
   * Gets the contact patient details for new visit.
   *
   * @param contactId the contact id
   * @return the contact patient details for new visit
   */
  public Map<String, Object> getContactPatientDetailsForNewVisit(Integer contactId) {
    Map<String, Object> appointmentData = new HashMap<>();
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    List<BasicDynaBean> apptList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> contactPatientAppointmentDetails = resourceService
        .getTodaysOPAppointments(contactId, centerId);
    if (contactPatientAppointmentDetails != null && contactPatientAppointmentDetails.size() > 0) {
      apptList.addAll(contactPatientAppointmentDetails);
    }
    appointmentData.put("todays_appointments", ConversionUtils.listBeanToListMap(apptList));
    return appointmentData;
  }
  
  /**
   * Gets the previous prescriptions.
   *
   * @param mrNo the mr no
   * @return the previous prescriptions
   */
  public Map<String, Object> getPreviousPrescriptions(String mrNo) {
    Map<String, Object> previousPrescriptions = new HashMap<>();
    List<BasicDynaBean> previousVisitDetails = patientRegistrationRepository
        .listPreviousConsultations(mrNo, true);
    if (previousVisitDetails != null && !previousVisitDetails.isEmpty()) {
      for (BasicDynaBean bean : previousVisitDetails) {
        List<BasicDynaBean> previousTestPrescriptions = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> previousServicePrescriptions = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> previousConsultationPrescriptions = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> previousOperationPrescriptions = new ArrayList<BasicDynaBean>();
        Integer consultationId = (Integer) bean.get(Constants.CONSULTATION_ID);
        List<BasicDynaBean> ltest = testPrescService.getPrescriptionsByConsultation(consultationId);

        if (ltest != null && ltest.size() > 0) {
          previousTestPrescriptions.addAll(ltest);
        }

        List<BasicDynaBean> lserv = servicePrescService
            .getPrescriptionsByConsultation(consultationId);
        if (lserv != null && lserv.size() > 0) {
          previousServicePrescriptions.addAll(lserv);
        }

        List<BasicDynaBean> lcons = consultationPrescService
            .getPrescriptionsByConsultation(consultationId);
        if (lcons != null && lcons.size() > 0) {
          previousConsultationPrescriptions.addAll(lcons);
        }

        List<BasicDynaBean> lops = operationPrescService
            .getPrescriptionsByConsultation(consultationId);
        if (lops != null && lops.size() > 0) {
          previousOperationPrescriptions.addAll(lops);
        }

        String visitId = (String) bean.get(Constants.PATIENT_ID);
        Map<String, Object> map = new HashMap<>();
        map.put("previous_service_prescriptions",
            ConversionUtils.listBeanToListMap(previousServicePrescriptions));
        map.put("previous_test_prescriptions",
            ConversionUtils.listBeanToListMap(previousTestPrescriptions));
        map.put("previous_consultation_prescriptions",
            ConversionUtils.listBeanToListMap(previousConsultationPrescriptions));
        map.put("previous_operation_prescriptions",
            ConversionUtils.listBeanToListMap(previousOperationPrescriptions));
        map.put("doctor", bean.get("doctor_name"));
        map.put("primary_sponsor_name", bean.get("primary_sponsor_name"));
        map.put("secondary_sponsor_name", bean.get("secondary_sponsor_name"));
        map.put("prescription_date", bean.get("prescription_date"));
        map.put("visitId", visitId);
        map.put("visitedDate", bean.get("visited_date"));
        previousPrescriptions.put(consultationId.toString(), map);
      }
    }
    return previousPrescriptions;
  }

  /**
   * Gets the previous visit patient plan details.
   *
   * @param mrNo    the mr no
   * @param visitId the visit id
   * @return the previous visit patient plan details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<Map> getPreviousVisitPatientPlanDetails(String mrNo, String visitId) {
    List<BasicDynaBean> planBeanList = patientRegistrationRepository
        .getPreviousVisitPatientPlanDetails(mrNo, visitId);
    List<Map> mapList = new ArrayList<Map>();
    for (int i = 0; i < planBeanList.size(); i++) {
      BasicDynaBean bean = planBeanList.get(i);
      Map<String, Object> map = new HashMap<String, Object>(bean.getMap());
      map.put("insurance_plan_details",
          ConversionUtils.listBeanToListMap(patientInsurancePlanDetailsService
              .getPreviousPlanDetails((Integer) bean.get("plan_id"), "o", visitId)));
      mapList.add(map);
    }
    return mapList;
  }

  /**
   * Gets the patient latest visit.
   *
   * @param mrNo      the mr no
   * @param active    the active
   * @param visitType the visit type
   * @param centerId  the center id
   * @return the patient latest visit
   */
  public BasicDynaBean getPatientLatestVisit(String mrNo, Boolean active, String visitType,
      Integer centerId) {
    return patientRegistrationRepository.getPatientLatestVisit(mrNo, active, visitType, centerId);
  }

  /**
   * Gets the patient demography.
   *
   * @param mrNo the mr no
   * @return the patient demography
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map getPatientDemography(String mrNo) {
    if (mrNo != null && mrNo != "") {
      BasicDynaBean patientBean = patientDetailsService.getPatientDetailsDisplayBean(mrNo);
      if (patientBean == null) {
        throw new EntityNotFoundException(new String[] { "patient", "MR NO", mrNo });
      }
      Map patientBeanMap = new HashMap(patientBean.getMap());
      // RC : TransformNullFields & TransformDateFields should be implemented,
      // somehow
      if (patientBeanMap.get("portal_access") != null
          && (Boolean) patientBeanMap.get("portal_access")) {
        patientBeanMap.put("portal_access", "true");
      } else {
        patientBeanMap.put("portal_access", "false");
      }
      patientBeanMap.put(MOBILE_ACCESS, "false");
      patientBeanMap.put("preferred_language", patientBeanMap.get("lang_code"));
      patientBeanMap.put("old_reg_auto_generate", "Y");
      patientBeanMap.put("raise_case_file_indent", "Y");
      patientBeanMap.put("middle_name2", "");
      String middleName = (String) patientBeanMap.get("middle_name");
      Map regPref = regPrefService.getRegistrationPreferences().getMap();
      if ((Integer) regPref.get("name_parts") == 4) {
        if (middleName != null && !middleName.equals("")) {
          int indexOfSpace = middleName.indexOf(" ");
          if (indexOfSpace >= 0) {
            patientBeanMap.put("middle_name", middleName.substring(0, indexOfSpace));
            patientBeanMap.put("middle_name2",
                middleName.substring(indexOfSpace + 1, middleName.length()));
          }
        }
      }

      return patientDetailsMapper.map(patientBeanMap);
    }
    return null;
  }

  /**
   * Gets the patient visit info.
   *
   * @param patientId the patient id
   * @return the patient visit info
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map getPatientVisitInfo(String patientId) {
    BasicDynaBean patientBean = patientRegistrationRepository.getPatientVisitDetailsBean(patientId);
    if (patientBean == null) {
      throw new EntityNotFoundException(new String[] { "patient", "visit id", patientId });
    }
    boolean precise = (patientBean.get("dateofbirth") != null);
    if (patientBean.get("expected_dob") != null) {
      patientBean.set("age_text",
          DateUtil.getAgeText((java.sql.Date) patientBean.get("expected_dob"), precise));
    }
    Map visitBeanMap = new HashMap(patientBean.getMap());
    BasicDynaBean billAndPaymentStatus = billService.getBillAndPaymentStatus(patientId,
        (String) patientBean.get("visit_type"));
    BasicDynaBean firstBillForVisit = billService.getFirstBillForVisit(patientId);
    if (billAndPaymentStatus == null) {
      visitBeanMap.put("bill_and_payment_status", Collections.EMPTY_MAP);
    } else {
      visitBeanMap.put("bill_and_payment_status", billAndPaymentStatus.getMap());
    }
    if (firstBillForVisit != null) {
      visitBeanMap.put("first_bill_for_visit", firstBillForVisit.get("bill_no"));
    }
    // Added bill_type
    visitBeanMap.put(BILL_TYPE, billService.getBillType(patientId));
    List<BasicDynaBean> list = mlcDocService.listPatientMLCTemplate(patientId);
    if (list != null && list.size() > 0) {
      String value = list.get(0).get("template_id") + "," + list.get(0).get("doc_format");
      visitBeanMap.put("mlc_template", value);
    }

    return patientRegistrationMapper.map(visitBeanMap);
  }

  /**
   * Gets the patient insurance details.
   *
   * @param visitId the visit id
   * @return the patient insurance details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Map> getPatientInsuranceDetails(String visitId) {
    List<BasicDynaBean> planBeanList = patientInsurancePlansService
        .getPatientInsuranceDetails(visitId);
    List<Map> mapList = new ArrayList<Map>();
    for (int i = 0; i < planBeanList.size(); i++) {
      BasicDynaBean bean = planBeanList.get(i);
      Map<String, Object> map = new HashMap<String, Object>(bean.getMap());
      map.put("insurance_plan_details",
          ConversionUtils.listBeanToListMap(
              patientInsurancePlanDetailsService.getPlanDetails((Integer) bean.get("plan_id"),
                  (String) bean.get("visit_type"), (String) bean.get("visit_id"))));
      mapList.add(map);
    }
    return mapList;
  }

  /**
   * Update patient visit.
   *
   * @param params the params
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws ParseException           the parse exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public void updatePatientVisit(Map<String, Object> params)
      throws IOException, NoSuchAlgorithmException, ParseException {
    Map<String, Object> nestedException = new HashMap<String, Object>();
    try {
      updatePatientDemography(params);
    } catch (ValidationException ex) {
      nestedException.put("patient", ex.getErrors());
    }
    try {
      updatePatientVisitInfo(params);
    } catch (ValidationException ex) {
      nestedException.put(VISIT, ex.getErrors());
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    sendADTMessageOnUpdate(params);
    
    triggerEditVisitAndEditPatientEvents(params);
  }
  
  /**
   * Hl7 implementation for malaffi.
   * 
   * @param params the param
   */
  private void triggerEditVisitAndEditPatientEvents(Map<String, Object> params) {
    Map<String, Object> patientMap = (Map<String, Object>) params.get("patient");
    Map<String, Object> visitMap = (Map<String, Object>) params.get(VISIT);

    if (visitMap != null && !visitMap.isEmpty()) {
      if (!StringUtils.isEmpty(visitMap.get("close")) && "Y".equals(visitMap.get("close"))) {
        interfaceEventService.visitCloseEvent((String) visitMap.get("patient_id"), null);
      } else {
        interfaceEventService.editVisitEvent((String) visitMap.get("patient_id"));
      }
    } else if (patientMap != null && !patientMap.isEmpty()) {
      interfaceEventService.editPatientEvent(null, (String) patientMap.get("visit_id"));
    }
  }

  /**
   * Send ADT message on update.
   *
   * @param params the params
   */
  private void sendADTMessageOnUpdate(Map<String, Object> params) {
    Map<String, Object> patientMap = (Map<String, Object>) params.get("patient");
    Map<String, Object> visitMap = (Map<String, Object>) params.get(VISIT);

    Map<String, Object> adtData = new HashMap<>();

    if ((patientMap != null && !patientMap.isEmpty()) && patientMap.get("original_mr_no") != null
        && !((String) patientMap.get("original_mr_no")).isEmpty()) {
      adtData.put("patient_id", patientMap.get("patient_id"));
      adtData.put(Constants.MR_NO, (String) patientMap.get("original_mr_no"));
      adtData.put("old_mr_no", patientMap.get(Constants.MR_NO));
      adtService.createAndSendADTMessage("ADT_18", adtData);
    } else if (visitMap != null && !visitMap.isEmpty()) {
      adtData.put("patient_id", visitMap.get("patient_id"));
      adtData.put(Constants.MR_NO, (String) visitMap.get(Constants.MR_NO));
      adtService.createAndSendADTMessage("ADT_08", adtData);
    } else if (patientMap != null && !patientMap.isEmpty()) {
      adtData.put("patient_id", patientMap.get("visit_id"));
      adtData.put(Constants.MR_NO, (String) patientMap.get(Constants.MR_NO));
      adtService.createAndSendADTMessage("ADT_08", adtData);
    }

  }

  /**
   * Update insurance information edited on registration screen. Currently only
   * supports updation of Eligibility Authorization related fields.
   *
   * @param params the params
   */
  private void updateInsuranceInfo(Map<String, Object> params) {
    // update eligibility Authorization information
    ArrayList insuranceList = (ArrayList) params.get(INSURANCE);
    updatePatientPolicyDetails(insuranceList);
  }

  /**
   * Update Patient Policy Details.
   *
   * @param insuranceList the insurance list
   */
  private void updatePatientPolicyDetails(ArrayList insuranceList) {
    for (int i = 0; i < insuranceList.size(); i++) {
      Map insParams = (Map) insuranceList.get(i);
      BasicDynaBean policyDetailsBean = patientInsurancePolicyDetailsService.getBean();
      policyDetailsBean.set("eligibility_reference_number",
          insParams.get("eligibility_reference_number"));
      policyDetailsBean.set("eligibility_authorization_remarks",
          insParams.get("eligibility_authorization_remarks"));
      policyDetailsBean.set("eligibility_authorization_status",
          insParams.get("eligibility_authorization_status"));
      policyDetailsBean.set("patient_policy_id", insParams.get("patient_policy_id"));
      patientInsurancePolicyDetailsService.update(policyDetailsBean);
    }
  }

  /**
   * Update patient visit info.
   *
   * @param params the params
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public void updatePatientVisitInfo(Map<String, Object> params) throws IOException {
    Map<String, Object> visitParams = (Map<String, Object>) params.get(VISIT);
    String visitId = (String) getValue(Constants.PATIENT_ID, visitParams);
    List<String> errors = new ArrayList<String>();
    updateInsuranceInfo(params);

    BasicDynaBean existingVisitBean = patientRegistrationRepository.findByKey(Constants.PATIENT_ID,
        visitId);
    if (existingVisitBean == null) {
      throw new EntityNotFoundException(new String[] { "patient", "visit id", visitId });
    }
    BasicDynaBean editVisitBean = patientRegistrationRepository.getBean();

    ConversionUtils.copyJsonToDynaBean(visitParams, editVisitBean, errors, true);

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }

    String orgid = (String) existingVisitBean.get(Constants.ORG_ID);
    String mrNo = (String) existingVisitBean.get(Constants.MR_NO);

    String orgId = DEFAULT_ORG;
    if (orgid != null && !orgid.equals("")) {
      orgId = orgid;
    }
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get(USER_ID);
    editVisitBean.set("user_name", userName);
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    editVisitBean.set("patient_care_oftext", getValue("patient_care_oftext", patientParams));
    editVisitBean.set("patient_careof_address", getValue("patient_careof_address", patientParams));
    editVisitBean.set(PATIENT_C_OCC, getValue(PATIENT_C_OCC, patientParams));
    editVisitBean.set(RELATION, getValue(RELATION, patientParams));
    editVisitBean.set("classification", getValue("classification", visitParams));

    String existingMainVisitId = (String) existingVisitBean.get("main_visit_id");
    String existingOpType = (String) existingVisitBean.get(OP_TYPE);
    String newOpType = (String) getValue(OP_TYPE, visitParams);

    if (existingOpType != null && !existingOpType.equals(newOpType)) {
      String newMainVisitId = editVisitBean.get("main_visit_id") != null
          ? (String) editVisitBean.get("main_visit_id")
          : null;
      // Set Main visit Id and Op type
      editVisitBean.set("main_visit_id", newMainVisitId);
      editVisitBean.set(OP_TYPE, newOpType);
      if ("M".equals(newOpType) || "R".equals(newOpType)) {
        sectionFormService.updateFormTypeOnConsultationTypeChange(newMainVisitId, "Form_CONS",
            "Form_OP_FOLLOW_UP_CONS");
      } else if ("F".equals(newOpType)) {
        sectionFormService
            .updateFormTypeOnConsultationTypeChange(newMainVisitId, "Form_OP_FOLLOW_UP_CONS",
                "Form_CONS");
      }
    } else {
      editVisitBean.set(OP_TYPE, existingOpType);
      editVisitBean.set("main_visit_id", existingMainVisitId);
    }
    editVisitBean.set("analysis_of_complaint", getValue("analysis_of_complaint", visitParams));

    ValidationErrorMap validationErrors = new ValidationErrorMap();

    // ACL Check. throws accessdeniedexception is acl fails.
    registrationACL.authenticateVisitInfo(editVisitBean, existingVisitBean);

    // Validations.
    if (!registrationValidator.validatePatientVisitInfoUpdate(editVisitBean, existingVisitBean,
        (String) getValue("patient_gender", patientParams), validationErrors)) {
      throw new ValidationException(validationErrors);
    }

    // int billsCount = 0;
    String billNo = null;

    // TODO: Chetan MLCDOCService.
    List<BasicDynaBean> visitMLCTemplateList = mlcDocService.listPatientMLCTemplate(visitId);
    BasicDynaBean visitMLCTemplateBean = null;
    if (visitMLCTemplateList != null && visitMLCTemplateList.size() > 0) {
      visitMLCTemplateBean = visitMLCTemplateList.get(0);
    }
    String existingMlcDocIdFormat = null;
    if (visitMLCTemplateBean != null) {
      existingMlcDocIdFormat = visitMLCTemplateBean.get("template_id") + ","
          + visitMLCTemplateBean.get("doc_format");
    }
    String templateIdFormat = getValue("mlc_template", visitParams).toString();
    if (existingMlcDocIdFormat != null && !existingMlcDocIdFormat.equals("")
        && templateIdFormat != null && !existingMlcDocIdFormat.equals(templateIdFormat)) {
      // Remove MLC
      deleteMLC(visitId, userName);
      existingMlcDocIdFormat = null;
    }

    if ((existingMlcDocIdFormat == null || existingMlcDocIdFormat.equals(""))
        && templateIdFormat != null && !templateIdFormat.equals("")) {

      editVisitBean.set("mlc_status", "Y");
      if (editVisitBean.get("mlc_no") == null
          || ((String) editVisitBean.get("mlc_no")).equals("")) {
        // TODO: Chetan. Need to change this.
        editVisitBean.set("mlc_no", DatabaseHelper.getNextPatternId("MLCNO"));
      }
      String templatename = (String) getValue("mlc_template_name", visitParams);
      // Add MLC
      String bedType = (String) existingVisitBean.get("bed_type");
      Boolean isInsurance = existingVisitBean.get("primary_sponsor_id") != null
          && !((String) existingVisitBean.get("primary_sponsor_id")).isEmpty();

      int[] planIds = patientInsurancePlansService.getPlanIds(visitId);

      createMLC(mrNo, visitId, orgId, bedType, templateIdFormat, templatename, billNo, userName,
          isInsurance, planIds);
    }

    // ---- changed the order so that only one database update query is
    // executed.
    /* Discharge Patient or Close Visit */
    String dischargeOrClose = (String) getValue("close", visitParams);
    if (dischargeOrClose.equals("Y")) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(Constants.MR_NO, mrNo);
      map.put(Constants.PATIENT_ID, visitId);
      map.put("user_name", userName);
      map.put("discharge_time", DateUtil.getCurrentTime());
      map.put("discharge_date", DateUtil.getCurrentDate());
      closeVisit(map, editVisitBean);
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put(Constants.PATIENT_ID, visitId);
    BasicDynaBean bean = patientRegistrationRepository.findByKey(Constants.PATIENT_ID, visitId);
    editVisitBean.set("doc_id", bean.get("doc_id"));
    patientRegistrationRepository.update(editVisitBean, keys);

    BasicDynaBean mlcVisitIdBean = patientRegistrationRepository.getMlcStatusVisitId(mrNo);
    String mlcVisitId = mlcVisitIdBean != null ? (String) mlcVisitIdBean.get(Constants.PATIENT_ID)
        : null;
    BasicDynaBean patDetailsBean = patientDetailsService.getBean();
    if (mlcVisitId != null && !mlcVisitId.equals("")) {
      patDetailsBean.set("first_mlc_visitid", mlcVisitId);
    } else {
      patDetailsBean.set("first_mlc_visitid", null);
    }

    keys = new HashMap<String, Object>();
    keys.put(Constants.MR_NO, mrNo);

    patientDetailsService.update(patDetailsBean, keys);
  }

  /**
   * Creates the MLC.
   *
   * @param mrno             the mr no
   * @param visitId          the visit id
   * @param orgId            the org id
   * @param bedType          the bed type
   * @param templateidformat the template id format
   * @param templatename     the template name
   * @param billNo           the bill no
   * @param userName         the user name
   * @param isInsurance      the is insurance
   * @param planIds          the plan ids
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private boolean createMLC(String mrno, String visitId, String orgId, String bedType,
      String templateidformat, String templatename, String billNo, String userName,
      boolean isInsurance, int[] planIds) throws IOException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(Constants.MR_NO, mrno);
    map.put(Constants.PATIENT_ID, visitId);
    map.put(DOC_NAME, templatename);
    map.put("username", userName);
    map.put("mlc_template_id", templateidformat);

    boolean success = true;
    success = (Boolean) mlcDocStore.create(map).get(STATUS);
    return success;
  }

  /**
   * Delete MLC.
   *
   * @param visitId  the visit id
   * @param userName the user name
   * @return true, if successful
   */
  private boolean deleteMLC(String visitId, String userName) {
    boolean success = false;
    success = mlcDocService.deleteMLCDetails(visitId, userName);
    return success;
  }

  /**
   * Check patient details exists.
   *
   * @param params the params
   * @return the map
   * @throws ParseException the parse exception
   */
  public Map checkPatientDetailsExists(Map<String, Object> params) throws ParseException {
    List<BasicDynaBean> list = patientDetailsService.listExistingPatientDetails(params);
    return list.size() > 0 ? list.get(0).getMap() : null;
  }

  /**
   * Gets the patient doctor visits.
   *
   * @param mrNo     the mr no
   * @param doctorId the doctor id
   * @param opType   the op type
   * @return the patient doctor visits
   */
  @SuppressWarnings("rawtypes")
  public List getPatientDoctorVisits(String mrNo, String doctorId, String opType) {

    List<BasicDynaBean> previousDocVisits = null;

    if (opType != null && !opType.trim().equals("")) {
      previousDocVisits = getPatientPreviousVisits(mrNo, doctorId);
    } else {
      previousDocVisits = getPatientPreviousMainVisits(mrNo, doctorId);
    }
    return ConversionUtils.listBeanToListMap(previousDocVisits);
  }

  /**
   * Gets the patient visit's prepopulate info.
   *
   * @param mrNo the mr no
   * @return the patient visit prepopulate info
   */
  @SuppressWarnings("rawtypes")
  public String getPrePopulateVisitInfo(String mrNo) {

    BasicDynaBean prepopulateInfo = null;
    Map<String, Object> patientData = new HashMap<String, Object>();
    Map<String, String> prepopulateInfoJSON = new HashMap<String, String>();
    String prepopulateInfoString = null;
    List<String> activatedModules = securityService.getActivatedModules();
    if (activatedModules.contains("mod_sync_external_patient")) {
      prepopulateInfo = prepopulateVisitInfoRepository.getPrepopulateVisitInfo(mrNo);
      if (prepopulateInfo != null && prepopulateInfo.get("visit_values") != null) {
        prepopulateInfoString = (String) prepopulateInfo.get("visit_values");
      }
    }
    return prepopulateInfoString;
  }

  /**
   * Gets the patient previous main visits.
   *
   * @param mrNo   the mr no
   * @param doctor the doctor
   * @return the patient previous main visits
   */
  public List<BasicDynaBean> getPatientPreviousMainVisits(String mrNo, String doctor) {
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();
    // D - Doctor, S - Department/Speciality
    String visittypedependence = (String) regPrefs.get("visit_type_dependence");
    if (visittypedependence.equals("D")) {
      return patientRegistrationRepository.getPatientPreviousMainVisitsDoctor(mrNo, doctor);
    } else {
      return patientRegistrationRepository.getPatientPreviousMainVisitsDept(mrNo, doctor);
    }
  }

  /**
   * Gets the patient previous visits.
   *
   * @param mrNo   the mr no
   * @param doctor the doctor
   * @return the patient previous visits
   */
  public List<BasicDynaBean> getPatientPreviousVisits(String mrNo, String doctor) {
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();
    // D - Doctor, S - Department/Speciality
    String visittypedependence = (String) regPrefs.get("visit_type_dependence");
    if (visittypedependence.equals("D")) {
      return patientRegistrationRepository.getPatientPreviousVisitsDoctor(mrNo, doctor);
    } else {
      return patientRegistrationRepository.getPatientPreviousVisitsDept(mrNo, doctor);
    }
  }

  /**
   * Gets the patient previous visits by center.
   *
   * @param mrNo     the mr no
   * @param doctor   the doctor
   * @param centerId the center id
   * @return the patient previous visits by center
   */
  public List<BasicDynaBean> getPatientPreviousVisitsByCenter(String mrNo, String doctor,
      int centerId) {
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();
    // D - Doctor, S - Department/Speciality
    String visittypedependence = (String) regPrefs.get("visit_type_dependence");
    if (visittypedependence.equals("D")) {
      return patientRegistrationRepository.getPatientPreviousVisitsByDoctorAndCenter(mrNo, doctor,
          centerId);
    } else {
      return patientRegistrationRepository.getPatientPreviousVisitsByDeptAndCenter(mrNo, doctor,
          centerId);
    }
  }

  /**
   * Gets the previous main visits.
   *
   * @param mrNo   the mr no
   * @param doctor the doctor
   * @return the previous main visits
   */

  public List<BasicDynaBean> getPreviousMainVisits(String mrNo, String doctor) {
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();
    // D - Doctor, S - Department/Speciality
    String visittypedependence = (String) regPrefs.get("visit_type_dependence");
    if (visittypedependence.equals("D")) {
      return patientRegistrationRepository.getPreviousMainVisitsDoctor(mrNo, doctor);
    } else {
      return patientRegistrationRepository.getPreviousMainVisitsDept(mrNo, doctor);
    }
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientRegistrationRepository.getBean();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return patientRegistrationRepository.update(bean, keys);
  }

  /**
   * Close visit.
   *
   * @param map           the map
   * @param editVisitBean the edit visit bean
   * @return true, if successful
   */
  public boolean closeVisit(Map<String, Object> map, BasicDynaBean editVisitBean) {
    // editVisitBean cannot be empty.
    editVisitBean.set("user_name", map.get("user_name"));
    editVisitBean.set(STATUS, "I");
    editVisitBean.set("discharge_flag", "D");
    editVisitBean.set("patient_discharge_status", "D");
    editVisitBean.set("ready_to_discharge", "Y");
    editVisitBean.set("discharge_date", map.get("discharge_date"));
    editVisitBean.set("discharge_time", map.get("discharge_time"));
    editVisitBean.set("discharged_by", map.get("user_name"));
    boolean success = false;
    success = mrdCaseFileAttrService.setMRDCaseFileStatus((String) map.get(Constants.MR_NO),
        MRDCaseFileIndentService.MRD_CASE_FILE_STATUS_ON_DISCHARGE);
    // possible when when discharged from EditVisitDetails screen i.e,Force
    // Discharge
    if (editVisitBean.get("discharge_type_id") == null) {
      editVisitBean.set("discharge_type_id", 1);
    }

    return success;
  }

  /**
   * Update patient demography.
   *
   * @param params the params
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws ParseException           the parse exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public void updatePatientDemography(Map<String, Object> params)
      throws NoSuchAlgorithmException, IOException, ParseException {
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    List<String> errors = new ArrayList<String>();
    BasicDynaBean patDetailsBean = patientDetailsService.getBean();
    String mrno = (String) getValue(Constants.MR_NO, patientParams);
    BasicDynaBean existingPatientBean = patientDetailsService.getPatientDetailsDisplayBean(mrno);

    if (existingPatientBean == null) {
      throw new EntityNotFoundException(new String[] { "patient", "MR NO", mrno });
    }
    ConversionUtils.copyJsonToDynaBean(patientParams, patDetailsBean, errors, true);

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }

    processPatientParamsToBean(patDetailsBean, patientParams);

    int defaultCategory = 1;
    defaultCategory = (Integer) (existingPatientBean.get("patient_category_id"));
    String dateOfBirth = (String) getValue("dateofbirth", patientParams);

    /*
     * Calculate the patient current age from the visit age and set into patient
     * params, if the user has modified the age and age in fields.
     */
    Map<String, Object> visitParams = (Map<String, Object>) params.get(VISIT);
    calculateAgeFromVisitAge(visitParams, patientParams, existingPatientBean);

    String ageIn = (String) getValue("agein", patientParams);
    String ageString = getValue("age", patientParams).toString();
    int existingAge = (existingPatientBean.get("age") != null)
        ? (Integer) existingPatientBean.get("age")
        : 0;
    String existingAgeIn = (existingPatientBean.get("agein") != null)
        ? (String) existingPatientBean.get("agein")
        : null;

    /*
     * Set Date of birth and expected date of birth. based on existing value and
     * params.
     */
    setAge(patDetailsBean, existingAge, existingAgeIn, dateOfBirth, ageString, ageIn);

    if ((Boolean) patDetailsBean.get(MOBILE_ACCESS) && existingPatientBean != null
        && (null == existingPatientBean.get("mobile_password")
            || existingPatientBean.get("mobile_password").equals(""))) {
      patDetailsBean.set("mobile_password", RandomGeneration.randomGeneratedPassword(6));
    }

    String categoryexpirydate = (String) getValue("category_expiry_date", patientParams);
    if (categoryexpirydate != null) {
      patDetailsBean.set("category_expiry_date", DateUtil.parseDate(categoryexpirydate));
    }

    Object patCatIdObject = (Object) getValue("patient_category_id", patientParams);
    if (patCatIdObject != null && !patCatIdObject.equals("")) {
      defaultCategory = (Integer) patCatIdObject;
    }
    patDetailsBean.set("patient_category_id", defaultCategory);

    /* Bug 20326, 20398 */
    String caseFileNo = (String) getValue("casefile_no", patientParams);
    String oldRegAutoGenerate = (String) getValue("old_reg_auto_generate", patientParams);
    setCaseFileNo(patDetailsBean, caseFileNo, oldRegAutoGenerate);

    if (patientParams.get("user_name") == null) {
      // ACL check. throws accessdeniedexception is acl fails.
      registrationACL.authenticatePatientDemography(patDetailsBean, existingPatientBean);
    }
    ValidationErrorMap validationErrors = new ValidationErrorMap();

    // Validations.
    Boolean isErVisit = false;
    if (visitParams != null) {
      isErVisit = (null == visitParams.get(IS_ER_VISIT)) ? false
          : (Boolean) visitParams.get(IS_ER_VISIT);
    }
    if (!registrationValidator.validatePatientDemographyUpdate(patDetailsBean, validationErrors,
        isErVisit)) {
      throw new ValidationException(validationErrors);
    }

    BasicDynaBean latestVisitBean = getPatientLatestVisit(mrno, null, null, null);

    /* the patient params value is used to update visit data. */
    patientParams.put("patient_care_oftext", patDetailsBean.get("patient_care_oftext"));
    patientParams.put(PATIENT_C_OCC, patDetailsBean.get(PATIENT_C_OCC));

    /*
     * As the patient Params contains the visit Corresponding next of kin details,
     * we have to update the patient demography only if the updating visit is recent
     * visit.
     */
    if (latestVisitBean != null && visitParams != null && !(visitParams.get(Constants.PATIENT_ID))
        .equals(latestVisitBean.get(Constants.PATIENT_ID))) {
      patDetailsBean.set("patient_care_oftext", existingPatientBean.get("patcontactperson"));
      patDetailsBean.set("patient_careof_address", existingPatientBean.get("pataddress"));
      patDetailsBean.set(PATIENT_C_OCC, existingPatientBean.get(PATIENT_C_OCC));
      patDetailsBean.set(RELATION, existingPatientBean.get("patrelation"));
    }

    // Updating the patient detils bean.

    String password = RandomGeneration.randomGeneratedPassword(6);
    boolean success = updatePatientDetails(patDetailsBean, password);
    List<String> activatedModules = securityService.getActivatedModules();
    if (activatedModules != null && activatedModules.contains("mod_messaging")) {
      String sendSMS = patientParams.get("send_sms") != null
          ? (String) patientParams.get("send_sms")
          : "N";
      String sendEmail = patientParams.get("send_email") != null
          ? (String) patientParams.get("send_email")
          : "N";
      patientCommunicationService.convetAndSavePatientCommPreference(mrno, sendSMS, sendEmail,
          (String) patientParams.get("preferred_language"));
    }
    if (success) {
      Map<String, Object> adtData = new HashMap<>();
      adtData.put("patient_id", (String) patDetailsBean.get("visit_id"));
      
      if (patDetailsBean.get("original_mr_no") != null
          && !((String) patDetailsBean.get("original_mr_no")).isEmpty()) {
        interfaceEventService.editPatientEvent((String) patDetailsBean.get("original_mr_no"));
      } else {
        interfaceEventService.editPatientEvent((String) patDetailsBean.get(Constants.MR_NO));
      }
      
      if (patDetailsBean.get("original_mr_no") != null
          && !((String) patDetailsBean.get("original_mr_no")).isEmpty()) {
        adtData.put(Constants.MR_NO, (String) patDetailsBean.get("original_mr_no"));
        adtData.put("old_mr_no", patDetailsBean.get(Constants.MR_NO));
        adtService.createAndSendADTMessage("ADT_18", adtData);
      } else {
        adtData.put(Constants.MR_NO, (String) patDetailsBean.get(Constants.MR_NO));
        adtService.createAndSendADTMessage("ADT_08", adtData);
      }

      if ("Y".equals(oldRegAutoGenerate) || !caseFileNo.isEmpty()) {
        Boolean exists = mrdCaseFileAttrService.exist(Constants.MR_NO, mrno);
        if (!exists) {
          BasicDynaBean mrdFileBean = mrdCaseFileAttrService.getBean();
          mrdFileBean.set(Constants.MR_NO, mrno);
          mrdFileBean.set("file_status", "A");
          mrdFileBean.set("case_status", "A");
          mrdFileBean.set("created_date", DateUtil.getCurrentDate());
          success = (mrdCaseFileAttrService.insert(mrdFileBean) > 0);
        }
      }
    }

    if (success) {
      // update the modified patient details to the incoming
      // patient in case of internal lab test.
      incomingSampleRegService.updateModifiedPatientDetailsForIncomingPatient(mrno);
      Boolean portalAccess = (Boolean) patDetailsBean.get("portal_access");
      Boolean mobileAccess = (Boolean) patDetailsBean.get("mobile_access");
      if (portalAccess != null && portalAccess) {
        // send out activation mail to patient
        try {
          sendActivationMail(patDetailsBean, password);
        } catch (Exception exc) {
          log.error("Error while sending email for " + mrno, exc);
        }
      }
      if (mobileAccess && visitParams != null 
          && messageUtil.allowMessageNotification(GENERAL_MSG_SEND)) {
        sendMobileAccessCommunications(mrno, (String) visitParams.get(Constants.PATIENT_ID));
      }
    }
  }

  /**
   * Calculate age from visit age.
   *
   * @param visitParams         the visit params
   * @param patientParams       the patient params
   * @param existingPatientBean the existing patient bean
   * @throws ParseException the parse exception
   */
  private void calculateAgeFromVisitAge(Map<String, Object> visitParams,
      Map<String, Object> patientParams, BasicDynaBean existingPatientBean) throws ParseException {
    Map ageMap = null;
    if (patientParams.get("dateofbirth") != null && !"".equals(patientParams.get("dateofbirth"))) {
      return;
    } else {
      if (existingPatientBean.get("dateofbirth") != null
          && !"".equals(existingPatientBean.get("dateofbirth"))) {
        ageMap = DateUtil.getAgeBetweenDates(
            (java.util.Date) existingPatientBean.get("dateofbirth"),
            (java.util.Date) DateUtil.parseDate((String) visitParams.get("reg_date")));
      } else {
        ageMap = DateUtil.getAgeBetweenDates(
            (java.util.Date) existingPatientBean.get("expected_dob"),
            (java.util.Date) DateUtil.parseDate((String) visitParams.get("reg_date")));
      }
    }

    String oldVisitAge = ageMap.get("age").toString();
    String oldVisitAgeIn = (String) ageMap.get("ageIn");
    String newVisitAgeString = getValue("age", patientParams).toString();
    String newVisitAgeIn = (String) getValue("agein", patientParams);

    if (newVisitAgeString.equals(oldVisitAge) && newVisitAgeIn.equals(oldVisitAgeIn)) {
      patientParams.put("age",
          existingPatientBean.get("age") == null ? "" : existingPatientBean.get("age"));
      patientParams.put("agein", oldVisitAgeIn);
      return;
    }

    long currentDateInMillis = DateUtil.getCurrentDate().getTime();
    long strDateInMillis = ((java.util.Date) DateUtil
        .parseDate((String) visitParams.get("reg_date"))).getTime();
    long diffInMillis = currentDateInMillis - strDateInMillis;
    long millisecday = 1000L * 60 * 60 * 24;
    long days = diffInMillis / millisecday;
    BigDecimal diffDays = new BigDecimal(days);

    if (newVisitAgeIn.equals("D")) {
      int age = Integer.parseInt(newVisitAgeString) + diffDays.intValue();
      patientParams.put("age", String.valueOf(age));
    } else if (newVisitAgeIn.equals(OP_TYPE_MAIN)) {
      int age = Integer.parseInt(newVisitAgeString)
          + (diffDays.divide(BigDecimal.valueOf(30.43), BigDecimal.ROUND_FLOOR)).intValue();
      patientParams.put("age", String.valueOf(age));
    } else {
      int age = Integer.parseInt(newVisitAgeString)
          + (diffDays.divide(BigDecimal.valueOf(365.25), BigDecimal.ROUND_FLOOR)).intValue();
      patientParams.put("age", String.valueOf(age));
    }

  }

  /**
   * Sets the case file no.
   *
   * @param bean               the bean
   * @param caseFileNo         the case file no
   * @param oldRegAutoGenerate the old reg auto generate
   * @return true, if successful
   */
  private boolean setCaseFileNo(BasicDynaBean bean, String caseFileNo, String oldRegAutoGenerate) {
    String caseFileRequired = "N";

    Map<String, Object> categoryMap = new HashMap<>();
    categoryMap.put("category_id", (Integer) bean.get("patient_category_id"));
    BasicDynaBean categoryDetBean = patientCategoryService.findByPk(categoryMap);
    if (categoryDetBean != null && categoryDetBean.get("case_file_required") != null
        && !categoryDetBean.get("case_file_required").equals("")) {
      caseFileRequired = categoryDetBean.get("case_file_required").toString();
    }

    if (caseFileRequired.equals("Y")) {

      if ("Y".equals(oldRegAutoGenerate)) {
        caseFileNo = DatabaseHelper.getNextPatternId("CASENO");
        bean.set("casefile_no", caseFileNo);
      } else if (caseFileNo != null && !caseFileNo.isEmpty()) {
        bean.set("casefile_no", caseFileNo);
      }
    }
    return true;
  }

  /**
   * Gets the patient visit details.
   *
   * @param visitId the visit id
   * @return the patient visit details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> getPatientVisitDetails(String visitId) {
    Map visitMap = getPatientVisitInfo(visitId);
    String mrno = (String) visitMap.get(Constants.MR_NO);
    Map patientMap = getPatientDemography(mrno);
    Map ageMap;
    if (patientMap.get("dateofbirth") != null && !"".equals(patientMap.get("dateofbirth"))) {
      ageMap = DateUtil.getAgeBetweenDates((java.util.Date) patientMap.get("dateofbirth"),
          (java.util.Date) visitMap.get("reg_date"));
    } else {
      ageMap = DateUtil.getAgeBetweenDates((java.util.Date) patientMap.get("expected_dob"),
          (java.util.Date) visitMap.get("reg_date"));
    }

    patientMap.put("patient_care_oftext", visitMap.get("patient_care_oftext"));
    patientMap.put("patient_careof_address", visitMap.get("patient_careof_address"));
    patientMap.put(RELATION, visitMap.get(RELATION));

    patientMap.put("current_age", patientMap.get("age"));
    patientMap.put("current_agein", patientMap.get("agein"));
    patientMap.put("age", ageMap.get("age"));
    patientMap.put("agein", ageMap.get("ageIn"));
    Map<String, Object> data = new HashMap<String, Object>();
    data.put(VISIT, visitMap);
    if (visitMap.containsKey("reference_docto_id") && visitMap.get("reference_docto_id") != null) {
      BasicDynaBean refBean = referralDoctorService
          .getReferralForVisit((String) visitMap.get("reference_docto_id"));
      if (refBean != null && refBean.getMap() != null) {
        data.put("referralFilter", refBean.getMap());
      }
    }
    data.put("patient", patientMap);
    data.put(INSURANCE, getPatientInsuranceDetails(visitId));
    return data;
  }

  /**
   * Sets the age.
   *
   * @param patDetailsBean the pat details bean
   * @param existingAge    the existing age
   * @param existingAgeIn  the existing age in
   * @param dateOfBirth    the date of birth
   * @param ageString      the age string
   * @param ageIn          the age in
   * @return true, if successful
   */
  private boolean setAge(BasicDynaBean patDetailsBean, int existingAge, String existingAgeIn,
      String dateOfBirth, String ageString, String ageIn) {
    java.sql.Date expectedDob = null;
    if (dateOfBirth.trim().equals("") && !ageIn.equals("D") && !ageString.isEmpty()) {
      int age = Integer.parseInt(ageString);
      java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(age, ageIn, true, true);
      expectedDob = new java.sql.Date(date.getTime());

      patDetailsBean.set("dateofbirth", null);
      if (existingAge != age || !ageIn.equals(existingAgeIn)) {
        patDetailsBean.set("expected_dob", expectedDob);
      }

    } else {
      java.sql.Date dob = null;
      if (dateOfBirth.trim().equals("") && !ageString.isEmpty()) {
        int age = Integer.parseInt(ageString);
        java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(age, ageIn, true, true);
        dob = new java.sql.Date(date.getTime());
      } else if (!dateOfBirth.trim().equals("")) {
        try {
          dob = new java.sql.Date(DateUtil.parseDate(dateOfBirth).getTime());
        } catch (ParseException exc) {
          throw new ConversionException(Arrays.asList("dateofbirth"));
        }
      }
      patDetailsBean.set("dateofbirth", dob);
      patDetailsBean.set("expected_dob", null);
    }

    if (patDetailsBean.get("dateofbirth") == null && patDetailsBean.get("expected_dob") == null) {
      patDetailsBean.set("expected_dob", expectedDob);
    }
    return true;
  }

  /**
   * Update patient details.
   *
   * @param patDetailsBean the pat details bean
   * @param password       the password
   * @return true, if successful
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException              Signals that an I/O exception has occurred.
   */
  public boolean updatePatientDetails(BasicDynaBean patDetailsBean, String password)
      throws NoSuchAlgorithmException, IOException {
    Map<String, Object> keys = new HashMap<String, Object>();
    String mrNo = (String) patDetailsBean.get(Constants.MR_NO);
    keys.put(Constants.MR_NO, mrNo);

    boolean success = false;
    int update = patientDetailsService.update(patDetailsBean, keys);
    if (update > 0) {
      success = true;
      Boolean portalAccess = (Boolean) patDetailsBean.get("portal_access");
      if (portalAccess != null && portalAccess) {
        success = createLoginAccess(mrNo, password);
      }

      if (success) {
        String cityid = (String) patDetailsBean.get("patient_city");
        String area = (String) patDetailsBean.get("patient_area");
        if (area != null && !area.equals("")) {
          success = checkAndInsertArea(cityid, area);
        }
      }
    }
    return success;
  }

  /**
   * Creates the login access.
   *
   * @param mrNo     the mr no
   * @param password the password
   * @return true, if successful
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  private boolean createLoginAccess(String mrNo, String password)
      throws IOException, NoSuchAlgorithmException {
    List<BasicDynaBean> roleList = roleService.listAll("portal_id", "P");
    if (null == roleList || roleList.size() == 0 || roleList.get(0).get("role_id") == null) {
      return false;
    }
    String roleid = roleList.get(0).get("role_id").toString();
    if (null == roleid || roleid.equals("")) {
      return false;
    }
    int roleId = Integer.parseInt(roleid);

    boolean success = false;

    BasicDynaBean existing = userService.findByKey("emp_username", mrNo);
    BasicDynaBean userbean = userService.getBean();
    if (existing != null && (new BigDecimal(roleId).equals(existing.get("role_id")))) {
      // update password for existing
      boolean isEncrypted = (Boolean) existing.get("is_encrypted");
      password = isEncrypted ? PasswordEncoder.encode(password) : password;
      userbean.set("emp_password", password);

      Map<String, Object> key = new HashMap<String, Object>();
      key.put("emp_username", mrNo);

      success = (userService.update(userbean, key) > 0);
    } else {
      userbean.set("emp_username", mrNo);
      userbean.set("emp_password", PasswordEncoder.encode(password));
      userbean.set("role_id", new BigDecimal(roleId));
      ;
      userbean.set("hosp_user", "Y");
      userbean.set("emp_status", "A");
      userbean.set("password_change_date", DateUtil.getCurrentTimestamp());
      userbean.set("is_encrypted", true);

      success = (userService.insert(userbean) > 0);
    }
    return success;
  }

  /**
   * Check and insert area.
   *
   * @param cityId   the city id
   * @param areaName the area name
   * @return true, if successful
   */
  private boolean checkAndInsertArea(String cityId, String areaName) {
    boolean success = true;

    BasicDynaBean areaByCity = areaService.listAreaByCity(cityId, areaName);
    if (areaByCity == null) {
      BasicDynaBean bean = areaService.getBean();
      bean.set("area_name", areaName);
      bean.set("city_id", cityId);
      bean.set(STATUS, "A");

      success = (areaService.insert(bean) > 0);
    }
    return success;
  }

  /**
   * Create Consultation Doctor Bean.
   *
   * @param doctorId      the doctor id
   * @param doctorCharge  the doctor charge
   * @param consDate      the cons date
   * @param consTime      the cons time
   * @param consRemarks   the cons remarks
   * @param appointmentId the appointment id
   * @return the basic dyna bean
   */
  public BasicDynaBean regBaseDoctor(String doctorId, String doctorCharge, String consDate,
      String consTime, String consRemarks, int appointmentId) {
    BasicDynaBean baseDocBean = null;
    java.util.Date parsedDate = new java.util.Date();
    java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
    DateUtil dateutil = new DateUtil();

    if (doctorId != null && !doctorId.equals("") && doctorCharge != null
        && !doctorCharge.equals("")) {
      baseDocBean = doctorConsultationService.getBean();

      Timestamp visitedTime = new Timestamp(parsedDate.getTime());
      if (consDate != null && !consDate.equals("") && consTime != null && !consTime.equals("")) {
        try {
          visitedTime = dateutil.parseTheTimestamp(consDate + " " + consTime);
        } catch (ParseException exc) {
          log.error("parse exceptinon ", exc);
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("date_time", "exception.invalid.value",
              Arrays.asList(StringUtil.prettyName("date_time")));
          throw new ValidationException(errorMap);
        }
      }

      if (visitedTime.getTime() < parsedDate.getTime()) {
        visitedTime = datetime;
      }

      baseDocBean.set("doctor_name", doctorId);
      baseDocBean.set("presc_date", datetime);
      baseDocBean.set("visited_date", visitedTime);
      baseDocBean.set("remarks", consRemarks);
      baseDocBean.set("head", doctorCharge);
      baseDocBean.set(STATUS, "A");
      baseDocBean.set("visit_mode", "I");
      if (appointmentId > 0) {
        BasicDynaBean apptBean = resourceService.findByKey(appointmentId);
        if (doctorId.equals(apptBean.get("prim_res_id"))) {
          baseDocBean.set(APPOINTMENT_ID, appointmentId);
          baseDocBean.set("presc_doctor_id", apptBean.get("presc_doc_id"));
          baseDocBean.set("visit_mode", apptBean.get("visit_mode"));
        }
      }
    }
    return baseDocBean;
  }

  /**
   * Send activation mail.
   *
   * @param patient  the patient
   * @param password the password
   * @throws AddressException   the address exception
   * @throws MessagingException the messaging exception
   * @throws SQLException       the SQL exception
   */
  public void sendActivationMail(BasicDynaBean patient, String password)
      throws AddressException, MessagingException, SQLException {
    // TODO: Chetan need to change this.
    HttpServletRequest req = RequestContext.getHttpRequest();

    BasicDynaBean genPrefBean = genPrefService.getAllPreferences();
    StringBuilder builder = new StringBuilder(req.getScheme());
    builder.append("://");

    String domainName = (String) genPrefBean.get("domain_name");
    if (domainName == null || domainName.isEmpty()) {
      domainName = req.getLocalName();
    } // hardcoded?
    builder.append(domainName).append(req.getContextPath()).append("/patient/loginForm.do");

    String[] recipients = new String[1];
    String from = null;
    String subject = null;
    String message = null;
    String patientName = null;
    BasicDynaBean patResultBean = patientDetailsService
        .getPatientMailId(patient.get(Constants.MR_NO).toString());
    if (patResultBean != null) {
      recipients[0] = (String) patResultBean.get("email_id");
      patientName = (String) patResultBean.get("patient_name");
    }

    BasicDynaBean emailResultBean = emailTemplateService
        .getMailParametersFromTemplateNameAndEmailCategory("Patient Registration", "C");
    if (emailResultBean != null) {
      from = (String) emailResultBean.get("from_address");
      subject = (String) emailResultBean.get("subject");
      message = (String) emailResultBean.get("mail_message");
    }

    Map<String, String> vars = new HashMap<String, String>();
    vars.put("user", patientName);
    String hospitalName = (String) genPrefBean.get("hospital_name");
    vars.put("hospital", hospitalName);
    vars.put("mrno", patient.get(Constants.MR_NO).toString());
    vars.put("password", password);
    String portalUrl = builder.toString();
    vars.put("portalurl", portalUrl);

    Map<String, Object> eventData = new HashMap<String, Object>();

    eventData.put("from", from);
    eventData.put("recipients", recipients);
    eventData.put("message", message);
    eventData.put("vars", vars);

    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put(EVENT_DATA, eventData);
    String userName = (String) sessionService.getSessionAttributes().get(USER_ID);
    jobData.put(USER_NAME, userName);
    jobData.put(SCHEMA, RequestContext.getSchema());
    jobData.put(EVENT_ID, subject);

    jobService.scheduleImmediate(
        buildJob("PatientUpdatePortalJob_" + patient.get(Constants.MR_NO).toString(),
            RegistrationUpdateEmailJob.class, jobData));

  }

  /**
   * Gets the baby DOB and member id validity details.
   *
   * @param mrNo        the mr no
   * @param visitId     the visit id
   * @param sponsorType the sponsor type
   * @return the baby DOB and member id validity details
   */
  public Map<String, Object> getBabyDOBAndMemberIdValidityDetails(String mrNo, String visitId,
      String sponsorType) {
    String parentMrNo = null;
    Map<String, Object> babyInfo = null;
    String govtIdentifier = null;
    String memberId = null;

    int centerId = 0;
    BasicDynaBean babyVisitInfo = patientRegistrationRepository.findByKey(Constants.PATIENT_ID,
        visitId);
    BasicDynaBean babyDetails = patientDetailsService.getBabyDOBAndSalutationDetails(mrNo);
    if (babyDetails != null) {
      govtIdentifier = (String) babyDetails.get("parent_government_identifier");
      parentMrNo = (String) babyDetails.get("parent_mr_no");
    }
    if (babyVisitInfo != null) {
      centerId = (Integer) babyVisitInfo.get(CENTER_ID1);
    }

    if (null != parentMrNo && !parentMrNo.equals("")) {
      babyInfo = new HashMap<String, Object>();
      BasicDynaBean healthAuthPrefs = healthAuthPrefService.listBycenterId(centerId);
      if (sponsorType != null) {
        // TODO : if sponsorType != null. for now not needed.
        // memberId = VisitDetailsDAO.getPatientMemberId(parentMrNo,
        // sponsorType);

        babyInfo.put("babyDetails", babyDetails.getMap());
      }
      babyInfo.put("babyVisitInfo", babyVisitInfo != null ? babyVisitInfo.getMap() : null);
      babyInfo.put("helathAuthPrefs", healthAuthPrefs.getMap());
      babyInfo.put("member_id", memberId);
      babyInfo.put("govtIdentifier", govtIdentifier != null ? govtIdentifier : "");
    }
    return null;
  }

  /**
   * Baby govt idt details handler.
   *
   * @param babyInfo       the baby info
   * @param govtIdentifier the govt identifier
   * @return the boolean
   */
  @SuppressWarnings("rawtypes")
  public Boolean babyGovtIdtDetailsHandler(Map<String, Object> babyInfo, String govtIdentifier) {
    if (babyInfo != null) {
      Map babyDetails = (Map) babyInfo.get("babyDetails");
      Map helthAuthPrefs = (Map) babyInfo.get("helathAuthPrefs");
      String parentGovtIdentifier = (String) babyInfo.get("govtIdentifier");
      parentGovtIdentifier = parentGovtIdentifier == null ? "" : parentGovtIdentifier;
      if (babyDetails != null && helthAuthPrefs != null) {
        String salutation = (String) babyDetails.get("salutation");
        salutation = salutation.toUpperCase();
        if (salutation.equals("BABY") && parentGovtIdentifier.equals(govtIdentifier)) {
          java.sql.Date sqldob = (java.sql.Date) babyDetails.get("dateofbirth");
          Integer childmotherinsmembervaliditydays = (Integer) helthAuthPrefs
              .get("child_mother_ins_member_validity_days");
          java.util.Date dob = new java.util.Date(sqldob.getTime());
          java.util.Date serverDate = new java.util.Date();
          int diffInDays = (int) ((serverDate.getTime() - dob.getTime()) / 60 / 60 / 24 / 1000);
          if (childmotherinsmembervaliditydays != null
              && diffInDays < childmotherinsmembervaliditydays) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Gets the patient visit details bean.
   *
   * @param patientId the patient id
   * @return the patient visit details bean
   */
  public BasicDynaBean getPatientVisitDetailsBean(String patientId) {
    return patientRegistrationRepository.getPatientVisitDetailsBean(patientId);
  }

  /**
   * Gets the value.
   *
   * @param key      the key
   * @param params   the params
   * @param sendNull the send null
   * @return the value
   */
  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params, boolean sendNull) {
    Object obj = params.get(key);
    if (sendNull && obj == null) {
      return null;
    } else if (obj != null) {
      return obj;
    }
    return "";
  }

  /**
   * Gets the value.
   *
   * @param key    the key
   * @param params the params
   * @return the value
   */
  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params) {
    return getValue(key, params, false);
  }

  /**
   * Gets the patient visits.
   *
   * @param mrno            the mr no
   * @param visitType       the visit type
   * @param activeOnly      the active only
   * @param allowOspPatient the allow osp patient
   * @return the patient visits
   */
  @SuppressWarnings("rawtypes")
  public List getPatientVisits(String mrno, String visitType, boolean activeOnly,
      boolean allowOspPatient) {
    return ConversionUtils.listBeanToListMap(patientRegistrationRepository.getPatientVisits(mrno,
        visitType, (Integer) sessionService.getSessionAttributes().get(CENTER_ID), activeOnly,
        allowOspPatient));
  }

  /**
   * Gets the patient visits.
   *
   * @param mrno the mr no
   * @return the patient visits
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getPatientVisits(String mrno) {
    return ConversionUtils
        .listBeanToListMap(patientRegistrationRepository.getPatientAllActiveVisits(mrno,
            (Integer) sessionService.getSessionAttributes().get(CENTER_ID), true));
  }

  /**
   * Gets the patient all visits.
   *
   * @param mrno       the mr no
   * @param activeOnly the active only
   * @return the patient all visits
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getPatientAllVisits(String mrno, boolean activeOnly) {
    return ConversionUtils
        .listBeanToListMap(patientRegistrationRepository.getPatientAllActiveVisits(mrno,
            (Integer) sessionService.getSessionAttributes().get(CENTER_ID), activeOnly));
  }

  /**
   * Gets the patient visits.
   *
   * @param mrno the mr no
   * @return the patient visits
   */
  @SuppressWarnings("rawtypes")
  public BasicDynaBean getLatestActiveVisit(String mrno) {
    return patientRegistrationRepository.getLatestActiveVisit(mrno);
  }
  
  /**
   *  Gets patient visit type as per follow up rules.
   *
   * @param mrNo MR Number of the patient.
   * @param doctorId The doctor id.
   * @param sponsorId Primary sponsor id.
   * @param centerId center id.
   * @return visit type based on advanced follow-up rules.
   */
  private String getAdvancedVisitType(String mrNo, String doctorId,
      String sponsorId, Integer centerId) {
    BasicDynaBean doctorBean = doctorService.getDoctorById(doctorId);
    String doctorDepartmentId = (String) doctorBean.get("dept_id");
    
    BasicDynaBean ruleApplicability = opVisitTypeRuleApplicabilityService
        .getApplicableRule(centerId, sponsorId, doctorDepartmentId, doctorId);
    
    if (ruleApplicability == null) {
      log.info("Picking visit type as main as no rule found");
      return OP_TYPE_MAIN;
    }
    
    log.info("Found matching rule applicability with id "
        + ruleApplicability.get("rule_applicability_id"));
    BasicDynaBean rule = opVisitTypeRuleService
        .getRule( (Integer) ruleApplicability.get("rule_id"));
    Integer ipVisitLimit = (Integer) rule.get("ip_main_visit_limit");
    Long ruleDetailCount =  (Long)rule.get("op_rule_details_count");
    
    if (ruleDetailCount == 0) {
      log.info("Picking visit type as main as no rule details for rule " + rule.get("rule_name"));
      return OP_TYPE_MAIN;
    }
    
    List<String> visitList = new ArrayList<>();
    visitList.add(OP_TYPE_MAIN);
    if (ruleDetailCount <= 1) {
      visitList.add(OP_TYPE_REVISIT);
    }
    String visitDependence = "D";
    if (ruleApplicability.get("doctor_id")
        .equals(OpVisitTypeRuleApplicabilityService.DOCTOR_NOT_APPLICABLE)) {
      visitDependence = "S";
    }
    
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();
    String followupAcrossCenters = (String) regPrefs.get("followup_across_centers");
    String consultationValidityUnits = (String) clinicalPrefService.getClinicalPreferences()
        .get("consultation_validity_units");
    BasicDynaBean ipVisitBean = getPatientIpVisitWithinValidity(followupAcrossCenters,
        visitDependence, consultationValidityUnits, mrNo, doctorId, doctorDepartmentId,
        (Integer)rule.get("max_ip_validity") + 1, centerId, sponsorId, true);
    Timestamp dischargeDateTime = ipVisitBean != null ? (Timestamp) ipVisitBean
        .get("discharge_date_time") : null;
            
    if (ipVisitBean != null) {
      log.info("IP Visit found with discharge in range");
      Integer followupCountObj = getPatientFollowupCount(followupAcrossCenters,
          visitDependence, mrNo, doctorId, doctorDepartmentId, dischargeDateTime,
          centerId, sponsorId, true);
      int followupCount = followupCountObj != null ? followupCountObj.intValue() : 0;
      
      if (followupCount == ipVisitLimit) {
        log.info(
            "IP Visit limit over setting next immediate visit to "
            + rule.get("post_limit_visit"));
        return (String) rule.get("post_limit_visit");
      }
      
      if (followupCount < ipVisitLimit) {
        java.util.Date visitedDate = new java.util.Date(dischargeDateTime.getTime());
        Integer days = (int) DateUtil.getDifferenceDays(visitedDate,
            new java.util.Date(), DateUtil.DURATION_DAYS);
        
        BasicDynaBean ruleDetail = opVisitTypeRuleService
            .getVisitType("I", days, (Integer)rule.get("rule_id"));
        if (ruleDetail != null) {
          log.info("Matched IP rule " + rule.get("rule_name"));
          return (String)ruleDetail.get("op_visit_type");
        } else {
          log.info("No IP rule found.");
        }
      } else {
        log.info("IP followup visits consumed");
      }
      
    } else {
      log.info("No IP visit found with discharge in rule range.");
    }
    
    if (!isPatientVisitsExists(followupAcrossCenters, visitDependence, mrNo, doctorId,
        doctorDepartmentId, centerId, sponsorId, true)) {
      log.info("No previous visit found. Falling to Main Visit, as this is the first.");
      return OP_TYPE_MAIN;
    }
    
    BasicDynaBean visitBean = getPatientMainVisitWithinValidity(followupAcrossCenters,
        visitDependence, consultationValidityUnits, mrNo, doctorId, doctorDepartmentId,
        (Integer)rule.get("max_op_validity") + 1, centerId, visitList, sponsorId, true);
    
    
    if (visitBean == null) {
      log.info("No main visit found matching the rule duration. Falling back to post limit visit.");
      return (String) rule.get("post_limit_visit");
    }
    
    Integer followupCount = getPatientFollowupCount(followupAcrossCenters, visitDependence,
        mrNo, doctorId, doctorDepartmentId, (Timestamp) visitBean.get("visited_date"),
        centerId, sponsorId, true);
    
    followupCount = followupCount == null ? 0 : followupCount;
    
    Integer opVisitLimit = (Integer) rule.get("op_main_visit_limit");
    if (followupCount >= opVisitLimit) {
      log.info("Follow up count exceeds allowed op followups. Falling back to post limit visit.");
      return (String) rule.get("post_limit_visit"); 
    }
    
    Timestamp visitedTimestamp = (Timestamp) visitBean.get("visited_date") ;
    java.util.Date visitedDate = new java.util.Date(visitedTimestamp.getTime());
    Integer days = (int) DateUtil.getDifferenceDays(visitedDate, new java.util.Date(),
        DateUtil.DURATION_DAYS);
    
    BasicDynaBean ruleDetail = opVisitTypeRuleService.getVisitType("O",
        days, (Integer)rule.get("rule_id"));
        
    if (ruleDetail != null) {
      log.info("Rule matched. Visit type: " + ruleDetail.get("op_visit_type"));
      return (String)ruleDetail.get("op_visit_type");
    }
    
    log.info("No matching rule detail found. Falling back to main.");
    return OP_TYPE_MAIN;
  }
  
  /**
   * Gets the patient visit type.
   *
   * @param mrNo     the mr no
   * @param doctorId the doctor id
   * @param sponsorId the sponsor id
   * @return the patient visit type
   * @throws ParseException the parse exception
   */
  public Map<String, Object> getPatientVisitType(String mrNo, String doctorId, String sponsorId)
      throws ParseException {
    return getPatientVisitType(mrNo, doctorId, sponsorId, true,null);
  }

  /**
   * Gets the patient visit type.
   *
   * @param mrNo                the mr no
   * @param doctorId            the doctor id
   * @param sponsorId the sponsor id
   * @param withPreviousDetails set to true to fetch previous visit details
   * @param centerId the center id
   * @return the patient visit type
   * @throws ParseException the parse exception
   */
  public Map<String, Object> getPatientVisitType(String mrNo, String doctorId,
      String sponsorId, boolean withPreviousDetails, Integer centerId) throws ParseException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("doctor_id", doctorId);
    if (centerId == null) {
      centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    }
    Map<String, Object> map = new HashMap<String, Object>();
    BasicDynaBean regPrefs = regPrefService.getRegistrationPreferences();

    String latestVisitId = "";

    String defaultVisitDetailsAcrossCenter = (String) regPrefs
        .get("default_visit_details_across_center");
    if (defaultVisitDetailsAcrossCenter.equals("Y")) {
      latestVisitId = patientRegistrationRepository.getPatientPreviousVisitIdAcrossCenter(mrNo);
    } else {
      latestVisitId = patientRegistrationRepository.getPatientPreviousVisitIdInSameCenter(centerId,
          mrNo);
    }
    
    Map<String, Object> previousPrescriptions = getPreviousPrescriptions(mrNo);
    if (!StringUtils.isEmpty(latestVisitId)) {
      Map<String, Map<String, Object>> listVisitPreAuthPrescriptions;
      listVisitPreAuthPrescriptions = getVisitWisePriorauthItems(latestVisitId);
      previousPrescriptions.putAll(listVisitPreAuthPrescriptions);    
    }
    map.put("previous_prescriptions", previousPrescriptions);
    
    BasicDynaBean doctorBean = doctorService.findByPk(params);
    if (doctorBean == null) {
      map.put(OP_TYPE, OP_TYPE_MAIN);
      map.put("main_visit_id", "");
      map.put("practitioner_id", "");
      map.put("previous_doctor_visit", null);
      return map;
    }

    String followupAcrossCenters = (String) regPrefs.get("followup_across_centers");
    String visitDependenceType = (String) regPrefs.get("visit_type_dependence");
    String doctorDepartmentId = (String) doctorBean.get("dept_id");
    String consultationValidityUnits = (String) clinicalPrefService.getClinicalPreferences()
        .get("consultation_validity_units");

    boolean isVisitsExist = isPatientVisitsExists(followupAcrossCenters, visitDependenceType, mrNo,
        doctorId, doctorDepartmentId, centerId);
    if (!isVisitsExist) {
      // after Discharge if patient have OP visit
      Integer doctorValidityDays = 0;
      if (doctorBean.get("ip_discharge_consultation_validity") != null) {
        doctorValidityDays = ((BigDecimal) doctorBean.get("ip_discharge_consultation_validity"))
            .intValue();
      }
      Integer doctorFollowupCount = (Integer) doctorBean
          .get("ip_discharge_consultation_count") != null
              ? (Integer) doctorBean.get("ip_discharge_consultation_count")
              : 0;

      BasicDynaBean ipVisitBean = getPatientIpVisitWithinValidity(followupAcrossCenters,
          visitDependenceType, consultationValidityUnits, mrNo, doctorId, doctorDepartmentId,
          doctorValidityDays, centerId);
      Timestamp dischargeDateTime = ipVisitBean != null
          ? (Timestamp) ipVisitBean.get("discharge_date_time")
          : null;

      Integer followupCountObj = getPatientFollowupCount(followupAcrossCenters, visitDependenceType,
          mrNo, doctorId, doctorDepartmentId, dischargeDateTime, centerId);
      int followupCount = followupCountObj != null ? followupCountObj.intValue() : 0;
      
      
      
      if (visitDependenceType.equals("A")) {
        map.put(OP_TYPE, getAdvancedVisitType(mrNo, doctorId,sponsorId, centerId));
      } else if (ipVisitBean == null) {
        BasicDynaBean regBean = null;
        Integer applicableCenterId = null;
        // consider the center if applicable
        if (followupAcrossCenters.equals("N")) {
          applicableCenterId = centerId;
        }
        if (visitDependenceType.equals("S")) { // check if any visit exist for same dept doctor
          regBean = patientRegistrationRepository.getLatestIpVisitBYDoctorOrDept(mrNo,
              applicableCenterId, null, doctorDepartmentId);
        } else { // check if any visit exist for the given doctor
          regBean = patientRegistrationRepository.getLatestIpVisitBYDoctorOrDept(mrNo,
              applicableCenterId, doctorId, null);
        }
        if ((regBean != null)
            && followupCount <= doctorFollowupCount) {
          map.put(OP_TYPE, OP_TYPE_REVISIT);
        } else {
          map.put(OP_TYPE, OP_TYPE_MAIN);
        }      
      } else {
        if (followupCount >= doctorFollowupCount) {
          map.put(OP_TYPE, OP_TYPE_REVISIT);
        } else {
          map.put(OP_TYPE, OP_TYPE_FOLLOWUP);
        }          
      }
          
      map.put("main_visit_id", "");
      map.put("practitioner_id", doctorBean.get("practitioner_id"));
      map.put("previous_doctor_visit", null);
      return map;
    }

    // can be Revisit or Followup visit
    Integer doctorValidityDays = 0;
    if (doctorBean.get("op_consultation_validity") != null) {
      doctorValidityDays = ((BigDecimal) doctorBean.get("op_consultation_validity")).intValue();
    }
    Integer doctorFollowupCount = (Integer) doctorBean.get("allowed_revisit_count") != null
        ? (Integer) doctorBean.get("allowed_revisit_count")
        : 0;
    String mainVisitId = null;

    BasicDynaBean mainVisitBean = getPatientMainVisitWithinValidity(followupAcrossCenters,
        visitDependenceType, consultationValidityUnits, mrNo, doctorId, doctorDepartmentId,
        doctorValidityDays, centerId);
    Timestamp mainVisitDate = mainVisitBean != null ? (Timestamp) mainVisitBean.get("visited_date")
        : null;
    Integer followupCountObj = getPatientFollowupCount(followupAcrossCenters, visitDependenceType,
        mrNo, doctorId, doctorDepartmentId, mainVisitDate, centerId);
    int followupCount = followupCountObj != null ? followupCountObj.intValue() : 0;

    if (visitDependenceType.equals("A")) {
      map.put(OP_TYPE, getAdvancedVisitType(mrNo, doctorId,sponsorId, centerId));
    } else if (mainVisitBean == null || followupCount >= doctorFollowupCount) { // Revisit
      map.put(OP_TYPE, OP_TYPE_REVISIT);
    } else { // followup visit
      map.put(OP_TYPE, OP_TYPE_FOLLOWUP);
      mainVisitId = (String) mainVisitBean.get("main_visit_id");
    }

    if (!withPreviousDetails) {
      return map;
    }

    if (mainVisitId == null) { // get the latest mainvisit
      BasicDynaBean latestMainVisitBean = getPatientLatestMainVisitBean(followupAcrossCenters, mrNo,
          doctorId, centerId);
      mainVisitId = latestMainVisitBean != null ? (String) latestMainVisitBean.get("main_visit_id")
          : null;
    }

    if (mainVisitId != null && !mainVisitId.equals("")) {
      map.put("previous_doctor_visit", getPatientVisitInfo(mainVisitId));
    } else {
      map.put("previous_doctor_visit", null);
    }

    map.put("practitioner_id", doctorBean.get("practitioner_id"));

    return map;
  }
  
  
  /**
   * Gets the visit wise priorauth items.
   *
   * @param visitId the visit id
   * @return the visit wise priorauth items
   */
  public Map<String, Map<String, Object>> getVisitWisePriorauthItems(String visitId) {
    Map<String, Map<String, Object>> listVisitPreAuthPrescriptions = new HashMap<>();
    Map<String, Object> visitPreAuthPrescriptions = new HashMap<>();
    Map<String, Map<PreAuthItemType, List<BasicDynaBean>>> activePreAuthApprovedItems = 
        preAuthItemsService.getVisitActivePreAuthApprovedItems(visitId);
    for (Map.Entry<String, Map<PreAuthItemType, List<BasicDynaBean>>> itemEntry : 
        activePreAuthApprovedItems.entrySet()) {
      Map<PreAuthItemType, List<BasicDynaBean>> itemsMap = itemEntry.getValue();
      for (Map.Entry<PreAuthItemType, List<BasicDynaBean>> itemTypeBeanMap : 
          itemsMap.entrySet()) {
        visitPreAuthPrescriptions.put(itemTypeBeanMap.getKey().getPendingPrescResponseKey(),
            ConversionUtils.listBeanToListMap(itemTypeBeanMap.getValue()));
      }
      BasicDynaBean visitDetails = getVisitDetailsWithReferralDoctor(
          itemEntry.getKey());
      visitPreAuthPrescriptions.put("doctor", visitDetails.get("prescribed_doctor_name"));
      visitPreAuthPrescriptions.put("primary_sponsor_name", visitDetails.get("tpa_name"));
      visitPreAuthPrescriptions.put("secondary_sponsor_name", visitDetails.get("sec_tpa_name"));
      visitPreAuthPrescriptions.put("prescription_date", visitDetails.get("reg_date"));
      visitPreAuthPrescriptions.put("visitId", itemEntry.getKey());
      visitPreAuthPrescriptions.put("visitedDate", visitDetails.get("reg_date"));
      
      listVisitPreAuthPrescriptions.put(itemEntry.getKey(),visitPreAuthPrescriptions);
    }    
    return listVisitPreAuthPrescriptions;
  }
 
  /**
   * Save insurance and create bills.
   *
   * @param params           the params
   * @param patientBean      the patient bean
   * @param visitDetailsBean the visit details bean
   * @return the map
   * @throws Exception the exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> saveInsuranceAndCreateBills(Map<String, Object> params,
      BasicDynaBean patientBean, BasicDynaBean visitDetailsBean) throws Exception {

    ArrayList insuranceList = (ArrayList) params.get(INSURANCE);
    Map<String, Object> visitDetailsParams = (Map<String, Object>) params.get(VISIT);

    boolean isTpa = false;
    List<BasicDynaBean> plansList = new ArrayList<BasicDynaBean>();

    String visitId = (String) visitDetailsBean.get(Constants.PATIENT_ID);
    String registrationChargesApplicable = (String) getValue("apply_registration_charges",
        visitDetailsParams);
    // marking reg_charge_accepted as "NO" when user uncheck's Registration
    // Charges check box from
    // OP Registration screen.
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    if (registrationChargesApplicable.equals("N")) {
      visitDetailsBean.set("reg_charge_accepted", "N");
    }
    String estimateAmtStr = (String) getValue("estimateAmt", visitDetailsParams, true);
    // consultation charge
    BigDecimal estimatedAmt = (estimateAmtStr != null) ? new BigDecimal(estimateAmtStr)
        : BigDecimal.ZERO;

    registrationChargesApplicable = (registrationChargesApplicable == null
        || registrationChargesApplicable.equals("")) ? "Y" : registrationChargesApplicable;

    String billType = (String) getValue(BILL_TYPE, visitDetailsParams);

    BasicDynaBean regPerfBean = regPrefService.getRegistrationPreferences();
    boolean noGenRegCharge = Arrays.asList(regPerfBean.get("no_reg_charge_sources")
        .toString().split(",")).contains(patientBean.get("resource_captured_from"));

    String regAndBill = (String) getValue("regAndBill", visitDetailsParams);

    isTpa = saveInsurance(insuranceList, visitId, isTpa, plansList, visitDetailsBean);

    // Create bill and Bill Charges
    // IF TPA create Bill Charge Claims , Bill Claim, Insurance Claim

    Map<String, Object> patientDetailsParams = (Map<String, Object>) params.get("patient");
    BasicDynaBean bill = billService.createBill(patientDetailsParams, visitDetailsParams, visitId,
        isTpa, plansList, visitDetailsBean, billType, registrationChargesApplicable, estimatedAmt,
        regAndBill, noGenRegCharge);
    // Update visit details with sponsorId/Ins CO/Prio Auth/Perdium DRG flag

    updateVisitDetails(visitDetailsBean);

    String billNo = null;
    if (null != bill && !bill.getMap().isEmpty()) {
      billNo = (String) bill.get("bill_no");
    }

    Map<String, Map<String, List<Object>>> orderItems =
            (Map<String, Map<String, List<Object>>>) params.get("ordered_items");
    orderService.orderItems(null, true, billNo,
        (Integer) visitDetailsBean.get(CENTER_ID1), plansList, params, visitDetailsBean, true,
        orderItems);

    boolean docEandmCodificationRequired = regPrefService.getRegistrationPreferences()
        .get("doc_eandm_codification_required").equals("Y");
    // Update bill status and payment status after all the bill charges are
    // inserted.
    billService.updatePaymentAndBillStatus(regAndBill, bill, docEandmCodificationRequired, 
        estimatedAmt);

    Map<String, Object> statusMap = new HashMap<String, Object>();
    statusMap.put("bill", bill);
    return statusMap;
  }

  /**
   * Creates the receipt.
   *
   * @param params the params
   * @param map    the map
   * @return the map
   * @throws SQLException the SQL exception
   */
  @Transactional(rollbackFor = Exception.class)
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> createReceipt(Map<String, Object> params, Map<String, Object> map)
      throws SQLException {

    ArrayList insuranceList = (ArrayList) params.get(INSURANCE);
    Map<String, Object> visitDetailsParams = (Map<String, Object>) params.get(VISIT);
    BasicDynaBean bill = (BasicDynaBean) map.get("bill");
    BasicDynaBean visitBean = (BasicDynaBean) map.get(VISIT);
    BasicDynaBean patientBean = (BasicDynaBean) map.get("patient");
    String estimateAmtStr = (String) getValue("estimateAmt", visitDetailsParams, true);
    // consultation
    // charge
    BigDecimal estimatedAmt = (estimateAmtStr != null) ? new BigDecimal(estimateAmtStr)
        : BigDecimal.ZERO;
    boolean istpa = (null != insuranceList && !insuranceList.isEmpty());
    String billType = (String) getValue(BILL_TYPE, visitDetailsParams);
    String regAndBill = (String) getValue("regAndBill", visitDetailsParams);
    Map<String, Object> printMap = new HashMap<String, Object>();

    boolean docEandmCodificationRequired = regPrefService.getRegistrationPreferences()
        .get("doc_eandm_codification_required").equals("Y");

    // Re-Calculate Sponsor Amount For All charges after Insertion.
    sponsorService.recalculateSponsorAmount((String) visitBean.get(Constants.PATIENT_ID));

    /* Receipt generation while register and pay */
    if (regAndBill.equals("Y")
        && billService.checkToCreatePrepaidBill(regAndBill, istpa, estimatedAmt, billType)) {
      receiptService.createReceipt(bill, visitDetailsParams, docEandmCodificationRequired,
          visitBean, printMap);
    }

    if (regAndBill.equals("Y") && messageUtil.allowMessageNotification(GENERAL_MSG_SEND)
        && (bill != null && !bill.getMap().isEmpty())) {
      sendReceiptSms(visitDetailsParams, patientBean, visitBean, bill.get("bill_no").toString(),
          insuranceList);
    }

    if (messageUtil.allowMessageNotification(GENERAL_MSG_SEND)
        && (bill != null && !bill.getMap().isEmpty())) {
      String referredBy = null;
      String doctor = null;
      if (null != visitDetailsParams.get("doctor")) {
        doctor = visitDetailsParams.get("doctor").toString();
      }
      if (null != visitDetailsParams.get("reference_docto_id")) {
        referredBy = visitDetailsParams.get("reference_docto_id").toString();
      }
      Map<String, String> tokenMap = populateTokenMap(patientBean,
          ((Integer) visitBean.get(CENTER_ID1)), referredBy, doctor);
      tokenMap.put(USER_ID, (String) sessionService.getSessionAttributes().get(USER_ID));
      BasicDynaBean billBean = billService.findByKey((String) bill.get("bill_no"));

      sendBillEmail(billBean, tokenMap);

    }
    Map<String, Object> returnMap = getPatientVisitDetails(
        (String) visitBean.get(Constants.PATIENT_ID));

    if (printMap != null && !printMap.isEmpty()) {
      if (printMap.get("print_urls") != null) {
        returnMap.put("print_urls", printMap.get("print_urls"));
      } else {
        returnMap.put("print_urls", Collections.EMPTY_LIST);
      }
    }

    returnMap.put("bill_no", bill != null ? bill.get("bill_no") : "");

    // Schedule Accounting for bill if it is finalized/closed
    if (bill != null && (((String) bill.get("status")).equals(Bill.BILL_STATUS_FINALIZED)
        || ((String) bill.get("status")).equals(Bill.BILL_STATUS_CLOSED))) {
      accountingJobScheduler.scheduleAccountingForBill((String) visitBean.get("patient_id"),
          (String) bill.get("bill_no"));
    }

    return returnMap;
  }

  /**
   * Send bill email.
   *
   * @param bill     the bill
   * @param billData the bill data
   */
  private void sendBillEmail(BasicDynaBean bill, Map<String, String> billData) {

    if (bill.get(BILL_TYPE).toString().equalsIgnoreCase(Bill.BILL_TYPE_PREPAID)
        && bill.get("visit_type").toString().equalsIgnoreCase(Bill.BILL_VISIT_TYPE_OP)) {
      if (bill.get(STATUS) != null
          && (bill.get(STATUS).toString().equalsIgnoreCase(Bill.BILL_STATUS_CLOSED)
              || bill.get(STATUS).toString().equalsIgnoreCase(Bill.BILL_STATUS_FINALIZED))) {
        BigDecimal netPatientDue = BillRepository.getNetPatientDue(bill.get("bill_no").toString());
        BigDecimal patientDue = BillRepository.getPatientDue(bill.get("bill_no").toString());
        if (netPatientDue.compareTo(BigDecimal.ONE) < 0
            || patientDue.compareTo(BigDecimal.ONE) < 0) {
          StringWriter writer = new StringWriter();// "BUILTIN_HTML"
          String template = null;
          if (bill.get(BILL_TYPE).toString().equals("P")) { // Bill Now
            template = (String) genericPreferencesService.getAllPreferences()
                .get("email_bill_now_template");
          } else { // Bill Later
            template = (String) genericPreferencesService.getAllPreferences()
                .get("email_bill_later_template");
          }
          String[] templateName = template.split("-");
          try {
            String[] returnVals = BillPrintHelper.processBillTemplate(writer,
                bill.get("bill_no").toString(), templateName[1], billData.get(USER_ID));
          } catch (Exception exc) {
            log.error("", exc);
          }
          String report = writer.toString();
          billData.put("_report_content", report);
          billData.put("_message_attachment", report);
          billData.put("message_attachment_name", "Bill_" + bill.get("bill_no").toString());
          billData.put("bill_no", bill.get("bill_no").toString());
          if (null != genericPreferencesService.getAllPreferences().get("email_bill_printer")) {
            billData.put("printtype", String.valueOf(
                (Integer) genericPreferencesService.getAllPreferences().get("email_bill_printer")));
          } else {
            billData.put("printtype", "0");
          }
          billData.put("category", "Bill");
          billData.put("bill_date", billData.get("payment_date"));
          if (bill.get("total_amount") == null) {
            billData.put("bill_amount", null);
          } else {
            billData.put("bill_amount", ((BigDecimal) bill.get("total_amount")).toString());
          }
          billData.put("receipient_id__", billData.get(Constants.MR_NO));
          billData.put("receipient_type__", "PATIENT");
          Map<String, Object> jobData = new HashMap<>();
          jobData.put(EVENT_DATA, billData);
          String userName = (String) sessionService.getSessionAttributes().get(USER_ID);
          jobData.put(USER_NAME, userName);
          jobData.put(SCHEMA, RequestContext.getSchema());
          if (!(Boolean) bill.get("is_tpa") && patientDue.intValue() == 0) {
            String eventId = "op_bn_cash_bill_paid_closed";
            jobData.put(EVENT_ID, eventId);
            jobService.scheduleImmediate(buildJob("BillEmailJob_" + billData.get("bill_no"),
                RegistrationSmsJob.class, jobData));
          }
          // Sharing of Bill over PHR Practo Drive
          if (netPatientDue.compareTo(BigDecimal.ONE) < 0) {
            String eventId = "op_bill_paid";
            jobData.put(EVENT_ID, eventId);
            log.info("Scheduling Immediate jobs for BillEmailJobPHR");
            jobService
                .scheduleImmediate(buildJob("BillEmailJobPHR_" + billData.get("bill_no").toString(),
                    RegistrationSmsJob.class, jobData));
          }
        }
      }
    }
  }

  /**
   * Send receipt sms.
   *
   * @param visitDetailsParams the visit details params
   * @param patientDetailsBean the patient details bean
   * @param visitDetailsBean   the visit details bean
   * @param billNo             the bill no
   * @param plansList          the plans list
   */
  private void sendReceiptSms(Map<String, Object> visitDetailsParams,
      BasicDynaBean patientDetailsBean, BasicDynaBean visitDetailsBean, String billNo,
      List<BasicDynaBean> plansList) {

    String referredBy = (null != visitDetailsParams.get("reference_docto_id"))
        ? visitDetailsParams.get("reference_docto_id").toString()
        : null;
    String doctorId = visitDetailsParams.get("doctor").toString();
    String userName = (String) sessionService.getSessionAttributes().get(USER_ID);
    Map<String, String> smsBillData = new HashMap<String, String>();
    try {
      smsBillData = populateTokenMap(patientDetailsBean, (Integer) visitDetailsBean.get(CENTER_ID1),
          referredBy, doctorId);
      smsBillData.put("lang_code",
          PatientDetailsDAO.getContactPreference(smsBillData.get(Constants.MR_NO)));
      // String messageFooterToken =
      // "SELECT message_footer from message_types WHERE
      // message_type_id = 'sms_bill_payment_received'";
      // String messageFooterTokenvalue =
      // DatabaseHelper.getString(messageFooterToken);
      // smsBillData.put("message_footer", messageFooterTokenvalue);
      smsBillData.put("bill_no", billNo);
      String estimateAmtStr = "";
      if (null != visitDetailsParams.get("estimateAmt")) {
        estimateAmtStr = visitDetailsParams.get("estimateAmt").toString();
      }
      if (null != plansList && plansList.isEmpty()) {
        smsBillData.put("amount_paid", estimateAmtStr);
      } else {
        smsBillData.put("amount_paid", (String) visitDetailsParams.get("patientAmt"));
      }
      smsBillData.put("total_amount", estimateAmtStr);
      smsBillData.put("patient_due", "0.00");

      String patientMobileNo = smsBillData.get("recipient_phone");
      smsBillData.put("recipient_mobile", patientMobileNo);
      String eventId = "bill_payment_message";
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put(SCHEMA, RequestContext.getSchema());
      jobData.put(EVENT_ID, eventId);
      jobData.put(EVENT_DATA, smsBillData);
      jobData.put(USER_NAME, userName);
      jobService.scheduleImmediate(buildJob("BillSmsJob_" + smsBillData.get("bill_no").toString(),
          RegistrationSmsJob.class, jobData));
      // mgr.processEvent("bill_payment_message", smsBillData);
    } catch (Exception exc) {
      log.error("Error while trying to send Payment Received SMS : " + exc.getMessage());
    }
  }

  /**
   * Populate token map.
   *
   * @param patientDetailsBean the patient details bean
   * @param centerId           the center id
   * @param referredBy         the referred by
   * @param doctorId           the doctor id
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, String> populateTokenMap(BasicDynaBean patientDetailsBean, Integer centerId,
      String referredBy, String doctorId) throws SQLException {
    Map<String, String> tokenMap = new HashMap<>();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar currDate = Calendar.getInstance();
    String currentDate = dateFormat.format(currDate.getTime());
    tokenMap.put("payment_date", currentDate);
    String mrNo = (String) patientDetailsBean.get(Constants.MR_NO);
    tokenMap.put(Constants.MR_NO, mrNo);
    tokenMap.put("receipient_id__", mrNo);
    String patientMobileNo = (String) patientDetailsBean.get("patient_phone");
    tokenMap.put("recipient_phone", patientMobileNo);
    tokenMap.put("recipient_mobile", patientMobileNo);
    String salId = (String) patientDetailsBean.get("salutation");
    String patientSalutation = "";
    String fullName = "";
    if (salId != null && !salId.equals("")) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("salutation_id", salId);
      BasicDynaBean salBean = salutationService.findByPk(filterMap);
      patientSalutation = (String) salBean.get("salutation");
    }
    if (patientSalutation != null && !patientSalutation.equals("")) {
      fullName = patientSalutation + " ";
    }
    if (patientDetailsBean.get("patient_name") != null) {
      fullName = fullName + patientDetailsBean.get("patient_name") + " ";
    }
    if (patientDetailsBean.get("middle_name") != null) {
      fullName = fullName + patientDetailsBean.get("middle_name") + " ";
    }
    if (patientDetailsBean.get("last_name") != null) {
      fullName = fullName + patientDetailsBean.get("last_name");
    }
    tokenMap.put("recipient_name", fullName);
    tokenMap.put("recipient_email", (String) patientDetailsBean.get("email_id"));
    String currency = genericPreferencesService.getAllPreferences().get("currency_symbol")
        .toString();
    tokenMap.put("currency_symbol", currency);
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(CENTER_ID1, centerId);
    BasicDynaBean centerData = centerService.findByPk(filterMap);
    String centerName = (String) centerData.get("center_name");
    tokenMap.put("center_name", centerName);
    String centerPhone = (String) centerData.get("center_contact_phone");
    tokenMap.put("center_contact_phone", centerPhone);
    String centerAddress = (String) centerData.get("center_address");
    tokenMap.put("center_address", centerAddress);
    String referralDoctor = "";
    if (referredBy != null && !referredBy.equals("")) {
      Map<String, Object> referralMap = new HashMap<>();
      filterMap.put("referal_no", referredBy);
      BasicDynaBean refBeanFromReferral = referralDoctorService.findByPk(referralMap);
      if (refBeanFromReferral != null) {
        referralDoctor = (String) refBeanFromReferral.get("referal_name");
      } else {
        Map<String, Object> doctorMap = new HashMap<>();
        doctorMap.put("doctor_id", referredBy);
        BasicDynaBean refBeanFromDoc = doctorService.findByPk(doctorMap);
        if (refBeanFromDoc != null) {
          referralDoctor = (String) refBeanFromDoc.get("doctor_name");
        }
      }
    }
    tokenMap.put("referal_doctor", referralDoctor);
    String concDoctor = "";
    if (doctorId != null && !doctorId.equals("")) {
      Map<String, Object> doctorMap = new HashMap<>();
      doctorMap.put("doctor_id", doctorId);
      concDoctor = (String) doctorService.findByPk(doctorMap).get("doctor_name");
    }
    tokenMap.put("doctor_name", concDoctor);
    tokenMap.put("receipient_type__", "PATIENT");
    return tokenMap;
  }

  /**
   * Save insurance. In order to copy the Main Visit Insurance details changes to
   * all Follow Up Visits, get all episode follow up visits. Delete All Insurance
   * Details for Main/follow up's Save Insurance Details
   * 
   * 
   * @param insuranceList    the insurance list
   * @param visitId          the visit id
   * @param isTpa            the is tpa
   * @param plansList        the plans list
   * @param visitDetailsBean the visit details bean
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean saveInsurance(ArrayList insuranceList, String visitId, boolean isTpa,
      List<BasicDynaBean> plansList, BasicDynaBean visitDetailsBean) {
    if (null != insuranceList && !insuranceList.isEmpty()) {
      isTpa = true;
      List<BasicDynaBean> allVisits = getMainAndFollowUpVisits(visitId);
      deleteAllInsuranceRecords(allVisits);
      for (int i = 0; i < insuranceList.size(); i++) {
        Map insParams = (Map) insuranceList.get(i);
        BasicDynaBean patInsPlanBean = setPlanDetails(insParams, i, visitDetailsBean);
        savePatientInsurancePlanDetails(patInsPlanBean, allVisits);
        plansList.add(patInsPlanBean);
        ArrayList<BasicDynaBean> planDetailBeanList = setDynamicCoPayValues(insParams,
            patInsPlanBean, visitDetailsBean);
        saveCoPayDetails(planDetailBeanList, allVisits);

        BasicDynaBean patInsPolicyDetailsBean = setPatientPolicydetails(insParams, visitDetailsBean,
            patInsPlanBean);
        savePatientPolicyDetails(patInsPolicyDetailsBean, allVisits, visitDetailsBean, i,
            patInsPlanBean);

        if (null != insParams.get(DOC_ID) && !insParams.get(DOC_ID).toString().isEmpty()) {
          int cardDocId = (Integer) insParams.get(DOC_ID);
          int patientPolicyId = (Integer) patInsPolicyDetailsBean.get(PATIENT_POLICY_ID);
          BasicDynaBean existingPlanDocBean = policyDocImgService.findByPK(cardDocId);
          if (existingPlanDocBean == null
              || ((Integer) existingPlanDocBean.get(PATIENT_POLICY_ID) != patientPolicyId)) {
            BasicDynaBean newDocBean = patientDocumentService.findByKey(cardDocId);
            Integer newDocId = patientDocumentService.getNextSequence();
            newDocBean.set(DOC_ID, newDocId);
            patientDocumentService.insert(newDocBean);
            if (newDocBean.get("is_migrated").equals("1")) {
              BasicDynaBean oldMinioDoc = minioPatientDocumentsService.findByDocId(cardDocId);
              minioPatientDocumentsService.insert(newDocId, (String) oldMinioDoc.get("path"));
            }
            cardDocId = newDocId;
          }
          BasicDynaBean planImgDocBean = policyDocImgService.getBean();
          planImgDocBean.set(DOC_ID, cardDocId);
          planImgDocBean.set(DOC_NAME, "Insurance Card");
          planImgDocBean.set("doc_date", DateUtil.getCurrentDate());
          planImgDocBean.set("username",
              (String) sessionService.getSessionAttributes().get(USER_ID));
          planImgDocBean.set(PATIENT_POLICY_ID, patientPolicyId);
          policyDocImgService.insert(planImgDocBean);
        } else {
          Map<String, Object> identifiers1 = new HashMap<>();
          identifiers1.put(Constants.PATIENT_ID, visitDetailsBean.get(Constants.PATIENT_ID));

          identifiers1.put(PRIORITY, i + 1);
          BasicDynaBean patInsuPlanBean = patientInsurancePlansService.findByKey(identifiers1);

          BasicDynaBean planImgDocBean = policyDocImgService.getBean();
          List<String> columns = new ArrayList<>();
          columns.add(DOC_ID);
          List<BasicDynaBean> beanList = policyDocImgService.listAll(columns, PATIENT_POLICY_ID,
              (Integer) patInsuPlanBean.get(PATIENT_POLICY_ID));
          Integer docId = null;
          if (!beanList.isEmpty()) {
            docId = (Integer) beanList.get(0).get(DOC_ID);
          }

          if (null != docId) {
            BasicDynaBean newDocBean = patientDocumentService.findByKey(docId);
            Integer newDocId = patientDocumentService.getNextSequence();
            newDocBean.set(DOC_ID, newDocId);
            planImgDocBean.set(DOC_ID, newDocId);
            planImgDocBean.set(DOC_NAME, "Insurance Card");
            planImgDocBean.set("doc_date", DateUtil.getCurrentDate());
            planImgDocBean.set(USERNAME,
                (String) sessionService.getSessionAttributes().get(USER_ID));
            planImgDocBean.set(PATIENT_POLICY_ID,
                (Integer) patInsPolicyDetailsBean.get(PATIENT_POLICY_ID));
            policyDocImgService.insert(planImgDocBean);
          }
        }
        updatePlanPolicy(patInsPlanBean);
      }
    }
    return isTpa;
  }

  /**
   * Check if co pay is edited.
   *
   * @param insuranceList the insurance list
   * @return true, if successful
   */
  private boolean checkIfCoPayisEdited(ArrayList insuranceList) {
    boolean insDetailsEdited = false;
    for (int i = 0; i < insuranceList.size(); i++) {
      Map insParams = (Map) insuranceList.get(i);
      if ((Boolean) getValue("insurance_edited", insParams)) {
        insDetailsEdited = true;
      }
    }
    return insDetailsEdited;
  }

  /**
   * Update plan policy.
   *
   * @param patInsPlanBean the pat ins plan bean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void updatePlanPolicy(BasicDynaBean patInsPlanBean) {
    Map keys = new HashMap();
    keys.put(Constants.PATIENT_ID, patInsPlanBean.get(Constants.PATIENT_ID));
    keys.put("plan_id", patInsPlanBean.get("plan_id"));
    patientInsurancePlansService.update(patInsPlanBean, keys);
  }

  /**
   * Update visit details.
   *
   * @param visitDetailsBean the visit details bean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void updateVisitDetails(BasicDynaBean visitDetailsBean) {
    Map keys = new HashMap();
    keys.put(Constants.PATIENT_ID, visitDetailsBean.get(Constants.PATIENT_ID));
    patientRegistrationRepository.update(visitDetailsBean, keys);
  }

  /**
   * Delete all insurance records.
   *
   * @param allVisits the all visits
   */
  private void deleteAllInsuranceRecords(List<BasicDynaBean> allVisits) {
    for (BasicDynaBean visitBean : allVisits) {
      patientInsurancePolicyDetailsService.delete((String) visitBean.get(Constants.PATIENT_ID));
      patientInsurancePlansService.delete((String) visitBean.get(Constants.PATIENT_ID));
      patientInsurancePlanDetailsService.delete((String) visitBean.get(Constants.PATIENT_ID));
    }
  }

  /**
   * Sets the plan details.
   *
   * @param params           the params
   * @param index            the index
   * @param visitDetailsBean the visit details bean
   * @return the basic dyna bean
   */
  @SuppressWarnings("rawtypes")
  private BasicDynaBean setPlanDetails(Map<String, String[]> params, int index,
      BasicDynaBean visitDetailsBean) {
    BasicDynaBean patPlanBean = patientInsurancePlansService.getBean();
    ArrayList errors = new ArrayList();
    ConversionUtils.copyJsonToDynaBean(params, patPlanBean, errors, false);
    patPlanBean.set(PRIORITY, index + 1);
    patPlanBean.set(Constants.MR_NO, visitDetailsBean.get(Constants.MR_NO));
    if (index == 0) {
      visitDetailsBean.set("primary_sponsor_id", patPlanBean.get("sponsor_id"));
      visitDetailsBean.set("primary_insurance_co", patPlanBean.get("insurance_co"));
      visitDetailsBean.set("primary_insurance_approval", patPlanBean.get("insurance_approval"));
      visitDetailsBean.set("prior_auth_id", patPlanBean.get("prior_auth_id"));
      visitDetailsBean.set("prior_auth_mode_id", patPlanBean.get("prior_auth_mode_id"));
      visitDetailsBean.set(USE_DRG,
          (null != patPlanBean.get(USE_DRG) && patPlanBean.get(USE_DRG).equals("") ? "N"
              : patPlanBean.get(USE_DRG)));
      if (patPlanBean.get(USE_PERDIEM) != null && patPlanBean.get(USE_PERDIEM).equals("")) {
        visitDetailsBean.set(USE_PERDIEM, patPlanBean.get(USE_PERDIEM));
      }
      visitDetailsBean.set("plan_id", patPlanBean.get("plan_id"));
      visitDetailsBean.set("category_id", patPlanBean.get("plan_type_id"));
    }
    if (index == 1) {
      visitDetailsBean.set("secondary_sponsor_id", patPlanBean.get("sponsor_id"));
      visitDetailsBean.set("secondary_insurance_co", patPlanBean.get("insurance_co"));
      visitDetailsBean.set("secondary_insurance_approval", patPlanBean.get("insurance_approval"));
      if (patPlanBean.get(USE_PERDIEM) != null && patPlanBean.get(USE_PERDIEM).equals("")) {
        visitDetailsBean.set(USE_PERDIEM, patPlanBean.get(USE_PERDIEM));
      }
    }
    return patPlanBean;
  }

  /**
   * Save patient insurance plan details.
   *
   * @param patInsPlanBean the pat ins plan bean
   * @param allVisits      the all visits
   */
  private void savePatientInsurancePlanDetails(BasicDynaBean patInsPlanBean,
      List<BasicDynaBean> allVisits) {
    for (BasicDynaBean visitBean : allVisits) {
      patInsPlanBean.set(Constants.PATIENT_ID, visitBean.get(Constants.PATIENT_ID));
      patientInsurancePlansService.insert(patInsPlanBean);
    }
  }

  /**
   * Sets the patient policydetails.
   *
   * @param params           the params
   * @param visitDetailsBean the visit details bean
   * @param patInsPlanBean   the pat ins plan bean
   * @return the basic dyna bean
   */
  @SuppressWarnings("rawtypes")
  private BasicDynaBean setPatientPolicydetails(Map<String, String[]> params,
      BasicDynaBean visitDetailsBean, BasicDynaBean patInsPlanBean) {
    BasicDynaBean patInsPolicyDetailsBean = patientInsurancePolicyDetailsService.getBean();
    ArrayList errors = new ArrayList();
    ConversionUtils.copyJsonToDynaBean(params, patInsPolicyDetailsBean, errors, false);
    patInsPolicyDetailsBean.set("plan_id", patInsPlanBean.get("plan_id"));
    patInsPolicyDetailsBean.set(Constants.MR_NO, visitDetailsBean.get(Constants.MR_NO));
    patInsPolicyDetailsBean.set(STATUS, "A");
    String memberId = (String) patInsPolicyDetailsBean.get("member_id");
    patInsPolicyDetailsBean.set("member_id", memberId != null ? memberId.trim() : null);
    return patInsPolicyDetailsBean;
  }

  /**
   * Save patient policy details.
   *
   * @param patInsPolicyDetailsBean the pat ins policy details bean
   * @param allVisits               the all visits
   * @param visitDetailsBean        the visit details bean
   * @param index                   the index
   * @param patInsPlanBean          the pat ins plan bean
   */
  private void savePatientPolicyDetails(BasicDynaBean patInsPolicyDetailsBean,
      List<BasicDynaBean> allVisits, BasicDynaBean visitDetailsBean, int index,
      BasicDynaBean patInsPlanBean) {
    if (null != allVisits && allVisits.size() > 0) {
      for (BasicDynaBean visitBean : allVisits) {
        patInsPolicyDetailsBean.set("visit_id", visitBean.get(Constants.PATIENT_ID));
        patientInsurancePolicyDetailsService.insert(patInsPolicyDetailsBean);
        patInsPlanBean.set(PATIENT_POLICY_ID, patInsPolicyDetailsBean.get(PATIENT_POLICY_ID));
      }
    }
  }

  /**
   * Sets the dynamic co pay values.
   *
   * @param params           the params
   * @param patInsPlanBean   the pat ins plan bean
   * @param visitDetailsBean the visit details bean
   * @return the array list
   */
  @SuppressWarnings("rawtypes")
  private ArrayList<BasicDynaBean> setDynamicCoPayValues(Map<String, Object> params,
      BasicDynaBean patInsPlanBean, BasicDynaBean visitDetailsBean) {
    ArrayList errors = new ArrayList();
    ArrayList<BasicDynaBean> patInsPlanDetailBeanList = new ArrayList<BasicDynaBean>();
    ArrayList categoryIds = (ArrayList) params.get("insurance_plan_details");
    for (int i = 0; i < categoryIds.size(); i++) {
      Map insuDetailsParams = (Map) categoryIds.get(i);
      BasicDynaBean patInsPlanDetailBean = patientInsurancePlanDetailsService.getBean();
      ConversionUtils.copyJsonToDynaBean(insuDetailsParams, patInsPlanDetailBean, errors, false);
      patInsPlanDetailBean.set("plan_id", patInsPlanBean.get("plan_id"));
      patInsPlanDetailBean.set("visit_id", visitDetailsBean.get(Constants.PATIENT_ID));
      patInsPlanDetailBean.set(PATIENT_TYPE, visitDetailsBean.get("visit_type"));
      patInsPlanDetailBean.set("patient_insurance_plans_id",
          patInsPlanBean.get("patient_insurance_plans_id"));
      patInsPlanDetailBeanList.add(patInsPlanDetailBean);
    }
    return patInsPlanDetailBeanList;
  }

  /**
   * Save co pay details.
   *
   * @param patInsPlanDetailBeanList the pat ins plan detail bean list
   * @param allVisits                the all visits
   */
  private void saveCoPayDetails(List<BasicDynaBean> patInsPlanDetailBeanList,
      List<BasicDynaBean> allVisits) {
    for (BasicDynaBean visitBean : allVisits) {
      for (BasicDynaBean patInsPlanDetails : patInsPlanDetailBeanList) {
        patInsPlanDetails.set("visit_id", visitBean.get(Constants.PATIENT_ID));
        patientInsurancePlanDetailsService.insert(patInsPlanDetails);
      }
    }
  }

  /**
   * Gets the visit details.
   *
   * @param visitId the visit id
   * @return the visit details
   */
  public BasicDynaBean getVisitDetails(String visitId) {
    return patientRegistrationRepository.getVisitDetails(visitId);
  }
  
  /**
   * Gets the visit details with referral doctor.
   *
   * @param visitId the visit id
   * @return the visit details
   */
  public BasicDynaBean getVisitDetailsWithReferralDoctor(String visitId) {
    return patientRegistrationRepository.getVisitDetailsWithReferralDoctor(visitId);
  }

  /**
   * Gets the main and follow up visits.
   *
   * @param visitId the visit id
   * @return the main and follow up visits
   */
  public List<BasicDynaBean> getMainAndFollowUpVisits(String visitId) {

    BasicDynaBean visitDetailsBean = patientRegistrationRepository.findByKey(Constants.PATIENT_ID,
        visitId);
    List<BasicDynaBean> followupVisits = new ArrayList<>();
    String opType = visitDetailsBean.get(OP_TYPE) != null ? (String) visitDetailsBean.get(OP_TYPE)
        : OP_TYPE_MAIN;
    if (opType != null && (opType.equals(OP_TYPE_MAIN) || opType.equals(OP_TYPE_REVISIT))) {
      followupVisits = patientRegistrationRepository.getEpisodeAllFollowUpVisitsOnly(visitId);
    }
    // Main Visit
    BasicDynaBean mainVisit = patientRegistrationRepository.findByKey(Constants.PATIENT_ID,
        visitId);

    // List of all visits (Follow up and the Main visit)
    List<BasicDynaBean> allVisits = new ArrayList<>();
    allVisits.add(mainVisit);

    if (followupVisits != null && !followupVisits.isEmpty()) {
      allVisits.addAll(followupVisits);
    }
    return allVisits;
  }

  /**
   * Gets the center id.
   *
   * @param visitId the visit id
   * @return the center id
   */
  public int getCenterId(String visitId) {
    Object[] obj = new Object[] { visitId, visitId, visitId };
    return patientRegistrationRepository.getCenterId(obj);
  }

  /**
   * Find by key.
   *
   * @param visitId the visit id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String visitId) {
    return patientRegistrationRepository.findByKey(Constants.PATIENT_ID, visitId);
  }

  /**
   * Checks if is reg validity expired.
   *
   * @param visitDetailsBean the visit details bean
   * @param mrno             the mrno
   * @return true, if is reg validity expired
   */
  public boolean isRegValidityExpired(BasicDynaBean visitDetailsBean, String mrno) {
    boolean isRenewal = false;
    if (null != mrno && !mrno.equals("")) {
      BasicDynaBean regDates = patientDetailsService.getPreviousRegDateChargeAccepted(mrno);
      if (regDates == null) {
        return isRenewal;
      }
      Date previousRegDate = (Date) regDates.get("visit_reg_date");
      DateUtil dateUtil = new DateUtil();
      if (previousRegDate != null) {
        BasicDynaBean regPrefbean = regPrefService.getRegistrationPreferences();
        String regValidityPeriod = regPrefbean.get("reg_validity_period").toString();
        String dateDiff = DataBaseUtil.dateDiff(
            dateUtil.getDateFormatter().format(DateUtil.getCurrentDate()),
            dateUtil.getDateFormatter().format(previousRegDate));
        if (Integer.parseInt(dateDiff) >= Integer.parseInt(regValidityPeriod)) {
          visitDetailsBean.set("reg_charge_accepted", "Y");
          isRenewal = true;
        } else {
          visitDetailsBean.set("reg_charge_accepted", "N");
        }
      } else {
        // there is no previous visit: this could be migrated data. Check based
        // on patient reg_date
        Date regDate = (Date) regDates.get("patient_reg_date");
        Date goLiveDate = (Date) genPrefService.getAllPreferences().get("go_live_date");
        if (regDate != null && (regDate.compareTo(goLiveDate) < 0)) {
          // for uploaded patients: first_visit_reg_date is optional, if the
          // user fills the
          // first_visit_reg_date take that or insert the uploaded date as the
          // first_visit_reg_date
          // patient registered before the system went live: belongs to previous
          // software.
          // Assume that registration is still valid, so don't charge again.
          visitDetailsBean.set("reg_charge_accepted", "N");
        } else {
          // first visit registration date will be null for the patients who has
          // done using
          // pre-registration.
          // No previous visit, so it is a pre-reg patient. Charge normal (not
          // renewal)
          visitDetailsBean.set("reg_charge_accepted", "Y");
        }
      }
    } else {
      // new registration, add the reg charge
      visitDetailsBean.set("reg_charge_accepted", "Y");
    }
    return isRenewal;
  }

  /**
   * Gets the param default.
   *
   * @param visitDetailsParams the visit details params
   * @param paramName          the param name
   * @param defaultValue       the default value
   * @return the param default
   */
  public String getParamDefault(Map<String, Object> visitDetailsParams, String paramName,
      String defaultValue) {
    String value = (String) visitDetailsParams.get(paramName);
    if ((value == null) || value.trim().equals("")) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Gets the sponsor details.
   *
   * @param tpaId      the tpa id
   * @param categoryId the category id
   * @return the sponsor details
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map getSponsorDetails(String tpaId, String categoryId) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    Integer maxCentersIncDefault = (Integer) genPrefService.getPreferences()
        .get(MAX_CENTERS_DEFAULT);
    Map map = new HashMap();
    map.put("tpa_id", tpaId);
    BasicDynaBean bean = tpaService.getDetails(tpaId);
    map = new HashMap<String, Object>(bean.getMap());
    List<BasicDynaBean> insuCompList = insuranceCompanyService.getMappedInsuranceCompanies(tpaId,
        categoryId);
    List<Map> insMapList = new ArrayList<>();
    for (int i = 0; (insuCompList != null && i < insuCompList.size()); i++) {
      Map insMap = new HashMap<String, Object>(insuCompList.get(i).getMap());
      List<BasicDynaBean> planTypesList;
      if (centerId != 0 && maxCentersIncDefault > 1) {
        planTypesList = insurancePlanTypeService
            .getMappedPlanTypes((String) insuCompList.get(i).get(INSURANCE_CO_ID), centerId);
      } else {
        planTypesList = insurancePlanTypeService
            .getMappedPlanTypes((String) insuCompList.get(i).get(INSURANCE_CO_ID), null);
      }
      List<Map> planTypesMapList = new ArrayList<>();
      for (int j = 0; (planTypesList != null && j < planTypesList.size()); j++) {
        Map planTypeMap = new HashMap<String, Object>(planTypesList.get(j).getMap());
        planTypeMap.put("plan_names", ConversionUtils.listBeanToListMap(insurancePlanService
            .getMappedPlans((Integer) planTypesList.get(j).get("category_id"), tpaId)));
        planTypesMapList.add(planTypeMap);
      }
      insMap.put("network_plan_types", planTypesMapList);
      insMapList.add(insMap);
    }
    map.put("insurance_companies", insMapList);
    return map;
  }

  /**
   * Gets the sponsordet.
   *
   * @param tpaId          the tpa id
   * @param categoryString the category string
   * @return the sponsordet
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map getSponsordet(String tpaId, String categoryString) {
    Integer categoryId = null;
    if (categoryString != null && !categoryString.isEmpty()) {
      categoryId = Integer.parseInt(categoryString);
    }
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    Integer maxCentersIncDefault = (Integer) genPrefService.getPreferences()
        .get(MAX_CENTERS_DEFAULT);
    if (!(centerId != 0 && maxCentersIncDefault > 1)) {
      centerId = null;
    }
    BasicDynaBean sponsorBean = tpaService.getDetails(tpaId);
    if (sponsorBean == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    List<BasicDynaBean> insuranceCompaniesList = insuranceCompanyService
        .getMappedInsuranceCompanies(tpaId, categoryString);
    List<BasicDynaBean> networkPlanTypesList = insurancePlanTypeService
        .getPlanTypesForSponsor(tpaId, categoryId, centerId);
    List<BasicDynaBean> planNamesList = insurancePlanService.getPlanNamesForSponsor(tpaId,
        categoryId, centerId);
    Map<Object, Object> networkPlansMap = new HashMap<>();
    Map<Object, Object> insuranceCompaniesMap = new HashMap<>();
    for (BasicDynaBean planBean : planNamesList) {
      Integer networkPlanId = (Integer) planBean.get("category_id");
      List<Map> planList = (List<Map>) networkPlansMap.get(networkPlanId);
      if (planList == null) {
        List<Map> list = new ArrayList<>();
        list.add(planBean.getMap());
        networkPlansMap.put(networkPlanId, list);
      } else {
        planList.add(planBean.getMap());
        networkPlansMap.put(networkPlanId, planList);
      }
    }
    for (BasicDynaBean networkPlanBean : networkPlanTypesList) {
      String insuCompId = (String) networkPlanBean.get(INSURANCE_CO_ID);
      List<Map> planTypeList = (List<Map>) insuranceCompaniesMap.get(insuCompId);
      List<Map> planNames = (List<Map>) networkPlansMap.get(networkPlanBean.get("category_id"));
      Map map = new HashMap(networkPlanBean.getMap());
      if (planNames == null) {
        continue;
      }
      map.put("plan_names", planNames);
      if (planTypeList == null) {
        List<Map> list = new ArrayList<>();
        list.add(map);
        insuranceCompaniesMap.put(insuCompId, list);
      } else {
        planTypeList.add(map);
        insuranceCompaniesMap.put(insuCompId, planTypeList);
      }
    }
    List<Map> finalInsuCompList = new ArrayList<>();
    for (BasicDynaBean insuCompBean : insuranceCompaniesList) {
      String insuCompId = (String) insuCompBean.get(INSURANCE_CO_ID);
      List<Map> planTypeList = (List<Map>) insuranceCompaniesMap.get(insuCompId);
      Map map = new HashMap(insuCompBean.getMap());
      map.put("network_plan_types", planTypeList != null ? planTypeList : Collections.EMPTY_LIST);
      finalInsuCompList.add(map);
    }
    Map<String, Object> sponsorMap = new HashMap<>(sponsorBean.getMap());
    sponsorMap.put("insurance_companies", finalInsuCompList);
    BasicDynaBean healthAuthTpaBean = tpaService.getTpaHealthAuthorityDetails(tpaId, centerId);
    if (healthAuthTpaBean != null) {
      sponsorMap.put("enable_eligibility_authorization",
          healthAuthTpaBean.get("enable_eligibility_authorization"));
    }
    sponsorMap.put("eligibility_authorization_status",
        ConversionUtils.listBeanToListMap(eligibilityAuthService.listAll()));
    return sponsorMap;
  }

  /**
   * Gets the plan details.
   *
   * @param planId    the plan id
   * @param visitType the visit type
   * @param map       the map
   * @return the plan details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getPlanDetails(String planId, String visitType,
      Map<String, Object> map) {
    List<BasicDynaBean> beans = insurancePlanDetailsService
        .getMappedPlanDetails(Integer.parseInt(planId), visitType);
    return (Map<String, Object>) map.put("plan_details", ConversionUtils.listBeanToListMap(beans));
  }

  /**
   * View ins card image for visit.
   *
   * @param mapping the mapping
   * @param af      the af
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  @SuppressWarnings({ "rawtypes" })
  public ActionForward viewInsCardImageForVisit(ActionMapping mapping, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    String sponsorType = req.getParameter("sponsorType").trim();
    String sponsorIndex = req.getParameter("sponsorIndex");
    String visitId = req.getParameter("visitId");
    String policyIDstr = req.getParameter(PATIENT_POLICY_ID);
    Integer policyID = (policyIDstr == null || policyIDstr.equals("")) ? 0
        : Integer.parseInt(policyIDstr);

    OutputStream os = res.getOutputStream();
    Map cardMap = sponsorIndex.equals("P")
        ? PatientDetailsDAO.getCurrentPatientInsCardImageMap(visitId, sponsorType, policyID)
        : PatientDetailsDAO.getCurrentPatientInsSecCardImageMap(visitId, sponsorType, policyID);

    InputStream stream = cardMap != null ? (InputStream) cardMap.get("CONTENT") : null;
    String contentType = cardMap != null ? (String) cardMap.get("CONTENT_TYPE") : "image/png";
    res.setContentType(contentType);

    if (stream != null) {
      byte[] bytes = new byte[1024];
      int len = 0;
      while ((len = stream.read(bytes)) > 0) {
        os.write(bytes, 0, len);
      }
      os.flush();
      stream.close();
      return null;
    } else {
      return mapping.findForward("error");
    }

  }

  /**
   * Gets the insurance document.
   *
   * @param visitId         the visit id
   * @param patientPolicyId the patient policy id
   * @param planName        the plan name
   * @return the insurance document
   */
  public byte[] getInsuranceDocument(String visitId, int patientPolicyId, String planName) {
    BasicDynaBean insDocBean = patientInsurancePlansService
        .getInsuranceDocument(new Object[] { visitId, patientPolicyId });
    if (insDocBean != null && insDocBean.get("doc_content_bytea") != null) {
      return (byte[]) insDocBean.get("doc_content_bytea");
    } else if (insDocBean == null) {
      throw new EntityNotFoundException(new String[] { "Document", "Plan Name", planName });
    } else {
      throw new EntityNotFoundException(new String[] { "Document", "Plan Name", planName });
    }
  }

  /**
   * Gets the main visit id.
   *
   * @param visitId the visit id
   * @return the main visit id
   */
  public String getMainVisitId(String visitId) {
    return patientRegistrationRepository.getMainVisitId(new Object[] { visitId });
  }

  /**
   * Gets the rate plans.
   *
   * @param categoryId the category id
   * @param planId     the plan id
   * @param insCompId  the ins comp id
   * @return the rate plans
   */
  @SuppressWarnings({ "rawtypes" })
  public List getRatePlans(String categoryId, String planId, String insCompId) {
    registrationValidator.validateGetRatePlan(categoryId, planId);
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);
    List<BasicDynaBean> ratePlansBean = new ArrayList<BasicDynaBean>();
    if (planId != null && !planId.equals("")) {
      ratePlansBean = insurancePlanService.getPlanDefaultRatePlan(Integer.parseInt(planId));
      if (ratePlansBean != null && !ratePlansBean.isEmpty() && categoryId != null
          && !categoryId.equals("")) {
        BasicDynaBean patCatRatePlanBean = orgService.checkForOpAllowedRatePlans(
            (String) ratePlansBean.get(0).get(Constants.ORG_ID), Integer.parseInt(categoryId));
        if (patCatRatePlanBean == null
            || (patCatRatePlanBean != null && patCatRatePlanBean.getMap().isEmpty())) {
          throw new ValidationException(
              "js.registration.patient.valid.rate.plans.against.category.plan.insurance.company");
        }
      }
    }
    if ((null == ratePlansBean || (ratePlansBean != null && ratePlansBean.isEmpty()))
        && insCompId != null && !insCompId.equals("")) {
      ratePlansBean = insuranceCompanyService.getInsCompDefaultRatePlan(insCompId, categoryId);
    }
    if (null == ratePlansBean || (ratePlansBean != null && ratePlansBean.isEmpty())) {
      ratePlansBean = patientCatService.getPatCategoryDefaultRatePlan(categoryId, centerId);
    }
    return ratePlansBean;
  }

  /**
   * Gets the orders info.
   *
   * @return the orders info
   */
  private Map<String, Object> getOrdersInfo() {
    Map<String, Object> basicInfo = new HashMap<String, Object>();
    basicInfo.put("service_groups",
        ConversionUtils.listBeanToListMap(serviceGroupService.lookup(true)));
    basicInfo.put("service_sub_groups",
        ConversionUtils.listBeanToListMap(serviceSubGroupService.listOrderActiveRecord()));
    basicInfo.put("all_doctor_consultation_types",
        ConversionUtils.listBeanToListMap(consultationTypesService.getConsultationTypes()));
    basicInfo.put("charge_heads",
            ConversionUtils.listBeanToListMap(chargeHeadsService.lookup(false)));
    return basicInfo;
  }

  /**
   * Gets the bill patient info.
   *
   * @param patientId the patient id
   * @param billNo    the bill no
   * @return the bill patient info
   */
  public BasicDynaBean getBillPatientInfo(String patientId, String billNo) {
    return patientRegistrationRepository.getPatientBillInfo(patientId, billNo);
  }

  /**
   * Gets the patient info.
   *
   * @param patientId the patient id
   * @return the patient info
   */
  public BasicDynaBean getPatientInfo(String patientId) {
    return patientRegistrationRepository.getPatientInfo(patientId);
  }

  /**
   * Estimate amount.
   *
   * @param requestBody the request body
   * @return the map
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("rawtypes")
  public Map estimateAmount(ModelMap requestBody) throws SQLException {
    return billService.estimateAmount(requestBody);
  }

  /**
   * Gets the visit ins details.
   *
   * @param insuranceList the insurance list
   * @param visitId       the visit id
   * @param visitParams   the visit params
   * @return the visit ins details
   */
  @SuppressWarnings("rawtypes")
  public List<BasicDynaBean> getVisitInsDetails(ArrayList insuranceList, String visitId,
      Map<String, Object> visitParams) {

    List<BasicDynaBean> visitInsDetails = new ArrayList<BasicDynaBean>();
    setVisitInsDetails(insuranceList, visitInsDetails, visitId, visitParams);
    return visitInsDetails;
  }

  /**
   * Sets the visit ins details.
   *
   * @param insuranceList   the insurance list
   * @param visitInsDetails the visit ins details
   * @param visitId         the visit id
   * @param visitParams     the visit params
   */
  @SuppressWarnings("rawtypes")
  private void setVisitInsDetails(ArrayList insuranceList, List<BasicDynaBean> visitInsDetails,
      String visitId, Map<String, Object> visitParams) {

    String opType = (String) getValue("op_type", visitParams);
    int planId = 0;
    for (int i = 0; i < insuranceList.size(); i++) {
      Map insParams = (Map) insuranceList.get(i);
      ArrayList categoryIds = (ArrayList) insParams.get("insurance_plan_details");
      for (int j = 0; j < categoryIds.size(); j++) {
        BasicDynaBean bean = visitInsuDetailsRepo.getBean();
        ConversionUtils.copyJsonToDynaBean(insParams, bean, null, false);

        String limitIncludeFollowUp = (String) getValue("limits_include_followup", insParams);

        if (null != limitIncludeFollowUp && !limitIncludeFollowUp.equals("")
            && limitIncludeFollowUp.equals("Y")) {
          bean.set("visit_limit",
              getValue("episode_limit", insParams, true) != null
                  && !getValue("episode_limit", insParams, true).equals("")
                      ? new BigDecimal(String.valueOf(getValue("episode_limit", insParams)))
                      : BigDecimal.ZERO);
          bean.set("visit_deductible",
              getValue("episode_deductible", insParams, true) != null
                  && !getValue("episode_deductible", insParams, true).equals("")
                      ? new BigDecimal(String.valueOf(getValue("episode_deductible", insParams)))
                      : BigDecimal.ZERO);
          bean.set("visit_copay_percentage",
              getValue("episode_copay_percentage", insParams, true) != null
                  && !getValue("episode_copay_percentage", insParams, true).equals("")
                      ? new BigDecimal(
                          String.valueOf(getValue("episode_copay_percentage", insParams)))
                      : BigDecimal.ZERO);
          bean.set("visit_max_copay_percentage",
              getValue("episode_max_copay_percentage", insParams, true) != null
                  && !getValue("episode_max_copay_percentage", insParams, true).equals("")
                      ? new BigDecimal(
                          String.valueOf(getValue("episode_max_copay_percentage", insParams)))
                      : BigDecimal.ZERO);
          bean.set("limits_include_followup", limitIncludeFollowUp);
        }

        planId = (Integer) bean.get("plan_id");
        if (null != opType && !opType.equals("") && (opType.equals(OP_TYPE_FOLLOWUP)
            || opType.equals("D"))) {
          Integer prevVisitPlan = (Integer) getValue("previous_plan", insParams, true);
          // default value -1
          planId = prevVisitPlan != null && prevVisitPlan > 0 ? prevVisitPlan
              : (Integer) bean.get("plan_id");
        }

        Map insuDetailsParams = (Map) categoryIds.get(j);
        ConversionUtils.copyJsonToDynaBean(insuDetailsParams, bean, null, false);

        Map filterKey = new HashMap();
        filterKey.put("plan_id", planId);
        BasicDynaBean planBean = insurancePlanService.findByPk(filterKey);

        String copayApplOnPostDiscountedAmt = isCopayApplOnPostDiscountedAmt(planBean);
        bean.set("is_copay_pc_on_post_discnt_amt", copayApplOnPostDiscountedAmt);

        String limitType = getPlanLimitType(planBean);

        String planCategoryPayable = isPlanCategoryPayable(planId,
            (Integer) bean.get("insurance_category_id"));
        bean.set("is_category_payable", insuDetailsParams.get("category_payable").equals("Y"));
        bean.set("plan_category_payable", planCategoryPayable == null ? "" : planCategoryPayable);
        bean.set("limit_type", limitType);
        bean.set("visit_id", visitId);
        bean.set("plan_id", planId);
        bean.set(PATIENT_TYPE, "o");
        bean.set(PRIORITY, i + 1);
        visitInsDetails.add(bean);
      }
    }
  }

  /**
   * Get insurance plan limit type.
   *
   * @param planBean the plan bean
   * @return the string
   */
  private String getPlanLimitType(BasicDynaBean planBean) {
    String limitType = "C";

    if (null != planBean && !planBean.getMap().isEmpty()) {
      if (null != planBean.get("limit_type")) {
        limitType = (String) planBean.get("limit_type");
      }
    }

    return limitType;
  }

  /**
   * Checks if is copay appl on post discounted amt.
   *
   * @param planBean the plan bean
   * @return the string
   */
  private String isCopayApplOnPostDiscountedAmt(BasicDynaBean planBean) {
    String copayApplOnPostDiscAmt = "Y";

    if (null != planBean && !planBean.getMap().isEmpty()) {
      if (null != planBean.get("is_copay_pc_on_post_discnt_amt")) {
        copayApplOnPostDiscAmt = (String) planBean.get("is_copay_pc_on_post_discnt_amt");
      }
    }
    return copayApplOnPostDiscAmt;
  }

  /**
   * Checks if is plan category payable.
   *
   * @param planId     the plan id
   * @param categoryId the category id
   * @return the string
   */
  private String isPlanCategoryPayable(int planId, int categoryId) {
    String planCategoryPayable = null;
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("plan_id", planId);
    keys.put("insurance_category_id", categoryId);
    keys.put(PATIENT_TYPE, "o");
    List<BasicDynaBean> bean = insurancePlanDetailsService.listAll(keys);
    if (bean != null && !bean.isEmpty()) {
      planCategoryPayable = (String) bean.get(0).get("category_payable");
    }
    return planCategoryPayable;
  }

  /**
   * Gets the bill nos.
   *
   * @param visitId the visit id
   * @return the bill nos
   */
  public List<BasicDynaBean> getBillNos(String visitId) {
    List<String> columns = new ArrayList<String>();
    columns.add("bill_no");
    columns.add("is_tpa");
    columns.add("is_primary_bill");
    columns.add(BILL_TYPE);
    columns.add(STATUS);
    columns.add("total_amount");
    columns.add("total_claim");
    columns.add("total_amount - total_claim AS patient_amount");
    return billService.listAll(columns, "visit_id", visitId);
  }

  /**
   * Gets the bill or receipt for patient payments.
   *
   * @param params the params
   * @return the bill or receipt for patient payments
   */
  public List<BillOrReceipt> getBillOrReceiptForPatientPayments(
      MultiValueMap<String, String> params) {
    return billService.getBillOrReceiptForPatientPayments(params);
  }

  /**
   * Gets the visit type.
   *
   * @param visitId the visit id
   * @return the visit type
   */
  public String getVisitType(String visitId) {
    return patientRegistrationRepository.getVisitType(new Object[] { visitId });
  }

  /**
   * Gets the policy details.
   *
   * @param mrNo the mr no
   * @return the policy details
   */
  public List<BasicDynaBean> getPolicyDetails(String mrNo) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    String defaultVisitDetailsAcrossCenter = (String) regPrefService.getRegistrationPreferences()
        .get("default_visit_details_across_center");
    return patientInsurancePlansService.getPolicyDetails(mrNo,
        defaultVisitDetailsAcrossCenter.equals("Y") ? null : centerId);
  }

  /**
   * Gets the visit rate plan.
   *
   * @param visitId   the visit id
   * @param visitType the visit type
   * @return the visit rate plan
   */
  public BasicDynaBean getVisitRatePlan(String visitId, String visitType) {
    return patientRegistrationRepository.getVisitRatePlan(visitId, visitType);
  }

  /**
   * Gets the visit rate plan.
   *
   * @param visitId the visit id
   * @return the visit rate plan
   */
  public BasicDynaBean getVisitRatePlan(String visitId) {
    return patientRegistrationRepository.getVisitRatePlan(visitId);
  }

  /**
   * Gets the store code.
   *
   * @param integrationName the integration name
   * @return the store code
   */
  private int getStoreCode(String integrationName) {
    int storeCode = 0;
    if (!integrationName.isEmpty()) {
      BasicDynaBean bean = instaIntegrationService.getCenterIntegrationDetails(integrationName,
          RequestContext.getCenterId());
      if (bean != null && bean.get(STORE_CODE) != null) {
        String storeCodeStr = (String) bean.get(STORE_CODE);
        storeCode = Integer.valueOf(storeCodeStr);
      }
    }

    return storeCode;
  }

  /**
   * Gets the department unit by rules.
   *
   * @param deptId the dept id
   * @return the department unit by rules
   */
  public BasicDynaBean getDepartmentUnitByRules(String deptId) {
    return departmentUnitService.getDepartmentUnitByRules(deptId);
  }

  /**
   * Gets the patient visits having order.
   *
   * @param mrNo      the mr no
   * @param visitType the visit type
   * @return the patient visits having order
   */
  public List<BasicDynaBean> getPatientVisitsHavingOrder(String mrNo, String visitType) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get(CENTER_ID);
    return patientRegistrationRepository.getPatientVisitsHavingOrder(mrNo, visitType, centerId);
  }

  /**
   * Gets the patient details.
   *
   * @param visitIdList the visit id list
   * @param centerId    the center id
   * @return the patient details
   */
  public List<BasicDynaBean> getPatientDetails(List<String> visitIdList, Integer centerId) {
    return patientRegistrationRepository.getPatientDetails(visitIdList, centerId);
  }

  /**
   * List all package items.
   *
   * @param packageId the package id
   * @return the order package details
   */
  public List<BasicDynaBean> getOrderPackageDetails(String packageId) {
    return packagesService.getPackageComponentDetails(Integer.parseInt(packageId));
  }

  /**
   * This method is used to get referral doctor name for patient id.
   *
   * @param patientId the patient id
   * @return the visit referral doctor name
   */
  public String getVisitReferralDoctorName(String patientId) {
    return patientRegistrationRepository.getVisitReferralDoctorName(patientId);
  }

  /**
   * This method used to get patient details bean including bed type value.
   *
   * @param patientId the patient id
   * @return returns BasicDynaBean
   */
  public BasicDynaBean getPatientBeanWithBedTypeValue(String patientId) {
    return patientRegistrationRepository.getPatientBeanWithBedType(patientId);
  }

  /**
   * Gets the ip credit limit amount.
   *
   * @param visitId the visit id
   * @return the ip credit limit amount
   */
  public BigDecimal getIpCreditLimitAmount(String visitId) {
    BigDecimal creditLimitAmount = patientRegistrationRepository.getIpCreditLimitAmount(visitId);
    return creditLimitAmount == null ? BigDecimal.ZERO : creditLimitAmount;
  }

  /**
   * Gets the patient visit details map.
   *
   * @param patientId String
   * @return the patient visit details map
   */
  public Map getPatientVisitDetailsMap(String patientId) {
    return patientRegistrationRepository.getPatientVisitDetailsMap(patientId);
  }

  /**
   * List all.
   *
   * @param keys the keys
   * @return the list
   */
  public List<BasicDynaBean> listAll(Map<String, Object> keys) {
    // TODO Auto-generated method stub
    return patientRegistrationRepository.listAll(null, keys, null);
  }

  /**
   * Checks if is patient visits exisit.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType   the visit dependence type
   * @param mrNo                  the mr no
   * @param doctorId              the doctor id
   * @param departmentId          the department id
   * @param centerId              the center id
   * @return true, if is patient visits exisit
   */
  private boolean isPatientVisitsExists(String followupAcrossCenters, String visitDependenceType,
      String mrNo, String doctorId, String departmentId, int centerId) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientHasVisitsExistByDoctor(mrNo, doctorId);
      }
      return patientRegistrationRepository.getPatientHasVisitsExistByDoctorInCenter(mrNo, doctorId,
          centerId);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientHasVisitsExistByDepartment(mrNo, departmentId);
    }
    return patientRegistrationRepository.getPatientHasVisitsExistByDepartmentInCenter(mrNo,
        departmentId, centerId);
  }

  
  /**
   * Checks if is patient visits exists.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType the visit dependence type
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param departmentId the department id
   * @param centerId the center id
   * @param sponsorId the sponsor id
   * @param considerSponsor the consider sponsor
   * @return true, if is patient visits exists
   */
  private boolean isPatientVisitsExists(String followupAcrossCenters, String visitDependenceType,
      String mrNo, String doctorId, String departmentId, int centerId, String sponsorId,
      Boolean considerSponsor) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientHasVisitsExistByDoctor(mrNo, doctorId,
            sponsorId, considerSponsor);
      }
      return patientRegistrationRepository.getPatientHasVisitsExistByDoctorInCenter(mrNo, doctorId,
          centerId, sponsorId, considerSponsor);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientHasVisitsExistByDepartment(mrNo, departmentId,
          sponsorId, considerSponsor);
    }
    return patientRegistrationRepository.getPatientHasVisitsExistByDepartmentInCenter(mrNo,
        departmentId, centerId, sponsorId, considerSponsor);
  }
  
  /**
   * Gets the patient main visit within validity.
   *
   * @param followupAcrossCenters     the followup across centers
   * @param visitDependenceType       the visit dependence type
   * @param consultationValidityUnits the consultation validity units
   * @param mrNo                      the mr no
   * @param doctorId                  the doctor id
   * @param departmentId              the department id
   * @param doctorValidityDays        the doctor validity days
   * @param centerId                  the center id
   * @return the patient main visit within validity
   */
  private BasicDynaBean getPatientMainVisitWithinValidity(String followupAcrossCenters,
      String visitDependenceType, String consultationValidityUnits, String mrNo, String doctorId,
      String departmentId, int doctorValidityDays, int centerId) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientMainVisitWithinValidityDays(
            consultationValidityUnits, mrNo, doctorId, doctorValidityDays);
      }
      return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysByCenter(
          consultationValidityUnits, mrNo, doctorId, doctorValidityDays, centerId);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysInDepartment(
          consultationValidityUnits, mrNo, departmentId, doctorValidityDays);
    }
    return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysInDepartmentByCenter(
        consultationValidityUnits, mrNo, departmentId, doctorValidityDays, centerId);
  }
  
  /**
   * Gets the patient main visit within validity.
   *
   * @param followupAcrossCenters     the followup across centers
   * @param visitDependenceType       the visit dependence type,
   *     D if doctor dependent, S if department dependent
   * @param consultationValidityUnits the consultation validity units
   * @param mrNo                      the mr no
   * @param doctorId                  the doctor id
   * @param departmentId              the department id
   * @param doctorValidityDays        the doctor validity days
   * @param centerId                  the center id
   * @param visitTypeList the visit type list
   * @param sponsorId the sponsor id
   * @param considerSponsor the consider sponsor
   * @return the patient main visit within validity
   */
  private BasicDynaBean getPatientMainVisitWithinValidity(String followupAcrossCenters,
      String visitDependenceType, String consultationValidityUnits, String mrNo, String doctorId,
      String departmentId, int doctorValidityDays, int centerId, List<String> visitTypeList,
      String sponsorId, Boolean considerSponsor) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientMainVisitWithinValidityDays(
            consultationValidityUnits, mrNo, doctorId, doctorValidityDays, visitTypeList,
            sponsorId, considerSponsor);
      }
      return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysByCenter(
          consultationValidityUnits, mrNo, doctorId, doctorValidityDays, centerId, visitTypeList,
          sponsorId, considerSponsor);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysInDepartment(
          consultationValidityUnits, mrNo, departmentId, doctorValidityDays, visitTypeList,
          sponsorId, considerSponsor);
    }
    return patientRegistrationRepository.getPatientMainVisitWithinValidityDaysInDepartmentByCenter(
        consultationValidityUnits, mrNo, departmentId, doctorValidityDays, centerId, visitTypeList,
        sponsorId, considerSponsor);
  }

  /**
   * Gets the patient ip visit within validity.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType the visit dependence type
   * @param consultationValidityUnits the consultation validity units
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param departmentId the department id
   * @param doctorValidityDays the doctor validity days
   * @param centerId the center id
   * @return the patient ip visit within validity
   */
  private BasicDynaBean getPatientIpVisitWithinValidity(String followupAcrossCenters,
      String visitDependenceType, String consultationValidityUnits, String mrNo, String doctorId,
      String departmentId, int doctorValidityDays, int centerId) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientIpVisitWithinValidityDays(
            consultationValidityUnits, mrNo, doctorId, doctorValidityDays);
      }
      return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysByCenter(
          consultationValidityUnits, mrNo, doctorId, doctorValidityDays, centerId);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysInDepartment(
          consultationValidityUnits, mrNo, departmentId, doctorValidityDays);
    }
    return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysInDepartmentByCenter(
        consultationValidityUnits, mrNo, departmentId, doctorValidityDays, centerId);
  }

  /**
   * Gets the patient ip visit within validity.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType the visit dependence type
   * @param consultationValidityUnits the consultation validity units
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param departmentId the department id
   * @param doctorValidityDays the doctor validity days
   * @param centerId the center id
   * @param sponsorId the sponsor id
   * @param considerSponsor the consider sponsor
   * @return the patient ip visit within validity
   */
  private BasicDynaBean getPatientIpVisitWithinValidity(String followupAcrossCenters,
      String visitDependenceType, String consultationValidityUnits, String mrNo, String doctorId,
      String departmentId, int doctorValidityDays, int centerId, String sponsorId,
      Boolean considerSponsor) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientIpVisitWithinValidityDays(
            consultationValidityUnits, mrNo, doctorId, doctorValidityDays, sponsorId,
            considerSponsor);
      }
      return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysByCenter(
          consultationValidityUnits, mrNo, doctorId, doctorValidityDays, centerId, sponsorId,
          considerSponsor);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysInDepartment(
          consultationValidityUnits, mrNo, departmentId, doctorValidityDays, sponsorId,
          considerSponsor);
    }
    return patientRegistrationRepository.getPatientIpVisitWithinValidityDaysInDepartmentByCenter(
        consultationValidityUnits, mrNo, departmentId, doctorValidityDays, centerId, sponsorId,
        considerSponsor);
  }

  /**
   * Gets the patient followup count.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType   the visit dependence type
   * @param mrNo                  the mr no
   * @param doctorId              the doctor id
   * @param departmentId          the department id
   * @param visitedDate           the visited date
   * @param centerId              the center id
   * @param sponsorId the sponsor id
   * @param considerSponsor the consider sponsor
   * @return the patient followup count
   */
  private Integer getPatientFollowupCount(String followupAcrossCenters, String visitDependenceType,
      String mrNo, String doctorId, String departmentId, Timestamp visitedDate, Integer centerId,
      String sponsorId, Boolean considerSponsor) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientFollowupCountByDoctor(mrNo, doctorId,
            visitedDate, sponsorId, considerSponsor);
      }
      return patientRegistrationRepository.getPatientFollowupCountByDoctorInCenter(mrNo, doctorId,
          visitedDate, centerId, sponsorId, considerSponsor);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientFollowupCountByDepartment(mrNo, departmentId,
          visitedDate, sponsorId, considerSponsor);
    }
    return patientRegistrationRepository.getPatientFollowupCountByDepartmentInCenter(mrNo,
        departmentId, visitedDate, centerId, sponsorId, considerSponsor);
  }
  /**
   * Gets the patient followup count.
   *
   * @param followupAcrossCenters the followup across centers
   * @param visitDependenceType   the visit dependence type
   * @param mrNo                  the mr no
   * @param doctorId              the doctor id
   * @param departmentId          the department id
   * @param visitedDate           the visited date
   * @param centerId              the center id
   * @return the patient followup count
   */
  
  private Integer getPatientFollowupCount(String followupAcrossCenters, String visitDependenceType,
      String mrNo, String doctorId, String departmentId, Timestamp visitedDate, int centerId) {
    if (visitDependenceType.equals("D")) {
      if (followupAcrossCenters.equals("Y")) {
        return patientRegistrationRepository.getPatientFollowupCountByDoctor(mrNo, doctorId,
            visitedDate);
      }
      return patientRegistrationRepository.getPatientFollowupCountByDoctorInCenter(mrNo, doctorId,
          visitedDate, centerId);
    }
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientFollowupCountByDepartment(mrNo, departmentId,
          visitedDate);
    }
    return patientRegistrationRepository.getPatientFollowupCountByDepartmentInCenter(mrNo,
        departmentId, visitedDate, centerId);
  }

  /**
   * Gets the patient latest main visit bean.
   *
   * @param followupAcrossCenters the followup across centers
   * @param mrNo                  the mr no
   * @param doctorId              the doctor id
   * @param centerId              the center id
   * @return the patient latest main visit bean
   */
  private BasicDynaBean getPatientLatestMainVisitBean(String followupAcrossCenters, String mrNo,
      String doctorId, int centerId) {
    if (followupAcrossCenters.equals("Y")) {
      return patientRegistrationRepository.getPatientLatestMainVisitBean(mrNo, doctorId);
    }
    return patientRegistrationRepository.getPatientLatestMainVisitBeanByCenter(mrNo, doctorId,
        centerId);
  }

  /**
   * Gets the default values for unidentified patients.
   *
   * @return the default values for unidentified patients
   */
  public Map<String, Object> getDefaultValuesForUnidentifiedPatients() {
    Map<String, Object> defaultValues = new HashMap<>();
    Map<String, String> patientDefaults = new HashMap<>();
    BasicDynaBean registrationPrefs = regPrefService.getRegistrationPreferences();
    patientDefaults.put("patient_name",
        (String) registrationPrefs.get("unidentified_patient_first_name"));
    patientDefaults.put("last_name",
        (String) registrationPrefs.get("unidentified_patient_last_name")
            + patientRegistrationRepository.getNextUnidentifiedPatientNumber().toString());
    defaultValues.put("patient", patientDefaults);
    return defaultValues;
  }

  /**
   * Gets the ip patient visit list.
   *
   * @param mrNo     the mr no
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return the ip patient visit list
   */
  public List<BasicDynaBean> getIpPatientVisitList(String mrNo, String doctorId, Integer centerId) {
    return patientRegistrationRepository.getIpPatientVisitList(mrNo, doctorId, centerId);
  }

  /**
   * Gets the patient visit summary info.
   *
   * @param patientId the patient id
   * @return the patient visit summary info
   */
  public BasicDynaBean getPatientVisitSummaryInfo(String patientId) {
    return patientRegistrationRepository.getPatientVisitSummaryInfo(patientId);
  }

  /**
   * Convert contact to mr no.
   *
   * @param mrNo      the mr no
   * @param contactId the contact id
   */
  public void convertContactToMrNo(String mrNo, Integer contactId) {
    resourceService.convertContactToMrNo(mrNo, contactId);
  }

  /**
   * get all visit Details for a patient on basis of visit type.
   * 
   * @param mrNo      the mrNo
   * @param visitType the visitType
   * @return map
   */
  public Map<String, Object> getAllVisitDetails(String mrNo, String visitType) {
    Map<String, Object> visitData = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> visitDetails = patientRegistrationRepository.getAllVisitDetails(mrNo,
        visitType, centerId);
    visitData.put("visit_ist", ConversionUtils.listBeanToListMap(visitDetails));
    return visitData;
  }

  /**
   * Reopen ip emr form.
   *
   * @param visitId       the visit id
   * @param reopenRemarks the reopen remarks
   * @return the string
   */
  public String reopenIpEmrForm(String visitId, String reopenRemarks) {
    BasicDynaBean oldVisitBean = findByKey(visitId);
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String ipemrStatus = (String) oldVisitBean.get("ipemr_status");
    if (registrationValidator.validateIpemrStatus(ipemrStatus)) {
      BasicDynaBean bean = patientRegistrationRepository.getBean();
      bean.set("ipemr_status", "P");
      bean.set("user_name", userName);
      bean.set("ipemr_reopen_remarks", reopenRemarks);
      bean.set("ipemr_complete_time", null);
      bean.set("ipemr_reopened", true);
      Map<String, Object> key = new HashMap<>();
      key.put("patient_id", visitId);
      if (patientRegistrationRepository.update(bean, key) > 0) {
        ipemrStatus = (String) bean.get("ipemr_status");
      }
    }
    return ipemrStatus;
  }

  /**
   * Get all visits across all centers for a patient.
   *
   * @param mrNo       mr number of patient
   * @param activeOnly set to true, to fetch only active visits
   * @return List of Visit Data Map
   */
  public List<Map> getPatientAllVisitsInAllCenters(String mrNo, boolean activeOnly) {
    return ConversionUtils.listBeanToListMap(
        patientRegistrationRepository.getPatientAllActiveVisits(mrNo, null, activeOnly));
  }

  /**
   * Imports bulk patient data.
   * 
   * @param patientDataList the list of Map
   */
  public void importBulkPatientData(List<Map<String, Object>> patientDataList) {
    List<Map<String, Object>> patientMapList = validatePatDataListAndConvertToBean(patientDataList);
    BasicDynaBean patientDetailsBean = patientDetailsService.getBean();
    Map<String, Object> patMap;
    boolean validData = true;
    List<String> errors = new ArrayList<String>();
    TransactionStatus patTs = DatabaseHelper.startTransaction("patient_upload_begin");
    for (Map<String, Object> map : patientMapList) {
      patMap = new HashMap<String, Object>();
      patMap.put("patient", map);
      try {
        copyToPatientDemographyBean(patMap, patientDetailsBean);
        insertPatientDemography(patMap, patientDetailsBean);
      } catch (DuplicateKeyException exception) {
        validData = false;
        log.error(exception.toString());
        errors.add("Duplicate entry :" + map.get("patient_name"));
      } catch (Exception exception) {
        validData = false;
        log.error(exception.toString());
        errors.add(exception.toString());
      }
    }
    if (validData) {
      DatabaseHelper.commit(patTs);
    } else {
      DatabaseHelper.rollback(patTs);
      throw new HMSException(errors);
    }
  }

  /**
   * Converts patient data map from csv to patient bean.
   * 
   * @param patientDataList the map list
   * @return list of map
   */
  private List<Map<String, Object>> validatePatDataListAndConvertToBean(
      List<Map<String, Object>> patientDataList) {
    List<Map<String, Object>> patientMapList = new ArrayList<Map<String, Object>>();
    Map<String, Object> patDetailsMap;
    List<String> errors = new ArrayList<String>();

    for (Map<String, Object> patientData : patientDataList) {
      patDetailsMap = new HashMap<String, Object>();

      patDetailsMap.put("user_provided_mr_no", patientData.get("new_mr_no"));
      patDetailsMap.put("oldmrno", patientData.get("old_mr_no"));

      BasicDynaBean regPrefBean = registrationPreferencesService.getRegistrationPreferences();
      if (patientData.get("patient_category") != null) {
        if (!regPrefBean.get("patient_category_field_label").equals("")
            && patientData.get("patient_category").equals("")) {
          errors
              .add("Patient Category value is not available for " + patientData.get("first_name"));
          continue;
        }
        if (!patientData.get("patient_category").equals("")) {
          List<BasicDynaBean> patCat = patientCatService.getAllCategoriesIncSuperCenter(0);
          for (BasicDynaBean pc : patCat) {
            if (pc.get("category_name").toString()
                .equalsIgnoreCase(patientData.get("patient_category").toString())) {
              patDetailsMap.put("patient_category_id", pc.get("category_id"));
              break;
            }
          }
        }
      } else {
        if (!regPrefBean.get("patient_category_field_label").equals("")) {
          errors
              .add("Patient Category value is not available for " + patientData.get("first_name"));
          continue;
        }
      }

      Map<String, Object> map = new HashMap<String, Object>();
      String title = patientData.get("title").toString();
      if (title.equalsIgnoreCase("Mr")) {
        map.put("salutation", "Mr.");
      } else if (title.equalsIgnoreCase("Ms")) {
        map.put("salutation", "Ms.");
      } else if (title.equalsIgnoreCase("Mrs")) {
        map.put("salutation", "Mrs.");
      } else if (title.equalsIgnoreCase("Dr")) {
        map.put("salutation", "Dr.");
      } else {
        map.put("salutation", patientData.get("title"));
      }
      BasicDynaBean sal = salutationService.getSalutationId(map);
      if (sal != null) {
        patDetailsMap.put("salutation", sal.get("salutation_id"));
      } else {
        sal = salutationRepository.getBean();
        sal.set("salutation_id", salutationRepository.getNextId());
        sal.set("salutation", title);
        sal.set("status", "A");
        salutationRepository.insert(sal);
        patDetailsMap.put("salutation",
            salutationService.getSalutationId(map).get("salutation_id"));
      }

      patDetailsMap.put("patient_name", patientData.get("first_name"));
      patDetailsMap.put("middle_name", patientData.get("middle_name"));
      patDetailsMap.put("last_name", patientData.get("last_name"));

      String gender = patientData.get("gender").toString();
      if (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase(OP_TYPE_MAIN)) {
        patDetailsMap.put("patient_gender", OP_TYPE_MAIN);
      } else if (gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase(OP_TYPE_FOLLOWUP)) {
        patDetailsMap.put("patient_gender", OP_TYPE_MAIN);
      } else if (gender.equalsIgnoreCase("Couple") || gender.equalsIgnoreCase("C")) {
        patDetailsMap.put("patient_gender", "C");
      } else if (gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O")) {
        patDetailsMap.put("patient_gender", "O");
      }

      patDetailsMap.put("dateofbirth", patientData.get("dob"));
      patDetailsMap.put("patient_phone", patientData.get("phone_no"));
      patDetailsMap.put("patient_phone2", patientData.get("additional_phone"));
      patDetailsMap.put("patient_address", patientData.get("address"));
      patDetailsMap.put("patient_area", patientData.get("area"));

      BasicDynaBean countryBean = countryService
          .getNationality(patientData.get("country").toString());
      if (countryBean != null) {
        patDetailsMap.put("country", countryBean.get("country_id"));
      } else {
        errors.add("Invalid country entered for patient :" + patientData.get("first_name"));
        continue;
      }

      String state = patientData.get("state").toString();
      BasicDynaBean st = stateRepository.findByKey("state_name", state);
      if (st != null) {
        patDetailsMap.put("patient_state", st.get("state_id"));
      } else {
        st = stateRepository.getBean();
        st.set("state_id", stateRepository.getNextId());
        st.set("state_name", state);
        st.set("status", "A");
        st.set("country_id", patDetailsMap.get("country"));
        stateRepository.insert(st);
        patDetailsMap.put("patient_state", st.get("state_id"));
      }

      if (patientData.get("city") != null) {
        String city = patientData.get("city").toString();
        BasicDynaBean cityBean = cityRepository.findByKey("city_name", city);
        if (cityBean != null) {
          patDetailsMap.put("patient_city", cityBean.get("city_id"));
        } else {
          cityBean = cityRepository.getBean();
          cityBean.set("city_id", cityRepository.getNextId());
          cityBean.set("city_name", city);
          cityBean.set("status", "A");
          cityBean.set("state_id", patDetailsMap.get("patient_state"));
          cityRepository.insert(cityBean);
          patDetailsMap.put("patient_city", cityBean.get("city_id"));
        }
      }

      patDetailsMap.put("first_visit_reg_date", patientData.get("firstvisitdate"));
      patDetailsMap.put("nationality_id", patientData.get("nationality"));
      patDetailsMap.put("email_id", patientData.get("email_id"));
      patDetailsMap.put("custom_field1", patientData.get("field1value"));
      patDetailsMap.put("custom_field2", patientData.get("field2value"));
      patDetailsMap.put("custom_field3", patientData.get("field3value"));
      patDetailsMap.put("custom_field4", patientData.get("field4value"));
      patDetailsMap.put("custom_field5", patientData.get("field5value"));
      patDetailsMap.put("custom_field6", patientData.get("field6value"));
      patDetailsMap.put("custom_field7", patientData.get("field7value"));
      patDetailsMap.put("custom_field8", patientData.get("field8value"));
      patDetailsMap.put("custom_field9", patientData.get("field9value"));
      patDetailsMap.put("custom_field10", patientData.get("field10value"));
      patDetailsMap.put("custom_field11", patientData.get("field11value"));
      patDetailsMap.put("custom_field12", patientData.get("field12value"));
      patDetailsMap.put("custom_field13", patientData.get("field13bvalue"));
      patDetailsMap.put("user_name", RequestContext.getUserName());
      // Setting default English language for contact_preferences
      patDetailsMap.put("preferred_language", "en");
      patientMapList.add(patDetailsMap);

    }
    if (errors.isEmpty()) {
      return patientMapList;
    } else {
      throw new HMSException(errors);
    }
  }

  /**
   * Obtain lock for new registration, limits one concurrent registration per user.
   */
  public void obtainLock() {
    HashOperations hashOperations = redisTemplate.opsForHash();
    String key = "Registration_Create_" + RequestContext.getUserName();
    if (!hashOperations.putIfAbsent(key, key, true)) {
      throw new HMSException(HttpStatus.TOO_MANY_REQUESTS, "exception.bad.request", null);
    }
    redisTemplate.expire(key, 1, TimeUnit.MINUTES);
  }

  /**
   * Release lock for new registration for user.
   */
  public void releaseLock() {
    HashOperations hashOperations = redisTemplate.opsForHash();
    String key = "Registration_Create_" + RequestContext.getUserName();
    hashOperations.delete(key, key);
  }

  /**
   * Gets patient cash limit details.
   *
   * @param mrNo mr number of patient
   * @return the patient cash limit details
   */
  public HashMap<String, BigDecimal> getPatientCashLimitDetails(String mrNo) {
    HashMap<String, BigDecimal> cashLimitDetails = new HashMap<>();
    BasicDynaBean cashLimitBean = null;
    cashLimitBean = this.receiptService.getCashLimit(mrNo, null);

    if (cashLimitBean != null) {
      cashLimitDetails.put("day_cash_payment", (BigDecimal) cashLimitBean
              .get("day_cash_payment"));
      cashLimitDetails.put("transaction_limit", (BigDecimal) cashLimitBean
              .get("transaction_limit"));
    } else {
      HashMap<String, Integer> cashModeFitler = new HashMap<>();
      cashModeFitler.put("mode_id", -1);
      BasicDynaBean cashPaymentBean = this.paymentModeService.findByPk(cashModeFitler);
      cashLimitDetails.put("day_cash_payment", BigDecimal.valueOf(0));
      cashLimitDetails.put("transaction_limit", (BigDecimal) cashPaymentBean
              .get("transaction_limit"));
    }

    return cashLimitDetails;
  }
  
  /**
   * Get Visit Date.
   * 
   * @param visitId the visit Id
   * @return string
   */
  public String getVisitDate(String visitId) {
    String visitDate = null;
    BasicDynaBean visitBean = getVisitDetails(visitId);
    if (visitBean != null && visitBean.get("reg_date") != null) {
      visitDate = visitBean.get("reg_date").toString();
    }
    return visitDate;
  }

  /**
   * validate Email.
   * 
   * @param email the emailId
   * @return boolean
   */
  private boolean isValid(String email) {
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@"
        + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";

    Pattern pat = Pattern.compile(emailRegex);
    if (email == null) {
      return false;
    }
    return pat.matcher(email).matches();
  }
  
  /**
   * Checks is Visit Active.
   * 
   * @param visitId visit id
   * @return boolean
   */
  public boolean isVisitActive(String visitId) {
    BasicDynaBean bean = patientRegistrationRepository.findByKey("patient_id", visitId);
    String visitStatus = "I";
    if (bean != null) {
      visitStatus = (String) bean.get("status");
    }
    return visitStatus.equals("A");
  }

  /**
   * Marks patient package as discontinued while updating the remark.
   *
   * @param patientPackageId the patient package id
   * @param discontinueRemark the discontinue remark
   *
   */
  public void discontinuePackage(Integer patientPackageId, String discontinueRemark) {
    this.packageOrderItemService.discontinuePackage(patientPackageId, discontinueRemark);
  }
  
 
  /**
   * Api create visit.
   *
   * @param params the params
   * @return the map
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> apiCreateVisit(Map<String, Object> params)
      throws Exception {
    Map<String, Object> patientData = (Map<String, Object>) params.get("patient");
    Map<String, Object> visitData = (Map<String, Object>) params.get("visit");
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (StringUtils.isEmpty(patientData.get("mr_no"))) {
      validationErrors.addError("mr_no", "exception.patient.mr_no.empty");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("patient", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    List appToArrive = new ArrayList();
    if (!StringUtils.isEmpty(visitData.get("appointment_id"))) {
      BasicDynaBean appointmentBean = appointmentService
          .findByKey(Integer.parseInt((String) visitData.get("appointment_id")));
      if ((int) appointmentBean.get("res_sch_id") != 1) {
        validationErrors.addError("appointment_id", "ui.label.appointment.category");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("visit", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      HashMap app = new HashMap();
      app.put("appointment_id", visitData.get("appointment_id"));
      app.put("appointment_status", "Arrived");
      ArrayList updateAppList = new ArrayList();
      updateAppList.add(app);
      if (!appointmentValidator.validateUpdateAppointmentStatusParams(updateAppList,
          validationErrors)) {
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("visit", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      java.util.Date appTime = (Date) appointmentBean.get("appointment_time");
      DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
      String currentDate = dtFormat.format(Calendar.getInstance().getTime());
      String appDate = dtFormat.format(appTime);
      if (appDate.compareTo(currentDate) != 0) {
        validationErrors.addError("appointment_id", "ui.label.appointement.date");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("visit", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      BasicDynaBean doctor = doctorService
          .getDoctorById((String) appointmentBean.get("prim_res_id"));
      visitData.put("dept_name", doctor.get("dept_id"));
      appToArrive.add(appointmentBean.get("appointment_id"));
      String schOrder = (String) genPrefService.getAllPreferences().get("scheduler_generate_order");
      if (!StringUtils.isEmpty(schOrder) && schOrder.equals("Y")) {
        visitData.put("doctor", appointmentBean.get("prim_res_id"));
      }
    }
    if (!registrationValidator.validateVisitDetails(visitData, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("visit", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    ArrayList insuranceData = (ArrayList) params.get(INSURANCE);
    if (insuranceData != null && !insuranceData.isEmpty()) {
      String primPlanCode = (String) ((Map<String, Object>) insuranceData.get(0)).get("plan_code");
      if (!StringUtils.isEmpty(primPlanCode)) {
        Map<String, Object> primInsurance = new HashMap<String, Object>();
        List<BasicDynaBean> primIns = insurancePlanService.getTPADetailsFromCode(primPlanCode, 1,
            "op");
        if (primIns.size() > 1) {
          validationErrors.addError("primaryPlanCode", "ui.label.insurance.plan.code.tpa");
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("insurance", ex.getErrors());
          throw new NestableValidationException(nestedException);
        } else if (primIns.size() == 1) {
          BasicDynaBean primMap = primIns.get(0);
          primInsurance.put("insurance_co", primMap.get("insurance_company_id"));
          primInsurance.put("sponsor_id", primMap.get("sponsor_id"));
          primInsurance.put("plan_id", primMap.get("insurance_plan_id"));
          primInsurance.put("plan_type_id", primMap.get("plan_type_id"));
          if (((Map<String, Object>) insuranceData.get(0)).get("ins_doc") != null) {
            primInsurance.put("ins_doc",
                ((Map<String, Object>) insuranceData.get(0)).get("ins_doc"));
          }
          primInsurance.put("member_id",
              ((Map<String, Object>) insuranceData.get(0)).get("member_id"));
          insuranceData.remove(0);
          insuranceData.add(0, primInsurance);
        } else {
          validationErrors.addError("primaryPlanCode", "ui.label.insurance.plan.code");
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("insurance", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }

      } else {
        if (!registrationValidator.validateInsuranceDetails((Map) insuranceData.get(0),
            validationErrors)) {
          ValidationException ex = new ValidationException(validationErrors);
          nestedException.put("insurance", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
      }
      if (insuranceData.size() > 1) {
        String secPlanCode = (String) ((Map<String, Object>) insuranceData.get(1)).get("plan_code");
        if (!StringUtils.isEmpty(secPlanCode)) {
          Map<String, Object> secInsurance = new HashMap<String, Object>();

          List<BasicDynaBean> secIns = insurancePlanService.getTPADetailsFromCode(secPlanCode, 1,
              "op");
          if (secIns.size() > 1) {
            validationErrors.addError("secondaryPlanCode", "ui.label.insurance.plan.code.tpa");
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("insurance", ex.getErrors());
            throw new NestableValidationException(nestedException);
          } else if (secIns.size() == 1) {
            BasicDynaBean secMap = secIns.get(0);
            secInsurance.put("insurance_co", secMap.get("insurance_company_id"));
            secInsurance.put("sponsor_id", secMap.get("sponsor_id"));
            secInsurance.put("plan_id", secMap.get("insurance_plan_id"));
            secInsurance.put("plan_type_id", secMap.get("plan_type_id"));
            if (((Map<String, Object>) insuranceData.get(1)).get("ins_doc") != null) {
              secInsurance.put("ins_doc",
                  ((Map<String, Object>) insuranceData.get(1)).get("ins_doc"));
            }
            secInsurance.put("member_id",
                ((Map<String, Object>) insuranceData.get(1)).get("member_id"));
            insuranceData.remove(1);
            insuranceData.add(1, secInsurance);
          } else {
            validationErrors.addError("secondaryPlanCode", "ui.label.insurance.plan.code");
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("insurance", ex.getErrors());
            throw new NestableValidationException(nestedException);
          }
        } else {
          if (!registrationValidator.validateInsuranceDetails((Map) insuranceData.get(1),
              validationErrors)) {
            ValidationException ex = new ValidationException(validationErrors);
            nestedException.put("insurance", ex.getErrors());
            throw new NestableValidationException(nestedException);
          }
        }
      }
    }
    String sponsorId = (insuranceData != null && !insuranceData.isEmpty())
        ? (String) ((Map<String, Object>) insuranceData.get(0)).get("sponsor_id") : null;

    int consId = 0;
    String orgId = (String) visitData.get("org_id");
    Map<String, Object> visitDetails = getPatientVisitType((String) patientData.get("mr_no"),
        (String) visitData.get("doctor"), sponsorId, true, (int) visitData.get("center_id"));
    Map patientPreVisitData = (HashMap<String, Object>) getPatientDetailsForNewVisit(
        (String) patientData.get("mr_no"));
    HashMap<String, Object> patientBean = (HashMap<String, Object>) patientPreVisitData
        .get("patient");
    Integer patientCat = patientBean != null ? (int)patientBean.get("patient_category_id") : null;
    patientCat = patientCat == null ? (int) centerPrefService
        .getCenterPreferences((int) visitData.get("center_id")).get("pref_op_default_category")
        : patientCat;

    if (StringUtils.isEmpty(orgId)) {
      if (!(insuranceData != null && !insuranceData.isEmpty())) {
        List ratePlans = getDefaultRatePlan(String.valueOf(patientCat), null, null,
            (Integer) visitData.get("center_id"), "o");
        orgId = (ratePlans != null && !ratePlans.isEmpty())
            ? (String) ((BasicDynaBean) ratePlans.get(0)).get("org_id") : null;
      } else {
        List ratePlans = getDefaultRatePlan(String.valueOf(patientCat),
            String.valueOf(((Map<String, Object>) insuranceData.get(0)).get("plan_id")),
            (String) ((Map<String, Object>) insuranceData.get(0)).get("insurance_co"),
            (Integer) visitData.get("center_id"), "o");
        orgId = (ratePlans != null && !ratePlans.isEmpty())
            ? (String) ((BasicDynaBean) ratePlans.get(0)).get("org_id") : null;
      }
    }
    List allowedRatePlans = patientCategoryService.getAllowedRatePlans(patientCat, "o");
    Boolean allowflag = false;
    if (!StringUtils.isEmpty(orgId)) {
      for (Object ratePlan : allowedRatePlans) {
        if (((BasicDynaBean) ratePlan).get("org_id").equals(orgId)) {
          allowflag = true;
          break;
        }
      }
      if (!allowflag) {
        validationErrors.addError("org_id", "ui.label.invalid.rate.plan");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("visit", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    visitData.put("org_id", orgId);
    if (!StringUtils.isEmpty(visitData.get("doctor"))) {
      consId = getConsultationType((String) visitData.get("doctor"),
          (int) visitData.get("center_id"), (String) visitDetails.get("op_type"));

      if (consId == 0) {
        validationErrors.addError("consultationId", "ui.label.consultation.mapping");
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("visit", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }

    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    String regDate = dateFormat.format(date);
    DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    String regTime = timeFormat.format(date);
    visitData.put("reg_date", regDate);
    visitData.put("reg_time", regTime);
    if (StringUtils.isEmpty(visitData.get("cons_date"))) {
      visitData.put("cons_date", regDate);
    }
    if (StringUtils.isEmpty(visitData.get("cons_time"))) {
      visitData.put("cons_time", regTime);
    }
    String billingBedTypeForOP = (String) genPrefService.getAllPreferences()
        .get("billing_bed_type_for_op");
    billingBedTypeForOP = (billingBedTypeForOP != null && !billingBedTypeForOP.equals(""))
        ? billingBedTypeForOP : "GENERAL";

    params.put("apptIds_to_arrive", appToArrive);
    visitData.put("regAndBill", "N");
    visitData.put("apply_registration_charges", "N");
    visitData.put("bill_type", "C");
    visitData.put("codification_status", "P");
    visitData.put("apply_registration_charges", "Y");
    visitData.put("is_er_visit", false);
    visitData.put("collection_center_id", "-1");
    visitData.put("system_date", DateUtil.getCurrentDate().toString());
    visitData.put("system_Time", DateUtil.getCurrentTime().toString());

    if (patientBean.get("dateofbirth") != null) {
      String dob = dateFormat.format(patientBean.get("dateofbirth"));
      patientBean.put("dateofbirth", dob);
    }
    patientData.putAll(patientBean);
    patientData.put("user_name", RequestContext.getUserName());
    Map<String, Object> regChargeMAp = new HashMap<String, Object>();
    regChargeMAp.put("ORG_ID", visitData.get("org_id"));
    regChargeMAp.put("type", "Direct Charge");
    regChargeMAp.put("id", "OPREG");
    regChargeMAp.put("visit_type", "o");
    regChargeMAp.put("bed_type", billingBedTypeForOP);
    // getting registration charges
    Map<String, Object> regCharges = orderService.getItemCharges(regChargeMAp);
    List chanrgeList = (List) regCharges.get("charge_list");
    BasicDynaBean charg = (BasicDynaBean) chanrgeList.get(0);
    Map<String, Object> charges = new HashMap<String, Object>();
    charges.put("act_description_id", visitData.get("doctor"));
    charges.put("consultation_type_id", consId != 0 ? String.valueOf(consId) : null);
    charges.put("doc_chargegroup", "DOC");
    charges.put("registrationCharge", ((BigDecimal) charg.get("amount")).intValue());
    charges.put("doc_tax_amt", charg.get("tax_amt"));
    charges.put("doc_discount", charg.get("discount"));
    charges.put("doc_chargehead", "OPDOC");
    params.put("charges", charges);

    BigDecimal amount = (BigDecimal) charg.get("amount");
    BigDecimal patientAmt = BigDecimal.ZERO;
    ArrayList insuranceDetails = new ArrayList();
    if (insuranceData != null && !insuranceData.isEmpty()) {
      insuranceDetails = getInsuranceDetails(insuranceData);
    }
    if (!StringUtils.isEmpty(visitData.get("doctor"))) {
      Map<String, Object> docChargeMap = new HashMap<String, Object>();
      docChargeMap.put("org_id", visitData.get("org_id"));
      docChargeMap.put("type", "Doctor");
      // conultation type
      docChargeMap.put("charge_type", consId != 0 ? String.valueOf(consId) : null);
      docChargeMap.put("id", visitData.get("doctor"));
      docChargeMap.put("visit_type", "o");
      docChargeMap.put("bed_type", billingBedTypeForOP);

      ArrayList manu = new ArrayList();
      List<Integer> planIds = new ArrayList();
      if (insuranceData != null && !insuranceData.isEmpty()) {
        docChargeMap.put("sponsor_id", sponsorId);
        HashMap primaryIns = (HashMap) insuranceData.get(0);
        planIds.add((int) primaryIns.get("plan_id"));
        if (insuranceData.size() > 1) {
          HashMap secondaryIns = (HashMap) insuranceData.get(1);
          planIds.add((int) secondaryIns.get("plan_id"));
        }
        docChargeMap.put("plan_ids", planIds);
      }

      // getting doctor charges
      Map<String, Object> docCharges = orderService.getItemCharges(docChargeMap);
      List docChargeList = (List) docCharges.get("charge_list");
      BasicDynaBean docChr = (BasicDynaBean) docChargeList.get(0);
      if (insuranceData != null && !insuranceData.isEmpty()) {
        Map<String, Object> visit = new HashMap<String, Object>();
        visit.put("act_description_id", visitData.get("doctor"));
        visit.put("consultation_type_id", consId != 0 ? String.valueOf(consId) : null);
        visit.put("doc_amount", String.valueOf(docChr.get("amount")));
        visit.put("doc_chargegroup", "DOC");
        visit.put("doc_chargehead", "OPDOC");
        visit.put("doc_discount", String.valueOf(docChr.get("discount")));
        visit.put("doc_tax_amt", String.valueOf(docChr.get("tax_amt")));
        visit.put("op_type", visitDetails.get("op_type"));
        visit.put("reg_screen", "Y");
        visit.put("sponsor_id", sponsorId);
        visit.put("visit_type", "o");
        visit.put("visit_id", "");

        Map<String, Object> estAmt = new HashMap<String, Object>();
        estAmt.put("visit", visit);
        ArrayList orderedItems = new ArrayList();
        estAmt.put("ordered_items", orderedItems);
        estAmt.put("insurance_details", insuranceDetails);
        Map estimatedAmt = billService.estimateAmount(estAmt);
        Map primaryIns = (HashMap) insuranceData.get(0);
        Map<String, Object> estimateAmountMap = (Map<String, Object>) ((Map<String, Object>)
            estimatedAmt.get("estimate_amount")).get((int) primaryIns.get("plan_id"));
        Map<String, Object> primaryAmt = (Map<String, Object>) estimateAmountMap.get("_0");
        BigDecimal claimAmt = (BigDecimal) primaryAmt.get("insurance_claim_amt");
        if (insuranceData.size() > 1) {
          HashMap secondaryIns = (HashMap) insuranceData.get(1);
          Map<String, Object> secEstimateAmountMap = (Map<String, Object>) ((Map<String, Object>)
              estimatedAmt.get("estimate_amount")).get((int) secondaryIns.get("plan_id"));
          Map<String, Object> secondaryAmt = (Map<String, Object>) secEstimateAmountMap.get("_0");
          BigDecimal amt = (BigDecimal) secondaryAmt.get("insurance_claim_amt");
          claimAmt = claimAmt.add(amt);
        }
        patientAmt = ((BigDecimal) docChr.get("amount")).subtract(claimAmt);
      } else {
        patientAmt = (BigDecimal) docChr.get("amount");
      }
      amount = ((BigDecimal) docChr.get("amount")).add(amount);
      visitData.put("consFees", docChr.get("amount"));
    }
    visitData.put("doctorCharge", String.valueOf(consId));
    visitData.put("estimateAmt", String.valueOf(amount));
    visitData.put("patientAmt", patientAmt);
    visitData.put("op_type", visitDetails.get("op_type"));
    visitData.put("main_visit_id", patientPreVisitData.get("previous_visit") != null
        ? ((Map) patientPreVisitData.get("previous_visit")).get("main_visit_id") : null);
    params.put("insurance", insuranceDetails);
    Map<String, List<Object>> order = new HashMap<String, List<Object>>();
    params.put("ordered_items", order);
    Map<String, Object> map = createNewVisit(params);

    map = createReceipt(params, map);

    return getResponeforCreateVisitApi(map);
  }
  
  /**
   * Gets the consultation type.
   *
   * @param doctorId the doctor id
   * @param centerId the center id
   * @param visitId the visit id
   * @return the consultation type
   */
  public Integer getConsultationType(String doctorId, Integer centerId, String visitId) {
    Map<String, String> filterMap = new HashMap<>();
    List defaultList = new ArrayList();
    filterMap.put("doctor_id", doctorId);
    int consId = 0;
    BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
    if (doctorBean != null && doctorBean.get("practitioner_id") != null) {
      Integer practitionerTypeId = (Integer) doctorBean.get("practitioner_id");
      List<BasicDynaBean> practitionerTypesMapping = practitionerMapService
          .getPractitionerMappings(centerId, practitionerTypeId);
      for (BasicDynaBean practitionerType : practitionerTypesMapping) {
        if (((String) practitionerType.get("visit_type")).equalsIgnoreCase(visitId)) {
          consId = (int) practitionerType.get("consultation_type_id");
          break;
        }
      }
    }
    return consId;
  }

  /**
   * Gets the insurance details.
   *
   * @param insurancedata the insurancedata
   * @return the insurance details
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ArrayList getInsuranceDetails(ArrayList<Map<String, Object>> insurancedata)
      throws IOException {
    Map<String, Object> secinsPlanDetails = new HashMap();
    Map primaryIns = (HashMap) insurancedata.get(0);
    primaryIns.put("use_drg", "N");
    primaryIns.put("patient_policy_id", null);
    if (primaryIns.get("ins_doc") != null && !((ArrayList) primaryIns.get("ins_doc")).isEmpty()) {
      int docId = uploadInsDoc((ArrayList) primaryIns.get("ins_doc"));
      primaryIns.put("doc_id", docId);
    }
    Map<String, Object> priminsPlanDetails = new HashMap();
    getPlanDetails(String.valueOf(primaryIns.get("plan_id")), "o", priminsPlanDetails);
    ArrayList prmInsurance = new ArrayList();
    ArrayList sendInsurance = new ArrayList();
    ArrayList insData = new ArrayList();
    ArrayList<Map<String, Object>> primPlanDetails = (ArrayList) priminsPlanDetails
        .get("plan_details");
    for (Map<String, Object> planDetails : primPlanDetails) {
      Map<String, Object> prmInsuranceData = new HashMap<String, Object>();
      for (Map.Entry<String, Object> plan : planDetails.entrySet()) {
        prmInsuranceData.put(plan.getKey(), plan.getValue());
      }
      prmInsurance.add(prmInsuranceData);
    }
    primaryIns.put("insurance_plan_details", prmInsurance);
    insData.add(primaryIns);
    if (insurancedata.size() > 1) {
      HashMap secondaryIns = (HashMap) insurancedata.get(1);
      secondaryIns.put("use_drg", "N");
      secondaryIns.put("patient_policy_id", null);
      if (secondaryIns.get("ins_doc") != null
          && !((ArrayList) secondaryIns.get("ins_doc")).isEmpty()) {
        int docId = uploadInsDoc((ArrayList) secondaryIns.get("ins_doc"));
        secondaryIns.put("doc_id", docId);
      }
      getPlanDetails(String.valueOf(secondaryIns.get("plan_id")), "o", secinsPlanDetails);
      ArrayList<Map<String, Object>> secPlanDetails = (ArrayList) secinsPlanDetails
          .get("plan_details");
      for (Map<String, Object> planDeatils : secPlanDetails) {
        Map<String, Object> secdInsuranceDetails = new HashMap<String, Object>();
        for (Map.Entry<String, Object> planDetail : planDeatils.entrySet()) {
          secdInsuranceDetails.put(planDetail.getKey(), planDetail.getValue());
        }
        sendInsurance.add(secdInsuranceDetails);
      }
      secondaryIns.put("insurance_plan_details", sendInsurance);
      insData.add(secondaryIns);
    }
    return insData;
  }
  
  /**
   * Upload ins doc.
   *
   * @param insDoc the ins doc
   * @return the int
   */
  private int uploadInsDoc(ArrayList insDoc) {

    BasicDynaBean patientdocbean = patientdocrepo.getBean();
    String document = (String) insDoc.get(0);
    String contentType = document.split("/")[1].split(";")[0];
    int docId = patientdocrepo.getNextSequence();
    patientdocbean.set("doc_id", docId);
    patientdocbean.set("doc_format", "doc_fileupload");
    patientdocbean.set("doc_type", "SYS_RG");
    patientdocbean.set("content_type", contentType.equals("pdf") ? "application/pdf" : contentType);
    byte[] decodedBytes = Base64.decodeBase64(document.split(",")[1].getBytes());
    InputStream is = new ByteArrayInputStream(decodedBytes);
    patientdocbean.set("doc_content_bytea", is);
    boolean success = patientdocrepo.insert(patientdocbean) > 0; 
    if (!success) {
      throw new ValidationException("exception.document.upload");
    }
    return docId;

  }
  
  /**
   * Gets the responefor create visit api.
   *
   * @param response the response
   * @return the responefor create visit api
   */
  public Map<String, Object> getResponeforCreateVisitApi(Map<String, Object> response) {
    HashMap patientData = (HashMap) response.get("patient");
    ArrayList insuranceList = (ArrayList) response.get("insurance");
    ArrayList insurance = new ArrayList();
    for (int i = 0; i < insuranceList.size(); i++) {
      HashMap ins = (HashMap) insuranceList.get(i);
      ins.remove("insurance_plan_details");
      ins.remove("policy_validity_start");
      ins.remove("codified_by");
      ins.remove("per_diem_code");
      ins.remove("utilization_amount");
      ins.remove("eligibility_authorization_status");
      ins.remove("prior_auth_mode_id");
      ins.remove("episode_limit");
      ins.remove("limits_include_followup");
      ins.remove("visit_per_day_limit");
      ins.remove("episode_deductible");
      ins.remove("priority");
      ins.remove("policy_holder_name");
      ins.remove("status");
      ins.remove("eligibility_authorization_remarks");
      ins.remove("patient_relationship");
      ins.remove("use_perdiem");
      ins.remove("employer_name");
      ins.remove("visit_type");
      ins.remove("eligibility_reference_number");
      ins.remove("codification_remarks");
      ins.remove("insurance_approval");
      ins.remove("discount_plan_id");
      ins.remove("visit_limit");
      ins.remove("codification_status");
      ins.remove("discount_plan_name");
      ins.remove("drg_code");

      insurance.add(ins);
    }
    HashMap visitData = (HashMap) response.get("visit");
    Map<String, Object> patient = new HashMap<String, Object>();
    patient.put("oldmrno", patientData.get("oldmrno"));
    patient.put("original_mr_no", patientData.get("original_mr_no"));
    patient.put("primary_sponsor_id", patientData.get("primary_sponsor_id"));
    patient.put("mr_no", patientData.get("mr_no"));
    patient.put("previous_visit_id", patientData.get("previous_visit_id"));
    patient.put("category_name",visitData.get("patient_category_name"));

    Map<String, Object> visit = new HashMap<String, Object>();
    visit.put("patient_category_id", visitData.get("patient_category_id"));
    visit.put("main_visit_id", visitData.get("main_visit_id"));
    visit.put("revisit", visitData.get("revisit"));
    visit.put("token_no", visitData.get("token_no"));
    visit.put("visit_id", patientData.get("visit_id"));
    visit.put("bed_type", visitData.get("bed_type"));
    visit.put("insurance_id", visitData.get("insurance_id"));
    visit.put("sec_tpa_name", visitData.get("sec_tpa_name"));
    visit.put("corporate_sponsor_id", visitData.get("corporate_sponsor_id"));
    visit.put("admission_time", visitData.get("admission_time"));
    visit.put("sec_member_id", visitData.get("sec_member_id"));
    visit.put("relation", visitData.get("relation"));
    visit.put("tpa_name", visitData.get("tpa_name"));
    visit.put("sec_insurance_co_name", visitData.get("sec_insurance_co_name"));
    visit.put("patient_policy_id", visitData.get("patient_policy_id"));
    visit.put("sec_employee_id", visitData.get("sec_employee_id"));
    visit.put("primary_insurance_co", visitData.get("primary_insurance_co"));
    visit.put("cons_time", visitData.get("cons_time"));
    visit.put("policy_number", visitData.get("policy_number"));
    visit.put("alloc_bed_type", visitData.get("alloc_bed_type"));
    visit.put("sec_policy_holder_name", visitData.get("sec_policy_holder_name"));
    visit.put("employee_name", visitData.get("employee_name"));
    visit.put("policy_validity_end", visitData.get("policy_validity_end"));
    visit.put("secondary_sponsor_id", visitData.get("secondary_sponsor_id"));
    visit.put("established_type", visitData.get("established_type"));
    visit.put("plan_type_name", visitData.get("plan_type_name"));
    visit.put("plan_notes", visitData.get("plan_notes"));
    visit.put("op_type", visitData.get("op_type"));
    visit.put("visit_type", visitData.get("visit_type"));
    visit.put("policy_validity_start", visitData.get("policy_validity_start"));
    visit.put("employee_id", visitData.get("employee_id"));
    visit.put("op_type_name", visitData.get("op_type_name"));
    visit.put("primary_sponsor_id", visitData.get("primary_sponsor_id"));
    visit.put("bill_type", visitData.get("bill_type"));
    visit.put("patient_category_name", visitData.get("patient_category_name"));
    visit.put("plan_name", visitData.get("plan_name"));
    visit.put("patient_group_name", visitData.get("patient_group_name"));
    visit.put("patient_group", visitData.get("patient_group"));
    visit.put("sec_policy_validity_end", visitData.get("sec_policy_validity_end"));
    visit.put("admitted_dept_name", visitData.get("admitted_dept_name"));
    visit.put("plan_exclusions", visitData.get("plan_exclusions"));
    visit.put("sec_plan_name", visitData.get("sec_plan_name"));
    visit.put("sponsor_type", visitData.get("sponsor_type"));
    visit.put("reg_time", visitData.get("reg_time"));
    visit.put("visit_type", visitData.get("visit_type"));
    visit.put("status", visitData.get("status"));

    HashMap<String, Object> billAndPaymentStatus = new HashMap<String, Object>();
    billAndPaymentStatus.put("first_bill_for_visit", visitData.get("first_bill_for_visit"));
    billAndPaymentStatus.put("bill_no", response.get("bill_no"));
    billAndPaymentStatus.putAll((DynaBeanMapDecorator) visitData.get("bill_and_payment_status"));

    Map<String, Object> responseMap = new HashMap();
    responseMap.put("patient", patient);
    responseMap.put("visit", visit);
    responseMap.put("bill_and_payment_status", billAndPaymentStatus);
    responseMap.put("insurance", insurance);

    return responseMap;
  }

  /**
   * Gets the default rate plan.
   *
   * @param categoryId the category id
   * @param planId the plan id
   * @param insCompId the ins comp id
   * @param centerId the center id
   * @param visitType the visit type
   * @return the default rate plan
   * @throws NumberFormatException the number format exception
   * @throws SQLException the SQL exception
   */
  private List getDefaultRatePlan(String categoryId, String planId, String insCompId,
      Integer centerId, String visitType) throws NumberFormatException, SQLException {
    registrationValidator.validateGetRatePlan(categoryId, planId);
    List<BasicDynaBean> ratePlansBean = new ArrayList<BasicDynaBean>();
    if (!StringUtils.isEmpty(planId)) {
      ratePlansBean = insurancePlanService.getPlanDefaultRatePlan(Integer.parseInt(planId));
    }
    if ((null == ratePlansBean || (ratePlansBean != null && ratePlansBean.isEmpty()))
        && !StringUtils.isEmpty(insCompId)) {
      ratePlansBean = insuranceCompanyService.getInsCompDefaultRatePlan(insCompId, categoryId);
    }
    if (null == ratePlansBean || (ratePlansBean != null && ratePlansBean.isEmpty())) {
      ratePlansBean = patientCatService.getDefaultRatePlan(Integer.parseInt(categoryId), visitType);
    }
    if (ratePlansBean != null && !ratePlansBean.isEmpty() && !StringUtils.isEmpty(categoryId)) {
      BasicDynaBean patCatRatePlanBean = orgService.checkForOpAllowedRatePlans(
          (String) ratePlansBean.get(0).get(Constants.ORG_ID), Integer.parseInt(categoryId));
      if (patCatRatePlanBean == null
          || (patCatRatePlanBean != null && patCatRatePlanBean.getMap().isEmpty())) {
        throw new ValidationException(
            "js.registration.patient.valid.rate.plans.against.category.plan.insurance.company");
      }
    }
    return ratePlansBean;
  }

  public boolean isFormReopened(String patientId, String columnIdentifier) {
    BasicDynaBean bean = findByKey(patientId);
    return bean != null ? (boolean) bean.get(columnIdentifier) : false;
  }

  public Map getIpEmrFormSaveEventSegmentData(String patientId) {
    return patientRegistrationRepository.getIpEmrFormSaveEventSegmentData(patientId).getMap();
  }
}
