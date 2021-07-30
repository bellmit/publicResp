
// Insta = function() {};
Insta.Dialog = function(dialogElId, contextElId, callbacks) {
	this.elId = dialogElId;
	if (null != callbacks) {
		this.cancelCallback = callbacks.cancelCallback;
	}
	var _dialog = null;
	
	this.init = function() {
		var dialog = new YAHOO.widget.Dialog(elId,
		{
			width:"600px",
			context : [contextElId, "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		} );
		
		registerEventHandlers(dialog);
		_dialog = dialog;
	};

	function registerEventHandlers(dialog) {
		var escKeyListener = new YAHOO.util.KeyListener(dialog, { keys:27 },
				{ fn:cancelHandler, scope:dialog, correctScope:true} );
		dialog.cfg.setProperty("keylisteners", escKeyListener);
		dialog.render();
	};
	
	function cancelHandler() {
		// This refers to the dialog here
		var def = true;
		if (null != this.cancelCallback) {
			def = this.cancelCallback.call(_dialog);
		}
		if (def) this.cancel();
	}
};

Insta.DataGrid = function(elId, deletable, insertable, editable, deleteCallback, addCallback, editCallback) {

	this.elId = elId;
	this.insertable = insertable;
	this.deletable = deletable; 
	this.deleteCallback = deleteCallback;
	this.addCallback = addCallback;
	this.editable = editable;
	this.editCallback = editCallback;
	
	this.tableEl = {};
	this.templateRow = {};
	this.addButtonRow = {};

	var _dt = null

	this.init = function (data) {
		this.tableEl = document.getElementById(elId);
		if (!this.tableEl) return false;
		
		if (this.insertable == null || this.insertable === '' || this.insertable == 'undefined') {
			this.insertable = true;
		}
		
		if (this.deletable == null || this.deletable === '' || this.deletable == 'undefined') {
			this.deletable = true;
		}

		if (this.editable == null || this.editable === '' || this.editable == 'undefined') {
			this.editable = true;
		}

		if (!this.deleteCallback) {
			this.deleteCallback = this.deleteItem;
		}
		
		if (!this.addCallback) {
			this.addCallback = this.showAddDialog;
		}
		
		if (!this.editCallback) {
			this.editCallback = this.showEditDialog;
		}
		

		this.templateRow = prepareTemplateRow(this.tableEl, this.deletable, this.editable);
		this.addButtonRow = insertAddButtonRow(this.insertable, this.templateRow, this.addCallback);
		
		_dt = this;
	};

	this.showAddDialog = function(table) {
		this.showDialog();
	}

	this.showEditDialog = function(table) {
		this.showDialog();
	}

	function initEditDialog (dialogEl) {
		document.getElementById(dialogId).style.display = 'block';
		var dialog = new YAHOO.widget.Dialog(dialogId, { width:"700px",
				context :[this.contextId, "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:false
				});
		dialog.render();
		subscribeEscKeyEvent(dialog);
		registerCancelEventHandler(dialog);
	}
	
	function subscribeEscKeyEvent(dialog) {
		var kl = new YAHOO.util.KeyListener(document, { keys:27 }, { 
			fn:dialog.cancel, 
			scope:dialog, 
			correctScope:true });
		dialog.cfg.setProperty("keylisteners", kl);
	} 

	function registerCancelEventHandler(dialog) {
		dialog.cancelEvent.subscribe(dialog.cancelCallback, dialog, true);
	}

	this.deleteItem = function(table, rowId) {
		if (table) {
			var row = table.rows[rowId];
			var deleted =
				document.getElementById('deleted' + rowId).value = document.getElementById('deleted' + rowId).value == 'false' ? 'true' : 'false';
			if (deleted == 'true') {
				addClassName(document.getElementById("row"+rowId), "delete");
				document.getElementById('delItem'+rowId).src = cpath+'/icons/Deleted.png';
			} else {
				removeClassName(document.getElementById("row"+rowId), "delete");
				document.getElementById('delItem'+rowId).src = cpath+'/icons/Delete.png';
			}
		}
	};

	this.addRow = function() {
		    
		// For a table with 6 rows (index n)
	    // 1st (index 0) row is the header (index 0)
	    // 6th (index 5 i.e n-1) row is the one without any cell content, just the add button
	    // 5th (index 4 i.e n-2) row is a hidden row which is used as a template
	
		var nTotalRows = this.tableEl.rows.length;   			   		// one for the header and one hidden row. 
		var dataRows =  nTotalRows - 2 - ((this.insertable) ? 1 : 0); 	// if the table is insertable, there is a last add button row, 
																   		// otherwise 1 less
		var templateRow = this.templateRow;            					// last row which needs to be populated with cells adjusted for 0 index
												    		       		// if the table already has 5 data rows, this value will be 5
	   	var deleteCallback = this.deleteCallback;
	   	var rowIndex = dataRows;										// one row above the template row
	   	
	   	return addNewRow(this.templateRow, rowIndex, this.deleteCallback);
	}
	
	function addNewRow(templateRow, newRowIndex, deleteCallback) {

		if (null == templateRow) return null;
		
	   	var newRow = {};
	   	newRow = templateRow.cloneNode(true);
	   	newRow.style.display = '';
	   	newRow.id = "row" + newRowIndex;

	   	if (newRow.cells) {
		   	var lastCol = newRow.cells.length - 1;
		   	if (lastCol >= 0) {
		   		newRow.cells[lastCol].setAttribute("class", "last");
		   	}
		   	for (var k = 0; k <= lastCol; k++) {
		   		processInputNodes(newRow.cells[k], newRowIndex);
		   		processDeleteButtons(newRow.cells[k], newRowIndex, deleteCallback);
		   	}
	   	}
	   	YAHOO.util.Dom.insertBefore(newRow, templateRow);
	   	return newRow;
	}

	function processDeleteButtons(tableCell, rowIndex, deleteCallback) {
		
	   	var deleteNodes = YAHOO.util.Dom.getChildrenBy(tableCell, function(child) {
	   		var name = child.nodeName.toUpperCase();
	   		if (name == "IMG" && child.getAttribute("name") == "delItem") {
	   			return true;
	   		} else {
	   			return false;
	   		}
	   	});
	   	for (var i = 0; i < deleteNodes.length; i++) {
	   		deleteNodes[i].id = deleteNodes[i].id + rowIndex;
	   		deleteNodes[i].onclick = function() {
	   			deleteCallback.call(_dt, _dt.tableEl, rowIndex);
	   		};
	   	}
	}

	function processInputNodes(tableCell, rowIndex) {
		
	   	var inputNodes = YAHOO.util.Dom.getChildrenBy(tableCell, function(child) {
	   		var name = child.nodeName.toUpperCase();
	   		if (name == "SELECT" || name =="INPUT") {
	   			return true;
	   		} else {
	   			return false;
	   		}
	   	});
	   	for (var i = 0; i < inputNodes.length; i++) {
	   		inputNodes[i].id = inputNodes[i].id + rowIndex;
	   	}
	}
	
	function prepareTemplateRow(tableEl, deletable, editable) {
		var tRow = null;
		if (tableEl) {
			tRow = tableEl.rows[tableEl.rows.length - 1];
			if (!tRow) return null;
	
			tRow.style.display = 'none';
			
			// add a delete icon to the template row
			var delCell = makeDeleteCell(deletable);
			tRow.appendChild(delCell);

			// add a edit icon to the template row
			var editCell = makeEditCell(editable);
			tRow.appendChild(editCell);
			
			if (!editable) delCell.setAttribute("class", "last");
		}
		return tRow;
	}
	
	function insertAddButtonRow(insertable, tRow, addCallback) {
		if (insertable) {
			// add a row at the end for the add button
			var addButtonRow = makeAddButtonRow(tRow, addCallback);
			YAHOO.util.Dom.insertAfter(addButtonRow, tRow);
			return addButtonRow;
		}
		return null;
	}
	
	function makeAddButtonRow(tRow, addCallback) {
		
		var row = document.createElement("tr");
		
		if (tRow && tRow.cells && tRow.cells.length > 0) {
			var emptyCols = document.createElement("td");
			emptyCols.setAttribute("colspan", tRow.cells.length - 1);
			row.appendChild(emptyCols);
		}
		var img = makeImageButton("_te_img_add", "_te_img_add_id", "", cpath + '/icons/Add.png');
		img.setAttribute("align", "center");

		var addButton = document.createElement("button");
		addButton.setAttribute("type", "button");
		addButton.setAttribute("id", "_te_addrows");
		addButton.setAttribute("class", "imgButton");
		
		addButton.onclick = function() {
			addCallback.call(_dt, _dt.tableEl);
		};
		
		addButton.appendChild(img);

		var addButtonCol = document.createElement("td");
	    addButtonCol.setAttribute("style", "width: 20px");
		addButtonCol.appendChild(addButton);
		
		row.appendChild(addButtonCol);
		return row;
	}
	
	function makeDeleteCell(deletable) {
		var cell = document.createElement("TD");
	    if (deletable) {
			var del = makeImageButton("delItem", "delItem", "imgDelete", cpath + "/icons/Delete.png");
			del.setAttribute("style", "width: 16px");
			cell.appendChild(del);
	
			var hidDel = makeHidden("deleted", "deleted", "false"); 
		    cell.appendChild(hidDel);
	
		    var hidAdd = makeHidden("addedNew", "addedNew", "true");
		    cell.appendChild(hidAdd);
	    }
		return cell;
	}

	function makeEditCell(editable) {
		var cell = document.createElement("TD");
	    cell.setAttribute("class", 'last');
	    if (editable) {
			var edt = makeImageButton("editItem", "editItem", "imgEdit", cpath + "/icons/Edit.png");
			edt.setAttribute("style", "width: 16px");
			cell.appendChild(edt);
	
			var hidEdit = makeHidden("edited", "edited", "false"); 
		    cell.appendChild(hidEdit);
	    }
		return cell;
	}
};
