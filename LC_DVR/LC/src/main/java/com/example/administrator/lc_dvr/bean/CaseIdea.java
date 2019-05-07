package com.example.administrator.lc_dvr.bean;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/08/06
 *   desc   :
 *  version :
 * </pre>
 */
public class CaseIdea {
    private String idea;
    private String username;
    private String createtime;
    private String userkind;
    private int id;
    private String failkind;
    private String fromstatus;
    private String tostatus;
    private String actiondone ;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }


    public String getUserkind() {
        return userkind;
    }

    public void setUserkind(String userkind) {
        this.userkind = userkind;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getFailkind() {
        return failkind;
    }

    public void setFailkind(String failkind) {
        this.failkind = failkind;
    }

    public String getFromstatus() {
        return fromstatus;
    }

    public void setFromstatus(String fromstatus) {
        this.fromstatus = fromstatus;
    }

    public String getTostatus() {
        return tostatus;
    }

    public void setTostatus(String tostatus) {
        this.tostatus = tostatus;
    }

    public String getActiondone() {
        return actiondone;
    }

    public void setActiondone(String actiondone) {
        this.actiondone = actiondone;
    }
}
