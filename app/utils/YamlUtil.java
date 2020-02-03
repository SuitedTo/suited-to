package utils;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.scanner.ScannerException;

import play.Play;
import play.exceptions.YAMLException;
import play.vfs.VirtualFile;

public class YamlUtil {

	private YamlUtil(){}
	
	@SuppressWarnings("unchecked")
    public static LinkedHashMap loadYaml(String name) {
		Yaml yaml = new Yaml(new CustomClassLoaderConstructor(Object.class, Play.classloader));
        yaml.setBeanAccess(BeanAccess.FIELD);
        VirtualFile yamlFile = null;
        try {
            for (VirtualFile vf : Play.javaPath) {
                yamlFile = vf.child(name);
                if (yamlFile != null && yamlFile.exists()) {
                    break;
                }
            }
            InputStream is = Play.classloader.getResourceAsStream(name);
            if (is == null) {
                throw new RuntimeException("Cannot load yaml file " + name + ", the file was not found");
            }
            return (LinkedHashMap)yaml.load(is);
        } catch (ScannerException e) {
            throw new YAMLException(e, yamlFile);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot load yaml file " + name + ": " + e.getMessage(), e);
        }
    }
}
