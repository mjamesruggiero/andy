(ns andy.article-test
  (:require [andy.article :as article]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [taoensso.carmine :as car]))

(def test-conn
  {:spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body]
  `(car/wcar test-conn ~@body))

(defn clean-database []
  (let [globs ["time:*" "voted:*" "score:*" "article:*" "group:*"]
        dead-keys (flatten (map #(wcar* (car/keys %)) globs))]
        kill-key (fn [k] (wcar* (car/del k)))
    (doseq [k dead-keys] (kill-key k))))

(defn each-fixture [f]
  (f)
  (clean-database))

(use-fixtures :each each-fixture)

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
                    "votes" "1"]]
      (is (= returned expected)))))
