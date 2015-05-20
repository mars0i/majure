(ns students.core
  (:import [students StudentsSimState Students AltState])
  (:gen-class))

(defn -main
  [& args]
  (let [alt-state (AltState.)
        students (repeatedly (.getNumStudents alt-state) #(Student.))]
  (StudentsSimState/main(make-array String 0))))
;; I don't know how to pass the students into the SimState.
;; you can't do it in an inherited constructor, and main just
;; calls the constructor with a class which calls a constructor with
;; a MakeSimState, so either way, you don't see the instance until
;; inside the doLoop.  i.e. everything has to be done in start().

;(defn -main
;  [& args]
;  (sim.engine.SimState/doLoop students.StudentsSimState (into-array String args))
;  (System/exit 0))
