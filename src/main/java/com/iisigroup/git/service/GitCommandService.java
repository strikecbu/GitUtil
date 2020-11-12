package com.iisigroup.git.service;


import com.iisigroup.git.CommandLineUtil;
import com.iisigroup.git.model.CommandResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2020/11/3 AndyChen,new
 * </ul>
 * @since 2020/11/3
 */
public interface GitCommandService {

    String DEV_TYPE = "DEV";
    String UAT_TYPE = "UAT";
    String ACCUMULATE_TYPE = "ACCUMULATE";

    String importToSVN(String sSvnUser, String sSvnPsw, String sSvnPathFile, File fOriginPathFile, String sDescription) throws Exception;

    void checkoutEmpFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, File fLocalDir) throws Exception;

    void updateToWC(String sSvnUser, String sSvnPsw, File fLocalFile, String reposType) throws Exception;

    String commitToSVN(String sSvnUser, String sSvnPsw, File fLocalFile, String sDescription, String reposType) throws Exception;

    String getFileRevision(String sSvnUser, String sSvnPsw, String sSvnPathFile) throws Exception;

    void exportFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile, File fLocalDir, String sSvnRev) throws Exception;

    void diff(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile, String sLocalFile,
                            String sDiffFile, String sSvnRev, String sBasUatRev) throws Exception;

    void backup(String sSvnUser, String sSvnPsw, String sSvnUrl, String sUATDir, String sUATSubDir,
                              String sDelDir) throws Exception;


    void checkoutFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, File fLocalDir)
            throws Exception;

    default CommandResult gitCommand(File gitHomeFolder, File folderPath, String[] gitCommand) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        ArrayList<String> commands = new ArrayList<>();

        //init
        String gitStartCommand;
        if (isWindows) {
            if(gitHomeFolder == null || !gitHomeFolder.exists())
                throw new IllegalArgumentException("gitHomePath must can not be empty");
            gitStartCommand = gitHomeFolder.getAbsolutePath() + "\\bin\\git.exe";
        } else {
            gitStartCommand = "git";
        }
        commands.add(gitStartCommand);
        commands.addAll(Arrays.asList(gitCommand));

        return CommandLineUtil.commonCommand(folderPath, commands.toArray(new String[]{}));
    }

}
