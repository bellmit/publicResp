package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.stores.StockFIFODAO;
import com.insta.hms.stores.StoreItemStock;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sirisha.rachkonda
 *
 */
public class OTServicesBO {

	static Logger logger = LoggerFactory.getLogger(OTServicesBO.class);
	private static final GenericDAO bedOperationScheduleDAO = new GenericDAO("bed_operation_schedule");
    private static final GenericDAO otConsumableUsageDAO = new GenericDAO("ot_consumable_usage");
    private static final GenericDAO doctorConsultationDAO = new GenericDAO("doctor_consultation");


	public boolean scheduleOrCompleteOperation(BasicDynaBean operationDetails, String userName, String modConsumableActive,
			String starttimestmp,String endtimestmp,StringBuilder flashmsg) throws Exception{
        boolean sucess = true;
        Connection con = null;

        try{
	        con = DataBaseUtil.getConnection();
	        con.setAutoCommit(false);

	        int prescribedId =  (Integer)operationDetails.get("prescribed_id");
	        sucess &= (bedOperationScheduleDAO.update(con, operationDetails.getMap(), "prescribed_id", prescribedId) > 0)
	        		  && (ResourceDAO.updateAppointments(con, prescribedId,"OPE"));
	        BasicDynaBean opPrescBean = bedOperationScheduleDAO.findByKey("prescribed_id", prescribedId);
	        boolean reagentsStckReduced = (Boolean)opPrescBean.get("stock_reduced");
	        if ( operationDetails.get("status") != null && operationDetails.get("status").equals("C")
	        		&& modConsumableActive.equals("Y") && (! reagentsStckReduced ))
	        	sucess &= consumeReagents( con, opPrescBean,	userName, null,true,flashmsg);

	        //updating status to doctor consultation for Anaesthetist.

	        Map<String,String> columnData = new HashMap<String,String>();
	        Map<String,Integer> keys = new HashMap<String,Integer>();
	        BasicDynaBean bean = doctorConsultationDAO.findByKey("operation_ref", prescribedId);
	        if(null != bean) {
	        	columnData.put("status", operationDetails.get("status") != null ? (String)operationDetails.get("status"):(String)bean.get("status"));
	        	keys.put("operation_ref", prescribedId);
	        	sucess &= doctorConsultationDAO.update(con, columnData, keys) > 0;
	        }

	        //updating activity details in bill_charge table
	        ChargeDAO chrDAO = new ChargeDAO(con);

	        ChargeDTO curCharge = new BillActivityChargeDAO(con).getCharge("OPE", prescribedId);
			List<ChargeDTO> opeCurCharges = chrDAO.getChargeAndRefs(curCharge.getChargeId());

			for( ChargeDTO charge : opeCurCharges){
				charge.setActivityConducted(operationDetails.get("status") != null && operationDetails.get("status").equals("C") ? "Y" : "N");
				chrDAO.updateActivityDetails(charge);
			}

	        return sucess;
        }finally{
            DataBaseUtil.commitClose(con, sucess);
            //update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
        }
     }



	 public static boolean saveOTConsumableUsage(Connection con,String serviceId,int prescriptionId,
				String[] usageSeqNo,List reagentsRequired,Preferences pref, boolean isPrimaryOperation)throws Exception{
		String invModAct = null;
		boolean status = true;
		Map<String, Object> keys = new HashMap<String, Object>();
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			invModAct = (String)pref.getModulesActivatedMap().get("mod_stores");
			if(invModAct == null || "".equals(invModAct)){
				invModAct = "N";
			}
		}
		int noOfReagents = 0;
		int[] reagentRequired = new int[reagentsRequired.size()];
		BigDecimal[] actualQty = new BigDecimal[reagentsRequired.size()];
		if ("Y".equals(invModAct)) {
			for (int i = 0; i<reagentsRequired.size(); i++) {
				noOfReagents = reagentsRequired.size();
				DynaBean regaents = (DynaBean)reagentsRequired.get(i);
				reagentRequired[i] = (Integer)regaents.get("item_id");
				actualQty[i] = (BigDecimal)regaents.get("qty");
			}
			if (new GenericDAO("ot_consumables").findByKey("operation_id", serviceId) != null) {

				BasicDynaBean otConsumableBean = otConsumableUsageDAO.getBean();
				if(usageSeqNo[0].isEmpty()){
					for ( int reagent=0; reagent < noOfReagents; reagent++ ) {
						otConsumableBean.set("operation_id", serviceId);
						otConsumableBean.set("consumable_id", (Integer)reagentRequired[reagent]);
						otConsumableBean.set("prescription_id", prescriptionId);
						otConsumableBean.set("usage_no", otConsumableUsageDAO.getNextSequence());
						otConsumableBean.set("qty",(BigDecimal)actualQty[reagent]);
						otConsumableBean.set("operation_type",isPrimaryOperation ? "P" : "S");
					status &= otConsumableUsageDAO.insert(con, otConsumableBean);
					}
				}else{
					for(int i = 0;i<reagentsRequired.size();i++){
						DynaBean regaents = (DynaBean)reagentsRequired.get(i);
						keys.put("usage_no", Integer.parseInt(usageSeqNo[i]));
						keys.put("prescription_id", prescriptionId);
						otConsumableBean.set("qty", (BigDecimal)regaents.get("qty"));
						status &= otConsumableUsageDAO.update(con, otConsumableBean.getMap(), keys) > 0;
					}
				}
			}
		}
		return status;
	}


	public static boolean consumeReagents( Connection con, BasicDynaBean opPrescribedBean,
			String userName,String operationName,boolean isPrimaryOperation, StringBuilder flashmsg) throws SQLException, IOException, Exception {

		String opeartionType = isPrimaryOperation ? "P" : "S";
		boolean status =false;
		int prescriptionId = (Integer)opPrescribedBean.get("prescribed_id");
		String theatreId = (String) opPrescribedBean.get("theatre_name");
		int storeId = TheatreMasterDAO.getStoreOfOpearationTheatre(theatreId);
		if(operationName == null || operationName.isEmpty())
			operationName = (String) opPrescribedBean.get("operation_name");

		String operationType = isPrimaryOperation ? "P" : "S";
		Map<String,Object> filters = new HashMap<String, Object>();
		filters.put("prescription_id",prescriptionId);
		filters.put("operation_type", opeartionType);

		if ( otConsumableUsageDAO.findByKey(filters) == null ) {
			status =  StoreItemStock.updateReagents(con, operationName,
					prescriptionId, userName, storeId, null, 0, "OT", flashmsg);

			if (status && isPrimaryOperation) {
				BasicDynaBean opPrescBean = bedOperationScheduleDAO.getBean();
				opPrescBean.set("stock_reduced", true);
				status &= bedOperationScheduleDAO.update(con, opPrescBean.getMap(), "prescribed_id",	prescriptionId) > 0;
			}
		}else {
			 List reagents = OTServicesDAO.getOTConsumablesUsed(prescriptionId,operationName,opeartionType);
			 List<DynaBean> reagentsRequired = new ArrayList<DynaBean>();
			 DynaBeanBuilder builder = new DynaBeanBuilder();
			 builder.add("item_id", Integer.class).add("qty",BigDecimal.class).add("redusing_qty",BigDecimal.class);
			 DynaBean reagentsbean = builder.build();
			 for(int k = 0;k<reagents.size();k++){
				BasicDynaBean reagentsUsed = (BasicDynaBean)reagents.get(k);
				reagentsbean = builder.build();
				reagentsbean.set("item_id",reagentsUsed.get("item_id"));
				reagentsbean.set("redusing_qty", (BigDecimal)reagentsUsed.get("qty"));
				reagentsbean.set("qty", (BigDecimal)reagentsUsed.get("qty"));
				reagentsRequired.add(reagentsbean);
			 }
			 status =  StoreItemStock.updateReagents(con, operationName,
					 prescriptionId, userName, storeId, reagentsRequired, 0, "OT", flashmsg );
		 }
		if (status && isPrimaryOperation) {
			BasicDynaBean opBean = bedOperationScheduleDAO.getBean();
			opBean.set("stock_reduced", true);
			status &= (bedOperationScheduleDAO.update(con, opBean.getMap(), "prescribed_id",
					prescriptionId) > 0);
		}

		return status;
	}
}

