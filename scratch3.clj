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
