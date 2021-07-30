[Back to Development Guide](devguide_toc.md)

## Internationalization 

* Do not hardcode strings in presentation layer templates or class files (JSP, JSX). Having text, labels and error messages  as part of code slows down localization efforts, as someone needs to scan through code to remove these strings and put it into properties file.
* Always add strings to ```instahms/src/main/java/resources/application.properties``` file.
* Do not concatenate resource string to form resultant messages. Don’t assume grammar of resultant messages that looks great for english would also hold good for other languages. For example ```Total family expenditure``` & ```in this financial year is {0}``` forms ```Total family expenditure in this financial year is {0}``` which is grammatically correct for English but its hindi equivalents ```कुल पारिवारिक व्यय``` & ```इस वित्तीय वर्ष में है {0}``` forms ```कुल पारिवारिक व्यय इस वित्तीय वर्ष में है {0}``` is grammatically incorrect. The correct translated string is ```इस वित्तीय वर्ष में कुल परिवार व्यय {0} है```.
* Never add placeholders for pluralizing words. It may not yield gramatically correct results. Ex: ```You've bought {0} {1}s``` is bad. Use ```You've bought {0} {1}```.

_Rules to follow while adding resource strings to properties file_

* Do not introduce duplicate keys
* Create keys based on values rather than modules, to avoid redundant keys
* Keys should only contain smallcase english alphabets, numbers, period(.) or underscore(_) characters. 
* Each key-value pair must be in single line. Use ```\n``` to mark separation between lines for multiline values.
* Each entry should follow ```<key> = <value>``` format, i.e., add single space character on both sides of =.
* For date time formatting and localization leverage date formatter with locale of Javascript and Java over introducing properties for Short Day, Long Day, Short Month, Long Month names.
* Sort the translatable properties section before commiting changes (After ```# ------------------- translateable properties beyond this line -------------------```)
