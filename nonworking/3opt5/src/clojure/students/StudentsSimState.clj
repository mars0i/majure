;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

;(set! *warn-on-reflection* true)
;(set! *unchecked-math* true)

(ns students.StudentsSimState
  (:import [students Student AltState] ; TemperingSteppable]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           [sim.util Double2D Interval]
           [sim.engine Steppable Schedule])
  (:gen-class
    :name students.Students
    :extends sim.engine.SimState  ; includes signature for the start() method
    :exposes-methods {start superStart} ; alias method start() in superclass. (Don't name it 'super-start'. Use a Java name.)
    ; NOTE some accessors named "git" instead of "get": JavaBean-named fields get pulled into inspector,
    ; and I want to prevent that in some cases.  (Nothing to do with git the version control tool.)
    :exposes {random {:get gitRandom}, schedule {:get gitSchedule}}
    :methods [[gitAltState [] students.AltState]
              [gitStudents [] java.util.Collection]]
    :constructors {[java.util.Collection] []}
    :state instanceState
    :init init-instance-state
    :main true)) 

;; The corresponding Java vars for these aren't declared static, but I'm going to
;; treat them that way since they're all uppercase, no code reassigns them.
(def ^:const +tempering-initial-random-multiplier+ 10.0)

(def ^:const +tempering-cut-down+ 0.99) ; note during experimentation also defined in another file

(defn -init-instance-state
  "students should be a java.util.Collection.
  Do not type-hint it with any more specificity."
  [students]
  (let [alt-state (AltState.)]
    (.setStudents students)
    [[] {:alt-state alt-state}]))

(declare find-other-student add-random-edge!)

;; You'd think that type hints wouldn't help here, since they're in the signature above:
(defn -gitAltState ^AltState [this] (:alt-state (.instanceState ^students.Students this)))

;; All of the initialization happens in start(), which is called before starting the Schedule to begin looping.
(defn -main
  [& args]
  (sim.engine.SimState/doLoop sim-state (into-array String args)
  (System/exit 0)

(defn -start
  [this]
  (.superStart this)
  (let [alt-state (.gitAltState this)
        yard (.gitYard alt-state)
        yard-width (.getWidth yard)
        yard-height (.getHeight yard)
        buddies (.gitBuddies alt-state)
        random (.gitRandom this)
        schedule (.gitSchedule this)
        that this] ; proxy below will capture 'this', but we want it to be able to refer to this this, too.
    (when (.isTempering alt-state)
      (.setRandomMultiplier alt-state +tempering-initial-random-multiplier+)
      ;; This is a hack to cause a global effect on every tick:
      ;; We make a special "agent" whose job it is to change the class global:
      (.scheduleRepeating schedule Schedule/EPOCH 1 
                          ;(TemperingSteppable.) ; gen-class version
                          (proxy [Steppable] []           ; proxy version
                            (step [^students.Students state]
                              (let [^AltState alt-state (.gitAltState state)]
                                (when (.isTempering alt-state)
                                  (.setRandomMultiplier alt-state 
                                                        (* (.getRandomMultiplier alt-state)
                                                           +tempering-cut-down+))))))
                          ))
    (.clear yard)
    (.clear buddies)))

(defn -gitStudents ^java.util.Collection [this] (students.Students/get-students))

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
