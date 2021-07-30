
Insta.AddMRDCasefileDialog = function(contextId, addHandler, contenxtPath, screen) {
	this.contextId = contextId;
	this.addHandler = addHandler;
	this.screen = screen;

	this.addDialog = null;


	this.start = function(){
		clearFields();
		this.showMRDCasefileDialog();
	}

	this.initAddMRDCasefileDialog = function(){
		document.getElementById("mrdCasefileDialog").style.display = 'block';
		this.addDialog = new YAHOO.widget.Dialog("mrdCasefileDialog", {width:"700px",
				context: [this.contextId, "tr", "br"],
				visible:false,
				model:true,
				constraintoviewport:true
				});
		this.addDialog.render();
		subscribeEscKeyEvent(this.addDialog);
	}

	function clearFields(){
		document.getElementById("mrno").value = '';
		document.getElementById("mr_no").textContent = '';
		document.getElementById("casefile_no").textContent='';
		document.getElementById("patName").textContent = '';
		document.getElementById("deptName").textContent='';
		document.getElementById("add_dept_name").value='';
		document.getElementById("add_dept_id").value='';
		document.getElementById("mlc_status").value='';
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
			document.getElementById("mrno").value  = "";
			document.getElementById("mrno").focus();
		}else{
			for (var i=0;i<details.length; i++){
				if (screen == 'indent'){
					if ((details[i].file_status =='A'||details[i].file_status == 'U') && details[i].indented != 'Y'){
						this.addToTable(details);
						if (details[i].dept_name==null){
							document.getElementById("add_dept_name").focus();
						}
					}else if (details[i].indented == 'Y'){
						if (details[i].dept_name != null){
							alert("Case file "+details[i].mr_no+" is already indented to "+details[i].requesting_dept);
							document.getElementById("mrno").value  = "";
							document.getElementById("mrno").focus();
						}
					}
				}else if(screen == 'issue'){
					if (details[i].file_status =='A'){
						 this.addToTable(details);
						 if (details[i].requesting_dept==null){
							document.getElementById("add_dept_name").focus();
						}
					}else if (details[i].file_status =='U'){
						var issueDate =  formatDateTime(new Date(details[i].issued_on));
						alert("Case file is issued to "+details[i].casefile_with+ " on "+issueDate);
						document.getElementById("mrno").value  = "";
						document.getElementById("mrno").focus();
					}else{
					}
				}else if (screen == 'return'){
					if (details[i].file_status != 'A'){
						 this.addToTable(details);
					}else{
						alert("Case file is not issued to any department");
						document.getElementById("mrno").value  = "";
						document.getElementById("mrno").focus();

					}
				}else if (screen == 'closeIndent'){
					if (details[i].indented == 'Y'){
						this.addToTable(details);
					}else{
						alert("Case file is not indented");
						document.getElementById("mrno").value  = "";
						document.getElementById("mrno").focus();
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
			document.getElementById("patName").textContent = details[i].patient_full_name;
			document.getElementById("casefile_no").textContent = details[i].casefile_no;
			document.getElementById("mlc_status").value = details[i].mlc_status;
			document.getElementById("requestedBy").value = trim(details[i].requesting_dept);
			document.getElementById("requestedById").value = details[i].dept_id;
			document.getElementById("regdate").value = formatDate(new Date(details[i].regdate),'ddmmyyyy','-');
			document.getElementById("regtime").value = formatTime(new Date(details[i].regtime), false);
			document.getElementById("deathstatus").value = details[i].death_status;
			if (screen == "indent"){
				document.getElementById("add_dept_name").value = trim(details[i].dept_name);
				document.getElementById("add_dept_id").value = trim(details[i].dept_id);
				document.getElementById("add_dept_type").value = details[i].dept_type;
				document.getElementById("deptName").textContent = details[i].dept_name;
			}else if(screen == "issue"){
				if (details[i].indented == "Y"){
					document.getElementById("add_dept_name").value = trim(details[i].requesting_dept);
					document.getElementById("add_dept_id").value = trim(details[i].requesting_dept_id);
					document.getElementById("add_dept_type").value = details[i].dept_type;
					document.getElementById("deptName").textContent = details[i].requesting_dept;

					if(details[i].indent_date != null && details[i].ind_date != null && details[i].ind_time != null){
						document.getElementById("indentOnhid").value = details[i].indent_date;
						document.getElementById("indentOnDatehid").value = details[i].ind_date;
						document.getElementById("indentOnTimehid").value = details[i].ind_time; }
					else {
						document.getElementById("indentOnhid").textContent = '';
						document.getElementById("indentOnDatehid").value = '';
						document.getElementById("indentOnTimehid").value = '';
					}

				}else{
					document.getElementById("add_dept_name").value = details[i].dept_name;
					document.getElementById("add_dept_id").value= details[i].dept_id;
					document.getElementById("add_dept_type").value = details[i].dept_type;
					document.getElementById("deptName").textContent = details[i].dept_name;

					if(details[i].indent_date != null && details[i].ind_date != null && details[i].ind_time != null){
						document.getElementById("indentOnhid").textContent = details[i].indent_date;
						document.getElementById("indentOnDatehid").value = details[i].ind_date;
						document.getElementById("indentOnTimehid").value = details[i].ind_time; }
					else {
						document.getElementById("indentOnhid").textContent = '';
						document.getElementById("indentOnDatehid").value = '';
						document.getElementById("indentOnTimehid").value = '';
					}
				}
			}else if(screen == "return"){
				document.getElementById("add_dept_name").value = trim(details[i].casefile_with);
				document.getElementById("add_dept_id").value = trim(details[i].casefile_with_id);
				document.getElementById("add_dept_type").value = details[i].dept_type;
				document.getElementById("deptName").textContent = details[i].casefile_with;
			}else if(screen == "closeIndent"){
				document.getElementById("add_dept_name").value = trim(details[i].requesting_dept);
				document.getElementById("add_dept_id").value = trim(details[i].requesting_dept_id);
				document.getElementById("add_dept_type").value = details[i].dept_type;
				document.getElementById("deptName").textContent = details[i].requesting_dept;
			}
		}
	}


	this.validateCasefileAdd = function(){
		var mrno =  document.getElementById("mr_no").textContent;
		var mrNo = document.getElementById("mrno").value;
		var deathStat = document.getElementById("deathstatus").value;

		if (mrNo == "" && mrno ==""){
			alert("Select MRNo and get details to add grid");
			return false;
		}else{
			if (mrno == ""){
				alert("Get details for selected mrno");
				return false;
			}else{
				var mrd = {
					mrno             : mrno,
		 		    patientName      : document.getElementById("patName").textContent,
				    casefileNo       :  document.getElementById("casefile_no").textContent,
				    indentedDeptName : document.getElementById("requestedBy").value,
					addDeptName      : document.getElementById("add_dept_name").value,
					addDeptId        : document.getElementById("add_dept_id").value,
					addDeptType      : document.getElementById("add_dept_type").value,
					regDate          : document.getElementById("regdate").value,
				 	regTime          : document.getElementById("regtime").value,
				 	mlcStatus        : document.getElementById("mlc_status").value,
					indentOn         : document.getElementById("indentOnhid") ? document.getElementById("indentOnhid").value:'',
					indentDate       : document.getElementById("indentOnDatehid") ? document.getElementById("indentOnDatehid").value:'',
					indnentTime      : document.getElementById("indentOnTimehid") ? document.getElementById("indentOnTimehid").value:''
				}
			}
		}

		var dept = document.getElementById("add_dept_name").value;
		if (dept == ""){
			alert("Enter Department name");
			document.getElementById("add_dept_name").focus();
			return false;

		}

		if (deathStat == 'D'){
			alert("Patient is dead");
		}
		addHandler(mrd);
		return true;
	}

	this.initAddDeptAutoComplete= function() {
		var dataSource = new YAHOO.util.XHRDataSource(contextPath +"/medicalrecorddepartment/MRDCaseFileIssue.do");
		dataSource.scriptQueryAppend="_method=getDepartmentList";
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		dataSource.responseSchema = {
resultsList :"result",
			 fields : [  {key : "issued_to_name"},
						 {key : "issue_to_id"},
						 {key : "type"}
			 	]
		};
		oAutoComp = new YAHOO.widget.AutoComplete('add_dept_name', 'addDeptDropdown', dataSource);
		oAutoComp.minQueryLength = 2;
		//oAutoComp.forceSelection = true;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.resultTypeList = false;
		oAutoComp._bItemSelected = true;

		oAutoComp.itemSelectEvent.subscribe(getDeptId);
		oAutoComp.selectionEnforceEvent.subscribe(clearDeptId);

		function getDeptId(oSelf, elItem, oData){
			document.getElementById("add_dept_id").value = elItem[2].issue_to_id;
			document.getElementById("add_dept_type").value = elItem[2].type;
			//document.getElementById("deptName").textContent=elItem[2].issued_to_name;
		}

		function clearDeptId(oSelf , sClearedValue){
			document.getElementById("add_dept_id").value = '';
		}
	}


	function subscribeEscKeyEvent(dialog) {
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:dialog.cancel, scope:dialog, correctScope:true } );
		dialog.cfg.setProperty("keylisteners", escKeyListener);
		clearFields();
	}

	this.closeDialog = function(){
		this.addDialog.cancel();
	}

	this.align= function(){
		clearFields();
		this.addDialog.align("tr", "br");
		document.getElementById("mrno").focus();
	}

	this.initAddMRDCasefileDialog();
	 initMrNoAutoComplete(contextPath);

	 var enterKeyListener= new YAHOO.util.KeyListener(document.getElementById("mrno"), {keys:13 },
			 { fn: this.getPatientDetails, scope: this, correctScope: true } );
	 this.addDialog.cfg.setProperty("keylisteners", enterKeyListener);


	 YAHOO.util.Event.addListener(patDetails, "click", this.getPatientDetails, this , true);
	 YAHOO.util.Event.addListener(btnAddCasefile, "click", this.validateCasefileAdd, this , true);
	 YAHOO.util.Event.addListener(btnClose, "click", this.closeDialog, this , true);
}


