function getSupplierDetails(){

    //document.getElementById("suppName").value=document.getElementById("suppName").value.toUpperCase();
    document.forms[0].action="suppdetails.do?method=getSearchSupplierDetails";
 	document.forms[0].submit();

 }

 function clearValues(){
 	document.getElementById("suppName").value = '';
 	document.supplierform.statusActive.checked = false;
 	document.supplierform.statusActive.disabled = true;
 	document.supplierform.statusInActive.checked = false;
	document.supplierform.statusInActive.disabled = true;
	document.supplierform.statusAll.checked = true;

 }

 function clearAll(){
 	document.getElementById("suppName").value = '';
 	document.supplierform.statusActive.checked = false;
 	document.supplierform.statusInActive.checked = false;
	document.supplierform.statusAll.checked = false;

 }
 function enableStatus(){
 	var disabled = document.supplierform.statusAll.checked;
	document.supplierform.statusActive.disabled = disabled;
	document.supplierform.statusInActive.disabled = disabled;
 }