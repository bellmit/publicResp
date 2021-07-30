	function init() {
		displayNameAutoComplete();
	}
	function displayNameAutoComplete() {
		var datasource = new YAHOO.util.LocalDataSource({result: diplayNames});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "display_name"},{key : "recurrence_daily_id"} ]
		};
		var autoComp = new YAHOO.widget.AutoComplete('display_name','displaycontainer', datasource);
		autoComp.minQueryLength = 0;
	 	autoComp.maxResultsDisplayed = 20;
	 	autoComp.forceSelection = false ;
	 	autoComp.animVert = false;
	 	autoComp.resultTypeList = false;

		autoComp.dataRequestEvent.subscribe(cahngeRecurrencedailyId)
		autoComp.itemSelectEvent.subscribe(setRecurrenceDailyId);
		return autoComp;
	}

	function cahngeRecurrencedailyId() {
		document.recurrenceDailyMaster.recurrence_daily_id.value = '';
	}
	function setRecurrenceDailyId(oSelf, elItem) {
		var record = elItem[2];
		document.recurrenceDailyMaster.recurrence_daily_id.value = record.recurrence_daily_id;
	}

	function countNoOfActivities() {
		var timings = document.getElementById('timings').value;
		if (timings != null && timings != '') {
			var individualTimeField = timings.split(',');
			document.getElementById('num_activities').value = individualTimeField.length ;
			document.getElementById('noofact').textContent = individualTimeField.length;
		} else {
			document.getElementById('noofact').textContent = 0;
		}
	}

	function validateTimings() {
		var timings = document.getElementById('timings').value;
		if (timings == null || timings == '') {
			alert("timings should not be empty");
			document.getElementById('timings').focus();
			return false;
		}
		var individualTime = timings.split(',');
		for (var i=0 ; i<individualTime.length;i++) {
				var time = individualTime[i].replace('\n', '');
				time = time.trim();
				if (!isTime(time)) {
					alert("Invalid time format: all entries should be in HH:MM format");
					document.getElementById('timings').focus();
					return false;
				}
		}
		return true;
	}

	function doSave() {
		if (!validateTimings()) return false;
		alert("To Define Serving Frequency for Frequency \""
				+ document.getElementById('display_name').value
				+ "\", Contact your Administrator. Auto setup for MAR will not happen till the Serving  Frequency is done");
		recurrenceDailyMaster.submit();
		return true;
	}