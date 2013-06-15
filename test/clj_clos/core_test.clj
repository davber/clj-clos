(ns clj-clos.core-test
  (:require [clojure.test :refer :all]
            [clj-clos.core :refer :all]))

(deftest a-test
  (testing "I succeed, trivially"
    (is (= 1 1))))
