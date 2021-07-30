
var hiddenAreaId = '${bean.map.area_id}';

function checkduplicate(){
	var newAreaName = trimAll(document.forms[0].area_name.value);
	var selectedCity = document.forms[0].city_id.value;
	if(document.forms[0]._method.value != 'update'){
		for(var i=0;i<chkAreaName.length;i++){
			item = chkAreaName[i];
			if(selectedCity == item.CITY_ID){
				if (newAreaName == item.AREA_NAME){
					alert(document.forms[0].area_name.value+" already exists pls enter other name...");
			    	document.forms[0].area_name.value='';
			    	document.forms[0].area_name.focus();
			    	return false;
				}
			}

		}
	}
 	if(document.forms[0]._method.value == 'update'){
  		if (backupName != newAreaName){
			for(var i=0;i<chkAreaName.length;i++){
				item = chkAreaName[i];
				if(selectedCity == item.CITY_ID){
					if (newAreaName == item.AREA_NAME){
						alert(document.forms[0].area_name.value+" already exists pls enter other name");
				    	document.forms[0].area_name.focus();
				    	return false;
					}
  				}
  			}
 		}
 	}
}//end of function

