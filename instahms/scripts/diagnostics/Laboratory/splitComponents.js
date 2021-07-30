function getTestSectionHeadHTML(conductionCenterName, outsourceDestinationID, isCheckBoxVisible) {
	var checkBoxClass = "split-sample-section-checkbox",
		checkBoxIDHTML = "";
	if (outsourceDestinationID) {
		if (conductionCenterName === 'UNDECIDED') {
			outsourceDestinationID = -2;
			isCheckBoxVisible = false;
		}
		checkBoxIDHTML = 'id = "' + outsourceDestinationID + '"';
	}
	if (!isCheckBoxVisible) {
		checkBoxClass += " hidden";
	}

	var testSectionHeadTemplate = '<div class="card-details-element card-details-element-header">'
								+ '<div class="left-column">'
								+ '<input type="checkbox" ' + checkBoxIDHTML + 'class="' + checkBoxClass + '"/>'
								+ '<h2 class="card-details-row-element"><b style="padding-right: 4px">' + conductionCenterName + '</b></h2>'
								+ '<h2 class="card-details-row-element">Conduction Center</h2>'
								+ '</div>'
								+ '<h2 class="card-details-row-element">Department</h2>'
								+ '</div>';

	return testSectionHeadTemplate;
}

function getTestSectionElementHTML(testData, isCheckBoxVisible) {
	var checkBoxClass = "split-sample-section-checkbox split-sample-element-checkbox",
		testName = testData.testName,
		prescribedID = testData.prescribedID, 
		departmentName = testData.deptName,
		outsourceDestinationID = testData.outsourceDestinationID;

	if (!isCheckBoxVisible) {
		checkBoxClass += " hidden";
	}
	if (!outsourceDestinationID) {
		outsourceDestinationID = -1;
	}
	var testSectionListTemplate = '<div class="card-details-element">'
								+ '<div class="left-column">'
								+ '<input type="checkbox" name="splitSamplePrescID" value="' + prescribedID + '" class="'
								+ checkBoxClass + '"'
								+ 'id = "' + outsourceDestinationID + '-' + prescribedID + '"/>'
								+ '<h2 class="test-name card-details-row-element truncate">' + testName + '</h2>'
								+ '</div>'
								+ '<h2 class="test-department card-details-row-element truncate">' + departmentName + '</h2>'
								+ '</div>';

	return testSectionListTemplate;
}

function getTestSectionHTML(testList, destination, isCheckBoxVisible) {
	var testSection = "";
	for (var i = 0; i < testList.length; i++) {
		if (destination === 'UNDECIDED') {
			testList[i].outsourceDestinationID = -2;
		}
		testSection += getTestSectionElementHTML(testList[i], isCheckBoxVisible);
	}

	return testSection;
}

function getCardHeadHTML(sampleNo, sampleKind) {
	var cardHeadHTML = '<div class="card-header">'
					 + '<h2>'
					 + 'Tests in <span class="sample-no">' + sampleNo + '</span><span class="normal-font-weight"> (' + sampleKind + ')</span>'
					 + '</h2>'
					 + '</div>';

	return cardHeadHTML;
}

function getCardBottomSectionHTML(sampleNo, sampleKind, visitId) {
	var splitSampleButtonHTML = "";
	var printBarcodeButtonHTML = '<button type="button" class = "lab-receive-button right-button print-barcode" id="' + sampleNo + '-' + visitId + '"'
							   + 'target=\'_blank\'>'
							   + 'Sample Label Print'
							   + '</button>';

	if (sampleKind === "Parent") {
		splitSampleButtonHTML = '<button type="button" class = "lab-receive-button" id="splitSample" disabled>'
							  + 'Split Sample'
							  + '</button>';
	}

	var cardBottomSectionHTML = splitSampleButtonHTML + printBarcodeButtonHTML;
	return cardBottomSectionHTML;
}

function getParentSampleCardHTML(parentSampleDetailsJSON) {
	var sampleDetailsCardHTML = '',
		testsInSampleJSON = parentSampleDetailsJSON.tests;
	for (var destination in testsInSampleJSON) {
		var outsourceDestinationID = ((testsInSampleJSON[destination])[0]).outsourceDestinationID;
		if (typeof outsourceDestinationID === 'undefined') {
			outsourceDestinationID = -1;
		}

		sampleDetailsCardHTML += ('<div class="card-details">'
			+ getTestSectionHeadHTML(destination, outsourceDestinationID, true) 
			+ getTestSectionHTML(testsInSampleJSON[destination], destination, true)
			+ '</div>');
	}
	
	sampleDetailsCardHTML = getCardHeadHTML(parentSampleDetailsJSON.sampleNo, "Parent") + sampleDetailsCardHTML;
	
	return sampleDetailsCardHTML;
}

function getParentSampleDetailsHTML(parentSampleDetailsJSON, visitId) {
	if (!jQuery.isEmptyObject(parentSampleDetailsJSON)) {
		var sampleDetailsHTML = ('<div id = "sample-no-' + parentSampleDetailsJSON.sampleNo + '">'
				+ '<div class="card" id="parent-sample-details">' 
				+ getParentSampleCardHTML(parentSampleDetailsJSON) 
				+ '</div>'
				+ '<div class="card-bottom">' 
				+ getCardBottomSectionHTML(parentSampleDetailsJSON.currSampleNo, "Parent", visitId) 
				+ '</div>'
				+ '</div>');

		return sampleDetailsHTML;
	}
}

function getChildSampleDetails(childSamplesJSONList, visitId) {
	var childSampleHTML = "";
	if (typeof childSamplesJSONList !== "undefined") {
		childSamplesJSONList.forEach(function(childSample, index) {
			var childSampleTestsList = [];
			for (var i = 0;i < childSample.testName.length;i++) {
				var childSampleObj = {};
				childSampleObj.testName = (childSample.testName)[i];
				childSampleObj.deptName = (childSample.deptName)[i];
				childSampleObj.prescribedID = "";
				childSampleTestsList.push(childSampleObj);
			}

			childSampleHTML += ('<div class="card">'
				+ getCardHeadHTML(childSample.sampleNo, "Child")
				+ '<div class="card-details"">'
				+ getTestSectionHeadHTML(childSample.destination, null, false)
				+ getTestSectionHTML(childSampleTestsList, "", false)
				+ '</div></div>'
				+ '<div class="card-bottom">' + getCardBottomSectionHTML(childSample.sampleNo, "Child", visitId)
				+ '</div>');
		});
	}

	return childSampleHTML;
}

function populateTestsInConfirmationDialog(testNameList) {
	var testListHTML = '';
	testNameList.forEach(function(testName, index) {
		testListHTML += ('<p class="dialog-content-list-item">' + (index + 1) + ". " + testName + '</p>');
	});

	return testListHTML;
}
