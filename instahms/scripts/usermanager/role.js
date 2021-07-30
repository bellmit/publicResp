/*
 * Javascript functions for usermanager/role.jsp
 */


function resetValues() {
    document.roleForm.name.value="";
    document.roleForm.remarks.value="";

    var action = document.getElementById("actionRights");
    inputs = document.getElementsByTagName('input');
    for (var i=0; i<inputs.length; i++) {
        if (inputs[i].value == "N") {
            inputs[i].checked = true;
        }
    }
}

/*
 * This method is to get the details of a particular role
 */
function getRoleScreens() {
    var roleId = document.roleForm.roleId.value;

    if (roleId!="") {
        var ajaxReqObject = newXMLHttpRequest();
        var url="../../pages/usermanager/RoleAction.do?method=getRoleDetailsJSON&roleId="+roleId;
        getResponseHandlerText(ajaxReqObject, handleAjaxResponse, url);
    } else {
    	tree1 = new YAHOO.widget.TreeView("treeDiv", screensTree);
		tree1.subscribe('clickEvent',tree1.onEventToggleHighlight);
		tree1.setNodesProperty('propagateHighlightUp',true);
		tree1.setNodesProperty('propagateHighlightDown',true);
		tree1.render();
    }
}
 var portalRight=null //globlal decalaration for our convinience

function handleAjaxResponse(responseText) {
    /* The json string should look like this:
    {
      role: { roleId: 1, name: "my name", status: "A", remarks: "Some remarks" }
      rights: { screenRightsMap: {reg_registration:"N", reg_cancel:"N"},
                actionRightsMap: {bill_reopen:"N", ...},
                urlRightsMap: {reg_registration:"A", reg_cancel:"N"}
              }
    }
    To use the string, just eval the text.
    */

    debug(responseText);
    eval("var roleDetails = " + responseText);

    // set the remarks for this role
    if (roleDetails.role.modUser!=null)
    	document.getElementById('mod_user').innerHTML = roleDetails.role.modUser;
    if (roleDetails.role.modDate!=null)
    	document.getElementById('mod_date').innerHTML = roleDetails.role.modDate;
    document.roleForm.remarks.value = roleDetails.role.remarks;
    portalRight=roleDetails.role.portalId;
	var roleId = document.roleForm.roleId.value;
	document.roleForm.name.value=document.roleForm.rolename.value;
	document.roleForm.operation.value="edit";

	tree1 = new YAHOO.widget.TreeView("treeDiv", screensTree);
	tree1.subscribe('clickEvent',tree1.onEventToggleHighlight);
	tree1.setNodesProperty('propagateHighlightUp',true);
	tree1.setNodesProperty('propagateHighlightDown',true);

	for (var screen in roleDetails.rights.screenRightsMap) {
    	var screenNodes = tree1.getNodesByProperty("data", screen);
		// get all the nodes with the data matching the screen id
		if (null != screenNodes) {
	    	for (var i=0; i<screenNodes.length; i++) {
				// there may be more than 1, so loop through
	    		if (roleDetails.rights.screenRightsMap[screen] == 'A') {
	    			screenNodes[i].highlight(); // highlight if the screen has rights
	    		} else {
	    			screenNodes[i].unhighlight(); // remove the highlight otherwise.
	    		}
	    	}
    	}
    }
	tree1.render();

    // 3. Set all action rights to None (or All if role ID is 1 or 2)
    fieldset = document.getElementById("actionRights");
    inputs = fieldset.getElementsByTagName('input');

	toCheck = ( (roleId == 1) || (roleId ==2) ) ? "A" : "N";

    for (var i=0; i<inputs.length; i++) {
        if (inputs[i].value == toCheck) {
            inputs[i].checked = true;
        }
    }
	if(easyRewardzModule != 'Y'){
		document.getElementById("easyRewardzActionTrId").remove();
		delete roleDetails.rights.actionRightsMap["allow_easyrewardz_coupon_redemption"];
	}
    // 4. Set the action rights to the real ones for what we got
	if ( (roleId != 1) || (roleId != 2) ) {
		for (var action in roleDetails.rights.actionRightsMap) {
			var elId = "actionRights_" + action + "_" + roleDetails.rights.actionRightsMap[action];
			var el = document.getElementById(elId);
			if (el != null) {
				el.checked = true;
			} else {
				alert ("Element not found: " + elId);
			}
		}
	}

}


function doSubmit() {

	// create role
	var roleName = trim(document.roleForm.name.value);
	var remarks = document.roleForm.remarks.value;
	document.roleForm.name.value = roleName;

	if (roleName=="") {
		alert("Enter role name");
		document.roleForm.name.value = "";
		document.roleForm.name.focus();
		return false;
	}

	if (document.roleForm.remarks.value!="") {
		if(document.roleForm.remarks.value.length>99){
			alert("Remarks length can not be more than 100 characters");
			document.roleForm.remarks.focus();
			return false;
		}
	}

	// prevent superusers from being edited
	var roleId = document.roleForm.roleId.value;
	if ( (roleId == 1) || (roleId ==2) ) {
		alert("Superuser roles cannot be changed");
		return false;
	}


	if (portalRight == "P") {
		alert("Patient roles cannot be changed");
		return false;
	}
	if (portalRight== "D") {
		alert("Doctor roles cannot be changed");
		return false;
	}

	// Hidden form variables values has been set according to the screens and screenGroups selected from the tree.
	var highLighted = YAHOO.widget.TreeView.getTree('treeDiv').getNodesByProperty('highlightState',1);
	if (highLighted!=null) {
		for (var i = 0; i < highLighted.length; i++) {
			if (document.getElementById("screenRights_"+highLighted[i].data+"_A")!=null)
				document.getElementById("screenRights_"+highLighted[i].data+"_A").value = 'A';
		}
	}

	document.roleForm.submit();
}



function debug(str) {
    var div = document.getElementById("debug");
    div.innerHTML = div.innerHTML + str + "<br>";
}





