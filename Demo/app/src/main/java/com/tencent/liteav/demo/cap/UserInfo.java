package com.tencent.liteav.demo.cap;

public class UserInfo {
    public String user_id;
    public String device_id;
    public String mobile;
    public String pwd;
    public String c_time;
    public String app_last_login_time;
    public String ca_last_login_time;
    public String user_name;
    public String real_name;
    public String user_img;
    public String department;
    public String role;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("user_id=").append(user_id)
            .append(", device_id=").append(device_id)
            .append(", mobile=").append(mobile)
            .append(", pwd=").append(pwd)
            .append(", c_time=").append(c_time)
            .append(", app_last_login_time=").append(app_last_login_time)
            .append(", ca_last_login_time=").append(ca_last_login_time)
            .append(", user_name=").append(user_name)
            .append(", real_name=").append(real_name)
            .append(", user_img=").append(user_img)
            .append(", department=").append(department)
            .append(", role=").append(role);
        return sb.toString();
    }
}
