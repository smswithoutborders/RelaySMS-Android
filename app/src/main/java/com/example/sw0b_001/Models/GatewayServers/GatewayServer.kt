package com.example.sw0b_001.Models.GatewayServers;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value={"url"}, unique = true)})
public class GatewayServer {


    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String publicKey;

    @NonNull
    private String url;

    @NonNull
    private String protocol;

    @NonNull
    private Integer port = 80;

    private String seedsUrl;

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSeedsUrl(String seedsUrl) {
        this.seedsUrl = seedsUrl;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public Integer getPort() {
        return this.port;
    }
    public String getUrl() {
        return this.url;
    }

    public String getSeedsUrl() {
        return this.seedsUrl;
    }

    public String getProtocol() {
        return this.protocol;
    }
    public long getId() {
        return this.id;
    }

    public GatewayServer(){}

    public String composeFullURL() {
        return this.protocol + "://" + this.url + ":" + this.port;
    }
}
