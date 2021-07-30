
	 var toolbar = {
			Types : {
				title : 'Edit Types',
				imageSrc : 'icons/Edit.png',
				href : '/master/consultCharges.do?_method=editTypes',
				onclick : null,
				description : 'View and/or Edit Consultation Types Details'
			},
			Charges : {
				title : 'Edit Charges',
				imageSrc : 'icons/Edit.png',
				href : '/master/consultCharges.do?_method=edit',
				onclick : null,
				description : 'View and/or Edit Consultation Chagre Details'
			}
		}

		function doUpload(formName) {
			if (formName == 'consDetailsUpload') {
				var form = document.consDetailsUpload;
				if (document.consDetailsUpload.xlsConsultaionDetails.value == '') {
					alert('Please select a file to upload');
					return false;
				}
			document.consDetailsUpload.orgId.value =
								document.ConsultationChargesForm.org_id.value;
			} else {
				var form = document.DetailsUpload;
				if (document.DetailsUpload.xlsConsultaionCharges.value == '') {
					alert('Please select a file to upload');
					return false;
				}
				document.DetailsUpload.orgId.value =
								document.ConsultationChargesForm.org_id.value;
			}
			form.submit();
		}

		function doExport() {
			document.DetailsForm.orgId.value =
							document.ConsultationChargesForm.org_id.value;
			return true;
		}

		function onChangeAllConsultations() {
			var val = getRadioSelection(document.updateform.allConsultations);
			// if allConsultations = yes, then disable the page selections
			var disabled = (val == 'yes');

			var consultationform = document.ConsultationChargesForm;
			consultationform.allPageConsultations.disabled = disabled;
			consultationform.allPageConsultations.checked = false;

			var length = consultationform.selectConsultation.length;

			if (length == undefined) {
				consultationform.selectConsultation.disabled = disabled;
				consultationform.selectConsultation.checked  = false;
			} else {
				for (var i=0;i<length;i++) {
					consultationform.selectConsultation[i].disabled = disabled;
					consultationform.selectConsultation[i].checked = false;
				}
			}
	}

	function selectAllPageConsultations() {
		var checked = document.ConsultationChargesForm.allPageConsultations.checked;
		var length = document.ConsultationChargesForm.selectConsultation.length;

		if (length == undefined) {
			document.ConsultationChargesForm.selectConsultation.checked = checked;
		} else {
			for (var i=0;i<length;i++) {
				document.ConsultationChargesForm.selectConsultation[i].checked = checked;
			}
		}
	}

	function doGroupUpdate() {

		var updateform = document.updateform;
		var ConsultationChargesForm = document.ConsultationChargesForm;
		updateform.org_id.value = document.ConsultationChargesForm.org_id.value;

		var anyConsultation = false;
		var allConsultations = getRadioSelection(document.updateform.allConsultations);
		if (allConsultations == 'yes') {
			anyConsultation = true;
		} else {
			var div = document.getElementById("equipmentListInnerHtml");
			while (div.hasChildNodes()) {
				div.removeChild(div.firstChild);
			}
			var length = ConsultationChargesForm.selectConsultation.length;
			if (length == undefined) {
				if (ConsultationChargesForm.selectConsultation.checked ) {
					anyConsultation = true;
					div.appendChild(makeHidden("selectConsultation", "", ConsultationChargesForm.selectConsultation.value));
				}
			} else {
				for (var i=0;i<length;i++) {
					if (ConsultationChargesForm.selectConsultation[i].checked){
						anyConsultation = true;
						div.appendChild(makeHidden("selectConsultation", "", ConsultationChargesForm.selectConsultation[i].value));
					}
				}
			}
		}

		if (!anyConsultation) {
			alert('Select at least one Consultation for updation');
			return;
		}

		var anyBedTypes = false;
		if (updateform.allBedTypes.checked) {
			anyBedTypes = true;
		} else {
			var bedTypeLength = updateform.selectBedType.length;

			for (var i=0; i<bedTypeLength ; i++) {
				if(updateform.selectBedType.options[i].selected){
					anyBedTypes = true;
					break;
				}
			}
		}

		if (!anyBedTypes) {
			alert('Select at least one Bed Type for updation');
			return ;
		}

		if (!updateOption()) {
			alert("Select any update option");
			updateform.updateTable[0].focus();
			return ;
		}


		if (updateform.amount.value=="") {
			alert("Value required for Amount");
			updateform.amount.focus();
			return ;
		}

		if(updateform.amtType.value == '%') {
			if(getAmount(updateform.amount.value) > 100){
				alert("Discount percent cannot be more than 100");
				updateform.amount.focus();
				return false;
			}
		}

		updateform.submit();
}

	function updateOption() {
		for (var i=0; i<updateform.updateTable.length ; i++) {
			if(updateform.updateTable[i].checked){
				return true;
			}
		}
		return false;
	}
	function deselectAllBedTypes(){
		document.updateform.allBedTypes.checked = false;
	}
	function selectAllBedTypes(){
		var selected = document.updateform.allBedTypes.checked;
		var bedTypesLen = document.updateform.selectBedType.length;

		for (i=bedTypesLen-1;i>=0;i--) {
			document.updateform.selectBedType[i].selected = selected;
		}
	}