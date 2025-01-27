(ns balthasar.main
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]))

(defui app []
  ($ :h1 "Hello, Balthasar!"))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root ($ app) root))

(defn ^:export init! []
  (render))