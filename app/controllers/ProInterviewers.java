package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.RoleHolderPresent;
import db.jpa.S3Blob;
import models.*;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.mvc.With;

import java.io.FileInputStream;
import java.util.List;

@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class ProInterviewers extends ControllerBase {


    /**
     * Displays the pro interviewer request form
     */
    public static void apply() {
        User user = Security.connectedUser();

        render(user);
    }

    /**
     * Saves the pro interviewer request form
     */
    public static void applySave(@Required String categories,
                                 @Required String phone,
                                 @Required String yearsCategoryExperience,
                                 @Required String yearsInterviewerExperience,
                                 String linkedInProfile,
                                 java.io.File supportingDocument){

        List<Category> categoryList = Categories.categoriesFromStandardRequestParam(categories, false);
        ProInterviewerRequestFile proInterviewerRequestFile = null;

        if (supportingDocument != null) {
            try {
                Blob supportingDocumentBlob = new Blob();
                String name = supportingDocument.getName();
                String type = MimeTypes.getContentType(supportingDocument.getName());
                supportingDocumentBlob.set(new FileInputStream(supportingDocument), type);

                S3Blob s3Blob = new S3Blob();
                await(s3Blob.setAsJob(new FileInputStream(supportingDocumentBlob.getFile()),
                        type).now());
                proInterviewerRequestFile = new ProInterviewerRequestFile(name, type, s3Blob);
            } catch(Exception e) {
                Logger.error("Unable to upload pro interviewer request document. %e", e.getMessage());
            }
        }
        User user = Security.connectedUser();
        ProInterviewerRequest proInterviewerRequest =
                new ProInterviewerRequest(user, categoryList, phone, yearsCategoryExperience,
                                          yearsInterviewerExperience, linkedInProfile);
        for (Category category : categoryList) {
            if (user.getProInterviewerOverrideCategories(true).contains(category)) {
                Validation.addError("categories",
                        "You are already a pro interviewer for one or more categories.");
                break;
            }
        }

        if (validation.hasErrors()) {
            renderArgs.put("proInterviewerRequest", proInterviewerRequest);
            render("@apply");
        }

        proInterviewerRequest.save();
        if (proInterviewerRequestFile != null) {
            proInterviewerRequestFile.proInterviewerRequest = proInterviewerRequest;
            proInterviewerRequestFile.save();
        }

        Users.show(Security.connectedUser().id, "My Account");
    }
}