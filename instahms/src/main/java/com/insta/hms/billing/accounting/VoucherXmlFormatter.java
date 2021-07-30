package com.insta.hms.billing.accounting;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class VoucherXmlFormatter.
 */
public class VoucherXmlFormatter implements AccountingVoucherFormatter {

  /** The format. */
  private String format = null;

  /**
   * Instantiates a new voucher xml formatter.
   *
   * @param format
   *          the format
   */
  public VoucherXmlFormatter(String format) {
    this.format = format;
  }

  /**
   * @see com.insta.hms.billing.accounting
   *      .AccountingVoucherFormatter#format(com.insta.hms.billing.accounting.Voucher,
   *      java.io.OutputStream)
   */
  public void format(Voucher voucher, OutputStream stream) throws IOException, TemplateException {
    Template template = getVoucherTemplate(format);
    if (template == null) {
      return;
    }

    Map ftlMap = new HashMap();
    ftlMap.put("VOUCHER", voucher);
    StringWriter stringWriter = new StringWriter();
    template.process(ftlMap, stringWriter);

    stream.write(stringWriter.toString().getBytes());
    stream.flush();
  }

  /**
   * Gets the voucher template.
   *
   * @param format
   *          the format
   * @return the voucher template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Template getVoucherTemplate(String format) throws IOException {
    return AccountingHelper.getFormattingTemplate(format);
  }

}