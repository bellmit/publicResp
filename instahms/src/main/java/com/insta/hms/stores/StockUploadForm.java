package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StockUploadForm extends ActionForm {

	static Logger logger = LoggerFactory.getLogger(StockUploadForm.class);

	private static final long serialVersionUID = 1L;

	private FormFile stockXlsFile;

	public FormFile getStockXlsFile() {return stockXlsFile;}
	public void setStockXlsFile(FormFile v){this.stockXlsFile = v;}

}