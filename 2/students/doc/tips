
Some things that can go in gen-class:

  :implements [interfacename ...]
  :extends [classname ...]
  :methods [[step [sim.engine.SimState] void]]

To call super in a method, there's no super function, but you can give a method
in the superclass an alias using :exposes-method.
See http://stackoverflow.com/questions/9060127/how-to-invoke-superclass-method-in-a-clojure-gen-class-method

(There is a super function, proxy-super, for classes generated with proxy, I think.)

NOTE: If a method is in an implemented interface, don't list it under methods.
Listing an interface's methods in the gen-class :methods entry
will throw java.lang.ClassFormatError: Duplicate method name&signature


;; Maybe it would be worth adding "^Continuous2D" before "yard", and "^Double2D" before "base-coords".
;; Can't do this to "student", though, without creating a cyclic dependency that Clojure
;; won't allow during compilation (but it's OK at runtime, apparently).
