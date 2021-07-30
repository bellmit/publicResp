
function time_pm(){
	  if(document.getElementById("night_pm").value > 24){
	       alert("Enter time in 24Hr Format");
	       document.mainform.night_pm.value= "";
	       document.getElementById("night_pm").focus();
	       return false;
	}
}
function time_am(){
	  if(document.getElementById("night_am").value > 12){
			alert("Enter time in 12Hr Format");
			document.mainform.night_am.value= "";
			document.getElementById("night_am").focus();
	     return false;
	}
}


function validatePreferences(){

if(document.mainform.genregcharge.value==''){
		alert("Enter General Registration charge");
		document.mainform.genregcharge.focus();
		return false;
	}

	if(document.mainform.ipregcharge.value==''){
		alert("Enter IP Registration charge");
		document.mainform.ipregcharge.focus();
		return false;
	}

	if(document.mainform.opregcharge.value==''){
		alert("Enter OP Registration charge");
		document.mainform.opregcharge.focus();
		return false;
	}


	if(document.mainform.medreccharge.value==''){
		alert("Enter Medical Record charge");
		document.mainform.medreccharge.focus();
		return false;
	}

	if(document.mainform.mlccharge.value==''){
		alert("Enter MLC charge");
		document.mainform.mlccharge.focus();
		return false;
	}

	if(document.mainform.night_pm.value==''){
		alert("Enter Night Consultation charge");
		document.mainform.night_pm.focus();
		return false;
	}

	if(document.mainform.night_am.value==''){
		alert("Enter Night Consultation charge");
		document.mainform.night_am.focus();
		return false;
	}

	return true;
}


function savePreferences(){
	document.mainform.action = 'RegistrationChargesAction.do';
	document.mainform.method.value='saveRegPreferences';
	document.mainform.submit();

}


function updatePreferences(){

	 if(validatePreferences()==false){
	 	 return false;
	  }

	document.mainform.action = 'RegistrationChargesAction.do';
	document.mainform.method.value='updateRegPreferences';
	document.mainform.submit();


}


function getcitynames(){
		var state_name = document.mainform.state.options[document.mainform.state.selectedIndex].value;
		var obj = document.mainform.city;
		var k=0;
		root = document.getElementById("STATECITY");
		offlist = root.getElementsByTagName("statecity");
		len=offlist.length;
		obj.length=1;
		for (var ii=0; ii<len; ii++) {
		    off = offlist.item(ii);
		     if (state_name == off.attributes.getNamedItem('class3').nodeValue) {
		            k++;
		            obj.length=k+1;
		            obj.options[k].value = off.attributes.getNamedItem('class1').nodeValue;
		            obj.options[k].text = off.attributes.getNamedItem('class2').nodeValue;
		        }
		    } //end of the for loop
}














