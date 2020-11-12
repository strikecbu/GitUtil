package com.iisigroup.git.model;

import java.io.File;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2020/11/4 AndyChen,new
 * </ul>
 * @since 2020/11/4
 */
public class GitProject {
    private String reposType;
    private String url;
    private String userName;
    private String userPsw;
    private File projectFolder;

    public GitProject(String reposType, String url, String userName, String userPsw, File projectFolder) {
        this.reposType = reposType;
        this.url = url;
        this.userName = userName;
        this.userPsw = userPsw;
        this.projectFolder = projectFolder;
    }

    public String getReposType() {
        return reposType;
    }

    public void setReposType(String reposType) {
        this.reposType = reposType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPsw() {
        return userPsw;
    }

    public void setUserPsw(String userPsw) {
        this.userPsw = userPsw;
    }

    public File getProjectFolder() {
        return projectFolder;
    }

    public void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder;
    }
}
