;; setup

(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as st]
         '[clojure.string :as str])
;; => nil


;; PART I

;; example fn
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search]
  (str/index-of source search))
;; => #'user/my-index-of



;; some evals


(my-index-of "foobar" "b")
;; => 3

(apply my-index-of ["foobar" "b"])
;; => 3

;; then, any fn invocation can be described this way, regardless of its arity


;; my consts
(def ^:const valid-data ["foobar" "b"])
;; => #'user/valid-data

(def ^:const invalid-data ["foo" 42])
;; => #'user/invalid-data

;; spec regex (of args, not chars)


(s/def ::index-of-args
  (s/cat :source string? :search string?))
;; => user/index-of-args

;; let's check validation...


(s/valid? ::index-of-args valid-data)
;; => true

(s/valid? ::index-of-args invalid-data)
;; => false


;; lets' check conformance and do some destructuring...


;; checking conformance...

(s/conform ::index-of-args valid-data)
;; => {:source "foobar" :search "b"}

(s/conform ::index-of-args invalid-data)
;; => clojure.spec.alpha/invalid


;; destructuring...
(s/unform ::index-of-args (s/conform ::index-of-args valid-data))
;; => ("foobar" "b")

;; conform and unform are inverse to each other


;; precise error messages for invalid data

;; explain evals to nil (not really useful for valid data)
(s/explain ::index-of-args valid-data)
;; => nil

;; ...but gives useful feedback when evaluating invalid data
(s/explain ::index-of-args invalid-data)
;; 42 - failed: string? in: [1] at: [:search] spec: :user/index-of-args
;; => nil

(s/explain-str ::index-of-args invalid-data)
;; => "42 - failed: string? in: [1] at: [:search] spec: :user/index-of-args\n"

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

;; example data generation
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

;; assertion
(s/check-asserts true)
;; => true

;; with valid data, returns the data
(s/assert ::index-of-args valid-data)
;; => ["foobar" "b"]

;; with invalid data, returns something else...
(s/assert ::index-of-args invalid-data)
;; => Execution error - invalid arguments to user/eval14889 at (REPL:133).
;; 42 - failed: string? at: [:search]


;; specing a fn
(s/fdef my-index-of
  :args (s/cat :source string? :search string?)
  :ret nat-int?
  :fn #(<= (:ret %) (-> % :args :source count)))


;; (enhanced) documentation - for free!
(doc my-index-of)
;; user/my-index-of
;; ([source search
;;   Returns the index at which search appears in source)
;; Spec
;;   args: (cat :source string? :search string?)
;;   ret: nat-int?
;;   fn: (<= (:ret %) (-> % :args :source count)))
;; nil


;; generative testing (it is supposed to fail and to be improved in Part II, but it succeeds...)
(st/summarize-results (st/check 'my-index-of))
;; {:total 0}


;; instrumentation
(st/instrument 'my-index-of)
;; this one is supposed to return "[user/my-index-of]", but it returns "[]"

(my-index-of "foo" 42)
;; this one is supposed to return an Exception, but it returns an Execution error:
;;
;; Execution error (ClassCastException) at user/my-index-of (REPL:54).
;; class java.lang.Long cannot be cast to class java.lang.String (java.la;; ng.Long and java.lang.String are in module java.base of loader 'bootstrap')


;; PART II (only the beginning of it)

;; fn under test (slightly improved from Part I)
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search & opts]
  (apply str/index-of source search opts))
;; => #'users/my-index-of


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
;; => users/my-index-of


;; generative testing (with the revised s/fdef, it  is supposed to work, but it gives a different return)
(->> (st/check 'my-index-of) st/summarize-results)
;; => {:total 0}
;; according to the screencast, it should be "{:total 1, :check-passed 1}"
