package club.coolcat.common.dao;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.*;
import java.util.*;

public class MybatisGeneratorUtil {
    static String xmlRelativeDir = "/src/test/resources/xml";
    static String xmlFileSnapShotRelativeDir = "/src/test/resources/xml_file_snapshot.txt";
    private Map<String, Long> xmlFileSnapshot;
    private Map<String, Long> generatedFileSnapshot;

    public MybatisGeneratorUtil() {
    }

    public Map<String, Long> scanDir(String dirName) {
        Map<String, Long> snapshot = new HashMap<String, Long>();
        File dir = null;
        try {
            dir = new File(dirName);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    snapshot.put(file.getName(), file.lastModified());
                }
            } else {
                snapshot.put(dir.getName(), dir.lastModified());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return snapshot;
    }

    public Map<String, Long> scanFile(String fileLoc) {
        Map<String, Long> snapshot = new HashMap<String, Long>();
        File file = new File(fileLoc);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] records = line.split(" ");
                if (records.length == 2) {
                    snapshot.put(records[0], Long.valueOf(records[1]));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return snapshot;
    }

    public void saveMap(Map<String, Long> snapshot, String filename) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
                bw.write(entry.getKey() + " " + entry.getValue().toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getXmlDir(Class<? extends Object> clazz) {
        String xmlDir = getProjectPath(clazz) + xmlRelativeDir;
        return xmlDir;
    }

    public String getXmlFileSnapShotDir(Class<? extends Object> clazz) {
        String xmlDir = getProjectPath(clazz) + xmlFileSnapShotRelativeDir;
        return xmlDir;
    }

    public String getProjectPath(Class<? extends Object> clazz) {
        File thisfilepath = new File(clazz.getClass().getResource("/").getPath());
        String parent = thisfilepath.getParent();
        String projectPath = parent.substring(0, parent.lastIndexOf(File.separator));
        return projectPath;
    }

    public void generateAll(Class<? extends Object> clazz) throws Throwable {
        String dir = getXmlDir(clazz);
        String snapshotFile = getXmlFileSnapShotDir(clazz);
        System.setProperty("xml.dir", dir);
        System.setProperty("user.dir", getProjectPath(clazz));

        generatedFileSnapshot = scanFile(snapshotFile);

        xmlFileSnapshot = scanDir(dir);

        Map<String, Long> newSnapshot = new HashMap<String, Long>();
        for (Map.Entry<String, Long> entry : xmlFileSnapshot.entrySet()) {
            newSnapshot.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Long> entry : generatedFileSnapshot.entrySet()) {
            if (xmlFileSnapshot.containsKey(entry.getKey())) {
                // 这里有个问题
                // xml_file_snapshot.txt 中的 时间大于 生成的xml的本地创建时间 才跳过;
                // 这样会出现一个问题，本地更新了非本人生成的xml，本地的创建时间会大于xml_file_snapshot.txt中的时间戳，
                // 会重新生成xml_file_snapshot.txt已经存在的表的代码
                // 故注释掉 2019-05-23
//                if (xmlFileSnapshot.get(entry.getKey()).longValue() <= entry.getValue().longValue()) {
                System.out.println(entry.getKey() + " was skipped.");
                xmlFileSnapshot.remove(entry.getKey());
//                }
            }
        }

        saveMap(newSnapshot, snapshotFile);

        if (xmlFileSnapshot.size() > 0) {
            System.out.println("files to be used for generating: ");
            String[] xmls = new String[xmlFileSnapshot.size()];
            xmlFileSnapshot.keySet().toArray(xmls);
            System.out.println(Arrays.toString(xmls));
            generate(xmls);
        }
    }

    @Deprecated
    public void generate1(String... xmllist) throws Throwable {
        if (null != xmllist) {
            for (int i = 0; i < xmllist.length; ++i) {
                List<String> warnings = new ArrayList();
                boolean overwrite = true;
                String path = (new File("./src/main/resources/")).getAbsolutePath() + "/" + xmllist[i];
                System.out.println(path);
                File configFile = new File(path);
                ConfigurationParser cp = new ConfigurationParser(warnings);
                Configuration config = cp.parseConfiguration(configFile);
                DefaultShellCallback callback = new DefaultShellCallback(overwrite);
                MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
                myBatisGenerator.generate((ProgressCallback) null);
            }

            System.out.println("生成结束");
        }
    }

    public void generate(String... xmllist) throws Throwable {
        if (null == xmllist) {
            return;
        }

        for (int i = 0; i < xmllist.length; i++) {
            List<String> warnings = new ArrayList<String>();
            boolean overwrite = true;
            String path = System.getProperty("xml.dir") + "/" + xmllist[i];
            System.out.println(path);
            File configFile = new File(path);
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
        }

        System.out.println("生成结束");
    }

    /*public static void main(String[] args) {
        Object obj = null;
        System.out.println("".equals(obj));

        //        String homeDir = "E:\\5project\\idea\\201801\\";
    //        String xmlDir = homeDir + "java-panda-crm-admin-dubbo/crm-admin-dao/src/test/resources/xml";
    //        String snapshotFileLoc = homeDir + "java-panda-crm-admin-dubbo/crm-admin-dao/src/test/resources/xml_file_snapshot.txt";
    //        System.setProperty("xml.dir", xmlDir);
    //        System.setProperty("user.dir", homeDir + "java-panda-crm-admin-dubbo/crm-admin-dao");
    //        new MybatisGeneratorUtil().generateAll(snapshotFileLoc, xmlDir);
    //        new MybatisGeneratorUtil().generate1("t_template.xml");
    }*/
}
