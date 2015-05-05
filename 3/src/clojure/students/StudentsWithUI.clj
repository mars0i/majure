(ns students.StudentsWithUI
  (:import [sim.portrayal.continuous ContinuousPortrayal2D]
           [sim.portrayal.network NetworkPortrayal2D SpatialNetwork2D SimpleEdgePortrayal2D]
           [sim.portrayal.simple OvalPortrayal2D]
           [sim.display Console Display2D]
           [java.awt Color])
  (:gen-class
    :name students.StudentsWithUI
    :extends sim.display.GUIState
    :main true
    :exposes {state {:get getState}}  ; make accessors for fields in superclass
    :exposes-methods {start superStart, quit superQuit, init superInit}
    :methods [[getDisplay [] sim.display.Display2D]
              [setDisplay [sim.display.Display2D] void]
              [getDisplayFrame [] javax.swing.JFrame]
              [setDisplayFrame [javax.swing.JFrame] void]
              [getYardPortrayal [] sim.portrayal.continuous.ContinuousPortrayal2D]
              [getBuddiesPortrayal [] sim.portrayal.network.NetworkPortrayal2D]
              [setupPortrayals [] void]]
    :state instanceState      ; superclass already has a variable named "state"
    :init init-instance-state  ; we define a MASON function named "init" below
    ;; Supposed to have a no-arg constructor. not sure how this is supposed to work.  TODO ?
    ;:constructors {[]                    [sim.engine.SimState] 
    ;               [sim.engine.SimState] [sim.engine.SimState]}
    ))

(defn -init-instance-state
  [& args]
  [(vec args) {:display (atom nil)
               :display-frame (atom nil)
               :yard-portrayal (ContinuousPortrayal2D.)
               :buddies-portrayal (NetworkPortrayal2D.)}])

(defn -getDisplay [this] @(:display (.instanceState this)))
(defn -setDisplay [this newval] (reset! (:display (.instanceState this)) newval))
(defn -getDisplayFrame [this] @(:display-frame (.instanceState this)))
(defn -setDisplayFrame [this newval] (reset! (:display-frame (.instanceState this)) newval))
(defn -getYardPortrayal [this] (:yard-portrayal (.instanceState this)))
(defn -getBuddiesPortrayal [this] (:buddies-portrayal (.instanceState this)))

;;;;;;;;;;;;;;;;;;;;

(defn -main
  [& args]
  (let [vid (students.StudentsWithUI.
              (students.Students. (System/currentTimeMillis)))] ; can't figure out how to define no-arg constructor, but this works.
        (.setVisible (Console. vid) true)))

(defn -getName [this] "Student Schoolyard Cliques") ; override method in super

(defn -start
  [this]
  (.superStart this)
  (.setupPortrayals this))

(defn -setupPortrayals
  [this]
  (let [students (.getState this)
        yard-portrayal (.getYardPortrayal this)
        buddies-portrayal (.getBuddiesPortrayal this)
        display (.getDisplay this)]
    (.setField yard-portrayal (.getYard students))
    (.setPortrayalForAll yard-portrayal (OvalPortrayal2D.)) ; TODO: write ad-hoc subclass
    (.setField buddies-portrayal (SpatialNetwork2D. (.getYard students) (.getBuddies students)))
    (.setPortrayalForAll buddies-portrayal (SimpleEdgePortrayal2D.))
    (.reset display)
    (.setBackdrop display Color/white)
    (.repaint display)))


(defn -init
  [this controller] ; controller is called c in Java version
  (.superInit this controller)
  (let [display (Display2D. 600 600 this)]
    (.setDisplay this display)
    (.setClipping display false)
    (let [display-frame (.createFrame display)] ; can this be moved before setClipping?
      (.setDisplayFrame this display-frame)
      (.setTitle display-frame "Schoolyard Display")
      (.registerFrame controller display-frame)
      (.setVisible display-frame true)
      (.attach display (.getBuddiesPortrayal this) "Buddies")
      (.attach display (.getYardPortrayal this) "Yard"))))


(defn -quit
  [this]
  (.superQuit this)
  (when-let [display-frame (.getDisplayFrame this)]
    (.dispose display-frame))
  (.setDisplayFrame this nil)
  (.setDisplay this nil))
