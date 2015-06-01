tips.md
====
Marshall Abrams

Things I've learned that are relevant to using MASON with Clojure from
experimenting with different ways to implement the Students example in
Clojure, and other tips.

My goal was to see what's involved in writing a MASON simulation using
only Clojure by using Clojure to rewrite the Students simulation in
chapter 2 of the v18 MASON manual.  In my "majure" git repo, there are
several versions--each exploring different ways of writing the Students
simulation.

These notes aren't intended to be self-explanatory to someone who's
unfamiliar with Clojure, or unfamiliar with MASON, or even unfamiliar
with Java.  You can, of course, nevertheless use them to figure out what
you want to learn more about, if you're unfamiliar with something I
mention.

Note that while Clojure emphasizes pure functional programming, MASON is
designed for routine use of mutable data structures.  I didn't try to
fight this aspect of MASON in my versions of the Students simulation,
but I did try to make it clear in my code what parts were purely
functional and what parts were not.  It's important to have a clear view
of this distinction; otherwise mixing with Clojure's lazy sequences are
likely to cause problems at some point.

Clojure makes it trivially easy to call methods on Java classes using
its Clojure's dot syntax.  (Example: Using `ec.util.MersenneTwisterFast`
as a standalone random number generator in a Clojure program is easy,
and is a good idea since Clojure's built-in random functions use Java's
built-in random functions.)
The notes below focus on more intimate Clojure-Java interoperability
involving subclassing, interface implementation, defining methods that
can be found by MASON's Java classes, and a few other tricks.

### Students

There are four classes defined in the Students simulation:

* `Student`, which implements `sim.engine.Steppable`
* `Students`, which extends `sim.engine.SimState`
* `StudentsWithUI`, which extends `sim.display.GUIState`.
* An inner class in `Students`, which implements `Steppable`.

I tried out various methods to define these classes in Clojure.
In the end, I settled on using the `gen-class` macro to define
`Students` and `StudentsWithUI`, `deftype` to define `Students`, and
`reify` to define the inner class.  Other options are possible for the
last two cases.  See below for details.


### Classes

There are five ways to make classes in Clojure:

* `defrecord`
* `deftype`
* `reify`
* `proxy`
* `gen-class`

The *alternativeStudentClasses2step* version of my Students-in-Clojure
program contains files illustrating alternative ways of defining the
`Student` class using each of these options.  See the README.me file in
that directory for discussion of their speed differences.

`defrecord` is commonplace in Clojure so, other things being equal, it
should perhaps be preferred.  Other things are not always equal, though.
`deftype` is similar, and in the *alternativeStudentClasses2step* tests,
it was a lot faster than `defrecord`.

All of the five class creation macros allow implementing interfaces.
but only `proxy` and `gen-class` allow you to extend a class (such as
`SimState`).  However, `proxy` is potentially the slowest of the five
methods, because it uses an extra level of indirection to execute
methods, and it's more limited than `gen-class` in many ways.  I think
that `proxy` is unlikely to be a good choice for use with MASON if you
want optimal speed.  You can use `reify` instead, for example, to create
a Clojure equivalent of an inner class, if the class doesn't need to
extend a class. Neither `reify` nor `proxy` has built-in ways to store
state, though you may be able to use a closure, perhaps with atoms or
some other reference type, to associate state with a `reify` or `proxy`
object.

Note that I tried using `reify`, `proxy`, and `gen-class` to define the
inner class in `Students`.  They were all equally fast.  I suspect that
the `proxy` version was no slower simply because this class doesn't do
much.  Note that `proxy` was much slower than `reify` when used to
define the `Student` class in *alternativeStudentClasses2step*.

Overall, `gen-class` is the most flexible way to create classes in
Clojure.  I used it define `Students`, subclassing `SimState`, and to
define `StudentsWithUI`, subclassing `GUIState`.  I don't think `proxy`
can do everything needed for these cases.

### Mutable state

Only `deftype` allows *multiple* mutable fields, using the
`:unsyncronized-mutable` or `:volatile-mutable` keywords.  (There is a
scary warning about these options in the docstring for `deftype`, but my
understanding is that these options are unproblematic as long as you're
not going to have multiple threads accessing the same field.)

`gen-class` only allows a single mutable "state" field.

One way to get an effect like multiple mutable fields with `gen-class`,
`defrecord`, or non-mutable fields with `deftype`, is to use one of
Clojure's reference types.  For example, to have mutable state with
`gen-class`, you can store Clojure atoms in a Clojure `defrecord`
object that's stored in the state variable.

Another alternative is to store a `deftype` object  with mutable fields
in the state variable.  `deftype` with mutable fields was a little bit
faster than `defrecord` with atoms; I used `deftype` to go from 50% of
Java speed to 60% of Java speed.  (Using `deftype` for this purpose is
very verbose, though--I ended up with four similar signatures for each
field.  It might be worth writing a macro to generate all of the
relevant code.  This is a little tricky, though, if you want to use type
hints--which you do, if you're goint to the trouble of using mutable
fields with `deftype`.)

Another alternative is to stored data in the state field using Java
arrays, which you can create in Clojure using functions such as
`make-array` and `double-array`.

Note that there's no problem with using fields inherited from a
superclass defined in Java from within the same Clojure class definition
(probably: the same namespace).  They can be accessed as if they were
Java methods called from Clojure, i.e. with expressions such as
`(.random students)` to get the random number generator defined in the
SimState superclass of the `Students`.  You can modify these instance
variables using `set!`.


### Interfaces

The are three ways to define interfaces:

* `defprotocol`
* `definterface`
* `gen-interface`

For use with MASON, it's not clear that it matters whether you use
`defprotocol` or `definterface`.  I've gotten the same speed with both.
`definterface` requires that you use type specifications.  Type hints
are allowed but ignored on `defprotocol`.  `defprotocol` provides more
conveniences for use with Clojure.  (However, if you want to use an
interface defined in Clojure from Java, you might have to use
`definterface`.)

`gen-interface` allows you to extend another interface, but
this probably isn't needed.  For example, it was easy to define
`Student` implement both `Steppable` and an interface I defined.
(`definterface` is a simple wrapper around `gen-interface, and it's
probably better style to use `definterface` if you don't need
`gen-interface`'s extra functionality.  However, unlike `gen-class`,
`gen-interface` is simple to use.)


### Type hints

Type hints make a *huge* difference in speed, when used to avoid
reflection.  `(set! *warn-on-reflection* true)` will turn on reflection
compilation warnings.  I try to restrict type hints to use in and near
parameter lists, but sometimes I've needed to stick them in the middle
of a function's code.  Sometimes you can avoid type hints using
`definterface` or `gen-interface` (and maybe `defprotocol`--I'm not
sure).


### Cyclic dependencies

Clojure doesn't allow compile-time cyclic dependencies between
classes.  For example, without type hints, giving the `Students` and
`Student` classes their own namespaces (in separate source files, as
usual) worked fine.  These classes refer to each other's methods and
fields repeatedly, which is OK in Java.  It's also OK in Clojure without
type hints; Clojure figures it all out at runtime.  However, that was
very slow.  When you add in type hints to get rid of all reflection, you
end up with a cyclic dependency.  My solution was to define `Student`
using `defrecord` (or `deftype`) in the same namespace (and file) as
`Students`, also defining an interface for `Student`'s methods.  
(I had to place one of `Students`' methods *after* the `Student` class
definition in order to type hint it properly.)

Be careful:  Sometimes if you modify and recompile a single Clojure
source file, without recompiling the others, you might get away with a
cyclic dependency.  You've fooled the compiler by adding the dependency
of class `A` on class `B` after `B` was already compiled.  When `B` was
compiled, there was no cycle, so this works.  However, when you
recompile all of the files, you'll either get a cyclic dependency
exception, or you will not be able to find an ordering of
classes/namespaces in the `:aot` specification in project.clj that will
allow you to compile all of the source files.  (It's even possible to
get this effect with multiple classes in a single source file.
Apparently, the compiler can reference a previously compiled version 
while recompiling a source file.)


### Leiningen

The standard way to manage Clojure projects is with Leiningen, using
commands such as `lein compile`, `lein run`, and `lein repl`.  Among
other things, you can use Leiningeng's project.clj file to specify
libraries to be used, and the `:aot` keyword to specify that certain
source files will be compiled, and will be compile in a certain order
when needed.  (Some people use Maven, though.)


### Sequences, etc.

Any Java class that implements `java.util.Collection` can be used
with Clojure sequence/list-oriented functions: `map`, `reduce`,
`filter`, `vec`, etc.  This is very convenient--for example for working
with the contents of a `sim.util.Bag`.

Clojure allows you to create Java arrays of primitives using
functions such as `double-array`.  You can set values in arrays of
primitives efficiently using functions such as `aset-double`.
You can access the values efficiently using `aset` with type hints.

Many Clojure functions generate lazy sequences.  One has to be very
careful with these when dealing with mutable state.  Sometimes it's a
good idea to wrap a result in `doall` to cause a lazy sequence to be
realized immediately.
