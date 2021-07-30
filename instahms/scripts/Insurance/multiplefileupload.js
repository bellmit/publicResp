/**
 * Convert a single file-input element into a 'multiple' input list
 *
 * Usage:
 *
 *   1. Create a file input element (no name)
 *      eg. <input type="file" id="first_file_element">
 *
 *   2. Create a DIV for the output to be written to
 *      eg. <div id="files_list"></div>
 *
 *   3. Instantiate a MultiSelector object, passing in the DIV and an (optional) maximum number of files
 *      eg. var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 3 );
 *
 *   4. Add the first element
 *      eg. multi_selector.addElement( document.getElementById( 'first_file_element' ) );
 *
 *   5. That's it.
 *
 *   You might (will) want to play around with the addListRow() method to make the output prettier.
 *
 *   You might also want to change the line
 *       element.name = 'file_' + this.count;
 *   ...to a naming convention that makes more sense to you.
 *
 * Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Stickman -- http://www.the-stickman.com
 *      with thanks to:
 *      [for Safari fixes]
 *         Luis Torrefranca -- http://www.law.pitt.edu
 *         and
 *         Shawn Parker & John Pennypacker -- http://www.fuzzycoconut.com
 *      [for duplicate name bug]
 *         'neal'
 */
 var list_target;
 var file_index;
function MultiSelector( _target, max ){

	// Where to write the list
	list_target = _target;
	// How many elements?
	this.count = 0;
	// How many elements?
	this.id = 0;
	// Is there a maximum?
	file_index = 0;
	if( max ){
		this.max = max;
	} else {
		this.max = -1;
	};

	/**
	 * Add a new file input element
	 */

	this.addElement = function( element ){
		// Make sure it's a file input element
		if( element.tagName == 'INPUT' && element.type == 'file' ){

			// Element name -- what number am I?
			element.name = 'attachment[' + this.id++ +']';
			element.id = 'attachment[' + file_index +']';

			// Add reference to this object
			element.multi_selector = this;

			// What to do when a file is selected
			element.onchange = function( ){
			   clearDocs('sys');
			};
			// If we've reached maximum number, disable input element
			if( this.max != -1 && this.count >= this.max ){
				element.disabled = true;
			};

			// File element counter
			this.count++;
			// Most recent element
			this.current_element = element;

		} else {
			// This can only be applied to file input elements!
			alert( 'Error: not a file input element' );
		};

	};

	this.addWhenClickedAddButton = function(obj, _target, firstCell) {
				list_target = _target

				var fileName = obj.value;
				var fileExt = fileName.substr(fileName.lastIndexOf('.')+1,fileName.length-1);
				//alert(fileExt);
				var new_element = document.createElement( 'input' );
				new_element.type = 'file';
                new_element.indexed = ''+file_index;
                new_element.className = 'forminput';
              // Add new element
				obj.parentNode.insertBefore( new_element, obj );

				// Apply 'update' to element
				obj.multi_selector.addElement( new_element);

				// Update list
				obj.multi_selector.addListRow( obj , firstCell);

				// Hide this: we can't use display:none because Safari doesn't like it
				obj.style.position = 'absolute';
				obj.style.left = '-1000px';

	}

	/**
	 * Add a new row to the list of files
	 */
	 var fileName='';
	this.addListRow = function( element, firstCell ){

		// Row div
		var new_row = document.createElement( 'div' );

		// Delete button
		var new_row_button = document.createElement( 'img' );
		new_row_button.src = '../../images/delete.jpg';
		new_row_button.alt = 'Select elements to Cancel';
		new_row_button.title = 'Select elements to Cancel';
		new_row_button.width = '20';
		new_row_button.height = '20';

		//new_row_button.type = "checkbox";

		// References
		firstCell.element = element;

		// Delete function
		new_row_button.onclick= function(){
			if (confirm("do you want to delete the row")) {
				// Remove element from form
				this.parentNode.element.parentNode.removeChild( this.parentNode.element );

				// Remove this row from the list
				this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode);
				//this.parentNode.parentNode.removeChild( this.parentNode );

				// Decrement counter
				this.parentNode.element.multi_selector.count--;

				// Re-enable input element (if it's disabled)
				this.parentNode.element.multi_selector.current_element.disabled = false;

				// Appease Safari
				//    without it Safari wants to reload the browser window
				//    which nixes your already queued uploads

				// enables or disables the sentToTPA button.
				enableAndDisableSentToTpa();
				return false;
			} else {
				return false;
			}
		};

		// Set row value
		// if we attach the document, then generate support documentation should go with the empty value
		new_row.innerHTML = element.value+'&nbsp;<input type="hidden" name="fileName" id="fileName" value="'+ element.value +'">' +
								'<input type="hidden" name="txtSysGenDoc" id="txtSysGenDoc" value=""/>';
        new_row.className = 'fileText';
		// Add button
		//td = element.parentNode.parentNode.getElementsByTagName('td')[0];

		//new_row.appendChild( new_row_button );
		firstCell.appendChild( new_row_button );

		// Add it to the list
		list_target.appendChild( new_row );

	};

};

