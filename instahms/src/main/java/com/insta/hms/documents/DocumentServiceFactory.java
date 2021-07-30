package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Component;

/**
 * A factory for creating DocumentService objects.
 */
@Component
public class DocumentServiceFactory {

  /** The generic documents sevice. */
  @LazyAutowired
  private GenericDocumentsService genericDocumentsSevice;

  /**
   * Gets the single instance of DocumentServiceFactory.
   *
   * @param documentCategory the document category
   * @param specialized      the specialized
   * @return single instance of DocumentServiceFactory
   */
  public DocumentsService getInstance(String documentCategory, String specialized) {
    if (specialized.equals("Y")) {
      DocumentsCategory category = null;
      try {
        category = DocumentsCategory.valueOf(documentCategory);
      } catch (Exception exc) {
        exc.printStackTrace();

        switch (category) {
          default:
            return genericDocumentsSevice;
        }
      }
    }
    return genericDocumentsSevice;
  }
}
