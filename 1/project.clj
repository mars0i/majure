(defproject students "0.1.0-SNAPSHOT"
  :description "Environment for experiments with integrating Mason and Clojure"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  ;:source-paths ["src/cloljure"]
  ;:java-source-paths ["src/java"]
  :resource-paths ["resources/mason.18.jar"
                   "resources/hamcrest-core-1.3.jar"
                   "resources/jcommon-1.0.23.jar"
                   "resources/jfreechart-1.0.19-experimental.jar"
                   "resources/jfreechart-1.0.19-swt.jar"
                   "resources/jfreechart-1.0.19.jar"
                   "resources/jfreesvg-2.0.jar"
                   "resources/junit-4.11.jar"
                   "resources/orsoncharts-1.4-eval-nofx.jar"
                   "resources/orsonpdf-1.6-eval.jar"
                   "resources/servlet.jar"
                   "resources/swtgraphics2d.jar"
                   "resources/itext-pdfa-5.5.5-javadoc.jar"
                   "resources/itext-pdfa-5.5.5-sources.jar"
                   "resources/itext-pdfa-5.5.5.jar"
                   "resources/itext-xtra-5.5.5-javadoc.jar"
                   "resources/itext-xtra-5.5.5-sources.jar"
                   "resources/itext-xtra-5.5.5.jar"
                   "resources/itextpdf-5.5.5-javadoc.jar"
                   "resources/itextpdf-5.5.5-sources.jar"
                   "resources/itextpdf-5.5.5.jar"]
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
