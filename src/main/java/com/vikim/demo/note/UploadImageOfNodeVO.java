package com.vikim.demo.note;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Setter
public class UploadImageOfNodeVO {
    private UUID uuid;
    private String url;
    private Path target;
}
