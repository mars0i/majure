;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students and Student class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.
;;; This version makes some state mutable by storing it in Java arrays.

;;; NOTE:
;;; In this version, the Students class is defined using gen-class in the namespace specification.
;;; The Student class is defined in the second half of the file using gen-interface and deftype.
;;; There's also an inner class to Students that may be defined with proxy or reify.

;(set! *warn-on-reflection* true)
;(set! *unchecked-math* true)

(ns students.Students
  (:import [students Students] ; needed for type hints below
           [sim.field.continuous Continuous2D]
           [sim.field.network Network Edge]
           [sim.util Double2D MutableDouble2D Interval]
           [sim.engine Steppable Schedule]
           [ec.util MersenneTwisterFast])
  (:gen-class
    :name students.Students
    :extends sim.engine.SimState  ; includes signature for the start() method
    :exposes-methods {start superStart} ; alias method start() in superclass. (Don't name it 'super-start'. Use a Java name.)
    ; NOTE some accessors named "git" instead of "get": JavaBean-named fields get pulled into inspector,
    ; and I want to prevent that in some cases.  (Nothing to do with git the version control tool.)
    :exposes {random {:get gitRandom}, schedule {:get gitSchedule}}
    :methods [[gitYard [] sim.field.continuous.Continuous2D]
              [gitBuddies [] sim.field.network.Network]
              [getNumStudents [] int]
              [setNumStudents [int] void]
              [getForceToSchoolMultiplier [] double]
              [setForceToSchoolMultiplier [double] void]
              [getRandomMultiplier [] double]
              [setRandomMultiplier [double] void]
              [domRandomMultiplier [] sim.util.Interval]
              [isTempering [] boolean]
              [setTempering [boolean] void]
              [getAgitationDistribution [] "[D"]] ; SEE end of file
    :state instanceState
    :init init-instance-state
    :main true)) 

(declare make-student teacher-force-coord wander-force-coord
         collect-buddy-forces buddy-force-add find-other-student add-random-edge!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Students CLASS METHODS

;; The corresponding Java vars for these aren't declared static, but I'm going to
;; treat them that way since they're all uppercase, no code reassigns them.
(def ^:const +tempering-initial-random-multiplier+ 10.0)

(def ^:const +tempering-cut-down+ 0.99) ; note during experimentation also defined in another file

;; Store instance state elements in a record.
;; We use Java arrays to store those that need to be mutable.
;; (The type names with "s" added are already taken.)

;; Indexes into arrays in the instance-state
(def ^:const +yard-idx+ 0)
(def ^:const +buddies-idx+ 1)
(def ^:const +num-students-idx+ 2)
(def ^:const +force-to-school-multiplier-idx+ 3)
(def ^:const +random-multiplier-idx+ 4)
(def ^:const +tempering-idx+ 5)

(defn -init-instance-state
  [seed]
  [[seed] (object-array [(Continuous2D. 1.0 100 100) ; yard
                         (Network. false)            ; buddies
                         50                          ; num-students
                         0.01                        ; force-to-school-multiplier
                         0.1                         ; random-multiplier
                         true])])                    ; tempering

;; You'd think that type hints on 'this' wouldn't help here, since they're in the signature above, but they do.
(defn -gitYard ^Continuous2D
  [^Students this]
  (:yard (.instanceState this)))

(defn -gitBuddies ^Network
  [^Students this]
  (:buddies (.instanceState this)))

(defn -getNumStudents
  ^long [^Students this]
  (aget ^longs (:longz (.instanceState this))
        +longz-num-students-idx+))

(defn -setNumStudents
  [^Students this ^long newval]
  (when (> newval 0)
    (aset-long (:longz (.instanceState this))
               +longz-num-students-idx+
               newval)))

(defn -getForceToSchoolMultiplier
  ^double [^Students this]
  (aget ^doubles (:doublez (.instanceState this))
        +doublez-force-to-school-multiplier-idx+))

(defn -setForceToSchoolMultiplier
  [^Students this ^double newval]
  (when (>= newval 0.0)
    (aset-double (:doublez (.instanceState this))
                 +doublez-force-to-school-multiplier-idx+
                 newval)))

(defn -getRandomMultiplier
  ^double [^Students this]
  (aget ^doubles (:doublez (.instanceState this))
        +doublez-random-multiplier-idx+))

(defn -setRandomMultiplier
  [^Students this ^double newval]
  (when (>= newval 0.0)
    (aset-double (:doublez (.instanceState this))
          +doublez-random-multiplier-idx+
          newval)))

;; "isTempering" rather than "getTempering" to tell the UI to handle it differently (using reflection).
(defn -isTempering
  ^java.lang.Boolean [^Students this]
  (aget ^booleans (:booleanz (.instanceState this))
        +booleanz-tempering-idx+))

(defn -setTempering
  [^Students this ^java.lang.Boolean newval]
  (aset-boolean (:booleanz (.instanceState this))
                +booleanz-tempering-idx+
                newval))

(defn -domRandomMultiplier
  ^Interval [^Students this]
  (Interval. 0.0 100.0))

;; -getAgitationDistribution: See end of file.


;; The next several functions only run during initialization, so type hints wouldn't have much effect.

(defn -main
  [& args]
  (sim.engine.SimState/doLoop students.Students (into-array String args))
  (System/exit 0))

(defn -start
  [^Students this]
  (.superStart this)
  (let [yard (.gitYard this)
        yard-width (.getWidth yard)
        yard-height (.getHeight yard)
        buddies (.gitBuddies this)
        random (.gitRandom this)     ; type hint doesn't help
        schedule (.gitSchedule this)
        students (repeatedly (.getNumStudents this) #(make-student))
        ^students.Students that this] ; In reify below, clarify we're not referring to its own 'this'.
    (when (.isTempering this)
      (.setRandomMultiplier this +tempering-initial-random-multiplier+)
      ;; Hack to cause a global effect every tick: Make an "agent" whose job it is to change the class global:
      (.scheduleRepeating schedule Schedule/EPOCH 1 
                          (reify Steppable
                            (step [_ _]
                              (when (.isTempering that)
                                (.setRandomMultiplier that (* (.getRandomMultiplier that)  ; replacing this with a dedicated scale multiplier method doesn't increase speed
                                                              +tempering-cut-down+)))))))
    (.clear yard)
    (.clear buddies)
    ;; first for-loop in Students.java--create students, add them to buddies:
    (doseq [student students
            :let [x-loc (+ (* 0.5 yard-width)  (.nextDouble random) -0.5)
                  y-loc (+ (* 0.5 yard-height) (.nextDouble random) -0.5)]]
      (.setObjectLocation yard student (Double2D. x-loc y-loc))
      (.addNode buddies student)
      (.scheduleRepeating schedule student))
    ;; second for-loop in Students.java--create links between students:
    (doseq [student students]
      (add-random-edge! buddies random  1.0 students student)
      (add-random-edge! buddies random -1.0 students student))))

(defn add-random-edge!
  "Adds an edge with random absolute weight, with sign, to buddies 
  from student to a randomly chosen element in students"
  [buddies random sign students student]
  (let [studentB (find-other-student random students student)
        buddiness (.nextDouble random)]
    (.addEdge buddies student studentB (Double. (* buddiness sign)))))


;; Is there a simpler method?
(defn find-other-student
  "Returns a random student, not identical? to student, from students."
  [random students student]
  (let [num-students (count students)
        other-student (fn []
                        (let [other (nth students (.nextInt random num-students))]
                          (when (not (identical? student other))
                            other)))]
    (some identity (repeatedly other-student))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; THE Student CLASS
;; Extends Steppable, adding Student-specific methods.

;; Here type hints matter; the step() function is called for each Student on each timestep.

(def ^:const +max-force+ 3.0)

;(defprotocol StudentMethods (getAgitation [this]))
(definterface StudentMethods (^double getAgitation []))

(deftype Student [agitation]
  Steppable
  (step [this state]
    ;; Note that this code is functional until the few last steps.
    (let [students ^students.Students state ; We can't use this type hint in the params--conflicts with interface.
          rng (.random students) ; type hint doesn't help
          ^Continuous2D yard (.gitYard students)                  ; dimensions of the yard. a Continuous2D
          ^Double2D curr-loc (.getObjectLocation yard this) ; my location in the yard. a Double2D (Luke says might be more efficient to also store loc in agent)
          curr-x (.-x curr-loc)
          curr-y (.-y curr-loc)
          ;; individual forces: student's internal tendencies without regard to buddies:
          indiv-force-x (+ (wander-force-coord students (.nextDouble rng)) ; 'add a bit of randomness' (p. 18 top,  p. 27 bottom)
                           (teacher-force-coord curr-x          ; 'add in a vector to the "teacher"' (p. 18 top,  p. 27 bottom)
                                                (.-width yard)
                                                students)) 
          indiv-force-y (+ (wander-force-coord students (.nextDouble rng))        ; see previous note
                           (teacher-force-coord curr-y          ; see previous note
                                                (.-height yard)
                                                students))
          ;; buddy forces from attraction and repulsion to/from other students:
          [buddy-force-x buddy-force-y agit] (collect-buddy-forces students this)]  ; 'Go through my buddies and determine how much I want to be near them' (for-loop, p. 27 middle)
      ;; Now finish with all of the imperative code in one place:
      (reset! (.agitation this) agit)   ; (p. 31: friendsClose, enemiesCloser)
      (.setObjectLocation yard this    ; modify location for me in yard in students (end of step(), p. 18 top, p. 27 bottom):
                          (Double2D. (+ curr-x indiv-force-x buddy-force-x)
                                     (+ curr-y indiv-force-y buddy-force-y)))))
  (toString [this] (str "[" (System/identityHashCode this) "] agitation: " (.getAgitation this)))

  StudentMethods
  (getAgitation [this] @(.agitation this)))


(defn make-student [] (Student. (atom 0.0)))


(defn ^double wander-force-coord
  "Returns a student's random wandering force in one dimension (x or y).
  (See 'add a bit of randomness', p. 18 top, p. 27 bottom.)"
  [^Students students ^double rand-num]
  (* ^double (.getRandomMultiplier students) (- rand-num 0.5)))

(defn ^double teacher-force-coord
  "Returns a student's force toward center of yard in one
  dimension (x or y).  coord is student's previous x or y coordinate.
  yard-dim is size of yard in the same dimension, so we can calculate 
  its center.  students is passed to get the global multiplier determining
  strength of tendency toward center.
  (See 'add in a vector to the \"teacher\"', p. 18 top, p. 27 bottom.)"
  [^double coord ^double yard-dim ^Students students]
  (* (.getForceToSchoolMultiplier students)
     (- (* 0.5 yard-dim) coord)))


;; TODO: ? Rather than processing edges, process nodes at the other end of edges.  Then we don't have to pass me.
;; Question: Why is that better? 
;;
;; This version gets me's location once, and passes it to buddy-force-add,
;; rather than letting buddy-force-add get the location over and over again.
(defn collect-buddy-forces
  "Returns summed forces in x and y dimensions, and summed vector lengths of
  to attraction/repulsion vectors toward other students.  students is the 
  SimState object for this simulation.  me is the current Student.
  (In the MASON manual v. 18, see 'Go through my buddies and determine how much
  I want to be near them': for-loop, p. 27 middle.)"
  [^Students students ^Student me]
  (let [yard (.gitYard students)]
    (reduce (partial buddy-force-add yard me (.getObjectLocation yard me))
            [0.0 0.0 0.0]   ; initial sums of x and y components, length
            (.getEdges ^Network (.gitBuddies students) me nil)))) ; don't use .. since it doesn't seem to allow proper type hinting


;; This version designed to work with new version of collect-buddy-forces above.
;;
;; (I explored separating out the reduce-oriented summing aspect of this function
;; into a separate wrapper--i.e. in theory it ought to be possible to separate the
;; the addition to the accumulated quantities from the rest of the calculation--but 
;; it made the code more difficult to understand.)
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
  [^Continuous2D yard me ^Double2D my-loc [^double acc-x ^double acc-y ^double acc-agit] ^Edge edge]
  (let [buddiness (.info edge)
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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Students (not Student) METHOD
;; We put it here so that we can type hint node as a Student
(defn -getAgitationDistribution
  [^students.Students this]
  (double-array
    (map (fn [^Student node] (.getAgitation node)) ; why can't I type hint node as Student?
         (.getAllNodes (.gitBuddies this)))))
