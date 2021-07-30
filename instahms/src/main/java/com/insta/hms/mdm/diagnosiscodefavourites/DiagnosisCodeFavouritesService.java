/**
 * 
 */

package com.insta.hms.mdm.diagnosiscodefavourites;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class DiagnosisCodeFavouritesService.
 *
 * @author anup vishwas
 */

@Service
public class DiagnosisCodeFavouritesService extends MasterService {

  /** The diag code fav repo. */
  @LazyAutowired
  private DiagnosisCodeFavouritesRepository diagCodeFavRepo;

  /** The diag code fav validator. */
  @LazyAutowired
  private DiagnosisCodeFavouritesValidator diagCodeFavValidator;

  /**
   * Instantiates a new diagnosis code favourites service.
   *
   * @param repository
   *          DiagnosisCodeFavouritesRepository
   * @param validator
   *          DiagnosisCodeFavouritesValidator
   */
  public DiagnosisCodeFavouritesService(DiagnosisCodeFavouritesRepository repository,
      DiagnosisCodeFavouritesValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the diag code fav of code type list.
   *
   * @param searchInput
   *          String
   * @param doctorId
   *          String
   * @param codeType
   *          String
   * @return the diag code fav of code type list
   */
  public List<BasicDynaBean> getDiagCodeFavOfCodeTypeList(String searchInput, String doctorId,
      String codeType) {

    diagCodeFavValidator.validateDiagCodeFavParameters(searchInput, doctorId, codeType);
    return diagCodeFavRepo.getDiagCodeFavOfCodeTypeList(searchInput, doctorId, codeType);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return diagCodeFavRepo.getBean();
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public Integer getNextSequence() {
    return diagCodeFavRepo.getNextSequence();
  }

  /**
   * Batch insert diag code fav list.
   *
   * @param diagCodeFavList
   *          List of BasicDynaBean
   * @return the int[]
   */
  public int[] batchInsertDiagCodeFavList(List<BasicDynaBean> diagCodeFavList) {

    return diagCodeFavRepo.batchInsert(diagCodeFavList);
  }

  /**
   * Checks if is duplicate fav diag code.
   *
   * @param bean
   *          BasicDynaBean
   * @param errMap
   *          ValidationErrorMap
   * @return true, if is duplicate fav diag code
   */
  public boolean isDuplicateFavDiagCode(BasicDynaBean bean, ValidationErrorMap errMap) {
    return diagCodeFavValidator.validateCodeFavInsert(bean, errMap);
  }

}
