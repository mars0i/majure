;; Convenience macros or functions for defining types, records, etc.
(ns utils.typeabbrevs)

;; Sometimes you just want record or type object with methods, but
;; don't need any other type to implement the interface or protocol
;; that will be required in order to have those methods.  These macros
;; will automatically define a protocol or interface for this purpose.


;; This has no role for type hints, etc.  Uh oh.
;; One option would be to use only protocols, and then have type hints required,
;; but then only used them in the protocol def (which you can do).
;; However, in that case, to keep the macro def simple, we should require that there always
;; be a type hint on return value, even if it's ^void.

(defmacro defrec-with-prot
  "Define a protocol matching the methods in meths, and then define
  a record type using recname, fields, and that protocol."
  [recname fields & meths]
  (let [protname# (gensym recname)]
    `(do 
       (defprotocol ~protname# ~@meths)
       (defrecord ~recname ~fields ~protname# ~@meths))))

(defmacro deftype-with-prot
  "Define a protocol matching the methods in meths, and then define
  a type using recname, fields, and that protocol.  Example:
      (deftype-with-prot Foo
        [^:volatile-mutable x ^:unsynchronized-mutable y]
        (getsum [this] (+ x y)))"
  [typname fields & meths]
  (let [protname# (gensym typname)]
    `(do 
       (defprotocol ~protname# ~@meths)
       (deftype ~typname ~fields ~protname# ~@meths))))

(defmacro deftype-with-intf
  "Define an interface matching the methods in meths, and then define
  a type using recname, fields, and that interface.  Example:
      (deftype-with-intf Foo
        [x ^:volatile-mutable y ^:unsynchronized-mutable z]
        (getsum [this] (+ x y z)))"
  [recname fields & meths]
  (let [intfname# (gensym recname)]
    `(do 
       (definterface ~intfname# ~@(map ; strip first args
                                       (fn [[nm args & body]]
                                         (cons nm (cons (vec (rest args)) body)))
                                       meths))
       (deftype ~recname ~fields ~intfname# ~@meths))))
