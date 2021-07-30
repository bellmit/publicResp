
function getEditResults(anchor, params, id, toolbar) {
	var reportId = '';
	var visitId = '';
	var visitType = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'reportId')
			reportId = paramvalue;
		if (paramname == 'visitid')
			visitId = paramvalue
	}

 	var form = document.mainForm;
	var check = form.checkBox;
	form.visitId.value = visitId;
	form.reportId.value = reportId;
	var flag = false;
	var prescriptionArray = [];
	var index = 0;
	if(reportId==-1) {
	  form.reportId.value = '';
	  //when user clicks on Edit corresponding to 'No Report';
	   for(var i=0;i<prescriptionJSON.length;i++){
			var record = prescriptionJSON[i];
			if(record["visitId"]==visitId){
	  			var reportList = record["reportList"];
	  			//alert("no of repports: " + reportList.length);
	  			for(var j=0;j<reportList.length;j++){
					var report = reportList[j];
					if(report["reportName"]=='No Report'){
						var testList = report["serviceList"];
						//alert("no of tests have NO Report: "+testList.length);
						for(var k=0;k<testList.length;k++){
							var test =  testList[k];
							var prescribedId =test["prescribedId"];
							//alert("prescribedId: "+prescribedId);
							prescriptionArray[index++]=prescribedId;
						}
					}
	  			}
			}
	   }
	   //alert(prescriptionArray);
	   if(check.length != undefined){
			outer:for(var  checkedIndex=0; checkedIndex<check.length; checkedIndex++) {
				if(check[checkedIndex].checked){
					var checedPrescribedId = check[checkedIndex].value;
					for(var i=0;i<prescriptionArray.length;i++){
						if(prescriptionArray[i] == checedPrescribedId){
							flag = true;
							break outer;
						}
					}
				}
			}
	   }else{
		if(check.checked){flag = true;}
	   }
	}else{
	   //when user tries to edit testList corresponding to Report.
		form.reportId.value = reportId;
		flag = true;
	}


	if(flag == false) {
		alert("Please select one or more services corresponding to a  MR No. for editing");
		return false;
	}
	form.action = cPath+"/serviceModule/ManageReport.do?method=getBatchConductionScreen&prescription_id="+prescribedId+"&reportId="+form.reportId.value+"&visitid="+form.visitId.value;
	form.method.value = "getBatchConductionScreen";
	form.submit();
}


function checkCompleted(id){
   var form = document.conduction;
   var source = "conducted"+id;
	if(document.getElementById(source).checked){
		document.getElementById("completed"+id).value = 'C';
	}else{
		document.getElementById("completed"+id).value = 'N';
	}
}

function printReport(reportId){
	var cpath = document.conduction.cPath.value;
	var url = cpath+ '/serviceModule/Service/Service.do?method=printReport&reportId='+reportId;
	window.open(url);
}

function editTemplate(docId,prescribedId,id){
	var form =  document.conduction;
	if(docId == -1){
      obj = "template"+id;
      docId = document.getElementById(obj).value;
	  if(docId == '') 	{
	  	alert('select template to edit');
	  	document.getElementById(obj).focus();
	  	return ;
	  }
	}

	var cpath = document.conduction.cPath.value;
	var url = cpath+ '/serviceModule/Service/ServicePopup.do?method=getTemplate' ;
    url = url + "&templetId=" + docId + "&prescribedId=" + prescribedId ;

	window.open(url,'Popup_Window',"width=700, height=700,screenX=80,screenY=50,left=300,top=50,scrollbars=yes,menubar=0,resizable=yes");
}
function deleteTemplate(docId,prescribedId,id){
	var form =  document.conduction;
	if(docId == -1){
      obj = "template"+id;
      docId = document.getElementById(obj).value;
	  if(docId == '') 	{
	  	alert('select template to delete');
	  	document.getElementById(obj).focus();
	  	return ;
	  }
	}
	if(!confirm("Do you want to delete the report?")){
		return false;
	}
	form.action = "Service.do?method=deleteReport&prescription_id="+prescribedId;
	form.submit();
}
function SignOff(){
	var form = document.mainForm;
	form.method.value = "signOffSelectedReports";
	form.submit();
}
function enablePatientType() {
	var theForm = document.searchFilter;
	var disabled = theForm.patientAll.checked;

	theForm.patientIp.disabled = disabled;
	theForm.patientOp.disabled = disabled;
}
function doSearch() {
	var theForm = document.searchFilter;
	if (!doValidateDateField(theForm.fdate))
		return false;
	if (!doValidateDateField(theForm.tdate))
		return false;
	theForm.pageNum.value="";
	theForm.method.value = "getList";
	return true;
}
function enableConductionRequired(){
	var theForm = document.searchFilter;
	var disabled = theForm.conductionAll.checked;

	theForm.conductionYes.disabled = disabled;
	theForm.conductionNo.disabled = disabled;
}
function clearSearch() {
	var theForm = document.searchFilter;
	theForm.fdate.value = "";
	theForm.tdate.value = "";
	theForm.servicename.value = "";
	theForm.service_department.value = "";
	theForm.patientAll.checked = true;
	theForm.mrno.value = "";
	enablePatientType();
}