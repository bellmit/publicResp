function getGenericDetails(){

    //document.getElementById("genName").value=document.getElementById("genName").value.toUpperCase();
    document.forms[0].action="gendetails.do?method=getSearchGenericDetails";
 	document.forms[0].submit();

 }

 function clearValues(){
 	document.getElementById("genName").value = '';
 	document.genericform.statusActive.checked = false;
 	document.genericform.statusActive.disabled = true;
 	document.genericform.statusInActive.checked = false;
	document.genericform.statusInActive.disabled = true;
	document.genericform.statusAll.checked = true;

 }

 function clearAll(){
 	document.getElementById("genName").value = '';
 	document.genericform.statusActive.checked = false;
 	document.genericform.statusInActive.checked = false;
	document.genericform.statusAll.checked = false;

 }
 function enableStatus(){
 	var disabled = document.genericform.statusAll.checked;
	document.genericform.statusActive.disabled = disabled;
	document.genericform.statusInActive.disabled = disabled;
 }