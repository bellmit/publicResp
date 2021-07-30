
var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["poprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'pages/stores/poprint.do?_method=generatePOprint',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["poprint"]["description"]
	};

	toolbar.Validate= {
		title: toolbarOptions["validatepo"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/validatepo.do?_method=getValidatePoScreen',
		onclick: null,
		description: toolbarOptions["validatepo"]["description"],
		show: ( validateReq == 'true')
	};

	toolbar.Edit= {
		title: toolbarOptions["vieweditpo"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/poscreen.do?_method=getPOScreen',
		onclick: null,
		description: toolbarOptions["vieweditpo"]["description"]
	};

	toolbar.StockEntry= {
		title: toolbarOptions["stockentry"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/stockentry.do?_method=getScreen',
		onclick: null,
		description: toolbarOptions["stockentry"]["description"]
	};

	toolbar.Copy= {
		title: toolbarOptions["copypo"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/poscreen.do?_method=getCopyPoScreen',
		onclick: null,
		description: toolbarOptions["copypo"]["description"]
	};
	
	toolbar.Amend= {
			title: toolbarOptions["amendpo"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/stores/amendpo.do?_method=getPOScreen',
			onclick: null,
			description: toolbarOptions["amendpo"]["description"]
	};

var theForm = document.POSearchForm;

function init() {
	theForm = document.POSearchForm;
	setMultipleSelectedIndexs(theForm.supplier_id,suppArray);
	if ( max_centers_inc_default > 1 && !empty(userstoresList) )
		filterStores(theForm._center_id);
	theForm.po_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	checkstoreallocation();
}

function changeCancel (index){
	if (document.getElementById('_cancel'+index).checked == true) {
		document.getElementById('_hidcancel'+index).value = 'Y'
	}else document.getElementById('_hidcancel'+index).value = 'N'
}


function changeClose (index){
	if (document.getElementById('_close'+index).checked == true) {
		document.getElementById('_hidclose'+index).value = 'Y'
	}else {
		document.getElementById('_hidclose'+index).value = 'N'	
	}
	
}
function onSave() {
   var poTable = document.getElementById("resultTable");
   var numRows = poTable.rows.length-1;
   if (numRows == 0) {
	   	showMessage("js.stores.procurement.norecordssave");
	   	return false;
   }
	for (var k=0;k<numRows;k++) {
	  if (document.getElementById('_postatus'+k).value == 'O') {
		  if (document.getElementById('_close'+k).checked) {
		  	  if ((document.getElementById('_postatus'+k).value != 'O') || (document.getElementById('_count'+k).value > 0)) {
	          }else {
			    if (document.getElementById('_cancel'+k).checked) {
				  	  alert(document.getElementById('_pono'+k).value+" "+getString("js.stores.procurement.checkedbothclose.cancel")+" \n"+getString("js.stores.procurement.doeitherclose.cancel"));
				  	  return false;
		        } // if end cancel cond.
	          } // else end
		  } // if end close cond.
	   } // if end (main cond. status)
	 } // for loop end
	
 	theForm._method.value="savePOChanges";
    theForm.submit();
}  // fun end

function onKeyPressPOno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangePOno();
	} else {
		return true;
	}
}

function onChangePOno() {
	var poNoBox = theForm.po_no;

	// complete
	var valid = addPrefix(poNoBox, 'PO', 4);

	if (!valid) {
		showMessage("js.stores.procurement.invalidpoformat");
		theForm.po_no.value = ""
		theForm.po_no.focus();
		return false;
	}
}

function filterStores(centerObj){
    var userSelectedStoere = document.POSearchForm.store_id.value;
	if ( centerObj.value == '' )//default center
		loadSelectBox(document.POSearchForm.store_id,userstoresList, "dept_name", "dept_id",null,null);
	else
		loadSelectBox(document.POSearchForm.store_id,
				filterList(userstoresList, "center_id", centerObj.value), "dept_name", "dept_id",
				null,null);
	if ( document.POSearchForm.store_id.type == 'select-one' || document.POSearchForm.store_id.type == 'select-multiple'){
		if (!empty(paramStoreId))
			setMultipleSelectedIndexs(document.POSearchForm.store_id,paramStoreId);
		else
			setSelectedIndex(document.POSearchForm.store_id,userSelectedStoere);
	}
}

function checkstoreallocation() {
	 		if(gRoleId != 1 && gRoleId != 2) {
	 		if(deptId == "") {
	 		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		}
	 	}
	}



