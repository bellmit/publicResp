package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * This Job is used to cancel the PO's after po_cancel_frequency_days are reached.
 * 
 * @author irshad
 *
 */
public class AutoPOCancelJob extends GenericJob {
	static final Logger log = LoggerFactory.getLogger(AutoPOCancelJob.class);

	private String params;
	
	public String getParams() {
        return params;
    }
	
	public void setParams(String params) {
        this.params = params;
    }
	
	@Override
	public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
		setJobConnectionDetails();
		Connection con = null;
		StoreMasterDAO storeDAO = new StoreMasterDAO();
		boolean status = true;
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			List<BasicDynaBean> centers = new CenterMasterDAO().listAll(null,"status","A");
			
			for (BasicDynaBean center : centers ){
				int centerId = (Integer)center.get("center_id");
				List<BasicDynaBean>  storeWisePOList = storeDAO.getStoresForAutoPOCancel(centerId);
				String prevStoreName = "";
				int prevStoreId = -1;
				String poString = "";
				for(int i=0; i < storeWisePOList.size(); i++){
					BasicDynaBean storeWisePOBean = storeWisePOList.get(i);
					String poNo = (String)storeWisePOBean.get("po_no");
					String curStoreName = (String)storeWisePOBean.get("dept_name");
					int curStoreId = (Integer)storeWisePOBean.get("dept_id");
					if(!prevStoreName.isEmpty() && !prevStoreName.equals(curStoreName)) {
						log.info("Auto PO Cancel for Store :" + prevStoreName);
						int updatedColumns = updatePOStatus(con, poString.substring(0, poString.length()-1), prevStoreId);
						if(updatedColumns > 0) 
							storeUpdate(con, prevStoreId);
						poString = "";
						
					}
					prevStoreId = curStoreId;
					prevStoreName = curStoreName;
					poString = poString + "'" + poNo + "'," ;
				}
				if(!poString.isEmpty()) {
					log.info("Auto PO Cancel for Store :" + prevStoreName);
					int updatedColumns = updatePOStatus(con, poString.substring(0, poString.length()-1), prevStoreId);
					int updatedItemColumns = updatePOItemStatus(con, poString.substring(0, poString.length()-1));
					if(updatedColumns > 0 && updatedItemColumns > 0) 
						storeUpdate(con, prevStoreId);
					poString = "";
				}
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			throw new JobExecutionException(e.getMessage());
		} finally {
			try{
				DataBaseUtil.commitClose(con, status);
			} catch(SQLException se) {
				log.error("Failed POItem lists ", se);
			}
			
		}
	}
	
	private int updatePOStatus (Connection con, String poList, int storeId) throws SQLException {
		
		String UPDATE_PO =  " UPDATE store_po_main SET STATUS ='X',cancelled_by='auto_po_cancel' where PO_NO IN ( ";
		UPDATE_PO += poList + ") AND store_id = ?";
		PreparedStatement ps= null;
		try{
			ps = con.prepareStatement(UPDATE_PO);
			ps.setInt(1, storeId);
			
		  return ps.executeUpdate();	
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}  	
		
	}
	
	private int updatePOItemStatus (Connection con, String poList) throws SQLException {
		
		String UPDATE_PO =  " UPDATE store_po SET STATUS ='X' where PO_NO IN ( ";
		UPDATE_PO += poList + ")";
		PreparedStatement ps= null;
		try{
			ps = con.prepareStatement(UPDATE_PO);
			
		  return ps.executeUpdate();	
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}  	
		
	}
	
	
	private final String STORE_UPDATE_QUERY = "update stores set last_auto_po_cancel_date = now() where dept_id = ?";
	
	private int storeUpdate(Connection con,int storeId) throws SQLException{
		
		PreparedStatement ps= null;
		try{
			ps = con.prepareStatement(STORE_UPDATE_QUERY);
			ps.setInt(1, storeId);

		  return ps.executeUpdate();	
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		
	}
	
}