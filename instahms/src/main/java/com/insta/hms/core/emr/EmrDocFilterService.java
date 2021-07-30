package com.insta.hms.core.emr;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emraccess.EMRAccessRuleProcessor;
import com.insta.hms.mdm.documenttypes.DocumentTypeRepository;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * EMR doc filter service class.
 */
@Service
public class EmrDocFilterService {

  /**
   * EMR doc access repository.
   */
  @LazyAutowired
  private EmrAccessRightRepository emrAccessRightRepository;

  /**
   * Document types Repository.
   */
  @LazyAutowired
  private DocumentTypeRepository documentTypeRepository;

  static Logger log = LoggerFactory.getLogger(EmrDocFilterService.class);

  /**
   * Take a list of documents and apply filter.
   * @param allDocs
   * @param list
   * @param req
   * @param userInRC
   * @return
   * @throws ParseException
   */
  public List<EMRDoc> applyFilter(List<EMRDoc>allDocs, List list, HttpServletRequest req, boolean userInRC)throws ParseException {

    if(list == null) {
      return allDocs;
    }
    if(list != null && list.isEmpty()) {
      return allDocs;
    }
    String indocType = null;
    String exdocType = null;
    String fromDate = null;
    String toDate = null;
    boolean accessRight = true;
    boolean docAccessRight = true;
    boolean roleAccessRight = true;

    HttpSession session = req.getSession();
    String userId = null;
    Integer centerId = null;
    Integer roleId = null;

    if(userInRC) {
      userId = RequestContext.getUserName();
      centerId = RequestContext.getCenterId();
      roleId = RequestContext.getRoleId();
    } else {
      userId = (String) session.getAttribute("userid");
      centerId = (Integer)session.getAttribute("centerId");
      roleId = (Integer)session.getAttribute("roleId");
    }

    String filterType= req.getParameter("filterType")!=null?(String)req.getParameter("filterType"):"visits";

    if (req.getParameter("indocType")!=null){
      indocType = req.getParameter("indocType");
      if (indocType.equals("*")) indocType = null;
    }
    if (req.getParameter("exdocType")!=null){
      exdocType = req.getParameter("exdocType");
      if (exdocType.equals("*")) exdocType = null;
    }
    if (req.getParameter("fromDate")!=null)
      fromDate = req.getParameter("fromDate");
    if (req.getParameter("toDate")!=null)
      toDate = req.getParameter("toDate");

    if (list != null && !list.isEmpty()) {
      Map userAttrs = null;
      boolean checkRoleAccess = true;
      boolean checkDocAccess = true;
      Map ruleMap = null;
      try {
        userAttrs = emrAccessRightRepository.getUserAttributesMap(userId, centerId, roleId);
        if (roleId == 1 || roleId == 2) {

        } else {
          List<BasicDynaBean> rules = emrAccessRightRepository.getRules(roleId);
          ruleMap = ConversionUtils.groupByColumn(rules, "rule_type");
          if (!ruleMap.containsKey("ROLE")) {
            checkRoleAccess = false;
          }
          if (!ruleMap.containsKey("DOC")) {
            checkDocAccess = false;
          }
        }
      } catch (SQLException e) {
        log.error("Error Processing EMR Access Rules : " + e.getMessage());
      }
      Iterator<EMRDoc> it = list.iterator();
      while (it.hasNext()) {
        EMRDoc p = it.next();
        // docAccessRight = EMRAccessRuleProcessor.docAccessCheck(p,userId,centerId,roleId);
        // roleAccessRight = EMRAccessRuleProcessor.roleAccessCheck(p,userId,centerId,roleId);
        // accessRight = (docAccessRight && roleAccessRight)?true:false;
        log.debug("Processing EMR access rules...");
        if ((ruleMap != null && !ruleMap.isEmpty()) && (checkRoleAccess || checkDocAccess)) {
          accessRight = new EMRAccessRuleProcessor().processRules(p, userId, centerId, roleId, userAttrs, checkRoleAccess, checkDocAccess);
        }
        log.debug("Done processing EMR access rules...");

        if(!accessRight){
          log.debug(" docType : " + p.getType() + " title : " + p.getTitle() + "accessRight : "+accessRight);
          continue;
        }else{
          if (filterType.equals("visits")) {
            if ((indocType!=null && exdocType!=null) || (indocType!=null && exdocType==null) || (indocType==null && exdocType!=null)) {
              if (p.getType().equals(indocType) && exdocType==null) {
                allDocs.add(p);
              }
              if (!p.getType().equals(exdocType) && indocType==null) {
                allDocs.add(p);
              }
            } else {
              allDocs.add(p);
            }
          } else {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            if ((!fromDate.equals("") && !toDate.equals("")) || (fromDate.equals("") && !toDate.equals("")) || (!fromDate.equals("") && toDate.equals(""))) {
              if (p.getDate() == null) {
                // skip the document. when the document date is null, but searching within a date range.
                continue;
              }
              if (!toDate.equals("") && fromDate.equals("")) {
                java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
                int j = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valtoDate);
                if (j <= 0) {
                  allDocs.add(p);
                }
              }
              if (!fromDate.equals("") && toDate.equals("")) {
                java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
                int k = p.getDate().compareTo(valfromDate);
                if (k >= 0) {
                  allDocs.add(p);
                }
              }
              if (!fromDate.equals("") && !toDate.equals("") ) {
                java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
                java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
                if( p.getDate() == null) {
                  allDocs.add(p);
                }else{
                  int j = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valtoDate);
                  int k = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valfromDate);
                  if ((j <= 0) && (k >= 0)) {
                    allDocs.add(p);
                  }
                }
              }
            } else {
              allDocs.add(p);
            }
          }
        }
      }
    }

    return allDocs;
  }

  /**
   * This is a Wrapper that calls existing applyFilter which applies on EMRDocs. This accepts list type docs.
   * @param list list of maps
   * @param request request object
   * @return returns list
   * @throws ParseException throws parse exception
   */
  public List<Map> applyFilter(List<Map> list, HttpServletRequest request) throws ParseException {
    List<EMRDoc> doc = new ArrayList<>();
    List<EMRDoc> convertedDocList = convertListToDoc(list);
    List<EMRDoc> docs = applyFilter(doc, convertedDocList, request,false);
    return convertDocToList(docs, list);
  }

  /**
   * Apply category filter on documents.
   * @param list list of maps
   * @param catgeory document category
   * @return returns list
   */
  public List<Map> applyCategoryFilter(List<Map> list, String catgeory) {
    List<BasicDynaBean> documentsByCategory = documentTypeRepository.getDocTypesByCategory(catgeory, "N");
    List<Map> documentsFilteredByCat = new ArrayList<>();
    for (Map m : list) {
      for(BasicDynaBean basicDynaBean : documentsByCategory) {
        if(m.get("doc_type").equals(basicDynaBean.get("doc_type_id"))) {
          documentsFilteredByCat.add(m);
        }
      }
    }
    return documentsFilteredByCat;
  }

  /**
   * Apply filter on document types.
   * @param list list of map
   * @param visitId visit id
   * @param request request object
   * @return returns list
   * @throws ParseException throws parse exception
   */
  public List<BasicDynaBean> applyFilterOnDocTypes(List<BasicDynaBean> list, String visitId, HttpServletRequest request)
      throws ParseException {
    List<EMRDoc> doc = new ArrayList<>();
    List<EMRDoc> convertedDocTypesList = convertListToDocTypes(list, visitId);
    List<EMRDoc> docs  = applyFilter(doc, convertedDocTypesList, request, false);
    return convertDocTypesToList(docs, list);
  }

  /**
   * convert list to doc types.
   * @param list list of map
   * @param visitId visit id
   * @return returns list of EMRDocs
   */
  private List<EMRDoc> convertListToDocTypes(List<BasicDynaBean> list, String visitId) {
    List<EMRDoc> emrDocList = new ArrayList<>(list.size());
    String userId = RequestContext.getUserName();
    for (BasicDynaBean m : list) {
      EMRDoc emrDoc = new EMRDoc();
      emrDoc.setType((String) m.get("doc_type_id"));
      emrDoc.setUserName(userId);
      emrDoc.setVisitid(visitId);
      String templateName  = m.getMap().containsKey("template_name") ? String.valueOf(m.get("template_name")) : null;
      if(templateName !=null ) {
        emrDoc.setTitle((String) m.get("template_name"));
      } else {
        emrDoc.setTitle((String) m.get("doc_type_name"));
      }
      String templateId = m.getMap().containsKey("template_id") ? String.valueOf(m.get("template_id")) : null;
      emrDoc.setDocid(templateId);
      emrDocList.add(emrDoc);
    }
    return emrDocList;
  }

  /**
   * Convert Doc types to list.
   * @param docs list of EMRDocs
   * @param list list of doc types
   * @return returns list of filtered doc types
   */
  private List convertDocTypesToList(List<EMRDoc> docs, List<BasicDynaBean> list) {
    List<BasicDynaBean> convertedToList = new ArrayList<>();
    for(EMRDoc doc : docs) {
      for(BasicDynaBean basicDynaBean : list) {
        if(basicDynaBean.getMap().containsKey("template_name")) {
          if (String.valueOf(basicDynaBean.get("doc_type_id")).equals(doc.getType())
              && String.valueOf(basicDynaBean.get("template_name")).equals(doc.getTitle())
              && String.valueOf(basicDynaBean.get("template_id")).equals(doc.getDocid())) {
            convertedToList.add(basicDynaBean);
          }
        } else {
          if (String.valueOf(basicDynaBean.get("doc_type_id")).equals(doc.getType())
              && String.valueOf(basicDynaBean.get("doc_type_name")).equals(doc.getTitle())) {
            convertedToList.add(basicDynaBean);
          }
        }
      }
    }
    return convertedToList;
  }

  /**
   * Converts EMRDoc to Map type.
   * @param docs takes EMRDoc types list
   * @param list list of documents
   * @return returns list of filtered documents
   */
  private List convertDocToList(List<EMRDoc> docs, List<Map> list) {
    List<Map> convertedToList = new ArrayList<>();
    for(EMRDoc doc : docs) {
      for(Map map : list) {
        if(String.valueOf(map.get("doc_id")).equals(doc.getDocid())) {
          convertedToList.add(map);
        }
      }
    }
    return convertedToList;
  }

  /**
   * Converts list to doc.
   * @param list list of documents
   * @return returns list of EMRDoc doc
   */
  private List<EMRDoc> convertListToDoc(List<Map> list) {
    List<EMRDoc> emrDocList = new ArrayList<>(list.size());
    for (Map m : list) {
      EMRDoc emrDoc = new EMRDoc();
      emrDoc.setDocid(String.valueOf(m.get("doc_id")));
      emrDoc.setType((String) m.get("doc_type"));
      emrDoc.setUserName((String) m.get("username"));
      emrDoc.setVisitid((String) m.get("patient_id"));
      emrDocList.add(emrDoc);
    }
    return emrDocList;
  }

  /**
   * Get all doc types without filter.
   * @return returns list of BasicDynaBeans
   */
  public List<BasicDynaBean> getAllDocumentTypes(){
    return documentTypeRepository.listAll();
  }
}
