(ns ghjq.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(def token (env :github-api-token))


(def q "
{
  search(query: \"language:clojure sort:stars\", type: REPOSITORY, first: 50) {
    nodes {
      ... on Repository {
        databaseId
        name
        issues(first: 50) {
          nodes {
            databaseId
            body
            author {
              login
            }
          }
        }
      }
    }
  }
}
")

(comment
  (let [s (->
            (client/post "https://api.github.com/graphql"
              {:headers {"Authorization" (str "Bearer " token)}
               :body    (json/generate-string {:query q})})
            :body
            (json/parse-string true)
            pr-str
            )]
    #_(clojure.pprint/pprint s (clojure.java.io/writer "data.end"))
    (spit "data.edn" s)
    )
  )



(read-string (slurp "data.edn"))
