package com.util;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

public class UrlUtils {
    public static final XPath xPath = XPathFactory.newInstance().newXPath();

    public static Document read(String url){
        try {
            return Jsoup.connect(url)
                    .userAgent(UserAgent.getUserAgent())
                    .timeout(5_000)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static org.w3c.dom.Document cleanDom(String html){
        try {
            HtmlCleaner hc = new HtmlCleaner();
            TagNode tn = hc.clean(html);
            org.w3c.dom.Document dom = null;
            dom = new DomSerializer(new CleanerProperties()).createDOM(tn);
            return dom;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
