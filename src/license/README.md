### License management

_The following information is for maintainers of this project._

* Add license to header of each source code file of this project:

  `mvn license:update-file-header`
  
  Remember to commit changed files.

* Update file LICENSE-3RD-PARTY that lists all 3rd party libraries used and their licenses:

  `mvn license:add-third-party`
  
    Remember to commit changed file LICENSE-3RD-PARTY

* Download all 3rd party licenses to /scr/license folder:

  `mvn license:download-licenses`

  NB! Before you commit - remove duplicates and check the quality of license file contents.