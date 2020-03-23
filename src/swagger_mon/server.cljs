
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
  (let [method (:method req)
        cors-headers {"Access-Control-Allow-Credentials" true,
                      "Access-Control-Allow-Origin" (:origin (:headers req)),
                      "Access-Control-Allow-Methods" "GET,POST,DELETE,PUT",
                      "Access-Control-Allow-Headers" "Content-Type"}]
    (if (= :options method)
      {:code 200, :headers cors-headers, :body "OK"}
      (case (:url req)
        "/favicon.ico" {:code 200, :body "{}"}
        "/"
          {:code 200,
           :body (in-json
                  (let [modified-time (.-mtime (fs/statSync "swagger-api.json"))]
                    {:last-modify modified-time,
                     :last-upload-time (.toLocaleString
                                        (js/Date. modified-time)
                                        "zh-CN"
                                        (clj->js {"timeZone" "Asia/Shanghai"})),
                     :message "mock server generated from swagger",
                     :available-apis (let [paths (get @*swagger-file "paths")]
                       (->> paths
                            (map (fn [[k item]] [k (string/join " " (keys item))]))
                            (into {})))}))}
        (let [matched-path (find-match-path
                            (get-pathname (:url req))
                            (keys (get @*swagger-file "paths")))
              json-headers (merge cors-headers {"Content-Type" "application/json"})]
          (if (nil? matched-path)
            {:code 404,
             :headers json-headers,
             :body (in-json {:message (str "not found path " (:url req))})}
            (let [get-schema (get-in
                              @*swagger-file
                              ["paths" matched-path (name method) "responses" "200"])]
              (if (nil? get-schema)
                {:code 400,
                 :headers json-headers,
                 :body (in-json {:message "Found no get of this Path"})}
                {:code 200,
                 :headers json-headers,
                 :body (in-json (expand-node (get get-schema "schema")))}))))))))

(defn main! []
  (skir/create-server!
   #(on-request! %)
   {:port (let [user-port js/process.env.PORT]
      (if (some? user-port) (js/parseInt user-port) 7801))})
  (js/setInterval (fn [] (reset! *swagger-file (read-swagger!))) (* 1000 30)))

(defn reload! [] (println "reloaded"))
