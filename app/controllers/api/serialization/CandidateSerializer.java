package controllers.api.serialization;

import com.google.gson.*;
import models.Candidate;
import models.CandidateFile;
import models.embeddable.Email;
import models.embeddable.ExternalLink;
import models.embeddable.PhoneNumber;

import java.lang.reflect.Type;

public class CandidateSerializer extends BaseSerializer implements JsonSerializer<Candidate> {

    public JsonElement serialize(Candidate i, Type type,
                                 JsonSerializationContext jsc) {

        JsonObject result = new JsonObject();

        addStandardModelFields(result, i);
        nullSafeAdd(result, "name", i.name);
        //phone numbers
        JsonArray phoneNumbers = new JsonArray();
        for (PhoneNumber phoneNumber : i.phoneNumbers) {
            JsonObject jsonPhone = new JsonObject();
            nullSafeAdd(jsonPhone, "type", phoneNumber.phoneNumberType);
            nullSafeAdd(jsonPhone, "value", phoneNumber.phoneNumberValue);
            phoneNumbers.add(jsonPhone);
        }
        result.add("phoneNumbers", phoneNumbers);

        //email addresses
        JsonArray emails = new JsonArray();
        for (Email email : i.emails) {
            JsonObject jsonEmail = new JsonObject();
            nullSafeAdd(jsonEmail, "type", email.emailType);
            nullSafeAdd(jsonEmail, "value", email.emailAddress);
            emails.add(jsonEmail);
        }
        result.add("emails", emails);

        //social profile urls
        JsonArray socialProfiles = new JsonArray();
        for ( ExternalLink link : i.externalLinks) {
            JsonObject jsonObject = new JsonObject();
            nullSafeAdd(jsonObject, "type", link.externalLinkType);
            nullSafeAdd(jsonObject, "value", link.externalLinkValue);
            socialProfiles.add(jsonObject);
        }
        result.add("socialProfiles", socialProfiles);

        nullSafeAdd(result, "address", i.address);

        //docs
        JsonArray files = new JsonArray();
        for (CandidateFile candidateFile : i.files) {
            JsonObject jsonObject = new JsonObject();
            nullSafeAdd(jsonObject, "name", candidateFile.name);
            nullSafeAdd(jsonObject, "type", candidateFile.type);
            nullSafeAdd(jsonObject, "id", candidateFile.id);
            nullSafeAdd(jsonObject, "candidateDocType", candidateFile.docType);
            files.add(jsonObject);
        }
        result.add("files", files);

        return result;
    }
}
