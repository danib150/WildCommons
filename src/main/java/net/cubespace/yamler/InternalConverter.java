package net.cubespace.yamler;

import net.cubespace.yamler.ConfigSection;
import net.cubespace.yamler.InvalidConverterException;
import net.cubespace.yamler.YamlerConfig;
import net.cubespace.yamler.converter.Converter;
import net.cubespace.yamler.converter.Primitive;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashSet;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class InternalConverter {
    private LinkedHashSet<Converter> converters = new LinkedHashSet<>();

    public InternalConverter() {
        try {
            addConverter(Primitive.class);
            addConverter(net.cubespace.yamler.converter.Config.class);
            addConverter(net.cubespace.yamler.converter.Enum.class);
            addConverter(net.cubespace.yamler.converter.List.class);
            addConverter(net.cubespace.yamler.converter.Map.class);
            addConverter(net.cubespace.yamler.converter.Array.class);
            addConverter(net.cubespace.yamler.converter.Set.class);
        } catch (InvalidConverterException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addConverter(Class<?> converter) throws InvalidConverterException {
        if (!Converter.class.isAssignableFrom(converter)) {
            throw new InvalidConverterException("Converter does not implement the Interface Converter");
        }

        try {
            Converter converter1 = (Converter) converter.getConstructor(net.cubespace.yamler.InternalConverter.class).newInstance(this);
            converters.add(converter1);
        } catch (NoSuchMethodException e) {
            throw new InvalidConverterException("Converter does not implement a Constructor which takes the InternalConverter instance", e);
        } catch (InvocationTargetException e) {
            throw new InvalidConverterException("Converter could not be invoked", e);
        } catch (InstantiationException e) {
            throw new InvalidConverterException("Converter could not be instantiated", e);
        } catch (IllegalAccessException e) {
            throw new InvalidConverterException("Converter does not implement a public Constructor which takes the InternalConverter instance", e);
        }
    }

    public Converter getConverter(Class<?> type) {
        for(Converter converter : converters) {
            if (converter.supports(type)) {
                return converter;
            }
        }

        return null;
    }

    public void fromConfig(YamlerConfig config, Field field, ConfigSection root, String path) throws Exception {
        Object obj = field.get(config);

        Converter converter;

        if (obj != null) {
            converter = getConverter(obj.getClass());

            if (converter != null) {
                field.set(config, converter.fromConfig(obj.getClass(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
                return;
            } else {
                converter = getConverter(field.getType());
                if (converter != null) {
                    field.set(config, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
                    return;
                }
            }
        } else {
            converter = getConverter(field.getType());

            if (converter != null) {
                field.set(config, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
                return;
            }
        }

        field.set(config, root.get(path));
    }

    public void toConfig(YamlerConfig config, Field field, ConfigSection root, String path) throws Exception {
        Object obj = field.get(config);

        Converter converter;

        if (obj != null) {
            converter = getConverter(obj.getClass());

            if (converter != null) {
                root.set(path, converter.toConfig(obj.getClass(), obj, (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
                return;
            } else {
                converter = getConverter(field.getType());
                if (converter != null) {
                    root.set(path, converter.toConfig(field.getType(), obj, (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
                    return;
                }
            }
        }

        root.set(path, obj);
    }
}
