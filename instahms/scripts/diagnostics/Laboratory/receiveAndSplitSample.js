$(document).ready(function() {
	/*code related to confirmation dialog box.*/
	var splitSampleConfirmationDialog = document.getElementById('split-sample-confirmation-dialog');
	splitSampleConfirmationDialog.style.display = 'block';
	splitSampleConfirmationDialog = new YAHOO.widget.Dialog("split-sample-confirmation-dialog", {
		width : "760px",
		visible : false,
		context :["splitSample", "tl", "tr"],
		modal : true,
		constraintoviewport : true
	});
	YAHOO.util.Event.addListener("split-abort", "click",
			closeDialog, splitSampleConfirmationDialog, true);
	subscribeKeyListeners(splitSampleConfirmationDialog, 'custom');
	splitSampleConfirmationDialog.render();

	/*setting right pane height equal to left pane height*/
	var leftPaneHeight = parseInt($('.sample-meta-details').height(), 10);
	var sampleSpecificDetailsHeaderHeight = parseInt($('.sample-specific-details-header').height(), 10);
	$('#sample-specific-details-body').height(leftPaneHeight - sampleSpecificDetailsHeaderHeight);

	/*showing left form submit buttons when records are retrieved*/
	if (!jQuery.isEmptyObject(sampleDetailsDataMap)) {
		$('.form-submit-option').removeClass('hidden');
	}
	
    $('.sample-meta-details-list-item').on('click', function () {
		if ($('.rowbgToolBar').length == 0) {
			$('.hover-state-color').removeClass('hover-state-color');
			$('.selected-sample').removeClass('selected-sample');
			$(this).addClass('selected-sample');
			var id = $(this).attr('id');
			id = id.replace("toolbarRow","");
            var outsourceDestinationIDs = sampleDetailsDataMap[id].parentSample.outsourceDestinationIDs;
			var sampleType = sampleDetailsDataMap[id].parentSample.sampleType;
			$('#patient-name').text(sampleDetailsDataMap[id].parentSample.patientName);
			$('#source-center').text(sampleDetailsDataMap[id].parentSample.sourceCenter);
			$('#transferred-date').text(sampleDetailsDataMap[id].parentSample.transferDateTime);
			var mrNoWithSponsorType = "-";
			if (sampleDetailsDataMap[id].parentSample.mrNo.length !== 0) {
				mrNoWithSponsorType =  sampleDetailsDataMap[id].parentSample.mrNo
								+ " (" + sampleDetailsDataMap[id].parentSample.sponsorType + ")";
			}
			$('#mr-no').text(mrNoWithSponsorType);
			$('#visit-id').text(sampleDetailsDataMap[id].parentSample.visitID);
			$('#collection-center').text(sampleDetailsDataMap[id].parentSample.collectionCenter);
			$('#transfer-batch-id').text(sampleDetailsDataMap[id].parentSample.transferBatchId);
			$('#collection-date').text(sampleDetailsDataMap[id].parentSample.collectionDateTime);
			$('#age-gender').text(sampleDetailsDataMap[id].parentSample.ageWithUnit + 
				" / " + sampleDetailsDataMap[id].parentSample.patientGender);
			$('#sample-type').text(sampleType);
			$('#sample-no').text(sampleDetailsDataMap[id].parentSample.sampleNo);
			$('#parent_sample_field').val(sampleDetailsDataMap[id].parentSample.sampleNo);
			$('#parent-sample-card').html(getParentSampleDetailsHTML(sampleDetailsDataMap[id].parentSample, sampleDetailsDataMap[id].visitID));
			$('#child-samples').html(getChildSampleDetails(sampleDetailsDataMap[id].childSamples, sampleDetailsDataMap[id].visitID));
			var isSampleSplittable = sampleDetailsDataMap[id].parentSample.isSplittable;
			var totalDestinations = sampleDetailsDataMap[id].parentSample.totalDestinations
			$('#total_destinations_field').val(totalDestinations);
			$('#outsource_dest_id_field').removeData("totalChecks");
			
			Array.from($('.truncate > span')).forEach(function (truncatedElement, index) {
				$(truncatedElement).attr('title', $(truncatedElement).text());
			});
			Array.from($('.card-details-element .truncate')).forEach(function (truncatedElement, index) {
				$(truncatedElement).attr('title', $(truncatedElement).text());
			});
			
			$('#deduct_total_destinations_field').val(false);
			var normalCheckBoxDOM = $('.card-details-element:not(.card-details-element-header) input:not(.hidden)'),
				masterCheckBoxDOM = $('.card-details-element-header input:not(.hidden)');

			function isChildOfMasterCheckBox(testCheckBoxDOM, checkBoxID) {
				var testCheckBoxID = $(testCheckBoxDOM).attr('id'),
				    childOfMasterCheckBox = (testCheckBoxID.indexOf(checkBoxID) === 0);

				return childOfMasterCheckBox;
			}
			
			$('.print-barcode').on('click', function() {
				var printBarcodeButtonDOM = $(this),
					barcodeData = printBarcodeButtonDOM.attr('id').split('-'),
					sampleNo = barcodeData[0],
					visitId = barcodeData[1],
					printBarcodeLink;
				var templateName = $('#sample-barcode-template-selection').val();
				printBarcodeLink = cpath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionReport&visitid="
						+visitId+"&sampleNo='"+sampleNo+"'&template_name="+templateName
						+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType;
				if(samplePrintType !== "SL") {	
					printBarcodeLink = cpath + "/Laboratory/GenerateSamplesBarCodePrint.do?method=execute";
					printBarcodeLink += '&visitId=' + visitId;
					printBarcodeLink += '&sampleNo=\'' + sampleNo + '\'';
					printBarcodeLink += '&template_name=' + templateName;
				}
				window.open(printBarcodeLink);
			});

			var totalTests = 0,
				selectedPrescribedIDs = [];
			normalCheckBoxDOM.on('click', function() {
                if (isSampleSplittable) {
                    var checkBoxDOM = $(this),
                        checkBoxID = checkBoxDOM.attr('id'),
                        outsourceIDInputDOM = $('#outsource_dest_id_field'),
                        splitSampleDialogContentDOM = $('#split-sample-dialog-content');
                    if (checkBoxID) {
                        var totalChecks = outsourceIDInputDOM.data("totalChecks"),
                        	prescribedIDSelected = checkBoxDOM.val(),
                            outsourceDestinationID;
                        if (checkBoxID[0] !== '-') {
                            outsourceDestinationID = parseInt(checkBoxID.split("-")[0], 10);
                        } else {
                            checkBoxID = checkBoxID.substring(1);
                            outsourceDestinationID = -1 * parseInt(checkBoxID.split("-")[0], 10);
                        }
                        if (checkBoxDOM.prop('checked')) {
                            if (typeof totalChecks === 'undefined') {
                                totalTests = 0;
                                totalChecks = 0;
                                outsourceIDInputDOM.val(outsourceDestinationID);
                                for (var i = 0; i < normalCheckBoxDOM.length; i++) {
                                    var testCheckBoxDOM = normalCheckBoxDOM[i],
                                        testCheckBoxCheckStatus = $(testCheckBoxDOM).prop('checked');

                                    if (!isChildOfMasterCheckBox(testCheckBoxDOM, "" + outsourceDestinationID)) {
                                        $(testCheckBoxDOM).prop('disabled', !testCheckBoxCheckStatus);
                                    } else {
                                        if (outsourceDestinationID === -2) {
                                            $(testCheckBoxDOM).prop('disabled', !testCheckBoxCheckStatus);
                                        }
                                        totalTests = totalTests + 1;
                                    }
                                }

                                for (var i = 0; i < masterCheckBoxDOM.length; i++) {
                                    var masterCheckBoxElementDOM = masterCheckBoxDOM[i],
                                        masterCheckBoxElementID = parseInt($(masterCheckBoxElementDOM).attr('id'), 10);
                                    if (masterCheckBoxElementID != outsourceDestinationID) {
                                        $(masterCheckBoxElementDOM).prop('disabled', true);
                                    }
                                }
                                $('#splitSample').prop('disabled', false);
                            }
                            totalChecks = totalChecks + 1;
                            if ((totalChecks === totalTests) || (outsourceDestinationID === -2)) {
                                if (totalDestinations === 1) {
                                    $('#splitSample').prop('disabled', true);
                                }
                                $('#deduct_total_destinations_field').val(true);
                            }
                            outsourceIDInputDOM.data("totalChecks", totalChecks);
                            selectedPrescribedIDs.push(prescribedIDSelected);
                        } else {
                            if (totalChecks === 1) {
                                outsourceIDInputDOM.val("");
                                outsourceIDInputDOM.removeData("totalChecks");
                                for (var i = 0; i < normalCheckBoxDOM.length; i++) {
                                    $(normalCheckBoxDOM[i]).prop('disabled', false);
                                }

                                for (var i = 0; i < masterCheckBoxDOM.length; i++) {
                                    $(masterCheckBoxDOM[i]).prop('disabled', false);
                                }
                                $('#splitSample').prop('disabled', true);
                            } else {
                                totalChecks = totalChecks - 1;
                                if ($('#splitSample').prop('disabled')) {
                                    $('#splitSample').prop('disabled', false);
                                }
                                outsourceIDInputDOM.data("totalChecks", totalChecks);
                            }
                            $('#deduct_total_destinations_field').val(false);
                            $('#' + outsourceDestinationID).prop('checked', false);
                            var indexOfTestToRemove = selectedPrescribedIDs.indexOf(prescribedIDSelected);
                            if (indexOfTestToRemove > -1) {
                            	selectedPrescribedIDs.splice(indexOfTestToRemove, 1);
                            } 
                        }
                    }
                }
			});

			masterCheckBoxDOM.on('click', function() {
                if (isSampleSplittable) {
    				var checkBoxDOM = $(this),
    					checkBoxID = checkBoxDOM.attr('id');
    				if (checkBoxID) {
    					var masterCheckBoxCheckStatus = checkBoxDOM.prop('checked');
    					for (var i = 0; i < normalCheckBoxDOM.length; i++) {
    						var testCheckBoxDOM = normalCheckBoxDOM[i],
    						    testCheckBoxCheckStatus = $(testCheckBoxDOM).prop('checked');
    						if (isChildOfMasterCheckBox(testCheckBoxDOM, checkBoxID)) {
    							if (masterCheckBoxCheckStatus !== testCheckBoxCheckStatus) {
    								$(testCheckBoxDOM).click();
    							}
    						}
    					}

    					for (var i = 0; i < masterCheckBoxDOM.length; i++) {
    						if (checkBoxID !== $(masterCheckBoxDOM[i]).attr('id')) {
    							$(masterCheckBoxDOM[i]).prop('disabled', masterCheckBoxCheckStatus);
    						}
    					}
    				}
                }
			});
			
			function showConfirmationDialog() {
				var testNameSelected = [];
				for (var i = 0;i < selectedPrescribedIDs.length; i++) {
					var prescribedID = selectedPrescribedIDs[i];
					testNameSelected.push(prescribedIDTestNameMap[prescribedID]);
				}
				$('#test-list').html(populateTestsInConfirmationDialog(testNameSelected));
				splitSampleConfirmationDialog.show();
			}
			
			$('#splitSample').on('click', function() {
                var parentOutsourceDestinationID;
                if (outsourceDestinationIDs.length === 2) {
                    var childOutsourceDestinationID = parseInt($('#outsource_dest_id_field').val(), 10);
                    parentOutsourceDestinationID = ((childOutsourceDestinationID === outsourceDestinationIDs[0]) ?
                                                    outsourceDestinationIDs[1] : outsourceDestinationIDs[0]);
                }
				document.splitSampleForm.action = cpath + "/Laboratory/ReceiveAndSplitSamples.do?_method=splitSample";
                if (typeof parentOutsourceDestinationID !== 'undefined') {
                    document.splitSampleForm.action += "&parent_outsource_destination_id=" + parentOutsourceDestinationID;
                }
				showConfirmationDialog();
			});

			$('#split-confirm').on('click', function() {
				document.splitSampleForm.submit();
			});

			$('#sample-specific-details-body').scrollTop(0);
			$('form[name="splitSampleForm"]').removeClass('hidden');
		}
	});
	
	$('.sample-meta-details-list-item').hover(function () {
		if (!$(this).hasClass('selected-sample') && !$(this).hasClass('rowbgToolBar')) {
			$(this).children().addClass('hover-state-color');
		} else {
			$(this).children().addClass('hover-state-selected-color');
		}
	}, function () {
		if (!$(this).hasClass('selected-sample') && !$(this).hasClass('rowbgToolBar')) {
			$(this).children().removeClass('hover-state-color');
		} else {
			$(this).children().removeClass('hover-state-selected-color');
		}
	});
	
	$('#master-check-box').prop('disabled', !isPendingSampleInCurrentPage());
	
	function isPendingSampleInCurrentPage() {
		var inputDOMList = $('.checkbox-td > input');
		for(var i = 0;i < inputDOMList.length;i++) {
			if(!inputDOMList.eq(i).hasClass('hidden')) {
				return true;
			}
		}
		return false;
	}
	
	function receiveSampleFromTextBox() {
		document.receiveSampleManualBarcode.action = cpath +"/Laboratory/ReceiveAndSplitSamples.do?" +
				"_method=saveMarkedReceivedSamples&isRequestFromCheckBox=false";
		document.receiveSampleManualBarcode.submit();
	}
	
	$('#receive').on('click', function() {
		receiveSampleFromTextBox();
	});
	
	$('#receive_form_sample_no').keypress(function(event) {
		var key = event.which || event.keyCode || 0;
		if (key === 13) {
			receiveSampleFromTextBox();
		}
	});
	
	$('#print-all').on('click', function() {
		window.open(cpath+"/Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet" +
				"&sampleCollectionIds="+sampleCollectionIDs+"&bulkWorkSheetPrint=Y");
	});
	
	$('#receive_form_sample_no').on('keyup', function() {
		var receiveFormInput = $('#receive_form_sample_no').val();
		$('#receive').prop('disabled', (receiveFormInput.length === 0));
	})
	
	if (!jQuery.isEmptyObject(sampleDetailsDataMap)) {
		$('#toolbarRow0').click();
	}
});

function toggleCheck(checkboxObject, index) {
	var isCheckBoxChecked = $(checkboxObject).prop('checked');
	var rowDOM = $('#toolbarRow' + index);
	if (isCheckBoxChecked === true) {
		if ($('#markReceived').prop('disabled') === true) {
			$('#markReceived').prop('disabled', false);
		}
		rowDOM.addClass('rowbgToolBar');
		$('#toolbarRow' + index).children().removeClass('hover-state-color');
	} else {
		rowDOM.removeClass('rowbgToolBar');
		rowDOM.removeClass('selected-sample');
		$('#toolbarRow' + index).children().removeClass('hover-state-selected-color');
	}
	
	if ($('.rowbgToolBar').length == 0) {
		$('#markReceived').prop('disabled', true);
		$('#master-check-box').prop('checked', false);
	}
}

function toggleCheckForGroup(elName, obj) {
	$('.rowbgToolBar').removeClass('rowbgToolBar');
	$('.selected-sample').removeClass('selected-sample');
	checkOrUncheckAll(elName, obj);
	$('#markReceived').prop('disabled', !obj.checked);
}

function showMoreCustom(divMoreId) {
	var divMore = document.getElementById(divMoreId);
	if (divMore.style.display == 'none') {
		// show it
		show(divMore, true);
		document.getElementById('aMore').innerHTML = getString("js.common.search.less.options");
	} else {
		// hide it
		show(divMore, false);
		document.getElementById('aMore').innerHTML = getString("js.common.search.more.options");
	}
}