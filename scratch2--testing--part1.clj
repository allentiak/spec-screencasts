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
