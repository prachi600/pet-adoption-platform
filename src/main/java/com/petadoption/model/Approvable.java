package com.petadoption.model;

/** Interface implemented by anything that goes through an approve / reject workflow. */
public interface Approvable {
    ApprovalStatus getStatus();

    void setStatus(ApprovalStatus status);

    default boolean isPending() {
        return getStatus() == ApprovalStatus.PENDING;
    }

    default void approve() {
        setStatus(ApprovalStatus.APPROVED);
    }

    default void reject() {
        setStatus(ApprovalStatus.REJECTED);
    }
}
