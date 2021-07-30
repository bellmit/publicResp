package com.insta.hms.batchjob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@SuppressFBWarnings(value = "HARD_CODE_KEY", justification = "To be refactored later")
public class SessionCleanupJob extends GenericJob {

  private String params;
  private String userName;
  private static String[] PAYMENT_TYPE = new String[] { "R", "A", "L" };

  @LazyAutowired
  public RedisTemplate<String, Object> redisTemplate;

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  private CacheManager cacheManager;

  public SessionCleanupJob() {
    this.cacheManager = CacheManager.getInstance();
  }

  private String bytesToHex(byte[] bytes) {
    StringBuffer result = new StringBuffer();
    for (byte byt : bytes) {
      result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString().toLowerCase();
  }

  private Map<String, Object> readLicenseData(String schema) {
    String salt = "UWlf7bJkfgZ0jrCbSAYBHDSJY6yRCpdDHGFUmvbVl1SjJ9rxO35VmLEVOv6nlQcs";
    try {
      Mac sha256HMac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(), "HmacSHA256");
      sha256HMac.init(secretKey);
      byte[] hash = sha256HMac.doFinal(schema.getBytes());
      String hexDigest = bytesToHex(hash);
      String licenseFilePath = "/usr/local/lib/" + hexDigest.substring(0, 9) + "/data.so";
      File licenseFile = new File(licenseFilePath);
      int licenseSize = 10;
      if (licenseFile.exists() && !licenseFile.isDirectory()
          && licenseSize == (int) licenseFile.length()) {
        byte[] licenseBytes = new byte[licenseSize];
        FileInputStream fis = new FileInputStream(licenseFile);
        fis.read(licenseBytes); // read file into bytes[]
        fis.close();
        Date dueDate = null;
        Date noticeDate = null;
        Date expiryDate = null;
        Date extensionDate = null;
        try {
          dueDate = DateUtil
              .parseDate(String.valueOf(licenseBytes[4]) + "-" + String.valueOf(licenseBytes[3])
                  + "-" + String.valueOf(licenseBytes[1]) + String.valueOf(licenseBytes[2]));
          long duration = 30 * 24 * 60 * 60 * 1000L;
          expiryDate = new Date(dueDate.getTime() + duration);
          noticeDate = new Date(dueDate.getTime() - duration);
          if (licenseBytes[5] != 0) {
            extensionDate = DateUtil
                .parseDate(String.valueOf(licenseBytes[8]) + "-" + String.valueOf(licenseBytes[7])
                    + "-" + String.valueOf(licenseBytes[5]) + String.valueOf(licenseBytes[6]));
            expiryDate = extensionDate;
          }
        } catch (ParseException exception) {
          return null;
        }
        if (dueDate == null) {
          return null;
        }
        Map<String, Object> licenseData = new HashMap<String, Object>();
        licenseData.put("type", PAYMENT_TYPE[licenseBytes[0]]);
        licenseData.put("due", dueDate);
        licenseData.put("expiry", expiryDate);
        licenseData.put("extension", extensionDate);
        licenseData.put("block", licenseBytes[9] != 0);
        Date now = new Date();
        String status = "";
        if (now.after(expiryDate)) {
          status = "expired";
        } else if (now.after(dueDate)) {
          status = "overdue";
        } else if (now.after(noticeDate)) {
          status = "due";
        }
        licenseData.put("status", status);
        licenseData.put("active", !status.equals("expired"));
        return licenseData;
      }
    } catch (NoSuchAlgorithmException exception) {
      return null;
    } catch (FileNotFoundException exception) {
      return null;
    } catch (IOException exception) {
      return null;
    } catch (InvalidKeyException exception) {
      return null;
    }
    return null;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    Cache instaLicenseCache = cacheManager.getCache("insta_license");
    if (instaLicenseCache == null) {
      instaLicenseCache = new Cache("insta_license", 1000, MemoryStoreEvictionPolicy.LRU, false,
          "/tmp", true, 0, 0, false, 0, null);
      cacheManager.addCache(instaLicenseCache);
    }
    RequestContext.setConnectionDetails(new String[] { null, "", getSchema(), "", "", "" });
    String schemaName = getSchema();
    Map<String, Object> licenseData = readLicenseData(schemaName);
    String schemaKey = "license_" + schemaName;
    if (licenseData != null) {
      instaLicenseCache.put(new Element(schemaKey, licenseData));
      redisTemplate.opsForHash().putAll(schemaKey, licenseData);
    } else {
      instaLicenseCache.remove(schemaKey);
      redisTemplate.delete(schemaKey);
    }
  }

}
