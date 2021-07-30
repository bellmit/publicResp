

var toolbar = {
	combined : {
		title : "Payment Voucher (Combined)",
		imageSrc : "icons/Add.png",
		href: "/pages/payments/PaymentVoucherForCombined.do?_method=createVoucher",
		onclick: null,
		description: " Create Voucher for the selected payee (Doctor) ",
		},
	forIp : {
		title : "Payment Voucher (IP only)",
		imageSrc : "icons/Add.png",
		href: "/pages/payments/PaymentVoucherForIp.do?_method=createVoucher",
		onclick: null,
		description: " Create Voucher for the selected payee (Doctor)",
		},
	forOp : {
		title : "Payment Voucher (OP only)",
		imageSrc : "icons/Add.png",
		href: "/pages/payments/PaymentVoucherForOp.do?_method=createVoucher",
		onclick: null,
		description: " Create Voucher for the selected payee (Doctor)",
		},
	forOthers : {
		title : "Payment Voucher",
		imageSrc : "icons/Add.png",
		href: "/pages/payments/PaymentVoucherForOthers.do?_method=createVoucher",
		onclick: null,
		description: " Create Voucher for the selected payee (Others)",
		},
}


function init(){
	payeeListAutoComplete();
/*	var pHref;
	pHref = "pages/payments/PaymentVoucher.do?_method=createVoucher";
	toolbar.Create.href = pHref;*/
	createToolbar(toolbar);
	getVoucherDescription();
	getCategory();
}

function printVoucher(voucherno, printType, paymentType){
	if(voucherno!="" && voucherno != "Voucher not created"){
		window.open("../../pages/payments/PaymentVoucherPrint.do?_method=printVoucher&voucherno="+voucherno+"&printType="+printType+"&paymentType="+paymentType);
	}
}

function validateForm(){
	form = document.paymentDashboardForm;
	var fdate = document.getElementById("fdate").value;
	var tdate = document.getElementById("tdate").value;
	if (fdate != "" || tdate != ""){
		var msg = validateDateStr(document.getElementById("fdate").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("tdate").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("fdate").value,document.getElementById("tdate").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}
	}
	if (screenType == 'Payment'){
		document.forms[0]._method.value ="getPaymentDues";
	}else {
		document.forms[0]._method.value ="getPaymentDues";
	}
return true;
}

function printreport(){
	document.forms[0]._method.value = "printallvouchers";
}

var payeeListAutoComp = null;
function payeeListAutoComplete(){
	for (var i in payeeNamesList){
		var payee = payeeNamesList[i];
		if (document.getElementById("payeeId").value != ''
				&& document.getElementById("payeeId").value == payee.payee_id){
			document.forms[0].payeesName.value = payee.payee_for_payment;
		}
	}
	if (payeeListAutoComp == null){
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
		payeeListAutoComp = new YAHOO.widget.AutoComplete("payeesName", "payeeNameContainer", ds);
		payeeListAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		payeeListAutoComp.typeAhead = true;
		payeeListAutoComp.useShadow = false;
		payeeListAutoComp.allowBrowserAutocomplete = false;
		payeeListAutoComp.autoHighlight = true;
		payeeListAutoComp.minqueryLength = 0;
		payeeListAutoComp.forceSelection = true;

		payeeListAutoComp.itemSelectEvent.subscribe(getPayeeId);
		payeeListAutoComp.selectionEnforceEvent.subscribe(clearPayeeId);
	}

}

	function getPayeeId(sType, oArgs) {
		var record = oArgs[2];
		document.forms[0].payee_name.value = record[1]
	}

	function clearPayeeId(){
		document.getElementById("payeeId").value = '';
	}


function getVoucherDescription(){
	var descDataSource = new YAHOO.util.XHRDataSource(contextPath +"/pages/payments/PaymentDashboard.do");
	descDataSource.scriptQueryAppend="_method=voucherDescription";
	descDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	descDataSource.responseSchema = {
resultsList :"result",
			 fields : [  {key : "description"}	 ]
	};
	var oAutoComp = new YAHOO.widget.AutoComplete('description', 'descriptionDiv', descDataSource);
	oAutoComp.minQueryLength = 2;

	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.resultTypeList = false;
}

function getCategory(){
	var catDataSource = new YAHOO.util.XHRDataSource(contextPath +"/pages/payments/PaymentDashboard.do");
	catDataSource.scriptQueryAppend="_method=voucherCategory";
	catDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	catDataSource.responseSchema = {
resultsList :"result",
			 fields : [  {key : "category"}	 ]
	};
	var oAutoComp = new YAHOO.widget.AutoComplete('category', 'categoryDiv', catDataSource);
	oAutoComp.minQueryLength = 2;

	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.resultTypeList = false;

}

function clearPayeeName(form){
	clearForm(form);
	// also clear hidden fields that clearForm will ignore
	form._payeeName.value = "";
	form.payee_name.value = "";
}
