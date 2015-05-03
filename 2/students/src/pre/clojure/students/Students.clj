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
  (:import [students Student]
           [sim.field.continuous Continuous2D]
           [sim.field.network Network]
           [sim.util Double2D])
  (:gen-class
    :name students.Students
    :extends sim.engine.SimState  ; includes signature for the start() method
    :exposes {random {:get getRandom}, schedule {:get getSchedule}}  ; make accessors for fields in superclass
    :exposes-methods {start super-start} ; alias method start() in superclass
    :methods [[getYard [] sim.field.continuous.Continuous2D]
              [getBuddies [] sim.field.network.Network]
              [getNumStudents [] int]
              [getForceToSchoolMultiplier [] double]
              [getRandomMultiplier [] double]]
    :state state
    :init init
    )) 
    ;:main true
    ;:constructors {[long] [long]}

;; re constructors: maybe I don't need to do anything?
;; is public Students(long seed) {super(seed);} auto-generated?
;; cf http://stackoverflow.com/questions/18780071/clojure-multiple-constructors-using-gen-class

(defn -init
  []
  [[] {:yard (Continuous2D. 1.0 100 100)
       :buddies (Network. false)
       :num-students (atom 50)
       :force-to-school-multiplier (atom 0.01)
       :random-multiplier (atom 0.1)}])

;; I don't think Clojure can handle multiple instance vars.
(defn -getYard [this] (:yard (.state this)))
(defn -getBuddies [this] (:buddies (.state this)))
(defn -getNumStudents [this] @(:num-students (.state this)))
(defn -getForceToSchoolMultiplier [this] @(:force-to-school-multiplier (.state this)))
(defn -getRandomMultiplier [this] @(:random-multiplier (.state this)))

(defn -start
  [this]
  (.super-start this)

  (let [yard (.yard this)
        yard-width (.getWidth yard)
        yard-height (.getHeight yard)
        buddies (.buddies this)
        random (.getRandom this)
        schedule (.getSchedule this)]

    (.clear yard)
    (.clear buddies)

    ;; first for-loop in Students.java
    (dotimes [i (.getNumStudents this)]
      (let [student (Student.)
            x-loc (+ (* 0.5 yard-width)  (.nextDouble random) -0.5)
            y-loc (+ (* 0.5 yard-height) (.nextDouble random) -0.5)]

        (.setObjectLocation yard student (Double2D. x-loc y-loc))
        (.addNode buddies student)
        (.scheduleRepeating schedule student)))

    (let [students (.getAllNodes buddies) ; returns a Bag, which is a Collection
          num-students (count students)]

      ;; second for-loop in Students.java
      ;; first choose a different student
      (doseq [student students
              :let [randint (.nextInt random num-students)]
              :when 

      (let [studentB (nth students 

      )))

  ))

;; Re MAIN
;; main() as written in Students.java can't be duplicated in Clojure
;; because that would require a two-pass compiler.
;; 
;; I may be able to def main using the MakeSimState version of doLoop, but
;; that requires implementing interface MakeSimState.  Or maybe put doLoop
;; in a separate class from this one, which must implement SimState as well.
;; Or write my own loop?  But I'll put this off, since this main() is only
;; needed in order to run headless.
;;
;(defn -main 
;  [args]
;  ;(.doLoop Students.class args) ; TODO how the heck can I do *this* in Clojure??
;                                 ; Students.class doesn't exist until this file is compiled!
;                                 ; Note there is another version of doLoop that takes a
;                                 ; MakeSimState (an interface, though) instead of a class.
;  (.exit System 0))
