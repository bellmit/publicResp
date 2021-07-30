package com.insta.hms.master.RegularExpression;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 *
 * @author Anil N
 *
 */
public class RegularExpValidation {

	public static String RegExpValidation(String regPattern){
		String errorMessage = null;
		try {
			Pattern.compile(regPattern);
			}
		catch (PatternSyntaxException exception) {
			errorMessage = exception.getDescription();
            }
		return errorMessage;
		}
	}
