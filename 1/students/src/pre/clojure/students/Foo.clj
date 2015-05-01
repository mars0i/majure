(ns students.Foo
  (:gen-class
    :name students.Foo
    :state bar
    ; :state baz
    :init init))

(defn -init
  []
  [[] 42])
