(ns clj-clos.t_core
  "Midje tests for the clj-clos.core namespace"
  (:use midje.sweet clj-clos.core))

(derive ::child-1-1 ::parent-1)
(derive ::child-1-1 ::parent-1)
(derive ::child-2-1 ::parent-2)
(derive ::child-2-2 ::parent-2)
(derive ::grandchild-1-1-1 ::child-1-1)

(defn called-in
  "This is just here for Midje to decide whether it was called and with what value"
  [tag])

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
    (call-next-method chained-methods ::child-1-1 x)
    ::child-1-1)

  (defmulti three-related identity)
  (defmethod three-related ::grandchild-1-1-1 [x]
    (called-in ::grandchild-1-1-1)
    (call-next-method three-related ::grandchild-1-1-1 x))
  (defmethod three-related ::parent-1 [x]
    (called-in ::parent-1)
    (call-next-method three-related ::parent-1 x))
  (defmethod three-related ::child-1-1 [x]
    (called-in ::child-1-1)
    (call-next-method three-related ::child-1-1 x))
  (defmethod three-related ::parent-2 [x] (called-in ::parent-2))

  (defmulti chained-method* identity)
  (defmethod* chained-method* ::child-1-1 [x]
    (called-in ::child-1-1)
    (call-next-method))
  (defmethod* chained-method* ::parent-1 [x]
    (called-in ::parent-1)
    (call-next-method)))
  
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

(fact "Three related methods will call in to all of them when chaining methods"
  (three-related ::grandchild-1-1-1 ) => anything
  (provided
   (called-in ::grandchild-1-1-1) => anything
   (called-in ::child-1-1) => anything
   (called-in ::parent-1) => anything))

(fact "Chaining methods via our own defmethod* does invoke a cache recall, which fails
      since it is the first time"
  (chained-method* ::child-1-1) => anything
  (provided
   (recall-next-method chained-method* ::child-1-1) => nil?))

(fact "Chaining methods will recall the cached method second time around"
  (chained-method* ::child-1-1) => anything
  (provided (recall-next-method chained-method* ::child-1-1) => (get-method chained-method* ::parent-1)
            (recall-next-method chained-method* ::parent-1) => nil)
  (against-background (before :facts (chained-method* ::child-1-1))))


(fact "Chaining method via our own defmethod* indeed calls into the more generic variant as well"
  (chained-method* ::child-1-1) => anything
  (provided
   (called-in ::child-1-1) => anything
   (called-in ::parent-1) => anything))

(fact "Defining new method with defmethod* clears the next method cache"
  (defmethod* chained-method* ::foo [x]) => anything
  (provided
   (clear-next-method! chained-method*) => anything))
