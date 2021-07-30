function init(){
	document.forms[0].allType.checked = true;
	document.forms[0].incomingTests.disabled = true;
	document.forms[0].outGoing.disabled = true;
	selectordeselectAll();
}

function selectordeselectAll(){
	var length = document.forms[0].diagdept.length;
	var disabled = document.forms[0].allDept.checked;
	for (var i=0;i<length;i++){
		document.forms[0].diagdept[i].selected = disabled;
	}
}


var ddeptIds = "";
function validateDeptNames(){
	var temp="";
	var deptSelected = false;
	var len = document.forms[0].diagdept.length;
	var options = document.forms[0].diagdept;
	var deptAll = document.forms[0].allDept;

	if (deptAll.checked){
		deptSelected=true;
		ddeptIds = "all";
	}else{
		for (var i=0;i<len;i++){
			if (options[i].selected == true){
				deptSelected=true;
				if(temp==""){
					temp = options[i].value;
					ddeptIds = "'"+temp+"'";
				}else{
					temp = options[i].value;
					ddeptIds=ddeptIds+ ','+"'"+temp+"'";
				}
			}
		}
	}
	if(!deptSelected){
		return "Select atleast one department for report";
	}

}

function deselectAll(){
	document.forms[0].allDept.checked = false;
}

var sampleType = "";
function validateSampleType(){
		var form = document.forms[0];
		var temp = "";
		var disabled = form.allType.checked;
		form.incomingTests.disabled = disabled;
		form.outGoing.disabled = disabled;
		if (disabled){
			form.incomingTests.checked = !disabled;
			form.outGoing.checked = !disabled;
		}

		if(form.incomingTests.checked){
			if (temp == ""){
				temp = form.incomingTests.value;
			}else{
				temp = temp + ","+form.incomingTests.value;
			}
		}

		if(form.outGoing.checked){
			if (temp == ""){
				temp = form.outGoing.value;
			}else{
				temp = temp + ","+form.outGoing.value;
			}
		}

		if (form.allType.checked){
			temp = "'*'";
		}

		if (!form.incomingTests.checked && !form.outGoing.checked && !form.allType.checked){
			return false;
		}
		sampleType = temp;

		return true;
}


function getlabRevenue(){
		validateDeptNames();
		validateSampleType();
		var fromdate = document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
		var flag=validateFromToDate(document.forms[0].fdate, document.forms[0].todate);
		if(flag)
			window.open("labrevenue.do?method=getLabRevenueReport&fromdate="+fromdate+"&todate="+todate+"&status=print"+"&ddept="+ddeptIds+"&sampleType="+sampleType);



}


function getLabrevenueExportReport(){
		validateDeptNames();
		validateSampleType();
		var fromdate = document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
		var flag=validateFromToDate(document.forms[0].fdate, document.forms[0].todate);
		if(flag)
			window.open("labrevenue.do?method=getExportToCSV&fromdate="+fromdate+"&todate="+todate+"&status=excel"+"&ddept="+ddeptIds+"&sampleType="+sampleType);

}