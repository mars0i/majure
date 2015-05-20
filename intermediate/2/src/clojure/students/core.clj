(ns students.core
  (:gen-class :main true))

(defn -main
  [& args]
  (sim.engine.SimState/doLoop students.Students (into-array String args))
  (System/exit 0))
