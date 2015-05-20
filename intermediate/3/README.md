MASON tutorial: Replacing Java with Clojure, v. 3
====

**See README.md in the root of this git repository for a general overview
of this project.**

Clojure source code here is copyright 2015 by Marshall Abrams, and is
distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  The java source code
is copied from the the MASON manual v. 18, and is by Sean Luke.  See the
MASON manual for its license.

----------

In this version, I replaced Student.java with Student.clj, Students.java
with Students.clj, and StudentsWithUI.java with StudentsWithUI.clj.

The corresponding Java source files can be found in src/disabled.

See README.md in the root of this git repository and the README.md files in
1/ and 2/ for additional information.

----------

Most of the techniques used in StudentsWithUI.clj are the same as those
that I used in Students.clj.  The new element in StudentsWithUI.clj is
the use of Clojure's `proxy` macro to generate an unnamed subclass of
`OvalPortrayal2D`, parallelling an analogous technique used in the
StudentsWithUI.java.  

(However, for performance-sensitive code, it might be better to use
`gen-class`.  Executing `proxy`-generated methods involves a kind of
indirection that provides no benefit for the students application.)

----------

To compile, execute 'lein compile'.  To run, after compiling if
necessary, execute 'lein run'.  
