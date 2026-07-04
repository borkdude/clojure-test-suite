(ns clojure.core-test.vals
  (:require [clojure.test :as t :refer [deftest is testing]]
            #?@(:squint [[clojure.core-test.squint-immutable :as sqim]])
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists vals
  (deftest test-vals
    (testing "common"
      (is (= nil (vals nil)))
      (is (= nil (vals {})))
      (is (= nil (vals [])))
      (is (= nil (vals '())))
      (is (= nil (vals #{})))
      (is (= '(0.0) (vals {0 0.0})))
      (is (= '(:b) (vals {:a :b})))
      #?(:squint (is (contains? (sqim/iset #{'(:b :d) '(:d :b)})
                                (sqim/->imm (vals {:a :b :c :d}))))
         :default (is (contains? #{'(:b :d) '(:d :b)} (vals {:a :b :c :d}))))
      (is (= '("b") (vals {"a" "b"})))
      (is (= '([:b :c]) (vals {:a [:b :c]})))
      (is (= '((:c)) (vals {:a (vals {:b :c})})))
      #?@(:cljs [(is (p/thrown? (vals 0)))]
          :default [(is (p/thrown? (vals 0)))]))))
