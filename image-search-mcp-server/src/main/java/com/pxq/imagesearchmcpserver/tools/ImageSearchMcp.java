package com.pxq.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pexels 图片搜索工具（MCP）。
 *
 * <p>封装对 {@code GET https://api.pexels.com/v1/search} 接口的调用，
 * 使用 Hutool 的 {@link HttpRequest} 发起请求并解析返回的 JSON。
 *
 * <p>调用方只需传入用户的搜索关键词 {@code query}，其余参数（apiKey、分页、方向等）
 * 均从 {@code application.yml} 的 {@code pexels} 配置项中读取。
 *
 * <p>接口文档：<a href="https://www.pexels.com/api/documentation/#photos-search">Pexels API - Search Photos</a>
 */
@Slf4j
@Component
public class ImageSearchMcp {

    /** Pexels API 基础地址 */
    private static final String PEXELS_API_BASE = "https://api.pexels.com/v1/search";

    /** 授权请求头名称 */
    private static final String AUTH_HEADER = "Authorization";

    /** 单次请求最大返回数量（Pexels 上限为 80） */
    private static final int MAX_PER_PAGE = 80;

    /** Pexels API Key（从配置文件读取） */
    @Value("${pexels.api-key:}")
    private String apiKey;

    /** 每页数量 */
    @Value("${pexels.per-page:15}")
    private int perPage;

    /** 搜索方向：landscape / portrait / square */
    @Value("${pexels.orientation:}")
    private String orientation;

    /** 搜索尺寸：large / medium / small */
    @Value("${pexels.size:}")
    private String size;

    /** 颜色过滤 */
    @Value("${pexels.color:}")
    private String color;

    /** 语言区域 */
    @Value("${pexels.locale:zh-CN}")
    private String locale;

    /**
     * 根据用户输入的关键词搜索图片。
     *
     * @param query 用户的搜索关键词（必填）
     * @return 解析后的图片结果列表，请求失败或参数缺失时返回空列表
     */
    public List<PexelsPhoto> search(String query) {
        if (StrUtil.isBlank(apiKey)) {
            log.error("Pexels API Key 未配置，请在 application.yml 中设置 pexels.api-key");
            return new ArrayList<>();
        }
        if (StrUtil.isBlank(query)) {
            log.warn("search 调用失败：query 不能为空");
            return new ArrayList<>();
        }

        int safePerPage = Math.max(1, Math.min(perPage, MAX_PER_PAGE));

        try {
            HttpRequest request = HttpRequest.get(PEXELS_API_BASE)
                    .header(AUTH_HEADER, apiKey)
                    .form("query", query)
                    .form("page", 1)
                    .form("per_page", safePerPage);

            if (StrUtil.isNotBlank(orientation)) {
                request.form("orientation", orientation);
            }
            if (StrUtil.isNotBlank(size)) {
                request.form("size", size);
            }
            if (StrUtil.isNotBlank(color)) {
                request.form("color", color);
            }
            if (StrUtil.isNotBlank(locale)) {
                request.form("locale", locale);
            }

            HttpResponse response = request.execute();
            if (!response.isOk()) {
                log.error("Pexels 搜索请求失败，状态码：{}，响应体：{}",
                        response.getStatus(), response.body());
                return new ArrayList<>();
            }

            return parsePhotos(response.body());
        } catch (Exception e) {
            log.error("调用 Pexels 搜索接口发生异常，query={}", query, e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析 Pexels 返回 JSON 中的 photos 数组。
     *
     * <p>返回结构示例：
     * <pre>
     * {
     *   "total_results": 123,
     *   "page": 1,
     *   "per_page": 15,
     *   "photos": [ { "id":..., "url":..., "src":{ "original":..., "large2x":..., "large":..., "medium":..., "small":..., "portrait":..., "landscape":..., "tiny":... }, "alt":..., "photographer":... }, ... ]
     * }
     * </pre>
     */
    private List<PexelsPhoto> parsePhotos(String jsonBody) {
        List<PexelsPhoto> result = new ArrayList<>();
        if (StrUtil.isBlank(jsonBody) || !JSONUtil.isTypeJSON(jsonBody)) {
            return result;
        }

        JSONObject root = JSONUtil.parseObj(jsonBody);
        JSONArray photos = root.getJSONArray("photos");
        if (photos == null) {
            return result;
        }

        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = photos.getJSONObject(i);
            PexelsPhoto.PexelsPhotoBuilder builder = PexelsPhoto.builder()
                    .id(photo.getLong("id"))
                    .width(photo.getInt("width"))
                    .height(photo.getInt("height"))
                    .url(photo.getStr("url"))
                    .photographer(photo.getStr("photographer"))
                    .photographerUrl(photo.getStr("photographer_url"))
                    .alt(photo.getStr("alt"));

            JSONObject src = photo.getJSONObject("src");
            if (src != null) {
                builder.original(src.getStr("original"))
                        .large2x(src.getStr("large2x"))
                        .large(src.getStr("large"))
                        .medium(src.getStr("medium"))
                        .small(src.getStr("small"))
                        .portrait(src.getStr("portrait"))
                        .landscape(src.getStr("landscape"))
                        .tiny(src.getStr("tiny"));
            }

            result.add(builder.build());
        }
        return result;
    }

    /**
     * Pexels 单张图片的精简模型。
     */
    @Data
    @Builder
    public static class PexelsPhoto {
        /** 图片 ID */
        private Long id;
        /** 原图宽度 */
        private Integer width;
        /** 原图高度 */
        private Integer height;
        /** Pexels 页面地址 */
        private String url;
        /** 摄影师名称 */
        private String photographer;
        /** 摄影师主页 */
        private String photographerUrl;
        /** 图片替代文本 / 描述 */
        private String alt;

        // ─── 不同尺寸的图片 URL ───────────────────────
        private String original;
        private String large2x;
        private String large;
        private String medium;
        private String small;
        private String portrait;
        private String landscape;
        private String tiny;
    }

    /**
     * 搜索方向枚举（对应接口 orientation 参数）。
     */
    @Getter
    public enum Orientation {
        LANDSCAPE("landscape"),
        PORTRAIT("portrait"),
        SQUARE("square");

        private final String value;

        Orientation(String value) {
            this.value = value;
        }
    }

    /**
     * 搜索尺寸枚举（对应接口 size 参数）。
     */
    @Getter
    public enum Size {
        LARGE("large"),
        MEDIUM("medium"),
        SMALL("small");

        private final String value;

        Size(String value) {
            this.value = value;
        }
    }
}
