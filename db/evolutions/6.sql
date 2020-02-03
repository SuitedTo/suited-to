# --- !Ups

ALTER TABLE app_user_Category DROP KEY reviewCategories_id;
ALTER TABLE app_user_Category ADD UNIQUE KEY user_category(User_id, reviewCategories_id);