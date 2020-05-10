;; setup

(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as st]
         '[clojure.string :as str])
;; => nil


;; Feature Tour

;; _BTW: as any fn can be applied, any fn invocation can be described this way, regardless of its arity (explanation in part 2).

;; let's spec regexes (of args, not chars)
(s/def ::index-of-args
  (s/cat :source string? :search string?))
;; => user/index-of-args

;; considering this data...
(def ^:const valid-data ["foobar" "b"])
;; => #'user/valid-data
(def ^:const invalid-data ["foo" 42])
;; => #'user/invalid-data

;; let's check validation...
(s/valid? ::index-of-args valid-data)
;; => true
(s/valid? ::index-of-args invalid-data)
;; => false

;; now, lets' check conformance
(s/conform ::index-of-args valid-data)
;; => {:source "foobar" :search "b"}
(s/conform ::index-of-args invalid-data)
;; => clojure.spec.alpha/invalid

;; let's do some destructuring...
(s/unform ::index-of-args (s/conform ::index-of-args valid-data))
;; => ("foobar" "b")
;; 'conform' and 'unform' are inverse to each other

;; we get precise error messages for invalid data
;;
;; explain evals to nil (not really useful for valid data)
(s/explain ::index-of-args valid-data)
;; => nil
;; ...but gives useful feedback when evaluating invalid data
(s/explain ::index-of-args invalid-data)
;; 42 - failed: string? in: [1] at: [:search] spec: :user/index-of-args
;; => nil
;;
(s/explain-str ::index-of-args invalid-data)
;; => "42 - failed: string? in: [1] at: [:search] spec: :user/index-of-args\n"
;;
(s/explain-data ::index-of-args invalid-data)
;; #:clojure.spec.alpha{:problems
;;                      [{:path [:search],
;;                        :pred clojure.core/string?,
;;                        :val 42,
;;                        :via [:user/index-of-args],
;;                        :in [1]}],
;;                      :spec :user/index-of-args,
;;                      :value ["foo" 42]}

;; composition
(s/explain (s/every ::index-of-args) [["bad1" 3]
                                      ["OK" "1"]
                                      ["bad2" 43]
                                      [38 "bad3"]])
;; 3 - failed: string? in: [0 1] at: [:search] spec: :user/index-of-args
;; 43 - failed: string? in: [2 1] at: [:search] spec: :user/index-of-args
;; 38 - failed: string? in: [3 0] at: [:source] spec: :user/index-of-args
;; => nil

;; we can generate example data
(s/exercise ::index-of-args)
;; ([("" "") {:source "", :search ""}]
;;  [("" "6") {:source "", :search "6"}]
;;  [("r1" "X") {:source "r1", :search "X"}]
;;  [("a" "") {:source "a", :search ""}]
;;  [("" "77Y2") {:source "", :search "77Y2"}]
;;  [("O2I9" "eUEQy") {:source "O2I9", :search "eUEQy"}]
;;  [("" "v") {:source "", :search "v"}]
;;  [("pe4" "U") {:source "pe4", :search "U"}]
;;  [("2" "L5Rv") {:source "2", :search "L5Rv"}]
;;  [("36iG66593" "Vm") {:source "36iG66593", :search "Vm"}])

;; and use assertions
(s/check-asserts true)
;; => true
;;
;; with valid data, returns the data
(s/assert ::index-of-args valid-data)
;; => ["foobar" "b"]
;;
;; with invalid data, returns something else...
(s/assert ::index-of-args invalid-data)
;; => Execution error - invalid arguments to user/eval14889 at (REPL:133).
;; 42 - failed: string? at: [:search]
