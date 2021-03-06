;; setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as st]
         '[clojure.string :as str])
;; user => nil

;; fn under test (slightly improved from Part I)
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search & opts]
  (apply str/index-of source search opts))
;; => #'user/my-index-of

;; for our spec'ed fn, the count of the :args and the :source has to be larger than the value of the return value (you cannot find anything beyond the end of the string!)
;;  #(<= (:ret %) (-> % :args :source count)))

;; our first fspec, with ':fn'
(s/fdef my-index-of
  :args (s/cat :source string?
               :search string?)
  :ret nat-int?
  :fn #(<= (:ret %) (-> % :args :source count)))
;; => user/my-index-of

(s/exercise-fn `my-index-of)
;; ([("" "") 0]
;;  [("" "O") nil]
;;  [("6Y" "0v") nil]
;;  [("" "XdI") nil]
;;  [("Q1p" "") 0]
;;  [("Sm15" "7W") nil]
;;  [("U5eM" "") 0]
;;  [("" "3sO") nil]
;;  [("T" "ZcR") nil]
;;  [("28g" "K7t") nil])

;; we would like to specify that the search arg can be either a string or a char;
;;to do that, we add 's/alt' to ':fn'
(s/fdef my-index-of
  :args (s/cat :source string?
               :search (s/alt :string string?
                              :char char?))
  :ret nat-int?
  :fn #(<= (:ret %) (-> % :args :source count)))
;; => user/my-index-of

;; it works
(s/exercise-fn `my-index-of)
;; ([("" "") 0]
;;  [("" "5") nil]
;;  [("" "I") nil]
;;  [("Tcf" \µ) nil]
;;  [("nm" "0513") nil]
;;  [("qy" "cu") nil]
;;  [("z1ok9" "7eU7E") nil]
;;  [("" \P) nil]
;;  [("" \À) nil]
;;  [("" "w20") nil])

;; we also want to support an optional third argument: the position to start the search
;; so we add the quantification operator 's/?' to ':fn' and 's/alt'
(s/fdef my-index-of
  :args (s/cat :source string?
               :search (s/alt :string string?
                              :char char?)
               :from (s/? nat-int?))
  :ret nat-int?
  :fn #(<= (:ret %) (-> % :args :source count)))
;; => user/my-index-of

;; it works
(s/exercise-fn `my-index-of)
;; ([("" "" 1) 0]
;;  [("" \return) nil]
;;  [("TR" "") 0]
;;  [("D3e" "5B6" 0) nil]
;;  [("12" "bOP" 1) nil]
;;  [("t8" \ä) nil]
;;  [("0c3" "IRt6CD" 11) nil]
;;  [("" "VfK") nil]
;;  [("" "qV9" 0) nil]
;;  [("q8" "3dV") nil])

;; now that our fn is fully spec'd, lets test it!
;;
;; with traditional, manual testing...
;;
;; we have to makeup expected values (in this case, counting from 0)...
(my-index-of "testing manually" "m")
;; => 8
;;
;; use that value in the equality...
(= 8 (my-index-of "testing manually" "m"))
;; => true
;;
;; and write the assertion...
(assert (= 8 (my-index-of "testing manually" "m")))
;; => nil

;; this manual process  is:
;; - repetitive,
;; - error-prone,
;; - tedious, and
;; - time-consuming (and time is precious!)

;; what about let spec write the test for us?

;; we let spec do the hard work, and it finds an error!
(->> (st/check `my-index-of) st/summarize-results)
;; {:spec
;;  (fspec
;;   :args
;;   (cat
;;    :source
;;    string?
;;    :search
;;    (alt :string string? :char char?)
;;    :from
;;    (? nat-int?))
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
;;   #object[clojure.spec.alpha$spec_impl$reify__2059 0x554ea9ee "clojure.spec.alpha$spec_impl$reify__2059@554ea9ee"],
;;   :clojure.spec.alpha/value nil,
;;   :clojure.spec.test.alpha/args ("" \  0),
;;   :clojure.spec.test.alpha/val nil,
;;   :clojure.spec.alpha/failure :check-failed}}
;; {:total 1, :check-failed 1}

;; we expected a nat-int as a return value, but we got nil!
;; the error was caused by this (shrunk) input: '("" \  0)'

;; we hadn't thought about a possibly absent 'search' param!
;; to do that, we add 's/nilable' to ':fn', 's/alt', s/?
(s/fdef my-index-of
  :args (s/cat :source string?
               :search (s/alt :string string?
                              :char char?)
               :from (s/? nat-int?))
  :ret (s/nilable nat-int?)
  :fn #(<= (:ret %) (-> % :args :source count)))
;; => user/my-index-of

;; when we try again, it fails once more!
;; (now with a lovely NullPointerException...)
(->> (st/check `my-index-of) st/summarize-results)
;; {:spec
;;  (fspec
;;   :args
;;   (cat
;;    :source
;;    string?
;;    :search
;;    (alt :string string? :char char?)
;;    :from
;;    (? nat-int?))
;;   :ret
;;   (nilable nat-int?)
;;   :fn
;;   (<= (:ret %) (-> % :args :source count))),
;;  :sym user/my-index-of,
;;  :failure #error {
;;  :cause nil
;;  :via
;;  [{:type java.lang.NullPointerException
;;    :message nil}]
;;  :trace
;;  []}}
;; {:total 1, :check-threw 1}

;; this is because, just like ':ret' needs to deal with nil, ':fn' has to do that as well...

;; when we take care of this, by adding 's/or' to ':fn', 's/nilable', 's/?', and 's/alt'...
;; we finally get the working version of our spec!
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

;; with that, our fn finally works our generative testing process!
(->> (st/check `my-index-of) st/summarize-results)
;; => {:total 1, :check-passed 1}
