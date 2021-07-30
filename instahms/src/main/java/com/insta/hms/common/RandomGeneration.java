package com.insta.hms.common;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class RandomGeneration.
 *
 * @author kalpana.muvvala
 */
public class RandomGeneration {

  /**
   * Random generated password.
   *
   * @param size the size
   * @return the string
   */
  public static String randomGeneratedPassword(int size) {

    char[] charSet = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    int varI;
    int varJ = 0;
    StringBuilder pwd = new StringBuilder();
    List<Character> list = new ArrayList<>();
    SecureRandom randomGenerator = new SecureRandom();

    do {
      varI = randomGenerator.nextInt(1000000) % charSet.length;
      if (!list.contains(charSet[varI])) {
        list.add(charSet[varI]);
        varJ++;
      }
    } while (varJ < size);

    Iterator<Character> itr = list.iterator();
    while (itr.hasNext()) {
      pwd.append(itr.next());
    }
    return pwd.toString();
  }

}
