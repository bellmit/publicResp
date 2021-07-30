var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/InventoryItemMaster.do?_method=show',
		onclick: null,
		description: "View/Edit Item Details"
	},

};

var theForm = document.itemListSearchForm;

function init() {
	theForm = document.itemListSearchForm;
	theForm.item_name.focus();
	createToolbar(toolbar);
	autoItem();
	automanf();
}
var oAutoItem;
function autoItem(){
	YAHOO.example.ACJSArray = new function(){
		var dataSource = new YAHOO.widget.DS_JSArray(itemList);

		oAutoItem = new YAHOO.widget.AutoComplete("item","itemcontainer",dataSource);
		oAutoItem.minQueryLength = 0;
		oAutoItem.typeAhead = false;
		oAutoItem.prehighlightClassname = "yui-ac-prehighlight";
		oAutoItem.autoHighlight = true;
		oAutoItem.useShadow = false;
		oAutoItem.forceSelection = true;
		oAutoItem.maxResultsDispalyed = 5;
		oAutoItem.allowBroserAutocomplete = false;
		oAutoItem.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get("item").value;
			if (sInputValue.length === 0){
				var oSelf = this;
				setTimeout(oSelf.sendQuery(sInputValue),0);
			}
		});
	}
}

var oAutoManf;
function automanf(){
	YAHOO.example.ACJSArray = new function(){
		datasource = new YAHOO.widget.DS_JSArray(manfList);

		oAutoManf = new YAHOO.widget.AutoComplete("manf_name","manfcontainer",datasource);
		oAutoManf.minQueryLength = 0;
		oAutoManf.typeAhead = false;
		oAutoManf.prehighlightClassname = "yui-ac-prehighlight";
		oAutoManf.autoHighlight = true;
		oAutoManf.useShadow = false;
		oAutoManf.forceSelection = true;
		oAutoManf.maxResultsDispalyed = 5;
		oAutoManf.allowBroserAutocomplete = false;
		oAutoManf.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get("manf_name").value;
			if (sInputValue.length === 0){
				var oSelf = this;
				setTimeout(oSelf.sendQuery(sInputValue),0);
			}
		});
	}
}