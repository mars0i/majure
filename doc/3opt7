3opt7 versions
====

I copy different versions of Student into file Student.clj to experiment with them.

* Recall that in 3opt2, which uses gen-class, I get 45% to 50% of the speed of Java,
but only by deleting the second type hint in the step function, then adding it back
and recompiling after the other classes are compiled.  Call this the "two-step".

* The proxy version runs at about a 1/3 the speed of the Java version, but only by
doing the same two-step that I used with gen-class in 3opt2.  Without the type
hint on the second param to step, proxy is 5-10% of Java.

* The reify version is 20-22% of Java without an extra type hint on the
Students arg to step, but about 50% of Java with a type hint--which must
added using the two-step.  The wrinkle here is that because reify is
intimately influenced by the interface--whether it's Steppable or my
extension SteppableStudent--you can't put a type hint on the second param
of step.  The interface says that's a SimState, and you can't conflict with
the interface.  And I've see no way to use an interface that has Students
in its signature.  But it doesn't matter; it's easy to "cast" the SimState
argument to a Students in a let binding.

* The defrecord version is about 10% of Java, or 12-15% of Java adding a type
hint via let as in the reify version.  That also requires the two-step.

* The (nonmutable) deftype version is similar to the reify version:
21-22% of Java without an extra type hint, 52% of Java with a type hint
added via a let binding, again requiring the two-step.

* The deftype version that uses a mutable field has exactly the same
speed profile as the nonmutable version.  (So since mutable fields are
considered dangerous, I should not use this version.)
