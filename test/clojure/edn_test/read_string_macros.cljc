(ns clojure.edn-test.read-string-macros)

(defmacro are-read-as [& pairs]
  `(clojure.test/are [expected# edn#] (= expected# (clojure.edn/read-string edn#)) ~@pairs))

(defmacro are-thrown [& edns]
  `(clojure.test/are [edn#] (~'p/thrown? (clojure.edn/read-string edn#)) ~@edns))
