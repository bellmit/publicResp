package com.insta.hms.documents;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;

import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentTemplateRepository.
 */
@Repository
public abstract class GenericDocumentTemplateRepository extends GenericRepository {

  // RC TODO : Incorrect constructor, the table name should be specified while
  // calling the super ctor and should not be passed in as a parameter to this
  // ctor

  /**
   * Instantiates a new generic document template repository.
   *
   * @param tableName the table name
   */
  public GenericDocumentTemplateRepository(String tableName) {
    super(tableName);
    // TODO Auto-generated constructor stub
  }

  /**
   * Instantiates a new generic document template repository.
   */
  // To solve component scan of test cases
  public GenericDocumentTemplateRepository() {
    super("");
    // Don't use this constructor. As this is not pointing to any of the table. Use argument
    // constructor.
  }

  /** The Constant ALL_TEMPLATE_FIELDS. */
  public static final String ALL_TEMPLATE_FIELDS = "select doc_type_id, doc_type_name, "
      + "dept_name, foo.template_id, format, "
      + "template_name, status ";

  /** The Constant ALL_TEMPLATE_TABLES. */
  public static final String ALL_TEMPLATE_TABLES = " FROM (select dt.doc_type_id ,"
      + " dt.doc_type_name, "
      + " hvf.template_id,'doc_hvf_templates' as format, hvf.template_name,"
      + " hvf.status, hvf.dept_name, hvf.specialized "
      + " FROM doc_type dt JOIN doc_hvf_templates as hvf ON dt.doc_type_id=hvf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rich.template_id,'doc_rich_templates' "
      + "as format, rich.template_name,"
      + " rich.status, rich.dept_name, rich.specialized  FROM doc_type dt JOIN doc_rich_templates "
      + "as rich ON dt.doc_type_id=rich.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, pdf.template_id,'doc_pdf_form_templates' "
      + "as format, pdf.template_name, "
      + " pdf.status, pdf.dept_name, pdf.specialized FROM doc_type dt JOIN  "
      + "doc_pdf_form_templates as pdf ON dt.doc_type_id=pdf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rtf.template_id ,'doc_rtf_templates' "
      + "as format, rtf.template_name, "
      + " rtf.status,rtf.dept_name, rtf.specialized FROM doc_type dt JOIN doc_rtf_templates "
      + "as rtf on dt.doc_type_id=rtf.doc_type) as foo";

  /** The Constant ALL_CENTER_TEMPLATE_FIELDS. */
  public static final String ALL_CENTER_TEMPLATE_FIELDS = "select distinct template_name,"
      + " doc_type_id, doc_type_name, dept_name, foo.template_id, format, "
      + "status, templ_cen_status ";

  /** The Constant ALL_CENTER_TEMPLATE_TABLES. */
  public static final String ALL_CENTER_TEMPLATE_TABLES = " FROM (select dt.doc_type_id ,"
      + " dt.doc_type_name, "
      + " hvf.template_id,'doc_hvf_templates' as format, 'H' as type, hvf.template_name, "
      + "hvf.status, dtcm.status as templ_cen_status, hvf.dept_name, hvf.specialized, "
      + " dtcm.center_id "
      + " FROM doc_type dt JOIN doc_hvf_templates as hvf ON dt.doc_type_id=hvf.doc_type "
      + " JOIN doc_template_center_master dtcm on(hvf.template_id = dtcm.template_id "
      + "AND dtcm.doc_template_type = 'H')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rich.template_id,'doc_rich_templates' "
      + "as format, 'R' as type, rich.template_name,"
      + " rich.status, dtcm.status as templ_cen_status, rich.dept_name, rich.specialized,"
      + " dtcm.center_id FROM doc_type dt JOIN doc_rich_templates as rich "
      + " ON dt.doc_type_id=rich.doc_type "
      + " JOIN doc_template_center_master dtcm on(rich.template_id = dtcm.template_id AND"
      + " dtcm.doc_template_type = 'R')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, pdf.template_id,'doc_pdf_form_templates'"
      + " as format, 'P' as type, pdf.template_name, "
      + " pdf.status, dtcm.status as templ_cen_status, pdf.dept_name, pdf.specialized,"
      + " dtcm.center_id FROM doc_type dt JOIN  doc_pdf_form_templates as pdf "
      + "ON dt.doc_type_id=pdf.doc_type "
      + " JOIN doc_template_center_master dtcm on(pdf.template_id = dtcm.template_id "
      + "AND dtcm.doc_template_type = 'P')"
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rtf.template_id ,'doc_rtf_templates' "
      + " as format, 'T' as type, rtf.template_name, "
      + " rtf.status, dtcm.status as templ_cen_status, rtf.dept_name, rtf.specialized, "
      + " dtcm.center_id FROM doc_type dt JOIN doc_rtf_templates as rtf "
      + " on dt.doc_type_id=rtf.doc_type"
      + " JOIN doc_template_center_master dtcm on(rtf.template_id = dtcm.template_id"
      + "  AND dtcm.doc_template_type = 'T')"
      + ") as foo ";

  /** The Constant ALL_TEMPLATE_COUNT. */
  private static final String ALL_TEMPLATE_COUNT = " SELECT count(doc_type_id) ";

  /**
   * Gets the generic doc templates.
   *
   * @param filterParams the filter params
   * @param specialization the specialization
   * @param listingParams the listing params
   * @return the generic doc templates
   * @throws ParseException the parse exception
   */
  public static PagedList getGenericDocTemplates(Map filterParams, Boolean specialization,
      Map<LISTING, Object> listingParams) throws ParseException {
    SearchQueryAssembler qb = null;
    if (specialization) {
      qb = new SearchQueryAssembler(ALL_TEMPLATE_FIELDS, ALL_TEMPLATE_COUNT, ALL_TEMPLATE_TABLES,
          listingParams);
      qb.addFilterFromParamMap(filterParams);
      qb.addFilter(QueryAssembler.BOOLEAN, "specialized", "=", specialization);
      qb.addSecondarySort("template_name");
    } else {
      qb = new SearchQueryAssembler(ALL_CENTER_TEMPLATE_FIELDS, ALL_TEMPLATE_COUNT,
          ALL_CENTER_TEMPLATE_TABLES, listingParams);
      qb.addFilterFromParamMap(filterParams);
      qb.addFilter(QueryAssembler.BOOLEAN, "specialized", "=", specialization);
      qb.addSecondarySort("template_name");
      int centerID = RequestContext.getCenterId();
      if (centerID != 0) {
        List values = new ArrayList();
        values.add(0);
        values.add(centerID);
        qb.addFilter(QueryAssembler.INTEGER, "center_id", "IN", values);
      }
      qb.addFilter(QueryAssembler.STRING, "templ_cen_status", "=", "A");
    }
    qb.build();

    return qb.getMappedPagedList();
  }

}
