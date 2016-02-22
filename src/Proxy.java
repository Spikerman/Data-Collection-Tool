import java.util.Date;

/**
 * Created by chenhao on 2/22/16.
 */
public class Proxy {
    private String ip;
    private String port;
    private String type;
    private String responseTime;
    private Date lastVerifyDate;

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getType() {
        return type;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public Date getLastVerifyDate() {
        return lastVerifyDate;
    }

    public Proxy(String ip, String port, String type, String responseTime, Date lastVerifyDate) {
        this.ip = ip;
        this.port = port;
        this.type = type;
        this.responseTime = responseTime;
        this.lastVerifyDate = lastVerifyDate;
    }

    public Proxy(String ip, String port, String type, String responseTime) {
        this.ip = ip;
        this.port = port;
        this.type = type;
        this.responseTime = responseTime;
    }
}
