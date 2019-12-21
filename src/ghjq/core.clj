(ns ghjq.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [environ.core :refer [env]]
            [graphql-query.core :refer [graphql-query]]
            [ghjq.db :as db]))

(def token (env :github-api-token))


(def q2 {:queries
         [[:search {:query "language:clojure sort:stars" :type :REPOSITORY :first 50}
           [[:nodes [:fragment/repoFields]]]]]
         :fragments [{:fragment/name :fragment/repoFields
                      :fragment/type :Repository
                      :fragment/fields [:databaseId
                                        :name
                                        [:primaryLanguage [:name]]
                                        [:stargazers [:totalCount]]
                                        [:owner [:login]]]}]})

(graphql-query q2)

(def q1 {:queries
         [[:repository {:owner "yangshun" :name "tech-interview-handbook"}
           [:id
            :databaseId
            :name
            :url
            [:issues {:first 10 :after "Y3Vyc29yOnYyOpHOD9ZOFA=="}
             [[:pageInfo [:endCursor :hasNextPage]]
              [:nodes
               [:id
                :databaseId
                :title
                [:author [:login]]
                [:comments {:first 10 :after nil}
                 [[:pageInfo [:endCursor :hasNextPage]]
                  [:nodes
                   [:id
                    :databaseId
                    [:author [:login]]
                    :bodyText]]]]]]]]
            ]]]})

(graphql-query q1)


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
               :body (json/generate-string {:query (graphql-query q2)})})
            :body
            (json/parse-string true)
            ; pr-str
            )]
    #_(clojure.pprint/pprint s)
    (def data s)
    #_(clojure.pprint/pprint s (clojure.java.io/writer "data.end"))
    #_(spit "data.edn" s)
    )
  )

(defn insert-repos [data]
  (let [repos (-> data :data :search :nodes)]
    (doall
      (map (fn [repo]
             (let [id (:databaseId repo)
                   name (:name repo)
                   lang (-> repo :primaryLanguage :name)
                   stars (-> repo :stargazers :totalCount)
                   owner-username (-> repo :owner :login)]
               (db/insert-or-get-repository id name lang stars owner-username))) repos))
    (println "Inserted" (count repos) "repos")))

(comment
  (insert-repos data))


#_(read-string (slurp "data.edn"))
