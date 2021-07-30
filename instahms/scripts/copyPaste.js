function pasteImage(event, elementId,callback) {
	if (event.clipboardData) {
		var items = event.clipboardData.items;
	    if (!items) return;
	    for (var i = 0; i < items.length; i += 1) {
	      if (items[i].type.indexOf('image') !== -1) {
	        var blob = items[i].getAsFile();
	        var URLObj = window.URL || window.webkitURL;
	        var source = URLObj.createObjectURL(blob);
	        callback(elementId, source, blob, blob.name, blob.type);
	      }
	    }
	    event.preventDefault();
	}
}

function copyPasteImage(elementId, callback) {
	var element = document.getElementById(elementId);
	if (element) {
	    element.addEventListener('paste',function (event) {pasteImage(event, elementId,callback)}, false);
	};
}

function imageToData(blob, blobType, elementId,callback) {
	  var reader = new FileReader();
	  reader.readAsDataURL(blob);
	  reader.onload = function () {
	    return callback(elementId, blobType, reader.result);
	  };
}

function base64ToBlob(dataURI) {
    // convert base64 to raw binary data held in a string
	var splitArray = dataURI.split(',');
    var byteString = atob(splitArray[1]);

    // separate out the mime component
    var mimeString = splitArray[0].split(':')[1].split(';')[0]

    // write the bytes of the string to an ArrayBuffer
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

    // write the ArrayBuffer to a blob
    return new Blob([ab], {type: mimeString});
}
