(ns clj-clos.t_core
  "Midje tests for the clj-clos.core namespace"
  (:use midje.sweet clj-clos.core))

(derive ::child-1-1 ::parent-1)
(derive ::child-1-1 ::parent-1)
(derive ::child-2-1 ::parent-2)
(derive ::child-2-2 ::parent-2)
(derive ::grandchild-1-1-1 ::child-1-1)

(defn called-in [tag]
  ;; NOTE: this is just here for Midje to decide whether it was called and with what value
)

(defn setup-methods []
  (defmulti two-unrelated identity)
  (defmethod two-unrelated ::parent-1 [_] ::parent-1)
  (defmethod two-unrelated ::parent-2 [_] ::parent-2)

  (defmulti two-related identity)
  (defmethod two-related ::child-1-1 [_] ::child-1-1)
  (defmethod two-related ::parent-1 [_] ::parent-1)

  (defmulti chained-methods identity)
  (defmethod chained-methods ::parent-1 [_]
    (called-in ::parent-1)
    ::parent-1)
  (defmethod chained-methods ::child-1-1 [x]
    (called-in ::child-1-1)
    (call-next-function chained-methods ::child-1-1 x)
    ::child-1-1)

  (defmulti three-related identity)
  (defmethod three-related ::grandchild-1-1-1 [x]
    (called-in ::grandchild-1-1-1)
    (call-next-function three-related ::grandchild-1-1-1 x))
  (defmethod three-related ::parent-1 [x]
    (called-in ::parent-1)
    (call-next-function three-related ::parent-1 x))
  (defmethod three-related ::child-1-1 [x]
    (called-in ::child-1-1)
    (call-next-function three-related ::child-1-1 x))
  (defmethod three-related ::parent-2 [x] (called-in ::parent-2))

  (defmulti chained-method* identity)
  (defmethod* chained-method* ::child-1-1 [x]
    (called-in ::child-1-1)
    (call-next-function))
  (defmethod* chained-method* ::parent-1 [x]
    (called-in ::parent-1)
    (call-next-function)))

(defn teardown-methods []
  (doseq [mf [two-unrelated two-related chained-methods three-related]]
    (remove-all-methods mf)))

(background (before :facts (setup-methods) :after (teardown-methods)))

(fact "A multifunction with two unrelated methods generates empty ancestor method chain for each dispatch value"
  (method-chain two-unrelated ::parent-1) => empty?
  (method-chain two-unrelated ::parent-2) => empty?)

(fact "A multifunction with two related methods generates a singleton and empty set of ancestor dispatch values"
  (method-chain two-related ::parent-1) => empty?
  (method-chain two-related ::child-1-1) => (just anything))

(fact "A multifunction with two related methods generates empty method chains for dispatch values without methods"
  (method-chain two-related ::parent-2) => empty?)

(fact "Even if a dispatch value has parents, the method chain will be empty whenever those parents do not have defined methods"
  (method-chain two-related ::child-2-1) => empty?)

(fact "The following methnods will include a more generic method"
  (dispatch-chain two-related ::child-1-1) => (just ::parent-1))

(fact "call-next-function works with a simple two-taxa system"
  (chained-methods ::child-1-1) => ::child-1-1
  (provided
   (called-in ::parent-1) => anything
   (called-in ::child-1-1) => anything))

(fact "Three related methods (of four) will have the two ancestor dispatch values
       in the chain, skipping the unrelated one"
  (dispatch-chain three-related ::grandchild-1-1-1) => (just ::child-1-1 ::parent-1))

(fact "The defmethod* macro indeed replaces call-next-function with a parameterized version"
  (macroexpand-1 `(defmethod* foo ::foo [x] (call-next-function))) =>
  `(defmethod foo ::foo [x] (call-next-function foo ::foo x)))

(fact "Three related methods will call in to all of them when chaining methods"
  (three-related ::grandchild-1-1-1 ) => anything
  (provided
   (called-in ::grandchild-1-1-1) => anything
   (called-in ::child-1-1) => anything
   (called-in ::parent-1) => anything))

(fact "Chaining methods via our own defmethod* works"
  (chained-method* ::child-1-1) => anything)