(ns balthasar.video
  (:require
   [uix.core :as uix :refer [defui $]]))

(defui video-upload [{:keys [on-upload-complete]}]
  (let [[video set-video!] (uix/use-state nil)
        [preview set-preview!] (uix/use-state nil)
        [progress set-progress!] (uix/use-state 0)
        [error set-error!] (uix/use-state nil)
        file-input-ref (uix/use-ref nil)]

    (letfn [(handle-file-select [e]
              (let [file (-> e .-target .-files (aget 0))]
                (when file
                  (if (not (.startsWith (.-type file) "video/"))
                    (set-error! "Please select a video file")
                    (do
                      (set-video! file)
                      (set-preview! (.createObjectURL js/URL file))
                      (set-error! nil))))))

            (upload-video []
              (when video
                (let [form-data (js/FormData.)]
                  (.append form-data "video" video)
                  (-> (js/fetch "http://localhost:9000/api/upload"
                                #js {:method "POST"
                                     :mode "no-cors"
                                     :credentials "include"
                                     :body form-data})
                      (.then (fn [response]
                               (if (.-ok response)
                                 (.json response)
                                 (throw (js/Error. "Upload failed")))))
                      (.then (fn [data]
                               (on-upload-complete (js->clj data :keywordize-keys true))
                               (set-video! nil)
                               (set-preview! nil)
                               (set-progress! 0)
                               (set! (.-value @file-input-ref) "")))
                      (.catch (fn [err]
                                (set-error! (.-message err))))))))]

      ($ :div.w-full.max-w-md.p-4.bg-white.rounded-lg.shadow
         ($ :div.mb-4
            ($ :input.w-full.p-2.border.rounded
               {:ref file-input-ref
                :type "file"
                :accept "video/*"
                :on-change handle-file-select}))

         (when preview
           ($ :div.mb-4
              ($ :video.w-full.rounded
                 (clj->js {:src preview
                           :controls true}))))

         (when (pos? progress)
           ($ :div.w-full.bg-gray-200.rounded.h-2.mb-4
              ($ :div.bg-blue-500.rounded.h-2
                 {:style {:width (str progress "%")}})))

         (when error
           ($ :div.text-red-500.mb-4
              error))

         ($ :button.w-full.px-4.py-2.text-white.rounded
            {:class (if (or (not video) (pos? progress))
                      "bg-gray-400"
                      "bg-blue-500 hover:bg-blue-600")
             :disabled (or (not video) (pos? progress))
             :on-click upload-video}
            (if (pos? progress)
              (str "Uploading... " progress "%")
              "Upload Video"))))))
