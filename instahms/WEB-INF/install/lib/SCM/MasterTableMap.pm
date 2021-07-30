package SCM::MasterTableMap;

use strict;
use warnings;


my %table_map = (


	XXNMC_HIS_GENERIC_MASTER_STG 	=> 'generic_name',

	XXNMC_HIS_ISSUE_UOM_MTR_STG 	=> 'package_issue_uom',

	XXNMC_HIS_ITEM_CONTROL_MTR_STG 	=> 'store_item_controltype',

	XXNMC_HIS_ITEM_FORM_MTR_STG 	=> 'item_form_master',

	XXNMC_HIS_MANUF_MASTER_STG 	=> 'manf_master',

	XXNMC_HIS_ROUTE_OF_ADMIN_STG 	=> 'medicine_route',

	XXNMC_HIS_STORE_ITEM_DTLS_STG 	=> 'store_item_details',

	XXNMC_HIS_STRENGTH_UNIT_STG 	=> 'strength_units',

	XXNMC_HIS_SUPPLIER_MTR_STG 	=> 'supplier_master',

#	XXNMC_HIS_CATEGORY_MASTER_STG => ITEM , INSURANCE, ASSET, SUPPLIER

	ITEM 		=> 'store_category_master',

	INSURANCE 	=> 'item_insurance_categories',

#	ASSET      	=>,

	SUPPLIER 	=> 'supplier_category_master',

);

my %table_field_hash = (

	generic_name => {

		generic_name => 'generic_name',
		generic_code => 'generic_code',
		status => 'status',
		classification_id => 'classification_id',
		sub_classification_id => 'sub_classification_id',
		standard_adult_dose => 'standard_adult_dose',
		criticality => 'criticality',
	},

	package_issue_uom => {

		package_uom => 'package_uom',
		issue_uom => 'issue_uom',
		package_size => 'package_size',
	},

	store_item_controltype => {

		control_type_id => 'control_type_id',
		control_type_name => 'control_type_name',
	},


	item_form_master => {

		item_form_id => 'item_form_id',
		item_form_name => 'item_form_name',
		status => 'status',
	},


	manf_master => {
		manf_code => 'manf_code',
		manf_name => 'manf_name',
		manf_address => 'manf_address',
		manf_city => 'manf_city',
		manf_state => 'manf_state',
		manf_country => 'manf_country',
		manf_pin => 'manf_pin',
		manf_phone1 => 'manf_phone1',
		manf_phone2 => 'manf_phone2',
		manf_fax => 'manf_fax',
		manf_mailid => 'manf_mailid',
		manf_website => 'manf_website',
		status => 'status',
		manf_mnemonic => 'manf_mnemonic',  # This field is mandatory which is not mention in data import.
		pharmacy => 'pharmacy',
		inventory => 'inventory',
	},

	medicine_route => {

		route_id => 'route_id',
		route_name => 'route_name',
		status => 'status',
		route_code => 'route_code',
	},

	store_item_details => {

		medicine_name => 'medicine_name',
		manf_name => 'manf_name',
		generic_name => 'generic_name',
		composition => 'composition',
		therapatic_use => 'therapatic_use',
		package_size  => 'issue_base_unit',
		package_type => 'package_type',
		h_drug_status => 'h_drug_status',
		status => 'status',
		medicine_short_name => 'medicine_short_name',
		med_category_id => 'med_category_id',
		issue_base_unit => 'issue_units',
		max_cost_price => 'max_cost_price',
		medicine_id => 'medicine_id',
		issue_qty => 'issue_qty',
		value => 'value',
		supplier_name => 'supplier_name',
		invoice_details => 'invoice_details',
		HAAD_CODE	=>	'HAAD_CODE',
		MOH_CODE	 => 	'ITEM_CED',
		CODE_TYPE	 => 	'CODE_TYPE',
		service_sub_group_id => 'service_sub_group_id',
		consumption_uom => 'consumption_uom',
		consumption_capacity => 'consumption_capacity',
		insurance_category_id => 'insurance_category_id',
		control_type_id => 'control_type_id',
		route_of_admin => 'route_of_admin',
		item_barcode_id => 'item_barcode_id',
		CODE_DESC	 => 	'CODE_DESC',
		prior_auth_required => 'prior_auth_required',
		package_uom => 'package_uom',
		preferred_supplier => 'preferred_supplier',
		item_form_id => 'item_form_id',
		item_strength => 'item_strength',
		creation_date => 'created_timestamp',  #column name different
		last_update_date => 'updated_timestamp', #column name different
		tax_rate => 'tax_rate',
		tax_type => 'tax_type',
		bin => 'bin',
		batch_no_applicable => 'batch_no_applicable',
		item_selling_price => 'item_selling_price',
		item_strength_units => 'item_strength_units',
		item_grace_period   => 'grace_period',
	},

	strength_units => {

		unit_id => 'unit_id',
		unit_name => 'unit_name',
		status => 'status',
	},



	supplier_master => {

		supplier_code => 'supplier_code',
		supplier_name => 'supplier_name',
		supplier_address => 'supplier_address',
		supplier_city => 'supplier_city',
		supplier_state => 'supplier_state',
		supplier_country => 'supplier_country',
		supplier_pin => 'supplier_pin',
		supplier_phone1 => 'supplier_phone1',
		supplier_phone2 => 'supplier_phone2',
		supplier_fax => 'supplier_fax',
		supplier_mailid => 'supplier_mailid',
		supplier_website => 'supplier_website',
		contact_person_name => 'contact_person_name',
		contact_person_mobile_number => 'contact_person_mobile_number',
		contact_person_mailid => 'contact_person_mailid',
		supplier_tin_no => 'supplier_tin_no',
		status => 'status',
		credit_period => 'credit_period',
		supp_category_id => 'supp_category_id',
	},


	 store_category_master => {
                category_id => 'category_id',
                category_name => 'category',
                identification => 'identification',
                issue_type => 'issue_type',
                billable => 'billable',    # This field is mandatory which is not mention to import the data from oracle
                status => 'status',
                claimable => 'claimable',
                expiry_date_val => 'expiry_date_val',
                retailable => 'retailable',
                discount => 'discount',
#               p_cat_vat_a_p => 'purchases_cat_vat_account_prefix',
#               p_cat_cst_a_p => 'purchases_cat_cst_account_prefix',
#               sales_cat_vat_a_p => 'sales_cat_vat_account_prefix',
                asset_tracking => 'asset_tracking',
        },

	item_insurance_categories => {
		category_id => 'insurance_category_id',
		category_name => 'insurance_category_name',
	},

	supplier_category_master => {
		category_id => 'supp_category_id',
		category_name => 'supp_category_name',

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

