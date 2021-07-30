package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class basically reorganize the the result set. So that more relevant search comes upward in
 * search result(auto complete)
 * 
 * @author ritolia
 *
 */
public class RelevantSorting {

  /**
   * Items that starts with searchId are given more priority. Rank is being calculated based on
   * position in filteredItems and searchId passed. More the rank value scored it will come higher
   * in search result.
   *
   * @param filteredItems the filtered items
   * @param searchId      the search id
   * @param key           the key
   * @return the list
   */
  public static List<BasicDynaBean> rankBasedSorting(List<BasicDynaBean> filteredItems,
      String searchId, String key) {
    if (searchId == null || searchId.trim().equals("") || filteredItems == null) {
      return filteredItems;
    }

    Map<Float, List<BasicDynaBean>> sortedResult = new TreeMap<Float, List<BasicDynaBean>>();
    List<BasicDynaBean> filteredSortedList = new ArrayList<BasicDynaBean>();

    String[] searchTerms = searchId.trim().toLowerCase().split(" ");

    for (BasicDynaBean item : filteredItems) {
      float rank = 0;
      int termsMatched = 0;
      String[] itemNameTerms = ((String) item.get(key)).toLowerCase().split(" ");

      int filterIndex = 0;
      for (String filterSubString : searchTerms) {
        String filterSubStringVal = filterSubString.trim();
        if (filterSubStringVal.equals("")
            || filterSubStringVal.matches("(\\+|-|\\(|\\)|\\[|\\])*")) {
          continue;
        }
        int termPos = filterIndex + 1;
        float termScore = 0;

        int thisTermIndex = 0;
        for (String itemNameTerm : itemNameTerms) {
          String itemNameTermVal = itemNameTerm.trim();
          if (itemNameTermVal.equals("") || itemNameTermVal.matches("(\\+|-|\\(|\\)|\\[|\\])*")) {
            continue;
          }
          int phrasePos = thisTermIndex + 1;
          if (itemNameTermVal.indexOf(filterSubStringVal) == 0 && termScore == 0) {
            termScore += (termPos < phrasePos) ? ((termPos * 1f) / phrasePos)
                : ((phrasePos * 1f) / termPos);
            termsMatched += 1;
          }
          thisTermIndex++;
        }
        rank += termScore;
        filterIndex++;
      }

      rank = (-1 * rank * termsMatched) / itemNameTerms.length;
      if (sortedResult.containsKey(rank)) {
        List<BasicDynaBean> rankList = sortedResult.get(rank);
        rankList.add(item);
        sortedResult.put(rank, rankList);
      } else {
        List<BasicDynaBean> tempList = new ArrayList<BasicDynaBean>();
        tempList.add(item);
        sortedResult.put(rank, tempList);
      }
    }

    for (Map.Entry<Float, List<BasicDynaBean>> entry : sortedResult.entrySet()) {
      List<BasicDynaBean> value = entry.getValue();
      filteredSortedList.addAll(value);
    }
    return filteredSortedList;
  }
}
