package com.example.android.mcdaily;

public class NewsModel
{
    private String title;
    private String section;
    private String author;
    private String pubDate;
    private String url;

    public NewsModel(String title, String section, String author, String pubDate, String url)
    {
        this.title   = title;
        this.section = section;
        this.author  = author;
        this.pubDate = pubDate;
        this.url     = url;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSection()
    {
        return section;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getPubDate()
    {
        return pubDate;
    }

    public String getUrl()
    {
        return url;
    }
}
