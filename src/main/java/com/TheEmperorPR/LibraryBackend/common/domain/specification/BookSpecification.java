package com.TheEmperorPR.LibraryBackend.common.domain.specification;

import com.TheEmperorPR.LibraryBackend.common.database.dto.BookFilterCriteria;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> withCriteria(BookFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.conjunction();
            }

            if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
                String pattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("bookName")), pattern);
                Predicate authorLike = cb.like(cb.lower(root.get("authorName")), pattern);
                predicates.add(cb.or(titleLike, authorLike));
            }

            if (criteria.getTitle() != null && !criteria.getTitle().isBlank()) {
                String pattern = "%" + criteria.getTitle().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("bookName")), pattern));
            }

            if (criteria.getAuthor() != null && !criteria.getAuthor().isBlank()) {
                String pattern = "%" + criteria.getAuthor().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("authorName")), pattern));
            }

            if (criteria.getDomain() != null && !criteria.getDomain().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("domain")), criteria.getDomain().trim().toLowerCase()));
            }

            if (criteria.getIsbn() != null && !criteria.getIsbn().isBlank()) {
                predicates.add(cb.equal(root.get("isbn"), criteria.getIsbn().trim()));
            }

            if (Boolean.TRUE.equals(criteria.getAvailableOnly())) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
