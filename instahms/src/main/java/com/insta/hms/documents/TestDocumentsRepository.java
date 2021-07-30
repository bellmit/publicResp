package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDocumentsRepository.
 */
@Repository
public class TestDocumentsRepository extends GenericRepository {

  /**
   * Instantiates a new test documents repository.
   */
  public TestDocumentsRepository() {
    super("test_documents");
  }

  /** The Constant UPDATE_TEST_DOCUMENTS. */
  private static final String UPDATE_TEST_DOCUMENTS = "UPDATE test_documents "
      + " SET prescribed_id = ? WHERE ";

  /**
   * Update documents prescribed id.
   *
   * @param docIdsList the doc ids list
   * @param prescribedId the prescribed id
   * @return the int
   */
  public int updateDocumentsPrescribedId(List<Integer> docIdsList, Integer prescribedId) {

    StringBuilder query = new StringBuilder(UPDATE_TEST_DOCUMENTS);

    List<Object> params = new ArrayList<>();
    params.add(prescribedId);

    if (docIdsList != null && !docIdsList.isEmpty()) {
      String[] placeholdersArr = new String[docIdsList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append("doc_id IN ( ").append(StringUtils.arrayToCommaDelimitedString(placeholdersArr))
          .append(")");
      params.addAll(docIdsList);
    }
    return DatabaseHelper.update(query.toString(), params.toArray());
  }

  /** The Constant GET_ADDITIONAL_DOCS. */
  private static final String GET_ADDITIONAL_DOCS = "SELECT * FROM test_documents "
      + " WHERE prescribed_id = ? AND doc_id IS NOT NULL ";

  /**
   * Gets the additional docs.
   *
   * @param prescribedId the prescribed id
   * @return the additional docs
   */
  public List<BasicDynaBean> getAdditionalDocs(Integer prescribedId) {
    return DatabaseHelper.queryToDynaList(GET_ADDITIONAL_DOCS, new Object[] { prescribedId });
  }
  
  private static final String GET_SUPPORTING_TEST_DOC = "SELECT td.* FROM test_documents td"
      + " JOIN tests_prescribed tp ON (td.prescribed_id = tp.prescribed_id) WHERE td.doc_id=?";
  
  public BasicDynaBean getSupportingTestDoc(int docId) {
    return DatabaseHelper.queryToDynaBean(GET_SUPPORTING_TEST_DOC, new Object[] {docId});
  }

}
