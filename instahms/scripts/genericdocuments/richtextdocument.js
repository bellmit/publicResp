/* initialize the tinyMCE editor: todo: font and size to be customizable */
initEditor("doc_content_text", contextPath, 'sans-serif', 12,
contextPath + "/pages/GenericDocuments/PatientGeneralImageAction.do?_method=getImageListJS&mr_no="+mrNo);
function submitValues(){
	tinyMCE.triggerSave();
	document.forms[0].submit();
}