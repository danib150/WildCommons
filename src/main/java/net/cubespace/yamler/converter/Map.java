package net.cubespace.yamler.converter;

import net.cubespace.yamler.ConfigSection;
import net.cubespace.yamler.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Map implements Converter {

    private InternalConverter internalConverter;

    public Map(InternalConverter internalConverter) {
        this.internalConverter = internalConverter;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {

        java.util.Map<Object, Object> originalMap = (java.util.Map<Object, Object>) obj;
        java.util.Map<Object, Object> serializedMap = new HashMap<>();

        for (java.util.Map.Entry<Object, Object> entry : originalMap.entrySet()) {
            if (entry.getValue() == null) continue;

            Class<?> clazz = entry.getValue().getClass();

            Converter converter = internalConverter.getConverter(clazz);

            Object key;
            if (entry.getKey().getClass().isEnum()) {
            	key = formatEnum(((java.lang.Enum<?>) entry.getKey()).name());
            } else {
            	key = entry.getKey();
            }

            serializedMap.put(key, (converter != null) ? converter.toConfig(clazz, entry.getValue(), null) : entry.getValue());
        }

        return serializedMap;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
        if (genericType != null) {

            java.util.Map<Object, Object> map;
            try {
                map = ((java.util.Map<Object, Object>) ((Class<?>) genericType.getRawType()).newInstance());
            } catch (InstantiationException e) {
                map = new HashMap<>();
            }

            if (genericType.getActualTypeArguments().length == 2) {
                Class<?> keyClass = ((Class<?>) genericType.getActualTypeArguments()[0]);

                java.util.Map<?, ?> map1 = (section instanceof java.util.Map) ? (java.util.Map<?, ?>) section : ((ConfigSection) section).getRawMap();
                for (java.util.Map.Entry<?, ?> entry : map1.entrySet()) {
                    Object key;

                    if (keyClass.equals(Integer.class) && !(entry.getKey() instanceof Integer)) {
                        key = Integer.valueOf((String) entry.getKey());
                    } else if (keyClass.equals(Short.class) && !(entry.getKey() instanceof Short)) {
                        key = Short.valueOf((String) entry.getKey());
                    } else if (keyClass.equals(Byte.class) && !(entry.getKey() instanceof Byte)) {
                        key = Byte.valueOf((String) entry.getKey());
                    } else if (keyClass.equals(Float.class) && !(entry.getKey() instanceof Float)) {
                        key = Float.valueOf((String) entry.getKey());
                    } else if (keyClass.equals(Double.class) && !(entry.getKey() instanceof Double)) {
                        key = Double.valueOf((String) entry.getKey());
                    } else if (keyClass.isEnum() && !(entry.getKey().getClass().isEnum())) {
                        key = getEnum(keyClass, (String) entry.getKey());
                    } else {
                        key = entry.getKey();
                    }

                    Class<?> clazz;
                    if (genericType.getActualTypeArguments()[1] instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericType.getActualTypeArguments()[1];
                        clazz = (Class<?>) parameterizedType.getRawType();
                    } else {
                        clazz = (Class<?>) genericType.getActualTypeArguments()[1];
                    }

                    Converter converter = internalConverter.getConverter(clazz);
                    map.put(key, (converter != null) ? converter.fromConfig(clazz, entry.getValue(), (genericType.getActualTypeArguments()[1] instanceof ParameterizedType) ? (ParameterizedType) genericType.getActualTypeArguments()[1] : null) : entry.getValue());
                }
            } else {
                Converter converter = internalConverter.getConverter((Class<?>) genericType.getRawType());

                if (converter != null) {
                    return converter.fromConfig((Class<?>) genericType.getRawType(), section, null);
                }

                return (section instanceof java.util.Map) ? (java.util.Map<?, ?>) section : ((ConfigSection) section).getRawMap();
            }

            return map;
        } else {
            return section;
        }
    }

    private Object getEnum(Class<?> type, String obj) {
        java.lang.Enum<?>[] enumConstants = (java.lang.Enum<?>[]) type.getEnumConstants();
		for (java.lang.Enum<?> enumConstant : enumConstants) {
			if (formatEnum(enumConstant.name()).equals(formatEnum(obj))) {
				return enumConstant;
			}
		}

		throw new IllegalArgumentException("Invalid enum " + obj + " in " + type.getName());
    }

    private String formatEnum(String s) {
		return s.replace(" ", "").replace("_", "").toLowerCase();
	}

    @Override
    public boolean supports(Class<?> type) {
        return java.util.Map.class.isAssignableFrom(type);
    }
}
