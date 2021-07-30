function doSave(){

	var reason=document.customform.reason.value.trim();
	if(reason==''){
		alert('Please enter the reason for Customization');
		document.customform.reason.focus();
		return false;
	}else if(reason.length >= 2000){
		alert('Reason for Costomization should not exceed 2000 chars');
		document.customform.reason.focus();
		return false;
	}
	document.customform.customized.value=true;
	document.customform.submit();
}

function doReset(){

	var reason=document.customform.reason.value;
	if(reason !='' && reason.length > 2000){
		alert("Reason for Customization should not exceed 2000 chars");
		document.customform.reason.focus();
    	return false;
	}
	document.customform.customized.value= false;
	document.customform.resetToDefault.value=true;
	document.customform.submit();
}