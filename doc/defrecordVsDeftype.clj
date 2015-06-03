(ns expt.defrecordVsDeftype
  (:use [criterium.core :only [bench]])
  (:gen-class))

(defprotocol P (incx2y [this]) (decy2x [this]))

(defrecord R [x y]
  P
  (incx2y [this] (reset! x (inc @y)))
  (decy2x [this] (reset! y (dec @x))))

(deftype T [x y]
  P
  (incx2y [this] (reset! x (inc @y)))
  (decy2x [this] (reset! y (dec @x))))

;(def r (R. (atom 2) (atom 1)))
;(def t (T. (atom 2) (atom 1)))

(defn testem []
  (let [r (R. (atom 2) (atom 1))
        t (T. (atom 2) (atom 1))]
    (println "defrecord:") 
    (bench (def _ (dotimes [_ 50000000] (incx2y r) (decy2x r) r)))
    (println "deftype:") 
    (bench (def _ (dotimes [_ 50000000] (incx2y t) (decy2x t) t))))
  (println "Done."))

(defn -main
  [& args]
  (testem))
