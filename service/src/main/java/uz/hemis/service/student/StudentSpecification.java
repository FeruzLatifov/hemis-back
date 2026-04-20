package uz.hemis.service.student;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.hemis.domain.entity.student.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA Specification for dynamic Student queries
 *
 * <p>Builds WHERE clause dynamically based on provided filter parameters.</p>
 * <p>All filters are optional — null values are skipped.</p>
 * <p>Multi-value filters (comma-separated) use IN clause.</p>
 *
 * @since 2.0.0
 */
public final class StudentSpecification {

    private StudentSpecification() {
    }

    public static Specification<Student> withFilters(
            String q,
            String university,
            String educationType,
            String paymentForm,
            String studentStatus,
            String course,
            String faculty,
            String educationForm,
            String educationYear,
            String gender
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("lastname")), pattern),
                        cb.like(cb.lower(root.get("firstname")), pattern),
                        cb.like(cb.lower(root.get("fathername")), pattern),
                        cb.like(root.get("code"), pattern),
                        cb.like(root.get("pinfl"), pattern)
                ));
            }

            addFilter(predicates, root, cb, "university", university);
            addFilter(predicates, root, cb, "educationType", educationType);
            addFilter(predicates, root, cb, "paymentForm", paymentForm);
            addFilter(predicates, root, cb, "studentStatus", studentStatus);
            addFilter(predicates, root, cb, "course", course);
            addFilter(predicates, root, cb, "faculty", faculty);
            addFilter(predicates, root, cb, "educationForm", educationForm);
            addFilter(predicates, root, cb, "educationYear", educationYear);
            addFilter(predicates, root, cb, "gender", gender);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Add a filter predicate for a field. Supports comma-separated multi-values (IN clause).
     */
    private static void addFilter(
            List<Predicate> predicates,
            Root<Student> root,
            CriteriaBuilder cb,
            String field,
            String value
    ) {
        if (value == null || value.isBlank()) return;

        if (value.contains(",")) {
            List<String> values = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!values.isEmpty()) {
                predicates.add(root.get(field).in(values));
            }
        } else {
            predicates.add(cb.equal(root.get(field), value.trim()));
        }
    }
}
