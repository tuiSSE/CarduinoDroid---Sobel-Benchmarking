package swp.tuilmenau.carduinodroid.Filter;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FilterFactory {
	protected static Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();
	protected Map<String, Object> _objects = new TreeMap<String, Object>();
	protected static FilterFactory _instance;
	
	private FilterFactory(){
		//hidden constructor
		//register your own filters to the factory here
		//
		this.reg("sobel", Sobel.class);
		this.reg("Edge Detection", Sobel.class);
	}
	
    public static FilterFactory getFactory() {
        if (_instance == null) {
            _instance = new FilterFactory();
        }

        return _instance;
    }
	
	public ImageFilter newInstance(String filterType, Class<?>[] paramTypes, Object[] params) {
		Object obj = null;

        try {
            Class<?> clazz = _classes.get(filterType);

            if (clazz == null) {
                throw new RuntimeException(filterType + " is unknown to the factory.");
            }

            Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
            obj = ctor.newInstance(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (ImageFilter) obj;
    }

    public ImageFilter newInstance(String filterType) {
        return newInstance(filterType, new Class[] {  }, new Object[] {  });
    }

    /**
     * Returns a singleton instance of the class
     */
    public ImageFilter getInstance(String filterType, Class<?>[] paramTypes, Object[] params) {
    	Object obj = _objects.get(filterType);

        if (obj == null) {
            obj = newInstance(filterType, paramTypes, params);
            _objects.put(filterType, obj);
        }

        return (ImageFilter) obj;
    }

    public ImageFilter getInstance(String filterType) {
        return getInstance(filterType, new Class[] {  }, new Object[] {  });
    }
	
	protected void reg(String filterType, Class<?> clazz ){
		if(clazz.getSuperclass().equals(ImageFilter.class)){
			_classes.put(filterType, clazz);
		} else{
			throw new RuntimeException(filterType + " cannot be registered to factory as it is not of type ImageFilter");
		}
	}
	
	public Set<String> getAvailableImageFilters(){
		return _classes.keySet();
	}
}
