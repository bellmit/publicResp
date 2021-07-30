/*
* Open docs directyly in editable mode
*/
function openDocInEditView (){
  var url = new URL(window.location.href);
		const editParams = url.searchParams.get('openDocInEditView');
		if(editParams){
			document.mainform.submit();
        }
}