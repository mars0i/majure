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
  :main students.StudentsWithUI
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :profiles {:withgui {:main students.StudentsWithUI} ; execute this with 'lein with-profile withgui run'
             :nogui   {:main students.Students} })      ; execute this with 'lein with-profile nogui run'
