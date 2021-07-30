var toolbar = {
View :{
		title :  "View Voucher",
		imageSrc: "icons/View.png",
		href:"#",
		onclick: null,
		description:"View voucher details"
	   },
Log :{
		title :  "View Log",
		imageSrc: "icons/View.png",
		href:"/payments/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=payments_audit_log",
		onclick: null,
		description:"View Audit Log"
	}
};



function init(){
	var vHref;
	vHref = "pages/payments/PaymentDashboard.do?_method=viewVoucherDetails";
	payeeNamesListAutoComplete();
	toolbar.View.href = vHref;
	createToolbar(toolbar);
}



function validateForm(){
		form = document.paymentDashboardForm;
		var fdate = document.getElementById("date0").value;
		var tdate = document.getElementById("date1").value;
		if (fdate != "" || tdate != ""){
				var msg = validateDateStr(document.getElementById("date0").value,"past");
				if (msg == null){
				}else{
						alert("From "+msg);
						return false;
				}

				var msg = validateDateStr(document.getElementById("date1").value,"past");
				if (msg == null){
				}else{
						alert("To "+msg);
						return false;
				}


				if (getDateDiff(document.getElementById("date0").value,document.getElementById("date1").value)<0){
						alert("From date should not greater than Todate");
						return false;
				}
		}
		return true;
}


function getVoucherReport(){
	var paymentType = getPaymentType();
	var payeeId = document.forms[0].payee_name.value;
	var category = document.forms[0].voucher_category.value;
	var fDate = document.getElementById("date0").value;
	var tDate = document.getElementById("date1").value;

	window.open("../../pages/payments/PaymentVoucher.do?_method=printAllVouchers&payment_type="+paymentType+"&screen="+screenType+"&payeeId="+payeeId+"&category="+category+"&fDate="+fDate+"&tDate="+tDate);

}


function exportToCSV(){
	var url = cpath+"/pages/payments/PaymentVoucher.do?_method=exportPaymentDetails&screen="+screenType;
	var form = document.forms[0];
	var paramName="";
	var paramValue="";
	for(i=0;i<form.elements.length;i++){
		var type = form.elements[i].type.toLowerCase();
		if (type=='text'  || type == 'hidden' || type == 'select-one'){
			paramName = form.elements[i].name;
			paramValue = form.elements[i].value;
		}else if (type == 'checkbox' && form.elements[i].checked){
			paramName = form.elements[i].name;
            paramValue = form.elements[i].value;
		}
		if( (paramName !="") && (paramValue != "")) {
			url += "&"+ paramName +"="+paramValue;
		}
	}
	window.open(url);
}

function getPaymentType(){
	var payType = "";
	var paymentType = document.forms[0].payment_type;
	for (i=0;i<paymentType.length;i++){
		if (document.forms[0].payment_type[i].checked){
			if(document.forms[0].payment_type[i].value != ''){
				payType = payType+ "'"+document.forms[0].payment_type[i].value+"'"+",";
			}
		}
	}
	return trimAllCommas(payType);

}

function trimAllCommas(sString){
	while (sString.substring(0,1) == ",") {
		sString = sString.substring(1, sString.length);
	}
	while (sString.substring(sString.length-1, sString.length) == ","){
		sString = sString.substring(0,sString.length-1);
	}
	return sString;
}

var payeeNameAutoComp = null;
function payeeNamesListAutoComplete(){
	for (var i in payeeNamesList){
		var payees = payeeNamesList[i];
		if ((document.getElementById("payeeId").value != '')
					&& document.getElementById("payeeId").value == payees.payee_id){
			document.forms[0].payeesName.value = payees.payee_for_payment;
		}
	}

	if (payeeNameAutoComp == null){
		var resList = {
			result: payeeNamesList
		};
		var ds = new YAHOO.util.LocalDataSource(resList);
		ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		ds.responseSchema = {
		resultsList: "result",
		fields: [	{key: "payee_for_payment"},
					{key: "payee_id"}
				]
		};
		payeeNameAutoComp = new YAHOO.widget.AutoComplete("payeesName", "payeeListContainer", ds);
		payeeNameAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		payeeNameAutoComp.typeAhead = true;
		payeeNameAutoComp.useShadow = false;
		payeeNameAutoComp.allowBrowserAutocomplete = false;
		payeeNameAutoComp.autoHighlight = true;
		payeeNameAutoComp.minqueryLength = 0;
		payeeNameAutoComp.forceSelection = true;

		payeeNameAutoComp.itemSelectEvent.subscribe(getPayeeId);
		payeeNameAutoComp.selectionEnforceEvent.subscribe(clearPayeeId);
	}
}

function getPayeeId(sType, oArgs) {
		var record = oArgs[2];
		document.forms[0].payee_name.value = record[1]
}

function clearPayeeId(){
	document.forms[0].payee_name.value ='';
}

function clearPayeeName(form){
	clearForm(form);
	// also clear hidden fields that clearForm will ignore
	form._payeeName.value = "";
	form.payee_name.value = "";
}

function hidePaymentModeForPaymentVoucher() {
	var paymentModeId = "paymentModeId";
	
	$("#"+paymentModeId+" option[value='-8']").remove();
	$("#"+paymentModeId+" option[value='-6']").remove();
	$("#"+paymentModeId+" option[value='-7']").remove();
	$("#"+paymentModeId+" option[value='-9']").remove();
}
