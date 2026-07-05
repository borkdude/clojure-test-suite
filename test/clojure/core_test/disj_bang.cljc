(ns clojure.core-test.disj-bang
  (:require [clojure.test :refer [are deftest is testing]]
            #?@(:squint [[clojure.core-test.squint-immutable :as sqim]])
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists disj!
  (deftest test-disj!

    (testing "nominal cases"
      (are [expected set keys] (= expected (persistent! (apply disj! (transient set) keys)))
                               #{} #{} [nil]
                               #{} #{1} [1]
                               #{} #{1} [1 1 1]
                               #{} #{1 2 3} [1 2 3]
                               #{3} #{1 2 3} [1 2]
                               #{1 2 3} #{1 2 3} [4 5 6]
                               #?@(:squint [#{[1 1] [3 3]}] :default [#{[3 3]}]) #{[1 1] 2 [3 3]} [[1 1] 2]
                               #{:a :b} #{:a :b :c} [:c]
                               #{true nil} #{true false nil} [false]))

    #?(:squint nil
       :default
       (testing "cannot disj! transient after persistent! call"
         (let [t (transient #{1 2 3}), _ (persistent! t)]
           (is (p/thrown? (disj! t 1))))))

    (testing "bad shape"
      (are [set keys] (p/thrown? (apply disj! set keys))
                      nil [nil]
                      #?@(:squint [] :default [#{} [nil]])
                      '(1) [1]
                      [1] [1]
                      (transient [1]) [1]
                      {:a 1} [:a]
                      (transient {:a 1}) [:a]
                      42 [42]
                      3.14 [3.14]
                      "string" [\s \t]))

    #?(:squint
       (testing "immutable.js through the squint protocols"
         (is (= (sqim/iset #{[3 3]}) (persistent! (disj! (transient (sqim/iset #{[1 1] [3 3]})) (sqim/->imm [1 1])))))))))
