;; setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.gen.alpha :as gen]
         '[clojure.string :as str])

;; abitrary predicates are not efficient constructors
(s/def ::id (s/and string? #(str/starts-with? % "FOO-")))
;; user => :user/id

(s/exercise ::id)
;; Error printing return value (ExceptionInfo) at clojure.test.check.generators/fn (generators.cljc:435).
;; Couldn't satisfy such-that predicate after 100 tries.

;; {:pred #function[clojure.spec.alpha/gensub/fn--1876],
;;     :gen {:gen #function[clojure.test.check.generators/such-that/fn--11552]},
;;     :max-tries 100
;;  generators.cljc:  435  clojure.test.check.generators$fn__11549/}
;; ...


;; transform an existing generator
(defn fun-gen
  []
  (->> (s/gen (s/int-in 1 100))
       (gen/fmap #(str "FOO-" %))))
;; #'user/fun-gen

(s/exercise ::id 10 {::id fun-gen})
;; user=> (["FOO-1" "FOO-1"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-3" "FOO-3"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-3" "FOO-3"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-8" "FOO-8"]
;;  ["FOO-1" "FOO-1"])


;; redefine ::id, adding generator to spec registry
(s/def ::id
  (s/spec (s/and string? #(str/starts-with? % "FOO-"))
          :gen fun-gen))
;; user => :user/id

;; now, it works without having to explicitly mention "fun-gen"
(s/exercise ::id)
;; user=> (["FOO-1" "FOO-1"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-3" "FOO-3"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-3" "FOO-3"]
;;  ["FOO-2" "FOO-2"]
;;  ["FOO-8" "FOO-8"]
;;  ["FOO-1" "FOO-1"])


;; lookup
(s/def ::lookup (s/map-of keyword? string? :min-count 1))
;; user => :user/lookup

(s/exercise ::lookup)
;; ([{:*/* "",
;;    :_/r "",
;;    :!/_ "",
;;    ...
;;    :OlnY/Q*!+ "263qId75",
;;    :GT/-+Mt "H7Ct43vj",
;;    :z+?3/?B "OgYL"}])


;; dependent values
(s/def ::lookup-finding-k (s/and (s/cat :lookup ::lookup :k keyword?)
                                 (fn [{:keys [lookup k]}])))
;; user => :user/lookup-finding-k

;; just like the previous case, it is difficult to generate values without a generator fn
(s/exercise ::lookup-finding-k)
;; Error printing return value (ExceptionInfo) at clojure.test.check.generators/fn (generators.cljc:435).
;; Couldn't satisfy such-that predicate after 100 tries.
;;
;; {:pred #function[clojure.spec.alpha/gensub/fn--1876],
;;    :gen {:gen #function[clojure.test.check.generators/such-that/fn--11552]},
;;    :max-tries 100})


;; to solve the problem, we can use a model to generate the data
(defn lookup-finding-k-gen
  []
  (gen/bind
   (s/gen ::lookup)
   #(gen/tuple
     (gen/return %)
     (gen/elements (keys %)))))
;; user => #'user/lookup-finding-k-gen

;; now, invoking s/exercise gives us conformant (albeit gibberish) data
(s/exercise ::lookup-finding-k 10
            {::lookup-finding-k lookup-finding-k-gen})
;; this is supposed to work, but it gives an error instead
;;
;; Error printing return value (ExceptionInfo) at clojure.test.check.generators/fn (generators.cljc:435).
;; Couldn't satisfy such-that predicate after 100 tries.
;; {:pred #function[clojure.spec.alpha/gensub/fn--1876],
;;     :gen {:gen #function[clojure.test.check.generators/gen-bind/fn--11427]},
;;     :max-tries 100}
