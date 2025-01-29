(ns balthasar.main
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [balthasar.video :as video]))

(defui app []
  ($ video/video-upload {:on-upload-complete
                         (fn [data] (js/alert (str "Video uploaded: " (.-title data))))}))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root ($ app) root))

(defn ^:export init! []
  (render))