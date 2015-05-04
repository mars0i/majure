majure
====

## Clojure + MASON agent-based modeling library: experiments 

Clojure source code here is copyright 2015 by Marshall Abrams, and is
distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  

The java source code is copied from the the MASON manual v. 18, and is
by Sean Luke.  See the MASON manual for its license.

MASON can be found at
[http://cs.gmu.edu/~eclab/projects/mason](http://cs.gmu.edu/~eclab/projects/mason).

Clojure can be found at [http://clojure.org](http://clojure.org)

These examples follow the tutorial in chapter 2 of the MASON manual v. 18. 

Throughout, I tried to write relatively idiomatic Clojure rather than
giving a direct translation.  However, I didn't take complete freedom; I
did use the original Java source as a basis, and much of the methodology
there remains in the Clojure source.  The Clojure code that results is a
kind of Clojure/Java hybrid.  I also renamed some variable names, etc.
However, the source files contain some pointers that should allow
finding corresponding code in the original Java listed in the manual.
(Note that I didn't necessarily add comments that are in the manual to
the Java source here, but you can read the comments in the manual.)

Note that the `:gen-class` directives (which call Clojure function
`gen-class` does much of the work of presenting a Clojure namespace so
that it looks to Java like a Java class.  (There are other ways to
generate "Java classes" in Clojure, but none of them seem suitable for
this application.)

### 1:

Replaces Student.java with Student.clj, i.e. reimplements the `Student`
class in Clojure.  The `Students` and `StudentsWithUI` classes are
still written in Java.

### 2:

Starting from experiment **1**, replaces Students.java with
Students.clj, i.e. both `Student` and `Students` are written in Clojure.
`StudentsWithUI` remains in java.
