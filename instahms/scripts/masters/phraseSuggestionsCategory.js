	function validate() {

		var phraseSuggestionsCategoryName = document.getElementById('phrase_suggestions_category').value.trim();
		if (empty(phraseSuggestionsCategoryName)) {
			alert('Please enter phrase suggestions category name.');
			document.getElementById('phrase_suggestions_category').focus();
			return false;
		}

		var status = document.getElementById('status');
		if(document.phraseSuggestionsCategoryMaster._method.value == 'update') {
			var phraseSuggestionsCategoryId = document.getElementById('phrase_suggestions_category_id').value;
			if(status.value == 'I') {
				for(var i=0;i<phraseSuggestinList.length;i++){
					item = phraseSuggestinList[i];
					if (phraseSuggestionsCategoryId == item.phrase_suggestions_category_id){
						alert(document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.value+" is being used in Phrase Suggestions master ...");
				    	return false;
					}
				}
			}
		}

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newPhraseSuggestionsCategoryName = trimAll(document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.value);

		if(document.phraseSuggestionsCategoryMaster._method.value != 'update'){
			for(var i=0;i<chkPhraseSuggestionsCategory.length;i++){
				item = chkPhraseSuggestionsCategory[i];
				if (newPhraseSuggestionsCategoryName == item.phrase_suggestions_category){
					alert(document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.value+" already exists pls enter other name...");
			    	document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.value='';
			    	document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.focus();
			    	return false;
				}
			}
		}

		if(document.phraseSuggestionsCategoryMaster._method.value == 'update'){
		  		if (backupName != newPhraseSuggestionsCategoryName){
					for(var i=0;i<chkPhraseSuggestionsCategory.length;i++){
						item = chkPhraseSuggestionsCategory[i];
						if(newPhraseSuggestionsCategoryName == item.phrase_suggestions_category){
							alert(document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.value+" already exists pls enter other name.");
					    	document.phraseSuggestionsCategoryMaster.phrase_suggestions_category.focus();
					    	return false;
		  				}
		  			}
		 		}
		 	}
		return true;
	}

	var rAutoComp;
	function autoPhraseSuggestionCategoryMaster() {
		var datasource = new YAHOO.util.LocalDataSource({result: phraseSuggestionCategory});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "phrase_suggestions_category"},{key : "phrase_suggestions_category_id"} ]
		};
		var rAutoComp = new YAHOO.widget.AutoComplete('phrase_suggestions_category','phrasesuggestionscategorycontainer', datasource);
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