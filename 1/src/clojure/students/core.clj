(ns students.core
  (:import [students StudentsWithUI])
  (:gen-class))

(defn -main
  [& args]
  (StudentsWithUI/main(make-array String 0)))
