package com.wangyang.common.bean;

/**
 * 消息统一返回模板
 */
public class Result {

    private Integer code;
    private String meg;
    private Object date;

    public Result(Integer code, String meg, Object date) {
        this.code = code;
        this.meg = meg;
        this.date = date;
    }

    public Result(Integer code, String meg) {
        this.code = code;
        this.meg = meg;
    }

    public Result() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMeg() {
        return meg;
    }

    public void setMeg(String meg) {
        this.meg = meg;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", meg='" + meg + '\'' +
                ", date=" + date +
                '}';
    }

}
