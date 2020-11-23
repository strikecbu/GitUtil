package com.iisigroup.git.service.impl;

import com.iisigroup.git.Constants;
import com.iisigroup.git.Type;
import com.iisigroup.git.model.CommandResult;
import com.iisigroup.git.model.GitProject;
import com.iisigroup.git.service.GitCommandService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2020/11/3 AndyChen,new
 * </ul>
 * @since 2020/11/3
 */
public class GitCommandServiceImpl implements GitCommandService {

    private Logger logger = Logger.getLogger(GitCommandServiceImpl.class);

    protected final Properties properties;
    protected final File gitHomeFolder;
    protected List<GitProject> projects = new ArrayList<>();
    public static final String REPOS_INFO_NAME = "ReposInfo.repos";

    public GitCommandServiceImpl(Properties properties) {
        this.properties = properties;
        gitHomeFolder = new File(properties.getProperty(Constants.GIT_HOME_PATH));
        if(!gitHomeFolder.exists())
            throw new RuntimeException("Can not found git home, please set in sys.properties.");
        // initial project folder
        for (Constants.Repository repos : Constants.Repository.values()) {
            final GitProject project =
                    initialProjectSpace(repos.toString(),
                            properties.getProperty(repos.getProjectFolderPath()),
                            properties.getProperty(repos.getUrl()), properties.getProperty(repos.getUser()), properties.getProperty(repos.getPsw()));
            if(project != null)
                projects.add(project);
        }
    }

    private GitProject initialProjectSpace(String reposType, String projectPlaceFolderPath, String url, String userName, String userPsw) {
        if(projectPlaceFolderPath == null || "".equals(projectPlaceFolderPath) || url == null || "".equals(url))
            return null;

        final File projectFolder = new File(projectPlaceFolderPath);
        if (!projectFolder.exists())
            projectFolder.mkdirs();

        final GitProject gitProject = new GitProject(reposType, url, userName, userPsw, projectFolder);
        // init git
        String reg = "(https?://)(.*)";
        final Matcher matcher = Pattern.compile(reg).matcher(url);
        if (matcher.find()) {
            try {
                final String NameEncode = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString());
                final String PswEncode = URLEncoder.encode(userPsw, StandardCharsets.UTF_8.toString());
                url = matcher.group(1).concat(NameEncode).concat(":").concat(PswEncode).concat("@").concat(matcher.group(2));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //檢查裡面有沒有repository folder
        final List<File> collect = getRealFolder(projectFolder);
        if(collect.size() > 1)
            throw new RuntimeException("Project folder should not have some much folder in it.");
        File folder = collect.size() == 0 ? projectFolder : collect.get(0);
        gitProject.setProjectFolder(folder);
        final CommandResult result = gitCommand(gitHomeFolder, folder, new String[]{"status"});
        if (result.getResultCode() != 0) {
            //https://github.com/strikecbu/TestSvnMigrate.git
            final CommandResult cloneResult = gitCommand(gitHomeFolder, projectFolder, new String[]{"clone", url});
            if(cloneResult.getResultCode() != 0)
                throw new RuntimeException("Fail to clone repository.");
            final List<File> collect1 = getRealFolder(projectFolder);
            if(collect1.size() > 1)
                throw new RuntimeException("Project folder should be one folder in it after git init.");
            folder = collect1.get(0);
            gitProject.setProjectFolder(folder);
        } else {
            // 更新到最新
            resetToHead(gitProject, "master");
            pullRemote(gitProject);
        }
        // windows 中文encode問題
        gitCommand(gitHomeFolder, projectFolder,  new String[]{"config", "--local", "core.quotePath", "off"});


        return gitProject;
    }

    private List<File> getRealFolder(File projectFolder) {
        return Arrays.stream(Objects.requireNonNull(projectFolder.listFiles())).filter(file -> {
            final String name = file.getName();
            if (".git".equals(name))
                return false;
            if (".DS_Store".equals(name))
                return false;
            return true;
        }).collect(Collectors.toList());
    }

    private CommandResult resetToHead(GitProject project, String branch) {
        CommandResult result = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"reset", "--hard", "origin/" + branch});
        if(result.getResultCode() != 0)
            throw new RuntimeException("Fail to reset repository. " + result.getErrorMsg());
        CommandResult cleanResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"clean", "-fd"});
        if(cleanResult.getResultCode() != 0)
            throw new RuntimeException("Fail to clean repository. " + cleanResult.getErrorMsg());
        return result;
    }

    private CommandResult resetToTargetVersion(GitProject project, String versionHash) {
        CommandResult result = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"reset", "--hard", versionHash});
        if(result.getResultCode() != 0)
            throw new RuntimeException(String.format("Fail to reset repository to target version %s, msg: %s.",
                    versionHash, result.getErrorMsg()));
        return result;
    }

    private CommandResult pullRemote(GitProject project) {
        String url = urlEncode(project);
        CommandResult result = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"pull", url});
        if(result.getResultCode() != 0)
            throw new RuntimeException("Fail to pull repository. " + result.getErrorMsg());
        return result;
    }

    private String urlEncode(GitProject project) {
        String url = project.getUrl();
        String reg = "(https?://)(.*)";
        final Matcher matcher = Pattern.compile(reg).matcher(url);
        if (matcher.find()) {
            try {
                final String NameEncode = URLEncoder.encode(project.getUserName(), StandardCharsets.UTF_8.toString());
                final String PswEncode = URLEncoder.encode(project.getUserPsw(), StandardCharsets.UTF_8.toString());
                url = matcher.group(1).concat(NameEncode).concat(":").concat(PswEncode).concat("@").concat(matcher.group(2));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }



    private GitProject pickProjectByPathFileOrUrl(String sSvnPathFile) {
        GitProject gitProject = null;
        for (GitProject project : projects) {
            if (sSvnPathFile.contains(project.getUrl())) {
                gitProject = project;
                break;
            }
        }
        if (gitProject == null) {
            throw new RuntimeException("Can NOT found any git project model, init process maybe wrong.");
        }
        return gitProject;
    }

    private File getGitTargetFile(GitProject project, String sSvnPathFile) {
        String url = project.getUrl();
        String reg = "^" + url + "/?(.*)$";
        Matcher matcher = Pattern.compile(reg).matcher(sSvnPathFile);
        if (matcher.find()) {
            return new File(project.getProjectFolder(), matcher.group(1));
        } else {
            throw new RuntimeException("Can NOT match path to project folder: " + sSvnPathFile);
        }
    }

    public String commitFileAndPush(GitProject project, File commitFile, String commitMsg) {
        if(commitMsg == null || "".equals(commitMsg)) {
            logger.info("CommitMsg is empty, use 'no comment' instead.");
            commitMsg = "no comment";
        }
        CommandResult addResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"add", commitFile.getAbsolutePath()});
        if (addResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to add file to stage." + addResult.getErrorMsg());
        }

        CommandResult commitResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"commit", "-m", commitMsg});
        if (commitResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to commit file." + commitResult.getErrorMsg());
        }

        CommandResult logResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"log", "-1"});
        if (logResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to get log." + logResult.getErrorMsg());
        }

        String hash = logResult.getOutputMsg().toString().split(System.lineSeparator())[0];
        Matcher matcher = Pattern.compile("^commit\\s(.*)$").matcher(hash);
        if (matcher.find()) {
            hash = matcher.group(1);
        } else {
            throw new RuntimeException("Can NOT get commit hash." + logResult.getOutputMsg());
        }

        CommandResult pushResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"push", urlEncode(project)});
        if (pushResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to push branch." + pushResult.getErrorMsg());
        }
        return hash;
    }

    @Override
    public String importToSVN(String sSvnUser, String sSvnPsw, String sSvnPathFile, File fOriginPathFile,
                              String sDescription) throws Exception {

        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnPathFile);
        // 先同步project
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        File gitTargetFile = getGitTargetFile(gitProject, sSvnPathFile);
        FileUtils.copyFile(fOriginPathFile, gitTargetFile, true);

        return commitFileAndPush(gitProject, gitTargetFile, sDescription);
    }

    @Override
    public void checkoutEmpFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, File fLocalDir) {
        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnUrl);
        // 先同步project
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        //建立資料夾
        if (!fLocalDir.exists()) {
            fLocalDir.mkdirs();
        }
        // 要記錄對應的repos資訊
        writeReposInfo(sSvnUrl, fLocalDir);
    }

    private void writeReposInfo(String sSvnUrl, File fLocalDir) {
        File file = new File(fLocalDir, REPOS_INFO_NAME);
        try {
            logger.info("Write repos info to " + fLocalDir.getAbsolutePath());
            FileUtils.writeStringToFile(file, sSvnUrl, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Fail to write repos info file.");
        }
    }

    @Override
    public void updateToWC(String sSvnUser, String sSvnPsw, File fLocalFile, Type reposType) throws Exception {
        GitProject gitProject = matchProject(reposType.getType());
        // 先同步project
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        // 取出target file to fLocal
        File file = matchGitAndLocalFile(gitProject.getProjectFolder(), fLocalFile, gitProject);
        FileUtils.copyFile(file, fLocalFile, true);
    }

    private GitProject matchProject(String reposType) {
        GitProject gitProject = null;
        for (GitProject project : projects) {
            if (project.getReposType().equals(reposType)) {
                gitProject = project;
                break;
            }
        }
        if (gitProject == null) {
            throw new RuntimeException("Can NOT found specific project: " + reposType);
        }
        return gitProject;
    }

    /**
     * 對應git folder and local temp folder，取出git folder中的file
     * @param gitProjectFolder git base folder
     * @param localFile local file
     * @param project
     * @return file in git folder
     */
    private File matchGitAndLocalFile(File gitProjectFolder, File localFile, GitProject project) {
        File info = new File(localFile.getParentFile(), REPOS_INFO_NAME);
        List<File> fileList = new ArrayList<>();
        if (info.exists()) {
            try {
                final String infoStr = FileUtils.readFileToString(info, StandardCharsets.UTF_8);
                String gitFilePath = infoStr.replaceAll("^(.*?)[/\\\\]?$", "$1").concat(File.separator).concat(localFile.getName());
                String filePath = gitFilePath.replace(project.getUrl(), "");
                fileList = catchAllFiles(gitProjectFolder).stream()
                        .filter(file -> {
                            String path = file.getAbsolutePath().replace(gitProjectFolder.getAbsolutePath(), "");
                            return filePath.equals(path);
                        }).collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException("Can NOT read info file cause: " + e);
            }
        } else {
            fileList = catchAllFiles(gitProjectFolder).stream()
                .filter(file -> {
                    String path = file.getAbsolutePath();
                    path = path.replace(gitProjectFolder.getAbsolutePath(), "");
                    Matcher matcher = Pattern.compile("^.*" + path + "$").matcher(localFile.getAbsolutePath());
                    return matcher.find();
                }).collect(Collectors.toList());
        }
        if (fileList.size() != 1) {
            throw new RuntimeException(String.format("It should be only one file. But found %s, file: %s", fileList.size(), localFile.getAbsolutePath()));
        }
        return fileList.get(0);
    }

    private List<File> catchAllFiles(File folder) {
        List<File> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                list.addAll(catchAllFiles(file));
            } else {
                list.add(file);
            }
        }
        return list;
    }

    @Override
    public String commitToSVN(String sSvnUser, String sSvnPsw, File fLocalFile, String sDescription, Type reposType) throws Exception {
        GitProject gitProject = matchProject(reposType.getType());
        // 先同步project
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        File gitFile = matchGitAndLocalFile(gitProject.getProjectFolder(), fLocalFile, gitProject);
        FileUtils.copyFile(fLocalFile, gitFile, true);

        return commitFileAndPush(gitProject, gitFile, sDescription);
    }

    @Override
    public String getFileRevision(String sSvnUser, String sSvnPsw, String sSvnPathFile) {
        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnPathFile);
        resetToHead(gitProject, "master");
        pullRemote(gitProject);
        File gitTargetFile = getGitTargetFile(gitProject, sSvnPathFile);
//        git log -p --name-status /Users/maiev/Documents/iisi/CTBC_GIT_TEST/dev_project/TestSvnMigrate/testFile6958.txt
        CommandResult logResult = gitCommand(gitHomeFolder, gitProject.getProjectFolder(), new String[]{"log",
                "-p", "--name-status", gitTargetFile.getAbsolutePath()});
        if (logResult.getResultCode() != 0) {
            throw new RuntimeException(String.format("Can not get version from %s, message: s", gitTargetFile.getAbsolutePath(), logResult.getErrorMsg()));
        }
        String lineOne = logResult.getOutputMsg().toString().split(System.lineSeparator())[0];
        return lineOne.split("\\s")[1];
    }

    @Override
    public void exportFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile, File fLocalDir,
                              String sSvnRev) throws Exception {
        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnUrl);
        // 更新
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        resetToTargetVersion(gitProject, sSvnRev);
        sSvnPathFile = sSvnUrl + sSvnPathFile;
        File gitTargetFile = getGitTargetFile(gitProject, sSvnPathFile);

        FileUtils.copyFileToDirectory(gitTargetFile, fLocalDir, true);
    }

    @Override
    public void diff(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile, String sLocalFile,
                     String sDiffFile, String sSvnRev, String sBasUatRev) throws Exception {
        //sDiffFile no use
        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnUrl);
        // 更新
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        //git diff 48d83 454417da9 /Users/maiev/Documents/iisi/CTBC_GIT_TEST/dev_project/TestSvnMigrate/testFile79713.txt
        sSvnPathFile = sSvnUrl + sSvnPathFile;
        File gitTargetFile = getGitTargetFile(gitProject, sSvnPathFile);
        CommandResult diffResult = gitCommand(gitHomeFolder, gitProject.getProjectFolder(),
                new String[]{"diff", sBasUatRev, sSvnRev, gitTargetFile.getAbsolutePath()});
        if (diffResult.getResultCode() != 0) {
            throw new RuntimeException(String.format("Can not get diff from %s to %s, file: %s", sBasUatRev, sSvnRev, gitTargetFile.getAbsolutePath()));
        }
        StringBuffer outputMsg = diffResult.getOutputMsg();
        FileUtils.writeStringToFile(new File(sDiffFile), outputMsg.toString());
    }

    @Override
    public void backup(String sSvnUser, String sSvnPsw, String sSvnUrl, String sUATDir, String sUATSubDir,
                       String sDelDir) throws Exception {
        GitProject project = pickProjectByPathFileOrUrl(sSvnUrl);
        // 更新
        resetToHead(project, "master");
        pullRemote(project);

        File uatFolder = new File(project.getProjectFolder(), sUATDir);
        File uatCopyFolder = new File(project.getProjectFolder(), sDelDir);
        if (!uatFolder.exists()) {
            logger.info("Not found any uat folder, skip copy to uat copy folder.");
            // 還沒備份過
            return;
        }
        for (File file : Objects.requireNonNull(uatFolder.listFiles())) {
            if (file.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(file, uatCopyFolder);
            } else {
                FileUtils.copyFileToDirectory(file, uatCopyFolder, true);
            }
        }
        FileUtils.deleteDirectory(uatFolder);
        CommandResult addResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"add", "."});
        if (addResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to add file to stage." + addResult.getErrorMsg());
        }

        String commitMsg = "backup uat to uat_copy folder";
        CommandResult commitResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"commit", "-m", commitMsg});
        if (commitResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to commit file." + commitResult.getErrorMsg());
        }

        CommandResult pushResult = gitCommand(gitHomeFolder, project.getProjectFolder(), new String[]{"push", urlEncode(project)});
        if (pushResult.getResultCode() != 0) {
            throw new RuntimeException("Fail to push branch." + pushResult.getErrorMsg());
        }
    }

    @Override
    public void checkoutFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, File fLocalDir) throws Exception {
        GitProject gitProject = pickProjectByPathFileOrUrl(sSvnUrl);
        // 更新
        resetToHead(gitProject, "master");
        pullRemote(gitProject);

        // 要記錄對應的repos資訊
        writeReposInfo(sSvnUrl, fLocalDir);

        File targetFolder = new File(gitProject.getProjectFolder(), sSvnUrl.replace(gitProject.getUrl(), ""));
        if (targetFolder.exists()) {
            for (File file : Objects.requireNonNull(targetFolder.listFiles())) {
                if (file.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(file, fLocalDir);
                } else {
                    FileUtils.copyFileToDirectory(file, fLocalDir, true);
                }
            }
        }
    }
}
