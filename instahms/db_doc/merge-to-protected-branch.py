#!/usr/bin/env python3
import requests
import sys
import time

settings = __import__("settings", fromlist=['*'])

config = settings.CONFIG

check_runs_to_check = ["Travis CI - Pull Request"]

if len(sys.argv) != 3:
    print("Usage : " + sys.argv[0] + " <build_version> <base_branch>")
    sys.exit(1)

base = sys.argv[2]
version = sys.argv[1]

pr_url = "https://api.github.com/repos/practo/insta-hms/pulls"
base_url = "https://api.github.com/repos/practo/insta-hms/"

pr_payload = {
	"title" : "Build " + version,
    "head" : "build_" + version,
    "base" : base,
    "body" : "Automated PR created by build script for build " + version
}
build_bot_headers = {
    "Authorization" : "token " + config['github_tokens']['buildbot']
}

response = requests.post(pr_url, json=pr_payload, headers=build_bot_headers)
if response.status_code != 201:
    sys.stdout.write("Failed to create Pull request for build " + version + "\n")
    sys.stdout.write(response.status_code + " ==> ")
    sys.stdout.write(response.content + "\n")
    sys.exit(1)
response_body = response.json()
pr_number = str(response_body["number"])
commit_sha = response_body["head"]["sha"]
review_url = pr_url + "/" + pr_number + "/reviews"
merge_url = pr_url + "/" + pr_number + "/merge"
pr_check_runs_url = base_url + "commits/" + commit_sha + "/check-runs"

ci_headers = {
    "Authorization" : "token " + config["github_tokens"]["gitbot"],
    "Accept" : "application/vnd.github.antiope-preview+json"
}
git_bot_headers = {
    "Authorization" : "token " + config["github_tokens"]["gitbot"]
}
approval_payload = {
    "event" : "APPROVE",
    "body" : "Auto approving PR " + pr_number + " for build " + version
}

response = requests.post(review_url, json=approval_payload, headers=git_bot_headers)
if response.status_code != 200:
    sys.stdout.write("Failed to create Pull request review for build " + version  + "\n")
    sys.stdout.write(response.status_code + " ==> ")
    sys.stdout.write(response.content + "\n")
    sys.exit(1)

ci_build_progress = True
total_check_runs = len(check_runs_to_check)
while ci_build_progress:
    sys.stdout.write("Waiting for travis ci build to complete\n")
    time.sleep(30)
    response = requests.get(pr_check_runs_url, headers=ci_headers)
    if response.status_code != 200:
        continue
    body = response.json()
    if "check_runs" not in body:
        continue 
    check_runs = body["check_runs"]
    check_runs_found = 0
    check_runs_passed = 0
    check_runs_completed = 0
    for check_run in check_runs:
        if check_run["name"] in check_runs_to_check:
            check_runs_found = check_runs_found + 1
            check_runs_passed = check_runs_passed + (1 if check_run["conclusion"] == "success" else 0)
            check_runs_completed = check_runs_completed + (1 if check_run["status"] == "completed" else 0)
    if check_runs_found < total_check_runs:
        continue
    if check_runs_completed < total_check_runs:
        continue
    if check_runs_passed == check_runs_completed:
        ci_build_progress = False
    else:
        sys.stdout.write("Failed to merge Pull request review for build " + version + ". Travis build failed.\n")
        sys.stdout.write(response.status_code + " ==> ")
        sys.stdout.write(response.content + "\n")
        sys.exit(1)
sys.stdout.write("Travis CI build completed. Proceeding with merge after 10s\n")
time.sleep(10)
response = requests.put(merge_url, headers=git_bot_headers)
if response.status_code != 200:
    sys.stdout.write("Failed to merge Pull request #" + pr_number + " for build " + version  + "\n")
    sys.stdout.write(response.status_code + " ==>")
    sys.stdout.write(response.content + "\n")
    sys.exit(1)
