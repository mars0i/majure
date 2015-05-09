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
    :exposes-methods {start superStart, quit superQuit, init superInit, getInspector superGetInspector}
    :methods [[getDisplay [] sim.display.Display2D]
              [setDisplay [sim.display.Display2D] void]
              [getDisplayFrame [] javax.swing.JFrame]
              [setDisplayFrame [javax.swing.JFrame] void]
              [getYardPortrayal [] sim.portrayal.continuous.ContinuousPortrayal2D]
              [getBuddiesPortrayal [] sim.portrayal.network.NetworkPortrayal2D]
              [setupPortrayals [] void]]
    :state instanceState         ; superclass already has a variable named "state"
    :init init-instance-state))  ; we define a MASON function named "init" below

;; Supposed to have a no-arg constructor. not sure how this is supposed to work.  TODO ?
;:constructors {[]                    [sim.engine.SimState] 
;               [sim.engine.SimState] [sim.engine.SimState]}

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

(defn -getSimulationInspectedObject [this] (.state this))

(defn -getInspector
  [this]
  (let [i (.superGetInspector this)]
    (.setVolatile i true)
    i))

;;;;;;;;;;;;;;;;;;;;

(defn -main
  [& args]
  (let [vid (students.StudentsWithUI.
              (students.Students. (System/currentTimeMillis)))] ; don't yet know how to define no-arg constructor; this works.
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
    (doto yard-portrayal
      (.setField (.getYard students))
      (.setPortrayalForAll (proxy [OvalPortrayal2D] []
                             (draw [student graphics info]
                               (let [agitation-shade (min 255 (int 
                                                                (* (.getAgitation student) (/ 255 10.0))))]
                                 (set! (.-paint this)  ; paint var in OvalPortrayal2D; 'this' is auto-captured by proxy
                                       (Color. agitation-shade 0 (- 255 agitation-shade)))
                                 (proxy-super draw student graphics info))))))
    (doto buddies-portrayal
      (.setField (SpatialNetwork2D. (.getYard students) (.getBuddies students)))
      (.setPortrayalForAll (SimpleEdgePortrayal2D.)))
    (doto display
      (.reset )
      (.setBackdrop Color/white)
      (.repaint))))


;; Note I reorganized the order of operations in this function
;; to put all of the display ops together, and all of the display-frame 
;; ops together, after creating both display and display-frame in the same 
;; let.  This seems to work, and seems to make sense.  e.g. original version
;; set clipping on display before creating frame.  As far as I can tell 
;; (e.g. from DisplayFrame2D source), this shouldn't matter.
(defn -init
  [this controller] ; controller is called c in Java version
  (.superInit this controller)
  (let [display (Display2D. 600 600 this)
        display-frame (.createFrame display)]
    ;; set up display:
    (.setDisplay this display)
    (doto display
      (.setClipping false)
      (.attach (.getBuddiesPortrayal this) "Buddies")
      (.attach (.getYardPortrayal this) "Yard"))
    ;; set up display frame:
    (.setDisplayFrame this display-frame)
    (.registerFrame controller display-frame)
    (doto display-frame 
      (.setTitle "Schoolyard Display")
      (.setVisible true))))


(defn -quit
  [this]
  (.superQuit this)  ; combine in doto?
  (when-let [display-frame (.getDisplayFrame this)]
    (.dispose display-frame))
  (doto this
    (.setDisplayFrame nil)
    (.setDisplay nil)))


;; Earlier (working) version with order of operations following original Java code:
;(defn -init
;  [this controller] ; controller is called c in Java version
;  (.superInit this controller)
;  (let [display (Display2D. 600 600 this)]
;    (.setDisplay this display)
;    (.setClipping display false)
;    (let [display-frame (.createFrame display)] ; can this be moved before setClipping?
;      (.setDisplayFrame this display-frame)
;      (.setTitle display-frame "Schoolyard Display")
;      (.registerFrame controller display-frame)
;      (.setVisible display-frame true)
;      (.attach display (.getBuddiesPortrayal this) "Buddies")
;      (.attach display (.getYardPortrayal this) "Yard"))))
