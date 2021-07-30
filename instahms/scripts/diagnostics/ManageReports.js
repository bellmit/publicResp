//Global variable declaration
  var itemListTable="";
  var numRows="";
  var intNum=2;
  var intReportNum="";
  var flag="";

  function addReports(){

	  itemListTable = document.getElementById("addReport");
	  numRows = itemListTable.rows.length;
	  testType=document.diagcenterform.category.value
	  var currDate=currentDate();
	  var testTypePrefix="";
	  var testTypePrefix_Date="";

	  if(testType=="DEP_LAB")
	    testTypePrefix="LR";
	    else
	    testTypePrefix="RR";

	  if((flag=="")&&(flag != true)){
	 	 intReportNum=totalReports+1;
	  }else{
	 	 intReportNum=intReportNum;
	  }
	  for(var i=1;i<=numRows-1;i++){
	     testTypePrefix_Date=testTypePrefix+"-"+currDate;
	 	 addOption(document.diagcenterform.report+i, testTypePrefix_Date+"-"+intReportNum,"NEW"+intNum,i);
	 	 testTypePrefix_Date="";
	 	 flag=true;
	  }


	  addRows(testTypePrefix+"-"+currDate+"-"+intReportNum);
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

    var itemListTable = document.getElementById("testsItemList");
	numRows = itemListTable.rows.length;
	var id = numRows ;
	var row = itemListTable.insertRow(id);

	var cell;
	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<label>'+text+'</label>'

	cell = row.insertCell(-1);
	cell.setAttribute("align","center");
	cell.innerHTML = '<html:hidden name="manageReportId" value="0"/>' +
					 '<html:hidden name="manageReportName" value="'+text+'"/> ';


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



 function checkManageReport(reportName,reportId,testId){
 	var id = "report"+reportId;

	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["REPORT_NAME"] == reportName){
			if(record["SIGNED_OFF"] == 'Y' ){
				showMessage('js.laboratory.radiology.managereports.cannotadd.signedoffreport');
				assignOrginalReport(id,testId);
				return false;
			}
		}
	}
	return true;
 }

 function assignOrginalReport(reportId,testId){
 	for(var i=0; i<prescriptionList.length;i++){
		var record = prescriptionList[i];
		if(record["TEST_ID"] ==testId ){
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
		if(record["TEST_ID"] ==testId ){
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


function currentDate() {
	var currentDate = new Date();
	var dateStr = formatDate(currentDate, 'ddmmyyyy', '');
	var returnDateStr=dateStr.substring(0,dateStr.length-4);
	var returnYearStr=dateStr.substring(dateStr.length,6);
	return returnDateStr+returnYearStr;
}


var reportNameDialog;

function initReportNameDialog() {

	var dialogDiv = document.getElementById("reportNameDialog");
	dialogDiv.style.display = 'block';
	reportNameDialog = new YAHOO.widget.Dialog("reportNameDialog",{
			width:"250px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeReportNameDialog,
	                                                scope:reportNameDialog,
	                                                correctScope:true } );
	reportNameDialog.cfg.queueProperty("keylisteners", escKeyListener);
	reportNameDialog.render();

}

function closeReportNameDialog(){
	reportNameDialog.hide();
}

function showReportNameDialog( obj ){

	var row = getThisRow(obj);

	document.getElementById("reportNameEditId").value = getThisRow( obj ).rowIndex;
	document.getElementById("eReportName").value = row.cells[0].getElementsByTagName("label")[0].innerHTML;
	reportNameDialog.cfg.setProperty("context",[obj, "tl", "bl"], false);
	reportNameDialog.show();
}

function setReportName(){
	var rowIndex = document.getElementById("reportNameEditId").value;
	var table = document.getElementById("testsItemList");
	var row = table.rows[rowIndex];
	row.cells[0].getElementsByTagName("label")[0].innerHTML = document.getElementById("eReportName").value;
	getElementByName( row,"manageReportName" ).value = document.getElementById("eReportName").value;
	reportNameDialog.hide();
}
