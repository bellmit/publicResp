package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AutoPOGeneratorJob  extends GenericJob {
	static final Logger log = LoggerFactory.getLogger(AutoPOGeneratorJob.class);
	
	private String params;
	
	public String getParams() {
        return params;
    }
	
	public void setParams(String params) {
        this.params = params;
    }
	
	JobExecutionContext jobContext;
	@Override
	public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
	    
	    setJobConnectionDetails();
		Connection con = null;
		StoreMasterDAO storeDAO = new StoreMasterDAO();	
		GenericDAO storePOMainDAO = new GenericDAO("store_po_main");
        GenericDAO storePODAO = new GenericDAO("store_po");
		List<BasicDynaBean> toInsertBeans =  new ArrayList();
		boolean status = true;
		
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			List<BasicDynaBean> centers = new CenterMasterDAO().listAll(null,"status","A");
			
			for (BasicDynaBean center : centers ){
				
				List<BasicDynaBean>  storeList =storeDAO.getStoresForAutoPO((Integer)center.get("center_id"));
				List<BasicDynaBean> reorderItems = null;	
				Map<String,List<BasicDynaBean>> supplierWiseReorderItemsMap = null;
				String po_no = null;
				for(int i=0;i<storeList.size();i++){
					po_no = null;
					//to get reorder query 
					
					//append supplier filter
					reorderItems = getReorderLevelItems(con,(Integer)storeList.get(i).get("dept_id"),storeList.get(i).get("store_rate_plan_id") == null ? 0 :(Integer)storeList.get(i).get("store_rate_plan_id"),(Integer)center.get("center_id"));
					
					//grouping reorder items by supplier
					supplierWiseReorderItemsMap = ConversionUtils.listBeanToMapListBean(reorderItems, "supplier_code");
					Iterator<Map.Entry<String, List<BasicDynaBean>>> entries = supplierWiseReorderItemsMap.entrySet().iterator();
					while(entries.hasNext()){
						log.info("Auto PO generating for Store :" + storeList.get(i).get("dept_name"));
						Map.Entry<String, List<BasicDynaBean>> entry = entries.next();
						String supplier = entry.getKey();
						List<BasicDynaBean> reorderItemsOfSupplier = entry.getValue();
						
						BasicDynaBean storePOMainBean = storePOMainDAO.getBean();
						po_no = PurchaseOrderDAO.getNextId(""+0);
						storePOMainBean.set("po_no", po_no);
						storePOMainBean.set("supplier_id",supplier);
						storePOMainBean.set("user_id","auto_po");
						storePOMainBean.set("actual_po_date",DateUtil.getCurrentTimestamp());
						storePOMainBean.set("mrp_type","I");
						storePOMainBean.set("vat_type","VAT");
						storePOMainBean.set("vat_rate", BigDecimal.ZERO);
						storePOMainBean.set("status","O");
						storePOMainBean.set("dept_id",String.valueOf((Integer)storeList.get(i).get("dept_id")));
						storePOMainBean.set("store_id",(Integer)storeList.get(i).get("dept_id"));
						storePOMainBean.set("po_date" , DateUtil.getCurrentDate());
						
						
						//loop over reorderItemsOfSupplier
						BasicDynaBean storePOBean = null;
						long TotalOfMedValue = 0;
						for(int k=0;k<reorderItemsOfSupplier.size();k++){
							
							BasicDynaBean reorderItem = reorderItemsOfSupplier.get(k);
							
							List<Integer> centerStores = new ArrayList();
							centerStores.add((Integer)((BasicDynaBean)storeList.get(i)).get("dept_id"));
							
							BasicDynaBean item = null;
							item = PurchaseOrderDAO.getItemDetailsForPo(
									(Integer)reorderItem.get("item_id"),(Integer)((BasicDynaBean)storeList.get(i)).get("dept_id"), supplier ,centerStores,0);
							
							storePOBean = storePODAO.getBean();
							storePOBean.set("po_no", po_no);
							storePOBean.set("qty_req", reorderItem.get("po_qty"));
							storePOBean.set("medicine_id",(Integer)reorderItem.get("item_id"));
							BigDecimal pkg_size = (BigDecimal)reorderItem.get("pkg_size");
							storePOBean.set("po_pkg_size",pkg_size == null ? 1 : pkg_size);
							storePOBean.set("status", "O");
							storePOBean.set("vat_rate", item.get("tax_rate")==null ? BigDecimal.ZERO :(BigDecimal)item.get("tax_rate") );
							storePOBean.set("discount", BigDecimal.ZERO);
							BigDecimal cost_price = (BigDecimal)item.get("cost_price");
							storePOBean.set("cost_price", cost_price==null? BigDecimal.ZERO : cost_price);
							
	
							Double vat_rate = item.get("tax_rate") == null ? 0.0 : ((BigDecimal)item.get("tax_rate")).doubleValue();
							Double qty_req = reorderItem.get("po_qty") == null ? 0.0 : ((BigDecimal)reorderItem.get("po_qty")).doubleValue();
							Double  mrp = item.get("mrp") == null ? 0.0 : ((BigDecimal)item.get("mrp")).doubleValue();
							long adj_mrp = Math.round(mrp/(1 + vat_rate/100));
							Double  costPrice = item.get("cost_price") == null ? 0.0 : ((BigDecimal)item.get("cost_price")).doubleValue();
							Double pkgSize = reorderItem.get("pkg_size") == null ? 1.0 : ((BigDecimal)reorderItem.get("pkg_size")).doubleValue();
						
							
							storePOBean.set("mrp", new BigDecimal(mrp));
							storePOBean.set("adj_mrp", new BigDecimal(adj_mrp));
							long vat =0;
							
							String vat_type = (String)item.get("tax_type");
							if (vat_type.equals("M")) {							
								vat = Math.round(mrp * qty_req / pkgSize * vat_rate/100 );
							} else if (vat_type.equals("MB")) {
								vat = Math.round(mrp * (qty_req + 0) / pkgSize * vat_rate/100 );
							} else if (vat_type.equals("C")) {
								vat = Math.round((costPrice * qty_req / pkgSize - 0 + 0) * vat_rate/100 );
							}else if (vat_type.equals("CB")) {
								vat = Math.round((costPrice * (qty_req + 0 )/ pkgSize - 0 ) * vat_rate/100 );
							}
							
							storePOBean.set("vat_type",vat_type);
							long med_total = Math.round(costPrice * qty_req / pkgSize- 0 + vat );
							TotalOfMedValue = TotalOfMedValue + med_total;
							
							BigDecimal vatValue = BigDecimal.valueOf(vat);
							BigDecimal medTotal = BigDecimal.valueOf(med_total);
							
							storePOBean.set("vat",vatValue);
							storePOBean.set("med_total",medTotal);
							
							//add detail bean to toInsertBeans.add(storePOBean);
							toInsertBeans.add(storePOBean);
						}
						
							//insert all
						    BigDecimal POTotalOfMedValue = BigDecimal.valueOf(TotalOfMedValue);
							storePOMainBean.set("po_total",POTotalOfMedValue);
							status = status && storePOMainDAO.insert(con, storePOMainBean);
							
							status = status && storePODAO.insertAll(con, toInsertBeans);
							toInsertBeans.clear();
							
							log.info("Auto PO Number :" + po_no);
										
					}
					
					if ( po_no != null ){
						//update stores last auto po date
						int updateLastAutoPODate  = storeUpdate(con,(Integer)storeList.get(i).get("dept_id"));
					}
				}
				
			}
			
		}catch(Exception e){
			log.error(e.getMessage());
			throw new JobExecutionException(e.getMessage());
		}finally {
			
			try{
				DataBaseUtil.commitClose(con, status);
			} catch(SQLException se) {
				log.error("Failed POItem lists ", se);
			}
			
		}
		
	}
	
	
	
    public static final String ITEMS_UNDER_REORDER_LEVEL_QUERY=
       "select * from ("
      + "SELECT *, (max_level - min_level) AS po_qty,(reorder_level-availableqty) as ord_qty FROM  ("
      +" SELECT itd.medicine_id AS item_id, coalesce(SUM(stock_qty),0) AS availableqty,  SUM(indent_qty) "
      +" 	AS indentqty,sum(po_qty) AS poqty,  SUM(pur_qty) AS flaggedqty,SUM(consumption_qty) "
      +" 	AS consumedqty,SUM(reorder_level) AS reorder_level,  SUM(danger_level) AS danger_level,SUM(max_level) "
      +" 	AS max_level,  SUM(min_level) AS min_level, medicine_name, issue_base_unit AS pkg_size,  "
      +" 	itps.supplier_code,itps.center_id as supplier_center_id, coalesce(sup.supplier_name,'')  "
      +" 	AS pref_supplier_name, seg.service_group_id,ssg.service_sub_group_id,"
      +"   COALESCE(sir.tax_rate,itd.tax_rate) as tax_rate, "
      +"    COALESCE(sir.tax_type,itd.tax_type) as tax_type,sup.cust_supplier_code, itd.cust_item_code"
      +" FROM ( SELECT r.medicine_id , SUM(min_level) AS min_level, SUM(max_level) "
      +" 	AS max_level, SUM(danger_level) AS danger_level,  SUM(reorder_level) AS reorder_level, "
      +" 	0 AS stock_qty,  0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,0 AS po_qty,0 AS consumption_qty "
      +" FROM store_reorder_levels r   WHERE dept_id IN (?) and reorder_level > 0    GROUP BY medicine_id, "
      +" 	min_level, max_level, danger_level, reorder_level   "
      +" UNION ALL  "
      +" SELECT s.medicine_id , 0 AS min_level,"
      +" 	0 AS max_level,   0 AS danger_level, 0 AS reorder_level , sum(qty) AS stock_qty,  "     
      +" 	0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,0 AS po_qty,0 AS consumption_qty   "
      +" FROM store_stock_details s    JOIN store_item_batch_details sibd USING(item_batch_id) WHERE dept_id "
      +" 	IN (?) AND    ( sibd.exp_dt >= current_date   OR sibd.exp_dt IS null )  GROUP BY "
      +" 	s.medicine_id, min_level, max_level, danger_level , reorder_level   "
      +" UNION ALL  "
      + "SELECT  medicine_id , 0 AS min_level, 0 AS max_level, 0 AS danger_level, "
      +" 	0 AS reorder_level ,   0 AS stock_qty,  SUM(qty-qty_fullfilled) AS indent_qty , 0 AS sales_qty,  "
      +" 	COALESCE((select sum(pnd.qty - pnd.qty_fullfilled) WHERE purchase_flag='Y' AND pnd.po_no IS NULL), "
      +" 	0)  AS pur_qty,0 AS po_qty,0 AS consumption_qty  "
      + "FROM store_indent_main pndm  "
      +" JOIN store_indent_details pnd USING (indent_no)  WHERE pnd.status::text <> 'F'::text   "
      +" 	AND indent_store IN (?)  GROUP BY medicine_id, indent_no,purchase_flag,po_no  "
      +" UNION ALL  "
      + "SELECT medicine_id,  0 AS min_level, 0 AS max_level,  0 AS danger_level, "
      +" 	0 AS reorder_level , 0 AS stock_qty,   0 AS indent_qty,  sum(quantity) AS sales_qty , "
      +" 	0 AS pur_qty,0 AS po_qty,0 AS consumption_qty  "
      + "FROM store_sales_main pmsm  "
      +" JOIN store_sales_details pms USING (sale_id)  WHERE   store_id IN (?)   GROUP BY medicine_id   "
      +" UNION ALL  "
      + "SELECT  medicine_id , 0 AS min_level, 0 AS max_level,  0 AS danger_level, "
      +" 	0 AS reorder_level , 0 AS stock_qty,    0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty, "
      +" 	SUM(ROUND((po.qty_req+po.bonus_qty_req - po.qty_received- po.bonus_qty_received),2)) AS po_qty, "
      +" 	0 AS consumption_qty   "
      + "FROM store_po_main pom   "
      + "JOIN store_po po USING (po_no)    "
      +" WHERE store_id IN (?) AND pom.status NOT IN('FC','X')  AND po.status NOT IN ('R')  "
      +" GROUP BY medicine_id   ) AS foo  " 
      +" JOIN store_item_details itd USING(medicine_id)  " 
      +" LEFT JOIN store_item_rates sir ON (itd.medicine_id = sir.medicine_id AND " 
      +" sir.store_rate_plan_id = ?) "
      +" JOIN store_category_master ic ON itd.med_category_id=ic.category_id " 
      +" JOIN manf_master m ON m.manf_code=itd.manf_name " 
	  +" JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = itd.service_sub_group_id)  "
	  +" JOIN service_groups seg ON(ssg.service_group_id = seg.service_group_id)  "
	  +" JOIN item_supplier_prefer_supplier itps ON (itps.medicine_id = itd.medicine_id)  "
	  +" LEFT JOIN supplier_master sup ON (sup.supplier_code = itps.supplier_code) "
	  +" LEFT JOIN supplier_center_master scm ON (sup.supplier_code = scm.supplier_code AND scm.status = 'A')  "
	  +" WHERE itd.status = 'A' AND sup.status = 'A' AND sup.supplier_name != '' GROUP BY  " 
	  +" itd.medicine_id,  medicine_name, issue_base_unit, itps.supplier_code, "
	  + "sir.tax_rate,itd.tax_rate, sir.tax_type,itd.tax_type,sup.cust_supplier_code, "
	  +" sup.supplier_name, itps.center_id,  seg.service_group_id, ssg.service_sub_group_id  ) "
	  +" AS outer_query )as ffoo WHERE  ord_qty > 0 AND po_qty > 0 AND reorder_level > 0 "
	  + " AND reorder_level > availableqty AND supplier_center_id = ? ";
		    
    public final String NOT_RAISED_IN_PO_FILTER = "  AND item_id "
    		+ "  NOT IN ( "
    			+ "  SELECT distinct medicine_id "
    			+ "  FROM store_po_main pom "
    			+ "  JOIN store_po po using(po_no) "
    			+ " JOIN stores s ON(s.dept_id = store_id) "
    			+ "  where pom.store_id = ? "
    			+ "  AND pom.status IN ('O','A','AA','AO','V','AV')  "
    			+ "  AND po.status IN ('A','O','P','AA','AO','V','AV') "
    			+ "  ORDER BY medicine_id "
    			+ "  ) ";

	
	public List<BasicDynaBean> getReorderLevelItems(Connection con,int storeId,int ratePlanId,int centerId) throws SQLException{
	
		PreparedStatement ps= null;
		try{
			ps = con.prepareStatement(ITEMS_UNDER_REORDER_LEVEL_QUERY + NOT_RAISED_IN_PO_FILTER);
			ps.setInt(1, storeId);
			ps.setInt(2, storeId);
			ps.setInt(3, storeId);
			ps.setInt(4, storeId);
			ps.setInt(5, ratePlanId);
			ps.setInt(6, storeId);
			ps.setInt(7, centerId);
			ps.setInt(8, storeId);
			
		  return  DataBaseUtil.queryToDynaList(ps);	
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		
	}
	
	private String store_update_query="update stores set last_auto_po_date = now() where dept_id = ?";
	public int  storeUpdate(Connection con,int storeId) throws SQLException{
		
		PreparedStatement ps= null;
		try{
			ps = con.prepareStatement(store_update_query);
			ps.setInt(1, storeId);

		  return ps.executeUpdate();	
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		
	}
	
	
}