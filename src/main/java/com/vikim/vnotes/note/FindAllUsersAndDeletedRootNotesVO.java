package com.vikim.vnotes.note;

public record FindAllUsersAndDeletedRootNotesVO(
        Long userId,
        Long deletedRootId
) {
}
