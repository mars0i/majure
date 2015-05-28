majure
====

## Clojure + MASON agent-based modeling library

#### Experimental use of MASON from Clojure

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

### directories:

#### 0plus:

Pure Java.  More or less copied from the manual.  I didn't copy all of the comments.

#### 3plus:

Pure Clojure, unoptimized, mainly using gen-class.  Very slow.

#### 3opt2:

Optimized, mainly using gen-class, but a funny "two-step"
compilation process is required.  See notes in that directory and in
doc.

#### 3opt6:

Non-working experiments with methods other than gen-class.

#### 3opt7:

Experiments using something other than gen-class for the Student class.
Still requires the odd "two-step" compilation process.  Optimized.

#### 3opt8:

Uses deftype for Students (deftype and reify were tied for fastest).
Optimized, and the normal compilation process works.  i.e. `lein clean`,
`lein compile` works.

#### 3opt9:

Following a suggestion by Tassilo Horn at
https://groups.google.com/d/msg/clojure/cQyqQxEjXDc/Rs0ncM5bHcoJ ,
replaces `gen-interface` with a simpler call to `defprotocol`, which is
then used to separate different methods by interfaces in `deftype
students`.  This is just as fast as (or a little faster than?) 3opt8.

#### 3opt10

Based on 3opt9, but adds a small optimization to the buddy forces
calculation.  This seems to speed up simulations by 1-8%.

#### 3opt11

Based on 3opt10, but uses `deftype InstanceState` with
`:volatile-mutable` on fields that are supposed to be mutable, rather
rather using a map with atoms for those fields.  This is 5% to 12%
faster than 3opt10.

NOTE: `:volatile-mutable` is supposed to be too dangerous for use by
anyone but an expert (which I am not).  However, I believe that it's
safe for single-threaded use outside of lazy contexts, which is how
I'm using it.  But I need to investigate further to be sure.

A drawback is that one has to define methods to access these fields, an
interface for them, and then wrapper methods--i.e. the methods on the
Students object.  So you have to give similar signatures three times!
If I can use deftype alone, without gen-class, that will ameliorate teh
problem.

(Also, I finally succeeded in type hinting node as Student in
getAgitationDistribution.)

#### 3opt12

Like 3opt10, but uses a record rather than a map in the gen-class state
variable.  This doesn't seem to be significantly faster than 3opt10.
3opt11 is faster.

#### 3opt13

Like 3opt12, in that it uses a record, but rather than using atoms as in
3opt12 and 3opt10 and below, it uses typed (Java) arrays to hold data
that must be mutable.  These arrays are stored in fields in the record.
Speed is similar to the atoms versions, i.e.  not faster than 3opt11
(which is the one that uses deftype with :volatile-mutable).

#### 3opt14

Unlike the preceding few versions, there is no `defrecord` or `deftype`
in the instance state system.  Instead, there's just a single `Object`
array.  This is at least as fast as 3opt11, the mutable `deftype`
version.  Maybe slightly faster?  Kind of a PITA, though, with index
constants for each variable, and hand-boxing numbers when using `aset`.
(On the other hand, it doesn't require four--count 'em!--signatures for
every accessor, as the 3opt11 does.)
