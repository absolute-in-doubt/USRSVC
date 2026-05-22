package com.innowise.userservice.infrastructure.persistence.specification;

import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.domain.model.PaymentCard;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> withUserFirstName(String firstName){
        return (root, query, cb) -> (firstName == null)? null :
                cb.equal(root.join("user", JoinType.INNER).get("firstName"), firstName);
    }

    public static Specification<PaymentCard> withUserLastName(String lastName){
        return (root, query, cb) -> (lastName == null)? null :
                cb.equal(root.join("user", JoinType.INNER).get("lastName"), lastName);
    }


    public static Specification<PaymentCard> fromUserFilter(PaymentCardFilter paymentCardFilter){
        return  Specification.allOf(
                withUserFirstName(paymentCardFilter.userFirstName()),
                withUserLastName(paymentCardFilter.userLastName())
        );
    }
}
