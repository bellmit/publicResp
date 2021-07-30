/*
 * This file contains all the constants mapped to tour number for FTUE. 
 */

var tourConstant = {
	"HUDTour" : 1,
	"QuickLinksTour" : 2
};

function isTourPlayValid(storageKey, storageVal) {
	if (!localStorage.getItem(storageKey)) {
		return true;
	}
	
	if (localStorage.getItem(storageKey) < storageVal) {
		return true;
	}
	
	return false;
}

var hamburgerTourInProgress = false;

function isTourInProgress() {
	return hamburgerTourInProgress;
}