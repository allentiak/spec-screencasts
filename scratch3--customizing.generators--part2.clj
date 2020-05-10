;; setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.gen.alpha :as gen]
         '[clojure.string :as str])

;; Let's go back to our first fn (from Part I)
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search]
  (str/index-of source search))
;; => #'user/my-index-of

;; let's try with with the built-in generator...
(s/fdef my-index-of
  :args (s/cat :source string?
               :search string?))
;; => user/my-index-of--with-builtin-gen

;; whereas the built-in gen does work,
;; it does not model the domain properly,
;; as it takes too long to generate a findable, nontrivial string...
(s/exercise-fn `my-index-of)
;; ([("" "") 0]
;;  [("Q" "") 0]
;;  [("n" "s") nil]
;;  [("" "6za") nil]
;;  [("" "1A") nil]
;;  [("8N8BO" "T2x9z") nil]
;;  [("Y" "BRu9") nil]
;;  [("y" "kYX2s5") nil]
;;  [("zR7CHl" "3S8YOlrt") nil]
;;  [("wPuV9D5" "vq") nil])

;; we use a model to constructively generate a string and a substring,
;; so my generator will be able to find matching strings
(defn gen-string-and-substring
  []
  (let [model (s/cat :prefix string?
                     :match string?
                     :suffix string?)]
    (gen/fmap
     (fn [[prefix match suffix]]
       [(str prefix match suffix) match])
     (s/gen model))))
;; => #'user/gen-string-and-substring

;; let's combine both
(s/def ::my-index-of-args
  (s/cat :source string?
         :search string?))
;; => :user/my-index-of-args

(s/fdef my-index-of
  :args (s/spec ::my-index-of-args
                :gen gen-string-and-substring))
;; => user/my-index-of

;; now, we get matches (but matches only!)
(s/exercise-fn `my-index-of)
;; ([["" ""] 0]
;;  [["5" ""] 0]
;;  [["x7lBNz" "lB"] 2]
;;  [["p6MPg86" "P"] 3]
;;  [["A5m2J9r1X" "J9r1"] 4]
;;  [["DB7D6S" "6S"] 4]
;;  [["uU2i9" "U2i9"] 1]
;;  [["ctgdpj" "ct"] 0]
;;  [["6uIgjsHwQvde3" ""] 0]
;;  [["eL65QxIIkhGg" "65QxIIkh"] 2])

;; but we want to generate both matches and non-matches...
;; so, we combine both generators with 'one-of'
(defn gen-my-index-of-args
  []
  (gen/one-of [(gen-string-and-substring)
               (s/gen ::my-index-of-args)]))
;; => #'user/gen-my-index-of-args

;; with that, my-index-of is complete
(s/fdef my-index-of
  :args (s/spec (s/cat :source string?
                       :search string?)
                :gen gen-my-index-of-args))
;; => user/my-index-of

;; and our generator works as expected: generating both matches and non-matches...
(s/exercise-fn `my-index-of)
;; ([["" ""] 0]
;;  [("Y" "") 0]
;;  [("" "5l") nil]
;;  [("" "h") nil]
;;  [["h9cD9T4" ""] 0]
;;  [("j" "s") nil]
;;  [("bGWS" "d5wKX") nil]
;;  [["USP9Rue36r78F" "ue36r7"] 5]
;;  [("hazobg" "Llwp9") nil]
;;  [("r" "O") nil])
