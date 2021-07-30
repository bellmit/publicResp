var chart;
var xlength;
var filter1;
var filter2;
var filename;
var jsonResponse;
makeChart(url);

function makeChart(url) {
	$.get("//" + url.split("://")[1],
			function(response) {
				jsonResponse = response;
				var title, trendType = response.graphObj.trendType;

				// Set header titles
				if (trendType == 'month')
					title = "Monthly " + response.graphObj.title;
				else if (trendType == 'week')
					title = "Weekly " + response.graphObj.title;
				else if (trendType == 'day')
					title = "Daily " + response.graphObj.title;
				else
					title = response.graphObj.title;
				$('#titlename').text(title);

				$('#filterName1').text(response.graphObj.filterName1);
				$('#filterName2').text(response.graphObj.filterName2);
				$('#dateRange').text(
						response.graphObj.fromDate + " to "
								+ response.graphObj.toDate);

				// Length of x-axis
				xlength = response.data['trend'].length;
				// Filename for exported files
				filename = response.graphObj.title + " "
						+ response.graphObj.fromDate + " to "
						+ response.graphObj.toDate;
				// Create chart
				chart = c3.generate({
					bindto : '#chart',
					data : {
						columns : [],
						x : 'trend',
						empty : {
							label : {
								text : "No Data"
							}
						},
						selection : {
							enabled : true,
							multiple : false
						}
					},
					axis : {
						x : {
							type : 'category',
							tick : {
								centered : true,
								fit : false,
								multiline : false,
								rotate : 45
							},
							height : 60
						}
					},
					line : {
						connectNull : true
					},
					zoom : {
						enabled : false,
						rescale : true
					},
					size : {
						width : $("#container").width(),
						height : window.innerHeight * 0.6
					},
					padding : {
						top : 0,
						right : 10,
						bottom : 0,
						left : 5.5 * response.graphObj.maxDigits + 10
					},
					transition : {
						duration : 0
					},
					grid : {
						y : {
							show : true
						}
					},
					point : {
						r : 2,
						focus : {
							expand : {
								r : 4
							}
						}
					},
					legend : {
						item : {
							onclick : function(id) {
								chart.toggle(id);
								eventTracking("Graphical Reports", "Legend toggled");
							}
						}
					}
				});
				document.getElementsByClassName('c3-title')[0].innerHTML = title + filename.replace(response.graphObj.title,'');
				document.getElementsByClassName('c3-title')[0].style.display= 'none';
				window.resize = chart.resize()

				// Show totals
				showDefaultData();

				// Initialize filters
				filter1 = $('#filter1').magicSuggest(
						{
							data : response.graphObj.filter1Set,
							maxSelection : null,
							placeholder : "  Select a "
									+ response.graphObj.filterName1,
							editable : false,
							expandOnFocus : true,
							id : 'filter1'
						});
				$(filter1).on('selectionchange', filter1Changed);
				filter2 = $('#filter2').magicSuggest(
						{
							data : response.graphObj.filter2Set,
							maxSelection : null,
							placeholder : "  Select a "
									+ response.graphObj.filterName2,
							editable : false,
							expandOnFocus : true,
							id : 'filter2',
							disabled : true
						});
				$(filter2).on('selectionchange', filter2Changed);

				$(filter1).on('expand', function(c) {
					$('#filter1 .ms-trigger-ico').css({
						"border-bottom" : "4px solid #ABB1B8",
						"border-top" : "0"
					});
				});
				$(filter1).on('collapse', function(c) {
					$('#filter1 .ms-trigger-ico').css({
						"border-top" : "4px solid #ABB1B8",
						"border-bottom" : "0"
					});
				});

				$(filter2).on('expand', function(c) {
					$('#filter2 .ms-trigger-ico').css({
						"border-bottom" : "4px solid #ABB1B8",
						"border-top" : "0"
					});
				});
				$(filter2).on('collapse', function(c) {
					$('#filter2 .ms-trigger-ico').css({
						"border-top" : "4px solid #ABB1B8",
						"border-bottom" : "0"
					});
				});

				$('.ms-ctn').append(
						'<span class="ms-clear-all">Clear all</span>');
				$('#filter1 .ms-clear-all').click(function() {
					filter1.clear();
					filter1.collapse();
				});
				$('#filter2 .ms-clear-all').click(function() {
					filter2.clear();
					filter2.collapse();
				});
				$('.ms-clear-all').hide();
				if (response.graphObj.filterName2 === null) {
					$("#filter2").hide();
				}

				// Custom panning
				var oX = 0, dragging = false, threshold = $('#chart').width()
						/ (xlength-1);
				$('.c3-chart').on('mousedown touchstart', function(e) {
					var touch = undefined;
					if (e.originalEvent.touches)
						touch = e.originalEvent.touches[0]
					oX = e.pageX || touch.pageX;
					dragging = true;
				});
				$(document).on('mouseup touchend', function(e) {
					dragging = false;
				});
				$('.c3-chart').on('mousemove touchmove', function(e) {
					if (!dragging)
						return;
					var touch = undefined;
					if (e.originalEvent.touches)
						touch = e.originalEvent.touches[0];
					var posx = e.pageX || touch.pageX;
					var distance = oX - posx;
					var x1 = chart.zoom()[0];
					var x2 = chart.zoom()[1];
					threshold = $('#chart').width() / (x2 - x1);

					var move = distance / threshold;
					if (move < 0) {
						newX1 = Math.max(0, x1 + move);
						newX2 = x2 - (x1 - newX1);
					} else {
						newX2 = Math.min(xlength, x2 + move);
						newX1 = x1 + (newX2 - x2);
					}
					if (x1 === newX1 && x2 === newX2)
						return;
					if (newX1 != newX2)
						chart.zoom([ newX1, newX2 ]);
					var touch = undefined;
					if (e.originalEvent.touches)
						touch = e.originalEvent.touches[0];
					oX = e.pageX || touch.pageX;
				});
				
				if (xlength <= 4) {
					$(".button:has(span.zoom-in-icon)").addClass("disabled");
				}

				eventTracking("Graphical Reports", "Chart opened", title);
			});
}

//Custom zoom
function zoom(direction) {
	var x1 = 0, x2 = xlength;
	if (direction === 'in') {
		x1 = chart.zoom()[0];
		x2 = (chart.zoom()[1] == 0) ? x2 : chart.zoom()[1];
		x1 = Math.max(0, Math.ceil((3 * x1 + x2) / 4));
		x2 = Math.min(xlength, Math.floor((3 * x2 + x1) / 4));
		if (x1 != x2 && x2 - x1 > 4
				&& !($(".button:has(span.zoom-in-icon)").hasClass("disabled"))) {
			chart.zoom([ x1, x2 ]);
			$(".button:has(span.zoom-out-icon)").removeClass("disabled");
			$(".button:has(span.zoom-reset-icon)").removeClass("disabled");
		}
		x1 = Math.max(0, Math.ceil((3 * x1 + x2) / 4));
		x2 = Math.min(xlength, Math.floor((3 * x2 + x1) / 4));
		if (x1 != x2 && x2 - x1 <= 4) {
			$(".button:has(span.zoom-in-icon)").addClass("disabled");
		}
	} else if (direction === 'out') {
		x1 = chart.zoom()[0];
		x2 = (chart.zoom()[1] == 0) ? x2 : chart.zoom()[1];
		if (x1 != x2) {
			x1 = Math.max(0, Math.floor((3 * x1 - x2) / 2));
			x2 = Math.min(xlength, Math.floor((3 * x2 - x1) / 2));
		}
		if (!($(".button:has(span.zoom-out-icon)").hasClass("disabled"))) {
			chart.zoom([ x1, x2 ]);
			$(".button:has(span.zoom-in-icon)").removeClass("disabled");
		}
		if (x1 === 0 && (x2 === 0 || x2 === xlength)) {
			$(".button:has(span.zoom-out-icon)").addClass("disabled");
			$(".button:has(span.zoom-reset-icon)").addClass("disabled");
		}
	} else {
		$(".button:has(span.zoom-in-icon)").removeClass("disabled");
		if (xlength <= 4) {
			$(".button:has(span.zoom-in-icon)").addClass("disabled");
		}
		$(".button:has(span.zoom-out-icon)").addClass("disabled");
		$(".button:has(span.zoom-reset-icon)").addClass("disabled");
		chart.unzoom();
	}

	chart.tooltip.hide();
	eventTracking("Graphical Reports", "Zoom", direction);
}

// Set css properties for exported image
function d3style() {
	var zoomRect = d3.select('.c3-zoom-rect').node().getBBox();
	d3.select('defs').append("mask").attr("id", "export-mask").append("rect")
		.attr("width", zoomRect.width).attr("height", zoomRect.height)
		.style('stroke', 'none').style('fill', '#fff');
	d3.selectAll('#chart .c3-shapes').each(
		function(e) {
			d3.select(this).attr('mask', 'url(#export-mask)');
		});
	d3.selectAll('#chart *').each(
			function(e) {
				if (d3.select(this).style('fill-opacity') == 0)
					d3.select(this).style('opacity', 0);
				else if (d3.select(this).style('fill-opacity') == 0.1
						|| d3.select(this).style('fill-opacity') == 0.9)
					d3.select(this).style('opacity', 0.9);
				d3.select(this).style('fill', d3.select(this).style('fill'));
				d3.select(this)
						.style('stroke', d3.select(this).style('stroke'));
			});
	d3.selectAll('#chart text').each(
			function(e) {
				d3.select(this).style('font-size',
						d3.select(this).style('font-size'));
				d3.select(this).style('font-family',
						d3.select(this).style('font-family'));
			});

	// html2canvas does not recognize dy
	d3.selectAll('#chart tspan').each( function(e) {
		// convert em to px
		if (d3.select(this).attr('dy').indexOf('em') !== -1
				&& d3.select(this).style('font-size').indexOf('px') !== -1) {
			d3.select(this).attr('dy',
					d3.select(this).attr('dy').replace('em', '') *
					d3.select(this).style('font-size').replace('px', ''));
		}
	
		if (d3.select(this).attr('dy') != 0) {
			d3.select(this.parentNode).attr('y',
					Number(d3.select(this.parentNode).attr('y')) +
					Number(d3.select(this).attr('dy')));
			d3.select(this).attr('dy', 0);
		}
	});
}

$("#export").click(function() {
	$("#export .dropdown-menu").toggle();
	if ($("#export .dropdown-menu").css('display') === 'none') {
		$("#export .arrow-icon").css({
			"border-top" : "4px solid #777",
			"border-bottom" : "0"
		});
	} else {
		$("#export .arrow-icon").css({
			"border-bottom" : "4px solid #777",
			"border-top" : "0"
		});
	}
});

function exportGraph(type) {
	d3style();
	document.getElementsByClassName('c3-title')[0].style.display= 'initial';
	var svgElement = document.querySelector('svg');
	var myCanvas = document.getElementById("svg-canvas");
	myCanvas.height = d3.selectAll("svg").attr("height");
	myCanvas.width = d3.selectAll("svg").attr("width");
	var ctx = myCanvas.getContext("2d");

	var svgURL = new XMLSerializer().serializeToString(svgElement);
	var img = new Image();
	img.onload = function() {
		ctx.drawImage(this, 0, 0);
		if (type === "pdf") {
			exportCanvasAsPdf('svg-canvas', filename);
		} else {
			exportCanvasAsImage('svg-canvas', filename, type);
		}
	}
	img.src = 'data:image/svg+xml; charset=utf8, ' + encodeURIComponent(svgURL);
	document.getElementsByClassName('c3-title')[0].style.display= 'none';
	eventTracking("Graphical Reports", "Chart exported", type);
}

function exportCanvasAsPdf(id, fileName) {
	var canvas = document.getElementById(id);
	var imgData = canvas.toDataURL('image/png');
	var doc = new jsPDF('l', 'mm');
	doc.addImage(imgData, 'PNG', 10, 10);
	doc.save(fileName + '.pdf');
}

function exportCanvasAsImage(id, fileName, type) {
	var canvasElement = document.getElementById(id);

	var ctx = canvasElement.getContext("2d");
	ctx.globalCompositeOperation = 'destination-over';
	ctx.fillStyle = "#FFF";
	ctx.fillRect(0, 0, canvasElement.width, canvasElement.height);

	var MIME_TYPE = "image/" + type;

	var imgURL = canvasElement.toDataURL(MIME_TYPE);
	var dlLink = document.createElement('a');
	dlLink.download = fileName + "." + type;
	dlLink.href = imgURL;
	dlLink.dataset.downloadurl = [ MIME_TYPE, dlLink.download, dlLink.href ]
			.join(':');

	document.body.appendChild(dlLink);
	dlLink.click();
	document.body.removeChild(dlLink);
}

function filter1Changed() {
	var f1s;
	// Disable second filter if first filter not selected
	if (filter1.getValue().length > 0) {
		f1s = filter1.getValue();
		filter2.enable()
	} else {
		f1s = [];
		filter2.clear();
		filter2.collapse();
		filter2.disable();
	}
	var f2s = filter2.getValue();
	var noOfLines = f1s.length * jsonResponse.graphObj.displayFields.length;
	if (f2s.length > 0)
		noOfLines = noOfLines * f2s.length;
	if (noOfLines > 20) {
		$('#filter1 > .ms-helper').text(
				'Please limit your selection to 20 trend lines.')
		$('#filter1 > .ms-helper').show();
		filter1.removeFromSelection(filter1.getSelection().pop());
		return;
	}
	updateChart(f1s, f2s);
	if(f1s.length == jsonResponse.graphObj.filter1Set.length)
		filter1.collapse();
	eventTracking("Graphical Reports", "Filter applied", jsonResponse.graphObj.filterName1);
}

function filter2Changed() {
	var f1s = filter1.getValue();
	var f2s = filter2.getValue();
	var noOfLines = f1s.length * jsonResponse.graphObj.displayFields.length;
	if (f2s.length > 0)
		noOfLines = noOfLines * f2s.length;
	if (noOfLines > 20) {
		$('#filter2 > .ms-helper').text(
				'Please limit your selections to 20 lines.');
		$('#filter2 > .ms-helper').show();
		filter2.removeFromSelection(filter2.getSelection().pop());
		return;
	}
	if(noOfLines > 0)
		updateChart(f1s, f2s);
	if(f2s.length == jsonResponse.graphObj.filter2Set.length)
		filter2.collapse();
	eventTracking("Graphical Reports", "Secondary filter applied", jsonResponse.graphObj.filterName2);
}

function updateChart(f1s, f2s) {
	// Show clear all button only if something is selected
	if (filter1.getValue().length != 0)
		$('#filter1 .ms-clear-all').show();
	else
		$('#filter1 .ms-clear-all').hide();

	if (filter2.getValue().length != 0)
		$('#filter2 .ms-clear-all').show();
	else
		$('#filter2 .ms-clear-all').hide();

	var chartData = getChartData(f1s, f2s);

	chart.resize({
		height : setHeight()
	});

	chart.load({
		json : chartData,
		unload : true,
		done : function() {
			chart.tooltip.show({
				x : xlength-1
			});
		}
	});
	// Chart + legend height is constant so total height needs be increased to
	// keep chart height same, otherwise chart height becomes smaller as the
	// number of legend items increase
	function setHeight() {
		var multiplier = 1;
		var legendItemCount = $('.c3-legend-item').length;
		var legendItemHeight = 20;
		return (legendItemHeight * legendItemCount / 4)
				+ (window.innerHeight * 0.6);
	}
}

function showDefaultData() {
	fieldNumbers = jsonResponse.graphObj.displayFields.length;
	var chartData = {};
	chartData['trend'] = jsonResponse.data['trend'];
	for (var i = 0; i < fieldNumbers; i++) {
		var name = "Total " + jsonResponse.graphObj.displayFields[i];
		chartData[name] = jsonResponse.data[name];
	}
	chart.load({
		json : chartData,
		unload : true,
		done : function() {
			chart.tooltip.show({x:xlength-1});
			$('#loading').hide();
		}
	});
}

function getChartData(f1s, f2s) {
	var chartData = {};
	chartData['trend'] = jsonResponse.data['trend'];
	if (f1s.length) {
		for ( var k in f1s) {
			if (f2s.length) {
				for ( var l in f2s) {
					var name = f1s[k] + " " + f2s[l];
					// Create zero trend line for filter combinations which do
					// not have data
					chartData[name] = jsonResponse.data[name]
							|| Array.from(jsonResponse.data['trend'], function(
									v) {
								return 0;
							});
				}
			} else {
				var fieldNumbers = jsonResponse.graphObj.displayFields.length;
				for (var i = 0; i < fieldNumbers; i++) {
					var name = f1s[k] + " "
							+ jsonResponse.graphObj.displayFields[i];
					chartData[name] = jsonResponse.data[name];
				}
			}
		}
	} else {
		var fieldNumbers = jsonResponse.graphObj.displayFields.length;
		for (var i = 0; i < fieldNumbers; i++) {
			var name = "Total " + jsonResponse.graphObj.displayFields[i];
			chartData[name] = jsonResponse.data[name];
		}
	}
	return chartData;
}