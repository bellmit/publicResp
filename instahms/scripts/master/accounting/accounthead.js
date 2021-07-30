function checkSelected() {
	var checkBoxes = document.getElementsByName("accountHead");
	for (var i in checkBoxes) {
		var box = checkBoxes[i];
		if (box.checked) {
			return true;
		}
	}
	alert("Please select atleast one Account Head");
	return false;
}

function dashboard(contextPath) {
	window.location.href = contextPath+"/master/Accounting/AccountHeads.do?method=list";
}
