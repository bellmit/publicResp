var Dom = YAHOO.util.Dom;
var gColumnMenuDialogs = new Array();

YAHOO.util.Event.onContentReady("content", initColumnMenus);

function initColumnMenus() {
	var tableEls = Dom.getElementsByClassName("dialog_displayColumns", "table");
	for (var i=0; i<tableEls.length; i++) {
		var table = tableEls[i];
		var row = table.rows[0];
		row.setAttribute("onclick", "toggleColumnMenu(event, '"+i+"')");

		var colsArray = YAHOO.util.Cookie.get(actionId +"_headerColumns_"+i);
		if (null != colsArray) {
			colsArray = YAHOO.lang.JSON.parse(colsArray);
		}

		if (colsArray == null || colsArray == '') {
			colsArray = new Array();
		}

		alterColumns(colsArray, i);
		var div = createHtmlForHeaderToolbar(i, colsArray, row);
		document.body.appendChild(div);

		var gColumnMenuDialog = new YAHOO.widget.Dialog("ColumnMenuDialogDiv"+i,
				{	width:"200px",
					context : ["gColumnMenuDialogDiv"+i, "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true
				});
		YAHOO.util.Event.addListener('Menu'+ i +'_okBtn', 'click', saveCookieAndAlterColumns, gColumnMenuDialog, true);
		gColumnMenuDialog.cancelEvent.subscribe(saveCookieAndAlterColumns);
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:gColumnMenuDialog.cancel,
		                                                scope:gColumnMenuDialog,
		                                                correctScope:true } );
		gColumnMenuDialog.cfg.queueProperty("keylisteners", escKeyListener);
		gColumnMenuDialog.render();
		gColumnMenuDialogs["ColumnMenuDialogDiv"+i] = gColumnMenuDialog;
	}
}


function toggleColumnMenu(e, index) {
	var evt = window.event || e;
	var mX, mY;

    if (!evt.target) {//if event obj doesn't support e.target, presume it does e.srcElement
    	evt.target=evt.srcElement
    } //extend obj with custom e.target prop

    var obj = evt.target;
	var toolbarObj = document.getElementById('ColumnMenuDialogDiv' + index);
	if (obj.type == 'checkbox' || obj.tagName.toLowerCase() == 'a') {

    } else {
    	hideOtherMenuToolbar("ColumnMenuDialogDiv"+index, index, obj);
    }
}

function hideOtherMenuToolbar(id, menuIndex, obj) {
	for (var key in gColumnMenuDialogs) {
		document.getElementById(key).style.display =
			key == id ? 'block' : 'none';
		var table = document.getElementById('columnMenuTable'+menuIndex);
		gColumnMenuDialogs[key].cfg.setProperty('context', [obj, 'tl', 'bl'], false);
		if (key == id) {
			gColumnMenuDialogs[key].show();
		} else {
			gColumnMenuDialogs[key].hide();
		}
	}
}

function saveCookieAndAlterColumns() {
	var menuIndex = this.getData().menuIndex;
	var chkEls = document.getElementsByName("Menu"+menuIndex+"_columnMenu");
	var columns = new Array();
	var unCheckedCount = 0;
	for (var i=0; i<chkEls.length; i++) {
		if (!chkEls[i].checked) {
			columns[unCheckedCount] = {label: chkEls[i].value};
			unCheckedCount++;
		}
	}
	if (unCheckedCount == chkEls.length) {
		alert("Atleast one column should be displayable.");
		return false
	}
	var expDate = new Date();
	expDate.setDate(expDate.getDate() + 100);
	YAHOO.util.Cookie.set(actionId +"_headerColumns_"+menuIndex, YAHOO.lang.JSON.stringify(columns), {expires: expDate});

	alterColumns(columns, menuIndex);
	this.hide();
}

function alterColumns(columns, menuIndex) {
	var tableEl = document.getElementsByClassName('dialog_displayColumns')[menuIndex];
	var row = tableEl.rows[0];
	for (var j=0; j<row.cells.length; j++) {
		var display = 'table-cell';
		var firstChild = Dom.getFirstChildBy(row.cells[j]);
		var label = "";
		var isDisabled = Dom.hasClass(row.cells[j], 'noremove');
		if ((firstChild == null || firstChild.nodeType == 3) && row.cells[j].innerHTML != '')
			label = row.cells[j].innerHTML.trim();
		else if (firstChild != null && firstChild.tagName.toLowerCase() == 'a')
			label = firstChild.innerHTML.trim();

		for (var k=0; k<columns.length; k++) {
			if (label == columns[k].label && !isDisabled) {
				display = 'none';
				break;
			}
		}
		for (var l=0; l<tableEl.rows.length; l++) {
			if (tableEl.rows[l].cells[j] != null)
				tableEl.rows[l].cells[j].style.display = display;
		}
	}
}

function createHtmlForHeaderToolbar(index, colsArray, headerRow) {
	var div = document.createElement('div');
	div.id = 'ColumnMenuDialogDiv'+index;
	div.style.display = 'none';

	var table = document.createElement('table');
	table.id = 'columnMenuTable'+index;

	var dialogBody = document.createElement('div');
	dialogBody.className = 'bd';

	var fieldset = document.createElement("fieldset");
	fieldset.className = 'fieldSetBorder';
	var legend = document.createElement("legend");
	legend.className = 'fieldSetLabel';
	legend.appendChild(document.createTextNode("Display Columns"));

	var form = document.createElement("form");
	form.name = 'ColumnMenuDialogForm' + index;

	var inputEl = makeHidden('menuIndex', 'menuIndex', index+'');
	form.appendChild(inputEl);
	fieldset.appendChild(legend);
	fieldset.appendChild(table);
	form.appendChild(fieldset);

	div.appendChild(dialogBody);
	dialogBody.appendChild(form);

	var menuIndex = "Menu"+index;
	var checkBoxIndex = 0;

	for (var colIndex=0; colIndex<headerRow.cells.length; colIndex++) {
		var firstChild = Dom.getFirstChildBy(headerRow.cells[colIndex]);
		//if the table's cell's class is set to noremove then the field is disabled from edit.
		var isDisabled = Dom.hasClass(headerRow.cells[colIndex], 'noremove');
		var disabledStr = isDisabled ? '  disabled="true"  ': '';
		var label = '';
		if ((firstChild == null || firstChild.nodeType == 3) && headerRow.cells[colIndex].innerHTML.trim() != '')
			label = headerRow.cells[colIndex].innerHTML.trim();
		else if (firstChild != null && firstChild.tagName.toLowerCase() == 'a')
			label = firstChild.innerHTML.trim();

		if (label != '') {
			var radioLabelName = label;
			var checked = 'checked';
			for (var i=0; i<colsArray.length; i++) {
				if (label == colsArray[i].label && !isDisabled)
					checked = '';
			}
			var row = table.insertRow(-1);
			var cell = row.insertCell(-1);
			cell.innerHTML = radioLabelName;
			cell.setAttribute("style", "text-align: right");

			cell = row.insertCell(-1);
			cell.innerHTML = '<input type="checkbox" name="'+ menuIndex +'_columnMenu" id="'+ menuIndex +'_chkbox_'+ checkBoxIndex +'" value="'+radioLabelName+'" '+checked+ disabledStr +' >';
			checkBoxIndex++;
		}

	}
	var row = table.insertRow(-1);
	var cell = row.insertCell(-1);
	cell.colspan = 2;
	cell.innerHTML = '<input type="button" id="'+ menuIndex +'_okBtn" value="Ok"/>';
	return div;
}
