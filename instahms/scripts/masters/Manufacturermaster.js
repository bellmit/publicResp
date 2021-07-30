function validations(){
	if(!scriptvalidation()){
		return false;
	}

	document.manfacturer_Form._method.value = "insertOrUpdatemanufacturerDetails";
	document.manfacturer_Form.submit();
	return true;
 }



function scriptvalidation(){
      hname=document.manfacturer_Form.manf_name.value;
	  document.getElementById("manf_name").value=trimAll(document.getElementById("manf_name").value);
	  msg="Manufacturer Name ";
	 if(trimAll(hname)==""){
	    alert("Manufacture Name Should Not be Empty");
	    hname="";
	 	document.manfacturer_Form.manf_name.focus();
		return false;
	 }
	 if (trimAll(document.manfacturer_Form.manf_mnemonic.value)=="") {
	 	alert("Manufacture Code Should Not be Empty");
	 	document.manfacturer_Form.manf_mnemonic.value = "";
	 	document.manfacturer_Form.manf_mnemonic.focus();
	 	return false;
	 }
	 return true;
}

function funcClose(){
	document.manfacturer_Form.action = "ManufacturerDetails.do?_method=getManufacturerDetails&sortOrder=manf_name&sortReverse=false";
	document.manfacturer_Form.submit();
	return true;
}
function chk(e){
	var key=0;
  	if(window.event || !e.which) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
    if(document.manfacturer_Form.manf_address.value.length<200 || key==8) {
		key=key;
		return true;
	} else {
		key=0;
		return false;
	}
}

function chklen(){
	if(document.manfacturer_Form.manf_address.value.length > 200){
		alert("Address should be 200 characters only");
	  	var s = document.manfacturer_Form.manf_address.value;
	  	s = s.substring(0,200);
	  	document.manfacturer_Form.manf_address.value = s;
	}
}


