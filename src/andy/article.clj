(ns andy.article
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.string :as str]))

(def server1-conn
  {:spec {:host "127.0.0.1" :port 6379}}) ; See `wcar` docstring for opts

(defmacro wcar* [& body]
  `(car/wcar server1-conn ~@body))

(wcar*
 (car/set "account" "wellsfargo")
 (car/get "account"))

(def *ONE-WEEK-IN-SECONDS* (* 7 86400))

(def *VOTE-SCORE* 432)

(def now-in-seconds
  (quot (System/currentTimeMillis) 1000))

(defn article-vote
  "create vote score and persist"
  [user article]
  (let [cutoff (- now-in-seconds *ONE-WEEK-IN-SECONDS*)]
    (if (< (car/zscore "time:" article) cutoff)
      nil
      (let [article-id (last (str/split article #":"))]
        (if (car/zadd (str "voted:" article-id, user))
          (car/zincrby "score:" article *VOTE-SCORE*)
          (car/hincrby article "votes" 1))))))
