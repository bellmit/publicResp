
Insta.AddMRDCasefileDialog = function(contextId, addHandler, contenxtPath, screen) {
	this.contextId = contextId;
	this.addHandler = addHandler;
	this.screen = screen;

	this.addDialog = null;


	this.start = function(){
		this.clearFields();
		this.showMRDCasefileDialog();
	}

	this.initAddMRDCasefileDialog = function(){
		document.getElementById("mrdCasefileDialog").style.display = 'block';
		this.addDialog = new YAHOO.widget.Dialog("mrdCasefileDialog", {width:"650px",
				context: [this.contextId, "tr", "br"],
				visible:false,
				model:true,
				constraintoviewport:true
				});
		this.addDialog.render();
		subscribeEscKeyEvent(this.addDialog);
	}

	this.clearFields = function(){
		document.getElementById("mrno").value = '';
	}

	this.showMRDCasefileDialog = function(){
		this.addDialog.show();
		document.getElementById("mrno").focus();
	}

	this.getPatientDetails = function(mrno){
		var mrno = document.getElementById("mrno").value;
		var url = contenxtPath +'/medicalrecorddepartment/RaiseMRDCasefileIndent.do?_method=getMRDCasefiles&mrno='+mrno;
		var reqObj = newXMLHttpRequest();
		reqObj.open("POST", url.toString(),false);
		reqObj.send(null);
		var details = null;
		if (reqObj.readyState == 4) {
			if ( (reqObj.status == 200) && (reqObj.responseText != null ) ) {
				details =  eval(reqObj.responseText);
			}
		}
	
		if (details.length == 0){
			if (document.getElementById("mrno").value == ""){
				alert("Enter MRNo to get details");
			}else{
				document.getElementById("mr_no").textContent = '';
				document.getElementById("casefile_no").textContent='';
				document.getElementById("patName").textContent = '';
				document.getElementById("deptName").textContent='';
				alert("Casefile is not available for this mrno");
			}
			document.getElementById("mrno").focus();
		}else{
			for (var i=0;i<details.length; i++){
				if (screen == 'indent'){
					if (details[i].file_status =='A' && details[i].indented != 'Y'){
						this.addToTable(details);
					}else if (details[i].file_status =='U'){
						alert("Case file is issued to "+details[i].issued_to);
					}else if (details[i].indented == 'Y'){
						alert("Case file "+details[i].mr_no+" is already indented by "+details[i].requested_by); 
					}
				}else if(screen == 'issue'){
					if (details[i].file_status =='A'){
						 this.addToTable(details);
					}else if (details[i].file_status =='U'){
						alert("Case file is issued to "+details[i].issued_to);
					}else{
					}
				}else if (screen == 'return'){
					if (details[i].file_status != 'A'){
						 this.addToTable(details);
					}else{
						alert("Case file is not issued to any user");
					}
				}else if (screen == 'closeIndent'){
					if (details[i].indented == 'Y'){
						this.addToTable(details);
					}else{
						alert("Case file is not indented");
					}
				}
			} // for 
		}//main else 
		return null;
	}
	
	this.addToTable = function(details){
		for (var i=0;i<details.length; i++){
			document.getElementById("resultTable").style.display='block';
			document.getElementById("mr_no").textContent = details[i].mr_no;
			document.getElementById("patName").textContent = details[i].salutation + '' +
				details[i].patient_name +' '+details[i].last_name ;
			document.getElementById("casefile_no").textContent = details[i].casefile_no;
			document.getElementById("deptName").textContent = details[i].dept_name;
			document.getElementById("requestedby").value = details[i].requesting_dept;
			document.getElementById("regdate").value = formatDate(new Date(details[i].regdate),'ddmmyyyy','-');
			document.getElementById("regtime").value = formatTime(new Date(details[i].regtime), false);
			document.getElementById("deathstatus").value = details[i].death_status;
		}
	}


	this.validateCasefileAdd = function(){
		var mrno =  document.getElementById("mr_no").textContent;
		var mrNo = document.getElementById("mrno").value;
		var deathStat = document.getElementById("deathstatus").value;

		if (mrNo == ""){
			alert("Select MRNo and get details to add grid");
			return false;
		}else{
			if (mrno == ""){
				alert("Get details for selected mrno");
				return false;
			}else{		
				var mrd = {
					mrno : mrno,
		 		    patientName : document.getElementById("patName").textContent,
				    casefileNo :  document.getElementById("casefile_no").textContent,
				    deptName : document.getElementById("deptName").textContent,
					requestedBy : document.getElementById("requestedby").value,
					regDate : document.getElementById("regdate").value,
				 	regTime : document.getElementById("regtime").value
				}
			}
		}
		if (deathStat == 'D'){
			alert("Patient is dead");
		}
		return addHandler(mrd);
	}

	this.onAddCheckDuplicatefiles = function(){
		
	}

	function subscribeEscKeyEvent(dialog) {
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:dialog.cancel, scope:dialog, correctScope:true } );
		dialog.cfg.setProperty("keylisteners", escKeyListener);
	}

	this.initAddMRDCasefileDialog();
	 initMrNoAutoComplete(contextPath);
	 var enterKeyListener= new YAHOO.util.KeyListener(document, {keys:13 },
			 { fn: this.getPatientDetails, scope: this, correctScope: true } );
	 this.addDialog.cfg.setProperty("keylisteners", enterKeyListener);

	 YAHOO.util.Event.addListener(patDetails, "click", this.getPatientDetails, this , true);
	 YAHOO.util.Event.addListener(btnAddCasefile, "click", this.validateCasefileAdd, this , true);

}
