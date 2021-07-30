	function validate() {

		var packagecategory = document.getElementById('package_category').value.trim();
		if (empty(packagecategory)) {
			alert('Please enter package category name.');
			document.getElementById('package_category').focus();
			return false;
		}

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newPackageCategory = trimAll(document.packageCategoryMaster.package_category.value);

		if(document.packageCategoryMaster._method.value != 'update'){
			for(var i=0;i<chkpackageCategory.length;i++){
				item = chkpackageCategory[i];
				if (newPackageCategory == item.PACKAGE_CATEGORY){
					alert(document.packageCategoryMaster.package_category.value+" already exists pls enter other name...");
			    	document.packageCategoryMaster.package_category.value='';
			    	document.packageCategoryMaster.package_category.focus();
			    	return false;
				}
			}
		}

		if(document.packageCategoryMaster._method.value == 'update'){
		  		if (backupName != newPackageCategory){
					for(var i=0;i<chkpackageCategory.length;i++){
						item = chkpackageCategory[i];
						if(newPackageCategory == item.package_category){
							alert(document.packageCategoryMaster.package_category.value+" already exists pls enter other name.");
					    	document.packageCategoryMaster.package_category.focus();
					    	return false;
		  				}
		  			}
		 		}
		 	}
		return true;
	}

	var rAutoComp;
	function autoPackageCategoryMaster() {
		var datasource = new YAHOO.util.LocalDataSource({result: packageCategory});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "PACKAGE_CATEGORY"},{key : "PACKAGE_CATEGORY_ID"} ]
		};
		var rAutoComp = new YAHOO.widget.AutoComplete('package_category','packagecategorycontainer', datasource);
		rAutoComp.minQueryLength = 0;
	 	rAutoComp.maxResultsDisplayed = 20;
	 	rAutoComp.forceSelection = false ;
	 	rAutoComp.animVert = false;
	 	rAutoComp.resultTypeList = false;
	 	rAutoComp.typeAhead = false;
	 	rAutoComp.allowBroserAutocomplete = false;
	 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		rAutoComp.autoHighlight = true;
		rAutoComp.useShadow = false;
	 	if (rAutoComp._elTextbox.value != '') {
				rAutoComp._bItemSelected = true;
				rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
		}
	}