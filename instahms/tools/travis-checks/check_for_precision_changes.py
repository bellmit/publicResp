import os
import re
from utils import request_review, get_added_lines_in_folder

## This is a script to check and post a review comment
## if there are any changes/additions to numeric columns
migrations_folder = "instahms/src/main/resources/migrations/"
def has_new_numeric_columns():
    # if travis build is not a pull request; do nothing
    added_lines = get_added_lines_in_folder(migrations_folder)
    comment = ("This PR includes some changes to numeric columns; Please ensure"
         + "that the corresponding precision-3 changes are added too" 
         + "if required.")
    if "numeric" in added_lines:
        print("Numeric keyword present in changes; posting review comment")
        request_review(comment)
        return True
    print("Numeric keyword not present in changes")
    return False
