;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;(set! *warn-on-reflection* true)

(ns students.TemperingSteppable
  (:import [students Students]
           [sim.engine Steppable])
  (:gen-class
    :name students.TemperingSteppable
    :implements [sim.engine.Steppable]))

(def ^:const +tempering-cut-down+ 0.99) ; note during experimentation also defined in another file

(defn -step
  [^students.TemperingSteppable this ^students.Students state]
    (when (.isTempering state)
      (.setRandomMultiplier state (* (.getRandomMultiplier state)
                                     +tempering-cut-down+))))
