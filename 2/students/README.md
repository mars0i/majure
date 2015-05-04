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

I'm not sure why it's possible to pass class `students.Student` to
`doLoop` *in* the definition of class `students.Student`, given
Clojure's one-pass compiler, but it is.

----------

Note default constructors are automatically generated.
cf. http://stackoverflow.com/questions/18780071/clojure-multiple-constructors-using-gen-class
