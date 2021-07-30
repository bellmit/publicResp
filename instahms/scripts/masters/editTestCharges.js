
function fillRatePlanDetails(){
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
		    cell1.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].org_name;
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
			var testId = derivedRatePlanDetails[i].test_id;
			var cell4 = row.insertCell(-1);
			var baseRateSheet = derivedRatePlanDetails[i].base_rate_sheet_id;
		    if (derivedRatePlanDetails[i].applicable) {
					var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
						'&test_id='+testId+'&chargeCategory=diagnostics&fromItemMaster=true&baseRateSheet='+baseRateSheet;
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