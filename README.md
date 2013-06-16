# CLOS for Clojure

This library contains tools to provide a more dynamic method dispatching mechanism to
Clojure's multi-methods.

## Features

* `call-next-method` - invokable from with a `defmethod` or `defmethod*` to propagate to
the next more generic method for the given dispatch value.
* `defmethod*` - just like the good old `defmethod` but with some extra maintenance for
handling cached method chains. **NOTE**: this is preferable to `defmethod`.
* `:before` - keyword to `defmethod*` to make sure the method is invoked before automatically
propagating to next method in chain. **NOTE**: the behavior of these before methods is slightly
distinct from CLOS, in that all methods are invoked in same stage, rather than the
stratified approach (of `:before`, `:after` and `:around`, and regular methods) of CLOS.
* `:after` - keyword to `defmethod*` to make sure the method is invoked after automatically
propagating to next method in chain. **NOTE**: see note for `:before`.

One can actually propagate manually, via `call-next-method`, even in a `:before` or `:after` method,
but that will then invoke that chain **twice**.

## Usage

Include the `clj-clos` namespace

```clojure
(:use [clj-clos.core :only [call-next-method defmethod*]])
```

### Verbose version

One can use the full version of `call-next-method` from within a regular `defmethod`, as in

```clojure
(defmulti my-fun type)
(defmethod my-fun Object [_] (println "my-fun on a regular object"))
(defmethod my-fun Integer [_] (println "my-fun on an int") (call-next-method my-fun Integer x))
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
(defmethod* my-fun Integer [_] (println "my-fun on an int") (call-next-method))
```

Or, using a `:before` method:

```clojure
(defmethod* my-fun Integer :before [x] (println "my-fun on an int"))
```

which will automatically call into the `Object` version.

## NOTE

* The implementation is quite inefficient since it calculates the whole method chain even though only next one is used. **NOTE**: not really, since the chain is lazy, but the way it is calculated and then sorted makes it quite likely that all elements of the chain are indeed calculated.

* Whenever a method chain is calculated it *is* cached, though, for use next time. **NOTE**: this
cache will be cleared every time `defmethod*` is invoked, but not for regular `defmethod` forms, so
the latter can create a stale cache. I.e., please use `defmethod*` instead of `defmethod`.

* It currently resolves the chain from the universal taxonomy, i.e., any custom taxonomy provided to
`defmulti` will be silently ignored.

* Another distinction from CLOS, is that no exception is raised when trying to call next method at
the end of the chain, but instead it becomes a noop.


## Testing

You can run the Midje unit tests by

```bash
lein midje
```

## License

Copyright © 2013 David Bergman

Distributed under the Eclipse Public License, the same as Clojure.
