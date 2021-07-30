var psAc = null;
var avblityDialog = null;
var gCurrentRow = null;

var toolbar = {
	Issue: {
		title: "Issue",
		imageSrc: "icons/Collect.png",
		href: 'pages/stores/stocktransfer.do?_method=show',
		onclick: null,
		description: "Kit issue to OT store"
	}
};

function saveStockOrigQty() {
	for (var store in stock) {
		var storeStock = stock[store];
		for (var medicineId in storeStock) {
			storeStock[medicineId].available = storeStock[medicineId].qty;
		}
	}
}

/*
 * Calculate the availability, reduce stock and display appropriate availability
 * List of surgeries is in surgeries = { op_id: ..., kit_id:  }
 * Kit Details is in kitDetails = { kit_id: [{kit_item_id: 11, qty: 22}, ...] ... }
 * Stock is in the variable stock = { 0: { 1234: { qty: 11}, ... }, ... }
 */
function setAvailability(reduceStock) {
	// copy quantity in stock to available quantity
	var table = document.getElementById('resultTable');
	var storeStock = stock[document.getElementById("storeSelect").value];
	if (!storeStock) {
		storeStock = {};
	}

	for (var i=0; i<surgeries.length; i++) {
		var s = surgeries[i];
		var imgPath = cpath + '/images/empty_flag.gif';

		if (s.issued == 'Y') {
			imgPath = cpath + '/images/grey_flag.gif';

		} else {
			s.kit_availability = [];		// for saving the kit availability details for this surgery
			var kitId = s.kit_id;
			var kitItems = kitDetails[kitId];

			for (var k=0; k<kitItems.length; k++) {
				var kitItem = kitItems[k];
				var medicineId = kitItem.kit_item_id;

				// ensure we have a record for the stock
				if (!storeStock[medicineId]) {
					storeStock[medicineId] = { qty:0, available:0 };
				}

				// save the availability data before reducing the stock
				s.kit_availability.push({
					medicine_name: kitItem.medicine_name,
					req_qty: kitItem.qty,
					avbl_qty: storeStock[medicineId].available
				});

				// reduce the available qty for the rest of the items
				storeStock[medicineId].available -= kitItem.qty;

				if (storeStock[medicineId].available < 0) {
				//reducestock is true when virtually qty is reduced for each surgery.
				//this function can be called even when store is changed,in such case no need to reduce stock and check wether -ve.
					imgPath = cpath + '/images/red_flag.gif';
				}
			}
		}

		var statusCell = table.rows[i+1].cells[6];
		var img = statusCell.getElementsByTagName('img')[0];
		img.src = imgPath;
	}
}

function onChangeStore(e) {
	refreshItemStock();
	setAvailability();
}

function loadStores() {
	var storesSel = document.getElementById("storeSelect");
	loadSelectBox(storesSel, stores, "dept_name", "dept_id");
	if (stores.length == 1) {
		document.getElementById("storeSelectDiv").style.display = 'none';
	} else if (stores.length == 0) {
		alert("There are no sterile stores defined, all kits will be unavailable");
	}
}

/*
 * To prevent the toolbar from showing when clicking on the info icon.
 */
function checkAndShowToolbar(rowIndex, e, tableId, requestParams, enableList, toolbarKey, validateOnRClick) {
	if (!gCurrentRow)
		showToolbar (rowIndex, e, tableId, requestParams, enableList, toolbarKey, validateOnRClick);
}

function showAvailability(row) {

	if (gCurrentRow)
		YAHOO.util.Dom.removeClass(gCurrentRow, 'editing');
	gCurrentRow = row;

	var table = document.getElementById("avDialogTable");
	var len = table.rows.length;
    for (var p = len - 2; p > 0; p--) {
		// delete all except last (template) and first (header) rows
        table.deleteRow(p);
    }
	var itemDetails = surgeries[row.rowIndex - 1].kit_availability;

	if (itemDetails) {
		document.getElementById("avDialogDetails").style.display = 'block';
		document.getElementById("avDialogIssued").style.display = 'none';
		for (var i=0; i < itemDetails.length; i++) {
			var item = itemDetails[i];
			var templateRow = table.rows[1];
			var r = templateRow.cloneNode(true);
			r.style.display = '';
			table.tBodies[0].insertBefore(r, templateRow);

			setNodeText(r.cells[0], item.medicine_name, 30);
			setNodeText(r.cells[1], item.req_qty);
			setNodeText(r.cells[2], item.avbl_qty);
			var img = r.cells[3].getElementsByTagName('img')[0];
			if (item.req_qty > item.avbl_qty) {
				img.src = cpath + '/images/red_flag.gif';
			} else {
				img.src = cpath + '/images/empty_flag.gif';
			}
		}

	} else {
		document.getElementById("avDialogDetails").style.display = 'none';
		document.getElementById("avDialogIssued").style.display = 'block';
	}

	YAHOO.util.Dom.addClass(row, 'editing');
	avDialog.cfg.setProperty("context", [row.cells[7], "tr", "bl"], false);
}

function avDialogShow(e) {
	YAHOO.util.Event.preventDefault(e);

	document.getElementById("avDialog").style.display = 'block';
	var btn = YAHOO.util.Event.getTarget(e);
	showAvailability(getThisRow(btn));
	avDialog.show();
}

function avDialogHide() {
	avDialog.hide();
	if (gCurrentRow)
		YAHOO.util.Dom.removeClass(gCurrentRow, 'editing');
	gCurrentRow = null;
}

function avDialogNext() {
	var table = document.getElementById("resultTable");
	var len = table.rows.length;
	if (gCurrentRow.rowIndex < len-1) {
		showAvailability(table.rows[gCurrentRow.rowIndex + 1]);
	}
}

function avDialogPrev() {
	var table = document.getElementById("resultTable");
	var len = table.rows.length;
	if (gCurrentRow.rowIndex > 1) {
		showAvailability(table.rows[gCurrentRow.rowIndex - 1]);
	}
}

function init() {
	saveStockOrigQty();
	loadStores();
	setAvailability();

	YAHOO.util.Event.addListener("storeSelect", "change", onChangeStore);

	avDialog = new YAHOO.widget.Dialog("avDialog", {
        width: "400px",
        visible: false,
		modal: true
	});
	avDialog.render();
	avDialog.cancelEvent.subscribe(avDialogHide);

	infoHrefs = document.getElementById('resultTable').getElementsByTagName('A');
	YAHOO.util.Event.addListener(infoHrefs, "click", avDialogShow);

	YAHOO.util.Event.addListener("avDialogCloseBtn", "click", function() {avDialogHide()});
	YAHOO.util.Event.addListener("avDialogNextBtn", "click", function() {avDialogNext()});
	YAHOO.util.Event.addListener("avDialogPrevBtn", "click", function() {avDialogPrev()});

	createToolbar(toolbar);
}

YAHOO.util.Event.onDOMReady(init);

function refreshItemStock(){
	var storeStock = stock[document.getElementById("storeSelect").value];
	if (!storeStock) {
		storeStock = {};
	}

	for (var i=0; i<surgeries.length; i++) {
		var s = surgeries[i];

		s.kit_availability = [];		// for saving the kit availability details for this surgery
		var kitId = s.kit_id;
		var kitItems = kitDetails[kitId];

		for (var k=0; k<kitItems.length; k++) {
			var kitItem = kitItems[k];
			var medicineId = kitItem.kit_item_id;

			// ensure we have a record for the stock
			if (!storeStock[medicineId]) {
				storeStock[medicineId] = { qty:0, available:0 };
			}

			 storeStock[medicineId] = { qty:storeStock[medicineId].qty, available:storeStock[medicineId].qty };

		}

	}
}