[Back to Development Guide](devguide_toc.md)

## Versioning

Versioning follows MM.mm.cc schema where MM is Major, mm is Minor and cc is correction package.

__Major Version__

Release with changes that bring paradigm shift for a user or a major new feature. This release may have significant learning curve for a user, hence requiring more time to migrate. 

__Minor Version__

Release with minor changes that do not affect user workflow significantly, like introduction of additional fields in existing screen, minor new features with no impact on workflow. This release has minimal or no learning curve for user. 

__Correction Package__

Releases containing only the CIG bug fixes.

Bug fixes are provided only on last stable Minor release of current Major release. e.g if current release is 11.12 and upcoming release is 11.13 then all CIG bug fixes are provided on 11.12

On new Major Release, the last stable Minor release of previous Major release will continue to receive bug fixes for upto 4 weeks from date of release of such Major Release. e.g, Consider current release as 11.13 and upcoming release as 12.0. On release of 12.0, 11.13 will continue to receive bug fixes for upto 4 weeks from date of release of 12.0.
