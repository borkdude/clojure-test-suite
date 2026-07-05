(ns clojure.core-test.squint-immutable
  "Plugs immutable.js collections into squint's collection protocols, giving
  the squint tests a value-semantics container for assertions that need one.
  Empty for every other dialect."
  #?(:squint (:require ["immutable" :as im])))

#?(:squint
   (do
     (extend-type im/Map
       ILookup
       (-lookup [m k nf] (.get m k nf))
       IAssociative
       (-assoc [m k v] (.set m k v))
       (-contains-key? [m k] (.has m k))
       IMap
       (-dissoc [m k] (.delete m k))
       ICounted
       (-count [m] (.-size m))
       IKVReduce
       (-kv-reduce [m f init] (.reduce m (fn [acc v k] (f acc k v)) init))
       ICollection
       (-conj [m x]
         (if (vector? x)
           (.set m (nth x 0) (nth x 1))
           (reduce (fn [acc e] (.set acc (nth e 0) (nth e 1))) m (seq x))))
       IEmptyableCollection
       (-empty [m] (.clear m))
       IEquiv
       (-equiv [m other] (.equals m other))
       IEditableCollection
       (-as-transient [m] (.asMutable m))
       ITransientAssociative
       (-assoc! [m k v] (.set m k v))
       ITransientMap
       (-dissoc! [m k] (.remove m k))
       ITransientCollection
       (-conj! [m x]
         (if (vector? x)
           (.set m (nth x 0) (nth x 1))
           (reduce (fn [acc e] (.set acc (nth e 0) (nth e 1))) m (seq x))))
       (-persistent! [m] (.asImmutable m)))

     (extend-type im/List
       ILookup
       (-lookup [l i nf] (.get l i nf))
       IAssociative
       (-assoc [l i v] (.set l i v))
       (-contains-key? [l i] (.has l i))
       ICounted
       (-count [l] (.-size l))
       ICollection
       (-conj [l x] (.push l x))
       IEmptyableCollection
       (-empty [l] (.clear l))
       IEquiv
       (-equiv [l other] (.equals l other))
       IEditableCollection
       (-as-transient [l] (.asMutable l))
       ITransientCollection
       (-conj! [l x] (.push l x))
       (-persistent! [l] (.asImmutable l)))

     (extend-type im/Set
       IAssociative
       (-contains-key? [s x] (.has s x))
       ICounted
       (-count [s] (.-size s))
       ICollection
       (-conj [s x] (.add s x))
       IEmptyableCollection
       (-empty [s] (.clear s))
       IEquiv
       (-equiv [s other] (.equals s other))
       ISet
       (-disjoin [s x] (.remove s x))
       IEditableCollection
       (-as-transient [s] (.asMutable s))
       ITransientSet
       (-disjoin! [s x] (.remove s x))
       ITransientCollection
       (-conj! [s x] (.add s x))
       (-persistent! [s] (.asImmutable s)))

     (defn ->imm
       "Recursively converts vectors, sets, maps and seqs to immutable.js
       List, Set and Map, so equality and membership are by value. Leaves
       non-collections alone."
       [x]
       (cond
         (vector? x) (im/List. (map ->imm x))
         (set? x) (im/Set. (map ->imm x))
         (map? x) (reduce-kv (fn [acc k v] (assoc acc (->imm k) (->imm v))) (im/Map.) x)
         (and (some? x) (not (string? x)) (seq? x)) (im/List. (map ->imm x))
         :else x))

     (defn iset
       "An immutable.js Set of the value-converted elements of coll:
       membership through contains? is by value."
       [coll]
       (im/Set. (map ->imm coll)))))
