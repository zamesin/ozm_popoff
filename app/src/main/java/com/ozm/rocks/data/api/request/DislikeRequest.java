package com.ozm.rocks.data.api.request;

import java.util.List;

public class DislikeRequest {
    private List<Action> dislikes;

    public DislikeRequest(List<Action> likes) {
        this.dislikes = likes;
    }
}
