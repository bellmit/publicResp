/*
 * Block users from submitting the form twice 
 */

function attachToAllForms(e) {
	//search the DOM for all forms
	var frms = document.getElementsByTagName('form');

	// attach to all forms
	for(var i=0;i<frms.length;i++) {
		var form = frms[i];
		// debug("Found form: " + form.name);

		// trap form.submit() calls
		prototypeForm(form);

		// attach to the form's event
		/*
		 * This is not working for some reason (likely YUI problem, does not happen
		 * in a plain html page without YUI). If there is sufficient gap between the
		 * first and second click the event infra seems to get unloaded, and even the
		 * button's onClick doesn't reach us.
		 *
		 * The workaround is to always use form.submit() after some validations if
		 * it is a POST.
		 *
		YAHOO.util.Event.addListener(form, "submit", submit_listener);
		*/
	}
}

function prototypeForm(form) {
	form.osubmit = form.submit;

	form.submit = function validatesubmit() {
		// debug("Submit via form.submit called");
		if (allow_submit()) {
			form.osubmit();
		}
	};
}

function getEventElementById(e) {
	if (typeof(e)!='string')
		return e;
	if (document.getElementById)
		e=document.getElementById(e);
	else if (document.all)
		e=document.all[e];
	else e=null;
	return e;
}

var _dblclick_timestamp = 0;

function allow_submit() {
	var d = new Date();

	if (_dblclick_timestamp == 0) {
		_dblclick_timestamp = d.getTime();
		// debug("First submit: " + _dblclick_timestamp);
		return true;
	} else {
		var current = d.getTime();

		if ((current - _dblclick_timestamp) < 5000) {
			// debug("Block: current: " + current + "; orig: " + _dblclick_timestamp);
			_dblclick_timestamp = d.getTime();
			return false;
		} else {
			// debug("OK: current: " + current + "; orig: " + _dblclick_timestamp);
			_dblclick_timestamp = d.getTime();
			return true;
		}
	}
	return true;
}

function submit_listener(e) {
	// debug("Submit event called");
	if (typeof(e) == "string") {
		//the id was supplied, get the object reference
		e = getEventElementById(e);
		if (!e) {
			// debug("No event found");
			return true;
		}
	}
	if (!allow_submit()) {
		// debug("Blocking the submit event that was called");
		YAHOO.util.Event.stopEvent(e);
		return false;
	}
	return true;
}

