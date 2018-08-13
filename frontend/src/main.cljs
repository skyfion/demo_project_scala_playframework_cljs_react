(ns main
  (:require [rum.core :as rum]
            [clojure.walk :as walk]
            [httpurr.client.xhr :as http]
            [promesa.core :as p]))

;; main app state
(def state (atom nil))

(enable-console-print!)

(def initial-state
  {:modal? false
   :cards  []})                                             ;; group by id

(def urls {:new-task      "/api/task/new"
           :edit-task     "/api/task/edit"
           :list-task     "/api/task/list"
           :delete-task   "/api/task/delete/"
           :change-status "/api/task/status/"})

(def default-status "todo")

(def default-task {:id          nil
                   :name        ""
                   :description ""
                   :status      default-status})

(defn get-val [e]
  (.-value (.-target e)))

(defn clj->json
  [ds]
  (.stringify js/JSON (clj->js ds)))

(defn str-json->cljs-map [s]
  (map walk/keywordize-keys (js->clj (js/JSON.parse s))))

(defn task-update []
  (p/then (http/send! {:method :get
                       :url    (:list-task urls)})
          (fn [r] (when-let [body (:body r)]
                    (swap! state assoc :cards
                           (group-by :id (str-json->cljs-map body)))))))

(rum/defc task-status-select [status on-select]
  [:select {:on-change (fn [e] (let [val (get-val e)]
                                 (on-select val))) :value status}
   [:option {:value "todo"} "TODO"]
   [:option {:value "in_progress"} "IN PROGRESS"]
   [:option {:value "done"} "DONE"]])

(rum/defc modal-form < rum/reactive
  [task-ref]
  (let [task @task-ref
        edit? (get task :id)
        close-modal #(swap! state assoc :modal? false)
        on-change #(swap! task-ref assoc %1 (get-val %2))]  ;; todo reset task ?
    [:div.modal
     [:div.modal-content
      [:span.close {:on-click close-modal} "x"]
      [:h4 (if edit? "Edit task" "Add new task")]
      [:fieldset
       [:label "Name"
        [:input {:type      "text"
                 :value     (:name task)
                 :on-change #(on-change :name %)}]]
       [:label "Description"
        [:input {:type      "text"
                 :value     (:description task)
                 :on-change #(on-change :description %)}]]
       [:label "Status"
        (task-status-select (get task :status default-status)
                            #(swap! task-ref assoc :status %))]
       [:div.button-group
        [:button.button.button-primary {:on-click (fn []
                                                    (let [task-data @task-ref
                                                          task-data (if-not (:status task)
                                                                      (assoc task-data :status default-status)
                                                                      task-data)
                                                          task-data (if-not (:id task)
                                                                      (assoc task-data :id -1) task-data)]
                                                      (p/then (http/send! {:method :post
                                                                           :url    (if edit?
                                                                                     (:edit-task urls)
                                                                                     (:new-task urls))
                                                                           :body   (clj->json task-data)
                                                                           :headers
                                                                                   {"Content-Type" "application/json"}})
                                                              (fn [r]
                                                                (when (= (:status r) 200) ;; todo if error
                                                                  (do
                                                                    (task-update)
                                                                    (close-modal))))))
                                                    )} "Save"]
        [:button.button.button-outline {:on-click close-modal} "Cancel"]]]]]))

(defn confirm-delete [title f-ok]
  (when (js/confirm title) (f-ok)))

(rum/defc card-form < rum/reactive [card-ref]
  (let [card (first @card-ref)
        {:keys [name description status]} card
        f-delete #(p/then (http/send! {:method :get
                                       :url    (str (:delete-task urls) (:id card))})
                          (fn [r]
                            (js/console.log (str "delete " r))
                            (task-update)))]
    [:div.card-form
     [:label (str "name: " name)]
     [:label (str "description: " description)]
     [:label "status: "]
     (task-status-select status
                         (fn [status]
                           (p/then (http/send! {:method :get :url (str (:change-status urls) (:id card) "/" status)})
                                   (fn [r]
                                     (when (= 200 (:status r)) ;; todo error
                                       (swap! card-ref assoc-in [0 :status] status))))))
     [:div.button-group
      [:button.button.button-outline {:on-click (fn [_]
                                                  (swap! state assoc :edit-task card)
                                                  (swap! state assoc :modal? true))} "Edit"]
      [:button.button.button-outline {:on-click #(confirm-delete (str "Delete " (:name card) " task?") f-delete)}
       "Delete"]]]))

(rum/defc card < rum/reactive [card-ref]
  (let [card (first (rum/react card-ref))
        status (:status card)
        id (:id card)
        status? #(when (= % status) (card-form card-ref))]
    [:div.row {:id id}
     [:div.column {:id "todo"} (status? "todo")]
     [:div.column {:id "in_progress"} (status? "in_progress")]
     [:div.column {:id "done"} (status? "done")]]))




(rum/defc app < rum/reactive
  []
  (let [s (rum/react state)
        modal? (:modal? s)
        task-ref (rum/cursor state :edit-task)]
    [:div.container
     (when modal? (modal-form task-ref))
     [:div.row [:h3 "Bug tracker app"]]
     [:div.row
      [:button.button.button-outline
       {:on-click (fn [_]
                    (swap! state assoc :edit-task default-task)
                    (swap! state assoc :modal? true))}
       "new task"]]
     [:div.row
      [:div.column [:h5 "TODO"]]
      [:div.column [:h5 "IN PROGRESS"]]
      [:div.column [:h5 "DONE"]]]
     (for [k (keys (:cards @state))]
       (card (rum/cursor-in state [:cards k])))
     ]))

(reset! state initial-state)

(task-update)

(rum/mount
  (app)
  (.getElementById js/document "app"))

