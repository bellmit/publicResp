package com.insta.hms.batchjob;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.core.inventory.stock.adjustment.StoresStockAdjustmentService;
import com.insta.hms.core.inventory.stock.transfer.StoresStockTransferService;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.inventory.supplierreturn.debit.SupplierReturnsService;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.mdm.integration.CsvImportable;
import com.insta.hms.mdm.integration.controltype.ControlTypeIntegrationService;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;
import com.insta.hms.mdm.integration.itemforms.ItemFormIntegrationService;
import com.insta.hms.mdm.integration.iteminsurancecategory.ItemInsuranceCategoriesIntegrationService;
import com.insta.hms.mdm.integration.packageuom.PackageUomIntegrationService;
import com.insta.hms.mdm.integration.servicegroup.ServiceGroupIntegrationService;
import com.insta.hms.mdm.integration.servicesubgroup.ServiceSubgroupIntegrationService;
import com.insta.hms.mdm.integration.storecategory.StoreCategoryIntegrationService;
import com.insta.hms.mdm.integration.stores.ManufacturerIntegrationService;
import com.insta.hms.mdm.integration.stores.genericnames.GenericNamesIntegrationService;
import com.insta.hms.mdm.integration.taxgroups.TaxGroupsIntegrationService;
import com.insta.hms.mdm.integration.taxsubgroups.TaxSubGroupsIntegrationService;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.http.conn.UnsupportedSchemeException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CsvImportJob extends GenericJob {

  @LazyAutowired
  private ManufacturerIntegrationService manufacturerIntegrationService;

  @LazyAutowired
  private GenericNamesIntegrationService genericNamesIntegrationService;

  @LazyAutowired
  private ServiceGroupIntegrationService serviceGroupIntegrationService;

  @LazyAutowired
  private ServiceSubgroupIntegrationService serviceSubgroupIntegrationService;

  @LazyAutowired
  private ControlTypeIntegrationService controlTypeIntegrationService;

  @LazyAutowired
  private ItemFormIntegrationService itemFormIntegrationService;

  @LazyAutowired
  private ItemInsuranceCategoriesIntegrationService itemInsuranceCategoriesIntegrationService;

  @LazyAutowired
  private TaxGroupsIntegrationService taxGroupsIntegrationService;

  @LazyAutowired
  private TaxSubGroupsIntegrationService taxSubGroupsIntegrationService;

  @LazyAutowired
  private StoreCategoryIntegrationService storeCategoryIntegrationService;

  @LazyAutowired
  private StoreItemDetailIntegrationService storeItemDetailIntegrationService;

  @LazyAutowired
  private PackageUomIntegrationService packageUomIntegrationService;

  @LazyAutowired
  private StockService stockEntryIntegrationService;
  
  @LazyAutowired
  private SupplierReturnsService supplierReturnsIntegrationService;
  
  @LazyAutowired
  private StoresStockAdjustmentService storesStockAdjustmentService;
  
  @LazyAutowired
  private StoresStockTransferService storesStockTransferService;

  private final Logger logger = LoggerFactory.getLogger(CsvImportJob.class);
  private static final String STATUS_COLUMN = "_import_status";
  private static final String DESC_COLUMN = "_import_desc";
  private static final String[] SUPPORTED_SCHEMES = new String[] { "sftp", "file" };

  private static final String[] IMPORT_ORDER = new String[] { "uom_master", "item_category_master",
      "manufacturer_master", "generic_name_master", "control_type_master", "item_form_master",
      "item_insurance_categories", "item_tax_groups", "item_tax_subgroups", "item_master",
      "stock_entry","store_debit_note", "stock_transfer", "stock_adjustment"};

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    StandardFileSystemManager fileSystemManager = null;
    try {
      if (EnvironmentUtil.getCsvImportUri() == null
          || EnvironmentUtil.getCsvImportUri().trim().isEmpty()) {
        logger.error(EnvironmentUtil.URI_IMPORT_CSVS + " not defined. Exiting job");
        return;

      }

      URI csvImportUri = new URI(EnvironmentUtil.getCsvImportUri() + "/" + getSchema());
      String uriScheme = csvImportUri.getScheme();

      if (!ArrayUtils.contains(SUPPORTED_SCHEMES, uriScheme)) {
        throw new UnsupportedSchemeException("Scheme " + uriScheme + "not supported.");
      }

      fileSystemManager = new StandardFileSystemManager();
      fileSystemManager.init();
      FileObject csvImportDirectory = fileSystemManager.resolveFile(csvImportUri);

      if (!csvImportDirectory.exists() || !csvImportDirectory.isFolder()) {
        logger.error("CSV import directory " + csvImportDirectory.getPublicURIString()
            + " does not exist. Exiting job.");
        return;
      }

      for (String entityToImport : IMPORT_ORDER) {
        URI inputDirectoryPath = makePath(csvImportUri, entityToImport, "in");
        URI outputDirectoryPath = makePath(csvImportUri, entityToImport, "out");
        URI errorDirectoryPath = makePath(csvImportUri, entityToImport, "error");
        FileObject inputDirectory = fileSystemManager.resolveFile(inputDirectoryPath);
        FileObject outputDirectory = fileSystemManager.resolveFile(outputDirectoryPath);
        FileObject errorDirectory = fileSystemManager.resolveFile(errorDirectoryPath);

        if (!(inputDirectory.exists() && inputDirectory.isFolder())) {
          logger.info("Path not found " + inputDirectory.getPublicURIString()
              + ". Skipping to next entity.");
          continue;
        }
        if (!(outputDirectory.exists() && outputDirectory.isFolder())) {
          logger.info("Path not found " + outputDirectory.getPublicURIString());
          outputDirectory.createFolder();
          logger.info("Created " + outputDirectory.getPublicURIString());
        }
        if (!(errorDirectory.exists() && errorDirectory.isFolder())) {
          logger.info("Path not found " + errorDirectory.getPublicURIString());
          errorDirectory.createFolder();
          logger.info("Created " + errorDirectory.getPublicURIString());
        }

        CsvImportable entityService = getService(entityToImport);

        if (entityService == null) {
          logger.error("Skipping unsupported entity " + entityToImport);
          continue;
        }

        for (FileObject file : inputDirectory.getChildren()) {
          if (file.isFile() && file.getName().getBaseName()
              .matches(entityToImport + "_\\d{4}-\\d{2}-\\d{2}T\\d{6}\\.csv")) {
            logger.info("importing " + file.getPublicURIString());
            InputStreamReader readerToParse = null;
            InputStreamReader reader = null;
            BufferedWriter outWriter = null;
            BufferedWriter errorWriter = null;
            try {
              readerToParse = new InputStreamReader(file.getContent().getInputStream());

              Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();

              entityService.importCsv(readerToParse, feedback);

              Map<Object, List<Object>> warnings = feedback.get("warnings");

              reader = new InputStreamReader(file.getContent().getInputStream());

              outWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
                  .resolveFile(makePath(outputDirectoryPath, file.getName().getBaseName()))
                  .getContent().getOutputStream()));
              CSVReader csvReader = new CSVReader(reader);
              CSVWriter csvOutWriter = new CSVWriter(outWriter);
              CSVWriter csvErrorWriter = null;

              if (!warnings.isEmpty()
                  && !(warnings.keySet().size() == 1 && warnings.containsKey(0))) {
                errorWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
                    .resolveFile(makePath(errorDirectoryPath, file.getName().getBaseName()))
                    .getContent().getOutputStream()));
                csvErrorWriter = new CSVWriter(errorWriter);
              }

              String[] row;

              for (Integer rowNum = 1; (row = csvReader.readNext()) != null; rowNum++) {
                List<String> rowItems = new ArrayList<>(Arrays.asList(row));
                if (rowNum == 1) {
                  rowItems.add(STATUS_COLUMN);
                  rowItems.add(DESC_COLUMN);
                  if (!warnings.isEmpty()
                      && !(warnings.keySet().size() == 1 && warnings.containsKey(0))) {
                    csvErrorWriter.writeNext(rowItems.toArray(new String[0]));
                  }
                } else {
                  if (warnings.keySet().contains(rowNum)) {
                    rowItems.add("false");
                    rowItems.add(StringUtils.join(warnings.get(rowNum), "\n"));
                    csvErrorWriter.writeNext(rowItems.toArray(new String[0]));
                  } else {
                    rowItems.add("true");
                    rowItems.add("Imported");
                  }
                }
                csvOutWriter.writeNext(rowItems.toArray(new String[0]));

              }

              csvOutWriter.flush();
              csvOutWriter.close();
              reader.close();
              if (csvErrorWriter != null) {
                csvErrorWriter.flush();
                csvErrorWriter.close();
              }

              if (file.delete()) {
                logger.info("Deleted " + file.getPublicURIString());
              } else {
                logger.error("Error deleting file " + file.getPublicURIString());
              }

            } catch (IOException ex) {
              logger.error("", ex);
            } finally {
              if (reader != null) {
                try {
                  reader.close();

                } catch (IOException ex) {
                  logger.error("Failed to close reader", ex);
                }
              }
              if (outWriter != null) {
                try {
                  outWriter.close();
                } catch (IOException ex) {
                  logger.error("Failed to close outWriter", ex);
                }
              }
              if (errorWriter != null) {
                try {
                  errorWriter.close();
                } catch (IOException ex) {
                  logger.error("Failed to close errorWriter", ex);
                }
              }

            }

          } else {
            logger.info("Skipping " + file.getPublicURIString());
          }

        }
      }
    } catch (URISyntaxException | UnsupportedSchemeException | FileSystemException ex) {
      logger.error("Something went wrong", ex);
    } finally {
      if (fileSystemManager != null) {
        fileSystemManager.close();
      }
    }
  }

  private CsvImportable getService(String entity) {
    switch (entity) {
      case "manufacturer_master":
        return manufacturerIntegrationService;
      case "generic_name_master":
        return genericNamesIntegrationService;
      case "service_groups_master":
        return serviceGroupIntegrationService;
      case "service_subgroups_master":
        return serviceSubgroupIntegrationService;
      case "control_type_master":
        return controlTypeIntegrationService;
      case "item_form_master":
        return itemFormIntegrationService;
      case "item_insurance_categories":
        return itemInsuranceCategoriesIntegrationService;
      case "item_tax_groups":
        return taxGroupsIntegrationService;
      case "item_tax_subgroups":
        return taxSubGroupsIntegrationService;
      case "item_category_master":
        return storeCategoryIntegrationService;
      case "item_master":
        return storeItemDetailIntegrationService;
      case "uom_master":
        return packageUomIntegrationService;
      case "stock_entry":
        return stockEntryIntegrationService;
      case "store_debit_note":
        return supplierReturnsIntegrationService;
      case "stock_adjustment" :
        return storesStockAdjustmentService;
      case "stock_transfer" :
        return storesStockTransferService;
      default:
        return null;
    }
  }

  private URI makePath(URI uri, String... pathSegments) throws URISyntaxException {
    StringBuilder uriStringBuilder = new StringBuilder(uri.toString());
    for (String pathSegment : pathSegments) {
      uriStringBuilder.append("/").append(pathSegment);
    }
    return new URI(uriStringBuilder.toString());
  }
}
