;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

(ns students.Students
  (:import [students Student]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           [sim.util Double2D])
  (:gen-class
    :name students.Students
    :extends sim.engine.SimState  ; includes signature for the start() method
    :exposes {random {:get getRandom}, schedule {:get getSchedule}}  ; make accessors for fields in superclass
    :exposes-methods {start superStart} ; alias method start() in superclass. (Don't name it 'super-start'. Use a Java name.)
    :methods [[getYard [] sim.field.continuous.Continuous2D]
              [getBuddies [] sim.field.network.Network]
              [getNumStudents [] int]
              [getForceToSchoolMultiplier [] double]
              [getRandomMultiplier [] double]]
    :state instanceState
    :init init-instance-state
    :main true)) 


(defn -init-instance-state
  [seed]
  [[seed] {:yard (Continuous2D. 1.0 100 100)
           :buddies (Network. false)
           :num-students (atom 50)
           :force-to-school-multiplier (atom 0.01)
           :random-multiplier (atom 0.1)}])

(defn -getYard [this] (:yard (.instanceState this)))
(defn -getBuddies [this] (:buddies (.instanceState this)))
(defn -getNumStudents [this] @(:num-students (.instanceState this)))
(defn -getForceToSchoolMultiplier [this] @(:force-to-school-multiplier (.instanceState this)))
(defn -getRandomMultiplier [this] @(:random-multiplier (.instanceState this)))


(declare find-other-student add-random-edge!)


(defn -main
  [& args]
  (sim.engine.SimState/doLoop students.Students (into-array String args))
  (System/exit 0))


(defn -start
  [this]
  (.superStart this)
  (let [yard (.getYard this)
        yard-width (.getWidth yard)
        yard-height (.getHeight yard)
        buddies (.getBuddies this)
        random (.getRandom this)
        schedule (.getSchedule this)
        students (repeatedly (.getNumStudents this) #(Student.))]
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
