package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.Group;

import java.util.List;

/**
 * 新建实体类Group，将id，name，count属性移动到Group实体中。本实体采用List封装Groups信息
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetGroupsResponse extends BaseResponse {

//    private Integer id;
//    private String  name;
//    private Integer count;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public Integer getCount() {
//        return count;
//    }
//
//    public void setCount(Integer count) {
//        this.count = count;
//    }

    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
