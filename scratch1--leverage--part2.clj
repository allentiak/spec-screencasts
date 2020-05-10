;; setup

(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as st]
         '[clojure.string :as str])
;; => nil

;; example fn
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search]
  (str/index-of source search))
;; => #'user/my-index-of

;; let's compare some evals
(my-index-of "foobar" "b")
;; => 3
(apply my-index-of ["foobar" "b"])
;; => 3
;; the result is the same!
;;
;; so, as any fn can be applied; then, any fn invocation can be described this way, (regardless of its arity).

;; specing a fn
(s/fdef my-index-of
  :args (s/cat :source string? :search string?)
  :ret nat-int?
  :fn #(<= (:ret %) (-> % :args :source count)))

;; we get (enhanced) documentation - for free!
(doc my-index-of)
;; user/my-index-of
;; ([source search
;;   Returns the index at which search appears in source)
;; Spec
;;   args: (cat :source string? :search string?)
;;   ret: nat-int?
;;   fn: (<= (:ret %) (-> % :args :source count)))
;; nil


;; generative testing

;; if we check our fn, it fails(*)... but now provides with very useful data
;; (*)fn to be improved in Part II
(st/summarize-results (st/check `my-index-of))
;; {:spec
;;  (fspec
;;   :args
;;   (cat :source string? :search string?)
;;   :ret
;;   nat-int?
;;   :fn
;;   (<= (:ret %) (-> % :args :source count))),
;;  :sym user/my-index-of,
;;  :failure
;;  {:clojure.spec.alpha/problems
;;   [{:path [:ret],
;;     :pred clojure.core/nat-int?,
;;     :val nil,
;;     :via [],
;;     :in []}],
;;   :clojure.spec.alpha/spec
;;   #object[clojure.spec.alpha$spec_impl$reify__2059 0x28884515 "clojure.spec.alpha$spec_impl$reify__2059@28884515"],
;;   :clojure.spec.alpha/value nil,
;;   :clojure.spec.test.alpha/args ("" "0"),
;;   :clojure.spec.test.alpha/val nil,
;;   :clojure.spec.alpha/failure :check-failed}}
;; {:total 1, :check-failed 1}


;; instrumentation
(st/instrument `my-index-of)
;; [user/my-index-of]


;; considering this data...
;;
(let
  [valid-data ["foobar" "b"]
   invalid-data ["foo" 42]]
;; when we invoke the fn with valid data, nothing new...
 (my-index-of valid-data)
;; => 3
;; ...but when we do it with invalid data,
;; now we get a precise error message
 (my-index-of invalid-data))
;; Execution error - invalid arguments to user/my-index-of at (REPL:449).
;; 42 - failed: string? at: [:search]
;;
;;  Unhandled clojure.lang.ExceptionInfo
;;    Spec assertion failed.
;;
;;          Spec: #object[clojure.spec.alpha$regex_spec_impl$reify__2509 0x13249f0c "clojure.spec.alpha$regex_spec_impl$reify__2509@13249f0c"]
;;         Value: ("foo" 42)
;;
;;      Problems:
;;
;;             val: 42
;;              in: [1]
;;          failed: string?
;;              at: [:search]
;;
;;                  alpha.clj:  132  clojure.spec.test.alpha/spec-checking-fn/conform!
;;                  alpha.clj:  140  clojure.spec.test.alpha/spec-checking-fn/fn)
;; ...
