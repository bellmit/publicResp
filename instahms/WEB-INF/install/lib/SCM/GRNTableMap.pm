package SCM::GRNTableMap;

use strict;
use warnings;


my %table_map = (

	XXNMC_STORE_GRN_MAIN_STG	=> 'store_grn_main',

	XXNMC_STORE_IVOICE_STG		=> 'store_invoice',

	XXNMC_HIS_GRN_LINE_DETAILS_STG	=> 'store_grn_details',

);

my %table_field_hash = (

		 store_grn_main => {

			GRN_NO				=> 'grn_no',
			GRN_DATE			=> 'grn_date',
			PO_NO				=> 'po_no',
			INVOICE_DATE		=> 'invoice_date',
			INVOICE_AMT			=> 'invoice_amt',
			REFERENCE			=> 'reference',
			USER_NAME			=> 'user_name',
			DEBIT_NOTE_NO		=> 'debit_note_no',
			SUPPLIER_INVOICE_ID	=> 'supplier_invoice_id',
			STORE_ID			=> 'store_id',
			CONSIGNMENT_STOCK	=> 'consignment_stock',
			GRN_QTY_UNIT		=> 'grn_qty_unit',
		},

		store_invoice => {

			SUPPLIER_ID			=> 'supplier_id',
			INVOICE_NO			=> 'invoice_no',
			INVOICE_DATE		=> 'invoice_date',
			DUE_DATE			=> 'due_date',
			PO_NO				=> 'po_no',
			PO_REFERENCE		=> 'po_reference',
			DISCOUNT			=> 'discount',
			ROUND_OFF			=> 'round_off',
			STATUS				=> 'status',
			PAYMENT_ID			=> 'payment_id',
			DISCOUNT_TYPE		=> 'discount_type',
			DISCOUNT_PER		=> 'discount_per',
			DATE_TIME			=> 'date_time',
			OTHER_CHARGES		=> 'other_charges',
			OTHER_CHARGES_REMARKS=> 'other_charges_remarks',
			REMARKS				=> 'remarks',
			PAID_DATE			=> 'paid_date',
			PAYMENT_REMARKS		=> 'payment_remarks',
			CESS_TAX_RATE		=> 'cess_tax_rate',
			CESS_TAX_AMT		=> 'cess_tax_amt',
			TAX_NAME			=> 'tax_name',
			CST_RATE			=> 'cst_rate',
			ACCOUNT_GROUP		=> 'account_group',
			SUPPLIER_INVOICE_ID	=> 'supplier_invoice_id',
			CONSIGNMENT_STOCK	=> 'consignment_stock',
			DEBIT_AMT			=> 'debit_amt',
			CASH_PURCHASE		=> 'cash_purchase',
			INVOICE_FILE_NAME	=> 'invoice_file_name',
			SUPPLIER_INVOICE_ATTACHMENT	=> 'supplier_invoice_attachment',
			INVOICE_CONTENTTYPE	=> 'invoice_contenttype',
		},

		store_grn_details => {

			GRN_NO				=> 'grn_no',
			BATCH_NO			=> 'batch_no',
			EXP_DT				=> 'exp_dt',
			MRP					=> 'mrp',
			BILLED_QTY			=> 'billed_qty',
			BONUS_QTY			=> 'bonus_qty',
			COST_PRICE			=> 'cost_price',
			DISCOUNT			=> 'discount',
			TAX_RATE			=> 'tax_rate',
			TAX					=> 'tax',
			ADJ_MRP				=> 'adj_mrp',
			TAX_TYPE			=> 'tax_type',
			OUTGOING_TAX_RATE	=> 'outgoing_tax_rate',
			MEDICINE_ID			=> 'medicine_id',
			ISSUE_QTY			=> 'issue_qty',
			TOTAL_QTY			=> 'total_qty',
			GRN_PKG_SIZE		=> 'grn_pkg_size',
			ORIG_DEBIT_RATE		=> 'orig_debit_rate',
			ORIG_DISCOUNT		=> 'orig_discount',
			ORIG_TAX			=> 'orig_tax',
			ITEM_CED_PER		=> 'item_ced_per',
			ITEM_CED			=> 'item_ced',
			ORIG_CED			=> 'orig_ced',
			ITEM_ORDER			=> 'item_order',
			GRN_PACKAGE_UOM		=> 'grn_package_uom',
			ITEM_BATCH_ID		=> 'item_batch_id',
			BONUS_TAX			=> 'bonus_tax',
		},


);


sub getTableFieldMap {


	my $table_name = $_[0];

	if(!exists  $table_field_hash{$table_name}) {

		print "Table field  map is not available, Please provide the correct table name : $table_name";
		return;
	}
	return %{$table_field_hash{$table_name}};
}

sub getPgTableName{

	my $ora_table_name = $_[0];

	if(!exists $table_map{$ora_table_name}){

		print "Oracle table is not available for postgres, Please provide the correct table name : $ora_table_name";
		return;
	}
	return $table_map{$ora_table_name};
}

