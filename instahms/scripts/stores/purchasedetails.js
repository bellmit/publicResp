/*
 * Functions (common) to show purchase details dialog. Used in conjunction 
 * with pages/stores/PurchaseDetails.jsp (included)
 */

var gPurchaseDetails = {};
var purchaseDialog;
var purchaseDialogOnCloseFocusElement = null;

function initPurchaseDetailsDialog(onCloseFocusElement) {
    purchaseDialog = new YAHOO.widget.Dialog("purchaseDialog", {
        width: "800px",
		fixedcenter: true,
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    var escKeyListener = new YAHOO.util.KeyListener("purchaseDialog", {keys: 27 },
			purchaseDialogClose);
    purchaseDialog.cfg.queueProperty("keylisteners", escKeyListener);
    purchaseDialog.render();
	purchaseDialogOnCloseFocusElement = onCloseFocusElement;
}

function showPurchaseDetails(medicineId, storeId) {

	// purchase details not in our cache, get it using ajax
	var url = cpath + '/stores/stockentry.do?_method=getPurchaseDetails&itemId=' + medicineId+'&storeId='+storeId;
	YAHOO.util.Connect.asyncRequest('GET', url, {
       	success: function(response) {
			eval('var purchase =' + response.responseText);
			gPurchaseDetails[medicineId] = purchase;
			showPurchaseDialog(purchase);
		}
    });
}

function showPurchaseDialog(pur) {
	var table = document.getElementById("purchaseDialogTable");
	var len = table.rows.length;
    for (var p = len - 2; p > 0; p--) {
		// delete all except last (template) and first (header) rows
        table.deleteRow(p);
    }

	if (pur == null || pur.length == 0) {
		document.getElementById('purchaseDialogExisting').style.display = 'none';
		document.getElementById('purchaseDialogNoStock').style.display = 'block';

	} else {

		document.getElementById('purchaseDialogExisting').style.display = 'block';
		document.getElementById('purchaseDialogNoStock').style.display = 'none';

		for (var i=0; i<pur.length; i++) {
			var templateRow = table.rows[1];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);

			for (var c=0; c<row.cells.length; c++) {
				// the cells are like <td name="mrp"> etc.
				var cell = row.cells[c];
				var name = cell.getAttribute('name');
				if (name && (pur[i][name] != null)) {
					if (YAHOO.util.Dom.hasClass(cell, "amount"))
						setNodeText(cell, formatAmountValue(pur[i][name]));
					else
						setNodeText(cell, pur[i][name]);
				}
			}
		}
	}

	document.getElementById("purchaseDialog").style.display = "block";
	purchaseDialog.show();
	document.getElementById("purchaseDialogCloseBtn").focus();
}

function onPurchaseDialogClose() {
	purchaseDialogClose();
}

function purchaseDialogClose() {
	purchaseDialog.cancel();
	if (purchaseDialogOnCloseFocusElement)
		purchaseDialogOnCloseFocusElement.focus();
}

