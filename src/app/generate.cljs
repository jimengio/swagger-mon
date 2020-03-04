
(ns app.generate (:require [shadow.resource :refer [inline]] [clojure.string :as string]))

(def cities-data (js->clj (js/JSON.parse (inline "cities.json")) :keywordize-keys true))

(defn gen-long [n]
  (->> (range (inc (rand-int n)))
       (map
        (fn [idx]
          (let [city (rand-nth cities-data)] (rand-nth [(:city city) (:cityEn city)]))))
       (string/join " ")))

(defn gen-short []
  (let [city (rand-nth cities-data)] (rand-nth [(:city city) (:cityEn city)])))
