(ns andy.article
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.string :as str]))

(def server1-conn
  {:spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body]
  `(car/wcar server1-conn ~@body))

(wcar*
 (car/set "account" "wellsfargo")
 (car/get "account"))

(def ^:dynamic *ONE-WEEK-IN-SECONDS* (* 7 86400))

(def ^:dynamic *VOTE-SCORE* 432)

(def ^:dynamic *ARTICLES-PER-PAGE* 25)

(defn now-in-seconds []
  (quot (System/currentTimeMillis) 1000))

(defn article-vote
  "create vote score and persist"
  [user article]
  (let [cutoff (- now-in-seconds *ONE-WEEK-IN-SECONDS*)]
    (if (< (wcar* (car/zscore "time:" article) cutoff))
      nil
      (let [article-id (last (str/split article #":"))]
        (if (wcar* (car/zadd (str "voted:" article-id, user)))
          (wcar* (car/zincrby "score:" article *VOTE-SCORE*))
          (wcar* (car/hincrby article "votes" 1)))))))


(defn post-article [user title link]
  (let [article-id (str (wcar* (car/incr "article:")))
        voted (str "voted:" (str article-id))
        now (now-in-seconds)
        article-key (str "article:" article-id)
        new-score (+ now *VOTE-SCORE*)]
    (wcar* (car/sadd voted user))
    (wcar* (car/expire voted *ONE-WEEK-IN-SECONDS*))

    ;; create article hash
    (wcar* (car/hmset article-key
                      :title title
                      :link link
                      :poster user
                      :time now
                      :votes 1))

    ;; add article to time and score ordered zsets
    (wcar* (car/zadd "score:" new-score article-key))
    (wcar* (car/zadd "time:" now article-key))
    article-id))


(defn get-articles
  "Pull articles"
  [page]
  (let [start (* (- page 1) *ARTICLES-PER-PAGE*)
        end (+ start (- *ARTICLES-PER-PAGE* 1))
        order "score:"
        ids (wcar* (car/zrevrange order start end))]
    (map #((wcar* car/hgetall) %) ids)))


(defn remove-all-keys []
  (let [globs ["time:*" "voted:*" "score:*" "article:*" "group:*"]
        keys (mapcat #(wcar* (car/keys %)) globs)]
    (map #(wcar* (car/del %)) keys)))
