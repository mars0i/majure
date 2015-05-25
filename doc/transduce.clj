
;; I thought this might be able to replace the call to reduce in
;; the definition of Student, and wondered if it would be faster.
;; It doesn't work, though.  I don't fully understand transducers yet.

;; EXPERIMENT
(defn transduce-helper [f]
  (fn 
    ([] (f))    
    ([coll] (f coll))    
    ([coll input] (f coll input))))

(def treduce (partial transduce transduce-helper))
