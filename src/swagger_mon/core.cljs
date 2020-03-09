
(ns swagger-mon.core
  (:require [shadow.resource :refer [inline]]
            [clojure.string :as string]
            ["shortid" :as shortid]))

(def cities-data (js->clj (js/JSON.parse (inline "cities.json")) :keywordize-keys true))

(defn gen-long [n]
  (->> (range (inc (rand-int n)))
       (map
        (fn [idx]
          (let [city (rand-nth cities-data)] (rand-nth [(:city city) (:cityEn city)]))))
       (string/join " ")))

(defn gen-short []
  (let [city (rand-nth cities-data)] (rand-nth [(:city city) (:cityEn city)])))

(defn expand-node [schema]
  (case (get schema "type")
    "object"
      (let [data (->> (get schema "properties")
                      (map
                       (fn [[k child-schema]]
                         [k
                          (cond
                            (= k "createdAt") (.toISOString (js/Date.))
                            (= k "updatedAt") (.toISOString (js/Date.))
                            (= k "id") (.generate shortid)
                            (= k "name") (gen-short)
                            (= k "description") (gen-long 24)
                            (string/ends-with? k "Id") (.generate shortid)
                            :else (expand-node child-schema))]))
                      (into {}))]
        (if (and (seq? (get data "result")) (number? (get data "total")))
          (assoc data "total" (count (get data "result")))
          data))
    "string" (gen-long (rand-int 4))
    "boolean" (> (rand) 0.5)
    "number" (rand-int 100)
    "integer" (rand-int 100)
    "array" (->> (range (rand-int 6)) (map (fn [idx] (expand-node (get schema "items")))))
    (do (js/console.warn "Unknown schema:" schema) schema)))

(defn gen-code [schema-text]
  (let [schema (js->clj (js/JSON.parse schema-text)), data (expand-node schema)]
    (js/JSON.stringify (clj->js data) nil 2)))
