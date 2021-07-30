package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.scheduler.AppointmentService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "appointmentid" }, urlEntityName = { "appointment" })
public class AppointmentConfidentialityValidator implements
    ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(AppointmentConfidentialityValidator.class);

  @LazyAutowired
  private AppointmentService appointmentService;

  @Override
  public List<String> getAssociatedMrNo(List<String> appointmentIds) {
    List<String> mrnos = new ArrayList<String>();
    for (String appointmentId : appointmentIds) {
      try {
        Integer appId = Integer.parseInt(appointmentId);
        BasicDynaBean appBean = appointmentService.getAssociatedMrNoForAppointment(appId);
        if (appBean != null) {
          String mrNo = (String) appBean.get("mr_no");
          if (mrNo == null || mrNo.equals("")) {
            mrnos.add("APPOINTMENT");
          } else if (!mrnos.contains(mrNo)) {
            mrnos.add(mrNo);
          }
        }
      } catch (NumberFormatException exception) {
        logger.error("Invalid appointment id provided");
        return null;
      }
    }
    return mrnos;
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return appointmentService.isAppointmentIdValid(parameter);
  }
}
