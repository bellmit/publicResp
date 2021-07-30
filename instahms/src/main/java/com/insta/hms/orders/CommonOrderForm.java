package com.insta.hms.orders;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class CommonOrderForm.
 *
 * @author krishna
 */
public class CommonOrderForm extends ActionForm {

  /** The ad test doc id. */
  private String[] ad_test_doc_id;

  /** The ad main row id. */
  private String[] ad_main_row_id;

  /** The ad clinical notes. */
  private String[] ad_clinical_notes;

  /** The ad test category. */
  private String[] ad_test_category;

  /** The ad notes entered. */
  private Boolean[] ad_notes_entered;

  /** The ad test row edited. */
  private Boolean[] ad_test_row_edited;

  /** The ad test doc delete. */
  private Boolean[] ad_test_doc_delete;

  /** The ad package activity index. */
  private int[] ad_package_activity_index;

  /** The ad test file upload. */
  private List ad_test_file_upload = new ArrayList();

  /**
   * Gets the ad test doc delete.
   *
   * @return the ad test doc delete
   */
  public Boolean[] getAd_test_doc_delete() {
    return ad_test_doc_delete;
  }

  /**
   * Sets the ad test doc delete.
   *
   * @param ad_test_doc_delete the new ad test doc delete
   */
  public void setAd_test_doc_delete(Boolean[] ad_test_doc_delete) {
    this.ad_test_doc_delete = ad_test_doc_delete;
  }

  /**
   * Gets the ad test category.
   *
   * @return the ad test category
   */
  public String[] getAd_test_category() {
    return ad_test_category;
  }

  /**
   * Sets the ad test category.
   *
   * @param ad_test_category the new ad test category
   */
  public void setAd_test_category(String[] ad_test_category) {
    this.ad_test_category = ad_test_category;
  }

  /**
   * Gets the ad clinical notes.
   *
   * @return the ad clinical notes
   */
  public String[] getAd_clinical_notes() {
    return ad_clinical_notes;
  }

  /**
   * Sets the ad clinical notes.
   *
   * @param ad_clinical_notes the new ad clinical notes
   */
  public void setAd_clinical_notes(String[] ad_clinical_notes) {
    this.ad_clinical_notes = ad_clinical_notes;
  }

  /**
   * Gets the ad main row id.
   *
   * @return the ad main row id
   */
  public String[] getAd_main_row_id() {
    return ad_main_row_id;
  }

  /**
   * Sets the ad main row id.
   *
   * @param ad_main_row_id the new ad main row id
   */
  public void setAd_main_row_id(String[] ad_main_row_id) {
    this.ad_main_row_id = ad_main_row_id;
  }

  /**
   * Gets the ad package activity index.
   *
   * @return the ad package activity index
   */
  public int[] getAd_package_activity_index() {
    return ad_package_activity_index;
  }

  /**
   * Sets the ad package activity index.
   *
   * @param ad_package_activity_index the new ad package activity index
   */
  public void setAd_package_activity_index(int[] ad_package_activity_index) {
    this.ad_package_activity_index = ad_package_activity_index;
  }

  /**
   * Gets the ad test file upload.
   *
   * @return the ad test file upload
   */
  public List getAd_test_file_upload() {
    return this.ad_test_file_upload;
  }

  /**
   * Sets the ad test file upload.
   *
   * @param index the index
   * @param file  the file
   */
  public void setAd_test_file_upload(int index, FormFile file) {
    // formfile elements doesn't come in the order mentioned in jsp,
    // we will get randomly, first we may get 10th element then 20th and then 1st
    // etc.,
    // so first add null values if the index is greater than the elements of the
    // list.
    // when we get actual indexed element, replace the null with element.

    while (index > this.ad_test_file_upload.size()) {
      FormFile f = null;
      this.ad_test_file_upload.add(f);
    }
    if (index < this.ad_test_file_upload.size()) {
      this.ad_test_file_upload.set(index, file); // replace
    } else {
      this.ad_test_file_upload.add(index, file);
    }
  }

  /**
   * Gets the ad test row edited.
   *
   * @return the ad test row edited
   */
  public Boolean[] getAd_test_row_edited() {
    return ad_test_row_edited;
  }

  /**
   * Sets the ad test row edited.
   *
   * @param ad_test_row_edited the new ad test row edited
   */
  public void setAd_test_row_edited(Boolean[] ad_test_row_edited) {
    this.ad_test_row_edited = ad_test_row_edited;
  }

  /**
   * Gets the ad notes entered.
   *
   * @return the ad notes entered
   */
  public Boolean[] getAd_notes_entered() {
    return ad_notes_entered;
  }

  /**
   * Sets the ad notes entered.
   *
   * @param ad_notes_entered the new ad notes entered
   */
  public void setAd_notes_entered(Boolean[] ad_notes_entered) {
    this.ad_notes_entered = ad_notes_entered;
  }

  public String[] getAd_test_doc_id() {
    return ad_test_doc_id;
  }

  public void setAd_test_doc_id(String[] ad_test_doc_id) {
    this.ad_test_doc_id = ad_test_doc_id;
  }
}
