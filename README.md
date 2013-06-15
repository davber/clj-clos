# CLOS for Clojure

This library contains tools to provide a more dynamic method dispatching mechanism to
Clojure's multi-methods. It currently adds a way to call the next method in the chain,
via a CLOS-esque `call-next-method`.

## Usage

Include the `clj-clos` namespace

```clojure
(:use [clj-clos :only [call-next-method defmethod*]])
```

One can either use the full version of `call-next-method` from within a regular `defmethod`, as in

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

To avoid having to supply the multi-function,  current dispatch value and formal parameter in the invocation
`call-next-method`, one can use the `defmethod*` macro which yields a form very similar to CLOS:

```clojure
(defmethod* my-fun Integer [x] (println "my-fun on an int") (call-next-method))
```

## NOTE

The implementation is currently horrendously ineffective, basically recreating the method chain for
each invocation to `call-next-method`. On the upside, there is no overhead unless `call-next-method`
is actually invoked, and it is completely compliant with regular `defmethod`'s and `defmulti`'s, so
can coexist with regular method definitions for a multi-function.

## Testing

You can rune the Midje unit tests by

```bash
lein midje
```

## License

Copyright © 2013 David Bergman

Distributed under the Eclipse Public License, the same as Clojure.
