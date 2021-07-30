package com.insta.hms.mdm.documenttypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;



@Repository
public class DocumentTypeRepository extends MasterRepository<String> {
  public DocumentTypeRepository() {
    super("doc_type", "doc_type_id", "doc_type_name");
  }
  
  @LazyAutowired
  private SessionService sessionService;
  
  private static final String GET_DOC_TYPES_BY_CATEGORY =
      " SELECT distinct(doc_type_id), doc_type_name "
          + " from doc_type dt LEFT JOIN doc_type_category_mapping"
          + " dtcm using (doc_type_id) LEFT JOIN "
          + " doc_category_master dtc ON (dtc.doc_category_id = dtcm.doc_type_category_id) where "
          + " status = 'A' AND ##CATEGORY_FILTER## ##SPECIALIZED_FILTER##"
          + " ORDER BY doc_type_name";

  /**
   * Get document types by category.
   * 
   * @param category document category
   * @return returns list of document types
   */
  public List<BasicDynaBean> getDocTypesByCategory(String category, String specialized) {
    String query = GET_DOC_TYPES_BY_CATEGORY;
    if (category == null || category.equals("")) {
      query = query.replace("##CATEGORY_FILTER##", " doc_type_category_mapping_id IS NULL  OR ");
    } else {
      query = query.replace("##CATEGORY_FILTER##", " dtc.doc_category_name = ? AND ");
    }
    query = query.replace("##SPECIALIZED_FILTER##", " dtc.specialized = ? ");
    if (specialized == null || specialized.equals("")) {
      specialized = "N";
    }
    return category == null || category.equals("")
        ? DatabaseHelper.queryToDynaList(query, new Object[] {specialized})
        : DatabaseHelper.queryToDynaList(query, new Object[] {category, specialized});
  }
  
  private static final String GET_TEMPLATE_NAMES_BY_CATEGORY_AND_CENTER_APPLICABILITY =
      "SELECT template_id, template_name, doc_format, doc_type_name, doc_type_id FROM ("
      + " (SELECT hvf.template_id, hvf.template_name, 'doc_hvf_templates' as doc_format,"
      + " doc_type_name, dt.doc_type_id FROM doc_type dt "
      + " JOIN doc_hvf_templates as hvf ON (dt.doc_type_id=hvf.doc_type) "
      + " JOIN doc_template_center_master dtcm ON (hvf.template_id = dtcm.template_id "
      + "  AND dtcm.doc_template_type = 'H' AND dtcm.status='A') "
      + " JOIN doc_type_category_mapping dtcma ON (dt.doc_type_id = dtcma.doc_type_id) "
      + "WHERE #docIdFilter dtcma.doc_type_category_id = :category"
      + " AND hvf.specialized=:specialized AND dtcm.center_id IN (:centerIds) "
      + " AND hvf.status='A' AND dt.status='A' #searchfilter LIMIT 25)"
      + " UNION "
      + " (SELECT rich.template_id, rich.template_name, 'doc_rich_templates' as doc_format,"
      + " doc_type_name, dt.doc_type_id FROM doc_type dt "
      + " JOIN doc_rich_templates as rich ON (dt.doc_type_id=rich.doc_type) "
      + " JOIN doc_template_center_master dtcm ON (rich.template_id = dtcm.template_id AND"
      + " dtcm.doc_template_type = 'R' AND dtcm.status='A') "
      + " JOIN doc_type_category_mapping dtcma ON (dt.doc_type_id = dtcma.doc_type_id) "
      + "WHERE #docIdFilter dtcma.doc_type_category_id = :category"
      + " AND rich.specialized=:specialized AND dtcm.center_id IN (:centerIds) "
      + " AND rich.status='A' AND dt.status='A' #searchfilter LIMIT 25)"
      + " UNION "
      + " (SELECT pdf.template_id, pdf.template_name, 'doc_pdf_form_templates' as doc_format,"
      + " doc_type_name, dt.doc_type_id FROM doc_type dt "
      + " JOIN doc_pdf_form_templates as pdf ON (dt.doc_type_id=pdf.doc_type) "
      + " JOIN doc_template_center_master dtcm ON (pdf.template_id = dtcm.template_id "
      + " AND dtcm.doc_template_type = 'P' AND dtcm.status='A')"
      + " JOIN doc_type_category_mapping dtcma ON (dt.doc_type_id = dtcma.doc_type_id) "
      + "WHERE #docIdFilter dtcma.doc_type_category_id = :category"
      + " AND pdf.specialized=:specialized AND dtcm.center_id IN (:centerIds) "
      + " AND pdf.status='A' AND dt.status='A' #searchfilter LIMIT 25)"
      + " UNION "
      + " (SELECT rtf.template_id, rtf.template_name, 'doc_rtf_templates' as doc_format,"
      + " doc_type_name, dt.doc_type_id FROM doc_type dt "
      + " JOIN doc_rtf_templates as rtf ON (dt.doc_type_id=rtf.doc_type) "
      + " JOIN doc_template_center_master dtcm ON (rtf.template_id = dtcm.template_id "
      + " AND dtcm.doc_template_type = 'T' AND dtcm.status='A') "
      + " JOIN doc_type_category_mapping dtcma ON (dt.doc_type_id = dtcma.doc_type_id) "
      + "WHERE #docIdFilter dtcma.doc_type_category_id = :category"
      + " AND rtf.specialized=:specialized AND dtcm.center_id IN (:centerIds) "
      + " AND rtf.status='A' AND dt.status='A' #searchfilter LIMIT 25)"
      + ") AS foo ORDER BY template_name";

  /**
   * Get document template names by category.
   * @param category category identifier
   * @param docTypeId document type identifier
   * @param specialized true indicates specialized
   * @return List of document template names
   */
  public List<BasicDynaBean> getDocTemplateNamesByCategory(String category, String docTypeId,
      boolean specialized, String searchQuery) {
    MapSqlParameterSource queryParams = new MapSqlParameterSource();
    List<Integer> centerIds = new ArrayList<>();
    centerIds.add(0);
    centerIds.add((Integer) sessionService.getSessionAttributes().get("centerId"));
    queryParams.addValue("category", category);
    queryParams.addValue("centerIds", centerIds);
    queryParams.addValue("specialized", specialized);
    queryParams.addValue("docTypeId", docTypeId);
    String docIdFilter = StringUtils.isEmpty(docTypeId) ? "" : "dt.doc_type_id = :docTypeId AND";
    String query = GET_TEMPLATE_NAMES_BY_CATEGORY_AND_CENTER_APPLICABILITY;
    query = query.replace("#docIdFilter", docIdFilter);
    if (StringUtils.isEmpty(searchQuery)) {
      return DatabaseHelper.queryToDynaList(query.replace("#searchfilter", ""), queryParams);
    }
    String[] searchWords = searchQuery.split(" ");
    String searchFilter =
        " AND (template_name ILIKE :ss OR template_name ILIKE :se OR template_name ILIKE :sb) ";
    StringBuilder searchFilterBuilder = new StringBuilder();
    for (int i = 0; i < searchWords.length; i++) {
      String filter = searchFilter.replace(":ss", ":ss" + i);
      filter = filter.replace(":se", ":se" + i);
      filter = filter.replace(":sb", ":sb" + i);
      searchFilterBuilder.append(filter);
      queryParams.addValue("ss" + i, "%" + searchWords[i]);
      queryParams.addValue("se" + i, searchWords[i] + "%");
      queryParams.addValue("sb" + i, "%" + searchWords[i] + "%");
    }
    query = query.replace("#searchfilter", searchFilterBuilder.toString());
    return DatabaseHelper.queryToDynaList(query, queryParams);
  }
  
}
