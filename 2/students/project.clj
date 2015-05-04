(defproject students "0.1.0-SNAPSHOT"
  :description "Environment for experiments with integrating Mason and Clojure"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :resource-paths ["resources/mason.18.jar"]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main students.StudentsWithUI ; default action with 'lein run'

  ;; See https://github.com/technomancy/leiningen/blob/master/doc/MIXED_PROJECTS.md
  :profiles {:comp1 {:source-paths ["src/pre/clojure"]               ; see compile.sh
                        :aot [students.Student students.Students] } 
             :comp2 {:source-paths ["src/clojure"]                   ; see compile.sh
                       ;:aot [students.core] 
                       :java-source-paths ["src/java"]}

             :withgui {:main students.StudentsWithUI} ; execute this with 'lein with-profile withgui run'
             :nogui   {:main students.Students}       ; execute this with 'lein with-profile nogui run'
            })
