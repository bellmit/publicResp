package com.insta.hms.core.insurance.claimhistory;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.insurance.ClaimSubmission;
import com.insta.hms.core.insurance.ClaimSubmissionClaim;
import com.insta.hms.core.insurance.ClaimSubmissionHeader;
import com.insta.hms.core.insurance.ClaimXMLDigester;
import com.insta.hms.core.insurance.InsuranceSubmissionBatchService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class ClaimHistoryService.
 */
@Service
public class ClaimHistoryService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ClaimHistoryService.class);

  /** The ins sub batch service. */
  @LazyAutowired
  private InsuranceSubmissionBatchService insSubBatchService;

  /** The claim submission history service. */
  @LazyAutowired
  private ClaimSubmissionHistoryService claimSubmissionHistoryService;

  /** The claim activity history service. */
  @LazyAutowired
  private ClaimActivityHistoryService claimActivityHistoryService;

  /**
   * Creates the claim history.
   *
   * @param submissionBatchID
   *          the submission batch ID
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public String createClaimHistory(String submissionBatchID) throws IOException {

    BasicDynaBean insSubmissionBean = insSubBatchService.findByKey(submissionBatchID);

    String fileName = (String) insSubmissionBean.get("file_name") + ".xml";
    BOMInputStream isr = null;

    File file = new File("/var/log/insta/insta-ia-sync/" + fileName);
    if (file.exists()) {
      MultipartFile multipartFile = new MockMultipartFile(file.getName(), 
          new FileInputStream(file));
      isr = new BOMInputStream(multipartFile.getInputStream());
    }

    ClaimXMLDigester xmlDigester = new ClaimXMLDigester();

    String msg = "";
    ClaimSubmission desc = null;
    try {
      if (null != isr) {
        desc = (ClaimSubmission) xmlDigester.parse(isr);
        insertClaimSubmissionHistory(submissionBatchID, desc);
      }
    } catch (Exception exception) {
      logger.error("", exception);
      msg = "Invalid XML header, please check XML tags";
      return msg;
    }
    
    if (isr == null) {
      msg = "Claim xml is not generated to create claim history entries..";
      return msg;
    } else if (desc == null) {
      msg = "File syntax not compliant with mentioned digester guidelines..";
      return msg;
    }

    return msg;
  }

  /**
   * Insert claim submission history.
   *
   * @param submissionBatchID the submission batch ID
   * @param desc the desc
   * @throws ParseException the parse exception
   */
  private void insertClaimSubmissionHistory(String submissionBatchID, ClaimSubmission desc)
      throws ParseException {
    ArrayList<ClaimSubmissionHeader> headerArr = desc.getHeader();
    ArrayList<ClaimSubmissionClaim> claims = desc.getClaim();

    ClaimSubmissionHeader header = headerArr.get(0);

    claimSubmissionHistoryService.insertClaimSubmissionHistory(submissionBatchID, header, claims);
  }

  public List<BasicDynaBean> getClaimActivityHistory(String claimId, 
      String chargeId, Integer saleItemId) {
    return claimSubmissionHistoryService.getClaimActivityHistory(claimId, chargeId, saleItemId);
  }

  /**
   * Gets the bill level claim history.
   *
   * @param fromSubmissionDate the from submission date
   * @param toSubmissionDate the to submission date
   * @return the bill level claim history
   * @throws ParseException the parse exception
   */
  public Map<String, Object> getBillLevelClaimHistory(String fromSubmissionDate,
      String toSubmissionDate) throws ParseException {
    return claimSubmissionHistoryService.getBillLevelClaimHistory(fromSubmissionDate,
        toSubmissionDate);
  }

}
