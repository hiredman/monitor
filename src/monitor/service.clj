(ns monitor.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(def mhp
  (sun.jvmstat.perfdata.monitor.protocol.local.MonitoredHostProvider.
   (sun.jvmstat.monitor.HostIdentifier. (cast String nil))))

(defn active-vms [request]
  {:status 200
   :body
   (pr-str
    (into {}
          (map (fn [vmid]
                 [(str vmid) (first (.split (.getValue (.findByName (.getMonitoredVm mhp (sun.jvmstat.monitor.VmIdentifier. (str vmid))) "sun.rt.javaCommand"))
                                            " "))])
               (.activeVms mhp))))})

(defn perf-variables [request]
  {:status 200
   :body (pr-str (mapv #(.getName %)
                       (.findByPattern (.getMonitoredVm mhp (sun.jvmstat.monitor.VmIdentifier.
                                                             (str (:vmid (:path-params request)))))
                                       ".*")))})

(defn perf-variable-value [request]
  {:status 200
   :body (pr-str (.getValue (.findByName (.getMonitoredVm mhp (sun.jvmstat.monitor.VmIdentifier.
                                                               (str (:vmid (:path-params request)))))
                                         (str (:perf (:path-params request))))))})

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]
     ["/vms" {:get active-vms}]
     ["/vms/:vmid" {:get perf-variables}]
     ["/vms/:vmid/:perf" {:get perf-variable-value}]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by monitor.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
