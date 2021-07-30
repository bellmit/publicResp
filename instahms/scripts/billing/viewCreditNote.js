
function loadTemplates(obj) {
	var templateName = '';
	var subParts;
	if (obj != null) {
		templateName = obj.value;

		if (!empty(templateName)) {
			subParts = templateName.split("-");
		}
		var disabled = true;
		if (!empty(templateName) && (subParts[0] == 'CUSTOM' || subParts[0] == "CUSTOMEXP")) {
			subPart = templateName.substring(parseInt(subParts[0].length)+1,templateName.length);

		var template = findInList(templateList, "template_name", subPart);
		if (template && !empty(template.download_content_type))
			disabled = false;
		}
		document.getElementById('downloadButton').disabled = disabled;
	}
}

function billPrint(option,userNameInBillPrint) {
	
	var billNo = document.mainform.billNo.value;
	var visitId = document.mainform.visitId.value;
	var printerType = document.mainform.printType.value;
	var billType = document.mainform.printBill.value;
	var optionParts  = billType.split("-");
	console.log(billNo + "::" + visitId + "::" + printerType + "::" + billType + "::" + optionParts);
	var url = cpath + "/pages/Enquiry/billprint.do?_method=";
	if (optionParts[0] == 'BILL')
		url += "billPrint";
	else if (optionParts[0] == 'EXPS')
		url += "expenseStatement";
	else if (optionParts[0] == 'PHBI')
		url += "pharmaBreakupBill";
	else if (optionParts[0] == 'PHEX')
		url += "pharmaExpenseStmt";
	else if (optionParts[0] == "CUSTOM")
		url += "billPrintTemplate";
	else if(optionParts[0] == 'CUSTOMEXP'){
		url += "visitExpenceStatement";
	}else	{
		alert("Unknown bill print type: " + optionParts[0]);
		return false;
	}
	url += "&billNo="+billNo;		// will be ignored for expense statement
	url += "&visitId="+visitId;		// will be ignored for bills
	url += "&printUserName="+userNameInBillPrint;
	if (optionParts[1])
		url += "&detailed="+optionParts[1];

	if (optionParts[2])
		url += "&option="+optionParts[2];
	url += "&printerType="+printerType;
	url +="&billType="+optionParts[1];
	window.open(url);
}
