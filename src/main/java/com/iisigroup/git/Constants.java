package com.iisigroup.git;

public interface Constants {

    String GIT_DEV_PROJECT_FOLDER_PATH = "sGitDevProjectFolder";
    String GIT_SIT_PROJECT_FOLDER_PATH = "sGitSitProjectFolder";
    String GIT_UAT_PROJECT_FOLDER_PATH = "sGitUatProjectFolder";
    String GIT_PROD_PROJECT_FOLDER_PATH = "sGitProdProjectFolder";
    String GIT_ACCUMULATE_PROJECT_FOLDER_PATH = "sGitAccumulateProjectFolder";
    String GIT_USER = "sGitUser";
    String GIT_PSW = "sGitPsw";
    String GIT_DEV_URL = "sGit_dev_url";
    String GIT_SIT_URL = "sGit_sit_url";
    String GIT_UAT_URL = "sGit_uat_url";
    String GIT_PROD_URL = "sGit_prod_url";
    String GIT_ACCUMULATE_URL = "sGit_accumulate_url";
    String GIT_HOME_PATH = "sGitHomePath";

    enum Repository {
        DEV(GIT_USER, GIT_PSW, GIT_DEV_URL, GIT_DEV_PROJECT_FOLDER_PATH),
        SIT(GIT_USER, GIT_PSW, GIT_SIT_URL, GIT_SIT_PROJECT_FOLDER_PATH),
        UAT(GIT_USER, GIT_PSW, GIT_UAT_URL, GIT_UAT_PROJECT_FOLDER_PATH),
        PROD(GIT_USER, GIT_PSW, GIT_PROD_URL, GIT_PROD_PROJECT_FOLDER_PATH),
        ACCUMULATE(GIT_USER, GIT_PSW, GIT_ACCUMULATE_URL, GIT_ACCUMULATE_PROJECT_FOLDER_PATH);

        private final String user;
        private final String psw;
        private final String url;
        private final String projectFolderPath;

        Repository(String user, String psw, String url, String projectFolderPath) {
            this.user = user;
            this.psw = psw;
            this.url = url;
            this.projectFolderPath = projectFolderPath;
        }

        public String getUser() {
            return user;
        }

        public String getPsw() {
            return psw;
        }

        public String getUrl() {
            return url;
        }

        public String getProjectFolderPath() {
            return projectFolderPath;
        }
    }
}
