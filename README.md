# monitor

expose jvm monitoring via a rest end point

## Getting Started

```sh
$ pwd
/home/kevin/monitor
$ nohup lein run-dev &
[1] 31431
$ curl localhost:8080/vms; echo
{"1984" "org.apache.zookeeper.server.quorum.QuorumPeerMain", "10162" "org.elasticsearch.bootstrap.ElasticSearch", "27684" "clojure.main", "31431" "clojure.main", "24527" "clojure.main", "24542" "clojure.main", "8985" "org.netbeans.Main"}
$ curl localhost:8080/vms/1984; echo
["java.ci.totalTime" "java.cls.loadedClasses" "java.cls.sharedLoadedClasses" ... ]
$ curl localhost:8080/vms/1984/java.ci.totalTime; echo
8914462
$  
```
