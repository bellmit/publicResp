	var askDateAndTime = false;
	function init(){
		if(document.bedstatusform.bed_status.value != 'A'){
			askDateAndTime = true;
		}
	}
	function showDateAndTime(){
		if(document.bedstatusform.bed_status.value != 'A'){
				askDateAndTime =true;
				document.getElementById("avbleTimingsDiv").style.visibility = "visible";
			}else{
				askDateAndTime = false;
				document.getElementById("avbleTimingsDiv").style.visibility = "hidden";
			}
		}
	function validate(){
		if(document.bedstatusform.bed_status.value == ''){
			alert("please select bed status");
			document.bedstatusform.bed_status.focus();
			return false;
		}
		if(askDateAndTime){
			if(document.bedstatusform.avbl_date.value == ''){
			   	alert("Bed Available date can not be empty");
			   	document.bedstatusform.avbl_date.focus();
			   	return false;
		   } else {
			 if (!doValidateDateField(document.bedstatusform.avbl_date)){
		                return false;
		        }
		        var msg = validateDateStr(document.bedstatusform.avbl_date.value);
		        if (msg != null && msg!=""){
		               alert(msg);
		               return false;
		         }
			}
		    if(document.bedstatusform.avbl_time.value == ''){
			   	alert("Bed Available time can not be empty");
			   	document.bedstatusform.avbl_time.focus();
			   	return false;
		    }else{
		   		if (!validateTime(document.bedstatusform.avbl_time)){
				return false;
			 }
		   }
	   }
	return true;
}