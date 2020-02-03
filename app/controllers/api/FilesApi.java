package controllers.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.api.serialization.FileSerializer;
import models.CandidateFile;
import models.File;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilesApi extends ApiController {

    private static final FileSerializer DEFAULT_SERIALIZER = new FileSerializer();

    public static void get() throws IOException {
        Object entity = getSpecifiedEntity();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        File file = (File) entity;
        result.put("data", file);
        renderJSON(getDefaultGson().toJson(result));
    }


    private static Gson getDefaultGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(File.class, DEFAULT_SERIALIZER)
                .registerTypeAdapter(CandidateFile.class, DEFAULT_SERIALIZER)
                .create();
    }
}
