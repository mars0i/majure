tips.md
====

Things I've learned that are relevant to using MASON with Clojure from
experimenting with different ways to implement the Students example in
Clojure, and other tips.

These notes are not intended to be self-explanatory to someone who's
unfamiliar with Clojure, or unfamiliar with MASON, or even unfamiliar
with Java.  (But you can, of course, nevertheless use them to figure out
what you want to learn more about if you're unfamiliar with something
I mention.)


### Students

There are four classes defined in the Students simulation:

* `Students`, which extends `SimState`
* `Student`, which implements `Steppable`
* An inner FIXME
* `StudentsUI`




### Classes

Note that there are five ways to make classes in Clojure:

* `defrecord`
* `deftype`
* `reify`
* `proxy`
* `gen-class`

(The *3opt7* version of my Students-in-Clojure program contains
files illustrating alternative ways of defining the `Student` class
using each of these options.  See docs/3opt7.md for discussion of their
speed differences.)

`defrecord` is commonplace in Clojure so, other things being equal, it
should perhaps be preferred.  Other things are not always equal, though.
`deftype` is similar, and in the 3opt7 tests, it was a lot faster than
`defrecord`.  (I'm a little bit surprised at that.  I wonder if I didn't
use `defrecord` in the best possible way.)

Only `deftype` allows *multiple* mutable fields, using the
`:unsyncronized-mutable` or `:volatile-mutable` keywords.

Only `proxy` and `gen-class` allow you to inherit from an existing class
such as `SimState`.  The other macros only allow you to implement
interfaces.  However, `proxy` is potentially the slowest of the five
methods, and it's more limited than `gen-class` in many ways.  I think
that `proxy` is unlikely to be a good choice for use with MASON if you
want optimal speed.  You can use `reify` instead, for example, to create
a Clojure equivalent of an inner class, if the class doesn't need to
inherit.  Neither `reify` nor `proxy` has built-in ways to store state,
though you may be able to use a closure, perhaps using atoms or some
other reference type, to associate state with a `reify` or `proxy`
object.

`gen-class` is most flexible of the ways to create Clojure classes.
I use it to subclass `SimState`.  I don't think `proxy` can do
everything needed for this use.

However, `gen-class` only allows a single mutable field.

To get an effect like multiple mutable fields with `gen-class`, or
with `defrecord`, you can use one of Clojure's reference types.  For
example, to have mutable state with `gen-class`, you can store Clojure
atoms in a Clojure `defrecord` objects that's stored in the state
variable, or store a `deftype` object with mutable fields, in the
state variable.  There are other options, but those seem the best. 
`deftype` with mutable fields was a little bit faster than `defrecord`
with atoms; I used `deftype` to go from 50% of Java speed to 60% of
Java speed.  (Using `deftype` for this purpose is very verbose,
though--I ended up with four similar signatures for each field.  If I
find I really need this extra speed, maybe I'll write some kind of
macro to generate the code.  This is a little tricky to do with type
hints.)

There is usually no problem with using fields inherited from a
superclass defined in Java from within the same Clojure class definition
(probably: the same namespace).  They can be accessed as if they were Java
methods called from Clojure, i.e. with expressions such as `(.random
students)` to get the random number generator defined in the SimState
superclass of the `Students`.  You can modify these instance variables
using `set!`.


### Interfaces

The are three ways to define interfaces:

* `defprotocol`
* `definterface`
* `gen-interface`

For use with MASON, it probably doesn't matter which you use--they seem
to be equally fast--and `defprotocol` is potentially more convenient.


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
allow you to compile all of the source files.


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
functions such as `long-array`.  You can set values in arrays of
primitives efficiently using functions such as `aset-long`.
You can access the values efficiently using `aset` with type hints.

Many Clojure functions generate lazy sequences.  One has to be very
careful with these when dealing with mutable state.  Sometimes it's a
good idea to wrap a result in `doall` to cause a lazy sequence to be
realized immediately.
