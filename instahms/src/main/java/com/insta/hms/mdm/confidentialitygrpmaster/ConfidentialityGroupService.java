package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ConfidentialityGroupService.
 */
@Service
public class ConfidentialityGroupService extends MasterDetailsService {

  @LazyAutowired
  private UserService userService;

  ConfidentialityGroupRepository confidentialityGroupRepository;
  UserConfidentialityAssociationRepository userConfidentialityAssociationRepository;

  /**
   * Instantiates a new confidentiality group service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param userConfidentialityAssociationRepository
   *          the user confidentiality association repository
   */
  public ConfidentialityGroupService(ConfidentialityGroupRepository repository,
      ConfidentialityGroupValidator validator,
      UserConfidentialityAssociationRepository userConfidentialityAssociationRepository) {
    super(repository, validator,
        new MasterRepository[] { userConfidentialityAssociationRepository });
    confidentialityGroupRepository = repository;
    this.userConfidentialityAssociationRepository = userConfidentialityAssociationRepository;
  }

  /**
   * Gets the user confidentiality groups.
   *
   * @param username
   *          the username
   * @return the user confidentiality groups
   */
  public List<BasicDynaBean> getUserConfidentialityGroups(String username) {
    return confidentialityGroupRepository.getUserConfidentialityGroups(username);
  }

  /**
   * Gets the all users.
   *
   * @return the all users
   */
  public List getAllUsers() {
    return userService.getAllUsernames(true);
  }

  /**
   * Gets the confidentiality group users associated with a confidential group id.
   *
   * @param id
   *          the confidential group id
   * @return the confidentiality group users
   */
  public List getConfidentialityGroupUsers(int id) {
    return userConfidentialityAssociationRepository.listAll(null, "confidentiality_grp_id", id,
        null);
  }

  /**
   * Filter.
   *
   * @param parameters
   *          request parameters
   * @param filterText
   *          string to match
   * @return the paged list
   */
  public PagedList filterOnNameAndAbbreviation(Map<String, String[]> parameters,
      String filterText) {
    SearchQuery query = confidentialityGroupRepository.getSearchQuery();
    if (null == query) {
      return null;
    }
    String whereClause = " WHERE name ILIKE ? OR abbreviation ILIKE ? ";
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), whereClause, ConversionUtils.getListingParameter(parameters));
    String match = "%" + filterText + "%";
    qb.addInitValue(SearchQueryAssembler.STRING, match);
    qb.addInitValue(SearchQueryAssembler.STRING, match);
    qb.build();
    return qb.getMappedPagedList();
  }

  public List<BasicDynaBean> getUserDefinedConfidentialityGroups() {
    return confidentialityGroupRepository.getUserDefinedConfidentialityGroups();
  }

}
