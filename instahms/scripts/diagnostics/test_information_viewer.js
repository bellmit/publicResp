var tree;
var finalArray = new Array();

function treeInit() {
	if (docsFound) {
        buildRandomTextNodeTree();
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
	var firstDoc = true;
	
	var tmpNode = null;
	var centerName = null;
	var docCountPerCenter = 1;
	for (var i=0; i<allDocsList.length; i++) {
		 var record = allDocsList[i];
		 var rootNodeName =  record.center_name;
		 
		 if (centerName != rootNodeName) {
			 tmpNode = new YAHOO.widget.TextNode(rootNodeName, tree.getRoot(), false);
			 docCountPerCenter = 1;
		 }
		 centerName = record.center_name;
		 if (record["title"] != null) {
			 var textNodeName = record["title"];
			 textNodeName += ' (' + docCountPerCenter + ') ';
			 textNodeName += (record["date"]) ? " - (" + formatDate(new Date(record["date"])) + ")": "";
			 
			 docCountPerCenter++;
			 
			 n1 = new YAHOO.widget.TextNode(textNodeName, tmpNode, false);
			 n1.target="display1";
			 if (record['description'] != null)
				 n1.title = record['description']
			 var test = {	'displayUrl':record["displayUrl"],
							'Type':record["type"],
							'Visit':record["visitid"],
							'Label':n1 ,
							'Doctor': record["doctor"],
							'visitDate': record["visitDate"],
							'docDate': record["date"],
							'UpdatedBy':record["updatedBy"],
							'UpdatedDate':record["updatedDate"],
							"Title": record["title"],
							"externalLink" : record["externalLink"],
							"DocId" : record["docid"],

						};
			 finalArray = finalArray.concat(test);
			 // if it is a external link do not add the context path to it.
			 if (record['externalLink']) {
				n1.href = record["displayUrl"];
			 } else {
				 n1.href = cpath+record["displayUrl"];
			 }

			 if (firstDoc) {
				 var url = null;
				 if (record['externalLink']) {
					 url = record["displayUrl"];
					 document.getElementById('printerId').disabled = true;
				 } else {
					 url = cpath+record["displayUrl"];
				 }
				 window.open(url, "display1");

				 createNavigateLinks("0");
				 var DocDate = "";
				 if (record["date"]) {
					 var epochdate = new Date(record["date"]);
					 DocDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
				 }
				 var visitDate = "";
				 if (record["visitDate"]) {
					 var epochdate = new Date(record["visitDate"]);
					 visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
				 }

				 var type = record["type"];
				 for (var d in docTypeDetails) {
					 if (type == docTypeDetails[d]["DOC_TYPE_ID"]){
						 type = docTypeDetails[d]["DOC_TYPE_NAME"];
						 break;
					 }
				 }
				 createDocSummary(document.getElementById("documentSummary"),
						 type, record["doctor"], record["visitid"], visitDate, DocDate, record["updatedBy"],
						 record["updatedDate"], record["title"], record["docid"]);
				 firstDoc = false;
			}

		}
	}
}

function createEMRRHS(node){

	var selectedIndex = 0;
	var externalLink = false;
	for (var i in finalArray) {
		var record = finalArray[i]
  		if (node == record["Label"]){
  			selectedIndex = i;
  			externalLink = record["externalLink"];
  			var myTable = document.getElementById("documentSummary");
			var docDate = "";
			if (record["docDate"]) {
				var epochdate = new Date(record["docDate"]);
				docDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
			}
			var visitDate = "";
			if (record["visitDate"]) {
				var epochdate = new Date(record["visitDate"]);
				visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
			}
			var type = record["Type"];
			for (var d in docTypeDetails){
                if (type == docTypeDetails[d]["DOC_TYPE_ID"]){
                	type = docTypeDetails[d]["DOC_TYPE_NAME"];
                    break;
                }
            }
			createDocSummary(myTable, type, record["Doctor"], record["Visit"], visitDate, docDate,
					record["UpdatedBy"], record["UpdatedDate"], record["Title"], record["DocId"]);
			break;
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

function funGetInNewWindow(index, anchor){

	var href = finalArray[parseInt(index)]["displayUrl"];
	var externalLink = finalArray[parseInt(index)]["externalLink"];
	if (externalLink) {
		// no need to do any extra processing on the href. and no need to append the context path.
		 anchor.setAttribute("href", href);
		 return true;
	}
	href = cpath + href;
	
	var printerId = document.test_info_viewer_form.printerId.value;
	if (printerId != null && !printerId == '')
		href = href +"&printerId="+printerId;
	else
		href = href;
	
	anchor.setAttribute("href", href);
	
	return true;

}

function funGetPrevious(index, anchor){
	if (index > 0) {
		var myTable = document.getElementById("documentSummary");
		var docDate = "";
		var visitDate = "";
		if (finalArray[parseInt(index)-1]["docDate"]) {
			var epochdate = new Date(finalArray[parseInt(index) -1]["docDate"]);
			docDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
		}
		if (finalArray[parseInt(index)-1]["visitDate"]) {
			var epochdate = new Date(finalArray[parseInt(index) -1]["visitDate"]);
			visitDate = epochdate.getDate() + "-" + (epochdate.getMonth()+1) + "-" + epochdate.getFullYear();
		}

		var type = finalArray[parseInt(index)-1]["Type"];
		for (var d in docTypeDetails) {
			if (type == docTypeDetails[d]["DOC_TYPE_ID"]){
				type = docTypeDetails[d]["DOC_TYPE_NAME"];
				break;
			}
		}
		createDocSummary(myTable, type, finalArray[parseInt(index)-1]["Doctor"],
				finalArray[parseInt(index)-1]["Visit"], visitDate, docDate, finalArray[parseInt(index)-1]["UpdatedBy"],
				finalArray[parseInt(index)-1]["UpdatedDate"], finalArray[parseInt(index)-1]["Title"], finalArray[parseInt(index)-1]["DocId"]);

		createNavigateLinks(parseInt(index-1));

		if (finalArray[parseInt(index)-1]["externalLink"])
			anchor.setAttribute("href", finalArray[parseInt(index)-1]["displayUrl"]);
		else
			anchor.setAttribute("href", cpath + finalArray[parseInt(index)-1]["displayUrl"]);

		document.getElementById('printerId').disabled = finalArray[parseInt(index)-1]["externalLink"];
		return true
	 	
	} else{
		anchor.setAttribute("href", "#");
		alert("No Previous data to View");
		return false;
	}
}

function funGetNext(index, anchor){
	if(index < (finalArray.length-1)) {
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
        for (var d in docTypeDetails){
            if(type == docTypeDetails[d]["DOC_TYPE_ID"]){
                    type = docTypeDetails[d]["DOC_TYPE_NAME"];
                    break;
            }
        }

		createDocSummary(myTable, type, finalArray[parseInt(index)+1]["Doctor"],
			finalArray[parseInt(index)+1]["Visit"], visitDate, docDate, finalArray[parseInt(index)+1]["UpdatedBy"],
			finalArray[parseInt(index)+1]["UpdatedDate"], finalArray[parseInt(index)+1]["Title"], finalArray[parseInt(index)+1]["DocId"]);

		createNavigateLinks(parseInt(index+1));
		
		if (finalArray[parseInt(index)+1]["externalLink"])
			anchor.setAttribute("href", finalArray[parseInt(index)+1]["displayUrl"]);
		else
			anchor.setAttribute("href", cpath + finalArray[parseInt(index)+1]["displayUrl"]);

		document.getElementById('printerId').disabled = finalArray[parseInt(index)+1]["externalLink"];
		return true;
	} else {
		anchor.setAttribute("href","#");
		alert("No Next data to View");
		return false;
	}
}

function createDocSummary(myTable, type, doctor, visit, visitDate, docDate, updatedBy, updatedDate, title, docid) {

	
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
	
	cell = row.insertCell(-1);
	cell.setAttribute("style", "width: 60px");
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = "Document Date:";

	cell=row.insertCell(-1);
	cell.setAttribute("class", "forminfo");
	cell.innerHTML = docDate;

	row = myTable.insertRow(-1);

	

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

}

