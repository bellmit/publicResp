
function fillRatePlanDetails(category,categoryId,idValue){
	if(derivedRatePlanDetails.length>0) {
		document.getElementById("ratePlanDiv").style.display = 'block' ;
		for (var i =0; i<derivedRatePlanDetails.length; i++) {
			var ratePlanTbl = document.getElementById("ratePlanTbl");
			var len = ratePlanTbl.rows.length;
			var templateRow = ratePlanTbl.rows[len-1];
		   	var row = '';
		   		row = templateRow.cloneNode(true);
		   		row.style.display = '';
		   		row.id = len-2;
		   		len = row.id;
		   	YAHOO.util.Dom.insertBefore(row, templateRow);
			var cell = row.insertCell(-1);
			var checkbox = makeCheckbox('applicableChk', 'applicableChk'+len, '', derivedRatePlanDetails[i].applicable)
			checkbox.setAttribute("onclick", "setApplicable('"+len+"')");
			var inp1 = document.createElement("INPUT");
		    inp1.setAttribute("type", "hidden");
		    inp1.setAttribute("name", "applicable");
		    inp1.setAttribute("id", "applicable"+len);
		    inp1.setAttribute("value", derivedRatePlanDetails[i].applicable);
			cell.appendChild(checkbox);
			cell.appendChild(inp1);

			var cell1 = row.insertCell(-1);
		    cell1.setAttribute("style", "width: 70px");

			if(derivedRatePlanDetails[i].is_override=='Y')
		    	cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/blue_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
		    else
		    	cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/empty_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
		    var inp2 = document.createElement("INPUT");
		    inp2.setAttribute("type", "hidden");
		    inp2.setAttribute("name", "ratePlanId");
		    inp2.setAttribute("id", "ratePlanId"+len);
		    inp2.setAttribute("value", derivedRatePlanDetails[i].org_id);
		    cell1.appendChild(inp2);

			var cell2 = row.insertCell(-1);
		    cell2.setAttribute("style", "width: 70px");
		    cell2.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].discormarkup;

		    var cell3 = row.insertCell(-1);
		    cell3.setAttribute("style", "width: 40px");
		    cell3.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].rate_variation_percent;

			var orgId = derivedRatePlanDetails[i].org_id;
			//var testId = derivedRatePlanDetails[i].test_id;
			var cell4 = row.insertCell(-1);
			var baseRateSheet = derivedRatePlanDetails[i].base_rate_sheet_id;
			var orgName = derivedRatePlanDetails[i].org_name;
		    if (derivedRatePlanDetails[i].applicable) {
					var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
						'&'+categoryId+'='+idValue+'&chargeCategory='+ category +'&fromItemMaster=true&baseRateSheet='+baseRateSheet+
						'&org_name='+orgName;
					cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
				}
		}
	}
}

function setApplicable(id) {
	var applicableChk = document.getElementById("applicableChk"+id);
	if(applicableChk.checked)
		document.getElementById("applicable"+id).value = true;
	else
		document.getElementById("applicable"+id).value = false;
}

function fillRatePlanDetailsMigrated(category,categoryId,idValue){
	if(derivedRatePlanDetails.length>0) {
		document.getElementById("ratePlanDiv").style.display = 'block' ;
		for (var i =0; i<derivedRatePlanDetails.length; i++) {
			var ratePlanTbl = document.getElementById("ratePlanTbl");
			var len = ratePlanTbl.rows.length;
			var templateRow = ratePlanTbl.rows[len-1];
		   	var row = '';
		   		row = templateRow.cloneNode(true);
		   		row.style.display = '';
		   		row.id = len-2;
		   		len = row.id;
		   	YAHOO.util.Dom.insertBefore(row, templateRow);
			var cell = row.insertCell(-1);
			var checkbox = makeCheckbox('applicableChk', 'applicableChk'+len, '', derivedRatePlanDetails[i].map.applicable)
			checkbox.setAttribute("onclick", "setApplicable('"+len+"')");
			var inp1 = document.createElement("INPUT");
		    inp1.setAttribute("type", "hidden");
		    inp1.setAttribute("name", "applicable");
		    inp1.setAttribute("id", "applicable"+len);
		    inp1.setAttribute("value", derivedRatePlanDetails[i].map.applicable);
			cell.appendChild(checkbox);
			cell.appendChild(inp1);

			var cell1 = row.insertCell(-1);
		    cell1.setAttribute("style", "width: 70px");

			if(derivedRatePlanDetails[i].map.is_override=='Y')
		    	cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/blue_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].map.org_name;
		    else
		    	cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/empty_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].map.org_name;
		    var inp2 = document.createElement("INPUT");
		    inp2.setAttribute("type", "hidden");
		    inp2.setAttribute("name", "ratePlanId");
		    inp2.setAttribute("id", "ratePlanId"+len);
		    inp2.setAttribute("value", derivedRatePlanDetails[i].map.org_id);
		    cell1.appendChild(inp2);

			var cell2 = row.insertCell(-1);
		    cell2.setAttribute("style", "width: 70px");
		    cell2.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].map.discormarkup;

		    var cell3 = row.insertCell(-1);
		    cell3.setAttribute("style", "width: 40px");
		    cell3.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].map.rate_variation_percent;

			var orgId = derivedRatePlanDetails[i].map.org_id;
			//var testId = derivedRatePlanDetails[i].test_id;
			var cell4 = row.insertCell(-1);
			var baseRateSheet = derivedRatePlanDetails[i].map.base_rate_sheet_id;
			var orgName = derivedRatePlanDetails[i].map.org_name;
		    if (derivedRatePlanDetails[i].map.applicable) {
					var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
						'&'+categoryId+'='+idValue+'&chargeCategory='+ category +'&fromItemMaster=true&baseRateSheet='+baseRateSheet+
						'&org_name='+orgName;
					cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
				}
		}
	}
}
