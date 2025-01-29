(ns balthasar.video
  (:require
   [ring.util.response :as response]
   [reitit.ring.middleware.multipart :as multipart]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]])
  (:import
   (java.io File)))

(def allowed-types #{"video/mp4" "video/quicktime" "video/x-m4v"})
(def max-size (* 100 1024 1024)) ; 100MB

(defn convert-to-hls [input-path output-dir]
  (let [segment-path (str output-dir "/segment_%d.ts")
        playlist-path (str output-dir "/playlist.m3u8")
        result
        (sh "ffmpeg" "-i" input-path
            "-c:v" "libx264"
            "-preset" "fast"
            "-g" "48"
            "-sc_threshold" "0"
            "-hls_time" "6"        ; Duration of each segment in seconds
            "-hls_list_size" "0"    ; Keep all segments
            "-f" "hls"
            "-hls_segment_filename" segment-path
            playlist-path)]
    (println "FFmpeg result:" result)
    result))

(defn generate-video-id []
  (str (random-uuid)))

(defn upload [file filename content-type]
  (let [new-file (File. "uploads" filename)]
    (io/copy file new-file)
    (convert-to-hls (str "uploads/" filename) "videos")))

(defn handle-upload [{:keys [tempfile filename content-type size]}]
  (try
    (cond
      (not (allowed-types content-type))
      (response/bad-request {:error "Invalid file type"})

      (> size max-size)
      (response/bad-request {:error "File too large"})

      :else
      (let [video-id (generate-video-id)
            ext (last (str/split filename #"\."))
            new-filename (str video-id "." ext)
            cdn-url (str "http://localhost:9000/videos/" new-filename)]

        (upload tempfile new-filename content-type)

        (response/response
         {:id video-id
          :url cdn-url
          :size size})))

    (catch Exception e
      (println "Upload error:" (.getMessage e))
      (response/status
       (response/response "Got your upload")))))

(def upload-routes
  [["/api/upload"
    {:post {:middleware [[multipart/multipart-middleware]]
            :parameters {:multipart {:file multipart/temp-file-part}}
            :handler (fn [{{video "video"} :multipart-params}]
                       (handle-upload video))}}]])
