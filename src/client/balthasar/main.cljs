(ns balthasar.main
  (:require
   [uix.core :as u :refer [defui $]]
   [uix.dom]))

(defn ^:dev/after-load start []
  (js/console.log "Start"))

(defn init! []
  (js/console.log "Init")
  (let [root-element (js/document.getElementById "app")]
    (uix.dom/render root-element
                    [:div
                     [:h1 "Hello, Balthasar!"]
                     [:p "This is a ClojureScript SPA."]]))
  (start))

(defn ^:dev/before-load stop []
  (js/console.log "Stop"))