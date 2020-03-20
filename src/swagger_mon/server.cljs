
(ns swagger-mon.server
  (:require [skir.core :as skir]
            ["fs" :as fs]
            [clojure.string :as string]
            [swagger-mon.core :refer [gen-data expand-node]]))

(defn read-swagger! []
  (when-not (fs/existsSync "swagger-api.json")
    (println "swagger-api.json file not found!")
    (js/process.exit 1))
  (println "reading swagger file." (js/Date.))
  (js->clj (js/JSON.parse (fs/readFileSync "swagger-api.json" "utf8"))))

(defonce *swagger-file (atom (read-swagger!)))

(defn get-pathname [url-path]
  (if (string? url-path) (first (string/split url-path "?")) ""))

(defn get-segments [x] (->> (string/split x "/") (remove (fn [y] (string/blank? y)))))

(defn match-path [pathname item]
  (let [actual-xs (get-segments pathname), defined-ys (get-segments item)]
    (if (not= (count actual-xs) (count defined-ys))
      false
      (loop [xs actual-xs, ys defined-ys]
        (if (and (empty? xs) (empty? ys))
          true
          (let [x0 (first xs), y0 (first ys)]
            (if (= x0 y0)
              (recur (rest xs) (rest ys))
              (if (= "{" (first y0)) (recur (rest xs) (rest ys)) false))))))))

(defn find-match-path [pathname defined-paths]
  (let [choices (map get-pathname defined-paths)]
    (loop [xs choices]
      (if (empty? xs)
        nil
        (let [item (first xs)] (if (match-path pathname item) item (recur (rest xs))))))))

(defn in-json [x] (js/JSON.stringify (clj->js x) nil 2))

(defn on-request! [req]
  (case (:url req)
    "/favicon.ico" {:code 200, :body "{}"}
    (let [matched-path (find-match-path
                        (get-pathname (:url req))
                        (keys (get @*swagger-file "paths")))]
      (if (nil? matched-path)
        {:code 404, :body (in-json {:message (str "not found path " (:url req))})}
        (let [get-schema (get-in
                          @*swagger-file
                          ["paths" matched-path "get" "responses" "200"])]
          (if (nil? get-schema)
            {:code 400, :body (in-json {:message "Found no get of this Path"})}
            {:code 200, :body (in-json (expand-node (get get-schema "schema")))}))))))

(defn main! []
  (skir/create-server!
   #(on-request! %)
   {:port (let [user-port js/process.env.PORT]
      (if (some? user-port) (js/parseInt user-port) 7801))})
  (js/setInterval (fn [] (read-swagger!)) (* 1000 30)))

(defn reload! [] (println "reloaded"))
