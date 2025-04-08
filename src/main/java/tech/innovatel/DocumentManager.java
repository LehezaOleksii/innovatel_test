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

    private boolean isTitleMatched(Document document, SearchRequest request) {
        List<String> prefixes = request.getTitlePrefixes();
        if (prefixes == null || prefixes.isEmpty()) {
            return true;
        }
        String title = document.getTitle();
        if (title == null) {
            return false;
        }
        return prefixes.stream()
                .filter(Objects::nonNull)
                .anyMatch(title::startsWith);
    }


    private boolean isContentMatched(Document document, SearchRequest request) {
        List<String> contents = request.getContainsContents();
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        String content = document.getContent();
        if (content == null) {
            return false;
        }
        return contents.stream()
                .filter(Objects::nonNull)
                .anyMatch(content::contains);
    }


    private boolean isAuthorMatched(Document document, SearchRequest request) {
        List<String> authorIds = request.getAuthorIds();
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        Author author = document.getAuthor();
        String authorId = author != null ? author.getId() : null;
        if (authorId == null) {
            return false;
        }
        return authorIds.stream()
                .filter(Objects::nonNull)
                .anyMatch(authorId::equals);
    }

    private boolean isCreatedInRange(Document doc, SearchRequest request) {
        Instant created = doc.getCreated();
        Instant from = request.getCreatedFrom();
        Instant to = request.getCreatedTo();

        boolean isAfterFrom = from == null || (created != null && !created.isBefore(from));
        boolean isBeforeTo = to == null || (created != null && !created.isAfter(to));

        return isAfterFrom && isBeforeTo;
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