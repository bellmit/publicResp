package com.insta.hms.batchjob;

public class VaccReminderJob extends MessagingJob {

  public VaccReminderJob() {
    super("vaccine_reminder");
  }

  @Override
  protected String[] getModuleDependencies() {
    return new String[] { "mod_messaging", "mod_vaccination" };
  }
}
