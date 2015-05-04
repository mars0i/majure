(ns students.StudentsWithUI
  (:imports [simp.portrayal.continuous ContinuousPortrayal2D]
            [simp.portrayal.network NetworkPortrayal2D]
    )
  (:gen-class
    :name students.StudentsWithUI
    :extends sim.display.GUIState
    :main true
    :state state
    :init init-state

    ))

(defn -init-state
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


