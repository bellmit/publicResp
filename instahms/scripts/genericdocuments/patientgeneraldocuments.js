/*
* generic documents tool bar items
*/
var genericDocToolbar = {
	Print : 	{	title: "Print",
					imageSrc: "icons/Print.png",
					href: 'pages/GenericDocuments/GenericDocumentsPrint.do?_method=print&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
			  	}
};

/*
* reg documents tool bar items
*/
var regDocToolbar = {
	Print : 	{	title: "Print",
					imageSrc: "icons/Print.png",
					href: 'pages/RegistrationDocuments/RegistrationDocumentsPrint.do?_method=print&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
			  	}
};

/*
* op documents tool bar items
*/
var opDocToolbar = {
	Edit :		{	title: "Edit",
					imageSrc: "icons/Edit.png",
					href: 'Outpatient/OutPatientDocuments.do?_method=show',
					onclick: 'checkForAuthorized'
			  	},
	Print : 	{	title: "Print",
					imageSrc: "icons/Print.png",
					href: 'Outpatient/OutpatientDocumentsPrint.do?_method=print&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
			  	},
	PrintWithTreament : {
					title: "Print (with Treatment Info)",
					imageSrc: "icons/Print.png",
					href: 'Outpatient/OutpatientDocumentsPrint.do?_method=print&appendTreatment=true&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
	}
};

/*
* insurance documents tool bar items
*/
var insDocToolbar = {
	Print : 	{	title: "Print",
					imageSrc: "icons/Print.png",
					href: 'Insurance/InsuranceGenericDocumentsPrint.do?_method=print&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
			  	}
};

var operationDocToolbar = {
	Print : 	{	title: "Print",
					imageSrc: "icons/Print.png",
					href: 'otservices/OperationDocumentsPrint.do?_method=print&allFields=N',
					onclick: 'checkForAuthorized',
					target : '_blank'
			  	}
}


function init(docType) {
	if (docType == 'Generic') {
		genericDocToolbar.Edit = {	title: "Edit",
			imageSrc: "icons/Edit.png",
			href: 'pages/GenericDocuments/GenericDocumentsAction.do?_method=show',
			onclick: 'checkForAuthorized',
	  	}
		createToolbar(genericDocToolbar);
	} else if (docType == 'regDoc') {
		regDocToolbar.Edit =
					{	title: "Edit",
						imageSrc: "icons/Edit.png",
						href: 'pages/RegistrationDocuments.do?_method=show',
						onclick: 'checkForAuthorized'
				  	}
		createToolbar(regDocToolbar);
	} else if (docType == 'InsuranceDocs') {
		insDocToolbar.Edit = {
			title: "Edit",
			imageSrc: "icons/Edit.png",
			href: 'Insurance/InsuranceGenericDocuments.do?_method=show',
			onclick: 'checkForAuthorized'
		}
		createToolbar(insDocToolbar);
	} else if (docType == 'opdoc') {
		createToolbar(opDocToolbar);
	} else if (docType == 'Test') {
		var testDocToolbar = {
			Print : 	{	title: "View",
							imageSrc: "icons/Print.png",
							href: (category == 'DEP_LAB' ? 'Laboratory' : 'Radiology') + '/TestDocumentsPrint.do?_method=print&allFields=N',
							onclick: 'checkForAuthorizedUser',
							target : '_blank'
					  	},
			Edit : 		{	title: "Edit",
							imageSrc: "icons/Edit.png",
							href: (category == 'DEP_LAB' ? 'Laboratory' : 'Radiology') + '/AddrEditTestDocuments.do?_method=show',
							onclick: 'checkForAuthorized',
					  	}
		};
		createToolbar(testDocToolbar);
	} else if (docType == 'Operation') {
		operationDocToolbar.Edit = {
			title: "Edit",
			imageSrc: "icons/Edit.png",
			href: 'otservices/AddrEditOperationDocuments.do?_method=show',
			onclick: 'checkForAuthorized'
		}
		createToolbar(operationDocToolbar);
	}
}


function deleteSelected(e, form) {
	var deleteEl = document.getElementsByName("deleteDocument");
	for (var i=0; i< deleteEl.length; i++) {
		if (deleteEl[i].checked) {
			form._method.value = "deleteDocuments";
			form.submit();
			return true;
		}
	}
	alert("Select at least one document for delete");
	YAHOO.util.Event.stopEvent(e);
	return false;
}

function finalizeSelected(e, form) {
	var deleteEl = document.getElementsByName("deleteDocument");
	for (var i=0; i< deleteEl.length; i++) {
		if (deleteEl[i].checked) {
			form._method.value = "finalizeDocuments";
			form.submit();
			return true;
		}
	}
	alert("Select at least one document to finalize");
	YAHOO.util.Event.stopEvent(e);
	return false;
}

function checkForAuthorized(anchor, params, id, toolbar) {
	var authorizedUser = '';
	var access_rights = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'username')
			authorizedUser = paramvalue;
		if (paramname == 'access_rights')
			access_rights = paramvalue
	}
	if (access_rights == 'A') {
		if (roleId == 1 || roleId == 2 || loggedInUser == authorizedUser) {
		} else {
			alert('Only authorized user can Edit/View the document.');
			return false;
		}
	}
	return true;
}

function checkForAuthorizedUser(anchor, params, id, toolbar) {
	var authorizedUser = '';
	var access_rights = '';
	var format = '';
	var docLocation = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'username')
			authorizedUser = paramvalue;
		if (paramname == 'access_rights')
			access_rights = paramvalue;
		if (paramname == 'format')
			format = paramvalue;
		if (paramname == 'docLocation' &&  paramvalue != '') {
			docLocation = paramvalue;
		}
	}
	if (access_rights == 'A') {
		if (roleId == 1 || roleId == 2 || loggedInUser == authorizedUser) {
		} else {
			alert('Only authorized user can Edit/View the document.');
			return false;
		}
	}
	if(format == 'doc_link') {
		window.open(docLocation);
		return false;
	}
	return true;
}

function openNewUploadDocumentPopUp(mrNo, visitId, screenId, uplodLimitInMb) {
	if (screenId == 'ip_registration') {
		window.visit_type = 'IP';
		window.uplodLimitInMb = uplodLimitInMb;
	}
	else if (screenId == 'out_pat_reg') {
		window.visit_type = 'OSP';
	}
	window.uplodLimitInMb = uplodLimitInMb;
	window.setUploadDocMode('Registration');
	window.routeParams = {
			patientId : mrNo,
			visitId: visitId,
	};
	window.fetchVisitsList();
	window.changeSelectedFilter(visitId);
	window.getUploadedDocumetsList();
	window.fetchDocumentTypes();
	window.openAddDocument();
}

 function openConsentUploadDocumentPopUp(mrNo,uploadLimit){
   window.uplodLimitInMb = uploadLimit;
   window.setUploadConsentDocMode('');
   window.routeParams = {
 		patientId :mrNo,
 		};
   window.openAddConsentDocument('SYS_HIE','HIE CONSENT');
 }
