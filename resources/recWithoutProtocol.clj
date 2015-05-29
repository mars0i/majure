
(defmacro defrec [recname fields & meths]
  "Define a protocol matching the methods in meths, and then define
  a record type using recname, fields, and that protocol."
  (let [protname# (symbol (str recname "prot"))]
    `(do 
       (defprotocol ~protname# ~@meths)
       (defrecord ~recname ~fields ~protname# ~@meths))))

(defmacro defrec2 [recname fields & meths]
  "Define an interface matching the methods in meths, and then define
  a record type using recname, fields, and that interface."
  (let [protname# (symbol (str recname "prot"))]
    `(do 
       (definterface ~protname# ~@(map ; strip first args
                                    (fn [nm args & body]
                                      (cons nm (cons (vec (rest args)) body)))
                                    meths))
       (defrecord ~recname ~fields ~protname# ~@meths))))
