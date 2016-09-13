(ns andy.search-test
  (:require [andy.search :as sut]
            [clojure.test :refer :all]))

(deftest test-segment-words
  (testing "search - strings can be segmented"
    (let [fake-sentence "That time of year thou meest in me behold that late the sweet"
          returned (vec (sut/segment-words fake-sentence))
          expected ["that" "time" "of" "year" "thou" "meest" "in" "me" "behold" "that" "late" "the" "sweet"]]
          (is (= returned expected)))))

(deftest test-tokenize
  (testing "search - tokenize segments and removes stop words"
    (let [fake-sentence "The guick brown fox jumped across the lazy dogs"
          stopwords #{"the" "across"}
          returned (sut/tokenize fake-sentence stopwords)
          expected #{"fox" "brown" "jumped" "guick" "lazy" "dogs"}]
      (is (= returned expected)))))
