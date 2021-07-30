
// Insta = function() {};

Insta.DataGrid = function(_elId, _config, columnDefs) {

	var _dt = null;
	var _rendered = false;
	var _templateRow = null;
	var _editor = null;
	var _editorForm = null;
	var _addButtonRow = null;
	var _header = null;
	var _editedRow = -1;
	
	this.elId = _elId;
	this.tableEl = document.getElementById(_elId);
	this.config = {actions:{insert:true, delete:true, edit:true}};
	this.columnDefs = columnDefs;
	
	// initialize configuration
	
	if (null!= _config) {
		if (_config.actions) {
			if (null != _config.actions.insert) {
				this.config.actions.insert = _config.actions.insert;
			}
			if (null!= _config.actions.delete) {
				this.config.actions.delete= _config.actions.delete;
			}
			if (null != _config.actions.edit) {
				this.config.actions.edit= _config.actions.edit;
			}
		}
		if (_config.headers) {
			this.config.headers = _config.headers.split(","); 
		} else {
			this.config.headers = [];
		}
		
		this.config.handlers = {};
	}
	
	this.subscribe = function(handlers) {
		if (empty(handlers)) {
			return;
		}
		if (handlers.onEditRow) {
			this.config.handlers.onEditRow = handlers.onEditRow;
		}
		if (handlers.onUpdateRow) {
			this.config.handlers.onUpdateRow = handlers.onUpdateRow;
		}
		if (handlers.onAddRow) {
			this.config.handlers.onAddRow = handlers.onAddRow;
		}
		if (handlers.onDeleteRow) {
			this.config.handlers.onDeleteRow = handlers.onDeleteRow;
		}
	}

	// register default callbacks
	
	this.addRow = function(button, rowIndex) {
		var cfg = this.getEditorConfig();
		if (null != cfg) {
			cfg.setProperty("context", [button.id, "tr", "br"]);	
		}
		this._editedRow = -1;
		var e = this.getEditor();
		var form = this.getEditorForm();
		clearFormAll(form);
		if (this.config.handlers.onAddRow) {
			this.config.handlers.onAddRow.call(this, null);
		}
		if (null != e) e.show();
	}
	
	this.updateDataRow = function(rowIndex, data) {
		var table = this.tableEl;
		if (table && table.rows && table.rows.length > rowIndex + 1) {

			if (this.config.handlers.onUpdateRow) {
				this.config.handlers.onUpdateRow.call(this, row, data);
			}
			
			var row = table.rows[rowIndex+1]
			addClassName(row, "edited" );
			objectToHidden(data, row); // TODO review the +1.
			objectToRowLabelIds(data, row, "lbl_");
			var editor = this.getEditor();
			if (null != editor) {
				editor.cancel();
			}
			return true;
		}
		return false;
	}
	
	this.getEditedRow = function() {
		return this._editedRow;
	}

	this.editRow = function(button, rowIndex) {
		if (null != this.getEditorConfig()) {
			this.getEditorConfig().setProperty("context", [button.id, "tr", "br"]);
		}
		var table = this.tableEl;
		var rowObj = table.rows[rowIndex+1]; // +1 for the header
		if (!isDeleted(rowObj)) {
			// Load data from grid to dialog
			var editorForm = this.getEditorForm();
			if (table) {
				if (rowObj && editorForm) {
					hiddenToForm(rowObj, editorForm);
				}
			}
			this._editedRow = rowIndex;
			if (this.config.handlers.onEditRow) {
				this.config.handlers.onEditRow.call(this, rowObj);
			}
			// Show editor
			if (null != this.getEditor()) {
				this.getEditor().show();
			}
		}
	}

	this.deleteRow = function(button, rowId) {
		var table = this.tableEl;
		if (table) {
			var row = findAncestor(button, "TR");
			var allowDelete = document.getElementById('item_status' + rowId).value != 'X';
			var deletedRowObj =document.getElementById('deleted' + rowId);
			if(null != deletedRowObj  && !empty(deletedRowObj)){
				var deleting = 
					document.getElementById('deleted' + rowId).value = deletedRowObj.value == 'false' ? 'true' : 'false';
			}
			if (isNew(row) || allowDelete) {
				if (deleting == 'true') {
					addClassName(document.getElementById("row"+rowId), "delete");
					document.getElementById('delItem'+rowId).src = cpath+'/icons/Deleted.png';
				} else {
					removeClassName(document.getElementById("row"+rowId), "delete");
					document.getElementById('delItem'+rowId).src = cpath+'/icons/Delete.png';
				}
				disableEditButton(this, rowId);
			}
			
			if (this.config.handlers.onDeleteRow) {
				this.config.handlers.onDeleteRow.call(this, row);
			}
		}
	};

	this.config.callbacks = {"insert":this.addRow, "delete":this.deleteRow, "edit":this.editRow};
	_dt = this;

	this.getEditor = function() {
		return _editor;
	}
	
	this.getEditorForm = function() {
		return _editorForm;
	}

	this.getEditorConfig = function() {
		return this.getEditor().cfg;
	}
	
	
	this.getTemplateRow = function() {
		if (null != _templateRow) return _templateRow;
		if (!this.tableEl) return null;
		if (this.tableEl.rows.length <= 0) return null;
		var tRow = this.tableEl.rows[this.tableEl.rows.length - 1];
		return tRow;
	}

	this.render = function() {
		// initialize template
		_header = insertHeader(this);
		_templateRow = prepareTemplateRow(this);
		_addButtonRow = insertAddButtonRow(this);

		_rendered = ((_templateRow != null) && (_addButtonRow != null));
		return _rendered;
	}
	
	this.registerEditor = function(dialogId) {
		_editor = createEditorDialog(this, dialogId);
		_editorForm = findAncestor(document.getElementById(dialogId), "FORM");
	}
	
	this.init = function (data) {
		if (!_rendered) return false;
		if (null != data) {
			addDataRows(this, data) ;
		}
		return true;
	};

	this.addEmptyRow = function() {
		var rowIndex = getLastRowIndex(this);
	   	return addNewRow(this, rowIndex);
	}
	
	this.insertDataRow = function(data) {
		var dataList = [data];
		addDataRows(this, dataList);
		var addedRowIndex = getLastRowIndex(this);
		var table = this.tableEl;
		var row = table.rows[addedRowIndex+1]
		addClassName(row, "added" );
		objectToHidden(data, row); // TODO review the +1.
		this._editedRow = -1;
		var form = this.getEditorForm();
		if (form) {
			//clearFormAll(form);
		}
		if (this.config.handlers.onAddRow) {
			this.config.handlers.onAddRow.call(this, null);
		}
		return addedRowIndex;
	}

	function disableDeleteButton(grid, rowIndex) {
		var table = grid.tableEl;
		if (rowIndex >= 0 && table && table.rows && table.rows.length > rowIndex + 1) {
			var row = table.rows[rowIndex + 1];
			if (row && row.cells) {
				var column = row.cells[row.cells.length -2];
				column.innerHTML = "";
				var cell = addDeleteButton(row, row.cells.length - 2, rowIndex);
				processDeleteButtons(grid, cell, rowIndex);
			}
		}
	}

	function disableEditButton(grid, rowIndex) {
		var table = grid.tableEl;
		if (rowIndex >= 0 && table && table.rows && table.rows.length > rowIndex + 1) {
			var row = table.rows[rowIndex + 1];
			if (row && row.cells) {
				var column = row.cells[row.cells.length -1];
				column.innerHTML = "";
				var cell = addEditButton(row, row.cells.length - 1);
				processEditButtons(grid, cell, rowIndex);
			}
		}
	}
	// private functions
	function insertHeader(grid) {
		var tRow = grid.getTemplateRow();
		if (null == tRow) return null;
		
	   	var newRow = {};
	   	newRow = tRow.cloneNode(true);
	   	newRow.style.display = '';
	   	newRow.id = "rowHeader";
	   	addClassName(newRow, "header");

	   	YAHOO.util.Dom.insertBefore(newRow, tRow);
	   	addActionColumns(newRow, grid.config);
	   	addHeaderText(newRow, grid.config.headers);
	   	// processHeader(grid, newRow, grid.columnDefs)
	   	return newRow;
	}

	function addHeaderText(row, titles) {
		if (titles && titles.length > 0) {
			for (var i = 0; i < titles.length; i++) {
				row.cells[i].innerHTML = titles[i];
			}
		}
	}
	
	function processHeader(grid, headerRow, columnDefs) {
		var extraColumns = 0;
		if (grid.config.actions.edit) extraColumns += 1;
		if (grid.config.actions.delete) extraColumns += 1;
		if (null != columnDefs && columnDefs.length > 0) {
			for (var i = 0; i < headerRow.cells.length - extraColumns; i++) {
				if (columnDefs.length > i) {
					headerRow.cells[i].innerHTML = columnDefs[i].name;
				} else {
					headerRow.cells[i].innerHTML = "";
				}
			}
		}
		var editColumnIndex = -1;
		var delColumnIndex = -1;
		if (grid.config.actions.edit && headerRow.cells.length > 0) {
			editColumnIndex = headerRow.cells.length - 1;
		}
		
		if (grid.config.actions.delete && headerRow.cells.length > 0) {
			delColumnIndex = (editColumnIndex > 0) ?  editColumnIndex - 1 : headerRow.cells.length - 1;
		}
		if (editColumnIndex >= 0) headerRow.cells[editColumnIndex].innerHTML = "";
		if (delColumnIndex >= 0) headerRow.cells[delColumnIndex].innerHTML = "";
	}
	
	function getLastRowIndex(grid) {
		var table = grid.tableEl;
		var totalRows = 0, dataRows = 0;
		if (null != table && null != table.rows) {
			totalRows = table.rows.length;
			dataRows = totalRows - 3 ; // -3 for header, template row and add button row
		}
		return dataRows-1;
	}
	
	function addDataRows(grid, dataList) {
		var lastRow = getLastRowIndex(grid);
		for (var i = 0 ; i < dataList.length; i++) {
			var row = addNewRow(grid, lastRow+1+i);
			processRow(grid, row, lastRow+1+i, dataList[i]);
			if (dataList[i] && dataList[i].item_status && dataList[i].item_status == "X") {
				disableDeleteButton(grid, lastRow+1+i);
				disableEditButton(grid, lastRow+1+i);
			}
		}
	}
	
	function createEditorDialog (grid, dlgElId) {
		return initDialog(dlgElId);
	}
	
	function addNewRow(grid, newRowIndex) {

		var tRow = grid.getTemplateRow();
		if (null == tRow) return null;
		
	   	var newRow = {};
	   	newRow = tRow.cloneNode(true);
	   	newRow.style.display = '';
	   	newRow.id = "row" + newRowIndex;

	   	YAHOO.util.Dom.insertBefore(newRow, _addButtonRow);
	   	return newRow;
	}
	
	function processRow(grid, row, rowIndex, data) {
		
	   	if (row && row.cells) {
		   	var lastCol = row.cells.length - 1;
		   	if (lastCol >= 0) {
		   		row.cells[lastCol].setAttribute("class", "last");
		   	}
	   		insertData(grid, row, rowIndex, data);
		   	for (var k = 0; k <= lastCol; k++) {
		   		processInputNodes(grid, row.cells[k], rowIndex);
		   		processDeleteButtons(grid, row.cells[k], rowIndex);
		   		processEditButtons(grid, row.cells[k], rowIndex)
		   	}
		   	
	   	}
	}
	
	function insertData(grid, row, rowIndex, data) {
		objectToRowLabelIds(data, row, "lbl_");
		if (grid.columnDefs) {
			if (row.cells && row.cells[0]) {
				for (var i = 0; i < grid.columnDefs.length; i++) {
					var hidden = makeHidden(grid.columnDefs[i].field, grid.columnDefs[i].field);
					row.cells[0].appendChild(hidden);
				}
			}
		}
		objectToHidden(data, row);
	}
	
	function processEditButtons(grid, tableCell, rowIndex) {
		var editNodes = getNodes(tableCell, "IMG", "editItem");
		processActionButtons(grid, editNodes, rowIndex, grid.config.callbacks.edit);
	}
	
	function processDeleteButtons(grid, tableCell, rowIndex) {
	   	var deleteNodes = getNodes(tableCell, "IMG","delItem");
	   	processActionButtons(grid, deleteNodes, rowIndex, grid.config.callbacks.delete);
	}
	
	function getNodes(parent, nodeName, elName) {
	   	var nodes = YAHOO.util.Dom.getChildrenBy(parent, function(child) {
	   		var name = child.nodeName.toUpperCase();
	   		if (name == nodeName.toUpperCase() && child.getAttribute("name").toUpperCase() == elName.toUpperCase()) {
	   			return true;
	   		} else {
	   			return false;
	   		}
	   	});
	   	return nodes;
	}

	function processActionButtons(grid, actionNodes, rowIndex, actionCallback) {
		if (null == actionNodes) return ;
	   	for (var i = 0; i < actionNodes.length; i++) {
	   		var actionNode = actionNodes[i];
	   		actionNode.id = actionNodes[i].id + rowIndex;
	   		actionNode.onclick = function() {
	   			actionCallback.call(grid, actionNode, rowIndex);
	   		};
	   	}
	}

	function processInputNodes(grid, tableCell, rowIndex) {
		
	   	var inputNodes = YAHOO.util.Dom.getChildrenBy(tableCell, function(child) {
	   		var name = child.nodeName.toUpperCase();
	   		if (name == "SELECT" || name =="INPUT") {
	   			return true;
	   		} else {
	   			return false;
	   		}
	   	});
	   	var columnDefs = grid.columnDefs;
	   	for (var i = 0; i < inputNodes.length; i++) {
	   		var nodeName = inputNodes[i].getAttribute("name");
	   		var nodeId = inputNodes[i].getAttribute("id");
	   		if (!nodeName && nodeId) { inputNodes[i].setAttribute("name",inputNodes[i].id) };
	   		if (!nodeId && nodeName) { inputNodes[i].setAttribute("id",inputNodes[i].getAttribute("name")) };
	   		if (!nodeId && !nodeName && columnDefs) {
	   			for (var hid in columDefs) {
	   				
	   			}
	   		}
	   		if (inputNodes[i].id) { inputNodes[i].id = inputNodes[i].id + rowIndex };
	   	}
	}
	
	function prepareTemplateRow(grid) {
		var tRow = null;
		if (grid.tableEl) {
			tRow = grid.getTemplateRow(); // tableEl.rows[tableEl.rows.length - 1];
			var numDataColumns = (tRow && tRow.cells) ? tRow.cells.length : 0; 
			if (!tRow) return null;
	
			tRow.style.display = 'none';
			
			var numActionColumns = addActionColumns(tRow, grid.config);
			var colIndex = numDataColumns + numActionColumns -1;
			
			// First edit button, since it goes at the end
			if (grid.config.actions.edit) {
				addEditButton(tRow, colIndex); // -1 for zero based index
				colIndex = colIndex - 1; // one column consumed
			}
			
			// Delete button after edit, if there is one
			if (grid.config.actions.delete) {
				addDeleteButton(tRow, colIndex);
				colIndex = colIndex - 1;
			}
			tRow.cells[tRow.cells.length-1].setAttribute("class", "last");
		}
		return tRow;
	}
	
	function insertAddButtonRow(grid) {
		// add a row at the end for the add button
		var addButtonRow = makeAddButtonRow(grid);
		if (null != addButtonRow)
			YAHOO.util.Dom.insertBefore(addButtonRow, grid.getTemplateRow());
		if (!grid.config.actions.insert) addButtonRow.style.display = "none";
		return addButtonRow;
	}
	
	function makeAddButtonRow(grid) {
		var tRow = grid.getTemplateRow();
		var config = grid.config;
		var row = document.createElement("tr");
		
		if (tRow && tRow.cells && tRow.cells.length > 0) {
			var emptyCols = document.createElement("td");
			emptyCols.setAttribute("colspan", tRow.cells.length - 1);
			row.appendChild(emptyCols);
		}
		
		
		var addButtonCol = document.createElement("td");
	    addButtonCol.setAttribute("style", "width: 20px");

		if (config.actions.insert) {
		    var addButton = makeAddButton(); 
			addButton.onclick = function() {
				config.callbacks.insert.call(grid, addButton, -1);
			};
			addButtonCol.appendChild(addButton);
		}

		row.appendChild(addButtonCol);
		return row;
	}

	function makeAddButton() {
		var img = makeImageButton("_te_img_add", "_te_img_add_id", "", cpath + '/icons/Add.png');
		img.setAttribute("align", "center");

		var addButton = document.createElement("button");
		addButton.setAttribute("type", "button");
		addButton.setAttribute("id", "_te_addrows");
		addButton.setAttribute("class", "imgButton");
		
		addButton.appendChild(img);
		return addButton;
	}
	
	function addActionColumns(row, config) {
		var numColumns = 0; 
		// We anyway add one column
		var cell1 = document.createElement("td");
		cell1.setAttribute("style", "width: 16px;");
		row.appendChild(cell1);
		numColumns++;
		// We add one more columns if there are 2 actions on a row
		if (config.actions.delete && config.actions.edit) {
			cell2 = document.createElement("td");
			cell2.setAttribute("style", "width: 16px;");
			row.appendChild(cell2);
			numColumns++;
	    }
		return numColumns;
	}

	function addDeleteButton(row, colIndex, rowIndex) {
		var cell = null;
	    if (row && row.cells && row.cells.length > colIndex) {
	    	cell = row.cells[colIndex];
	    	var icon = isDeleted(row) ? "/icons/Deleted.png" : "/icons/Delete.png";
			var del = makeImageButton("delItem", "delItem", "imgDelete", cpath + icon);
			del.setAttribute("style", "width: 16px;");
			cell.appendChild(del);
			var hidDel = makeHidden("deleted", "deleted" + (rowIndex != null) ? rowIndex : "", "false"); 
		    cell.appendChild(hidDel);
	
		    var hidAdd = makeHidden("addedNew", "addedNew", "true");
		    cell.appendChild(hidAdd);
	    }
		return cell;
	}

	function addEditButton(row, colIndex) {
		var cell = null;
	    if (row && row.cells && row.cells.length > colIndex) {
	    	var icon = isDeleted(row) ? "/icons/Edit1.png" : "/icons/Edit.png";
	    	cell = row.cells[colIndex];
			var img = makeImageButton("editItem", "editItem", "button", cpath + icon);
			cell.appendChild(img);
	    }
		return cell;
	}
	
	function isDeleted(row) {
		var statusEl = getElementByAttribute(row, "item_status", "name");
		var status = (null != statusEl ? statusEl.value : "");
		return (row.classList.contains("delete")) || (status == "X");
	}

	function isNew(row) {
		return row.classList.contains("added");
	}
	
};
