# CLOS for Clojure

This library contains tools to provide a more dynamic method dispatching mechanism to
Clojure's multi-methods. It currently adds a way to call the next method in the chain,
via a CLOS-esque `call-next-method`.

## Usage

Include the `clj-clos` namespace

```clojure
(:use [clj-clos.core :only [call-next-method defmethod*]])
```

### Verbose version

One can use the full version of `call-next-method` from within a regular `defmethod`, as in

```clojure
(defmulti my-fun type)
(defmethod my-fun Object [x] (println "my-fun on a regular object"))
(defmethod my-fun Integer [x] (println "my-fun on an int") (call-next-method my-fun Integer x))
```

When invoked with

```clojure
(my-fun 42)
```

it will print out

```
my-fun on an int
my-fun on a regular object
```

### Succinct version

To avoid having to supply the multi-function,  current dispatch value and formal parameter in the invocation of
`call-next-method`, one can use the `defmethod*` macro which yields a form very similar to CLOS:

```clojure
(defmethod* my-fun Integer [x] (println "my-fun on an int") (call-next-method))
```

## NOTE

* The implementation is quite inefficient since it calculates the whole method chain even though only next one is used.

* Whenever a method chain is calculated it *is* cached, though, for use next time. **NOTE**: this
cache will be cleared every time `defmethod*` is invoked, but not for regular `defmethod` forms, so
the latter can create a stale cache. I.e., please use `defmethod*` instead of `defmethod`.

* It currently resolves the chain from the universal taxonomy, i.e., any custom taxonomy provided to
`defmulti` will be silently ignored.

* Another distinction from CLOS, is that no exception is raised when trying to call next method at
the end of the chain, but instead it becomes a noop.

* When using `call-next-method` inside a method, one **has to** capture the actual parameters to the
method with proper formal parameters, i.e., one **cannot** use `_` as a formal parameter.

## Testing

You can run the Midje unit tests by

```bash
lein midje
```

## License

Copyright © 2013 David Bergman

Distributed under the Eclipse Public License, the same as Clojure.
