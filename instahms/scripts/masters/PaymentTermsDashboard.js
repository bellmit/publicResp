function getTemplateDetails(){

    //document.getElementById("genName").value=document.getElementById("genName").value.toUpperCase();
    document.forms[0].action="tempdetails.do?method=getSearchTemplateDetails";
 	document.forms[0].submit();

 }

 function clearValues(){
 	document.getElementById("tempName").value = '';
 	document.templateform.statusActive.checked = false;
 	document.templateform.statusActive.disabled = true;
 	document.templateform.statusInActive.checked = false;
	document.templateform.statusInActive.disabled = true;
	document.templateform.statusAll.checked = true;

 }

 function clearAll(){
 	document.getElementById("tempName").value = '';
 	document.templateform.statusActive.checked = false;
 	document.templateform.statusInActive.checked = false;
	document.templateform.statusAll.checked = false;

 }
 function enableStatus(){
 	var disabled = document.templateform.statusAll.checked;
	document.templateform.statusActive.disabled = disabled;
	document.templateform.statusInActive.disabled = disabled;
 }