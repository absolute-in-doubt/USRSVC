package com.innowise.userservice.infrastructure.persistence.specification;

import com.innowise.userservice.application.dto.UserFilter;
import com.innowise.userservice.domain.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> withFirstName(String firstName){
        return (root, query, cb) -> (firstName == null)? null :
                cb.equal(root.get("firstName"), firstName);
    }

    public static Specification<User> withLastName(String lastName){
        return (root, query, cb) -> (lastName == null)? null :
                cb.equal(root.get("lastName"), lastName);
    }


    public static Specification<User> fromUserFilter(UserFilter userFilter){
        return  Specification.allOf(
               withFirstName(userFilter.firstName()),
               withLastName(userFilter.lastName())
        );
    }
}
