
(ns swagger-mon.core
  (:require [shadow.resource :refer [inline]]
            [clojure.string :as string]
            ["shortid" :as shortid]))

(def cities-data (js->clj (js/JSON.parse (inline "cities.json")) :keywordize-keys true))

(defn gen-chinese-words [n spaced?]
  (->> (range n)
       (map (fn [] (let [city (rand-nth cities-data)] (:city city))))
       (string/join (if spaced? " " ""))))

(defn gen-digits [n] (->> (range n) (map (fn [] (str (rand-int 9)))) (string/join "")))

(defn gen-ip-address []
  (let [ip (->> (range 4) (map (fn [] (rand-int 256))) (string/join "."))
        need-port? (> (rand) 0.5)]
    (if need-port? (str ip ":" (rand-int 65536)) ip)))

(defn gen-long [n]
  (->> (range (inc (rand-int n)))
       (map
        (fn [idx]
          (let [city (rand-nth cities-data)] (rand-nth [(:city city) (:cityEn city)]))))
       (string/join " ")))

(defn gen-uppercase [n]
  (->> (range n)
       (map (fn [] (js/String.fromCharCode (rand-nth (range 65 91)))))
       (string/join "")))

(defn gen-short []
  (let [city (rand-nth cities-data)]
    (rand-nth
     [(:city city)
      (:cityEn city)
      (str
       (gen-chinese-words 2 false)
       "-"
       (gen-chinese-words 1 false)
       "-"
       (gen-chinese-words 1 false))
      (str (gen-uppercase 3) "-" (gen-uppercase 3) "-" (gen-digits 3))])))

(defn expand-node [schema]
  (case (get schema "type")
    "object"
      (let [data (->> (get schema "properties")
                      (map
                       (fn [[k child-schema]]
                         [k
                          (cond
                            (string/ends-with? k "At") (.toISOString (js/Date.))
                            (string/ends-with? k "From") (.toISOString (js/Date.))
                            (string/ends-with? k "To") (.toISOString (js/Date.))
                            (= k "id") (.generate shortid)
                            (= k "name") (gen-short)
                            (= k "description") (gen-long 32)
                            (string/ends-with? k "Id") (.generate shortid)
                            (string/ends-with? k "Addr") (gen-ip-address)
                            :else (expand-node child-schema))]))
                      (into {}))]
        (if (and (seq? (get data "result")) (number? (get data "total")))
          (assoc data "total" (count (get data "result")))
          data))
    "string"
      (rand-nth [(gen-long (rand-int 4)) (gen-chinese-words (rand-int 24) true) (gen-short)])
    "boolean" (> (rand) 0.5)
    "number" (rand-int 100)
    "integer" (rand-int 100)
    "array" (->> (range (rand-int 50)) (map (fn [idx] (expand-node (get schema "items")))))
    (do (js/console.warn "Unknown schema:" schema) schema)))

(defn gen-data [schema-obj]
  (let [schema (js->clj schema-obj), data (expand-node schema)] (clj->js data)))

(defn gen-english-words [n]
  (->> (range n)
       (map (fn [] (let [city (rand-nth cities-data)] (:cityEn city))))
       (string/join " ")))

(defn gen-lowercase [n]
  (->> (range n) (js/String.fromCharCode (rand-nth (range 97 123))) (string/join "")))
