
var dialog = {};

var i=0;
ITEM_COL = i++; REQUIRED_QTY_COL = i++;PENDING_QTY_COL = i++; ISSUED_QTY_COL = i++; AVAILABLE_QTY_COL = i++; PO_RAISED_COL=i++;PKG_SIZE_COL=i++; ISSUE_UOM_COL = i++;
BATCH_NO_COL=i++;PURCHASE_COL=i++;INDENT_EDIT_COL=i++;


function initDialog() {
	var rowLen = document.getElementById("indentItemListTab").rows.length;
	dialog.length = rowLen-1;
	for(var i=0;i<rowLen;i++) {
		var dialogid = "dialog"+i;
		dialog[i] = new YAHOO.widget.Dialog(dialogid,
			{
				width:"430px",
				context : ["indentItemListTab", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
		} );
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:closeDialog, scope:dialog[i], correctScope:true } );
		YAHOO.util.Event.addListener(document.getElementsByName("okButton"+i), "click", handleSubmit, dialog[i], true);
		YAHOO.util.Event.addListener(document.getElementsByName("cancelButton"+i), "click", closeDialog, dialog[i], true);
		YAHOO.util.Event.addListener(document.getElementsByName("nextButton"+i), "click", handleNextIndent, dialog[i], true);
		YAHOO.util.Event.addListener(document.getElementsByName("prevButton"+i), "click", handlePreviousIndent, dialog[i], true);
		//YAHOO.util.Event.addListener("opIpConvertCancelBtn", "click", closeDialog, opIpDialog, true);
		dialog[i].cfg.queueProperty("keylisteners", escKeyListener);
		dialog[i].render();
	}
}

function closeDialog(){
	this.hide();
	return false;
}

function setItemCheckBoxEdited() {
		fieldEdited = true;
}

function setAllCheckBoxEdited(id) {
	var selectAll = document.getElementById('selectall'+i);

	var elmts = document.getElementsByName("item_check"+id);
	var qtyelmts = document.getElementsByName("item_qty"+id);
	var countelmt = document.getElementById("itemcount"+id);
	var selectelmt = document.getElementById("selectall"+id);
	for(var i=0;i<elmts.length;i++) {
		if (elmts[i].checked) {
			fieldEdited = true;
		} else {
			fieldEdited = false;
		}
	}
}
var fieldEdited = false;

function setEdited() {
		fieldEdited = true;
}

function CheckNextDisableDialog() {
	var id = document.getElementById("dialogId").value;
	var nextId = "";
	var nextToNextId = "";
	var nextDisableDialog = "";
	var table = document.getElementById('indentItemListTab');
	var rowLen = table.rows.length;
	var totRow = rowLen-1;
	for(var i=0;i<totRow;i++) {
		nextId = parseInt(id)+1;
		nextToNextId = parseInt(nextId)+1;
		nextDisableDialog = document.getElementById('disableDialog'+nextToNextId);
		if (nextDisableDialog !=null && nextDisableDialog !="" && nextDisableDialog == "true") {
			CheckNextDisable();
		} else {
			dialog[id].hide();
			if ( document.getElementById("disableDialog"+nextToNextId).value == "false" )
					showIndentifierDialog(nextToNextId);
		}
	}
}

function CheckPrevDisableDialog() {
	var id = document.getElementById("dialogId").value;
	var prevId = "";
	var idBeforePrev = "";
	var prevDisableDialog = "";
	var table = document.getElementById('indentItemListTab');
	var rowLen = table.rows.length;
	var totRow = rowLen-1;
	for(var i=0;i<totRow;i++) {
		prevId = parseInt(id)-1;
		idBeforePrev = parseInt(prevId)-1;
		prevDisableDialog = document.getElementById('disableDialog'+idBeforePrev);
		if (prevDisableDialog !=null && prevDisableDialog !="" && prevDisableDialog == "true") {
			CheckPrevDisableDialog();
		} else {
			dialog[id].hide();
			if ( document.getElementById("disableDialog"+idBeforePrev).value == 'false' )
					showIndentifierDialog(idBeforePrev);
		}
	}
}

function handleNextIndent() {
	var id = document.getElementById("dialogId").value;
	var nextId = parseInt(id)+(1);
	var nextToNextId = parseInt(nextId)+(1);
	var row = getIndentRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
	var disableDialog = document.getElementById('disableDialog'+nextId);
	var nextDisableDialog=document.getElementById('disableDialog'+nextToNextId);
    if (nRow != null) {
        //YAHOO.util.Dom.removeClass(row, 'editing');
        //YAHOO.util.Dom.addClass(row, 'selectedRow');
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[INDENT_EDIT_COL]);
		var rowObj = getThisRow(anchor);
		if (disableDialog != null && disableDialog.value == 'false') {
			if (rowObj != null) {
				var rowId = rowObj.rowIndex -1;
					if (fieldEdited) {
						if(handleSubmit()){
							fieldEdited = false;
						} else {
							fieldEdited = true;
							return false;
						}
					}
				dialog[id].hide();
				if ( document.getElementById("disableDialog"+rowId).value == 'false' )
					showIndentifierDialog(rowId);
			}
		} else {
			if (disableDialog != null && disableDialog.value == 'true' && nextDisableDialog != null) {
				if (fieldEdited) {
						if(handleSubmit()){
							fieldEdited = false;
						} else {
							fieldEdited = true;
							return false;
						}
				}
				CheckNextDisableDialog();
			} else {
				if(fieldEdited) {
					if(handleSubmit()) {
						dialog[id].show();
				    	fieldEdited = false;
				    } else {
				    	fieldEdited = true;
				    	return false;
				    }
				}
			}
		}
    } else {
		if(fieldEdited) {
			if(handleSubmit()) {
				dialog[id].show();
		    	fieldEdited = false;
		    } else {
		    	fieldEdited = true;
		    	return false;
		    }
		}
	}
}

function handlePreviousIndent() {
	var id = document.getElementById("dialogId").value;
	var row = getIndentRow(id);
	var prevId = parseInt(id)-(1);
	var idBeforePrev = parseInt(prevId)-1;
	var rowObj = null;
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
	var disableDialog = document.getElementById('disableDialog'+prevId);
	var prevDisableDialog=document.getElementById('disableDialog'+idBeforePrev);
	 if (prevRow != null) {
	//YAHOO.util.Dom.removeClass(row, 'editing');
	//YAHOO.util.Dom.addClass(prevRow, 'selectedRow');
		var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[INDENT_EDIT_COL]);
		rowObj = getThisRow(anchor);
		if (rowObj != null) {
			var rowId = rowObj.rowIndex -1;
			if (prevId != -1 && disableDialog != null && disableDialog.value == 'false') {
				if (fieldEdited) {
					if(handleSubmit()){
						fieldEdited = false;
					} else {
						fieldEdited = true;
						return false;
					}
				}
				dialog[id].hide();
				if ( document.getElementById("disableDialog"+rowId).value = 'false' )
					showIndentifierDialog(rowId);
			} else {
				if (disableDialog != null && disableDialog.value == 'true' && prevDisableDialog != null) {
					if (fieldEdited) {
						if(handleSubmit()){
							fieldEdited = false;
						} else {
							fieldEdited = true;
							return false;
						}
					}
					CheckPrevDisableDialog();
				} else {
					if(fieldEdited) {
						if(handleSubmit()) {
							dialog[id].show();
					    	fieldEdited = false;
					    } else {
					    	fieldEdited = true;
					    	return false;
					    }
					}
				}
			}
		} else {
			if(fieldEdited) {
				if(handleSubmit()) {
					dialog[id].show();
			    	fieldEdited = false;
			    } else {
			    	fieldEdited = true;
			    	return false;
			    }
			}
		}
	}
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndentRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i+1];
}

function getRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i];
}

function showIndentifierDialog(id) {
	document.getElementById('dialog' + id).style.display = 'block';
	var button = document.getElementById('itemrow'+id);
	dialog[id].cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	selectChoosenItemValues(id);
	dialog[id].show();
	if (document.getElementsByName("item_qty"+id).length > 0) document.getElementById('item_qty'+id+'0').focus();
	var maskid = "dialog"+id+"_mask";
	var dialog_mask = document.getElementById(maskid);
	dialog_mask.setAttribute("class","newDialogMask");
}

function selectChoosenItemValues(id) {

	if ( document.getElementById("disableDialog"+id).value == 'false' ) {
		if(document.getElementById("batchlbl"+id).textContent == '') {
			var identification = document.getElementById("identification"+id).value;
			if(identification == 'S') {
				var elmts = document.getElementsByName("item_check"+id);
				var qtyelmts = document.getElementsByName("item_qty"+id);
				var countelmt = document.getElementById("itemcount"+id);
				for(var i=0;i<elmts.length;i++) {
					elmts[i].checked = false;
					elmts[i].value  = "";
					qtyelmts[i].value  = "0";
					countelmt.value = parseFloat(0);
				}
			}else {
				var elmts = document.getElementsByName("item_qty"+id);
				for(var i=0;i<elmts.length;i++) {
					elmts[i].value  = "";
				}
			}
		}else{
			var batchArr = document.getElementById("batchlbl"+id).textContent.split(",");
			var identification = document.getElementById("identification"+id).value;
			if(identification == 'S') {
				var identifierelmts = document.getElementsByName("item_identifier"+id);

				var elmts = document.getElementsByName("item_check"+id);
				var qtyelmts = document.getElementsByName("item_qty"+id);

				var countelmt = document.getElementById("itemcount"+id);

				for(var i=0;i<elmts.length;i++) {
					elmts[i].checked = false;
					elmts[i].value  = "";
					qtyelmts[i].value  = "0";
					countelmt.value = parseFloat(0);
				}
				for(var i=0;i<batchArr.length;i++) {
					for(var j=0;j<elmts.length;j++) {
						if(identifierelmts[j].value == batchArr[i].substring(0,batchArr[i].indexOf("("))) {
							elmts[j].checked = true;
							elmts[j].value  = "1";
							qtyelmts[j].value  = "1";
							countelmt.value = parseFloat(countelmt.value) + parseFloat(qtyelmts[j].value);
						}
					}
				}
			}else if(identification == 'B'){
				var elmts = document.getElementsByName("item_qty"+id);
				var identifierelmts = document.getElementsByName("item_identifier"+id);

				for(var i=0;i<elmts.length;i++) {
					elmts[i].value  = "";
				}
				for(var i=0;i<batchArr.length;i++) {
					for(var j=0;j<elmts.length;j++) {
						if(identifierelmts[j].value == batchArr[i].substring(0,batchArr[i].indexOf("("))) {
							elmts[j].value  = batchArr[i].substring(batchArr[i].indexOf("(")+1,batchArr[i].indexOf(")"));
							if (j == 0){
								elmts[0].focus();
							}
						}
					}
				}
			}
		}
	}
}

function handleCancel() {
	this.cancel();
}

function handleSubmit(){
	var dialogId = document.getElementById("dialogId").value;
	var tab = document.getElementById("itemIdentifierTab"+dialogId);

	var batchelmts = document.getElementsByName("batch_no"+dialogId);
	var identifierelmts = document.getElementsByName("item_identifier"+dialogId);

	var qtyavlblelmts = document.getElementsByName("item_qty_avlbl"+dialogId);
	var qtypending =  document.getElementById("itempendingqtylbl"+dialogId).textContent;

	var identification = document.getElementById("identification"+dialogId).value;

	var elmts  = null;
	if(identification == 'B') {
		elmts = document.getElementsByName("item_qty"+dialogId);
	} else if(identification == 'S'){
		elmts = document.getElementsByName("item_check"+dialogId);
	}

	var totalItemIssueQty = 0;
	var batch = '';
	for(var i=0;i<elmts.length;i++) {
		if (!isValidNumber(elmts[i], allowDecimalsForQty)) return false;

		if(identification == 'B') {
			/*if(!validateQtyValues(elmts)) {
				alert("Please enter Qty...");
				document.getElementById(elmts[i].id).focus();
				return false;
			}*/
			if(elmts[i].value > 0) {
				if(parseFloat(elmts[i].value) > parseFloat(qtyavlblelmts[i].value)) {
					showMessage("js.stores.mgmt.indents.qtymore.availableqty");
					document.getElementById(elmts[i].id).value  = '';
					document.getElementById(elmts[i].id).focus();
					return false;
				}
				totalItemIssueQty = totalItemIssueQty + parseFloat(elmts[i].value);
				batch =  batch + identifierelmts[i].value+"("+elmts[i].value+")"+",";
			}
			if(totalItemIssueQty > qtypending) {
				showMessage("js.stores.mgmt.indents.totalqtymore.pendingqty");
				for(var i=0;i<elmts.length;i++) {
					elmts[i].value = 0 ;
				}
				document.getElementById(elmts[0].id).focus();
				return false;
			}
		} else if(identification == 'S'){
			if(!validateQtyCheck(elmts)) {
				showMessage("js.stores.mgmt.indents.checkitems");
				return false;
			}

			if(elmts[i].checked) {
				totalItemIssueQty = totalItemIssueQty + parseFloat(elmts[i].value);
				batch =  batch + identifierelmts[i].value+"("+elmts[i].value+")"+",";
				if(i%3 == 0)  batch = batch + "<br/>";

			}

			if(totalItemIssueQty > qtypending) {
				showMessage("js.stores.mgmt.indents.qtychecked.morethan.pendingqty");
				valSerialChk (dialogId);
				selectAllItems(dialogId);
				return false;
			}
		}
	}
	document.getElementById("batchlbl"+dialogId).innerHTML = batch;
	dialog[dialogId].hide();
	return true;
}

function checkoruncheckItems (elmts,val) {
	for(var i=0;i<elmts.length;i++) {
		elmts[i].checked = val;
	}
}

function valSerialChk (dialogId) {
	document.getElementById("selectall"+dialogId).checked = false;
	checkoruncheckItems (document.getElementsByName("item_check"+dialogId),false);
	document.getElementById("itemcount"+dialogId).value = 0;
	document.getElementById("batchlbl"+dialogId).innerHTML = '';
}

function validateQtyValues(elmts) {
	for(var i=0;i<elmts.length;i++) {
		if(elmts[i].value > 0) {
			return true;
		}
	}
	return false;
}

function validateQtyCheck(elmts) {
	for(var i=0;i<elmts.length;i++) {
		if(elmts[i].checked) {
			return true;
		}
	}
	return false;
}

function assignValue(obj,itemqtyObj,countObj) {
	if(obj.checked) {
		obj.value  = "1";
		itemqtyObj.value  = "1";
		countObj.value = parseFloat(countObj.value) + parseFloat(obj.value);
	}else{
		obj.value  = "0";
		itemqtyObj.value  = "0";
		countObj.value = parseFloat(countObj.value) - 1;
	}
}

function selectRow(itemSelectObj,btnObj) {
	if(document.getElementById(itemSelectObj).value == 'false') {
		document.getElementById(itemSelectObj).value = "true";
		document.getElementById(btnObj).disabled = false;
	} else {
		document.getElementById(itemSelectObj).value = "false";
		document.getElementById(btnObj).disabled = true;
	}
}

function selectpur(id) {
	if(document.getElementById('purchase'+id).checked) {
		document.getElementById('purchaseSelect'+id).value = "true";
	} else {
		document.getElementById('purchaseSelect'+id).value = "false";
	}
}



function checkEmptyRows() {
	var selectElmts = document.getElementsByName("indentSelect");
	var empty = true;
	for(var i=0;i<selectElmts.length;i++) {
		var batchElmt = document.getElementById("batchlbl"+i);
		if(batchElmt.textContent != '') {
			document.getElementById("indentSelect"+i).value = 'true';
			empty = false;
		} else {
			document.getElementById("indentSelect"+i).value = 'false';
		}
	}
	return empty;
}

function selectAllItems(id) {
	var elmts = document.getElementsByName("item_check"+id);
	var qtyelmts = document.getElementsByName("item_qty"+id);
	var countelmt = document.getElementById("itemcount"+id);
	var selectelmt = document.getElementById("selectall"+id);
	if(selectelmt.checked) {
		for(var i=0;i<elmts.length;i++) {
			elmts[i].checked = true;
			elmts[i].value  = "1";
			qtyelmts[i].value  = "1";
			countelmt.value = parseFloat(elmts.length);
		}
	}else{
		for(var i=0;i<elmts.length;i++) {
			elmts[i].checked = false;
			elmts[i].value  = "0";
			qtyelmts[i].value  = "0";
			countelmt.value = parseFloat(0);
		}
	}
}
