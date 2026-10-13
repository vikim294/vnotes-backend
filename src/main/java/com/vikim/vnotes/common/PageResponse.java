package com.vikim.vnotes.common;

import java.util.List;

public record PageResponse<T>(
        List<T> list,
        int page,
        int pageSize,
        int total
) {

}
