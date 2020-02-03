
# --- !Ups

ALTER TABLE PREP_Session DROP FOREIGN KEY FKAC0BDE643DA00BE4;
ALTER TABLE PREP_Interview drop FOREIGN KEY prepinterview_prepuser;
alter table PREP_InterviewBuild  drop foreign key FK58622DDF97AFFA50;
alter table PREP_User drop foreign key prepuser_prepcoupon;
ALTER TABLE PREP_Question drop FOREIGN KEY FK3C3259D897AFFA50;
ALTER TABLE PREP_InterviewCategory_PREP_InterviewCategoryList drop FOREIGN KEY FK288B91BDA1C8966F;
ALTER TABLE PREP_InterviewCategory_PREP_InterviewCategoryList drop FOREIGN KEY FK288B91BD323A7BD1;
alter table PREP_InterviewReview drop foreign key PREP_InterviewReview_ibfk_1;
alter table PREP_QuestionReview drop foreign key PREP_QuestionReview_ibfk_1;
alter table PREP_QuestionReview drop foreign key PREP_QuestionReview_ibfk_2;
alter table PREP_InterviewCategory drop foreign key FKA18253EDFB29B5B7;
alter table PREP_InterviewCategoryListBuild drop foreign key FK154CBF036E4DFB1;
alter table PREP_Job drop foreign key fk_primaryName_id_PREP_Job;
alter table PREP_JobCategory drop foreign key fk_prepCategory_id_PREP_JobCategory;
alter table PREP_JobCategory drop foreign key fk_prepJob_id_PREP_JobCategory;
alter table PREP_JobName drop foreign key fk_prepJob_id_PREP_JobName;
alter table PrepUser_currentCouponChargeHistory drop foreign key PrepUser_currentCouponChargeHistory_ibfk_1;
alter table PrepUser_roles drop foreign key FKE2E0CF761BE60D17;

alter table PREP_Interview CHANGE currentQuestion current_question int(11);
alter table PREP_Question CHANGE questionId question_id varchar(255);
alter table PREP_Question CHANGE staticAnswers static_answers longtext;
alter table PREP_InterviewReview CHANGE reviewKey review_key varchar(255);
alter table PREP_InterviewReview CHANGE reviewerEmail reviewer_email varchar(255);
alter table PREP_InterviewReview CHANGE prepInterview_id prep_interview_id bigint(20);
alter table PREP_QuestionReview CHANGE prepQuestion_id prep_question_id bigint(20);
alter table PREP_QuestionReview CHANGE prepInterviewReview_id prep_interview_review_id bigint(20);
alter table PREP_Category CHANGE categoryId category_id varchar(255);
alter table PREP_Category CHANGE companyName company_name varchar(255);
alter table PREP_Category CHANGE isPrepSearchable is_prep_searchable bit(1);
alter table PREP_Coupon CHANGE currentUses current_uses int(11);
alter table PREP_Coupon CHANGE maxUses max_uses int(11);
alter table PREP_Coupon CHANGE payPeriods pay_periods int(11);
alter table PREP_InterviewCategory CHANGE prepCategory_id prep_category_id bigint(20);
alter table PREP_InterviewCategoryListBuild CHANGE categoryList_id category_list_id bigint(20);
alter table PREP_Job CHANGE primaryName_id primary_name_id bigint(20);
alter table PREP_JobCategory CHANGE prepCategory_id prep_category_id bigint(20);
alter table PREP_JobCategory CHANGE prepJob_id prep_job_id bigint(20);
alter table PREP_JobCategory CHANGE primaryCategory primary_category bit(1);
alter table PREP_JobCategory CHANGE prepJobLevel prep_job_level varchar(20);
alter table PREP_JobName CHANGE prepJob_id prep_job_id bigint(20);
alter table PREP_User CHANGE externalAuthProviderId external_auth_provider_id varchar(255);
alter table PREP_User CHANGE externalAuthProvider external_auth_provider varchar(255);
alter table PREP_User CHANGE firstName first_name varchar(255);
alter table PREP_User CHANGE lastLogin last_login datetime;
alter table PREP_User CHANGE profilePictureUrl profile_picture_url varchar(255);
alter table PREP_User CHANGE lastCharge last_charge datetime;
alter table PREP_User CHANGE stripeId stripe_id varchar(255);
alter table PREP_User CHANGE lastFourCardDigits last_four_card_digits varchar(255);
alter table PREP_User CHANGE tempExpire temp_expire datetime;
alter table PREP_User CHANGE tempPassword temp_password varchar(255);
alter table PrepUser_currentCouponChargeHistory CHANGE PrepUser_id prep_user_id bigint(20);
alter table PrepUser_currentCouponChargeHistory CHANGE currentCouponChargeHistory current_coupon_charge_history datetime;
alter table PrepUser_roles CHANGE PrepUser_id prep_user_id bigint(20);


ALTER TABLE PREP_Category MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_Coupon MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_Interview MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_InterviewBuild MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_InterviewCategory MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_InterviewCategoryList MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_InterviewCategoryListBuild MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_InterviewReview MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_Job MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_JobCategory MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
#ALTER TABLE PREP_JobLevel MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_JobName MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_Question MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_QuestionReview MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_Session MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE PREP_User MODIFY COLUMN id bigint(20) AUTO_INCREMENT;

delete from PREP_InterviewCategoryListBuild;
delete from PREP_InterviewCategory_PREP_InterviewCategoryList;
delete from PREP_InterviewCategoryList;
delete from PREP_InterviewCategory;

alter table PREP_InterviewCategory CHANGE contribution contribution varchar(255);
alter table PREP_InterviewCategory CHANGE difficulty difficulty varchar(255);


alter table PREP_InterviewCategory_PREP_InterviewCategoryList CHANGE list_id PREP_InterviewCategoryList_id bigint(20);

alter table PREP_InterviewCategory_PREP_InterviewCategoryList CHANGE category_id PREP_InterviewCategory_id bigint(20);


#ALTER TABLE PREP_Coupon ADD CONSTRAINT unique_name UNIQUE (name);
#ALTER TABLE PREP_User ADD CONSTRAINT unique_email UNIQUE (email);
#ALTER TABLE PREP_User ADD CONSTRAINT unique_externalAuthProviderId UNIQUE (external_auth_provider_id);

alter table PREP_InterviewReview ADD CONSTRAINT PREP_InterviewReview_ibfk_1 FOREIGN KEY (prep_interview_id) references PREP_Interview(id);
alter table PREP_QuestionReview ADD CONSTRAINT PREP_QuestionReview_ibfk_1 FOREIGN KEY (prep_question_id) references PREP_Question(id);
alter table PREP_QuestionReview ADD CONSTRAINT PREP_QuestionReview_ibfk_2 FOREIGN KEY (prep_interview_review_id) references PREP_InterviewReview(id);
alter table PREP_InterviewCategory ADD CONSTRAINT FKA18253EDFB29B5B7 FOREIGN KEY (prep_category_id) references PREP_Category(id);
alter table PREP_Job ADD CONSTRAINT fk_primaryName_id_PREP_Job FOREIGN KEY (primary_name_id) references PREP_JobName(id);
alter table PrepUser_roles ADD CONSTRAINT FKE2E0CF761BE60D17 FOREIGN KEY (prep_user_id) references PREP_User(id);
alter table PREP_JobName ADD CONSTRAINT fk_prepJob_id_PREP_JobName FOREIGN KEY (prep_job_id) references PREP_Job(id);
alter table PREP_JobCategory ADD CONSTRAINT fk_prepJob_id_PREP_JobCategory FOREIGN KEY (prep_job_id) references PREP_Job(id);
alter table PREP_InterviewCategoryListBuild ADD CONSTRAINT FK154CBF036E4DFB1 FOREIGN KEY (category_list_id) references PREP_InterviewCategoryList(id);
alter table PREP_JobCategory ADD CONSTRAINT fk_prepCategory_id_PREP_JobCategory FOREIGN KEY (prep_category_id) references PREP_Category(id);
alter table PrepUser_currentCouponChargeHistory ADD CONSTRAINT PrepUser_currentCouponChargeHistory_ibfk_1 FOREIGN KEY (prep_user_id) references PREP_User(id);
ALTER TABLE PREP_User ADD CONSTRAINT prepuser_prepcoupon FOREIGN KEY ( coupon_id ) REFERENCES PREP_Coupon ( id );


ALTER TABLE PREP_Question ADD CONSTRAINT FK3C3259D897AFFA50 FOREIGN KEY (interview_id) REFERENCES PREP_Interview (id);
ALTER TABLE PREP_InterviewCategory_PREP_InterviewCategoryList ADD CONSTRAINT FK288B91BDA1C8966F FOREIGN KEY (PREP_InterviewCategoryList_id) REFERENCES PREP_InterviewCategoryList (id);
ALTER TABLE PREP_InterviewCategory_PREP_InterviewCategoryList ADD CONSTRAINT FK288B91BD323A7BD1 FOREIGN KEY (PREP_InterviewCategory_id) REFERENCES PREP_InterviewCategory (id);
ALTER TABLE PREP_InterviewBuild ADD CONSTRAINT FK58622DDF97AFFA50 FOREIGN KEY (interview_id) REFERENCES PREP_Interview (id);
ALTER TABLE PREP_Interview ADD CONSTRAINT prepinterview_prepuser FOREIGN KEY (owner_id) REFERENCES PREP_User(id);
ALTER TABLE PREP_Session ADD CONSTRAINT FKAC0BDE643DA00BE4 FOREIGN KEY (user_id) REFERENCES PREP_User (id);

# --- !Downs




