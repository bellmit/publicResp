function viewpackages(){
	var packageid=document.getElementById('packNames').options[document.getElementById('packNames').selectedIndex].value;
	var ajaxobj = newXMLHttpRequest();
	var url = "PackageMasterAction.do?method=viewpackagecomponents&packageid="+packageid;
	getResponseHandler(ajaxobj,getresponsepackagecontent,url.toString());

}

function getresponsepackagecontent(domDocObj,responseText){

	var testlistlength = domDocObj.getElementsByTagName("diagnosticslist")[0].getElementsByTagName("testname").length;

	var packagedetails="";
	for(var i=0;i<testlistlength;i++){
		packagedetails = packagedetails+"\n"+"Test Name:" +domDocObj.getElementsByTagName("diagnosticslist")[0].getElementsByTagName("testname")[i].firstChild.nodeValue;

	}

	var wardlength = domDocObj.getElementsByTagName("beddetails")[0].getElementsByTagName("wardname").length;
	for(var i=0;i<wardlength;i++){
		packagedetails = packagedetails + "\n"+ "ward:" +
				 domDocObj.getElementsByTagName("beddetails")[0].getElementsByTagName("wardname")[i].firstChild.nodeValue;
	}

	var bedlength = domDocObj.getElementsByTagName("beddetails")[0].getElementsByTagName("bedname").length;
	for(var i=0;i<bedlength;i++){
		packagedetails = packagedetails + "\n"+ "bed:" +
				 domDocObj.getElementsByTagName("beddetails")[0].getElementsByTagName("bedname")[i].firstChild.nodeValue;
	}

	var servicenamelength = domDocObj.getElementsByTagName("servicedetails")[0].getElementsByTagName("servicename").length;
	for(var i=0;i<servicenamelength;i++){
		packagedetails = packagedetails + "\n"+ "servicename:" +
				 domDocObj.getElementsByTagName("servicedetails")[0].getElementsByTagName("servicename")[i].firstChild.nodeValue;
	}

	var visitingdoctorlength = domDocObj.getElementsByTagName("visitingdoctordetails")[0].getElementsByTagName("visitingdoctor").length;
	for(var i=0;i<visitingdoctorlength;i++){
		packagedetails = packagedetails + "\n"+ "visiting doctor:" +
				 domDocObj.getElementsByTagName("visitingdoctordetails")[0].getElementsByTagName("visitingdoctor")[i].firstChild.nodeValue;

	}

	var consultingdoctorlength = domDocObj.getElementsByTagName("consultingdoctordetails")[0].getElementsByTagName("consultingdoctor").length;
	for(var i=0 ; i<consultingdoctorlength;i++){
		packagedetails = packagedetails + "\n"+ "consulting doctor:" +
				 domDocObj.getElementsByTagName("consultingdoctordetails")[0].getElementsByTagName("consultingdoctor")[i].firstChild.nodeValue;
	}


	var operationlist = domDocObj.getElementsByTagName("operationdetails")[0].getElementsByTagName("operationname").length;
	for(var i=0;i<operationlist;i++){
		packagedetails = packagedetails + "\n"+ "Operation Name:" +
					domDocObj.getElementsByTagName("operationdetails")[0].getElementsByTagName("operationname")[i].firstChild.nodeValue;
	}
	document.forms[0].packagedetails.value = packagedetails;

}//getresponsepackagecontent


function setSelectedIndex(opt, set_value) {
  var index=0;
  for(var i=0; i<opt.options.length; i++) {
    var opt_value = opt.options[i].value;
    if (opt_value == set_value) {
      opt.selectedIndex = i;
      return;
    }
  }
}//end of setSelectedIndex




function validateallgrids(){

	var comp = document.forms[0].component.length;
	if(comp == undefined){
				  if(document.forms[0].component.value=="Diagnostics"){
						var  l = diagGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Diagnostics grid, at least one row should be present in Diag Grid");
							return false;
						}
				  }


				   if(document.forms[0].component.value == "OT"){
						var l = otGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Operation Theatre(OT) grid,  at least one row should be present in OT Grid ")
							return false;
						}
					}


					if(document.forms[0].component.value == "BedCharges"){
						var l = bedTypeGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Bed Charges grid,  at least one row should be present in Bed Charges Grid ")
							return false;
						}
					}

					if(document.forms[0].component.value == "Services"){
						var l = servicesGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Services grid,  at least one row should be present in Services Grid ")
							return false;
						}
					}

					 if(document.forms[0].component.value == "visDoctor Charges"){
							var l = visDocGrid.getRowCount();
							if(l <= 0){
								alert("No Rows in the Other Charges grid,  at least one row should be present in Visiting Doctors Grid ")
								return false;
							}
					}

					if(document.forms[0].component.value == "conDoctor Charges"){
						var l = consDocGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Other Charges grid,  at least one row should be present in Consulting Doctors Grid ")
							return false;
					}
				  }
	}else{
					//validate all grid records begin
				for(var i=0;i<comp;i++){
				  if(document.forms[0].component[i].value=="Diagnostics"){
						var  l = diagGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Diagnostics grid, at least one row should be present in diag Grid");
							return false;
						}
				  }


				   if(document.forms[0].component[i].value == "OT"){
						var l = otGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Operation Theatre(OT) grid,  at least one row should be present in OT Grid ")
							return false;
						}
					}


					if(document.forms[0].component[i].value == "BedCharges"){
						var l = bedTypeGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Bed Charges grid,  at least one row should be present in Bed Charges Grid ")
							return false;
						}
					}

					if(document.forms[0].component[i].value == "Services"){
						var l = servicesGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Services grid,  at least one row should be present in Services Grid ")
							return false;
						}
					}

					 if(document.forms[0].component[i].value == "visDoctor Charges"){
							var l = visDocGrid.getRowCount();
							if(l <= 0){
								alert("No Rows in the Other Charges grid,  at least one row should be present in Visiting Doctors Grid ")
								return false;
							}
					}

					if(document.forms[0].component[i].value == "conDoctor Charges"){
						var l = consDocGrid.getRowCount();
						if(l <= 0){
							alert("No Rows in the Other Charges grid,  at least one row should be present in Consulting Doctors Grid ")
							return false;
					}
				  }
				 }//for loop
			return true;
	}
}//validateallgrids


