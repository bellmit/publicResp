package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.common.SampleCollection;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PendingSamplesBO {
	static Logger log = LoggerFactory.getLogger(PendingSamplesBO.class);
	public static PagedList pendingSamplesList(Map filterParams, Map listingParams, BasicDynaBean diagGenericPref)
		throws SQLException, IOException, ParseException {

		Boolean hasSampleFlow = ((String) diagGenericPref.get("sampleflow_required")).equals("Y");

		filterParams.put("conducted", new String[]{"N","NRN"});
		//prescribed id's retreived using order by clause.
		PagedList pagedListPrescId = LaboratoryDAO.getDiagSchedulesPresIds(filterParams, listingParams, null);
		if (pagedListPrescId.getDtoList().size() > 0) {
			ArrayList idList = new ArrayList();
			List dtoList = pagedListPrescId.getDtoList();
			// iterate over all the prescribed ids.
			for (int i=0; i<dtoList.size(); i++) {
				idList.add(((BasicDynaBean) dtoList.get(i)).get("prescribed_id"));
			}
			// pass the prescribed id's and get the individual test sample details. without order by.
			PagedList pagedListTestDetails = PendingSamplesDAO.pendingSamplesList(filterParams, idList);

			Map<Object, BasicDynaBean> mappedList = ConversionUtils.listBeanToMapBean(pagedListTestDetails.getDtoList(), "prescribed_id");
			List modifiedList = new ArrayList();

			for (int i=0; i<idList.size() ; i++) {
				int prescId = (Integer) idList.get(i);

				BasicDynaBean bean = (BasicDynaBean) mappedList.get(prescId);

				Map test = new HashMap(bean.getMap());

				String billType = (String) test.get("bill_type");
				String billStatus = (String) test.get("payment_status");
				String patientFrom = (String) test.get("hospital");
				boolean billPaid = true;
				boolean canEdit = false;
				boolean collectSample = false;
				boolean assignOuthouse = false;

				if ( billStatus != null && billStatus.equals("U")) {
					if (billType.equals("P"))
						billPaid = false;
				}
				test.put("billPaid", billPaid);
				String sampleNeeded = (String) test.get("sample_needed");
				String houseStatus = (String) test.get("house_status");
				Boolean sampleCollected = ((String) test.get("sflag")).equals("1") ;
				Boolean resultEntryApplicable = !((String)test.get("conducted")).equalsIgnoreCase("NRN") &&
				!((String)test.get("conducted")).equalsIgnoreCase("CRN") ;

				if (hasSampleFlow || houseStatus.equals("O")) {
					if (sampleNeeded.equals("y")) {
						if (sampleCollected) {
							if (billPaid) canEdit = true;
						} else {
							collectSample = true;
						}
					} else {
						if (!houseStatus.equals("O") && billPaid) {
							canEdit = true;
						} else if (houseStatus.equals("O") &&
								((String) test.get("is_outhouse_selected")).equals("Y") &&
								billPaid) {
							canEdit = true;
						}
					}

				} else {
					if (billPaid) canEdit = true;
				}

				if ((sampleNeeded.equals("n") || (sampleNeeded.equals("y") && patientFrom.equals("incoming"))) &&
						houseStatus.equals("O") &&
						((String) test.get("is_outhouse_selected")).equals("N")){
					assignOuthouse = true;
				}
				test.put("billPaid", billPaid);
				test.put("collectSample", collectSample);
				test.put("canEdit", canEdit);
				test.put("assignOuthouse", assignOuthouse);
				test.put("resultEntryApplicable", resultEntryApplicable);
				test.put("sample_date", test.get("sample_date") != null ?
						DataBaseUtil.timeStampFormatter.format((Timestamp)test.get("sample_date")) : null);
				test.put("sample_type", test.get("sample_type"));
				modifiedList.add(test);
			}

			pagedListPrescId.setDtoList(modifiedList);
		}
		return pagedListPrescId;
	}


	public static boolean saveSamples(ArrayList<SampleCollection> scList,
			ArrayList<OutHouseSampleDetails> ohlist,ArrayList<BasicDynaBean> tpList, Connection con,int centerId)
		throws SQLException, Exception{

		boolean status = false;
		Iterator<SampleCollection> it = null;
		Iterator<OutHouseSampleDetails> ohit = null;
		Iterator<BasicDynaBean> tpIt = null;
		SampleTypeDAO dao = new SampleTypeDAO();
		Set<Integer> sampleTypes = new HashSet<Integer>();

		if(scList !=null)
			it = scList.iterator();

		if(ohlist !=null)
			ohit = ohlist.iterator();

			outer:do{

    				if (it != null) {	
			            while (it.hasNext()) {
    						SampleCollection sc = it.next();
    						sampleTypes.add(sc.getSampleTypeId());
    						status = LaboratoryDAO.insertSample(con, sc,centerId);
    						if (!status)
    							break outer;
    					}
    				}

					if(ohit != null){
                      OutHouseSampleDetails osd = null;
                      BasicDynaBean outHouseBean = null;
                      GenericDAO outSourceSampleDetailsDAO =
                          new GenericDAO("outsource_sample_details"); 
						while(ohit.hasNext()){
							osd = ohit.next();

                            outHouseBean = outSourceSampleDetailsDAO.findByKey("prescribed_id",
                                osd.getPrescribedId());

							if (outHouseBean == null ) {

								status = LaboratoryDAO.setSamplesToOuthouse(osd,con);
							}else{

								status= LaboratoryDAO.updateSamplesToOuthouse(osd,con);
							}

							if(status){
								ChargeBO chargeBo = new ChargeBO();
								String chargeId = LaboratoryDAO.getOhTestChargeId(con, osd.getPrescribedId(),ChargeDTO.CH_DIAG_LAB);
								boolean ohAmtStatus = chargeBo.updateOhPayment(con, chargeId, 
										Integer.parseInt(osd.getoutSourceId()), centerId, osd.getTestId());
								if (!ohAmtStatus) break outer;
							}else{
								break outer;
							}
						}
					}

					tpIt = tpList.iterator();
					GenericDAO testPresDAO = new GenericDAO("tests_prescribed");
					while( tpIt.hasNext() ){
						BasicDynaBean testPresBean = tpIt.next();

						if(testPresBean.get("prescribed_id") !=null && testPresDAO.update(con, testPresBean.getMap(),"prescribed_id",
								testPresBean.get("prescribed_id")) == 0)
							break outer;
					}

			}while(false);

		return status;
	}
}
