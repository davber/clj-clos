(ns clj-clos.core
  "Defines some CLOS-like primitives for use with Clojure multimethods"
  (:use [clj-clos.sort :only [kahn-sort]]
        [clojure.set :only [intersection]]))

(defn taxonomy
  "Yields a DAG of the given taxa, based on ancestry"
  [taxa]
  (into {} (map (fn [taxon] [taxon (intersection taxa (ancestors taxon))]) taxa)))

(defn dispatch-chain
  "Get a chain of potential following dispatch values for a given multifn and given dispatch value"
  [multifn dispatch-value]
  (let [meths (methods multifn)
        generic-values (intersection (set (keys meths)) (ancestors dispatch-value))]
    (->> generic-values taxonomy kahn-sort)))

(defn method-chain
  "Get a chain of potential following methods for a given multifn and current dispatch value"
  [multifn dispatch-value]
  (map (methods multifn) (dispatch-chain multifn dispatch-value)))

(defn call-next-function
  "Emulates CLOS 'call-next-function' by in call-time creating a calling chain
   based on the current dispatch value and arguments.
   NOTE: quite slow.
   Supposed to be used inside a 'defmethod', using the dispatch-value and arguments
   of that invocation"
  [multifn dispatch-value & args]
  (when-let [next-fn (first (method-chain multifn dispatch-value))]
    (apply next-fn args)))

(defmacro defmethod*
  "Like defmethod but allows the use of a simple parameter-less 'call-next-function'
   in the body, just like CLOS.
   NOTE: if using 'call-next-function', you do need to capture the arguments, i.e.,
   no _ ..."
  [multifn dispatch-val params & fn-tail]
  ;; We replace each '(call-next-function)' form with one with parameters
  (let [new-fn-tail (map #(if (= % `(~'call-next-function))
                            `(call-next-function ~multifn ~dispatch-val ~@params)
                            %) fn-tail)]
    `(defmethod ~multifn ~dispatch-val ~params ~@new-fn-tail)))

