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
    :extends sim.engine.SimState    ; includes signature for the start() method
    ;:constructors {[long] [long]}
    :methods [[getYard [] sim.field.continuous.Continuous2D]
              [getBuddies [] sim.field.network.Network]
              [getNumStudents [] int]
              [getForceToSchoolMultiplier [] double]
              [getRandomMultiplier [] double]]
    :main true
    :exposes-methods [start super-start]
    :state state
    :init init
    )) 

; re constructors: maybe I don't need to do anything?
;; is public Students(long seed) {super(seed);} auto-generated?
;; see also:
;http://stackoverflow.com/questions/18780071/clojure-multiple-constructors-using-gen-class

;(defn -init-yard [] [[] (Continuous2D. 1.0 100 100)])
;(defn -init-buddies [] [[] (Network. false)])

(defn -init
  []
  [[] {:yard (Continuous2D. 1.0 100 100)
       :buddies (Network. false)
       :num-students (atom 50)
       :force-to-school-multiplier (atom 0.01)
       :random-multiplier (atom 0.1)}])

;; I don't think Clojure can handle multiple instance vars.
;; Have to rewrite using classes to use accessors instead.  Also allows setting.
(defn -getYard [this] (:yard (.state this)))
(defn -getBuddies [this] (:buddies (.state this)))
(defn -getNumStudents [this] @(:num-students (.state this)))
(defn -getForceToSchoolMultiplier [this] @(:force-to-school-multiplier (.state this)))
(defn -getRandomMultiplier [this] @(:random-multiplier (.state this)))

(defn -start
  [this]
  ;(super-start) ; TODO not working
  (let [yard (.yard this)
        buddies (.buddies this)]
  ))


(defn -main 
  [args]
  ;(.doLoop Students.class args) ; TODO how the heck can I do *this* in Clojure??
                                 ; Students.class doesn't exist until this file is compiled!
                                 ; Note there is another version of doLoop that takes a
                                 ; MakeSimState (an interface, though) instead of a class.
  (.exit System 0))
