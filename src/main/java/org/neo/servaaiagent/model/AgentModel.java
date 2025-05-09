package org.neo.servaaiagent.model;

import java.util.Date;

import org.neo.servaframe.model.VersionEntity;

public class AgentModel {
    public static class UserAccount {
        public static final String ENTITYNAME = "useraccount";
        private VersionEntity versionEntity = null;

        public static final String USERNAME = "username";
        public static final String ENCRYPTEDPASSWORD = "encyptedpassword";
        public static final String REGISTTIME = "registtime";
        public static final String IP = "ip";

        public UserAccount(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public UserAccount(String username) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(USERNAME, username);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getId() {
            return versionEntity.getId();
        }

        public void setId(String id) {
            versionEntity.setId(id);
        }

        public String getUserName() {
            return (String)versionEntity.getAttribute(USERNAME);
        }

        public void setUserName(String username) {
            versionEntity.setAttribute(USERNAME, username);
        }

        public String getEncryptedPassword() {
            return (String)versionEntity.getAttribute(ENCRYPTEDPASSWORD);
        }

        public void setEncryptedPassword(String encryptedPassword) {
            versionEntity.setAttribute(ENCRYPTEDPASSWORD, encryptedPassword);
        }

        public Date getRegistTime() {
            return (Date)versionEntity.getAttribute(REGISTTIME);
        }

        public void setRegistTime(Date registTime) {
            versionEntity.setAttribute(REGISTTIME, registTime);
        }

        public String getIP() {
            return (String)versionEntity.getAttribute(IP);
        }

        public void setIP(String ip) {
            versionEntity.setAttribute(IP, ip);
        }
    }

    public static class LoginSession {
        public static final String ENTITYNAME = "loginsession";
        private VersionEntity versionEntity = null;

        public static final String SESSION = "session";
        public static final String ACCOUNTID = "accountid";
        public static final String EXPIRETIME = "expiretime";
        public static final String IP = "ip";
        public static final String ISDELETED = "isdeleted";

        public LoginSession(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public LoginSession(String session) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(SESSION, session);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getId() {
            return versionEntity.getId();
        }

        public void setId(String id) {
            versionEntity.setId(id);
        }

        public String getSession() {
            return (String)versionEntity.getAttribute(SESSION);
        }

        public void setSession(String session) {
            versionEntity.setAttribute(SESSION, session);
        }

        public String getAccountId() {
            return (String)versionEntity.getAttribute(ACCOUNTID);
        }

        public void setAccountId(String accountId) {
            versionEntity.setAttribute(ACCOUNTID, accountId);
        }

        public Date getExpireTime() {
            return (Date)versionEntity.getAttribute(EXPIRETIME);
        }

        public void setExpireTime(Date expireTime) {
            versionEntity.setAttribute(EXPIRETIME, expireTime);
        }

        public String getIP() {
            return (String)versionEntity.getAttribute(IP);
        }

        public void setIP(String ip) {
            versionEntity.setAttribute(IP, ip);
        }

        public boolean getIsDeleted() {
            return (boolean)versionEntity.getAttribute(ISDELETED);
        }

        public void setIsDeleted(boolean inputIsDeleted) {
            versionEntity.setAttribute(ISDELETED, inputIsDeleted);
        }
    }

    public static class ChasedCredits {
        public static final String ENTITYNAME = "chasedcredits";
        private VersionEntity versionEntity = null;

        public static final String ACCOUNTID = "accountid";
        public static final String CREDITS = "credits";
        public static final String EXPIRETIME = "expiretime";

        public ChasedCredits(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public ChasedCredits(String accountId) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(ACCOUNTID, accountId);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getId() {
            return versionEntity.getId();
        }

        public void setId(String id) {
            versionEntity.setId(id);
        }

        public String getAccountId() {
            return (String)versionEntity.getAttribute(ACCOUNTID);
        }

        public void setAccountId(String accountId) {
            versionEntity.setAttribute(ACCOUNTID, accountId);
        }

        public int getCredits() {
            return (int)versionEntity.getAttribute(CREDITS);
        }

        public void setCredits(int credits) {
            versionEntity.setAttribute(CREDITS, credits);
        }

        public Date getExpireTime() {
            return (Date)versionEntity.getAttribute(EXPIRETIME);
        }

        public void setExpireTime(Date expireTime) {
            versionEntity.setAttribute(EXPIRETIME, expireTime);
        }
    }

    public static class ConsumedCredits {
        public static final String ENTITYNAME = "consumedcredits";
        private VersionEntity versionEntity = null;

        public static final String ACCOUNTID = "accountid";
        public static final String CREDITS = "credits";
        public static final String CONSUMETIME = "consumetime";
        public static final String CONSUMEFUNCTION = "consumefunction";

        public ConsumedCredits(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public ConsumedCredits(String accountId) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(ACCOUNTID, accountId);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getId() {
            return versionEntity.getId();
        }

        public void setId(String id) {
            versionEntity.setId(id);
        }

        public String getAccountId() {
            return (String)versionEntity.getAttribute(ACCOUNTID);
        }

        public void setAccountId(String accountId) {
            versionEntity.setAttribute(ACCOUNTID, accountId);
        }

        public int getCredits() {
            return (int)versionEntity.getAttribute(CREDITS);
        }

        public void setCredits(int credits) {
            versionEntity.setAttribute(CREDITS, credits);
        }

        public Date getConsumeTime() {
            return (Date)versionEntity.getAttribute(CONSUMETIME);
        }

        public void setConsumeTime(Date consumeTime) {
            versionEntity.setAttribute(CONSUMETIME, consumeTime);
        }

        public String getConsumeFunction() {
            return (String)versionEntity.getAttribute(CONSUMEFUNCTION);
        }

        public void setConsumeFunction(String consumeFunction) {
            versionEntity.setAttribute(CONSUMEFUNCTION, consumeFunction);
        }
    }
}

