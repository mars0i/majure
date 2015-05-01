(ns students.Foo
  (:gen-class
    :name students.Foo
    :state baz
    ;:state bar
    :init init))

(defn -init
  []
  [[] 42])
