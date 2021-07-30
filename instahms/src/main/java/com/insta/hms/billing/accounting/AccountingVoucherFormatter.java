package com.insta.hms.billing.accounting;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Interface AccountingVoucherFormatter.
 */
public interface AccountingVoucherFormatter {
  
  /**
   * Format.
   *
   * @param voucher the voucher
   * @param stream the stream
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void format(Voucher voucher, OutputStream stream) throws IOException, TemplateException;

}
