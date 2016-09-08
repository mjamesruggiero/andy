(ns andy.article-test
  (:require [andy.article :as article]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [taoensso.carmine :as car]))

(def test-conn
  {:spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body]
  `(car/wcar test-conn ~@body))

(defn remove-all-keys []
  (let [globs ["time:*" "voted:*" "score:*" "article:*" "group:*"]
        keys (mapcat #(wcar* (car/keys %)) globs)]
    (println (str "keys are" (str/join keys)))
    (map #(wcar* (car/del %)) keys)))

(deftest test-post-article
  (testing "article - posting it generates a hash"
    (let [article-id (article/post-article
                      "fake-user"
                      "Fake title"
                      "http://www.example.com")
          returned (wcar* (car/hgetall (str "article:" (str article-id))))
          expected ["title" "Fake title"
                    "link" "http://www.example.com"
                    "poster" "fake-user"
                    "time" (str (article/now-in-seconds))
                    "votes" "1"]
          _ (remove-all-keys)]
      (is (= returned expected)))))
