


/**
	Function use fule for ajaxing to get order Auto generated Alias name
**/
function ajaxForOrderCode(type,deptId,grp,subGrp,codeBox){
	if(subGrp == '')
		return true;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath +'/master/orderItems.do?method=getOrderAlias&type='+type+'&group='+grp+'&subgroup='+subGrp+'&deptId='+deptId;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			codeBox.value= reqObject.responseText;
		}
	}
	return true;
}