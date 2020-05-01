package kien.instanttranslator.translation;

public class LanguageModel {

  private String language;
  private int languageCode;
  private boolean isDownloaded;

  public LanguageModel(String language, int languageCode, boolean isDownloaded) {

    this.language = language;
    this.languageCode = languageCode;
    this.isDownloaded = isDownloaded;
  }

  public String getLanguage() { return language; }

  public void setLanguage(String language) { this.language = language; }

  public int getLanguageCode() { return languageCode; }

  public void setLanguageCode(int languageCode) { this.languageCode = languageCode; }

  public boolean isDownloaded() { return isDownloaded; }

  public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }
}
