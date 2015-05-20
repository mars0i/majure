AltStudents uses Student
StudentSimState uses AltStudents
Student contains step() which by definition is passed a SimState
and needs to read from StudentSimState
which contains the needed reference to AltState.
But what if I put a ref to AltState in each student?

student needs global state from altstate
simstate needs to call something that makes the students.

simstate doesn't need to know about students.
but it needs to know a way to get to students
since that's how each student gets info about the other students
unless each student knows how


I was thinking that I could just create a Collection of Student's in
core.clj, and then stick them into the SimState instance there.  Since
this is before we need efficiency, there'd be no need to identify the
collection as being a collection of students.  But the only way to put
things into the SimState is via its start() method.  So you have to put
the code there: Anything that's stuck into the SimState has to happen in
start().


note an alternative might be to not calculate positions and forces
object-orientedly in each student do it at the field level or something.

