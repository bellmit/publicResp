/*
 * Instantiate a HTML wysiwyg editor using TinyMCE, using a standard set of options.
 * (Remember to include the following in your jsp before calling initEditor:
 *     <insta:link type="js" file="tiny_mce/tiny_mce.js" />
 */

function initEditor(elementId, contextPath, defaultFontName, defaultFontSize, imageListUrl) {

	tinyMCE.init({
		mode : "exact",
		elements : elementId,
		theme : "advanced",
		cleanup: true,
		gecko_spellcheck: true,		/* does not work well with contextmenu plugin, so disabled contextmenu */

		plugins : "safari,spellchecker,pagebreak,style,table,advhr,inlinepopups,searchreplace"+
			",paste,noneditable,visualchars,nonbreaking,xhtmlxtras,template",

		// Theme options
		theme_advanced_buttons1 : 
			"formatselect,fontselect,fontsizeselect"+
			",bold,italic,underline,strikethrough,sub,sup"+
			",|,forecolor,backcolor"+
			",|,help",

		theme_advanced_buttons2 : 
			"paste,pastetext,pasteword"+
			",|,search,replace,|,undo,redo"+
			",|,justifyleft,justifycenter,justifyright,justifyfull"+
			",|,bullist,numlist,outdent,indent"+
			",|,image,nonbreaking,advhr,charmap,pagebreak,blockquote",

		theme_advanced_buttons3 : 
			"tablecontrols,delete_table"+
			",|,visualchars,visualaid"+
			",|,removeformat,cleanup,code",

		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_statusbar_location : "bottom",
		theme_advanced_resizing : false,

		content_css : contextPath + "/css/editorstyles.css, " + 
			contextPath + "/GetEditorBodyStyle.do?font=" + defaultFontName + "&fontSize=" + defaultFontSize,

		//theme_advanced_styles: "Single Border Table=bordertable", (with plugin styleselect)

		external_image_list_url : imageListUrl,
		relative_urls: false,

		pagebreak_separator : '<p class="pagebreak"><!-- pagebreak --></p>',

		table_styles : "No Border=noborder; Thin Border=thinborder; Thick Border=thickborder; Box=box",
		table_cell_styles : "No Border=noborder; Box=box",
		table_row_styles : "Plain=plain; Border Above=borderabove; Border Below=borderbelow"

	});

}

