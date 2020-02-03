package controllers.api.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import db.jpa.S3Blob;
import models.File;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSerializer extends BaseSerializer implements JsonSerializer<File> {
    private static final DateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public JsonElement serialize(File file, Type type,
                                 JsonSerializationContext jsc) {
        JsonObject result = new JsonObject();
        Date expirationDate = S3Blob.generateUrlExpirationDate();
        nullSafeAdd(result, "url", file.contents.getTemporarySignedUrl(expirationDate, file.name));
        nullSafeAdd(result, "expires", longFormat.format(expirationDate));

        return result;

    }




}
