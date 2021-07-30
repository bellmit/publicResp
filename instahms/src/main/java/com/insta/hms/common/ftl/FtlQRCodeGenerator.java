package com.insta.hms.common.ftl;

import com.insta.hms.documents.GenerateQRCodeService;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FtlQRCodeGenerator implements TemplateMethodModelEx {
   
  /* (non-Javadoc)
   * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
  */
  @Override
  public Object exec(List args) throws TemplateModelException {
    if ( args.size() < 2 ) {
      throw new TemplateModelException("Incorrect arguments: Need at least 2 arguments");
    }
    Map<String, Object> params = new HashMap();
    String text = (String) DeepUnwrap.unwrap((TemplateModel) args.get(0));
    String format = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));
    String height = null;
    String width = null;
    if ( args.size() > 2 ) {
      height =  (String) DeepUnwrap.unwrap((TemplateModel) args.get(2));
      width = (String) DeepUnwrap.unwrap((TemplateModel) args.get(3)) ;
    }
    String binary = "data:image/png;base64,";
    params.put("text", text);
    params.put("barcode_format", format);
    params.put("height", height);
    params.put("width", width);
    
    try {
      binary = binary.concat(GenerateQRCodeService.getQRCode(params));
      return binary;
    } catch (Exception exception) {
      throw new TemplateModelException(exception);
    }
  }
}
