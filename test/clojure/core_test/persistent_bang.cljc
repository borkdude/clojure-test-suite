(ns clojure.core-test.persistent-bang
  (:require [clojure.test :refer [are deftest is testing]]
            #?@(:squint [[clojure.core-test.squint-immutable :as sqim]])
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists persistent!
  (deftest test-persistent!

    (testing "map"
      (are [expected coll] (= expected (persistent! coll))
                           {} (transient {})
                           {nil nil} (transient {nil nil})
                           {:a 1 :b 2} (transient {:a 1 :b 2})))

    (testing "vector"
      (are [expected coll] (= expected (persistent! coll))
                           [] (transient [])
                           [nil] (transient [nil])
                           [1 2 3] (transient [1 2 3])))

    (testing "set"
      (are [expected coll] (= expected (persistent! coll))
                           #{} (transient #{})
                           #{nil} (transient #{nil})
                           #{:a :b :c} (transient #{:a :b :c})))

    #?@(:lpy []
        :squint []
        :default
        [(testing "calling persistent! a second time throws"
           (let [coll (transient {}), _ (persistent! coll)]
             (is (p/thrown? (persistent! coll)))))])

    #?(:squint nil
       :default
    (testing "bad shape"
      (are [coll] (p/thrown? (persistent! coll))
                  nil
                  {:a 1 :b 2}
                  [1 2 3]
                  '(1 2 3)
                  #{1 2 3}
                  true
                  false)))

    #?(:squint
       (testing "immutable.js through the squint protocols"
         (is (= (sqim/->imm {:a 1}) (persistent! (assoc! (transient (sqim/->imm {})) :a 1))))))))
