
(ns swagger-mon.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp defeffect cursor-> <> div button textarea span input]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [swagger-mon.config :refer [dev?]]
            [clojure.string :as string]
            [swagger-mon.core :refer [gen-data]]
            ["copy-to-clipboard" :as copy!]))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       state (or (:data states) {:schema "", :code ""})]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (div
     {:style (merge ui/row-parted {:padding 8})}
     (span nil)
     (div
      {}
      (button
       {:inner-text "Gen & copy",
        :style ui/button,
        :on-click (fn [e d! m!]
          (let [data-code (js/JSON.stringify (gen-data (js/JSON.parse (:schema state))) nil 2)]
            (m!
             (merge
              state
              {:code data-code,
               :schema (js/JSON.stringify (js/JSON.parse (:schema state)) nil 2)}))
            (copy! data-code)))})))
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
