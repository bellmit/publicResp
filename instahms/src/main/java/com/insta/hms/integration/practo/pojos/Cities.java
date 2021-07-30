package com.insta.hms.integration.practo.pojos;

public class Cities {
  private String id;

  private String promoted;

  private String name;

  private State state;

  private String published;

  private String ranking;

  private String modified_at;

  private String live;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPromoted() {
    return promoted;
  }

  public void setPromoted(String promoted) {
    this.promoted = promoted;
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

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public String getRanking() {
    return ranking;
  }

  public void setRanking(String ranking) {
    this.ranking = ranking;
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
    return "ClassPojo [id = " + id + ", promoted = " + promoted + ", name = " + name + ", state = "
        + state + ", published = " + published + ", ranking = " + ranking + ", modified_at = "
        + modified_at + ", live = " + live + "]";
  }
}
