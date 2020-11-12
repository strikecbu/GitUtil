package com.iisigroup.git.model;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2019-05-15 AndyChen,new
 * </ul>
 * @since 2019-05-15
 */
public class CommandResult {
    private String commandStr;
    private Integer resultCode;
    private StringBuffer outputMsg = new StringBuffer();
    private StringBuffer errorMsg = new StringBuffer();

    public String getCommandStr() {
        return commandStr;
    }

    public void setCommandStr(String commandStr) {
        this.commandStr = commandStr;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public StringBuffer getOutputMsg() {
        return outputMsg;
    }

    public void setOutputMsg(StringBuffer outputMsg) {
        this.outputMsg = outputMsg;
    }

    public StringBuffer getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(StringBuffer errorMsg) {
        this.errorMsg = errorMsg;
    }
}
