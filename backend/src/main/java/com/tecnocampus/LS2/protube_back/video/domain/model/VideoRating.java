package com.tecnocampus.LS2.protube_back.video.domain.model;

import java.math.BigDecimal;

public record VideoRating(
        String videoId,
        BigDecimal averageRating,
        long ratingCount,
        Integer userRating
) {
}
