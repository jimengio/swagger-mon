
(ns swagger-mon.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp defeffect cursor-> list-> <> div button textarea span input]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [swagger-mon.config :refer [dev?]]
            [clojure.string :as string]
            [swagger-mon.core
             :refer
             [gen-data
              gen-short
              gen-long
              gen-ip-address
              gen-uppercase
              gen-digits
              gen-chinese-words
              gen-english-words]]
            ["copy-to-clipboard" :as copy!]
            ["shortid" :as shortid]
            [cumulo-util.core :refer [delay!]]))

(defcomp
 comp-nav
 (router)
 (list->
  {:style (merge ui/row {:border-bottom (str "1px solid " (hsl 0 0 90)), :padding 8})}
  (->> [{:title "Swagger Mon", :value :home} {:title "Random", :value :random}]
       (map-indexed
        (fn [idx info]
          [idx
           (div
            {}
            (span
             {:style (merge
                      ui/link
                      {:font-family ui/font-fancy,
                       :font-size 18,
                       :text-decoration :none,
                       :margin-right 16}
                      (if (= (:name router) (:value info)) {:color (hsl 200 80 50)})),
              :inner-text (:title info),
              :on-click (fn [e d! m!] (d! :router {:name (:value info)}))}))])))))

(defcomp
 comp-random-list
 (states)
 (let [state (or (:data states) {:count 0, :copied nil})]
   (let [render-line (fn [x]
                       (div
                        {:style (merge ui/row-middle {:padding "8px 0"})}
                        (<>
                         (str (count (str x)))
                         {:width 40,
                          :display :inline-block,
                          :font-family ui/font-code,
                          :font-size 12,
                          :color (hsl 0 0 80),
                          :text-align :right,
                          :margin-right 16})
                        (div
                         {:style (merge
                                  ui/expand
                                  {:width :auto,
                                   :font-family ui/font-code,
                                   :cursor :pointer}),
                          :class-name "hover-item",
                          :on-click (fn [e d! m!] (m! (assoc state :copied x)) (copy! x))}
                         (<> x))))]
     (div
      {:style (merge ui/expand {:padding 40})}
      (div
       {:style (merge ui/row-middle {:padding "16px 8px"})}
       (button
        {:inner-text "Random",
         :style ui/button,
         :on-click (fn [e d! m!] (m! (update state :count inc)))})
       (=< 8 nil)
       (if (some? (:copied state))
         (span
          {:style (merge ui/row-middle {:color (hsl 0 0 60), :font-family ui/font-fancy})}
          (<>
           (str "Copied " (:copied state))
           {:max-width 400,
            :display :inline-block,
            :white-space :nowrap,
            :overflow :hidden,
            :text-overflow :ellipsis})
          (<> (str " (" (count (:copied state)) " characters)")))))
      (render-line (str (gen-uppercase 3) "-" (gen-uppercase 3) "-" (gen-digits 3)))
      (render-line
       (str (gen-uppercase 4) "-" (gen-uppercase 4) "-" (gen-digits 2) (gen-uppercase 1)))
      (render-line (str (gen-uppercase 6) "_" (gen-uppercase 4) "_" (gen-digits 1)))
      (render-line
       (str
        (gen-chinese-words 2 false)
        "-"
        (gen-chinese-words 1 false)
        "-"
        (gen-chinese-words 1 false)))
      (render-line
       (str (gen-chinese-words 2 false) "-" (gen-chinese-words 1 false) "-" (gen-digits 2)))
      (render-line (gen-chinese-words 1 false))
      (render-line (gen-english-words 1))
      (render-line (gen-chinese-words 4 false))
      (render-line (gen-chinese-words 3 true))
      (render-line (gen-english-words 3))
      (render-line (gen-uppercase 10))
      (render-line (gen-long 20))
      (render-line (gen-long 100))
      (render-line (rand-int 100))
      (render-line (gen-digits 10))
      (render-line (.generate shortid))
      (render-line (gen-ip-address))
      (render-line (.toISOString (js/Date.)))))))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       state (or (:data states) {:schema "", :code ""})
       router (:router store)]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (comp-nav (:router store))
    (case (:name router)
      :random (cursor-> :random comp-random-list states)
      (div
       {:style (merge ui/expand ui/column)}
       (div
        {:style (merge ui/row-parted {:padding 8})}
        (span nil)
        (div
         {}
         (button
          {:inner-text "Gen & copy",
           :style ui/button,
           :on-click (fn [e d! m!]
             (let [data-code (js/JSON.stringify
                              (gen-data (js/JSON.parse (:schema state)))
                              nil
                              2)]
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
         {:style (merge
                  ui/expand
                  ui/textarea
                  {:font-family ui/font-code, :white-space :pre}),
          :value (:code state),
          :disabled true}))))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
