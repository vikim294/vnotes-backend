package com.vikim.demo.note;

public record FindAllUsersAndDeletedRootNotesVO(
        Long userId,
        Long deletedRootId
) {
}
