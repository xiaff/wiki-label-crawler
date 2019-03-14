package wiki.label.dto;

public class WikiLabelDTO {
  private String label;

  private String titleEn;

  private String titleEnLower;

  private String titleCn;

  public WikiLabelDTO(String label) {
    this.label = label;
  }

  public WikiLabelDTO(String label, String titleEn, String titleEnLower, String titleCn) {
    this.label = label;
    this.titleEn = titleEn;
    this.titleEnLower = titleEnLower;
    this.titleCn = titleCn;
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

  @Override
  public String toString() {
    return "WikiLabelDTO{" +
        "label='" + label + '\'' +
        ", titleEn='" + titleEn + '\'' +
        ", titleEnLower='" + titleEnLower + '\'' +
        ", titleCn='" + titleCn + '\'' +
        '}';
  }
}
