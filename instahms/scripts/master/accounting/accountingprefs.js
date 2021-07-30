function dashboard(contextPath) {
	window.location.href = contextPath +"/master/Accounting/AccountingPrefs.do?method=show";
}

function validatePartyAccounts() {
	var els = document.forms[0].elements;
	var len = 0;
	for (var i=0; i<els.length; i++) {
		var el = els[i];
		if(el.type == 'radio' && el.checked && el.value == 'N') {
			var idStartsWith = (el.id).split("_")[0];
			var accNameEl = document.getElementById(idStartsWith + "_ac_name");

			if (accNameEl.value == '') {
				addClassName(accNameEl, 'validation-failed');
				removeClassName(accNameEl, 'validation-passed');
				len++;

			} else {
				addClassName(accNameEl, 'validation-passed');
				removeClassName(accNameEl, 'validation-failed');
			}
		}
	}
	if (len > 0) {
		alert("Please enter party account name. check the items in red.");
		return false;
	}
	return true;

}

function validateAccountHeads() {
	var form = document.categoryForm;
	var categoryId = form.category_id.value;
	if (categoryId == '') {
		alert("Please select the Category");
		form.category_id.focus();
		return false;
	}
	var vatAccountHead = form.purchases_cat_vat_account_prefix.value;
	var cstAccountHead = form.purchases_cat_cst_account_prefix.value;
	if (vatAccountHead == '') {
		alert("Please enter VAT Account Head");
		form.purchases_cat_vat_account_prefix.focus();
		return false;
	}
	if (cstAccountHead == '') {
		alert("Please enter CST Account Head");
		form.purchases_cat_cst_account_prefix.focus();
		return false;
	}
	return true;
}