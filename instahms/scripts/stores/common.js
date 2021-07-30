var ouserAutoComp ;
var oItemAutoComp;
var isbillable = 0;

function fillUsers(field){
	var usersArray = [];
	if(hospuserlist != null){
		usersArray.length = hospuserlist.length;
		for(var k =0;k<hospuserlist.length;k++){
			usersArray[k] = hospuserlist[k].hosp_user_name;
		}
	}
	this.ousersSCDS = new YAHOO.widget.DS_JSArray(usersArray);
	ouserAutoComp = new YAHOO.widget.AutoComplete(field,'hosp_user_dropdown', this.ousersSCDS);
	ouserAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	ouserAutoComp.typeAhead = false;
	ouserAutoComp.useShadow = false;
	ouserAutoComp.allowBrowserAutocomplete = false;
	ouserAutoComp.minQueryLength = 0;
	ouserAutoComp.forceSelection = false;
	ouserAutoComp.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get(field).value;
		if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		} });
}
function refreshForm(){
	document.getElementById("refresh").value = true;
	var itemstable = document.getElementById("itemListtable");
	var tablelength = itemstable.rows.length-1;
	if(tablelength > 0){
		for(var i =tablelength;i>0;i--){
				itemstable.deleteRow(i);
		}
	}
	if (document.getElementById("Patient_field") != null){
		document.getElementById("Patient_field").class = '';
		document.getElementById("Patient_field").value='';
		document.getElementById("Patient_field").disabled=false;
	} else{
		document.getElementById("Hospital_field").class = 'required'
		document.getElementById("store").value = 'ISTORE0001';
		document.getElementById("Hospital_field").value='';

	}

	valReset();

}

function valReset () {
	document.getElementById("user_issue_nos").length = 1;
	document.getElementById("user_issue_nos").value='no';
	document.getElementById("user_issue_date").length = 1;
	document.getElementById("user_issue_date").value='';
	document.getElementById("creditbill").style.display = 'none';
	if (document.getElementById("Patient_field") != null){
		document.getElementById("patientDetails").style.display = 'none';
		document.getElementById("patientMrno").innerHTML = '';
		document.getElementById("patientName").innerHTML = '';
		document.getElementById("patientAge").innerHTML = '';
		document.getElementById("patientContactNo").innerHTML = '';
		document.getElementById("patientVisitNo").innerHTML = '';
		document.getElementById("patientDept").innerHTML = '';
		document.getElementById("patientDoctor").innerHTML = '';
		document.getElementById("patientBedType").innerHTML = ''
	}
}

function getItemDetails(){

	var item = YAHOO.util.Dom.get('items').value;
	var storeid = document.getElementById("store").value;
	var url;
	if(item != ''){
		var ajaxReqObject = newXMLHttpRequest();
		if((document.forms[0].name == 'stockissueform') || (document.forms[0].name == 'patientissueform')){
			url = "StockUserIssue.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid;
		}else{
			url = "stocktransfer.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid;
		}
		getResponseHandlerText(ajaxReqObject, fillItemDetails_common, url);
	}
}
var itemdetails;
var exp_dt = [];
var package_type = [];
var qty_avbl = [];
var item_ids = [];
var qty_available=0;
var identification_type = [];
var iss_type = [];
var stock_type = [];
var issueUnits = [];
var item_billable = [];
function fillItemDetails_common(responseText){
	isbillable = 1;
	var type = '';
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
	if (document.forms[0].name == 'stockissueform') type = 'Hospital';
	if (document.forms[0].name == 'patientissueform') type = 'Patient';
    eval("itemdetails = " + responseText);
    var index = 1;
    document.getElementById("issueUnits").innerHTML = '';
    if(itemdetails.length == 0){
    	alert("There is no available stock for the requested item");
    	document.getElementById("items").value = '';
    	document.getElementById("batch").value="";
    	document.getElementById("batch").length = 1;
    	document.getElementById("issuetype").value="";
    	document.getElementById("issuQty").value="";
    	document.getElementById("issueUnits").innerHTML = '';
    	if (document.getElementById("itemBillable") != null) document.getElementById("itemBillable").value="";
    	return false;
    }

    for(var t= 0;t<itemdetails.length;t++){
    	if(document.getElementById("inventory").value == 'transfer'){
    		document.getElementById("batch").length = 1+itemdetails.length;
	    	document.getElementById("batch").options[index].text = itemdetails[t].batch_no;
	    	document.getElementById("batch").options[index].value = itemdetails[t].batch_no;
	    	exp_dt[itemdetails[t].batch_no] = itemdetails[t].exp_dt;
	    	package_type[itemdetails[t].batch_no] = itemdetails[t].package_type;
	    	qty_avbl[itemdetails[t].batch_no] = itemdetails[t].qty;
	    	item_ids[itemdetails[t].batch_no] = itemdetails[t].medicine_id;
	    	identification_type[itemdetails[t].batch_no] = itemdetails[t].identification;
            stock_type[itemdetails[t].batch_no] = itemdetails[t].consignment_stock;
	    	document.getElementById("issuetype").value = itemdetails[0].issue_type;
	    	document.getElementById("issueUnits").innerHTML = "<b>"+itemdetails[0].issue_units+"</b>";
	    	if (itemdetails.length == 1) changeIdentifiers(itemdetails[t].batch_no);

    	}else{
    	if (document.getElementById("mrno") != null) {
    		if (patient == null) {
        		alert("Please give patient MR No");
        		return false;
        	}

    		if(itemdetails[0].billable){

	    		isbillable++;
	    		if(document.getElementById("bill_no").value == ''){
	    			alert("patient does not have any open bills, you can not issue this item");
	    			document.getElementById("items").value='';
	    			return false;
	    		}
				if(patient.billstatus == 'F'){
					alert("patient bill is finalized,you can not issue this item");
		    			document.getElementById("items").value='';
		    			return false;
				}
				if(patient.billstatus == 'S'){
					alert("patient bill is settled,you can not issue this item");
		    			document.getElementById("items").value='';
		    			return false;
				}
    			document.getElementById("creditbill").style.display = 'block';
	    	}else{
	    		document.getElementById("creditbill").style.display = 'none';
	    	}
	    }
	    document.getElementById("batch").length = 1+itemdetails.length;
	    if(itemdetails[t].qty != 0){
	    	qty_available++;
	    }
	    document.getElementById("batch").options[index].text = itemdetails[t].batch_no+"/"+itemdetails[t].qty+"/"+formatExpiry(itemdetails[t].exp_dt);
	    	document.getElementById("batch").options[index].value = itemdetails[t].batch_no;
	    	document.getElementById("issuetype").value = itemdetails[t].issue_type;
	    	exp_dt[itemdetails[t].batch_no] = itemdetails[t].exp_dt;
	    	item_ids[itemdetails[t].batch_no] = itemdetails[t].medicine_id;
	    	identification_type[itemdetails[t].batch_no] = itemdetails[t].identification;
            stock_type[itemdetails[t].batch_no] = itemdetails[t].consignment_stock;
	    	iss_type[itemdetails[t].batch_no] = itemdetails[t].issue_type;
	    	issueUnits[itemdetails[t].batch_no] = itemdetails[t].issue_units;
	    	item_billable[itemdetails[t].batch_no] = itemdetails[t].billable;
	    	if (itemdetails.length == 1) changeItems(itemdetails[t].batch_no,type);
    	}
    	index++;
    }
    if (itemdetails.length > 1){
     document.getElementById("batch").disabled = false;
     if(document.getElementById("inventory").value == 'transfer'){
     		document.getElementById("batch").selectedIndex = 0;
	    	document.getElementById("exp_dt").innerHTML = '';
			document.getElementById("pkg_type").innerHTML = '';
			document.getElementById("qty_avbl").innerHTML = '';
		}

     }
    else {
    	document.getElementById("batch").selectedIndex = 1;
    	document.getElementById("batch").disabled = true;
    	if(document.getElementById("inventory").value == 'transfer'){
	    	document.getElementById("exp_dt").innerHTML = formatExpiry(itemdetails[0].exp_dt);
			document.getElementById("pkg_type").innerHTML = itemdetails[0].package_type;
			document.getElementById("qty_avbl").innerHTML = itemdetails[0].qty;
		}
    }

    if ((itemdetails[0].identification == 'S') &&  (qty_available == 0) && ((document.forms[0].name == 'stockissueform')||(document.forms[0].name == 'patientissueform'))){
	    		alert("There is no available stock for the requested item");
		    	document.getElementById("items").value = '';
		    	document.getElementById("batch").value="";
		    	document.getElementById("batch").length = 1;
		    	document.getElementById("issuetype").value="";
		    	document.getElementById("issuQty").value="";
		    	document.getElementById("issueUnits").innerHTML = '';
		    	if (document.getElementById("itemBillable") != null) document.getElementById("itemBillable").value="";
		    	return false;
	    	}

}
function formatExpiry(dateMSecs) {
	var dateStr = '';
	if (dateMSecs != null) {
		var dateObj = new Date(dateMSecs);
		dateStr = formatDate(dateObj, 'ddmmyyyy', '-');
	}
	return dateStr;
}

function validMrno(){
}

function clearFields(){
}
function makeLabel(name, id, value) {
		var el = document.createElement('label');
		if (name!=null && name!="")
			el.name = name;
		if (id!=null && id!="")
			el.id = id;
		if (value!=null && value!="")
			el.value = value;
		return el;
	}
