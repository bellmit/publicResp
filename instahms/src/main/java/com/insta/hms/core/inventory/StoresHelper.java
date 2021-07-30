package com.insta.hms.core.inventory;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds different types of helper methods for stores.
 * 
 * @author irshadmohammed
 *
 */
public class StoresHelper {

	public void getTaxDetails(Map<String, Object> taxAmtsMap, List subGroupCodes, Map<String, Object> taxInfoMap) {
		Iterator<BasicDynaBean> subGroupCodesIterator = subGroupCodes.iterator();
		while(subGroupCodesIterator.hasNext()) {
			BasicDynaBean subGroupCodesBean = subGroupCodesIterator.next();
			String subGroupCode = (String)subGroupCodesBean.get("subgroup_code");
			String subGroupName = (String)subGroupCodesBean.get("item_subgroup_name");
			int subGroupId = (Integer)subGroupCodesBean.get("item_subgroup_id");
			if(taxAmtsMap.get(subGroupCode) != null) {
				Map processedTaxDetailsMap = new HashMap();
				processedTaxDetailsMap.putAll((Map<String, String>)taxAmtsMap.get(subGroupCode));
				processedTaxDetailsMap.put("subgroup_id", subGroupId);
				taxInfoMap.put(subGroupName, processedTaxDetailsMap);
			}
		}
	}
	
	public void setTaxDetails(Map<String, String[]> param, int parentIndex, int childIndex, BasicDynaBean taxBean) {
		Iterator<Entry<String, String[]>> paramIterator = param.entrySet().iterator();
		while(paramIterator.hasNext()) {
			Entry<String, String[]> paramEntry = paramIterator.next();
			if(paramEntry.getKey().equals("taxrate"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxRate = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxRate.compareTo(BigDecimal.ZERO) > 0)
					taxBean.set("tax_rate", taxRate);
				}
				
			}
			if(paramEntry.getKey().equals("taxamount"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxAmt = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxAmt.compareTo(BigDecimal.ZERO) > 0)
					taxBean.set("tax_amt", taxAmt);
				}
			}
			if(paramEntry.getKey().equals("taxsubgroupid"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					int itemSubgroupId = Integer.valueOf(paramEntry.getValue()[parentIndex]);
					if(itemSubgroupId != 0 && itemSubgroupId != -1)
						taxBean.set("item_subgroup_id", itemSubgroupId);
				}
			}
		}
	}
	
  public int getOldTaxSubgroup(Map<String, String[]> param, int parentIndex, int childIndex) {
    Iterator<Entry<String, String[]>> paramIterator = param.entrySet().iterator();
    while (paramIterator.hasNext()) {
      Entry<String, String[]> paramEntry = paramIterator.next();
      if (paramEntry.getKey().equals("oldtaxsubgroupid" + childIndex)) {
        if (paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null
            && !paramEntry.getValue()[parentIndex].isEmpty()) {
          return Integer.valueOf(paramEntry.getValue()[parentIndex]);
        }

      }
    }
    return 0;
  }
  public void setTaxDetails(Map<String, Object> param, int childIndex, BasicDynaBean taxBean) {
    Iterator<Entry<String, Object>> paramIterator = param.entrySet().iterator();
    while (paramIterator.hasNext()) {
      Entry<String, Object> paramEntry = paramIterator.next();
      if (paramEntry.getKey().equals("taxrate" + childIndex)) {
        if (paramEntry.getValue() != null && paramEntry.getValue() != null
            && !((String) paramEntry.getValue()).isEmpty()) {
          BigDecimal taxRate = new BigDecimal((String)paramEntry.getValue());
          taxBean.set("tax_rate", taxRate);
        }

      }
      if (paramEntry.getKey().equals("taxamount" + childIndex)) {
        if (paramEntry.getValue() != null && paramEntry.getValue() != null
            && !((String) paramEntry.getValue()).isEmpty()) {
          BigDecimal taxAmt = new BigDecimal((String)paramEntry.getValue());
          taxBean.set("tax_amt", taxAmt);
        }
      }
      if (paramEntry.getKey().equals("taxsubgroupid" + childIndex)) {
        if (paramEntry.getValue() != null && paramEntry.getValue() != null
            && !((String) paramEntry.getValue()).isEmpty()) {
          int itemSubgroupId = Integer.valueOf((String)paramEntry.getValue());
          if (itemSubgroupId != 0 && itemSubgroupId != -1)
            taxBean.set("item_subgroup_id", itemSubgroupId);
        }
      }
    }
  }
	
	public Map<String, Object> getTaxDetailsMap(Map param, int parentIndex, int childIndex) {
		Map<String, Object> taxMap = new HashMap<String, Object>();
		Iterator<Entry<String, String[]>> paramIterator = param.entrySet().iterator();
		while(paramIterator.hasNext()) {
			Entry<String, String[]> paramEntry = paramIterator.next();
			if(paramEntry.getKey().equals("taxrate"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxRate = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxRate.compareTo(BigDecimal.ZERO) >= 0)
					taxMap.put("tax_rate", taxRate);
				}
				
			}
			if(paramEntry.getKey().equals("taxamount"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxAmt = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxAmt.compareTo(BigDecimal.ZERO) >= 0)
					taxMap.put("tax_amt", taxAmt);
				}
			}
			
			if(paramEntry.getKey().equals("originaltaxamount"+childIndex)){
			  if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
			    BigDecimal originalTaxAmt = new BigDecimal(paramEntry.getValue()[parentIndex]);
			    taxMap.put("original_tax_amt", originalTaxAmt);
			  }
			}
			
			if(paramEntry.getKey().equals("taxsubgroupid"+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					int itemSubgroupId = Integer.valueOf(paramEntry.getValue()[parentIndex]);
					if(itemSubgroupId != 0 && itemSubgroupId != -1)
						taxMap.put("item_subgroup_id", itemSubgroupId);
				}
			}
		}
		return taxMap;
	}
	
	/**
	 *  This method is used to extract tax split from request map. we have to specify the fieldGetKeys with ui field name
	 *  and fieldSetKeys with database column names.
	 * @param param
	 * @param parentIndex
	 * @param childIndex
	 * @param fieldGetKeys
	 * @param fieldSetKeys
	 * @return
	 */
	public Map<String, Object> getTaxDetailsMap(Map param, int parentIndex, int childIndex, String[] fieldGetKeys, String[] fieldSetKeys) {
		Map<String, Object> taxMap = new HashMap<String, Object>();
		Iterator<Entry<String, String[]>> paramIterator = param.entrySet().iterator();
		while(paramIterator.hasNext()) {
			Entry<String, String[]> paramEntry = paramIterator.next();
			if(paramEntry.getKey().equals(fieldGetKeys[0]+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue().length > parentIndex && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxRate = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxRate.compareTo(BigDecimal.ZERO) > 0)
					taxMap.put(fieldSetKeys[0], taxRate);
				}
				
			}
			if(paramEntry.getKey().equals(fieldGetKeys[1]+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue().length > parentIndex && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					BigDecimal taxAmt = new BigDecimal(paramEntry.getValue()[parentIndex]);
					//if(taxAmt.compareTo(BigDecimal.ZERO) > 0)
					taxMap.put(fieldSetKeys[1], taxAmt);
				}
			}
			if(paramEntry.getKey().equals(fieldGetKeys[2]+childIndex)){
				if(paramEntry.getValue() != null && paramEntry.getValue().length > parentIndex && paramEntry.getValue()[parentIndex] != null && !paramEntry.getValue()[parentIndex].isEmpty()) {
					int itemSubgroupId = Integer.valueOf(paramEntry.getValue()[parentIndex]);
					if(itemSubgroupId != 0 && itemSubgroupId != -1)
						taxMap.put(fieldSetKeys[2], itemSubgroupId);
				}
			}
		}
		return taxMap;
	}
}
