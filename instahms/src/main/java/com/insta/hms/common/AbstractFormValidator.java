package com.insta.hms.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Generic form validator. Can hold a list of Exceptions,Strings etc depending on sub-implementors.
 */

@NotThreadSafe
public abstract class AbstractFormValidator<T> {
  protected List<T> errorList = new ArrayList<>();
  protected boolean isValidated = false;

  protected abstract void validate();

  public boolean isValidAction() {
    process();
    return errorList.isEmpty();
  }

  public List<T> getErrorList() {
    process();
    return errorList;
  }

  private void process() {
    if (!isValidated) {
      validate();
      isValidated = true;
    }
  }

}
