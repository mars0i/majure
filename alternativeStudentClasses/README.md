alternativeStudentClasses2step
====

Experiments using methods other than `gen-class` to define the
`Student` class(es), with `gen-interface` to define a new interface
that includes `getAgitation`.  cf. ../doc/whynot .

----------------------------

I tried to avoid a compile-time cyclic dependency (caused by adding type
hints) by splitting some code that would go in Students.clj into another
namespace, in AltState.clj.  However, there's still a cyclic dependency,
which Clojure doesn't like.  

As a result, you have to do a two step compilation hack:  Remove 
the type hint `^students.Students` from the `state` parameter to the `step` function in
Students.clj, then compile everything.  Then put the type hint back, and compile that one
file again.  This works around the cyclic dependency, and the type hint improves speed.

----------------------------

src/clojure/students contains different versions of the Student class source, using
different ways of defining classes in Clojure.

I copy different versions of Student into file Student.clj to experiment
with them, called "StudentUsing<ClassDefiner>.clj".  The file names make
it obvious which version is which.


----------------------------

### speed comparisons

Summary: `reify`, and `deftype` are fastest; `gen-class` is almost as
fast.  `proxy` is slower, and `defrecord` is slowest.

* With `gen-class`, I get 45% to 50% of the speed of Java, but only by
deleting the second type hint in the step function, then adding it back
and recompiling after the other classes are compiled.  Call this the
"two-step".

* The `proxy` version runs at about a 1/3 the speed of the Java version, but only by
doing the same two-step that I used with gen-class in 3opt2.  Without the type
hint on the second param to step, `proxy` is 5-10% of Java.

* The `reify` version is 20-22% of Java without an extra type hint on the
`Students` arg to step, but about 50% of Java with a type hint--which must
added using the two-step.  The wrinkle here is that because `reify` is
intimately influenced by the interface--whether it's `Steppable` or my
extension `SteppableStudent`--you can't put a type hint on the second param
of step.  The interface says that's a `SimState`, and you can't conflict with
the interface.  And I've see no way to use an interface that has `Students`
in its signature.  But it doesn't matter; it's easy to "cast" the `SimState`
argument to a `Students` in a `let` binding.

* The (nonmutable) `deftype` version is similar to the `reify` version:
21-22% of Java without an extra type hint, 52% of Java with a type hint
added via a let binding, again requiring the two-step.

* The `defrecord` version is about 10% of Java, or 12-15% of Java
adding a type hint via let as in the `reify` version.  That also
requires the two-step.  (Note that replacing the use of
`gen-interface` with the use of `defprotocol`, as in the other
directories with versions using `deftype`, does not seem to affect
speed.) **I don't understand why the `defrecord` version is so much
slower than the `deftype` version.**  In simple contexts with careful
testing, I've found them to be nearly equally fast.  `defrecord` does
provide additional functionality, but you'd think that what's common
to both would be compiled to (almost) the same underlying code. Maybe
the speed difference comes from `step` being called from Java? 
`deftype` *is* supposedly intended to be used for interop; `defrecord`
is not.  (I'd prefer to use `defrecord`, other things being equal.)

* The `deftype` version that uses a mutable field has exactly the same
speed profile as the nonmutable version.
