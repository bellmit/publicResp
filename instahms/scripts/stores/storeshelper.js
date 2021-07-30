/**
 * This file is for the helper methods.
**/
function setFieldValue(_key, _value, _id) {
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			document.getElementById(_key+_id).value = _value;
	} else {
		if(document.getElementById(_key))
			document.getElementById(_key).value = _value;
	}
}

function setFieldText(_key, _value, _id) {
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			document.getElementById(_key+_id).textContent = _value;
	} else {
		if(document.getElementById(_key))
			document.getElementById(_key).textContent = _value;
	}
}

function setFieldHtml(_key, _value, _id) {
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			document.getElementById(_key+_id).innerHTML = _value;
	} else {
		if(document.getElementById(_key))
			document.getElementById(_key).innerHTML = _value;
	}
}

function setSelectedValue(field, fieldValue) {
    for (var i = 0; i < field.options.length; i++) {
        if (field.options[i].value == fieldValue) {
        	field.options[i].selected = true;
            return;
        }
    }
}

function getField(_key, _id) {
	var _fieldObj;
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			_fieldObj = document.getElementById(_key+_id);
	} else {
		if(document.getElementById(_key))
			_fieldObj = document.getElementById(_key);
	}
	return _fieldObj;
}

function getFieldValue(_key, _id) {
	var _fieldValue;
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			_fieldValue = document.getElementById(_key+_id).value;
	} else {
		if(document.getElementById(_key))
			_fieldValue = document.getElementById(_key).value;
	}
	return _fieldValue;
}

function getFieldText(_key, _id) {
	var _fieldValue;
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			_fieldValue = document.getElementById(_key+_id).textContent;
	} else {
		if(document.getElementById(_key))
			_fieldValue = document.getElementById(_key).textContent;
	}
	return _fieldValue;
}

function getFieldHtml(_key, _id) {
	var _fieldValue;
	if(_id != undefined && _id != null && _id != 'null' && _id != '' && _id != 'undefined' ) {
		if(document.getElementById(_key+_id))
			_fieldValue = document.getElementById(_key+_id).innerHTML;
	} else {
		if(document.getElementById(_key))
			_fieldValue = document.getElementById(_key).innerHTML;
	}
	return _fieldValue;
}

function copyFieldValue(_srcKey, _descKey, _descId, _srcId) {
	var _srcField;
	var _descField;
	
	if(_srcId != undefined && _srcId != null && _srcId != 'null' && _srcId != '' && _srcId != 'undefined' ) {
		if(document.getElementById(_srcKey+_srcId))
			_srcField = document.getElementById(_srcKey+_srcId);
	} else {
		if(document.getElementById(_srcKey))
			_srcField = document.getElementById(_srcKey);
	}
	
	if(_descId != undefined && _descId != null && _descId != 'null' && _descId != '' && _descId != 'undefined' ) {
		if(document.getElementById(_descKey+_descId))
			_descField = document.getElementById(_descKey+_descId);
	} else {
		if(document.getElementById(_descKey))
			_descField = document.getElementById(_descKey);
	}
	if(_descField && _srcField)
		_descField.value = _srcField.value;
}

function copyFieldText(_srcKey, _descKey, _descId, _srcId) {
	var _srcField;
	var _descField;
	
	if(_srcId != undefined && _srcId != null && _srcId != 'null' && _srcId != '' && _srcId != 'undefined' ) {
		if(document.getElementById(_srcKey+_srcId))
			_srcField = document.getElementById(_srcKey+_srcId);
	} else {
		if(document.getElementById(_srcKey))
			_srcField = document.getElementById(_srcKey);
	}
	
	if(_descId != undefined && _descId != null && _descId != 'null' && _descId != '' && _descId != 'undefined' ) {
		if(document.getElementById(_descKey+_descId))
			_descField = document.getElementById(_descKey+_descId);
	} else {
		if(document.getElementById(_descKey))
			_descField = document.getElementById(_descKey);
	}
	if(_descField && _srcField)
		_descField.textContent = _srcField.textContent;
}

function copyFieldHtml(_srcKey, _descKey, _descId, _srcId) {
	var _srcField;
	var _descField;
	
	if(_srcId != undefined && _srcId != null && _srcId != 'null' && _srcId != '' && _srcId != 'undefined' ) {
		if(document.getElementById(_srcKey+_srcId))
			_srcField = document.getElementById(_srcKey+_srcId);
	} else {
		if(document.getElementById(_srcKey))
			_srcField = document.getElementById(_srcKey);
	}
	
	if(_descId != undefined && _descId != null && _descId != 'null' && _descId != '' && _descId != 'undefined' ) {
		if(document.getElementById(_descKey+_descId))
			_descField = document.getElementById(_descKey+_descId);
	} else {
		if(document.getElementById(_descKey))
			_descField = document.getElementById(_descKey);
	}
	if(_descField && _srcField)
		_descField.innerHTML = _srcField.innerHTML;
}

function isNotNullObj(obj) {
	var _isFieldNotNull = false;
	if(obj != undefined && typeof obj === "boolean") {
		_isFieldNotNull = obj;
	
	} else if(obj != undefined && obj != '' && obj != null && obj != 'null' && obj != 'undefined') {
		_isFieldNotNull = true;
	}
	return _isFieldNotNull;
}

function isNotNullValue(field, id) {
	var _isFieldNotNull = false;
	var _field;
	if(id != undefined && id != '' && id != null && id != 'null' && id != 'undefined') {
		if(document.getElementById(field+id)) {
			_field = document.getElementById(field+id).value;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	} else {
		if(document.getElementById(field)) {
			_field = document.getElementById(field).value;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	}
	return _isFieldNotNull;
}

function isNotNullText(field, id) {
	var _isFieldNotNull = false;
	var _field;
	if(id != undefined && id != '' && id != null && id != 'null' && id != 'undefined') {
		if(document.getElementById(field+id)) {
			_field = document.getElementById(field+id).textContent;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	} else {
		if(document.getElementById(field)) {
			_field = document.getElementById(field).textContent;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	}
	return _isFieldNotNull;
}

function isNotNullHtml(field, id) {
	var _isFieldNotNull = false;
	var _field;
	if(id != undefined && id != '' && id != null && id != 'null' && id != 'undefined') {
		if(document.getElementById(field+id)) {
			_field = document.getElementById(field+id).innerHTML;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	} else {
		if(document.getElementById(field)) {
			_field = document.getElementById(field).innerHTML;
			if(_field != null && _field != 'null' && _field != undefined && _field != '' && _field != 'undefined') {
				_isFieldNotNull = true;
			}
		}
	}
	return _isFieldNotNull;
}

function isNumberObj(field) {
	var _isNaNAndNotNull = false;
	if(isNotNullObj(field)) {
		if(!isNaN(field)) 
			_isNaNAndNotNull = true;
	}
	return _isNaNAndNotNull;
}

function isNotNullFormObj(field) {
	var _isFieldNotNull = false;
	if((field != undefined && field != null && field != 'undefined' && field != 'null' && field != '') && 
			(field.value != undefined && field.value != null && field.value != 'undefined' && field.value != 'null' && field.value != '')) {
		_isFieldNotNull = true;
	}
	return _isFieldNotNull;
}

/**
 * This method is used to submit form data using post request.
 * @param form id.
 * @param url request url.
 * @param isSync used for synchronous or asynchronous ajax calls.
 * @param onSuccess success call back method.
 * @param onFailure failure call back method.
 */
function ajaxForm(formId, url, isSync, onSuccess, onFailure ) {
	var form = document.getElementById(formId);
    var inputs = form.getElementsByTagName("input");
    var selects = form.getElementsByTagName("select");
    var formFieldObj = {};
    
    for(var i=0, len=inputs.length; i<len; i++){
    	if(inputs[i].type === "hidden"){
    		if(isNotNullObj(inputs[i].value) && inputs[i].value != 'N.aN')
    			formFieldObj[inputs[i].name] = inputs[i].value;
    	}
    	if(inputs[i].type === "text"){
    		if(isNotNullObj(inputs[i].value) && inputs[i].value != 'N.aN')
    			formFieldObj[inputs[i].name] = inputs[i].value;
    	}
    	if(inputs[i].type === "checkbox"){
    		if(isNotNullObj(inputs[i].value) && inputs[i].value != 'N.aN')
    			formFieldObj[inputs[i].name] = inputs[i].value;
    	}
	}
    var itemSubgroupId = [];
    for(var i=0, len=selects.length; i<len; i++){
    	if(isNotNullObj(selects[i].value) && selects[i].value != 'N.aN') {
    		if(selects[i].name == 'subgroups') {
    			itemSubgroupId.push(selects[i].value);
    		} else {
        		formFieldObj[selects[i].name] = selects[i].value;
    		}
    	}
    }
    
    formFieldObj['item_subgroup_id'] = itemSubgroupId;
    var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    var name;
    for(name in formFieldObj) {
    	urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(formFieldObj[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    var ajaxReqObject = new XMLHttpRequest();
    
    ajaxReqObject.addEventListener('load', function(event) {
    	if (ajaxReqObject.readyState == 4) {
    		if ( (this.status == 200) && (this.responseText != null) && (this.responseText != undefined) ) {
    			onSuccess(JSON.parse(this.responseText));
    		}
    	}
    });

    ajaxReqObject.addEventListener('error', function(event) {
    	if(onFailure)
    		onFailure();
    });
    ajaxReqObject.open("POST",url.toString(), isSync);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
}

/**
 * This method is used to submit json object using post request.
 * @param formFieldObj - item obj.
 * @param url - request url.
 * @param isSync used for synchronous or asynchronous ajax calls.
 */
function ajaxFormObj(formFieldObj, url, isSync) {
	var response;
    var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    var name;
    for(name in formFieldObj) {
    	urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(formFieldObj[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    var ajaxReqObject = new XMLHttpRequest();
    
    ajaxReqObject.open("POST",url.toString(), isSync);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
    if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) ) {
			response = JSON.parse(ajaxReqObject.responseText);
		}
	}
    return response;
}

function objectToFormData(formFieldObj) {
    var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    var name;
    for(name in formFieldObj) {
    	urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(formFieldObj[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    return urlEncodedData;
}

function formDataPost(formData, url, sync) {
	var response;
	var ajaxReqObject = new XMLHttpRequest();
	ajaxReqObject.open("POST", url.toString(), false);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	ajaxReqObject.send(formData);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
			response = JSON.parse(ajaxReqObject.responseText);
		}
	}
	return response;
}

/**
 * This method is used to submit json object using get request.
 * @param formFieldObj - item obj.
 * @param url - request url.
 * @param isSync used for synchronous or asynchronous ajax calls.
 */
function ajaxGetFormObj(formFieldObj, url, isSync) {
	var response;
    var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    var name;
    if(formFieldObj && formFieldObj != null) {
    	for(name in formFieldObj) {
        	urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(formFieldObj[name]));
        }
    	urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    }
    var ajaxReqObject = new XMLHttpRequest();
    
    ajaxReqObject.open("GET",url.toString(), isSync);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
    if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) ) {
			response = JSON.parse(ajaxReqObject.responseText);
		}
	}
    return response;
}

/**
 * This method is used to submit form data using get request.
 * @param form id.
 * @param url request url.
 * @param isSync used for synchronous or asynchronous ajax calls.
 * @param onSuccess success call back method.
 * @param onFailure failure call back method.
 */
function ajaxGetForm(formId, url, isSync, onSuccess, onFailure ) {
	var form = document.getElementById(formId);
    var inputs = form.getElementsByTagName("input");
    var selects = form.getElementsByTagName("select");
    var formFieldObj = {};
    
    for(var i=0, len=inputs.length; i<len; i++){
    	if(inputs[i].type === "hidden"){
    		formFieldObj[inputs[i].name] = inputs[i].value;
    	}
    	if(inputs[i].type === "text"){
    		formFieldObj[inputs[i].name] = inputs[i].value;
    	}
    	if(inputs[i].type === "checkbox"){
    		formFieldObj[inputs[i].name] = inputs[i].value;
    	}
	}
    
    for(var i=0, len=selects.length; i<len; i++){
    	formFieldObj[selects[i].name] = selects[i].value;
    }
    
    var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    var name;
    for(name in formFieldObj) {
    	urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(formFieldObj[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    var ajaxReqObject = new XMLHttpRequest();
    
    ajaxReqObject.addEventListener('load', function(event) {
    	if (ajaxReqObject.readyState == 4) {
    		if ( (this.status == 200) && (this.responseText != null) && (this.responseText != undefined) ) {
    			onSuccess(JSON.parse(this.responseText));
    		}
    	}
    });

    ajaxReqObject.addEventListener('error', function(event) {
    	if(onFailure)
    		onFailure();
    });
    
    ajaxReqObject.open("GET",url.toString(), isSync);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
}

/**
 * This method is used to make ajax calls with response handler(POST).
 * 
 * @param reqObject
 * @param responseHandler
 * @param url
 * @param isAsync = true - means asynchronous and false means synchronous.
 */

function ajaxPOSTRequest(reqObject, responseHandler, url, isAsync) {
    if (reqObject) {
		reqObject.onreadystatechange = function() {
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
					responseHandler(reqObject.responseText);
				} else {
					responseHandler(null);
				}
			}
		}
		reqObject.open("POST", url.toString(), isAsync != undefined ? isAsync : true);
		reqObject.send(null);
	}
}

/**
 * This method is used to make ajax calls with response handler(GET).
 * 
 * @param reqObject
 * @param responseHandler
 * @param url
 * @param isAsync = true - means asynchronous and false means synchronous.
 */
function ajaxGETRequest(reqObject, responseHandler, url, isAsync) {
    if (reqObject) {
		reqObject.onreadystatechange = function() {
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
					responseHandler(reqObject.responseText);
				} else {
					responseHandler(null);
				}
			}
		}
		reqObject.open("GET", url.toString(), isAsync != undefined ? isAsync : true);
		reqObject.send(null);
	}
}