function clearSearch() {
	var tpaNames = document.forms[0].tpaNames;
	for (var i=0; i<tpaNames.length; i++) {
		if (tpaNames[i].selected) {
			tpaNames[i].selected = false;
		}
	}
	document.forms[0].fromDate.value = '';
	document.forms[0].toDate.value = '';
}