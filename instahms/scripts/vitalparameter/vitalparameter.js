  /* This function  will populate vital info
 if the user is trying for edit.
 */

 /* function populateVitalDetails(){


	  var containerOption=document.forms[0].param_container;
	  var labelName=document.forms[0].param_label;
	  var paramUOM=document.forms[0].param_uom;
	  var paramOrder=document.forms[0].param_order;
	  var statusOptions=document.forms[0].param_status;
	  var method=document.forms[0]._method.value;

    if(method == "show"){
  if(vitalParameterJSON.length>0){

       setSelectedIndex(containerOption, vitalParameterJSON[0].param_container);
       labelName.value=vitalParameterJSON[0].param_label;
       paramUOM.value=vitalParameterJSON[0].param_uom;
       paramOrder.value=vitalParameterJSON[0].param_order;
       setSelectedIndex(statusOptions, vitalParameterJSON[0].param_status);
       document.getElementById("save").value="update";

       }
     }
  }*/

  function setHiddenvar() {
  	document.getElementById("h_visit_type").value = document.getElementById("visit_type").value;
  }

  function changeVisit(obj)
  {
  	var paramContainer = obj.value;
  	if(paramContainer == 'I' || paramContainer == 'O') {
  		document.getElementById("visit_type").value = 'I';
  		document.getElementById("h_visit_type").value = 'I';
  		document.getElementById("visit_type").disabled = true;
  		document.getElementById('divforIntake').style.display = 'block';
  		document.getElementById('divforVitals').style.display = 'none';
  		document.getElementById('expressionRow').style.display = 'none';

  	} else {
	  	document.getElementById("visit_type").value = '';
	  	document.getElementById("h_visit_type").value = '';
  		document.getElementById("visit_type").disabled = false;
  		document.getElementById('divforIntake').style.display = 'none';
  		document.getElementById('divforVitals').style.display = 'block';
  		document.getElementById('expressionRow').style.display = 'table-row';
  	}
  }

  function validate(){
  	if(document.forms[0].param_label.value==""){
  		alert("Name is Required");
  		document.forms[0].param_label.focus();
  		return false;
  	}
  	if(document.forms[0].param_order.value==""){
  		alert("Order is Required");
  		document.forms[0].param_order.focus();
  		return false;
  	}
  	var newVitalName = document.forms[0].param_label.value;
  	var newOrder = document.forms[0].param_order.value;
  	var paramId = document.forms[0].param_id.value;

  	for(i=0;i<avlVitalList.length;i++){
  		item = avlVitalList[i];
  		if(paramId!=item["param_id"] && item["param_status"]=='A'){
	  		if(newVitalName == item["param_label"]){
				alert("Vital with same Name already exists");
	  			document.forms[0].param_label.focus();
	  			return false;
	  		}
	  		if(newOrder == item["param_order"]){
	  			alert("Vital with same order already exists");
	  			document.forms[0].param_order.focus();
	  			return false;
	  		}
  		}
  	}
  	document.forms[0].submit();

  }