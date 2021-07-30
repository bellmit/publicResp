package com.insta.hms.jobs.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class JobMap.
 */

public class JobMap {

  /** The Constant JOB container. */
  @SuppressWarnings("rawtypes")
  public static final Map<String, Class> JOB = new LinkedHashMap<String, Class>();

  static {
    JOB.put("AutoPOGeneratorJob", com.insta.hms.stores.AutoPOGeneratorJob.class);
    JOB.put("AutoPOCancelJob", com.insta.hms.stores.AutoPOCancelJob.class);
    JOB.put("PackageJob", com.insta.hms.batchjob.PackageJob.class);
    JOB.put("RateMasterJob", com.insta.hms.batchjob.RateMasterJob.class);
    JOB.put("UpdateStatsMessageJob", com.insta.hms.batchjob.UpdateStatsMessageJob.class);
    JOB.put("SetAppoitmentJob", com.insta.hms.batchjob.SetAppoitmentJob.class);
    JOB.put("ResetDeptTokensJob", com.insta.hms.batchjob.ResetDeptTokensJob.class);
    JOB.put("ResetSequenceJob", com.insta.hms.batchjob.ResetSequenceJob.class);
    JOB.put("OpDeactivateJob", com.insta.hms.batchjob.OpDeactivateJob.class);
    JOB.put("CloseNoChargeBillsJob", com.insta.hms.batchjob.CloseNoChargeBillsJob.class);
    JOB.put("CasefileAutoCloseJob", com.insta.hms.batchjob.CasefileAutoCloseJob.class);
    JOB.put("DailyEmailReportJob", com.insta.hms.batchjob.EmailReportJob.class);
    JOB.put("WeeklyEmailReportJob", com.insta.hms.batchjob.EmailReportJob.class);
    JOB.put("MonthlyEmailReportJob", com.insta.hms.batchjob.EmailReportJob.class);
    JOB.put("FinalizeOpenOPBills", com.insta.hms.batchjob.FinalizeOpenOPBills.class);
    JOB.put("NextDayAppointmentJob", com.insta.hms.batchjob.NextDayAppointmentJob.class);
    JOB.put("SponsorApprovalsJob", com.insta.hms.batchjob.SponsorApprovalsJob.class);
    JOB.put("PractoMessageStatusUpdateJob",
        com.insta.hms.batchjob.PractoMessageStatusUpdateJob.class);
    JOB.put("VaccReminderJob", com.insta.hms.batchjob.VaccReminderJob.class);
    JOB.put("DailyCollectionSMSJob", com.insta.hms.batchjob.DailyCollectionSMSJob.class);
    JOB.put("CEODashboardJob", com.insta.hms.batchjob.CEODashboardJob.class);

    JOB.put("BedChargeUpdateJobScheduler", com.insta.hms.ipservices.BedChargesCalculator.class);
    JOB.put("PatDataExporter", com.insta.hms.Registration.PatDataExporter.class);
    JOB.put("ScheduledAccountingExporter",
        com.insta.hms.billing.accounting.ScheduledAccountingExporter.class);
    JOB.put("PatientTokenGenerator", com.insta.hms.batchjob.PatientTokenResetJob.class);
    JOB.put("FeedbackSMSJob_1", com.insta.hms.batchjob.FeedbackSMSJob.class);
    JOB.put("FeedbackSMSJob_2", com.insta.hms.batchjob.FeedbackSMSJob.class);
    JOB.put("TaxCalculationJob", com.insta.hms.batchjob.TaxCalculationJob.class);
    JOB.put("CsvImportJob", com.insta.hms.batchjob.CsvImportJob.class);
    JOB.put("AutoCloseConsultationJob", com.insta.hms.batchjob.AutoCloseConsultationJob.class);
    JOB.put("SupplierReturnsWithDebitNoteCsvExporterJob",
            com.insta.hms.batchjob.SupplierReturnsWithDebitNoteCsvExporterJob.class);
    JOB.put("BirthdayMessageJob", com.insta.hms.batchjob.BirthdayMessageJob.class);
    JOB.put("AccountingFailedExportRetryJob", 
        com.insta.hms.core.fa.AccountingFailedExportRetryJob.class);
    JOB.put("AccountingQueueMissingBillsJob", 
        com.insta.hms.core.fa.AccountingQueueMissingBillsJob.class);
    JOB.put("RemittanceAutoProcessJob",
        com.insta.hms.integration.insurance.remittance.RemittanceAutoProcessJob.class);
    JOB.put("UpdateDiscardableSearchPrefixJob",
        com.insta.hms.batchjob.UpdateDiscardableSearchPrefixJob.class);
    JOB.put("ClearInterfaceMessageQueue", com.insta.hms.batchjob.ClearInterfaceMessageQueue.class);
  }
}
