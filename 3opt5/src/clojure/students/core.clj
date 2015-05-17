(ns students.core
  (:import [students StudentsSimState])
  (:gen-class))

(defn -main
  [& args]
  ;; add something that will pass a Collection of students to StudentsSimState here.
  (StudentsSimState/main(make-array String 0)))

;(defn -main
;  [& args]
;  (sim.engine.SimState/doLoop students.StudentsSimState (into-array String args))
;  (System/exit 0))
