var tree;
var finalArray = new Array();

    function treeInit() {

    	if(document.emrTreeForm.filterType != null  && docsFound) {
		    if(filterTypeFromAction != 'null' &&  document.emrTreeForm.filterType.type=='select-one'){
				 for( j=0;j<document.emrTreeForm.filterType.options.length;j++){
					if(document.emrTreeForm.filterType.options[j].value == filterTypeFromAction){
						document.emrTreeForm.filterType.options.selectedIndex = j;
						break;
					}
				}
			}

			if (document.emrTreeForm.filterType.value == "visits") {
				document.getElementById('documentTypeSearch').style.display='block';
			} else {
				document.getElementById('datesearch').style.display='block';
			}
	        buildRandomTextNodeTree();
        }
        if (document.patientSearch != null && document.patientSearch.mr_no != null)
			document.patientSearch.mr_no.focus();
    }

 function docTypeChange(isInclude) {
 	if (isInclude) {
 		document.emrTreeForm.exdocType.disabled = document.emrTreeForm.indocType.value != "*";
 	} else {
 		document.emrTreeForm.indocType.disabled = document.emrTreeForm.exdocType.value != "*";
 	}
 }

 function buildRandomTextNodeTree() {
		//instantiate the tree:
        tree = new YAHOO.widget.TreeView("treeDiv1");

        buildVisitBranch();

       // Expand and collapse happen prior to the actual expand/collapse,
       // and can be used to cancel the operation
       tree.subscribe("expand", function(node) {
              // return false; // return false to cancel the expand
           });

       tree.subscribe("collapse", function(node) {
           });

       // Trees with TextNodes will fire an event for when the label is clicked:
       tree.subscribe("labelClick", function(node, event) {
       		if (node.parent != "RootNode" ) {
       			createEMRRHS(node);
			}
		});

		// following code scrolls the page to the top of the window.
		var myIframe = document.getElementById('display1');
		myIframe.onload = function () {
			window.scroll(0, 0);
		}

		//The tree is not created in the DOM until this method is called:
        tree.draw();
        //This is to keep the tree expanded onload
        //tree.getRoot().expandAll();
  }

	function  expandTree() {
		tree.getRoot().expandAll();
	}

	//function builds  children for the node you pass in:

    function buildVisitBranch() {
    	var filterType = document.emrTreeForm.filterType.value;
		var firstDoc = true;

		for ( var i in allDocsList ) {
			for (var j in allDocsList[i]) {
				 var rootNodeName =  allDocsList[i][j]["filterId"];

				 if(rootNodeName == "GEN" )
				 	rootNodeName = "Documents [No Visits]";
				 else
					rootNodeName = allDocsList[i][j]["label"];

				  var tmpNode = new YAHOO.widget.TextNode(rootNodeName, tree.getRoot(), false);

				var filterId = allDocsList[i][j]["filterId"];
				var title = allDocsList[i][j]["label"];
				var docList = allDocsList[i][j]["viewDocs"];
				for ( var k in docList ) {
						var nodeArray  = new Array ();
						if(docList[k]["title"] != null){
							var textNodeName = docList[k]["title"];
							if(filterType == "visits"){
								for(var d in docTypeDetails){
				   		 			if(docList[k]["type"] == docTypeDetails[d]["DOC_TYPE_ID"]){
				   		 				if(docTypeDetails[d]["PREFIX"] != null && docTypeDetails[d]["PREFIX"] !=""){
											textNodeName = '['+docTypeDetails[d]["PREFIX"]+']'+' - '+textNodeName;
										}
										break;
									}
					 	 	 	}
							}
							if(emrurldate == 'D') {
								textNodeName += (docList[k]["date"]) ? " - (" + formatDate(new Date(docList[k]["date"])) + ")": "";
							} else {
								textNodeName += (docList[k]["visitDate"]) ? " - (" + formatDate(new Date(docList[k]["visitDate"])) + ")": "";
							}
							n1 = new YAHOO.widget.TextNode(textNodeName, tmpNode, false);
							n1.target="display1";
							if (docList[k]['description'] != null)
								n1.title = docList[k]['description']
							var test = {	'displayUrl':docList[k]["displayUrl"],
											'Type':docList[k]["type"],
											'Visit':docList[k]["visitid"],
											'Label':n1 ,
											'Doctor': docList[k]["doctor"],
											'visitDate': docList[k]["visitDate"],
											'docDate': docList[k]["date"],
											'UpdatedBy':docList[k]["updatedBy"],
											'UpdatedDate':docList[k]["updatedDate"],
											"Authorized":docList[k]["authorized"],
											"Title": docList[k]["title"],
											"externalLink" : docList[k]["externalLink"],
											"DocId" : docList[k]["docid"],
											"Provider" : docList[k]["provider"]

										};
							var authorized = docList[k]["authorized"];
							finalArray = finalArray.concat(test);
							if (!authorized) {
								cpath+docList[k]["displayUrl"];
							} else {
								// if it is a external link do not add the context path to it.
								if (docList[k]['externalLink']) {
									n1.href = docList[k]["displayUrl"];
								} else {
									n1.href = cpath+docList[k]["displayUrl"];
								}
							}

							if (firstDoc) {
								if (authorized) {
									var url = null;
									if (docList[k]['externalLink']) {
										url = docList[k]["displayUrl"];
										document.getElementById('printerId').disabled = true;
									} else {
										url = cpath+docList[k]["displayUrl"];
									}
									window.open(url, "display1");
								}

								createNavigateLinks("0");
								var DocDate = "";
								if (docList[k]["date"]) {
									var epochdate = new Date(docList[k]["date"]);
									DocDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
								}
								var visitDate = "";
								if (docList[k]["visitDate"]) {
									var epochdate = new Date(docList[k]["visitDate"]);
									visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
								}

								var type = docList[k]["type"];
								for(var d in docTypeDetails) {
		                                if(type == docTypeDetails[d]["DOC_TYPE_ID"]){
	                	                	type = docTypeDetails[d]["DOC_TYPE_NAME"];
	                                         break;
	                                    }
	                            }
								createDocSummary(document.getElementById("documentSummary"),
									type, docList[k]["doctor"], docList[k]["visitid"], visitDate, DocDate, docList[k]["updatedBy"],
									docList[k]["updatedDate"], docList[k]["title"], docList[k]["docid"], docList[k]["provider"], 
									docList[k]["displayUrl"]);
								firstDoc = false;
							}

						}
				}
			}
		}
    }

function getEMRSearchResults(){
	var filterType = document.emrTreeForm.filterType.value;
	document.emrTreeForm.mr_no.value = mrnoFromAction;
	if (filterType=="visits") {
		document.getElementById('documentTypeSearch').style.display='block';
		document.getElementById('datesearch').style.display='none';
	} else {
		document.getElementById('datesearch').style.display='block';
		document.getElementById('documentTypeSearch').style.display='none';
	}
	// document.emrTreeForm.submit();
}

function submitValues() {
	var filterType = document.emrTreeForm.filterType.value;
	document.emrTreeForm.mr_no.value = mrnoFromAction;
	document.emrTreeForm.VisitId.value = VisitId;
	document.emrTreeForm.submit();
}

function submitVisit() {
	document.emrTreeForm.submit();
}

function createEMRRHS(node){

	var selectedIndex = 0;
	var externalLink = false;
	for (var i in finalArray) {
	  for (var j in finalArray[i]) {
	  		if (node == finalArray[i]["Label"]){
	  			selectedIndex = i;
	  			externalLink = finalArray[i]["externalLink"];
	  			var myTable = document.getElementById("documentSummary");
				var docDate = "";
				if (finalArray[i]["docDate"]) {
					var epochdate = new Date(finalArray[i]["docDate"]);
					docDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
				}
				var visitDate = "";
				if (finalArray[i]["visitDate"]) {
					var epochdate = new Date(finalArray[i]["visitDate"]);
					visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
				}
				var type = finalArray[i]["Type"];
				for(var d in docTypeDetails){
	                                if(type == docTypeDetails[d]["DOC_TYPE_ID"]){
                	                        type = docTypeDetails[d]["DOC_TYPE_NAME"];
                                                break;
                                        }
                                }
				createDocSummary(myTable, type, finalArray[i]["Doctor"], finalArray[i]["Visit"], visitDate, docDate,
						finalArray[i]["UpdatedBy"], finalArray[i]["UpdatedDate"], finalArray[i]["Title"], finalArray[i]["DocId"], 
						finalArray[i]["Provider"], finalArray[i]["displayUrl"]);
				break;
	  		}
		}
	}
	document.getElementById('printerId').disabled = externalLink;
	createNavigateLinks(selectedIndex);
}


function createNavigateLinks(selectedIndex){

	var imgLink = document.getElementById('imgLink');
	var prevLink = document.getElementById('prevLink');
	var nextLink = document.getElementById('nextLink');

	prevLink.setAttribute("onclick", "return funGetPrevious(" + selectedIndex + " , this)");
	nextLink.setAttribute("onclick", "return funGetNext("+ selectedIndex + ", this)");
	if (imgLink != null)
		imgLink.setAttribute("onclick", "return funGetInNewWindow("+selectedIndex+ ", this)");
}

function funGetInNewWindow(index,anchor){

		var href = finalArray[parseInt(index)]["displayUrl"];
		var externalLink = finalArray[parseInt(index)]["externalLink"];
		if (externalLink) {
			// no need to do any extra processing on the href. and no need to append the context path.
			 anchor.setAttribute("href", href);
			 return true;
		}
		href = cpath + href;

		/**
		* allFields : used for op case sheet and prescription print.
		* in display : it was set to Y (display all fields).
		* when taking the print we have to set it to 'N' get the print with only printable fields and values.
		*/
		href = href.replace('allFields=Y', 'allFields=N');

		var tem = href.split("&forcePdf");
		var temF = tem[0];
		var temS = tem[1];
		if(temS != null && !temS == '') {
			var nHref = '';
			var endPart = '';
			var s = temS.split("&");
			for (var i=1; i<s.length; i++ )
				endPart = endPart + "&"+s[i];
			if(endPart != null && !endPart == '')
				nHref = temF + endPart;
		else
			nHref = temF;
		}
		else
			nHref = temF;

		var printerId = document.emrTreeForm.printerId.value;
		var parts = nHref.split("printerId=");
		var fPart = parts[0];
		var lPart = parts[1];
		var ePart = '';
		var remParams = lPart!=null? lPart.split("&") : '';
		for (var i=1; i<remParams.length; i++)
			ePart = ePart + "&"+remParams[i];
		if (ePart != '' )
			href = fPart + ePart +"&printerId="+printerId;
		else
			href = fPart + "&printerId="+printerId;
		if (printerId != null && !printerId == '')
			anchor.setAttribute("href", href);
		else
			anchor.setAttribute("href", nHref);
			return true;

}

function funGetPrevious(index,anchor){

	if (index > 0) {
		var myTable = document.getElementById("documentSummary");
		var docDate = "";
		var visitDate = "";
		 if (finalArray[parseInt(index)-1]["Authorized"]) {
	         if (finalArray[parseInt(index)-1]["docDate"]) {
	         	var epochdate = new Date(finalArray[parseInt(index) -1]["docDate"]);
	                 docDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
	         }
	          if (finalArray[parseInt(index)-1]["visitDate"]) {
	         	var epochdate = new Date(finalArray[parseInt(index) -1]["visitDate"]);
	                 visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
	         }

	         var type = finalArray[parseInt(index)-1]["Type"];
	         for(var d in docTypeDetails){
	         	if(type == docTypeDetails[d]["DOC_TYPE_ID"]){
	                 	type = docTypeDetails[d]["DOC_TYPE_NAME"];
	                         break;
	                 }
	         }
			createDocSummary(myTable, type, finalArray[parseInt(index)-1]["Doctor"],
				finalArray[parseInt(index)-1]["Visit"], visitDate, docDate, finalArray[parseInt(index)-1]["UpdatedBy"],
				finalArray[parseInt(index)-1]["UpdatedDate"], finalArray[parseInt(index)-1]["Title"], finalArray[parseInt(index)-1]["DocId"], 
				finalArray[parseInt(index)-1]["Provider"], finalArray[parseInt(index)-1]["displayUrl"]);
			createNavigateLinks(parseInt(index-1));
			if (finalArray[parseInt(index)-1]["externalLink"])
				anchor.setAttribute("href", finalArray[parseInt(index)-1]["displayUrl"]);
			else
				anchor.setAttribute("href", cpath + finalArray[parseInt(index)-1]["displayUrl"]);

			document.getElementById('printerId').disabled = finalArray[parseInt(index)-1]["externalLink"];
			return true
	 	} else {
			funGetPrevious(index-1, anchor);
		}
	}else{
		anchor.setAttribute("href","#");
		alert("No Previous data to View");
		return false;
	}
}

function funGetNext(index,anchor){

	if(index < (finalArray.length-1)){
		if (finalArray[parseInt(index)+1]["Authorized"]){
			var myTable = document.getElementById("documentSummary");
			var docDate = "";
			var visitDate = "";
	                if (finalArray[parseInt(index)+1]["docDate"]) {
	                        var epochdate = new Date(finalArray[parseInt(index)+1]["docDate"]);
	                        docDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
	                }
	                if (finalArray[parseInt(index)+1]["visitDate"]) {
	                        var epochdate = new Date(finalArray[parseInt(index)+1]["visitDate"]);
	                        visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
	                }

	                var type = finalArray[parseInt(index)+1]["Type"];
	                for(var d in docTypeDetails){
	                        if(type == docTypeDetails[d]["DOC_TYPE_ID"]){
	                                type = docTypeDetails[d]["DOC_TYPE_NAME"];
	                                break;
	                        }
	                }

			createDocSummary(myTable, type, finalArray[parseInt(index)+1]["Doctor"],
				finalArray[parseInt(index)+1]["Visit"], visitDate, docDate, finalArray[parseInt(index)+1]["UpdatedBy"],
				finalArray[parseInt(index)+1]["UpdatedDate"], finalArray[parseInt(index)+1]["Title"], finalArray[parseInt(index)+1]["DocId"], 
				finalArray[parseInt(index)+1]["Provider"], finalArray[parseInt(index)+1]["displayUrl"]);

			createNavigateLinks(parseInt(index+1));
			if (finalArray[parseInt(index)+1]["externalLink"])
				anchor.setAttribute("href", finalArray[parseInt(index)+1]["displayUrl"]);
			else
				anchor.setAttribute("href", cpath + finalArray[parseInt(index)+1]["displayUrl"]);

			document.getElementById('printerId').disabled = finalArray[parseInt(index)+1]["externalLink"];
			return true;
		}else{
			funGetNext(index+1,anchor);
		}
	}else{
		anchor.setAttribute("href","#");
		alert("No Next data to View");
		return false;
	}
}

function createDocSummary(myTable, type, doctor, visit, visitDate, docDate, updatedBy, updatedDate, title, docid, provider, displayUrl) {

	if (type=='CI') {
		type='Clinical Information';
			if (visit=="") {
				visitDate="";
			} else {
				visitDate=visitDate;
			}
	} else {
		type=type;
	}
    if ( updatedBy == undefined) {
		updatedBy = "";
	}


	while (myTable.rows.length>0) {
		myTable.deleteRow(-1);
	}

	row = myTable.insertRow(-1);
	cell = row.insertCell(-1);
	cell.setAttribute("style", "width: 50px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Visit:";
	cell.id = "VisitId";

	cell = row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = visit;

	cell = row.insertCell(-1);
	cell.setAttribute("style", "width: 50px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Type:";

	cell = row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = type;

	cell = row.insertCell(-1);
	cell.setAttribute("style", "width: 50px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Doctor:";

	cell = row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = doctor;

	row = myTable.insertRow(-1);

	if(emrurldate == 'V') {
		cell = row.insertCell(-1);
		cell.setAttribute("style", "width: 60px");
		cell.setAttribute("class", "formlabel");
		cell.innerHTML = "Visit Date:";

		cell=row.insertCell(-1);
		cell.setAttribute("class", "forminfo");
		cell.innerHTML = visitDate;
	} else {
		cell = row.insertCell(-1);
		cell.setAttribute("style", "width: 60px");
		cell.setAttribute("class", "formlabel");
		cell.innerHTML = "Document Date:";

		cell=row.insertCell(-1);
		cell.setAttribute("class", "forminfo");
		cell.innerHTML = docDate;
	}

	cell = row.insertCell(-1);
	cell.setAttribute("style", "width: 50px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Title:";

	cell=row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = title;

	cell=row.insertCell(-1);
	cell.setAttribute("style", "width: 70px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Updated By:";

	cell=row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = updatedBy + (empty(updatedDate) ? "" : "(" + updatedDate + ")");

	if(!empty(orderUrl) && provider == "DIAGProvider" && type == 'Radiology Reports'){
		row = myTable.insertRow(-1);

		cell=row.insertCell(-1);
		cell.setAttribute("style", "width: 80px");
		cell.setAttribute("class", "formlabel");
		cell.innerHTML = "PACS Details:";

		cell=row.insertCell(-1);
		cell.setAttribute("class", "forminfo");
		cell.setAttribute("style", "width: 200px");
		cell.setAttribute("colspan", "5");
		
		var url = "";
		if (displayUrl.includes("DiagReportPrint"))
			url = cpath +'/emr/EMRMainDisplay.do?_method=getTestDetails&reportId='+docid;
		else if (displayUrl.includes("TestDocumentsPrint"))
			url = cpath +'/emr/EMRMainDisplay.do?_method=getTestDocumentDetails&docId='+docid;
		
		Ajax.get(url, function(data, status) {
			var item = eval("(" + data + ")");
			for (var i in item) {
				var aTag = document.createElement('a');
				aTag.setAttribute('target', '_blank');
				aTag.innerHTML = item[i].test_name;
				var replacedOUrl = orderUrl.replace('$O', item[i].prescribed_id);
				var replacedUrl = replacedOUrl.replace('$M', item[i].mr_no);
				aTag.setAttribute('href', replacedUrl);
				cell.appendChild(aTag);
				if(i != item.length-1)
					cell.appendChild(document.createTextNode(' | '));
			}
		});
	}
}