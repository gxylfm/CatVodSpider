package com.github.catvod.spider;

import android.os.SystemClock;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Eighteen extends Spider {

    private final String url = "https://maa1815.com/zh/";

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Class> classes = new ArrayList<>();
        List<Vod> list = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(proxy(), url));
        for (Element a : doc.select("ul.animenu__nav > li > a")) {
            String typeName = a.text();
            String typeId = a.attr("href").replace(url, "");
            if (!typeId.contains("random/all/")) continue;
            if (typeName.contains("18H")) break;
            classes.add(new Class(typeId, typeName));
        }
        for (Element div : doc.select("div.post")) {
            String id = div.select("a").attr("href").replace(url, "");
            String name = div.select("h3").text();
            String pic = div.select("a > img").attr("src");
            String remark = div.select("div.meta").text();
            list.add(new Vod(id, name, pic, remark));
        }
        return Result.string(classes, list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        tid = tid.replace("random", "list");
        tid = tid.replace("index", pg);
        Document doc = Jsoup.parse(OkHttp.string(proxy(), url + tid));
        for (Element div : doc.select("div.post")) {
            String id = div.select("a").attr("href").replace(url, "");
            String name = div.select("h3").text();
            String pic = div.select("a > img").attr("src");
            String remark = div.select("div.meta").text();
            list.add(new Vod(id, name, pic, remark));
        }
        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(proxy(), url + ids.get(0)));
        Element wrap = doc.select("div.video-wrap").get(0);
        String name = wrap.select("div.archive-title > h1").text();
        String pic = wrap.select("div.player-wrap > img").attr("src");
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodName(name);
        vod.setVodPlayFrom("18AV");
        vod.setVodPlayUrl("播放$" + ids.get(0));
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return searchContent(key, pg);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        HashMap<String, String> result = new HashMap<>();
        Utils.loadWebView(url + id, getClient(result));
        while (result.isEmpty()) SystemClock.sleep(10);
        return Result.get().url(result.get("url")).string();
    }

    @Override
    public void destroy() {
        OkHttp.get().resetProxy();
    }

    private String searchContent(String key, String pg) {
        HashMap<String, String> params = new HashMap<>();
        params.put("search_keyword", key);
        params.put("search_type", "fc");
        params.put("op", "search");
        String res = OkHttp.post(proxy(), url + "searchform_search/all/" + pg + ".html", params);
        List<Vod> list = new ArrayList<>();
        for (Element div : Jsoup.parse(res).select("div.post")) {
            String id = div.select("a").attr("href").replace(url, "");
            String name = div.select("h3").text();
            String pic = div.select("a > img").attr("src");
            String remark = div.select("div.meta").text();
            list.add(new Vod(id, name, pic, remark));
        }
        return Result.string(list);
    }

    private WebViewClient getClient(HashMap<String, String> result) {
        return new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.endsWith(".m3u8")) {
                    result.put("url", url);
                    view.destroy();
                }
            }
        };
    }
}
