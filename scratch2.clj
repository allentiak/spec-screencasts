;; setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as st]
         '[clojure.string :as str])


;; fn under test (slightly improved from Part I)
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search & opts]
  (apply str/index-of source search opts))
;; => #'user/my-index-of


;; revised s/fdef, with s/alt, s/?, nilable, :fn, and :or
(s/fdef my-index-of
  :args (s/cat :source string?
               :search (s/alt :string string?
                              :char char?)
               :from (s/? nat-int?))
  :ret (s/nilable nat-int?)
  :fn (s/or
       :not-found #(nil? (:ret %))
       :found #(<= (:ret %) (-> % :args :source count))))
;; => user/my-index-of


;; generative testing (with the revised s/fdef)
(->> (st/check `my-index-of) st/summarize-results)
;; => {:total 1, :check-passed 1}


;; calling a spec'ed fn
(defn which-came-first
  "Returns :chicken or :egg, depending on which sting appears first in s, starting in position from"
  [s from]
  (let [c-idx (my-index-of s "chicken" :from from)
        e-idx (my-index-of s "egg" :from from)]
    (cond
      (< c-idx e-idx) :chicken
      (< e-idx c-idx) :egg)))
;; => #'user/which-came-first


;; stacktrace-assisted debugging

(which-came-first "the chicken or the egg" 0)
;; Execution error (ArityException) at user/my-index-of (REPL:60).
;; Wrong number of args (4) passed to: clojure.string/index-of

(pst)
;; ArityException Wrong number of args (4) passed to: clojure.string/index-of
;;   clojure.core/apply (core.clj:669)
;;   clojure.core/apply (core.clj:660)
;;   user/my-index-of (NO_SOURCE_FILE:60)
;;   user/my-index-of (NO_SOURCE_FILE:57)
;;   user/which-came-first (NO_SOURCE_FILE:99)
;;   user/which-came-first (NO_SOURCE_FILE:96)
;;   user/eval11092 (NO_SOURCE_FILE:47)
;;   user/eval11092 (NO_SOURCE_FILE:47)
;;   clojure.lang.Compiler.eval (Compiler.java:7177)
;;   clojure.lang.Compiler.eval (Compiler.java:7132)
;;   clojure.core/eval (core.clj:3214)
;;   clojure.core/eval (core.clj:3210))


;; instrumentation
(st/instrument `my-index-of)
;; => [user/my-index-of]

;; let's try again...
(which-came-first "the chicken or the egg" 0)
;; Execution error - invalid arguments to user/my-index-of at (REPL:99).
;; :from - failed: nat-int? at: [:from]

;; now, the error message is much more useful: it's not about the number of args - it's about their type!

;; and we can get much more information about the error itself:

;; Unhandled clojure.lang.ExceptionInfo
;;    Spec assertion failed.
;;
;;          Spec: #object[clojure.spec.alpha$regex_spec_impl$reify__2509 0x2c5f4667 "clojure.spec.alpha$regex_spec_impl$reify__2509@2c5f4667"]
;;         Value: ("the chicken or the egg" "chicken" :from 0)
;;
;;      Problems:
;;
;;             val: :from
;;              in: [2]
;;          failed: nat-int?
;;              at: [:from]
;;
;;                  alpha.clj:  132  clojure.spec.test.alpha/spec-checking-fn/conform!
;;                  alpha.clj:  140  clojure.spec.test.alpha/spec-checking-fn/fn


;; test + instrument

(s/fdef which-came-first
  :args (s/cat :source string? :from nat-int?)
  :ret #{:chicken :egg})
;; user => user/which-came-first

(->> (st/check `which-came-first)
     st/summarize-results)
;; {:spec
;;  (fspec
;;   :args
;;   (cat :source string? :from nat-int?)
;;   :ret
;;   #{:egg :chicken}
;;   :fn
;;   nil),
;;  :sym user/which-came-first,
;;  :failure
;;  {:clojure.spec.alpha/problems
;;   ({:path [:from],
;;     :pred clojure.core/nat-int?,
;;     :val :from,
;;     :via [],
;;     :in [2]}),
;;   :clojure.spec.alpha/spec
;;   #object[clojure.spec.alpha$regex_spec_impl$reify__2509 0x2c5f4667 "clojure.spec.alpha$regex_spec_impl$reify__2509@2c5f4667"],
;;   :clojure.spec.alpha/value ("" "chicken" :from 0),
;;   :clojure.spec.alpha/fn user/my-index-of,
;;   :clojure.spec.alpha/args ("" "chicken" :from 0),
;;   :clojure.spec.alpha/failure :instrument,
;;   :clojure.spec.test.alpha/caller
;;   {:file "NO_SOURCE_FILE", :line 99, :var-scope user/which-came-first}}}
;; user => {:total 1, :instrument 1}
