package com.iisigroup.git;


import com.iisigroup.git.service.GitCommandService;
import com.iisigroup.git.service.impl.GitCommandServiceImpl;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class GitTest {

    @Test
    public void test_git() throws Exception {

        long startLong = System.currentTimeMillis();
        final Properties properties = new Properties();
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prop/sys.properties")){
            properties.load(inputStream);
        }
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prop/Log4j.properties")){
            Properties logp = new Properties();
            logp.load(inputStream);
            PropertyConfigurator.configure(logp);
        }

        Git.setEnv(properties);

        final String fileName = "testFile" + new Random().nextInt(100000) + ".txt";
        final String sSvnUrl = "https://github.com/strikecbu/TestSvnMigrate.git";
//		final String sSvnUrl = "svn://127.0.0.1:3690/repo";
        final String sSvnUser = "andy";
        final String sSvnPsw = "andy1234";
        final String testFolder = "/Users/maiev/Downloads/testSVN2/repo";
        final File file = genFile(new File(testFolder), fileName);
        final String sSvnPathFile = sSvnUrl + "/" + fileName;
        System.out.println("Gen file: " + file.getName());
        final String commitMsg = "autoimport123";

        // 一定要是新檔案才能import
//		final long svn = Git.importToSVN(sSvnUser, sSvnPsw, sSvnPathFile, file, commitMsg);
        String hash = Git.importToSVN(sSvnUser, sSvnPsw, sSvnPathFile, file, commitMsg);
        System.out.println(hash);


        final File fLocalDir = new File(testFolder);
        deleteFolder(fLocalDir.getParentFile());
        System.out.println(" === checkoutEmpFromSVN ===");
        // 將指定目錄checkout成空的 svn資料夾
        Git.checkoutEmpFromSVN(sSvnUser, sSvnPsw, sSvnUrl, fLocalDir);

        System.out.println("=== updateToWC ===");
        // 將指定的檔案拉下來到資料夾內
        Git.updateToWC(sSvnUser, sSvnPsw, file, Type.DEV_TYPE);
        // 將要更新的檔案放進來
        genFile(new File(testFolder), fileName);
        System.out.println("=== commitToSVN ===");
//		// commit to SVN
        final String svn1 = Git.commitToSVN(sSvnUser, sSvnPsw, file, commitMsg, Type.DEV_TYPE);
        System.out.println("svn1: " + svn1);

        // 取得剛剛commit的revision
        final String fileRevision = Git.getFileRevision(sSvnUser, sSvnPsw, sSvnPathFile);
        System.out.println("fileRevision: " + fileRevision + ", latest update revision: " + svn1);

        System.out.println("=== diff ===");
        // diff file with 2 revision
        final String resultPath = new File("/Users/maiev/Downloads/testSVN", fileName).getAbsolutePath();
        Git.diff(sSvnUser, sSvnPsw, sSvnUrl, "/" + fileName, file.getAbsolutePath(), resultPath,
                svn1, hash);

        deleteFolder(fLocalDir.getParentFile()); // 全部清空
        System.out.println("=== checkoutFromSVN ===");
        // checkout all file from repository
        Git.checkoutFromSVN(sSvnUser, sSvnPsw, sSvnUrl, fLocalDir);

        deleteFolder(fLocalDir.getParentFile()); // 全部清空
        System.out.println("=== exportFromSVN ===");
        // checkout single file from repository
        Git.exportFromSVN(sSvnUser, sSvnPsw,sSvnUrl, "/" + fileName, fLocalDir, String.valueOf(svn1));


        // copy uat
        final String sUATDir = "ReleaseToUAT";
        final String sUATZDir = "Z-backup-ReleaseToUAT";
        final String sSvnUrl_uat = "https://github.com/strikecbu/TestSvnMigrate.git";
        System.out.println("=== backup ===");
        // backup 不支援直接存放在根目錄的檔案，會錯
        Git.backup(sSvnUser, sSvnPsw, sSvnUrl_uat, sUATDir, "", sUATZDir);

        // import
        final String testFolder2 = "/Users/maiev/Downloads/testSVN3/uat";
        final File uat_folder = new File(testFolder2, sUATDir);
        final File file2 = genFile(new File(uat_folder, String.valueOf(new Random().nextInt(100000))), fileName);
        final String sSvnPathFile2 = getSvnPath(sSvnUrl_uat, file2, testFolder2);
        System.out.println("Gen file2: " + file2.getName());

        // 一調要是新檔案才能import
        System.out.println("=== importToSVN ===");
        final String svn2 = Git.importToSVN(sSvnUser, sSvnPsw, sSvnPathFile2, file2, commitMsg);
        System.out.println(svn2);


        final File file3 = genFile(new File(uat_folder, "staticFolder"), "static.txt");
        final String sSvnPathFile3 = getSvnPath(sSvnUrl_uat, file3, testFolder2);
        System.out.println("Gen file3: " + file3.getName());

        // 一調要是新檔案才能import
        System.out.println("=== importToSVN ===");
        final String svn3 = Git.importToSVN(sSvnUser, sSvnPsw, sSvnPathFile3, file2, commitMsg);
        System.out.println(svn3);

        long finishLong = System.currentTimeMillis();
        System.out.printf("Cast %s secs %n", (finishLong - startLong)/1000);

    }

    private String getSvnPath(String svnUrl, File localFile, String baseFolderPath) {
        String replace = localFile.getAbsolutePath().replace(baseFolderPath, "");
//		final String keyWord = svnUrl.substring(svnUrl.lastIndexOf("/") + 1);
//		final String s = localFile.getAbsolutePath().substring(localFile.getAbsolutePath().indexOf(keyWord)).replaceAll(keyWord, "");
        return svnUrl.concat(replace);
    }

    private void deleteFolder(File folder ) {
        for (File listFile : folder.listFiles()) {
            if(listFile.isDirectory()) {
                deleteFolder(listFile);
                try {
                    FileUtils.deleteDirectory(listFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileUtils.forceDelete(listFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static File genFile(File folder, String fileName) {
        final File file = new File(folder, fileName);
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if(!file.exists())
                file.createNewFile();
            try (final FileWriter fileWriter = new FileWriter(file)) {
                final String str = new Date().toString();
                System.out.println("Write to file word: " + str);
                fileWriter.write(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}