package net.cubespace.yamler;

import net.cubespace.yamler.ConfigBasic;
import net.cubespace.yamler.ConfigSection;
import net.cubespace.yamler.YamlerConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public abstract class YamlConfigMapper extends ConfigBasic {
	
    private transient Yaml yaml;
    protected transient ConfigSection root;
    private transient HashMap<String, ArrayList<String>> comments = new HashMap<>();
    private transient Representer yamlRepresenter = new Representer();

    protected YamlConfigMapper() {
        DumperOptions yamlOptions = new DumperOptions();
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new CustomClassLoaderConstructor(net.cubespace.yamler.YamlConfigMapper.class.getClassLoader()), yamlRepresenter, yamlOptions);
    }

    protected void loadFromYaml() throws YamlerConfigurationException {
        root = new ConfigSection();

        try (FileReader fileReader = new FileReader(configFile)) {
            Object object = yaml.load(fileReader);

            if (object != null)
                convertMapsToSections((Map<?, ?>) object, root);
        } catch (IOException | ClassCastException | YAMLException e) {
            throw new YamlerConfigurationException("Could not load YML", e);
        }
    }

    private void convertMapsToSections(Map<?, ?> input, ConfigSection section) {
        if (input == null) return;

        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.create(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected void saveToYaml() throws YamlerConfigurationException {
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            if (configHeader != null) {
                for (String line : configHeader) {
                    fileWriter.write("# " + line + "\n");
                }

                fileWriter.write("\n");
            }

            Integer depth = 0;
            ArrayList<String> keyChain = new ArrayList<>();

            String yamlString = yaml.dump(root.getValues(true));
            StringBuilder writeLines = new StringBuilder();
            for (String line : yamlString.split("\n")) {
                if (line.startsWith(new String(new char[depth]).replace("\0", " "))) {
                    keyChain.add(line.split(":")[0].trim());
                    depth = depth + 2;
                } else {
                    if (line.startsWith(new String(new char[depth - 2]).replace("\0", " "))) {
                        keyChain.remove(keyChain.size() - 1);
                    } else {
                        //Check how much spaces are infront of the line
                        int spaces = 0;
                        for (int i = 0; i < line.length(); i++) {
                            if (line.charAt(i) == ' ') {
                                spaces++;
                            } else {
                                break;
                            }
                        }

                        depth = spaces;

                        if (spaces == 0) {
                            keyChain = new ArrayList<>();
                            depth = 2;
                        } else {
                            ArrayList<String> temp = new ArrayList<>();
                            int index = 0;
                            for (int i = 0; i < spaces; i = i + 2, index++) {
                                temp.add(keyChain.get(index));
                            }

                            keyChain = temp;

                            depth = depth + 2;
                        }
                    }

                    keyChain.add(line.split(":")[0].trim());
                }

                String search;
                if (keyChain.size() > 0) {
                    search = join(keyChain, ".");
                } else {
                    search = "";
                }


                if (comments.containsKey(search)) {
                    for (String comment : comments.get(search)) {
                        writeLines.append(new String(new char[depth - 2]).replace("\0", " "));
                        writeLines.append("# ");
                        writeLines.append(comment);
                        writeLines.append("\n");
                    }
                }

                writeLines.append(line);
                writeLines.append("\n");
            }

            fileWriter.write(writeLines.toString());
        } catch (IOException e) {
            throw new YamlerConfigurationException("Could not save YML", e);
        }
    }

    private static String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }

        return sb.toString();
    }

    public void addComment(String key, String value) {
        if (!comments.containsKey(key)) {
            comments.put(key, new ArrayList<String>());
        }

        comments.get(key).add(value);
    }

    public void clearComments() {
        comments.clear();
    }
}
