/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.UrlUtil;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class PBMPrescriptionHelper {

    /*
     * TODO - Kindly change usage of the following scenarios as you migrate
     * or work on the specific scenarios to use UrlUtil's build url as used in
     * the case when type is account group.
     */
	public String urlString(String path, String type, String id, String name) {

		HttpSession session = RequestContext.getSession();
		java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
		java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");

		String url = "";
		path = path +"/";

		if (path == null || type == null)
			return url;
		if (type.equals("diagnosis")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("update_mrd").equals("A")) {
				url = (String)actionUrlMap.get("update_mrd");
				url = "<b><a target='_blank' href='"+path + url+"?_method=getMRDUpdateScreen&patient_id="+id+"'>"+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("pbmprescription")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("pbm_presc").equals("A")) {
				url = (String)actionUrlMap.get("pbm_presc");
				url = "<b><a target='_blank' href='"+path+url+"?_method=getPBMPrescription&pbm_presc_id="+id+"'>PBM ID "+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("preauth")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("preauth_presc").equals("A")) {
				url = (String)actionUrlMap.get("preauth_presc");
				url = "<b><a target='_blank' href='"+path+url+"?_method=getEAuthPrescription&preauth_presc_id="+id+"'>Prior Auth ID "+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("attachment")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("pbm_presc").equals("A")) {
				url = (String)actionUrlMap.get("pbm_presc");
				url = "<b><a target='_blank' href='"+path+url+"?_method=addOrEditAttachment&pbm_presc_id="+id+"'>PBM ID "+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("doctor")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_doctors_detail").equals("A")) {
				url = (String)actionUrlMap.get("mas_doctors_detail");
				url = "<b><a target='_blank' href='"+path+url+"?_method=getDoctorDetailsScreen&mode=update&doctor_id="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}

		}else if (type.equals("referral")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_ref_doctors").equals("A")) {
				url = (String)actionUrlMap.get("mas_ref_doctors");
				url = "<b><a target='_blank' href='"+path+url+"?_method=show&referal_no="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}

		}else if (type.equals("patient")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("edit_visit_details").equals("A")) {
				url = (String)actionUrlMap.get("edit_visit_details");
				url = "<b><a target='_blank' href='"+path+url+"?_method=getPatientVisitDetails&ps_status=all&patient_id="+id+"'>"+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("account-group")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("accounting_group_master").equals("A")) {
				url = UrlUtil.buildURL("accounting_group_master", UrlUtil.SHOW_URL_VALUE, "account_group_id="+id, null, id);
				url = "<b><a target='_blank' href='"+url+"'>"+name+" Group</a></b>";
			}else {
				url = "<b>"+name+" Group</b>";
			}

		}else if (type.equals("center-name")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_centers").equals("A")) {
				url = UrlUtil.buildURL("mas_centers", UrlUtil.SHOW_URL_VALUE, "center_id="+id, null, id);
				url = "<b><a target='_blank' href='"+url+"'>"+name+" Center</a></b>";
			}else {
				url = "<b>"+name+" Center</b>";
			}

		}else if (type.equals("pre-registration")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("reg_general").equals("A")) {
				url = (String)actionUrlMap.get("reg_general");
				url = "<b><a target='_blank' href='"+path+url+"?_method=show&regType=regd&mr_no="+id+"&mrno="+id+"'>"+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}

		}else if (type.equals("drug")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_medicines").equals("A")) {
				url = (String)actionUrlMap.get("mas_medicines");
				url = "<b><a target='_blank' href='"+path+url+"?_method=editItemCode&medicine_id="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}

		}else if (type.equals("insurance")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("change_visit_tpa").equals("A")) {
				url = (String)actionUrlMap.get("change_visit_tpa");
				url = "<b><a target='_blank' href='"+path + url+"?_method=changeTpa&visitId="+id+"'>"+id+"</a></b>";
			}else {
				url = "<b>"+id+"</b>";
			}
		}else if (type.equals("sponsor")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_ins_tpas").equals("A")) {
				url = (String)actionUrlMap.get("mas_ins_tpas");
				url = "<b><a target='_blank' href='"+path+url+"?_method=show&tpa_id="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}

		}else if (type.equals("company")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_insurance_comp").equals("A")) {
				url = (String)actionUrlMap.get("mas_insurance_comp");
				url = "<b><a target='_blank' href='"+path+url+"?_method=show&insurance_co_id="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}

		}else if (type.equals("pbmobservation")) {

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("mas_pbm_observations").equals("A")) {
				url = (String)actionUrlMap.get("mas_pbm_observations");
				url = "<b><a target='_blank' href='"+path+url+"?_method=show&id="+id+"'>"+name+"</a></b>";
			}else {
				url = "<b>"+name+"</b>";
			}
		}
		return url;
	}

	public String convertToBase64Binary(InputStream in) throws IOException, UnsupportedEncodingException, Exception {
		String encodedStr = null;
		try {
			byte[] bytes = new byte[4096];

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			while(true) {
	            int r = in.read(bytes);
	            if(r <= 0) {
	            	break;
	            }
	            buffer.write(bytes, 0, r);
			}
	        byte[] filebytes = buffer.toByteArray();

	        //all chars in encoded are guaranteed to be 7-bit ASCII
	        byte[] encoded = Base64.encodeBase64(filebytes);
	        encodedStr = new String(encoded, "ASCII");

		}catch (Exception e) {
			throw e;
		}
        return encodedStr;
	}
}
