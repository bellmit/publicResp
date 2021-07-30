package com.insta.hms.messaging;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating MessageBuilder objects.
 */
public class MessageBuilderFactory {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageBuilderFactory.class);

  /** The Constant builderMap. */
  private static final Map<String, String> builderMap = new HashMap<>();

  /** The Constant defaultBuilderMap. */
  private static final Map<String, String> defaultBuilderMap = new HashMap<>();

  /** The Constant DEFAULT_MESSAGE_BUILDER. */
  private static final String DEFAULT_MESSAGE_BUILDER = 
      "com.insta.hms.messaging.providers.BulkMessageBuilder";

  /** The Constant DEFAULT_NOTIFICATION_MESSAGE_BUILDER. */
  private static final String DEFAULT_NOTIFICATION_MESSAGE_BUILDER = 
      "com.insta.hms.messaging.providers.BulkNotificationBuilder";

  static {
    builderMap.put("sms_birthday", 
        "com.insta.hms.messaging.providers.BirthdayMessageBuilder");
    builderMap.put("email_birthday", 
        "com.insta.hms.messaging.providers.BirthdayMessageBuilder");
    builderMap.put("sms_appointment_planner",
        "com.insta.hms.messaging.providers.AppointmentPlannerMessageBuilder");
    builderMap.put("sms_appointment_confirmation",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("sms_followup_reminder",
        "com.insta.hms.messaging.providers.FollowupMessageBuilder");
    builderMap.put("sms_appointment_reschedule",
        "com.insta.hms.messaging.providers.ApptRescheduleMessageBuilder");
    builderMap.put("sms_doctor_appointments",
        "com.insta.hms.messaging.providers.DoctorAppointmentMessageBuilder");
    builderMap.put("sms_report_ready",
        "com.insta.hms.messaging.providers.ReportReadyMessageBuilder");
    builderMap.put("sms_appointment_reminder",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("email_diag_report",
        "com.insta.hms.messaging.providers.DiagReportMessageBuilder");
    builderMap.put("notification_diag_report_signed_off",
        "com.insta.hms.messaging.providers.DiagNotificationSignedOffBuilder");
    builderMap.put("email_phr_diag_report",
        "com.insta.hms.messaging.providers.OpPhrDiagReportMessageBuilder");
    builderMap.put("email_phr_diag_report_cancel",
        "com.insta.hms.messaging.providers.PhrDiagReportCancelMessageBuilder");
    builderMap.put("email_phr_gen_doc_finalize",
        "com.insta.hms.messaging.providers.PhrGenericDocMessageBuilder");
    builderMap.put("email_phr_gen_doc_delete",
        "com.insta.hms.messaging.providers.PhrGenericDocDeleteMessageBuilder");
    builderMap.put("email_phr_op_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("email_phr_ip_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("email_phr_pharmacy_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("email_op_bn_cash_bill",
        "com.insta.hms.messaging.providers.BillEmailMessageBuilder");
    builderMap.put("email_manual_op_bn_cash_bill",
        "com.insta.hms.messaging.providers.ManualBillEmailMessageBuilder");
    builderMap.put("sms_patient_admitted",
        "com.insta.hms.messaging.providers.AdmissionMessageBuilder");
    builderMap.put("sms_op_patient_admitted",
        "com.insta.hms.messaging.providers.OpAdmissionMessageBuilder");
    builderMap.put("sms_vaccine_reminder",
        "com.insta.hms.messaging.providers.VaccReminderMessageBuilder");
    builderMap.put("sms_appointment_cancellation",
        "com.insta.hms.messaging.providers.AppointmentCancelMessageBuilder");
    builderMap.put("sms_appointment_details_change",
        "com.insta.hms.messaging.providers.ApptDetailsChangeMessageBuilder");
    builderMap.put("notification_bill_cancellation",
        "com.insta.hms.messaging.providers.BillCancellationMessageBuilder");
    builderMap.put("sms_next_day_appointment_reminder",
        "com.insta.hms.messaging.providers.PatientNextDayAppointmentMessageBuilder");
    builderMap.put("sms_edit_patient_access",
        "com.insta.hms.messaging.providers.EditPatientAppointmentAccessMessageBuilder");
    builderMap.put("email_purches_oder_report",
        "com.insta.hms.messaging.providers.PoReportMessageBuilder");
    builderMap.put("sms_bill_payment_received",
        "com.insta.hms.messaging.providers.PaymentReceivedMessageBuilder");
    builderMap.put("sms_bill_refund", "com.insta.hms.messaging.providers.RefundMessageBuilder");
    builderMap.put("sms_patient_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("sms_next_of_kin_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("sms_doctor_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("sms_appointment_confirmation_for_doctor",
        "com.insta.hms.messaging.providers.AppointmentConfirmationDoctorMessageBuilder");
    builderMap.put("sms_advance_paid",
        "com.insta.hms.messaging.providers.AdvancePaymentMessageBuilder");
    builderMap.put("sms_daily_collection",
        "com.insta.hms.messaging.providers.DailyCollectionMessageBuilder");
    builderMap.put("sms_discount_given",
        "com.insta.hms.messaging.providers.DiscountSmsMessageBuilder");
    builderMap.put("sms_dynamic_appointment_reminder",
        "com.insta.hms.messaging.providers.DynamicAppointmentReminderMessageBuilder");
    builderMap.put("sms_patient_due_for_visit",
        "com.insta.hms.messaging.providers.PatientDueMessageBuilder");
    builderMap.put("email_ip_phr_diag_report",
        "com.insta.hms.messaging.providers.IpPhrDiagReportMessageBuilder");
    builderMap.put("email_phr_prescription",
        "com.insta.hms.messaging.providers.PhrPrescriptionMessageBuilder");
    builderMap.put("sms_appointment_booked",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("sms_deposit_paid",
        "com.insta.hms.messaging.providers.PatientDepositMessageBuilder");
    builderMap.put("sms_enable_mobile_access",
        "com.insta.hms.messaging.providers.MobileAccessMessageBuilder");
    builderMap.put("email_enable_mobile_access",
        "com.insta.hms.messaging.providers.MobileAccessMessageBuilder");

    // deprecated welcome messages
    builderMap.put("sms_patient_on_patient_admitted",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_revise_patient_on_patient_admitted",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");

    // builders for new welcome messages
    builderMap.put("sms_patient_on_ip_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_family_on_ip_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_patient_on_op_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_family_on_op_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_patient_on_op_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_family_on_op_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_patient_on_ip_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("sms_family_on_ip_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");

    builderMap.put("sms_patient_on_discharge",
        "com.insta.hms.messaging.providers.PatientDischargeMessageBuilder");
    builderMap.put("sms_nok_on_patient_discharge",
        "com.insta.hms.messaging.providers.PatientDischargeNokMessageBuilder");
    builderMap.put("sms_to_doctor_on_pat_discharge",
        "com.insta.hms.messaging.providers.PhysicalDischargeMessageBuilder");
    builderMap.put("message_for_lab_critical_val",
        "com.insta.hms.messaging.providers.CriticalLabTestsMessageBuilder");
    builderMap.put("sms_feedback_reminder",
        "com.insta.hms.messaging.providers.FeedbackSmsMessageBuilder");
    builderMap.put("email_on_coder_review_update",
        "com.insta.hms.messaging.providers.CoderReviewUpdateMessageBuilder");

    // replicas
    builderMap.put("email_appointment_planner",
        "com.insta.hms.messaging.providers.AppointmentPlannerMessageBuilder");
    builderMap.put("email_appointment_confirmation",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("email_followup_reminder",
        "com.insta.hms.messaging.providers.FollowupMessageBuilder");
    builderMap.put("email_appointment_reschedule",
        "com.insta.hms.messaging.providers.ApptRescheduleMessageBuilder");
    builderMap.put("email_doctor_appointments",
        "com.insta.hms.messaging.providers.DoctorAppointmentMessageBuilder");
    builderMap.put("email_report_ready",
        "com.insta.hms.messaging.providers.ReportReadyMessageBuilder");
    builderMap.put("email_appointment_reminder",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("sms_diag_report", "com.insta.hms.messaging.providers.DiagReportMessageBuilder");
    builderMap.put("notification_diag_report_signed_off",
        "com.insta.hms.messaging.providers.DiagNotificationSignedOffBuilder");
    builderMap.put("sms_phr_diag_report",
        "com.insta.hms.messaging.providers.OpPhrDiagReportMessageBuilder");
    builderMap.put("sms_phr_diag_report_cancel",
        "com.insta.hms.messaging.providers.PhrDiagReportCancelMessageBuilder");
    builderMap.put("sms_phr_gen_doc_finalize",
        "com.insta.hms.messaging.providers.PhrGenericDocMessageBuilder");
    builderMap.put("sms_phr_gen_doc_delete",
        "com.insta.hms.messaging.providers.PhrGenericDocDeleteMessageBuilder");
    builderMap.put("sms_phr_op_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("sms_phr_ip_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("sms_phr_pharmacy_bill_paid",
        "com.insta.hms.messaging.providers.PhrBillMessageBuilder");
    builderMap.put("sms_op_bn_cash_bill",
        "com.insta.hms.messaging.providers.BillEmailMessageBuilder");
    builderMap.put("sms_manual_op_bn_cash_bill",
        "com.insta.hms.messaging.providers.ManualBillEmailMessageBuilder");
    builderMap.put("email_patient_admitted",
        "com.insta.hms.messaging.providers.AdmissionMessageBuilder");
    builderMap.put("email_op_patient_admitted",
        "com.insta.hms.messaging.providers.OpAdmissionMessageBuilder");
    builderMap.put("email_vaccine_reminder",
        "com.insta.hms.messaging.providers.VaccReminderMessageBuilder");
    builderMap.put("email_appointment_cancellation",
        "com.insta.hms.messaging.providers.AppointmentCancelMessageBuilder");
    builderMap.put("email_appointment_details_change",
        "com.insta.hms.messaging.providers.ApptDetailsChangeMessageBuilder");
    builderMap.put("notification_bill_cancellation",
        "com.insta.hms.messaging.providers.BillCancellationMessageBuilder");
    builderMap.put("email_next_day_appointment_reminder",
        "com.insta.hms.messaging.providers.PatientNextDayAppointmentMessageBuilder");
    builderMap.put("email_edit_patient_access",
        "com.insta.hms.messaging.providers.EditPatientAppointmentAccessMessageBuilder");
    builderMap.put("sms_purches_oder_report",
        "com.insta.hms.messaging.providers.PoReportMessageBuilder");
    builderMap.put("email_bill_payment_received",
        "com.insta.hms.messaging.providers.PaymentReceivedMessageBuilder");
    builderMap.put("email_bill_refund", "com.insta.hms.messaging.providers.RefundMessageBuilder");
    builderMap.put("email_patient_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("email_next_of_kin_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("email_doctor_ward_bed_shift",
        "com.insta.hms.messaging.providers.WardShiftingMessageBuilder");
    builderMap.put("email_appointment_confirmation_for_doctor",
        "com.insta.hms.messaging.providers.AppointmentConfirmationDoctorMessageBuilder");
    builderMap.put("email_advance_paid",
        "com.insta.hms.messaging.providers.AdvancePaymentMessageBuilder");
    builderMap.put("email_daily_collection",
        "com.insta.hms.messaging.providers.DailyCollectionMessageBuilder");
    builderMap.put("email_discount_given",
        "com.insta.hms.messaging.providers.DiscountSmsMessageBuilder");
    builderMap.put("email_dynamic_appointment_reminder",
        "com.insta.hms.messaging.providers.DynamicAppointmentReminderMessageBuilder");
    builderMap.put("email_patient_due_for_visit",
        "com.insta.hms.messaging.providers.PatientDueMessageBuilder");
    builderMap.put("sms_ip_phr_diag_report",
        "com.insta.hms.messaging.providers.IpPhrDiagReportMessageBuilder");
    builderMap.put("sms_phr_prescription",
        "com.insta.hms.messaging.providers.PhrPrescriptionMessageBuilder");
    builderMap.put("email_appointment_booked",
        "com.insta.hms.messaging.providers.PatientAppointmentMessageBuilder");
    builderMap.put("email_deposit_paid",
        "com.insta.hms.messaging.providers.PatientDepositMessageBuilder");
    builderMap.put("email_enable_mobile_access",
        "com.insta.hms.messaging.providers.MobileAccessMessageBuilder");
    builderMap.put("sms_enable_mobile_access",
        "com.insta.hms.messaging.providers.MobileAccessMessageBuilder");

    // builders for new welcome messages
    builderMap.put("email_patient_on_ip_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_family_on_ip_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_patient_on_op_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_family_on_op_admission",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_patient_on_op_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_family_on_op_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_patient_on_ip_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");
    builderMap.put("email_family_on_ip_revisit",
        "com.insta.hms.messaging.providers.PatientAdmissionMessageBuilder");

    builderMap.put("email_patient_on_discharge",
        "com.insta.hms.messaging.providers.PatientDischargeMessageBuilder");
    builderMap.put("email_nok_on_patient_discharge",
        "com.insta.hms.messaging.providers.PatientDischargeNokMessageBuilder");
    builderMap.put("email_to_doctor_on_pat_discharge",
        "com.insta.hms.messaging.providers.PhysicalDischargeMessageBuilder");
    builderMap.put("message_for_lab_critical_val",
        "com.insta.hms.messaging.providers.CriticalLabTestsMessageBuilder");
    builderMap.put("email_feedback_reminder",
        "com.insta.hms.messaging.providers.FeedbackSmsMessageBuilder");
    builderMap.put("sms_on_coder_review_update",
        "com.insta.hms.messaging.providers.CoderReviewUpdateMessageBuilder");
    builderMap.put("sms_waitlist",
        "com.insta.hms.messaging.providers.WaitlistMessageBuilder");
    builderMap.put("email_waitlist",
        "com.insta.hms.messaging.providers.WaitlistMessageBuilder");

    builderMap.put("ADT_04", "com.insta.hms.messaging.hl7.providers.HL7ADT04Builder");
    builderMap.put("ADT_08", "com.insta.hms.messaging.hl7.providers.HL7ADT08Builder");
    builderMap.put("ADT_18", "com.insta.hms.messaging.hl7.providers.HL7ADT18Builder");
    builderMap.put("email_prescription_manual",
        "com.insta.hms.messaging.providers.PrescriptionEmailMessageBuilder");
    builderMap.put("email_prescription_auto",
        "com.insta.hms.messaging.providers.PrescriptionEmailMessageBuilder");
  }

  static {
    defaultBuilderMap.put("EMAIL", DEFAULT_MESSAGE_BUILDER);
    defaultBuilderMap.put("SMS", DEFAULT_MESSAGE_BUILDER);
    defaultBuilderMap.put("NOTIFICATION", DEFAULT_NOTIFICATION_MESSAGE_BUILDER);
  }

  /**
   * Gets the builder.
   *
   * @param messageTypeId
   *          the message type id
   * @param messageMode
   *          the message mode
   * @return the builder
   */
  public static MessageBuilder getBuilder(String messageTypeId, String messageMode) {
    // get the builder based on message type most specific
    String builderClass = builderMap.get(messageTypeId);
    MessageBuilder builder = null;
    if (null != messageMode && null == builderClass) {
      // if specific builder not available, get the generic one applicable to mode
      builderClass = defaultBuilderMap.get(messageMode);
    }
    if (null == builderClass) {
      builderClass = DEFAULT_MESSAGE_BUILDER; // you did not find anything, pick the default one
    }

    try {
      Class cls = Class.forName(builderClass);
      builder = (MessageBuilder) cls.newInstance();
    } catch (ClassNotFoundException cnfe) {
      logger.error("ClassNotFoundException in messageBuilderFactory", cnfe);
    } catch (InstantiationException ie) {
      logger.error("InstantiationException in messageBuilderFactory", ie);
    } catch (IllegalAccessException iae) {
      logger.error("IllegalAccessException in messageBuilderFactory", iae);
    }
    return builder;
  }

  /**
   * Gets the builder.
   *
   * @param messageTypeId
   *          the message type id
   * @return the builder
   */
  public static MessageBuilder getBuilder(String messageTypeId) {
    GenericDAO dao = new GenericDAO("message_types");
    String messageMode = null;
    try {
      BasicDynaBean bean = dao.findByKey("message_type_id", messageTypeId);
      if (null != bean) {
        messageMode = (String) bean.get("message_mode");
      }
    } catch (SQLException ex) {
      logger.error("Error retrieving message mode for message type :" + messageTypeId
          + ", using default mode");
      // do not error out, continue with a null message mode.
    }

    return getBuilder(messageTypeId, messageMode);
  }
}
