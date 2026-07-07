package com.kiniot.uflex.api.media.domain.services;

import com.kiniot.uflex.api.media.domain.model.commands.ConfirmMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.commands.CreateMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.commands.DeleteMediaAssetCommand;
import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.results.MediaUploadTicket;

public interface MediaAssetCommandService {
    MediaUploadTicket handle(CreateMediaUploadCommand command);
    MediaAsset handle(ConfirmMediaUploadCommand command);
    void handle(DeleteMediaAssetCommand command);
}
