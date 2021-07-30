package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

/**
 * # Periodically fix the rate master, to work around bugs # that introduce missing charges. #
 * run_in_all_schemas
 *
 * @author insta This CronJob class is to fix issues with rate masters This class will fixes the
 *         following issues:
 *
 *         1. Missing rates are copied from ORG0001/GENERAL 2. Missing entries for xxx_org_details
 *         are created as applicable and no item code.
 *
 *
 *         Note: This relies on missing_xxx_views having been installed.
 *
 */

public class RateMasterJob extends SQLUpdateJob {

  private static List<String> queryList = new ArrayList<String>();

  static {

    /*
     * -- 1. Remove non-existent bed-types from charge tables.
     */

    queryList.add("DELETE FROM diagnostic_charges WHERE bed_type "
        + " NOT IN (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    /*
     * -- 2. Remove duplicates: in case the table does not have the primary key
     */

    /*
     * --SELECT remove_dups_on('diagnostic_charges', 'test_id, org_name, bed_type')
     *
     * -- 3. Insert missing values into the the charges table.
     *
     * -- a. Insert 0 where there is no charge for GENERAL/GENERAL itself.
     */

    queryList.add(
        "INSERT INTO diagnostic_charges(test_id, org_name, bed_type, priority, charge, discount) "
            + " SELECT m.test_id, m.org_id, m.bed_type, 'R', 0, 0 "
            + "FROM missing_test_charges_view m WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    /*
     * -- b. All other rate-plan/bed-type combinations, copy from GENERAL/GENERAL
     */
    queryList.add(
        "INSERT INTO diagnostic_charges (test_id, org_name, bed_type, priority, charge, discount) "
            + "SELECT m.test_id, m.org_id, m.bed_type, g.priority, g.charge, g.discount "
            + " FROM missing_test_charges_view m "
            + " JOIN diagnostic_charges g ON (g.test_id = m.test_id "
            + " AND g.org_name='ORG0001' AND g.bed_type='GENERAL') "
            + " WHERE (SELECT count(*) FROM "
            + " diagnostic_charges) != (SELECT count(*) FROM all_beds_orgs_view) * "
            + " (SELECT count(*) FROM diagnostics)");

    /*
     * -- 4. Insert into the org_details table for rate plan applicability
     */

    queryList.add("INSERT INTO test_org_details SELECT m.test_id, m.org_id "
        + " FROM missing_test_org_view m WHERE (SELECT count(*) "
        + " FROM test_org_details) != (SELECT count(*) FROM organization_details) "
        + " * (SELECT count(*) FROM diagnostics)");

    /*
     *
     * -- =====same for all other masters===== ...
     *
     * /
     * 
     * /* --===================Services=================================
     */

    queryList.add("DELETE FROM service_master_charges WHERE bed_type "
        + " NOT IN (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add(
        "INSERT INTO service_master_charges (service_id, bed_type, org_id, unit_charge, discount) "
            + " SELECT m.service_id, m.bed_type, m.org_id, 0, 0 "
            + " FROM missing_service_charges_view m WHERE bed_type = 'GENERAL' "
            + " and org_id = 'ORG0001'");

    queryList.add(
        "INSERT INTO service_master_charges (service_id, bed_type, org_id, unit_charge, discount) "
            + " SELECT m.service_id, m.bed_type, m.org_id, g.unit_charge, g.discount "
            + " FROM missing_service_charges_view m "
            + " JOIN service_master_charges g ON (g.service_id=m.service_id "
            + " AND g.org_id='ORG0001' AND g.bed_type='GENERAL') WHERE (SELECT count(*) "
            + " FROM service_master_charges) != (SELECT count(*) FROM all_beds_orgs_view) "
            + " * (SELECT count(*) FROM services)");

    queryList.add("INSERT INTO service_org_details SELECT m.service_id, m.org_id "
        + " FROM missing_service_org_view m WHERE "
        + " (SELECT count(*) FROM service_org_details) != "
        + " (SELECT count(*) FROM organization_details) * (SELECT count(*) FROM services)");

    /*
     * -- Operations
     */

    queryList.add("DELETE FROM operation_charges WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO operation_charges (op_id, org_id, bed_type, "
        + " surg_asstance_charge, surgeon_charge, "
        + " anesthetist_charge, surg_asst_discount, surg_discount, anest_discount) "
        + "SELECT m.op_id, m.org_id, m.bed_type, 0, 0, 0, 0, 0, 0 "
        + " FROM missing_operation_charges_view m WHERE bed_type = 'GENERAL' "
        + " and org_id = 'ORG0001'");

    queryList.add("INSERT INTO operation_charges (op_id, org_id, bed_type, surg_asstance_charge, "
        + " surgeon_charge, anesthetist_charge, surg_asst_discount, surg_discount, anest_discount) "
        + " SELECT m.op_id, m.org_id, m.bed_type, g.surg_asstance_charge, "
        + " g.surgeon_charge, g.anesthetist_charge, g.surg_asst_discount,"
        + "  g.surg_discount, g.anest_discount "
        + " FROM missing_operation_charges_view m JOIN operation_charges g "
        + " ON (g.op_id = m.op_id AND g.org_id = 'ORG0001' AND g.bed_type = 'GENERAL') "
        + " WHERE (SELECT count(*) FROM operation_charges) "
        + " != (SELECT count(*) FROM all_beds_orgs_view) * "
        + " (SELECT count(*) FROM operation_master)");

    queryList.add("INSERT INTO operation_org_details SELECT m.op_id, m.org_id "
        + " FROM missing_operation_org_view m "
        + " WHERE (SELECT count(*) FROM operation_org_details)"
        + " != (SELECT count(*) FROM organization_details) * "
        + " (SELECT count(*) FROM operation_master)");

    /*
     * -- Equipment
     */

    queryList.add("DELETE FROM equipement_charges " + " WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO equipement_charges ("
        + " equip_id, org_id, bed_type, daily_charge, min_charge, slab_1_charge, "
        + " incr_charge, tax, daily_charge_discount, min_charge_discount, "
        + " slab_1_charge_discount, incr_charge_discount) "
        + " SELECT m.eq_id, m.org_id, m.bed_type, 0, 0, 0, 0, 0, 0, 0, 0, 0 "
        + " FROM missing_equipment_charges_view m WHERE bed_type = 'GENERAL' "
        + " and org_id = 'ORG0001'");

    queryList.add("INSERT INTO equipement_charges (equip_id, org_id, bed_type, daily_charge, "
        + " min_charge, slab_1_charge, incr_charge, tax, daily_charge_discount, "
        + " min_charge_discount, slab_1_charge_discount, incr_charge_discount) "
        + " SELECT m.eq_id, m.org_id, m.bed_type,g.daily_charge, g.min_charge, "
        + " g.slab_1_charge, g.incr_charge, "
        + " tax,g.daily_charge_discount, g.min_charge_discount, "
        + " g.slab_1_charge_discount, g.incr_charge_discount "
        + " FROM missing_equipment_charges_view m JOIN equipement_charges g "
        + " ON (g.equip_id=m.eq_id AND g.org_id = 'ORG0001' AND g.bed_type = 'GENERAL') "
        + " WHERE (SELECT count(*) FROM equipement_charges) "
        + " != (SELECT count(*) FROM all_beds_orgs_view) * "
        + " (SELECT count(*) FROM equipment_master)");

    /*
     *
     * --no org_details for equipment
     *
     * /
     * 
     * /* -- ==============Theatre============
     */

    queryList.add("DELETE FROM theatre_charges WHERE bed_type "
        + " NOT IN (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO theatre_charges (theatre_id, org_id, bed_type,"
        + " daily_charge, min_charge, incr_charge, slab_1_charge, "
        + " tax,daily_charge_discount, min_charge_discount, incr_charge_discount, "
        + " slab_1_charge_discount) " + " SELECT m.theatre_id, m.org_id, m.bed_type, "
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0 " + " FROM missing_theatre_charges_view m "
        + " WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO theatre_charges (theatre_id, org_id, bed_type,"
        + " daily_charge, min_charge, incr_charge, slab_1_charge, "
        + " tax,daily_charge_discount, min_charge_discount, incr_charge_discount, "
        + " slab_1_charge_discount) "
        + " SELECT m.theatre_id, m.org_id, m.bed_type,g.daily_charge, "
        + " g.min_charge, g.incr_charge, g.slab_1_charge, g.tax,g.daily_charge_discount, "
        + " g.min_charge_discount, g.incr_charge_discount, g.slab_1_charge_discount "
        + " FROM missing_theatre_charges_view m JOIN theatre_charges g "
        + " ON (g.theatre_id=m.theatre_id AND g.org_id='ORG0001' AND g.bed_type='GENERAL') "
        + " WHERE (SELECT count(*) FROM theatre_charges) != "
        + " (SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) FROM theatre_master)");

    /*
     *
     * ==================Doctor IP consultation================
     *
     */

    queryList.add("DELETE FROM doctor_consultation_charge " + " WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO doctor_consultation_charge "
        + " (doctor_name, bed_type, organization, doctor_ip_charge, "
        + " night_ip_charge, ot_charge, "
        + " co_surgeon_charge, assnt_surgeon_charge, ward_ip_charge, "
        + " doctor_ip_charge_discount, night_ip_charge_discount, ot_charge_discount, "
        + " co_surgeon_charge_discount, assnt_surgeon_charge_discount, "
        + " ward_ip_charge_discount) SELECT m.doctor_id, m.bed_type, m.org_id, "
        + " 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 FROM "
        + " missing_doctor_charges_view m WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO doctor_consultation_charge "
        + " (doctor_name, bed_type, organization, doctor_ip_charge, "
        + " night_ip_charge, ot_charge, " + "co_surgeon_charge, assnt_surgeon_charge, "
        + " ward_ip_charge, doctor_ip_charge_discount, "
        + " night_ip_charge_discount, ot_charge_discount, "
        + " co_surgeon_charge_discount,assnt_surgeon_charge_discount, ward_ip_charge_discount) "
        + " SELECT m.doctor_id, m.bed_type, m.org_id, g.doctor_ip_charge, g.night_ip_charge, "
        + " g.ot_charge, g.co_surgeon_charge, g.assnt_surgeon_charge, "
        + " g.ward_ip_charge, g.doctor_ip_charge_discount, g.night_ip_charge_discount, "
        + " g.ot_charge_discount, g.co_surgeon_charge_discount, "
        + " g.assnt_surgeon_charge_discount, g.ward_ip_charge_discount "
        + " FROM missing_doctor_charges_view m JOIN doctor_consultation_charge g "
        + " ON (g.doctor_name=m.doctor_id AND g.organization='ORG0001' "
        + " AND g.bed_type='GENERAL') "
        + " WHERE (SELECT count(*) FROM doctor_consultation_charge) "
        + " != (SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) FROM doctors)");

    /*
     * ==========Doctor OP Consultation (no bed type)=====================
     */

    queryList.add("INSERT INTO doctor_op_consultation_charge (doctor_id, org_id,op_charge, "
        + " op_revisit_charge, private_cons_charge, "
        + "private_cons_revisit_charge, op_oddhr_charge,op_charge_discount, "
        + " op_revisit_discount, private_cons_discount, "
        + " private_revisit_discount,op_oddhr_charge_discount) "
        + " SELECT m.doctor_id, m.org_id, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 "
        + "FROM missing_doctor_op_charges_view m WHERE org_id = 'ORG0001'");

    queryList.add("INSERT INTO doctor_op_consultation_charge "
        + " (doctor_id, org_id, op_charge, op_revisit_charge, private_cons_charge, "
        + " private_cons_revisit_charge, op_oddhr_charge, op_charge_discount, "
        + " op_revisit_discount, private_cons_discount, "
        + " private_revisit_discount, op_oddhr_charge_discount) "
        + " SELECT m.doctor_id, m.org_id, g.op_charge, g.op_revisit_charge, "
        + " g.private_cons_charge, g.private_cons_revisit_charge, "
        + " g.op_oddhr_charge, g.op_charge_discount, g.op_revisit_discount, "
        + " g.private_cons_discount, g.private_revisit_discount, g.op_oddhr_charge_discount "
        + " FROM missing_doctor_op_charges_view m " + " JOIN doctor_op_consultation_charge g "
        + " ON (m.doctor_id=g.doctor_id AND g.org_id='ORG0001')");

    /*
     * -- no org details
     *
     *
     * -- Consultation Charges
     *
     */

    queryList.add("DELETE FROM consultation_charges "
        + " WHERE bed_type NOT IN (SELECT bed_type_name FROM bed_types "
        + " WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO consultation_charges (consultation_type_id, org_id, "
        + " bed_type, charge, discount) "
        + "SELECT m.consultation_type_id, m.org_id, m.bed_type, 0, 0 "
        + " FROM missing_consultation_charges_view m "
        + "WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");
    queryList.add("INSERT INTO consultation_charges (consultation_type_id, org_id, "
        + " bed_type, charge, discount) "
        + " SELECT m.consultation_type_id, m.org_id, m.bed_type, g.charge, "
        + " g.discount FROM missing_consultation_charges_view m "
        + " JOIN consultation_charges g ON "
        + " (g.consultation_type_id=m.consultation_type_id AND g.org_id = 'ORG0001' "
        + " AND g.bed_type='GENERAL')");

    queryList.add("INSERT INTO consultation_org_details (consultation_type_id, org_id) "
        + "SELECT m.consultation_type_id, m.org_id FROM missing_consultation_org_view m");

    /*
     * -- -- Anesthesia charges --
     */

    queryList.add("DELETE FROM anesthesia_type_charges WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add(
        "INSERT INTO anesthesia_type_charges (anesthesia_type_id, org_id, bed_type, min_charge, "
            + " slab_1_charge, incr_charge, min_charge_discount, "
            + " slab_1_charge_discount, incr_charge_discount) "
            + " SELECT m.anesthesia_type_id, m.org_id, " + " m.bed_type, 0, 0, 0, 0, 0, 0 "
            + " FROM missing_anesthesia_type_charges_view m WHERE bed_type = 'GENERAL' and "
            + " org_id = 'ORG0001'");

    queryList.add(
        "INSERT INTO anesthesia_type_charges (anesthesia_type_id, org_id, bed_type, min_charge, "
            + " slab_1_charge, incr_charge,min_charge_discount, "
            + " slab_1_charge_discount, incr_charge_discount) SELECT m.anesthesia_type_id, "
            + " m.org_id, m.bed_type, g.min_charge, "
            + " g.slab_1_charge, g.incr_charge, g.min_charge_discount, g.slab_1_charge_discount, "
            + " g.incr_charge_discount " + " FROM missing_anesthesia_type_charges_view m "
            + " JOIN anesthesia_type_charges g ON "
            + " (m.anesthesia_type_id=g.anesthesia_type_id AND g.org_id='ORG0001' "
            + " AND g.bed_type='GENERAL')"
            + " WHERE (SELECT count(*) FROM anesthesia_type_charges) != (SELECT count(*) "
            + " FROM all_beds_orgs_view) * (SELECT count(*) FROM anesthesia_type_master)");

    queryList.add("INSERT INTO anesthesia_type_org_details (anesthesia_type_id, org_id) "
        + " SELECT m.anesthesia_type_id, m.org_id FROM missing_anesthesia_type_org_view m");

    /*
     * -- -- Package charges --
     *
     */
    queryList.add("DELETE FROM package_charges WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO package_charges (package_id, org_id, bed_type, charge, discount) "
        + " SELECT m.package_id, m.org_id, m.bed_type, 0, 0 "
        + " FROM missing_package_charges_view m "
        + " WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO package_charges (package_id, org_id, bed_type, charge, discount) "
        + "SELECT m.package_id, m.org_id, m.bed_type, g.charge, g.discount "
        + "FROM missing_package_charges_view m "
        + "JOIN package_charges g ON (g.package_id=m.package_id AND "
        + "g.org_id='ORG0001' AND g.bed_type='GENERAL') "
        + "WHERE (SELECT count(*) FROM package_charges) != "
        + "(SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) FROM packages)");

    /*
     * -- -- Dyna Package Charges --
     */

    queryList.add("DELETE FROM dyna_package_charges WHERE bed_type NOT IN "
        + "(SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO dyna_package_charges (dyna_package_id, org_id, bed_type, charge) "
        + "SELECT m.dyna_package_id, m.org_id, m.bed_type, 0 "
        + "FROM missing_dyna_package_charges_view m "
        + "WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO dyna_package_charges (dyna_package_id, org_id, bed_type, charge) "
        + "SELECT m.dyna_package_id, m.org_id, m.bed_type, g.charge "
        + "FROM missing_dyna_package_charges_view m JOIN dyna_package_charges g ON "
        + "(g.dyna_package_id=m.dyna_package_id "
        + "AND g.org_id='ORG0001' AND g.bed_type='GENERAL') WHERE (SELECT count(*) "
        + "FROM dyna_package_charges) != (SELECT count(*) FROM all_beds_orgs_view) * "
        + "(SELECT count(*) FROM dyna_packages)");

    queryList.add("INSERT INTO dyna_package_org_details SELECT m.dyna_package_id, m.org_id "
        + " FROM missing_dyna_package_org_view m WHERE (SELECT count(*) "
        + " FROM dyna_package_org_details) != (SELECT count(*) "
        + " FROM organization_details) * (SELECT count(*) FROM dyna_packages)");

    queryList.add("INSERT INTO dyna_package_category_limits (dyna_package_id, org_id, bed_type, "
        + " dyna_pkg_cat_id, pkg_included, amount_limit, qty_limit) "
        + " SELECT m.dyna_package_id, m.org_id, m.bed_type, m.dyna_pkg_cat_id, 'Y', 0, 0 "
        + " FROM missing_dyna_package_limits_view m "
        + " WHERE (SELECT count(*) FROM dyna_package_category_limits) != (SELECT count(*) "
        + " FROM all_beds_orgs_view) * (SELECT count(*) FROM dyna_packages) "
        + " * (SELECT count(*) FROM dyna_package_category_limits)");

    /*
     * -- -- Diet charges --
     */

    queryList.add("DELETE FROM diet_charges WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO diet_charges (diet_id, org_id, bed_type, charge, discount) "
        + " SELECT m.diet_id, m.org_id, m.bed_type, 0, 0 "
        + "FROM missing_diet_charges_view m WHERE bed_type = 'GENERAL' AND org_id = 'ORG0001'");

    queryList.add("INSERT INTO diet_charges (diet_id, org_id, bed_type, charge, discount) "
        + " SELECT m.diet_id, m.org_id, m.bed_type, g.charge, g.discount "
        + " FROM missing_diet_charges_view m "
        + " JOIN diet_charges g ON (g.diet_id = m.diet_id AND "
        + " g.org_id='ORG0001' AND g.bed_type = 'GENERAL') "
        + " WHERE (SELECT count(*) FROM diet_charges) != "
        + " (SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) FROM diet_master)");

    /*
     * -- -- Beds and ICU --
     */

    queryList
        .add("INSERT INTO bed_details (bed_type, organization, intensive_bed_status, bed_charge, "
            + " nursing_charge, initial_payment, duty_charge, maintainance_charge, luxary_tax, "
            + " hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, "
            + " daycare_slab_3_charge) "
            + " SELECT m.bed_type, m.org_id, 'N', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 FROM "
            + " missing_bed_charges_view m WHERE bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO bed_details (bed_type, organization, intensive_bed_status, "
        + " bed_charge, nursing_charge, initial_payment, duty_charge, "
        + " maintainance_charge, luxary_tax, hourly_charge, daycare_slab_1_charge, "
        + " daycare_slab_2_charge, daycare_slab_3_charge) "
        + " SELECT m.bed_type, m.org_id, 'N', g.bed_charge, g.nursing_charge, "
        + " g.initial_payment, g.duty_charge, g.maintainance_charge, "
        + " g.luxary_tax, g.hourly_charge, g.daycare_slab_1_charge, g.daycare_slab_2_charge, "
        + " g.daycare_slab_3_charge " + " FROM missing_bed_charges_view m JOIN bed_details g ON "
        + " (g.bed_type = 'GENERAL' AND g.organization = 'ORG0001')");

    queryList.add("INSERT INTO icu_bed_charges (intensive_bed_type, organization, bed_type, "
        + " bed_charge, nursing_charge, initial_payment, duty_charge, "
        + "maintainance_charge, luxary_tax, hourly_charge, daycare_slab_1_charge, "
        + " daycare_slab_2_charge, daycare_slab_3_charge) "
        + " SELECT m.intensive_bed_type, m.org_id, m.bed_type, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 "
        + " FROM missing_icu_bed_charges_view m WHERE "
        + " bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList.add("INSERT INTO icu_bed_charges (intensive_bed_type, organization, bed_type, "
        + " bed_charge, nursing_charge, initial_payment, duty_charge, "
        + " maintainance_charge, luxary_tax, hourly_charge, daycare_slab_1_charge, "
        + " daycare_slab_2_charge, daycare_slab_3_charge) "
        + " SELECT m.intensive_bed_type, m.org_id, m.bed_type, g.bed_charge, g.nursing_charge, "
        + " g.initial_payment, g.duty_charge, "
        + " g.maintainance_charge, g.luxary_tax, g.hourly_charge, g.daycare_slab_1_charge, "
        + " g.daycare_slab_2_charge, "
        + " g.daycare_slab_3_charge FROM missing_icu_bed_charges_view m JOIN icu_bed_charges "
        + " g ON (g.bed_type = 'GENERAL' " + " AND g.organization = 'ORG0001' AND "
        + " g.intensive_bed_type = m.intensive_bed_type)");

    /*
     * -- -- Registration Charges --
     */

    queryList
        .add("INSERT INTO registration_charges (org_id, bed_type, ip_reg_charge, op_reg_charge, "
            + " gen_reg_charge, reg_renewal_charge, mrcharge,"
            + " ip_mlccharge, op_mlccharge) SELECT m.org_id, m.bed_type, 0, 0, 0, 0, 0, 0, 0 "
            + " FROM missing_registration_charges_view m WHERE "
            + " bed_type = 'GENERAL' and org_id = 'ORG0001'");

    queryList
        .add("INSERT INTO registration_charges (org_id, bed_type, ip_reg_charge, op_reg_charge, "
            + " gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge) "
            + " SELECT m.org_id, m.bed_type, g.ip_reg_charge, g.op_reg_charge, "
            + " g.gen_reg_charge, g.reg_renewal_charge, g.mrcharge, g.ip_mlccharge, g.op_mlccharge "
            + " FROM missing_registration_charges_view m "
            + " JOIN registration_charges g ON (g.bed_type = 'GENERAL' AND g.org_id = 'ORG0001')");
    /*
     * -- -- Per Diem Charges --
     */

    queryList.add("DELETE FROM per_diem_codes_charges WHERE bed_type NOT IN "
        + " (SELECT bed_type_name FROM bed_types WHERE billing_bed_type = 'Y')");

    queryList.add("INSERT INTO per_diem_codes_charges (per_diem_code, org_id, bed_type, charge) "
        + " SELECT m.per_diem_code, m.org_id, m.bed_type, 0 "
        + "FROM missing_per_diem_codes_charges_view m WHERE bed_type = 'GENERAL' "
        + " and org_id = 'ORG0001'");

    queryList.add("INSERT INTO per_diem_codes_charges (per_diem_code, org_id, bed_type, charge) "
        + " SELECT m.per_diem_code, m.org_id, m.bed_type, g.charge "
        + " FROM missing_per_diem_codes_charges_view m "
        + " JOIN per_diem_codes_charges g ON (g.per_diem_code=m.per_diem_code AND "
        + " g.org_id='ORG0001' AND g.bed_type='GENERAL') "
        + " WHERE (SELECT count(*) FROM per_diem_codes_charges) "
        + " != (SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) "
        + " FROM per_diem_codes_master)");

    /*
     * -- -- Store Item Charges --
     */

    queryList.add("INSERT INTO store_item_rates SELECT m.store_rate_plan_id, m.medicine_id, "
        + " 0 FROM missing_store_item_charges_view m");

  }

  @Override
  protected List<String> getQueryList() {
    return queryList;
  }

}
