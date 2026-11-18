package com.fan.screenadmin.content;

import java.math.BigDecimal;
import java.util.List;

public final class ContentDtos {

    private ContentDtos() {
    }

    public record PageResponse<T>(
            List<T> items,
            long total,
            int page,
            int pageSize
    ) {
    }

    public record WorkResponse(
            String id,
            String title,
            String cover,
            String desc,
            List<String> tags,
            int year
    ) {
    }

    public record WorkDetailResponse(
            String id,
            String title,
            String cover,
            String desc,
            List<String> tags,
            int year,
            String role,
            String period,
            String projectUrl,
            List<String> techStack,
            String content,
            String result,
            List<String> images,
            long viewCount,
            long visitorCount,
            List<RelatedContentResponse> related
    ) {
    }

    public record RelatedContentResponse(
            String id,
            String title,
            String cover,
            String contentType
    ) {
    }

    public record WorkRequest(
            String title,
            String cover,
            String desc,
            List<String> tags,
            Integer year,
            String role,
            String period,
            String projectUrl,
            List<String> techStack,
            String content,
            String result,
            List<String> images
    ) {
    }

    public record ComicResponse(
            String id,
            String title,
            String cover,
            String status,
            int chapters,
            String desc
    ) {
    }

    public record ComicChapterResponse(
            String id,
            String comicId,
            int chapterNo,
            String title,
            String summary,
            List<String> pages,
            String date
    ) {
    }

    public record ComicChapterRequest(
            Integer chapterNo,
            String title,
            String summary,
            List<String> pages,
            String date
    ) {
    }

    public record ComicDetailResponse(
            String id,
            String title,
            String cover,
            String status,
            int chapters,
            String desc,
            List<ComicChapterResponse> chapterList,
            long viewCount,
            long visitorCount,
            List<RelatedContentResponse> related
    ) {
    }

    public record ComicRequest(
            String title,
            String cover,
            String status,
            Integer chapters,
            String desc
    ) {
    }

    public record MovieResponse(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String note,
            String watchDate,
            String director,
            List<String> castNames,
            String review,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue
    ) {
    }

    public record MovieRequest(
            String title,
            String cover,
            String playUrl,
            Integer year,
            BigDecimal rating,
            String genre,
            String note,
            String watchDate,
            String director,
            List<String> castNames,
            String review,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue
    ) {
    }

    public record MovieDetailResponse(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String note,
            String watchDate,
            String director,
            List<String> castNames,
            String review,
            Integer storyScore,
            Integer visualScore,
            Integer soundScore,
            Integer rewatchValue,
            long viewCount,
            long visitorCount,
            List<RelatedContentResponse> related
    ) {
    }

    public record AnimeResponse(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String status,
            int episodes,
            String note,
            String review
    ) {
    }

    public record AnimeRequest(
            String title,
            String cover,
            String playUrl,
            Integer year,
            BigDecimal rating,
            String genre,
            String status,
            Integer episodes,
            String note,
            String review
    ) {
    }

    public record AnimeDetailResponse(
            String id,
            String title,
            String cover,
            String playUrl,
            int year,
            BigDecimal rating,
            String genre,
            String status,
            int episodes,
            String note,
            String review,
            long viewCount,
            long visitorCount,
            List<RelatedContentResponse> related
    ) {
    }

    public record ArticleSummaryResponse(
            String id,
            String title,
            String desc,
            List<String> tags,
            String date,
            String status,
            String scheduledAt,
            long viewCount
    ) {
    }

    public record ArticleDetailResponse(
            String id,
            String title,
            String desc,
            List<String> tags,
            String date,
            String status,
            String content,
            long viewCount,
            long visitorCount,
            ArticleSummaryResponse previous,
            ArticleSummaryResponse next,
            List<ArticleSummaryResponse> related
    ) {
    }

    public record ArticleRequest(
            String title,
            String desc,
            List<String> tags,
            String date,
            String content,
            String status,
            String scheduledAt
    ) {
    }

    public record ContentFacetResponse(
            List<String> workTags,
            List<Integer> workYears,
            List<String> comicStatuses,
            List<String> movieGenres,
            List<Integer> movieYears,
            List<String> animeGenres,
            List<Integer> animeYears,
            List<String> articleTags,
            List<Integer> articleYears
    ) {
    }

    public record HomeFeatureResponse(
            String title,
            String desc,
            String image
    ) {
    }

    public record HomeConfigResponse(
            String platformName,
            String heroBadge,
            String heroTitle,
            String heroDescription,
            String heroVideoUrl,
            String heroPrimaryButtonText,
            String heroSecondaryButtonText,
            String introTitle,
            String introDescription,
            String introBackgroundUrl,
            String featureTitle,
            List<HomeFeatureResponse> features,
            String contactTitle,
            String contactDescription,
            String contactPhone,
            String footerText
    ) {
    }

    public record HomeConfigRequest(
            String platformName,
            String heroBadge,
            String heroTitle,
            String heroDescription,
            String heroVideoUrl,
            String heroPrimaryButtonText,
            String heroSecondaryButtonText,
            String introTitle,
            String introDescription,
            String introBackgroundUrl,
            String featureTitle,
            List<HomeFeatureRequest> features,
            String contactTitle,
            String contactDescription,
            String contactPhone,
            String footerText
    ) {
    }

    public record HomeFeatureRequest(
            String title,
            String desc,
            String image
    ) {
    }

    public record ContactMessageRequest(
            String name,
            String contact,
            String content
    ) {
    }

    public record ContactMessageResponse(
            String id,
            String name,
            String contact,
            String content,
            boolean handled,
            String createdAt
    ) {

    }

    public record CommentRequest(
            String authorName,
            String body
    ) {
    }

    public record CommentResponse(
            String id,
            String contentType,
            String contentId,
            String authorName,
            String body,
            String status,
            String createdAt
    ) {
    }

    public record ReadingProgressRequest(
            String chapterId,
            Integer chapterNo
    ) {
    }

    public record ReadingProgressResponse(
            String comicId,
            String chapterId,
            Integer chapterNo,
            String updatedAt
    ) {
    }

    public record GlobalSearchResponse(
            List<WorkResponse> works,
            List<ComicResponse> comics,
            List<MovieResponse> movies,
            List<AnimeResponse> anime,
            List<ArticleSummaryResponse> articles,
            long total
    ) {
    }

    public record DashboardResponse(
            long workCount,
            long comicCount,
            long movieCount,
            long animeCount,
            long articleCount,
            long publishedArticleCount,
            long draftArticleCount,
            long articleViewCount,
            long unreadMessageCount,
            List<ArticleSummaryResponse> popularArticles,
            List<ContactMessageResponse> recentMessages
    ) {
    }
}
