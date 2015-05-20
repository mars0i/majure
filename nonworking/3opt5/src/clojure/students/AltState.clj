;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; This class holds the data that was formerly in the Students class.
;;; Splitting this out avoids a cyclic dependency that was made it impossible
;;; to add some type hints.
;;; (However, it interferes with the model reporting in the GUI.)

;(set! *warn-on-reflection* true)
;(set! *unchecked-math* true)

(ns students.AltState
  (:import [students AltState]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           [sim.util Double2D Interval]
           [sim.engine Steppable Schedule])
  (:gen-class
    :name students.AltState
    ; NOTE some accessors named "git" instead of "get": JavaBean-named fields get pulled into inspector,
    ; and I want to prevent that in some cases.  (Nothing to do with git the version control tool.)
    :methods [[gitYard [] sim.field.continuous.Continuous2D]
              [gitBuddies [] sim.field.network.Network]
              [getNumStudents [] int]
              [setNumStudents [int] void]
              [getForceToSchoolMultiplier [] double]
              [setForceToSchoolMultiplier [double] void]
              [getRandomMultiplier [] double]
              [setRandomMultiplier [double] void]
              [domRandomMultiplier [] sim.util.Interval]
              [isTempering [] boolean]
              [setTempering [boolean] void]
              [getAgitationDistribution [] "[D"]]
    :constructors {[] []}
    :state instanceState
    :init init-instance-state))

;; The corresponding Java vars for these aren't declared static, but I'm going to
;; treat them that way since they're all uppercase, no code reassigns them.
(def ^:const +tempering-initial-random-multiplier+ 10.0)

(def ^:const +tempering-cut-down+ 0.99) ; note during experimentation also defined in another file

(defn -init-instance-state
  []
  [[] {:yard (Continuous2D. 1.0 100 100)
       :buddies (Network. false)
       :num-students (atom 50)
       :students (atom nil)
       :force-to-school-multiplier (atom 0.01)
       :random-multiplier (atom 0.1)
       :tempering (atom true)}])

;; You'd think that type hints wouldn't help here, since they're in the signature above:
(defn -gitYard [this] (:yard (.instanceState ^students.AltState this)))
(defn -gitBuddies [this] (:buddies (.instanceState ^students.AltState this)))
(defn -getNumStudents [this] @(:num-students (.instanceState ^students.AltState this)))
(defn -setNumStudents [this newval] (when (> newval 0) (reset! (:num-students (.instanceState ^students.AltState this)) newval)))
(defn -getStudents [this] @(:num-students (.instanceState ^students.AltState this)))
(defn -setStudents [this newval] (reset! (:num-students (.instanceState ^students.AltState this)) newval))
(defn -getForceToSchoolMultiplier [this] @(:force-to-school-multiplier (.instanceState ^students.AltState this)))
(defn -setForceToSchoolMultiplier [this newval] (when (>= newval 0.0) (reset! (:force-to-school-multiplier (.instanceState ^students.AltState this)) newval)))
(defn -getRandomMultiplier [this] @(:random-multiplier (.instanceState ^students.AltState this)))
(defn -setRandomMultiplier [this newval] (when (>= newval 0.0) (reset! (:random-multiplier (.instanceState ^students.AltState this)) newval)))
(defn -domRandomMultiplier [this] (Interval. 0.0 100.0))
(defn -isTempering [this] @(:tempering (.instanceState ^students.AltState this)))
(defn -setTempering [this newval] (reset! (:random-multiplier (.instanceState ^students.AltState this)) newval))

(defn -getAgitationDistribution
  [this]
  (double-array
    (map #(.getAgitation %)
         (.getAllNodes (.gitBuddies ^students.AltState this)))))
