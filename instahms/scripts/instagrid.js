/*
 * Wrapper over yui datatable, to simulate ActiveWidgets API, which
 * we use no longer.
 */

Insta = function() {};

Insta.Grid = function(id) {
    this.name = id;
    this.dirty = false;
    this.height = 0;
    this.width = 0;
    this.needEditing = false;
    this.columnDefs = new Array();
    this.data = new YAHOO.util.DataSource([]);
    this.data.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
    this.data.responseSchema = { fields: ["rid"] };
    this.selectionMode = "single";

    this.setSize = function(w,h) {
        this.width = w;
        this.height = h -20;  // AW height includes header, YUI height excludes
        this.dirty = true;
    };

    this.setHeaderHeight = function(h) { /*ignore*/ };
    this.setColumnCount = function(h) { /*ignore*/ };
    this.clearCellModel = function() { /*ignore*/ };
    this.clearRowModel = function() { /*ignore*/ };
    this.clearScrollModel = function() { /*ignore*/ };
    this.clearSortModel = function() { /*ignore*/ };
    this.clearSelectionModel = function() { /*ignore*/ };

    this.setRowCount = function(count) {
        // do anything only if the grid is active
        if (this.gridImpl) {
            this.gridImpl.getRecordSet().reset();
            for (var row=0 ; row<count ; row++) {
                this.gridImpl.addRow({rid: row});
                // todo: not calling onRowAdded here, OK?
            }
        }
    }

    this.setSelectionMode = function (s) {
        if (s == "multi-row-marker") {
            this.selectionMode = "standard";
        } else {
            this.selectionMode = "single";
        }
    };

    /*
     * The following functions setup the column properties. In AW, each column property
     * is set by a function call, whereas YUI expects it to be supplied in the columnDefs
     * array. Thus, we just manipulate the columnDefs in the column property functions.
     */
    this.setHeaderText = function(labels) {
        /* input is an array of column lables only */
        for (var i=0; i<labels.length; i++) {
            if (!this.columnDefs[i]) {
                this.columnDefs[i] = {};
            }
            this.columnDefs[i].key = "col" + i;
            this.columnDefs[i].label = labels[i];
            this.columnDefs[i].resizeable = true;
            // also add a field in the responseSchema
            this.data.responseSchema.fields[i+1] = "col" + i;
        }
        this.dirty = true;
    };

    this.setCellEditable = function(enable, col) {
        /* if column is given, do it for that column alone. Else, do it for all columns */
        var startColumn = 0; var endColumn = this.columnDefs.length-1;
        if (col != null) {
            startColumn = col; endColumn = col;
        }
        for (var column=startColumn; column<=endColumn; column++) {
            if (enable) {
                // Although YUI supports different editors, AW supports only textbox.
                this.columnDefs[column].editor = "textbox";
                this.needEditing = true;
            } else {
                this.columnDefs[column].editor = null;
            }
            if (this.gridImpl) {
                // dynamic change: we need to remove and re-add the row with new properties.
                if (this.gridImpl.getRecordSet().getLength() > 0) {
                    alert("Warning: Setting cell editable will clear the row data. " +
                       "Move setCellEditable to initialization stage, or before adding any rows.");
                }
                var oColumn = this.gridImpl.getColumn(column);
                this.gridImpl.removeColumn(oColumn);
                this.gridImpl.insertColumn(this.columnDefs[column], column);
                // note: this loses all data. There is no other alternative. The change only
                // applies to rows that are being added anew.
                this.gridImpl.initializeTable();
                this.gridImpl.render();
            }
        }
    };

    this.setCellFormat = function(formats) {
        // assume setHeaderText has been called by now.
        for (var i=0; i<formats.length; i++) {
            this.columnDefs[i].formatter = formats[i];
        }
    };

    this.getCellFormat = function(col,row) {
        return this.columnDefs[col].formatter;
    }

    this.setColumnWidth = function(w,i) {
        // todo: column widths don't seem to take effect till we have at least one row
        if (this.gridImpl) {
            this.gridImpl.setColumnWidth(this.columnDefs[i],w);
        } else {
            this.columnDefs[i].width = w - 25;  // YUI includes padding
        }
    };

    this.findRecordID = function(rid) {
        var recordSet = this.gridImpl.getRecordSet();
        for (var r=0; r<recordSet.getLength(); r++) {
            var record = recordSet.getRecord(r);
            if (record.getData("rid") == rid) {
                return r;
            }
        }
        return null;
    }

    /* 
     * This is when we really instantiate the widget, this is called when
     * window is loaded. YUI expects an existing element in the DOM,
     * whereas AW creates that element as innerHTML 
     */
    this.init = function() {
        //this.logReader = new YAHOO.widget.LogReader();

        // the following is required for proper functioning in IE7. See 
        // http://developer.yahoo.com/yui/datatable under top known issues for more info
        YAHOO.widget.DataTable._bStylesheetFallback = !!YAHOO.env.ua.ie;

        /* instantiate the widget now */
        this.gridImpl = new YAHOO.widget.DataTable(this.name, this.columnDefs, this.data,
            { width: this.width+"px", height: this.height+"px", scrollable: true,
              selectionMode: this.selectionMode }
        );
        this.gridImpl.instaWrapper = this;

        /* enable row selection */
        this.gridImpl.subscribe("rowClickEvent",this.gridImpl.onEventSelectRow);

        /* if editing is enabled, need to subscribe for more events */
        if (this.needEditing) {
            this.gridImpl.subscribe("editorSaveEvent", function(oArgs) { 
                var text = oArgs.newData;
                var colID = oArgs.editor.column.key;
                var col = colID.substring(3);
                var row = oArgs.editor.record.getData("rid");
                var invalid = false;
                if (this.instaWrapper.onCellValidating) {
                    invalid = this.instaWrapper.onCellValidating(text,col,row);
                }
                if (invalid) {
                    // undo, set back the old data
                    this.getRecordSet().updateRecordValue(
                        oArgs.editor.record, colID, oArgs.oldData);
                    this.render();
                } else {
                    if (this.instaWrapper.onCellEditEnded && !invalid) {
                        this.instaWrapper.onCellEditEnded(text,col,row); 
                    }
                }
            });

            this.gridImpl.subscribe("cellMouseoverEvent", this.highlightEditableCell); 
            this.gridImpl.subscribe("cellMouseoutEvent", this.gridImpl.onEventUnhighlightCell); 
            this.gridImpl.subscribe("cellClickEvent", this.gridImpl.onEventShowCellEditor); 
        }

        if (this.onCellDoubleClicked) {
            this.gridImpl.subscribe("cellDblclickEvent", function(oArgs) {
                var recordSet = this.getRecordSet(); 
                var record = this.getRecord(oArgs.target);
                var row = record.getData("rid");
                if (this.instaWrapper.onCellDoubleClicked) {
                    this.instaWrapper.onCellDoubleClicked("", 0, row);
                }
            });
        }
    };

    this.highlightEditableCell = function(oArgs) { 
        var elCell = oArgs.target; 
        //YAHOO.log("highlight editable cell, this is: " + this);
        if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) { 
            this.highlightCell(elCell); 
        }
    };

    this.toString = function() {
        var style = "text-align: left;";
        style += "width: " + this.width + "px;";
        style += "font-size: 11px;";
        return  "<div class='yui-skin-sam' style='" + style + "'>" +
                "<div id='" + this.name + "'>" +
                "</div></div>";
    };

    /*
     * The following are typically called after the init
     */
    this.getRowCount = function() {
        if (this.gridImpl) {
            return this.gridImpl.getRecordSet().getLength();
        } else {
            return 0;
        }
    };

    this.getRowIndices = function() {
        if (this.gridImpl) {
            var recordSet = this.gridImpl.getRecordSet(); 
            var numRows = recordSet.getLength();
            var indices = new Array(numRows);
            for (i=0; i<numRows; i++) {
                indices[i] = recordSet.getRecord(i).getData("rid");
            }
            return indices;
        } else {
            return [];
        }
    };

    this.getCurrentRow = function() {
        if (this.gridImpl) {
            var selRecordId = this.gridImpl.getLastSelectedRecord();
            if (selRecordId) { 
                var selRecord = this.gridImpl.getRecord(selRecordId);
                if (selRecord) { return selRecord.getData("rid"); }
            }
        }
        return null;
    };

    this.getRowTemplate = function(rid) {
        if (this.gridImpl) {
            var row = this.findRecordID(rid);
            //YAHOO.log("Row template: " + this.gridImpl.getTrEl(row));
            return this.gridImpl.getTrEl(row);
        } else {
            return null;
        }
    }

    this.addRow = function(rid) {
        /*
         * The index that they give us is a record identifier, not a sequence number 
         * neither an index into the list of records. This is what we return for getCurrentRow,
         * and this is what we get called with for deleteRow, setCellText etc.
         */
        if (this.gridImpl) {
            this.gridImpl.addRow({rid: rid});
            if (this.onRowAdded) {
                this.onRowAdded(rid);
            }
        } else {
            // todo: maybe store the data somewhere so that init can use it
            alert("Insta.Grid: Cannot add rows before initialization");
        }
    };

    this.getCellText = function(column,rid) {
        if (this.gridImpl) {
            var row = this.findRecordID(rid);
            var recordSet = this.gridImpl.getRecordSet();
            var record = recordSet.getRecord(row);
            return record.getData("col"+column);
        } else {
            return null;
        }
    };

    this.getCellValue = this.getCellText;

    this.setCellText = function(value,column,rid) {
        if (this.gridImpl) {
            var row = this.findRecordID(rid);
            var recordSet = this.gridImpl.getRecordSet();
            var record = recordSet.getRecord(row);
            if (record) {
                record.setData("col"+column, value);
             } else {
                alert("Insta.Grid: Cannot find row: " + rid + ". Please add row first");
             }
            //recordSet.updateRecordValue(row, "col"+column, value);
            //this.gridImpl.render();
        } else {
            // todo: maybe store the data somewhere so that init can use it
            alert("Insta.Grid: Cannot set cell text before initialization");
        }
    };

    this.deleteRow = function(rid) {
        if (this.gridImpl) {
            var index = this.findRecordID(rid);
            this.gridImpl.deleteRow(index);
            if (this.onRowDeleted) {
                this.onRowDeleted(rid);
            }
        } else {
            // todo: maybe store the data somewhere so that init can use it
            alert("Insta.Grid: Cannot delete rows before initialization");
        }
    };

    this.setRowSelected = function (select, row) {
        if (this.gridImpl) {
            if (select) {
                this.gridImpl.selectRow(row);
            } else {
                this.gridImpl.unselectRow(row);
            }
        }
    };

    this.getSelectedRows = function () {
        if (this.gridImpl) {
            var selRecords = this.gridImpl.getSelectedRows();
            var selRowIDs = new Array();
            for (var i=0; i<selRecords.length; i++) {
                selRowIDs[i] = this.gridImpl.getRecordSet().getRecord(selRecords[i]).getData("rid");
            }
            return selRowIDs;
        }
    };

    this.refresh = function() {
        if (this.gridImpl) {
            this.gridImpl.render();
        }
    };

    this.render = this.refresh;

    //YAHOO.util.Event.addListener(window, "load", function() {this.init()});
};

