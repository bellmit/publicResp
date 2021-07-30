[Back to Development Guide](devguide_toc.md)

## Code Review Guidelines

### OVERVIEW

This document is meant to be a list of guidelines and rules about the process of Code Review in Insta. While reviewing the code, it is important to answer the following basic questions:

1. Am I able to **understand** the code easily?
2. Is the code written following the **coding standards/guidelines**?
3. Is the same code **duplicated** more than once?
4. Can I **unit test / debug** the code easily to find the root cause?
5. Is this function or class **too big**? If yes, is the function or class having too many responsibilities?

![Expert Code Reviewer](code_review.png)


## GUIDELINES FOR THE AUTHOR:

1. Ensure that a **code reviewer is chosen BEFORE** starting the development of a feature. A code reviewer should be part of the planning as much as possible to ensure that he/she gets a context of the feature being developed.
2. It is good to have **multiple code reviews for a huge feature**.
3. It is good to have **multiple reviewers** if you are changing the code of multiple modules in one PR.
4. **Smaller is better:** Keep your pull requests small so that you can iterate more quickly and accurately. In general, smaller code changes are also easier to test and verify as stable.
5. **Keep code reviews that change logic separate** from reviews that change code style. If you have changed both, submit the code style changes as a branch and then follow-up with a branch to change logic.
6. **Timeline/Effort Estimation** : The initial estimate for a feature must be inclusive of the effort it would take to get a code review done and unit tests written.
7. Ensure that **Sonarlint** is turned on in the local development environment. Refactoring the code is much easier in the initial stages of development.
8. Follow the **DRY (Don&#39;t repeat yourself)** principle as diligently as possible: **Reuse** code.
9. Code should be [Self Documenting](https://stackoverflow.com/questions/209015/what-is-self-documenting-code-and-can-it-replace-well-documented-code).
10. Commit message should contain appropriate JIRA ID to improve tracking.
11. **Mention any dependent branches/PRs in the PR description** and ensure all of them are merged/ready to be merged before merging the PR after approval.

## GUIDELINES FOR THE CODE REVIEWER:

1. The primary responsibility of a code review is to ensure the code meets functional requirement. The code should not break any existing functionality and cover all the edge cases.
2. Ensure that the PR is opened on day one and **regularly follow the PR** throughout its development.
3. When adding a code review comment, provide examples on how to improve the code wherever applicable.
4. The **code must satisfy the requirements of the checklist** below, before it is approved.

## CHECKLIST FOR THE CODE REVIEWER:

### Security
  1. The code mustn&#39;t make the application vulnerable to [Sql-Injection](https://www.w3schools.com/sql/sql_injection.asp) or XSS vulnerability.
  2. **Mustn&#39;t log private information** in logs like patient phone number, name etc.
  3. Log messages should have **appropriate log levels**. Debug messages shouldn&#39;t be classified as Info level logs to avoid spamming of the application log.
  4. Must close any open resources(File pointers, Streams, Connections).
  5. **Object initialization(such as DAO classes;utility classes) should be done at the class level** and not at the function level. This prevents excessive memory utilization and frequent garbage collection cycles.
  6. Any new endpoint must have the correct action\_id; screen rights associated with it. (We should **avoid passthru endpoints** as much as possible).
  7. **Backend validations** are a must.
### Exception Handling
  1. Avoid throwing exceptions that can be handled at the same level.
  2. Catch compile time exceptions (like ParseException; IOException) and throw them as custom exceptions to ensure that the user sees a meaningful message on the screen.
  3. Mustn&#39;t catch an exception and log it without actually handling the exception. The catch block must provide a way to handle the exception gracefully.
### Documentation
  1. The **code should be readable** with meaningful variable and function names (self documenting).
  2. If the function is too big or has complex logic provide meaningful comments.
  3. There should be a **confluence document for new features** detailing the technical specifications of a feature.
  4. **Meaningful code commit** messages help in tracking the code changes effectively.
  5. The pull request **MUST** have a description **and a** link to the Associated JIRA ID.**
  6. Each method must do **one thing only**.
4. **Unit testing**: There must be associated unit tests with all new features.
5. Commits should be atomic to make rollback easier.
6. There should be **no dead code**. Commented out code must be deleted.
