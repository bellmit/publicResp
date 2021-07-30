package com.insta.hms.batchjob.pushevent;

import com.insta.hms.jobs.GenericJob;

import java.util.HashMap;
import java.util.List;

abstract class EventSubscriber extends GenericJob {

  public abstract List clientDetails(String str);
}
