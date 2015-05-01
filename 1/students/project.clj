(defproject students "0.1.0-SNAPSHOT"
  :description "Environment for experiments with integrating Mason and Clojure"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  ;:source-paths ["src/cloljure"]
  ;:java-source-paths ["src/java"]
  :resource-paths ["resources/mason.18.jar"]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  ;:aot :all
  ;:aot [students.Student]
  ;:main students.core ; 'lein run' will run students/core.clj; 'lein repl' won't
  ;:main ^:skip-aot students.core  ; I don't think I need or want skip-aot
  ;:main students.StudentsWithUI ; run the Java code directly without calling it from Clojure
  :main students.Bar ; run the Java code directly without calling it from Clojure
  ;:target-path "target/%s"
  ;:profiles {:uberjar {:aot :all}}

  ;; See https://github.com/technomancy/leiningen/blob/master/doc/MIXED_PROJECTS.md
  :profiles {:precomp {:source-paths ["src/pre/clojure"]
                        :aot [students.Student students.Foo] } 
             :midcomp {:source-paths ["src/clojure"]
                       :java-source-paths ["src/java"]
                       :aot [students.core] }} 
)
