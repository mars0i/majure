;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Student class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

;(set! *warn-on-reflection* true)

(ns students.Student
  (:import [students AltState]
           [sim.util Double2D MutableDouble2D]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network Edge])
  (:gen-class
    :name students.Student
    :implements [sim.engine.Steppable]   ; includes signature for the step method
    :methods [[getAgitation [] double]]
    :state state  ; returns instance agitation data
    :init init))  ; inits instance agitation data

(declare teacher-force-coord wander-force-coord collect-buddy-forces buddy-force-add)

(def ^:const +max-force+ 3.0)

;; In the tutorial, friendsClose and enemiesCloser are separate, and incremented 
;; separately, but they're treated identically, and there's no reason not to combined
;; into a var agitation.

(defn -init
  []
  [[] {:agitation (atom 0.0)}])

(defn -getAgitation
  "Returns sum of agitation values from instance state."
  [^students.Student this]
  @(:agitation (.state this)))

;; override super
(defn -toString
  [this]
  (str "[" (System/identityHashCode this) "] agitation: " (.getAgitation this)))

(defn -step
  "Clojure version of step function required by interface Steppable,
  based on code on p. 27 of the Mason Manual v18.
  Updates the location of this student based on locations of other
  students and \"buddy\" relationships to them.  First argument is
  an instance of Student; it will be passed implicity when called from
  Java, should be passed explicitly from Clojure code.  Second argument
  is an instance of students.Students, which extends sim.engine.SimState."
  [^students.Student this ^students.Students students]
  ;; Note that this code is functional until the last step.
  (let [^AltState alt-state (.gitAltState students)
        rng (.random students)
        ^Continuous2D yard (.gitYard alt-state)                  ; dimensions of the yard. a Continuous2D
        ^Double2D curr-loc (.getObjectLocation yard this) ; my location in the yard. a Double2D (Luke says might be more efficient to also store loc in agent)
        curr-x (.-x curr-loc)
        curr-y (.-y curr-loc)
        ;; individual forces: student's internal tendencies without regard to buddies:
        indiv-force-x (+ (wander-force-coord alt-state (.nextDouble rng)) ; 'add a bit of randomness' (p. 18 top,  p. 27 bottom)
                         (teacher-force-coord curr-x          ; 'add in a vector to the "teacher"' (p. 18 top,  p. 27 bottom)
                                              (.-width yard)
                                              alt-state)) 
        indiv-force-y (+ (wander-force-coord alt-state (.nextDouble rng))        ; see previous note
                         (teacher-force-coord curr-y          ; see previous note
                                              (.-height yard)
                                              alt-state))
        ;; buddy forces from attraction and repulsion to/from other students:
        [buddy-force-x buddy-force-y agitation] (collect-buddy-forces alt-state this)  ; 'Go through my buddies and determine how much I want to be near them' (for-loop, p. 27 middle)
        state (.state this)]

    ;; Now finish with all of the imperative code in one place:
    (reset! (:agitation state) agitation)   ; (p. 31: friendsClose, enemiesCloser)
    (.setObjectLocation yard this    ; modify location for me in yard in students (end of step(), p. 18 top, p. 27 bottom):
                        (Double2D. (+ curr-x indiv-force-x buddy-force-x)
                                   (+ curr-y indiv-force-y buddy-force-y)))))


(defn ^double wander-force-coord
  "Returns a student's random wandering force in one dimension (x or y).
  (See 'add a bit of randomness', p. 18 top, p. 27 bottom.)"
  [^AltState alt-state ^double rand-num]
  (* ^double (.getRandomMultiplier alt-state) (- rand-num 0.5)))

(defn ^double teacher-force-coord
  "Returns a student's force toward center of yard in one
  dimension (x or y).  coord is student's previous x or y coordinate.
  yard-dim is size of yard in the same dimension, so we can calculate 
  its center.  students is passed to get the global multiplier determining
  strength of tendency toward center.
  (See 'add in a vector to the \"teacher\"', p. 18 top, p. 27 bottom.)"
  [^double coord ^double yard-dim ^AltState alt-state]
  (* (.getForceToSchoolMultiplier alt-state)
     (- (* 0.5 yard-dim) coord)))


(defn collect-buddy-forces
  "Returns summed forces in x and y dimensions, and summed vector lengths of
  to attraction/repulsion vectors toward other students.  students is the 
  SimState object for this simulation.  me is the current Student.
  (In the MASON manual v. 18, see 'Go through my buddies and determine how much
  I want to be near them': for-loop, p. 27 middle.)"
  [^AltState alt-state ^students.Student me]
  (reduce (partial buddy-force-add (.gitYard alt-state) me)
          [0.0 0.0 0.0]   ; initial sums of x and y components, length
          ^Collection (.getEdges ^Network (.gitBuddies alt-state) me nil))) ; don't use .. since it doesn't seem to allow proper type hinting


;; I explored separating out the reduce-oriented summing aspect of this function
;; into a separate wrapper, but it made the code more difficult to understand.
;; Note this imperatively modifies the MutableDouble2D forceVector, but it's
;; a fresh instance and doesn't leave this function.  Thus from the point
;; of view of functions using this one, this is purely functional, and we
;; don't need to worry about laziness gotchas.
(defn buddy-force-add
  "Adds the force in x and y dimension between me and the student at the
  other end of edge to the forces from other students that have been
  summed so far.  Also adds values for agitation (formerly: friendsClose, 
  enemiesCloser).  yard is a Continuous2D from the SimState students.
  me is the current Student.  [acc-x, acc-y, acc-agit] contain the summed results of
  forces between me and other students calculated so far.  edge is the edge
  representing the force (to another student) that we are adding in this time.
  (In the MASON manual v. 18, see 'Go through my buddies and determine how much
  I want to be near them': for-loop, p. 27 middle.)"
  [^Continuous2D yard ^students.Student me [^double acc-x ^double acc-y ^double acc-agit] ^Edge edge]
  (let [buddiness (.info edge)
        my-loc (.getObjectLocation yard me)
        buddy-loc (.getObjectLocation yard (.getOtherNode edge me)) ; buddy = him in java
        forceVector (MutableDouble2D. (* buddiness (- (.-x buddy-loc) (.-x my-loc)))     ; inside the if/else in Java version
                                      (* buddiness (- (.-y buddy-loc) (.-y my-loc))))
        length (.length forceVector)]
    ;; Modify a forceVector to scale vector coords if necessary:
    (if (>= buddiness 0.0)
      (when (> length +max-force+) ; the further I am from her the more I want to go to her
        (.resize forceVector +max-force+))
      (if (> length +max-force+) ; the nearer I am to her the more I want to get away from her, up to a limit
        (.resize forceVector 0.0)
        (when (> length 0.0)
          (.resize forceVector (- +max-force+ length)))))
    ;; We're done using forceVector to resize vector; return a Clojure data structure:
    ;; We add resized vector data to what's been accumulated so far by reduce.
    [(+ acc-x (.-x forceVector)) ; x component of vector
     (+ acc-y (.-y forceVector)) ; y component of vector
     (+ acc-agit (.length forceVector))])) ; recompute length after possible resize (p. 31, friendsClose, enemiesCloser)
