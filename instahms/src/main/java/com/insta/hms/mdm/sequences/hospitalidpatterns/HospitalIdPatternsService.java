package com.insta.hms.mdm.sequences.hospitalidpatterns;

import com.insta.hms.common.InputValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class HospitalIdPatternsService.
 */
@Service
public class HospitalIdPatternsService extends MasterService {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(HospitalIdPatternsService.class);

  /** The repository. */
  HospitalIdPatternsRepository repository;
  
  @LazyAutowired
  TransactionalSequenceRepository transactionalSequenceRepository;
  
  /** The Constant SEQUENCE_SUFFIX. */
  private static final String SEQUENCE_SUFFIX = "_seq";
  
  /** The Constant SEQUENCE_NAME_MAX_LENGTH. */
  private static final Integer SEQUENCE_NAME_MAX_LENGTH = 25;

  public static final String TRANSACTION_TYPE_BILL_NO = "BLN";

  /**
   * Instantiates a new hospital id patterns service.
   *
   * @param idPatternRepo the r
   * @param idPatternValidator the v
   */
  public HospitalIdPatternsService(HospitalIdPatternsRepository idPatternRepo, 
                                      HospitalIdPatternsValidator idPatternValidator) {
    super(idPatternRepo, idPatternValidator);
    this.repository = idPatternRepo;
  }

  /**
   * @see com.insta.hms.mdm.MasterService#insert(org.apache.commons.beanutils.BasicDynaBean)
   */
  @Transactional
  public Integer insert(BasicDynaBean bean) {
    Integer ret = 0;
    String sequenceName = (String) bean.get("sequence_name");
    String sequenceNameSanitized;
    String sequenceNameSuffixed;
    try {
      sequenceNameSanitized =
          InputValidator.getSafeSpecialString(
                  "sequence_name",
                  sequenceName,
                  SEQUENCE_NAME_MAX_LENGTH,
                  false) //getSafeSpecialString used because sequence name can contain underscore.
              .trim();
      sequenceNameSuffixed =
          sequenceNameSanitized.toLowerCase().endsWith(SEQUENCE_SUFFIX)
              ? sequenceNameSanitized
              : sequenceNameSanitized.concat(SEQUENCE_SUFFIX);
      boolean isTransactionalSequence = TRANSACTION_TYPE_BILL_NO
          .equals(bean.get("transaction_type"));      
      
      if (isTransactionalSequence) {
        if (transactionalSequenceRepository.findByKey("sequence_name",
            sequenceNameSuffixed) == null) {
          BasicDynaBean transactionalSequenceBean = transactionalSequenceRepository.getBean();
          transactionalSequenceBean.set("sequence_name", sequenceNameSuffixed);
          transactionalSequenceBean.set("value", new Long(1));
          transactionalSequenceRepository.insert(transactionalSequenceBean);
        }
        bean.set("is_transactional_sequence", true);
      } else {
        boolean ifExists = repository.isSequenceExists(sequenceNameSuffixed);
        if (!ifExists) {
          repository.createSequence(sequenceNameSuffixed);
        }        
      }
      bean.set("sequence_name", sequenceNameSuffixed.toLowerCase());
      bean.set("type", "Txn");
      ret = super.insert(bean);

    } catch (ValidationException validatorException) {
      logger.error("Exception occurred while validating Sequence name " 
              + validatorException.getMessage());
    } catch (IntrusionException intrusException) {
      logger.error(
          "IntrusionException is thrown. It is likely to be the result of an attack in progress. "
              + intrusException.getMessage());
    }

    return ret;
  }

  /**
   * Gets the hospital id pattern details.
   *
   * @param patternId the pattern id
   * @return the hospital id pattern details
   */
  public BasicDynaBean getHospitalIdPatternDetails(String patternId) {
    return repository.getHospIdPatternDetails(patternId);
  }

  /**
   * Gets the hospital id pattern list.
   *
   * @param bean the bean
   * @param transactionType the transaction type
   * @return the hospital id pattern list
   */
  public Map<String, List<BasicDynaBean>> getHospitalIdPatternList(
      BasicDynaBean bean, String transactionType) {
    Map<String, List<BasicDynaBean>> referenceData = new HashMap<String, List<BasicDynaBean>>();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (null != transactionType) {
      filterMap.put("transaction_type", transactionType);
    }

    List<BasicDynaBean> hospIdPatternDetails = new ArrayList<BasicDynaBean>();
    if (null != bean && bean.get("pattern_id") != null) {
      hospIdPatternDetails.add(repository.getHospIdPatternDetails((String) bean.get("pattern_id")));
    }

    referenceData.put("hospitalIdPatternList", lookup(false, filterMap));
    referenceData.put("hospIdPatternDetail", hospIdPatternDetails);

    return referenceData;
  }
}
