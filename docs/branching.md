[Back to Development Guide](devguide_toc.md)

## Git Protected branches and Release Mapping

[__release_candidate__](/practo/insta-hms/tree/release_candidate) points to stable development build of upcoming release.

[__develop__](/practo/insta-hms/tree/develop) points to current development.

__instaxx.xx__ points to codebase state of previous released version for patches.

Examples: 
Consider latest release as 11.13 with 3 patches done, upcoming release as 12.0 then

insta11.13: 11.13.3

release_candidate: 12.0 Release Candidate

develop: 12.0 development


PS: It is much easier to peek in instahms/pom.xml <version> tag to check release version

### Branch naming rules

Follow below guidelines while naming branches

__feature-<feature_name>__ Branches for feature or enhancements

__fix-<jira_id>__ Branches for fixes for JIRA reported bugs. 
  
 __fix-<fix_short_name>__ Avoid random fixes as much as possible. Create and track using JIRA Tickets.

### Guidelines to follow while developing on a branch

* Make a pull request _immediately_(with the title as the JIRA ID) and in the description mention(using the appropriate markdown syntax)  
Why: Why is this PR Required   
What: What is it doing  
And any other relevant information like testing scope,associated JIRA Id etc.
* Tag the PR as **Work in Progress**
* After development is done; assign a reviewer(another developer) for code review and change the tag to **Code Review Pending**
* The reviewer can change the tag to **Code Review Done** after he/she is done with the code review.
* Make appropriate changes(according to the code review;a second round of code review may be required) and change the branch tag to **ready to merge**.
* Mention Revelent JIRA ID as part of Commit Messages so that JIRA also has a link back to this PR or commit.

### Checking out UI or HMS code as of a particular release

At times as a developer you may need to dwell into codebase searching commit hash for a particular released version in order to figure out how the code looked like for debugging production issues. To help out in this effort all productions releases after 12.0.0 are tagged using git tags.
You can access them at https://github.com/practo/insta-hms/tags or in cli using
```sh
git tag -l
```
This now allows you to switch to a particular production release commit by saying
```sh
git checkout <version-with-build-number>
```
example
```sh
git checkout 12.2.8-9558
```
will checkout code as it was released in `12.2.8-9558`.

### Branching Guidelines for generating patch fixes for IES Issues
Use these convention while creating branches for fixes in UI or HMS codebase
Branches should be named using format `{version}_{prodschema}` where `version` is version without build number e.g, 12.3.17 and `{prodschema}` is production schema name.
For example if you are planning to release a patch for schema `ahllclinics` for their production version `12.2.8-9558` then branch name for such patch would be `12.2.8_ahllclinics`.
An alternate scenario where you wish to create a patch for a issue reported on `ahllclinics_pr` which is undergoing pre upgrade testing and is currently on `12.3.17`. In such case the branch name would be `12.3.17_ahllclinics` In a nutshell strip `_t` or `_pr` from schema names when creating branch names.

#### Checking out or creating new branch
You can make use of below commands from root of your hms code base to checkout an existing patch branch or create one if does not exist.
```sh
git checkout {version-with-build}
git pull
cd insta-ui
git checkout {version-with-build}
git checkout -B {version}_{prodschema}
cd ..
git checkout -B {version}_{prodschema}
```
`-B` parameter instructs git to checkout an existing branch if exists otherwise create a new one. `{version-with-build}` is essentially full version number as shown in footer of Insta HMS pages e.g, `12.3.17-10268`.
If you are checking out existing patch branch ensure you fork a new branch for your fix. This ensures the patch branch is stable for shipping patches fellow developers may be working on. Once your patch is tested, merge it to patch branch for patch release and release branch (instaxx.x) for inclusion of fix in next CIG release.

#### Changes in creation of patch manifest
As the patch is incremental recommended approach is to create a patch manifest with all patches till date, to obtain list of all files that need to be included use following command from  root of your hms code base
`git diff {version-with-build}..{version}_{prodschema} --name-only`

