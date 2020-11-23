package com.iisigroup.git;

import com.iisigroup.git.service.GitCommandService;
import com.iisigroup.git.service.impl.GitCommandServiceImpl;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

public class Git {

	private static final Logger logger = Logger.getLogger(Git.class);

	private static GitCommandService service;
	private static boolean isSetEnv = false;

	public Git() {
		throw new RuntimeException("You should not do this.");
	}

	public static void setEnv(Properties properties) {
		File logConfig = new File("./properties/Log4j.properties");
		Properties logProp = new Properties();
		if(!logConfig.exists()) {
			System.out.println("Tool had no prepare log4j config, use embed config.");
			try(InputStream stream = Git.class.getClassLoader().getResourceAsStream("properties/Log4j" +
					".properties");) {
				logProp.load(stream);
				PropertyConfigurator.configure(logProp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			File versionFile = new File(Objects.requireNonNull(Git.class.getClassLoader().getResource("version")).toURI());
			String version = FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8);
			logger.info("Git util version Info: " + version);
		} catch (URISyntaxException | IOException e) {
			logger.warn("Can NOT read version info.", e);
		}

		logger.info("GitUtil Loading properties...");
		//處理git source
		String path = properties.getProperty(Constants.GIT_HOME_PATH);
		File gitFolder = new File(path);
		File gitExe = new File(gitFolder, "bin" + File.separator + "git.exe");
		if(!gitExe.exists()) {
			throw new RuntimeException("Can not found git.ext! Make sure your property sGitHomePath is correct.");
		}
		// 處理密碼轉換
		String pswBase64 = properties.getProperty(Constants.GIT_PSW);
		String decodePsw = new String(Base64.getDecoder().decode(pswBase64), StandardCharsets.UTF_8);
		properties.setProperty(Constants.GIT_PSW, decodePsw);
		service = new GitCommandServiceImpl(properties);
		isSetEnv = true;
	}


	/**
	 * 回傳值由long -> String
	 * @param sSvnUser
	 * @param sSvnPsw
	 * @param sSvnPathFile
	 * @param fOriginPathFile
	 * @param sDescription
	 * @return commit hash
	 * @throws Exception
	 */
	public static String importToSVN(String sSvnUser, String sSvnPsw, String sSvnPathFile,
	        File fOriginPathFile, String sDescription) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");

		return service.importToSVN(sSvnUser, sSvnPsw, sSvnPathFile, fOriginPathFile, sDescription);
	}

	public static void checkoutEmpFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl,
	        File fLocalDir) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.checkoutEmpFromSVN(sSvnUser, sSvnPsw, sSvnUrl, fLocalDir);
	}

	/**
	 * 修改過後的method額外傳入reposType，才能確定是要更新哪一個repository的資料
	 * @param sSvnUser
	 * @param sSvnPsw
	 * @param fLocalFile
	 * @param reposType GitCommandService.XXX_TYPE
	 * @throws Exception
	 */
	public static void updateToWC(String sSvnUser, String sSvnPsw, File fLocalFile, Type reposType)
	        throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.updateToWC(sSvnUser, sSvnPsw, fLocalFile, reposType);
	}

	/**
	 * 修改過後的method額外傳入reposType，才能確定是要更新哪一個repository的資料
	 * 回傳值由long -> String
	 * @param sSvnUser
	 * @param sSvnPsw
	 * @param fLocalFile
	 * @param sDescription
	 * @param reposType GitCommandService.XXX_TYPE
	 * @return commit hash
	 * @throws Exception
	 */
	public static String commitToSVN(String sSvnUser, String sSvnPsw, File fLocalFile,
									 String sDescription, Type reposType) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		return service.commitToSVN(sSvnUser, sSvnPsw, fLocalFile, sDescription, reposType);
	}

	/**
	 * 回傳值由long -> String
	 * @param sSvnUser
	 * @param sSvnPsw
	 * @param sSvnPathFile
	 * @return commit hash
	 * @throws Exception
	 */
	public static String getFileRevision(String sSvnUser, String sSvnPsw, String sSvnPathFile)
	        throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		return service.getFileRevision(sSvnUser,sSvnPsw, sSvnPathFile);
	}

	public static void exportFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile,
									 File fLocalDir, String sSvnRev) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.exportFromSVN(sSvnUser, sSvnPsw, sSvnUrl, sSvnPathFile, fLocalDir, sSvnRev);
	}

	public static void diff(String sSvnUser, String sSvnPsw, String sSvnUrl, String sSvnPathFile, String sLocalFile,
							String sDiffFile, String sSvnRev, String sBasUatRev) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.diff(sSvnUser, sSvnPsw, sSvnUrl, sSvnPathFile, sLocalFile, sDiffFile, sSvnRev, sBasUatRev);
	}

	public static void backup(String sSvnUser, String sSvnPsw, String sSvnUrl, String sUATDir, String sUATSubDir,
							  String sDelDir) throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.backup(sSvnUser, sSvnPsw, sSvnUrl, sUATDir, sUATSubDir, sDelDir);
	}

	public static void checkoutFromSVN(String sSvnUser, String sSvnPsw, String sSvnUrl, File fLocalDir)
			throws Exception {
		if (!isSetEnv)
			throw new RuntimeException("Make sure setEnv method should been executed.");
		service.checkoutFromSVN(sSvnUser, sSvnPsw, sSvnUrl, fLocalDir);
	}
}
