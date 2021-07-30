function init() {
	initRatingDialog();
	if (!recordExists) {
		alert("please fill the rating dialog first");
		showRatingDialog();
	}
 }

 function addHeaderTableRows() {
 	var table = document.getElementById('headerQuestionTable');
	var len = table.rows.length;
	var id = len-1;

	var row = table.insertRow(-1);
	var cell = row.insertCell(-1);

	cell.innerHTML = '<label id="header_q_num' + id + '"></label>'  +
 		'<input type="hidden" name="insertOrUpdateForHeader" id="insertOrUpdateForHeader' + id + '" value="N">';

 	cell = row.insertCell(-1);

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="header_q_num_display" id="header_q_num_display' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :600px" name="header_q_description" id="header_q_description' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="header_answer_size" id="header_answer_size' + id + '" value="" onkeypress="return enterNumOnly(event)" maxlength="2">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<a title="Cancel Header Question" onclick="cancelRow(this, '+id+')" href="javascript:void(0)">' +
					  	'<img class="button" src="'+cpath+'/icons/delete.gif"></img>'+
					   '</a>';

	cell = row.insertCell(-1);
	document.getElementById('btnAddItemHeader').focus();

 }

 function addTopicTableRows() {
 	var table = document.getElementById('topicQuestionTable');
	var len = table.rows.length;
	var id = len-1;

	var row = table.insertRow(-1);
	var cell = row.insertCell(-1);

	cell.innerHTML = '<label id="topic_q_num' + id + '"></label>' +
					'<input type="hidden" name="insertOrUpdateForTopic" id="insertOrUpdateForTopic' + id + '" value="N">';

	cell = row.insertCell(-1);

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="topic_q_num_display" id="topic_q_num_display' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :150px" name="topic_name" id="topic_name' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :600px" name="topic_q_description" id="topic_q_description' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="topic_q_type" id="topic_q_type' + id + '" value="" onchange="showQuestionTypes(this)" maxlength="2">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="topic_q_remark_lines" id="topic_q_remark_lines' + id + '" value="" onkeypress="return enterNumOnly(event)" maxlength="2">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<a title="Cancel Header Question" onclick="cancelRow(this, '+id+')" href="javascript:void(0)">' +
					  	'<img class="button" src="'+cpath+'/icons/delete.gif"></img>'+
					   '</a>';

	cell = row.insertCell(-1);
	document.getElementById('btnAddItemTopic').focus();

 }

 function addSummaryTableRows() {
 	var table = document.getElementById('summaryQuestionTable');
	var len = table.rows.length;
	var id = len-1;

	var row = table.insertRow(-1);
	var cell = row.insertCell(-1);

	cell.innerHTML = '<label id="summary_q_num' + id + '"></label>' +
					 '<input type="hidden" name="insertOrUpdateForSummary" id="insertOrUpdateForSummary' + id + '" value="N">';

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="summary_q_num_display" id="summary_q_num_display' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :600px" name="summary_q_description" id="summary_q_description' + id + '" value="">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="summary_q_type" id="summary_q_type' + id + '" value="" onchange="showQuestionTypes(this)" maxlength="2">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="summary_q_overall_rating" id="summary_q_overall_rating' + id + '" value="" maxlength="1" onchange="showIsItOverallRating(this)">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" style="width :50px" name="summary_q_remark_lines" id="summary_q_remark_lines' + id + '" value="" onkeypress="return enterNumOnly(event)" maxlength="2">' ;

	cell = row.insertCell(-1);
	cell.innerHTML = '<a title="Cancel Header Question" onclick="cancelRow(this, '+id+')" href="javascript:void(0)">' +
					  	'<img class="button" src="'+cpath+'/icons/delete.gif"></img>'+
					   '</a>';


	cell = row.insertCell(-1);
	document.getElementById('btnAddItemSummary').focus();

 }

 function cancelRow(imgObj) {
 	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	row.parentNode.removeChild(row);
 }

 function cancelInsertedRow(imjObj, index, type) {
 	var deleteObj = null;
 	var table = null;
 	if (type == 'H') {
		deleteObj = document.getElementById("deleteHeaderFlag"+index);
		table = "headerQuestionTable";
	} else if (type == 'T') {
		deleteObj = document.getElementById("deleteTopicFlag"+index);
		table = "topicQuestionTable";
	} else if (type == 'S') {
		deleteObj = document.getElementById("deleteSummaryFlag"+index);
		table = "summaryQuestionTable";
	}

	var isDeleted = deleteObj.value;
	var row = null;

	if (isDeleted == "Y") {
		imjObj.src = cpath+"/icons/delete.gif";
		row = getTableRow(index,table)
		row.setAttribute("class", "");
	}else {
		imjObj.src = cpath+"/icons/undo_delete.gif";
		row = getTableRow(index,table);
		row.setAttribute("class", "cancelled");
	}

	deleteObj.value = (deleteObj.value == "Y") ? "N" : "Y";
 }

 function getTableRow(i,table) {
	i = parseInt(i);
	var table = document.getElementById(table);
	return table.rows[i + 1];
 }

 function initRatingDialog() {
	ratingDialog = new YAHOO.widget.Dialog("ratingDialog",
		{
			width:"600px",
			context : ["ratingTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
			buttons : [ { text:"OK", handler:handleRatingSubmit, isDefault:true },
						{ text:"Cancel", handler:handleRatingCancel } ]
		} );
	ratingDialog.render();
}

function showRatingDialog() {
	var button = document.getElementById('openRating');
	ratingDialog.cfg.setProperty("context",[button, "tl", "bl"], false);
	getRatingDetails();
}

function showRatingDetails() {
	if (!recordExists) {
		alert("there is no record to show");
		showRatingDialog();
		document.getElementById('rating_scale').focus();
		return false;
	} else {
		showRatingDialog();
	}
}

function handleRatingSubmit() {
	var number = document.getElementById('rating_scale').value;

	if (empty(number)) {
		alert("please enter the rating scale");
		document.getElementById('rating_scale').focus();
		return false;
	}

	if (!empty(number)&&(number < 3 || number == 4 || number == 6 || number == 8 || number == 9 || number >10)) {
		alert("please enter 3,5,7 or 10");
		document.getElementById('rating_scale').focus();
		return false;
	}
	var table = document.getElementById('ratingTable');
	var len = table.rows.length;
	if (!empty(number)) {
		for (var i=0;i<len-1;i++) {
			if (empty(document.getElementById('rating_number'+i).value)) {
				alert("please enter the rating number");
				document.getElementById('rating_number'+i).focus();
				return false;
			}

			if (empty(document.getElementById('rating_short_name'+i).value)) {
				alert("please enter the short name of rating");
				document.getElementById('rating_short_name'+i).focus();
				return false;
			}

			if (empty(document.getElementById('rating_long_name'+i).value)) {
				alert("please enter description of the rating");
				document.getElementById('rating_long_name'+i).focus();
				return false;
			}
		}
	}
	document.patientFeedbackRatingForm._method.value = "saveRatingDetails";
	document.getElementById('rating_scale_value').value = number;
	document.patientFeedbackRatingForm.submit();
}

function handleRatingCancel() {
	if (empty(ratingList)) {
		clearFields();
		deleteDummyRows();
	}
	ratingDialog.cancel();
}
function deleteDummyRows() {
	var table = document.getElementById('ratingTable');
	var len = table.rows.length;
	var id = len-1;
	if (id > 1) {
		for (var i=len-1; i>1;i--) {
			table.deleteRow(i);
		}
	}
}
function clearFields() {
	var number = document.getElementById('rating_scale').value;
	if (!empty(number)) {
		document.getElementById('rating_scale').value = '';
	}

	var table = document.getElementById('ratingTable');
	var len = table.rows.length;

	if (len > 1) {
		for (var i=0;i<len-1;i++) {
			if (!empty(document.getElementById('rating_number'+i).value)) {
				document.getElementById('rating_number'+i).value = '';
			}

			if (!empty(document.getElementById('rating_short_name'+i).value)) {
				document.getElementById('rating_short_name'+i).value = '';
			}

			if (!empty(document.getElementById('rating_long_name'+i).value)) {
				document.getElementById('rating_long_name'+i).value = '';
			}
		}
	}
}

function showRatingDialogByNo() {
	var number = document.getElementById('rating_scale').value;
	var table = document.getElementById('ratingTable');
	var len = table.rows.length;
	var id = len-1;
	if (!empty(number)&&(number == 3 || number == 5 || number == 7 || number == 10)) {
		if (id > 1) {
			clearFields();
			deleteDummyRows();
		}

		for(var i=1;i<= number;i++) {

			var row = table.insertRow(-1);
			var cell = row.insertCell(-1);

			cell.innerHTML = '<input type="text" style="width: 50px" name="rating_number" id="rating_number' + i + '" onkeypress="return enterNumOnly(event)">' +
							 '<input type="hidden" name="rating_number_disable" id="rating_number_disable' + i +'">';

			cell = row.insertCell(-1);
			cell.innerHTML = '<input type="text" style="width: 50px" name="rating_short_name" id="rating_short_name' + i + '">' ;

			cell = row.insertCell(-1);
			cell.innerHTML = '<input type="text" style="width: 300px"  name="rating_long_name" id="rating_long_name' + i + '">' ;
		}

	fillRatingValues(number);

	} else {
		alert("please enter 3,5,7 or 10");
		document.getElementById('rating_scale').focus();
		return false;
	}
}
function validateHeaderPortion() {
	var hedaerDesc1 = document.getElementById('header_line1');
	var hedaerDesc2 = document.getElementById('header_line2');
	var hedaerDesc3 = document.getElementById('header_line3');
	var topicDesc1 = document.getElementById('topic_header_line1');
	var topicDesc2 = document.getElementById('topic_header_line2');
	var topicDesc3 = document.getElementById('topic_header_line3');
	var summaryDesc1 = document.getElementById('summary_header_line1');
	var summaryDesc2 = document.getElementById('summary_header_line2');
	var summaryDesc3 = document.getElementById('summary_header_line3');

	if (hedaerDesc1 != null && hedaerDesc1.value != '') {
		if (hedaerDesc1.value.length > 1000) {
			alert("header1 description length should not be more than 1000 character");
			hedaerDesc1.focus();
			return false;
		}
	}

	if (hedaerDesc2 != null && hedaerDesc2.value != '') {
		if (hedaerDesc2.value.length > 1000) {
			alert("header2 description length should not be more than 1000 character");
			hedaerDesc2.focus();
			return false;
		}
	}

	if (hedaerDesc3 != null && hedaerDesc3.value != '') {
		if (hedaerDesc3.value.length > 1000) {
			alert("header3 description length should not be more than 1000 character");
			hedaerDesc3.focus();
			return false;
		}
	}

	if (topicDesc1 != null && topicDesc1.value != '') {
		if (topicDesc1.value.length > 1000) {
			alert("topicheader1 description length should not be more than 1000 character");
			topicDesc1.focus();
			return false;
		}
	}

	if (topicDesc2 != null && topicDesc2.value != '') {
		if (topicDesc2.value.length > 1000) {
			alert("topicheader2 description length should not be more than 1000 character");
			topicDesc2.focus();
			return false;
		}
	}

	if (topicDesc3 != null && topicDesc3.value != '') {
		if (topicDesc3.value.length > 1000) {
			alert("topicheader3 description length should not be more than 1000 character");
			topicDesc3.focus();
			return false;
		}
	}

	if (summaryDesc1 != null && summaryDesc1.value != '') {
		if (summaryDesc1.value.length > 1000) {
			alert("summaryheader1 description length should not be more than 1000 character");
			summaryDesc1.focus();
			return false;
		}
	}

	if (summaryDesc2 != null && summaryDesc2.value != '') {
		if (summaryDesc2.value.length > 1000) {
			alert("summaryheader2 description length should not be more than 1000 character");
			summaryDesc2.focus();
			return false;
		}
	}

	if (summaryDesc3 != null && summaryDesc3.value != '') {
		if (summaryDesc3.value.length > 1000) {
			alert("summaryheader3 description length should not be more than 1000 character");
			summaryDesc3.focus();
			return false;
		}
	}
	return true;
}

function checkAndValidate() {
	var tableForSummary = document.getElementById('summaryQuestionTable');
	var lenForSummary = tableForSummary.rows.length;
	var idForSummary = lenForSummary-1;

	var topicResRating = null;
	var summaOverallRating = null;
	var summaResRating = null;

	var tableForTopic = document.getElementById('topicQuestionTable');
	var lenForTopic = tableForTopic.rows.length;
	var idForTopic = lenForTopic-1;

	for (var i=0 ;i<idForSummary; i++) {
		summaOverallRating = document.getElementById('summary_q_overall_rating'+i);
		summaResRating = document.getElementById('summary_q_type'+i);

		if(!showIsItOverallRating(summaOverallRating)) {
			return false;
		}

		if(!showQuestionTypes(summaResRating)) {
			return false;
		}
	}

	for (var i=0 ;i<idForTopic; i++) {
		 topicResRating = document.getElementById('topic_q_type'+i);

		 if(!showQuestionTypes(topicResRating)) {
		 	return false;
		 }
	}

	if (!validateHeaderPortion()) {
		return false;
	}

	return true;
}

function formatHeaderData() {
	var hedaerDesc1 = document.getElementById('header_line1');
	var hedaerDesc2 = document.getElementById('header_line2');
	var hedaerDesc3 = document.getElementById('header_line3');
	var topicDesc1 = document.getElementById('topic_header_line1');
	var topicDesc2 = document.getElementById('topic_header_line2');
	var topicDesc3 = document.getElementById('topic_header_line3');
	var summaryDesc1 = document.getElementById('summary_header_line1');
	var summaryDesc2 = document.getElementById('summary_header_line2');
	var summaryDesc3 = document.getElementById('summary_header_line3');
	var maxlength = 88;

	if (hedaerDesc1 != null && hedaerDesc1.value != '') {
		if (hedaerDesc1.value.length > maxlength) {
			var retValue = formatData(hedaerDesc1,maxlength);
			hedaerDesc1.value = retValue;
		}
	}

	if (hedaerDesc2 != null && hedaerDesc2.value != '') {
		if (hedaerDesc2.value.length > maxlength) {
			var retValue = formatData(hedaerDesc2,maxlength);
			hedaerDesc2.value = retValue;
		}
	}

	if (hedaerDesc3 != null && hedaerDesc3.value != '') {
		if (hedaerDesc3.value.length > maxlength) {
			var retValue = formatData(hedaerDesc3,maxlength);
			hedaerDesc3.value = retValue;
		}
	}

	if (topicDesc1 != null && topicDesc1.value != '') {
		if (topicDesc1.value.length > maxlength) {
			var retValue = formatData(topicDesc1,maxlength);
			topicDesc1.value = retValue;
		}
	}

	if (topicDesc2 != null && topicDesc2.value != '') {
		if (topicDesc2.value.length > maxlength) {
			var retValue = formatData(topicDesc2,maxlength);
			topicDesc2.value = retValue;
		}
	}

	if (topicDesc3 != null && topicDesc3.value != '') {
		if (topicDesc3.value.length > maxlength) {
			var retValue = formatData(topicDesc3,maxlength);
			topicDesc3.value = retValue;
		}
	}

	if (summaryDesc1 != null && summaryDesc1.value != '') {
		if (summaryDesc1.value.length > maxlength) {
			var retValue = formatData(summaryDesc1,maxlength);
			summaryDesc1.value = retValue;
		}
	}

	if (summaryDesc2 != null && summaryDesc2.value != '') {
		if (summaryDesc2.value.length > maxlength) {
			var retValue = formatData(summaryDesc2,maxlength);
			summaryDesc2.value = retValue;
		}
	}

	if (summaryDesc3 != null && summaryDesc3.value != '') {
		if (summaryDesc3.value.length > maxlength) {
			var retValue = formatData(summaryDesc3,maxlength);
			summaryDesc3.value = retValue;
		}
	}
 }

function formatData(obj,maxlength) {
	var originalValue = obj.value;
	var totalStringLength = obj.value.length;
	var start = 0;
	var end = maxlength;
	var appendedPart = '';
	var subPart = '';
	var remainder = (totalStringLength % maxlength);

	for(var i=0; i<(totalStringLength/maxlength); i++) {
		subPart = originalValue.substring(start,end);
		if (subPart.indexOf('\n') != -1) {
			start = start + maxlength;
			end = end + maxlength;
			appendedPart = appendedPart + subPart;
		} else {
			start = start + maxlength;
			end = end + maxlength;
			appendedPart = appendedPart + '\n' + subPart;
		}
	}

	if (remainder > 0) {
		start = end;
		end = end + remainder;
		subPart = originalValue.substring(start,end);
		appendedPart = appendedPart + '\n' + subPart;
	}
	return appendedPart;
}

function SaveFeedbackDetails() {
	formatHeaderData();
	if (validateForm() && checkAndValidate()) {
		document.patientFeedbackform.submit();
	}
}

function submitForm() {
	var href="";
	href += cpath;
	href += "/patientfeedback/FeedbackForm.do?_method=printFeedbackForm"
	window.open(href);
 }

function validateForm() {
	var number = document.getElementById('rating_scale').value ;

	if (empty(number)) {
		alert("please enter rating scale");
		document.getElementById('rating_scale').focus();
		showRatingDialog();
		return false;
	}
	return true;
}

function fillRatingValues(ratingScale) {
		document.getElementById('rating_scale').value = ratingScale;

		for (var i=0;i<=ratingScale;i++) {
			document.getElementById('rating_number_disable'+i).value = i;
			document.getElementById('rating_number'+i).value = i;
			document.getElementById('rating_number'+i).disabled = true;
			if (i == 0) {
				document.getElementById('rating_short_name'+i).value = 'NR';
				document.getElementById('rating_long_name'+i).value = 'Not Rated';
			}

			if (ratingScale == 3) {
				 if (i == 1) {
					document.getElementById('rating_short_name'+i).value = 'P';
					document.getElementById('rating_long_name'+i).value = 'Poor';
				 } else if (i == 2) {
					document.getElementById('rating_short_name'+i).value = 'G';
					document.getElementById('rating_long_name'+i).value = 'Good';
				 } else if (i == 3) {
					document.getElementById('rating_short_name'+i).value = 'E';
				 	document.getElementById('rating_long_name'+i).value = 'Excellent';
				}
			} else if (ratingScale == 10) {
				 if (i == 1) {
					document.getElementById('rating_short_name'+i).value = 'ED';
					document.getElementById('rating_long_name'+i).value = 'Extremely Dissatisfied';
				} else if (i == 2) {
					document.getElementById('rating_short_name'+i).value = 'VD';
					document.getElementById('rating_long_name'+i).value = 'Very Dissatisfied';
				} else if (i == 3) {
					document.getElementById('rating_short_name'+i).value = 'SD';
					document.getElementById('rating_long_name'+i).value = 'Somewhat Dissatisfied';
				} else if (i == 4) {
					document.getElementById('rating_short_name'+i).value = 'MD';
					document.getElementById('rating_long_name'+i).value = 'Mostly dissatisfied though few satisfactorymoments';
				} else if (i == 5) {
					document.getElementById('rating_short_name'+i).value = 'ME';
					document.getElementById('rating_long_name'+i).value = 'Missed most of my expectations';
				} else if (i == 6) {
					document.getElementById('rating_short_name'+i).value = 'DE';
					document.getElementById('rating_long_name'+i).value = 'Did not meet expectations';
				} else if (i == 7) {
					document.getElementById('rating_short_name'+i).value = 'JS';
					document.getElementById('rating_long_name'+i).value = 'Not very satisfied but not unhappy';
				} else if (i == 8) {
					document.getElementById('rating_short_name'+i).value = 'S';
					document.getElementById('rating_long_name'+i).value = 'Somewhat Satisfied';
				} else if (i == 9) {
					document.getElementById('rating_short_name'+i).value = 'VS';
					document.getElementById('rating_long_name'+i).value = 'Very Satisfied';
				} else if (i == 10){
					document.getElementById('rating_short_name'+i).value = 'ES';
					document.getElementById('rating_long_name'+i).value = 'Extremely Satisfied';
				}
			} else if (ratingScale == 5) {
				if (i == 1) {
					document.getElementById('rating_short_name'+i).value = 'VD';
					document.getElementById('rating_long_name'+i).value = 'Very Dissatisfied';
				} else if (i == 2) {
					document.getElementById('rating_short_name'+i).value = 'SD';
					document.getElementById('rating_long_name'+i).value = 'Somewhat dissatisfied';
				} else if (i == 3) {
					document.getElementById('rating_short_name'+i).value = 'N';
					document.getElementById('rating_long_name'+i).value = 'Neither Dissatisfied not Satisfied';
				} else if (i == 4) {
					document.getElementById('rating_short_name'+i).value = 'SS';
					document.getElementById('rating_long_name'+i).value = 'Somewhat Satisfied';
				} else if (i == 5) {
					document.getElementById('rating_short_name'+i).value = 'VS';
					document.getElementById('rating_long_name'+i).value = 'Very Satisfied';
				}
			} else if (ratingScale == 7) {
				if (i == 1) {
					document.getElementById('rating_short_name'+i).value = 'VD';
					document.getElementById('rating_long_name'+i).value = 'Very Dissatisfied';
				} else if ( i == 2) {
					document.getElementById('rating_short_name'+i).value = 'D';
					document.getElementById('rating_long_name'+i).value = 'Dissatisfied';
				} else if (i == 3) {
					document.getElementById('rating_short_name'+i).value = 'DE';
					document.getElementById('rating_long_name'+i).value = 'Does not meet Expectations';
				} else if (i == 4) {
					document.getElementById('rating_short_name'+i).value = 'N';
					document.getElementById('rating_long_name'+i).value = 'Neither Dissatisfied not Satisfied';
				} else if (i == 5) {
					document.getElementById('rating_short_name'+i).value = 'SE';
					document.getElementById('rating_long_name'+i).value = 'Somewhat Satisfied';
				} else if (i == 6) {
					document.getElementById('rating_short_name'+i).value = 'M';
					document.getElementById('rating_long_name'+i).value = 'Meets Expectations';
				} else if (i == 7) {
					document.getElementById('rating_short_name'+i).value = 'EE';
					document.getElementById('rating_long_name'+i).value = 'Exceeds Expectations';
				}
			}
		}
}

function getRatingDetails() {
	var reqObj = newXMLHttpRequest();
	var url = cpath+ "/patientfeedback/FeedbackForm.do?_method=getRatingDetails";
	reqObj.open("POST",url.toString(), false);
	reqObj.send(null);
	if (reqObj.readyState == 4) {
		if ( (reqObj.status == 200) && (reqObj.responseText!=null) ) {
			eval("var rating =" + reqObj.responseText);
			if (rating != null && rating != '') {
				var number = document.getElementById('rating_scale').value;
				var table = document.getElementById('ratingTable');
				var len = rating.length;
				deleteDummyRows();

				for (var i=1 ;i<len;i++) {
					var row = table.insertRow(-1);
					var cell = row.insertCell(-1);
					cell.innerHTML = '<input type="text" style="width: 50px" name="rating_number" id="rating_number' + i + '" value="" onkeypress="return enterNumOnly(event)">' +
									 '<input type="hidden" name="rating_number_disable" id="rating_number_disable' + i +'">';

					cell = row.insertCell(-1);
					cell.innerHTML = '<input type="text" style="width: 50px" name="rating_short_name" id="rating_short_name' + i + '" value="">' ;

					cell = row.insertCell(-1);
					cell.innerHTML = '<input type="text" style="width: 300px"  name="rating_long_name" id="rating_long_name' + i + '" value="">' ;
				}
				document.getElementById('rating_scale').value = ratingScale;
				document.getElementById('rating_scale_hidden').value = ratingScale;
				for (var i=0; i<rating.length;i++) {
					document.getElementById('rating_number'+i).value = rating[i].rating_number;
					document.getElementById('rating_number_disable'+i).value = rating[i].rating_number;
					document.getElementById('rating_number'+i).disabled = true;
					document.getElementById('rating_short_name'+i).value = rating[i].rating_short_name;
					document.getElementById('rating_long_name'+i).value = rating[i].rating_long_name;
				}
				ratingDialog.show();
			} else {

				document.getElementById('rating_scale').value = 0;
				document.getElementById('rating_scale_hidden').value = 0;
				document.getElementById('rating_number0').disabled = false;
				document.getElementById('rating_number0').value = '0';
				document.getElementById('rating_number_disable0').value = '0';
				document.getElementById('rating_short_name0').value = 'NR';
				document.getElementById('rating_long_name0').value = 'Not Rated';

				ratingDialog.show();
			}
		}
	}
}

function showQuestionTypes(obj) {
	var responseType = obj;
	if (responseType != null) {
		if (responseType.value == 'AD' || responseType.value == 'R' || responseType.value == 'YN') {
			return true;
		} else if (responseType.value == '') {
			responseType.value = '';
			return true;
		} else {
			confirm(" ----please enter question type---- " +
					"\n if question type is rating then enter R"+
					"\n if question type is YN then enter YN"+
					"\n if question type is AD Then Enter AD");
			responseType.value = '';
			responseType.focus();
			return false;
		}
	}
}

function validateAndSubmit() {
	document.patientFeedbackform._method.value = 'showSurveyForm';
	if(validateForm() && checkAndValidate()){
		if ((generalInfoJson == null || generalInfoJson == '')) {
			alert("There is no data to show");
			document.getElementById('survey_title').focus();
			return false;
		}

		if (headerListJson == null || headerListJson == '') {
			alert("There is no data to show");
			document.getElementById('header_line1').focus();
			return false;
		}

		if (topicListJson == null || topicListJson == '') {
			alert("There is no data to show");
			document.getElementById('topic_header_line1').focus();
			return false;
		}

		if (summaryListJson == null || summaryListJson == '') {
			alert("There is no data to show");
			document.getElementById('summary_header_line1').focus();
			return false;
		}

		document.patientFeedbackform.submit();
	}

}

function showIsItOverallRating(obj) {
	var responseType = obj;
	if (responseType != null) {
		if (responseType.value == 'Y' || responseType.value == 'N') {
			return true;
		} else if (responseType.value == '') {
			responseType.value = '';
			return true;
		} else {
			confirm(" ----Follow this given Instruction---- " +
					"\n if it is an overall rating Question then enter only Y"+
					"\n if it is not an overall rating Question then enter only N");
			responseType.value = '';
			responseType.focus();
			return false;
		}
	}
}