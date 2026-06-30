package com.trevorism.filter

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import org.junit.jupiter.api.Test

class CacheControlFilterTest {

    private static final String IMMUTABLE = "public, max-age=31536000, immutable"
    private static final String NO_STORE = "no-store, no-cache, must-revalidate"

    private final CacheControlFilter filter = new CacheControlFilter()

    @Test
    void testAssetsPathGetsImmutableCacheControl() {
        HttpRequest<?> request = HttpRequest.GET("/assets/app.123abc.js")
        MutableHttpResponse<?> response = HttpResponse.ok()

        filter.applyCacheControl(request, response)

        assert response.getHeaders().get(HttpHeaders.CACHE_CONTROL) == IMMUTABLE
    }

    @Test
    void testAssetsPathReplacesExistingCacheControl() {
        HttpRequest<?> request = HttpRequest.GET("/assets/styles.css")
        MutableHttpResponse<?> response = HttpResponse.ok()
        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-cache")

        filter.applyCacheControl(request, response)

        assert response.getHeaders().getAll(HttpHeaders.CACHE_CONTROL) == [IMMUTABLE]
    }

    @Test
    void testHtmlRequestGetsNoStoreCacheControl() {
        HttpRequest<?> request = HttpRequest.GET("/some/spa/route").accept(MediaType.TEXT_HTML)
        MutableHttpResponse<?> response = HttpResponse.ok()

        filter.applyCacheControl(request, response)

        assert response.getHeaders().get(HttpHeaders.CACHE_CONTROL) == NO_STORE
    }

    @Test
    void testHtmlRequestRemovesLastModifiedAndEtag() {
        HttpRequest<?> request = HttpRequest.GET("/").accept(MediaType.TEXT_HTML)
        MutableHttpResponse<?> response = HttpResponse.ok()
        response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Wed, 21 Oct 2015 07:28:00 GMT")
        response.getHeaders().add(HttpHeaders.ETAG, "\"abc123\"")

        filter.applyCacheControl(request, response)

        assert response.getHeaders().get(HttpHeaders.LAST_MODIFIED) == null
        assert response.getHeaders().get(HttpHeaders.ETAG) == null
    }

    @Test
    void testNonHtmlNonAssetRequestIsUntouched() {
        HttpRequest<?> request = HttpRequest.GET("/api/ping").accept(MediaType.APPLICATION_JSON)
        MutableHttpResponse<?> response = HttpResponse.ok()

        filter.applyCacheControl(request, response)

        assert response.getHeaders().get(HttpHeaders.CACHE_CONTROL) == null
    }
}
