package students;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.field.network.*;

public class Student implements Steppable {

	// Note there is no constructor in this class.  When 'new Student()' is called
	// in Students.java, the default constructor that is added by the compiler is used.
	// See http://stackoverflow.com/questions/13773710/can-a-class-have-no-constructor .
	// (Note that Steppable is an interface, not a class, and all that it requires is that
	//  there be a step(SimState) function.)

	// AND NOTE that a Student HAS NO INSTANCE variables (at least at this point).
	// All of its properties are stored in the Students object.  Note that in step(),
	// which is passed the Students object, one of the first things is to get the location
	// of *this* student by getting that location from the Students object.  We use the
	// 'this' var for that, which is to say that a Student is simply used as an identity
	// or pointer within Students.  It's in effect just a pointer into a record in Students.
	// (Maybe at a later stage, S.Luke puts data into a Student, but you see that for
	// some purposes that's not part of the strategy; it's not essential.)


	public static final double MAX_FORCE = 3.0;

	double friendsClose = 0.0; // initially very close to my friends
	double enemiesCloser = 10.0; // WAY too close to my enemies
	public double getAgitation() {return friendsClose + enemiesCloser;}

	public String toString() {return "[" + System.identityHashCode(this) + "] agitation: " + getAgitation();}

	public void step(SimState state) {
		Students students = (Students) state; // I think the cast is to allow the compiler to accept Student member refs

		Continuous2D yard = students.yard;

		Double2D me = students.yard.getObjectLocation(this);
		MutableDouble2D sumForces = new MutableDouble2D();

		friendsClose = enemiesCloser = 0.0;

		MutableDouble2D forceVector = new MutableDouble2D();
		Bag out = students.buddies.getEdges(this, null); // second arg is the bag to check; in this case, a new bag of edges to/from this is created
		int len = out.size();

		for(int buddy = 0 ; buddy < len; buddy++) {
			Edge e = (Edge)(out.get(buddy));
			double buddiness = ((Double)(e.info)).doubleValue();
			Double2D him = students.yard.getObjectLocation(e.getOtherNode(this));

			if (buddiness >= 0) {
				forceVector.setTo((him.x - me.x) * buddiness, (him.y - me.y) * buddiness);
				if (forceVector.length() > MAX_FORCE)
					forceVector.resize(MAX_FORCE);
				friendsClose += forceVector.length();
			} else {
				forceVector.setTo((him.x - me.x) * buddiness, (him.y - me.y) * buddiness);
				if (forceVector.length() > MAX_FORCE)
					forceVector.resize(0.0);
				else if (forceVector.length() > 0)
					forceVector.resize(MAX_FORCE - forceVector.length());
				enemiesCloser += forceVector.length();
			}
			sumForces.addIn(forceVector);
		}

		sumForces.addIn(new Double2D((yard.width  * 0.5 - me.x) * students.forceToSchoolMultiplier,
		                             (yard.height * 0.5 - me.y) * students.forceToSchoolMultiplier));

		sumForces.addIn(new Double2D(students.randomMultiplier * (students.random.nextDouble() * 1.0 - 0.5),
		                             students.randomMultiplier * (students.random.nextDouble() * 1.0 - 0.5)));
		sumForces.addIn(me);
		students.yard.setObjectLocation(this, new Double2D(sumForces));
	}
}
