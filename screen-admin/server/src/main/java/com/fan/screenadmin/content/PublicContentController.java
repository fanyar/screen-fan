package com.fan.screenadmin.content;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicContentController {

    private final ContentService contentService;

    public PublicContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/works")
    public ContentDtos.PageResponse<ContentDtos.WorkResponse> works(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String tag,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize
    ) {
        return contentService.listWorks(keyword, tag, year, page, pageSize);
    }

    @GetMapping("/works/{id}")
    public ResponseEntity<ContentDtos.WorkDetailResponse> work(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        return ResponseEntity.of(contentService.findWork(id, visitorKey));
    }

    @GetMapping("/comics")
    public ContentDtos.PageResponse<ContentDtos.ComicResponse> comics(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize
    ) {
        return contentService.listComics(keyword, status, page, pageSize);
    }

    @GetMapping("/comics/{id}")
    public ResponseEntity<ContentDtos.ComicDetailResponse> comic(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        return ResponseEntity.of(contentService.findComic(id, visitorKey));
    }

    @GetMapping("/movies")
    public ContentDtos.PageResponse<ContentDtos.MovieResponse> movies(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize
    ) {
        return contentService.listMovies(keyword, genre, year, page, pageSize);
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<ContentDtos.MovieDetailResponse> movie(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        return ResponseEntity.of(contentService.findMovie(id, visitorKey));
    }

    @GetMapping("/anime")
    public ContentDtos.PageResponse<ContentDtos.AnimeResponse> anime(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize
    ) {
        return contentService.listAnime(keyword, genre, year, page, pageSize);
    }

    @GetMapping("/anime/{id}")
    public ResponseEntity<ContentDtos.AnimeDetailResponse> animeDetail(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        return ResponseEntity.of(contentService.findAnime(id, visitorKey));
    }

    @GetMapping("/articles")
    public ContentDtos.PageResponse<ContentDtos.ArticleSummaryResponse> articles(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String tag,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize
    ) {
        return contentService.listArticles(keyword, tag, year, page, pageSize);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ContentDtos.ArticleDetailResponse> article(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        return contentService.findPublishedArticle(id, visitorKey)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/home-config")
    public ContentDtos.HomeConfigResponse homeConfig() {
        return contentService.getHomeConfig();
    }

    @GetMapping("/content-facets")
    public ContentDtos.ContentFacetResponse contentFacets() {
        return contentService.listFacets();
    }

    @GetMapping("/search")
    public ContentDtos.GlobalSearchResponse search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        return contentService.search(keyword, limit);
    }

    @PostMapping("/contact-messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.ContactMessageResponse submitContactMessage(
            @RequestBody ContentDtos.ContactMessageRequest request
    ) {
        return contentService.createContactMessage(request);
    }

    @GetMapping("/{contentType}/{id}/comments")
    public List<ContentDtos.CommentResponse> comments(
            @PathVariable String contentType,
            @PathVariable String id
    ) {
        return contentService.listApprovedComments(contentType, id);
    }

    @PostMapping("/{contentType}/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.CommentResponse createComment(
            @PathVariable String contentType,
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey,
            @RequestBody ContentDtos.CommentRequest request
    ) {
        return contentService.createComment(contentType, id, visitorKey, request);
    }

    @GetMapping("/comics/{id}/progress")
    public ResponseEntity<ContentDtos.ReadingProgressResponse> readingProgress(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey
    ) {
        if (visitorKey == null || visitorKey.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        return contentService.getReadingProgress(visitorKey, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/comics/{id}/progress")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.ReadingProgressResponse saveReadingProgress(
            @PathVariable String id,
            @RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey,
            @RequestBody ContentDtos.ReadingProgressRequest request
    ) {
        return contentService.saveReadingProgress(visitorKey, id, request);
    }

    @GetMapping(value = "/rss", produces = "application/rss+xml;charset=UTF-8")
    public String rssFeed() {
        return contentService.generateRssFeed();
    }
}
