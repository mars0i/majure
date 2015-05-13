;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

(ns students.Students
  (:import [students Student]
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

;; The corresponding Java vars for these aren't declared static, but I'm going to
;; treat them that way since they're all uppercase, no code reassigns them.
(def ^:const +tempering-cut-down+ 0.99)
(def ^:const +tempering-initial-random-multiplier+ 10.0)

(defn -init-instance-state
  [seed]
  [[seed] {:yard (Continuous2D. 1.0 100 100)
           :buddies (Network. false)
           :num-students (atom 50)
           :force-to-school-multiplier (atom 0.01)
           :random-multiplier (atom 0.1)
           :tempering (atom true)}])

(declare find-other-student add-random-edge!)

(defn -gitYard [this] (:yard (.instanceState this)))
(defn -gitBuddies [this] (:buddies (.instanceState this)))
(defn -getNumStudents [this] @(:num-students (.instanceState this)))
(defn -setNumStudents [this newval] (when (> newval 0) (reset! (:num-students (.instanceState this)) newval)))
(defn -getForceToSchoolMultiplier [this] @(:force-to-school-multiplier (.instanceState this)))
(defn -setForceToSchoolMultiplier [this newval] (when (>= newval 0.0) (reset! (:force-to-school-multiplier (.instanceState this)) newval)))
(defn -getRandomMultiplier [this] @(:random-multiplier (.instanceState this)))
(defn -setRandomMultiplier [this newval] (when (>= newval 0.0) (reset! (:random-multiplier (.instanceState this)) newval)))
(defn -domRandomMultiplier [this] (Interval. 0.0 100.0))
(defn -isTempering [this] @(:tempering (.instanceState this)))
(defn -setTemering [this newval] (reset! (:random-multiplier (.instanceState this)) newval))

(defn -getAgitationDistribution
  [this]
  (double-array
    (map #(.getAgitation %)
         (.getAllNodes (.gitBuddies this)))))

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
        random (.gitRandom this)
        schedule (.gitSchedule this)
        students (repeatedly (.getNumStudents this) #(Student.))
        that this] ; proxy below will capture 'this', but we want it to be able to refer to this this, too.
    (when (.isTempering this)
      (.setRandomMultiplier this +tempering-initial-random-multiplier+)
      ;; This is a hack to cause a global effect on every tick:
      ;; We make a special "agent" whose job it is to change the class global:
      (.scheduleRepeating schedule Schedule/EPOCH 1
                          (proxy [Steppable] []
                            (step [state]
                              (when (.isTempering that)
                                (.setRandomMultiplier that 
                                                      (* (.getRandomMultiplier that)
                                                         +tempering-cut-down+)))))))
    (.clear yard)
    (.clear buddies)
    ;; first for-loop in Students.java--create students:
    (doseq [student students
            :let[x-loc (+ (* 0.5 yard-width)  (.nextDouble random) -0.5)
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
