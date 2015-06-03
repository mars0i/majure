majure
====

## Experiments with Clojure + MASON

#### Experimental use of the MASON agent-based modeling library from Clojure

These examples follow the tutorial in chapter 2 of the MASON manual v.
18.

Clojure source code and text here is copyright 2015 by Marshall Abrams,
and is distributed under the Gnu General Public License version 3.0 as
specified in the file LICENSE, except where noted.  

The Java source code is copied from the the MASON manual v. 18, and is
by Sean Luke.  See the file MASONmanualLICENSE for its license.

MASON can be found at
[http://cs.gmu.edu/~eclab/projects/mason](http://cs.gmu.edu/~eclab/projects/mason).
Its license is in the file MASONLICENSE.

Clojure can be found at [http://clojure.org](http://clojure.org).  If
you want to run this code, you'll probably want to use
[Leiningen](http://leiningen.org).  (If you install Leiningen first, you
can use it to install Clojure.)

And Java ... is like [Elvis](http://www.mojonixon.com/lyrics/elvisiseverywhere.html).

--------------------------------------------

See **doc/ClojureMASONinteropTips.md** for tips on using Clojure with
MASON (and on Clojure-Java interop in general.)

--------------------------------------------

### directories:

#### justJava

Pure Java.  More or less copied from the manual.  I didn't copy all of the comments.

#### noOptimization

Pure Clojure, unoptimized, mainly using gen-class.  Very slow.

#### alternativeStudentClasses

Experiments using something other than gen-class for the Student class.
*Look at this version to compare different ways of defining classes.*
Still requires the odd "two-step" compilation process.  Optimized.

#### StudentGenInterface

Uses deftype for Students (deftype and reify were tied for fastest in
the tests in alternativeStudentClasses).  Optimized, and the normal
compilation process works.  i.e. `lein clean`, `lein compile` works.
This uses interface inheritance, using `gen-interface`, to add a method
to `Student`.

#### StudentDefprotocol

Following a suggestion by Tassilo Horn at
https://groups.google.com/d/msg/clojure/cQyqQxEjXDc/Rs0ncM5bHcoJ ,
replaces `gen-interface` with a simpler call to `defprotocol`, which is
then used to separate different methods by interfaces in `deftype
students`.  This is just as fast as (or a little faster than?)
StudentGenInterface.

NOTE: I went back and forth between using `defprotocol` and
`definterface` in this version and the versions below.  The syntax is
different, but there doesn't seem to be a difference in speed when used
as part of the definition of the `Student` class.  The directories below
are all named "...Defprotocol...", but some of them might use
`definterface`.

#### StudentDefprotocolWithSmallOpt

Based on StudentDefprotocol, but adds a small optimization to the
buddy forces calculation.  This seems to speed up simulations by 1-8%.

#### StudentDefprotocolVolatileMutable

Based on StudentDefprotocolWithSmallOpt but uses `deftype
InstanceState` with `:volatile-mutable` on fields that are supposed to
be mutable, rather rather using a map with atoms for those fields.  This
is 5% to 12% faster than StudentDefprotocolWithSmallOpt.
*SEE ALSO StudentDefprotocolUnsynchronizedMutable.*

NOTE: `:volatile-mutable` is supposed to be too dangerous for use by
anyone but an expert (which I am not).  However, I believe that it's
safe for single-threaded use outside of lazy contexts, which is how
I'm using it.

A drawback is that one has to define methods to access these fields, an
interface for them, and then wrapper methods--i.e. the methods on the
Students object.  So you have to give similar signatures three times!
If I can use deftype alone, without gen-class, that will ameliorate teh
problem.

(Also, I finally succeeded in type hinting node as Student in
getAgitationDistribution.)

#### StudentDefprotocolUnsynchronizedMutable

Exactly the same as StudentDefprotocolVolatileMutable, but using
`:unsynchronized-mutable` rather than `:volatile-mutable`.  About the
same speed as StudentDefprotocolVolatileMutable: Running them
simultaneously on a machine with sufficient cores, sometimes one wins,
sometimes the other does, but the difference is no more than 3%.

#### StudentDefprotocolDefrecord

Like StudentDefprotocolWithSmallOpt but uses a record rather than
a map in the gen-class state variable.  This doesn't seem to be
significantly faster than StudentDefprotocolWithSmallOpt.  The
versions using mutable `deftype` fields are faster.

#### StudentDefprotocolTypedArrays

Like StudentDefprotocolDefrecord in that it uses a record, but
rather than using atoms, it uses typed (Java) arrays to hold data that
must be mutable.  These arrays are stored in fields in the record.
Speed is similar to the atom versions, i.e. not faster than the versions
with mutable `deftype` fields.

#### StudentDefprotocolObjectArray

Unlike the preceding few versions, there is no `defrecord` or `deftype`
in the instance state system.  Instead, there's just a single `Object`
array.  This is at least as fast as the mutable `deftype` versions.
Maybe slightly faster?  Kind of a PITA, though, with index constants for
each variable, and needing hand-boxed numbers when using `aset` to avoid
reflection.  On the other hand, this version doesn't require four
signatures (count 'em!) for every accessor, as the mutable `deftype`
versions do.

#### javaClojureHybrids:

Contains a series of versions in which one Java class after another is replaced
with a Clojure class.  The Clojure versions mostly use `gen-class`.  I thought
`gen-class` was necessary for mixed Clojure-Java versions, which is probably not corret.

#### nonworking

Various experiments that didn't work, but that I'm keeping around for now.
