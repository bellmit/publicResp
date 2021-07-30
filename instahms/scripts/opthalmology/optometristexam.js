
	function selectValue(obj,plusminus){
		var val = obj.children[0].innerHTML;
		var objToSet = document.getElementById("fromId").value;
		if (plusminus == 'plus'){
			document.getElementById(objToSet).value = "+"+val;
		} else{
			document.getElementById(objToSet).value = "-"+val;
		}
		dialog.cancel();
	}


	function openDialogBox(obj){
	var objId = obj.id;
	var inputObjId = objId.substr(objId.indexOf(0,'img_')-1,objId.length);
	document.getElementById("fromId").value = inputObjId;
	dialog.cfg.setProperty("context", [obj, "tr", "tr"], false);
	dialog.show();

}
function initDialog(){
dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"250px",
			context : ["valuesTab", "tr", "br"],
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

function enterNumbersAndPlusMinus(obj) {

	if (obj.value != '' && obj.value.search(/^[-+]?[0-9]+(\.[0-9]+)?$/) == -1){
		alert('Please Enter Valid Format');
		obj.value = '';
		return false;
	} else {
		return true;
	}
}

function setStatus(obj, val) {

	if (document.getElementById(obj.name).checked == true) {
		document.getElementById('status').value = val;
	} else {
		document.getElementById('status').value = '';
	}
}

function restrictLength(obj, len) {

	if (obj.value.length >= len+1) {
		obj.value = obj.value.substring(0, 200);
		alert("You should not enter more than "+len+" characters");
		return false;
	}
	return true;
}
