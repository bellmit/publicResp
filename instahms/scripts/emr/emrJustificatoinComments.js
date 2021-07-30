var emrCommentsDialog;
var submitPatientSearch = false;

function initDialog() {
	var emrCommentsDiv = document.getElementById("emrCommentsDiv");
	if ( emrCommentsDiv )
		emrCommentsDiv.style.display = 'block';
	emrCommentsDialog = new YAHOO.widget.Dialog("emrCommentsDiv",
			{	width:"500px",
				height: '200px',
				close: false,
				context : ["emrCommentsDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: true,
				constraintoviewport:true
			});
	emrCommentsDialog.render();
}

function addJustificationComments() {
	var mrNOEl = document.getElementById('ps_mrNo');
	var visitIDEl = document.getElementById('ps_visit');
	submitPatientSearch = true;
	var mrNO = '';
	var visitID = '';
	if (!empty(mrNOEl)) {
		mrNO = mrNOEl.value;
		document.getElementById('emr_mr_no').value = mrNO;
	} else if (!empty(visitIDEl)) {
		visitID = visitIDEl.value;
		document.getElementById('emr_visit_id').value = visitID;
	}
	var mandate_comments = '';
	var url = cpath+"/emr/justificationComments.do?_method=getEmrAccess&mr_no="+mrNO+"&visit_id="+visitID;
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				mandate_comments = ajaxobj.responseText;
			}
		}
	}
	
	if (!empty(mandate_comments) && mandate_comments == 'true') {
		var form = document.getElementsByName("patientSearch")[0];
		var inputElm = document.createElement("input");
		inputElm.type = "text";
		inputElm.name = "add_comments";
		inputElm.value = true;
		inputElm.style.display = 'none';
		form.appendChild(inputElm);
		emrCommentsDialog.show();
	} else {
		document.getElementById('patientSearch').submit();
	}
}

function addCommentsOnPageLoad() {
	submitPatientSearch = false;
	if ((!empty(mr_no) || !empty(visit_id)) && empty(add_comments) && mandate_emr_comments == 'true') {
		emrCommentsDialog.show();
		document.emrTreeForm.style.display = 'none';
		document.getElementById("patientDetailsTab").style.display = 'none';
	}
}

function saveComments() {
	var form = document.getElementById("emr_comments_form");
	var emrCommentsEle = document.getElementById('emrCommentsFieldId');
	emrCommentsEle.value = trim(emrCommentsEle.value);
	if (empty(emrCommentsEle.value) || emrCommentsEle.value.length < 30) {
		alert(getString("js.medicalrecords.patientemr.view.comments.30chars"));
		emrCommentsEle.focus();
		return false;
	}
    var url = cpath + "/emr/justificationComments.do?_method=saveComments";
    asyncPostForm(form, url, false,
    		null, null);
    clearEmrCommentsDialog();
    document.emrTreeForm.style.display = 'block';
    document.getElementById("patientDetailsTab").style.display = 'block';
    if (submitPatientSearch)
    	document.getElementById('patientSearch').submit();
}

function clearEmrCommentsDialog() {
	document.getElementById('emrCommentsFieldId').value = '';
	emrCommentsDialog.cancel();
}
