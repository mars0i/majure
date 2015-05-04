majure
====

## Clojure + MASON agent-based modeling library: experiments 

These examples follow the tutorial in chapter 2 of the MASON manual v. 18. 
I didn't necessarily complete the tutorial, but the basic elements are
all here.

Clojure source code here is copyright 2015 by Marshall Abrams, and is
distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  

The Java source code is copied from the the MASON manual v. 18, and is
by Sean Luke.  See the MASON manual for its license.

MASON can be found at
[http://cs.gmu.edu/~eclab/projects/mason](http://cs.gmu.edu/~eclab/projects/mason).

Clojure can be found at [http://clojure.org](http://clojure.org)

------------

In the Clojure source files,  I try to write relatively idiomatic
Clojure rather than giving a direct translation of the Java.  However, I
didn't give myself complete freedom; and much of the methodology from
the Java source files remains in the Clojure source.  (The Clojure code
that results is a kind of Clojure/Java hybrid, from the point of view of
a Clojure programmer.)  I also renamed some variable names, etc.
However, the source files contain some pointers that should allow
finding corresponding code in the original Java listed in the manual.
(Note that I didn't necessarily add comments that are in the manual to
the Java source files, but you can read the comments in the manual.)

See *Tips* below for additional information.

--------------------------------------------

## The experiments

Rather than ultimately replacing all of the Java source with Clojure in
one version, I preserved early versions.  Experiment 1 shows a case in
which a single Java source file can be replaced with a Clojure source
file, without making any changes in the other Java source files.
Experiment 2 shows a case in which changes had to be made both to the
remaining Java as well as the Clojure file that had been copied from
Experiment 2.

### 1:

Replaces Student.java with Student.clj, i.e. reimplements the `Student`
class in Clojure.  The `Students` and `StudentsWithUI` classes are
still written in Java.

### 2:

Starting from experiment 1, replaces Students.java with
Students.clj, i.e. both `Student` and `Students` are written in Clojure.
`StudentsWithUI` remains in Java.

-------------------------

## Tips

* This is neither a Clojure tutorial for Java programmers, nor a Java
  tutorial for Clojure programmers.  There are several good books and
  online sources for learning Clojure, as well as innumerable sources
  for learning Java.  Most of the notes below won't make sense unless
  you're comfortable with Clojure.  My goal, in the end, is to use MASON
  exclusively through Clojure, without writing any Java code, or at
  least minimizing the amount of Java I need to write.  However, there's
  no way to learn MASON without understanding a bit of Java.  The manual
  and class documentation are focused exclusively on Java, and using
  MASON with Clojure would necessarily involve some understanding of
  Java anyway, because it means using Java classes in Clojure.  If you're
  an experienced programmer, you might be able to get by with a quick
  introduction to Java concepts such as this one:
  [http://www.braveclojure.com/java](http://www.braveclojure.com/java).

* Many Clojure operations are lazy, which means that you have to be
  careful about interactions between laziness and imperative Java
  code.  This is another reason that it helps to have a bit of Clojure
  experience if you want to use MASON with Clojure.

* `ns`'s `:gen-class` directive (which calls Clojure `gen-class`
  function) does much of the work of presenting a Clojure namespace so
  that it looks to Java like a Java class.  (There are other ways to
  generate things that act like Java classes, but none of them seem very
  suitable for use with MASON.  See Chas Emerick's [Flowchart for
  choosing the right Clojure type definition
  form](http://cemerick.com/2011/07/05/flowchart-for-choosing-the-right-clojure-type-definition-form).)

* There are a few online sources that explain the basics of
  `gen-class` (you'll find them easily enough with a web search), but
  I've found nothing that covers all of the ideas I needed.  The most
  thorough documentation on `gen-class` that I've found is in [*The Joy
  of Clojure*](http://www.manning.com/fogus2) by Michael Fogus and Chris
  Houser, and in the standard docstring for `gen-class`.  Unfortunately,
  the [online version of the
  docstring](https://clojuredocs.org/clojure.core/gen-class) messes up
  its formatting, making it difficult to read.  If you have Clojure
  installed, running `(doc gen-class)` at the repl will be more useful,
  or you might want to enter that expression at the prompt at [Try
  Clojure](http://www.tryclj.com).  In the end I had to piece together
  the information I needed from other sources as well, including (of
  course) trial and error.

* While it can require a bit of effort to use `gen-class`
  appropriately in different contexts, there are many ways in which
  using Java from Clojure is trivially easy. For example, several MASON
  methods return instances of MASON's `sim.util.Bag`.  This
  implements the `java.util.Collection` interface, and many Clojure
  functions that work with Clojure collections can be applied directly
  to anything that implemements `Collection`.

* I try to follow the rule of prefacing accesses to Java instance
  variables, in Clojure, with `.-`.  (The dash is optional but
  recommended.)  Method calls are prefaced with `.` alone.

* I use `this` as the name of the first function argument for a
  function that's meant to be called as a Java method, and `me` when
  I want to pass a class instance to a method that's used only from
  within Clojure.

* Note that Clojure implementations of functions that are supposed to
  appear to be Java methods have to have a dash `-` prefixed to their
  name in the Clojure source, even though they're called with `.<name`
  and no `-`.  These methods also have to be listed with the `:methods`
  keyword in `gen-class`, unless they're implementing methods for an
  interface listed with the `:interface` keyword.

* Using `gen-class`, any instance state has to be stored in a single
  variable specified with the `:state` keyword.  This is pretty flexible
  as long as you can access the state variable using functions.  If you
  need a true public instance varible, you can have one (the state
  variable), but that's it.  So if you need more than one data item,
  other classes have to access instance data using accessor functions.

* On the other hand, multiple instance variable that are inherited from
  a Java class are unproblematic.

* By preference, Clojure uses a one-pass compiler, which means that
  specific order of compilation is sometimes needed to interleave
  Clojure and Java source files.

