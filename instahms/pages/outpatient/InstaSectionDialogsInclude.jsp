<div id="notesDialog" style="display: none; visibility: hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add/Edit</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Notes: </td>
					<td><textarea name="d_notes" id="d_notes" rows="3" cols="20"></textarea></td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
			<tr>
				<td>
					<input type="button" name="notes_ok" id="notes_ok" value="Ok"/>
					<input type="button" name="notes_ok" id="notes_close" value="Close"/>
				</td>
			</tr>
		</table>
	</div>
</div>
<div id="phraseDialog" style="display: none; visibility: hidden">
	<div class="hd">Phrases</div>
	<input type="hidden" id="d_phrase_category_id" value=""/>
	<div class="bd">
		<div style="float: right">
			<a id="section_field_clear_anchor" title="Clear Field" href="javascript:void(0);"><u>Clear Field</u></a> |
			<a id="section_field_undo_last" title="Undo Last Added Field" href="javascript:void(0);"><u>Undo Last</u></a>
		</div>
		<div style="clear: both"></div>
		<div style="overflow:auto; height: 200px; margin-top: 8px;">
			<table id="phrases_table" class="phrase_table" width="100%" >

			</table>
		</div>
	</div>
</div>