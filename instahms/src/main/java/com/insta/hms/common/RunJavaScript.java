package com.insta.hms.common;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * The Class RunJavaScript.
 *
 * @author krishnat
 * 
 *         used to execute javascript code from java.
 */
public class RunJavaScript {

  /** The script engine. */
  private static ScriptEngine scriptEngine = null;

  /**
   * Gets the script engine.
   *
   * @return the script engine
   */
  public static ScriptEngine getScriptEngine() {
    if (scriptEngine == null) {
      javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
      scriptEngine = manager.getEngineByName("js");
    }
    return scriptEngine;
  }

  /**
   * str - string to validate. regExp - regExp pattern to test.
   *
   * @param str    the str
   * @param regExp the reg exp
   * @return true, if successful
   * @throws ScriptException the script exception
   */
  public boolean validateRegExp(String str, String regExp) throws ScriptException {
    javax.script.ScriptEngine scriptEngine = getScriptEngine();
    scriptEngine.put("str", str);
    scriptEngine.eval("var regExp=" + regExp);
    return (Boolean) scriptEngine.eval("regExp.test(str)");
  }

}
