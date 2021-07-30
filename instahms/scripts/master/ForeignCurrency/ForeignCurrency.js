var toolBar = {
	Edit : {
		title : 'View/Edit',
		imageSrc : 'icons/Edit.png',
		href  : '/master/ForeignCurrency.do?_method=show',
		onclick : null,
		description : 'View and/or Edit Currency Details'
	}
};

function doExport() {
	return true;
}

function doUpload(formType) {

   if(formType == "uploadcurrencyform"){
	var form = document.uploadcurrencyform;
		if (form.xlsCurrencyFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
	}
	form.submit();
}