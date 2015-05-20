MASON tutorial: Replacing Java with Clojure, v. 1
====

**See README.md in the root of this git repository for a general overview
of this project.**

Clojure source code here is copyright 2015 by Marshall Abrams, and is
distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  The java source code
is copied from the the MASON manual v. 18, and is by Sean Luke.  See the
MASON manual for its license.

----------

In this version, I replaced Student.java with Student.clj.  Not
replacing the other files shows that, in this case, one can write a
drop-in replacement for the Java source file, using Clojure.  (See the
version in directory 2 for a case in which this is not quite true.)

Student.java can be found in src/disabled.

See README.md in the root of this git repository for additional
information.

----------

To compile, run the script compile.sh.  This causes Student.clj to be
compiled before the Java source files, so that the `Student` class will
be visible to javac during compilation.  By default Leiningen compiles
Java files before Clojure files.
