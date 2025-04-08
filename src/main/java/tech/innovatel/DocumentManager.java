package tech.innovatel;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> fakeDatabase = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document is null while saving");
        }
        String id = document.getId().trim();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            document.setId(id);
        }
        document = Document.builder()
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(document.getCreated())
                .build();
        fakeDatabase.put(id, document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return fakeDatabase.values().stream()
                .filter(document -> isTitleMatched(document, request))
                .filter(document -> isContentMatched(document, request))
                .filter(document -> isAuthorMatched(document, request))
                .filter(document -> isCreatedInRange(document, request))
                .collect(Collectors.toList());
    }

    private boolean isTitleMatched(Document document, SearchRequest request) {
        List<String> prefixes = request.getTitlePrefixes();
        return prefixes == null || prefixes.isEmpty()
                || (document.getTitle() != null && prefixes.stream()
                .filter(Objects::nonNull)
                .anyMatch(prefix -> document.getTitle().startsWith(prefix)));
    }

    private boolean isContentMatched(Document document, SearchRequest request) {
        List<String> contents = request.getContainsContents();
        return contents == null || contents.isEmpty()
                || (document.getContent() != null && contents.stream()
                .filter(Objects::nonNull)
                .anyMatch(substring -> document.getContent().contains(substring)));
    }

    private boolean isAuthorMatched(Document document, SearchRequest request) {
        List<String> authorIds = request.getAuthorIds();
        return authorIds == null || authorIds.isEmpty()
                || (document.getAuthor() != null && document.getAuthor().getId() != null &&
                authorIds.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(authorId -> document.getAuthor().getId().equals(authorId)));
    }

    private boolean isCreatedInRange(Document document, SearchRequest request) {
        Instant created = document.getCreated();
        Instant from = request.getCreatedFrom();
        Instant to = request.getCreatedTo();
        return (from == null || (created != null && !created.isBefore(from)))
                && (to == null || (created != null && !created.isAfter(to)));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        id = id.trim();
        return Optional.ofNullable(fakeDatabase.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}