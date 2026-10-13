package com.vikim.vnotes.note;

import java.util.List;

public record BatchUpdateDetailNodesTransactionResult(
        BatchUpdateDetailNodesResponse response,
        List<String> deletedImageObjectKeys
) {
}
