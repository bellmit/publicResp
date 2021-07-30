/**
   Requirements :-
   ------------
      1. First inclued the jsp page which is located at /HMSNew/pages/masters/Search.jsp.
      2. create a div with id = "findDiv" where table should be displayed
      3. write small javascript function  like this one

 	     function callSearch(typeOfSearch){
   		   var headerNames = new Array(1);
		   var dispIndexes = new Array(0);
		   var parameterIndexes = new Array(1);
		   parameterIndexes[0] = 0;
		   headerNames[0] = 'Item Names';
		   getSearchResults('ITEMFIND',1,typeOfSearch,headerNames,0,dispIndexes,'center',300,1,'javascript:reset();displayItem(\"^\")',parameterIndexes,'_self');
		}
		function name should be exactly callSearch.
		in that you have to call getSearchResults() function with required parameters



   Prameters :-
   ---------
  			 	xmlDocId              --> xml stirng  id which is place in jsp page

                searchAttributeIndex  --> index of attribute in xml string which should be searched (xml string attribute indexes starts with 0
                                          example:- if you want to search for company name in xml string like
                                          -------
                                          <?xml version="1.0"?><xml id="COMP"><COMPs><comp class1="COMP0004" class2="WIPRO" class3="INSID0001" class4="0" class5="3512" class6="0" class7="N" /></COMPs></xml>
                                          it is class2 which should be givent as 1 for serchAttributeIndex

                type of serach        --> -1 for starts with
                                           0 for contains
                                           1 for exact word
                                           2 for all

               headerNames            -->  Array of strings for table Header rows

               idAttributeIndex       -->  this is used in sigle column table with a function name FindVaues("SomeId") some id will be replace with idAttributeIndex value in Xml String

               displayIndexes         -->  It is an array of indexes other than searchAttributeIndex whose values should be displayed in table

               align                  -->  this is used to specify alignment for <TD>s

               width                  -->  this is used to specify width of a <TD>

               findOrAction           -->  if 1   value for href of anchor tag is not FindValues("some id") so you have to pass value to actUrl parameter

               actUrl                 -->  findOrAction =1 then a string is passed
                                           example:-  'javascript:findInsuranceValues(\"^\",\"^\")'

               parameterIndexes       -->  Array of indexes of xml string attributes
                                           if we specify actUrl as above then ^ symbols showuld be replaced by values of Xml attributes

               target                 -->  url target

               firstOrLast            -->  To display search column at first or last in the table  ( if 1-first,  any thing else is last column)
*/
function getSearchResults(xmlDocId,searchAttributeIndex,typeOfSearch,headerNames,idAttributeIndex,displayIndexes,align,width,findOrAction,actUrl,parameterIndexes,target,firstOrLast){
		searchString  = trimAll(document.getElementById('conditionValue').value);
		if((searchString == "") && (typeOfSearch != 2)){
		  alert("Enter Search String");
		  	resetSearch();
		   //document.getElementById('conditionValue').focus();
		  return;
		}
		//check whether the xml string available in jsp page with xmlDocId
		if(document.getElementById(xmlDocId)) {
			var list=document.getElementById(xmlDocId).getElementsByTagName(xmlDocId+'s');
			var root = list.item(0);
			offlist = root.getElementsByTagName(xmlDocId.toLowerCase());
			var len=offlist.length;
			var otable = document.createElement("TABLE");
			otable.id ="searchTable";
			otable.styleClass="totalBG";
			var row =  otable.insertRow(-1);
			row.align="center";
			row.height="28";
			for(var i = 0;i<headerNames.length;i++){
			 cell=row.insertCell(-1)
			 cell.style.width=width;
			 cell.innerHTML="<span style='color:darkgreen; font-size:16;font-weight:bold;'>"+headerNames[i]+"</span>";
			}
			var searchInString="";
			var found =true;
			var actionUrl ='';

			for(var i=0;i<len;i++){
				off=offlist.item(i);
				searchInString = off.attributes.getNamedItem('class'+(searchAttributeIndex+1)).nodeValue;
				actionUrl = actUrl;

				//if findOrAction = 1 construct url for href Attribute of anchor tag
				if(findOrAction ==1){
				        var previousIndex=0;
					    var presentIndex =0;
					    for(var k=0;k<parameterIndexes.length;k++){
					    	presentIndex = actionUrl.indexOf('^',previousIndex);
					    	//replace ^ symblos with actual values from XML String
					    	actionUrl = actionUrl.substring(0,presentIndex)+off.attributes.getNamedItem('class'+(parameterIndexes[k]+1)).nodeValue+actionUrl.substring(presentIndex+1);
				        }
				}
				//if search type is all
			  if(typeOfSearch==2){
				   row = otable.insertRow(-1);
				   if(firstOrLast==1){
				    	if(findOrAction !=1){
							cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}else{
							cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}
					}
				    for(var j =0;j<displayIndexes.length;j++){
					  cell = row.insertCell(-1);
					  cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+off.attributes.getNamedItem('class'+(displayIndexes[j]+1)).nodeValue+"</div>";

					}
					if(firstOrLast!=1){
				    	if(findOrAction !=1){
					 		cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'><a href='javascript:Findvalues(\""+off.attributes.getNamedItem('class'+(idAttributeIndex+1)).nodeValue+"\")' style='color:darkgreen;  font-size:12'>"+searchInString+"</a></div>";
							cell.align=align;
						}else{
							 cell = row.insertCell(-1);
						     cell.align=align;
						cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'><a href='"+actionUrl+"' style='color:darkgreen;  font-size:12' target='"+target+"'>"+searchInString+"</a></div>";

						}
					}
					found = false;
				}else if(checkCondition(searchInString,searchString,typeOfSearch)){
			     row = otable.insertRow(-1);

				  found = false;

				  if(firstOrLast==1){
				    	if(findOrAction !=1){
								 cell = row.insertCell(-1);

						cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'><a href='javascript:Findvalues(\""+off.attributes.getNamedItem('class'+(idAttributeIndex+1)).nodeValue+"\")' style='color:darkgreen;'>"+searchInString+"</a></div>";

						}else{
							 cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}
					}
				    for(var j =0;j<displayIndexes.length;j++){
				 			 cell = row.insertCell(-1);

						cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+off.attributes.getNamedItem('class'+(displayIndexes[j]+1)).nodeValue+"</div>";

					}
					if(firstOrLast!=1){
				    	if(findOrAction !=1){
							 cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'><a href='javascript:Findvalues(\""+off.attributes.getNamedItem('class'+(idAttributeIndex+1)).nodeValue+"\")' style='color:darkgreen;'>"+searchInString+"</a></div>";

						}else{
								 cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'><a href='"+actionUrl+"' style='color:darkgreen;' target='"+target+"'>"+searchInString+"</a></div>";

						}
					}
				}
			}
			if(found){
		          row = otable.insertRow(-1);
		          cell=row.insertCell(-1);
		          cell.colspan="2";
		              cell.innerHTML="<div align='center' style='font-family:Verdana;color:darkgreen; font-size:14;font-weight:bold'>No Results Found</div>";
			}
			 var nodes =document.getElementById('findDiv').childNodes;
			 var len = nodes.length;
			 for(var i=len-1;i>=0;i--){
			   document.getElementById("findDiv").removeChild(nodes[i]);
			 }
			/* document.getElementById('findDiv').appendChild(otable);
			document.getElementById('searchTable').border="2";
			document.getElementById('searchTable').borderColor="darkgreen";
			document.getElementById('searchTable').cellPadding="5";
			document.getElementById('searchTable').cellSpacing="0";
			document.getElementById('searchTable').style.backgroundColor = "totalBG";
		    document.getElementById('searchTable').style.borderCollapse="collapse";*/
		    otable.border="2";
			otable.borderColor="darkgreen";
			otable.cellPadding="5";
			otable.cellSpacing="0";
			otable.style.backgroundColor = "D9EABB";
		    otable.style.borderCollapse="collapse";
		    document.getElementById('findDiv').appendChild(otable);
		    document.getElementById('findDiv').innerHTML=document.getElementById('findDiv').innerHTML ;
		//	alert(otable.innerHTML);

	}
}

function getSearchResults1(xmlDocId,searchAttributeIndex,typeOfSearch,headerNames,idAttributeIndex,displayIndexes,align,width,findOrAction,actUrl,parameterIndexes,target,firstOrLast,colspan){
		searchString  = trimAll(document.getElementById('conditionValue').value);
		if((searchString == "") && (typeOfSearch != 2)){
		  alert("Enter Search String");
		  resetSearch();
		   document.getElementById('conditionValue').focus();
		  return;
		}
		//check whether the xml string available in jsp page with xmlDocId
		if(document.getElementById(xmlDocId)) {
			var list=document.getElementById(xmlDocId).getElementsByTagName(xmlDocId+'s');
			var root = list.item(0);
			offlist = root.getElementsByTagName(xmlDocId.toLowerCase());
			var len=offlist.length;
			var otable = document.createElement("TABLE");
			otable.id ="searchTable";
			otable.styleClass="totalBG";
			var row =  otable.insertRow(-1);
			row.align="center";
			row.height="20";
			for(var i = 0;i<headerNames.length;i++){
			 cell=row.insertCell(-1)
			 cell.style.width=width;
			 cell.style.background='darkgreen';
			 cell.innerHTML="<span style='color:white; font-size:16;font-weight:bold;'>"+headerNames[i]+"</span>";
			}
			var searchInString="";
			var found =true;
			var actionUrl ='';

			for(var i=0;i<len;i++){
				off=offlist.item(i);
				searchInString = off.attributes.getNamedItem('class'+(searchAttributeIndex+1)).nodeValue;
				actionUrl = actUrl;

				//if findOrAction = 1 construct url for href Attribute of anchor tag
				if(findOrAction ==1){
				        var previousIndex=0;
					    var presentIndex =0;
					    for(var k=0;k<parameterIndexes.length;k++){
					    	presentIndex = actionUrl.indexOf('^',previousIndex);
					    	//replace ^ symblos with actual values from XML String
					    	actionUrl = actionUrl.substring(0,presentIndex)+off.attributes.getNamedItem('class'+(parameterIndexes[k]+1)).nodeValue+actionUrl.substring(presentIndex+1);
				        }
				}
				//if search type is all
			  if(typeOfSearch==2){
				   row = otable.insertRow(-1);
				   if(firstOrLast==1){
				    	if(findOrAction !=1){
							cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}else{
							cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}
					}
				    for(var j =0;j<displayIndexes.length;j++){
					  cell = row.insertCell(-1);
					  cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+off.attributes.getNamedItem('class'+(displayIndexes[j]+1)).nodeValue+"</div>";

					}
					found = false;
				}else if(checkCondition(searchInString,searchString,typeOfSearch)){
			     row = otable.insertRow(-1);

				  found = false;

				  if(firstOrLast==1){
				    	if(findOrAction !=1){
								 cell = row.insertCell(-1);

						cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";

						}else{
							 cell = row.insertCell(-1);
							cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+searchInString+"</div>";
						}
					}
				    for(var j =0;j<displayIndexes.length;j++){
				 			 cell = row.insertCell(-1);
						     cell.innerHTML="<div align='"+align+"' style='font-family:Verdana;color:darkgreen; font-size:12'>"+off.attributes.getNamedItem('class'+(displayIndexes[j]+1)).nodeValue+"</div>";
					}
				}
			}
			if(found){
		          row = otable.insertRow(-1);
		          cell=row.insertCell(-1);
		          cell.colspan=colspan;
		              cell.innerHTML="<div align='center' style='font-family:Verdana;color:darkgreen; font-size:14;font-weight:bold'>No Results Found</div>";
			}
			 var nodes =document.getElementById('findDiv').childNodes;
			 var len = nodes.length;
			 for(var i=len-1;i>=0;i--){
			   document.getElementById("findDiv").removeChild(nodes[i]);
			 }
			/* document.getElementById('findDiv').appendChild(otable);
			document.getElementById('searchTable').border="2";
			document.getElementById('searchTable').borderColor="darkgreen";
			document.getElementById('searchTable').cellPadding="5";
			document.getElementById('searchTable').cellSpacing="0";
			document.getElementById('searchTable').style.backgroundColor = "#D9EABB";
		    document.getElementById('searchTable').style.borderCollapse="collapse";*/
		    otable.border="2";
			otable.borderColor="darkgreen";
			otable.cellPadding="5";
			otable.cellSpacing="0";
			otable.style.backgroundColor = "#D9EABB";
		    otable.style.borderCollapse="collapse";
		    document.getElementById('findDiv').appendChild(otable);
		    document.getElementById('findDiv').innerHTML=document.getElementById('findDiv').innerHTML ;
		//	alert(otable.innerHTML);

	}
}


    // this method is called when search type is not all
	function checkCondition(stringToSearch,searchString, type){
		stringToSearch=trimAll(stringToSearch);
		if(type == -1){
        	if(stringToSearch.substring(0,searchString.length) == searchString)
            	return true;
        	else
           		return false;
      	}else if(type == 0){
      		if(stringToSearch.search(searchString) != -1)
      	   		return true;
      		else
	      	   return false;
    	}else if(type == 1){
        	if(stringToSearch == searchString)
           		return true;
		    else
        	   return false;
      	}else{
        	return false;
      	}
	}

	// this function is called to clear fields in search.jsp file which is included
	function resetSearch(){
		callSearch(2);
	    document.getElementById("conditionValue").value='';
	    var conObj = document.forms[0].condition;

		 for(var i = 0;i<conObj.length; i++){
		    if(conObj[i].checked==true){
		       conObj[i].checked = false;
		       break;
		   }
		 }

		   document.getElementById('conditionValue').focus();
	 	document.forms[0].condition[3].checked = true;

	}
function trimAll(sString)
{

while (sString.substring(0,1) == ' ')
{
sString = sString.substring(1, sString.length);

}
while (sString.substring(sString.length-1, sString.length) == ' ')
{
sString = sString.substring(0,sString.length-1);
}

return sString;
}
