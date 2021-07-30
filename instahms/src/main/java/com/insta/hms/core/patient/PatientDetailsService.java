package com.insta.hms.core.patient;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

@Service
public class PatientDetailsService {

	@LazyAutowired
	private PatientDetailsRepository patdetailsRepository;

	public BasicDynaBean getPatientDetailsDisplayBean(String mr_no) {
		BasicDynaBean patientBean = patdetailsRepository.getPatientDetailsDisplayBean(mr_no);
		if (patientBean == null)
			return null;
		DateUtil.checkAndSetAgeComponents(patientBean);
		return patientBean;
	}

	public BasicDynaBean getBean() {
		return patdetailsRepository.getBean();
	}

	public int update(BasicDynaBean bean, Map<String, Object> keys) {
		return patdetailsRepository.update(bean, keys);
	}
	
	public int updateContactPrefLangCode(String mrNo, String contactPrefLangCode){
		return patdetailsRepository.updateContactPrefLangCode(mrNo, contactPrefLangCode);
	}
	
	public int insert(BasicDynaBean patDetailsBean) {
		return patdetailsRepository.insert(patDetailsBean);
	}

	public BasicDynaBean getPatientMailId(String mr_no) {
		return patdetailsRepository.getPatientMailId(mr_no);
	}
	
	/**
	 * This method gets basic patient details
	 * @param mrNo
	 * @param appointmentId
	 * @return basic patient details
	 */
	public BasicDynaBean getBasicDetails(String mrNo, String appointmentId) {
		return patdetailsRepository.getBasicDetails(mrNo, appointmentId);
	}
	
	/**
	 * This method get advanced patient details
	 * @param mrNo
	 * @return advanced patient details
	 */
	public BasicDynaBean getAdvancedDetails(String mrNo, String visitId) {
		return patdetailsRepository.getAdvancedDetails(mrNo, visitId);
	}
	
	/**
	 * This method is to get mr_no from oldMrNo.
	 * @param oldMrNo
	 * @return mr_no
	 */
	public BasicDynaBean getMrNoWithOldMrNoBean(String oldMrNo) {
		return patdetailsRepository.getMrNoWithOldMrNoBean(oldMrNo);
	}

	/**
	 * This method uploads photo
	 * @param mrNo
	 * @param patientPhoto
	 * @return
	 * @throws IOException 
	 */
	@Transactional
	public Boolean uploadPhoto(String mrNo, byte[] patientPhoto) throws IOException {
		Boolean ret = true;
        String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(patientPhoto).toString().split("/")[1];
        if (!(contentType.equals("png") || contentType.equals("jpeg"))) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("params", "exception.illegal.document.type");
          ValidationException ex = new ValidationException(errorMap);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("uploadPhoto", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
        BufferedImage image = toBufferedImage(patientPhoto);
        int type = (image.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
		ret = ret && patdetailsRepository.uploadPhoto(mrNo, 
				patientPhoto, 
				getScaledInstance(image, 36, 36,RenderingHints.VALUE_INTERPOLATION_BILINEAR, true, contentType, type), 
				getScaledInstance(image, 46, 46,RenderingHints.VALUE_INTERPOLATION_BILINEAR, true, contentType, type),
				getScaledInstance(image, 66, 66,RenderingHints.VALUE_INTERPOLATION_BILINEAR, true, contentType, type),
				getScaledInstance(image, 84, 84,RenderingHints.VALUE_INTERPOLATION_BILINEAR, true, contentType, type));
		
		return ret;
	}
	
	/**
	 * This method gets patient photo
	 * @param mrNo, columnName
	 * @return
	 */
	public BasicDynaBean getPhoto(String mrNo, String columnName) {
		return patdetailsRepository.getPhoto(mrNo, columnName);
	}
	
	private static BufferedImage toBufferedImage(byte[] photo) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(photo);
        BufferedImage img = ImageIO.read(inputStream);
        return img;		
	}
	/**
	 * This method scales the image to a specific size
	 * @param patientPhoto
	 * @param targetWidth
	 * @param targetHeight
	 * @param hint
	 * @param higherQuality
	 * @return
	 * @throws IOException
	 */
	private static byte[] getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
            boolean higherQuality, String contentType, int type) throws IOException {
        BufferedImage ret = (BufferedImage) img;				
        if (ret.getHeight() < targetHeight || ret.getWidth() < targetWidth) {
            higherQuality = false;
        }
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(ret,contentType, bo);
        return bo.toByteArray();
    }
	
	/**
	 * 
	 * @param mrNo
	 * @return
	 */
	public Boolean isMrNumberValid(String mrNumber) {
    	return patdetailsRepository.isMrNumberValid(mrNumber);
    }

	public BasicDynaBean getBabyDOBAndSalutationDetails(String mrNo) {
		return patdetailsRepository.getBabyDOBAndSalutation(mrNo);
	}
	
	public String getPatientGender(String mrNo) {
		return patdetailsRepository.getPatientGender(mrNo);
	}
	
	public boolean isUniqueGovtID(String govtIdentifier, String mrNo) {
		List<String> columns = new ArrayList<String>();
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("government_identifier", govtIdentifier);
		
		List<BasicDynaBean> govtIdentifierBean = patdetailsRepository.listAll(columns, filterMap, null);
		if(govtIdentifierBean.size() > 0) {
			String mr_no = (String) govtIdentifierBean.get(0).get("mr_no");
			if(mr_no.equals(mrNo)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public BasicDynaBean getPreviousRegDateChargeAccepted(String mrno) {
		return patdetailsRepository.getPreviousRegDateChargeAccepted(new Object[]{mrno});
	}

	public List<BasicDynaBean> listExistingPatientDetails(Map<String, Object> params)
			throws ParseException {
		return patdetailsRepository.listExistingPatientDetails(params);
	}

	public BasicDynaBean findByKey(String mrno){
		return patdetailsRepository.findByKey("mr_no", mrno);
	}

  public BasicDynaBean getPatientDetailsForVisit(String visitId) {
    return patdetailsRepository.getPatientDetailsForVisit(visitId);
  }

	public List<Map<String,String>> getPreferredLanguages(String userLangCode) {
		return  patdetailsRepository.getPreferredLanguages(userLangCode);
	}
	
	public List<BasicDynaBean> getBabyOrMotherDetails(String patientId) {
	  return patdetailsRepository.getBabyOrMotherDetails(patientId);
	}
	
	public Integer checkMrNoConfidentiality(List<String> mrno, List<Integer> userGroups, String userName) {
	  return patdetailsRepository.getCountPatientForMrNoAndUserGroup(mrno, userGroups, userName);
	}

	public List<String> getAssociatedMrNoForVisitId(List<String> visitId) {
	  return patdetailsRepository.getMrNoForVisitId(visitId);
	}

  public Boolean isBreakTheGlassAllowed(String mrno) {
    return patdetailsRepository.isBreakTheGlassAllowed(mrno);
  }

}
