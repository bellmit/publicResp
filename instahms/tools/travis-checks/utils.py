import requests
import os
from git import Repo
import re

pr_url = "https://api.github.com/repos/practo/insta-hms/pulls"
_GITBOT_TOKEN = os.environ['MAVEN_TOKEN']
_TRAVIS_PULL_REQUEST = os.environ['TRAVIS_PULL_REQUEST']
_TRAVIS_COMMIT_RANGE = os.environ['TRAVIS_COMMIT_RANGE']
git_bot_headers = {"Authorization" : "token " + _GITBOT_TOKEN}
review_url = pr_url + "/" + _TRAVIS_PULL_REQUEST + "/reviews"


def request_review(comment):
    request_changes_payload = {"event" : "REQUEST_CHANGES","body" : comment}
    if not has_the_same_review_posted(comment):
        response = requests.post(review_url, json=request_changes_payload, 
                                 headers=git_bot_headers)
        if response.status_code != 200:
            print("Sanity check failed. Contact Platform team.")
            print("Unable to post comment. Response returned as:" + response.text())
            exit(1)


def get_added_lines_in_folder(folder):
    repo = Repo("../")
    git = repo.git()
    diff = git.diff(_TRAVIS_COMMIT_RANGE, folder)  #diff of the last commit
    # regex to remove extraneous lines in the diff
    regex = re.compile(r"^((?:\+\+\+.+)|(?:[^\+].*))$",re.MULTILINE)
    regex1 = re.compile("^\+(.*)$",re.MULTILINE)
    added_lines = regex1.sub(r"\1",regex.sub(r"",diff)).strip()
    return str(added_lines)


def has_the_same_review_posted(comment):
    reviews_posted = requests.get(review_url,  headers=git_bot_headers)
    for review in reviews_posted.json():
        if comment == str(review['body']) and \
           str(review['state']) == 'CHANGES_REQUESTED':
            return True
    return False
