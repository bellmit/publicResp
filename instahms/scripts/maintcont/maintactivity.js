	/* itemNames AutoComplete */
	var oAutoComp = null;
	function initAssetAutoComplete() {
		var assetNames = []; var issueItemNames = []; var j = 0; var k = 0;

		if (null != assets){
			if(centerId == 0) {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A')
				 	assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
				}
			}else {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A' && assets[i].CENTER_ID == centerId)
				 		assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';

				}
			}
		}

		//YAHOO.util.Event.addListener(itemname,"keypress",isEventEnterEscAuto);
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = null;

			dataSource = new YAHOO.widget.DS_JSArray(assetNames);

			if(oAutoComp != null) {
				oAutoComp.destroy();
			}

			oAutoComp = new YAHOO.widget.AutoComplete('asset_name', 'asset_dropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 50;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = true;
			oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
			oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
			oAutoComp.itemSelectEvent.subscribe(onSelectAsset);
		}
	}

	function onSelectAsset(){
		var assetSel = document.forms[0].asset_name.value;
		var assetId = document.forms[0].asset_id;
		var batchNo = document.forms[0].batch_no;
		for (var i=0; i<assets.length; i++){
			var asset = assets[i]
			if ((null != asset) && (asset.MEDICINE_NAME != '')){
				var assetString = asset.MEDICINE_NAME+'-'+asset.BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
				if (assetString.trim() == assetSel){
					assetId.value = asset.MEDICINE_ID;
					batchNo.value = asset.BATCH_NO;
				}

			}
		}
	}

	function cancelComponent(index) {
		if (document.getElementById('description'+index).value != '') {
			if (document.getElementById('componentCheckBox'+index).checked) {
				document.getElementById('component'+index).disabled = true;
				document.getElementById('description'+index).disabled = true;
				document.getElementById('labourCost'+index).disabled = true;
				document.getElementById('cost'+index).disabled = true;
			} else {
				document.getElementById('component'+index).disabled = false;
				document.getElementById('description'+index).disabled = false;
				document.getElementById('labourCost'+index).disabled = false;
				document.getElementById('cost'+index).disabled = false;
			}
		}
	}


	function openDialogBox(id){
	var button = document.getElementById("tableRow"+id);


	document.maintactivityform.componentac.value = document.getElementById("component"+id).value;
	document.maintactivityform.descriptionac.value = document.getElementById("description"+id).value;
	document.maintactivityform.labourCostac.value = document.getElementById("labourCost"+id).value;
	document.maintactivityform.costac.value = document.getElementById("cost"+id).value;

	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;

	dialog.show();
	document.getElementById("componentac").focus();
}
function initDialog(){
dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"900px",
			context : ["activityTbl", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
		} );
	var escKeyListener = new YAHOO.util.KeyListener("dialog", { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);

dialog.render();
}

function handleCancel() {
	dialog.cancel();
	if (maintDate == '')
		document.maintactivityform.save.focus();
}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		addActivity();
		return false;
	} else {
		return enterNumAndDot(e);
	}
}

function addActivity() {
	var activitytable = document.getElementById("activityTbl");
	var len = activitytable.rows.length;//alert(len);
	var id = len;   // leave 1 for heading

	var activities = document.maintactivityform.componentac.value;
	if ((activities == null) || (activities == '')){
		alert('Please select a component');
		return false;
	}
	var descp = document.maintactivityform.descriptionac.value;
	var labourObj = document.maintactivityform.labourCostac;
	var costObj = document.maintactivityform.costac;
	var dialogId  = document.getElementById("dialogId").value;
	if (dialogId == (len-1)) {
		var editButton = document.getElementById("add"+parseFloat(len-1));
		var eBut =  document.getElementById("addbut"+parseFloat(len-1));
		editButton.setAttribute("src",popurl+'/icons/Edit.png');
		eBut.setAttribute("title", "Edit Activity");
		eBut.setAttribute("accesskey", "");
	}

	if(!(checkActivityDetails(activities)))
		return false;

	addToInnerHTML(activities, descp,labourObj,costObj);
}
function addToInnerHTML(activities, descp,labourObj,costObj)
{
	var activitytable = document.getElementById("activityTbl");
	var tabLen = activitytable.rows.length;
	var dialogId  = document.getElementById("dialogId").value;
	var flag = '';

	var checkbox = makeImageButton('imgDelete','imgDelete'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	checkbox.setAttribute('onclick','cancelRow(this.id,'+dialogId+')');

	var hiddenDelete = makeHidden('componentCheckBox','componentCheckBox'+dialogId,'false');
	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(checkbox);
	}
	document.getElementById("componentLabel"+dialogId).textContent =activities;
	document.getElementById("descriptionLabel"+dialogId).textContent =descp ;
	document.getElementById("labourCostLabel"+dialogId).textContent = formatAmountObj(labourObj) ;
	document.getElementById("costLabel"+dialogId).textContent = formatAmountObj(costObj);

	document.getElementById("component"+dialogId).value = activities;
	document.getElementById("description"+dialogId).value = descp;
	document.getElementById("labourCost"+dialogId).value = labourObj.value;
	document.getElementById("cost"+dialogId).value = costObj.value;
	document.getElementById("componentCheckBox"+dialogId).value = "false" ;

	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if(nextrow == null){
		AddRowsToGrid(tabLen);
	}
	openDialogBox(eval(dialogId)+1);
}

function AddRowsToGrid(tabLen){
	var activitytable = document.getElementById("activityTbl");
	var tdObj="",trObj="";
	var row = "tableRow" + tabLen;
	var deleteLabel			= makeLabel('itemRow'+tabLen,'');
	var actLabel			= makeLabel('componentLabel'+tabLen,'');
	var descLabel 		    = makeLabel('descriptionLabel'+tabLen,'');
	var labourLabel			= makeLabel('labourCostLabel'+tabLen,'');
	var costLabel			= makeLabel('costLabel'+tabLen,'');

	var componentacHidden 		= makeHidden('component','component'+tabLen,'');
	var itemidHidden 		= makeHidden('item_id','item_id'+tabLen,'');
	var descHidden 				= makeHidden('description','description'+tabLen,'');
	var labourHidden 			= makeHidden('labourCost','labourCost'+tabLen,'');
	var costHidden 			= makeHidden('cost', 'cost'+tabLen,'');

	var componentCheckBoxHidden 		= makeHidden('componentCheckBox','componentCheckBox'+tabLen,'');

	var buton = makeButton1("addbut", "addbut"+tabLen);
	buton.setAttribute("class", "imgButton");
	buton.setAttribute("onclick","openDialogBox('"+tabLen+"'); return false;");
	buton.setAttribute("title", "Add New Item (Alt_Shift_+)");
	buton.setAttribute("accesskey", "+");
	var itemrowbtn = makeImageButton('add','add'+tabLen,'imgAdd',cpath+'/icons/Add.png');
	buton.appendChild(itemrowbtn);

/**	var itemrowbtn = makeButton('add','add'+tabLen,'+');
	itemrowbtn.setAttribute("onclick","openDialogBox('"+tabLen+"')");
	itemrowbtn.setAttribute("class","plus");
	itemrowbtn.setAttribute("accesskey", "+");
	itemrowbtn.setAttribute("title", 'Add New Item (Alt_Shift_+)'); */
	trObj = activitytable.insertRow(tabLen);
	trObj.id = row;


	tdObj = trObj.insertCell(-1);
	tdObj.setAttribute("style","width:15em;padding-left:0.8em;");
	tdObj.appendChild(actLabel);
	tdObj.appendChild(componentacHidden);
	tdObj.appendChild(itemidHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(descLabel);
	tdObj.appendChild(descHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.setAttribute("style","text-align:right;");
	tdObj.appendChild(labourLabel);
	tdObj.appendChild(labourHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.setAttribute("style","text-align:right;");
	tdObj.appendChild(costLabel);
	tdObj.appendChild(costHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.setAttribute("style","text-align:right;");
	tdObj.appendChild(deleteLabel);
	tdObj.appendChild(componentCheckBoxHidden);


	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(buton);

}

function makeButton1(name, id, value){
	var el = document.createElement("button");

	if (name!=null && name!="")
		el.name= name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function checkActivityDetails(activity){
		var dialogId = document.getElementById("dialogId").value;
		if(activity == ''){
			alert("Add an activity");
			document.maintactivityform.componentac.focus();
			return false;
		}
		if(duplicate(activity,dialogId)) {
			alert("Duplicate activity component is not allowed ");
			return false;
		}
		return true;
}

function init () {
	initAssetAutoComplete();
	initDialog();

}

function validate(){
	var schdate = document.maintactivityform.scheduled_date.value;
	var maintdate = document.maintactivityform.maint_date.value;
	var assetname = document.maintactivityform.asset_name.value;
	if (null == schdate || schdate == ''){
		alert("Scheduled Date cannot be empty. Please enter a date ");
		return false;
	}
	if (null == assetname || assetname == ''){
		alert('Asset cannot be empty. Please choose an asset');
		return false;
	}
	return true;

}


function cancelRow(imgObjId,rowid){
	var rowId = rowid.id;
	var deletedInput = document.getElementById('componentCheckBox'+rowid);
	if(deletedInput.value == 'false'){
		deletedInput.value = 'true';
	    document.getElementById(imgObjId).src = popurl+"/icons/Deleted.png";
	    //deleteRowValidation (rowid,true);
	} else {
		deletedInput.value = 'false';
		document.getElementById(imgObjId).src = popurl+"/icons/Delete.png";
		//deleteRowValidation (rowid,false);
	}
}

function duplicate(itemname,id) {
		var tab = document.getElementById("activityTbl");
		for(var j=1;j<tab.rows.length;j++) {
			var name = trim(document.getElementById("componentLabel"+j).textContent);
			if((id != j) && (name == itemname)) {
				return true;
			}
		}
		return false;
	}


