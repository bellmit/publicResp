//Global variable declaration
  var itemListTable="";
  var numRows="";
  var intNum=2;
  var intReportNum="";
  var flag="";

  function addReports(){

	  itemListTable = document.getElementById("addReport");
	  numRows = itemListTable.rows.length;
	   var currDate=currentDate();
	   var servicePrefix="SR";
	   var servicePrefix_Date="";
	  if((flag=="")&&(flag != true)){
	 	 intReportNum=totalReports+1;
	  }else{
	 	 intReportNum=intReportNum;
	  }
	  for(var i=1;i<=numRows-1;i++){
	   servicePrefix_Date=servicePrefix+"-"+currDate;
	 	 addOption(document.managereport.report+i, servicePrefix_Date+"-"+intReportNum,"NEW"+intNum,i);
	 	 flag=true;
	  }
	  addRows(servicePrefix+"-"+currDate+"-"+intReportNum);
	  intNum++;
	  intReportNum++;
  }
  function addOption(selectbox,text,value,i ) {
  	var optn = document.createElement("OPTION");
	var  selectbox=document.getElementById("report"+i);
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
}
  function addRows(text){

    var itemListTable = document.getElementById("serviceItemList");
	numRows = itemListTable.rows.length;
	var id = numRows ;
	var row = itemListTable.insertRow(id);

	var cell;
	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>'+text+'</label>'


	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'


	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'


	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'


	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'

	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'

	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>---</label>'

  }



 function checkManageReport(reportName,reportId,serviceid){
 	var id = "report"+reportId;

	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["REPORT_NAME"] == reportName){
			if(record["SIGNED_OFF"] == 'Y' ){
				alert('You cannot add to a Signed-Off Report');
				assignOrginalReport(id,serviceid);
				return false;
			}
		}
	}
	return true;
 }

 function assignOrginalReport(reportId,serviceid){
 	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["SERVICE_ID"] ==serviceid ){
		   if(record["REPORT_NAME"] != '')
				document.getElementById(reportId).value = record["REPORT_NAME"];
			else
				document.getElementById(reportId).value = 'N';
		}
 	}
 }


function getOrginalReportForTest(testId){
	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["SERVICE_ID"] ==testId ){
			return record["REPORT_NAME"];
		}
 	}
}


function isReportHaveData(selectedReport){
    var isReportInPrescription = false;
	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["REPORT_NAME"] == selectedReport ){
		    isReportInPrescription = true;
			return record["REPORT_DATA"];
		}
	}

	if(!isReportInPrescription){
		//report is exist , but not there in existing current prescription.
		for(var i=0;i<reportList.length;i++){
			var record = reportList[i];
			if(record["REPORT_NAME"]== selectedReport){
				return 'Y';
			}
		}
	}

	return 'N';
}

function getReportId(selectedReportName){
	for(var i=0; i<reportList.length;i++){
		var record = reportList[i];
		if(record["REPORT_NAME"] == selectedReportName ){
			return record["REPORT_ID"];
		}
	}
	return 0;
}

function validate(){
	var canSave = true;
	var reportIdsForRegeneration = "";
	var form = document.managereport;
	var selectedReportNames =form.reportId;
	var serviceid = form.serviceid;

  try{
		if(selectedReportNames !=null && selectedReportNames.length!=undefined ){
			for(var i=0; i<selectedReportNames.length;i++){
				var selectedReport = selectedReportNames[i].value;
				var selectedTestId = null;
				if(serviceid.length != undefined)
					selectedTestId = serviceid[i].value;
				else
				   	selectedTestId  = serviceid.value;

				if(selectedReport !='N'){
					var orginalReport = getOrginalReportForTest(selectedTestId);
					if(orginalReport != selectedReport){
						//alert("orginalReport" + orginalReport);
						//alert("selectedReport" + selectedReport);
						 var isSelectedReporthaveData = isReportHaveData(selectedReport);
						 var isOrginalReporthaveData = isReportHaveData(orginalReport);
						 if(isSelectedReporthaveData == 'Y' || isOrginalReporthaveData == 'Y'){

 							 selectedReport = selectedReport.replace('NEW','Report ');
							 var alertMsg =  selectedReport +'/' +orginalReport+ " : some services have been added/removed, the report will be regenerated.\n"+
											 " You may lose any formatting changes that you have done in the existing report "

							 if(!confirm(alertMsg)){
								canSave = false;
							 }else{
							 	reportIdsForRegeneration += ',' +getReportId(selectedReport);
							 	reportIdsForRegeneration += ',' +getReportId(orginalReport);
							 }
						  }
					}
				}
			}
		}
  }catch(e){
	alert(e);
	return false;
  }
 if(canSave){
	//alert("final selection="+reportIdsForRegeneration);
	form.reportIdsForRegeneration.value = reportIdsForRegeneration;
	//return false;
 }
	return canSave;
}

function currentDate() {
	var currentDate = new Date();
	var dateStr = formatDate(currentDate, 'ddmmyyyy', '');
	var returnDateStr=dateStr.substring(0,dateStr.length-4);
	var returnYearStr=dateStr.substring(dateStr.length,6);
	return returnDateStr+returnYearStr;
}
