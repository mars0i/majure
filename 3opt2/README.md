MASON tutorial: Replacing Java with Clojure, v. 3opt2
====

**See README.md in the root of this git repository for a general overview
of this project.**

Clojure source code here is copyright 2015 by Marshall Abrams, and is
distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  The java source code
is copied from the the MASON manual v. 18, and is by Sean Luke.  See the
MASON manual for its license.

----------

This is a version 3plus, but with an attempt to optimize the code.
3opt2 goes deeper than 3opt by splitting Students into Students and
AltState.  This allows more type hinting because it avoids a cyclic
dependency.

Note that replacing the proxy class in `step` in Students.clj with a
gen-class version (TemperingSteppable) doesn't seem to make a
difference in speed, and actually introduces a new cyclic dependency.

----------

To compile, execute 'lein compile'.  To run, after compiling if
necessary, execute 'lein run'.  To run without the gui, execute 'lein
with-profile nogui run'.
