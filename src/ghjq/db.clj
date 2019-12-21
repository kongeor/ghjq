(ns ghjq.db
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers])
  #_(:import org.sqlite.JDBC)
  )

#_(Class/forName "org.sqlite.JDBC")

(def db-spec
  {:dbtype "sqlite"
   :db-spec
   {:classname   "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname     "db/database.db"}})

(def ds (jdbc/get-datasource "jdbc:sqlite:db/database.db"))

(defn create-schema []
  (jdbc/execute! ds
    ["CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY,
      username TEXT NOT NULL UNIQUE
    )"])
  (jdbc/execute! ds
    ["CREATE TABLE IF NOT EXISTS repositories (
      id INTEGER NOT NULL UNIQUE,
      name TEXT NOT NULL,
      stars INTEGER NOT NULL,
      language TEXT NOT NULL,
      owner INTEGER NOT NULL REFERENCES users(id)
    )"])
  (jdbc/execute! ds
    ["CREATE TABLE IF NOT EXISTS issues (
      id INTEGER NOT NULL UNIQUE,
      title TEXT,
      author INTEGER NOT NULL REFERENCES users(id)
     )"])
  (jdbc/execute! ds
    ["CREATE TABLE IF NOT EXISTS comments (
      id INTEGER NOT NULL UNIQUE,
      issue INTEGER NOT NULL REFERENCES issues(id),
      author INTEGER NOT NULL REFERENCES users(id),
      body TEXT
    )"]))

(defn find-user-by-username [username]
  (first
    (jdbc/execute! ds
      (sql/format
        {:select [:*]
         :from [:users]
         :where [:= :username username]}))))

(defn insert-or-get-user [username]
  (if-let [user (find-user-by-username username)]
    user
    (do
      (jdbc/execute! ds
        (->
          (insert-into :users)
          (columns :username)
          (values [[username]])
          sql/format))
      (find-user-by-username username))))

(defn find-repository-by-id [id]
  (first
    (jdbc/execute! ds
      (sql/format
        {:select [:*]
         :from [:repositories]
         :where [:= :id id]}))))

(defn insert-or-get-repository [id name language stars owner-username]
  (if-let [repo (find-repository-by-id id)]
    repo
    (do
      (let [owner (insert-or-get-user owner-username)
            _ (println owner)
            owner-id (:users/id owner)]
        (jdbc/execute! ds
          (->
            (insert-into :repositories)
            (columns :id :name :language :stars :owner)
            (values [[id name language stars owner-id]])
            sql/format)))
      (find-repository-by-id id))))

(defn find-issue-by-id [id]
  (first
    (jdbc/execute! ds
      (sql/format
        {:select [:*]
         :from [:issues]
         :where [:= :id id]}))))

(defn insert-or-get-issue [id name stars owner-username]
  #_(if-let [user (find-issue-by-id id)]
    user)
  (do
    (let [owner (insert-or-get-user owner-username)
          owner-id (:username/id owner)]
      (jdbc/execute! ds
        (->
          (insert-into :issues)
          (columns :id :name :stars :owner)
          (values [[id name stars owner-id]])
          sql/format)))
    (find-issue-by-id id)))

(comment
  (create-schema)
  (insert-user "foo4")
  (insert-or-get-user "foo2")
  (find-user-by-username "foo4")
  (find-repository-by-id 1)
  (find-issue-by-id 1)
  (insert-or-get-repository 2 "test" "http:/asdf" "js" 10 "foo1"))
