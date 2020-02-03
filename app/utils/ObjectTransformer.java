/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author hamptos
 */
public interface ObjectTransformer extends Transformer<Object>{
    
    public static class DummyTransformer implements ObjectTransformer {
        
        public static final DummyTransformer INSTANCE = 
            new DummyTransformer();
        
        private DummyTransformer() {
            
        }
        
        public Object transform(Object input) {
            return input;
        }
    }
}
