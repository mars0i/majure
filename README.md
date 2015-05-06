majure
====

## Clojure + MASON agent-based modeling library

#### Experimental use of MASON from Clojure

These examples follow the tutorial in chapter 2 of the MASON manual v.
18 through section 2.8.

Clojure source code and text here is copyright 2015 by Marshall Abrams,
and is distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  

The Java source code is copied from the the MASON manual v. 18, and is
by Sean Luke.  See the file MASONmanualLICENSE for its license.

MASON can be found at
[http://cs.gmu.edu/~eclab/projects/mason](http://cs.gmu.edu/~eclab/projects/mason).
Its license is in the file MASONLICENSE.

Clojure can be found at [http://clojure.org](http://clojure.org)

And Java ... is like [Elvis](http://www.mojonixon.com/lyrics/elvisiseverywhere.html).

--------------------------------------------

## The experiments

Each experiment replaces one additional Java source file with a Clojure
source file.  The sequence of experiments follows my sequence of
exploration, but I kept the old versions because I think it's important
to see that Clojure source and Java source can coexist--albeit
*sometimes* with small changes to the Java source.  As it turns out,
each additional experiment involved additional Clojure-Java interop
facilities.

### 1:

Replaces Student.java with Student.clj, i.e. reimplements the `Student`
class in Clojure.  The `Students` and `StudentsWithUI` classes are
still written in Java.  Student.clj is a drop-in replacement for
Student.java using Clojure's `gen-class` macro.

### 2:

Starting from experiment 1, replaces Students.java with Students.clj,
i.e. both `Student` and `Students` are written in Clojure.
`StudentsWithUI` remains in Java.  I needed to make small modifications
to StudentsWithUI.java as well as Student.clj to make this work.

### 3:

Starting from experiment 3, replaces StudentsWithUI.java with
StudentsWithUI.clj.  This is an all-Clojure version of the students app.
In this version I used Clojure's `proxy` as well as `gen-class` in order
to create Java classes.

-------------------------

## Notes and tips

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
  Java anyway, because it means using Java classes in Clojure.  If you
  know Clojure and are an experienced programmer, you might be able to
  get by with a quick introduction to Java concepts such as this one:
  [http://www.braveclojure.com/java](http://www.braveclojure.com/java).

* In the Clojure source files,  I try to write relatively idiomatic
  Clojure rather than giving a direct translation of the Java.  However,
  I didn't give myself complete freedom; much of the methodology from
  the Java source files remains in the Clojure source.  (The Clojure
  code that results looks like a kind of Clojure/Java hybrid from the
  point of view of a Clojure programmer.)  I also renamed some variable
  names, etc.  However, the source files contain some pointers that
  should allow finding corresponding code in the original Java listed in
  the manual.  (Note that I didn't necessarily add comments that are in
  the manual to the Java source files, but you can read the comments in
  the manual.)

* Clojure is usually used with the Leiningen dependencies tool.
  To run this code using leiningen, change to one of the numbered
  subdirectories, then execute `./compile.sh` and then one of the
  following commands:  
	`lein run`  
	`lein with-profile withgui run`  
	`lein with-profile nogui run`  
  You can also use `lein jar` or `lein uberjar` to make a jar file
  for use without Leiningen.

* Many convenient Clojure functions return lazy sequences.  Laziness
  has both advantages and disadvantages.  Among other things, it means
  that you has to be have to be careful about interactions between
  laziness and imperative code--including a lot of the Java-style code
  needed to use MASON.  (This is another reason that it helps to have a
  bit of Clojure experience if you want to use MASON with Clojure.)

* Most of my code here uses `ns`'s `:gen-class` directive (which calls Clojure `gen-class`
  function) to do much of the work of presenting a Clojure namespace so
  that it looks to Java like a Java class.  I also use `proxy` in one
  case.  (There are other ways to
  generate things that act like Java classes.  See Chas Emerick's [Flowchart for
  choosing the right Clojure type definition
  form](http://cemerick.com/2011/07/05/flowchart-for-choosing-the-right-clojure-type-definition-form).)

* There are (e.g. online) sources that explain the basics of
  `gen-class` (you'll find many of them easily enough with a web
  search), but I've found nothing that covers all of the ideas I needed.
  The most thorough documentation on `gen-class` that I've found is in
  [*The Joy of Clojure*](http://www.manning.com/fogus2) by Michael Fogus
  and Chris Houser, and in the standard docstring for `gen-class`.
  Neither is *completely* adequate.  In the end I had to piece together
  the information I needed from other sources as well, including (of
  course) trial and error.  (Note that the [official online version of the
  docstring](https://clojuredocs.org/clojure.core/gen-class) for
  `gen-class` messes up its formatting, making it difficult to read.
  [This
  version](http://conj.io/store/v1/org.clojure/clojure/1.6.0/clj/clojure.core/gen-class)
  preserves the formatting.)

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

--------------------------------------------

## Some thoughts about these experiments

Though I personally prefer Clojure to Java, the kind of extensive use of
Java required in this example makes writing Clojure a lot closer to Java
than what I'm used to.  The resulting code is more verbose than
usual--perhaps even more verbose than the Java source code it's intended
to replace.  Furthermore, it's necessary to keep thinking through the
relationship between Clojure and Java.  So ... if you like Java, and
only kind of like Clojure, this probably isn't worth the trouble.
However, over time, I suspect that I'll develop coding patterns that
streamline the process of writing MASON models in Clojure.
