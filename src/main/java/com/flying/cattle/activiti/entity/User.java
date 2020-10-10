package com.flying.cattle.activiti.entity;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;


import java.io.Serializable;

public class User implements Serializable {

    /**
     * 必须加序列化ID否则该类发生变化时，获取流程变量会获取失败(需要重新赋值流程参数)
     */
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(Integer id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public User() {
    }
}
