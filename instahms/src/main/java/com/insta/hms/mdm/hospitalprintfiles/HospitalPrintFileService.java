package com.insta.hms.mdm.hospitalprintfiles;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class HospitalPrintFileService.
 */
@Service
public class HospitalPrintFileService extends MasterService {

  /**
   * Instantiates a new hospital print file service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public HospitalPrintFileService(HospitalPrintFileRepository repo, 
       HospitalPrintFileValidator validator) {
    super(repo, validator);
  }

  /**
   * Update screen logo.
   *
   * @param imageStream the image stream
   * @return the integer
   */
  public Integer updateScreenLogo(InputStream imageStream) {
    BasicDynaBean hospPrintBean = ((HospitalPrintFileRepository) getRepository()).getBean();
    Map<String, Object> hospPrintKey = new HashMap<String, Object>();
    hospPrintKey.put("center_id", 0);
    hospPrintBean.set("screen_logo", imageStream);
    return ((HospitalPrintFileRepository) getRepository()).update(hospPrintBean, hospPrintKey);
  }

  /**
   * Gets the screen logo.
   *
   * @return the screen logo
   */
  public BasicDynaBean getScreenLogo() {
    return ((HospitalPrintFileRepository) getRepository())
        .listAll(Arrays.asList(new String[] {"screen_logo"}), "center_id", 0)
        .get(0);
  }

  /**
   * Delete screen logo.
   *
   * @return the integer
   */
  public Integer deleteScreenLogo() {
    BasicDynaBean hospPrintBean = ((HospitalPrintFileRepository) getRepository()).getBean();
    Map<String, Object> hospPrintKey = new HashMap<String, Object>();
    hospPrintKey.put("center_id", 0);
    hospPrintBean.set("screen_logo", null);
    return ((HospitalPrintFileRepository) getRepository()).update(hospPrintBean, hospPrintKey);
  }

  /**
   * Gets the screen logo size.
   *
   * @return the screen logo size
   */
  public BasicDynaBean getScreenLogoSize() {
    return ((HospitalPrintFileRepository) getRepository()).getFileSize();
  }
}
