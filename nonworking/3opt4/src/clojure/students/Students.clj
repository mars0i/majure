;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Students class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

;(set! *warn-on-reflection* true)
;(set! *unchecked-math* true)

(ns students.Students
  (:import [students Student]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           [sim.util Double2D]))

(defn make-students
  [rng schedule yard n]
  (let [students (repeatedly n #(students.Student/Student.))]

    ;; first for-loop in Students.java--create students, add them to buddies:
    (doseq [student students
            :let [x-loc (+ (* 0.5 yard-width)  (.nextDouble rng) -0.5)
                  y-loc (+ (* 0.5 yard-height) (.nextDouble rng) -0.5)]]
      (.setObjectLocation yard student (Double2D. x-loc y-loc))
      (.addNode buddies student)
      (.scheduleRepeating schedule student))

    ;; second for-loop in Students.java--create links between students:
    (doseq [student students]
      (add-random-edge! buddies random  1.0 students student)
      (add-random-edge! buddies random -1.0 students student))

    students))
