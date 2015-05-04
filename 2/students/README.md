MASON tutorial: Replacing Java with Clojure, version 2
====

In this version, I replaced Student.java with Student.clj, and
Students.java with Students.clj.

----------

In version 1, I replaced only Student.java.  Replacing Students.java was
more difficult, requiring modifications to StudentsWithUI.java and
Student.clj (or Student.java, if I were using it).

The original example used public instance variables (without accessor
functions) in Students.java, but using `:gen-class` to generate Java
classes in Clojure (which seems to be the best option here) doesn't
allow multiple public instance variables.  Thus references to these
instance variables in StudentsWithUI.java and Student.clj had to be
replaced by calls to getter functions now defined in Students.clj.  I
defined some of these variables as Clojure atoms so that I can add
setters later if needed.

----------

At this point, I'm also not writing a `main` in Students.clj.  It's
needed for "headless" execution without a GUI, but not for running with
the GUI.  The version of `main()` in Students.java can't be translated
to Clojure in any straightforward way because it calls `doLoop()`, a
method defined in the superclass `SimState` of `Students`, and passes
`Students.class` to `doLoop`.  I don't think Clojure can do this, since
Clojure prefers a one-pass compiler.  

I may be able to define `main` using the version of `doLoop` that accepts
a `MakeSimState` instead of a class, but that requires implementing
interface `MakeSimState`.  Or maybe I can put `doLoop` in a separate
class such as `core.clj`, either extending `SimState` or just making an
instance of it.  Or I could use the implementation of `doLoop` presented
in the manual.

**Note: I put `main` in core.clj.**

Note default constructors are automatically generated.
cf. http://stackoverflow.com/questions/18780071/clojure-multiple-constructors-using-gen-class
