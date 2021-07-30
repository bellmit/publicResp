package com.insta.hms.core.inventory.stocks;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.storeitemlotdetails.StoreItemLotDetailsService;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsvRowContext;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitemlotdetails.StoreItemLotDetailsService;
import com.insta.hms.mdm.stores.StoreService;

@Service
public class StockUploadService extends BulkDataService {
	private Logger logger = LoggerFactory.getLogger(StockUploadService.class);
	@LazyAutowired
	private StoreItemBatchDetailsService storeItemBatchDetailsService;
	@LazyAutowired
	private StoreItemLotDetailsService storeItemLotDetailsService;
	@LazyAutowired
	private StoreItemDetailsService storeItemDetailsService;
	@LazyAutowired
	private StoreService storeService;
	@LazyAutowired
	private SessionService sessionService;
	private static Map<String, String> aliases = new LinkedHashMap<String, String>();
	private Map<String, List<String[]>> csvData = new HashMap<String, List<String[]>>();
	

	MessageUtil messageUtil = null;

	static {
		aliases.put("dept_id", "Store");
		aliases.put("medicine_id", "Item Name");
		aliases.put("batch_no", "Batch No.");
		aliases.put("exp_dt", "Expiry Date");
		aliases.put("package_cp", "Cost Price");
		aliases.put("package_sp", "MRP");
		aliases.put("qty", "Qty");
		aliases.put("consignment_stock", "Consignment Stock");
	}

	public StockUploadService(StoreStockDetailsRepository r, StockUploadValidator v, StockUploadCSVBulkDataEntity csvEntity) {
		super(r, v, csvEntity);
	}

	@Transactional(rollbackFor = Exception.class)
	public String importData(MultipartFile file,
			Map<String, MultiValueMap<Object, Object>> feedback) {
		return super.importData(file, feedback);
	}

	public Map<String, String> getDynamicHeaderAliases() {
		return aliases;
	}


	public Integer getItemBatchId(String batchNo, Integer medicineId) {
		Map<String,Object> filterMap = new HashMap<String,Object>();
        filterMap.put("medicine_id", medicineId);
        filterMap.put("batch_no", batchNo);
        
		BasicDynaBean batchBean = storeItemBatchDetailsService.findByKey(filterMap);
		
		Integer itemBatchId = 0;
		if (batchBean != null) 
			 itemBatchId = (Integer) batchBean.get("item_batch_id");
		return itemBatchId;
	}
	
	public Map<String, List<String[]>> getCsvData() {
		return csvData;
	}

	@Override
	public Map<String, List<BasicDynaBean>> getMasterData() {
		Map<String, List<BasicDynaBean>> masterData = new HashMap<String, List<BasicDynaBean>>();
		masterData.put("dept_id", storeService.lookup(false));
		masterData.put("medicine_id", storeItemDetailsService.lookup(false));
		return masterData;
	}

	@Override
	protected boolean preUpdate(CsvRowContext rowContext) {
		String[] rowData = rowContext.getRow();
		BasicDynaBean bean = rowContext.getBean();
	  
		String userId = (String) sessionService.getSessionAttributes().get("userId");
		BasicDynaBean itemBatchDetailsBean = null;
		String expDate = rowData[3];
		String batchNo = (String)bean.get("batch_no");

		Integer itemBatchId = getItemBatchId(batchNo,(Integer) bean.get("medicine_id"));
		if (itemBatchId != 0) {
			bean.set("item_batch_id", itemBatchId);
		} else {
			// Insert new item batch id for
			itemBatchDetailsBean = storeItemBatchDetailsService.getBean();
			itemBatchDetailsBean.set("medicine_id", (Integer) bean.get("medicine_id"));
			itemBatchDetailsBean.set("batch_no", batchNo);
			
			if (expDate != null) {
				// trunc exp_dt ... date_trunc('MONTH',expt_dt);
				BigDecimal packageSP = BigDecimal.ZERO;
				if (null != (BigDecimal)bean.get("package_sp")) {
					packageSP = new BigDecimal(bean.get("package_sp").toString());
				} else {
					bean.set("package_sp", packageSP); //setting default value to zero.
				}
				try {
				    String[] expDateParts = getValidatedAndFormattedDateParts(expDate);
					int expYear = Integer.parseInt(expDateParts[0]);
					int expMonth = Integer.parseInt(expDateParts[1]);
					Date uDate = DateUtil.getLastDayInMonth(expMonth, expYear);
					itemBatchDetailsBean.set("exp_dt", new java.sql.Date(uDate.getTime()));
				} catch (NumberFormatException e) {
					logger.error("Date Parsing Exception", e.getCause());
				}
				itemBatchDetailsBean.set("mrp", packageSP);
				itemBatchDetailsBean.set("username", userId);
				
				// Insert itemBatchDetailsBean and get the
				// generated item_batch_id and
				// Update newly created item_batch_id in bean..
							
				Integer count = storeItemBatchDetailsService.insert(itemBatchDetailsBean);
				if (count != 0) {
					bean.set("item_batch_id", itemBatchDetailsBean.get("item_batch_id"));
				}
			}
		}
		BigDecimal packageCP = BigDecimal.ZERO;
		if (null != (BigDecimal)bean.get("package_cp")) {
			packageCP = new BigDecimal(bean.get("package_cp").toString());
		} else {
			bean.set("package_cp", packageCP); //setting default value to zero.
		}
		
		BasicDynaBean itemLotDetailsBean = storeItemLotDetailsService.getRepository().getBean();
		itemLotDetailsBean.set("item_batch_id",	(Integer) bean.get("item_batch_id"));
		itemLotDetailsBean.set("package_cp", packageCP);
		itemLotDetailsBean.set("grn_no", "stock_upload");
		itemLotDetailsBean.set("lot_source", "S");
		itemLotDetailsBean.set("purchase_type", "S");
		Integer count = storeItemLotDetailsService.insert(itemLotDetailsBean); // Insert itemLotDeatilsBean.
		if (count != 0) {
			bean.set("item_lot_id", itemLotDetailsBean.get("item_lot_id"));
		}
		
		//Set remaining properties to bean
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("medicine_id", bean.get("medicine_id"));
		
        BasicDynaBean itemBean = storeItemDetailsService.findByPk(params);
		if (itemBean != null) {
			bean.set("package_uom", (String) itemBean.get("package_uom"));
			bean.set("stock_pkg_size", (BigDecimal) itemBean.get("issue_base_unit"));
		}
        bean.set("username", userId);
        
        //Setting default values for not null fields.
        if (null == (BigDecimal) bean.get("qty")) {
        	bean.set("qty",BigDecimal.ZERO);
        }
        if (null == bean.get("consignment_stock")) {
        	bean.set("consignment_stock", false);
        }
        
		return true;
	}
	
    /*
     *  This method is use to return formatted date. 
     *  Example - if user pass date as 18-02-30 then this method returns 2018-02-30
     */
    public String[] getValidatedAndFormattedDateParts(String expDate) {
      StringBuilder sb = new StringBuilder(expDate);
      boolean isValidDate = isValidDate(expDate);
      if (isValidDate) {
        StringTokenizer st = new StringTokenizer(expDate, "-");
        String sYear = st.nextToken();
        if (sYear.length() == 2) {
          Integer year = DateUtil.convertTwoDigitYear(Integer.parseInt(sYear), "future");
          sb.replace(0, 2, year.toString());
        } 
      }
      return sb.toString().split("-");
    }
    /*
     *  This method Validate date pattern yy-MM-dd and yyyy-MM-dd.
     */
    public boolean  isValidDate(String date){
      boolean isValid = false;
      String expression = "^((20|21)\\d{2}|\\d{2})[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])?$";
      CharSequence inputStr = date;
      Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(inputStr);
      if(matcher.matches()){
          isValid = true;
      }
      return isValid;
   }
}
