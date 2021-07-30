// The following function is used in sales, stock transfer and transfer indent screens
// to fetch store medicines.

function getMedicinesForStore(storeIdObj, onCompletionFunction) {
	// get the medicine time stamp for this store: required for fetching the items.
	var storeId = storeIdObj.value;
	var url = cpath + "/stores/utils.do?_method=getStoreStockTimestamp&storeId=" + storeId;

	YAHOO.util.Connect.asyncRequest('GET', url, {
			success: function(response) {
				if (response.status != 200)
					return;

				var ts = parseInt(response.responseText);
				var url = cpath + "/stockdetails/getstockinstore.json? ts=" + ts +
					storeMedicineAjaxUrlParamQueryStr +
					"&storeId=" + storeId;

				// Note that since this is a GET, the results could potentially come from the browser cache.
				// This is desirable. That's why the sequence of request parameters must match the original
				// <script> in the jsp.
				YAHOO.util.Connect.asyncRequest('GET', url, { success: onGetStoreStock, argument: response.argument });
			},
			argument: onCompletionFunction,
		}
	);
}

function onGetStoreStock(response) {
	if (response.status != 200)
		return;

	eval(response.responseText);		// response is like var jMedicineNames = [...];
	// overwrite the global object.
	window.jMedicineNames = jMedicineNames;

	// the function to be called after the fetch.
	var completionFunction = response.argument;
	if (completionFunction)
		completionFunction();
}