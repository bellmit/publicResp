function initTestDetailsDialog() {

	var dialogDiv = document.getElementById("testDetailsDialog");
	dialogDiv.style.display = 'block';
	testDetailsDialog = new YAHOO.widget.Dialog("testDetailsDialog",{
			width:"600px",
			text: "Test Details",
			context :["loadDialog", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeTestDialog,
	                                                scope:testDetailsDialog,
	                                                correctScope:true } );
	testDetailsDialog.cfg.queueProperty("keylisteners", escKeyListener);
	testDetailsDialog.render();
}

function closeTestDialog() {
	testDetailsDialog.cancel();
}


function clearDialog(){

	document.getElementById("tdSampleIdLabel").innerHTML = '';
	document.getElementById("tdOrigSampleNoLabel").innerHTML = '';
	document.getElementById("tdSampleSource").innerHTML = '';
	document.getElementById("tdSampleDate").innerHTML = '';
	document.getElementById("tdSampleTime").innerHTML = '';
	document.getElementById("tdDoctor").value = '';
}

function showTestDetails(obj,prescribedId,commonIndex,sampleNo){

	var row = getThisRow(obj,'TR');
	var id = getRowIndex(row);
	clearDialog();
	document.getElementById("editedTest").value = commonIndex;
	document.getElementById("testDetailsDialog").display = 'block';
	var doctor = getElementByName(row,"doctor");
	var testRemarks = getElementByName(row,"testRemarks");
	var condDocMandatory = getElementByName(row,"conducting_doc_mandatory");
	var ddeptId = document.getElementById('ddeptid'+commonIndex).value;
	var technician = document.getElementById('technician');
	technician.options.length = 1;
	var users = deptUsersMap[ddeptId];
	if (users != undefined) {
		for (var i=1; i<users.length+1; i++) {
			technician.options.length = technician.options.length + 1;
			technician.options[i].value = users[i-1].emp_username;
			technician.options[i].text = users[i-1].emp_username;
		}
	}
	technician.value = document.getElementById('h_technician'+commonIndex).value;

	var testDialogTable = document.getElementById("testDetailsTable");
	for(var i =0;i<testandresultsJSON.length;i++){
		if(testandresultsJSON[i].prescribedId == prescribedId){
			var test = testandresultsJSON[i].test;

			if(sampleFlow == 'N' || test.sampleNeed == 'n')
				document.getElementById("sampleDetailsDiv").style.display = 'none';

			if ( sampleFlow == 'N' )//sample date is editable if sample flow is disabled
				document.getElementById("sampleDateDiv").style.display = 'block';

			testDialogTable.rows[0].cells[1].innerHTML = test.testName;
			var sampleExists = (test.sampleNeed == 'y' && sampleFlow == 'Y' && test.testStatus != 'O' &&  test.prescriptionType != 'i');
			if(!empty(trim(sampleNo))){
					document.getElementById("sampleDetailsDiv").style.display = 'block';
				 	document.getElementById("tdSampleIdLabel").innerHTML = test.sampleNo;
				 	if (test.incoming_source_type == 'H')
				 		document.getElementById("tdOrigSampleNoLabel").innerHTML = test.origSampelNo;
				 	document.getElementById("tdSampleIdLabel").innerHTML = test.sampleNo;
					document.getElementById("tdSampleSource").innerHTML = test.sampleSource;
					document.getElementById("tdSpecimenCondition").value = getElementByName(row,"specimen_condition").value;

					if(sampleFlow == 'Y'){
						document.getElementById("tdSampleDate").innerHTML = test.sampleDate;
						document.getElementById("tdSampleTime").innerHTML = test.sampleTime;
					}else{
						document.getElementById("sampleDate").value = empty(test.sampleDate) ? thisDate :test.sampleDate;
					}
			}
			if(empty(test.testDate)){
				document.getElementById("tdTestDate").value = thisDate;
				document.getElementById("tdTestTime").value = thisTime;
			}else{
				document.getElementById("tdTestDate").value = test.testDate;
				document.getElementById("tdTestTime").value = test.testTime;
			}
			if(category == 'DEP_RAD'){
					if((condDocMandatory.value == 'C' || condDocMandatory.value == 'O') && doctor.value == '')
					document.getElementById("tdDoctor").disabled = ( testandresultsJSON[i].condctionStatus != 'N' && testandresultsJSON[i].condctionStatus != 'P' && testandresultsJSON[i].condctionStatus != 'MA' && testandresultsJSON[i].condctionStatus != 'CC' && testandresultsJSON[i].condctionStatus != 'CR' && testandresultsJSON[i].condctionStatus != 'TS');
					else
					document.getElementById("tdDoctor").disabled = ( testandresultsJSON[i].condctionStatus != 'N' && testandresultsJSON[i].condctionStatus != 'MA' );
			}else{
					document.getElementById("tdDoctor").disabled = ( testandresultsJSON[i].condctionStatus != 'N' && testandresultsJSON[i].condctionStatus != 'P' );
             }
			document.getElementById("tdLabNoLabel").innerHTML= category == 'DEP_LAB' ? 'Lab No :' :'Radiology ID:';
			document.getElementById("tdLabno").value = getElementByName(row,"labNo").value;
			document.getElementById("tdLabno").readOnly = !empty(test.labno);
			document.getElementById("tdConductionInstr").innerHTML = ( test.conductionInstructions ).substring(0,20);
			document.getElementById("tdConductionInstr").title = test.conductionInstructions;
			document.getElementById("tdSampleType").innerHTML =  test.sampleType;
			document.getElementById("tdOrderRemarks").innerHTML = ( test.orderRemarks ).substring(0,20);
			document.getElementById("tdOrderRemarks").title = test.orderRemarks;

		}
	}
	document.getElementById("tdDoctor").value = doctor.value;
	document.getElementById("tdTestRemarks").value = testRemarks.value;

	testDetailsDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	testDetailsDialog.show();
}


function onEdit(){
	 var index = document.getElementById("editedTest").value;
	 var doctor = document.getElementsByName("doctor");
	 var testDate = document.getElementsByName("dateOfInvestigation");
	 var testTime = document.getElementsByName("timeOfInvestigation");
	 var sampleCondition = document.getElementsByName("specimen_condition");
	 var sampleDate = document.getElementsByName("dateOfSample");
	 var remarks = document.getElementsByName("testRemarks");

	if(validateTestDetailsEdit()){
		 document.getElementById("doctor"+index).value = document.getElementById("tdDoctor").value;
		 document.getElementById("dateOfInvestigation"+index).value = document.getElementById("tdTestDate").value;
		 document.getElementById("timeOfInvestigation"+index).value = document.getElementById("tdTestTime").value;
		 document.getElementById("specimen_condition"+index).value = document.getElementById("tdSpecimenCondition").value;
		 document.getElementById("testRemarks"+index).value = document.getElementById("tdTestRemarks").value;
		 document.getElementById("labNo"+index).value = document.getElementById("tdLabno").value;
		 document.getElementById('h_technician'+index).value = document.getElementById('technician').value

		 if(sampleFlow == 'N')
		 	document.getElementById("dateOfSample"+index).value = document.getElementById("sampleDate").value;
 	}

 	closeTestDialog();
}

