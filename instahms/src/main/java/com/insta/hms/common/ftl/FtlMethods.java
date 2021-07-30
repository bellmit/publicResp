package com.insta.hms.common.ftl;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.StringUtil;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtlMethods {

  public static class FtlMethodQueryToDynaList implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {

      if (args.isEmpty()) {
        throw new TemplateModelException("Incorrect arguments: Need at least 1 argument: query");
      }

      String query = (String) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      Object[] params = new Object[args.size() - 1];

      for (int i = 1; i < args.size(); i++) {
        params[i - 1] = DeepUnwrap.unwrap((TemplateModel) args.get(i));
      }

      try {
        if (args.size() == 1) {
          return DataBaseUtil.queryToDynaList(query, (short) 60);
        } else {
          return DataBaseUtil.queryToDynaList(query, params, (short) 60);
        }
      } catch (java.sql.SQLException exception) {
        throw new TemplateModelException(exception);
      }
    }

  }

  public static class FtlMethodListBeanToMapBean implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {
      if (args.size() != 2) {
        throw new TemplateModelException(
            "Incorrect arguments to listtBeanToMapBean: Need exactly 2 arguments");
      }

      List beanList = (List) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      String col = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));

      return ConversionUtils.listBeanToMapBean(beanList, col);
    }
  }

  public static class FtlMethodQueryToDynaBean implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {

      if (args.isEmpty()) {
        throw new TemplateModelException("Incorrect arguments: Need at least 1 argument: query");
      }

      String query = (String) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      Object[] params = new Object[args.size() - 1];

      for (int i = 1; i < args.size(); i++) {
        params[i - 1] = DeepUnwrap.unwrap((TemplateModel) args.get(i));
      }

      try {
        if (args.size() == 1) {
          List list = DataBaseUtil.queryToDynaList(query, (short) 60);
          if (list != null && !list.isEmpty()) {
            return list.get(0);
          }
          return null;
        } else {
          return DataBaseUtil.queryToDynaBean(query, params, (short) 60);
        }
      } catch (java.sql.SQLException exception) {
        throw new TemplateModelException(exception);
      }
    }
  }

  public static class FtlMethodListBeanToMapNumeric implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {
      if (args.size() != 3) {
        throw new TemplateModelException(
            "Incorrect arguments to listBeanToMapNumeric: Need exactly 3 arguments");
      }

      List beanList = (List) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      String col = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));
      String valueCol = (String) DeepUnwrap.unwrap((TemplateModel) args.get(2));

      return ConversionUtils.listBeanToMapNumeric(beanList, col, valueCol);
    }
  }

  public static class FtlMethodListBeanToMapMapNumeric implements TemplateMethodModelEx {
    
    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {
      if (args.size() != 4) {
        throw new TemplateModelException(
            "Incorrect arguments to listBeanToMapMapNumeric: Need exactly 4 arguments");
      }

      List beanList = (List) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      String col1 = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));
      String col2 = (String) DeepUnwrap.unwrap((TemplateModel) args.get(2));
      String valueCol = (String) DeepUnwrap.unwrap((TemplateModel) args.get(3));

      return ConversionUtils.listBeanToMapMapNumeric(beanList, col1, col2, valueCol);
    }
  }

  public static class FtlMethodListBeanToMapMapMapNumeric implements TemplateMethodModelEx {
    
    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {
      if (args.size() < 5) {
        throw new TemplateModelException(
            "Incorrect arguments listBeanToMapMapMapNumeric: Need exactly 5 arguments");
      }

      List beanList = (List) DeepUnwrap.unwrap((TemplateModel) args.get(0));
      String col1 = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));
      String col2 = (String) DeepUnwrap.unwrap((TemplateModel) args.get(2));
      String col3 = (String) DeepUnwrap.unwrap((TemplateModel) args.get(3));
      String valueCol = (String) DeepUnwrap.unwrap((TemplateModel) args.get(4));

      return ConversionUtils.listBeanToMapMapMapNumeric(beanList, col1, col2, col3, valueCol);
    }
  }

  public static class FtlMethodGetDatesInRange implements TemplateMethodModelEx {
    
    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {
      if (args.size() < 3) {
        throw new TemplateModelException(
            "Incorrect arguments getDateRange: Need  atleast 3 arguments");
      }
      try {
        java.util.Date fromDate = (java.util.Date) DeepUnwrap.unwrap((TemplateModel) args.get(0));
        java.util.Date toDate = (java.util.Date) DeepUnwrap.unwrap((TemplateModel) args.get(1));
        String type = (String) DeepUnwrap.unwrap((TemplateModel) args.get(2));
        return DateUtil.getDatesInRange(fromDate, toDate, type);
      } catch (Exception exception) {
        throw new TemplateModelException(exception);
      }
    }
  }

  /**
   * Ftl files need absolute path for images, this function returns the absolute path for print
   * logo.
   */
  public static class FtlMethodGetScreenLogoPath implements TemplateMethodModelEx {
    private int centerId;

    public FtlMethodGetScreenLogoPath(int centerId) {
      this.centerId = centerId;
    }

    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {
      try {
        File tmpFile = File.createTempFile("logo_", "");
        // int center_id = RequestContext.getCenterId();
        InputStream is = PrintConfigurationsDAO.getLogo(centerId);
        if (is != null && is.available() != 0) {
          HtmlConverter.writeStreamToFile(is, tmpFile);
          is.close();
          return tmpFile.getAbsolutePath();
        } else {
          is = PrintConfigurationsDAO.getLogo(0);
          if (is != null && is.available() != 0) {
            HtmlConverter.writeStreamToFile(is, tmpFile);
            is.close();
            return tmpFile.getAbsolutePath();
          } else {
            is = GenericPreferencesDAO.getScreenLogo();
            if (is != null) {
              HtmlConverter.writeStreamToFile(is, tmpFile);
              is.close();
              return tmpFile.getAbsolutePath();
            } else {
              return " ";
            }
          }
        }
      } catch (Exception exception) {
        throw new TemplateModelException(exception);
      }
    }
  }

  /** Ftl needs number conversion to string , this function returns string for number. */
  public static class FtlMethodNumberConversion implements TemplateMethodModelEx {
    
    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {

      if (args.isEmpty()) {
        throw new TemplateModelException("Incorrect arguments: Need at least 1 argument");
      }

      try {
        BigDecimal number = (BigDecimal) DeepUnwrap.unwrap((TemplateModel) args.get(0));
        // BigDecimal number = new BigDecimal(num.doubleValue());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        String strAmount = nf.format(number);
        int index = strAmount.indexOf(".");
        strAmount = strAmount.substring(index + 1);
        NumberToWordFormat ntsc = NumberToWordFormat.wordFormat();
        String amount = ntsc.toRupeesPaise(number);
        /*
         * long paise = (int) Integer.parseInt(strAmount); if (paise != 0) { amount = amount +
         * " Rupees and " + ntsc.toWord(paise) + " Paise"; } else { amount = amount + " Rupees "; }
         */
        return amount;

      } catch (Exception exception) {
        throw new TemplateModelException(exception);
      }
    }
  }

  public static class FtlMethodNumberToWordConversion implements TemplateMethodModelEx {
    
    /* (non-Javadoc)
     * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
     */
    @Override
    public Object exec(List args) throws TemplateModelException {

      if (args.isEmpty()) {
        throw new TemplateModelException("Incorrect arguments: Need at least 1 argument");
      }

      try {
        NumberToWordFormat ntsc = NumberToWordFormat.wordFormat();
        Object number = DeepUnwrap.unwrap((TemplateModel) args.get(0));
        if (number instanceof Integer) {
          return ntsc.toWord((Integer) number);
        } else {
          return ntsc.toWord((BigDecimal) number);
        }

      } catch (Exception exception) {
        throw new TemplateModelException(exception);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see freemarker.template.TemplateMethodModelEx#exec(java.util.List)
   */
  public static class FtlMethodMaskingSensitiveData implements TemplateMethodModelEx {
    
    private static final String DEFAULT_VALUE = "default";
  
    /**
     * This function masks the input string with masking character passed.
     * @param arguments List of arguments (dataToMask and maskCharacter)
     * @return maskedString
     * @throws TemplateModelException the templateModelException
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
      if (arguments.isEmpty()) {
        throw new TemplateModelException("Incorrect arguments: Need at least 1 argument");
      }
      try {
        String dataToMask = (String) DeepUnwrap.unwrap((TemplateModel) arguments.get(0));
        String maskCharacter = (String) DeepUnwrap.unwrap((TemplateModel) arguments.get(1));
        String defaultvalue = (arguments.size() > 2)
            ? (String) DeepUnwrap.unwrap((TemplateModel) arguments.get(2))
            : null;
        boolean isMaskCharEmpty = StringUtil.isNullOrEmpty(maskCharacter);
        if (StringUtil.isNullOrEmpty(dataToMask) || isMaskCharEmpty ) {
          return null;
        } 
        if (!isMaskCharEmpty && DEFAULT_VALUE.equalsIgnoreCase(maskCharacter)) {
          return (!StringUtil.isNullOrEmpty(defaultvalue)) ? defaultvalue : DEFAULT_VALUE;
        }
        String regEx = "[a-zA-Z0-9\\s]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(dataToMask);
        return matcher.replaceAll(maskCharacter);
      } catch (Exception exception) {
        throw new TemplateModelException(exception);
      }
    }
  }
}
