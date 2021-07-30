

/* This js file related to Customer Registration Card*/



/* This function  will populate card info
 if the user is trying for edit.
 */
function getDisplay() {
	var from=document.forms[0].from.value;
	if (from =="edit") {
		populateRegCardDetails();
		if (document.forms[0].flag.value=="true") {
			document.forms[0].visit_type.disabled=true;
			document.forms[0].rate_plan.disabled=true;
			document.forms[0].status.disabled=true;
		}
	}
}

  /* This function  will populate card info
 if the user is trying for edit.
 */

function populateRegCardDetails(){
	var cardName=document.forms[0].card_name;
	var visitTypeOptions=document.forms[0].visit_type;
	var ratePlanOptions=document.forms[0].rate_plan;
	var statusOptions=document.forms[0].status;

	if (regCardJSON.length>0) {
		cardName.value=regCardJSON[0].card_name;
		setSelectedIndex(visitTypeOptions, regCardJSON[0].visit_type);
		setSelectedIndex(ratePlanOptions, regCardJSON[0].rate_plan);
		setSelectedIndex(statusOptions, regCardJSON[0].status);
		document.getElementById("save").value="update";
	}

}

  /* This function will generate customer registration card
     PDF*/
function showCustomerRegCard(){
	var cardId=document.forms[0].cardId.value;
	window.open("RegistrationCards.do?_method=showCustomerRegcard&cardId="+cardId);
}

   /* validate upload file*/
function validate() {
	if (trimAll(document.getElementById("card_name").value) == "") {
		alert("Card name is required");
		document.getElementById("card_name").focus();
		return false;
	}
	if (document.forms[0].from.value == 'add') {
		if (document.forms[0].custom_reg_card_template.value == "") {
			alert("Custom Registration Card Template required");
			document.forms[0].custom_reg_card_template.focus();
			return false;
		}
	}
	if (document.forms[0].custom_reg_card_template.value != "" &&
			document.forms[0].odtFile.value == "") {
		alert("Please upload the odt file.");
		document.forms[0].odtFile.focus();
		return false;
	}
	var newVisitType = trimAll(document.forms[0].visit_type.value);
	var newRatePlan = trimAll(document.forms[0].rate_plan.value);
	for (var i=0; i<cardsList.length; i++){
		item = cardsList[i];

		var hiddenCardId = document.forms[0].cardId.value;
		if (hiddenCardId!=item.CARD_ID) {
			var actualVisitType = item.VISIT_TYPE;
			var actualRatePlan = item.RATE_PLAN;
			if (newVisitType == actualVisitType) {
				if (newRatePlan == actualRatePlan) {
					alert("Card with same RatePlan and Visittype already exists.");
					return false;
				}
			}
		}
	}
	if ((document.forms[0].flag.value=="true") && (document.forms[0]._method.value=="update")) {
		document.forms[0].visit_type.disabled=false;
		document.forms[0].rate_plan.disabled=false;
		document.forms[0].status.disabled=false;
	}
}




function checkDuplicate(){
	var newVisitType = trimAll(document.forms[0].visit_type.value);
	var newRatePlan = trimAll(document.forms[0].rate_plan.value);
	for (var i=0; i<cardsList.length; i++) {
		item = cardsList[i];
		var hiddenCardId = document.forms[0].cardId.value;
		if (hiddenCardId!=item.CARD_ID) {
			var actualVisitType = item.VISIT_TYPE;
			var actualRatePlan = item.RATE_PLAN;
			if (newVisitType == actualVisitType) {
	    		if (newRatePlan == actualRatePlan) {
					alert("Card with same RatePlan and Visittype already exists.");
					return false;
				}
			}
		}
	}
}
