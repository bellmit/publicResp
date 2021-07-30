package com.insta.hms.integration.practo.pojos;

public class City {
  private String id;

  private String name;

  private State state;

  private String created_at;

  private String published;

  private String zonal_ad_on_city_page;

  private String modified_at;

  private String live;

  private String ranking;

  public String getPromoted() {
    return promoted;
  }

  public void setPromoted(String promoted) {
    this.promoted = promoted;
  }

  private String promoted;

  public String getRanking() {
    return ranking;
  }

  public void setRanking(String ranking) {
    this.ranking = ranking;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public String getZonal_ad_on_city_page() {
    return zonal_ad_on_city_page;
  }

  public void setZonal_ad_on_city_page(String zonal_ad_on_city_page) {
    this.zonal_ad_on_city_page = zonal_ad_on_city_page;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  public String getLive() {
    return live;
  }

  public void setLive(String live) {
    this.live = live;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", name = " + name + ", state = " + state + ", created_at = "
        + created_at + ", published = " + published + ", zonal_ad_on_city_page = "
        + zonal_ad_on_city_page + ", modified_at = " + modified_at + ", live = " + live + "]";
  }
}
