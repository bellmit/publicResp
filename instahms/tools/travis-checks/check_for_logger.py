import os
import re
from utils import request_review, get_added_lines_in_folder

## This is a script to check and post a review comment
## if log4j logger is used instead of slf4j

def uses_older_logger():
    added_lines = get_added_lines_in_folder("instahms/src/main/java/")
    comment = (
        "Changes in this PR use `log4j` directly instead of `slf4j`. "
        "Use the logger from `slf4j` instead."
    )
    if "org.apache.log4j" in added_lines or "org.apache.logging" in added_lines:
        print("Log4j logger present in changes; posting review comment")
        request_review(comment)
        return True
    print("Log4j not used")
    return False
