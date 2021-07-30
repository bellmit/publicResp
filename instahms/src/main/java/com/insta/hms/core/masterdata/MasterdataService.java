package com.insta.hms.core.masterdata;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class MasterdataService {
  @LazyAutowired
  private DoctorService doctorService;
  @LazyAutowired
  private DepartmentService departmentService;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private MessageUtil messageUtil;

  public enum ReturnCode {
    FAILED("1021", HttpStatus.BAD_REQUEST.value(),
        "Invalid parameter value for status"), SUCCESS("2001", HttpStatus.OK.value(), "Success");

    private ReturnCode(String errorCode, int responseStatus, String returnMessage) {
      this.returnCode = errorCode;
      this.responseStatus = responseStatus;
      this.returnMessage = returnMessage;
    }

    private static Map<String, ReturnCode> returnCodeObjMap = new HashMap<String, ReturnCode>();

    static {
      for (ReturnCode returnCode : ReturnCode.values()) {
        returnCodeObjMap.put(returnCode.getReturnCode(), returnCode);
      }
    }

    private String returnCode;
    private int responseStatus;
    private String returnMessage;

    public String getReturnCode() {
      return this.returnCode;
    }

    public String getReturnMessage() {
      return this.returnMessage;
    }

    public int getHTTPResponseStatus() {
      return this.responseStatus;
    }

    public static Map<String, ReturnCode> getReturnCodeMap() {
      return Collections.unmodifiableMap(returnCodeObjMap);
    }

  }

  /**
   * Gets the master data.
   *
   * @param params the params
   * @return the master data
   */
  public Map<String, Object> getMasterData(Map<String, String[]> params) {
    boolean sendOnlyActiveData = true;
    if (params != null && params.get("status") != null && params.get("status")[0] != null) {
      if (params.get("status")[0].equalsIgnoreCase("all")) {
        sendOnlyActiveData = false;
      } else {
        throw new ValidationException("exception.invalid.parameter", new String[] {"status"});
      }
    }
    Map<String, Object> masterDataMap = new HashMap<>();
    masterDataMap.put("hospital_doctors",
        doctorService.getAllDoctorsData(sendOnlyActiveData, true));
    masterDataMap.put("hospital_departments",
        departmentService.getAllDepartmentsData(sendOnlyActiveData));
    masterDataMap.put("hospital_center_master",
        centerService.getAllCentersData(sendOnlyActiveData));
    return masterDataMap;

  }
}
