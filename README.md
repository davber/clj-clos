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

  (defmulti my-fun type)
  (defmethod my-fun Object [x] (println "my-fun on a regular object"))
  (defmethod my-fun Integer [x] (println "my-fun on an int") (call-next-method Integer x))

When invoked with

```clojure
(my-fun 42)
```

it will print out

```clojure
  my-fun on an int
  my-fun on a regular object
```

To avoid having to supply both current dispatch value and formal parameter in the invocation
`call-next-method`, one can use the `defmethod*` macro which yields a form very similar to CLOS:

```clojure
  (defmethod* my-fun Integer [x] (println "my-fun on an int") (call-next-method))
```

## Testing

You can rune the Midje unit tests by

```clojure
  lein midje
```

## License

Copyright © 2013 David Bergman

Distributed under the Eclipse Public License, the same as Clojure.
