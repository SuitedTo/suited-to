package data.binding.types;

import play.data.binding.AnnotationHelper;
import play.data.binding.As;
import play.data.binding.Binder;
import play.data.binding.TypeBinder;

import java.awt.Point;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import play.data.Upload;
import play.db.Model;
import play.exceptions.UnexpectedException;
import play.i18n.Lang;
import play.libs.I18N;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import utils.ImageUtil;

/**
 * Binary binder scales incoming images
 * 
 * @author joel
 *
 */
public class ImageBinder implements TypeBinder<Model.BinaryField> {

    @SuppressWarnings("unchecked")
    public Object bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        try {
            Model.BinaryField b = (Model.BinaryField) actualClass.newInstance();
            List<Upload> uploads = (List<Upload>) Request.current().args.get("__UPLOADS");
            for (Upload upload : uploads) {
                if (upload.getFieldName().equals(value) && upload.getSize() > 0) {
                    b.set(upload.asStream(), upload.getContentType());
                    return b;
                }
            }
            if (Params.current().get(value + "_delete_") != null) {
                return null;
            }
            return Binder.MISSING;
        } catch (Exception e) {
        	return Binder.MISSING;
        }
    }
}
