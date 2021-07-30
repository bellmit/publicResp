
function getPrint(docid, format, rawMode, printerId) {
	if (docid != '' && (format == 'doc_hvf_templates' || format == 'doc_rich_templates')) {
		var href = printPath + '?_method=print&doc_id='+docid;
		href += rawMode;
		href += '&printerId='+printerId;
		window.open(href);
	}
}