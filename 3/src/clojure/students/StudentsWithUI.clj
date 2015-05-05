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
    :state instance-state      ; superclass already has state
    :init init-instance-state  ; we define a MASON init below

    ;; TODO uh, how do I define this??:
    :constructors {[] [sim.engine.SimState]} ; also a state -> state version autogenerated
    ))

(defn -init-instance-state
  [& args]
  [(vec args) {:display (atom nil)
               :display-frame (atom nil)
               :yard-portrayal (ContinuousPortrayal2D.)
               :buddies-portrayal (NetworkPortrayal2D.)}])

(defn -getDisplay [this] @(:display (.state this)))
(defn -setDisplay [this newval] (reset! (:display (.state this)) newval))
(defn -getDisplayFrame [this] @(:display-frame (.state this)))
(defn -setDisplayFrame [this newval] (reset! (:display-frame (.state this)) newval))
(defn -getYardPortrayal [this] (:yard-portrayal (.state this)))
(defn -getBuddiesPortrayal [this] (:buddies-portrayal (.state this)))

;;;;;;;;;;;;;;;;;;;;

(defn -main
  [& args]
  (let [vid (students.StudentsWithUI.)
        c (Console. vid)]
    (.setVisible c true)))
;(.setVisible (Console. (students.StudentsWithUI.))))

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

    (.setPortrayalForAll yard-portrayal (OvalPortrayal2D.))
    ;; (.setPortrayalForAll yard-portrayal TODO)

    (.setField buddies-portrayal (SpatialNetwork2D. (.getYard students) (.getBuddies students)))
    (.setPortrayalForAll buddies-portrayal (SimpleEdgePortrayal2D.))

    (.reset display)
    (.setBackdrop display Color/white)
    (.repaint display)))


(defn -init
  [this controller] ; controller is called c in Java version
  (.superInit this)
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

