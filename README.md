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
