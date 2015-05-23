;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students and Student class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

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
              [getAgitationDistribution [] "[D"]]
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

(defn -init-instance-state
  [seed]
  [[seed] {:yard (Continuous2D. 1.0 100 100)
           :buddies (Network. false)
           :num-students (atom 50)
           :force-to-school-multiplier (atom 0.01)
           :random-multiplier (atom 0.1)
           :tempering (atom true)}])

;; You'd think that type hints on this wouldn't help here, since they're in the signature above, but they do.
;; It's not clear whether the other type hints help--not much, in any event.
(defn -gitYard ^Continuous2D [^students.Students this] (:yard (.instanceState this)))
(defn -gitBuddies ^Network [^students.Students this] (:buddies (.instanceState this)))
(defn -getNumStudents ^long [^students.Students this] @(:num-students (.instanceState this)))
(defn -setNumStudents [^students.Students this ^long newval] (when (> newval 0) (reset! (:num-students (.instanceState this)) newval)))
(defn -getForceToSchoolMultiplier ^double [^students.Students this] @(:force-to-school-multiplier (.instanceState this)))
(defn -setForceToSchoolMultiplier [^students.Students this ^double newval] (when (>= newval 0.0) (reset! (:force-to-school-multiplier (.instanceState this)) newval)))
(defn -getRandomMultiplier ^double [^students.Students this] @(:random-multiplier (.instanceState this)))
(defn -setRandomMultiplier [^students.Students this ^double newval] (when (>= newval 0.0) (reset! (:random-multiplier (.instanceState this)) newval)))
(defn -domRandomMultiplier ^Interval [^students.Students this] (Interval. 0.0 100.0))
(defn -isTempering ^java.lang.Boolean [^students.Students this] @(:tempering (.instanceState this)))
(defn -setTempering [^students.Students this ^java.lang.Boolean newval] (reset! (:random-multiplier (.instanceState this)) newval))

;; more type hints don't seem to help
(defn -getAgitationDistribution
  [^students.Students this]
  (double-array
    (map (fn [node] (.getAgitation node)) ; why can't I type hint node as Student?
         (.getAllNodes (.gitBuddies this)))))

;; The next several functions only run during initialization, so type hints wouldn't have much effect.

(defn -main
  [& args]
  (sim.engine.SimState/doLoop students.Students (into-array String args))
  (System/exit 0))

(defn -start
  [this]
  (.superStart this)
  (let [yard (.gitYard this)
        yard-width (.getWidth yard)
        yard-height (.getHeight yard)
        buddies (.gitBuddies this)
        random (.gitRandom this)     ; type hint doesn't help
        schedule (.gitSchedule this)
        students (repeatedly (.getNumStudents this) #(make-student))
        ^students.Students that this] ; proxy below will capture 'this', but we want it to be able to refer to this this, too.
    (when (.isTempering this)
      (.setRandomMultiplier this +tempering-initial-random-multiplier+)
      ;; This is a hack to cause a global effect on every tick: We make a special "agent" whose job it is to change the class global:
      (.scheduleRepeating schedule Schedule/EPOCH 1 
                          ;(students.TemperingSteppable.)      ; gen-class version
                          ;(proxy [Steppable] [] (step [state]  ; proxy version
                          (reify Steppable (step [this state] ; reify version
                              (when (.isTempering that)
                                (.setRandomMultiplier that (* (.getRandomMultiplier that)
                                                              +tempering-cut-down+)))))
                          ))
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

;; Note definterface and defprotocol don't allow inheritance, so we need gen-interface.
;(gen-interface
;  :name students.SteppableStudent
;  :extends [sim.engine.Steppable]
;  :methods [[getAgitation [] double]]) ;[step [students.Student] void] ; won't compile
;(import [students SteppableStudent])

(defprotocol StudentMethods (getAgitation [this]))
;(definterface StudentMethods (^double getAgitation []))

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


(defn collect-buddy-forces
  "Returns summed forces in x and y dimensions, and summed vector lengths of
  to attraction/repulsion vectors toward other students.  students is the 
  SimState object for this simulation.  me is the current Student.
  (In the MASON manual v. 18, see 'Go through my buddies and determine how much
  I want to be near them': for-loop, p. 27 middle.)"
  [^Students students ^Student me]
  (reduce (partial buddy-force-add (.gitYard students) me)
          [0.0 0.0 0.0]   ; initial sums of x and y components, length
          ^Collection (.getEdges ^Network (.gitBuddies students) me nil))) ; don't use .. since it doesn't seem to allow proper type hinting


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
  [^Continuous2D yard me [^double acc-x ^double acc-y ^double acc-agit] ^Edge edge]
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
