	function validate() {

		var unitName = document.getElementById('unit_name').value.trim();
		if (empty(unitName)) {
			alert('Please enter unit name.');
			document.getElementById('unit_name').focus();
			return false;
		}

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newUnitName = trimAll(document.strengthUnitMaster.unit_name.value);

  		if (backupName != newUnitName){
			for(var i=0;i<chkStrengthUnit.length;i++){
				item = chkStrengthUnit[i];
				if(newUnitName == item.unit_name){
					alert(document.strengthUnitMaster.unit_name.value+" already exists pls enter other name.");
			    	document.strengthUnitMaster.unit_name.focus();
			    	return false;
  				}
  			}
 		}
		return true;
	}

	var rAutoComp;
	function autoStrengthUnitMaster() {
		var datasource = new YAHOO.util.LocalDataSource({result: strengthUnits});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "unit_name"},{key : "unit_id"} ]
		};
		var rAutoComp = new YAHOO.widget.AutoComplete('unit_name','unitnamecontainer', datasource);
		rAutoComp.minQueryLength = 0;
	 	rAutoComp.maxResultsDisplayed = 20;
	 	rAutoComp.forceSelection = false ;
	 	rAutoComp.animVert = false;
	 	rAutoComp.resultTypeList = false;
	 	rAutoComp.typeAhead = false;
	 	rAutoComp.allowBroserAutocomplete = false;
	 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		rAutoComp.autoHighlight = true;
		rAutoComp.useShadow = false;
	 	if (rAutoComp._elTextbox.value != '') {
				rAutoComp._bItemSelected = true;
				rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
		}
	}