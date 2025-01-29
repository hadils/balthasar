(ns balthasar.main
  (:require
   [org.httpkit.server :as http]
   [reitit.ring :as ring]
   [balthasar.video :as video]))

(def routes
  (ring/router
   video/upload-routes))

(def app (ring/ring-handler routes))

(def my-server (http/run-server app {:port 9000}))

(comment
  (my-server))
  