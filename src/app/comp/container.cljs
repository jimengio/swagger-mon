
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp defeffect cursor-> <> div button textarea span input]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            ["lorem-ipsum" :refer [LoremIpsum]]
            ["shortid" :as shortid]
            [clojure.string :as string]))

(def lorem (LoremIpsum. (clj->js {})))

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
                            (= k "name") (.generateWords lorem 1)
                            (= k "description") (.generateWords lorem (rand-int 4))
                            (string/ends-with? k "Id") (.generate shortid)
                            :else (expand-node child-schema))]))
                      (into {}))]
        (if (and (seq? (get data "result")) (number? (get data "total")))
          (assoc data "total" (count (get data "result")))
          data))
    "string" (.generateWords lorem (rand-int 4))
    "boolean" (> (rand) 0.5)
    "number" (rand-int 100)
    "array" (->> (range (rand-int 6)) (map (fn [idx] (expand-node (get schema "items")))))
    (do (js/console.warn "Unknown schema:" schema) schema)))

(defn gen-code [schema-text]
  (let [schema (js->clj (js/JSON.parse schema-text)), data (expand-node schema)]
    (js/JSON.stringify (clj->js data) nil 2)))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       state (or (:data states) {:schema "", :code ""})]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (div
     {:style (merge ui/row-parted {:padding 4})}
     (span nil)
     (button
      {:inner-text "Gen",
       :style ui/button,
       :on-click (fn [e d! m!] (m! (assoc state :code (gen-code (:schema state)))))}))
    (div
     {:style (merge ui/expand ui/row)}
     (textarea
      {:value (:schema state),
       :placeholder "Content",
       :style (merge ui/expand ui/textarea {:font-family ui/font-code}),
       :on-input (fn [e d! m!] (m! (assoc state :schema (:value e))))})
     (textarea
      {:style (merge ui/expand ui/textarea {:font-family ui/font-code, :white-space :pre}),
       :value (:code state),
       :disabled true}))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
