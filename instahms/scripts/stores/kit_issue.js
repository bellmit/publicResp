
function autoFillKitItems(){
	var dialogIndex = 0;
	for (var i = 0;i<kitDetails.length;i++){
		dialogIndex = dialogIndex+1;
		var editButton = document.getElementById("add"+parseFloat(dialogIndex));
		var eBut =  document.getElementById("addBut"+parseFloat(dialogIndex));
		editButton.setAttribute("src",popurl+'/icons/Edit.png');
		eBut.setAttribute("title", "Edit Item");
		eBut.setAttribute("accesskey", "");
		var isEdit =  document.getElementById("isEdit"+parseFloat(dialogIndex));
		isEdit.value = 'Y';

		addToInnerHTML(kitDetails[i].medicine_name, '',document.stocktransferform.store.value,
						document.stocktransferform.to_store.value,kitDetails[i].qty,'',
						"",'', '', '','','','');
	}
}