package com.iisigroup.git;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2020/11/24 AndyChen,new
 * </ul>
 * @since 2020/11/24
 */
public class ProdMove {

    @Test
    public void moveProdJarToSource() throws IOException {
        File jarFile = new File("/Users/maiev/Documents/iisi/CTBC_GIT/myutil/GitUtil/target/git-util-1" +
                ".0-jar-with-dependencies.jar");
        for (File file : new File("/Users/maiev/Documents/iisi/CTBC_GIT/source/").listFiles()) {
            if(!file.isDirectory() || !file.getName().startsWith("R14_"))
                continue;

            File libFolder = new File(file, "lib");
            FileUtils.copyFileToDirectory(jarFile, libFolder, true);
        }
    }

    @Test
    public void cleanSource() throws IOException {
        for (File file : new File("/Users/maiev/Documents/iisi/CTBC_GIT/source/").listFiles()) {
            if(!file.isDirectory() || !file.getName().startsWith("R14_"))
                continue;

            String[] needsDel = {"default","classes", "log", "dev_project", "uat_project", "prod_project"};
            for (File checkFile : file.listFiles()) {
                if (Arrays.asList(needsDel).contains(checkFile.getName())) {
                    if(checkFile.isDirectory())
                        FileUtils.deleteDirectory(checkFile);
                    else
                        FileUtils.forceDelete(checkFile);
                }
            }
        }
    }
}
