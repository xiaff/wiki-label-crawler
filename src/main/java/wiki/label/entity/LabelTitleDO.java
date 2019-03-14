package wiki.label.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "label_title")
public class LabelTitleDO {
  @Id
  private Long id;

  private String label;

  private String titleEn;

  private String titleEnLower;

  private String titleCn;

  @Temporal(value = TemporalType.TIMESTAMP)
  @CreationTimestamp
  private Date createTime;

  @Temporal(value = TemporalType.TIMESTAMP)
  @UpdateTimestamp
  private Date updateTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getTitleEn() {
    return titleEn;
  }

  public void setTitleEn(String titleEn) {
    this.titleEn = titleEn;
  }

  public String getTitleEnLower() {
    return titleEnLower;
  }

  public void setTitleEnLower(String titleEnLower) {
    this.titleEnLower = titleEnLower;
  }

  public String getTitleCn() {
    return titleCn;
  }

  public void setTitleCn(String titleCn) {
    this.titleCn = titleCn;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    return "LabelTitleDO{" +
        "id=" + id +
        ", label='" + label + '\'' +
        ", titleEn='" + titleEn + '\'' +
        ", titleEnLower='" + titleEnLower + '\'' +
        ", titleCn='" + titleCn + '\'' +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        '}';
  }
}
