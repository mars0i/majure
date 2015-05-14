;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;(set! *warn-on-reflection* true)

(ns students.TemperingSteppable
  (:import [sim.engine Steppable]
           [students AltState])
  (:gen-class
    :name students.TemperingSteppable
    :implements [sim.engine.Steppable]))


(def ^:const +tempering-cut-down+ 0.99) ; note during experimentation also defined in another file

(defn -step
  [^students.TemperingSteppable this state]
  (let [^AltState alt-state (.gitAltState state)]
    (when (.isTempering alt-state)
      (.setRandomMultiplier alt-state (* (.getRandomMultiplier alt-state)
                                         +tempering-cut-down+)))))
