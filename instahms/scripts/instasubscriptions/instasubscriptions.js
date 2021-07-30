var addRecordDialog;
var addPayDialog;
var maxAllowedAmount;

function init() {
	//initPayDialog();
	initRecordDialog();
}	

function openRecordPaymentDialog(recordInvoice, recordAmount, currency_code) {
	addRecordDialog.show();
	maxAllowedAmount = parseInt(recordAmount)/100;
	document.getElementById("recordInvoice").value = recordInvoice;
	document.getElementById("recordAmount").value = maxAllowedAmount;
	document.getElementById("currencyAddOn").innerHTML = currency_code;
}
		
function initRecordDialog() {
	var dialogDiv = document.getElementById("addRecordDialog");
	dialogDiv.style.display = 'block';
	addRecordDialog = new YAHOO.widget.Dialog("addRecordDialog",
		{	width:"350px",
			fixedcenter : true,
			visible:false,
			modal:true,
			constraintoviewport:true
		});
		YAHOO.util.Event.addListener('Record', 'click', recordOk, addRecordDialog, true);
		YAHOO.util.Event.addListener('Close', 'click', recordCancel, addRecordDialog, true);
		addRecordDialog.render();
}
		
function validateRecord() {
	if 	(document.getElementById("recordAmount").value == '') {
		alert("Amount must be greater than Zero");
		return false;
	}
	if (document.getElementById("recordAmount").value > maxAllowedAmount || document.getElementById("recordAmount").value <= 0 ) {
		alert("Amount must be greater than Zero and should be less than due amount. " + maxAllowedAmount)
		return false;
	}
	if (document.getElementById("paymentMethod").value == '') {
		alert("Please select Payment Method");
		return false;
	}
	var payDate = document.getElementById("paymentDate");
	if (payDate.value == "" ) {
		alert("Please enter Payment Date");
		payDate.focus();
		return false;
	}		
	if (!doValidateDateField(payDate)) return false;
	if (document.getElementById("paymentMethod").value == 'bank_transfer' || document.getElementById("paymentMethod").value == 'check'){
		if(document.getElementById("refNo").value == '') {
			alert("Please enter Reference No.");
			document.getElementById("refNo").focus();
			return false;
		}
	}
	return true;
}
		
function recordOk() {
	if (validateRecord()) {
		addRecordDialog.hide();
		document.getElementById("recordPaymentForm").submit();
	}
}
		
function recordCancel() {
	addRecordDialog.hide();
}

function payInvoice(invoice) {
	addPayDialog.show();
}
		
/* function initPayDialog() {
	var dialogDiv = document.getElementById("addPayDialog");
	dialogDiv.style.display = 'block';
	addPayDialog = new YAHOO.widget.Dialog("addPayDialog",
		{	width:"350px",
		fixedcenter : true,
		visible:false,
		modal:true,
		constraintoviewport:true
		});
	YAHOO.util.Event.addListener('Pay', 'click', payok, addPayDialog, true);
	YAHOO.util.Event.addListener('Cancel', 'click', payCancel, addPayDialog, true);
	addPayDialog.render();
}
		
function payok(){
	addPayDialog.hide();
	alert("OK");
}
		
function payCancel(){
	addPayDialog.hide();
	alert("NO")
} */
		
function paidInvoiceList(page) {
	var url = cpath + "/instasubscriptions/InstaSubscriptions.do?_method=getNextPaidInvoices&page="+page;
	var myCallback = {
		success: paidInvoiceResponse,
		failure: paidInvoiceResponse,
	};
	var transaction =
		YAHOO.util.Connect.asyncRequest('GET', url, myCallback);
}
		
function paidInvoiceResponse(response) {
	var responseJson = JSON.parse(response.responseText);
	var table = document.getElementById("paymentTable");
	var tableLength = table.rows.length;
	for(i=tableLength - 1; i >0; i--) {
		table.deleteRow(i);
	}
	tableLength = responseJson["list"].length;
	if (responseJson["page"] > 1) {
		document.getElementById("previousPage").className = "cursorOnHover";
		document.getElementById("previousPage").onclick = function(){ (paidInvoiceList(responseJson["page"] -1)); };
	} else {
		document.getElementById("previousPage").className = "noFurtherPages";
		document.getElementById("previousPage").onclick = "";
	}
	if (responseJson["nextOffset"] != null) {
		document.getElementById("nextPage").className = "cursorOnHover";
		document.getElementById("nextPage").onclick = function(){ (paidInvoiceList(responseJson["page"] +1)); };
	} else {
		document.getElementById("nextPage").className = "noFurtherPages";
		document.getElementById("nextPage").onclick = "";
	}
	for (i=1; i<=tableLength; i++) {
		var row = table.insertRow();
		var arr = Array();
		for (var j=0; j<7; j++) {
			arr.push(row.insertCell(j));
		}
		arr[0].className = "paymentHistoryHeader paymentInvoiceId";
		arr[1].className = "paymentHistoryHeader paymentDate";
		arr[2].className = "paymentHistoryHeader paymentInvoiceAmount";
		arr[3].className = "paymentHistoryHeader paymentPaidAmount";
		arr[4].className = "paymentHistoryHeader paymentPaidDate";
		arr[5].className = "paymentHistoryHeader paymentStatus";
		arr[6].className = "paymentHistoryHeader paymentDownload";
		
		arr[0].innerHTML = responseJson["list"][i-1].id;
		arr[1].innerHTML = formatDate(new Date(responseJson["list"][i-1].date*1000), 'ddmmyyyy', '-');
		arr[2].innerHTML = (responseJson["list"][i-1].total/100).toFixed(1) + " " + responseJson["list"][i-1].currency_code;
		arr[3].innerHTML = (responseJson["list"][i-1].amount_paid/100).toFixed(1) + " " + responseJson["list"][i-1].currency_code;
		arr[4].innerHTML = formatDate(new Date(responseJson["list"][i-1].paid_at*1000), 'ddmmyyyy', '-');
		arr[5].innerHTML = "<div class='invoicePaidInfo'>Paid</div>"
		arr[6].innerHTML = "<a href=" + cpath + "/instasubscriptions/InstaSubscriptions.do?_method=getInvoicePdf&invoiceId=" + responseJson["list"][i-1].id + "><input class='invoiceDownload' type='button' value='Download'></a></div>"			   		
	}
}

function resetReferenceNo() {
	var method = document.getElementById("paymentMethod").value;
	if (method == "cash" || method == "other") {
		document.getElementById("refNo").disabled = true;
		document.getElementById("refNo").value = '';
	} else {
		document.getElementById("refNo").disabled = false;
	}
}
