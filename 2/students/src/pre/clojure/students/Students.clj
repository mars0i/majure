;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Clojure version of the Student class described in the tutorial in
;;; chapter 2 of the Mason Manual v18, by Sean Luke.

;;; Conventions:
;;; I use 'me' to refer to this instance of Student and generally don't pass
;;; it as the first argument in functions intended to be used here.  I use
;;; 'this' and pass it as the first argument only for methods intended to be
;;; called from other classes.

(ns students.Students
  (:import ;[sim.engine SimState]
           ;[sim.util ]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           )
  (:gen-class
    :name students.Students
    :extends sim.engine.SimState    ; includes signature for the start method
    ;:constructors {[long] [long]}
    :methods [[main [String] void] ; apart from the name, symbols must be void or class names
              ;[yard [] sim.field.continuous.Continuous2D]
              [numStudents [] int]
              [forceToSchoolMultiplier [] double]
              [randomMultiplier [] double]
              [buddies [] sim.field.network.Network]]
    ;:main true
    ;:exposes 
    ;:state state  ; returns instance data
    ;:init init
    :state yard
    :init init-yard
    ;:state buddies
    ;:init init-buddies
    )) 

; re constructors: maybe I don't need to do anything?
;; is public Students(long seed) {super(seed);} auto-generated?
;; see also:
;http://stackoverflow.com/questions/18780071/clojure-multiple-constructors-using-gen-class

(defn -init-yard [] [[] (Continuous2D. 1.0 100 100)])
;(defn -init-buddies [] [[] (Network. false)])


;(defn -init
;  []
;  [[] {:yard (Continuous2D. 1.0 100 100)
;       :num-students 50
;       :force-to-school-multiplier 0.01
;       :random-multiplier 0.1
;       :buddies (Network. false)}])
;
;;; I don't think this will work.  I'm trying to replace instance vars
;;; with functions, but StudentsWithUI expects public vars.
;(defn -yard [this] (:yard (.state this)))
;(defn -numStudents [this] (:num-students (.state this)))
;(defn -forceToSchoolMultiplier [this] (:force-to-school-multiplier (.state this)))
;(defn -randomMultiplier [this] (:random-multiplier (.state this)))
;(defn -buddies [this] (:buddies (.state this)))

(defn -start
  []
  )

(defn -main 
  [args]
  )
