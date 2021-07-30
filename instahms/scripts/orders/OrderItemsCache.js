// The function is useful to re cache the items in order screen

function getItems(billNo) {
	var bill = findInList(billDetails,"bill_no",billNo);

	if (!empty(addOrderDialog.getChargeRequest)) {
			if (YAHOO.lang.isArray(addOrderDialog.getChargeRequest)) {
				for (var i=0; i<getChargeRequest.length; i++) {
					YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest[i] , addOrderDialog.onGetCharge , true) ;
				}
			}else {
				YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest , addOrderDialog.onGetCharge , true) ;
			}
		}

		// clear the order table, since new rates are now applicable
		clearOrderTable(0);

		// tell orderd dialog the new org ID, so that it can use new rates
		if ( bill != null && addOrderDialog)
			 addOrderDialog.setOrgId(bill.bill_rate_plan_id);

		if(bill == null)
			addOrderDialog.setOrgId(null);
		
		var filterValue = filter;
		if(filter == 'Laboratory' || filter == 'Radiology') {
			filterValue = filter+ ',Order Sets';
		}

		var orderApplicability="";
		if(filter == 'Laboratory' || filter == 'Radiology' || filter == 'Service') {
			orderApplicability = "&order_controls_applicable=Y";
		}	
		var url =  cpath + "/master/orderItems.do?method=getOrderableItems&"+getString("js.common.message.insta.software.version")+"&"+sesHospitalId+"&mts="+masterTimeStamp+"&bill_no="+billNo;
		url = url + "&filter="+filterValue+"&orderable=Y";
		url = url + "&directBilling=&operationOrderApplicable="+operationApplicableFor;
		url = url + "&orgId="+( bill == null ? null : bill.bill_rate_plan_id)+"&visitType="+visitType;
		url = url + "&center_id="+centerId+"&tpa_id="+priSponsorId+"&dept_id="+departmentId+"&gender_applicability="+genderApplicability+orderApplicability+"&planId="+(planId == 0 ? '' : planId);

		if ( document.mainform.patientid.value != '')
			url = url + "&visit_id="+document.mainform.patientid.value;
		if ( mod_adv_packages == 'Y' )
			url = url + "&isMultiVisitPackage=true";

		// Note that since this is a GET, the results could potentially come from the browser cache.
		// This is desirable. That's why the sequence of request parameters must match the original
		// <script> in the jsp.

		var ajaxReqObject = newXMLHttpRequest();
		ajaxReqObject.open("POST", url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
				eval(ajaxReqObject.responseText);
				addOrderDialog.setNewItemList(rateplanwiseitems);
				addOrderDialog.setOrgId(rateplanwiseitems["orgId"]);
				window.rateplanwiseitems = rateplanwiseitems;
				getDoctorCharges();
			}
		}

}

