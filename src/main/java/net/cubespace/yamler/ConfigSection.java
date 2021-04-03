package net.cubespace.yamler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigSection {
    private String fullPath;
    protected final Map<Object, Object> map = new LinkedHashMap<>();

    public ConfigSection() {
        this.fullPath = "";
    }

    public ConfigSection(net.cubespace.yamler.ConfigSection root, String key) {
        this.fullPath = (!root.fullPath.equals("")) ? root.fullPath + "." + key : key;
    }

    public net.cubespace.yamler.ConfigSection create(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Cannot create section at empty path");
        }

        //Be sure to have all ConfigSections down the Path
        int i1 = -1, i2;
        net.cubespace.yamler.ConfigSection section = this;
        while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            net.cubespace.yamler.ConfigSection subSection = section.getConfigSection(node);

            //This subsection does not exists create one
            if (subSection == null) {
                section = section.create(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            net.cubespace.yamler.ConfigSection result = new net.cubespace.yamler.ConfigSection(this, key);
            map.put(key, result);
            return result;
        }

        return section.create(key);
    }

    private net.cubespace.yamler.ConfigSection getConfigSection(String node) {
        return (map.containsKey(node) && map.get(node) instanceof net.cubespace.yamler.ConfigSection) ? (net.cubespace.yamler.ConfigSection) map.get(node) : null;
    }

    public void set(String path, Object value) {
        if (path == null) {
            throw new IllegalArgumentException("Cannot set a value at empty path");
        }

        //Be sure to have all ConfigSections down the Path
        int i1 = -1, i2;
        net.cubespace.yamler.ConfigSection section = this;
        while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            net.cubespace.yamler.ConfigSection subSection = section.getConfigSection(node);

            if (subSection == null) {
                section = section.create(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        } else {
            section.set(key, value);
        }
    }

    protected void mapChildrenValues(Map<Object, Object> output, net.cubespace.yamler.ConfigSection section, boolean deep) {
        if (section != null) {
            for (Map.Entry<Object, Object> entry : section.map.entrySet()) {
                if (entry.getValue() instanceof net.cubespace.yamler.ConfigSection) {
                    Map<Object, Object> result = new LinkedHashMap<>();

                    output.put(entry.getKey(), result);

                    if (deep) {
                        mapChildrenValues(result, (net.cubespace.yamler.ConfigSection) entry.getValue(), true);
                    }
                } else {
                    output.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public Map<Object, Object> getValues(boolean deep) {
        Map<Object, Object> result = new LinkedHashMap<>();
        mapChildrenValues(result, this, deep);
        return result;
    }

    public void remove(String path) {
        this.set(path, null);
    }

    public boolean has(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Cannot remove a Value at empty path");
        }

        //Be sure to have all ConfigSections down the Path
        int i1 = -1, i2;
        net.cubespace.yamler.ConfigSection section = this;
        while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            net.cubespace.yamler.ConfigSection subSection = section.getConfigSection(node);

            if (subSection == null) {
                return false;
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            return map.containsKey(key);
        } else {
            return section.has(key);
        }
    }

    @SuppressWarnings("unchecked")
	public <T> T get(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Cannot remove a Value at empty path");
        }

        //Be sure to have all ConfigSections down the Path
        int i1 = -1, i2;
        net.cubespace.yamler.ConfigSection section = this;
        while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            net.cubespace.yamler.ConfigSection subSection = section.getConfigSection(node);

            if (subSection == null) {
                section = section.create(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            return (T) map.get(key);
        } else {
            return section.get(key);
        }
    }

    public Map<?, ?> getRawMap() {
        return map;
    }

    public static net.cubespace.yamler.ConfigSection convertFromMap(Map<?, ?> config) {
        net.cubespace.yamler.ConfigSection configSection = new net.cubespace.yamler.ConfigSection();
        configSection.map.putAll(config);

        return configSection;
    }
}
