package com.fan.screenadmin.content;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    private final ContentService contentService;

    public AdminContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/works")
    public List<ContentDtos.WorkDetailResponse> works() {
        return contentService.listAllWorks();
    }

    @GetMapping("/articles")
    public List<ContentDtos.ArticleSummaryResponse> articles() {
        return contentService.listAllArticles();
    }

    @GetMapping("/contact-messages")
    public List<ContentDtos.ContactMessageResponse> contactMessages() {
        return contentService.listContactMessages();
    }

    @GetMapping("/dashboard")
    public ContentDtos.DashboardResponse dashboard() {
        return contentService.getDashboard();
    }

    @PostMapping("/works")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.WorkDetailResponse createWork(@RequestBody ContentDtos.WorkRequest request) {
        return contentService.createWork(request);
    }

    @GetMapping("/comics")
    public List<ContentDtos.ComicDetailResponse> comics() {
        return contentService.listAllComics();
    }

    @PutMapping("/works/{id}")
    public ContentDtos.WorkDetailResponse updateWork(@PathVariable String id, @RequestBody ContentDtos.WorkRequest request) {
        return contentService.updateWork(id, request);
    }

    @DeleteMapping("/works/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWork(@PathVariable String id) {
        contentService.deleteWork(id);
    }

    @PostMapping("/comics")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.ComicDetailResponse createComic(@RequestBody ContentDtos.ComicRequest request) {
        return contentService.createComic(request);
    }

    @GetMapping("/movies")
    public List<ContentDtos.MovieResponse> movies() {
        return contentService.listAllMovies();
    }

    @PutMapping("/comics/{id}")
    public ContentDtos.ComicDetailResponse updateComic(@PathVariable String id, @RequestBody ContentDtos.ComicRequest request) {
        return contentService.updateComic(id, request);
    }

    @PostMapping("/comics/{comicId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.ComicChapterResponse createComicChapter(
            @PathVariable String comicId,
            @RequestBody ContentDtos.ComicChapterRequest request
    ) {
        return contentService.createComicChapter(comicId, request);
    }

    @PutMapping("/comic-chapters/{id}")
    public ContentDtos.ComicChapterResponse updateComicChapter(
            @PathVariable String id,
            @RequestBody ContentDtos.ComicChapterRequest request
    ) {
        return contentService.updateComicChapter(id, request);
    }

    @DeleteMapping("/comic-chapters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComicChapter(@PathVariable String id) {
        contentService.deleteComicChapter(id);
    }

    @DeleteMapping("/comics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComic(@PathVariable String id) {
        contentService.deleteComic(id);
    }

    @PostMapping("/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.MovieResponse createMovie(@RequestBody ContentDtos.MovieRequest request) {
        return contentService.createMovie(request);
    }

    @PutMapping("/movies/{id}")
    public ContentDtos.MovieResponse updateMovie(@PathVariable String id, @RequestBody ContentDtos.MovieRequest request) {
        return contentService.updateMovie(id, request);
    }

    @DeleteMapping("/movies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable String id) {
        contentService.deleteMovie(id);
    }

    @GetMapping("/anime")
    public List<ContentDtos.AnimeResponse> anime() {
        return contentService.listAllAnime();
    }

    @PostMapping("/anime")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.AnimeResponse createAnime(@RequestBody ContentDtos.AnimeRequest request) {
        return contentService.createAnime(request);
    }

    @PutMapping("/anime/{id}")
    public ContentDtos.AnimeResponse updateAnime(@PathVariable String id, @RequestBody ContentDtos.AnimeRequest request) {
        return contentService.updateAnime(id, request);
    }

    @DeleteMapping("/anime/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnime(@PathVariable String id) {
        contentService.deleteAnime(id);
    }

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDtos.ArticleSummaryResponse createArticle(@RequestBody ContentDtos.ArticleRequest request) {
        return contentService.createArticle(request);
    }

    @PutMapping("/articles/{id}")
    public ContentDtos.ArticleSummaryResponse updateArticle(@PathVariable String id, @RequestBody ContentDtos.ArticleRequest request) {
        return contentService.updateArticle(id, request);
    }

    @DeleteMapping("/articles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable String id) {
        contentService.deleteArticle(id);
    }

    @PutMapping("/contact-messages/{id}/handled")
    public ContentDtos.ContactMessageResponse markContactMessageHandled(@PathVariable String id) {
        return contentService.markContactMessageHandled(id);
    }

    @DeleteMapping("/contact-messages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContactMessage(@PathVariable String id) {
        contentService.deleteContactMessage(id);
    }

    @GetMapping("/comments")
    public List<ContentDtos.CommentResponse> comments() {
        return contentService.listAllComments();
    }

    @PutMapping("/comments/{id}/status")
    public ContentDtos.CommentResponse updateCommentStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        return contentService.updateCommentStatus(id, body.get("status"));
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable String id) {
        contentService.deleteComment(id);
    }

    @PutMapping("/home-config")
    public ContentDtos.HomeConfigResponse saveHomeConfig(@RequestBody ContentDtos.HomeConfigRequest request) {
        return contentService.saveHomeConfig(request);
    }
}
