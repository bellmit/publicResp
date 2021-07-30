
function intiTicketDetails() {
	if( 0 < ticketDetails.length ) {
		let selectedMessageType = filterListWithValues(messageTypeJson,'review_type_id',[Number(ticketDetails[0].review_type_id)]);
		if(selectedMessageType[0].review_type_status == 'I' || 
				selectedMessageType[0].review_category_status == 'I') {
			$('#msgTypeText').val( selectedMessageType[0].review_type+"(Inactive)" );			
		} else {
			$('#msgTypeText').val( selectedMessageType[0].review_type );
		}
		$('#role_id').val( ticketDetails[0].assigned_to_role );
		$('#review_type_id').val( ticketDetails[0].review_type_id );
		$('#review_type_category').val( ticketDetails[0].review_category );
        $('#ticketTitle').val( ticketDetails[0].title );
        $('#review_type_changed').val((selectedMessageType[0].review_type_status == 'I' || 
				selectedMessageType[0].review_category_status == 'I'));
        loadRecipients();
        $('#recipients').val( ticketDetails[0].assignedto );        
	}
    //exclude inactive review types
	messageTypeJson = filterListWithExcludingValues(messageTypeJson,"review_type_status","I");
	messageTypeJson = filterListWithExcludingValues(messageTypeJson,"review_category_status","I");

}
var showLoader = function() {
	var loaderMask = document.createElement("div");
	loaderMask.setAttribute("id", "loader");
	loaderMask.innerHTML = "Loading...";
	loaderMask.style.top = 0;
	loaderMask.style.paddingTop = "20%";
	loaderMask.style.bottom = 0;
	loaderMask.style.left = 0;
	loaderMask.style.right = 0;
	loaderMask.style.backgroundColor = "#FFFFFF";
	loaderMask.style.color = "#000000";
	loaderMask.style.opacity = "0.8";
	loaderMask.style.position = "absolute";
	loaderMask.style.textAlign = "center";
	loaderMask.style.fontSize="20px";
	loaderMask.style.fontWeight="bold";
	loaderMask.style.boxSizing="border-box";
	loaderMask.style.zIndex= 1;
	var contentareaTd = document.getElementsByClassName("contentarea")[0];
	contentareaTd.style.position = "relative";
	contentareaTd.appendChild(loaderMask);
	return true;
}

function init() {
	// If there is no consultation for selected visit, Do not include Physician
	// review types.
	if(patientDetails.length && !patientDetails[0].consultation_id) {
		messageTypeJson = filterListWithExcludingValues(messageTypeJson,"review_category","physician");
	}
	if(messageTypeJson.length == 0){
		alert("Review Types are empty.");
		return false;
	}
	intiTicketDetails();
	initCodificationMessageTypesAutocomplete('msgTypeText','msgTypeDropDown', 'review_type_id');
	// comment time ago
	var timeAgoClass = document.getElementsByClassName('timeAgo');
	if( timeAgoClass.length > 0 ) {
		Array.prototype.forEach.call(timeAgoClass, function(el) {
		    var timeAgoConvertd = moment(el.getAttribute("data-comment-date")).fromNow()
			el.innerHTML = timeAgoConvertd;
		});
	}
	if ( codificationIsInActive ) {
		$('select#role_id, select#recipients').prop("disabled","disabled");
	}
	$("form[name=ticketForm]").submit(function(){
		if( $("#msgTypeText").val().trim() == "" ) {
			alert("Please Select Review type.");
			$("#msgTypeText").focus();
	    	return false;
		} else {
			if( !$("#msgTypeText").prop("disabled") && $("#review_type_changed").val() == "true" ) {
				alert("Review Category/type is Inactive, you can't create/update a review with inactive type.");
				return false;
			}
			
		}
	    if( $("#role_id").val().trim() == "") {
	     	alert("Please select Role");
	     	return false;
	    }  
	    if( $("#ticketTitle").val().trim() == "" ) { 
			alert("Review can't be empty.");
			$("#ticketTitle").focus();
	    	return false;
		}
	 	if( $("#ticketBody").length && $("#ticketBody").val().trim() == "" ) {
			alert("Review Details can't be empty.");
			$("#ticketBody").focus();
	    	return false;
		}
		if( $("#recipients").val().trim() == "" && $("#review_type_category").val() == "physician" ) {
		    alert("Review Recipient can't be empty.");
		    $("#recipients").focus();
			return false;
	 	}
	    if (isCodificationClosed()) {
	    	alert("Codification is closed, you can't create/update reviews.")
	    	return false;
	    }
		showLoader();
	});
	$('form').submit(function(e) {
	    $(':disabled').each(function(e) {
	        $(this).removeAttr('disabled');
	    })
	});
	initActivity();
}


function initActivity(){
	initDiff();
	initCreateActivity();
}

function initDiff(){
	$('.diff').each(
		(i, diffElement) => {
			var oldValueElement = $(diffElement).find('.old-value');
			var newValueElement = $(diffElement).find('.new-value');
			var oldValue = oldValueElement.text();
			var newValue = newValueElement.text();

			oldValueElement.text("");
			newValueElement.text("");

			for(let difflet of JsDiff.diffWordsWithSpace(oldValue,newValue)){
				if(difflet.added){
					let addedSpan = document.createElement("span");
					addedSpan.className = 'diff-added';
					addedSpan.innerText = difflet.value;
					newValueElement.append(addedSpan);
				}else if (!difflet.removed){
					var normalSpan = document.createElement("span");
					normalSpan.innerText = difflet.value;
					newValueElement.append(normalSpan);
				}
			}

			for(let difflet of JsDiff.diffWordsWithSpace(oldValue,newValue)){
				if(difflet.removed){
					let addedSpan = document.createElement("span");
					addedSpan.className = 'diff-removed';
					addedSpan.innerText = difflet.value;
					oldValueElement.append(addedSpan);
				}else if (!difflet.added){
					let normalSpan = document.createElement("span");
					normalSpan.innerText = difflet.value;
					oldValueElement.append(normalSpan);
				}
			}
		}
	);
}

function initCreateActivity(){
	var createReviewJSON = $('.activity-create').text().trim();
	
	if(createReviewJSON){
		createReviewJSON = JSON.parse(createReviewJSON);
		var activityItem = $('<ul></ul>').addClass('changelist');
		activityItem.append('<li> Reason : ' + createReviewJSON.title);
		activityItem.append('<li> Review Type : ' + createReviewJSON.review_type);
		activityItem.append('<li> Details : ' + createReviewJSON.body);
		activityItem.append('<li> Role : ' + createReviewJSON.role);
		activityItem.append('<li> Assignee : ' + createReviewJSON.assignee);
		$('.activity-create').html(activityItem);
	}
}


function initCodificationMessageTypesAutocomplete(messageTypeTextBox,messageTypeDropdown, messageTypeId) {
    var messageTypeJsonResult = {result:messageTypeJson};
    let dataSource  = new YAHOO.util.LocalDataSource(messageTypeJsonResult, { queryMatchContains : true } );
    dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList : "result",
        fields: [    
            {key : "review_type"}, 
            {key : "review_type_id"}, 
            {key : "review_category_status"}, 
            {key : "review_title"}, 
            {key : "review_content"},
            {key : "review_category"},
            {key : "review_type_status"}
        ]
    };
	let oMessageAutoCom = new YAHOO.widget.AutoComplete(messageTypeTextBox, messageTypeDropdown, dataSource);
	oMessageAutoCom.maxResultsDisplayed = 20;
	oMessageAutoCom.allowBrowserAutocomplete = false;
	oMessageAutoCom.prehighlightClassName = "yui-ac-prehighlight";
	oMessageAutoCom.typeAhead = false;
	oMessageAutoCom.useShadow = true;
	oMessageAutoCom.minQueryLength = 0;
	oMessageAutoCom.forceSelection = true;
	oMessageAutoCom.filterResults = Insta.queryMatchWordStartsWith;
	oMessageAutoCom.itemSelectEvent.subscribe(function (oSelf, elItem){
        var record = elItem[2];
        $('#ticketTitle').val(record[3]);
		$('#review_type_id').val( record[1] );
		$('#review_type_category').val( record[5] );
		$('#ticketBody').val( record[4] ) ;
		$('#review_type_changed').val("changed");
		
        if( record[5] == 'physician' && duty_doctor_user_id != null) {
        	$('#role_id').val( duty_doctor_role_id );
	        loadRecipients();
	        if( $('#recipients option[value="'+duty_doctor_user_id+'"]').length>0 ) {
	        	$('#recipients option[value="'+duty_doctor_user_id+'"]').prop("selected","selected");
	        } 
        } else if( reviewTypeRolesJson.length > 0 ) {
        	var reviewRoles = filterList(reviewTypeRolesJson,'review_type_id',record[1])
        	if( reviewRoles.length ) {
        		$('#role_id').val( reviewRoles[0].role_id);		        	
	        		loadRecipients();
        	}else{
        		$('#role_id').val("");		        	
    			loadRecipients();
        	}
        } else {
			$('#role_id').val("");		        	
			loadRecipients();
		}
    });   
}

function loadRecipients() {
    
    var selectedRoleId = $("#role_id").val();
    var recipients = filterListWithValues(usersJson,"role_id",[Number(selectedRoleId)]); 
	$('#recipients option[value!=""]').remove();		    
	recipients.forEach(function(recipient){
		if( 0 < ticketDetails.length ) {
			// If already assigned user is inactive then show as inactive
			if( ticketDetails[0].assignedto == recipient.emp_username && recipient.emp_status=="I" ) {
				$("#recipients").append($("<option>").text(recipient.temp_username+"(Inactive)").attr("value",recipient.emp_username));	
			}else if(recipient.emp_status=="A"){
				$("#recipients").append($("<option>").text(recipient.temp_username).attr("value",recipient.emp_username));	
			}
		}else{
			if(recipient.emp_status=="A") {
				$("#recipients").append($("<option>").text(recipient.temp_username).attr("value",recipient.emp_username));	
			}
		}
	});
   	$("#recipients").attr("disabled", false);

}

function checkCommentEmpty() {
	if( document.getElementById('commentTextBox').value.length == 0) {
		alert('comment can not be empty.');
		return false;
	}
	if (isCodificationClosed()) {
    	alert("Codification is closed, you can't create/update reviews.")
    	return false;
    }
	if( !$("#msgTypeText").prop("disabled") && $("#review_type_changed").val() == "true" ) {
		alert("Review Category or Review type is inactive, you can't comment on this review.");
		return false;
	}
	showLoader();
}

function isCodificationClosed() {
	//is codification closed?
	if (patientId != undefined && patientId != '') {
	  let url = pagePath + "checkCodificationStatus.json?patient_id="+patientId;
      let ajaxReqObject = newXMLHttpRequest();
	  ajaxReqObject.open("GET",url.toString(), false); //synchronous call
	  ajaxReqObject.send(null);
	  if (ajaxReqObject.readyState == 4) {
	    if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) ) {
	  	  let codificationStatus = JSON.parse(ajaxReqObject.responseText);
	  	  if (codificationStatus.status == 'V' || codificationStatus.status == 'R'){
		    return true;
		  }
	  	}
	  }
	}
	return false;
}	

