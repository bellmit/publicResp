	function validate() {

		var phraseSuggestionName = document.getElementById('phrase_suggestions_desc').value.trim();
		if (empty(phraseSuggestionName)) {
			alert('Please enter Phrase Suggestions name.');
			document.getElementById('phrase_suggestions_desc').focus();
			return false;
		}
		var phraseSuggestionCategory = document.getElementById('phrase_suggestions_category_id').value.trim();
		if (empty(phraseSuggestionCategory)) {
			alert('Please select Phrase Suggestion Category.');
			document.getElementById('phrase_suggestions_category_id').focus();
			return false;
		}

		return true;
	}



	var rAutoComp;
	function autoPhraseSuggestionMaster() {
		var datasource = new YAHOO.util.LocalDataSource({result: phraseSuggestions});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "phrase_suggestions_desc"},{key : "phrase_suggestions_id"} ]
		};
		var rAutoComp = new YAHOO.widget.AutoComplete('phrase_suggestions_desc','phrasesuggestionscontainer', datasource);
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
