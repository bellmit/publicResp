function categoryChange() {
	var categoryId = document.mainform.patient_category_id.value;
    var ratePlan = document.getElementById("organization");
    var sponsor = document.getElementById("tpa");
    if (categoryId == '') {
        RatePlanList();
        TpaList();
    } else {
   		var orgIdList = null;
        var tpaIdList = null;
		var defaultRatePlan = "..Rate Plan..";
    	var defaultTpa = "..TPA/Sponsor..";

        for (var i = 0; i < categoryJSON.length; i++) {
            var item = categoryJSON[i];
            if (categoryId == item.category_id) {
                if ((item.allowed_rate_plans != null) && (item.allowed_rate_plans != '') )
					orgIdList = item.allowed_rate_plans.split(',');
                if ((item.allowed_sponsors != null) && (item.allowed_sponsors != ''))
					tpaIdList = item.allowed_sponsors.split(',');
                defaultRatePlan = item.rate_plan_id;
                defaultTpa = item.sponser_id;
                break;
            }
        }

        if (!sponsor) {
			if (previousTpaId != '') {
				if (tpaIdList == null) {
				} else {
					var flag = false;
					 for (var k = 0; k < tpaIdList.length; k++) {
					 	if (tpaIdList[k] == previousTpaId) {
					 		flag = true; // previous tpa id exists for this category too.
					 		break;
					 	}
					 }
					 if (!flag) {
					 	alert("Patient Sponser: "+ previousTpaName + " " + " does not exists in the allowed Sponser's list for the selected Category.");
					 	document.mainform.patient_category_id.value = patient_current_category;
						return false;
					 }
				}
			}
		} else {
	        if (tpaIdList != null ) {
	            for (var i = 0; i < sponsor.options.length; i++) {
	                sponsor.remove(i);
	            }
	            var len = 1;
	            sponsor.options.length = len;
	            var optn = new Option("..TPA/Sponsor..", "..TPA/Sponsor..");
	            sponsor.options[0] = optn;
	            for (var k = 0; k < tpaIdList.length; k++) {
	                for (var l = 0; l < tpanames.length; l++) {
	                    if (tpaIdList[k] == tpanames[l].TPA_ID) {
	                        var optn = new Option(tpanames[l].TPA_NAME, tpanames[l].TPA_ID);
	                        len = len + 1;
	                        sponsor.options.length = len;
	                        sponsor.options[len - 1] = optn;
	                    }
	                }
	            }

	        } else {
				TpaList();
			}
		}

        if (orgIdList != null) {
            for (var i = 0; i < ratePlan.options.length; i++) {
                ratePlan.remove(i);
            }

			var len = 1;
            var optn = new Option("..Rate Plan..", "..Rate Plan..");
			ratePlan.options[len-1] = optn;
			var generalAllowed = false;

            for (var k = 0; k < orgIdList.length; k++) {
					var org = findInList(orgNamesJSON, "ORG_ID", orgIdList[k]);
					optn = new Option(org.ORG_NAME, org.ORG_ID);
					len++;
					ratePlan.options.length = len;
					ratePlan.options[len-1] = optn;
			}

        } else {
			// no restrictions: all rate plans are allowed
			RatePlanList();
		}


		if (defaultRatePlan != null) {
			if (defaultRatePlan == "ORG0001") {
				// GENERAL == no rate plan
				ratePlan.selectedIndex = 0;
				document.mainform.organization.disabled = false;
			} else {
				setSelectedIndex(ratePlan, defaultRatePlan);
				document.mainform.organization.disabled = false;
			}
		}
		if ( (defaultTpa != null) && (sponsor) ) {
			setSelectedIndex(sponsor, defaultTpa);
			if (defaultTpa != "..TPA/Sponsor..") {
				document.mainform.tpa.disabled = false;
			}
		}

    }
}

function RatePlanList() {
	var ratePlan = document.getElementById("organization");
	var len = 1;
	for(var i=0;i<orgNamesJSON.length;i++){
		ratePlan.options.length = len+1;
		var optn = new Option(orgNamesJSON[i].ORG_NAME,orgNamesJSON[i].ORG_ID);
		ratePlan.options[len] =  optn;
		len++;
	}
}

function TpaList(){
	var tpaLen = 1;
	if (!document.getElementById("tpa"))  return;
	for(var i=0;i<tpanames.length;i++){
		document.getElementById("tpa").options.length = tpaLen+1;
		document.getElementById("tpa").options[tpaLen].text=tpanames[i].TPA_NAME;
		document.getElementById("tpa").options[tpaLen].value= tpanames[i].TPA_ID;
		tpaLen++;
	}

}

function loadRatePlan() {
	var ratePlanList = null;
	var tpaIdList = null;
	var t = 0;
	var sponsor = document.getElementById("tpa");
	for(var i=0;i<categoryJSON.length;i++){
		if(categoryJSON[i].category_id == patient_current_category) {
			if (categoryJSON[i].allowed_rate_plans!=null)
				ratePlanList = categoryJSON[i].allowed_rate_plans.split(',');
			if ((categoryJSON[i].allowed_sponsors != null) && (categoryJSON[i].allowed_sponsors != ''))
					tpaIdList = categoryJSON[i].allowed_sponsors.split(',');
		}
	}

	if (ratePlanList!=null) {
		for (var i=0;i<ratePlanList.length;i++) {
			for (var j=0;j<orgNamesJSON.length;j++) {
				item = orgNamesJSON[j];
				if (ratePlanList[i] == item.ORG_ID) {
					optn = new Option(item.ORG_NAME,item.ORG_ID);
					document.forms[0].organization.options[t] = optn;
					t++;
				}
			}
		}
	} else {
		for (var i=0;i<orgNamesJSON.length;i++) {
			item = orgNamesJSON[i];
			optn = new Option(item.ORG_NAME,item.ORG_ID);
			document.forms[0].organization.options[t] = optn;
			t++;
		}
	}
	setSelectedIndex(document.forms[0].organization, curent_rateplan);

	/*
	* if the patient dont have the active credit bill, sponsor will not be allowed to edit.
	*/
	if (!sponsor) return;
	if (tpaIdList != null) {
            for (var i = 0; i < sponsor.options.length; i++) {
                sponsor.remove(i);
            }
            var len = 1;
            sponsor.options.length = len;
            var optn = new Option("..TPA/Sponsor..", "..TPA/Sponsor..");
            sponsor.options[0] = optn;
            for (var k = 0; k < tpaIdList.length; k++) {
                for (var l = 0; l < tpanames.length; l++) {
                    if (tpaIdList[k] == tpanames[l].TPA_ID) {
                        var optn = new Option(tpanames[l].TPA_NAME, tpanames[l].TPA_ID);
                        len = len + 1;
                        sponsor.options.length = len;
                        sponsor.options[len - 1] = optn;
                    }
                }
            }

	} else {
		TpaList();
	}
	if (previousTpaId!="")
		setSelectedIndex(document.forms[0].tpa, previousTpaId);
}


function save() {
	if (document.mainform.organization.value == "..Rate Plan..") {
		alert("Select the RatePlan");
		document.mainform.organization.focus();
		return false;
	}
	document.mainform.submit();
}