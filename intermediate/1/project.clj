(defproject students "0.1.0-SNAPSHOT"
  :description "Environment for experiments with integrating Mason and Clojure"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :resource-paths ["resources/mason.18.jar" 
                   "resources/bsh-2.0b4.jar"
                   "resources/itext-1.2.jar"
                   "resources/jcommon-1.0.21.jar"
                   "resources/jfreechart-1.0.17.jar"
                   "resources/jmf.jar"
                   "resources/mason.18.jar"
                   "resources/portfolio.jar"]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main students.core ; 'lein run' will run students/core.clj; 'lein repl' won't
  ;:main students.Students

  ;; See https://github.com/technomancy/leiningen/blob/master/doc/MIXED_PROJECTS.md
  :profiles {:comp1 {:source-paths ["src/pre/clojure"]
                        :aot [students.Student] } 
             :comp2 {:source-paths ["src/clojure"]
                       :java-source-paths ["src/java"]
                       :aot [students.core] }} 
)
