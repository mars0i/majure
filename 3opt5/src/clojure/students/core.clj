(ns students.core
  (:import [students StudentsSimState])
  (:gen-class))

(defn -main
  [& args]
  (StudentsSimState/main(make-array String 0)))

;(defn -main
;  [& args]
;  (sim.engine.SimState/doLoop students.StudentsSimState (into-array String args))
;  (System/exit 0))
