	function initAutoCity(cityName, localityId, citydropdown, cityId){
		var cityAuthJson = {result:cityStateCountryJSON};
		dataSource  = new YAHOO.util.LocalDataSource(cityAuthJson, { queryMatchContains : true } );
		dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		
		dataSource.responseSchema = {
				resultsList : "result.cities",
				fields: [{key: "name"},
						 {key: "id"}
						]
				};

		oAutoComp = new YAHOO.widget.AutoComplete(cityName, citydropdown, dataSource);
		
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.maxResultsDisplayed = 5;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
		oAutoComp.resultTypeList= true;
		oAutoComp._bItemSelected = false;

		oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
			var record = elItem[2];
			document.getElementById(cityId).value = record[1];
			onChangeCity(record[1], localityId);
		});
		
	}	
	
	function onChangeCity(cityId, localityId){
		
		var ajaxobj = newXMLHttpRequest();
		var url = '../../integration/practo/CityLocalityMapping.do?city_id=' + cityId;

		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		localityId.length = 0;
		var defaultOpt = document.createElement('option');
		defaultOpt.value = "";
		defaultOpt.innerHTML= "-- Select --";
		localityId.appendChild(defaultOpt);
		
		
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var obj = eval ("(" + ajaxobj.responseText + ")");
				var len = obj.localities.length;
				for (var i=0;i<len;i++){
				    var opt = document.createElement('option');
				    opt.value = obj.localities[i].id;
				    opt.innerHTML = obj.localities[i].name;
				    localityId.appendChild(opt);
				}
			}
				
		}
		
	}
	
	function checkIsCenterSelected() {
		var checkBoxes = document.publishForm._selectCenter;
		var anyChecked = false;
		var disabledCount = 0;
		var totalConsultations = 1;
		if (checkBoxes.length) {
			totalConsultations = checkBoxes.length;
			for (var i=0; i<checkBoxes.length; i++) {
				if (!checkBoxes[i].disabled && checkBoxes[i].checked) {
					anyChecked = true;
					break;
				}
			}

			for (var i=0; i<checkBoxes.length; i++) {
				if (checkBoxes[i].disabled)
					disabledCount++;
			}

		} else {
			var checkBox = document.publishForm._selectCenter;
			if (!checkBox.disabled && checkBox.checked)
				anyChecked = true;
			if (checkBox.disabled)
				disabledCount++;
		}
		if (!anyChecked) {
			if (disabledCount == totalConsultations) {
				showMessage("js.outpatientlist.patientsconsultation.cons.closed");
				return false;
			}
			alert("Check one or more Centers");
			return false;
		}
		
		document.publishForm.submit();
		
		
	}
