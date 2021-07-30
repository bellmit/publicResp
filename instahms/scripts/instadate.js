/*
 * instadate.js: a set of utility functions to deal with date widgets, validations, etc.
 * This script is required to be included when using the insta:datewidget tag.
 * See Common/ServerDate.js for an example of the date widget usage.
 */

gMonthShortNames = getString("js.common.calendar.months.shortname").split(",");
gMonthLongNames = getString("js.common.calendar.months.longname").split(",");
gDayShortNames = getString("js.common.calendar.days.shortname").split(",");
gDayLongNames = getString("js.common.calendar.days.longname").split(",");
gDayMediumNames = getString("js.common.calendar.days.mediumname").split(",");
gDayCharNames = getString("js.common.calendar.days.charname").split(",");
gCalendarTitle = getString("js.common.calendar.title");
/*
 * Function to create a datewidget, returns a string that can be used
 * in innerHTML to crate a datewidget on the fly. Note that the value
 * must be supplied as a Date object.
 */
function getDateWidget(name, id, valueDate, validType, extravalidation, calButton, calDblClick, tabindex, cpath) {

	/* Set some default values */
	if ( (id == null) || (id == "") ) {
		id = name;
	}

	if (extravalidation == null)
		extravalidation = "";

	if (validType == null)
		validType = "";

	var valueStr;
	if (valueDate != null) {
		valueStr = formatDate(valueDate);
	} else {
		valueStr = "";
	}

	if (calButton == null) {
		calButton = false;
	}

	if (calDblClick == null) {
		calDblClick = true;
	}

	var widgetStr = '<input type="text"';

	widgetStr += makeAttrStr('name', name);
	widgetStr += makeAttrStr('id', id);
	widgetStr += makeAttrStr('value', valueStr);
	widgetStr += makeAttrStr('class', 'datefield');
	if ((tabindex != null) && (tabindex != undefined) && (tabindex != "")) {

		widgetStr += makeAttrStr('tabindex', tabindex);
	}
	widgetStr += makeAttrStr('onchange', "doValidateDateField(this,'"+validType+"');" + extravalidation);
	if (calDblClick) {
		widgetStr += makeAttrStr('ondblclick', "showCalendar('"+id+"','"+validType+"')");
	}
	widgetStr += '/>';
	widgetStr += '&nbsp;';

	if (calDblClick || calButton) {
		if (calButton) {
			var imgUrl = (cpath == null) ? "../../images/calendar.png" : cpath + "/images/calendar.png";
			widgetStr += '<img src="' + imgUrl + '" height="16" width="16" style="vertical-align: text-bottom"';
			widgetStr += makeAttrStr('onclick', "showCalendar('"+ id +"')");
			widgetStr += '/>';
		}
		widgetStr += '&nbsp;';
		widgetStr += '<span class="yui-skin-sam">';
		widgetStr +=   '<div';
		widgetStr +=   makeAttrStr('id', id + '_container');
		widgetStr +=   makeAttrStr('class', 'instadateCalContainer');
		widgetStr +=   '>';
		widgetStr +=     '<div class="hd">'+ gCalendarTitle + '</div>';
		widgetStr += 	 '<div class="bd">';
		widgetStr +=       '<div';
		widgetStr +=       makeAttrStr('id', id + '_cal');
		widgetStr +=       makeAttrStr('class', 'instadateCalendar');
		widgetStr +=       '>';
		widgetStr += 	 '</div>';
		widgetStr +=   '</div>';
		widgetStr += '</span>';
	}


	return widgetStr;
}

/*
 * todo: move this to some common place like common.js
 */
function makeAttrStr(attrName, attrValue) {
	return ' ' + attrName + '="' + attrValue + '"';
}

/*
 * Validate a date given the input text field's ID
 */
function doValidateDateFieldId(fieldId, validType) {
	var field = document.getElementById(fieldId);
	if (field == null) {
		alert("field " + fieldId + " not found");
		return false;
	} else {
		return doValidateDateField(field, validType);
	}
}

/*
 * Validate a date given the input text field object
 */
function doValidateDateField(dateInput, validType) {

	var dateStr = dateInput.value;
	if (dateStr == "") {
		return true;
	}

	var errorStr = validateDateFormat(dateStr);
	if (errorStr != null) {
		alert(errorStr);
		dateInput.focus();
		return false;
	}

	dateStr = cleanDateStr(dateStr, validType);
	errorStr = validateDateStr(dateStr, validType);

	if (errorStr != null) {
		alert(errorStr);
		dateInput.focus();
		return false;
	}

	dateInput.value = dateStr;		// the cleaned/converted string
	return true;
}

function doValidateTimeField(timeInput) {
	var timeStr = timeInput.value;
	if (timeStr == "")
		return true;
	if (!isTime(timeStr)) {
		alert(getString("js.common.message.time.incorrect.format"));
		timeInput.focus();
		return false;
	}
	return true;
}

function validateTime(timeInput) {

	var strTime = timeInput.value;
	if (strTime == '') {
		return true;
	}

	var timePattern = /^\d*:\d*$/;
	var regExp = new RegExp(timePattern);

	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			showMessage("js.common.incorrect.time.format.hour.not.a.number.string");
			timeInput.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			showMessage("js.common.incorrect.time.format.minute.not.a.number.string");
			timeInput.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			showMessage("js.common.incorrect.hour.enter.0.23.for.hour.string");
			timeInput.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			showMessage("js.common.incorrect.minute.enter.0.59.for.minute.string");
			timeInput.focus();
			return false;
		}

		if (strHours.length < 2 || strMinutes.length < 2) {
			if (strHours.length < 2) strHours = "0" + strHours;
			if (strMinutes.length < 2) strMinutes = "0" + strMinutes;
			timeInput.value = strHours + ":" + strMinutes;
		}
	} else {
		alert(getString("js.common.incorrect.time.fmt.string")+" " + strTime+" " + getString("js.common.pls.enter.hh.mm.string"));
		timeInput.focus();
		return false;
	}
	return true;
}

/*
 * isInteger: accepts only positive integers, no +/- sign allowed
 */
function isInteger(text) {
    var str = text.toString();
    var re = /^\d*$/;
	return re.test(text);
}

/*
 * Validates that a date is in the correct format, DD-MM-YYYY, or any
 * format that can be converted to something acceptable by Date.
 * Returns an error string or null if the date is valid.
 */
function validateDateFormat(dateStr) {

	var myarray = dateStr.split("-");

	if (myarray.length != 3) {
		return getString("js.common.incorrect.date.format.use.dd.mm.yyyy.string");
	}

	var dt = myarray[0];
	var mth = myarray[1];
	var yr = myarray[2];

	if (!isInteger(dt)) {
		return getString("js.common.incorrect.date.format.day.not.a.number.string");
	}
	if (!isInteger(mth)) {
		return getString("js.common.incorrect.date.format.month.not.a.number.string");
	}
	if (!isInteger(yr)) {
		return getString("js.common.incorrect.date.format.year.not.a.number");
	}

    if (parseInt(dt) > 31) {
        return getString("js.common.invalid.date.enter.1.31.for.day.string");
    }

    if (parseInt(mth) > 12) {
        return getString("js.common.invalid.date.enter.1.12.for.month.string");
    }

    if ( (yr.length != 2) && (yr.length != 4) ) {
		return getString("js.common.invalid.date.year.be.a.2.or.4.digit.year.string");
	}

	return null;
}

/*
 * Validate a date string. Assumes properly formatted DD-MM-YYYY,
 * no two-digit or other formats accepted. Use validateFormat and
 * cleanDateStr to ensure that format is correct.
 */
function validateDateStr(dateStr, validType) {
	var myarray=dateStr.split("-");

	var d = parseInt(myarray[0],10);
	var m = parseInt(myarray[1],10);
	var y = parseInt(myarray[2],10);

	var dt = new Date(y, m-1, d);

	// new Date can automatically convert 29 feb to 01 mar. We need to disallow it.
	if ( (d != dt.getDate()) || (m != dt.getMonth()+1) || y != dt.getFullYear() ) {
		return getString("js.common.date.is.not.valid.for.given.month.year.string");
	}

	if (validType != null) {
		return validateDate(dt, validType);
	} else {
		return null;
	}
}

/*
 * Validate a date (javascript date object), check if future/past constraints
 * are fulfilled
 */
function validateDate(dt, validType) {
	if (validType == null) {
		return true;
	} else {
		var curDate = new Date();
		if (gServerNow != null) {			// if using datewidget, this would be set.
			curDate.setTime(gServerNow);
		}
		curDate.setHours(0);
		curDate.setMinutes(0);
		curDate.setSeconds(0);
		curDate.setMilliseconds(0);

		if (validType == 'future') {
			if (dt < curDate) {
				return getString("js.common.date.can.not.be.in.past.string");
			}
		}

		if (validType == 'past') {
			if (dt > curDate) {
				return getString("js.common.date.can.not.be.in.future.string");
			}
		}
		return null;
	}
}




/*
 * Validate a date time (javascript date object), check if future/past constraints
 * are fulfilled
 */
function validateDateTime(dt, validType) {
	if (validType == null) {
		return null;
	} else {
		var curDate = new Date();
		if (gServerNow != null) {			// if using datewidget, this would be set.
			curDate.setTime(gServerNow);
		}
		curDate.setSeconds(0);
		curDate.setMilliseconds(0);
		if (validType == 'future') {
			if (dt < curDate) {
				return "Date cannot be in the past";
			}
		}

		if (validType == 'past') {
			if (dt > curDate) {
				return "Date cannot be in the future";
			}
		}
		return null;
	}
}

/*
 * Clean up a date string, convert 2 to 4 year spec, add 0s before single
 * digit values etc. Requires that the string passed in has already passed
 * the validateDateFormat validation.
 *
 * Returns a new cleaned up string.
 */
function cleanDateStr(dateStr, validType) {

	dateStr = trimAllDashes(dateStr);

	var dateParts = dateStr.split("-");
	var dt = dateParts[0];
	var mth = dateParts[1];
	var yr = dateParts[2];

	if (dt.length==1) {
		dt="0"+dt;
	}
	if (mth.length==1) {
		mth="0"+mth;
	}
	if (yr.length < 4) {
		yr = convertTwoDigitYear(parseInt(yr,10),validType);
	}

	return dt + "-" + mth + "-" + yr;
}

/*
 * Convert a two digit year to a 4 digit year depending on the validationType:
 * If validType is 'past', or future, then, use the current date to convert. Else,
 * use "50" as the cutoff for determining this century or previous century.
 */
function convertTwoDigitYear(year, valid) {

	if (year > 99) {
		return year;
	}

	var now = new Date();
	var thisYear = now.getFullYear();					// say 2008
	var century = Math.floor(thisYear/100) * 100;		// say 2000

	if (valid == 'past') {
		if (century+year > thisYear) {		// 2000+10 > 2008
			year += century -100;	// 10 + 2000 - 100 = 1910
		} else {					// 2000+7 < 2008
			year += century;		// 7 + 2000 = 2007
		}
	}
	else if (valid == 'future') {
		if (century+year < thisYear) {		// 2000+7 < 2008
			year += century + 100;	// 2107
		} else {					// 2000+10 > 2008
			year += century;		// 2010
		}
	}
	else {
		if (year >= 50) {				// say 75
			year += century -100;		// 1975
		} else {						// say 25
			year += century;			// 2025
		}
	}
	return year;
}

/*
 * Get the date from an input field: returns a javascript date object.
 * If the date is invalid or empty, returns null. You are advised to call
 * validation before getting the value from a field.
 */
function getDateFromField(field, validType) {
	var dateStr = field.value;
	if (dateStr == "") {
		return true;
	}

	var errorStr = validateDateFormat(dateStr);
	if (errorStr != null) {
		return null;
	}

	dateStr = cleanDateStr(dateStr, validType);
	errorStr = validateDateStr(dateStr, validType);

	if (errorStr != null) {
		return null;
	}
	return parseDateStr(dateStr);
}
/*
 * Get the date from a variable returns a javascript date object.
 * If the date is invalid or empty, returns null. You are advised to call
 * validation before getting the value from a field.
 */
function getDateFromVariable(variablevalue, validType) {
	if (variablevalue == "") {
		return true;
	}

	var errorStr = validateDateFormat(variablevalue);
	if (errorStr != null) {
		return null;
	}

	variablevalue = cleanDateStr(variablevalue, validType);
	errorStr = validateDateStr(variablevalue, validType);

	if (errorStr != null) {
		return null;
	}
	return parseDateStr(variablevalue);
}

/*
 * Parses a date string and returns a javascript date object.
 * The date string must be in DD-MM-YYYY format, and the function can
 * give unexpected results if the date format is not validated before calling this function.
 */
function parseDateStr(dateStr) {
	var myarray=dateStr.split("-");

	var d = parseInt(myarray[0],10);
	var m = parseInt(myarray[1],10);
	var y = parseInt(myarray[2],10);

	var dt = new Date(y, m-1, d);
	return dt;
}

/*
 * Get Date object from given date and time strings
 */
function getDateTime(dateStr, timeStr) {

	var darray=dateStr.split("-");

	var d = parseInt(darray[0],10);
	var m = parseInt(darray[1],10);
	var y = parseInt(darray[2],10);

	var tarray=timeStr.split(":");

	var h = parseInt(tarray[0],10);
	var i = parseInt(tarray[1],10);
	var s = 0;
	if(tarray[2] != null) {
		s = parseInt(tarray[2],10);
	}

	return new Date(y,m-1,d,h,i,s);

}

/*
 * Get Date object from given date and time fields
 */
function getDateTimeFromField(datefield, timefield) {
	return getDateTime(datefield.value, timefield.value);
}

/*
 * Gets the date part of a javascript date object, essentially setting the
 * hours/minutes/seconds to 0. Returns a new date object, leaves the original one untouched.
 */
function getDatePart(dt) {
	var retDate = new Date(dt);

	retDate.setHours(0);
	retDate.setMinutes(0);
	retDate.setSeconds(0);
	retDate.setMilliseconds(0);
	return retDate;
}

/*
 * Difference (in days) between d1 and d2, where both are javascript
 * Date objects (use parseDateStr to convert from date strings to javascript
 * date objects). If you want pure dates (without time) to be compared, use
 * getDatePart(d) to remove the time part before comparison.
 */
function daysDiff(d1, d2) {
	var millisecondsDiff = d2.getTime() - d1.getTime();
	var daysDiff = millisecondsDiff / 60 / 60 / 24 / 1000;
	return daysDiff;
}

/*
 * Returns a javascript date object given the field ID of the input text.
 * Absolutely no validation done.
 */
function getDate(Objid){
	var dateStr = document.getElementById(Objid).value;
	var myarray=dateStr.split("-");
	dt=myarray[0];
	mth=myarray[1];
	yr=myarray[2];
	return new Date(yr,eval(mth)-1,dt);
}

/*
 * Various date formatting utility, given a javascript date object.
 * Format can be one of the following:
 *   ddmmyyyy (default)
 *   mmyyyy
 *   mmyy
 *   ddmonyyyy
 *   monyyyy
 *   monyy
 *   yyyymmdd
 *   yyyymondd
 * The default separator is "-", so, in most of HMS, you are only required to
 * call the default function like formatDate(dtObj)
 *
 */
function formatDate(dateObj, format, separator) {
	var year = dateObj.getFullYear();
	var monthIndex = dateObj.getMonth();
	var month = monthIndex + 1;
	var day = dateObj.getDate();

	if (("" + month).length == 1) {
		month = "0" + month;
	}
	if (("" + day).length == 1) {
		day = "0" + day;
	}

	if (separator == null) {
		separator = "-";
	}
	if (format == null) {
		format = "ddmmyyyy";
	}

	if (format == "ddmmyyyy") {
		return day + separator + month + separator + year;
	} else if (format == "mmddyyyy") {
		return month + separator + day + separator + year;
	} else if (format == "mmyyyy") {
		return month + separator + year;
	} else if (format == "mmyy") {
		return month + separator + year.toString().substr(2,4);
	} else if (format == "ddmonyyyy") {
		return day + separator + gMonthShortNames[monthIndex] + separator + year;
	} else if (format == "monyyyy") {
		return gMonthShortNames[monthIndex] + separator + year;
	} else if (format == "monyy") {
		return gMonthShortNames[monthIndex] + separator + year.toString().substr(2,4);
	} else if (format == "yyyymmdd") {
		return year + separator + month + separator + day;
	} else if (format == "yyyymondd") {
		return year + separator + gMonthShortNames[monthIndex] + separator + day;
	}
	return "";
}

function formatTime(dateObj,includeSeconds) {
	var hour = dateObj.getHours();
	var min = dateObj.getMinutes();
	var sec = dateObj.getSeconds();

	if (("" + hour).length == 1)
		hour = "0" + hour;
	if (("" + min).length == 1)
		min = "0" + min;
	if (("" + sec).length == 1)
		sec = "0" + sec;

	if (includeSeconds)
		return "" + hour + ":" + min + ":" + sec;
	else
		return "" + hour + ":" + min;
}

function formatDateTime(dateObj) {
	return formatDate(dateObj) + ' ' + formatTime(dateObj);
}


/*
 * Pops up a calendar in a new window
 */
function openCalendar(field,valid) {
   	window.open('../../pages/Common/CalendarDate.jsp?field='+field+'&valid='+valid,
			'Calendar','width=270, height=280,status=no,minimizable=no,top=200,left=200,menubar=0');
}

/*
 * getCalendar is called by CalendarDate.jsp, to initialize the calendar in its page.
 */
function getCalendar(dateStr, valid) {
	var cal1;
	// if the date is null, or has an invalid date, use current date
	if ( (dateStr == "") || (validateDateFormat(dateStr) != null) ) {
		cal1 = new YAHOO.widget.Calendar("cal1","cal1Container", {START_WEEKDAY: gCalendarStartDay});
		initCalendarStrings(cal1);
	} else {
		dateStr = cleanDateStr(dateStr, valid);
		var dt = parseDateStr(dateStr);
		// YUI wants it in mm/dd/yyyy and mm/yyyy
		var curDateStr = formatDate(dt, 'mmddyyyy', '/');
		var pageDateStr = formatDate(dt, 'mmyyyy', '/');
		cal1 = new YAHOO.widget.Calendar("cal1","cal1Container",
				{pagedate:pageDateStr, selected: curDateStr, START_WEEKDAY: gCalendarStartDay});
		initCalendarStrings(cal1);
	}

	cal1.selectEvent.subscribe(handleSelect,cal1, true);
	cal1.render();
}

/*
 * Handle an in-page calendar as a popup in the same page
 */

/*
 * Store a mapping of the calendar object from the date input's ID
 */
var calendarObjs = {};
var dialogObjs = {};

/*
 * During initialization: construct the popupCalendar given a date input ID
 */
function makePopupCalendar(dateId, pos) {
	var popupCal = new YAHOO.widget.Calendar(dateId+"_cal",
			{ iframe:false, hide_blank_weeks:true, START_WEEKDAY: gCalendarStartDay} );
	initCalendarStrings(popupCal);

	popupCal.selectEvent.subscribe(handleDateSelect, popupCal, true);

	var calCorner =getCornerSpec("tl"), inputCorner = getCornerSpec("br");
	if (pos!=null) {
		if (pos=='left') {
			inputCorner = getCornerSpec("br");
			calCorner = getCornerSpec("tr");
		} else if (pos == 'topright') {
			inputCorner = getCornerSpec("tr");
			calCorner = getCornerSpec("bl");
		} else if (pos == 'topleft') {
			inputCorner = getCornerSpec("tr");
			calCorner = getCornerSpec("br");
		}
	}
	popupCal.dateId = dateId;
	calendarObjs[dateId] = popupCal;

	var dialog = new YAHOO.widget.Dialog(dateId+"_container", {
		context:[dateId, calCorner, inputCorner, ["beforeShow", "windowResize"]],
		width:"17em",  // Sam Skin dialog needs to have a width defined (7*2em + 2*1em = 16em).
		draggable:false,
		close:true
	});

	popupCal.renderEvent.subscribe(function() {
		dialog.fireEvent("changeContent");
	});

	dialogObjs[dateId] = dialog;

	dialog.render();
	dialog.hide();
	popupCal.render();
}

/*
 * Show the calendar when the cal button is clicked or input itself is double-clicked
 */
function showCalendar(dateId) {
	/*
	 * Don't show the calendar if the associated input field is disabled/readonly.
	 */
	if (document.getElementById(dateId).disabled || document.getElementById(dateId).readOnly)
		return;

	// the original div is hidden, need to unhide it after a dialog has been constructed over it.
	// The dialog is now hidden, so it will not show even now. But, on click of the button, it will.
	document.getElementById(dateId+"_container").style.display = 'block';

	var dateValue = "";
	var el = document.getElementById(dateId);
	if (el != null) {
		dateValue = el.value;
	}

	if ( (dateValue != "") && (validateDateFormat(dateValue) == null) ) {
		var pageDateStr;
		var curDateStr;
		dateValue = cleanDateStr(dateValue);
		var dt = parseDateStr(dateValue);
		// YUI wants it in mm/dd/yyyy and mm/yyyy
		curDateStr = formatDate(dt, 'mmddyyyy', '/');
		pageDateStr = formatDate(dt, 'mmyyyy', '/');

		var popupCal = calendarObjs[dateId];
		popupCal.select(curDateStr);
		popupCal.cfg.setProperty("pagedate",pageDateStr);
		popupCal.render();
	}

	for (var i in dialogObjs) {
		// hide the other dialogs if any opened.
		if (i == dateId) {
			dialogObjs[i].show();
		} else {
			dialogObjs[i].hide();
		}
	}
}

/*
 * Handle the event when a date is picked: close the calendar and set the date in the
 * input box
 */
function handleDateSelect(type,args,obj) {
	var dates = args[0];
	var date = dates[0];
	var year = date[0], month = date[1], day = date[2];

	var popupCal = obj;
	var dialog = dialogObjs[popupCal.dateId];
	var txtDate = document.getElementById(popupCal.dateId);
	if (!txtDate.readOnly && !txtDate.disabled)
		txtDate.value = getFullDay(day) + "-" + getFullMonth(month-1) + "-" + year;
	txtDate.onchange();
	dialog.hide();
}

function initCalendarStrings(calObj) {
	if (empty(calObj)) {
		return;
	}
	calObj.cfg.setProperty("MONTHS_SHORT", gMonthShortNames);
	calObj.cfg.setProperty("MONTHS_LONG", gMonthLongNames);
	calObj.cfg.setProperty("WEEKDAYS_LONG", gDayLongNames);
	calObj.cfg.setProperty("WEEKDAYS_SHORT", gDayShortNames);
	calObj.cfg.setProperty("WEEKDAYS_MEDIUM", gDayMediumNames);
	calObj.cfg.setProperty("WEEKDAYS_1CHAR", gDayCharNames);
}

function trimAllDashes(sString)
{
	while (sString.substring(0,1) == '-') {
		sString = sString.substring(1, sString.length);
	}
	while (sString.substring(sString.length-1, sString.length) == '-'){
		sString = sString.substring(0,sString.length-1);
	}
	return sString;
}

// function to Allow numbers and hyphen only
function enterNumOnlyANDhipWithArrowsBkspace(e)
{
	var key=0;
	if(window.event || !e.which){
		key = e.keyCode;
	}
	else{
		key = e.which;
	}
	if((key>=48)&&(key<=57)||key==8||key==9||key==45 || key==0 ||key==37||key==38||key==39 || key==40){
		key=key;
		return true;
	}
	else{
		key=0;
		return false;
	}
}

/*
 * Deprecated: please use validateDateFieldId
 */
function checkDateValidation(field, validationType) {
	return doValidateDateFieldId(field, validationType);
}

/*
 * Deprecated: please use validateDateField
 */
function calendarDateValidation(Obj, valid) {
	return doValidateDateField(Obj, valid);
}

/*
 * Deprecated: use parseDateStr instead
 */
function parseDate(dtStr){
	return parseDateStr(dateStr);
}

/*
 * convert a digit month  to double digit month (ex :'m' to 'mm' format)
 */
function getFullMonth(month) {
	if((month+1)<10){
		month="0"+(month+1);
	} else {
		month=month+1;;
	}
	return month;
}

/*
 * convert a single digit date to double digit date (ex :'d' to 'dd' format)
 */
function getFullDay(day){
	if (day<10) {
		day="0"+day;
	} else {
		day=day;
	}
	return day;
}

/*
 * Gets the server date, requires that you have in your JSP, some script defined
 * like this:
 *   <script>
 *     var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
 *   </script>
 * Then, based on gServerNow, without doing an AJAX call, the server date will
 * be available to you. Otherwise, the browser's date will be returned.
 */
function getServerDate() {
	if (gServerNow != null) {
		return new Date(gServerNow);
	}
	gServerNow = new Date();
	return new Date(gServerNow);
}

/*  function for convert dd-mm-yyyy date formt to yyyy-mm-dd*/

function getDateFormat(selecteddate){
	var day=selecteddate.substring(0,2);
	var month = selecteddate.substring(selecteddate.indexOf("-")+1,selecteddate.lastIndexOf("-"))
	var year =selecteddate.substring(selecteddate.lastIndexOf("-")+1)
	var dateformat = year+'-'+month+'-'+day;
	return dateformat;
}


/*
 * Returns the result of comparison (-1, 0, 1) of two dates in string format:
 *   dd-mm-yyyy
 * (you are discouraged to use this. Please convert strings to actual dates
 * and just to a > or < comparison, or do a + or - to get new date values)
 */
function getDateDiff(fromdate,todate){
	var today = new Date();
	var date1,month1,year1;
	var date2,month2,year2;

	if(fromdate==""){
		fromdate="";
	}else{
	date1 = fromdate.substring(0,2);
	month1 = fromdate.substring(fromdate.indexOf("-")+1,fromdate.lastIndexOf("-"));
	year1 = fromdate.substring(fromdate.lastIndexOf("-")+1);
	}

	if (todate == ""){
		todate = "";
	}else{
	date2 = todate.substring(0,2);
	month2 = todate.substring(todate.indexOf("-")+1,todate.lastIndexOf("-"));
	year2 = todate.substring(todate.lastIndexOf("-")+1);
	}

	if (year1 > year2) return -1;
	else if (year1 < year2) return 1;
	else if (month1 > month2) return -1;
	else if (month1 < month2) return 1;
	else if (date1 > date2)	return -1;
	else if (date1 < date2) return 1;
	else return 0;
}

/*
 * Following functions set a pair of dates to quick (shortcut) values
 * based on the current date.
 */
function setDateRangeYesterday(fromEl, toEl) {
	var today = getServerDate();
	var yesterday = new Date(today);
	yesterday.setDate(today.getDate() - 1);

	var dateStr = formatDate(yesterday);

	fromEl.value = dateStr;
	toEl.value = dateStr;
}

function setDateRangeTomorrow(fromEl, toEl) {
	var today = getServerDate();
	var tomorrow = new Date(today);
	tomorrow.setDate(today.getDate() + 1);

	var dateStr = formatDate(tomorrow);

	fromEl.value = dateStr;
	toEl.value = dateStr;
}

function setDateRangeToday(fromEl, toEl) {
	var today = getServerDate();
	var dateStr = formatDate(today);

	fromEl.value = dateStr;
	toEl.value = dateStr;
}

function setDateRangeWeek(fromEl, toEl) {
	var today = getServerDate();
	var fromDate = new Date(today);
	var toDate = new Date(today);
	var dayOfWeek = today.getDay();		// 0-6, sun-sat

	dayOfWeek = dayOfWeek == 0 ? 6 : dayOfWeek -1;		// 0-6 mon-sun

	fromDate.setDate(today.getDate() - dayOfWeek);
	toDate.setDate(today.getDate() - dayOfWeek + 6);

	fromEl.value = formatDate(fromDate);
	toEl.value = formatDate(toDate);
}

function setDateRangeMonth(fromEl, toEl) {
	var today = new Date(getServerDate());

	var fromDate = new Date(today.getFullYear(), today.getMonth(), 1);
	var toDate = new Date(today.getFullYear(), today.getMonth() +1, 0);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangePreviousMonth(fromEl, toEl) {
	var today = new Date(getServerDate());

	var fromDate = new Date(today.getFullYear(), today.getMonth() -1, 1);
	var toDate = new Date(today.getFullYear(), today.getMonth(), 0);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangeYear(fromEl, toEl) {
	var today = getServerDate();

	var fromDate = new Date(today);
	fromDate.setDate(1);
	fromDate.setMonth(0);

	var toDate = new Date(today);
	toDate.setMonth(11);
	toDate.setDate(31);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangeFinancialYear(fromEl, toEl) {
	var today = new Date(getServerDate());
	var fromDate='';
	var toDate='';
	st_month = fin_yr_start_month-1;
	en_month = fin_yr_end_month-1;
	if(st_month>0){
		if (today.getMonth() > en_month) {  //Ex:- apr - dec
			fromDate = new Date(today.getFullYear(), st_month, 1);		// this year, 1 apr
			toDate   = new Date(today.getFullYear() + 1, en_month+1, 0); // next year, 31 mar
			// 0- will give the previous month last date.hence we are incrementing month to 1 and giving 0 to get last date.
		} else {	// jan - mar
			fromDate = new Date(today.getFullYear() - 1, st_month, 1);		// prvs year, 1 apr
			toDate   = new Date(today.getFullYear(), en_month+1, 0); // this year, 31 mar
		}
	} else{
			fromDate = new Date(today.getFullYear(), st_month, 1);
			toDate = new Date(today.getFullYear(), en_month, 31);
	}
	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangePreviousFinancialYear(fromEl, toEl) {
	var today = new Date(getServerDate());
	var fromDate='';
	var toDate='';
	st_month = fin_yr_start_month-1;
	en_month = fin_yr_end_month-1;
	if(st_month>0){
		if (today.getMonth() > en_month) {  // apr - dec
			var fromDate = new Date(today.getFullYear() - 1, st_month, 1);		// prvs year, 1 apr
			var toDate   = new Date(today.getFullYear(), en_month+1, 0); // this year, 31 mar
		} else {	// jan - mar
			var fromDate = new Date(today.getFullYear() - 2, st_month, 1);		// prvs prvs year, 1 apr
			var toDate   = new Date(today.getFullYear() - 1, en_month+1, 0); // prvs year, 31 mar
		}
	} else{
			fromDate = new Date(today.getFullYear()-1, st_month, 1);
			toDate = new Date(today.getFullYear()-1, en_month, 31);
	}
	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangePreviousNMonths(fromEl, toEl, n) {
	var today = new Date(getServerDate());

	var fromDate = new Date(today.getFullYear(), today.getMonth() -n, 1);
	var toDate = new Date(today.getFullYear(), today.getMonth(), 0);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangePreviousNDays(fromEl, toEl, n) {
	var today = new Date(getServerDate());

	var fromDate = new Date();
	fromDate.setDate(today.getDate() - n);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(today);
}

function setDateRangePreviousYear(fromEl, toEl) {
	var today = getServerDate();

	var fromDate = new Date(today.getFullYear() - 1, 0, 1);
	var toDate   = new Date(today.getFullYear() -1, 11, 31);

	fromEl.value = formatDate(fromDate);
	if (toEl != null)
		toEl.value = formatDate(toDate);
}

function setDateRangePreviousWeek(fromEl, toEl) {
	var today = getServerDate();

	var toDate = new Date(today);
	var fromDate = new Date(today);
	var dayOfWeek = today.getDay();
	dayOfWeek = dayOfWeek == 0 ? 6 : dayOfWeek -1;

	var weekFirstDay = today.getDate() - dayOfWeek;
	var prvWeekLDay = weekFirstDay-1;

	fromDate.setDate(prvWeekLDay-6);
	toDate.setDate(prvWeekLDay);

	fromEl.value = formatDate(fromDate);
	toEl.value = formatDate(toDate);
}

/*
 * Convenience validators for commonly used from/to date ranges in many places,
 * especially reports. Assumes both from and to are mandatory.
 */
function validateFromToDate(fromObj, toObj) {

	if (!doValidateDateField(fromObj))
		return false;
	if (!doValidateDateField(toObj))
		return false;

	if (fromObj.value == "")  {
		alert("From Date is required");
		return false;
	}
	if (toObj.value == "")  {
		alert("To Date is required");
		return false;
	}

	var fromDt = getDateFromField(fromObj);
	var toDt = getDateFromField(toObj);

	if ( (toDt != null) && (fromDt != null) ) {
		if (fromDt > toDt) {
			alert("To date cannot be less than from date");
			return false;
		}
	}
	return true;
}

function validateFromToDateTime(fromDateO, fromTimeO, toDateO, toTimeO, fromReqd, toReqd) {

	// ensure all required values are given
	if (fromReqd == undefined || fromReqd == true) {
		if (fromDateO.value == "")  {
			alert("From Date is required");
			fromDateO.focus();
			return false;
		}
		if (fromTimeO.value == "")  {
			alert("From Time is required");
			fromTimeO.focus();
			return false;
		}
	}
	if (toReqd == undefined || toReqd == true) {
		if (toDateO.value == "")  {
			alert("To Date is required");
			toDateO.focus();
			return false;
		}
		if (toTimeO.value == "")  {
			alert("To Time is required");
			toTimeO.focus();
			return false;
		}
	}

	if (!doValidateDateField(fromDateO)) return false;
	if (!doValidateDateField(toDateO)) return false;
	if (!validateTime(fromTimeO)) return false;
	if (!validateTime(toTimeO)) return false;

	if (fromDateO.value != "" && fromTimeO.value != "" &&
			toDateO.value != "" && toTimeO.value != "") {

		var fromDateTime = getDateTimeFromField(fromDateO, fromTimeO);
		var toDateTime = getDateTimeFromField(toDateO, toTimeO);

		if (fromDateTime >= toDateTime){
			alert("To date/time must be more than From date/time");
			toDateO.focus();
			return false;
		}
	}
	return true;
}

/**  To get Expected Date Time by adding a specific duration for a given date time. */

function getExpectedToDateTime(fromDateStr, fromTimeStr, hours) {

	var dt = getDateTime(fromDateStr, fromTimeStr);
	var milliseconds = dt.getTime();

	var milliSecsToAdd = hours * 60 * 60 * 1000;
	var totalMilliSecs = milliseconds + milliSecsToAdd;

	dt.setTime(totalMilliSecs);
	var toDateTimeArray = [];
	toDateTimeArray[0] = formatDate(dt, 'ddmmyyyy', '-');
	toDateTimeArray[1] = formatTime(dt, false);
	return toDateTimeArray;
}

/******************* Credit card date validation *************************/

/*
 * Validates that a date is in the correct format, MM-YY, or any
 * format that can be converted to something acceptable by Date.
 * Returns an error string or null if the date is valid.
 */
function validateCardDateFormat(dateStr) {

	var dateSeparator = (dateStr.indexOf("/") != -1) ? "/" : "-";
	var myarray = dateStr.split(dateSeparator);

	if (myarray.length != 2) {
		return "Incorrect date format: please use MM-YY (or) MM/YY";
	}

	var mth = myarray[0];
	var yr = myarray[1];

	if (!isInteger(mth)) {
		return "Incorrect date format: month is not a number";
	}

	if (!isInteger(yr)) {
		return "Incorrect date format: year is not a number";
	}

    if (parseInt(mth) > 12) {
        return "Invalid date: please enter 1-12 for month";
    }

    if (yr.length != 2) {
		return "Invalid date: year must be a 2-digit";
	}

	return null;
}

/*
 * Validate a date string. Assumes properly formatted MM-YY,
 * no two-digit or other formats accepted. Use validateFormat and
 * cleanDateStr to ensure that format is correct.
 */
function validateCardDateStr(dateStr, validType) {
	var errorStr = validateCardDateFormat(dateStr);
	if (errorStr != null) {
		return errorStr;
	}

	var dateSeparator = (dateStr.indexOf("/") != -1) ? "/" : "-";
	var myarray = dateStr.split(dateSeparator);

	var m = parseInt(myarray[0],10);
	var y = parseInt(myarray[1],10);

	y = convertTwoDigitYear(y, validType);

	// For date format -- date is taken as 1 i.e first of that month.
	var dt = new Date(y, m-1, 1);

	// new Date can automatically convert 29 feb to 01 mar. We need to disallow it.
	if ( (m != dt.getMonth()+1) || (y != dt.getFullYear()) ) {
		return "Date is not valid for given month/year combination";
	}

	if (validType != null) {
		return validateDate(dt, validType);
	} else {
		return null;
	}
}

// Argument of date is in dd-mm-yyyy and time is in hh:mm format.
function mergeDateAndTime(date, time) {

		var dateArray=date.split("-");

	    var d = parseInt(dateArray[0],10);
	    var m = parseInt(dateArray[1],10);
	    var y = parseInt(dateArray[2],10);

	    var timeArray =time.split(":");
	    var hh = parseInt(timeArray[0],10);
	    var mm = parseInt(timeArray[1],10);

	    var dt = new Date(y, m-1, d, hh, mm);

	    // new Date can automatically convert 29 feb to 01 mar. We need to disallow it.
	    if ( (d != dt.getDate()) || (m != dt.getMonth()+1) || y != dt.getFullYear() ) {
	        alert(getString("js.common.date.is.not.valid.for.given.month.year.string"));
	        return null;
	    }

	    return dt;
}

