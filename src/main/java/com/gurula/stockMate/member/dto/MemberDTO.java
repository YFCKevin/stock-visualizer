package com.gurula.stockMate.member.dto;

import com.gurula.stockMate.member.Provider;
import com.gurula.stockMate.oauth.Role;

import java.util.List;

public class MemberDTO {
    private String id;
    private String userId;  //line使用
    private String pictureUrl;  //line大頭貼
    private String coverName;   //上傳大頭貼
    private String name;
    private String email;
    private Role role;
    private Provider provider;
    private List<String> favorites; // 版面收藏
    private long createAt;
    private String suspendAt;

    public MemberDTO() {
    }

    public MemberDTO(String id, String userId, String pictureUrl, String coverName, String name, String email, Role role, Provider provider, List<String> favorites, long createAt, String suspendAt) {
        this.id = id;
        this.userId = userId;
        this.pictureUrl = pictureUrl;
        this.coverName = coverName;
        this.name = name;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.favorites = favorites;
        this.createAt = createAt;
        this.suspendAt = suspendAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getCoverName() {
        return coverName;
    }

    public void setCoverName(String coverName) {
        this.coverName = coverName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public String getSuspendAt() {
        return suspendAt;
    }

    public void setSuspendAt(String suspendAt) {
        this.suspendAt = suspendAt;
    }
}
